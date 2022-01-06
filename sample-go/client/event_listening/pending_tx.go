package main

import (
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/ethclient/gethclient"
	"github.com/ethereum/go-ethereum/rpc"
	"log"
)

func main() {

	dial, err := rpc.Dial("ws://127.0.0.1:8546")
	client := gethclient.New(dial)

	event := make(chan common.Hash)
	tx, err := client.SubscribePendingTransactions(context.Background(), event)
	if err != nil {
		log.Fatal(err)
	}

	for {
		select {
		case err := <-tx.Err():
			log.Fatal(err)
		case vLog := <-event:
			fmt.Println(vLog) // pointer to event log
		}
	}
}
