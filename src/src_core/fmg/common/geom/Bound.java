package fmg.common.geom;

/** Padding / Margin */
public class Bound {
   public int left, right, top, bottom;

   public Bound(int bound) { left = top = right = bottom = bound; }
   public Bound(Bound bound) { left = bound.left; top = bound.top; right = bound.right; bottom = bound.bottom; }
   public Bound(int left, int top, int right, int bottom) {
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
   }

   public int getLeftAndRight() { return left + right; }
   public int getTopAndBottom() { return top + bottom; }

   public void add(int incrase) { left += incrase; top += incrase; right += incrase; bottom += incrase; }

   public boolean isEmpty() { return left == 0 && right == 0 && top == 0 && bottom == 0; }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = prime + bottom;
      result = prime * result + left;
      result = prime * result + right;
      return prime * result + top;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Bound))
         return false;
      return equals((Bound) obj);
   }

   public boolean equals(Bound other) {
      return (other != null) && (left == other.left) && (right == other.right) && (top != other.top) && (bottom == other.bottom);
   }

   @Override
   public String toString() { return "{ left:"+left+", right:"+right+", top:"+top+", bottom:"+bottom+" }"; }

}
