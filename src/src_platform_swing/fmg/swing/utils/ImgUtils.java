package fmg.swing.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/** вспомогательный класс для преобразований картинок */
public final class ImgUtils {

   /** загрузить картинку из локальных ресурсов */
   public static Image getImage(String resName) {
      URL url = ImgUtils.class.getResource("/"+resName);
      if (url != null)
         return Toolkit.getDefaultToolkit().createImage(url);
      return null;
//      String classPath = System.getProperties().getProperty("java.class.path");
//      if (classPath.toLowerCase().endsWith(".jar")) {
//          JarResources jar = new JarResources(classPath);
//          return Toolkit.getDefaultToolkit().createImage(jar.getResource(resName));
//      } else
//         try {
//            return Toolkit.getDefaultToolkit().getImage(
//                  new File(classPath +
//                        System.getProperties().getProperty("file.separator") +
//                        resName).toURI().toURL());
//         } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//            throw new RuntimeException(ex);
//         }
   }
   /** загрузить картинку из локальных ресурсов */
   public static Icon getIcon(String resName) {
      return toIco(getImage(resName));
   }

   /** convert Image -> Icon */
   public static Icon toIco(Image img) {
      if (img == null) return null;
      return new ImageIcon(img);
   }

   /** convert and change size Image -> Icon */
   public static Icon toIco(Image img, int newWidth, int newHeight) {
      if (img == null) return null;
      return new ImageIcon(img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
   }

   /** change size Icon */
   public static Icon zoom(Icon img, int newWidth, int newHeight) {
      if (img == null) return null;
      return toIco(
            (img instanceof ImageIcon)
               ? ((ImageIcon)img).getImage()
               : toImg(img),
            newWidth, newHeight);
   }

   public static Image zoom(Image img, int newWidth, int newHeight) {
      return img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
   }

   /** convert Icon -> Image */
   public static Image toImg(Icon ico) {
       if (ico instanceof ImageIcon)
           return ((ImageIcon) ico).getImage();

      //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      //GraphicsDevice gd = ge.getDefaultScreenDevice();
      //GraphicsConfiguration gc = gd.getDefaultConfiguration();
      //BufferedImage img = gc.createCompatibleImage(ico.getIconWidth(), ico.getIconHeight());
        BufferedImage img = new BufferedImage(ico.getIconWidth(), ico.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ico.paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
   }

   public static Image rotate(Image inputImage, double degrees) {
      BufferedImage sourceBI = new BufferedImage(
            inputImage.getWidth(null),
            inputImage.getHeight(null),
            BufferedImage.TYPE_INT_ARGB);

       Graphics2D g = (Graphics2D) sourceBI.getGraphics();
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
       g.drawImage(inputImage, 0, 0, null);

       AffineTransform at = new AffineTransform();

       // scale image
//       at.scale(1.0, 1.0);

       // rotate around image center
       at.rotate(degrees * Math.PI / 180.0, sourceBI.getWidth() / 2.0, sourceBI.getHeight() / 2.0);
//       at.rotate(degrees * Math.PI / 180.0, 0, 0);

       // translate to make sure the rotation doesn't cut off any image data
//       AffineTransform translationTransform = findTranslation(at, sourceBI);
//       at.preConcatenate(translationTransform);

       // instantiate and apply affine transformation filter
       BufferedImageOp bio;
       bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

       return bio.filter(sourceBI, null);
   }

//   /** find proper translations to keep rotated image correctly displayed */
//   private static AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
//      java.awt.geom.Point2D p2din, p2dout;
//
//      p2din = new java.awt.geom.Point2D.Double(0.0, 0.0);
//      p2dout = at.transform(p2din, null);
//      double ytrans = p2dout.getY();
//
//      p2din = new java.awt.geom.Point2D.Double(0, bi.getHeight());
//      p2dout = at.transform(p2din, null);
//      double xtrans = p2dout.getX();
//
//      AffineTransform tat = new AffineTransform();
//      tat.translate(-xtrans, -ytrans);
//      return tat;
//   }
}
