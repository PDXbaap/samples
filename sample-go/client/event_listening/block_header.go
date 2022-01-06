package main

import (
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/ethclient"
	"log"
)

func main() {
	client, err := ethclient.Dial("ws://127.0.0.1:8546")
	if err != nil {
		log.Fatal("Dial fail ", err)
	}
	event := make(chan *types.Header)
	header, err := client.SubscribeNewHead(context.Background(), event)
	if err != nil {
		log.Fatal(err)
	}

	for {
		select {
		case err := <-header.Err():
			log.Fatal(err)
		case vLog := <-event:
			fmt.Println(vLog) // pointer to event log
		}
	}
}
