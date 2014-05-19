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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
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
		long start = System.nanoTime();
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
			
			// Generate canonized values
			Map<Integer, List<Character>> charLengths = getCharLengths(huffmanTree, codes);
			Map<Character, String> prefixCodes = Util.canonize(charLengths);
				
			String target = args[1];
			generateOutputFile(file, target, prefixCodes);
			
			long diff = System.nanoTime() - start;
			System.out.println("Finished in " + TimeUnit.NANOSECONDS.toMillis(diff) + " ms.");

		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Calculates a mapping of character -> frequency given an input file
	 * 
	 * @param file the input file to read
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
	
	/**
	 * Generates the huffman tree
	 * @param pq a min priority queue of all the codes
	 * @return a huffman tree
	 */
	public static BinaryTree generateHuffmanTree(PriorityQueue<BinaryTree> pq) {
		while (pq.size() > 1) {

			BinaryTree leftLeaf = pq.poll();
			BinaryTree rightLeaf = pq.poll();

			BinaryTree root = new BinaryTree(leftLeaf, rightLeaf);
			pq.offer(root);
		}

		return pq.poll();
	}
	
	/**
	 * Generates a mapping of character to code lengths based on a huffman tree
	 * @param huffmanTree the tree to traverse to find encoding
	 * @param codes the huffman codes to search for
	 * @return
	 */
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

	/**
	 * Generates a huffman encoded file. 
	 * @param file The input file to encode
	 * @param target The output file
	 * @param prefixCodes The encoded values
	 * @param charLengths the lengths of the encoded values
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void generateOutputFile(File file, String target, Map<Character, String> prefixCodes) 
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
			
			// Write frequencies
			for(Entry<Character, String> entry : prefixCodes.entrySet()) {
				Character c = entry.getKey();
				int length = entry.getValue().length();
				output.write(c.toString().getBytes());
				output.write(length);
			}
			
			// Read file and write output
			int r;
			String binaryString = "";
			// -1 means EOF
			while ((r = reader.read()) != -1) {
				Character c = Character.valueOf((char) r);
				binaryString = binaryString + prefixCodes.get(c);
				binaryString = write(binaryString, output);
			}
			
			// Finished writing file input. Append EOF to end
			binaryString = binaryString + prefixCodes.get(new Character(EOF));
			binaryString = write(binaryString, output);
			
			// Handle overflow
			if(binaryString.length() > 0) {
				output.write(Integer.parseInt(binaryString, 2));
			}
			
			output.flush();
			output.close();
		}
	}
	
	/**
	 * Helper that deals with writing binary strings.
	 * @param binaryString the binary string to write
	 * @param output the file to write to
	 * @return the resulting binaryString that hasn't been written
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private static String write(String binaryString, BufferedOutputStream output) throws NumberFormatException, IOException {
		if(binaryString.length() == 8) {
			output.write(Integer.parseInt(binaryString, 2));
			binaryString = "";
		} else if (binaryString.length() > 8) {
			String toWrite = binaryString.substring(0, 8);
			output.write(Integer.parseInt(toWrite, 2));
			binaryString = binaryString.replace(toWrite, "");
		}
		return binaryString;
	}
}
