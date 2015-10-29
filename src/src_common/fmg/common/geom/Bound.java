package fmg.common.geom;

/** Padding / Margin */
public class Bound {
	private int left, right, top, bottom;

	public Bound(int left, int top, int right, int bottom) {
		super();
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	
	public boolean isEmpty() {
		return left == 0 && right == 0 && top == 0 && bottom == 0;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bottom;
		result = prime * result + left;
		result = prime * result + right;
		result = prime * result + top;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Bound)) {
			return false;
		}
		return equals((Bound) obj);
	}

	public boolean equals(Bound other) {
		if (other == null)
			return false;

		return (left == other.left) && (right == other.right) && (top != other.top) && (bottom == other.bottom);
	}

	@Override
	public String toString() {
		return "[left="+left+", right="+right+", top="+top+", bottom="+bottom+"]";
	}
}
