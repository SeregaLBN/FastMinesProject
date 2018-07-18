package fmg.swing.app.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.UUID;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import fmg.data.controller.serializable.PlayersModel;
import fmg.data.controller.types.User;
import fmg.swing.app.Main;
import fmg.swing.app.model.view.ManageTblModel;
import fmg.swing.utils.GuiTools;

/** Диалог управления пользователями */
public class ManageDlg extends JDialog {
   private static final long serialVersionUID = 1L;
   private static String DEFAULT_CAPTION = "Users manage";

   private JButton btnOk;
   private JTable table;
   private Main parent;
   private PlayersModel players;
   private JCheckBox doNotAskStartup;

   public ManageDlg(JFrame parent, boolean modal, PlayersModel players) {
      super(parent, DEFAULT_CAPTION, modal);
      if (parent instanceof Main)
         this.parent = (Main) parent;
      this.players = players;
      initialize(parent);
   }

   private void initialize(JFrame parent) {
      Object keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { ManageDlg.this.onCancel(); }
      });

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) { ManageDlg.this.onCancel(); }
      });

      this.setResizable(!false);
      createComponents();

      // задаю предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);
   }

   private void onOk() {
//      System.out.println("OnOk");
      int rowIndex = table.getSelectedRow();
      if (rowIndex == -1) {
//         if (parent != null)
//            System.out.println("User not changet. Active user id is " + parent.getActiveUserId());
//         else
//            System.out.println("User not selected");
      } else {
         User user = players.getUser(rowIndex);
         UUID activeUserId = user.getGuid();
//         System.out.println("Active user is: "+user);
         if (parent != null)
            parent.setActiveUserId(activeUserId);
      }
      onClose();
   }
   private void onCancel() {
//      System.out.println("OnCancel");
      onClose();
   }
   private void onClose() {
      // при выходе из диалогового окна - освобождаю ресурсы
      dispose();
   }

   private void onNewPlayer() {
//      System.out.println("OnNewPlayer");
      final LoginDlg loginDialog = new LoginDlg(this.isVisible() ? this : parent, true, null, false);
      final Runnable anew = () -> loginDialog.setVisible(true);

      loginDialog.setOkActionListener(
         e -> {
            String name = loginDialog.getName();
            if ((name == null) || name.isEmpty())
               SwingUtilities.invokeLater(anew);
            else
               try {
                  ManageDlg.this.players.addNewPlayer(name, loginDialog.getPass());
                  int maxPos = ManageDlg.this.players.size()-1; // new user added to end list
                  ManageDlg.this.table.getSelectionModel().setSelectionInterval(maxPos, maxPos);
               } catch (Exception ex) {
                  GuiTools.alert(ManageDlg.this, ex.getMessage());
                  SwingUtilities.invokeLater(anew);
               }
         });
      anew.run();
   }

   private void onDeleteRow() {
//      System.out.println("OnDeleteRow");
      int rowIndex = table.getSelectedRow();
      if (rowIndex == -1)
         Main.Beep();
      else
         players.removePlayer(players.getUser(rowIndex).getGuid());
   }

   /** создаю панели с нужным расположением */
   private void createComponents() {
      // 1. Центральная панель
      Box boxCenter = Box.createVerticalBox();
      {
         // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
         // использую пустую рамку
         boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

         table = new JTable(new ManageTblModel(players)) {
            private static final long serialVersionUID = 1L;
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
               TableCellRenderer rend = super.getCellRenderer(row, column);
               if (rend instanceof JLabel) {
                  // выравнивание текста в ячеке
                  ((JLabel)rend).setHorizontalAlignment(JLabel.LEFT);
//                  ((JLabel)rend).setVerticalAlignment(SwingConstants.TOP);
               }
               return rend;
            }
         };

//         // выравниваю текст заголовка таблицы по центру
//         // TODO Хоть текст и выравнивается, но сами ячейки заголовка таблицы уже выглядят хуже чем
//         //      в оригинальном рендере (особо заметно под Маком).
//         //      Т.е. DefaultTableCellRenderer выглядит паршиво, а как достать орининальный рендер заголовка - хз
//         table.getColumnModel().getColumn(0).setHeaderRenderer(new javax.swing.table.DefaultTableCellRenderer() {
//            private static final long serialVersionUID = 1L;
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
//            {
//               Component cmpnt = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//               if (cmpnt instanceof JLabel) {
//                  JLabel labl = (JLabel)cmpnt;
//                  labl.setHorizontalAlignment(JLabel.CENTER);
//               }
//
//               setBorder(javax.swing.UIManager.getBorder("TableHeader.cellBorder"));
//               //setForeground(table.getTableHeader().getForeground());
//               //setBackground(table.getTableHeader().getBackground());
//               setForeground(javax.swing.UIManager.getColor("TableHeader.foreground"));
//               if (hasFocus)
//                  setBackground(javax.swing.UIManager.getColor("TableHeader.focusCellBackground"));
//               else
//                  setBackground(javax.swing.UIManager.getColor("TableHeader.background"));
//               setFont(javax.swing.UIManager.getFont("TableHeader.font"));
//
//               return cmpnt;
//            }
//         });

         JScrollPane scroll = new JScrollPane(table);
         boxCenter.add(scroll);
         scroll.setPreferredSize(new Dimension(150,200));

         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//         ((JTextArea)table.getEditorComponent()).setEditable(false);
         table.setRowHeight(32);
         table.getTableHeader().setPreferredSize(new Dimension(scroll.getWidth(), 25));
         InputMap inputMap = table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         ActionMap actionMap = table.getActionMap();

         Object mapKey = "Delete row";
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), mapKey);
         actionMap.put(mapKey, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
               onDeleteRow();
            }
         });
         table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               switch (e.getClickCount()) {
               case 1:
//                  System.out.println(" sigle click" );
//                  btnOk.setEnabled(table.getSelectedRow() != -1);
                  break;
               case 2:
//                  System.out.println(" double click" );
                  ManageDlg.this.onOk();
                  break;
               }
            }
         });
         ListSelectionListener changeTblLineListener = e -> {
            btnOk.setEnabled(table.getSelectedRow() != -1);
//               // If cell selection is enabled, both row and column change events are fired
//               if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
//                  int first = e.getFirstIndex();
//                  int last = e.getLastIndex();
//                  System.out.println("Column selection changed: " + first+".."+last);
//               } else
//               if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed())
//               {
//                  int first = e.getFirstIndex();
//                  int last = e.getLastIndex();
//                  System.out.println("Row selection changed: " + first+".."+last);
//               }
//
//               System.out.println(e.getValueIsAdjusting());
//               if (e.getValueIsAdjusting()) {
//                  // The mouse button has not yet been released
//               }
         };
         table.getSelectionModel().addListSelectionListener(changeTblLineListener);
//         table.getColumnModel().getSelectionModel().addListSelectionListener(changeTblLineListener);

         boxCenter.add(Box.createVerticalStrut(6));

         boxCenter.add(doNotAskStartup = new JCheckBox("Do not ask at startup", true));
      }

      // 2. Панель кнопок слева
      JComponent panelLeft = Box.createVerticalBox();//new JPanel(new GridLayout(0, 1, 0, 12));
      {
         panelLeft.setBorder(BorderFactory.createEmptyBorder(12,0,12,12));

         JButton btnNp = new JButton("New Player");
         btnNp.addActionListener(e -> ManageDlg.this.onNewPlayer());
         panelLeft.add(btnNp);

//         btn = new JButton("Change password");
//         panelLeft.add(btn);
//         btn = new JButton("Avatar");
//         panelLeft.add(btn);
//         btn = new JButton("Rename");
//         panelLeft.add(btn);
//         btn = new JButton("Remove");
//         panelLeft.add(btn);

         panelLeft.add(Box.createVerticalGlue());

         JButton btnCancel = new JButton("Cancel");
         btnCancel.addActionListener(e -> ManageDlg.this.onClose());

         btnOk = new JButton("Ok");
         btnOk.addActionListener(e -> ManageDlg.this.onOk());

         panelLeft.add(btnOk);
         panelLeft.add(Box.createVerticalStrut(5));
         panelLeft.add(btnCancel);

         GuiTools.makeSameWidth(new Component[] {btnNp, btnCancel, btnOk});
      }

      // добавляю расположение в центр окна
      getContentPane().add(boxCenter, BorderLayout.CENTER);
      // ряд кнопок слева
      getContentPane().add(panelLeft, BorderLayout.EAST);
   }

   @Override
   public void setVisible(boolean b) {
      //System.out.println("> Manage::setVisible: " + b);
      if (b) {
         this.setTitle(DEFAULT_CAPTION);

         UUID activeUserId = (parent==null) ? null : parent.getActiveUserId();
         if ((activeUserId!=null) && players.isExist(activeUserId)) {
            int pos = players.getPos(activeUserId);
            table.getSelectionModel().setSelectionInterval(pos, pos);
            btnOk.setEnabled(true);
         } else {
            btnOk.setEnabled(false);
         }

         if (players.size() == 0)
            SwingUtilities.invokeLater(() -> ManageDlg.this.onNewPlayer());
      }
      super.setVisible(b);
   }

   public boolean isDoNotAskStartupChecked() {
      return doNotAskStartup.isSelected();
   }
   public void setDoNotAskStartupChecked(boolean checked) {
      doNotAskStartup.setSelected(checked);
   }

   //////////////////////////////////////////////////
   // TEST
   public static void main(String[] args) {
      try {
         PlayersModel players = new PlayersModel();
         players.Load();
         ManageDlg manage = new ManageDlg(null, true, players);
         manage.setVisible(true);
         players.Save();
      } catch (Exception ex) {
         System.err.println(ex);
      }
   }

}