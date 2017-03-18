package fmg.swing.mosaic;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.PaintContext;
import fmg.core.types.click.ClickResult;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.utils.ImgUtils;

public final class Mosaic {

/** MVC: controller */
public static class MosaicController implements AutoCloseable {

   /** MVC: model */
   private MosaicBase _mosaic;
   /** MVC: view */
   protected MosaicView _view;

   /** get model */
   public MosaicBase getMosaic() {
      if (_mosaic == null)
         setMosaic(new MosaicBase() {

            private MosaicView getView() { return MosaicController.this.getView(); }
            @Override
            public boolean GameNew() {
               getView().getPaintContext().getBackgroundFill().setMode(
                     1 + new Random().nextInt(
                           MosaicHelper.createAttributeInstance(getMosaicType(), getArea()).getMaxBackgroundFillModeValue()));
               boolean res = super.GameNew();
               if (!res)
                  getView().invalidateCells();
               return res;
            }

            @Override
            public void GameBegin(BaseCell firstClickCell) {
               getView().getPaintContext().getBackgroundFill().setMode(0);
               super.GameBegin(firstClickCell);
            }

         });
      return _mosaic;
   }
   /** set model */
   protected void setMosaic(MosaicBase model) {
      if (_mosaic != null)
         _mosaic.close();
      _mosaic = model;
   }

   /** get view */
   public MosaicView getView() {
      if (_view == null)
         setView(new MosaicView());
      return _view;
   }
   /** set view */
   public void setView(MosaicView view) {
      if (_view != null)
         _view.close();
      _view = view;
      if (_view != null)
         _view.setMosaic(getMosaic());
   }


   /** преобразовать экранные координаты в ячейку поля мозаики */
   private BaseCell CursorPointToCell(Point point) {
      if (point == null)
            return null;
      fmg.common.geom.PointDouble p = Cast.toPointDouble(point);
      for (BaseCell cell: getMosaic(). getMatrix())
         //if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            if (cell.PointInRegion(p))
               return cell;
      return null;
   }

   public ClickResult mousePressed(Point clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? getMosaic().onLeftButtonDown(CursorPointToCell(clickPoint))
         : getMosaic().onRightButtonDown(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseReleased(Point clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? getMosaic().onLeftButtonUp(CursorPointToCell(clickPoint))
         : getMosaic().onRightButtonUp(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseFocusLost() {
      BaseCell cellDown = getMosaic().getCellDown();
      if (cellDown == null)
         return null;
      ClickResult res = cellDown.getState().isDown()
         ? getMosaic().onLeftButtonUp(null)
         : getMosaic().onRightButtonUp(null);
      acceptClickEvent(res);
      return res;
   }

   /** уведомление о том, что на мозаике был произведён клик */
   private Consumer<ClickResult> clickEvent;
   public void setOnClickEvent(Consumer<ClickResult> handler) {
      clickEvent = handler;
   }
   private void acceptClickEvent(ClickResult clickResult) {
      if (clickEvent != null)
         clickEvent.accept(clickResult);
   }

   @Override
   public void close() {
      setMosaic(null); // unsubscribe & close
      setView(null); // unsubscribe & close
   }
}

public static class MosaicView implements AutoCloseable, PropertyChangeListener {

   private MosaicBase _mosaic;
   private JPanel _control;
   private PaintSwingContext<Icon> _paintContext;
   private CellPaintGraphics<Icon> _cellPaint;

   public static final String PROPERTY_PAINT_CONTEXT = "PaintContext";

   public JPanel getControl() {
      if (_control == null) {
         _control = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
               {
                  Graphics2D g2d = (Graphics2D) g;
                  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               }

               MosaicView.this.repaint(g);
            }

             @Override
             public Dimension getPreferredSize() {
                SizeDouble size = getMosaic().getWindowSize();
                size.height++;
                size.width++;
//                System.out.println("Mosaic::getPreferredSize: size="+size);
                return Cast.toSize(size);
             }
             @Override
             public Dimension getMinimumSize() {
                return getPreferredSize();
             }

         };
      }
      return _control;
   }

   public MosaicBase getMosaic() {
      return _mosaic;
   }
   public void setMosaic(MosaicBase mosaic) {
      if (_mosaic != null) {
         _mosaic.removeListener(this);
         _mosaic.close();
      }
      _mosaic = mosaic;
      if (_mosaic != null)
         _mosaic.addListener(this);
   }


   public PaintSwingContext<Icon> getPaintContext() {
      if (_paintContext == null)
         setPaintContext(new PaintSwingContext<>(false));
      return _paintContext;
   }
   private void setPaintContext(PaintSwingContext<Icon> paintContext) {
      if (_paintContext != null) {
         _paintContext.removeListener(this);
         _paintContext.close();
      }
      _paintContext = paintContext;
      if (_paintContext != null) {
         _paintContext.setImgMine(new Mine());
         _paintContext.setImgFlag(new Flag());
         _paintContext.addListener(this); // изменение контекста -> перерисовка мозаики
      }
   }

   public CellPaintGraphics<Icon> getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics.Icon();
      }
      return _cellPaint;
   }

   public void invalidateCells() { invalidateCells(null); }
   public void invalidateCells(Collection<BaseCell> modifiedCells) {
      JPanel control = getControl();
      if (control == null)
         return;

      assert !_alreadyPainted;

      if (modifiedCells == null)
         control.repaint(); // redraw all of mosaic
      else
         modifiedCells.forEach(cell -> control.repaint(Cast.toRect(cell.getRcOuter())) );
   }

   boolean _alreadyPainted = false;
   private void repaint(Graphics g) {
      _alreadyPainted = true;
      try {
         PaintSwingContext<Icon> pc = getPaintContext();

         // background color
         Rectangle rcFill = g.getClipBounds();
         g.setColor(Cast.toColor(pc.getBackgroundColor().darker(0.2)));
         g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);

         // paint cells
         g.setFont(pc.getFont());
         PaintableGraphics p = new PaintableGraphics(getControl(), g);
         RectDouble clipBounds = Cast.toRectDouble(g.getClipBounds());
         CellPaintGraphics<Icon> cellPaint = getCellPaint();
         for (BaseCell cell: getMosaic().getMatrix())
            if (cell.getRcOuter().Intersects(clipBounds)) // redraw only when needed - when the cells and update region intersect
               cellPaint.paint(cell, p, pc);
      } finally {
         _alreadyPainted = false;
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      if (ev.getSource() instanceof MosaicBase)
         onMosaicPropertyChanged((MosaicBase)ev.getSource(), ev);
      if (ev.getSource() instanceof PaintSwingContext)
         onPaintContextPropertyChanged((PaintSwingContext<?>)ev.getSource(), ev);
   }

   protected void onMosaicPropertyChanged(MosaicBase source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case MosaicBase.PROPERTY_MOSAIC_TYPE:
         changeFontSize();
         break;
      case MosaicBase.PROPERTY_AREA:
         changeFontSize(getPaintContext().getPenBorder());
         changeSizeImagesMineFlag();
         break;
      case MosaicBase.PROPERTY_MATRIX:
         invalidateCells();
         break;
      case MosaicBase.PROPERTY_MODIFIED_CELLS:
         @SuppressWarnings("unchecked")
         Collection<BaseCell> modifiedCells = (Collection<BaseCell>)ev.getNewValue();
         invalidateCells(modifiedCells);
         break;
      }
   }

   private void onPaintContextPropertyChanged(PaintSwingContext<?> source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case PaintContext.PROPERTY_PEN_BORDER:
         PenBorder penBorder = (PenBorder)ev.getNewValue();
         changeFontSize(penBorder);
         break;
      }
//      invalidateCells();
//      onSelfPropertyChanged(PROPERTY_PAINT_CONTEXT);
//      onSelfPropertyChanged(PROPERTY_PAINT_CONTEXT + "." + propertyName);
   }

   /** пересчитать и установить новую высоту шрифта */
   public void changeFontSize() { changeFontSize(getPaintContext().getPenBorder()); }
   /** пересчитать и установить новую высоту шрифта */
   private void changeFontSize(PenBorder penBorder) {
      getPaintContext().getFontInfo().setSize((int)getMosaic().getCellAttr().getSq(penBorder.getWidth()));
   }

   /** переустанавливаю заного размер мины/флага для мозаики */
   private void changeSizeImagesMineFlag() {
      PaintSwingContext<Icon> pc = getPaintContext();
      int sq = (int)getMosaic().getCellAttr().getSq(pc.getPenBorder().getWidth());
      if (sq <= 0) {
         System.err.println("Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
         sq = 3; // ат балды...
      }
      pc.setImgFlag(ImgUtils.zoom(new Flag(), sq, sq));
      pc.setImgMine(ImgUtils.zoom(new Mine(), sq, sq));
   }

   @Override
   public void close() {
      setPaintContext(null); // unsubscribe & dispose
   }

}

   /// TEST
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      MosaicController m = new MosaicControllerSwing();
      frame.add(m.getView().getControl());
      //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) {
            m.close();
            frame.dispose();
         }
      });

      frame.pack();
      frame.setVisible(true);
   }
}
