package main

import (
	"client_Sample/chaincode/protos"
	"client_Sample/utilities"
	"context"
	"encoding/json"
	"fmt"
	"github.com/ethereum/go-ethereum/common"
	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/crypto"
	"github.com/golang/protobuf/proto"
	"golang.org/x/crypto/sha3"
	"io/ioutil"

	"math/big"
)

const (
	//合约的拥有者、合约名称、合约版本号
	owner = "8000d109DAef5C81799bC01D4d82B0589dEEDb33"
	name  = "sample"
)

func main() {
	host := "http://127.0.0.1:8545"
	client, err := utilities.ToolConnect(host)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	priKey, err := crypto.HexToECDSA("d29ce71545474451d8292838d4a0680a8444e6e4c14da018b4a08345fb2bbb84")
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	from := crypto.PubkeyToAddress(priKey.PublicKey)
	fmt.Println("from:", from.String())
	//预编译合约的地址，必须是 :baap-deploy
	to := iKeccak256ToAddress(":baap-deploy")
	fmt.Printf("to:%s\n", to.String())
	fmt.Println(from.String())

	nonce, err := client.EthClient.NonceAt(context.TODO(), from, nil)
	if err != nil {
		fmt.Printf(err.Error())
		return
	}
	deployInfo := struct {
		FileName    string `json:"fileName"`
		ChaincodeId string `json:"chaincodeId"`
		Pbk         string `json:"pbk"`
	}{
		"MyCc.java",
		owner + ":" + name + ":",
		string(crypto.CompressPubkey(&priKey.PublicKey)),
	}
	deployInfoBuf, err := json.Marshal(deployInfo)
	if err != nil {
		fmt.Printf("marshal deployInfo err: %v", err)
		return
	}
	//  @发送方客户端
	myccBuf, err := ioutil.ReadFile("/path-to-your/MyCc.java")
	if err != nil {
		fmt.Printf("read java file err:%v", err)
		return
	}
	invocation := &protos.Invocation{
		Fcn:  "deploy",
		Args: [][]byte{deployInfoBuf},
		Meta: map[string][]byte{
			"baap-tx-type": []byte("exec"),//Baap要求
			"baap-cc-code": myccBuf,
		},
	}
	dep := &protos.Deployment{
		Owner:   owner,
		Name:    name,
		Payload: invocation,
	}
	payload, err := proto.Marshal(dep)
	if err != nil {
		fmt.Printf("proto marshal invocation error:%v", err)
		return
	}
	ptx := &protos.Transaction{
		Type:    2, //1invoke 2deploy
		Payload: payload,
	}
	data, err := proto.Marshal(ptx)
	if err != nil {
		fmt.Printf("!!!!!!!!proto marshal error:%v", err)
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

func iKeccak256ToAddress(ccName string) common.Address {
	hash := sha3.NewLegacyKeccak256()
	crypto.Keccak256()
	var buf []byte
	hash.Write([]byte(ccName))
	buf = hash.Sum(buf)
	fmt.Println("keccak256ToAddress:", common.BytesToAddress(buf).String())
	addr := common.BytesToAddress(crypto.Keccak256([]byte(ccName))[12:])
	fmt.Println("keccak256ToAddress:", addr.String())
	return common.BytesToAddress(buf)
}
