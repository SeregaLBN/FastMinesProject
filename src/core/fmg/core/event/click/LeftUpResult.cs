using System.Collections.Generic;
using ua.ksn.fmg.model.mosaics.cell;

namespace ua.ksn.fmg.Event.click {

public class LeftUpResult {
	public int countFlag, countOpen, countUnknown;
	public bool endGame, victory;
	public IList<BaseCell> needRepaint;

	public LeftUpResult()
	{
		this.countFlag =
		this.countOpen =
		this.countUnknown = 0;
		this.endGame =
		this.victory = false;
		needRepaint = null; // new ArrayList<BaseCell>();
	}

   public LeftUpResult(int iCountFlag, int iCountOpen, int iCountUnknown, bool bEndGame, bool bVictory)
	{
		this.countFlag = iCountFlag;
		this.countOpen = iCountOpen;
		this.countUnknown = iCountUnknown;
		this.endGame = bEndGame;
		this.victory = bVictory;
		needRepaint = null; // new ArrayList<BaseCell>();
	}
	
	public void addToRepaint(BaseCell cell) {
		if (needRepaint == null)
			needRepaint = new List<BaseCell>();
		needRepaint.Add(cell);
	}
}
}