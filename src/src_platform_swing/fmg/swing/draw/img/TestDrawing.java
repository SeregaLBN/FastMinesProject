package fmg.swing.draw.img;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.Size;
import fmg.core.img.PolarLightsImg;
import fmg.core.img.RotatedImg;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing {
   static final int SIZE = 300;
   static final int offset = 10;

   private static Random rnd = new Random(UUID.randomUUID().hashCode());
   private static int r(int max){ return rnd.nextInt(max); }
   private static boolean bl() { return (r(2) == 1); } // random bool
   private static int np() { return (bl() ? -1 : +1); } // negative or positive

   static <TImage> void applyRandom(RotatedImg<TImage> img) {
      int maxSize = (int)(SIZE/2.0 * 1.2);
      int minSize = (int)(maxSize * 0.8);
      img.setSize(new Size(minSize+r(maxSize-minSize), minSize+r(maxSize-minSize)));

      img.setRotate(true);
      img.setRotateAngleDelta((3 + r(5)) * np());
      img.setRedrawInterval(50);
      img.setBorderWidth(bl() ? 1 : 2);

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

   static void testApp(Function<Random, Pair<RotatedImg<?>, RotatedImg<?>>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;
         {
            Pair<RotatedImg<?>, RotatedImg<?>> icoImg = funcGetImages.apply(rnd);
            RotatedImg<?> img1 = icoImg.first;
            RotatedImg<?> img2 = icoImg.second;

            setSize(SIZE+30, SIZE+50);
            setTitle("test paints " + img1.getClass().getName() + " & " + img2.getClass().getName());
            setLocationRelativeTo(null);

            JPanel jPanel = new JPanel() {
               private static final long serialVersionUID = 1L;
               {
                  setPreferredSize(new Dimension(SIZE, SIZE));
               }
               @Override
               public void paintComponent(Graphics g) {
                  super.paintComponent(g);
                  //g.clearRect(offset, offset, SIZE-offset*2, SIZE-offset*2);
                  g.drawRect(offset, offset, SIZE-offset*2, SIZE-offset*2);

                  Object img = img1.getImage();
                  if (img instanceof Icon)
                     ((Icon)img).paintIcon(this, g, 2*offset, 2*offset);

                  img = img2.getImage();
                  if (img instanceof Image)
                     g.drawImage((Image)img, SIZE-2*offset-img2.getWidth(), SIZE-2*offset-img2.getHeight(), null);
               }
            };
            add(jPanel);

            PropertyChangeListener l = evt -> {
               if (RotatedImg.PROPERTY_IMAGE.equals(evt.getPropertyName())) {
                  //jPanel.invalidate();
                  //jPanel.revalidate();
                  jPanel.repaint();
               }
            };
            img1.addListener(l);
            img2.addListener(l);

            applyRandom(img1);
            applyRandom(img2);

            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent we) {
                  img1.removeListener(l);
                  img2.removeListener(l);
                  img1.close();
                  img2.close();
                  dispose();
               }
            });
            setVisible(true);
         }
      };
   }

   static void testApp2(Function<Integer /* size */, Icon> funcGetImage) {
      new JFrame() {
         private static final long serialVersionUID = 1L;
         {
             Icon ico = funcGetImage.apply(SIZE-offset*2);

             setSize(SIZE+20, SIZE+50);
             setTitle("test paints " + ico.getClass().getName());
             setLocationRelativeTo(null);

             JPanel jPanel = new JPanel() {
                private static final long serialVersionUID = 1L;
                {
                   setPreferredSize(new Dimension(SIZE, SIZE));
                }
                @Override
                public void paintComponent(Graphics g) {
                   super.paintComponent(g);
                   //g.clearRect(offset, offset, SIZE-offset*2, SIZE-offset*2);
                   g.drawRect(offset, offset, SIZE-offset*2, SIZE-offset*2);

                   ico.paintIcon(this, g, offset, offset);
                }

             };
             add(jPanel);

             setDefaultCloseOperation(EXIT_ON_CLOSE);
             setVisible(true);
         }
      };
   }

}
