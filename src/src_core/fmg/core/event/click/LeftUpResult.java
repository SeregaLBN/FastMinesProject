package fmg.core.event.click;

import java.util.ArrayList;
import java.util.List;

import fmg.core.model.mosaics.cell.BaseCell;

public class LeftUpResult {
	public int countFlag, countOpen, countUnknown;
	public boolean endGame, victory;
	public List<BaseCell> needRepaint;

	public LeftUpResult()
	{
		this.countFlag =
		this.countOpen =
		this.countUnknown = 0;
		this.endGame =
		this.victory = false;
		needRepaint = null; // new ArrayList<BaseCell>();
	}
	public LeftUpResult(int iCountFlag, int iCountOpen, int iCountUnknown, boolean bEndGame, boolean bVictory)
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
			needRepaint = new ArrayList<BaseCell>();
		needRepaint.add(cell);
	}

//	public int getCountFlag() {
//		return countFlag;
//	}
//	public void setCountFlag(int countFlag) {
//		this.countFlag = countFlag;
//	}
//	public int getCountOpen() {
//		return countOpen;
//	}
//	public void setCountOpen(int countOpen) {
//		this.countOpen = countOpen;
//	}
//	public int getCountUnknown() {
//		return countUnknown;
//	}
//	public void setCountUnknown(int countUnknown) {
//		this.countUnknown = countUnknown;
//	}
//	public boolean isEndGame() {
//		return endGame;
//	}
//	public void setEndGame(boolean endGame) {
//		this.endGame = endGame;
//	}
//	public boolean isVictory() {
//		return victory;
//	}
//	public void setVictory(boolean victory) {
//		this.victory = victory;
//	}
}
