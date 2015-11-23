package main

import (
	"github.com/julienschmidt/httprouter"
	_ "github.com/go-sql-driver/mysql"
	"github.com/garyburd/redigo/redis"
	"database/sql"
	"net/http"
	"fmt"
	"log"
	"os"
	"time"
	"io/ioutil"
	"github.com/pquerna/ffjson/ffjson"
	"math/rand"
	"bytes"
	"strings"
	"encoding/json"
	"strconv"
)

var (
	AppHost   = os.Getenv("APP_HOST")
	AppPort   = os.Getenv("APP_PORT")
	DbHost    = os.Getenv("DB_HOST")
	DbPort    = os.Getenv("DB_PORT")
	DbName    = os.Getenv("DB_NAME")
	DbUser    = os.Getenv("DB_USER")
	DbPass    = os.Getenv("DB_PASS")
	RedisHost = os.Getenv("REDIS_HOST")
	RedisPort = os.Getenv("REDIS_PORT")
)

var rp *redis.Pool
var db *sql.DB

type User struct {
	Id int
	Name string
	Pass string
	Placed bool
}

type Food struct {
	Id int `json:"id"`
	Price float32 `json:"price"`
	Stock int `json:"stock"`
}

type Ofood struct {
	Id int `json:"food_id"`
	Count int `json:"count"`
}

type Cart struct {
	Id string
	AccessToken string
	FoodNum int
	Foods []Ofood
}

type Order struct {
	Id string `json:"id"`
	Items []Ofood `json:"items"`
	Total float32 `json:"total"`
}

var users map[string]User
var gfoods map[string]Food
var carts map[string]Cart
var orders map[string]Order


var foodArr []Food
var actkPool map[string]User

func loadAllUser() {
	rows, _:= db.Query("select * from user")
	users = make(map[string]User)
	var id int
	var name, pass string
	for rows.Next() {
		rows.Scan(&id, &name, &pass)
		users[name] = User{id, name, pass, false}
	}
}

func loadAllFood() {
	rows, _:= db.Query("select * from food")

	gfoods = make(map[string]Food)
	var id int
	var stock int
	var price float32

	for rows.Next() {
		rows.Scan(&id, &stock, &price)
		gfoods[strconv.Itoa(id)] = Food{id, price, stock}
		foodArr = append(foodArr, Food{id, price, stock})
	}
}

func NewAccessToken(size int) []byte {
	ikind, kinds, result := 3, [][]int{[]int{10, 48}, []int{26, 97}, []int{26, 97}}, make([]byte, size)
	rand.Seed(time.Now().UnixNano())
	for i :=0; i < size; i++ {
		ikind = rand.Intn(3)
		scope, base := kinds[ikind][0], kinds[ikind][1]
		result[i] = uint8(base+rand.Intn(scope))
	}
	return result
}

func main() {
//	runtime.GOMAXPROCS(runtime.NumCPU())

	actkPool = make(map[string]User)
	carts = make(map[string]Cart)
	orders = make(map[string]Order)

	rp = redis.NewPool(func() (redis.Conn, error) {
		return redis.Dial("tcp", RedisHost+":"+RedisPort)
	}, 1000)

	var url = DbUser + ":" + DbPass + "@tcp(" + DbHost + ":" + DbPort + ")/" + DbName
	db, _ = sql.Open("mysql", url)
	db.SetMaxOpenConns(2000)
	db.SetMaxIdleConns(1000)
	defer db.Close()

	go loadAllUser()
	go loadAllFood()

	router := httprouter.New()
	router.POST("/login", LoginController)
	router.GET("/foods", FoodController)
	router.POST("/carts", NewCartsController)
	router.PATCH("/carts/:cart_id", FoodsToCartController)
	router.POST("/orders", PlaceOrderController)
	router.GET("/orders", ShowOrdersController)
	router.GET("/admin/orders", AdminShowOrdersController)

	log.Fatal(http.ListenAndServe(AppHost+":"+AppPort, router))
}

func LoginController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	body, _ := ioutil.ReadAll(r.Body)

	if len(body) == 0 {
		w.WriteHeader(400)
		fmt.Fprintf(w, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`)
		return
	}

	type Body struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}
	var req Body
	err := ffjson.Unmarshal(body, &req)

	if err != nil {
		w.WriteHeader(400)
		fmt.Fprintf(w, `{"code": "MALFORMED_JSON", "message": "格式错误"}`)
		return
	}

	user := users[req.Username]
	if req.Password != user.Pass {
		w.WriteHeader(403)
		fmt.Fprintf(w, `{"code": "USER_AUTH_FAIL", "message": "用户名或密码错误"}`)
		return
	}

	accessToken := NewAccessToken(16)

	actkPool[string(accessToken)] = user

	w.WriteHeader(200)
	fmt.Fprintf(w, `{"user_id": %d, "username": "%s", "access_token": "%s"}`, user.Id, user.Name, accessToken)
}

func FoodController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	_, ok := actkPool[accessToken]

	// AccessToken is not in Cache
	if !ok {
		w.WriteHeader(401)
		fmt.Fprintf(w, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	json, _ := json.Marshal(foodArr)

	w.WriteHeader(200)
	fmt.Fprintf(w, string(json))
}

func NewCartsController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	// AccessToken is not in Cache
	if 	_, ok := actkPool[accessToken]; !ok {
		w.WriteHeader(401)
		fmt.Fprintf(w, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	cartId := NewAccessToken(16)

	cId := string(cartId)
	carts[cId] = Cart{cId, accessToken, 0, nil}

	w.WriteHeader(200)
	fmt.Fprintf(w, `{"cart_id": "%s"}`, cartId)
}

func FoodsToCartController(w http.ResponseWriter, r *http.Request, p httprouter.Params) {

	body, _ := ioutil.ReadAll(r.Body)
	if len(body) == 0 {
		w.WriteHeader(400)
		fmt.Fprintf(w, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`)
		return
	}

	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	// AccessToken is not in Cache
	if _, ok := actkPool[accessToken]; !ok {
		w.WriteHeader(401)
		fmt.Fprintf(w,`{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	cartId := p.ByName("cart_id")

	type Req struct {
		FoodId int `json:"food_id"`
		Count int `json:"count"`
	}
	var req Req
	err := ffjson.Unmarshal(body, &req)

	if err != nil {
		w.WriteHeader(400)
		fmt.Println(err.Error())
		fmt.Fprintf(w,`{"code": "MALFORMED_JSON", "message": "格式错误"}`)
		return
	}

	cart, ok := carts[cartId]

	if !ok {
		w.WriteHeader(404)
		fmt.Fprintf(w, `{"code": "CART_NOT_FOUND", "message": "篮子不存在"}`)
		return
	}

	if cart.AccessToken != accessToken {
		w.WriteHeader(401)
		fmt.Fprintf(w, `{"code": "NOT_AUTHORIZED_TO_ACCESS_CART", "message": "无权限访问指定的篮子"}`)
		return
	}

	if cart.FoodNum + req.Count > 3 {
		w.WriteHeader(403)
		fmt.Fprintf(w, `{"code": "FOOD_OUT_OF_LIMIT", "message": "篮子中食物数量超过了三个"}`)
		return
	}

	_, ok = gfoods[strconv.Itoa(req.FoodId)];
	if !ok {
		w.WriteHeader(404)
		fmt.Fprintf(w, `{"code": "FOOD_NOT_FOUND", "message": "食物不存在"}`)
		return
	}

	if !ok {
		w.WriteHeader(404)
		fmt.Fprintf(w, `{"code": "CART_NOT_FOUND", "message": "篮子不存在"}`)
		return
	}

	foodNum := cart.FoodNum + req.Count
	foods := append(cart.Foods, Ofood{req.FoodId, req.Count})
	carts[cartId] = Cart{ cartId, accessToken, foodNum, foods }

	w.WriteHeader(204)
}

func PlaceOrderController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	body, _ := ioutil.ReadAll(r.Body)
	if len(body) == 0 {
		w.WriteHeader(400)
		fmt.Fprintf(w, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`)
		return
	}

	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	// AccessToken is not in Cache
	if _, ok := actkPool[accessToken]; !ok {
		w.WriteHeader(401)
		fmt.Fprintf(w,`{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	type Req struct {
		CartId string `json:"cart_id"`
	}
	var req Req
	ffjson.Unmarshal(body, &req)

	cart, ok := carts[req.CartId]

	if !ok {
		w.WriteHeader(404)
		fmt.Fprintf(w, `{"code": "CART_NOT_FOUND", "message": "篮子不存在"}`)
		return
	}

	if cart.AccessToken != accessToken {
		w.WriteHeader(403)
		fmt.Fprint(w, `{"code": "NOT_AUTHORIZED_TO_ACCESS_CART", "message": "无权限访问指定的篮子"}`)
		return
	}

	if users[accessToken].Placed {
		w.WriteHeader(403)
		fmt.Fprintf(w, `{"code": "ORDER_OUT_OF_LIMIT", "message": "每个用户只能下一单"}`)
		return
	}

	u := users[accessToken]
	// Can place an order normally
	users[accessToken] = User{u.Id, u.Name, u.Pass, true}

	// If this food is available
	// Then reduce the stock number of foods.
	orderId := string(NewAccessToken(16))

	var foods []Ofood
	var total float32 = 0
	for _, v := range cart.Foods {

		food := gfoods[strconv.Itoa(v.Id)]
		if food.Stock < v.Count {
			w.WriteHeader(403)
			fmt.Fprint(w, `{"code": "FOOD_OUT_OF_STOCK", "message": "食物库存不足"}`)
			return
		} else {
			gfoods[strconv.Itoa(v.Id)] = Food{food.Id, food.Price, food.Stock - v.Count}
			foods = append(foods, Ofood{v.Id, v.Count})
			total = total + food.Price * float32(v.Count)
		}
	}

	orders[accessToken] = Order{orderId, foods, total}

	w.WriteHeader(200)
	fmt.Fprintf(w, `{"id": "%s"}`, orderId)
}

func ShowOrdersController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	//	body, _ := ioutil.ReadAll(r.Body)
	//	if len(body) == 0 {
	//		w.WriteHeader(400)
	//		fmt.Fprintf(w, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`)
	//		return
	//	}

	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	if len(accessToken) <= 0 {
		w.WriteHeader(401)
		fmt.Fprintf(w,`{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	order, ok := orders[accessToken]

	if !ok {
		w.WriteHeader(200)
		fmt.Fprintf(w, `{}`)
		return
	}

	var j bytes.Buffer

	json, _ := json.Marshal(order)

	j.WriteString("[")
	j.WriteString(string(json))
	j.WriteString("]")

	w.WriteHeader(200)
	fmt.Fprintf(w, j.String())
}

func AdminShowOrdersController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	if len(accessToken) <= 0 {
		w.WriteHeader(401)
		fmt.Fprintf(w,`{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	c := rp.Get()
	gkey := fmt.Sprintf(`order:%s`,accessToken)

	_, e := redis.String( c.Do("hget", gkey, "id") )

	if e != nil {
		w.WriteHeader(200)
		fmt.Fprintf(w, `{}`)
		return
	}

	rep, err := redis.Values(c.Do("hvals", gkey))

	if err != nil {
		fmt.Println("err:", err.Error())
	}

	var orderId, uId, items string
	var total float32
	redis.Scan(rep, &orderId, &uId, &items, &total);

	var json bytes.Buffer
	json.WriteString("[{")
	json.WriteString(fmt.Sprintf(`"id": "%s", "user_id": "%s"`, orderId, uId))

	foods_slice := strings.Split(items, ";")

	json.WriteString(`"items": [`)
	for _, e := range foods_slice {
		food := strings.Split(e, ",")
		if len(food) == 2 {
			fid := food[0]
			count := food[1]
			json.WriteString(fmt.Sprintf(`{"food_id": %s, "count": %s}`, fid, count))
		}
	}
	json.WriteString(fmt.Sprintf(`],"total": %f`, total))
	json.WriteString("}]")

	w.WriteHeader(200)
	fmt.Fprintf(w, json.String())
}
