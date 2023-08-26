package fmg.swing.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Image view implementation over {@link javax.swing.Icon} */
public class SwingIconView<TModel extends IImageModel> implements IImageView<javax.swing.Icon> {

    private final TModel model;
    private final Consumer<Graphics2D> draw;
    private boolean valid;
    private javax.swing.Icon image;
    private Graphics2D gBuffImg;

    public SwingIconView(TModel model, Consumer<Graphics2D> draw) {
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
    public javax.swing.Icon getImage() {
        if (image == null) {
            image = create((int)model.getSize().width, (int)model.getSize().height);
            valid = false;
        }
        if (!valid) {
            draw.accept(gBuffImg);
            valid = true;
        }
        return image;
    }

    private javax.swing.Icon create(int width, int height) {
        if (gBuffImg != null)
            gBuffImg.dispose();

        var buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        gBuffImg = buffImg.createGraphics();
        gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gBuffImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //gBuffImg.setClip(0, 0, s.width, s.height);

        return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return width; }
            @Override
            public int getIconHeight() { return height; }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.drawImage(buffImg, x,y, c);
            }
        };
    }

    @Override
    public void reset() {
        if (gBuffImg != null)
            gBuffImg.dispose();
        gBuffImg = null;
        image = null;
        valid = false;
    }

}
