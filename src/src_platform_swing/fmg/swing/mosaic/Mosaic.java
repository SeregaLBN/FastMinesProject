package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fmg.common.geom.Matrisize;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.core.types.click.ClickResult;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.MosaicGraphicContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

public class Mosaic extends MosaicBase<PaintableGraphics> {

   private MosaicGraphicContext _gContext;
   private CellPaintGraphics _cellPaint;
   private JPanel _container;
   private MosaicMouseListeners _mosaicMouseListener;
   private static final boolean _DEBUG = true;

   public Mosaic() {
      super();
   }

   public Mosaic(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      super(sizeField, mosaicType, minesCount, area);
   }

   public JPanel getContainer() {
      if (_container == null) {
         _container = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
               {
                  Graphics2D g2d = (Graphics2D) g;
                  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               }

               // background color
               Rectangle rcFill = g.getClipBounds();
               g.setColor(getGraphicContext().getColorBk());
               g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);

               // paint cells
               g.setFont(getGraphicContext().getFont());
               PaintableGraphics p = new PaintableGraphics(g);
               RectDouble clipBounds = Cast.toRectDouble(g.getClipBounds());
               for (BaseCell cell: getMatrix())
                  if (cell.getRcOuter().Intersects(clipBounds)) // redraw only when needed - when the cells and update region intersect
                     getCellPaint().paint(cell, p);
            }

             @Override
             public Dimension getPreferredSize() {
                SizeDouble size = getWindowSize();
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
      return _container;
   }

   @Override
   protected void OnError(String msg) {
      if (_DEBUG)
         JOptionPane.showMessageDialog(getContainer(), msg, "Error", JOptionPane.QUESTION_MESSAGE, null);
      else
         super.OnError(msg);
   }

   public MosaicGraphicContext getGraphicContext() {
      if (_gContext == null) {
         _gContext = new MosaicGraphicContext(getContainer());
//         changeFontSize(_gContext.getPenBorder(), getArea());
         _gContext.addListener(this); // изменение контекста -> перерисовка мозаики
         _cellPaint = null;
      }
      return _gContext;
   }

   @Override
   public CellPaintGraphics getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics();
         _cellPaint.setGraphicContext(getGraphicContext());
      }
      return _cellPaint;
   }

   protected void revalidate() {
      //getContainer().revalidate();
   }

   @Override
   protected void Repaint(BaseCell cell) {
      if (_sheduleRepaint)
         return;

      if (cell == null) {
         _sheduleRepaint = true;
         SwingUtilities.invokeLater(() -> {
            getContainer().repaint();
            _sheduleRepaint = false;
         });
      } else {
         //getCellPaint().paint(cell, getContainer().getGraphics());
         getContainer().repaint(Cast.toRect(cell.getRcOuter()));
      }
   }
   private boolean _sheduleRepaint;

   @Override
   public boolean GameNew() {
      getGraphicContext().getBackgroundFill().setMode(
            1 + new Random().nextInt(
                  MosaicHelper.createAttributeInstance(getMosaicType(), getArea()).getMaxBackgroundFillModeValue()));
      boolean res = super.GameNew();
      if (!res)
         Repaint(null);
      return res;
   }

   @Override
   protected void GameBegin(BaseCell firstClickCell) {
      getGraphicContext().getBackgroundFill().setMode(0);
      super.GameBegin(firstClickCell);
   }

   /** преобразовать экранные координаты в ячейку поля мозаики */
   private BaseCell CursorPointToCell(Point point) {
      if (point == null)
            return null;
//      long l1 = System.currentTimeMillis();
//      try {
      fmg.common.geom.PointDouble p = Cast.toPointDouble(point);
      for (BaseCell cell: getMatrix())
         //if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            if (cell.PointInRegion(p))
               return cell;
      return null;
//      } finally {
//         System.out.println("Mosaic::CursorPointToCell: find cell: " + (System.currentTimeMillis()-l1) + "ms.");
//      }
   }

   public ClickResult mousePressed(Point clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? onLeftButtonDown(CursorPointToCell(clickPoint))
         : onRightButtonDown(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseReleased(Point clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? onLeftButtonUp(CursorPointToCell(clickPoint))
         : onRightButtonUp(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseFocusLost() {
      BaseCell cellDown = getCellDown();
      if (cellDown == null)
         return null;
      ClickResult res = cellDown.getState().isDown()
         ? onLeftButtonUp(null)
         : onRightButtonUp(null);
      acceptClickEvent(res);
      return res;
   }


   @Override
   protected boolean checkNeedRestoreLastGame() {
      int iRes = JOptionPane.showOptionDialog(getContainer(), "Restore last game?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
      return (iRes == JOptionPane.NO_OPTION);
   }

   /** уведомление о том, что на мозаике был произведён клик */
   private Consumer<ClickResult> clickEvent;
   public void setOnClickEvent(Consumer<ClickResult> handler) {
      clickEvent = handler;
   }
   public void acceptClickEvent(ClickResult clickResult) {
      if (clickEvent != null)
         clickEvent.accept(clickResult);
   }

   private class MosaicMouseListeners implements MouseInputListener, FocusListener {
      @Override
      public void mouseClicked(MouseEvent e) {}

      @Override
      public void mousePressed(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e)) {
            Mosaic.this.mousePressed(e.getPoint(), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
            Mosaic.this.mousePressed(e.getPoint(), false);
         }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         // Получаю этот эвент на отпускание клавиши даже тогда, когда окно проги неактивно..
         // Избегаю срабатывания onClick'a
         Component rootFrame = SwingUtilities.getRoot((Component) e.getSource());
         if (rootFrame instanceof Window) {
            boolean rootFrameActive = ((Window)rootFrame).isActive();
            if (!rootFrameActive)
               return;
         }

         if (SwingUtilities.isLeftMouseButton(e)) {
             Mosaic.this.mouseReleased(e.getPoint(), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
             Mosaic.this.mouseReleased(e.getPoint(), false);
         }
       }

      @Override
      public void mouseEntered(MouseEvent e) {}
      @Override
      public void mouseExited(MouseEvent e) {}
      @Override
      public void mouseDragged(MouseEvent e) {}
      @Override
      public void mouseMoved(MouseEvent e) {}
      @Override
      public void focusLost(FocusEvent e) {
         //System.out.println("Mosaic::MosaicMouseListeners::focusLost: " + e);
         Mosaic.this.mouseFocusLost();
      }
      @Override
      public void focusGained(FocusEvent e) {}
   }

   public MosaicMouseListeners getMosaicMouseListeners() {
      if (_mosaicMouseListener == null)
         _mosaicMouseListener = new MosaicMouseListeners();
      return _mosaicMouseListener;
   }

   @Override
   protected void initialize(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      this.getContainer().setFocusable(true); // иначе не будет срабатывать FocusListener

      this.getContainer().addMouseListener(getMosaicMouseListeners());
      this.getContainer().addMouseMotionListener(getMosaicMouseListeners());
      this.getContainer().addFocusListener(getMosaicMouseListeners());

      super.initialize(sizeField, mosaicType, minesCount, area);

      getContainer().setSize(getContainer().getPreferredSize()); // for run as java been
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "MosaicType":
         changeFontSize();
         break;
      case "Matrix":
         revalidate();
         break;
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      super.propertyChange(ev);
      if (ev.getSource() instanceof GraphicContext)
         onGraphicContextPropertyChanged((GraphicContext)ev.getSource(), ev);
   }

   @Override
   protected void onCellAttributePropertyChanged(BaseCell.BaseAttribute source, PropertyChangeEvent ev) {
      super.onCellAttributePropertyChanged(source, ev);
      if ("Area".equals(ev.getPropertyName())) {
         changeFontSize(getGraphicContext().getPenBorder());

         revalidate();
      }
   }

   private void onGraphicContextPropertyChanged(GraphicContext source, PropertyChangeEvent ev) {
      String propName = ev.getPropertyName();
      switch (propName) {
      case "PenBorder":
         PenBorder penBorder = (PenBorder)ev.getNewValue();
         changeFontSize(penBorder);
         break;
      //case "Font":
      //case "BackgroundFill":
      //   //Repaint(null);
      //   break;
      }
      Repaint(null);
      onPropertyChanged("GraphicContext");
      onPropertyChanged("GraphicContext." + propName);
   }

   /** пересчитать и установить новую высоту шрифта */
   public void changeFontSize() { changeFontSize(getGraphicContext().getPenBorder()); }
   /** пересчитать и установить новую высоту шрифта */
   private void changeFontSize(PenBorder penBorder) {
      getGraphicContext().setFontSize((int) getCellAttr().getSq(penBorder.getWidth()));
   }

   public static void main(String[] args) {
      JFrame frame = new JFrame();
      frame.add((new Mosaic()).getContainer());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
}
