using System.Collections.Generic;
using ua.ksn.fmg.model.mosaics.cell;

namespace ua.ksn.fmg.Event.click {

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