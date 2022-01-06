package main

import (
	"errors"
	so "github.com/PDXbaap/utopia_spi/contract"
)

var DynNativeContract dynNativeContract

type dynNativeContract struct{}

func (s *dynNativeContract) Run(stub interface{}) ([]byte, error) {
	v, ok := stub.(so.StubInterface)
	if !ok {
		return nil, nil
	}
	function, inputs := v.GetFunctionAndParameters()
	if function == "get" {
		return s.get(v, inputs)
	} else if function == "put" {
		return s.put(v, inputs)
	}
	return []byte{}, nil
}

func (s *dynNativeContract) get(stub so.StubInterface, args [][]byte) ([]byte, error) {
	if len(args) != 1 {
		return []byte{}, errors.New("查询输入有误")
	}

	key := args[0]
	v, err := stub.GetState(key)
	if err != nil {
		return nil, err
	}
	return v, nil
}

func (s *dynNativeContract) put(stub so.StubInterface, args [][]byte) ([]byte, error) {
	if len(args) != 2 {
		return nil, errors.New("输入有误")
	}
	key := args[0]
	v := args[1]
	err := stub.PutState(key, v)
	if err != nil {
		return nil, err
	}
	return v, nil
}

func main() {

}
