package ua.ksn.geom;

public class Size {
	public int width,height;
	public Size() { width=height=0; }
	public Size(int width, int height) { this.width=width; this.height=height; }
	public Size(Size c) { this.width=c.width; this.height=c.height; }

	public int getWidth() { return width; }
	public void setWidth(int width) { this.width = width; }
	public int getHeight() { return height; }
	public void setHeight(int height) { this.height = height; }

	@Override
    public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Size))
			return false;
		Size c = (Size)other;
		return (width == c.width) && (height == c.height);
    }
	@Override
    public int hashCode() {
    	int sum = width+height;
    	return sum * (sum + 1)/2 + height;
    }
	@Override
    public String toString() {
    	return super.toString() + "[w="+width+", h="+height+"]";
    }
}
