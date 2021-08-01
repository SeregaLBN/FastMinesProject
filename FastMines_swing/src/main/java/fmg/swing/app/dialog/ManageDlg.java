package fmg.swing.app.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.*;
import java.util.UUID;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import fmg.common.ui.UiInvoker;
import fmg.core.types.model.Players;
import fmg.core.types.model.User;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.model.view.ManageTblModel;
import fmg.swing.utils.GuiTools;

/** Диалог управления пользователями */
public class ManageDlg implements AutoCloseable {

    private static final String DEFAULT_CAPTION = "Users manage";

    private final FastMinesApp app;
    private final JDialog dialog;
    private JButton btnOk;
    private JTable table;
    private Players players;
    private JCheckBox doNotAskStartup;
    private ManageTblModel tableModel;

    public ManageDlg(FastMinesApp app, boolean modal, Players players) {
        this.app = app;
        dialog = new JDialog((app == null) ? null : app.getFrame(), DEFAULT_CAPTION, modal);
        this.players = players;
        initialize();
    }

    public JDialog getDialog() {
        return dialog;
    }

    private void initialize() {
        Object keyBind = "CloseDialog";
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        dialog.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { onCancel(); }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { onCancel(); }
        });

        dialog.setResizable(!false);
        createComponents();

        // задаю предпочтительный размер
        dialog.pack();
        dialog.setLocationRelativeTo((app == null) ? null : app.getFrame());
    }

    private void onOk() {
//        Logger.info("OnOk");
        int rowIndex = table.getSelectedRow();
        if ((rowIndex >= 0) &&
            (rowIndex < players.getUserCount()))
        {
            User user = players.getUser(rowIndex);
            UUID activeUserId = user.getId();
//            Logger.info("Active user is: " + user);
            if (app != null)
                app.setActiveUserId(activeUserId);
        }
        onClose();
    }

    private void onCancel() {
//        Logger.info("OnCancel");
        onClose();
    }

    private void onClose() {
        if (dialog.isModal())
            dialog.dispose();
        else
            setVisible(false);
    }

    @Override
    public void close() {
        tableModel.close();
        dialog.dispose();
    }

    private void onNewPlayer() {
//        Logger.info("OnNewPlayer");
        Window winParent = dialog.isVisible() ? dialog : ((app == null) ? null : app.getFrame());
        final LoginDlg loginDialog = new LoginDlg(winParent, true, null, false);
        final Runnable anew = () -> loginDialog.getDialog().setVisible(true);

        loginDialog.setOkActionListener(
            e -> {
                String name = loginDialog.getName();
                if ((name == null) || name.isEmpty())
                    SwingUtilities.invokeLater(anew);
                else
                    try {
                        players.addNewPlayer(name, loginDialog.getPass());
                        int maxPos = players.size()-1; // new user added to end list
                        UiInvoker.DEFERRED.accept(() -> table.getSelectionModel().setSelectionInterval(maxPos, maxPos));
                    } catch (Exception ex) {
                        GuiTools.alert(dialog, ex.getMessage());
                        SwingUtilities.invokeLater(anew);
                    }
            });
        anew.run();
    }

    private void onDeleteRow() {
//        Logger.info("OnDeleteRow");
        int rowIndex = table.getSelectedRow();
        if (rowIndex == -1)
            FastMinesApp.beep();
        else
            players.removePlayer(players.getUser(rowIndex).getId());
    }

    /** создаю панели с нужным расположением */
    private void createComponents() {
        // 1. Центральная панель
        Box boxCenter = Box.createVerticalBox();
        {
            // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
            // использую пустую рамку
            boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            tableModel = new ManageTblModel(players);
            table = new JTable(tableModel) {
                private static final long serialVersionUID = 1L;
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    TableCellRenderer rend = super.getCellRenderer(row, column);
                    if (rend instanceof JLabel) {
                        // выравнивание текста в ячеке
                        ((JLabel)rend).setHorizontalAlignment(JLabel.LEFT);
//                        ((JLabel)rend).setVerticalAlignment(SwingConstants.TOP);
                    }
                    return rend;
                }
            };

//            // выравниваю текст заголовка таблицы по центру
//            // TODO Хоть текст и выравнивается, но сами ячейки заголовка таблицы уже выглядят хуже чем
//            //      в оригинальном рендере (особо заметно под Маком).
//            //      Т.е. DefaultTableCellRenderer выглядит паршиво, а как достать орининальный рендер заголовка - хз
//            table.getColumnModel().getColumn(0).setHeaderRenderer(new javax.swing.table.DefaultTableCellRenderer() {
//                private static final long serialVersionUID = 1L;
//                @Override
//                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
//                {
//                    Component cmpnt = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                    if (cmpnt instanceof JLabel) {
//                        JLabel labl = (JLabel)cmpnt;
//                        labl.setHorizontalAlignment(JLabel.CENTER);
//                    }
//
//                    setBorder(javax.swing.UIManager.getBorder("TableHeader.cellBorder"));
//                    //setForeground(table.getTableHeader().getForeground());
//                    //setBackground(table.getTableHeader().getBackground());
//                    setForeground(javax.swing.UIManager.getColor("TableHeader.foreground"));
//                    if (hasFocus)
//                        setBackground(javax.swing.UIManager.getColor("TableHeader.focusCellBackground"));
//                    else
//                        setBackground(javax.swing.UIManager.getColor("TableHeader.background"));
//                    setFont(javax.swing.UIManager.getFont("TableHeader.font"));
//
//                    return cmpnt;
//                }
//            });

            JScrollPane scroll = new JScrollPane(table);
            boxCenter.add(scroll);
            scroll.setPreferredSize(new Dimension(150,200));

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            ((JTextArea)table.getEditorComponent()).setEditable(false);
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
//                        Logger.info(" sigle click" );
//                        btnOk.setEnabled(table.getSelectedRow() != -1);
                        break;
                    case 2:
//                        Logger.info(" double click" );
                        onOk();
                        break;
                    }
                }
            });
            ListSelectionListener changeTblLineListener = e -> {
               btnOk.setEnabled(table.getSelectedRow() != -1);
//               // If cell selection is enabled, both row and column change events are fired
//               if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
//                   int first = e.getFirstIndex();
//                   int last = e.getLastIndex();
//                   Logger.info("Column selection changed: " + first+".."+last);
//               } else
//               if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed())
//               {
//                   int first = e.getFirstIndex();
//                   int last = e.getLastIndex();
//                   Logger.info("Row selection changed: " + first+".."+last);
//               }
//
//               Logger.info(e.getValueIsAdjusting());
//               if (e.getValueIsAdjusting()) {
//                   // The mouse button has not yet been released
//               }
            };
            table.getSelectionModel().addListSelectionListener(changeTblLineListener);
//            table.getColumnModel().getSelectionModel().addListSelectionListener(changeTblLineListener);

            boxCenter.add(Box.createVerticalStrut(6));

            boxCenter.add(doNotAskStartup = new JCheckBox("Do not ask at startup", true));
        }

        // 2. Панель кнопок слева
        JComponent panelLeft = Box.createVerticalBox();//new JPanel(new GridLayout(0, 1, 0, 12));
        {
            panelLeft.setBorder(BorderFactory.createEmptyBorder(12,0,12,12));

            JButton btnNp = new JButton("New Player");
            btnNp.addActionListener(e -> onNewPlayer());
            panelLeft.add(btnNp);

//            btn = new JButton("Change password");
//            panelLeft.add(btn);
//            btn = new JButton("Avatar");
//            panelLeft.add(btn);
//            btn = new JButton("Rename");
//            panelLeft.add(btn);
//            btn = new JButton("Remove");
//            panelLeft.add(btn);

            panelLeft.add(Box.createVerticalGlue());

            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(e -> onClose());

            btnOk = new JButton("Ok");
            btnOk.addActionListener(e -> onOk());

            panelLeft.add(btnOk);
            panelLeft.add(Box.createVerticalStrut(5));
            panelLeft.add(btnCancel);

            GuiTools.makeSameWidth(new Component[] {btnNp, btnCancel, btnOk});
        }

        // добавляю расположение в центр окна
        dialog.getContentPane().add(boxCenter, BorderLayout.CENTER);
        // ряд кнопок слева
        dialog.getContentPane().add(panelLeft, BorderLayout.EAST);
    }

    public void setVisible(boolean b) {
        //Logger.info("> Manage::setVisible: " + b);
        if (b) {
            dialog.setTitle(DEFAULT_CAPTION);

            UUID activeUserId = (app==null) ? null : app.getActiveUserId();
            if ((activeUserId!=null) && players.isExist(activeUserId)) {
                int pos = players.getPos(activeUserId);
                table.getSelectionModel().setSelectionInterval(pos, pos);
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }

            if (players.size() == 0)
                SwingUtilities.invokeLater(this::onNewPlayer);
        }
        dialog.setVisible(b);
    }

    public boolean isDoNotAskStartupChecked() {
        return doNotAskStartup.isSelected();
    }

    public void setDoNotAskStartupChecked(boolean checked) {
        doNotAskStartup.setSelected(checked);
    }

}
