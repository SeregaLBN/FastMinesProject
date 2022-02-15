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
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary>Базовый класс фигуры-ячейки</summary>
    public abstract class BaseCell {

        private readonly BaseShape shape;
        public BaseShape Shape { get { return shape; } }

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
              BaseShape shape,
              Coord coord,
              int iDirection)
        {
            this.shape = shape;
            this.coord = coord;
            this.direction = iDirection;
            this.region = new RegionDouble(shape.GetVertexNumber(iDirection));

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
                var zx = GetCoord().x / Shape.GetDirectionSizeField().Width + 1;
                var zy = GetCoord().y / Shape.GetDirectionSizeField().Height + 1;
                return getColor(zx * zy);
            }
        }

        public override string ToString() {
            return this.GetType().Name + "{ x:" + coord.x + ", y:" + coord.y + " }";
        }

    }

}
