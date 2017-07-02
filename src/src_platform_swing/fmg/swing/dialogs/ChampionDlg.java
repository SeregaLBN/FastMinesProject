package fmg.swing.dialogs;

import java.awt.Dimension;

import javax.swing.SwingConstants;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.data.controller.serializable.ChampionsModel;
import fmg.swing.Main;
import fmg.swing.model.view.ChampionTblModel;
import fmg.swing.model.view.ReportTableModel;

/** Диалог отображения чемпионов */
public class ChampionDlg extends ReportDlg {
   private static final long serialVersionUID = 1L;

   private final ChampionsModel champions;

   public ChampionDlg(Main parent, boolean modal, ChampionsModel champions) {
      super(parent, modal);
      this.champions = champions;
   }

   @Override
   protected void updateModel(ESkillLevel eSkill) {
      setTitle("Champions - " + getSelectedMosaicType().getDescription(false));
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

   // тестовый метод для проверки диалогового окна
   public static void main(String[] args) {
      ChampionsModel champions = new ChampionsModel(null);
      champions.Load();
      try (ChampionDlg dlg = new ChampionDlg(null, true, champions)) {
         dlg.showData(ESkillLevel.eBeginner, EMosaic.eMosaicSquare1);
      }
   }

   @Override
   protected Dimension getPreferredScrollPaneSize() {
      return new Dimension(300, 10*getTableRowHeigt() + getTableHeaderHeigt() + 7);
   }

   @Override
   protected boolean isOneLineSkillLevelButtons() { return false; }

   public void showData(ESkillLevel eSkill, EMosaic eMosaic) {
      // найдёт позицию первого вхождения пользователя...
      int pos = champions.getPos((parent==null) ? null : parent.getActiveUserId(), eMosaic, eSkill);
      // ...на этой позиции и фокусируюсь
      super.showData(eSkill, eMosaic, pos);
   }
}