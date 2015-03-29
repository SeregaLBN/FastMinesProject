package fmg.core.event.click;

import java.util.HashSet;
import java.util.Set;

import fmg.core.model.mosaics.cell.BaseCell;

public class ClickReportContext {
	/** множество ячеек (нулевых  ) открытых           при последнем клике */
	public Set<BaseCell> setOpenNil;
	/** множество ячеек (ненулевых) открытых           при последнем клике */
	public Set<BaseCell> setOpen;
	/** множество ячеек с флажками  снятых/уставленных при последнем клике */
	public Set<BaseCell> setFlag;

    public ClickReportContext() {
    	setOpenNil = new HashSet<BaseCell>();
    	setOpen = new HashSet<BaseCell>();
    	setFlag = new HashSet<BaseCell>();
    }
}
