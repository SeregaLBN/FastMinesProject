using System.Collections.Generic;
using fmg.core.mosaic.cells;

namespace fmg.core.types.click {

public class LeftDownResult {
	public IList<BaseCell> needRepaint;

	public LeftDownResult()
	{
		needRepaint = new List<BaseCell>();
	}
}
}