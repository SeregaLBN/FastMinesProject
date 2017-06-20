////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Mosaic.cs"
//
// реализация алгоритма Мозаики состоящей из ячеек
// Copyright (C) 2011 Sergey Krivulya
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
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic
{

   /// <summary> MVC: model (mosaic field). Default implementation. </summary>
   public class Mosaic : NotifyPropertyChanged, IMosaic {

   #region Members

      private BaseCell.BaseAttribute _cellAttr;
      /// <summary>Matrix of cells, is represented as a vector <see cref="List{BaseCell}"/>.
      /// Матрица ячеек , представленная(развёрнута) в виде вектора</summary>
      private readonly List<BaseCell> _matrix = new List<BaseCell>(0);
      /// <summary>Field size in cells</summary>
      protected Matrisize _size = new Matrisize(10, 10);
      /// <summary>из каких фигур состоит мозаика поля</summary>
      protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;


   #endregion

      public BaseCell.BaseAttribute CellAttr {
         get {
            if (_cellAttr == null) {
               _cellAttr = MosaicHelper.CreateAttributeInstance(MosaicType);
               _cellAttr.Area = 500;
               _cellAttr.PropertyChanged += OnCellAttributePropertyChanged;
            }
            return _cellAttr;
         }
         private set {
            if (_cellAttr == null)
               return;
            if (value != null)
               throw new ArgumentException("Bad argument - support only null value!");
            _cellAttr.PropertyChanged -= OnCellAttributePropertyChanged;
            _cellAttr = null;
            _matrix.Clear();
            OnSelfPropertyChanged();
            OnSelfPropertyChanged(nameof(this.Matrix));
         }
      }

      /// <summary>площадь ячеек</summary>
      public double Area {
         get {
            return CellAttr.Area;
         }
         set {
            System.Diagnostics.Debug.Assert(value >= 1);
            CellAttr.Area = value;
         }
      }

      public IList<BaseCell> Matrix {
         get {
            if (!_matrix.Any()) {
               var attr = CellAttr;
               var size = SizeField;
               var mosaicType = MosaicType;
               //_matrix = new ArrayList<BaseCell>(size.width * size.height);
               for (var i = 0; i < size.m; i++)
                  for (var j = 0; j < size.n; j++) {
                     var cell = MosaicHelper.CreateCellInstance(attr, mosaicType, new Coord(i, j));
                     _matrix.Add( /*i*size.height + j, */cell);
                  }
            }
            return _matrix;
         }
      }

      /// <summary> размер поля в ячейках </summary>
      public Matrisize SizeField {
         get { return _size; }
         set {
            if (_size == value)
               return;
            _matrix.Clear();
            var tmp = SetProperty(ref _size, value);
            System.Diagnostics.Debug.Assert(tmp);
            OnSelfPropertyChanged(nameof(this.Matrix));
         }
      }

      /// <summary> тип мозаики
      /// (из каких фигур состоит мозаика поля) </summary>
      public EMosaic MosaicType {
         get { return _mosaicType; }
         set {
            if (_mosaicType == value)
               return;

            var saveArea = Area; // save

            var old = _mosaicType;
            _mosaicType = value;
            CellAttr = null;

            Area = saveArea; // restore

            OnSelfPropertyChanged(old, value, nameof(this.MosaicType));
         }
      }

      /// <summary> доступ к заданной ячейке </summary>
      public BaseCell getCell(int x, int y) { return Matrix[x*_size.n + y]; }
      /// <summary> доступ к заданной ячейке </summary>
      public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

      protected virtual void OnCellAttributePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == nameof(_cellAttr.Area)) {
            foreach (var cell in Matrix)
               cell.Init();

            OnSelfPropertyChanged<double>(ev, nameof(this.Area));
         }
         OnSelfPropertyChanged(nameof(this.CellAttr));
         OnSelfPropertyChanged(nameof(this.CellAttr) + "." + pn);
      }

      public void EnableCellAttributePropertyListener(bool enable) {
         if (enable)
            CellAttr.PropertyChanged += OnCellAttributePropertyChanged;
         else
            CellAttr.PropertyChanged -= OnCellAttributePropertyChanged;
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing)
            CellAttr = null; // call setter - unsubscribe & dispose
      }

   }

}
