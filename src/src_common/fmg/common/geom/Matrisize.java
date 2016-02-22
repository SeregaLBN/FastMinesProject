package fmg.common.geom;

/**
 * Matrix Size - a new dimension for the particular type of matrix, so as not to be confused with the similar types Size / Dimension (size in pixels).
 * m rows × n columns.
 * Ввёл отдельный тип для размерности матрицы, чтобы не путать со похожими типами Size/Dimension (размеры в пикселях)
 */
public class Matrisize {
   /** rows (width) */
   public int m;
   /** columns (height) */
   public int n;

   public Matrisize() { m=n=0; }
   public Matrisize(int m, int n) { this.m=m; this.n=n; }
   public Matrisize(Matrisize size) { this.m=size.m; this.n=size.n; }

   @Override
   public boolean equals(Object other) {
      if (this == other)
         return true;
      if (!(other instanceof Matrisize))
         return false;
      Matrisize c = (Matrisize)other;
      return (m == c.m) && (n == c.n);
   }

   @Override
   public int hashCode() {
      int sum = m+n;
      return sum * (sum + 1)/2 + n;
   }

   @Override
   public String toString() {
      return super.toString() + "[m="+m+", n="+n+"]";
   }

}
