package main

import (
	"client_Sample/chaincode/protos"
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/golang/protobuf/proto"
	"time"
)

func main() {
	var host = "http://81.69.236.242:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	// 合约地址
	to := common.HexToAddress("0x14632e85D8Cb91D943BD63e08E15DA8411DF2382")
	invocation := &protos.Invocation{
		Fcn:  "get", //getHis,get,getRange,del,
		Args: [][]byte{[]byte("a")}, //智能合约的参数
		Meta: map[string][]byte{"to": []byte(to.String())},
	}
	payload, err := proto.Marshal(invocation)
	if err != nil {
		fmt.Printf("proto marshal invocation error:%v", err)
		return
	}
	ptx := &protos.Transaction{
		Type:    1, //1invoke 2deploy
		Payload: payload,
	}
	data, err := proto.Marshal(ptx)
	if err != nil {
		fmt.Printf("!!!!!!!!proto marshal error:%v", err)
		return
	}
	c, _ := context.WithTimeout(context.Background(), 800*time.Millisecond)
	var result string
	result, err = client.BaapQuery(c, data)
	if err != nil {
		fmt.Println("query tx error", "err", err)
		return
	}
	fmt.Println("baap query", "resp", result)
	if err != nil {
		fmt.Printf("err:%v", err)
		return
	}
}
