package fmg.core.img;

import java.beans.PropertyChangeListener;

/**
 * MVC: view of images with burger menu (where burger menu its secondary model)
 * @param <TImage> - platform specific image
 * @param <TImageModel> - general model of image (not burger menu model)
 */
public abstract class WithBurgerMenuView<TImage, TImageModel extends ImageProperties> extends AImageView<TImage, TImageModel> {

   /** the second model of image */
   private final BurgerMenuModel _burgerMenuModel;
   private final PropertyChangeListener _burgerMenuModelListener;

   protected WithBurgerMenuView(TImageModel imageModel) {
      super(imageModel);
      _burgerMenuModel = new BurgerMenuModel(() -> imageModel.getSize());
      _burgerMenuModelListener = event -> {
         assert event.getSource() == _burgerMenuModel; // by reference
         onPropertyBurgerMenuModelChanged(event.getOldValue(), event.getNewValue(), event.getPropertyName());
      };
      _burgerMenuModel.addListener(_burgerMenuModelListener);
   }

   public BurgerMenuModel getBurgerMenuModel() { return _burgerMenuModel; }

   protected void onPropertyBurgerMenuModelChanged(Object oldValue, Object newValue, String propertyName) {
      invalidate();
   }

   @Override
   public void close() {
      _burgerMenuModel.removeListener(_burgerMenuModelListener);
      super.close();
   }

}
