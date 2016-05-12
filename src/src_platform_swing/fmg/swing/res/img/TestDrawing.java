package fmg.swing.res.img;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.Size;
import fmg.core.img.PolarLightsImg;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.swing.Cast;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing {

   static <TEntity> void testApp(Function<Random, Pair<StaticImg<TEntity, javax.swing.Icon>, StaticImg<TEntity, java.awt.Image>>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;
         static final int SIZE = 600;
         {
             Random rnd = new Random(UUID.randomUUID().hashCode());
             Pair<StaticImg<TEntity, javax.swing.Icon>, StaticImg<TEntity, java.awt.Image>> icoImg = funcGetImages.apply(rnd);
             StaticImg<TEntity, javax.swing.Icon> img1 = icoImg.first;
             StaticImg<TEntity, java.awt.Image> img2 = icoImg.second;
             Size size = new Size(SIZE/2, SIZE/2);
             img1.setSize(size);
             img2.setSize(size);

             setSize(SIZE+30, SIZE+50);
             setTitle("test paints " + img1.getClass().getName() + " & " + img2.getClass().getName());
             setLocationRelativeTo(null);

             JPanel jPanel = new JPanel() {
                private static final long serialVersionUID = 1L;
                {
                   setPreferredSize(new Dimension(SIZE, SIZE));
                }
                final int offset = 10;
                @Override
                public void paintComponent(Graphics g) {
                   super.paintComponent(g);
                   //g.clearRect(offset, offset, SIZE-offset, SIZE-offset);
                   g.drawRect(offset, offset, SIZE-offset, SIZE-offset);

                   img1.getImage().paintIcon(this, g, 2*offset, 2*offset);
                   g.drawImage(img2.getImage(), SIZE/2-offset, SIZE/2-offset, null);

                   //testHsv(g);
                }

                @SuppressWarnings("unused")
                private void testHsv(Graphics g) {
                   int offsetX = SIZE-offset-360, offsetY = offset*2;
                   HSV hsv = new HSV();

                   hsv.v = 100;
                   for (int h = 0; h < 360; ++h) {
                      hsv.h = h;
                      for (int s = 0; s <= 100; ++s) {
                         hsv.s = s;
                         g.setColor(Cast.toColor(hsv.toColor()));
                         g.drawLine(offsetX+h, offsetY+s, offsetX+h, offsetY+s); // set point
                      }
                   }

                   offsetY += 200;

                   hsv.s = 100;
                   for (int h = 0; h < 360; ++h) {
                      hsv.h = h;
                      for (int v = 0; v <= 100; ++v) {
                         hsv.v = v;
                         g.setColor(Cast.toColor(hsv.toColor()));
                         g.drawLine(offsetX+h, offsetY-v, offsetX+h, offsetY-v); // set point
                      }
                   }
                }
             };
             add(jPanel);

             PropertyChangeListener l = evt -> {
                if ("Image".equals(evt.getPropertyName())) {
                   //jPanel.invalidate();
                   //jPanel.revalidate();
                   jPanel.repaint();
                }
             };
             img1.addListener(l);
             img2.addListener(l);

             final boolean testTransparent = !false;
             if (testTransparent) {  // test transparent
                Color bkClr = Color.RandomColor(rnd); bkClr.setA((byte)0x40); // Color.Transparent; //
                img1.setBackgroundColor(bkClr);
                bkClr = Color.RandomColor(rnd); bkClr.setA((byte)0x30); // Color.Transparent; //
                img2.setBackgroundColor(bkClr);
             } else {
                img1.setBackgroundColor(Cast.toColor(jPanel.getBackground()));
                img2.setBackgroundColor(Cast.toColor(jPanel.getBackground()));
             }

             if (img1 instanceof PolarLightsImg) {
                PolarLightsImg<TEntity, javax.swing.Icon> imgP1 = (PolarLightsImg<TEntity, javax.swing.Icon>)img1;
                imgP1.setPolarLights(true);
             }
             if (img2 instanceof PolarLightsImg) {
                PolarLightsImg<TEntity, java.awt.Image> imgP2 = (PolarLightsImg<TEntity, java.awt.Image>)img2;
                imgP2.setPolarLights(true);
             }
             if (img1 instanceof RotatedImg) {
                RotatedImg<TEntity, javax.swing.Icon> imgR1 = (RotatedImg<TEntity, javax.swing.Icon>)img1;
                imgR1.setRotateAngleDelta(-(3/*+rnd.nextInt(3)*/)*imgR1.getRotateAngleDelta());
                imgR1.setRotate(true);
             } else {
                //img1.setRotateAngle(+15 + rnd.nextInt(15));
                img1.setRotateAngle(+33.3333);
             }
             if (img2 instanceof RotatedImg) {
                RotatedImg<TEntity, java.awt.Image> imgR2 = (RotatedImg<TEntity, java.awt.Image>)img2;
                imgR2.setRotateAngleDelta(+(5/*+rnd.nextInt(3)*/)*imgR2.getRotateAngleDelta());
                imgR2.setRotate(true);
             } else {
                //img2.setRotateAngle( -7 - rnd.nextInt(8));
                img2.setRotateAngle(-15);
             }

             //setDefaultCloseOperation(EXIT_ON_CLOSE);
             addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                   img1.close();
                   img2.close();
                   dispose();
                }
             });
             setVisible(true);
         }
      };
   }

}
