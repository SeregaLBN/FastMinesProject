namespace Fmg.Common.Geom {

    public struct Size {

        public int Width, Height;

        //public Size() { Width=Height=0; }
        public Size(int width, int height) { Width = width; Height = height; }
        public Size(Size c) { Width = c.Width; Height = c.Height; }

        public static bool operator !=(Size s1, Size s2) { return (s1.Width != s2.Width) || (s1.Height != s2.Height); }
        public static bool operator ==(Size s1, Size s2) { return (s1.Width == s2.Width) && (s1.Height == s2.Height); }

        public override bool Equals(object other) {
            if (ReferenceEquals(null, other))
                return false;
            return (other is Size) && (this == (Size)other);
        }

        public override int GetHashCode() {
            int sum = Width + Height;
            return sum * (sum + 1) / 2 + Height;
        }

        public override string ToString() {
            return "{w:" + Width + ", h:" + Height + "}";
        }

    }

}
