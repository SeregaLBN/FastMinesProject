package fmg.core.types.click;

public class RightDownReturn {
	public int countFlag, countUnknown;
	public boolean needRepaint;

	public RightDownReturn() {
		this.countFlag =
		this.countUnknown = 0;
		this.needRepaint = false;
	}
	public RightDownReturn(int countFlag, int countUnknown) {
		this.countFlag = countFlag;
		this.countUnknown = countUnknown;
		this.needRepaint = false;
	}
//
//	public int getCountFlag() {
//		return countFlag;
//	}
//	public void setCountFlag(int countFlag) {
//		this.countFlag = countFlag;
//	}
//	public int getCountUnknown() {
//		return countUnknown;
//	}
//	public void setCountUnknown(int countUnknown) {
//		this.countUnknown = countUnknown;
//	}
}
