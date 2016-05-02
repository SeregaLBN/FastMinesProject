package fmg.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import fmg.common.Color;
import fmg.core.types.EMosaic;
import fmg.data.controller.types.ESkillLevel;
import fmg.swing.Cast;
import fmg.swing.Main;
import fmg.swing.model.view.ReportTableModel;
import fmg.swing.res.img.MosaicsImg;
import fmg.swing.utils.ImgUtils;

abstract class ReportDlg extends JDialog {
   private static final long serialVersionUID = 1L;

   private static final int ImgSize = 40;
   private static final int ImgZoomQuality = 3;

   protected JTabbedPane tabPanel;
   protected JToggleButton[] btns = new JToggleButton[ESkillLevel.values().length-1];
   private Map<EMosaic, JScrollPane> scrollPanes = new HashMap<>(EMosaic.values().length);
   private Map<EMosaic, MosaicsImg.Icon> images = new HashMap<>(EMosaic.values().length);
   protected ButtonGroup radioGroup;
   protected Main parent;

   public ReportDlg(Main parent, boolean modal) {
      super(parent, "report window...", modal);
      this.parent = parent;
      initialize(parent);
   }

   private void initialize(JFrame parent) {
      Object keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { onClose(); }
      });

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) { onClose(); }
      });

      this.setResizable(!false);
      createComponents();

//      Dimension preferredSize = this.getPreferredSize();
//      preferredSize.width = Toolkit.getDefaultToolkit().getScreenSize().width-100;
//      this.setPreferredSize(preferredSize);
//      this.setVisible(true);
      // задаю предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);
   }

   private void onClose() {
      images.forEach((k,v) -> v.close());
      dispose();
//      System.exit(0);
   }

   protected Dimension getPreferredScrollPaneSize() {
      return new Dimension(450, 100);
   }

   /** создаю панели с нужным расположением */
   private void createComponents() {
      // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
      tabPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
      {
         // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
         // использую пустую рамку
         tabPanel.setBorder(BorderFactory.createEmptyBorder(12,12,2,12));

         for (EMosaic eMosaic: EMosaic.values()) {
            JScrollPane scroll = new JScrollPane();
            MosaicsImg.Icon img = new MosaicsImg.Icon(eMosaic, eMosaic.sizeIcoField(false), ImgSize*ImgZoomQuality);
            images.put(eMosaic, img);
            img.addListener(ev -> onImagePropertyChanged(eMosaic, ev));
            img.setBackgroundColor(bkTabBkColor);

            tabPanel.addTab(null, ImgUtils.zoom(img.getImage(), ImgSize, ImgSize), scroll, eMosaic.getDescription(false));
            scroll.setPreferredSize(getPreferredScrollPaneSize());
            scrollPanes.put(eMosaic, scroll);
         }

         // таблички создаю динамически - когда юзер выберет конкретную вкладку. См. getSelectedTable()

         tabPanel.addChangeListener(ev -> onChangeTab(getSelectedMosaicType()));
      }

      // 2. Панель кнопок снизу
      JPanel panelBottom = new JPanel(new GridLayout(0, isOneLineSkillLevelButtons() ? 4:2, 2, 2));
      {
         panelBottom.setBorder(BorderFactory.createEmptyBorder(2,12,12,12));
         radioGroup = new ButtonGroup();
         for (final ESkillLevel eSkill: ESkillLevel.values()) {
            if (eSkill == ESkillLevel.eCustom)
               continue;
            JToggleButton btn = btns[eSkill.ordinal()] = new JToggleButton(eSkill.getDescription());
            panelBottom.add(btn);
            radioGroup.add(btn);
            btn.addActionListener(e -> onClickBtnSkill(eSkill));
         }
      }

      // добавляю расположение в центр окна
      getContentPane().add(tabPanel, BorderLayout.CENTER);
      // ряд кнопок внизу
      getContentPane().add(panelBottom, BorderLayout.SOUTH);
   }

   protected boolean isOneLineSkillLevelButtons() { return false; }

   protected void onClickBtnSkill(ESkillLevel eSkill) {
//      System.out.println("OnClickBtnSkill: " + eSkill);
      UpdateModel(eSkill);
   }

   static final Color bkTabBkColor = Cast.toColor(UIManager.getColor("TabbedPane.light")); // Cast.toColor(getContentPane().getBackground());
   static final Color bkTabBkColorSelected = Cast.toColor(UIManager.getColor("TabbedPane.shadow")); // "TabbedPane.darkShadow"

   protected void onChangeTab(EMosaic eMosaic) {
//      System.out.println("OnChangeTab: " + mosaicType);
      UpdateModel(getSelectedSkillLevel());

      images.forEach((mosaicType, img) -> {
         boolean selected = (mosaicType == eMosaic);
         img.setRotate(selected);
         img.setBackgroundColor(selected ? bkTabBkColorSelected : bkTabBkColor);
      });
   }

   // тестовый метод для проверки диалогового окна
   public static void main(String[] args) {
      new ReportDlg(null, true) {
         private static final long serialVersionUID = 1L; }
      .ShowData(ESkillLevel.eAmateur, EMosaic.eMosaicTriangle1, -1);
   }

   /**
    * Отобразить интресуемые данные
    * @param eSkill
    * @param eMosaic
    * @param pos - позиция строки в табличке, которую выделить
    */
   public void ShowData(ESkillLevel eSkill, EMosaic eMosaic, int pos) {
      if (eSkill == ESkillLevel.eCustom)
         eSkill = ESkillLevel.eAmateur;

      radioGroup.setSelected(btns[eSkill.ordinal()].getModel(), true);
      tabPanel.setSelectedIndex(eMosaic.ordinal());
      onChangeTab(eMosaic);

      JTable table = getSelectedTable();
      if (pos != -1)
         table.getSelectionModel().setSelectionInterval(pos, pos);
      else
         table.getSelectionModel().clearSelection();

      this.setVisible(true);
   }

   protected void UpdateModel(ESkillLevel eSkill) {
      getSelectedTableModel().setSkill(eSkill);
   }

   protected ReportTableModel getSelectedTableModel() {
      return (ReportTableModel)getSelectedTable().getModel();
   }

   /** Вернёт табличку из выбраной вкладки */
   protected JTable getSelectedTable() {
      int pos = tabPanel.getSelectedIndex();
      EMosaic eMosaic = EMosaic.fromOrdinal(pos);
      JScrollPane scroll = scrollPanes.get(eMosaic);

      // Проверяю если ли таблица? Если нет - создаю...
      Component cmpnt = scroll.getViewport().getView();
      JTable table = (cmpnt != null) ? (JTable) cmpnt : createTable(eMosaic, scroll);
      return table;
   }

   protected int getTableCellHorizontalAlignment(int row, int column) { return SwingConstants.CENTER; }
   protected int getTableHeaderCellHorizontalAlignment(int column) { return SwingConstants.CENTER; }
   protected int getTableRowHeigt() { return 32; }
   protected int getTableHeaderHeigt() { return 25; }

   protected EMosaic getSelectedMosaicType() {
      int i = tabPanel.getSelectedIndex();
      if (i == -1)
         throw new RuntimeException("dialog Report::getSelectedMosaicType: tabPanel.getSelectedIndex() return -1 ???");
      return EMosaic.fromOrdinal(i);
   }
   protected ESkillLevel getSelectedSkillLevel() {
      ButtonModel model = radioGroup.getSelection();
      for (int i=0; i < btns.length; i++)
         if (model == btns[i].getModel())
            return ESkillLevel.fromOrdinal(i);
      throw new RuntimeException("dialog Report::getSelectedSkillLevel: radioGroup.getSelection() return unknown model ???");
   }

   private void onImagePropertyChanged(EMosaic mosaicType, PropertyChangeEvent ev) {
      if (ev.getPropertyName().equalsIgnoreCase("Image")) {
         int i = mosaicType.ordinal();
         MosaicsImg.Icon img = images.get(mosaicType);
         if (isVisible())
            tabPanel.setIconAt(i, ImgUtils.zoom(img.getImage(), ImgSize, ImgSize));
         else
            img.getImage();
      }
   }

   protected ReportTableModel createTableModel(EMosaic eMosaic) {
      return new ReportTableModel(eMosaic) {};
   }
   /** создаю табличку сразу добавляя её к JScrollPane */
   protected JTable createTable(EMosaic eMosaic, JScrollPane owner) {
      JTable table = new JTable(createTableModel(eMosaic)) {
         private static final long serialVersionUID = 1L;
         @Override
         public TableCellRenderer getCellRenderer(int row, int column) {
            TableCellRenderer rend = super.getCellRenderer(row, column);
            if (rend instanceof JLabel) {
               // выравнивание текста в ячеке
               ((JLabel)rend).setHorizontalAlignment(getTableCellHorizontalAlignment(row, column));
//               ((JLabel)rend).setVerticalAlignment(SwingConstants.TOP);
            }
            return rend;
         }
      };

      owner.getViewport().setView(table);

      table.setRowHeight(getTableRowHeigt());
      table.getTableHeader().setPreferredSize(new Dimension(owner.getWidth(), getTableHeaderHeigt()));
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//      ((JTextArea)table.getEditorComponent()).setEditable(false);
//      table.getCellEditor().stopCellEditing();// // TODO непашет ??
//      System.out.println(table.getModel()); // javax.swing.table.DefaultTableModel

//      // выравниваю текст заголовков таблицы по центру
//      // TODO Хоть текст и выравнивается, но сами ячейки заголовка таблицы уже выглядят хуже чем
//      //      в оригинальном рендере (особо заметно под Маком).
//      //      Т.е. DefaultTableCellRenderer выглядит паршиво, а как достать орининальный рендер заголовка - хз
//      //System.out.println(table.getColumnModel().getColumn(0).getHeaderRenderer()); // print null... hmmm
//      javax.swing.table.TableColumnModel tableColumnModel = table.getColumnModel();
//      for (int i=0; i<tableColumnModel.getColumnCount(); i++)
//         tableColumnModel.getColumn(i).setHeaderRenderer(defaultTableCellRenderer);
      return table;
   }

   public void CleanResource() {
      setVisible(false);
   }

   DefaultTableCellRenderer defaultTableCellRenderer = new CustomHeaderTableCellRenderer();
   /** для выравнивани текста заголовков таблицы по центру */
   class CustomHeaderTableCellRenderer extends DefaultTableCellRenderer {
      private static final long serialVersionUID = 1L;
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
         Component cmpnt = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (cmpnt instanceof JLabel) {
            JLabel labl = (JLabel)cmpnt;
            labl.setHorizontalAlignment(ReportDlg.this.getTableHeaderCellHorizontalAlignment(column));
         }
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         setForeground(table.getTableHeader().getForeground());
         setBackground(table.getTableHeader().getBackground());
         return cmpnt;
      }
   }
}