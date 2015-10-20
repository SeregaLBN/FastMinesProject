package fmg.swing.res.img;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

class ImageWrapper extends Image {
	private Image wrapper;

	public ImageWrapper(Image wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public int getWidth(ImageObserver observer) { return wrapper.getWidth(observer); }
	@Override
	public int getHeight(ImageObserver observer) { return wrapper.getHeight(observer); }
	@Override
	public ImageProducer getSource() { return wrapper.getSource(); }
	@Override 
	public Graphics getGraphics() { return wrapper.getGraphics(); }
	@Override
	public Object getProperty(String name, ImageObserver observer) {  return wrapper.getProperty(name, observer); }
}
