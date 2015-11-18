package main

import (
	"github.com/garyburd/redigo/redis"
	"github.com/go-martini/martini"
	_ "github.com/go-sql-driver/mysql"
	"github.com/pquerna/ffjson/ffjson"

	"database/sql"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"runtime"
	"time"
	//	"strconv"
	"math/rand"
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

var db *sql.DB
var err error

type User struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
	Pass string `json:"password"`
}

type CUser struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
	Placed bool `json:"placed"`
}

type Food struct {
	Id int `json:"id"`
	Price float32 `json:"price"`
	Stock int `json:"stock"`
}

type CFood struct {
	Id int `json:"id"`
	Price float32 `json:"price"`
	Stock int `json:"stock"`
	Count int `json:"count"`
}

type Cart struct {
	AccessToken string `json:"access_token"`
	FoodNum int `json:"food_num"`
	Foods []CFood `json:"foods"`
}

func loadFood() {
	rows, _ := db.Query("SELECT * FROM food")
	defer rows.Close()

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	var id, stock int
	var price float32
	var list []Food
	num := 0
	for rows.Next() {
		_ = rows.Scan(&id, &stock, &price)
		food := Food{id, price, stock}
		f, err := ffjson.Marshal(food)
		_, err = c.Do("SET", id, f)
		if err != nil {
			fmt.Println("食物写入缓存失败")
		}
		list = append(list, food)
		num = num + 1
	}
	c.Do("SET", "food_num", num)
	ret, _ := ffjson.Marshal(list)
	c.Do("SET", "food_list", string(ret))
}

func loadUser() {
	rows, _ := db.Query("SELECT * FROM user")
	defer rows.Close()

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	var id int
	var name, password string
	for rows.Next() {
		_ = rows.Scan(&id, &name, &password)
		uu := User{id, name, password}
		f, err := ffjson.Marshal(uu)
		_, err = c.Do("SET", name, f)
		if err != nil {
			fmt.Println("用户写入缓存失败")
		}
	}
}

func main() {
	// Ensure all CPU used by HTTP Handler
	runtime.GOMAXPROCS(runtime.NumCPU())
	var url = DbUser + ":" + DbPass + "@tcp(" + DbHost + ":" + DbPort + ")/" + DbName
	db, err = sql.Open("mysql", url)
	db.SetMaxOpenConns(2000)
	db.SetMaxIdleConns(1000)

	defer db.Close()
	if err != nil {
		panic(err.Error())
	}

	go func() {
		loadFood()
		loadUser()
	}()

	m := martini.Classic()
	martini.Env = "production"
	// Use json response middleware
	m.Use(func(c martini.Context, w http.ResponseWriter) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		c.Next()
	})

//	m.Get("/", HomeController)
	// For testing
//	m.Get("/login", LoginController)
//	m.Get("/carts", NewCartsController)
//	m.Get("/carts/:cart_id", FoodsToCartController)

	// All Routers
	m.Post("/login", LoginController)
	m.Get("/foods", FoodController)
	m.Post("/carts", NewCartsController)
	m.Patch("/carts/:cart_id", FoodsToCartController)
	m.Post("/orders", PlaceOrderController)
	m.Get("/orders", ShowOrdersController)
	m.Get("/admin/orders", AdminShowOrdersController)

	m.RunOnAddr(AppHost + ":" + AppPort)
}

func LoginController(w http.ResponseWriter, r *http.Request) (int, string) {

	body, _ := ioutil.ReadAll(r.Body)

	if len(body) == 0 {
		return 400, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`
	}

	type Body struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}
	var req Body
	err = ffjson.Unmarshal(body, &req)

	if err != nil {
		return 400, `{"code": "MALFORMED_JSON", "message": "格式错误"}`
	}

//	var id int
//	var username, password string
//
//	rows := db.QueryRow("SELECT * FROM user WHERE name=? and password=?", req.Username, req.Password)
//	err = rows.Scan(&id, &username, &password)
//	if err != nil {
//		return 403, `{"code": "USER_AUTH_FAIL", "message": "用户名或密码错误"}`
//	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	uu, err := redis.Bytes(c.Do("GET", req.Username));
	var user User
	ffjson.Unmarshal(uu, &user)
	if user.Pass != req.Password {
		return 403, `{"code": "USER_AUTH_FAIL", "message": "用户名或密码错误"}`
	}

	accessToken := NewAccessToken(32)
	ret := fmt.Sprintf(`{"user_id": %d, "username": "%s", "access_token": "%s"}`, user.Id, user.Name, accessToken)

	go func() {
		cache := fmt.Sprintf(`{"id": %d, "name": "%s", "placed": false}`, user.Id, user.Name)
		// Writes to redis cache.
		_, err = c.Do("SET", accessToken, cache)
	}()

	if err != nil {
		return 500, `{"code": "CACHE_ERROR", "message": "写入缓存错误"}`
	}

	return 200, ret
}

// Show all foods in database.
func FoodController(w http.ResponseWriter, r *http.Request) (int, string) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
		if len(accessToken) < 0 {
			return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
		}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	_, err := redis.String(c.Do("GET", accessToken));

	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

//	rows, err := db.Query("SELECT * FROM food")
//	defer rows.Close()
//	if err != nil {
//		return 500, `{"code": "DB_ERROR", "message": "查询数据库错误"}`
//	}
//
//	var list []Food
//	var id, stock int
//	var price float32
//	for rows.Next() {
//		err := rows.Scan(&id, &stock, &price)
//		if err != nil {
//			return 500, `{"code": "DB_ERROR", "message": "查询数据库错误"}`
//		}
//		food := Food{id, price, stock}
//		go func() {
//			f, err := ffjson.Marshal(food)
//			_, err = c.Do("SET", id, f)
//			if err != nil {
//				fmt.Println("食物写入缓存失败")
//			}
//		}()
//		list = append(list, food)
//	}
	list, err := redis.String(c.Do("GET", "food_list"));
	return 200, list
}

// Create a new Cart.
func NewCartsController(w http.ResponseWriter, r *http.Request) (int, string) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
		if len(accessToken) < 0 {
			return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
		}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	_, err := redis.String(c.Do("GET", accessToken));

	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

	cartId := NewAccessToken(32)

	go func() {
		ret, _ := ffjson.Marshal(Cart{accessToken, 0, nil})
		_, err = c.Do("SET", cartId, ret)
	}()

	return 200, fmt.Sprintf(`{"cart_id": "%s"}`, cartId)
}

// Add food to cart.
func FoodsToCartController(w http.ResponseWriter, r *http.Request, p martini.Params) (int, string) {

	body, _ := ioutil.ReadAll(r.Body)
	if len(body) == 0 {
		return 400, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`
	}

	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
		if len(accessToken) < 0 {
			return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
		}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	_, err := redis.String(c.Do("GET", accessToken));

	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

	cartId := p["cart_id"]

	type Req struct {
		FoodId int `json:"food_id"`
		Count int `json:"count"`
	}
	var req Req
	err = ffjson.Unmarshal(body, &req)
	if err != nil {
		return 400, `{"code": "MALFORMED_JSON", "message": "格式错误"}`
	}

	ct, err := redis.Bytes(c.Do("GET", cartId));
	if err != nil {
		return 404, `{"code": "CART_NOT_FOUND", "message": "篮子不存在"}`
	}

	var cart Cart
	err = ffjson.Unmarshal(ct, &cart)
	if err != nil {
		return 404, `{"code": "CACHE_ERROR", "message": "解析JSON出错"}`
	}

	if cart.AccessToken != accessToken {
		return 401, `{"code": "NOT_AUTHORIZED_TO_ACCESS_CART", "message": "无权限访问指定的篮子"}`
	}

	if cart.FoodNum + req.Count > 3 {
		return 403, `{"code": "FOOD_OUT_OF_LIMIT", "message": "篮子中食物数量超过了三个"}`
	}

	// Start add food procedure
	var oFood Food
	var newFood CFood
	ceFood, err := redis.Bytes(c.Do("GET", req.FoodId));
	if err != nil {
		return 404, `{"code": "FOOD_NOT_FOUND", "message": "食物不存在"}`
	}

	err = ffjson.Unmarshal(ceFood, &oFood)

	cart.FoodNum += req.Count
	newFood.Id = oFood.Id
	newFood.Price = oFood.Price
	newFood.Stock = oFood.Stock
	newFood.Count = req.Count

	cart.Foods = append(cart.Foods, newFood)
	newCart, _ := ffjson.Marshal(cart)
	_, err = c.Do("SET", cartId, newCart)

	return 204, ""
}

// Place an order.
func PlaceOrderController(w http.ResponseWriter, r *http.Request, p martini.Params) (int, string) {
	body, _ := ioutil.ReadAll(r.Body)
		if len(body) == 0 {
			return 400, `{"code": "EMPTY_REQUEST", "message": "请求体为空"}`
		}

		r.ParseForm()
		var accessToken string
		if len(r.Form["access_token"]) > 0 {
			accessToken = r.Form["access_token"][0]
		} else {
			accessToken = r.Header.Get("Access-Token")
			if len(accessToken) < 0 {
				return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
			}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	_, err := redis.String(c.Do("GET", accessToken));

	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

	type Req struct {
		CartId string `json:"cart_id"`
	}
	var req Req
	err = ffjson.Unmarshal(body, &req)
	if err != nil {
		return 400, `{"code": "MALFORMED_JSON", "message": "格式错误"}` + err.Error()
	}

	ct, err := redis.Bytes(c.Do("GET", req.CartId));
	if err != nil {
		return 404, `{"code": "CART_NOT_FOUND", "message": "篮子不存在"}`
	}

	var cart Cart
	err = ffjson.Unmarshal(ct, &cart)
	if err != nil {
		return 404, `{"code": "CACHE_ERROR", "message": "解析JSON出错"}`
	}

	if cart.AccessToken != accessToken {
		return 403, `{"code": "NOT_AUTHORIZED_TO_ACCESS_CART", "message": "无权限访问指定的篮子"}`
	}

	u, err := redis.Bytes(c.Do("GET", accessToken));
	var user CUser
	err = ffjson.Unmarshal(u, &user)

	if user.Placed {
		return 403, `{"code": "ORDER_OUT_OF_LIMIT", "message": "每个用户只能下一单"}`
	}

	// Can place an order normally
	user.Placed = true
	mUser, err := ffjson.Marshal(user)
	_, err = c.Do("SET", accessToken, mUser)

	// If this food is available or out of stock.
	// Then reduce the stock number of foods.
	var order COrder
	orderId := string(NewAccessToken(32))
	order.Id = orderId
	for _, curFood := range cart.Foods {
		ceFood, err := redis.Bytes(c.Do("GET", curFood.Id));
		if err != nil {
			return 404, `{"code": "NO_FOOD_IN_CACHE_ERROR", "message": "食物不存在，缓存错误"}`
		}

		var food Food
		err = ffjson.Unmarshal(ceFood, &food)

		if curFood.Count <= food.Stock {
			order.Items = append( order.Items, CItem{curFood.Id, curFood.Price, curFood.Count} )
			food.Stock = food.Stock - curFood.Count
			newFood, _ := ffjson.Marshal(food)
			c.Do("SET", curFood.Id, newFood)
		} else {
			return 403, `{"code": "FOOD_OUT_OF_STOCK", "message": "食物库存不足"}`
		}

	}

	corder, _ := ffjson.Marshal(order)

	c.Do("SET", "order:"+accessToken, corder)
	return 200, fmt.Sprintf(`{"id": "%s"}`, orderId)
}

type CItem struct {
	FoodId int `json:"food_id"`
	Price float32 `json:"price"`
	Count int `json:"count"`
}

type COrder struct {
	Id string `json:"id"`
	Items []CItem `json:"items"`
}

type Item struct {
	FoodId int `json:"food_id"`
	Count int `json:"count"`
}

type Order struct {
	Id string `json:"id"`
	Items []Item `json:"items"`
	Total float32 `json:"total"`
}

type AdminOrder struct {
	Id string `json:"id"`
	UserId int `json:"user_id"`
	Items []Item `json:"items"`
	Total float32 `json:"total"`
}

// Display all orders of a user.
func ShowOrdersController(w http.ResponseWriter, r *http.Request, p martini.Params) (int, string) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
		if len(accessToken) < 0 {
			return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
		}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	_, err := redis.String(c.Do("GET", accessToken));

	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

	od, err := redis.Bytes(c.Do("GET", "order:"+accessToken));
	if err != nil {
		return 200, `{}`
	}

	var order COrder
	ffjson.Unmarshal(od, &order)

	var poder Order
	poder.Id = order.Id

	var total float32
	total = 0
	for _, item := range order.Items {
		total += float32(item.Count) * item.Price
		poder.Items = append(poder.Items, Item{item.FoodId, item.Count})
	}
	poder.Total = total

	jsonOrder, _ := ffjson.Marshal(poder)
	return 200, fmt.Sprintf("[%s]",jsonOrder)
}

// Display all orders of a user.
// Caution: Little bit differ from previous one.
func AdminShowOrdersController(w http.ResponseWriter, r *http.Request, p martini.Params) (int, string) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
		if len(accessToken) < 0 {
			return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
		}
	}

	c := InitRedis("tcp", RedisHost+":"+RedisPort)
	u, err := redis.Bytes(c.Do("GET", accessToken));

	var user User
	ffjson.Unmarshal(u, &user)
	// AccessToken is not in Cache
	if err != nil {
		return 401, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`
	}

	od, err := redis.Bytes(c.Do("GET", "order:"+accessToken));
	if err != nil {
		return 200, `{}`
	}

	var order COrder
	ffjson.Unmarshal(od, &order)

	var poder AdminOrder
	poder.Id = order.Id
	poder.UserId = user.Id

	var total float32
	total = 0
	for _, item := range order.Items {
		total += float32(item.Count) * item.Price
		poder.Items = append(poder.Items, Item{item.FoodId, item.Count})
	}
	poder.Total = total


	jsonOrder, _ := ffjson.Marshal(poder)
	return 200, fmt.Sprintf("[%s]",jsonOrder)
}

// Redis pool size
var MAX_POOL_SIZE = 20
var redisPool chan redis.Conn

func putRedis(conn redis.Conn) {
	if redisPool == nil {
		redisPool = make(chan redis.Conn, MAX_POOL_SIZE)
	}
	if len(redisPool) >= MAX_POOL_SIZE {
		conn.Close()
		return
	}
	redisPool <- conn
}
func InitRedis(network, address string) redis.Conn {
	// Buffer like Message Queue
	if len(redisPool) == 0 {
		// Length is 0，dim a redis.Conn MAX_POOL_SIZE channel
		redisPool = make(chan redis.Conn, MAX_POOL_SIZE)
		go func() {
			for i := 0; i < MAX_POOL_SIZE/2; i++ {
				c, err := redis.Dial(network, address)
				if err != nil {
					panic(err)
				}
				putRedis(c)
			}
		}()
	}
	return <-redisPool
}

// 随机字符串
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

//func HomeController(w http.ResponseWriter) string {
//
//	startTime := time.Now()
//	rows, err := db.Query("SELECT * FROM user")
//	if err != nil {
//		panic(err.Error())
//	}
//
//	defer rows.Close()
//
//	var id int
//	var name, pass string
//	var buffer bytes.Buffer
//
//	for rows.Next() {
//		err := rows.Scan(&id, &name, &pass)
//		// fmt.Println(content)
//		buffer.WriteString(name)
//		buffer.WriteString("\n")
//		if err != nil {
//			panic(err.Error())
//		}
//	}
//	msg := fmt.Sprintf("用时：%s", time.Now().Sub(startTime))
//	buffer.WriteString(msg)
//	b, err := ffjson.Marshal(buffer.String())
//	return string(b)
//}
