package fmg.swing.res.img;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.Icon;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.IMosaic;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.types.EMosaic;
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** картинка поля конкретной мозаики. Используется для меню, кнопок, etc... */
public class MosaicsImgOld implements Icon, IMosaic<PaintableGraphics> {
   private static final boolean _randomCellBkColor = true;

   private EMosaic _mosaicType;
   private Matrisize _sizeField;
   private double _area = 230;
   private BaseCell.BaseAttribute _attr;
   private List<BaseCell> _matrix = new ArrayList<BaseCell>();
   private CellPaintGraphics _cellPaint;
   private Color _bkColor;
   private final Random _random = !_randomCellBkColor ? null : new Random();

   @Override
   public int getIconWidth() {
      BaseCell.BaseAttribute attr = getCellAttr();
      BoundDouble padding = getGraphicContext().getPadding();
      return (int)(attr.getOwnerSize(getSizeField()).width + padding.left + padding.right);
   }

   @Override
   public int getIconHeight() {
      BaseCell.BaseAttribute attr = getCellAttr();
      BoundDouble padding = getGraphicContext().getPadding();
      return (int)(attr.getOwnerSize(getSizeField()).height + padding.top + padding.bottom);
   }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      if (!false) {
         Color clr = getBackgroundColor();
         if (clr != null) {
            Color tmp = g.getColor(); // save
            g.setColor(clr); // change
            SizeDouble pixelSize = getCellAttr().getOwnerSize(getSizeField());
            BoundDouble padding = getGraphicContext().getPadding();
            g.fillRect(0, 0, (int)(pixelSize.width + padding.left + padding.right), (int)(pixelSize.height + padding.top + padding.bottom));
            g.setColor(tmp); // restore
         }
      }
      PaintableGraphics p = new PaintableGraphics(g);
      ICellPaint<PaintableGraphics> cellPaint = getCellPaint();
      for (BaseCell cell: getMatrix())
         cellPaint.paint(cell, p);
   }

   @Override
   public Matrisize getSizeField() {
      return _sizeField;
   }
   @Override
   public void setSizeField(Matrisize size) {
      // reset
      _matrix.clear();

      _sizeField = size;
   }

   @Override
   public BaseCell getCell(Coord coord) {
      return getMatrix().get(coord.x * getSizeField().n + coord.y);
   }

   @Override
   public BaseAttribute getCellAttr() {
      if (_attr == null)
         _attr = MosaicHelper.createAttributeInstance(getMosaicType(), getArea());
      return _attr;
   }

   @Override
   public ICellPaint<PaintableGraphics> getCellPaint() {
      return getCellPaintGraphics();
   }
   public CellPaintGraphics getCellPaintGraphics() {
      if (_cellPaint == null)
         _cellPaint = new CellPaintGraphics();
      return _cellPaint;
   }

   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
         BaseCell.BaseAttribute attr = getCellAttr();
         EMosaic type = getMosaicType();
         Matrisize size = getSizeField();
         for (int i=0; i<size.m; i++)
            for (int j=0; j<size.n; j++)
               _matrix.add(MosaicHelper.createCellInstance(attr, type, new Coord(i,j)));
      }
      return _matrix;
   }

   @Override
   public EMosaic getMosaicType() {
      return _mosaicType;
   }
   @Override
   public void setMosaicType(EMosaic type) {
      // reset
      _matrix.clear();
      _attr = null;

      _mosaicType = type;
   }

   @Override
   public double getArea() {
      return _area;
   }
   @Override
   public void setArea(double area) {
      this._area = area;
   }

   public Color getBackgroundColor() {
      //if (_bkColor == null) {
      //   _bkColor = fmg.swing.geom.Cast.toColor(getCellPaint().getDefaultBackgroundFillColor());
      //}
      return _bkColor;
   }
   public void setBackgroundColor(Color color) {
      _bkColor = color;
   }

   public GraphicContext getGraphicContext() {
      GraphicContext gContext = getCellPaintGraphics().getGraphicContext();
      if (gContext == null)
      {
         gContext = new GraphicContext(null, true);
         getCellPaintGraphics().setGraphicContext(gContext);
         gContext.getPenBorder().setWidth(2);
         gContext.getPenBorder().setColorLight(gContext.getPenBorder().getColorShadow());
         if (_randomCellBkColor)
            gContext.getBackgroundFill().setMode(1 + _random.nextInt(getCellAttr().getMaxBackgroundFillModeValue()));
      }
      return gContext;
   }
   public void setGraphicContext(GraphicContext gContext) {
      getCellPaintGraphics().setGraphicContext(gContext);
   }
}