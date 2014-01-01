using System.Collections.Generic;
using ua.ksn.fmg.model.mosaics.cell;

namespace ua.ksn.fmg.Event.click {

public class LeftDownResult {
	public IList<BaseCell> needRepaint;

	public LeftDownResult()
	{
		needRepaint = new List<BaseCell>();
	}
}
}