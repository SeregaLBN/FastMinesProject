package fmg.swing.dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.ESkillLevel;
import fmg.swing.Main;
import fmg.swing.utils.GuiTools;

public class CustomSkillDlg extends JDialog implements PropertyChangeListener {
   private static final long serialVersionUID = 1L;

   private JSpinner spinX, spinY, spinMines;
   private JButton btnPopup;
   private JButton btnCancel, btnOk;
   private JRadioButton radioFullScreenCurrSizeArea, radioFullScreenMiniSizeArea;
   private ButtonGroup radioGroup;
   private JPopupMenu popupMenu;
   private Main parent;

   public CustomSkillDlg(JFrame parent, boolean modal) {
      super(parent, "Select skill", modal);
      if (parent instanceof Main)
         this.parent = (Main) parent;
      initialize(parent);
   }

   private void initialize(JFrame parent) {
      Object keyBind = "OnOk";
      getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
      getRootPane().getActionMap().put(keyBind, new AbstractAction() {
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) { onOk(); }
      });

      keyBind = "CloseDialog";
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

      this.setResizable(false);
      createComponents();
      // задаю предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);

      if (this.parent != null)
         this.parent.getMosaicController().addListener(this);
   }

   // создаю панели с нужным расположением
   private void createComponents() {
      // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
      Box boxCenter = Box.createHorizontalBox();
      Box boxBottom = Box.createHorizontalBox();
      // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
      // использую пустую рамку
      boxBottom.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
//      boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
      boxCenter.setBorder(
            new javax.swing.border.CompoundBorder(
                  BorderFactory.createEmptyBorder(12,12,0,12),
                  new javax.swing.border.CompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(0,5,5,5))));

      JLabel lblX = new JLabel("Width ");
      JLabel lblY = new JLabel("Height ");
      JLabel lblMines = new JLabel("Mines ");

      // spin
      spinX = new JSpinner();
      spinY = new JSpinner();

      spinMines = new JSpinner();
      spinMines.setToolTipText("Mines count");

      // отслеживаю изменения
      ChangeListener changeSizeListener = e -> onChangeSizeField();
      spinX.addChangeListener(changeSizeListener);
      spinY.addChangeListener(changeSizeListener);

//      // отслеживаю изменения также и в редакторе
//      ((JSpinner.DefaultEditor)spinX    .getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinX    ));
//      ((JSpinner.DefaultEditor)spinY    .getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinY    ));
//      ((JSpinner.DefaultEditor)spinMines.getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinMines));

      btnPopup = new JButton();
      btnPopup.setText("\u25BC"); // http://www.fileformat.info/info/unicode/char/25bc/index.htm
      Insets margin = btnPopup.getMargin();
      margin.left = margin.right = 0; margin.top = margin.bottom = 0;
      btnPopup.setMargin(margin);
      btnPopup.addActionListener(e -> onPopup());

      btnOk = new JButton();
      btnOk.setText("Ok");
      margin = btnOk.getMargin();
      margin.left = margin.right = 5; margin.top = margin.bottom = 2;
      btnOk.setMargin(margin);
      btnOk.addActionListener(e -> onOk());

      btnCancel = new JButton();
      btnCancel.setText("Cancel");
      margin = btnCancel.getMargin();
      margin.left = margin.right = 5; margin.top = margin.bottom = 2;
      btnCancel.setMargin(margin);
      btnCancel.addActionListener(e -> onClose());


      JPanel panel4Radio = new JPanel(new GridLayout(0, 1, 0, 5));
      panel4Radio.setBorder(BorderFactory.createTitledBorder("Full screen"));
      radioGroup = new ButtonGroup();
      radioFullScreenCurrSizeArea = new JRadioButton("Current cell area");
      radioFullScreenMiniSizeArea = new JRadioButton("Minimal cell area");
      panel4Radio.add(radioFullScreenCurrSizeArea);
      panel4Radio.add(radioFullScreenMiniSizeArea);
      radioGroup.add(radioFullScreenCurrSizeArea);
      radioGroup.add(radioFullScreenMiniSizeArea);
      radioFullScreenCurrSizeArea.addActionListener(e -> onFullScreenCurrArea());
      radioFullScreenMiniSizeArea.addActionListener(e -> onFullScreenMiniArea());

      JPanel panel4Left = new JPanel(new GridLayout(0, 1, 0, 5));
      panel4Left.setBorder(BorderFactory.createEmptyBorder(7,0,2,0));
      Box boxLine = Box.createHorizontalBox();
      //Component rigid1 = Box.createRigidArea(new Dimension(1,1));
      boxLine.add(lblX); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinX); //boxLine.add(rigid1);
      panel4Left.add(boxLine);
      boxLine = Box.createHorizontalBox();
      //Component rigid2 = Box.createRigidArea(new Dimension(1,1));
      boxLine.add(lblY); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinY); //boxLine.add(rigid2);
      panel4Left.add(boxLine);
      boxLine = Box.createHorizontalBox();
      boxLine.add(lblMines); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinMines); boxLine.add(btnPopup);
      panel4Left.add(boxLine);
      GuiTools.makeSameWidth(new Component[] {lblX, lblY, lblMines});

      Dimension prefSize;
      prefSize = spinX.getPreferredSize(); prefSize.width += 20; spinX.setPreferredSize(prefSize);
      prefSize = spinY.getPreferredSize(); prefSize.width += 20; spinY.setPreferredSize(prefSize);
      prefSize = spinMines.getPreferredSize(); prefSize.width += 10; spinMines.setPreferredSize(prefSize);
      GuiTools.makeSameWidth(new Component[] {spinX, spinY, spinMines});
      //GuiTools.makeSameWidth(new Component[] {rigid1, rigid2, btnPopup});

      boxBottom.add(btnCancel);
      boxBottom.add(Box.createHorizontalGlue());
      boxBottom.add(btnOk);
      GuiTools.makeSameWidth(new JComponent[] {btnCancel, btnOk});

      boxCenter.add(panel4Left);
      boxCenter.add(Box.createHorizontalStrut(5));
      boxCenter.add(panel4Radio);

      // добавляю расположение в центр окна и внизу
      getContentPane().add(boxCenter, BorderLayout.CENTER);
      getContentPane().add(boxBottom, BorderLayout.SOUTH);
   }

   private void onChangeSizeField() {
//      System.out.println("OnChangeSizeField");
      int max = (Integer)spinX.getValue() * (Integer)spinY.getValue() - getNeighborNumber();
      ((SpinnerNumberModel )spinMines.getModel()).setMaximum(max);
      if ((Integer)spinMines.getValue() > max)
         spinMines.setValue(max);

      radioGroup.clearSelection();
   }

   private void onChangeMosaicType() {
      recalcModelValueXY(false, false);
      recalcModelValueMines();
      radioGroup.clearSelection();
   }

   private void onPopup() {
//      System.out.println("CustomSkill::OnPopup: ");
      if (popupMenu == null) {
         popupMenu = new JPopupMenu();
         for (final ESkillLevel val: ESkillLevel.values()) {
            if (val == ESkillLevel.eCustom)
               continue;

            JMenuItem menuItem = new JMenuItem();

            menuItem.setText(val.getDescription());
            menuItem.addActionListener(e -> onPopupSetSize(val));
            popupMenu.add(menuItem);
         }
      }
      Rectangle rc = btnPopup.getBounds();
      popupMenu.show(this, rc.x, rc.y + rc.height);
   }

   private void onOk() {
//      System.out.println("OnOk");

      if (parent != null) {
         int x = (Integer)spinX.getValue();
         int y = (Integer)spinY.getValue();
         int m = (Integer)spinMines.getValue();
         SwingUtilities.invokeLater(() -> parent.changeGame(new Matrisize(x,y), m) );
      }

      onClose();
   }
   private void onClose() {
      // при выходе из диалогового окна - освобождаю ресурсы
      dispose();
   }

   // тестовый метод для проверки диалогового окна
   public static void main(String[] args) {
      CustomSkillDlg sm = new CustomSkillDlg(null, true);
      sm.setVisible(true);
   }

   private int getNeighborNumber() {
      if (parent == null)
         return 21;
      BaseCell.BaseAttribute attr = MosaicHelper.createAttributeInstance(parent.getMosaicController().getMosaicType());
      int max = IntStream.range(0, attr.getDirectionCount())
            .map(i -> attr.getNeighborNumber(i))
            .max().getAsInt();
      return max + 1; // +thisCell
   }

   private void recalcModelValueXY(boolean isFullScreen, boolean isFullScreenAtCurrArea) {
      int currSizeX, currSizeY, miniSizeX, miniSizeY, maxiSizeX, maxiSizeY;
      if (parent == null) {
         currSizeX = currSizeY = 10;
         miniSizeX = miniSizeY = 5;
         maxiSizeX = maxiSizeY = 50;
      } else {
         miniSizeX = miniSizeY = 5;

         Matrisize s = parent.calcMaxMosaicSize(MosaicInitData.AREA_MINIMUM);
         maxiSizeX = s.m; maxiSizeY = s.n;

         if (isFullScreen) {
            if (isFullScreenAtCurrArea)
               s = parent.calcMaxMosaicSize(parent.getMosaicController().getArea());
         } else
            s = parent.getMosaicController().getSizeField();
         currSizeX = s.m; currSizeY = s.n;
      }
//      // recheck
//      if (currSizeX < miniSizeX) currSizeX = miniSizeX;
//      if (currSizeY < miniSizeY) currSizeY = miniSizeY;
//      if (currSizeX > maxiSizeX) currSizeX = maxiSizeX;
//      if (currSizeY > maxiSizeY) currSizeY = maxiSizeY;

      spinX.setModel(new SpinnerNumberModel(currSizeX, miniSizeX, maxiSizeX, 1));
      spinY.setModel(new SpinnerNumberModel(currSizeY, miniSizeY, maxiSizeY, 1));
   }

   private void recalcModelValueMines() {
      int minesCurr = (parent == null) ? 15 : parent.getMosaicController().getMinesCount();
      int minesMin = 1;
      int minesMax = (Integer)spinX.getValue() * (Integer)spinY.getValue() - getNeighborNumber();
//      // recheck
//      if (minesCurr < minesMin) minesCurr = minesMin;
//      if (minesCurr > minesMax) minesCurr = minesMax;

      spinMines.setModel(new SpinnerNumberModel(minesCurr, minesMin, minesMax, 1));
   }

   @Override
   public void setVisible(boolean b) {
//      System.out.println("setVisible: " + b);
      if (b) {
         recalcModelValueXY(false, false);
         recalcModelValueMines();
         radioGroup.clearSelection();
      }
      super.setVisible(b);
   }

   private void onFullScreenCurrArea() {
//      System.out.println("OnFullScreenCurrArea");
      recalcModelValueXY(true, true);
   }
   private void onFullScreenMiniArea() {
//      System.out.println("OnFullScreenMiniArea");
      recalcModelValueXY(true, false);
   }

   private void onPopupSetSize(ESkillLevel eSkill) {
      if (parent == null)
         return;
      Matrisize size = new Matrisize((Integer)spinX.getValue(), (Integer)spinY.getValue());
      int mines = eSkill.getNumberMines(parent.getMosaicController().getMosaicType(), size);
      spinMines.setValue(mines);
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      switch (evt.getPropertyName()) {
      case MosaicController.PROPERTY_MOSAIC_TYPE:
         if (isVisible())
            onChangeMosaicType();
         break;
      case MosaicController.PROPERTY_AREA:
         if (radioFullScreenCurrSizeArea.isSelected())
            radioGroup.clearSelection();
         break;
      //case MosaicController.PROPERTY_SIZE_FIELD:
      //   ...
      //   break;
      }
   }

}