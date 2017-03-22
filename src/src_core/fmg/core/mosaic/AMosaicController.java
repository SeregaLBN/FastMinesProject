package fmg.core.mosaic;

import java.util.function.Consumer;

import fmg.common.geom.PointDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.click.ClickResult;

/** MVC: controller. Base implementation */
public abstract class AMosaicController<TMosaicView extends IMosaicView> implements AutoCloseable {

   /** MVC: model */
   protected MosaicBase _mosaic;
   /** MVC: view */
   protected TMosaicView _view;

   /** get model */
   public MosaicBase getMosaic() {
      if (_mosaic == null)
         setMosaic(new MosaicBase());
      return _mosaic;
   }
   /** set model */
   protected void setMosaic(MosaicBase model) {
      if (_mosaic != null)
         _mosaic.close();
      _mosaic = model;
   }

   /** get view */
   public abstract TMosaicView getView();
   /** set view */
   protected abstract void setView(TMosaicView view);


   /** преобразовать экранные координаты в ячейку поля мозаики */
   private BaseCell CursorPointToCell(PointDouble point) {
      if (point == null)
            return null;
      for (BaseCell cell: getMosaic(). getMatrix())
         //if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            if (cell.PointInRegion(point))
               return cell;
      return null;
   }

   public ClickResult mousePressed(PointDouble clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? getMosaic().onLeftButtonDown(CursorPointToCell(clickPoint))
         : getMosaic().onRightButtonDown(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseReleased(PointDouble clickPoint, boolean isLeftMouseButton) {
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
