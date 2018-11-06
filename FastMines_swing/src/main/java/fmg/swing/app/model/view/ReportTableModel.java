package fmg.swing.app.model.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

public abstract class ReportTableModel implements TableModel {

    protected ESkillLevel eSkill;
    protected final EMosaic eMosaic;
    private List<TableModelListener> arrTableModelListener = new ArrayList<TableModelListener>();

    public ReportTableModel(EMosaic eMosaic) {
        this.eMosaic = eMosaic;
    }

    public void setSkill(ESkillLevel eSkill) {
        this.eSkill = eSkill;
        fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex == 0)
            if (columnIndex == 0)
                return ESkillLevel.class.getSimpleName();
            else
                return eSkill.getDescription();
        else
            if (columnIndex == 0)
                return EMosaic.class.getSimpleName();
            else
                return eMosaic.getDescription(false);
    }
    @Override
    public int getRowCount() { return 2; }
    @Override
    public String getColumnName(int columnIndex) { return (columnIndex == 0) ? "Key" : "Value"; }
    @Override
    public int getColumnCount() { return 2; }
    @Override
    public Class<?> getColumnClass(int columnIndex) { return String.class; }
    @Override
    public void addTableModelListener(TableModelListener l) {
        arrTableModelListener.add(l);
    }
    @Override
    public void removeTableModelListener(TableModelListener l) {
        arrTableModelListener.remove(l);
    }

    protected void fireTableChanged(TableModelEvent e) {
        for (TableModelListener listener: arrTableModelListener)
            listener.tableChanged(e);
    }

}
