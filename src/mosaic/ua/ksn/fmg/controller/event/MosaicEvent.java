package ua.ksn.fmg.controller.event;

import java.util.EventObject;

import ua.ksn.fmg.controller.Mosaic;
import ua.ksn.fmg.controller.types.EGameStatus;
import ua.ksn.fmg.model.mosaics.EMosaic;

public class MosaicEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private MosaicEvent(Mosaic source) {
		super(source);
	}

	public static class ClickEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		boolean leftClick;
		boolean down;

		public ClickEvent(Mosaic source, boolean leftClick, boolean down) {
			super(source);
			this.leftClick  = leftClick;
			this.down = down;
		}

		public boolean isLeftClick() {
			return leftClick;
		}
		public boolean isDown() {
			return down;
		}

	}

	public static class ChangeCountersEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;

		public ChangeCountersEvent(Mosaic source) {
			super(source);
		}
	}

	public static class ChangeGameStatusEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		EGameStatus oldGameStatus;

		public ChangeGameStatusEvent(Mosaic source, EGameStatus oldGameStatus) {
			super(source);
			this.oldGameStatus = oldGameStatus;
		}
	
		public EGameStatus getOldGameStatus() {
			return oldGameStatus;
		}
	}

	public static class ChangeAreaEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		int oldArea;

		public ChangeAreaEvent(Mosaic source, int oldArea) {
			super(source);
			this.oldArea = oldArea;
		}
	
		public int getOldArea() {
			return oldArea;
		}
	}

	public static class ChangeMosaicTypeEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		EMosaic oldMosaic;

		public ChangeMosaicTypeEvent(Mosaic source, EMosaic oldMosaic) {
			super(source);
			this.oldMosaic = oldMosaic;
		}
	
		public EMosaic getOldMosaic() {
			return oldMosaic;
		}
	}

	@Override
	public Mosaic getSource() {
		return (Mosaic)super.getSource();
	}
}