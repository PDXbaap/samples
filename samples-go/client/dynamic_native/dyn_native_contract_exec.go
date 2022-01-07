package main

import (
	"client_Sample/utilities"
	"context"
	"encoding/binary"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/ethereum/go-ethereum/rlp"
	"math/big"
)

func main() {
	var host = "http://110.42.191.221:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	priKey, err := crypto.HexToECDSA("a2f1a32e5234f64a6624210b871c22909034f24a52166369c2619681390433aa")
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	from := crypto.PubkeyToAddress(priKey.PublicKey)
	// 合约地址
	to := common.HexToAddress("0x178e1910226c15f65073AB1c2f78DA726B7d36A8")
	fmt.Println("from:", from.String())
	fmt.Printf("to:%s\n", to.String())
	nonce, err := client.EthClient.NonceAt(context.TODO(), from, nil)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	type callSoInfo struct {
		SoName          string
		LookUpClassName string
		Args            [][]byte
		Data            []byte
	}
	soInfo := &callSoInfo{
		LookUpClassName: "DynNativeContract",
		Args:            [][]byte{[]byte("put"), []byte("name"), Uint64ToByte(nonce)},
	}
	data, err := rlp.EncodeToBytes(soInfo)
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
	fmt.Println("nounce:", nonce)
	tx := types.NewTransaction(nonce, to, amount, gas, gasPrice, data)
	// 区块链 id为：777
	signer := types.NewEIP155Signer(big.NewInt(777))
	signedTx, _ := types.SignTx(tx, signer, priKey)
	txHash, err := client.SendRawTransaction(context.TODO(), signedTx)
	if err != nil {
		fmt.Printf("send raw tx:%s", err.Error())
		return
	}
	fmt.Printf("Transaction hash: %s\n", txHash.String())
}

func Uint64ToByte(n uint64) []byte {
	b := make([]byte, 8)
	binary.BigEndian.PutUint64(b, n)
	return b
}
