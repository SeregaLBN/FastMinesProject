package fmg.swing;

import javax.swing.JApplet;

import fmg.swing.mosaic.Mosaic;

public class Applet extends JApplet {
	private static final long serialVersionUID = -8406501303115617115L;
	
	public void init() {
		setContentPane((new Mosaic().getContainer()));
	}

}
