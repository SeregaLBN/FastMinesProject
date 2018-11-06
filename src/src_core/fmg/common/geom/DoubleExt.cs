using System;

namespace fmg.common.geom {

    public static class DoubleExt {

        /** Equals N digit precision */
        public static bool EqualsPrecision(this double value1, double value2, double precision = 0.00001) {
            return Math.Abs(value1 - value1) <= precision;
        }

        /// <summary> Минимально различны? Has minimal differences
        /// https://msdn.microsoft.com/en-us/library/ya2zha7s%28v=vs.110%29.aspx </summary>
        public static bool HasMinDiff(this double value1, double value2, uint units = 1) {
            //return Math.Abs(value1 - value2) <= (1.0/ (10 * units));
            var lValue1 = BitConverter.DoubleToInt64Bits(value1);
            var lValue2 = BitConverter.DoubleToInt64Bits(value2);

            // If the signs are different, return false except for +0 and -0.
            if ((lValue1 >> 63) != (lValue2 >> 63)) {
                return value1.Equals(value2);
            }

            var diff = Math.Abs(lValue1 - lValue2);
            return (diff <= units);
        }

    }

}
