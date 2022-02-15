package fmg.core.mosaic;

import java.util.List;

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.types.EMosaic;

/** interface of mosaic field */
public interface IMosaic {

    /** размер поля */
    Matrisize getSizeField();
    void setSizeField(Matrisize size);

    BaseShape getShape();

    /** матрица ячеек, представленная(развёрнута) в виде вектора */
    List<BaseCell> getMatrix();
    /** доступ к заданной ячейке */
    BaseCell getCell(Coord coord);

    /** из каких фигур состоит мозаика поля */
    EMosaic getMosaicType();
    void setMosaicType(EMosaic val);

    double getArea();
    void setArea(double area);

}
