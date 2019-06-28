package fmg.swing.app;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.viewmodel.serializable.ChampionsModel;
import fmg.core.types.viewmodel.serializable.PlayersModel;
import fmg.swing.app.dialog.*;
import fmg.swing.img.Animator;
import fmg.swing.mosaic.MosaicJPanelController;
import fmg.swing.utils.ProjSettings;

public class TestDialogs {

    private static void testMosaicJPanelController() {
        MosaicJPanelController ctrllr = new MosaicJPanelController();

        JFrame frame = new JFrame();
        frame.setContentPane(ctrllr.getViewPanel());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                ctrllr.close();
                frame.dispose();
            }
        });

        frame.setTitle("SWING: Demo " + MosaicJPanelController.class.getSimpleName());
        frame.pack();
        frame.setVisible(true);
    }

    private static void testAboutDlg() {
        try (AboutDlg dlg = new AboutDlg(null, true)) {
            dlg.getDialog().setVisible(true);
        }
    }

    private static void testChampionsModel() {
        ChampionsModel champions = new ChampionsModel(null);
        champions.Load();
        try (ChampionDlg dlg = new ChampionDlg(null, true, champions)) {
            dlg.showData(ESkillLevel.eBeginner, EMosaic.eMosaicSquare1);
        }
    }

    private static void testCustomSkillDlg() {
        CustomSkillDlg sm = new CustomSkillDlg(null, true);
        sm.setVisible(true);
    }

    private static void testLoginDlg() {
        LoginDlg dlg = new LoginDlg(null, true, "aasd", true);
        dlg.getDialog().setVisible(true);
    }

    private static void testManageDlg() {
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

    private static void testReportDlg() {
        try (ReportDlg dlg = new ReportDlg(null, true) { }) {
            dlg.showData(ESkillLevel.eAmateur, EMosaic.eMosaicTriangle1, -1);
        }
    }

    private static void testSelectMosaicDlg() {
        SwingUtilities.invokeLater(() -> {
            try (SelectMosaicDlg sm = new SelectMosaicDlg(null, true)) {
                sm.startSelect(EMosaicGroup.eQuadrangles);
            }
        });
    }

    private static void testStatisticDlg() {
        PlayersModel players = new PlayersModel();
        players.Load();
        try (StatisticDlg dlg = new StatisticDlg(null, true, players)) {
            dlg.showData(ESkillLevel.eAmateur, EMosaic.eMosaicTriangle3);
        }
    }


    public static void main(String[] args) {
        ProjSettings.init();

        JFrame frame = new JFrame("Test dialogs");
        Container pane = frame.getContentPane();
        GridLayout grLayout = new GridLayout(3,2);
        pane.setLayout(grLayout);

        BiConsumer<String, Runnable> add = (title, clickHandler) ->
            pane.add(new JButton(title + "...") {
                private static final long serialVersionUID = 1L;
                {
                    addActionListener(ev -> {
                        SwingUtilities.invokeLater(clickHandler);
                        frame.dispose();
                    });
                }
            });
        add.accept("Mosaic"      , TestDialogs::testMosaicJPanelController);
        add.accept("About"       , TestDialogs::testAboutDlg);
        add.accept("Champions"   , TestDialogs::testChampionsModel);
        add.accept("CustomSkill" , TestDialogs::testCustomSkillDlg);
        add.accept("Login"       , TestDialogs::testLoginDlg);
        add.accept("Manage"      , TestDialogs::testManageDlg);
        add.accept("Report"      , TestDialogs::testReportDlg);
        add.accept("SelectMosaic", TestDialogs::testSelectMosaicDlg);
        add.accept("Statistic"   , TestDialogs::testStatisticDlg);


        frame.setPreferredSize(new Dimension(500, 150));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Animator.getSingleton().close();
    }

}
