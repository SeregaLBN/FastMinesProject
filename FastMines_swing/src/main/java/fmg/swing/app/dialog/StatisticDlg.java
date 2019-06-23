package fmg.swing.app.dialog;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.types.viewmodel.User;
import fmg.core.types.viewmodel.serializable.PlayersModel;
import fmg.swing.app.MainApp;
import fmg.swing.app.model.view.ReportTableModel;
import fmg.swing.app.model.view.StaticsticTblModel;

/** Диалог отображения статистики пользователя */
public class StatisticDlg extends ReportDlg {

    private static final long serialVersionUID = 1L;

    private PlayersModel players;

    public StatisticDlg(MainApp parent, boolean modal, PlayersModel players) {
        super(parent, modal);
        this.players = players;
    }

    @Override
    protected void updateModel(ESkillLevel eSkill) {
        setTitle("Statistics - " + getSelectedMosaicType().getDescription(false));
        super.updateModel(eSkill);

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

    @Override
    protected Dimension getPreferredScrollPaneSize() {
        return new Dimension(800, 200);
    }

    @Override
    protected boolean isOneLineSkillLevelButtons() { return true; }

    public void showData(ESkillLevel eSkill, EMosaic eMosaic) {
        int pos = players.getPos((parent==null) ? null : parent.getActiveUserId());
        super.showData(eSkill, eMosaic, pos);
    }

}
