package fmg.swing.draw.img;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.core.img.ATestDrawing;
import fmg.core.img.ImageController;

/** @see {@link MosaicsSkillImg#main}, {@link MosaicsGroupImg#main}, {@link MosaicsImg#main} */
final class TestDrawing extends ATestDrawing {

   static final int margin = 10;

   public TestDrawing() {
      super("Swing");
   }

   static void testApp(Supplier<List<ImageController<?,?,?>>> funcGetImages) {
      SwingUtilities.invokeLater(() ->
         testApp2(funcGetImages)
      );
   }
   static void testApp2(Supplier<List<ImageController<?,?,?>>> funcGetImages) {
      new JFrame() {
         private static final long serialVersionUID = 1L;

         {
            TestDrawing td = new TestDrawing();

            List<ImageController<?,?,?>> images = funcGetImages.get();

            boolean[] testTransparent = { td.bl() };


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
                  ((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
                  if (rc[0] == null)
                     return;

                  if ((rc[0].width <= 0) || (rc[0].height <= 0))
                     return;

                //g.clearRect((int)rc[0].x, (int)rc[0].y, (int)rc[0].width, (int)rc[0].height);
                  g.drawRect((int)rc[0].x, (int)rc[0].y, (int)rc[0].width, (int)rc[0].height);

                  Size imgSize = ctr[0].imageSize;
                  if ((imgSize.width <= 0) || (imgSize.height <= 0))
                     return;

                  images.forEach(imgController -> {
                     Function<ImageController<?,?,?>, CellTilingInfo> callback = ctr[0].itemCallback;
                     CellTilingInfo cti = callback.apply(imgController);
                     PointDouble offset = cti.imageOffset;

                     Object imgObj = imgController.getImage();
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

            ComponentListener componentListener = new ComponentAdapter() {
               @Override
               public void componentResized(ComponentEvent ev) {
                   Dimension size = jPanel.getSize();
                   rc[0] = new RectDouble(margin, margin, size.getWidth()-margin*2, size.getHeight()-margin*2); // inner rect where drawing images as tiles
                   ctr[0] = td.cellTiling(rc[0], images, testTransparent[0]);

                   Size imgSize = ctr[0].imageSize;
                   if (imgSize.height < 1 || imgSize.width < 1)
                      return;
                   for (ImageController<?, ?, ?> img : images)
                      img.getModel().setSize(imgSize);
               }
            };
            MouseListener mouseListener = new MouseAdapter() {
               @Override
               public void mousePressed(MouseEvent e) {
                  testTransparent[0] = td.bl();
                  images.forEach(img -> {
                     td.applyRandom(img, testTransparent[0]);
                     componentListener.componentResized(null);
                  });
               }
            };
            PropertyChangeListener propertyChangeListener = evt -> {
               if (ImageController.PROPERTY_IMAGE.equals(evt.getPropertyName()))
                  jPanel.repaint();
            };

            jPanel.addMouseListener(mouseListener);
            jPanel.addComponentListener(componentListener);
            images.forEach(img -> {
               img.addListener(propertyChangeListener);
               td.applyRandom(img, testTransparent[0]);
            });

            //setDefaultCloseOperation(EXIT_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent we) {
                  Animator.getSingleton().close();
                  images.forEach(img -> {
                     img.removeListener(propertyChangeListener);
                     img.close();
                  });
                  jPanel.removeMouseListener(mouseListener);
                  jPanel.removeComponentListener(componentListener);
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
