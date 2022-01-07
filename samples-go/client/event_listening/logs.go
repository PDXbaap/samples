package main

import (
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/ethclient"
	"log"
)

func main() {
	client, err := ethclient.Dial("ws://127.0.0.1:8546")
	if err != nil {
		log.Fatal("Dial error", err)
	}
	// 合约地址
	contractAddress := common.HexToAddress("0xDa3Ce11D916fFBa4a1289cEf66A7f142eC5A0f74")
	query := ethereum.FilterQuery{
		Addresses: []common.Address{contractAddress},
	}
	event := make(chan types.Log)

	sub, err := client.SubscribeFilterLogs(context.Background(), query, event)
	if err != nil {
		log.Fatal(err)
	}

	for {
		select {
		case err := <-sub.Err():
			log.Fatal(err)
		case vLog := <-event:
			fmt.Println(vLog) // pointer to event log
		}
	}
}
