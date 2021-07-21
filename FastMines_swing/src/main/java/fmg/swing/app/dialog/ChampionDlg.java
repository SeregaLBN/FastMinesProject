package fmg.swing.app.dialog;

import java.awt.Dimension;

import javax.swing.SwingConstants;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.types.model.ChampionsModel;
import fmg.swing.app.FastMinesSwing;
import fmg.swing.app.model.view.ChampionTblModel;
import fmg.swing.app.model.view.ReportTableModel;

/** Диалог отображения чемпионов */
public class ChampionDlg extends ReportDlg {

    private final ChampionsModel champions;

    public ChampionDlg(FastMinesSwing app, boolean modal, ChampionsModel champions) {
        super(app, modal);
        this.champions = champions;
    }

    @Override
    protected void updateModel(ESkillLevel eSkill) {
        dialog.setTitle("Champions - " + getSelectedMosaicType().getDescription(false));
        super.updateModel(eSkill);
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

    @Override
    protected Dimension getPreferredScrollPaneSize() {
        return new Dimension(300, 10*getTableRowHeigt() + getTableHeaderHeigt() + 7);
    }

    @Override
    protected boolean isOneLineSkillLevelButtons() { return false; }

    public void showData(ESkillLevel eSkill, EMosaic eMosaic) {
        // найдёт позицию первого вхождения пользователя...
        int pos = champions.getPos((app == null) ? null : app.getActiveUserId(), eMosaic, eSkill);
        // ...на этой позиции и фокусируюсь
        super.showData(eSkill, eMosaic, pos);
    }

}
