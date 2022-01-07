package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/accounts/abi"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"log"

	"math/big"
	"strings"
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
	// 合约地址
	to := common.HexToAddress("0xDa3Ce11D916fFBa4a1289cEf66A7f142eC5A0f74")
	data := creatAbi([]byte{97}, []byte{98})
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
}

func creatAbi(key, value []byte) []byte {
	myContractAbi := `[{\"constant\":false,\"inputs\":[{\"internalType\":\"bytes\",\"name\":\"key\",\"type\":\"bytes\"},{\"internalType\":\"bytes\",\"name\":\"value\",\"type\":\"bytes\"}],\"name\":\"put\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]`
	/*
	    constant 是否改变合约
	   	inputs  方法参数
	    name 方法名称
	    outputs 返回值
	    payable 是否可以转账
	    type 类型 function，constructor，fallback（缺省方法）

	*/
	abi, err := abi.JSON(strings.NewReader(myContractAbi))
	if err != nil {
		log.Fatalln("JSON err", err)
	}
	//put是智能合约的方法名称, key, value是对应的参数
	abiBuf, err := abi.Pack("put", key, value)
	if err != nil {
		log.Fatalln("Pack err", err)
	}
	return abiBuf
}
