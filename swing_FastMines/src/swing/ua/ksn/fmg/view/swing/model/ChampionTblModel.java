package ua.ksn.fmg.view.swing.model;

import javax.swing.event.TableModelEvent;

import ua.ksn.fmg.controller.event.ChampionModelEvent;
import ua.ksn.fmg.controller.event.ChampionModelListener;
import ua.ksn.fmg.controller.serializable.ChampionsModel;
import ua.ksn.fmg.model.mosaics.EMosaic;

public class ChampionTblModel extends ReportTableModel {

	private final ChampionsModel champions;

	public ChampionTblModel(ChampionsModel champions, EMosaic eMosaic) {
		super(eMosaic);
		this.champions = champions;

		champions.addChampionListener(new ChampionModelListener() {
			@Override
			public void championChanged(ChampionModelEvent e) {
				if ((e.getMosaic() == ChampionTblModel.this.eMosaic) &&
					(e.getSkill() == ChampionTblModel.this.eSkill))
				{
					int size = ChampionTblModel.this.champions.getUsersCount(ChampionTblModel.this.eMosaic, ChampionTblModel.this.eSkill);
					int pos = e.getPos();
					switch (e.getType()) {
					case ChampionModelEvent.DELETE:
						if (pos == ChampionModelEvent.POS_ALL)
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, 0, size-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
						else
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
						break;
					case ChampionModelEvent.INSERT:
						if (pos == ChampionModelEvent.POS_ALL)
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, 0, size-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
						else
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
						break;
					case ChampionModelEvent.UPDATE:
						if (pos == ChampionModelEvent.POS_ALL)
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, 0, size-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
						else
							fireTableChanged(new TableModelEvent(ChampionTblModel.this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
						break;
					}
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
		if (columnIndex == 0)
			return champions.getUserName(rowIndex, eMosaic, eSkill);
		else
			return champions.getUserPlayTime(rowIndex, eMosaic, eSkill);
	}
	@Override
	public int getRowCount() { return champions.getUsersCount(eMosaic, eSkill); }
	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0: return "Player name";
		case 1: return "Game time";
		}
		return null;
	}
	@Override
	public int getColumnCount() { return 2; }
	@Override
	public Class<?> getColumnClass(int columnIndex) { return (columnIndex==0) ? String.class : Integer.class; }
}
