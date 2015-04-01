using System.Collections.Generic;
using fmg.core.model.mosaics.cell;

namespace fmg.core.Event.click {

public class ClickReportContext {
   /// <summary>��������� ����� (�������  ) ��������           ��� ��������� �����</summary>
   public ISet<BaseCell> setOpenNil;
   /// <summary>��������� ����� (���������) ��������           ��� ��������� �����</summary>
	public ISet<BaseCell> setOpen;
   /// <summary>��������� ����� � ��������  ������/����������� ��� ��������� �����</summary>
	public ISet<BaseCell> setFlag;

    public ClickReportContext() {
    	setOpenNil = new HashSet<BaseCell>();
    	setOpen = new HashSet<BaseCell>();
    	setFlag = new HashSet<BaseCell>();
    }
}
}