package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"math/big"
)



func main() {
	var host = "http://121.35.8.134:8545"

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
	nonce, err := client.EthClient.NonceAt(context.TODO(), from, nil)

	if err != nil {
		fmt.Printf("nonce err: %s", err.Error())
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
					To:   nil,
					Data: code,
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
	// 接收方地址
	to := common.HexToAddress("0x48c60bdeed69477460127c28b27e43a7ad442b9a")

	var data []byte
	// 转账金额
	amount := big.NewInt(0).Mul(big.NewInt(0), big.NewInt(1e18))

	tx := types.NewTransaction(nonce, to, amount, gas, gasPrice, data)
	// 区块链 id为：777
	signer := types.NewEIP155Signer(big.NewInt(777))

	signedTx, err := types.SignTx(tx, signer, priKey)

	if err != nil {
		fmt.Println("types.SignTx", err)

	}
	hash, err := client.SendRawTransaction(context.TODO(), signedTx)

	if err != nil {
		fmt.Println("send raw transaction err:", err.Error())

	}
	fmt.Println("交易hash", hash)
}
