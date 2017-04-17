package fmg.core.mosaic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ThreadLocalRandom;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.draw.PaintContext;

/** MVC: controller. Default implementation */
public class MosaicController<TMosaicView extends AMosaicView<TPaintable, TImage, TPaintContext>,
                              TPaintable  extends IPaintable,
                              TImage,
                              TPaintContext extends PaintContext<TImage>>
      extends AMosaicController<TMosaicView>
{

   /** get view */
   @Override
   public TMosaicView getView() {
      if (_view == null) {
         Type type = getClass().getGenericSuperclass();
         ParameterizedType paramType = (ParameterizedType) type;
         @SuppressWarnings("unchecked")
         Class<TMosaicView> clazz = (Class<TMosaicView>) paramType.getActualTypeArguments()[0];
         try {
            setView(clazz.newInstance());
         } catch (Exception ex) {
            throw new RuntimeException(ex);
         }
      }
      return _view;
   }
   /** set view */
   @Override
   protected void setView(TMosaicView view) {
      if (_view != null)
         _view.close();
      _view = view;
      if (_view != null)
         _view.setMosaic(getMosaic());
   }

   @Override
   public boolean GameNew() {
      getView().getPaintContext().getBackgroundFill().setMode(
            1 + ThreadLocalRandom.current().nextInt(
                  MosaicHelper.createAttributeInstance(getMosaic().getMosaicType()).getMaxBackgroundFillModeValue()));
      boolean res = super.GameNew();
      if (!res)
         getView().invalidate();
      return res;
   }

   @Override
   public void GameBegin(BaseCell firstClickCell) {
      getView().getPaintContext().getBackgroundFill().setMode(0);
      super.GameBegin(firstClickCell);
   }

}
