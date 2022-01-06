package fmg.swing.app.dialog;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;

import fmg.core.app.model.Champions;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.model.view.ChampionTblModel;
import fmg.swing.app.model.view.ReportTableModel;

/** Диалог отображения чемпионов */
public class ChampionDlg extends ReportDlg {

    private final Champions champions;
    private List<ChampionTblModel> tableModels = new ArrayList<>();

    public ChampionDlg(FastMinesApp app, boolean modal, Champions champions) {
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
        ChampionTblModel tableModel = new ChampionTblModel(champions, mosaic);
        tableModels.add(tableModel);
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

    @Override
    public void close() {
        tableModels.forEach(ChampionTblModel::close);
        super.close();
    }

}
