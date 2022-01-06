package main

import (
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/rpc"
	"log"
)

func main() {
	dial, err := rpc.Dial("ws://127.0.0.1:8546")
	if err != nil {
		log.Fatal("rpc.Dial", err)
	}
	event := make(chan *interface{})
	subscribe, err := dial.EthSubscribe(context.Background(), event, "syncing")
	if err != nil {
		log.Fatal("dial.Subscribe", err)
	}
	defer subscribe.Unsubscribe()
	for {
		select {
		case err := <-subscribe.Err():
			log.Fatal(err)
		case vLog := <-event:
			fmt.Println(vLog) // pointer to event log
		}
	}
}
