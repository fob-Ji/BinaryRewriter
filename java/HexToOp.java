
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.math.*;

public class HexToOp {

	
	static HashMap<String,String> hexMap = new HashMap<>();
	
	public static int isHasOperand(String opCode) {
		if(opCode.contains("PUSH")) {
			return Integer.parseInt((opCode.substring(4)));
		}
		else {
			return -1;
		}
	}
	
	public static void dasm_printer(String HexStr) {
		int index = 0;
		do {
			String hexToken = HexStr.substring(index, index+2); // operator
			try {
				int numOfOperand = isHasOperand(hexMap.get("0x"+hexToken)); // Opcode의 operand 수
				String operator = hexMap.get("0x"+hexToken);
				if(numOfOperand !=-1) {
					String operand = HexStr.substring(index+2,(index+2)+2*numOfOperand); //operand
					System.out.print(operator+" "); // 
					System.out.println("0x"+operand);
					index = (index+2)+2*numOfOperand;
				}//push일때
				else {
					System.out.println(operator+" ");
					index +=2;
				}//push가 아닐때 
			}catch(Exception e) {
				System.out.println("0x"+hexToken);
				index +=2;
				//Hex값이 opCode랑 매치 안될 때
			}
		}while(index < HexStr.length());
	}
	public static void main(String[] args) throws IOException {
		FileReader fr = new FileReader("./solcOPCode.txt");
		BufferedReader br = new BufferedReader(fr);
		String OPMapToken = br.readLine();
		
		System.out.println(OPMapToken);
		
		while((OPMapToken = br.readLine())!=null) {
			hexMap.put(OPMapToken.split(" +")[0],OPMapToken.split(" +")[2]);
			
		}
		//opCode Map 생성
		
		fr = new FileReader("./sample_sol_Coin.bin");
		br = new BufferedReader(fr);
		FileWriter fw = new FileWriter("./sample_sol_Coin_rewrite.bin",true);
		String HexStr = br.readLine();
		//HexOutput File 읽음
		System.out.println(HexStr);
		String hexToken = "";
		//Hex를 opcode 단위로 1바이트씩 읽음
		int index = 0;
		boolean keepingRewrite = true;
		String fallbackOPCode = "";
		String fallbackOperand = "";
		String fallbackJumpI = "";
		do {
			hexToken = HexStr.substring(index, index+2); // operator
			try {
				int numOfOperand = isHasOperand(hexMap.get("0x"+hexToken)); // Opcode의 operand 수				
				if(numOfOperand !=-1) {
					String operand = HexStr.substring(index+2,(index+2)+2*numOfOperand); //operand
					if(keepingRewrite) {
						if(numOfOperand ==2) { //push2일때
							int nextOpIndex = (index+2)+2*numOfOperand; // 다음 Opcode index
							String nextOpHex = HexStr.substring(nextOpIndex, nextOpIndex+2); //다음 opcode hex 
							if(hexMap.get("0x"+nextOpHex).contains("JUMPI")) { //다음Opcode 가 jumpi일때 rewrite
								fallbackOPCode = hexToken; 
								fallbackOperand = operand;
								fallbackJumpI = nextOpHex;
								fw.append(hexToken); // push2
								fw.append("0"+Long.toHexString(Math.round((long)HexStr.length()/2))); // 제일 끝에
								keepingRewrite = false;
							}
							else { // 다음 opcode가 jumpi가 아닐때 평소처럼 진행
								fw.append(hexToken);
								fw.append(operand);
							}
						}
						else { // push2가 아닐때
							fw.append(hexToken);
							fw.append(operand);							
						}
					}
					else {
						fw.append(hexToken);
						fw.append(operand);
					}
					index = (index+2)+2*numOfOperand;
				}//push일때
				else {
					fw.append(hexToken); 
					index +=2;
				}//push가 아닐때 그냥 opcode만 쓰기
			}catch(Exception e) {
				fw.append(hexToken);
				index +=2;
				//Hex값이 opCode랑 매치 안될 때
			}
		}while(index < HexStr.length());
		if(!keepingRewrite) {
			
			fw.append("5b"); // append jumpdest 
			fw.append(fallbackOPCode);
			fw.append(fallbackOperand);
			fw.append(fallbackJumpI);
		}
		
		
		br = new BufferedReader(new FileReader("./sample_sol_Coin_rewrite.bin"));
		System.out.println("rewriter\n"+br.readLine());
		System.out.println(HexStr.length());
		
		dasm_printer(HexStr);
		fr.close();
		br.close();
		fw.close();

	}
}
