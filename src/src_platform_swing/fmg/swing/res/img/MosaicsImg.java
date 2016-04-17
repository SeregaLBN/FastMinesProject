package fmg.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

import fmg.common.geom.Bound;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.IMosaic;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** картинка поля конкретной мозаики. Используется для меню, кнопок, etc... */
public abstract class MosaicsImg<TImage extends Object> extends RotatedImg<EMosaic, TImage>
      implements IMosaic<PaintableGraphics>
{
   private final boolean RandomCellBkColor = true;

   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField) {
      super(mosaicType);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) {
      super(mosaicType, widthAndHeight);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) {
      super(mosaicType, widthAndHeight, padding);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) {
      super(mosaicType, sizeImage, padding);
      _sizeField = sizeField;
   }

   /** из каких фигур состоит мозаика поля */
   @Override
   public EMosaic getMosaicType() { return getEntity(); }
   @Override
   public void setMosaicType(EMosaic value) {
      if (value != getEntity()) {
         EMosaic old = getEntity();
         setEntity(value);
         dependency_MosaicType_As_Entity(value, old);
      }
   }

   private Matrisize _sizeField;
   @Override
   public Matrisize getSizeField() { return _sizeField; }
   @Override
   public void setSizeField(Matrisize value) {
      if (setProperty(_sizeField, value, "SizeField")) {
         recalcArea();
         _matrix.clear();
         redraw();
      }
   }

   @Override
   public BaseCell getCell(Coord coord) { return getMatrix().get(coord.x * getSizeField().n + coord.y); }

   private BaseCell.BaseAttribute _cellAttr;
   @Override
   public BaseCell.BaseAttribute getCellAttr() {
      if (_cellAttr == null)
         setCellAttr(MosaicHelper.createAttributeInstance(getMosaicType(), getArea()));
      return _cellAttr;
   }
   private void setCellAttr(BaseCell.BaseAttribute value) {
      if (setProperty(_cellAttr, value, "CellAttr")) {
         dependency_GContext_CellAttribute();
         dependency_CellAttribute_Area();
         redraw();
      }
   }

   private ICellPaint<PaintableGraphics> _cellPaint;
   @Override
   public ICellPaint<PaintableGraphics> getCellPaint() {
      if (_cellPaint == null)
         setCellPaint(new CellPaintGraphics());
      return _cellPaint;
   }
   private void setCellPaint(ICellPaint<PaintableGraphics> value) {
      if (setProperty(_cellPaint, value, "CellPaint")) {
         dependency_CellPaint_GContext();
         redraw();
      }
   }

   /** caching rotated values */
   private final List<BaseCell> _matrixRotated = new ArrayList<BaseCell>();
   private final List<BaseCell> _matrix        = new ArrayList<BaseCell>();
   /** матрица ячеек, представленная(развёрнута) в виде вектора */
   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
         _matrixRotated.clear();
         BaseAttribute attr = getCellAttr();
         EMosaic type = getMosaicType();
         Matrisize size = getSizeField();
         for (int i = 0; i < size.m; i++)
            for (int j = 0; j < size.n; j++)
               _matrix.add(MosaicHelper.createCellInstance(attr, type, new Coord(i, j)));
         onPropertyChanged("Matrix");
         redraw();
      }
      return _matrix;
   }

   private List<BaseCell> getRotatedMatrix() {
      if (Math.abs(getRotateAngle()) < 0.1)
         return getMatrix();
      if (_matrixRotated.isEmpty()) {
         // create copy Matrix
         BaseCell.BaseAttribute attr = getCellAttr();
         EMosaic type = getMosaicType();
         Matrisize size = getSizeField();
         for (int i = 0; i < size.m; i++)
            for (int j = 0; j < size.n; j++)
               _matrixRotated.add(MosaicHelper.createCellInstance(attr, type, new Coord(i, j)));
      } else {
         // restore base coords
         for (BaseCell cell : _matrixRotated)
            cell.Init();
      }

      PointDouble center = new PointDouble(getWidth() / 2.0 - _paddingFull.left, getHeight() / 2.0 - _paddingFull.top);
      for (BaseCell cell : _matrixRotated) {
         RegionDouble reg = cell.getRegion();
         Stream<PointDouble> newReg = reg.getPoints()
               .stream()
               .map(p -> new PointDouble(p))
               .map(p -> {
                  p.x -= center.x;
                  p.y -= center.y;
                  return p;
               });
         newReg = FigureHelper
               .rotate(newReg, getRotateAngle())
               .map(p -> {
                  p.x += center.x;
                  p.y += center.y;
                  return p;
               });
         int[] i = { 0 };
         newReg.forEach(p -> reg.setPoint(i[0]++, (int) p.x, (int) p.y));
      }

      return _matrixRotated;
   }

   private void recalcArea() {
      int w = getWidth();
      int h = getHeight();
      Bound pad = getPadding();
      SizeDouble sizeImageIn = new SizeDouble(w - pad.getLeftAndRight(), h - pad.getTopAndBottom());
      SizeDouble sizeImageOut = new SizeDouble();
      double area = MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), sizeImageIn, sizeImageOut);
      setArea(area);
      assert (w >= (sizeImageOut.width + pad.getLeftAndRight()));
      assert (h >= (sizeImageOut.height + pad.getTopAndBottom()));
      BoundDouble paddingOut = new BoundDouble(
         (w - sizeImageOut.width)/2,
         (h - sizeImageOut.height)/2,
         (w - sizeImageOut.width)/2,
         (h - sizeImageOut.height)/2);
      assert DoubleExt.hasMinDiff(sizeImageOut.width + paddingOut.getLeftAndRight(), w);
      assert DoubleExt.hasMinDiff(sizeImageOut.height + paddingOut.getTopAndBottom(), h);

      setPaddingFull(paddingOut);
   }

   private double _area;
   @Override
   public double getArea() {
      if (_area <= 0)
         recalcArea();
      return _area;
   }
   @Override
   public void setArea(double value) {
      if (setProperty(_area, value, "Area")) {
         dependency_CellAttribute_Area();
         redraw();
      }
   }

   private BoundDouble _paddingFull;
   public BoundDouble getPaddingFull() { return _paddingFull; }
   protected void setPaddingFull(BoundDouble value) {
      if (setProperty(_paddingFull, value, "PaddingFull")) {
         dependency_GContext_PaddingFull();
         redraw();
      }
   }

   private GraphicContext _gContext;
   protected GraphicContext getGContext() {
      if (_gContext == null)
         setGContext(new GraphicContext(null, true));
      return _gContext;
   }
   protected void setGContext(GraphicContext value) {
      if (setProperty(_gContext, value, "GContext")) {
         dependency_GContext_CellAttribute();
         dependency_GContext_PaddingFull();
         dependency_CellPaint_GContext();
         dependency_GContext_BorderWidth();
         dependency_GContext_BorderColor();
         redraw();
      }
   }

//   protected override WriteableBitmap CreateImage() {
//      var w = Width;
//      var h = Height;
//      return BitmapFactory.New(w, h); // new WriteableBitmap(w, h); //
//   }

   /** Return painted mosaic bitmap
    *  if (!OnlySyncDraw) {
    *    Сама картинка возвращается сразу.
    *    Но вот её отрисовка - в фоне.
    *    Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
    *  }
    */
   public void drawBody(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = getWidth();
      int h = getHeight();

      Runnable funcFillBk = () -> {
         g.setColor(Cast.toColor(getBackgroundColor()));
         g.fillRect(0, 0, w, h);
      };

      List<BaseCell> matrix = getRotatedMatrix();
      PaintableGraphics paint = new PaintableGraphics(g);
      ICellPaint<PaintableGraphics> cp = getCellPaint();
      if (isOnlySyncDraw() || isLiveImage()) {
         // sync draw
         funcFillBk.run();
         for (BaseCell cell : matrix)
            cp.paint(cell, paint);
      } else {
         // async draw
         SwingUtilities.invokeLater(() ->  {
            funcFillBk.run();
            for (BaseCell cell : matrix) {
               BaseCell tmp = cell;
               SwingUtilities.invokeLater(() -> cp.paint(tmp, paint));
            }
         });
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //LoggerSimple.Put("OnPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "Entity":
         dependency_MosaicType_As_Entity((EMosaic) newValue, (EMosaic) oldValue);
         break;
      case "Size":
      case "Padding":
         recalcArea();
         break;
      case "BorderWidth":
         dependency_GContext_BorderWidth();
         break;
      case "BorderColor":
         dependency_GContext_BorderColor();
         break;
      }
   }

   ///////////// #region Dependencys
   void dependency_GContext_CellAttribute() {
      if ((_cellAttr == null) || (_gContext == null))
         return;
      if (RandomCellBkColor)
         getGContext().getBackgroundFill()
               .setMode(1 + new Random().nextInt(getCellAttr().getMaxBackgroundFillModeValue()));
   }

   void dependency_CellAttribute_Area() {
      if (_cellAttr == null)
         return;
      getCellAttr().setArea(getArea());
      if (!_matrix.isEmpty())
         for (BaseCell cell : getMatrix())
            cell.Init();
   }

   void dependency_GContext_PaddingFull() {
      if (_gContext == null)
         return;
      getGContext().setPadding(getPaddingFull());
   }

   void dependency_GContext_BorderWidth() {
      if (_gContext == null)
         return;
      getGContext().getPenBorder().setWidth(getBorderWidth());
   }

   void dependency_GContext_BorderColor() {
      if (_gContext == null)
         return;
      PenBorder pb = getGContext().getPenBorder();
      pb.setColorShadow(getBorderColor());
      pb.setColorLight(getBorderColor());
   }

   void dependency_CellPaint_GContext() {
      if (_cellPaint == null)
         return;
      assert (getCellPaint() instanceof CellPaintGraphics);
      ((CellPaintGraphics) getCellPaint()).setGraphicContext(getGContext());
   }

   void dependency_MosaicType_As_Entity(EMosaic newValue, EMosaic oldValue) {
      setArea(0);
      _matrix.clear();
      _matrixRotated.clear();
      setCellAttr(null);
      if ((newValue == null) || (oldValue == null))
         onPropertyChanged("MosaicType");
      else
         onPropertyChanged(oldValue, newValue, "MosaicType");
   }
   ////////////// #endregion

   @Override
   protected void close(boolean disposing) {
      if (disposing) {
         setGContext(null);
      }

      super.close(disposing);
   }

   public static class Icon extends MosaicsImg<javax.swing.Icon> {
      public Icon(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

      @Override
      protected javax.swing.Icon createImage() {
         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) { drawBody(g); }
         };
      }

      @Override
      protected void drawBody() {
         javax.swing.Icon ico = getImage();
         BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics g = img.createGraphics();
         ico.paintIcon(null, g, 0, 0);
         g.dispose();
      }

   }

   public static class Image extends MosaicsImg<java.awt.Image> {
      public Image(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
      public Image(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }

      @Override
      protected void drawBody() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics g = img.createGraphics();
         drawBody(g);
         g.dispose();
      }

   }

}
