package ua.ksn.fmg.event.click;

import java.util.ArrayList;
import java.util.List;

import ua.ksn.fmg.model.mosaics.cell.BaseCell;

public class LeftDownResult {
	public List<BaseCell> needRepaint;

	public LeftDownResult()
	{
		needRepaint = new ArrayList<BaseCell>();
	}
}
