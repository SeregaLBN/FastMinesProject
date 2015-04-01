using System.Collections.Generic;
using fmg.core.model.mosaics.cell;

namespace fmg.core.Event.click {

public class LeftDownResult {
	public IList<BaseCell> needRepaint;

	public LeftDownResult()
	{
		needRepaint = new List<BaseCell>();
	}
}
}