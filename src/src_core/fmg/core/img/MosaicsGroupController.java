package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/**
 * MVC controller of {@link EMosaicGroup} image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public class MosaicsGroupController<TImage,
                                    TImageView extends IImageView<TImage, MosaicsGroupModel>>
      extends AnimatedImgController<TImage, TImageView, MosaicsGroupModel>
{

   protected MosaicsGroupController(TImageView imageView) {
      super(imageView);
      addModelTransformer(new MosaicsGroupTransformer());
   }

}
