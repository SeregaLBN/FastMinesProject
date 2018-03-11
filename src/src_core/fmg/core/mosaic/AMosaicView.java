package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.data.view.draw.PenBorder;

/**
 * MVC: view. Base implementation
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TPaintContext> see {@link MosaicDrawModel}
 */
public abstract class AMosaicView<TPaintable extends IPaintable,
                                  TImage,
                                  TPaintContext extends MosaicDrawModel<TImage>>
               implements IMosaicView, AutoCloseable, PropertyChangeListener
{

   private MosaicGameModel _mosaic;
   private TPaintContext _paintContext;

   public static final String PROPERTY_PAINT_CONTEXT = "PaintContext";

   public abstract ICellPaint<TPaintable, TImage, TPaintContext> getCellPaint();

   @Override
   public abstract void invalidate(Collection<BaseCell> modifiedCells);

   public MosaicGameModel getMosaic() {
      return _mosaic;
   }
   public void setMosaic(MosaicGameModel mosaic) {
      if (_mosaic != null)
         _mosaic.removeListener(this);
      _mosaic = mosaic;
      if (_mosaic != null)
         _mosaic.addListener(this);
   }

   /** @return new TPaintContext() */
   protected abstract TPaintContext createPaintContext();

   public TPaintContext getPaintContext() {
      if (_paintContext == null)
         setPaintContext(createPaintContext());
      return _paintContext;
   }
   private void setPaintContext(TPaintContext paintContext) {
      if (_paintContext != null) {
         _paintContext.removeListener(this);
         _paintContext.close();
      }
      _paintContext = paintContext;
      if (_paintContext != null) {
         _paintContext.addListener(this); // изменение контекста -> перерисовка мозаики
         changeSizeImagesMineFlag();
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      if (ev.getSource() instanceof MosaicGameModel)
         onMosaicPropertyChanged((MosaicGameModel)ev.getSource(), ev);
      else
      if (getPaintContext().getClass().isAssignableFrom(ev.getSource().getClass())) // if (ev.getSource() instanceof TPaintContext)
      {
         @SuppressWarnings("unchecked")
         TPaintContext pc = (TPaintContext)ev.getSource();
         onPaintContextPropertyChanged(pc, ev);
      }
   }

   protected void onMosaicPropertyChanged(MosaicGameModel source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
         changeFontSize();
         break;
      case MosaicGameModel.PROPERTY_AREA:
         changeFontSize(getPaintContext().getPenBorder());
         changeSizeImagesMineFlag();
         break;
      case MosaicGameModel.PROPERTY_MATRIX:
         invalidate(null);
         break;
      }
   }

   private void onPaintContextPropertyChanged(TPaintContext source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case MosaicDrawModel.PROPERTY_PEN_BORDER:
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
   protected abstract void changeSizeImagesMineFlag();

   @Override
   public void close() {
      //super.close();
      setPaintContext(null); // unsubscribe & dispose
      setMosaic(null);
   }

}
