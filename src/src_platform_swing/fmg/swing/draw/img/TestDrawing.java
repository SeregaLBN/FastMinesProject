package fmg.swing.draw.img;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;
import fmg.core.img.ITestDrawing;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.swing.utils.ImgUtils;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing implements ITestDrawing {
   static final int SIZE = 300;
   static final int margin = 10;

   static Random rnd = new Random(UUID.randomUUID().hashCode());
   @Override
   public Random getRandom() { return rnd; }

   static void testApp(Function<Pair<Size, Random>, List<?>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;

         {
            Rect rc = new Rect(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
            List<?> images = funcGetImages.apply(new Pair<>(rc.size(), rnd));

            TestDrawing app = new TestDrawing();
            boolean testTransparent = app.bl();
            Pair<Size, // image size
                 Function<? /* image */, PointDouble /* image offset */>> // Stream mapper
               cellTilings = app.cellTiling(rc, images, testTransparent);

            JPanel jPanel = new JPanel() {

               private static final long serialVersionUID = 1L;

               {
                  setPreferredSize(new Dimension(SIZE, SIZE));
               }

               @Override
               public void paintComponent(Graphics g) {
                  super.paintComponent(g);
                  //g.clearRect(rc.x, rc.y, rc.width, rc.height);
                  g.drawRect(rc.x, rc.y, rc.width, rc.height);

                  Size imgSize = cellTilings.first;
                  Function<? /* image */, PointDouble /* image offset */> mapper = cellTilings.second;
                  images.stream()
                     .map(x -> (Object)x)
                     .forEach(imgObj -> {

                        @SuppressWarnings("unchecked")
                        Function<Object, PointDouble> mapper2 = (Function<Object, PointDouble>)mapper;
                        PointDouble offset = mapper2.apply(imgObj);

                        if (imgObj instanceof StaticImg) {
                           StaticImg<?> simg = (StaticImg<?>)imgObj;
                           simg.setSize(imgSize);
                           imgObj = simg.getImage();
                        }

                        if (imgObj instanceof Icon) {
                           Icon ico = (Icon)imgObj;
                           ico = ImgUtils.zoom(ico, imgSize.width, imgSize.height);
                           ico.paintIcon(null, g, (int)offset.x, (int)offset.y);
                        } else
                        if (imgObj instanceof Image) {
                           Image img = (Image)imgObj;
                           img = ImgUtils.zoom(img, imgSize.width, imgSize.height);
                           g.drawImage(img, (int)offset.x, (int)offset.y, null);
                        } else
                           throw new IllegalArgumentException("Not supported image type is " + imgObj.getClass().getName());
                     });
               }
            };
            add(jPanel);

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
                  app.applyRandom(img, testTransparent);
               });

            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent we) {
                  images.stream()
                     .filter(x -> x instanceof StaticImg)
                     .map(x -> (StaticImg<?>)x)
                     .forEach(img -> {
                        img.removeListener(l);
                        img.close();
                     });
                  dispose();
               }
            });

            setTitle("test paints: " + images.stream()
               .map(i -> i.getClass().getName())
               .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replaceAll("\\$", ".") )
               .collect(Collectors.joining(" & ")));
            setLocationRelativeTo(null);
            pack();
            setVisible(true);
         }
      };
   }

}
