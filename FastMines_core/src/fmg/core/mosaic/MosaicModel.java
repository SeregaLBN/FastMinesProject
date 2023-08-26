////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "MosaicModel.java"
//
// Data class: game model of mosaic field
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
package fmg.core.mosaic;

import static fmg.core.img.PropertyConst.PROPERTY_AREA;
import static fmg.core.img.PropertyConst.PROPERTY_BACKGROUND_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_CELL_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_FILL_MODE;
import static fmg.core.img.PropertyConst.PROPERTY_FONT_INFO;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_OFFSET;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_PEN_BORDER;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE_FIELD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.geom.*;
import fmg.core.app.model.MosaicInitData;
import fmg.core.img.IImageModel2;
import fmg.core.img.ImageHelper;
import fmg.core.img.MosaicImageModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.types.EMosaic;
import fmg.core.types.draw.FontInfo2;
import fmg.core.types.draw.PenBorder2;

/** MVC: model of mosaic field (representable {@link fmg.core.types.EMosaic} ). Default implementation. */
public class MosaicModel2 implements IImageModel2 {

    /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником. */
    public static Color DefaultBkColor   = Color.LightSlateGray().brighter();
    public static Color DefaultCellColor = Color.LightGray();

    /** the effect of changing one property on another. true - control mode; false - image mode */
    private final boolean isControlMode;

    /** из каких фигур состоит мозаика поля */
    private EMosaic mosaicType = EMosaic.eMosaicSquare1;

    private BaseShape shape;

    /** Matrix of cells, is represented as a vector {@link List<BaseCell>} */
    private final List<BaseCell> matrix = new ArrayList<>();

    /** Field size in cells */
    protected final Matrisize sizeField = new Matrisize(10, 10);

    /** size in pixels*/
    protected final SizeDouble size;
    protected final BoundDouble padding = new BoundDouble(0);
    private final PenBorder2 penBorder = new PenBorder2();
    private final FontInfo2 fontInfo = new FontInfo2();
    private Color backgroundColor = DefaultBkColor;
    /** Default fill color (at {@link #fillMode} == 0) */
    private Color cellColor = DefaultCellColor;
    /** Cell fill mode */
    private int fillMode = 0;
    /** cached cell background colors
     * <br/> No color? - create with the desired intensity! */
    private final Map<Integer, Color> fillColors = new HashMap<>();

    protected Consumer<String> changedCallback;


    public MosaicModel2(boolean isControlMode) {
        this.isControlMode = isControlMode;
        this.size = getSizeMosaic();
    }

    /** get mosaic type */
    public EMosaic getMosaicType() { return mosaicType; }

    /** set mosaic type */
    public void setMosaicType(EMosaic newMosaicType) {
        if (this.mosaicType == newMosaicType)
            return;

        this.mosaicType = newMosaicType;
        shape = null;
        matrix.clear();

        firePropertyChanged(PROPERTY_MOSAIC_TYPE);

        if (isControlMode)
            setPaddingInner(new BoundDouble(0));
        else
            uniformlyChangeMosaicSize();
    }

    protected BaseShape getShape() {
        if (shape == null)
            shape = MosaicHelper.createShapeInstance(mosaicType);
        return shape;
    }

    /** cell area */
    public double getArea() {
        return getShape().getArea();
    }

    /** set new cell area */
    public void setArea(double area) {
        if (DoubleExt.almostEquals(getArea(), area))
            return;

        if (area <= 0)
            throw new IllegalArgumentException("Area must be positive");
        if ((area < MosaicInitData.AREA_MINIMUM) && !(this instanceof MosaicImageModel2))
            Logger.warn("The area is very small = " + area);

      //getShape().setArea(Math.max(MosaicInitData.AREA_MINIMUM, area));
        getShape().setArea(area);
        matrix.forEach(BaseCell::init);

        firePropertyChanged(PROPERTY_AREA);
    }

    public List<BaseCell> getMatrix() {
        if (matrix.isEmpty()) {
            BaseShape shp = getShape();
            Matrisize mSize = getSizeField();
            for (int i=0; i < mSize.m; i++)
                for (int j=0; j < mSize.n; j++)
                    matrix.add(MosaicHelper.createCellInstance(shp, mosaicType, new Coord(i, j)));
        }
        return matrix;
    }

    /** доступ к заданной ячейке */
    public BaseCell getCell(int x, int y) { return getMatrix().get(x*sizeField.n + y); }
    /** доступ к заданной ячейке */
    public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

    /** get mosaic size in pixels */
    public SizeDouble getSizeMosaic() {
        return getShape().getSize(sizeField);
    }

    /** get mosaic field size in cells */
    public Matrisize getSizeField() { return sizeField; }

    /** set mosaic field size in cells */
    public void setSizeField(Matrisize newSizeField) {
        if (this.sizeField.equals(newSizeField))
            return;

        matrix.clear();
        this.sizeField.m = newSizeField.m;
        this.sizeField.n = newSizeField.n;

        firePropertyChanged(PROPERTY_SIZE_FIELD);

        if (isControlMode)
            setPaddingInner(new BoundDouble(0));
        else
            uniformlyChangeMosaicSize();
    }

    /** common size in pixels */
    @Override
    public SizeDouble getSize() {
        return size;
    }

    @Override
    public void setSize(SizeDouble size) {
        if (this.size.equals(size))
            return;

        ImageHelper.checkSize(size);

        SizeDouble oldSize = new SizeDouble(this.size);
        this.size.width  = size.width;
        this.size.height = size.height;

        firePropertyChanged(PROPERTY_SIZE);

        if (isControlMode) {
            setPaddingInner(new BoundDouble(0));
        } else {
            BoundDouble newPad = ImageHelper.recalcPadding(this.padding, size, oldSize);
            setPadding(newPad);
            uniformlyChangeMosaicSize();
        }
    }

    @Override
    public BoundDouble getPadding() {
        return padding;
    }

    @Override
    public void setPadding(BoundDouble padding) {
        if (this.padding.equals(padding))
            return;

        setPaddingInner(padding);
    }

    private void setPaddingInner(BoundDouble padding) {
        var oldPad = new BoundDouble(this.padding);
        ImageHelper.checkPadding(size, padding);

        this.padding.left   = padding.left;
        this.padding.right  = padding.right;
        this.padding.top    = padding.top;
        this.padding.bottom = padding.bottom;

        if (!oldPad.equals(padding))
            firePropertyChanged(PROPERTY_PADDING);

        uniformlyChangeMosaicSize();
    }

    private void uniformlyChangeMosaicSize() {
        var innerSize = new SizeDouble(size.width - padding.getLeftAndRight(), size.height - padding.getTopAndBottom());
        SizeDouble newMosaicSizeInPixels = new SizeDouble();
        double area = MosaicHelper.findAreaBySize(mosaicType, sizeField, innerSize, newMosaicSizeInPixels);

        if (area <= 0)
            throw new IllegalArgumentException("Area must be positive");
        if ((area < MosaicInitData.AREA_MINIMUM) && !(this instanceof MosaicImageModel2))
            Logger.warn("The area is very small = " + area);

        //area = Math.max(MosaicInitData.AREA_MINIMUM, area);

        if (DoubleExt.almostEquals(getArea(), area))
            return;

        setArea(area);
    }

    /** Offset to mosaic */
    public SizeDouble getMosaicOffset() {
        var innerSize = new SizeDouble(size.width - padding.getLeftAndRight(), size.height - padding.getTopAndBottom());
        var mosaicSize = getSizeMosaic();
        double dx = innerSize.width  - mosaicSize.width;
        double dy = innerSize.height - mosaicSize.height;
        return new SizeDouble(padding.left + dx / 2, padding.top + dy / 2);
    }

    /** set offset to mosaic */
    public void setMosaicOffset(SizeDouble offset) {
        var oldOffset = getMosaicOffset();
        if (oldOffset.equals(offset))
            return;

        double dx = offset.width  - oldOffset.width;
        double dy = offset.height - oldOffset.height;
        padding.left   += dx;
        padding.top    += dy;
        padding.right  -= dx;
        padding.bottom -= dy;

        firePropertyChanged(PROPERTY_MOSAIC_OFFSET);
    }

    /** the maximum number of background fill modes that this type of mosaic knows */
    public int getMaxCellFillMode() {
        return getShape().getMaxCellFillModeValue();
    }

    /** the size of the square inscribed in the cell */
    public double getCellSquareSize() {
        return getShape().getSq(getPenBorder().getWidth());
    }

    /** cell background fill mode */
    public int getFillMode() { return fillMode; }

    /** cell background fill mode
     * @param newFillMode
     *  <li> 0 - default background fill color
     *  <li> not 0 - rainbow %) */
    public void setFillMode(int newFillMode) {
        if (this.fillMode == newFillMode)
            return;

        this.fillMode = newFillMode;
        fillColors.clear();

        firePropertyChanged(PROPERTY_FILL_MODE);
    }

    /** cached cell background colors
     * <br/> No color? - create with the desired intensity! */
    public Color getFillColor(int index) {
        return fillColors
            .computeIfAbsent(index,
                             i -> Color.RandomColor().brighter(0.45));
    }

    public PenBorder2 getPenBorder() {
        return penBorder;
    }

    public void setPenBorder(PenBorder2 penBorder) {
        if (this.penBorder.equals(penBorder))
            return;

        this.penBorder.setWidth(penBorder.getWidth());
        this.penBorder.setColorLight(penBorder.getColorLight());
        this.penBorder.setColorShadow(penBorder.getColorShadow());
    }

    public FontInfo2 getFontInfo() {
        return fontInfo;
    }

    public void setFontInfo(FontInfo2 fontInfo) {
        if (this.fontInfo.equals(fontInfo))
            return;

        this.fontInfo.setName(fontInfo.getName());
        this.fontInfo.setBold(fontInfo.isBold());
        this.fontInfo.setSize(fontInfo.getSize());
    }

    public Color getCellColor() {
        return cellColor;
    }

    public void setCellColor(Color color) {
        if (this.cellColor.equals(color))
            return;

        this.cellColor = color;

        firePropertyChanged(PROPERTY_CELL_COLOR);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        if (this.backgroundColor.equals(color))
            return;

        this.backgroundColor = color;

        firePropertyChanged(PROPERTY_BACKGROUND_COLOR);
    }


    public Consumer<String> getListener() { return changedCallback; }
    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
        if (callback == null) {
            getPenBorder().setListener(callback);
            getFontInfo().setListener(callback);
        } else {
            getPenBorder().setListener(name -> callback.accept(PROPERTY_PEN_BORDER + '.' + name));
            getFontInfo ().setListener(name -> callback.accept(PROPERTY_FONT_INFO  + '.' + name));
        }
    }

    protected void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

}
