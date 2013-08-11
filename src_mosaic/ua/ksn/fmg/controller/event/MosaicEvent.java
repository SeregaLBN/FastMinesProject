package ua.ksn.fmg.controller.event;

import java.util.EventObject;

import ua.ksn.fmg.controller.Mosaic;

public class MosaicEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	boolean leftClick;
	boolean down;
	int realCountOpen;

	public MosaicEvent(Mosaic source) {
		super(source);
	}
	public MosaicEvent(Mosaic source, int realCountOpen) {
		super(source);
		this.realCountOpen = realCountOpen;
	}
	public MosaicEvent(Mosaic source, boolean leftClick, boolean down) {
		super(source);
		this.leftClick  = leftClick;
		this.down = down;
	}

	@Override
	public Mosaic getSource() {
		return (Mosaic)super.getSource();
	}

	public boolean isLeftClick() {
		return leftClick;
	}
	public boolean isDown() {
		return down;
	}
	public int getRealCountOpen() {
		return realCountOpen;
	}
}
