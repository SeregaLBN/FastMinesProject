namespace fmg.common.geom {

    /// <summary>
    /// Matrix Size - a new dimension for the particular type of matrix, so as not to be confused with the similar types Size / Dimension (size in pixels).
    /// m rows × n columns.
    /// Ввёл отдельный тип для размерности матрицы, чтобы не путать со похожими типами Size/Dimension (размеры в пикселях)
    /// </summary>
    public struct Matrisize {
        /// <summary> rows (width) </summary>
        public int m;
        /// <summary> columns (height) </summary>
        public int n;

        //public Matrisize() { m=n=0; }
        public Matrisize(int m, int n) { this.m = m; this.n = n; }
        public Matrisize(Matrisize size) { this.m = size.m; this.n = size.n; }

        public static bool operator !=(Matrisize s1, Matrisize s2) { return (s1.m != s2.m) || (s1.n != s2.n); }
        public static bool operator ==(Matrisize s1, Matrisize s2) { return (s1.m == s2.m) && (s1.n == s2.n); }

        public override bool Equals(object other) {
            if (ReferenceEquals(null, other))
                return false;
            return (other is Matrisize) && (this == (Matrisize)other);
        }
        public override int GetHashCode() {
            int sum = m + n;
            return sum * (sum + 1) / 2 + n;
        }
        public override string ToString() {
            return "{m:" + m + ", n:" + n + "}";
        }
    }

}
