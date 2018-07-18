package fmg.swing.model.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fmg.data.controller.event.PlayerModelEvent;
import fmg.data.controller.event.PlayerModelListener;
import fmg.data.controller.serializable.PlayersModel;

public class ManageTblModel implements TableModel {
   
   private final PlayersModel players;
   private List<TableModelListener> arrTableModelListener = new ArrayList<TableModelListener>();

   public ManageTblModel(PlayersModel players) {
      this.players = players;
      this.players.addPlayerListener(new PlayerModelListener() {
         @Override
         public void playerChanged(PlayerModelEvent e) {
            switch (e.getType()) {
            case PlayerModelEvent.INSERT    : fireTableChanged(new TableModelEvent(ManageTblModel.this, e.getPos(), e.getPos(), 0, TableModelEvent.INSERT)); break;
            case PlayerModelEvent.DELETE    : fireTableChanged(new TableModelEvent(ManageTblModel.this, e.getPos(), e.getPos(), 0, TableModelEvent.DELETE)); break;
            case PlayerModelEvent.UPDATE    : fireTableChanged(new TableModelEvent(ManageTblModel.this, e.getPos(), e.getPos(), 0, TableModelEvent.UPDATE)); break;
            case PlayerModelEvent.INSERT_ALL: fireTableChanged(new TableModelEvent(ManageTblModel.this,          0, e.getPos(), 0, TableModelEvent.INSERT)); break;
            case PlayerModelEvent.DELETE_ALL: fireTableChanged(new TableModelEvent(ManageTblModel.this,          0, e.getPos(), 0, TableModelEvent.DELETE)); break;
            case PlayerModelEvent.UPDATE_ALL: fireTableChanged(new TableModelEvent(ManageTblModel.this,          0, e.getPos(), 0, TableModelEvent.UPDATE)); break;
            case PlayerModelEvent.CHANGE_STATISTICS:
               break;
            }
         }
      });
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
}
