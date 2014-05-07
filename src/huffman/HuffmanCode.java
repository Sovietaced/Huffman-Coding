package huffman;

import java.util.Comparator;

public class HuffmanCode implements Comparable<HuffmanCode> {
	
	private char c;
	private int frequency;
	
	public HuffmanCode(char c, int frequency) {
		this.c = c;
		this.frequency = frequency;
	}
	
	public int getFrequency() {
		return this.frequency;
	}
	
	public String toString() {
		return String.format("Huffman code c=%s frequency=%s", c, frequency);
	}

	@Override
	public int compareTo(HuffmanCode o) {
		return Integer.compare(frequency, o.frequency);
	}
	
	public int compareTo(int frequency) {
		return Integer.compare(this.frequency, frequency);
	}
}
