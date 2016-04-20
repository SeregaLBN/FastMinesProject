package fmg.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;

import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.swing.Main;
import fmg.swing.model.SpinNumberDocListener;
import fmg.swing.model.SpinnerDiapasonModel;
import fmg.swing.res.Resources;
import fmg.swing.utils.GuiTools;

public class SelectMosaicDlg extends JDialog {
   private static final long serialVersionUID = 1L;

   private JSpinner spin;
   private JComboBox<?> cmbxMosaicTypes;
   private JButton btnOk;
   private Main parent;

   private Resources resources;
   private Resources getResources() {
      if (resources == null)
         resources = new Resources();
      return resources;
   }

   public SelectMosaicDlg(JFrame parent, boolean modal) {
      super(parent, "Select mosaic", modal);
      if (parent instanceof Main)
         this.parent = (Main) parent;
      initialize(parent);
   }
   public SelectMosaicDlg(JFrame parent, boolean modal, Resources resources) {
      super(parent, "Select mosaic", modal);
      if (parent instanceof Main)
         this.parent = (Main) parent;
      this.resources = resources;
      initialize(parent);
   }

   private void initialize(JFrame parent) {
      Object keyBind = "OnOk";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.OnOk(); }
      });

        keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.OnClose(); }
      });

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) { SelectMosaicDlg.this.OnClose(); }
      });

      this.setResizable(false);
      CreateComponents();
      // задаю предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);
   }

   public void startSelect(EMosaicGroup initMosaicGroup) {
      String txt = (initMosaicGroup.ordinal()+3) + "0";
      spin.setValue(Integer.parseInt(txt));

//      spin.getEditor().requestFocusInWindow();
//      spin.requestFocusInWindow();

//      SwingUtilities.invokeLater(new Runnable() {
//         @Override
//         public void run() {
//            System.out.println(spin.getValue());
            JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
            txtField.setText(spin.getValue().toString()); // TODO хз... иначе select не работет :(
//            System.out.println(txtField.getText());
            txtField.select(1, 2);

//            System.out.println(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
//            System.out.println(SelectMosaicDlg.this.getFocusOwner());
//            System.out.println(spin.hasFocus());
//            System.out.println(spin.getEditor().hasFocus());
//         }
//      });

      this.setVisible(true);
   }

   // создаю панели с нужным расположением
   private void CreateComponents() {
      // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
      Box boxCenter = Box.createVerticalBox();
      // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
      // использую пустую рамку
      boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
//      boxCenter.setBorder(
//            new javax.swing.border.CompoundBorder(
//                  BorderFactory.createEmptyBorder(12,12,12,12),
//                  new javax.swing.border.CompoundBorder(
//                        BorderFactory.createEtchedBorder(),
//                        BorderFactory.createEmptyBorder(12,12,12,12))));

      JLabel lbl1 = new JLabel("Number:");

      // spin
      spin = new JSpinner(new SpinnerDiapasonModel( EMosaic.getFastCodeValues() ));
      spin.setToolTipText("Fast code mosaic");

      // отслеживаю изменения в редакторе, ...
      JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
//      txtField.getDocument().addDocumentListener(new DocumentListener() {
//         @Override
//         public void removeUpdate(DocumentEvent e) { OnChangeMosaicNumber(); }
//         @Override
//         public void insertUpdate(DocumentEvent e) { OnChangeMosaicNumber(); }
//         @Override
//         public void changedUpdate(DocumentEvent e) {}
//
//      });
      txtField.getDocument().addDocumentListener(new SpinNumberDocListener(spin) {
         @Override
         protected boolean OnChangeTextSpin(DocumentEvent e) {
            boolean res = super.OnChangeTextSpin(e);
            if (res)
               OnChangeMosaicNumber();
            return res;
         }
      });
      // ... а не в самой модели spin'а
//      spin.addChangeListener(new ChangeListener() {
//         @Override
//         public void stateChanged(ChangeEvent e) { OnChangeMosaicNumber(); }
//      });

//      final Object keyBind = "Enter pressed in txt field";
//      txtField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
//      txtField.getActionMap().put(keyBind, new AbstractAction() {
//         private static final long serialVersionUID = 1L;
//         @Override
//         public void actionPerformed(ActionEvent e) {
//            System.out.println(keyBind.toString());
//         }
//      });

      JLabel lbl2 = new JLabel("Type:");

      cmbxMosaicTypes = new JComboBox<Object>(EMosaic.getDescriptionValues().toArray());
//      cmbxMosaicTypes.setPrototypeDisplayValue("aaaaaaaaaaaa");
      // слушатель смены выбранного элемента
      cmbxMosaicTypes.addItemListener(e -> OnChangeMosaicType(e));

      btnOk = new JButton();
      setBtnOkIcons(EMosaic.eMosaicTriangle1);
      btnOk.setToolTipText("Ok");
      Insets margin = btnOk.getMargin();
      margin.left = margin.right = 2; margin.top = margin.bottom = 2;
      btnOk.setMargin(margin);
      btnOk.addActionListener(e -> OnOk());

      // variant 1
      boxCenter.add(lbl1);
      lbl1.setAlignmentX(Component.CENTER_ALIGNMENT);
      boxCenter.add(spin);
      boxCenter.add(Box.createVerticalStrut(12));
      boxCenter.add(lbl2);
      lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);
      boxCenter.add(cmbxMosaicTypes);
      boxCenter.add(Box.createVerticalStrut(12));
      boxCenter.add(btnOk);
      btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
      GuiTools.makeSameWidth(new JComponent[] {lbl1, lbl2, spin, cmbxMosaicTypes});

//      // variant 2
//      Box boxLine = Box.createHorizontalBox();
////      boxLine.setBorder(GuiTools.getDummyBorder(Color.GREEN));
//      {
//         Box boxCol = Box.createVerticalBox();
//         {
//            JPanel flow = new JPanel(new FlowLayout( FlowLayout.LEFT ));
////            flow.setBorder(GuiTools.getDummyBorder(Color.RED));
//            flow.add(lbl1);
//            flow.add(spin);
//            boxCol.add(Box.createVerticalGlue());
//            boxCol.add(flow);
//            flow.setAlignmentX(Component.CENTER_ALIGNMENT);
//            boxCol.add(Box.createVerticalGlue());
//         }
//         boxLine.add(boxCol);
//      }
//      boxCenter.add(Box.createHorizontalStrut(12));
//      boxLine.add(btnOk);
////      btnOk.setAlignmentY(Component.CENTER_ALIGNMENT);
//      boxCenter.add(boxLine);
//      boxCenter.add(Box.createVerticalStrut(12));
//      boxCenter.add(lbl2);
//      lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);
//      boxCenter.add(cmbxMosaicTypes);
//      GuiTools.makeSameWidth(new JComponent[] {boxLine, lbl2, cmbxMosaicTypes});

      // добавляю расположение в центр окна
      getContentPane().add(boxCenter, BorderLayout.CENTER);
   }

   private void OnChangeMosaicType(ItemEvent e) {
      // выясняю, что случилось
      if ( e.getStateChange() == ItemEvent.SELECTED ) {
         // показываю выбранный номер
         final EMosaic item = EMosaic.fromDescription(e.getItem().toString());
//         System.out.println(item);
         final int groupNumber = item.getFastCode();
         if (groupNumber != (Integer)spin.getValue()) {
            SwingUtilities.invokeLater(() -> {
               setBtnOkIcons(item);
               spin.setValue(groupNumber);
            });
         }
      }
   }
   private void OnChangeMosaicNumber() {
//      System.out.println("OnChangeMosaicNumber: getMosaicNumber()=" + getMosaicNumber());
      int val;
      try {
         val = Integer.parseInt(getMosaicNumber());
      } catch (NumberFormatException e) {
         spin.setBackground(new Color(0xF24D5C));
         return;
      }
      EMosaic mosaicType = EMosaic.fromFastCode(val);
      if (mosaicType == null) {
         spin.setBackground(new Color(0xF24D5C));
         return;
      }
      spin.setBackground( UIManager.getDefaults().getColor("Spin.background") ); // TextField.background

      if (mosaicType != getSelectedMosaicType())
         for (int i=0; i<cmbxMosaicTypes.getItemCount(); i++) {
            EMosaic item = EMosaic.fromDescription(cmbxMosaicTypes.getItemAt(i).toString());
            if (item == mosaicType) {
               setBtnOkIcons(item);
               cmbxMosaicTypes.setSelectedIndex(i);
               break;
            }
         }
   }
   private void setBtnOkIcons(EMosaic mosaicType) {
      btnOk.setIcon(getResources().getImgMosaic(mosaicType, false, 51/*,41*/));
      btnOk.setRolloverIcon(getResources().getImgMosaic(mosaicType, false, 50/*,40*/));
   }
   private void OnOk() {
//      System.out.println("OnOk");

      if (parent != null) {
         EMosaic selectedMosaicType = getSelectedMosaicType();
         SwingUtilities.invokeLater(() -> parent.changeGame(selectedMosaicType) );
      }

      OnClose();
   }
   private void OnClose() {
      // при выходе из диалогового окна - освобождаю ресурсы
      dispose();
//      System.exit(0);
   }
   private EMosaic getSelectedMosaicType() {
      EMosaic item = EMosaic.fromDescription(cmbxMosaicTypes.getSelectedItem().toString());
      return item;
   }
   /** данные не из модели, а из редактора */
   private String getMosaicNumber() {
      //return spin.getValue().toString(); // из SpinnerNumberModel

      // я же хочу то что редактируется руцями в editor'е (до нажатия Enter'а на editbox'e)
      JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
      return txtField.getText();
   }

   // тестовый метод для проверки диалогового окна
   public static void main(String[] args) {
      SelectMosaicDlg sm = new SelectMosaicDlg(null, true);
      sm.startSelect(EMosaicGroup.eQuadrangles);
   }
}