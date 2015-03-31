namespace ua.ksn.fmg.Event.click {

public struct RightDownReturn {
	public int countFlag, countUnknown;
	public bool needRepaint;

   //public RightDownReturn() {
   //   this.countFlag =
   //   this.countUnknown = 0;
   //   this.needRepaint = false;
   //}
	public RightDownReturn(int countFlag, int countUnknown) {
		this.countFlag = countFlag;
		this.countUnknown = countUnknown;
		this.needRepaint = false;
	}
}
}