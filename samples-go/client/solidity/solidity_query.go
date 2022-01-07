package main

import (
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/accounts/abi"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/ethclient"
	"log"
	"strings"
)

var contractAbi = `[{"constant":true,"inputs":[{"internalType":"bytes","name":"key","type":"bytes"}],"name":"get","outputs":[{"internalType":"bytes","name":"value","type":"bytes"}],"payable":false,"stateMutability":"view","type":"function"}]`

func main() {
	var host = "http://102.37.8.134:8545"
	// 合约地址
	var to = common.HexToAddress("0xe28D7B5Da87cCA2545429B56Adc2DF6FBE3F4513")
	abi, err := abi.JSON(strings.NewReader(contractAbi))
	if err != nil {
		log.Fatalln("JSON fail", err)
	}
	//get是智能合约的方法名称, []byte{97}是对应的参数
	abiBuf, err := abi.Pack("get",[]byte{97})
	if err != nil {
		log.Fatalln("Pack fail", err)
	}
	callMsg := ethereum.CallMsg{
		To:   &to,
		Data: abiBuf,
	}
	client, err := ethclient.Dial(host)
	result, err := client.CallContract(context.TODO(), callMsg, nil)
	if err != nil {
		log.Fatalln("CallContract fail", err)
	}
	//get是智能合约的方法名称
	r, err := abi.Unpack("get", result)

	if err != nil {
		log.Fatalln("Unpack", err)
	}
	fmt.Println(r)
}
