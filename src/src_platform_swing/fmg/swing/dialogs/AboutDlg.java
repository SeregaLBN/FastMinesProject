package fmg.swing.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

import fmg.core.img.LogoModel;
import fmg.core.img.SmileModel;
import fmg.swing.draw.img.Animator;
import fmg.swing.draw.img.Logo;
import fmg.swing.draw.img.Smile;
import fmg.swing.utils.GuiTools;
import fmg.swing.utils.ImgUtils;

public class AboutDlg extends JDialog implements AutoCloseable {

   private static final long serialVersionUID = 1L;
   private static final int ImgZoomQuality = 3;

   private Logo.ControllerIcon _logo;
   private Smile.ControllerIcon _smile;

   public AboutDlg(JFrame parent, boolean modal) {
      super(parent, "About", modal);
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

      this.setResizable(false);
      createComponents();
      // задаю предпочтительный размер
      pack();
      this.setLocationRelativeTo(parent);
   }

   private void onClose() {
      // при выходе из диалогового окна - освобождаю ресурсы
      dispose();
//      System.exit(0);
   }

   // создаю панели с нужным расположением
   private void createComponents() {
      // 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
      Box boxCenter = Box.createVerticalBox();
      // Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов.
      // использую пустую рамку
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
//         firstLine.setBorder(GuiTools.getDummyBorder(Color.RED));

         // слева - кнопка иконки
         JComponent logo = createPanelLogo();
         logo.setAlignmentY(Component.TOP_ALIGNMENT);
         firstLine.add(logo);

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
      getContentPane().add(boxCenter, BorderLayout.CENTER);
      // ряд кнопок внизу
      getContentPane().add(createPanelOk(), BorderLayout.SOUTH);
   }

   /** логотип */
   private JComponent createPanelLogo() {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//      panel.setBorder(BorderFactory.createTitledBorder("Logos"));// getDefaultBorder());

      final int constSize = 48;
      int icoSize = constSize * ImgZoomQuality;
      if (_logo == null)
         _logo = new Logo.ControllerIcon();
      LogoModel lm = _logo.getModel();
      lm.setUseGradient(true);
      lm.setSize(icoSize);
      lm.setPadding(1);
      lm.setRotateMode(LogoModel.ERotateMode.color);
      _logo.usePolarLightFgTransforming(true);
      lm.setAnimated(true);
      lm.setAnimatePeriod(12000);
      lm.setTotalFrames(250);
      JButton btnLogo = new JButton(ImgUtils.zoom(_logo.getImage(), constSize, constSize));
      _logo.addListener(ev -> {
         if (Logo.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
            btnLogo.setIcon(ImgUtils.zoom(_logo.getImage(), constSize, constSize));
            btnLogo.repaint();
         }
      });

      _smile = new Smile.ControllerIcon(SmileModel.EFaceType.Face_Disappointed);
      _smile.getModel().setSize(icoSize, icoSize);
      btnLogo.setPressedIcon(ImgUtils.zoom(_smile.getImage(), constSize, constSize));
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
//      panel.setBorder(BorderFactory.createTitledBorder("titles"));// getDefaultBorder());

      String htmpWidth = "190px";
      JLabel lblTitle = new JLabel("<html><body " +
//            "bgcolor='#FEEF98'" +
            "><font size=6 color=dark face='serif'><center width='"+htmpWidth+"'>FastMines" ); // arial verdana
      /* Универсальные семейства шрифтов:
       *     serif — шрифты с засечками (антиквенные), типа Times;
       *     sans-serif — рубленные шрифты (шрифты без засечек или гротески), типичный представитель — Arial;
       *     cursive — курсивные шрифты;
       *     fantasy — декоративные шрифты;
       *     monospace — моноширинные шрифты, ширина каждого символа в таком семействе одинакова (шрифт Courier).
       **/
      lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//      lblTitle.setBorder(BorderFactory.createEtchedBorder());
      panel.add(lblTitle);

      panel.add(Box.createVerticalStrut(2));

      JLabel lblVersion = new JLabel("Version 2018.05.31 (Java SWING)");
      lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
//      lblVersion.setBorder(BorderFactory.createEtchedBorder());
      panel.add(lblVersion);

      panel.add(Box.createVerticalStrut(2));

      JLabel lblAuthor = new JLabel("Author Sergey Krivulya (KSerg)");
      lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);
//      lblAuthor.setBorder(BorderFactory.createEtchedBorder());
      panel.add(lblAuthor);

      panel.add(Box.createVerticalStrut(2));

      final String licenseUrl = "http://www.gnu.org/licenses/gpl.html";
      JLabel lblFreeSoft = new JLabel("<html><body " +
//            "bgcolor='#FEEF98'" +
            "><center width='"+htmpWidth+"'><a href='"+licenseUrl+"'>Free software, open source (GPL)");
      lblFreeSoft.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            OpenURI(licenseUrl);
         }});
      lblFreeSoft.setAlignmentX(Component.CENTER_ALIGNMENT);
//      lblFreeSoft.setBorder(BorderFactory.createEtchedBorder());
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
         JLabel lblMail = new JLabel("<html><body " +
//               "bgcolor='#FEEF98'" +
               "><center width='"+htmpWidth+"'><a href='"+mailTo+"'>"+mailTo);
         lblMail.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               OpenMail("mailto:"+mailTo);
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
         JLabel lblWeb = new JLabel("<html><body " +
//               "bgcolor='#FEEF98'" +
               "><center width='"+htmpWidth+"'><a href='"+webPage+"'>"+webPage);
         lblWeb.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               OpenURI(webPage);
            }});
         lblWeb.setBorder(customBorder);
         web.add(lblWeb);
      }
      GuiTools.makeSameWidth(new JComponent [] {lblLeftMail, lblLeftWeb});

      Box panel = Box.createVerticalBox();
//      JPanel panel = new JPanel(new GridLayout(2, 2, 5, 12));
//      panel.setBorder(BorderFactory.createTitledBorder("contacts"));// getDefaultBorder());

      panel.add(mail);
      panel.add(Box.createVerticalStrut(2));
      panel.add(web);

      return panel;
   }
   /** кнопка Ок */
   private JComponent createPanelOk() {
      JPanel panel = new JPanel( new FlowLayout(FlowLayout.CENTER, 12, 12) );
//      panel.setBorder(GuiTools.getDummyBorder(Color.LIGHT_GRAY));

      JButton ok = new JButton("OK");
      Insets margin = ok.getMargin();
      margin.left = margin.right = 12; margin.top = margin.bottom = 2;
      ok.setMargin(margin);

      // стандартный вид для кнопок
//      createRecommendedMargin(new JButton[] { ok } );

      ok.addActionListener(evt -> onClose());

      panel.add(ok);
      return panel;
   }

   public static boolean OpenURI(String uri) {
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

   public static boolean OpenMail(String mailTo) {
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
      _logo.close();
      _smile.close();
   }

   //////////////////////////////////////////////////
   // TEST
   public static void main(String[] args) {
      try (AboutDlg dlg = new AboutDlg(null, true)) {
         dlg.setVisible(true);
      }
      Animator.getSingleton().close();
   }

}
