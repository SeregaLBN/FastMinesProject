package fmg.core.mosaic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.draw.PaintContext;

/** MVC: controller. Default implementation */
public class MosaicController<TMosaicView extends AMosaicView<TPaintable, TImage, TPaintContext>,
                              TPaintable  extends IPaintable,
                              TImage,
                              TPaintContext extends PaintContext<TImage>> extends AMosaicController<TMosaicView>
{

   /** get model */
   @Override
   public MosaicBase getMosaic() {
      if (_mosaic == null)
         setMosaic(new MosaicIntenal());
      return _mosaic;
   }

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

   protected class MosaicIntenal extends MosaicBase {

      private TMosaicView getView() { return MosaicController.this.getView(); }

      @Override
      protected boolean checkNeedRestoreLastGame() {
         // TODO: override in child classes
//         int iRes = JOptionPane.showOptionDialog(getView().getControl(), "Restore last game?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
//         return (iRes == JOptionPane.NO_OPTION);
         return super.checkNeedRestoreLastGame();
      }

      @Override
      public boolean GameNew() {
         getView().getPaintContext().getBackgroundFill().setMode(
               1 + new Random().nextInt(
                     MosaicHelper.createAttributeInstance(getMosaicType()).getMaxBackgroundFillModeValue()));
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

}
