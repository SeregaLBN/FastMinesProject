package ua.ksn.fmg.view.swing.dialogs;

import java.awt.Dimension;

import javax.swing.SwingConstants;

import ua.ksn.fmg.controller.serializable.ChampionsModel;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.view.swing.Main;
import ua.ksn.fmg.view.swing.model.ChampionTblModel;
import ua.ksn.fmg.view.swing.model.ReportTableModel;
import ua.ksn.fmg.view.swing.res.Resources;

/** Диалог отображения чемпионов */
public class ChampionDlg extends ReportDlg {
	private static final long serialVersionUID = 1L;

	private final ChampionsModel champions;

	public ChampionDlg(Main parent, boolean modal, Resources resources, ChampionsModel champions) {
		super(parent, modal, resources);
		this.champions = champions;
	}

	@Override
	protected void UpdateModel(ESkillLevel eSkill) {
		setTitle("Champions - " + getSelectedMosaicType().getDescription(false));
		super.UpdateModel(eSkill);
	}

	@Override
	protected ReportTableModel createTableModel(EMosaic mosaic) {
		return new ChampionTblModel(champions, mosaic);
	}

	@Override
	protected int getTableCellHorizontalAlignment(int row, int column) {
		if (column == 0)
			return SwingConstants.LEFT;
		return super.getTableCellHorizontalAlignment(row, column);
	}

	// тестовый метод для проверки диалогового окна
	public static void main(String[] args) {
		ChampionsModel champions = new ChampionsModel(Main.serialVersionUID, null);
		champions.Load();
		new ChampionDlg(null, true, null, champions).ShowData(ESkillLevel.eBeginner, EMosaic.eMosaicSquare1);
	}

	@Override
	protected Dimension getPreferredScrollPaneSize() {
		return new Dimension(300, 10*getTableRowHeigt() + getTableHeaderHeigt() + 7);
	}

	@Override
	protected boolean isOneLineSkillLevelButtons() { return false; }

	public void ShowData(ESkillLevel eSkill, EMosaic eMosaic) {
		// найдёт позицию первого вхождения пользователя...
		int pos = champions.getPos((parent==null) ? null : parent.getActiveUserId(), eMosaic, eSkill);
		// ...на этой позиции и фокусируюсь
		super.ShowData(eSkill, eMosaic, pos);
	}
}