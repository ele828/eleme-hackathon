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
	AccessToken string
}

type Food struct {
	Id int `json:"id"`
	Price int `json:"price"`
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
	Total int `json:"total"`
}

var users map[string]User
var gfoods map[int]Food

func loadAllUser() {
	rows, _:= db.Query("select * from user")
	users = make(map[string]User)
	var id int
	var name, pass string
	for rows.Next() {
		rows.Scan(&id, &name, &pass)
		users[name] = User{id, name, pass, false, ""}
	}
}

func loadAllFood() {
	rows, _:= db.Query("select * from food")

	gfoods = make(map[int]Food)
	var id int
	var stock int
	var price int

	c := rp.Get()
	for rows.Next() {
		rows.Scan(&id, &stock, &price)
		gfoods[id] = Food{id, price, stock}
		c.Do("hset", "f", id, stock)
	}
}

func NewAccessToken(size int) string {
	ikind, kinds, result := 3, [][]int{[]int{10, 48}, []int{26, 97}, []int{26, 97}}, make([]byte, size)
	rand.Seed(time.Now().UnixNano())
	for i :=0; i < size; i++ {
		ikind = rand.Intn(3)
		scope, base := kinds[ikind][0], kinds[ikind][1]
		result[i] = uint8(base+rand.Intn(scope))
	}
	return string(result)
}

func main() {

	rp = redis.NewPool(func() (redis.Conn, error) {
		return redis.Dial("tcp", RedisHost+":"+RedisPort)
	}, 1000)

	var url = DbUser + ":" + DbPass + "@tcp(" + DbHost + ":" + DbPort + ")/" + DbName
	db, _ = sql.Open("mysql", url)
	defer db.Close()

	// Clear Redis
	go func() {
		c := rp.Get()
		c.Do("FLUSHALL")
	}()
	go loadAllUser()
	go loadAllFood()

//	var getScript = redis.NewScript(2, `return redis.call('set', KEYS[1], KEYS[2])`)
//	var getScript2 = redis.NewScript(1, `return redis.call('get', KEYS[1])`)
//
//	c := rp.Get()
//	reply, _ := getScript.Do(c, "foo", 100)
//	fmt.Println(reply)
//
//	r, _ := redis.Int(getScript2.Do(c, "foo"))
//	fmt.Println(r)


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

	go func() {
		c := rp.Get()
		c.Do("set", "u:"+accessToken, user.Id)
	}()

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

	lua := `if redis.call("get", "u:"..KEYS[1]) then
		  return redis.call("hgetall", "f")
		else
		  return {}
		end`
	var getScript = redis.NewScript(1, lua)

	c := rp.Get()
	reply, _ := redis.StringMap(getScript.Do(c, accessToken))
	//reply, _ := redis.Values(getScript.Do(c, accessToken))
	//fmt.Println("food", reply)

	if len(reply) == 0 {
		w.WriteHeader(401)
		fmt.Fprintf(w, `{"code": "INVALID_ACCESS_TOKEN", "message": "无效的令牌"}`)
		return
	}

	var json []string
	for i := 1; i <= len(gfoods); i++ {
		stock, _ := reply[strconv.Itoa(gfoods[i].Id)] //redis.Int(reply[i-1], nil)
		json = append(json, fmt.Sprintf(`{"id": %d, "price": %d, "stock": %s}`, gfoods[i].Id, gfoods[i].Price, stock))
	}
	jsonAry := strings.Join(json, ",")
	w.WriteHeader(200)
	fmt.Fprintf(w, fmt.Sprintf(`[%s]`, jsonAry))
}

func NewCartsController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
	r.ParseForm()
	var accessToken string
	if len(r.Form["access_token"]) > 0 {
		accessToken = r.Form["access_token"][0]
	} else {
		accessToken = r.Header.Get("Access-Token")
	}

	cartId := NewAccessToken(16)
	cId := cartId

	go func() {
		c := rp.Get()
		c.Do("set", "c:"+cId, accessToken)
		c.Do("set", "c:fn:"+cId, 0)
	}()

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

	lua := `local cart_id = KEYS[1]
		local access_token = KEYS[2]
		local food_id = KEYS[3]
		local qty = tonumber(KEYS[4])
		local cart = redis.call("get", "c:"..cart_id)
		local size = redis.call("get", "c:fn:"..cart_id)
		if size then
		  size = tonumber(size)
		else
		  size = 0
		end
		if cart then
		  if cart == access_token then
		    size = size + qty
		    if size <= 3 then
		      local f = redis.call("hget", "f", food_id)
		      if f then
		        local original = redis.call("hget", "c:f:"..cart_id, food_id)
			if original then
			  qty = tonumber(original) + qty
		        end
		        redis.call("hset", "c:f:"..cart_id, food_id, qty)
		        redis.call("set", "c:fn:"..cart_id, size)
		        return {204, ""}
		      else
		        return {404, "{\"code\": \"FOOD_NOT_FOUND\",\"message\":\"食物不存在\"}"}
		      end
		    else
		      return {403, "{\"code\": \"FOOD_OUT_OF_LIMIT\",\"message\":\"篮子中食物数量超过了三个\"}"}
		    end
		  else
		    return {401, "{\"code\": \"NOT_AUTHORIZED_TO_ACCESS_CART\",\"message\":\"无权限访问指定的篮子\"}"}
		  end
		else
		  return {404, "{\"code\": \"CART_NOT_FOUND\",\"message\":\"篮子不存在\"}"}
		end`
	var getScript = redis.NewScript(4, lua)
	c := rp.Get()
	reply, err := redis.Values(getScript.Do(c, cartId, accessToken, req.FoodId, req.Count))
	fmt.Println("addcart", err)

	var statusCode int
	var json string
	redis.Scan(reply, &statusCode, &json)

	w.WriteHeader(statusCode)
	fmt.Fprintf(w, json)
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

	type Req struct {
		CartId string `json:"cart_id"`
	}
	var req Req
	ffjson.Unmarshal(body, &req)
	orderId := string(NewAccessToken(16))

	lua := `local actk = redis.call("get", "c:"..KEYS[1])
		local user_id = redis.call("get", "u:"..KEYS[2])
		if redis.call("get", "u:o:"..user_id) == false then
		  if actk then
		    if KEYS[2] == actk then
		      local menu = redis.call("hgetall", "c:f:"..KEYS[1])
		      if menu then
		        local valid = true
		        local foods = {}
		        for i = 1, table.getn(menu), 2 do
		          foods[menu[i]] = tonumber(redis.call("hget", "f", menu[i]))
		          if foods[menu[i]] <= 0 or foods[menu[i]] - tonumber(menu[i+1]) < 0 then
		            valid = false
		          end
		        end
		        if valid then
		          for i = 1, table.getn(menu), 2 do
		            local new_stock = foods[menu[i]] - tonumber(menu[i+1])
		            redis.call("hset", "f", menu[i], new_stock)
		            redis.call("hset", "o:f:"..KEYS[3], menu[i], menu[i+1])
		          end
		          redis.call("set", "oid:"..KEYS[3], user_id)
		          redis.call("set", "u:o:"..user_id, KEYS[3])
		          return {200, "{\"id\":\""..KEYS[3].."\"}"}
		        else
		          return {403, "{\"code\": \"FOOD_OUT_OF_STOCK\",\"message\":\"食物库存不足\"}"}
		        end
		      else
		        return {403, "{\"code\": \"NO_FOOD_IN_CART\",\"message\":\"篮子中无食物\"}"}
		      end
		    else
		      return {401, "{\"code\": \"NOT_AUTHORIZED_TO_ACCESS_CART\",\"message\":\"无权限访问指定的篮子\"}"}
		    end
		  else
		    return {404, "{\"code\": \"CART_NOT_FOUND\",\"message\":\"篮子不存在\"}"}
		  end
		else
		  return {403, "{\"code\": \"ORDER_OUT_OF_LIMIT\",\"message\":\"每个用户只能下一单\"}"}
		end`
	var getScript = redis.NewScript(3, lua)
	c := rp.Get()
	reply, _ := redis.Values(getScript.Do(c, req.CartId, accessToken, orderId))

	var statusCode int
	var json string
	var msg string
	redis.Scan(reply, &statusCode, &json, &msg)
	fmt.Println("PO", err, msg)

	w.WriteHeader(statusCode)
	fmt.Fprintf(w, json)
}

func ShowOrdersController(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
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

	lua := `local uid = redis.call("get", "u:"..KEYS[1])
		local oid = redis.call("get", "u:o:"..uid)
		if oid then
			local menu = redis.call("hgetall", "o:f:"..oid)
			return {uid, oid, menu}
		end`
	var getScript = redis.NewScript(1, lua)
	c := rp.Get()
	reply, _ := redis.Values(getScript.Do(c, accessToken))

	var json bytes.Buffer
	if len(reply) != 0 {
		var userId, orderId string
		var items []interface{}
		redis.Scan(reply, &userId, &orderId, &items)

		var total int = 0
		var j []string
		v, _ := redis.Ints(items, nil)
		for i := 0; i < len(v); i += 2  {
			fid := v[i]
			cnt := v[i+1]
			total += gfoods[fid].Price * cnt
			j = append(j, fmt.Sprintf(`{"food_id": %d, "count": %d}`, fid, cnt))
		}
		json.WriteString("[{")
		json.WriteString(fmt.Sprintf(`"id": "%s", `, orderId))
		json.WriteString(fmt.Sprintf(`"items": [%s]`, strings.Join(j, ",")))
		json.WriteString(fmt.Sprintf(`,"total": %d`, total))
		json.WriteString("}]")

		fmt.Println(json.String())
	} else {
		json.WriteString("{}")
	}

	w.WriteHeader(200)
	fmt.Fprintf(w, json.String())
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

	lua := `local uid = redis.call("get", "u:"..KEYS[1])
		local okeys = redis.call("keys", "oid:*")
		local orders = {}
		for i=1,table.getn(okeys) do
		  local order = redis.call("get", okeys[i])
		  local oid = string.sub(okeys[i], 2)
		  local menu = redis.call("hgetall", "o:f:"..oid)
		  orders[i] = {oid, menu}
		end
		return orders`
	var getScript = redis.NewScript(1, lua)
	c := rp.Get()
	reply, _ := redis.Values(getScript.Do(c, accessToken))

	fmt.Println(reply)

	var json bytes.Buffer
	if len(reply) != 0 {
		var userId, orderId string
		var items []interface{}
		redis.Scan(reply, &userId, &orderId, &items)

		var total int = 0
		var j []string
		v, _ := redis.Ints(items, nil)
		for i := 0; i < len(v); i += 2  {
			fid := v[i]
			cnt := v[i+1]
			total += gfoods[fid].Price * cnt
			j = append(j, fmt.Sprintf(`{"food_id": %d, "count": %d}`, fid, cnt))
		}
		json.WriteString("[{")
		json.WriteString(fmt.Sprintf(`"id": "%s", `, orderId))
		json.WriteString(fmt.Sprintf(`"user_id": "%s", `, userId))
		json.WriteString(fmt.Sprintf(`"items": [%s]`, strings.Join(j, ",")))
		json.WriteString(fmt.Sprintf(`,"total": %d`, total))
		json.WriteString("}]")

		fmt.Println(json.String())
	} else {
		json.WriteString("{}")
	}

	w.WriteHeader(200)
	fmt.Fprintf(w, json.String())
}
