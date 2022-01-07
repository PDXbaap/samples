package main

import (
	"client_Sample/chaincode/protos"
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/golang/protobuf/proto"
	"math/big"
)

func main() {
	var host = "http://127.35.222.60:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	priKey, err := crypto.HexToECDSA(utilities.PrivKeys[0])
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	from := crypto.PubkeyToAddress(priKey.PublicKey)
	fmt.Println("from:", from.String())
	// 合约地址
	to := common.HexToAddress("0x14632e85D8Cb91D943BD63e08E15DA8411DF2382")
	fmt.Printf("to:%s\n", to.String())
	nonce, err := client.EthClient.NonceAt(context.TODO(), from, nil)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	var a []byte
	for i := 0; i < 10; i++ {
		a = append(a, []byte("a")...)
	}
	invocation := &protos.Invocation{
		// 智能合约方法
		Fcn:  "put",
		// 智能合约方法参数
		Args: [][]byte{[]byte("a"), a},
		// Baap要求
		Meta: map[string][]byte{"baap-tx-type": []byte("exec")},
	}
	payload, err := proto.Marshal(invocation)
	if err != nil {
		fmt.Printf("proto marshal invocation error:%v", err)
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
	fmt.Println("nounce:", nonce)
	//如果genesis.json文件配置blocksize,则不需要预估gas
	var (
		gas      uint64 = 0
		gasPrice        = big.NewInt(0)
	)

	/*
			msg := ethereum.CallMsg{
					From: from,
					To:   &to,
					Data: data,
				}
				//如果genesis.json文件没有配置blocksize, 则需要预估gas
				gas, err = client.EthClient.EstimateGas(context.Background(), msg)
				if err != nil {
					fmt.Println("预估的gas err", err)
					return
				}
				fmt.Println("预估的gas", "gas", gas)
		gasPrice        = new(big.Int).Mul(big.NewInt(1e9), big.NewInt(4000))
	*/
	amount := big.NewInt(0)
	tx := types.NewTransaction(nonce, to, amount, gas, gasPrice, data)
	// 区块链 id为：777
	signer := types.NewEIP155Signer(big.NewInt(777))

	signedTx, _ := types.SignTx(tx, signer, priKey)
	txHash, err := client.SendRawTransaction(context.TODO(), signedTx)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	fmt.Printf("Transaction hash: %s\n", txHash.String())
}
