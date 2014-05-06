package huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
			for(Entry<Character, Integer> e : frequencies.entrySet()) {
			}
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
}
