
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.math.*;

public class HexToOp {

	static HashMap<String, String> hexMap = new HashMap<>(); // key:bin, value:opCode map

	public static void dasm_printer(String HexStr) {
		int index = 0;

		do {
			String hexToken = HexStr.substring(index, index + 2); // operator
			try {
				int numOfOperand = Integer.parseInt(hexToken, 16) - 0x5f; // Opcode?? operand ??
				String operator = hexMap.get("0x" + hexToken);
				if (isPush(hexToken)) {
					String operand = HexStr.substring(index + 2, (index + 2) + 2 * numOfOperand); // operand
					System.out.print(operator + " "); //
					System.out.println("0x" + operand);
					index = (index + 2) + 2 * numOfOperand;
				} // push???
				else {
					System.out.println(operator + " ");
					index += 2;
				} // push?? ????
			} catch (Exception e) {
				System.out.println("0x" + hexToken);
				index += 2;
				// Hex???? opCode?? ??? ??? ??
			}
		} while (index < HexStr.length());
	}

	public static void createOpMap() throws IOException {
		FileReader fr = new FileReader("./solcOPCode.txt");
		BufferedReader br = new BufferedReader(fr);
		String OPMapToken = br.readLine();

		System.out.println(OPMapToken);

		while ((OPMapToken = br.readLine()) != null) {
			hexMap.put(OPMapToken.split(" +")[0], OPMapToken.split(" +")[2]);
		}
		// opCode Map ????
		fr.close();
		br.close();

	}

	public static boolean isPush(String hexToken) {
		int binaryToken = Integer.parseInt(hexToken, 16);
		if (binaryToken > 0x5f && binaryToken < 0x80) {
			return true;
		} else {
			return false;
		}
	}// opCode ?? push???? u???? operand ???? ????

	public static boolean isValidOpcode(String hexToken) {
		return hexMap.get("0x" + hexToken) != null;
	}

	static int numOfOperand(String hexToken) {
		return Integer.parseInt(hexToken, 16) - 0x5f;
	}

	static String getOperand(int index, String hexStr, String hexToken) {
		return hexStr.substring(index + 2, (index + 2) + 2 * numOfOperand(hexToken));
	}

	static boolean isNextOpJumpI(int index, String hexStr) {
		int nextIndex = (index + 2) + 4; // 2+numOfOperand
		String nextOpToken = hexStr.substring(nextIndex, nextIndex + 2);
		return (nextOpToken.equals("57"));
	}

	static String getRewriteOperandJumpI(String hexStr) {
		String lastAddress = Integer.toHexString(Math.round((float) hexStr.length() / 2));
		return lastAddress = (lastAddress.length() % 2) == 0 ? lastAddress : "0" + lastAddress;

	}

	static boolean isOverWritable(boolean keepingRewrite, String hexToken, int index, String hexStr) {
		return keepingRewrite && numOfOperand(hexToken) == 2 && isNextOpJumpI(index, hexStr);
	}

	static String voidTrail(int index, String hexStr) {
		return hexStr.substring(index, hexStr.length() - 1);
	}

	static String hex(String opcode) {
		if (opcode == "JUMPDEST")
			return "5b";
		if (opcode == "PUSH2")
			return "61";
		if (opcode == "JUMPI")
			return "57";
		return null;
	}

	public static String binary_rewrite_main(String hexStr) {
		String hexToken = "";
		String reWritedStr = "";
		int index = 0;

		boolean keepingRewrite = true;
		String fallbackOperand = "";

		do {
			hexToken = hexStr.substring(index, index + 2); // operator
			try {
				if (isPush(hexToken)) {
					String operand = getOperand(index, hexStr, hexToken);
					String rewriteOperand = operand;

					if (isOverWritable(keepingRewrite, hexToken, index, hexStr)) {
						fallbackOperand = operand;
						rewriteOperand = getRewriteOperandJumpI(hexStr);

						keepingRewrite = false;
					}

					index = (index + 2) + 2 * numOfOperand(hexToken);
					reWritedStr += hexToken + rewriteOperand;

				} else {
					reWritedStr += hexToken;
					index += 2;
				}

			} catch (Exception e) { // outOfindex
				reWritedStr = reWritedStr + voidTrail(index, hexStr) + hex("JUMPDEST") + hex("PUSH2") + fallbackOperand
						+ hex("JUMPI");

				System.out.println("reWrite Complete");
				break;
			}

		} while (index < hexStr.length());

		return reWritedStr;
	}

	public static void main(String[] args) throws IOException {

		createOpMap(); // static Hashmap create
		System.out.println("608060405234801561001057600080fd5b506004361061004c5760003560e01c8063075461721461005157806327e235e31461009b57806340c10f19146100f3578063d0679d3414610141575b600080fd5b61005961018f565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6100dd600480360360208110156100b157600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506101b4565b6040518082815260200191505060405180910390f35b61013f6004803603604081101561010957600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506101cc565b005b61018d6004803603604081101561015757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610277565b005b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60016020528060005260406000206000915090505481565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461022557610273565b80600160008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055505b5050565b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205410156102c3576103fd565b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254039250508190555080600160008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055507f3990db2d31862302a685e8086b5755072a6e2b5b780af1ee81ece35ee3cd3345338383604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001828152602001935050505060405180910390a15b505056fea264697066735822122050aae312e7504c57c39f448c6507a3db893195624becddfe307ca9a5db0671ac64736f6c63430006060033".length()/2);
		try (FileReader fr = new FileReader("./sample_sol_Coin.bin");
				BufferedReader br = new BufferedReader(fr);
				FileWriter fw = new FileWriter("./sample_sol_Coin_rewrite.bin", true)) {

			String hexStr = br.readLine();
			String reWritedBin = binary_rewrite_main(hexStr);
			System.out.println(reWritedBin);
			fw.write(reWritedBin);

			System.out.println(reWritedBin.length());
			dasm_printer(reWritedBin);

		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
