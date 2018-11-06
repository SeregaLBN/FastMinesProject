////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "IMosaic.cs"
//
// Interface of mosaic field
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
using System.Collections.Generic;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> interface of mosaic field </summary>
   public interface IMosaic : BaseCell.IMatrixCells {

      BaseCell.BaseAttribute CellAttr { get; }

      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      IList<BaseCell> Matrix { get; }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      EMosaic MosaicType { get; set; }

      double Area { get; set; }

   }

}
