package ua.ksn.fmg.view.swing;

import javax.swing.JApplet;

import ua.ksn.fmg.controller.swing.MosaicExt;

public class Applet extends JApplet {
	private static final long serialVersionUID = -8406501303115617115L;
	
	public void init() {
		setContentPane((new MosaicExt().getSwingPanel()));
	}

}
