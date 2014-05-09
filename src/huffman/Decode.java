package huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Decode {
	public static void main(String args[]) throws Exception {
		String source = args[0];
		File file = new File(source);
		try {
		
			
			String target = args[1];
			
			generateOutputFile(file);

		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public static void generateOutputFile(File file) throws FileNotFoundException, IOException {

		try (InputStream in = new FileInputStream(file);
				Reader reader = new InputStreamReader(in,
						Charset.defaultCharset());
				Reader buffer = new BufferedReader(reader)) {
			int numCharacters = buffer.read();
			System.out.println(numCharacters);
			
			Map<Integer, List<Character>> prefixCodes = new HashMap<Integer, List<Character>>();
			for(int i = 0; i < numCharacters; i++) {
				Character c = Character.valueOf((char)buffer.read());
				Integer length = new Integer(buffer.read());
				
				List<Character> chars = prefixCodes.get(length);
				
				if(chars != null) {
					chars.add(c);
				} else {
					chars = new ArrayList<Character>();
					chars.add(c);
					prefixCodes.put(length, chars);
				}
			}
			
			for(List<Character> chars : prefixCodes.values()) {
				Collections.sort(chars);
			}
			
			Map<String, Character> codeMapping = canonize(prefixCodes);
			System.out.println(codeMapping);
			
			int r;
			String binaryString = "";
			//System.out.println(buffer.read)
			// -1 means EOF
			while ((r = buffer.read()) != -1) {
				System.out.println("r " + r);
				String newString = addLeadingZeros(r);
				System.out.println("NS " + newString);
				binaryString = binaryString + newString;
				System.out.println("BS " + binaryString);
				for(int i = 1; i < binaryString.length(); i++) {
					String subString = binaryString.substring(0, i);
					Character c = codeMapping.get(subString);
					
					if(c != null) {
						System.out.println("YAY: " + c);
						binaryString = binaryString.substring(i, binaryString.length());
						System.out.println(binaryString);
						i = 1;
					}
				}
			}
		}
	}
	
	private static String addLeadingZeros(int r) {
		if(r > 255) {
			
			byte signedByte = -1;
			r = signedByte & (0xff);
		}
		String newString = Integer.toBinaryString(r);
		System.out.println(Integer.parseInt(newString, 2));
		System.out.println("ALZ " + newString);
		int len = newString.length();
		
		if(len < 8) {
			newString = String.format("%0" + (8-len) + "d", 0) + newString;
		}
		return newString;
	}

	public static Map<String, Character> canonize(Map<Integer, List<Character>> lengthToCharacters) {
		Map<String, Character> prefixCodes = new HashMap<String, Character>();
		List<Integer> reverse = new ArrayList<Integer>(lengthToCharacters.keySet());
		Collections.reverse(reverse);
		
		int firstLen = reverse.get(0);
		String base = String.format("%0" + firstLen + "d", 0);
		
		for(Integer i : reverse) {
			// Chop off if necessary
			int diff = base.length() - i.intValue();
			base = base.substring(diff, base.length());
			List<Character> chars = lengthToCharacters.get(i);
			for(int j = 0; j < chars.size(); j++) {
				prefixCodes.put(base, chars.get(j));
				base = incrementBinaryString(base);
			}
		}
		return prefixCodes;
	}
	
	public static String incrementBinaryString(String base) {
		String binaryString = Integer.toBinaryString(Integer.parseInt(base, 2) + 1);
		if(base.length() - binaryString.length() > 0) {
			return String.format("%0" + (base.length() - binaryString.length()) + "d", 0) + binaryString;
		}
		return binaryString;
		
	}
}
