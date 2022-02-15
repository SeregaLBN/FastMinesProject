using System;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Mosaic.Cells;
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic {

    public static class MosaicHelper {

        public const double AreaPrecision = 0.001;

        /// <summary>Создать экземпляр атрибута для конкретного типа мозаики</summary>
        public static BaseShape CreateShapeInstance(EMosaic mosaicType) {
            switch (mosaicType) {
            case EMosaic.eMosaicTriangle1        : return new ShapeTriangle1        ();
            case EMosaic.eMosaicTriangle2        : return new ShapeTriangle2        ();
            case EMosaic.eMosaicTriangle3        : return new ShapeTriangle3        ();
            case EMosaic.eMosaicTriangle4        : return new ShapeTriangle4        ();
            case EMosaic.eMosaicSquare1          : return new ShapeSquare1          ();
            case EMosaic.eMosaicSquare2          : return new ShapeSquare2          ();
            case EMosaic.eMosaicParquet1         : return new ShapeParquet1         ();
            case EMosaic.eMosaicParquet2         : return new ShapeParquet2         ();
            case EMosaic.eMosaicTrapezoid1       : return new ShapeTrapezoid1       ();
            case EMosaic.eMosaicTrapezoid2       : return new ShapeTrapezoid2       ();
            case EMosaic.eMosaicTrapezoid3       : return new ShapeTrapezoid3       ();
            case EMosaic.eMosaicRhombus1         : return new ShapeRhombus1         ();
            case EMosaic.eMosaicQuadrangle1      : return new ShapeQuadrangle1      ();
            case EMosaic.eMosaicPenrousePeriodic1: return new ShapePenrousePeriodic1();
            case EMosaic.eMosaicPentagonT24      : return new ShapePentagonT24      ();
            case EMosaic.eMosaicPentagonT5       : return new ShapePentagonT5       ();
            case EMosaic.eMosaicPentagonT10      : return new ShapePentagonT10      ();
            case EMosaic.eMosaicHexagon1         : return new ShapeHexagon1         ();
            case EMosaic.eMosaicTrSq1            : return new ShapeTrSq1            ();
            case EMosaic.eMosaicTrSq2            : return new ShapeTrSq2            ();
            case EMosaic.eMosaicSqTrHex          : return new ShapeSqTrHex          ();
            default:
                throw new Exception("Unknown type " + mosaicType);
            }
        }

        /// <summary>Создать экземпляр ячейки для конкретного типа мозаики</summary>
        public static BaseCell CreateCellInstance(BaseShape shape, EMosaic mosaicType, Coord coord) {
            BaseCell cell = CreateCell(shape, mosaicType, coord);
            cell.Init();
            return cell;
        }

        private static BaseCell CreateCell(BaseShape shape, EMosaic mosaicType, Coord coord) {
            switch (mosaicType) {
            case EMosaic.eMosaicTriangle1        : return new Triangle1        ((ShapeTriangle1        )shape, coord);
            case EMosaic.eMosaicTriangle2        : return new Triangle2        ((ShapeTriangle2        )shape, coord);
            case EMosaic.eMosaicTriangle3        : return new Triangle3        ((ShapeTriangle3        )shape, coord);
            case EMosaic.eMosaicTriangle4        : return new Triangle4        ((ShapeTriangle4        )shape, coord);
            case EMosaic.eMosaicSquare1          : return new Square1          ((ShapeSquare1          )shape, coord);
            case EMosaic.eMosaicSquare2          : return new Square2          ((ShapeSquare2          )shape, coord);
            case EMosaic.eMosaicParquet1         : return new Parquet1         ((ShapeParquet1         )shape, coord);
            case EMosaic.eMosaicParquet2         : return new Parquet2         ((ShapeParquet2         )shape, coord);
            case EMosaic.eMosaicTrapezoid1       : return new Trapezoid1       ((ShapeTrapezoid1       )shape, coord);
            case EMosaic.eMosaicTrapezoid2       : return new Trapezoid2       ((ShapeTrapezoid2       )shape, coord);
            case EMosaic.eMosaicTrapezoid3       : return new Trapezoid3       ((ShapeTrapezoid3       )shape, coord);
            case EMosaic.eMosaicRhombus1         : return new Rhombus1         ((ShapeRhombus1         )shape, coord);
            case EMosaic.eMosaicQuadrangle1      : return new Quadrangle1      ((ShapeQuadrangle1      )shape, coord);
            case EMosaic.eMosaicPenrousePeriodic1: return new PenrousePeriodic1((ShapePenrousePeriodic1)shape, coord);
            case EMosaic.eMosaicPentagonT24      : return new PentagonT24      ((ShapePentagonT24      )shape, coord);
            case EMosaic.eMosaicPentagonT5       : return new PentagonT5       ((ShapePentagonT5       )shape, coord);
            case EMosaic.eMosaicPentagonT10      : return new PentagonT10      ((ShapePentagonT10      )shape, coord);
            case EMosaic.eMosaicHexagon1         : return new Hexagon1         ((ShapeHexagon1         )shape, coord);
            case EMosaic.eMosaicTrSq1            : return new TrSq1            ((ShapeTrSq1            )shape, coord);
            case EMosaic.eMosaicTrSq2            : return new TrSq2            ((ShapeTrSq2            )shape, coord);
            case EMosaic.eMosaicSqTrHex          : return new SqTrHex          ((ShapeSqTrHex          )shape, coord);
            default:
                throw new Exception("Unknown type " + mosaicType);
            }
        }


    #region calc dimension window region

        /// <summary> Поиск больше-меньше </summary>
        /// <param name="baseDelta">начало дельты приращения</param>
        /// <param name="comparable">ф-ция сравнения</param>
        /// <returns>что найдено</returns>
        private static int FinderI(int baseDelta, Func<int, int> comparable) {
            double res = baseDelta;
            double d = baseDelta;
            bool deltaUp = true;
            do {
                var cmp = comparable((int)res);
                //System.Diagnostics.Debug.WriteLine("d={0}{1}; res={2}; cmp={3}", deltaUp ? '↑' : '↓', d, res, cmp);
                // Example:
                // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
                // init data:  res=100 d=100
                //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
                //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
                //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
                //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
                //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
                //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
                if (cmp == 0)
                    break;
                if ((d < 1) && (cmp == -1))
                    break;

                var resultUp = (cmp < 0);
                deltaUp = deltaUp && resultUp;
                if (deltaUp)
                    d *= 2;
                else
                    d /= 2;
                if (resultUp)
                    res += d;
                else
                    res -= d;
            } while (true);
            return (int)res;
        }

        private static double FinderD(double baseDelta, Func<double, double> comparable) {
            double res = baseDelta;
            double d = baseDelta;
            bool deltaUp = true;
            do {
                var cmp = comparable(res);
                //System.Diagnostics.Debug.WriteLine("d={0}{1}; res={2}; cmp={3}", deltaUp ? '↑' : '↓', d, res, cmp);
                // Example:
                // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
                // init data:  res=100 d=100
                //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
                //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
                //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
                //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
                //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
                //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
                if (cmp.HasMinDiff(0))
                    break;
                if ((d < AreaPrecision) && cmp.HasMinDiff(-1))
                    break;

                var resultUp = (cmp < 0);
                deltaUp = deltaUp && resultUp;
                if (deltaUp)
                    d *= 2;
                else
                    d /= 2;
                if (resultUp)
                    res += d;
                else
                    res -= d;
            } while (true);
            return res;
        }

        /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
        /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
        /// <param name="sizeClient"> размер окна/области (в пикселях):
        /// in - в которую должна вписаться мозаика;
        /// out - в которую реально впишется мозаика;
        /// </param>
        /// <returns>площадь ячейки</returns>
        private static double FindAreaBySize(BaseShape shape, Matrisize mosaicSizeField, ref SizeDouble sizeClient) {
            // сделал приватным, т.к. неявно меняет свойства параметра 'shape'

            if (Double.IsNaN(sizeClient.Height) || Double.IsNaN(sizeClient.Width))
                throw new ArgumentException("sizeClient must be defined");

            if (sizeClient.Height <= 0 || sizeClient.Width <= 0)
                throw new ArgumentException("sizeClient must be positive");

            var sizeClientCopy = sizeClient;
            var sizeIter = new SizeDouble();
            int iterations = 0;
            var res = FinderD(2000,
                area => {
                    System.Diagnostics.Debug.Assert(++iterations < 100);
                    shape.Area = area;
                    sizeIter = shape.GetSize(mosaicSizeField);
                    if (sizeIter.Width.HasMinDiff(sizeClientCopy.Width) && // меньше с минимальными различиями
                        (sizeIter.Width  <= sizeClientCopy.Width) &&       // less with minimal differences
                        (sizeIter.Height <= sizeClientCopy.Height))
                        return 0;
                    if ((sizeIter.Width  <= sizeClientCopy.Width) &&        // меньше с минимальными различиями
                        (sizeIter.Height <= sizeClientCopy.Height) &&       // less with minimal differences
                        sizeIter.Height.HasMinDiff(sizeClientCopy.Height))
                        return 0;
                    if ((sizeIter.Width < sizeClientCopy.Width) &&
                        (sizeIter.Height < sizeClientCopy.Height))
                        return -1;
                    return +1;
                });
            System.Diagnostics.Debug.Assert(sizeIter.Width <= sizeClient.Width);
            System.Diagnostics.Debug.Assert(sizeIter.Height <= sizeClient.Height);
            sizeClient = sizeIter;
            return res;
        }

        /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
        /// <param name="mosaicType">mosaic type</param>
        /// <param name="area">cell area of interest</param>
        /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
        /// <returns>размер поля мозаики</returns>
        public static Matrisize FindSizeByArea(EMosaic mosaicType, double area, SizeDouble sizeClient) {
            if (Double.IsNaN(sizeClient.Height) || Double.IsNaN(sizeClient.Width))
                throw new ArgumentException("sizeClient must be defined");

            if (sizeClient.Height <= 0 || sizeClient.Width <= 0)
                throw new ArgumentException("sizeClient must be positive");

            var shape = CreateShapeInstance(mosaicType);
            shape.Area = area;
            var result = new Matrisize();
            FinderI(2000, newWidth => {
                result.m = newWidth;
                var sizeWnd = shape.GetSize(result);
                if (sizeWnd.Width.HasMinDiff(sizeClient.Width))
                    return 0;
                if (sizeWnd.Width < sizeClient.Width)
                    return -1;
                return +1;
            });
            FinderI(2000, newHeight => {
                result.n = newHeight;
                var sizeWnd = shape.GetSize(result);
                if (sizeWnd.Height.HasMinDiff(sizeClient.Height))
                    return 0;
                if (sizeWnd.Height < sizeClient.Height)
                    return -1;
                return +1;
            });
            return result;
        }

        /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
        /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
        /// <param name="sizeClient"> размер окна/области (в пикселях):
        /// in - в которую должна вписаться мозаика;
        /// out - в которую реально впишется мозаика;
        /// </param>
        /// <returns>площадь ячейки</returns>
        public static double FindAreaBySize(EMosaic mosaicType, Matrisize mosaicSizeField, ref SizeDouble sizeClient) {
            return FindAreaBySize(CreateShapeInstance(mosaicType), mosaicSizeField, ref sizeClient);
        }

        /// <summary>The size in pixels where to place the matrix</summary>
        public static SizeDouble GetSize(EMosaic mosaicType, double area, Matrisize mosaicSizeField) {
            var shape = CreateShapeInstance(mosaicType);
            shape.Area = area;
            return shape.GetSize(mosaicSizeField);
        }

    #endregion

    }

}
