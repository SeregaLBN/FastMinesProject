package ua.ksn.fmg.view.swing.dialogs;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import ua.ksn.fmg.controller.serializable.PlayersModel;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.controller.types.User;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.view.swing.Main;
import ua.ksn.fmg.view.swing.model.ReportTableModel;
import ua.ksn.fmg.view.swing.model.StaticsticTblModel;
import ua.ksn.fmg.view.swing.res.Resources;

/** Диалог отображения статистики пользователя */
public class StatisticDlg extends ReportDlg {
	private static final long serialVersionUID = 1L;

	private PlayersModel players;

	public StatisticDlg(Main parent, boolean modal, Resources resources, PlayersModel players) {
		super(parent, modal, resources);
		this.players = players;
	}

	@Override
	protected void UpdateModel(ESkillLevel eSkill) {
		setTitle("Statistics - " + getSelectedMosaicType().getDescription(false));
		super.UpdateModel(eSkill);

		// выделяю рядок текущего пользователя
		try {
			User user = (parent == null) ? null : parent.getActiveUser();
			int pos = (user == null) ? -1 : players.indexOf(user);
			if (pos != -1) {
				JTable table = getSelectedTable();
				table.getSelectionModel().setSelectionInterval(pos, pos);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected ReportTableModel createTableModel(EMosaic mosaic) {
		return new StaticsticTblModel(players, mosaic);
	}

	@Override
	protected int getTableCellHorizontalAlignment(int row, int column) {
		if (column == 0)
			return SwingConstants.LEFT;
		return super.getTableCellHorizontalAlignment(row, column);
	}

	// тестовый метод для проверки диалогового окна
	public static void main(String[] args) {
		PlayersModel players = new PlayersModel(Main.serialVersionUID);
		players.Load();
		new StatisticDlg(null, true, null, players).ShowData(ESkillLevel.eAmateur, EMosaic.eMosaicTriangle3);
	}

	@Override
	protected Dimension getPreferredScrollPaneSize() {
		return new Dimension(800, 200);
	}

	@Override
	protected boolean isOneLineSkillLevelButtons() { return true; }

	public void ShowData(ESkillLevel eSkill, EMosaic eMosaic) {
		int pos = players.getPos((parent==null) ? null : parent.getActiveUserId());
		super.ShowData(eSkill, eMosaic, pos);
	}
}