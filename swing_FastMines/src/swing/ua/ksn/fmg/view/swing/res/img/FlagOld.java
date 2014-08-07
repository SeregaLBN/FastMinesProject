package ua.ksn.fmg.view.swing.res.img;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

/** flag image */
@Deprecated
public class FlagOld extends ImageWrapper {
	public FlagOld() {
		super(createImage());
	}

	public static Image createImage() {
		int w = 11, h = 11;
		int pixels[] = new int[w*h];

		// fill background to transparent color
		for (int i=0; i<pixels.length; i++)
			pixels[i] = 0x00112233; // aarrggbb

		// paint image

		// центральная стойка
		for (int y=5; y<10; y++)
				pixels[y*w+6] = 0xFF000000;

		// поддон
		for (int x=4; x<9; x++)
				pixels[10*w+x] = 0xFF000000;
		pixels[10*w+3] =
		pixels[9*w+5] =
		pixels[9*w+7] =
		pixels[10*w+9] = 0x7F000000;

		// флаг
		int mX = 6;
		for (int y=1; y<5; y++, mX--)
			for (int x=2; x<mX; x++)
				pixels[y*w+x] = 0xFFFF0000;
		mX = 6;
		for (int y=1; y<5; y++, mX--)
			for (int x=mX; x<7; x++)
				pixels[y*w+x] = 0xFF800000;

		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w,h, pixels, 0,w));
	}
}
