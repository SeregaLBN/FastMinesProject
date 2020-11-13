package fmg.swing.app;

import java.awt.event.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.core.types.ClickResult;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.draw.EShowElement;
import fmg.core.types.draw.EZoomInterface;
import fmg.swing.app.toolbar.EBtnNewGameState;

/** MainApp action handlers */
public class Handlers {

    private final FastMinesSwing app;

    public Handlers(FastMinesSwing app) {
        this.app = app;
    }

    private Action gameNewAction;
    public Action getGameNewAction() {
        if (gameNewAction == null)
            gameNewAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.gameNew();
                }
            };

        return gameNewAction;
    }

    private Map<ESkillLevel, Action> skillLevelActions;
    public Action getSkillLevelAction(ESkillLevel key) {
        if (skillLevelActions == null) {
            skillLevelActions = new EnumMap<>(ESkillLevel.class);

            for (final ESkillLevel val: ESkillLevel.values())
                skillLevelActions.put(val, new AbstractAction() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        app.changeGame(val);
                    }
                });
        }

        return skillLevelActions.get(key);
    }

    private Action playerManageAction;
    public Action getPlayerManageAction() {
        if (playerManageAction == null)
            playerManageAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.getPlayerManageDlg().setVisible(
                        !app.getPlayerManageDlg().getDialog().isVisible());
                }
            };

        return playerManageAction;
    }

    private Action gameExitAction;
    public Action getGameExitAction() {
        if (gameExitAction == null)
            gameExitAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.onClose();
                }
            };

        return gameExitAction;
    }

    private Action pauseAction;
    /** Action на нажатие кнопки/клавиши паузы */
    public Action getPauseAction() {
        if (pauseAction == null)
            pauseAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (app.isUsePause())
                        app.changePause();
                }
            };
        return pauseAction;
    }

    private Action minimizedAction;
    public Action getMinimizedAction() {
        if (minimizedAction == null)
            minimizedAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.iconify();
                }
            };

        return minimizedAction;
    }

    private Action centerScreenAction;
    public Action getCenterScreenAction() {
        if (centerScreenAction == null)
            centerScreenAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.toCenterScreen();
                }
            };

        return centerScreenAction;
    }

    private Map<EShowElement, Action> showElementsAction;
    public Action getShowElementAction(EShowElement key) {
        if (showElementsAction == null) {
            showElementsAction = new EnumMap<>(EShowElement.class);

            for (final EShowElement val: EShowElement.values())
                showElementsAction.put(val, new AbstractAction() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        app.optionsShowElement(val, e);
                    }
                });
        }
        return showElementsAction.get(key);
    }

    private Action themeDefaultAction;
    public Action getThemeDefaultAction() {
        if (themeDefaultAction == null)
            themeDefaultAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.optionsThemeDefault();
                }
            };

        return themeDefaultAction;
    }

    private Action themeSystemAction;
    public Action getThemeSystemAction() {
        if (themeSystemAction == null)
            themeSystemAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.optionsThemeSystem();
                }
            };

        return themeSystemAction;
    }

    private Consumer<ClickResult> mosaicClickHandler;
    public Consumer<ClickResult> getMosaicClickHandler() {
        if (mosaicClickHandler == null)
            mosaicClickHandler = (clickResult) -> {
                //Logger.info("OnMosaicClick: down=" + clickResult.isDown() + "; leftClick=" + clickResult.isLeft());
                if (clickResult.isLeft() && (app.getMosaicController().getGameStatus() == EGameStatus.eGSPlay)) {
                    Icon img = app.getToolbar().getSmileIco(
                            clickResult.isDown() ?
                                EBtnNewGameState.eNormalMosaic :
                                EBtnNewGameState.eNormal);
                    if (img != null)
                        app.getToolbar().getBtnNew().getButton().setIcon(img);
                }
            };

        return mosaicClickHandler;
    }

    private MouseListener pausePanelMouseListener;
    public MouseListener getPausePanelMouseListener() {
        if (pausePanelMouseListener == null)
            pausePanelMouseListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) && app.isPaused())
                        app.changePause();
                }
            };
        return pausePanelMouseListener;
    }

    private WindowAdapter windowAdapter;
    private WindowAdapter getWindowAdapter() {
        if (windowAdapter == null)
            windowAdapter = new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    app.onClose();
                }
                @Override
                public void windowStateChanged(WindowEvent ev) {
                    app.onWindowStateChanged(ev);
                }
            };
        return windowAdapter;
    }
    public WindowListener getWindowListener() {
        return getWindowAdapter();
    }
    public WindowStateListener getWindowStateListener() {
        return getWindowAdapter();
    }

    private WindowFocusListener windowFocusListener;
    public WindowFocusListener getWindowFocusListener() {
        if (windowFocusListener == null)
            windowFocusListener = new WindowFocusListener() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    if (!app.isPaused()) {
                        if (app.isUsePause())
                            app.changePause();

                        Icon img = app.getToolbar().getSmileIco(EBtnNewGameState.eNormal);
                        if (img != null)
                            app.getToolbar().getBtnNew().getButton().setIcon(img);
                    }
//                        getRootPane().requestFocusInWindow();
                }
                @Override
                public void windowGainedFocus(WindowEvent e) {}
            };
        return windowFocusListener;
    }

    private MouseWheelListener mouseWheelListener;
    public MouseWheelListener getMouseWheelListener() {
        if (mouseWheelListener == null)
            mouseWheelListener = evt -> {
//                      Logger.info("FMG::mouseWheelMoved: " + evt);
                if (!app.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected())
                    switch (evt.getWheelRotation()) {
                    case  1: app.sizeDec(); break;
                    case -1: app.sizeInc(); break;
                    }
            };

        return mouseWheelListener;
    }

    private Map<EZoomInterface, Action> zoomActions;
    public Action getZoomAction(EZoomInterface key) {
        if (zoomActions == null) {
            zoomActions = new EnumMap<>(EZoomInterface.class);

            for (final EZoomInterface val: EZoomInterface.values())
                zoomActions.put(val, new AbstractAction() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean alwaysMax = app.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();
                        switch (val) {
                        case eAlwaysMax:           app.sizeAlwaysMax(e); break;
                        case eMax: if (!alwaysMax) app.sizeMax(); break;
                        case eMin: if (!alwaysMax) app.sizeMin(); break;
                        case eInc: if (!alwaysMax) app.sizeInc(); break;
                        case eDec: if (!alwaysMax) app.sizeDec(); break;
                        }
                    }
                });
        }
        return zoomActions.get(key);
    }

    private Action championsAction;
    public Action getChampionsAction() {
        if (championsAction == null)
            championsAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (app.getChampionDialog().getDialog().isVisible())
                        app.getChampionDialog().setVisible(false);
                    else {
                        app.getChampionDialog().showData(
                                app.getSkillLevel(),
                                app.getMosaicController().getMosaicType());
                    }
                }
            };

        return championsAction;
    }

    private Action statisticsAction;
    public Action getStatisticsAction() {
        if (statisticsAction == null)
            statisticsAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (app.getStatisticDialog().getDialog().isVisible())
                        app.getStatisticDialog().setVisible(false);
                    else
                        app.getStatisticDialog().showData(app.getSkillLevel(), app.getMosaicController().getMosaicType());
                }
            };

        return statisticsAction;
    }

    private Action aboutAction;
    public Action getAboutAction() {
        if (aboutAction == null)
            aboutAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.getAboutDialog().getDialog().setVisible(!app.getAboutDialog().getDialog().isVisible());
                }
            };

        return aboutAction;
    }

    private Map<EMosaicGroup, Action> selectMosaicActions;
    public Action getSelectMosaicAction(final EMosaicGroup key) {
        if (selectMosaicActions == null) {
            selectMosaicActions = new EnumMap<>(EMosaicGroup.class);

            for (final EMosaicGroup val: EMosaicGroup.values())
                selectMosaicActions.put(val, new AbstractAction() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    app.getSelectMosaicDialog().startSelect(val);
                    }
                });
        }

        return selectMosaicActions.get(key);
    }

    private Action useUnknownAction;
    public Action getUseUnknownAction() {
        if (useUnknownAction == null)
            useUnknownAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.optionsUseUnknown(e);
                }
            };

        return useUnknownAction;
    }
    private Action usePauseAction;
    public Action getUsePauseAction() {
        if (usePauseAction == null)
            usePauseAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    app.optionsUsePause(e);
                }
            };

        return usePauseAction;
    }

    private Action timePlayAction;
    public Action getTimePlayAction() {
        if (timePlayAction == null)
            timePlayAction = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int val = Integer.parseInt(app.getToolbar().getEdtTimePlay().getText());
                        app.getToolbar().getEdtTimePlay().setText(Integer.toString(++val));
                    } catch (Exception ex) {
                        Logger.error("Handlers::getTimePlayAction", ex);
                    }
                }
            };

        return timePlayAction;
    }

    private Action mosaicSizeIncX, mosaicSizeDecX, mosaicSizeIncY, mosaicSizeDecY;
    public Action getMosaicSizeIncX() {
        if (mosaicSizeIncX == null)
            mosaicSizeIncX = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    Matrisize size = app.getMosaicController().getSizeField();
                    size = new Matrisize(size);
                    size.m++;
                    app.changeGame(size);
                }
            };

        return mosaicSizeIncX;
    }
    public Action getMosaicSizeDecX() {
        if (mosaicSizeDecX == null)
            mosaicSizeDecX = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    Matrisize size = app.getMosaicController().getSizeField();
                    size = new Matrisize(size);
                    size.m = Math.max(3, size.m - 1);
                    app.changeGame(size);
                }
            };

        return mosaicSizeDecX;
    }
    public Action getMosaicSizeIncY() {
        if (mosaicSizeIncY == null)
            mosaicSizeIncY = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    Matrisize size = app.getMosaicController().getSizeField();
                    size = new Matrisize(size);
                    size.n++;
                    app.changeGame(size);
                }
            };

        return mosaicSizeIncY;
    }
    public Action getMosaicSizeDecY() {
        if (mosaicSizeDecY == null)
            mosaicSizeDecY = new AbstractAction() {
                private static final long serialVersionUID = 1L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    Matrisize size = app.getMosaicController().getSizeField();
                    size = new Matrisize(size);
                    size.n = Math.max(3, size.n - 1);
                    app.changeGame(size);
                }
            };

        return mosaicSizeDecY;
    }

}
