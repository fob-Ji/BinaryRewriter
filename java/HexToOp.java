
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.math.*;

public class HexToOp {


	static HashMap<String,String> hexMap = new HashMap<>(); // key:bin, value:opCode map



	public static void dasm_printer(String HexStr) {
		int index = 0;

		do {
			String hexToken = HexStr.substring(index, index+2); // operator
			try {
				int numOfOperand = Integer.parseInt(hexToken,16) - 0x5f; // Opcode의 operand 수
				String operator = hexMap.get("0x"+hexToken);
				if(isPush(hexToken)) {
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

	public static void createOpMap() throws IOException {
		FileReader fr = new FileReader("./solcOPCode.txt");
		BufferedReader br = new BufferedReader(fr);
		String OPMapToken = br.readLine();

		System.out.println(OPMapToken);

		while((OPMapToken = br.readLine())!=null) {
			hexMap.put(OPMapToken.split(" +")[0],OPMapToken.split(" +")[2]);
		}
		//opCode Map 생성
		fr.close();
		br.close();

	}

	public static boolean isPush(String hexToken) {
		int binaryToken = Integer.parseInt(hexToken,16);
		if(binaryToken > 0x5f && binaryToken < 0x80) {
			return true;
//			return Integer.parseInt((opCode.substring(4)));
		}
		else {
			return false;
		}
	}//opCode 가 push인지 체크해서 operand 수를 리턴

	public static boolean isValidOpcode(String hexToken) {
		return hexMap.get("0x"+hexToken)!= null;
	}

	public static String binary_rewrite_main(String hexStr) {
		String hexToken = "";
		//Hex를 opcode 단위로 1바이트씩 읽음
		String reWritedStr = "";
		int index = 0;
		boolean keepingRewrite = true;
		String fallbackOPCode = "";
		String fallbackOperand = "";
		String fallbackJumpI = "";
		do {
			hexToken = hexStr.substring(index, index+2); // operator
			try {//hexStr의 마지막 outOfIndex 처리를 위함
//				int numOfOperand = isHasOperand(hexMap.get("0x"+hexToken)); // Opcode의 operand 수
				if(isPush(hexToken)) {
					int numOfOperand = Integer.parseInt(hexToken,16) - 0x5f;
					String operand = hexStr.substring(index+2,(index+2)+2*numOfOperand);
					if(keepingRewrite) {
						if(numOfOperand == 2 ) {
							int nextIndex = (index+2)+4;//2+numOfOperand
							String nextOpToken = hexStr.substring(nextIndex,nextIndex+2);
							if(nextOpToken.equals("57")) { // nextOpToken == jumpI
								fallbackOPCode = hexToken;
								fallbackOperand = operand;
								fallbackJumpI = nextOpToken;
								keepingRewrite = false;
								System.out.println(hexStr.length());
								String lastAddress = Integer.toHexString(Math.round((float)hexStr.length()/2));
								lastAddress = (lastAddress.length()%2)==0 ? lastAddress : "0"+lastAddress; // 짝수자리의 binary를 써주기 위함
								reWritedStr += hexToken + lastAddress;
								index = (index+2) + 2*numOfOperand;
							}else {
								reWritedStr += hexToken + operand;
								index = (index+2) + 2*numOfOperand;
							}
						}else {//일반 Push
							reWritedStr += hexToken + operand;
							index = (index+2) + 2*numOfOperand;
						}
					}
					else {
						reWritedStr += hexToken + operand;
						index = (index+2) + 2*numOfOperand;
					}
				}else if(isValidOpcode(hexToken)) {
					reWritedStr += hexToken;
					index+=2;
				}else {
					reWritedStr += hexToken;
					index+=2;
				}
//				if(numOfOperand !=-1) {
//					String operand = HexStr.substring(index+2,(index+2)+2*numOfOperand); //operand
//					if(keepingRewrite) {
//						if(numOfOperand ==2) { //push2일때
//							int nextOpIndex = (index+2)+2*numOfOperand; // 다음 Opcode index
//							String nextOpHex = HexStr.substring(nextOpIndex, nextOpIndex+2); //다음 opcode hex
//							if(hexMap.get("0x"+nextOpHex).contains("JUMPI")) { //다음Opcode 가 jumpi일때 rewrite
//								fallbackOPCode = hexToken;
//								fallbackOperand = operand;
//								fallbackJumpI = nextOpHex;
//								fw.append(hexToken); // push2
//								fw.append("0"+Long.toHexString(Math.round((long)HexStr.length()/2))); // 제일 끝에
//								keepingRewrite = false;
//							}
//							else { // 다음 opcode가 jumpi가 아닐때 평소처럼 진행
//								fw.append(hexToken);
//								fw.append(operand);
//							}
//						}
//						else { // push2가 아닐때
//							fw.append(hexToken);
//							fw.append(operand);
//						}
//					}
//					else {
//						fw.append(hexToken);
//						fw.append(operand);
//					}
//					index = (index+2)+2*numOfOperand;
//				}//push일때
//				else {
//					fw.append(hexToken);
//					index +=2;
//				}//push가 아닐때 그냥 opcode만 쓰기
			}catch(Exception e) { // outOfindex
				reWritedStr += hexStr.substring(index,hexStr.length()-1); // 끝까지 다 기록한 후에
				reWritedStr += "5b"+fallbackOPCode+ fallbackOperand+fallbackJumpI;
				System.out.println("reWrite Complete");
				break;
				//Hex값이 opCode랑 매치 안될 때
			}
		}while(index < hexStr.length());
		System.out.println("reWrited Bin :");
		System.out.println(reWritedStr);
//		if(!keepingRewrite) {
//
//			fw.append("5b"); // append jumpdest
//			fw.append(fallbackOPCode);
//			fw.append(fallbackOperand);
//			fw.append(fallbackJumpI);
//		}


//		br = new BufferedReader(new FileReader("./sample_sol_Coin_rewrite.bin"));
		return reWritedStr;
	}



	public static void main(String[] args) throws IOException {

		createOpMap(); //static Hashmap create


		FileReader fr = new FileReader("./sample_sol_Coin.bin");
		BufferedReader br = new BufferedReader(fr);
		FileWriter fw = new FileWriter("./sample_sol_Coin_rewrite.bin",true);
		String hexStr = br.readLine();
		String reWritedBin = binary_rewrite_main(hexStr);
		fw.write(reWritedBin);

//
//		System.out.println("rewriter\n"+br.readLine());
//		System.out.println(HexStr.length());
//
		System.out.println(reWritedBin.length());
		dasm_printer(reWritedBin);
		fr.close();
		br.close();
		fw.close();


	}
}
