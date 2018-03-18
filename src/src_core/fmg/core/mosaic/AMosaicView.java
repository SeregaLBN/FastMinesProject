package fmg.core.mosaic;

import java.util.Collection;
import java.util.HashSet;

import fmg.common.geom.RectDouble;
import fmg.core.img.ImageView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.data.view.draw.PenBorder;

/**
 * MVC: view. Base mosaic view implementation
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImage2> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TMosaicModel> mosaic data model
 */
public abstract class AMosaicView<TImage,
                                  TImage2,
                                  TMosaicModel extends MosaicDrawModel<TImage2>>
                extends ImageView<TImage, TMosaicModel>
                implements IMosaicView<TImage, TImage2, TMosaicModel>
{

   protected AMosaicView(TMosaicModel mosaicModel) {
      super(mosaicModel);
   }

   private Collection<BaseCell> _modifiedCells = new HashSet<>();

   @Override
   public void invalidate(Collection<BaseCell> modifiedCells) {
      _modifiedCells.addAll(modifiedCells);
      invalidate();
   }

   @Override
   public abstract void draw(Collection<BaseCell> modifiedCells, RectDouble clipRegion);

   /** repaint all */
   @Override
   protected void drawBody() {
      draw(_modifiedCells.isEmpty() ? null : _modifiedCells, null);
      _modifiedCells.clear();
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
         changeFontSize();
         break;
      case MosaicGameModel.PROPERTY_AREA:
         changeFontSize();
         changeSizeImagesMineFlag();
         break;
      case MosaicDrawModel.PROPERTY_PEN_BORDER:
         changeFontSize();
         break;
      }
   }

   /** пересчитать и установить новую высоту шрифта */
   private void changeFontSize() {
      TMosaicModel model = getModel();
      PenBorder penBorder = model.getPenBorder();
      model.getFontInfo().setSize((int)model.getCellAttr().getSq(penBorder.getWidth()));
   }

   /** переустанавливаю заного размер мины/флага для мозаики */
   protected abstract void changeSizeImagesMineFlag();

}
