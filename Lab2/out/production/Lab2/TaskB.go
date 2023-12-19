
package main

import (
"fmt"
)

type Item struct {
	name string
	cost int
}

var items = []Item{
	{"M11", 2200},
	{"M1911 pistol", 1350},
	{"AK-47", 1800},
	{"Jacket", 500},
	{"Cap", 40},
	{"Pants", 200},
	{"Shirt", 80},
}

func ivanov(output chan<- Item) {
	for _, item := range items {
		fmt.Printf("Іванов виніс зі складу річ %s\n", item.name)
		output <- item
	}
	close(output)
	fmt.Println("Іванов закінчив виносити речі зі складу")
}

func petrov(output <-chan Item, load chan<- Item) {
	for item := range output {
		fmt.Printf("Петров відвантажив %s на вантажівку\n", item.name)
		load <- item
	}
	close(load)
	fmt.Println("Петров закінчив вантажити речі")
}

var totalCost int

func nechiporchuck(load <-chan Item, done chan<- bool) {
	for item := range load {
		totalCost += item.cost
		fmt.Printf("Нечипорчук порахував вартість %s\n", item.name)
	}

	done <- true

	fmt.Println("Нечипорчук закінчив рахувати вартість викрадених речей")
}

func main() {
	output := make(chan Item)
	load := make(chan Item)
	done := make(chan bool)
	go ivanov(output)
	go petrov(output, load)
	go nechiporchuck(load, done)
	<-done
	fmt.Printf("Загальна вартість викрадених речей %d$\n", totalCost)
}
