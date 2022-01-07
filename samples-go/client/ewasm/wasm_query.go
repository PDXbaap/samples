package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/common/hexutil"
	"log"
)

func main() {
	var host = "http://127.0.0.1:8547"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		log.Fatal("ethclient Dial fail", err)
		return
	}
	// 合约地址
	to := common.HexToAddress("0x5d85F01d4B0Eedd70a07a7472F7350e25c24BE08")
	// key为智能合约get方法的参数
	key := "pdx"
	callMsg := ethereum.CallMsg{
		To:   &to,
		Data: []byte("get:" + key),
	}
	fmt.Println(hexutil.Bytes(callMsg.Data))
	bytes, err := client.EthClient.CallContract(context.Background(), callMsg, nil)
	if err != nil {
		log.Println(err)
		return
	}
	fmt.Printf("key=%v,value=%v", key, bytes)
}
