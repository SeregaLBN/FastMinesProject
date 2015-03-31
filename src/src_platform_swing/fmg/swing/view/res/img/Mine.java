package fmg.swing.view.res.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import javax.swing.Icon;

import fmg.swing.geom.Cast;
import fmg.swing.utils.ImgUtils;

/** mine image */
public class Mine extends ImageWrapper {
	public Mine() {
		super(ImgUtils.toImg(createIcon()));
	}

	@Deprecated
	public Mine(boolean newMineImage) {
		super(newMineImage
				? ImgUtils.toImg(createIcon())
				: createImageOld());
	}

	public static Icon createIcon() {
		return new Icon() {
			private Logo _logo;

			{
				_logo = new Logo(false);
				_logo.setMargin(10);
				_logo.setZoomX(0.7);
				_logo.setZoomY(0.7);
				for (int i = 0; i<_logo.Palette.length; i++)
					_logo.Palette[i] = Cast.toColor(Cast.toColor(_logo.Palette[i]).darker(0.5));
			}

			public int getIconWidth() {
				return _logo.getIconWidth();
			}

			public int getIconHeight() {
				return _logo.getIconHeight();
			}

			public void paintIcon(Component c, Graphics g, int x, int y) {
				_logo.paintIcon(c, g, x, y);
			}
		};
	}

	@Deprecated
	public static Image createImageOld() {
		int w = 25, h = 25;
		int pixels[] = new int[w*h];

		// fill background to transparent color
		for (int i=0; i<pixels.length; i++)
			pixels[i] = 0x00112233; // aarrggbb

		// paint image

		// центральное тело
		for (int x=7; x<18; x++)
			for (int y=7; y<18; y++)
				pixels[y*w+x] = 0xFF000000;

		// белая точка
		for (int x=9; x<11; x++)
			for (int y=9; y<11; y++)
				pixels[y*w+x] = 0xFFFFFFFF;

		// лучи
		for (int x=11; x<14; x++)
			for (int y=3; y<7; y++)
				pixels[y*w+x] = 0xFF000000;
		for (int x=18; x<22; x++)
			for (int y=11; y<14; y++)
				pixels[y*w+x] = 0xFF000000;
		for (int x=11; x<14; x++)
			for (int y=18; y<22; y++)
				pixels[y*w+x] = 0xFF000000;
		for (int x=3; x<7; x++)
			for (int y=11; y<14; y++)
				pixels[y*w+x] = 0xFF000000;

		// кончики лучей
		pixels[1*w+11] = 0x56000000; pixels[2*w+11] = 0x56000000; 
		pixels[0*w+12] = 0x56000000; pixels[1*w+12] = 0x8F000000; pixels[2*w+12] = 0x8F000000;
		pixels[1*w+13] = 0x56000000; pixels[2*w+13] = 0x56000000;

		pixels[22*w+11] = 0x56000000; pixels[23*w+11] = 0x56000000; 
		pixels[22*w+12] = 0x8F000000; pixels[23*w+12] = 0x8F000000; pixels[24*w+12] = 0x56000000;
		pixels[22*w+13] = 0x56000000; pixels[23*w+13] = 0x56000000;

		pixels[11*w+22] = 0x56000000; pixels[11*w+23] = 0x56000000; 
		pixels[12*w+22] = 0x8F000000; pixels[12*w+23] = 0x8F000000; pixels[12*w+24] = 0x56000000;
		pixels[13*w+22] = 0x56000000; pixels[13*w+23] = 0x56000000;

		pixels[11*w+1] = 0x56000000; pixels[11*w+2] = 0x56000000; 
		pixels[12*w+0] = 0x56000000; pixels[12*w+1] = 0x8F000000; pixels[12*w+2] = 0x8F000000;
		pixels[13*w+1] = 0x56000000; pixels[13*w+2] = 0x56000000;

		// точки по диагонали
		for (int x=5; x<7; x++)
			for (int y=5; y<7; y++)
				pixels[y*w+x] = 0xBB000000;
		for (int x=18; x<20; x++)
			for (int y=5; y<7; y++)
				pixels[y*w+x] = 0xBB000000;
		for (int x=5; x<7; x++)
			for (int y=18; y<20; y++)
				pixels[y*w+x] = 0xBB000000;
		for (int x=18; x<20; x++)
			for (int y=18; y<20; y++)
				pixels[y*w+x] = 0xBB000000;

		// точки 'под' лучами
		for (int x=9; x<11; x++)
			for (int y=5; y<7; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=14; x<16; x++)
			for (int y=5; y<7; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=18; x<20; x++)
			for (int y=9; y<11; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=18; x<20; x++)
			for (int y=14; y<16; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=14; x<16; x++)
			for (int y=18; y<20; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=9; x<11; x++)
			for (int y=18; y<20; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=5; x<7; x++)
			for (int y=14; y<16; y++)
				pixels[y*w+x] = 0x56000000;
		for (int x=5; x<7; x++)
			for (int y=9; y<11; y++)
				pixels[y*w+x] = 0x56000000;

		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w,h, pixels, 0,w));
	}
}
