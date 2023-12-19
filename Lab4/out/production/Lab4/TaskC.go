package main

import (
	"fmt"
	"strconv"
	"strings"
	"sync"
)

var wg sync.WaitGroup

type Graph struct {
	sync.RWMutex
	matrix [][]int
}

func newSampleGraph() *Graph {
	mtx := [][]int{
		//	 0   1   2   3   4   5
		{0, 16, -1, 12, -1, -1}, // 0
		{16, 0, 20, -1, -1, -1}, // 1
		{-1, 20, 0, 23, -1, -1}, // 2
		{12, -1, 23, 0, 10, -1}, // 3
		{-1, -1, -1, 10, 0, 10}, // 4
		{-1, -1, -1, -1, 10, 0}, // 5
	}
	g := Graph{matrix: mtx}
	return &g
}

func (g *Graph) print() {
	for _, row := range g.matrix {
		fmt.Println(row)
	}
}

func (g *Graph) changePrice(a, b, newPrice int) {
	g.RLock()
	okay := 0 <= a && a < len(g.matrix) && 0 <= b && b < len(g.matrix)
	g.RUnlock()
	if okay {
		if a == b {
			fmt.Printf("Рейс між одним і тим самим містом %d не прокладається\n", a)
			return
		}
		g.RLock()
		if g.matrix[a][b] == -1 {
			fmt.Printf("Рейсу між містами %d та %d не існує. Тому неможливо змінити ціну\n", a, b)
		}
		g.RUnlock()
		g.Lock()
		prev := g.matrix[a][b]
		g.matrix[a][b] = newPrice
		g.matrix[b][a] = newPrice
		fmt.Printf("Ціну проїзду між місто %d та містом %d було змінено з %d$ до %d$\n", a, b, prev, newPrice)
		g.Unlock()
	} else {
		fmt.Println("❌ Incorrect arguments: принаймні один із параметрів виходить за межі масиву.")
	}
	wg.Done()
}

func (g *Graph) deleteAddRoute(delA, delB, addA, addB, routePrice int) {
	g.RLock()
	okay := 0 <= delA && delA < len(g.matrix) && 0 <= delB && delB < len(g.matrix) && 0 <= addA && addA <= len(g.matrix) && 0 <= addB && addB <= len(g.matrix)
	g.RUnlock()
	if okay {
		g.Lock()
		if g.matrix[delA][delB] > 0 {
			g.matrix[delA][delB] = -1
			g.matrix[delB][delA] = -1
			fmt.Printf("Рейс між містами %d та %d було видалено\n", delA, delB)
		} else {
			fmt.Printf("Немає сенсу видаляти шлях між містами %d та %d, оскільки він уже відсутній\n", delA, delB)
		}
		g.Unlock()
		g.RLock()
		if g.matrix[addA][addB] > 0 {
			fmt.Printf("Рейс між містами %d та %d уже існує, тому неможливо додати новий\n", addA, addB)
			g.RUnlock()
		} else if g.matrix[addA][addB] == -1 {
			g.RUnlock()
			g.Lock()
			g.matrix[addA][addB] = routePrice
			g.matrix[addB][addA] = routePrice
			fmt.Printf("Додано рейс між містами %d та %d за ціною %d\n", addA, addB, routePrice)
			g.Unlock()
		}
	} else {
		fmt.Println("❌ Incorrect arguments: принаймні один із параметрів виходить за межі масиву.")
	}
	wg.Done()
}

func (g *Graph) deleteAddCity(delA int) {
	g.RLock()
	okay := 0 <= delA && delA < len(g.matrix)
	g.RUnlock()
	if okay {
		g.Lock()
		for i := 0; i < len(g.matrix); i++ {
			if i != delA {
				g.matrix[i][delA] = -1
				g.matrix[delA][i] = -1
			}
		}
		g.Unlock()
		fmt.Printf("Місто %d видалено\n", delA)
		fmt.Printf("Додано нове ізольоване місто %d\n", delA)
	} else {
		fmt.Println("❌ Incorrect arguments: принаймні один із параметрів виходить за межі масиву.")
	}
	wg.Done()
}

func (g *Graph) findPath(a, b int) {
	g.RLock()
	okay := 0 <= a && a < len(g.matrix) && 0 <= b && b < len(g.matrix)
	g.RUnlock()
	if okay {
		if a == b {
			fmt.Printf("Зрозуміло, що шлях з міста %d до міста %d існує і його ціна становить 0$")
		} else {
			g.RLock()
			way := g.route(a, b)
			g.RUnlock()
			if len(way) == 0 {
				fmt.Printf("Шляху між містами %d та %d не існує\n", a, b)
			} else {
				w := make([]string, len(way))
				for i, el := range way {
					w[i] = strconv.Itoa(el)
				}
				price := 0
				for i := 0; i < len(way)-1; i++ {
					price += g.matrix[way[i]][way[i+1]]
				}
				fmt.Printf("Ціна рейсу між містами %d та %d становить %d:\n"+strings.Join(w, " - ")+"\n", a, b, price)
			}
		}
	}
	wg.Done()
}

func (g *Graph) route(a, b int) []int {
	if g.matrix[a][b] > 0 {
		return []int{a, b}
	} else {
		routes := make([][]int, 8)
		routes = append(routes, []int{a})
		for currLen := 1; ; currLen++ {
			added := false
			for i := len(routes) - 1; i >= 0 && len(routes[i]) == currLen; i-- {
				route := routes[i]
				for k, el := range g.matrix[route[len(route)-1]] {
					if el > 0 && !contains(&route, k) {
						newRoute := make([]int, len(route)+1)
						j := 0
						for ; j < len(route); j++ {
							newRoute[j] = route[j]
						}
						newRoute[j] = k
						if k == b {
							return newRoute
						}
						routes = append(routes, newRoute)
						added = true
					}
				}
			}
			if !added {
				return []int{}
			}
		}
	}
}

func contains(way *[]int, a int) bool {
	for _, el := range *way {
		if el == a {
			return true
		}
	}
	return false
}

func main() {
	g := newSampleGraph()
	wg.Add(6)
	go g.findPath(0, 4)
	go g.changePrice(4, 5, 11)
	go g.deleteAddRoute(3, 4, 2, 5, 18)
	go g.findPath(1, 5)
	go g.deleteAddCity(2)
	go g.findPath(2, 5)
	wg.Wait()
}
