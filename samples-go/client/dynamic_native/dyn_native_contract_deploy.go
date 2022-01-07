package main

import (
	"client_Sample/utilities"
	"context"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/ethereum/go-ethereum/rlp"
	"golang.org/x/crypto/sha3"
	"io/ioutil"
	"math/big"
	"os"
	"strings"
)

func main() {
	var host = "http://110.42.191.221:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Println("err", err.Error())
		return
	}
	priKey, err := crypto.HexToECDSA("a2f1a32e5234f64a6624210b871c22909034f24a52166369c2619681390433aa")
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	from := crypto.PubkeyToAddress(priKey.PublicKey)
	fmt.Println("from:", from.String())
	//预编译合约的地址，必须是 callso
	to := iKeccak256ToAddress("callso")
	fmt.Printf("to:%s\n", to.String())
	nonce, err := client.EthClient.NonceAt(context.TODO(), from, nil)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	// 动态库本地path @发送方客户端
	path := "/path-to-your/DynNative_sample.1.11.so"
	soData := readSoFile(path)

	type callSoInfo struct {
		SoName          string
		LookUpClassName string
		Args            [][]byte
		Data            []byte
	}
	soInfo := &callSoInfo{
		SoName:          "DynNative",
		LookUpClassName: "DynNativeContract",
		Data:            soData,
	}
	data, _ := rlp.EncodeToBytes(soInfo)
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
		fmt.Printf("send raw tx:%s", err.Error())
		return
	}
	fmt.Printf("Transaction hash: %s\n", txHash.String())
	soname := soInfo.SoName
	ownerS := from.String()
	soName := strings.ToLower(ownerS[2:]) + ":" + soname
	address := common.BytesToAddress(crypto.Keccak256([]byte(soName))[12:])
	fmt.Println("address", address)

}

func iKeccak256ToAddress(ccName string) common.Address {
	hash := sha3.NewLegacyKeccak256()
	var buf []byte
	hash.Write([]byte(ccName))
	buf = hash.Sum(buf)
	fmt.Println("keccak256ToAddress:", common.BytesToAddress(buf).String())
	return common.BytesToAddress(buf)
}

func readSoFile(path string) []byte {
	file, err := os.Open(path)
	if err != nil {
		println("err:", err.Error())
	}
	data, err := ioutil.ReadAll(file)
	if err != nil {
		println("err:", err.Error())
	}
	return data
}
