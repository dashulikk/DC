package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type BarbershopState int
type Visiting int

const (
	NoClient Visiting = iota
	ClientWaiting
)

var wg sync.WaitGroup
var queue sync.WaitGroup
var once sync.Once
var visits []Visiting

func initClients() {
	rand.Seed(int64(time.Now().Second()))
	for i := 0; i < 10; i++ {
		if rand.Int()%2 == 0 {
			visits = append(visits, NoClient)
		} else {
			visits = append(visits, ClientWaiting)
		}
	}
	fmt.Println(visits)
}

func clientStream(clients chan<- int) {
	once.Do(initClients)
	num := 1
	inQueue := false
	for _, i := range visits {
		queue.Wait()
		queue.Add(1)
		if i == ClientWaiting {
			if inQueue {
				fmt.Printf("Відвідувач №%v стає в чергу\n", num)
			}
			inQueue = true
			clients <- num
			num++
		} else {
			inQueue = false
			clients <- 0
		}
	}
	close(clients)
	wg.Done()
}

func barber(clients <-chan int, done chan<- bool) {
	once.Do(initClients)
	// спочатку перукар спить
	sleep := false
	fmt.Println("✅ Перукарня відчиняється")
	for client := range clients {
		if client == 0 {
			if !sleep {
				sleep = true
				fmt.Printf("Відвідувачів немає - перукар спить\n")
			}
		} else {
			if sleep {
				sleep = false
				fmt.Printf("Відвідувач №%v будить перукаря, проходить до крісла\n", client)
			}
			fmt.Printf("Перукар стриже відвідувача №%v\n", client)
		}
		queue.Done()
	}
	fmt.Println("❌ Перукарня зачиняється")
	done <- true
	close(done)
	wg.Done()
}

func main() {
	clients := make(chan int, 0)
	done := make(chan bool)

	wg.Add(2)
	go clientStream(clients)
	go barber(clients, done)

	<-done
	wg.Wait()
}
