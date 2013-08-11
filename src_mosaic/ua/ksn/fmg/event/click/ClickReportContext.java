package ua.ksn.fmg.event.click;

import java.util.HashSet;
import java.util.Set;

import ua.ksn.fmg.model.mosaics.cell.BaseCell;

public class ClickReportContext {
	/** ��������� ����� (�������  ) ��������           ��� ��������� ����� */
	public Set<BaseCell> setOpenNil;
	/** ��������� ����� (���������) ��������           ��� ��������� ����� */
	public Set<BaseCell> setOpen;
	/** ��������� ����� � ��������  ������/����������� ��� ��������� ����� */
	public Set<BaseCell> setFlag;

    public ClickReportContext() {
    	setOpenNil = new HashSet<BaseCell>();
    	setOpen = new HashSet<BaseCell>();
    	setFlag = new HashSet<BaseCell>();
    }
}
