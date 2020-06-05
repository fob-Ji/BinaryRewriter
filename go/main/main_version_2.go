package main

import (
	"binaryRewriter/opcodes"
	"encoding/hex"
	"fmt"
	"math"
	"reflect"
	"strconv"

	//"binaryRewriter/opcodes"
	//"encoding/hex"
	//"math"
	//"strconv"
)

func dasm_printer(hexStr string) {
	var index int = 0

	for index < len(hexStr) {

		var hexStrToken string = ""
		hexStrToken = hexStr[index : index+2]                     // string Type hexToken
		hexToken ,_:= hex.DecodeString(hexStrToken)               // string to byte
		opCodeToken := opcodes.OpCode(hexToken[0])                // byte to OpCode(struct)
		var operator string = opcodes.String(opCodeToken) // operator

		switch {
		case opcodes.IsPush(opCodeToken): // operator is Push
			var numOfOperand int = 0
			numOfOperand = int(opCodeToken - 0x5f)
			var operand string = "0x" + hexStr[index+2:index+2+(2*numOfOperand)] // operand

			fmt.Print(operator + " ")
			fmt.Println(operand)
			index += 2 + (2 * numOfOperand)

		case len(opcodes.String(opCodeToken)) == 0:
			fmt.Printf("0x%x \n",hexToken)
			index += 2

		default:
			fmt.Println(operator + " ")
			index += 2

		}

	}


}
func numOfOperand (hexToken string) int{
	intHexToken , _ := strconv.ParseUint(hexToken,16,64)
	return int(intHexToken) - 0x5f
}

func getOperand (index int,hexStr string,hexToken string) string{
	return hexStr[index+2 : (index+2)+2*numOfOperand(hexToken)]
}

func isOverWritable(keepRewrite bool,hexToken string,index int ,hexStr string) bool{
	return keepRewrite && numOfOperand(hexToken) ==2 && isNextOpJumpI (index,hexStr)
}

func isNextOpJumpI(index int,hexStr string) bool{
	nextIndex := index +6
	var nextOpToken string = hexStr[nextIndex:nextIndex+2]
	return nextOpToken == "57"
}


func getRewriteOperandJumpI(hexStr string) string{
	lastAddress := int(math.Round(float64(len(hexStr)) / 2))
	stringAddress := fmt.Sprintf("%x",lastAddress)


	if len(stringAddress) % 2 ==0{
	}else{
		stringAddress = "0" + stringAddress
	}
	return stringAddress
}

func voidTrail (index int ,hexStr string) string{
	return hexStr[index:len(hexStr)]
}

func binRewrite(code []byte) string {

	var reWritedCode []byte
	var index int = 0
	var keepRewrite = true
	var fallbackOperand []byte
	var hexToken byte = 1

	for  hexToken != 0 {
		hexToken = code[index]
		decodedHexToken, _ := hex.DecodeString(hexToken)
		opcode := opcodes.OpCode(decodedHexToken[0])
		if opcodes.IsPush(opcode) {
			operand := getOperand(index, hexStr, hexToken)
			var rewriteOperand string = operand
			if isOverWritable(keepRewrite, hexToken, index, hexStr) {
				fallbackOperand = operand
				rewriteOperand = getRewriteOperandJumpI(hexStr)
				keepRewrite = false
			}
			index = (index + 2) + 2*numOfOperand(hexToken)
			reWritedStr += hexToken + rewriteOperand
		} else {
			reWritedStr += hexToken
			index += 2
		}
	}
	if fallbackOperand !=""{
		reWritedStr = reWritedStr + voidTrail(index, hexStr) + fmt.Sprintf("%x",opcodes.JUMPDEST) + fmt.Sprintf("%x",opcodes.PUSH2) +fallbackOperand+ fmt.Sprintf("%x",opcodes.JUMPI)
	}else{
		reWritedStr = reWritedStr + voidTrail(index, hexStr)
	}
	return reWritedStr
}




func main() {
	//hexStr := "6080604052600436106100705760003560e01c80633ccfd60b1161004e5780633ccfd60b146100ed5780634b449cba1461011c57806391f9015714610147578063d57bde791461019e57610070565b80631998aeef146100755780632a24f46c1461007f57806338af3eed14610096575b600080fd5b61007d6101c9565b005b34801561008b57600080fd5b506100946103e9565b005b3480156100a257600080fd5b506100ab6105dd565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156100f957600080fd5b50610102610602565b604051808215151515815260200191505060405180910390f35b34801561012857600080fd5b50610131610726565b6040518082815260200191505060405180910390f35b34801561015357600080fd5b5061015c61072c565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156101aa57600080fd5b506101b3610752565b6040518082815260200191505060405180910390f35b600154421115610241576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260168152602001807f41756374696f6e20616c726561647920656e6465642e0000000000000000000081525060200191505060405180910390fd5b60035434116102b8576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601e8152602001807f546865726520616c7265616479206973206120686967686572206269642e000081525060200191505060405180910390fd5b6000600354146103345760035460046000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055505b33600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550346003819055507ff4757a49b326036464bec6fe419a4ae38c8a02ce3e68bf0809674f6aab8ad3003334604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390a1565b600154421015610461576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260168152602001807f41756374696f6e206e6f742079657420656e6465642e0000000000000000000081525060200191505060405180910390fd5b600560009054906101000a900460ff16156104c7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260238152602001806107596023913960400191505060405180910390fd5b6001600560006101000a81548160ff0219169083151502179055507fdaec4582d5d9595688c8c98545fdd1c696d41c6aeaeb636737e84ed2f5c00eda600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16600354604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390a16000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc6003549081150290604051600060405180830381858888f193505050501580156105da573d6000803e3d6000fd5b50565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600080600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020549050600081111561071d576000600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055503373ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f1935050505061071c5780600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055506000915050610723565b5b60019150505b90565b60015481565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6003548156fe61756374696f6e456e642068617320616c7265616479206265656e2063616c6c65642ea2646970667358221220c79240beccc4fab21d04c405d33abb772b8b05a4fceda5ab8923afc9d5ec173964736f6c63430006060033"
	//fmt.Println(len(hexStr))
	//dasm_printer(hexStr)
	var test = [1]byte{0x6f}
	fmt.Println(reflect.TypeOf(test[0]))
	//fmt.Println(len(result))
	//fmt.Println(result)

}
