package fmg.swing.draw.img;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.core.img.ATestDrawing;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing extends ATestDrawing {

   static final int margin = 10;

   static void testApp(Function<Random, List<?>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;

         {
            TestDrawing td = new TestDrawing();

            List<?> images = funcGetImages.apply(td.getRandom());

            boolean testTransparent = td.bl();


            final RectDouble[] rc = new RectDouble[1];
            final CellTilingResult[] ctr = new CellTilingResult[1];

            JPanel jPanel = new JPanel() {

               private static final long serialVersionUID = 1L;

               {
                  setPreferredSize(new Dimension(300, 300));
               }

               @Override
               public void paintComponent(Graphics g) {
                  super.paintComponent(g);
                  if (rc[0] == null)
                     return;

                  if ((rc[0].width <= 0) || (rc[0].height <= 0))
                     return;

                //g.clearRect((int)rc[0].x, (int)rc[0].y, (int)rc[0].width, (int)rc[0].height);
                  g.drawRect((int)rc[0].x, (int)rc[0].y, (int)rc[0].width, (int)rc[0].height);

                  Size imgSize = ctr[0].imageSize;
                  if ((imgSize.width <= 0) || (imgSize.height <= 0))
                     return;

                  images.stream()
                     .map(x -> (Object)x)
                     .forEach(imgObj -> {

                        @SuppressWarnings("unchecked")
                        Function<Object, CellTilingInfo> callback = (Function<Object, CellTilingInfo>)ctr[0].itemCallback;
                        CellTilingInfo cti = callback.apply(imgObj);
                        PointDouble offset = cti.imageOffset;

                        if (imgObj instanceof StaticImg) {
                           StaticImg<?> simg = (StaticImg<?>)imgObj;
                           simg.setSize(imgSize);
                           imgObj = simg.getImage();
                        }

                        if (imgObj instanceof Icon) {
                           Icon ico = (Icon)imgObj;
                         //ico = ImgUtils.zoom(ico, imgSize.width, imgSize.height);
                           ico.paintIcon(null, g, (int)offset.x, (int)offset.y);
                        } else
                        if (imgObj instanceof Image) {
                           Image img = (Image)imgObj;
                         //img = ImgUtils.zoom(img, imgSize.width, imgSize.height);
                           g.drawImage(img, (int)offset.x, (int)offset.y, null);
                        } else
                           throw new IllegalArgumentException("Not supported image type is " + imgObj.getClass().getName());
                     });
               }
            };
            add(jPanel);

            jPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent ev) {
                    Dimension size = jPanel.getSize();
                    rc[0] = new RectDouble(margin, margin, size.getWidth()-margin*2, size.getHeight()-margin*2); // inner rect where drawing images as tiles
                    ctr[0] = td.cellTiling(rc[0], images, testTransparent);
                }
            });

            PropertyChangeListener l = evt -> {
               if (RotatedImg.PROPERTY_IMAGE.equals(evt.getPropertyName())) {
                  jPanel.repaint();
               }
            };
            images.stream()
               .filter(x -> x instanceof StaticImg)
               .map(x -> (StaticImg<?>)x)
               .forEach(img -> {
                  img.addListener(l);
                  td.applyRandom(img, testTransparent);
               });

            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent we) {
                  images.stream()
                     .filter(x -> x instanceof StaticImg)
                     .map(x -> (StaticImg<?>)x)
                     .forEach(img -> img.removeListener(l));
                  images.stream()
                     .filter(x -> x instanceof AutoCloseable)
                     .map(x -> (AutoCloseable)x)
                     .forEach(x -> {
                        try {
                           x.close();
                        } catch (Exception ex) {
                           ex.printStackTrace();
                        }
                     });
                  dispose();
               }
            });

            setTitle(td.getTitle(images));
            setLocationRelativeTo(null);
            pack();
            setVisible(true);
         }
      };
   }

}
