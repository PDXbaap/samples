package main

import (
	"fmt"
	"github.com/hyperledger/fabric-chaincode-go/shim"
	pb "github.com/hyperledger/fabric-protos-go/peer"
	"strconv"
	"strings"
)

// SimpleChaincode example simple Chaincode implementation
type SimpleChaincode struct {
}

var A, B string
var Aval, Bval, X int

// Init callback representing the invocation of a chaincode
// This chaincode will manage two accounts A and B and will transfer X units from A to B upon put
func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	var err error
	_, args := stub.GetFunctionAndParameters()
	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	// Initialize the chaincode
	A = args[0]
	Aval, err = strconv.Atoi(args[1])
	if err != nil {
		return shim.Error("Expecting integer value for asset holding")
	}
	B = args[2]
	Bval, err = strconv.Atoi(args[3])
	if err != nil {
		return shim.Error("Expecting integer value for asset holding")
	}
	fmt.Printf("Aval = %d, Bval = %d\n", Aval, Bval)

	/************
	    // Write the state to the ledger
	    err = stub.PutState(A, []byte(strconv.Itoa(Aval))
	    if err != nil {
	      return nil, err
	    }

	    stub.PutState(B, []byte(strconv.Itoa(Bval))
	    err = stub.PutState(B, []byte(strconv.Itoa(Bval))
	    if err != nil {
	      return nil, err
	    }
	************/
	return shim.Success(nil)
}

func (t *SimpleChaincode) put(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	err := stub.PutState(args[0], []byte(args[1]))
	if err != nil {
		fmt.Printf("Error put [%s:%s] to state: %s", args[0], args[1], err)
		return shim.Error(fmt.Sprintf("Error put [%s:%s] to state: %s", args[0], args[1], err))
	}
	fmt.Printf("put state success!!!!!!!!!!!")
	return shim.Success(nil)
}

func (t *SimpleChaincode) get(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	res, err := stub.GetState(args[0])
	if err != nil {
		fmt.Printf("Error get [%s] from state: %s", args[0], err)
		return shim.Error(fmt.Sprintf("Error get [%s] from state: %s", args[0], err))
	}
	fmt.Printf("get state success!!!!!!!!!!!")
	return shim.Success(res)
}

func (t *SimpleChaincode) del(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	err := stub.DelState(args[0])
	if err != nil {
		fmt.Printf("Error del [%s] from state: %s", args[0], err)
		return shim.Error(fmt.Sprintf("Error del [%s] from state: %s", args[0], err))
	}
	fmt.Printf("del state success!!!!!!!!!!!")
	return shim.Success(nil)
}

func (t *SimpleChaincode) his(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	hisIterator, err := stub.GetHistoryForKey(args[0])
	if err != nil {
		fmt.Printf("Error del [%s] from state: %s", args[0], err)
		return shim.Error(fmt.Sprintf("Error del [%s] from state: %s", args[0], err))
	}
	defer hisIterator.Close()

	var myHis []string
	for hisIterator.HasNext() {
		keyModify, err := hisIterator.Next()
		if err != nil {
			fmt.Printf("Error his [%s] from state: %s", args[0], err)
			shim.Error("history iterator next:"+err.Error())
		}
		myHis = append(myHis, fmt.Sprintf("key : %s, value : %s, isDelete : %s \n", args[0], keyModify.Value, strconv.FormatBool(keyModify.IsDelete)))
	}

	fmt.Printf("his state success!!!!!!!!!!!")
	return shim.Success([]byte(strings.Join(myHis,"")))
}

func (t *SimpleChaincode) rangeData(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	stateIterator, err := stub.GetStateByRange(args[0], args[1])
	if err != nil {
		fmt.Printf("Error range [%s:%s] from state: %s", args[0], args[1], err)
		return shim.Error(fmt.Sprintf("Error del [%s] from state: %s", args[0], err))
	}
	defer stateIterator.Close()

	var myRange []string
	for stateIterator.HasNext() {
		queryResult, err := stateIterator.Next()
		if err != nil {
			fmt.Printf("Error range [%s] from state: %s", args[0], err)
			shim.Error("range iterator next:"+err.Error())
		}
		myRange = append(myRange, fmt.Sprintf("key : %s, value : %s \n", queryResult.Key, queryResult.Value))
	}
	fmt.Printf("range state success!!!!!!!!!!!")
	return shim.Success([]byte(strings.Join(myRange,"")))
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	switch function {
	case "put":
		return t.put(stub, args)
	case "get":
		return t.get(stub, args)
	case "del":
		return t.del(stub, args)
	case "getHis":
		return t.his(stub, args)
	case "getRange":
		return t.rangeData(stub, args)
	}

	return shim.Error("Invalid function name. Expecting \"put or get or del or his or range\"")
}
