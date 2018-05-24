package fmg.core.mosaic;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import fmg.core.img.ImageView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.data.view.draw.PenBorder;

/**
 * MVC: view. Base mosaic view implementation
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImage2> image type of flag/mine into mosaic field
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

   public static boolean _DEBUG_DRAW_FLOW = false;
   private final Collection<BaseCell> _modifiedCells = new HashSet<>();

   @Override
   public void invalidate(Collection<BaseCell> modifiedCells) {
      if (modifiedCells == null) // mark NULL if all mosaic is changed
         _modifiedCells.clear();
      else
         _modifiedCells.addAll(modifiedCells);
      if (_DEBUG_DRAW_FLOW)
         System.out.println("AMosaicView.invalidate: " + ((modifiedCells==null) ? "all" : ("cnt=" + modifiedCells.size()) + ": " + modifiedCells.stream().limit(5).collect(Collectors.toList())));
      invalidate();
   }

   /** repaint all */
   @Override
   protected void drawBody() {
      if (_DEBUG_DRAW_FLOW)
         System.out.println("AMosaicView.drawBody: " + (_modifiedCells.isEmpty() ? "all" : ("cnt=" + _modifiedCells.size()) + ": " + _modifiedCells.stream().limit(5).collect(Collectors.toList())));
      draw(_modifiedCells.isEmpty() ? null : _modifiedCells);
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
      model.getFontInfo().setSize(model.getCellAttr().getSq((int)penBorder.getWidth()));
   }

}
