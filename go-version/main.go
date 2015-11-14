package main

import (
	"bytes"
	"database/sql"
	"github.com/go-martini/martini"
	_ "github.com/go-sql-driver/mysql"
	"github.com/pquerna/ffjson/ffjson"
	"net/http"
	"os"
)

var db *sql.DB
var err error

type User struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
	Pass string `json:"passwod"`
}

var (
	AppHost   string
	AppPort   string
	DbHost    string
	DbPort    string
	DbName    string
	DbUser    string
	DbPass    string
	RedisHost string
	RedisPort string
)

func InitEnv() {
	AppHost = os.Getenv("APP_HOST")
	AppPort = os.Getenv("APP_PORT")
	DbHost = os.Getenv("DB_HOST")
	DbPort = os.Getenv("DB_PORT")
	DbName = os.Getenv("DB_NAME")
	DbUser = os.Getenv("DB_USER")
	DbPass = os.Getenv("DB_PASS")
}

func main() {

	InitEnv()
	var url = DbUser + ":" + DbPass + "@tcp(" + DbHost + ":" + DbPort + ")/" + DbName
	db, err = sql.Open("mysql", url)
	defer db.Close()
	if err != nil {
		panic(err.Error())
	}

	m := martini.Classic()
	m.Get("/", HomeController)

	// For testing
	m.Get("/login", LoginController)

	// All Routers
	m.Post("/login", LoginController)
	m.Get("/foods", FoodController)
	m.Post("/carts", NewCartsController)
	m.Patch("/carts/:cart_id", FoodsToCartController)
	m.Post("/orders", OrdersController)
	m.Get("/orders", ShowOrdersController)
	m.Get("/admin/orders", AdminShowOrdersController)

	m.RunOnAddr(":8080")
}

func LoginController() (int, string) {
	return 200, "ok"
}

// Show all foods in database.
func FoodController() (int, string) {
	return 200, "ok"
}

// Create a new Cart.
func NewCartsController() (int, string) {
	return 200, "ok"
}

// Add food to cart.
func FoodsToCartController() (int, string) {
	return 200, "ok"
}

// Place an order.
func OrdersController() (int, string) {
	return 200, "ok"
}

// Display all orders of a user.
func ShowOrdersController() (int, string) {
	return 200, "ok"
}

// Display all orders of a user.
// Caution: Little bit differ from previous one.
func AdminShowOrdersController() (int, string) {
	return 200, "ok"
}

func tojson(w http.ResponseWriter, buf []byte) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.Write(buf)
}

func HomeController(w http.ResponseWriter) string {

	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	rows, err := db.Query("SELECT * FROM user where id < 10")
	if err != nil {
		panic(err.Error())
	}

	defer rows.Close()

	var id int
	var name, pass string
	var buffer bytes.Buffer

	for rows.Next() {
		err := rows.Scan(&id, &name, &pass)
		// fmt.Println(content)
		buffer.WriteString(name)
		buffer.WriteString("\n")
		if err != nil {
			panic(err.Error())
		}
	}
	b, err := ffjson.Marshal(buffer.String())
	return string(b)
}
