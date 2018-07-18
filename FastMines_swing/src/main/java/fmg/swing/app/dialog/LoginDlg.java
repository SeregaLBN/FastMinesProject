package fmg.swing.app.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import fmg.swing.utils.BoxLayoutUtils;
import fmg.swing.utils.GuiTools;

public class LoginDlg extends JDialog {
   private static final long serialVersionUID = 1L;

   private JTextField nameField, passwrdField;
   private ActionListener onOkActionListener, onCancelActionListener;

   public LoginDlg(
         Window parent,
         boolean modal,
         String username,
         boolean usePassword)
   {
      super(parent, (username==null) ? "New user" : "Confirm password", modal ? Dialog.DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
      initialize(parent, username, usePassword);
   }

   private void initialize(Window parent, String username, boolean usePassword) {
      Object keyBind = "OnOk";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { LoginDlg.this.onOk(e); }
      });

        keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { LoginDlg.this.onCancel(e); }
      });

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) { LoginDlg.this.onCancel(new ActionEvent(we.getSource(), we.getID(), "windowClosing")); }
      });

      // добавляем расположение в центр окна
      getContentPane().add(createComponents(username, usePassword));

      // задаем предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);
   }

   /** этот метод будет возвращать панель с созданным расположением */
   private JComponent createComponents(String username, boolean usePassword) {
      // 1. Создается панель, которая будет содержать все остальные элементы и панели расположения
      Box main = Box.createVerticalBox();

      // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
      // Для этого использую пустую рамку
      main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

      // 2. создаю "полосы", на которые был разбит интерфейс на этапе анализа
      // а) первое текстовое поле и надпись к нему
      Box name = Box.createHorizontalBox();

      JLabel nameLabel = new JLabel("Name:");
      name.add(nameLabel);
      name.add(Box.createHorizontalStrut(12));

      nameField = new JTextField(username, 15);
      name.add(nameField);
      if (username != null)
         nameField.setEditable(false);

      // б) второе текстовое поле и надпись к нему
      Box password = Box.createHorizontalBox();
      JLabel passwrdLabel = new JLabel("Password:");
      password.add(passwrdLabel);
      password.add(Box.createHorizontalStrut(12));
      passwrdField = new JTextField(15);
      password.add(passwrdField);

      // в) ряд кнопок
      JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      JPanel grid = new JPanel(new GridLayout(1, 2, 5, 0));
      JButton ok = new JButton("Ok");
      JButton cancel = new JButton("Cancel");

      grid.add(ok);
      grid.add(cancel);
      flow.add(grid);

      ok.addActionListener(e -> LoginDlg.this.onOk(e));
      cancel.addActionListener(e -> LoginDlg.this.onCancel(e));

      // 3. действия по выравниванию компонентов, уточнению их размеров, приданию одинаковых размеров
      // а) согласованное выравнивание вложенных панелей
      BoxLayoutUtils.setGroupAlignmentX(new JComponent[] { name, password, main, flow }, Component.LEFT_ALIGNMENT);
      // б) центральное выравнивание надписей и текстовых полей
      BoxLayoutUtils.setGroupAlignmentY(new JComponent[] { nameField, passwrdField, nameLabel, passwrdLabel }, Component.CENTER_ALIGNMENT);
      // в) одинаковые размеры надписей к текстовым полям
      GuiTools.makeSameWidth(new Component[] { nameLabel, passwrdLabel });
      // г) стандартный вид для кнопок
      GuiTools.createRecommendedMargin(new JButton[] { ok, cancel });
      // д) устранение "бесконечной" высоты текстовых полей
      GuiTools.fixTextFieldSize(nameField);
      GuiTools.fixTextFieldSize(passwrdField);

      // 4. сбор полос в интерфейс
      main.add(name);
      main.add(Box.createVerticalStrut(12));
      main.add(password);
      Component qwe;
      main.add(qwe = Box.createVerticalStrut(17));
      main.add(flow);

      if (!usePassword) {
         password.setVisible(false);
         qwe.setVisible(false);
      }

      // готово
      return main;
   }

   private void onOk(ActionEvent e) {
//      System.out.println("OnOk");
      if (onOkActionListener != null)
         onOkActionListener.actionPerformed(e);
      onClose();
   }
   private void onCancel(ActionEvent e) {
//      System.out.println("OnCancel");
      nameField.setText(null);
      passwrdField.setText(null);
      if (onCancelActionListener != null)
         onCancelActionListener.actionPerformed(e);
      onClose();
   }
   private void onClose() {
      // при выходе из диалогового окна - освобождаю ресурсы
      dispose();
      //System.exit(0);
   }

   @Override
   public String getName() { return nameField.getText(); }
   public String getPass() { return passwrdField.getText(); }

   public void setOkActionListener(ActionListener onOkActionListener) { this.onOkActionListener = onOkActionListener; }
   public void setCancelActionListener(ActionListener CancelOkActionListener) { this.onCancelActionListener = CancelOkActionListener; }

   //////////////////////////////////////////////////
   // TEST
   /** тестовый метод для проверки диалогового окна */
   public static void main(String[] args) {
      LoginDlg dlg = new LoginDlg(null, true, "aasd", true);
      dlg.setVisible(true);
   }

}