package fmg.swing.img;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Internal wrapper-image implementation over java.awt.Image */
@Deprecated
class ImageAwt {

    private IImageView<java.awt.Image, ? extends IImageModel> imageView;

    ImageAwt(IImageView<java.awt.Image, ? extends IImageModel> imageView) {
        this.imageView = imageView;
    }

    public java.awt.Image create() {
        SizeDouble s = imageView.getSize();
        return new BufferedImage((int)s.width, (int)s.height, BufferedImage.TYPE_INT_ARGB);
    }

    public void drawWrapper(Consumer<Graphics2D> drawBody) {
        BufferedImage img = (BufferedImage)imageView.getImage();
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        drawBody.accept(g);
        g.dispose();
    }

}
