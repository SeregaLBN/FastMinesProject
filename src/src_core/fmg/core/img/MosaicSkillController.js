/**
 * MVC controller of {@link ESkillLevel} image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
class MosaicSkillController extends AnimatedImgController {

    constructor(/*bool*/ showBurgerMenu, /*TImageView*/ imageView) {
        super(imageView);

        this.View.burgerMenuModel.show = showBurgerMenu;

        this.usePolarLightFgTransforming(true);
        this.useRotateTransforming(true);
    }

}
