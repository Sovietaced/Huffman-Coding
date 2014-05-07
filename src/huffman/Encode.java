package huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Performs Huffman Encoding on a source file.
 * 
 * @author Jason Parraga <Sovietaced@gmail.com>
 */
public class Encode {

	public static void main(String args[]) throws Exception {
		String source = args[0];
		File file = new File(source);
		try {
			Map<Character, Integer> frequencies = calculateFrequencies(file);
			List<HuffmanCode> codes = new ArrayList<HuffmanCode>();
			
			// Generate wrappers...
			for (Entry<Character, Integer> entry : frequencies.entrySet()) {
				codes.add(new HuffmanCode(entry.getKey().charValue(), entry.getValue().intValue()));
			}
			
			// Sort the wrappers...
			SortedSet<HuffmanCode> sorted = new TreeSet<HuffmanCode>(codes);
			
			// Insert sorted wrappers into priority queue
			PriorityQueue<BinaryTree> pq = new PriorityQueue<BinaryTree>();
			for(HuffmanCode hc : sorted) {
				BinaryTree tree = new BinaryTree(hc);
				pq.add(tree);
			}
			
			BinaryTree huffmanTree = generateHuffmanTree(pq);
			huffmanTree.dump();
			
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		String target = args[1];
	}

	/**
	 * Calculates a mapping of character -> frequency given an input file
	 * @param file the input file to read
	 * @return a mapping of character -> frquency
	 * @throws IOException
	 */
	public static Map<Character, Integer> calculateFrequencies(File file) throws IOException {
		Map<Character, Integer> frequencies = new HashMap<Character, Integer>();

		try (InputStream in = new FileInputStream(file);
				Reader reader = new InputStreamReader(in, Charset.defaultCharset());
				Reader buffer = new BufferedReader(reader)) {
			int r;
			// -1 means EOF
			while ((r = reader.read()) != -1) {
				Character c = new Character((char) r);
				
				// Fastest to get and check for null
				Integer i = frequencies.get(c);
				if(i != null) {
					frequencies.put(c, new Integer(i+1));
				}
				else{
					frequencies.put(c, new Integer(1));
				}
			}
		}
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
}
