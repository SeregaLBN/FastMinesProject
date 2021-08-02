package fmg.swing.app.model.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fmg.core.app.model.Players;
import fmg.core.app.model.User;

public class ManageTblModel implements TableModel, AutoCloseable {

    private final Players players;
    private final PropertyChangeListener onPlayersPropertyChangedListener = this::onPlayersPropertyChanged;
    private final List<TableModelListener> arrTableModelListener = new ArrayList<>();

    public ManageTblModel(Players players) {
        this.players = Objects.requireNonNull(players);
        players.addListener(onPlayersPropertyChangedListener);
    }

    @Override
    public void close() {
        players.removeListener(onPlayersPropertyChangedListener);
    }

    //=================================== Table model =========================================//
    @Override
    public int getRowCount() { return players.size(); }

    @Override
    public int getColumnCount() { return 1; }

    @Override
    public String getColumnName(int columnIndex) { return "Players"; }

    @Override
    public Class<?> getColumnClass(int columnIndex) { return String.class; }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return !true; }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex != 0)
            throw new IllegalArgumentException("Invalid column index " + columnIndex);
        if ((rowIndex < 0) || (rowIndex>=getRowCount()))
            throw new IllegalArgumentException("Invalid row index " + rowIndex);
            //return null;
        return players.getUser(rowIndex).getName();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0)
            throw new IllegalArgumentException("Invalid column index " + columnIndex);
        if ((rowIndex < 0) || (rowIndex>=getRowCount()))
            throw new IllegalArgumentException("Invalid row index " + rowIndex);
        if (aValue == null)
            throw new IllegalArgumentException("Bad value - is not null");
        if (!(aValue instanceof String))
            throw new IllegalArgumentException("Bad value type. Need 'String', not " + aValue.getClass());

        String name = (String)aValue;

        // имя должно быть уникальным
        for (int i=0; i<players.size(); i++) {
            if (i == rowIndex)
                continue;
            if (players.getUser(i).getName().equalsIgnoreCase(name))
                throw new IllegalArgumentException("Please enter a unique name");
        }

        players.setUserName(rowIndex, name);
        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        arrTableModelListener.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        arrTableModelListener.remove(l);
    }

    private void fireTableChanged(TableModelEvent e) {
        for (TableModelListener listener: arrTableModelListener)
            listener.tableChanged(e);
    }

    private void onPlayersPropertyChanged(PropertyChangeEvent ev) {
        User user;
        int pos;
        switch (ev.getPropertyName()) {
        case Players.USER_ADDED:
            user = (User)ev.getNewValue();
            pos = players.getPos(user.getId());
            fireTableChanged(new TableModelEvent(this, pos, pos, 0, TableModelEvent.INSERT));
            break;
        case Players.USER_DELETED:
            pos = (Integer)ev.getOldValue();
            fireTableChanged(new TableModelEvent(this, pos, pos, 0, TableModelEvent.DELETE));
            break;
        case Players.USER_NAME_UPDATED:
            user = (User)ev.getNewValue();
            pos = players.getPos(user.getId());
            fireTableChanged(new TableModelEvent(this, pos, pos, 0, TableModelEvent.UPDATE));
            break;
        default:
            // none
        }
    }

}
