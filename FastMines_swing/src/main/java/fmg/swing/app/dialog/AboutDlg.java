package fmg.swing.app.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.LogoModel;
import fmg.core.img.SmileModel;
import fmg.swing.img.Logo;
import fmg.swing.img.Smile;
import fmg.swing.utils.GuiTools;
import fmg.swing.utils.ImgUtils;

public class AboutDlg implements AutoCloseable {

    private static final int IMAGE_ZOOM_QUALITY = 3;
    private static final int ICON_SIZE = 48;
    private static final String HTML_BODY = "<html><body ";
    private static final String HTML_CENTER_WIDTH = "><center width='";
    private static final String HTML_A_HREF = "'><a href='";

    private final JDialog dialog;
    private Logo.IconController logo;
    private Smile.IconController smile;
    private JButton btnLogo;
    private final PropertyChangeListener onLogoPropertyChangedListener = this::onLogoPropertyChanged;

    public AboutDlg(JFrame parent, boolean modal) {
        dialog = new JDialog(parent, "About", modal);
        initialize(parent);
    }

    public JDialog getDialog() {
        return dialog;
    }

    private void initialize(JFrame parent) {
        Object keyBind = "CloseDialog";
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        dialog.getRootPane().getActionMap().put(keyBind, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { onClose(); }
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { onClose(); }
        });

        dialog.setResizable(false);
        createComponents();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
    }

    private void onClose() {
        dialog.dispose();
    }

    // создаю панели с нужным расположением
    private void createComponents() {
        // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
        Box boxCenter = Box.createVerticalBox();
        //boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        boxCenter.setBorder(
            new CompoundBorder(
                BorderFactory.createEmptyBorder(12,12,0,12),
                new CompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(12,12,12,12))));


        // 2. Поочередно создаются "полосы", на которые был разбит интерфейс
        // а) ряд для Иконки, НазвыПроги, версии, авторства и лицензии
        Box firstLine = Box.createHorizontalBox();
        {
//            firstLine.setBorder(GuiTools.getDummyBorder(Color.RED));

            // слева - кнопка иконки
            JComponent logo2 = createPanelLogo();
            logo2.setAlignmentY(Component.TOP_ALIGNMENT);
            firstLine.add(logo2);

            firstLine.add(Box.createHorizontalStrut(5));

            // справа - в отдельных стороках тексты НазвыПроги, версии, авторства и лицензии
            JComponent title = createPanelTitle();
            title.setAlignmentY(Component.TOP_ALIGNMENT);
            firstLine.add(title);
        }
        // б) вторая строка - контакты
        JComponent secondLine = createPanelContatcs();

        // 4. Окончательный "сбор" полос в интерфейс
        boxCenter.add(firstLine);
        boxCenter.add(Box.createVerticalStrut(12));
        boxCenter.add(secondLine);
        boxCenter.add(Box.createVerticalStrut(12));

        // добавляю расположение в центр окна
        dialog.getContentPane().add(boxCenter, BorderLayout.CENTER);
        // ряд кнопок внизу
        dialog.getContentPane().add(createPanelOk(), BorderLayout.SOUTH);
    }

    private void onLogoPropertyChanged(PropertyChangeEvent ev) {
        if (IImageController.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
            btnLogo.setIcon(ImgUtils.zoom(logo.getImage(), ICON_SIZE, ICON_SIZE));
            btnLogo.repaint();
        }
    }

    /** логотип */
    private JComponent createPanelLogo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//        panel.setBorder(BorderFactory.createTitledBorder("Logos"));// getDefaultBorder());

        int icoSize = ICON_SIZE * IMAGE_ZOOM_QUALITY;
        if (logo == null)
            logo = new Logo.IconController();
        LogoModel lm = logo.getModel();
        lm.setUseGradient(true);
        lm.setSize(new SizeDouble(icoSize, icoSize));
        lm.setPadding(new BoundDouble(1));
        lm.setRotateMode(LogoModel.ERotateMode.color);
        logo.usePolarLightFgTransforming(true);
        lm.setAnimated(true);
        lm.setAnimatePeriod(12000);
        lm.setTotalFrames(250);
        btnLogo = new JButton(ImgUtils.zoom(logo.getImage(), ICON_SIZE, ICON_SIZE));
        logo.addListener(onLogoPropertyChangedListener);

        smile = new Smile.IconController(SmileModel.EFaceType.Face_Disappointed);
        smile.getModel().setSize(new SizeDouble(icoSize, icoSize));
        btnLogo.setPressedIcon(ImgUtils.zoom(smile.getImage(), ICON_SIZE, ICON_SIZE));
        btnLogo.setFocusable(false);

        Insets margin = btnLogo.getMargin();
        margin.left = margin.right = margin.top = margin.bottom = 2;
        btnLogo.setMargin(margin);

        panel.add(btnLogo);
        return panel;
    }
    /** тексты НазвыПроги, версии, авторства и лицензии */
    private JComponent createPanelTitle() {
        Box panel = Box.createVerticalBox();
//        panel.setBorder(BorderFactory.createTitledBorder("titles"));// getDefaultBorder());

        String htmpWidth = "190px";
        JLabel lblTitle = new JLabel(HTML_BODY +
//              "bgcolor='#FEEF98'" +
              "><font size=6 color=dark face='serif'><center width='"+htmpWidth+"'>FastMines" ); // arial verdana
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//        lblTitle.setBorder(BorderFactory.createEtchedBorder());
        panel.add(lblTitle);

        panel.add(Box.createVerticalStrut(2));

        JLabel lblVersion = new JLabel("Version 2018.07.11 (Java SWING)");
        lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
//        lblVersion.setBorder(BorderFactory.createEtchedBorder());
        panel.add(lblVersion);

        panel.add(Box.createVerticalStrut(2));

        JLabel lblAuthor = new JLabel("Author Serhii Kryvulia aka SeregaLBN");
        lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);
//        lblAuthor.setBorder(BorderFactory.createEtchedBorder());
        panel.add(lblAuthor);

        panel.add(Box.createVerticalStrut(2));

        final String licenseUrl = "http://www.gnu.org/licenses/gpl.html";
        JLabel lblFreeSoft = new JLabel(HTML_BODY +
                                      //"bgcolor='#FEEF98'" +
                                        HTML_CENTER_WIDTH + htmpWidth +
                                        HTML_A_HREF + licenseUrl +
                                        "'>Free software, open source (GPL)");
        lblFreeSoft.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openURI(licenseUrl);
            }});
        lblFreeSoft.setAlignmentX(Component.CENTER_ALIGNMENT);
//        lblFreeSoft.setBorder(BorderFactory.createEtchedBorder());
        panel.add(lblFreeSoft);

        return panel;
    }
    /** контакты */
    private JComponent createPanelContatcs() {
        Border customBorder = new CompoundBorder(
            new EtchedBorder(EtchedBorder.RAISED), //BorderFactory.createBevelBorder(BevelBorder.RAISED),
            BorderFactory.createEmptyBorder(1,5,1,5));
        String htmpWidth = "200px";

        Box mail = Box.createHorizontalBox();
        JLabel lblLeftMail = new JLabel("Mail: ");
        {
            mail.add(Box.createHorizontalStrut(20));
            mail.add(lblLeftMail);
            mail.add(Box.createHorizontalStrut(2));
            final String mailTo = "FastMines@gmail.com";
            JLabel lblMail = new JLabel(HTML_BODY +
                                      //"bgcolor='#FEEF98'" +
                                        HTML_CENTER_WIDTH + htmpWidth +
                                        HTML_A_HREF + mailTo + "'>" + mailTo);
            lblMail.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    openMail("mailto:"+mailTo);
                }});
            lblMail.setBorder(customBorder);
            mail.add(lblMail);
        }


        Box web = Box.createHorizontalBox();
        JLabel lblLeftWeb = new JLabel("Web: ");
        {
            web.add(Box.createHorizontalStrut(20));
            web.add(lblLeftWeb);
            web.add(Box.createHorizontalStrut(2));
            final String webPage = "https://github.com/seregalbn/FastMines";
            JLabel lblWeb = new JLabel(HTML_BODY +
                                       //"bgcolor='#FEEF98'" +
                                       HTML_CENTER_WIDTH + htmpWidth +
                                       HTML_A_HREF + webPage +
                                       "'>" + webPage);
            lblWeb.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    openURI(webPage);
                }});
            lblWeb.setBorder(customBorder);
            web.add(lblWeb);
        }
        GuiTools.makeSameWidth(new JComponent [] {lblLeftMail, lblLeftWeb});

        Box panel = Box.createVerticalBox();
//        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 12));
//        panel.setBorder(BorderFactory.createTitledBorder("contacts"));// getDefaultBorder());

        panel.add(mail);
        panel.add(Box.createVerticalStrut(2));
        panel.add(web);

        return panel;
    }
    /** кнопка Ок */
    private JComponent createPanelOk() {
        JPanel panel = new JPanel( new FlowLayout(FlowLayout.CENTER, 12, 12) );
//        panel.setBorder(GuiTools.getDummyBorder(Color.LIGHT_GRAY));

        JButton ok = new JButton("OK");
        Insets margin = ok.getMargin();
        margin.left = margin.right = 12; margin.top = margin.bottom = 2;
        ok.setMargin(margin);

        // стандартный вид для кнопок
//        createRecommendedMargin(new JButton[] { ok } );

        ok.addActionListener(evt -> onClose());

        panel.add(ok);
        return panel;
    }

    public static boolean openURI(String uri) {
        if (!Desktop.isDesktopSupported()) {
            System.err.println("Fail - Desktop is not supported.");
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            System.err.println("Fail - Desktop doesn't support the browse action");
            return false;
        }
        try {
            desktop.browse(new URI(uri));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean openMail(String mailTo) {
        if (!Desktop.isDesktopSupported()) {
            System.err.println("Fail - Desktop is not supported.");
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.MAIL)) {
            System.err.println("Fail - Desktop doesn't support the mail action");
            return false;
        }
        try {
            desktop.mail(new URI(mailTo));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        logo.removeListener(onLogoPropertyChangedListener);
        logo.close();
        smile.close();
    }

}
