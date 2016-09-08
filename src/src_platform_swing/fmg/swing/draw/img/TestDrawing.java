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

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;
import fmg.core.img.PolarLightsImg;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.swing.utils.ImgUtils;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing {
   static final int SIZE = 300;
   static final int margin = 10;

   private static Random rnd = new Random(UUID.randomUUID().hashCode());
   private static int r(int max){ return rnd.nextInt(max); }
   private static boolean bl() { return (r(2) == 1); } // random bool
   private static int np() { return (bl() ? -1 : +1); } // negative or positive

   static void applyRandom(StaticImg<?> img) {
      int maxSize = (int)(SIZE/2.0 * 1.2);
      int minSize = (int)(maxSize * 0.8);
      img.setSize(new Size(minSize+r(maxSize-minSize), minSize+r(maxSize-minSize)));

      if (img instanceof RotatedImg) {
         RotatedImg<?> rImg = (RotatedImg<?>)img;
         rImg.setRotate(true);
         rImg.setRotateAngleDelta((3 + r(5)) * np());
         rImg.setRedrawInterval(50);
         rImg.setBorderWidth(bl() ? 1 : 2);
      }

      if (img instanceof PolarLightsImg) {
         PolarLightsImg<?> plImg = (PolarLightsImg<?>)img;
         plImg.setPolarLights(true);
      }

      if (img instanceof Logo) {
         Logo<?> logoImg = (Logo<?>)img;
         logoImg.setRotateMode(Logo.ERotateMode.values()[r(Logo.ERotateMode.values().length)]);
         logoImg.setUseGradient(bl());
      }

      if (img instanceof MosaicsImg) {
         MosaicsImg<?> mosaicsImg = (MosaicsImg<?>)img;
         mosaicsImg.setRotateMode(MosaicsImg.ERotateMode.values()[r(MosaicsImg.ERotateMode.values().length)]);
      }

      if (bl()) {
         // test transparent
         HSV bkClr = new HSV(Color.RandomColor(rnd)); bkClr.a = 50 + r(10);
         img.addListener(ev -> {
            if (RotatedImg.PROPERTY_ROTATE_ANGLE.equals(ev.getPropertyName())) {
               bkClr.h = img.getRotateAngle();
               img.setBackgroundColor(bkClr.toColor());
            }
         });
      } else {
         img.setBackgroundColor(Color.RandomColor(rnd).brighter());
      }
   }

   static void testApp(Function<Pair<Size, Random>, List<?>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;

         {
            Rect rc = new Rect(margin, margin, SIZE-margin*2, SIZE-margin*2); // inner rect where drawing images as tiles
            List<?> images = funcGetImages.apply(new Pair<>(rc.size(), rnd));
            int len = images.size();
            int cols = (int)Math.round( Math.sqrt(len)  + 0.4999999999); // columns
            int rows = (int)Math.round(len/(double)cols + 0.4999999999);
            int dx = rc.width  / cols; // cell tile width
            int dy = rc.height / rows; // cell tile height

            int pad = 2; // cell padding
            int w = Math.min(dx, dy) - 2*pad; // dx - 2*pad;
            int h = Math.min(dx, dy) - 2*pad; // dy - 2*pad;

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

                  for (int i=0; i<cols; ++i)
                     for (int j=0; j<rows; ++j) {
                        int pos = cols*j+i;
                        if (pos >= len)
                           break;

                        Object obj = images.get(pos);
                        if (obj instanceof StaticImg) {
                           @SuppressWarnings("resource")
                           StaticImg<?> simg = (StaticImg<?>)obj;
                           simg.setSize(new Size(w, h));
                           obj = simg.getImage();
                        }
                        if (obj instanceof Icon) {
                           Icon ico = (Icon)obj;
                           ico = ImgUtils.zoom(ico, w, h);
                           ico.paintIcon(null, g, margin+i*dx+pad, margin+j*dy+pad);
                        } else
                        if (obj instanceof Image) {
                           Image img = (Image)obj;
                           img = ImgUtils.zoom(img, w, h);
                           g.drawImage(img, margin+i*dx+pad, margin+j*dy+pad, null);
                        } else
                           throw new IllegalArgumentException("Not supported image type is " + obj.getClass().getName());
                     }
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
                  applyRandom(img);
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

            setTitle("test paints: " + images.stream().map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(" & ")));
            setLocationRelativeTo(null);
            pack();
            setVisible(true);
         }
      };
   }

}
