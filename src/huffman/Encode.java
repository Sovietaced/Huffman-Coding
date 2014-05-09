package huffman;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * Performs Huffman Encoding on a source file.
 * 
 * @author Jason Parraga <Sovietaced@gmail.com>
 */
public class Encode {
	
	// Exclamation mark
	public static final char EOF = (char) 0;
	
	public static void main(String args[]) throws Exception {
		String source = args[0];
		File file = new File(source);
		try {
			Map<Character, Integer> frequencies = calculateFrequencies(file);
			List<HuffmanCode> codes = new ArrayList<HuffmanCode>();

			// Generate wrappers...
			for (Entry<Character, Integer> entry : frequencies.entrySet()) {
				codes.add(new HuffmanCode(entry.getKey().charValue(), entry
						.getValue().intValue()));
			}

			Collections.sort(codes);

			// Insert sorted wrappers into priority queue
			PriorityQueue<BinaryTree> pq = new PriorityQueue<BinaryTree>();
			for (HuffmanCode hc : codes) {
				BinaryTree tree = new BinaryTree(hc);
				pq.add(tree);
			}

			// Generate Huffman Tree
			BinaryTree huffmanTree = generateHuffmanTree(pq);
			
			Map<Integer, List<Character>> charLengths = getCharLengths(huffmanTree, codes);
			Map<Character, String> prefixCodes = canonize(charLengths);
			System.out.println(prefixCodes.entrySet());
				
			String target = args[1];
			
			generateOutputFile(file, target, prefixCodes, charLengths);

		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Calculates a mapping of character -> frequency given an input file
	 * 
	 * @param file
	 *            the input file to read
	 * @return a mapping of character -> frquency
	 * @throws IOException
	 */
	public static Map<Character, Integer> calculateFrequencies(File file)
			throws IOException {
		Map<Character, Integer> frequencies = new HashMap<Character, Integer>();

		try (InputStream in = new FileInputStream(file);
				Reader reader = new InputStreamReader(in,
						Charset.defaultCharset());
				Reader buffer = new BufferedReader(reader)) {
			int r;
			// -1 means EOF
			while ((r = reader.read()) != -1) {
				Character c = new Character((char) r);
				// Fastest to get and check for null
				Integer i = frequencies.get(c);
				if (i != null) {
					frequencies.put(c, new Integer(i + 1));
				} else {
					frequencies.put(c, new Integer(1));
				}
			}
		}
		// Attach EOF
		frequencies.put(EOF, new Integer(1));
		return frequencies;
	}

	public static BinaryTree generateHuffmanTree(PriorityQueue<BinaryTree> pq) {
		while (pq.size() > 1) {

			BinaryTree leftLeaf = pq.poll();
			BinaryTree rightLeaf = pq.poll();

			BinaryTree root = new BinaryTree(leftLeaf, rightLeaf);
			pq.offer(root);
		}

		return pq.poll();
	}
	
	public static Map<Integer, List<Character>> getCharLengths(BinaryTree huffmanTree, List<HuffmanCode> codes) {
		Map<Integer, List<Character>> lengthToCharacters = new HashMap<Integer, List<Character>>();
		
		for (HuffmanCode hc : codes) {
			String binaryString = huffmanTree.getEncoding("", hc);
			int len = new Integer(binaryString.length());
			
			List<Character> characters = lengthToCharacters.get(len);
			Character c = hc.getCharacter();
			if(characters != null) {
				characters.add(c);
			} else {
				characters = new ArrayList<Character>();
				characters.add(c);
				lengthToCharacters.put(len, characters);
			}
		}
		
		for(List<Character> chars : lengthToCharacters.values()) {
			Collections.sort(chars);
		}
		
		return lengthToCharacters;
	}
	
	public static Map<Character, String> canonize(Map<Integer, List<Character>> charLengths) {
		Map<Character, String> prefixCodes = new HashMap<Character, String>();
		List<Integer> reverse = new ArrayList<Integer>(charLengths.keySet());
		Collections.reverse(reverse);
		
		int firstLen = reverse.get(0);
		String base = String.format("%0" + firstLen + "d", 0);
		
		for(Integer i : reverse) {
			// Chop off if necessary
			int diff = base.length() - i.intValue();
			base = base.substring(diff, base.length());
			System.out.println("after chopping: " + i);
			List<Character> chars = charLengths.get(i);
			for(int j = 0; j < chars.size(); j++) {
				System.out.println(chars.get(j));
				System.out.println(base);
				prefixCodes.put(chars.get(j), base);
				base = incrementBinaryString(base, prefixCodes.values());
			}
		}
		return prefixCodes;
	}
	
	public static String incrementBinaryString(String base, Collection<String> collection) {
		
		String binaryString = Integer.toBinaryString(Integer.parseInt(base, 2) + 1);
		if(base.length() - binaryString.length() > 0) {
			return String.format("%0" + (base.length() - binaryString.length()) + "d", 0) + binaryString;
		}
		else if(base.length() - binaryString.length() < 0){
			return binaryString.substring(0, binaryString.length()-1);
		}
		return binaryString;
	}

	public static void generateOutputFile(File file, String target, Map<Character, String> prefixCodes, Map<Integer, List<Character>> charLengths) 
			throws FileNotFoundException, IOException {
		File outputFile = new File(target);
		outputFile.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		BufferedOutputStream output = new BufferedOutputStream(outputStream);
		

		try (InputStream in = new FileInputStream(file);
				Reader reader = new InputStreamReader(in,
						Charset.defaultCharset());
				Reader buffer = new BufferedReader(reader)) {
			
			// Write header
			int numCharacters = prefixCodes.size();
			output.write(numCharacters);
			System.out.println(numCharacters);
			
			for(Entry<Character, String> entry : prefixCodes.entrySet()) {
				Character c = entry.getKey();
				int length = findCharLength(c, charLengths);
				System.out.println(c + " " + length);
				output.write(c.toString().getBytes());
				output.write(length);
			}
			
			int r;
			String binaryString = "";
			// -1 means EOF
			while ((r = reader.read()) != -1) {
				Character c = Character.valueOf((char) r);
				binaryString = binaryString + prefixCodes.get(c);
				System.out.println("just got " + c);
				System.out.println("binary string state: " + binaryString);
				if(binaryString.length() == 8) {
					System.out.println(Integer.parseInt(binaryString, 2));
					System.out.println("WRITING SWAG " + binaryString.getBytes().length);
					output.write(Integer.parseInt(binaryString, 2));
					binaryString = "";
				} else if (binaryString.length() > 8) {
					String toWrite = binaryString.substring(0, 8);
					System.out.println(Integer.parseInt(binaryString, 2));
					System.out.println("WRITING " + toWrite);
					output.write(Integer.parseInt(toWrite, 2));
					binaryString = binaryString.replace(toWrite, "");
				}		
			}
			System.out.println("EOFING");
			// Append EOF to end
			binaryString = binaryString + prefixCodes.get(new Character(EOF));
			
			if(binaryString.length() == 8) {
				System.out.println("WRITING AFTER EOF " + Integer.parseInt(binaryString, 2));
				System.out.println("WRITING AFTER EOF " + binaryString);
				output.write(Integer.parseInt(binaryString, 2));
				binaryString = "";
			} else if (binaryString.length() > 8) {
				String toWrite = binaryString.substring(0, 7);
				System.out.println("WRITING " + toWrite);
				output.write(Integer.parseInt(toWrite, 2));
				binaryString = binaryString.substring(8, binaryString.length()-1);
			}
			
			if(binaryString.length() > 0) {
				System.out.println("WRITING WHATS LEFT " + Integer.parseInt(binaryString, 2));
				output.write(Integer.parseInt(binaryString, 2));
			}
			
			output.flush();
			output.close();
		}
	}

	private static int findCharLength(Character c,
			Map<Integer, List<Character>> charLengths) {
		for(Entry<Integer, List<Character>> entry : charLengths.entrySet()) {
			if(entry.getValue().contains(c)) {
				return entry.getKey();
			}
		}
		System.out.println("broken");
		return -1;	
	}
}
