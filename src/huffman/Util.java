package huffman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
	public static Map<Character, String> canonize(Map<Integer, List<Character>> charLengths) {
		Map<Character, String> prefixCodes = new HashMap<Character, String>();
		List<Integer> reverse = new ArrayList<Integer>(charLengths.keySet());
		Collections.reverse(reverse);
		
		int firstLen = reverse.get(0);
		String base = String.format("%0" + firstLen + "d", 0);
		
		for(Integer i : reverse) {
			// Find how many bits to shift
			int diff = base.length() - i.intValue();
			int binaryBase = Integer.parseInt(base);
			base = Integer.toBinaryString(binaryBase >> diff);
			base = adjust(base, i.intValue());
			List<Character> chars = charLengths.get(i);
			for(int j = 0; j < chars.size(); j++) {
				prefixCodes.put(chars.get(j), base);
				base = incrementBinaryString(base, prefixCodes.values());
			}
		}
		return prefixCodes;
	}
	
	public static String adjust(String s, int length) {
		int diff = length - s.length();
		if(diff > 0) {
			return String.format("%0" + diff + "d", 0) + s;
		} else if (diff < 0) {
			diff = Math.abs(diff);
			return s.substring(diff, s.length());
		}
		return s;
	}
	
	public static String incrementBinaryString(String base, Collection<String> collection) {
		
		String binaryString = Integer.toBinaryString(Integer.parseInt(base, 2) + 1);
		int diff = base.length() - binaryString.length();
		
		if(diff > 0) {
			return String.format("%0" + (base.length() - binaryString.length()) + "d", 0) + binaryString;
		}
		else if(base.length() - binaryString.length() < 0){
			return binaryString.substring(0, binaryString.length()-1);
		}
		return binaryString;
	}
	
	//map must be a bijection in order for this to work properly
	public static <K,V> HashMap<V,K> swap(Map<K,V> map) {
	    HashMap<V,K> rev = new HashMap<V, K>();
	    for(Map.Entry<K,V> entry : map.entrySet())
	        rev.put(entry.getValue(), entry.getKey());
	    return rev;
	}
}
