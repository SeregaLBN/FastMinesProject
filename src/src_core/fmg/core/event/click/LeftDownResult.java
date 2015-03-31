package fmg.core.event.click;

import java.util.ArrayList;
import java.util.List;

import fmg.core.model.mosaics.cell.BaseCell;

public class LeftDownResult {
	public List<BaseCell> needRepaint;

	public LeftDownResult()
	{
		needRepaint = new ArrayList<BaseCell>();
	}
}
