package fmg.swing.view;

import javax.swing.JApplet;

import fmg.swing.controller.MosaicExt;

public class Applet extends JApplet {
	private static final long serialVersionUID = -8406501303115617115L;
	
	public void init() {
		setContentPane((new MosaicExt().getContainer()));
	}

}
