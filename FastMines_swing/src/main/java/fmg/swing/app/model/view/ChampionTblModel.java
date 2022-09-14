package fmg.swing.app.model.view;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;

import javax.swing.event.TableModelEvent;

import fmg.common.Logger;
import fmg.core.app.model.Champions;
import fmg.core.app.model.Champions.ChampionAdded;
import fmg.core.app.model.Champions.Record;
import fmg.core.types.EMosaic;

public class ChampionTblModel extends ReportTableModel {

    private final Champions champions;

    public ChampionTblModel(Champions champions, EMosaic eMosaic) {
        super(eMosaic);
        this.champions = champions;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // none
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Record rec = champions.getUserRecord(rowIndex, eMosaic, eSkill);
        switch (columnIndex) {
        case 0:
            return rec.userName;

        case 1:
            return rec.playTime / 1000.0;

        case 2:
            return rec.clicks;

        case 3:
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rec.date);

        default:
            Logger.error("Illegal value = " + columnIndex);
            return null;
        }
    }

    @Override
    public int getRowCount() { return champions.getUsersCount(eMosaic, eSkill); }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0: return "Player name";
        case 1: return "Game time";
        case 2: return "Clicks";
        case 3: return "End game";
        default:
            return null;
        }
    }

    @Override
    public int getColumnCount() { return 4; }

    @Override
    public Class<?> getColumnClass(int columnIndex) { return (columnIndex==0) ? String.class : Integer.class; }

    public void onChampionsPropertyChanged(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(Champions.CHAMPION_ADDED)) {
            ChampionAdded val = (ChampionAdded)ev.getNewValue();
            if ((eMosaic == val.mosaic) && (eSkill  == val.skill))
                fireTableChanged(new TableModelEvent(ChampionTblModel.this, val.pos, val.pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
        } else
        if (ev.getPropertyName().equals(Champions.CHAMPION_RENAMED)) {
            int size = champions.getUsersCount(eMosaic, eSkill);
            fireTableChanged(new TableModelEvent(ChampionTblModel.this, 0, size-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
        }
    }

}
