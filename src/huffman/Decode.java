package huffman;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Decode {
	public static void main(String args[]) throws Exception {
		String source = args[0];
		File file = new File(source);
		
			
			String target = args[1];
			
			generateOutputFile(file);
	}

	public static void generateOutputFile(File file) throws FileNotFoundException, IOException {
			
			FileInputStream stream = new FileInputStream(file);
			BufferedInputStream bufferedStream = new BufferedInputStream(stream);
			DataInputStream in = new DataInputStream(bufferedStream);

			int numCharacters = in.readByte();
			System.out.println(numCharacters);
			
			// Get Lengths
			Map<Integer, List<Character>> prefixCodes = new HashMap<Integer, List<Character>>();
			for(int i = 0; i < numCharacters; i++) {
				Character c = Character.valueOf((char)in.readByte());
				Integer length = new Integer(in.readByte());
				
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
			
			// Canonize
			Map<Character, String> canonized = Util.canonize(prefixCodes);
			System.out.println(canonized);
			Map<String, Character> codeMapping = Util.swap(canonized);
			System.out.println(codeMapping);
			
			String binaryString = "";
			while (in.available() != 0) {
				int b = in.readUnsignedByte();
				String newString = addLeadingZeros(b);
				binaryString = binaryString + newString;
				for(int i = 1; i < binaryString.length(); i++) {
					String subString = binaryString.substring(0, i);
					Character c = codeMapping.get(subString);
					
					if(c != null) {
						System.out.println(c);
						binaryString = binaryString.substring(i, binaryString.length());
						i = 1;
					}
				}
			}
			stream.close();
			bufferedStream.close();
			in.close();
		}
	
	private static String addLeadingZeros(int r) {

		String newString = Integer.toBinaryString(r);
		int len = newString.length();
		
		if(len < 8) {
			newString = String.format("%0" + (8-len) + "d", 0) + newString;
		}
		return newString;
		}
	}

