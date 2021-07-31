package fmg.swing.app.dialog;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.types.model.Players;
import fmg.core.types.model.User;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.model.view.ReportTableModel;
import fmg.swing.app.model.view.StaticsticTblModel;

/** Диалог отображения статистики пользователя */
public class StatisticDlg extends ReportDlg {

    private Players players;
    private StaticsticTblModel tableModel;

    public StatisticDlg(FastMinesApp app, boolean modal, Players players) {
        super(app, modal);
        this.players = players;
    }

    @Override
    protected void updateModel(ESkillLevel eSkill) {
        dialog.setTitle("Statistics - " + getSelectedMosaicType().getDescription(false));
        super.updateModel(eSkill);

        // выделяю рядок текущего пользователя
        try {
            User user = (app == null) ? null : app.getActiveUser();
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
        tableModel = new StaticsticTblModel(players, mosaic);
        return tableModel;
    }

    @Override
    protected int getTableCellHorizontalAlignment(int row, int column) {
        if (column == 0)
            return SwingConstants.LEFT;
        return super.getTableCellHorizontalAlignment(row, column);
    }

    @Override
    protected Dimension getPreferredScrollPaneSize() {
        return new Dimension(800, 200);
    }

    @Override
    protected boolean isOneLineSkillLevelButtons() { return true; }

    public void showData(ESkillLevel eSkill, EMosaic eMosaic) {
        int pos = players.getPos((app == null) ? null : app.getActiveUserId());
        super.showData(eSkill, eMosaic, pos);
    }

    @Override
    public void close() {
        tableModel.close();
        super.close();
    }

}
