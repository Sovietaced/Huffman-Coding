package huffman;

public class BinaryTree implements Comparable<BinaryTree> {
	
	private BinaryTree left;
	private BinaryTree right;
	private Object value;
	
	public BinaryTree(HuffmanCode hc) {
		this.value = hc;
	}
	
	public BinaryTree(BinaryTree left, BinaryTree right) {
		this.left = left;
		this.right = right;
		if(left.getValue() instanceof HuffmanCode) {
			HuffmanCode leftValue = (HuffmanCode) left.getValue();
			if(right.getValue() instanceof HuffmanCode) {
				HuffmanCode rightValue = (HuffmanCode) right.getValue();
				this.value = (leftValue.getFrequency() + rightValue.getFrequency());
			} else {
				this.value = (leftValue.getFrequency() + (int) right.getValue());
			}
		} else {
			int leftValue = (int) left.getValue();
			if(right.getValue() instanceof HuffmanCode) {
				HuffmanCode rightValue = (HuffmanCode) right.getValue();
				this.value = (leftValue + rightValue.getFrequency());
			} else {
				this.value = (leftValue + (int) right.getValue());
			}
		}
	}
	
	public BinaryTree getLeft() {
		return left;
	}
	public void setLeft(BinaryTree left) {
		this.left = left;
	}
	public BinaryTree getRight() {
		return right;
	}
	public void setRight(BinaryTree right) {
		this.right = right;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * In order traversal toString
	 */
	public void dump() {
		if(this.left != null) {
			this.left.dump();
		}
		System.out.println(this.value);
		if(this.right != null) {
			this.right.dump();
		}
	}

	@Override
	public int compareTo(BinaryTree o) {
		if(this.value instanceof HuffmanCode) {
			HuffmanCode thisCode = (HuffmanCode) this.value;
			if(o.value instanceof HuffmanCode) {
				HuffmanCode otherCode = (HuffmanCode) o.value;
				return thisCode.compareTo(otherCode);
			}
			else {
				int other = (int) o.value;
				return thisCode.compareTo(other);
			}
		} else {
			int thisFrequency = (int) this.value;
			if(o.value instanceof HuffmanCode) {
				HuffmanCode otherCode = (HuffmanCode) o.value;
				return Integer.compare(thisFrequency, otherCode.getFrequency());
			} else {
				int other = (int) o.value;
				return Integer.compare(thisFrequency, other);
			}
		}
	}
}