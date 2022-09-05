package fmg.swing.img;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;

/** Image view implementation over {@link java.awt.Image} */
public class AwtImageView<TModel extends IImageModel2> implements IImageView2<java.awt.Image> {

    private final TModel model;
    private final Consumer<Graphics2D> draw;
    private boolean valid;
    private BufferedImage image;

    public AwtImageView(TModel model, Consumer<Graphics2D> draw) {
        this.model = model;
        this.draw = draw;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public java.awt.Image getImage() {
        if (image == null) {
            image = create((int)model.getSize().width, (int)model.getSize().height);
            valid = false;
        }
        if (!valid) {
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            draw.accept(g);
            g.dispose();
            valid = true;
        }
        return image;
    }

    private BufferedImage create(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void reset() {
        image = null;
        valid = false;
    }

}
