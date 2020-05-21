
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
				int numOfOperand = Integer.parseInt(hexToken,16) - 0x5f; // Opcode?? operand ??
				String operator = hexMap.get("0x"+hexToken);
				if(isPush(hexToken)) {
					String operand = HexStr.substring(index+2,(index+2)+2*numOfOperand); //operand
					System.out.print(operator+" "); //
					System.out.println("0x"+operand);
					index = (index+2)+2*numOfOperand;
				}//push???
				else {
					System.out.println(operator+" ");
					index +=2;
				}//push?? ????
			}catch(Exception e) {
				System.out.println("0x"+hexToken);
				index +=2;
				//Hex???? opCode?? ??? ??? ??
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
		//opCode Map ????
		fr.close();
		br.close();

	}

	public static boolean isPush(String hexToken) {
		int binaryToken = Integer.parseInt(hexToken,16);
		if(binaryToken > 0x5f && binaryToken < 0x80) {
			return true;
		}
		else {
			return false;
		}
	}//opCode ?? push???? u???? operand ???? ????

	public static boolean isValidOpcode(String hexToken) {
		return hexMap.get("0x"+hexToken)!= null;
	}

	int numOfOperand(String hexToken) {
		return Integer.parseInt(hexToken,16) - 0x5f;
	}

	String getOperand (int index, String hexStr, String hexToken) {
		return hexStr.substring(index+2,(index+2)+2*numOfOperand(hexToken));
	}
	
	boolean isJumpI (int index, String hexStr){
		int nextIndex = (index+2) + 4; 				// 2+numOfOperand
		String nextOpToken = hexStr.substring(nextIndex, nextIndex + 2);
		return (nextOpToken.equals("57")); 
	}

	boolean getRewriteOperandJumpI (String hexStr){
		String lastAddress = Integer.toHexString(Math.round((float)hexStr.length()/2));
		rewriteOperand = (lastAddress.length()%2)==0 ? lastAddress : "0"+lastAddress; 
	}

	boolean  isOverWritable (isOverWritable(keepingRewrite, hexToken, index, hexStr)) {
				return  keepingRewrite 
						&& numOfOperand(hexToken) == 2  
						&& isJumpI(index, hexStr) ;
	}

	String voidTrail(int index, String hexStr) { 
		return hexStr.substring(index,hexStr.length() - 1);
	}
	String hex (String opcode ) {
		if (opcode == "JUMPDEST") 
			return"5b";
		if (opcode == "PUSH2")
			return "61";
		if (opcode == "JUMPI")
			return "57";
	}
					
	public static String binary_rewrite_main(String hexStr) {
		String hexToken = "";
		String reWritedStr = "";
		int index = 0;

		boolean keepingRewrite = true;
		String fallbackOperand = "";


		do {
			hexToken = hexStr.substring(index, index+2); // operator
			try { 
				if(isPush(hexToken)) {
					String rewriteOperand = operand;
					String operand = getOperand(index, hexStr, hexToken);

					if (isOverWritable(keepingRewrite, hexToken, index, hexStr)) {
						fallbackOperand = operand;
						rewriteOperand = getRewriteOperandJumpI();

						keepingRewrite = false;
					}

					index = (index + 2) + 2 * numOfOperand(hexToken);
					reWritedStr += hexToken + rewriteOperand;

				} else {
					reWritedStr += hexToken;
					index+=2;
				}

			} catch(Exception e) { // outOfindex
				reWritedStr = reWritedStr 
								+ voidTrail(index, hexstr)
								+ hex("JUMPDEST") 
								+ hex("PUSH2") + fallbackOperand 
								+ hex("JUMPI");

				System.out.println("reWrite Complete");
				break;
			}

		} while(index < hexStr.length());

		return reWritedStr;
	}

	public static void main(String[] args) throws IOException {

		createOpMap(); //static Hashmap create

		try (	FileReader fr = new FileReader("./sample_sol_Coin.bin"),
				BufferedReader br = new BufferedReader(fr);
				FileWriter fw = new FileWriter("./sample_sol_Coin_rewrite.bin",true) ) {

			String hexStr = br.readLine();
			String reWritedBin = binary_rewrite_main(hexStr);
			fw.write(reWritedBin);

			System.out.println(reWritedBin.length());
			dasm_printer(reWritedBin);

		} finally {
			fr.close();
			br.close();
			fw.close();
		}
}
