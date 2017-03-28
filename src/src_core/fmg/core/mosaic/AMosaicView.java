package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.draw.PaintContext;
import fmg.data.view.draw.PenBorder;

/**
 * MVC: view. Base implementation
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific image
 * @param <TPaintContext> see {@link PaintContext}
 */
public abstract class AMosaicView<TPaintable extends IPaintable,
                                  TImage,
                                  TPaintContext extends PaintContext<TImage>>
               implements AutoCloseable, IMosaicView, PropertyChangeListener
{

   private Mosaic _mosaic;
   private TPaintContext _paintContext;

   public static final String PROPERTY_PAINT_CONTEXT = "PaintContext";

   public abstract ICellPaint<TPaintable, TImage, TPaintContext> getCellPaint();

   @Override
   public abstract void invalidate();
   @Override
   public abstract void invalidate(Collection<BaseCell> modifiedCells);

   public Mosaic getMosaic() {
      return _mosaic;
   }
   public void setMosaic(Mosaic mosaic) {
      if (_mosaic != null)
         _mosaic.removeListener(this);
      _mosaic = mosaic;
      if (_mosaic != null)
         _mosaic.addListener(this);
   }

   private Class<TPaintContext> _paintContextClass;
   private Class<TPaintContext> getTPaintContextType() {
      if (_paintContextClass != null)
         return _paintContextClass;
      Type type = getClass();
      do {
         type = ((Class<?>)type).getGenericSuperclass();
      } while (type instanceof Class<?>);
      ParameterizedType paramType = (ParameterizedType) type;
      ParameterizedType param3 = (ParameterizedType)paramType.getActualTypeArguments()[2];
      @SuppressWarnings("unchecked")
      Class<TPaintContext> clazz = (Class<TPaintContext>)param3.getRawType();
      _paintContextClass = clazz;
      return clazz;
   }
   /** @return new TPaintContext() */
   protected TPaintContext createPaintContext() {
      try {
         Class<TPaintContext> clazz = getTPaintContextType();
         TPaintContext pc = clazz.newInstance();
         return pc;
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

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
      if (ev.getSource() instanceof Mosaic)
         onMosaicPropertyChanged((Mosaic)ev.getSource(), ev);
      if (getTPaintContextType().isAssignableFrom(ev.getSource().getClass())) // if (ev.getSource() instanceof TPaintContext)
      {
         @SuppressWarnings("unchecked")
         TPaintContext pc = (TPaintContext)ev.getSource();
         onPaintContextPropertyChanged(pc, ev);
      }
   }

   protected void onMosaicPropertyChanged(Mosaic source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case Mosaic.PROPERTY_MOSAIC_TYPE:
         changeFontSize();
         break;
      case Mosaic.PROPERTY_AREA:
         changeFontSize(getPaintContext().getPenBorder());
         changeSizeImagesMineFlag();
         break;
      case Mosaic.PROPERTY_MATRIX:
         invalidate();
         break;
      }
   }

   private void onPaintContextPropertyChanged(TPaintContext source, PropertyChangeEvent ev) {
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
   protected abstract void changeSizeImagesMineFlag();

   @Override
   public void close() {
      setPaintContext(null); // unsubscribe & dispose
   }

}
