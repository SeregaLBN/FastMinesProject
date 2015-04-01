using System.Collections.Generic;
using fmg.core.model.mosaics.cell;

namespace fmg.core.Event.click {

public class ClickReportContext {
   /// <summary>множество ячеек (нулевых  ) открытых           при последнем клике</summary>
   public ISet<BaseCell> setOpenNil;
   /// <summary>множество ячеек (ненулевых) открытых           при последнем клике</summary>
	public ISet<BaseCell> setOpen;
   /// <summary>множество ячеек с флажками  снятых/уставленных при последнем клике</summary>
	public ISet<BaseCell> setFlag;

    public ClickReportContext() {
    	setOpenNil = new HashSet<BaseCell>();
    	setOpen = new HashSet<BaseCell>();
    	setFlag = new HashSet<BaseCell>();
    }
}
}