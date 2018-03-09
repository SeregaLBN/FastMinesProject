package fmg.core.img;

/**
 * MVC controlle of MosaicsGroup image
 *
 * @param <TImage> plaform specific image or picture or other display context/canvas/window/panel
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
