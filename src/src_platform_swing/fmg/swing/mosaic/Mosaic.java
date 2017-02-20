package fmg.swing.mosaic;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import fmg.common.geom.Matrisize;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.PaintContext;
import fmg.core.types.EMosaic;
import fmg.core.types.click.ClickResult;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.utils.ImgUtils;

public class Mosaic extends MosaicBase<PaintableGraphics, Icon, PaintSwingContext<Icon>> implements AutoCloseable {

   private PaintSwingContext<Icon> _paintContext;
   private CellPaintGraphics<Icon> _cellPaint;
   private JPanel _container;
   private MosaicMouseListeners _mosaicMouseListener;
   private static final boolean _DEBUG = true;

   public Mosaic() {
      super();
   }

   public Mosaic(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      super(sizeField, mosaicType, minesCount, area);
   }

   public static final String PROPERTY_PAINT_CONTEXT = "PaintContext";

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
               g.setColor(Cast.toColor(getPaintContext().getBackgroundColor().darker(0.2)));
               g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);

               // paint cells
               g.setFont(getPaintContext().getFont());
               PaintableGraphics p = new PaintableGraphics(this, g);
               RectDouble clipBounds = Cast.toRectDouble(g.getClipBounds());
               for (BaseCell cell: getMatrix())
                  if (cell.getRcOuter().Intersects(clipBounds)) // redraw only when needed - when the cells and update region intersect
                     getCellPaint().paint(cell, p, getPaintContext());
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

   public PaintSwingContext<Icon> getPaintContext() {
      if (_paintContext == null)
         setPaintContext(new PaintSwingContext<>(false));
      return _paintContext;
   }
   private void setPaintContext(PaintSwingContext<Icon> paintContext) {
      PaintSwingContext<Icon> old = _paintContext;
      if (Objects.equals(paintContext, _paintContext))
         return;
      if (old != null)
         old.removeListener(this);
      _paintContext = paintContext;
      if (paintContext != null)
         _paintContext.addListener(this); // изменение контекста -> перерисовка мозаики

      _cellPaint = null;
   }

   @Override
   public CellPaintGraphics<Icon> getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics.Icon();
      }
      return _cellPaint;
   }

   protected void revalidate() {
      //getContainer().revalidate();
   }

   static final boolean ASYNC_PAINT = !true;
   @Override
   protected void repaint(java.util.List<BaseCell> needRepaint) {
      if (_alreadyPainted)
         throw new RuntimeException("Bad algorithm... (");

      if (needRepaint == null)
         _fullRepaint = true;
      else
         _toRepaint.addAll(needRepaint);

      if (ASYNC_PAINT)
         SwingUtilities.invokeLater(() -> {
            repaintAllMarked();
         });
      else
         repaintAllMarked();
   }

   private boolean _alreadyPainted;
   private boolean _fullRepaint = true;
   private final Set<BaseCell> _toRepaint = new HashSet<>();
   protected void repaintAllMarked() {
      if (_alreadyPainted)
         throw new RuntimeException("Bad algorithm... (");

      _alreadyPainted = true;
      try {
         if (_fullRepaint) {
            // redraw all of mosaic
            getContainer().repaint();
         } else {
            _toRepaint.forEach(cell -> getContainer().repaint(Cast.toRect(cell.getRcOuter())) );
         }
      } finally {
         _fullRepaint = false;
         if (_toRepaint != null)
            _toRepaint.clear();
         _alreadyPainted = false;
      }
   }

   @Override
   public boolean GameNew() {
      getPaintContext().getBackgroundFill().setMode(
            1 + new Random().nextInt(
                  MosaicHelper.createAttributeInstance(getMosaicType(), getArea()).getMaxBackgroundFillModeValue()));
      boolean res = super.GameNew();
      if (!res)
         repaint(null);
      return res;
   }

   @Override
   protected void GameBegin(BaseCell firstClickCell) {
      getPaintContext().getBackgroundFill().setMode(0);
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
   protected void onSelfPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onSelfPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case PROPERTY_MOSAIC_TYPE:
         changeFontSize();
         break;
      case PROPERTY_AREA:
         changeFontSize(getPaintContext().getPenBorder());
         changeSizeImagesMineFlag();
         break;
      case PROPERTY_MATRIX:
         revalidate();
         break;
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      super.propertyChange(ev);
      if (ev.getSource() instanceof PaintSwingContext)
         onPaintContextPropertyChanged((PaintSwingContext<?>)ev.getSource(), ev);
   }

   private void onPaintContextPropertyChanged(PaintSwingContext<?> source, PropertyChangeEvent ev) {
      String propName = ev.getPropertyName();
      switch (propName) {
      case PaintContext.PROPERTY_PEN_BORDER:
         PenBorder penBorder = (PenBorder)ev.getNewValue();
         changeFontSize(penBorder);
         break;
      //case PaintSwingContext.PROPERTY_FONT:
      //case PaintContext.PROPERTY_BACKGROUND_FILL:
      //   //repaint(null);
      //   break;
      }
      repaint(null);
      onSelfPropertyChanged(PROPERTY_PAINT_CONTEXT);
      onSelfPropertyChanged(PROPERTY_PAINT_CONTEXT + "." + propName);
   }

   /** пересчитать и установить новую высоту шрифта */
   public void changeFontSize() { changeFontSize(getPaintContext().getPenBorder()); }
   /** пересчитать и установить новую высоту шрифта */
   private void changeFontSize(PenBorder penBorder) {
      getPaintContext().getFontInfo().setSize((int) getCellAttr().getSq(penBorder.getWidth()));
   }

   /** переустанавливаю заного размер мины/флага для мозаики */
   private void changeSizeImagesMineFlag() {
      PaintSwingContext<Icon> pc = getPaintContext();
      int sq = (int)getCellAttr().getSq(pc.getPenBorder().getWidth());
      if (sq <= 0) {
         System.err.println("Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
         sq = 3; // ат балды...
      }
      pc.setImgFlag(ImgUtils.zoom(new Flag(), sq, sq));
      pc.setImgMine(ImgUtils.zoom(new Mine(), sq, sq));
   }

   @Override
   public void close() {
      super.close();
      getPaintContext().close();

      getContainer().removeMouseListener(getMosaicMouseListeners());
      getContainer().removeMouseMotionListener(getMosaicMouseListeners());
      getContainer().removeFocusListener(getMosaicMouseListeners());
   }

   /// TEST
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      Mosaic m = new Mosaic();
      frame.add(m.getContainer());
      //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) {
            frame.dispose();
            m.close();
         }
      });

      frame.pack();
      frame.setVisible(true);
   }
}
