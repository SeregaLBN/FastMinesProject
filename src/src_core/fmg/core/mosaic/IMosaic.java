package fmg.core.mosaic;

import java.util.List;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

/**
 * interface of mosaic field
 */
public interface IMosaic extends BaseCell.IMatrixCells {

   BaseCell.BaseAttribute getCellAttr();

   /** матрица ячеек, представленная(развёрнута) в виде вектора */
   List<BaseCell> getMatrix();

   /** из каких фигур состоит мозаика поля */
   EMosaic getMosaicType();

   void setMosaicType(EMosaic val);

   double getArea();

   void setArea(double area);

}
