////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "BaseShape.cs"
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
using System.ComponentModel;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;

namespace Fmg.Core.Mosaic.Shape {

    /// <summary> Base shape </summary>
    public abstract class BaseShape : INotifyPropertyChanged, IDisposable {

        public const double PI = 3.14159265358979323846; // Math.PI;
        public static readonly double SQRT2 = Math.Sqrt(2.0);
        public static readonly double SQRT3 = Math.Sqrt(3.0);
        public static readonly double SQRT27 = Math.Sqrt(27.0);
        public static readonly double SQRT48 = Math.Sqrt(48.0);
        public static readonly double SQRT147 = Math.Sqrt(147.0);
        public static readonly double SIN15 = Math.Sin(PI / 180.0 * 15.0);
        public static readonly double SIN18 = Math.Sin(PI / 180.0 * 18.0);
        public static readonly double SIN36 = Math.Sin(PI / 180.0 * 36.0);
        public static readonly double SIN54 = Math.Sin(PI / 180.0 * 54.0);
        public static readonly double SIN72 = Math.Sin(PI / 180.0 * 72.0);
        public static readonly double SIN75 = Math.Sin(PI / 180.0 * 75.0);
        public static readonly double SIN99 = Math.Sin(PI / 180.0 * 99.0);
        public static readonly double TAN15 = Math.Tan(PI / 180.0 * 15.0);
        public static readonly double TAN45_2 = Math.Tan(PI / 180.0 * 45.0 / 2);
        public static readonly double SIN135a = Math.Sin(PI / 180.0 * 135.0 - Math.Atan(8.0 / 3));

        /// <summary>площадь ячейки/фигуры</summary>
        private double _area = 500;
        protected bool Disposed { get; private set; }
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;
    
        protected BaseShape() {
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

}
