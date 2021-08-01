package fmg.swing.app.model.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.event.TableModelEvent;

import fmg.core.types.EMosaic;
import fmg.core.types.model.Players;
import fmg.core.types.model.Players.UserStatisticChanged;
import fmg.core.types.model.Statistics;
import fmg.core.types.model.User;

public class StaticsticTblModel extends ReportTableModel implements AutoCloseable {

    private final Players players;
    private final PropertyChangeListener onPlayersPropertyChangedListener = this::onPlayersPropertyChanged;

    public StaticsticTblModel(Players players, EMosaic eMosaic) {
        super(eMosaic);
        this.players = players;
        players.addListener(onPlayersPropertyChangedListener);
    }

    @Override
    public void close() {
        players.removeListener(onPlayersPropertyChangedListener);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = players.getUser(rowIndex);
        Statistics sc = players.getInfo(user.getId(), eMosaic, eSkill);
        NumberFormat formatter = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        switch (columnIndex) {
        case 0: return user.getName();
        case 1: return Long.toString(sc.gameNumber);
        case 2: return sc.gameWin + " / " + formatter.format(sc.gameWin*100./Math.max(1, sc.gameNumber)) + '%';
        case 3: return formatter.format(sc.openField*100./Math.max(1, sc.gameNumber) / (eSkill.getDefaultSize().m*eSkill.getDefaultSize().n)) + '%';
        case 4: return (sc.playTime  == 0) ? "???" : (formatter.format((sc.playTime/1000.0) / Math.max(1, sc.gameWin)) + " sec.");
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
//            Size ds = eSkill.DefaultSize();
//            int nm = eSkill.GetNumberMines(eMosaic);
//            return "Open (max "+((ds.x*ds.y-nm)*100./(ds.x*ds.y))+"%)";
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

    private void onPlayersPropertyChanged(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(Players.USER_STATISTIC_CHANGED)) {
            UserStatisticChanged usc = (UserStatisticChanged)ev.getNewValue();
            int pos = players.getPos(usc.userId);
            fireTableChanged(new TableModelEvent(this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
        } else {
            fireTableChanged(new TableModelEvent(this));
        }
    }

}
