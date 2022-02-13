////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "BaseCell.cs"
//
// Реализация базового класса BaseCell
// Author: 2002-2018  -  Serhii Kryvulia aka SeregaLBN
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using System.ComponentModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Types;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary>Базовый класс фигуры-ячейки</summary>
    public abstract class BaseCell {

        public const double PI = 3.14159265358979323846; // Math.PI;
        public static readonly double SQRT2   = Math.Sqrt(2.0);
        public static readonly double SQRT3   = Math.Sqrt(3.0);
        public static readonly double SQRT27  = Math.Sqrt(27.0);
        public static readonly double SQRT48  = Math.Sqrt(48.0);
        public static readonly double SQRT147 = Math.Sqrt(147.0);
        public static readonly double SIN15   = Math.Sin(PI / 180.0 * 15.0);
        public static readonly double SIN18   = Math.Sin(PI / 180.0 * 18.0);
        public static readonly double SIN36   = Math.Sin(PI / 180.0 * 36.0);
        public static readonly double SIN54   = Math.Sin(PI / 180.0 * 54.0);
        public static readonly double SIN72   = Math.Sin(PI / 180.0 * 72.0);
        public static readonly double SIN75   = Math.Sin(PI / 180.0 * 75.0);
        public static readonly double SIN99   = Math.Sin(PI / 180.0 * 99.0);
        public static readonly double TAN15   = Math.Tan(PI / 180.0 * 15.0);
        public static readonly double TAN45_2 = Math.Tan(PI / 180.0 * 45.0 / 2);
        public static readonly double SIN135a = Math.Sin(PI / 180.0 * 135.0 - Math.Atan(8.0 / 3));

        /// <summary>
        /// Контекст/метаданные, описывающий общие хар-ки для каждого из экземпляров BaseCell.
        /// (Полные данные о конкретной мозаике)
        /// Доопределаяется наследниками BaseCell</summary>
        public abstract class BaseAttribute : INotifyPropertyChanged, IDisposable {

            /// <summary>площадь ячейки/фигуры</summary>
            private double _area = 500;
            protected bool Disposed { get; private set; }
            public event PropertyChangedEventHandler PropertyChanged {
                add    { _notifier.PropertyChanged += value;  }
                remove { _notifier.PropertyChanged -= value;  }
            }
            protected readonly NotifyPropertyChanged _notifier;

            protected BaseAttribute() {
                _notifier = new NotifyPropertyChanged(this);
            }

            /// <summary>площадь ячейки/фигуры</summary>
            public double Area {
                get { return _area; }
                set {
                    if (_area.HasMinDiff(value))
                        return;
                    _notifier.SetProperty(ref this._area, value);
                }
            }

            /// <summary>размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
            /// на основе заданных параметров</summary>
            public abstract double GetSq(double borderWidth);

            /// <summary>значение A (базовая величина фигуры - обычно это размер одной из сторон фигуры) по заданной площади фигуры</summary>
            public abstract double A { get; }

            /// <summary>get parent container (owner window) size in pixels</summary>
            public abstract SizeDouble GetSize(Matrisize sizeField);

            /// <summary>размер поля из группы ячеек состоящих из разных direction</summary>
            public abstract Size GetDirectionSizeField();
            /// <summary>кол-во direction'ов, которые знает данный тип мозаики</summary>
            public int GetDirectionCount() { Size s = GetDirectionSizeField(); return s.Width * s.Height; }

            /// <summary>кол-во соседей у ячейки конкретной направленности</summary>
            public abstract int GetNeighborNumber(int direction);
            /// <summary>из скольки точек/вершин состоит фигура конкретной направленности</summary>
            public abstract int GetVertexNumber(int direction);
            /// <summary>сколько фигур пересекается в одной вершине (в среднем)</summary>
            public abstract double GetVertexIntersection();

            /// <summary>макс кол-во режимов заливки фона, которые знает данный тип мозаики
            /// (знает ф-ция BaseCell::GetCellFillColor() или её наследующая)
            /// (Не считая режима заливки цветом фона по-умолчанию...)</summary>
            public virtual int GetMaxCellFillModeValue() {
                return 19;
            }

            public void Dispose() {
                _notifier.Dispose();
            }

        }

        private readonly BaseAttribute attr;
        public BaseAttribute Attr { get { return attr; } }

        protected Coord coord;
        /// <summary>направление - 'третья координата' ячейки</summary>
        protected int direction;

        /// <summary>вписанный в фигуру квадрат - область в которую выводится изображение/текст</summary>
        public abstract RectDouble GetRcInner(double borderWidth);
        /// <summary>вернёт прямоугольник в который вписана фигура ячейки</summary>
        public RectDouble GetRcOuter() {
            var rcOuter = region.GetBounds();
            //rcOuter.Height++; rcOuter.Width++; // чтобы при repaint'е захватило и крайние границы
            return rcOuter;
        }

        /// <summary>массив координат точек из которых состоит фигура</summary>
        protected RegionDouble region;

        public class StateCell {

            public StateCell() {
                Reset();
            }

            public EState Status { get; set; } // _Open, _Close
            public EOpen  Open   { get; set; } // _Nil, _1, ... _21, _Mine
            public EClose Close  { get; set; } // _Unknown, _Clear, _Flag

            /// <summary>Нажата? Не путать с open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки</summary>
            public bool Down { get; set; }

            public void Reset() {
                Status = EState._Close;
                Open   = EOpen._Nil;
                Close  = EClose._Clear;
                Down   = false;
            }

        }
        private StateCell state;

        /// <summary>запретить установку мины на данную ячейку</summary>
        private bool lockMine;

        public void SetMine() {
            if (this.lockMine || (state.Open == EOpen._Mine))
                throw new InvalidOperationException("Illegal usage");
            state.Open = EOpen._Mine;
        }

        public StateCell State { get { return state; }
                                 set { state = value; }}

        protected BaseCell(
              BaseAttribute attr,
              Coord coord,
              int iDirection)
        {
            this.attr = attr;
            this.coord = coord;
            this.direction = iDirection;
            this.region = new RegionDouble(attr.GetVertexNumber(iDirection));

            this.state = new StateCell();
            Reset();
        }

        public void Init() {
            CalcRegion();
        }

        /// <summary>координаты соседей</summary>
        public abstract IList<Coord> GetCoordsNeighbor();

        public Coord GetCoord() { return coord; }
        public int GetDirection() { return direction; }
        /// <summary>координата центра фигуры (в пикселях) </summary>
        public PointDouble GetCenter() { return GetRcInner(1).Center(); }

        /// <summary>принадлежат ли эти экранные координаты ячейке</summary>
        public virtual bool PointInRegion(PointDouble point) { return region.Contains(point); }

        public RegionDouble GetRegion() { return region; }

        /// <summary>определить координаты точек из которых состоит фигура</summary>
        protected abstract void CalcRegion();

        public void Reset() {
            state.Reset();
            lockMine = false;
        }

        /// <summary>Index where border change color</summary>
        public abstract int GetShiftPointBorderIndex();

        /// <summary>Вернуть цвет заливки ячеки в зависимости от
        /// * режима заливки фона ячеек
        /// * координаты ячейки
        /// * направления ячейки
        /// * ... - как придумает дочерний класс </summary>
        public virtual Color GetCellFillColor(int fillMode, Color defaultColor, Func<int, Color> getColor) {
            switch (fillMode) {
            default:
                System.Diagnostics.Debug.Assert(false, this.GetType() + ".GetCellFillColor: fillMode=" + fillMode + ":  добавь цветовую обработку для этого режима!");
                //break;// !!! без break'а
                goto case 0;
            case 0:
                if ((state.Status == EState._Open) && (state.Open == EOpen._Mine) && state.Down)
                    return Color.Red.Brighter(0.05); // game over: игра завершена - клик на мине
                if ((state.Status == EState._Open) && (state.Open != EOpen._Mine) && (state.Close == EClose._Flag))
                    return Color.Magenta.Brighter(0.3); // game over: игра завершена - не верно проставлен флаг (на ячейке с цифрой)

                // для Down и Нажатого состояний делаю фон чуть и чуть-чуть темнее...
                if (state.Down)
                    return defaultColor.Darker((state.Status == EState._Close) ? 0.15 : 0.25);
                return defaultColor;

            case 1:
                return getColor(GetDirection());
            case 2: {
                // подсветить каждую i-тую строку c шагом div
                int i = 2;
                int div = 5;
                int tmp1 = GetCoord().x % div;
                int tmp2 = (GetCoord().y - tmp1) % div;
                return getColor((((tmp1 + tmp2) % div) == i) ? 0 : 1);
            }
            case 3: {
                // дуршлаг
                int i = 3;
                int div = 4;
                int tmp1 = GetCoord().x % div;
                int tmp2 = (GetCoord().y + tmp1) % div;
                return getColor((((tmp1 + tmp2) % div) == i) ? 0 : 1);
            }
            case 4: {
                // ход конём
                int i = 3;
                int div = 5;
                int tmp1 = GetCoord().x % div;
                int tmp2 = (GetCoord().y + tmp1) % div;
                return getColor((((tmp1 + tmp2) % div) == i) ? 0 : 1);
            }
            case 5: {
                // волны
                int div = 15;
                int tmp1 = GetCoord().x % div;
                int tmp2 = (GetCoord().y + tmp1) % div;
                return getColor((tmp1 + tmp2) % div);
            }
            case 6: {
                int div = 4;
                return getColor(((GetCoord().x % div + GetCoord().y % div) == div) ? 0 : 1);
            }
            case 7: case 8: case 9:
                return getColor(GetCoord().x % (-5 + fillMode));
            case 10: case 11: case 12:
                return getColor(GetCoord().y % (-8 + fillMode));
            case 13: case 14: case 15: case 16: case 17: case 18:
                return getColor(GetCoord().x % (-fillMode) - fillMode + GetCoord().y % (+fillMode));
            case 19:
                // подсветить direction
                var zx = GetCoord().x / Attr.GetDirectionSizeField().Width + 1;
                var zy = GetCoord().y / Attr.GetDirectionSizeField().Height + 1;
                return getColor(zx * zy);
            }
        }

        public override string ToString() {
            return this.GetType().Name + "{ x:" + coord.x + ", y:" + coord.y + " }";
        }

    }

}
