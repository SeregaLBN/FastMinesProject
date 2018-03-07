package fmg.core.img;

/**
 * MVC controlle of MosaicsGroup image
 *
 * @param <TImage> plaform specific image
 * @param <TImageView> MVC view
 */
public class MosaicsGroupController<TImage,
                                    TImageView extends IImageView<TImage, MosaicsGroupModel>>
      extends AAnimatedImgController<TImage, TImageView, MosaicsGroupModel>
{

   protected MosaicsGroupController(TImageView imageView) {
      super(imageView);
      addModelTransformer(new MosaicsGroupTransformer());
   }

}
