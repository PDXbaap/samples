package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"

	"log"
	"math/big"
)

func main() {
	//ewasm合约部署完成后需要等待哨兵合约检查完成后才能进行调用,大概需要10个normal区块的时间进行确认
	var host = "http://127.0.0.1:8547"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		log.Fatal("ethclient Dial fail", err)
		return
	}
	// 合约地址
	to := common.HexToAddress("0x5d85F01d4B0Eedd70a07a7472F7350e25c24BE08")
	priKey, err := crypto.HexToECDSA(utilities.PrivKeys[0])
	if err != nil {
		log.Fatal("err", err)
		return
	}
	from := crypto.PubkeyToAddress(priKey.PublicKey)
	fmt.Println("from", from.String())
	nonce, err := client.EthClient.PendingNonceAt(context.Background(), from)
	if err != nil {
		log.Fatal("err", err)
		return
	}
	//如果genesis.json文件配置blocksize,则不需要预估gas
	var (
		gas      uint64 = 0
		gasPrice        = big.NewInt(0)
	)

	/*
			msg := ethereum.CallMsg{
					From: from,
					To:   &to,
					Data: []byte("put:pdx,222"),
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
	//put为智能合约的方法,key=pdx value=222
	data := []byte("put:pdx,222")
	tx := types.NewTransaction(nonce, to, amount, gas, gasPrice, data)
	// 区块链 id为：777
	signer := types.NewEIP155Signer(big.NewInt(777))
	signedTx, err := types.SignTx(tx, signer, priKey)
	if err != nil {
		fmt.Println("types.SignTx", err)
		return
	}
	hash, err := client.SendRawTransaction(context.TODO(), signedTx)
	fmt.Println("交易hash", hash)
	if err != nil {
		fmt.Println("send raw transaction err:", err.Error())
		return
	}
	fmt.Println("txHash", hash.String())
}
