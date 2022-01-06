package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/rlp"
)

func main() {
	var host = "http://192.168.3.47:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Println("err", err.Error())
		return
	}
	// 合约地址
	to := common.HexToAddress("0x4bbd27AaC056178dc3eea9d14E1c8Eb10Fa3732f")
	fmt.Printf("to:%s\n", to.String())
	type callSoInfo struct {
		SoName          string
		LookUpClassName string
		Args            [][]byte
		Data            []byte
	}
	soInfo := &callSoInfo{
		LookUpClassName: "DynNativeContract",
		Args:            [][]byte{[]byte("get"), []byte("name")},
	}
	data, err := rlp.EncodeToBytes(soInfo)
	if err != nil {
		fmt.Println(err)
		return
	}
	callMsg := ethereum.CallMsg{
		To:   &to,
		Data: data,
	}
	result, err := client.EthClient.CallContract(context.TODO(), callMsg, nil)
	if err != nil {
		fmt.Println("err", err)
		return
	}
	fmt.Println("result", result)
}
