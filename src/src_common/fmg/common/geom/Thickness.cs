namespace src_fmg.common.geom
{
   /// <summary>
   /// Padding / Margin
   /// </summary>
   public struct Thickness
   {
      public Thickness(int left, int top, int right, int bottom)
      {
         Left = left;
         Top = top;
         Right = right;
         Bottom = bottom;
      }

      public int Bottom { get; set; }
      public int Left { get; set; }
      public int Right { get; set; }
      public int Top { get; set; }

      public bool Equals(Thickness other)
      {
         return this == other;
      }

      public override bool Equals(object other)
      {
         if (ReferenceEquals(null, other))
            return false;
         return (other is Thickness) && (this == (Thickness)other);
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
      public override string ToString() { return $"Bottom={Bottom}; Top={Top}; Right={Right}; Bottom={Bottom}"; }

      public static bool operator ==(Thickness t1, Thickness t2)
      {
         return (t1.Left == t2.Left) && (t1.Top == t2.Top) && (t1.Right == t2.Right) && (t1.Bottom == t2.Bottom);
      }

      public static bool operator !=(Thickness t1, Thickness t2)
      {
         return (t1.Left != t2.Left) || (t1.Top != t2.Top) || (t1.Right != t2.Right) || (t1.Bottom != t2.Bottom);
      }
   }
}
