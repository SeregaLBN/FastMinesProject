package fmg.core.types.event;

import java.util.EventObject;

import fmg.common.geom.Size;
import fmg.core.mosaic.Mosaic;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;

public class MosaicEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private MosaicEvent(Mosaic source) {
		super(source);
	}

	public static class ClickEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		BaseCell cell;
		boolean leftClick;
		boolean down;

		public ClickEvent(Mosaic source, BaseCell clickedCell, boolean leftClick, boolean down) {
			super(source);
			this.cell = clickedCell;
			this.leftClick  = leftClick;
			this.down = down;
		}

		public boolean isLeftClick() {
			return leftClick;
		}
		public boolean isDown() {
			return down;
		}
		public BaseCell getCell() {
			return cell;
		}
	}

	public static class ChangedCountersEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;

		public ChangedCountersEvent(Mosaic source) {
			super(source);
		}
	}

	public static class ChangedGameStatusEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		EGameStatus oldGameStatus;

		public ChangedGameStatusEvent(Mosaic source, EGameStatus oldGameStatus) {
			super(source);
			this.oldGameStatus = oldGameStatus;
		}
	
		public EGameStatus getOldGameStatus() {
			return oldGameStatus;
		}
	}

	public static class ChangedAreaEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		int oldArea;

		public ChangedAreaEvent(Mosaic source, int oldArea) {
			super(source);
			this.oldArea = oldArea;
		}
	
		public int getOldArea() {
			return oldArea;
		}
	}

	public static class ChangedMosaicTypeEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		EMosaic oldMosaic;

		public ChangedMosaicTypeEvent(Mosaic source, EMosaic oldMosaic) {
			super(source);
			this.oldMosaic = oldMosaic;
		}
	
		public EMosaic getOldMosaic() {
			return oldMosaic;
		}
	}

	public static class ChangedMosaicSizeEvent extends MosaicEvent {
		private static final long serialVersionUID = 1L;
		Size oldSize;

		public ChangedMosaicSizeEvent(Mosaic source, Size oldSize) {
			super(source);
			this.oldSize = oldSize;
		}
	
		public Size getOldSize() {
			return oldSize;
		}
	}

	@Override
	public Mosaic getSource() {
		return (Mosaic)super.getSource();
	}
}