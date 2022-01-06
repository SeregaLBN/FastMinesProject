package fmg.swing.app.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.model.control.SpinNumberDocListener;
import fmg.swing.app.model.control.SpinnerDiapasonModel;
import fmg.swing.img.MosaicImg;
import fmg.swing.utils.Cast;
import fmg.swing.utils.GuiTools;
import fmg.swing.utils.ImgUtils;

public class SelectMosaicDlg implements AutoCloseable {

    private final FastMinesApp app;
    private final JDialog dialog;
    private JSpinner spin;
    private JComboBox<?> cmbxMosaicTypes;
    private JButton btnOk;

    private MosaicImg.ImageAwtController mosaicsImg, mosaicsImgRollover;
    private final PropertyChangeListener onMosaicsImgPropertyChangedListener = this::onMosaicsImgPropertyChanged;

    private static final int ImgSize = 40;
    private static final int ImgZoomQuality = 3;
    private static final Color bkTabBkColor = Cast.toColor(fmg.common.Color.Transparent()); // UIManager.getColor("Button.light"); // "Button.light" "Button.foreground"
    private static final Color bkTabBkColorSelected = UIManager.getColor("Button.shadow"); // "Button.select" "Button.darkShadow"

    public SelectMosaicDlg(FastMinesApp app, boolean modal) {
        this.app = app;
        dialog = new JDialog((app == null) ? null : app.getFrame(), "Select mosaic", modal);
        initialize();
    }

    private void initialize() {
        Object keyBind = "onOk";
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        dialog.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.onOk(); }
        });

        keyBind = "CloseDialog";
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        dialog.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.onClose(); }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { SelectMosaicDlg.this.onClose(); }
        });

        dialog.setResizable(false);
        createComponents();
        dialog.pack();
        dialog.setLocationRelativeTo((app == null) ? null : app.getFrame());
    }

    public void startSelect(EMosaicGroup initMosaicGroup) {
        String txt = (initMosaicGroup.ordinal()+3) + "0";
        spin.setValue(Integer.parseInt(txt));

//        spin.getEditor().requestFocusInWindow();
//        spin.requestFocusInWindow();

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                Logger.info(spin.getValue());
                JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
                txtField.setText(spin.getValue().toString()); // TODO хз... иначе select не работет :(
//                Logger.info(txtField.getText());
                txtField.select(1, 2);

//                Logger.info(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
//                Logger.info(SelectMosaicDlg.this.getFocusOwner());
//                Logger.info(spin.hasFocus());
//                Logger.info(spin.getEditor().hasFocus());
//            }
//        });

        this.setVisible(true);
    }

    // создаю панели с нужным расположением
    private void createComponents() {
        // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
        Box boxCenter = Box.createVerticalBox();
        // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
        // использую пустую рамку
        boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
//        boxCenter.setBorder(
//            new javax.swing.border.CompoundBorder(
//                BorderFactory.createEmptyBorder(12,12,12,12),
//                new javax.swing.border.CompoundBorder(
//                    BorderFactory.createEtchedBorder(),
//                    BorderFactory.createEmptyBorder(12,12,12,12))));

        JLabel lbl1 = new JLabel("Number:");

        // spin
        spin = new JSpinner(new SpinnerDiapasonModel( EMosaic.getFastCodeValues() ));
        spin.setToolTipText("Fast code mosaic");

        // отслеживаю изменения в редакторе, ...
        JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
//        txtField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) { onChangeMosaicNumber(); }
//            @Override
//            public void insertUpdate(DocumentEvent e) { onChangeMosaicNumber(); }
//            @Override
//            public void changedUpdate(DocumentEvent e) {}
//
//        });
        txtField.getDocument().addDocumentListener(new SpinNumberDocListener(spin) {
            @Override
            protected boolean OnChangeTextSpin(DocumentEvent e) {
                boolean res = super.OnChangeTextSpin(e);
                if (res)
                    onChangeMosaicNumber();
                return res;
            }
        });
        // ... а не в самой модели spin'а
//        spin.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) { onChangeMosaicNumber(); }
//        });

//        final Object keyBind = "Enter pressed in txt field";
//        txtField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).info(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
//        txtField.getActionMap().info(keyBind, new AbstractAction() {
//            private static final long serialVersionUID = 1L;
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Logger.info(keyBind.toString());
//            }
//        });

        JLabel lbl2 = new JLabel("Type:");

        cmbxMosaicTypes = new JComboBox<>(EMosaic.getDescriptionValues().toArray());
//        cmbxMosaicTypes.setPrototypeDisplayValue("aaaaaaaaaaaa");
        // слушатель смены выбранного элемента
        cmbxMosaicTypes.addItemListener(e -> onChangeMosaicType(e));

        btnOk = new JButton();
        setBtnOkIcons(EMosaic.eMosaicTriangle1);
        btnOk.setToolTipText("Ok");
        Insets margin = btnOk.getMargin();
        margin.left = margin.right = 2; margin.top = margin.bottom = 2;
        btnOk.setMargin(margin);
        btnOk.addActionListener(e -> onOk());

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

//        // variant 2
//        Box boxLine = Box.createHorizontalBox();
////        boxLine.setBorder(GuiTools.getDummyBorder(Color.GREEN));
//        {
//            Box boxCol = Box.createVerticalBox();
//            {
//                JPanel flow = new JPanel(new FlowLayout( FlowLayout.LEFT ));
////                flow.setBorder(GuiTools.getDummyBorder(Color.RED));
//                flow.add(lbl1);
//                flow.add(spin);
//                boxCol.add(Box.createVerticalGlue());
//                boxCol.add(flow);
//                flow.setAlignmentX(Component.CENTER_ALIGNMENT);
//                boxCol.add(Box.createVerticalGlue());
//            }
//            boxLine.add(boxCol);
//        }
//        boxCenter.add(Box.createHorizontalStrut(12));
//        boxLine.add(btnOk);
////        btnOk.setAlignmentY(Component.CENTER_ALIGNMENT);
//        boxCenter.add(boxLine);
//        boxCenter.add(Box.createVerticalStrut(12));
//        boxCenter.add(lbl2);
//        lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);
//        boxCenter.add(cmbxMosaicTypes);
//        GuiTools.makeSameWidth(new JComponent[] {boxLine, lbl2, cmbxMosaicTypes});

        // добавляю расположение в центр окна
        dialog.getContentPane().add(boxCenter, BorderLayout.CENTER);
    }

    private void onChangeMosaicType(ItemEvent e) {
        // выясняю, что случилось
        if ( e.getStateChange() == ItemEvent.SELECTED ) {
            // показываю выбранный номер
            final EMosaic item = EMosaic.fromDescription(e.getItem().toString());
//            Logger.info(item);
            final int groupNumber = item.getFastCode();
            if (groupNumber != (Integer)spin.getValue()) {
                SwingUtilities.invokeLater(() -> {
                    setBtnOkIcons(item);
                    spin.setValue(groupNumber);
                });
            }
        }
    }
    private void onChangeMosaicNumber() {
//        Logger.info("onChangeMosaicNumber: getMosaicNumber()=" + getMosaicNumber());
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
        if (mosaicsImg == null) {
            mosaicsImg = new MosaicImg.ImageAwtController();
            mosaicsImg.setMosaicType(mosaicType);
            mosaicsImg.setSizeField(mosaicType.sizeIcoField(true));
            MosaicAnimatedModel<?> imgModel = mosaicsImg.getModel();
            imgModel.setSize(new SizeDouble(ImgSize*ImgZoomQuality, ImgSize*ImgZoomQuality));
            imgModel.setPadding(new BoundDouble(10));
            imgModel.setBackgroundColor(Cast.toColor(bkTabBkColor));
            int redrawInterval = 50;
            double rotateAngleDelta = 3.5;
            double totalFrames = 360 / rotateAngleDelta;
            imgModel.setAnimatePeriod((int)(totalFrames * redrawInterval));
            imgModel.setTotalFrames((int)totalFrames);
            imgModel.setAnimated(true);
            mosaicsImg.addListener(onMosaicsImgPropertyChangedListener);
        } else {
            mosaicsImg.setMosaicType(mosaicType);
        }
        btnOk.setIcon(ImgUtils.toIco(mosaicsImg.getImage(), ImgSize, ImgSize));

        if (mosaicsImgRollover == null) {
            mosaicsImgRollover = new MosaicImg.ImageAwtController();
            mosaicsImgRollover.setMosaicType(mosaicType);
            mosaicsImgRollover.setSizeField(mosaicType.sizeIcoField(true));
            MosaicAnimatedModel<?> imgModel = mosaicsImg.getModel();
            imgModel.setSize(new SizeDouble(ImgSize*ImgZoomQuality, ImgSize*ImgZoomQuality));
            imgModel.setPadding(new BoundDouble(3));
            imgModel.setBackgroundColor(Cast.toColor(bkTabBkColorSelected));
        } else {
            mosaicsImgRollover.setMosaicType(mosaicType);
        }
        btnOk.setRolloverIcon(ImgUtils.toIco(mosaicsImgRollover.getImage(), ImgSize, ImgSize));
    }

    private void onMosaicsImgPropertyChanged(PropertyChangeEvent ev) {
        if (!dialog.isVisible())
            return;
        if (ev.getPropertyName().equalsIgnoreCase(IImageController.PROPERTY_IMAGE)) {
            btnOk.setIcon(ImgUtils.toIco(mosaicsImg.getImage(), ImgSize, ImgSize));
        }
    }

    private EMosaic getSelectedMosaicType() {
        return EMosaic.fromDescription(cmbxMosaicTypes.getSelectedItem().toString());
    }

    /** данные не из модели, а из редактора */
    private String getMosaicNumber() {
        //return spin.getValue().toString(); // из SpinnerNumberModel

        // я же хочу то что редактируется руцями в editor'е (до нажатия Enter'а на editbox'e)
        JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
        return txtField.getText();
    }


    public void setVisible(boolean b) {
        mosaicsImg.getModel().setAnimated(b);
        dialog.setVisible(b);
    }

    private void onOk() {
//      Logger.info("onOk");

      if (app != null) {
          EMosaic selectedMosaicType = getSelectedMosaicType();
          SwingUtilities.invokeLater(() -> app.changeGame(selectedMosaicType) );
      }

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
        mosaicsImg.removeListener(onMosaicsImgPropertyChangedListener);
        mosaicsImg.close();
        mosaicsImgRollover.close();
    }

}
