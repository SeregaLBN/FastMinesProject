package fmg.core.img;

/** Image MVC: view (displayed view)
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel */
public interface IImageView<TImage> {

    TImage getImage();

    /** Mark the need to redraw the picture
     * Performs a call to the inner draw method (synchronously or asynchronously or implicitly, depending on the implementation) */
    void invalidate();

    /** image is actual? */
    boolean isValid();

    /** reset & invalidate */
    void reset();

}
