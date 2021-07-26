/**
 * MVC controlle of logo image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
class LogoController extends AnimatedImgController {

    constructor(/*TImageView*/ imageView) {
        super(imageView);
    }

    useRotateTransforming(/*bool*/ enable) {
        super.useRotateTransforming(enable);
        if (enable)
            this.addModelTransformer(new RotateLogoTransformer());
        else
            this.removeModelTransformer(RotateLogoTransformer.className);
    }

    usePolarLightFgTransforming(/*bool*/ enable) {
        //super.usePolarLightFgTransforming(enable); // hide super implementation!
        if (enable)
            this.addModelTransformer(new PolarLightLogoTransformer());
        else
            this.removeModelTransformer(PolarLightLogoTransformer.className);
    }

}
