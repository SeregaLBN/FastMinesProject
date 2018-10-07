package fmg.swing.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Internal wrapper-image implementation over javax.swing.Icon */
class IconSwing implements AutoCloseable {

    private IImageView<javax.swing.Icon, ? extends IImageModel> _imageView;

    IconSwing(IImageView<javax.swing.Icon, ? extends IImageModel> imageView) {
        this._imageView = imageView;
    }

    private BufferedImage buffImg;
    private Graphics2D gBuffImg;
    public javax.swing.Icon create() {
        if (gBuffImg != null)
            gBuffImg.dispose();

        SizeDouble s = _imageView.getSize();
        buffImg = new BufferedImage((int)s.width, (int)s.height, BufferedImage.TYPE_INT_ARGB);
        gBuffImg = buffImg.createGraphics();
        gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gBuffImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //gBuffImg.setClip(0, 0, s.width, s.height);

        return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return (int)_imageView.getSize().width; }
            @Override
            public int getIconHeight() { return (int)_imageView.getSize().height; }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.drawImage(buffImg, x,y, c);
            }
        };
    }

    public Graphics2D getGraphics() { return gBuffImg; }

    @Override
    public void close() {
        if (gBuffImg != null)
            gBuffImg.dispose();
        gBuffImg = null;
    }

    }
