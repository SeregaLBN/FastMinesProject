package fmg.common;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

/** See core file ./build.gradle */
public final class ConsoleImage {

    public static BufferedImage scaleToConsole(BufferedImage in) {
        int inW = in.getWidth();
        int inH = in.getHeight();
        int maxIn = Math.max(inW, inH);
        int maxOut = 35 + ThreadLocalRandom.current().nextInt(25);
        int outW = inW*maxOut/maxIn;
        int outH = (int)(0.6 * inH*maxOut/maxIn); // fix console char size
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_USHORT_GRAY);
        Graphics2D g = out.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, outW, outH);
        g.drawImage(in, 0, 0, outW, outH, null);
        out.flush();
        return out;
    }

    private static void printImage(BufferedImage img, PrintStream out) {
        int w = img.getWidth();
        int h = img.getHeight();
        for (int row = 0; row < h; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < w; col++)
                line.append((img.getRGB(col, row) == -1) ? " " : "█");
            out.println(line.toString());
        }
    }

    public static void main(String[] args) {
        try {
            final String fmgCanonicalLogo = "../res/Logo/Logo_2020x2020.png";
            BufferedImage img = ImageIO.read(new File(fmgCanonicalLogo));
            img = scaleToConsole(img);
            printImage(img, System.out);
        }catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

}
