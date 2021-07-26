namespace Fmg.Common.Geom {

    /// <summary> Padding / Margin </summary>
    public struct BoundDouble {

        public BoundDouble(BoundDouble copy) { Left = copy.Left; Top = copy.Top; Right = copy.Right; Bottom = copy.Bottom; }
        public BoundDouble(double left, double top, double right, double bottom) {
            Left   = left;
            Top    = top;
            Right  = right;
            Bottom = bottom;
        }

        public BoundDouble(double bound) {
            Left = Top = Right = Bottom = bound;
        }

        public double Left   { get; set; }
        public double Right  { get; set; }
        public double Top    { get; set; }
        public double Bottom { get; set; }

        public double LeftAndRight => Left + Right;
        public double TopAndBottom => Top + Bottom;

        public SizeDouble LeftTopOffset => new SizeDouble(Left, Top);

        public bool Equals(BoundDouble other) {
            return this == other;
        }

        public override bool Equals(object other) {
            if (ReferenceEquals(null, other))
                return false;
            return (other is BoundDouble) && (this == (BoundDouble)other);
        }

        public override int GetHashCode() {
            unchecked {
                var hashCode = Left.GetHashCode();
                hashCode = (hashCode * 397) ^ Right.GetHashCode();
                hashCode = (hashCode * 397) ^ Top.GetHashCode();
                hashCode = (hashCode * 397) ^ Bottom.GetHashCode();
                return hashCode;
            }
        }

        public override string ToString() {
            return string.Format("{{Left:{0:0.00}, Top:{1:0.00}, Right:{2:0.00}, Bottom:{3:0.00}}}", Left, Top, Right, Bottom);
        }


        public static bool operator ==(BoundDouble t1, BoundDouble t2) {
            return t1.Left.HasMinDiff(t2.Left) && t1.Top.HasMinDiff(t2.Top) && t1.Right.HasMinDiff(t2.Right) && t1.Bottom.HasMinDiff(t2.Bottom);
        }

        public static bool operator !=(BoundDouble t1, BoundDouble t2) {
            return !t1.Left.HasMinDiff(t2.Left) || !t1.Top.HasMinDiff(t2.Top) || !t1.Right.HasMinDiff(t2.Right) || !t1.Bottom.HasMinDiff(t2.Bottom);
        }

    }

}
