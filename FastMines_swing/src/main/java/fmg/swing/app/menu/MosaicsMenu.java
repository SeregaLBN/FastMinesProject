package fmg.swing.app.menu;

import java.awt.Container;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.*;

import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.IMosaicAnimatedModel.EMosaicRotateMode;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.swing.app.KeyCombo;
import fmg.swing.app.FastMinesApp;
import fmg.swing.img.MosaicGroupImg;
import fmg.swing.img.MosaicImg;

public class MosaicsMenu implements AutoCloseable {

    private static final boolean EXPERIMENTAL_MENU_MNEMONIC = true;

    private final FastMinesApp app;
    private final JMenu menu = new JMenu("Mosaics");
    private EnumMap<EMosaicGroup, JMenuItem> mosaicsGroup;
    private List<MosaicGroupImg.IconController> mosaicsGroupImages;
    private Map<EMosaic, JRadioButtonMenuItem> mosaics;
    private List<MosaicImg.IconController> mosaicsImages;
    private final PropertyChangeListener onMosaicImgPropertyChangedListener      = this::onMosaicImgPropertyChanged;
    private final PropertyChangeListener onMosaicGroupImgPropertyChangedListener = this::onMosaicGroupImgPropertyChanged;

    public MosaicsMenu(FastMinesApp app) {
        this.app = app;
        initialize();
    }

    public JMenu getMenu() {
        return menu;
    }

    private void onMosaicImgPropertyChanged(PropertyChangeEvent ev) {
        MosaicImg.IconController img = (MosaicImg.IconController)ev.getSource();
        JRadioButtonMenuItem menuItem = mosaics.get(img.getModel().getMosaicType());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (ev.getPropertyName().equalsIgnoreCase(IImageController.PROPERTY_IMAGE)) {
            MainMenu.setMenuItemIcon(menuItem, img.getImage());
        }
    }

    private void onMosaicGroupImgPropertyChanged(PropertyChangeEvent ev) {
        MosaicGroupImg.IconController img = (MosaicGroupImg.IconController)ev.getSource();
        JMenuItem menuItem = mosaicsGroup.get(img.getModel().getMosaicGroup());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (ev.getPropertyName().equalsIgnoreCase(IImageController.PROPERTY_IMAGE)) {
            MainMenu.setMenuItemIcon(menuItem, img.getImage());
        }
    }

    private void initialize() {
        menu.setMnemonic(KeyCombo.getMnemonic_MenuMosaic());
        for (EMosaicGroup key: EMosaicGroup.values())
            menu.add(getMenuItemMosaicGroup(key));

        ButtonGroup bg = new ButtonGroup();
        for (EMosaic key: EMosaic.values())
            bg.add(getMenuItemMosaic(key));
    }

    private JMenuItem getMenuItemMosaicGroup(EMosaicGroup key) {
        if (mosaicsGroup == null) {
            mosaicsGroup = new EnumMap<>(EMosaicGroup.class);
            mosaicsGroupImages = new ArrayList<>(EMosaicGroup.values().length);

            for (EMosaicGroup val: EMosaicGroup.values()) {
                JMenu menuItem = new JMenu(val.getDescription());// + (experimentalMenuMnemonic ?  "                      " : ""));
                for (EMosaic mosaic: val.getMosaics()) {
                    menuItem.add(getMenuItemMosaic(mosaic));
                    //menuItem.add(Box.createRigidArea(new Dimension(100,25)));
                }
//                        menuItem.setMnemonic(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val));
                MosaicGroupImg.IconController img = new MosaicGroupImg.IconController(val);
                MosaicGroupModel imgModel = img.getModel();
                double sq = MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR;
                imgModel.setSize(new SizeDouble(sq, sq));
                mosaicsGroupImages.add(img);
                imgModel.setPolarLights(true);
                imgModel.setBorderWidth(1 * MainMenu.ZOOM_QUALITY_FACTOR);
                imgModel.setBorderColor(Color.RandomColor().darker(0.4));
                imgModel.setForegroundColor(Color.RandomColor().brighter(0.7));
                imgModel.setBackgroundColor(Color.Transparent());
                imgModel.setAnimeDirection(false);
                imgModel.setAnimated(true);
                imgModel.setAnimatePeriod(13000);
                imgModel.setTotalFrames(250);
                MainMenu.setMenuItemIcon(menuItem,  img.getImage());
                img.addListener(onMosaicGroupImgPropertyChangedListener);

//                        if (experimentalMenuMnemonic) {
//                            menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT));
//                            menuItem.add(new JLabel("Num+111111111111"));// + (char)(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val))));
//                        }

                mosaicsGroup.put(val, menuItem);
            }

            recheckSelectedMosaicType();
        }
        return mosaicsGroup.get(key);
    }

    private JRadioButtonMenuItem getMenuItemMosaic(EMosaic mosaicType) {
        if (mosaics == null) {
            mosaics = new HashMap<>(EMosaic.values().length);
            mosaicsImages = new ArrayList<>(EMosaic.values().length);

            for (EMosaic val: EMosaic.values()) {
                String menuItemTxt = val.getDescription(false);
                if (EXPERIMENTAL_MENU_MNEMONIC)
                    menuItemTxt += "                      ";
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuItemTxt);
                menuItem.setMnemonic(KeyCombo.getMnemonic_Mosaic(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_Mosaic(val));
                menuItem.addActionListener(ev -> app.changeGame(val));

                MosaicImg.IconController img = new MosaicImg.IconController();
                MosaicAnimatedModel<?> imgModel = img.getModel();
                imgModel.setMosaicType(val);
                imgModel.setSizeField(val.sizeIcoField(true));
                imgModel.setSize(new SizeDouble(MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR, MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR));
                mosaicsImages.add(img);
                imgModel.setRotateMode(EMosaicRotateMode.someCells);
                imgModel.getPenBorder().setWidth(1);// * ZoomQualityFactor);
                Color borderColor = Color.RandomColor().darker(0.4);
                imgModel.getPenBorder().setColorLight(borderColor);
                imgModel.getPenBorder().setColorShadow(borderColor);
                imgModel.setBackgroundColor(Color.Transparent());
                imgModel.setAnimatePeriod(5400);
                imgModel.setTotalFrames(110);
                MainMenu.setMenuItemIcon(menuItem, img.getImage());
                img.addListener(onMosaicImgPropertyChangedListener);

                if (EXPERIMENTAL_MENU_MNEMONIC) {
                    menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, MainMenu.MENU_HEIGHT_WITH_ICON/2 - 4));
                    menuItem.add(new JLabel("NumPad " + val.getFastCode()));
                }

                mosaics.put(val, menuItem);
            }
        }
        return mosaics.get(mosaicType);
    }

    /** Выставить верный bullet для меню мозаики */
    public void recheckSelectedMosaicType() {
        EMosaic currentMosaicType = app.getMosaicController().getMosaicType();
        getMenuItemMosaic(currentMosaicType).setSelected(true);

        mosaicsImages.forEach(img -> img.getModel().setAnimated(img.getModel().getMosaicType() == currentMosaicType));
        mosaicsGroupImages.forEach(img -> {
            boolean isCurrentGroup = (img.getModel().getMosaicGroup() == currentMosaicType.getGroup());
            img.useRotateTransforming(isCurrentGroup);
        });
    }

    @Override
    public void close() {
        mosaicsGroupImages.forEach(img -> {
            img.removeListener(onMosaicGroupImgPropertyChangedListener);
            img.close();
        });
        mosaicsImages.forEach(img -> {
            img.removeListener(onMosaicImgPropertyChangedListener);
            img.close();
        });
    }

}
