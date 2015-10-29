namespace fmg.common.geom
{
   /// <summary>
   /// Padding / Margin
   /// </summary>
   public struct Bound
   {
      public Bound(int left, int top, int right, int bottom)
      {
         Left = left;
         Top = top;
         Right = right;
         Bottom = bottom;
      }

      public int Left { get; set; }
      public int Right { get; set; }
      public int Top { get; set; }
      public int Bottom { get; set; }

      public bool Equals(Bound other)
      {
         return this == other;
      }

      public override bool Equals(object other)
      {
         if (ReferenceEquals(null, other))
            return false;
         return (other is Bound) && (this == (Bound)other);
      }
      public override int GetHashCode()
      {
         unchecked
         {
            var hashCode = Bottom;
            hashCode = (hashCode * 397) ^ Left;
            hashCode = (hashCode * 397) ^ Right;
            hashCode = (hashCode * 397) ^ Top;
            return hashCode;
         }
      }
      public override string ToString() { return $"Left={Left}; Top={Top}; Right={Right}; Bottom={Bottom}"; }

      public static bool operator ==(Bound t1, Bound t2)
      {
         return (t1.Left == t2.Left) && (t1.Top == t2.Top) && (t1.Right == t2.Right) && (t1.Bottom == t2.Bottom);
      }

      public static bool operator !=(Bound t1, Bound t2)
      {
         return (t1.Left != t2.Left) || (t1.Top != t2.Top) || (t1.Right != t2.Right) || (t1.Bottom != t2.Bottom);
      }
   }
}
