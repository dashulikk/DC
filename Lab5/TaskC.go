package main

import (
	"fmt"
	"math/rand"
	"strconv"
	"sync"
	"time"
)

var wg sync.WaitGroup

func arraySum(arr []int, sum *int, add int) {
	arr[rand.Intn(len(arr))] += add
	*sum = 0
	for _, val := range arr {
		*sum += val
	}
	wg.Done()
}

func randomSlice(n int) []int {
	return rand.Perm(rand.Intn(n) + n)
}

func equals(s1, s2, s3 int, min, max *int) bool {
	if s1 == s2 && s2 == s3 {
		return true
	} else {
		*min = 1
		minSum := s1
		*max = 1
		maxSum := s1
		if s2 < minSum {
			minSum = s2
			*min = 2
		}
		if s2 > maxSum {
			maxSum = s2
			*max = 2
		}
		if s3 < minSum {
			*min = 3
		}
		if s3 > maxSum {
			*max = 3
		}
	}
	return false
}

func printStep(s1, s2, s3 int, a1, a2, a3 []int) {
	fmt.Print(strconv.Itoa(s1) + " ")
	fmt.Println(a1)
	fmt.Print(strconv.Itoa(s2) + " ")
	fmt.Println(a2)
	fmt.Print(strconv.Itoa(s3) + " ")
	fmt.Println(a3)
}

func main() {
	rand.Seed(time.Now().Unix())
	n := 9
	a1, a2, a3 := randomSlice(n), randomSlice(n), randomSlice(n)
	s1, s2, s3 := 0, 0, 0
	wg.Add(3)
	go arraySum(a1, &s1, 0)
	go arraySum(a2, &s2, 0)
	go arraySum(a3, &s3, 0)
	wg.Wait()
	printStep(s1, s2, s3, a1, a2, a3)
	min, max := 1, 1
	i := 1
	for !equals(s1, s2, s3, &min, &max) {
		i++
		q := -1
		if rand.Intn(2) == 1 {
			q = rand.Intn(2)
		}
		d1, d2, d3 := q, q, q
		switch min {
		case 1:
			d1 = 1
		case 2:
			d2 = 1
		case 3:
			d3 = 1
		}
		switch max {
		case 1:
			d1 = -1
		case 2:
			d2 = -1
		case 3:
			d3 = -1
		}
		wg.Add(3)
		go arraySum(a1, &s1, d1)
		go arraySum(a2, &s2, d2)
		go arraySum(a3, &s3, d3)
		wg.Wait()
		if i%100 == 0 {
			fmt.Println("i = " + strconv.Itoa(i))
			printStep(s1, s2, s3, a1, a2, a3)

		}
	}
	fmt.Println("Number of iterations = " + strconv.Itoa(i))
	printStep(s1, s2, s3, a1, a2, a3)

}
