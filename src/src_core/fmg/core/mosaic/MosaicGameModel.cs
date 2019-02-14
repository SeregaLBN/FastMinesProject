////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "MosaicGameModel.cs"
//
// Author: 2011-2018  -  Serhii Kryvulia aka SeregaLBN
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
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

    /// <summary> MVC: game model of mosaic field. Default implementation </summary>
    public class MosaicGameModel : IMosaic, INotifyPropertyChanged, IDisposable {

    #region Members

        private BaseCell.BaseAttribute _cellAttr;
        /// <summary> Matrix of cells, is represented as a vector <see cref="IList<BaseCell>"/>.
        /// Матрица ячеек, представленная(развёрнута) в виде вектора </summary>
        private readonly IList<BaseCell> _matrix = new List<BaseCell>();
        /// <summary> Field size in cells </summary>
        private Matrisize _sizeField = new Matrisize(10, 10);
        /// <summary> из каких фигур состоит мозаика поля </summary>
        private EMosaic _mosaicType = EMosaic.eMosaicSquare1;

        protected bool Disposed { get; private set; }
        private event PropertyChangedEventHandler PropertyChangedSync;
        public  event PropertyChangedEventHandler PropertyChanged/*Async*/;
        protected readonly NotifyPropertyChanged _notifier;
        private   readonly NotifyPropertyChanged _notifierAsync;
    #endregion

        public MosaicGameModel() {
            _notifier      = new NotifyPropertyChanged(this, ev => PropertyChangedSync?.Invoke(this, ev), false);
            _notifierAsync = new NotifyPropertyChanged(this, ev => PropertyChanged    ?.Invoke(this, ev), true);
            this.PropertyChangedSync += OnPropertyChanged;
        }

        public BaseCell.BaseAttribute CellAttr {
            get {
                if (_cellAttr == null) {
                    _cellAttr = MosaicHelper.CreateAttributeInstance(MosaicType);
                    _cellAttr.PropertyChanged += OnCellAttributePropertyChanged;
                }
                return _cellAttr;
            }
            set {
                if (_cellAttr == null)
                    return;
                if (value != null)
                    throw new ArgumentException("Bad argument - support only null value!");
                _cellAttr.PropertyChanged -= OnCellAttributePropertyChanged;
                _cellAttr = null;
                _matrix.Clear();
                _notifier.FirePropertyChanged();
                _notifier.FirePropertyChanged(nameof(this.Matrix));
            }
        }

        /// <summary> площадь ячеек </summary>
        public double Area {
            get { return CellAttr.Area; }
            set {
                if (value <= 0)
                    throw new ArgumentException("Area must be positive");
                CellAttr.Area = value;
            }
        }

        public IList<BaseCell> Matrix {
            get {
                if (!_matrix.Any()) {
                    var attr = CellAttr;
                    var size = SizeField;
                    var mosaicType = MosaicType;
                    //_matrix = new List<BaseCell>(size.width * size.height);
                    for (var i=0; i < size.m; i++)
                        for (var j=0; j < size.n; j++) {
                            var cell = MosaicHelper.CreateCellInstance(attr, mosaicType, new Coord(i, j));
                            _matrix.Add(/* i*_size.n + j, */ cell);
                        }
                }
                return _matrix;
            }
        }

        /// <summary> mosaic field size in cells </summary>
        public Matrisize SizeField {
            get { return _sizeField; }
            set {
                var old = this._sizeField;
                if (old == value)
                    return;

                _matrix.Clear();
                this._sizeField = value;

                _notifier.FirePropertyChanged(old, value);
                _notifier.FirePropertyChanged(nameof(this.Matrix));
            }
        }

        /// <summary> mosaic type (из каких фигур состоит мозаика поля) </summary>
        public EMosaic MosaicType {
            get { return _mosaicType; }
            set {
                EMosaic old = this._mosaicType;
                if (old == value)
                    return;

                this._mosaicType = value;
                CellAttr = null;
                _notifier.FirePropertyChanged(old, value);
            }
        }

        /// <summary> доступ к заданной ячейке </summary>
        public BaseCell GetCell(int x, int y) { return Matrix[x*_sizeField.n + y]; }
        /// <summary> доступ к заданной ячейке </summary>
        public BaseCell GetCell(Coord coord) { return GetCell(coord.x, coord.y); }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);
        }

        protected virtual void OnCellAttributePropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, CellAttr));

            if (ev.PropertyName == nameof(BaseCell.BaseAttribute.Area)) {
                foreach (var cell in Matrix)
                    cell.Init();
                _notifier.FirePropertyChanged<double>(ev, nameof(this.Area)); // ! rethrow event - notify parent class
            }
            _notifier.FirePropertyChanged(nameof(this.CellAttr));
        }

        /// <summary> off notifier </summary>
        protected virtual IDisposable Hold() {
            var a1 = _notifier.Hold();
            var a2 = CellAttr.Hold();
            return new PlainFree() {
                _onDispose = () => {
                    a1.Dispose();
                    a2.Dispose();
                }
            };
        }

        /// <summary>  Dispose managed resources </summary>/
        protected virtual void Disposing() {
            this.PropertyChangedSync -= OnPropertyChanged;
            _notifier.Dispose();
            _notifierAsync.Dispose();
            CellAttr = null; // call setter - unsubscribe & dispose
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            Disposing();
            GC.SuppressFinalize(this);
        }

    }

}
