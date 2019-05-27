using System;
using System.Collections.Generic;
using System.Linq;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Geom.Util;
using Fmg.Core.Types;

namespace Fmg.Core.Img {

    /// <summary> MVC model of <see cref="EMosaicGroup"/> representable as image </summary>
    public class MosaicGroupModel : AnimatedImageModel {

        private EMosaicGroup? _mosaicGroup;
        public static bool varMosaicGroupAsValueOthers1 = !true;
        /// <summary> triangle -> quadrangle -> hexagon -> anew triangle -> ... </summary>
        private readonly int[] _nmArray = { 3, 4, 6 };
        private int _nmIndex1 = 0, _nmIndex2 = 1;
        private double _incrementSpeedAngle;

        public MosaicGroupModel(EMosaicGroup? mosaicGroup) { _mosaicGroup = mosaicGroup; }

        public EMosaicGroup? MosaicGroup {
            get { return _mosaicGroup; }
            set { _notifier.SetProperty(ref _mosaicGroup, value); }
        }

        internal int[] NmArray => _nmArray;

        internal double IncrementSpeedAngle {
            get { return _incrementSpeedAngle; }
            set { _incrementSpeedAngle = value; }
        }

        internal int NmIndex1 {
            get { return _nmIndex1; }
            set { _nmIndex1 = value; }
        }

        internal int NmIndex2 {
            get { return _nmIndex2; }
            set { _nmIndex2 = value; }
        }

        public IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords {
            get {
                var mosaicGroup = MosaicGroup;
                return (mosaicGroup == null)
                    ? Coords_MosaicGroupAsType
                    : (mosaicGroup != EMosaicGroup.eOthers)
                        ? new Tuple<Color, IEnumerable<PointDouble>>[] { new Tuple<Color, IEnumerable<PointDouble>>(ForegroundColor, Coords_MosaicGroupAsValue) }
                        : varMosaicGroupAsValueOthers1
                            ? Coords_MosaicGroupAsValueOthers1
                            : Coords_MosaicGroupAsValueOthers2;
            }
        }

        private IEnumerable<PointDouble> Coords_MosaicGroupAsValue {
            get {
                var pad = Padding;
                double sq = Math.Min( // size inner square
                    Size.Width - pad.LeftAndRight,
                    Size.Height - pad.TopAndBottom);
                var mosaicGroup = MosaicGroup;
                var vertices = 3 + mosaicGroup.Value.Ordinal(); // verticles count
                var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);

                var ra = RotateAngle;
                if (mosaicGroup != EMosaicGroup.eOthers)
                    return FigureHelper.GetRegularPolygonCoords(vertices, sq / 2, center, ra);

                return FigureHelper.GetRegularStarCoords(4, sq / 2, sq / 5, center, ra);

                //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, RotateAngle, ra);
                //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, RotateAngle, 0).RotateBySide(2, center, ra);

                //return FigureHelper.GetFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, RotateAngle, ra);
                //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 3.5, 2, center, RotateAngle, 0).RotateBySide(2, center, ra);

                //var nm = GetNM(_nmIndex1);
                //return FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm.Item1, nm.Item2, sq / 2, center, _incrementSpeedAngle, 0).RotateBySide(2, center, ra);
            }
        }

        private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords_MosaicGroupAsValueOthers1 {
            get {
                var pad = Padding;
                double sq = Math.Min( // size inner square
                   Size.Width - pad.LeftAndRight,
                   Size.Height - pad.TopAndBottom);
                var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);


                var nm1 = GetNM(NmIndex1);
                var nm2 = GetNM(NmIndex2);
                var isa = IncrementSpeedAngle;
                var ra = RotateAngle;
                var sideNum = 2;
                var radius = sq / 3.7; // подобрал.., чтобы не вылазило за периметр изображения
                var sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

                const bool byRadius = false;
                // высчитываю координаты двух фигур.
                // с одинаковым размером одной из граней.
                var res1 = (byRadius
                                ? FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm1.Item1, nm1.Item2, radius, center, isa, 0)
                                : FigureHelper.GetFlowingToTheRightPolygonCoordsBySide  (nm1.Item1, nm1.Item2, sizeSide, sideNum, center, isa, 0))
                            .RotateBySide(sideNum, center, ra)
                            .ToList();
                var res2 = (byRadius
                                ? FigureHelper.GetFlowingToTheRightPolygonCoordsByRadius(nm2.Item1, nm2.Item2, radius, center, isa, 0)
                                : FigureHelper.GetFlowingToTheRightPolygonCoordsBySide  (nm2.Item1, nm2.Item2, sizeSide, sideNum, center, isa, 0))
                            .RotateBySide(sideNum, center, ra + 180) // +180° - разворачиваю вторую фигуру, чтобы не пересекалась с первой фигурой
                            .ToList();

                // и склеиваю грани:
                //  * нахожу середины граней
                var p11 = res1[sideNum - 1]; var p12 = res1[sideNum];
                var p21 = res2[sideNum - 1]; var p22 = res2[sideNum];
                var centerPoint1 = new PointDouble((p11.X + p12.X) / 2, (p11.Y + p12.Y) / 2);
                var centerPoint2 = new PointDouble((p21.X + p22.X) / 2, (p21.Y + p22.Y) / 2);

                //  * и совмещаю их по центру изображения
                var offsetToCenter1 = new PointDouble(center.X - centerPoint1.X, center.Y - centerPoint1.Y);
                var offsetToCenter2 = new PointDouble(center.X - centerPoint2.X, center.Y - centerPoint2.Y);
                var fgClr = ForegroundColor;
                bool pl = PolarLights;
                return  new Tuple<Color, IEnumerable<PointDouble>>[] {
                        new Tuple<Color, IEnumerable<PointDouble>>(   fgClr                                 , res1.Move(offsetToCenter1)),
                        new Tuple<Color, IEnumerable<PointDouble>>(pl
                                                                    ? new HSV(fgClr).AddHue(180).ToColor()
                                                                    : fgClr                                 , res2.Move(offsetToCenter2)),
                };
            }
        }

        private Tuple<int, int> GetNM(int index) {
            int[] nmArray = NmArray;
            int n = nmArray[index];
            int m = nmArray[(index + 1) % nmArray.Length];

            // Во вторую половину вращения фиксирую значение N равно M.
            // Т.к. в прервую половину, с 0 до 180, N стремится к M - см. описание FigureHelper.GetFlowingToTheRightPolygonCoordsByXxx...
            // Т.е. при значении 180 значение N уже достигло M.
            // Фиксирую для того, чтобы при следующем инкременте параметра index, значение N не менялось. Т.о. обеспечиваю плавность анимации.
            if (IncrementSpeedAngle >= 180) {
                if (AnimeDirection)
                    n = m;
            } else {
                if (!AnimeDirection)
                    n = m;
            }
            return new Tuple<int, int>(n, m);
        }

        private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords_MosaicGroupAsType {
            get {
                const bool accelerateRevert = true; // ускорение под конец анимации, иначе - в начале...

                var shapes = 4; // 3х-, 4х-, 5ти- и 6ти-угольники

                var angle = RotateAngle;
                //var angleAccumulative = angle;
                var anglePart = 360.0 / shapes;

                var pad = Padding;
                var sqMax = Math.Min( // размер квадрата куда будет вписана фигура при 0°
                      Size.Width - pad.LeftAndRight,
                      Size.Height - pad.TopAndBottom);
                var sqMin = sqMax / 7; // размер квадрата куда будет вписана фигура при 360°
                var sqDiff = sqMax - sqMin;

                var center = new PointDouble(Padding.Left + (Size.Width - Padding.LeftAndRight) / 2.0,
                                             Padding.Top + (Size.Height - Padding.TopAndBottom) / 2.0);

                var fgClr = ForegroundColor;
                bool pl = PolarLights;
                return Enumerable.Range(0, shapes)
                    .Select(shapeNum => {
                        var vertices = 3 + shapeNum;
                        var angleShape = FixAngle(angle + shapeNum * anglePart);
                        //angleAccumulative = Math.sin(FigureHelper.toRadian(angle/4))*angleAccumulative; // accelerate / ускоряшка..

                        var sq = angleShape * sqDiff / 360;
                        // (un)comment next line to view result changes...
                        sq = Math.Sin((angleShape / 4).ToRadian()) * sq; // accelerate / ускоряшка..
                        sq = accelerateRevert
                            ? sqMin + sq
                            : sqMax - sq;

                        var radius = sq / 1.8;

                        var clr = !pl
                            ? fgClr
                            : new HSV(fgClr).AddHue(+angleShape).ToColor(); // try: -angleShape

                        return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(
                            sq,
                            new Tuple<Color, IEnumerable<PointDouble>>(
                                clr,
                                FigureHelper.GetRegularPolygonCoords(
                                    vertices,
                                    radius,
                                    center,
                                    45 // try to view: angleAccumulative
                                )
                            )
                        );
                    })
                    .OrderBy(x => -x.Item1)
                    .Select(x => x.Item2);
            }
        }

        private IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords_MosaicGroupAsValueOthers2 {
            get {
                var pad = Padding;
                var sq = Math.Min( // size inner square
                      Size.Width  - pad.LeftAndRight,
                      Size.Height - pad.TopAndBottom);
                var radius = sq / 2.7;

                var shapes = 3; // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур: треугольники, квадраты и шестигранники

                var angle = RotateAngle;
                var anglePart = 360.0 / shapes;

                var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);
                var zero = new PointDouble(0, 0);
                var fgClr = ForegroundColor;
                bool pl = PolarLights;
                return Enumerable.Range(0, shapes)
                    .Select(shapeNum => {
                        var angleShape = angle * shapeNum;

                        // adding offset
                        var offset = FigureHelper.GetPointOnCircle(sq / 5, angleShape + shapeNum * anglePart, zero);
                        var centerStar = new PointDouble(center.X + offset.X, center.Y + offset.Y);

                        var clr = !pl
                            ? fgClr
                            : new HSV(fgClr).AddHue(shapeNum * anglePart).ToColor();

                        int vertices;
                        switch (shapeNum) { // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур:
                        case 0: vertices = 6; break; // шестигранники
                        case 1: vertices = 4; break; // квадраты
                        case 2: vertices = 3; break; // и треугольники
                        default: throw new Exception();
                        }
                        return new Tuple<double, Tuple<Color, IEnumerable<PointDouble>>>(
                            1.0, // const value (no ordering). Provided for the future...
                            new Tuple<Color, IEnumerable<PointDouble>>(
                                clr,
                                FigureHelper.GetRegularPolygonCoords(vertices, radius, centerStar, -angle)
                            )
                        );
                    })
                    .OrderBy(x => x.Item1)
                    .Select(x => x.Item2);
            }
        }

    }

}
