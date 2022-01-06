package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"io/ioutil"
	"math/big"
)


func main() {
	var host = "http://1.13.251.80:8545"
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
		fmt.Printf(err.Error())
		return
	}
	// @发送方客户端
	path := "/path-to-your/DynNative_sample.wasm"
	code, err := ioutil.ReadFile(path)
	if err != nil {
		fmt.Printf(err.Error())
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
	amount := big.NewInt(0)
	tx := types.NewContractCreation(nonce, amount, gas, gasPrice, code)
	// 区块链 id为：777
	signer := types.NewEIP155Signer(big.NewInt(777))
	signedTx, _ := types.SignTx(tx, signer, priKey)
	txHash, err := client.SendRawTransaction(context.TODO(), signedTx)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	if err != nil {
		fmt.Println("tx err", err)
		return
	}
	fmt.Println("txHash", txHash.Hex())
	fmt.Println("合约地址", crypto.CreateAddress(from, tx.Nonce()).Hex())
	return
}
