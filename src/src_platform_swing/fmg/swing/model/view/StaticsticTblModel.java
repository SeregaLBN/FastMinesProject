package fmg.swing.model.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.event.TableModelEvent;

import fmg.core.types.EMosaic;
import fmg.data.controller.event.PlayerModelEvent;
import fmg.data.controller.event.PlayerModelListener;
import fmg.data.controller.serializable.PlayersModel;
import fmg.data.controller.serializable.StatisticCounts;
import fmg.data.controller.types.User;

public class StaticsticTblModel extends ReportTableModel {

   private final PlayersModel players;

   public StaticsticTblModel(PlayersModel players, EMosaic eMosaic) {
      super(eMosaic);
      this.players = players;

      players.addPlayerListener(new PlayerModelListener() {
         @Override
         public void playerChanged(PlayerModelEvent e) {
               int pos = e.getPos();
               switch (e.getType()) {
               case PlayerModelEvent.CHANGE_STATISTICS:
                  if ((e.getMosaic() == StaticsticTblModel.this.eMosaic) &&
                     (e.getSkill() == StaticsticTblModel.this.eSkill))
                  {
                     StaticsticTblModel.this.fireTableChanged(
                        new TableModelEvent(
                           StaticsticTblModel.this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
                  }
                  break;
               default:
                  StaticsticTblModel.this.fireTableChanged(
                     new TableModelEvent(StaticsticTblModel.this));
                  break;
               }
         }
      });
   }

   @Override
   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      User user = players.getUser(rowIndex);
      StatisticCounts sc = players.getInfo(user.getGuid(), eMosaic, eSkill);
      NumberFormat formatter = new DecimalFormat("0.000");
      switch (columnIndex) {
      case 0: return user.getName();
      case 1: return Long.toString(sc.gameNumber);
      case 2: return sc.gameWin + " / " + formatter.format(sc.gameWin*100./Math.max(1, sc.gameNumber)) + '%';
      case 3: return formatter.format(sc.openField*100./Math.max(1, sc.gameNumber) / (eSkill.getDefaultSize().m*eSkill.getDefaultSize().n)) + '%';
      case 4: return (sc.playTime == 0) ? "???" : (formatter.format((double)sc.playTime / Math.max(1, sc.gameWin)) + " sec.");
      case 5: return (sc.clickCount ==0) ? "???" : formatter.format((double)sc.clickCount / Math.max(1, sc.gameWin));
      }
      return null;
   }
   @Override
   public int getRowCount() { return players.size(); }
   @Override
   public String getColumnName(int columnIndex) {
      switch (columnIndex) {
      case 0: return "Player name";
      case 1: return "Number of games";
      case 2: return "Games win";
      case 3:
//         Size ds = eSkill.DefaultSize();
//         int nm = eSkill.GetNumberMines(eMosaic);
//         return "Open (max "+((ds.x*ds.y-nm)*100./(ds.x*ds.y))+"%)";
         return "Open";
      case 4: return "~ Game time";//"Averaged game time";
      case 5: return "~ Clicks"; // "Averaged number of clicks";
      }
      return null;
   }
   @Override
   public int getColumnCount() { return 6; }
   @Override
   public Class<?> getColumnClass(int columnIndex) { return String.class; }
}
