package fmg.swing.app.menu;

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.*;

import javax.swing.*;

import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ImageHelper;
import fmg.core.img.MosaicGroupModel2;
import fmg.core.img.MosaicImageModel2.ERotateMode;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.KeyCombo;
import fmg.swing.img.MosaicGroupImg2;
import fmg.swing.img.MosaicImg2;

public class MosaicsMenu implements AutoCloseable {

    private static final boolean EXPERIMENTAL_MENU_MNEMONIC = true;

    private final FastMinesApp app;
    private final JMenu menu = new JMenu("Mosaics");
    private EnumMap<EMosaicGroup, JMenuItem> mosaicsGroup;
    private List<MosaicGroupImg2.MosaicGroupSwingIconController> mosaicsGroupImages;
    private Map<EMosaic, JRadioButtonMenuItem> mosaics;
    private List<MosaicImg2.MosaicSwingIconController> mosaicsImages;

    public MosaicsMenu(FastMinesApp app) {
        this.app = app;
        initialize();
    }

    public JMenu getMenu() {
        return menu;
    }

    private void onMosaicImgPropertyChanged(int index, String propertyName) {
        var img = mosaicsImages.get(index);
        JRadioButtonMenuItem menuItem = mosaics.get(img.getModel().getMosaicType());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (propertyName.equalsIgnoreCase(ImageHelper.PROPERTY_IMAGE)) {
            MainMenu.setMenuItemIcon(menuItem, img.getImage());
        }
    }

    private void onMosaicGroupImgPropertyChanged(int index, String propertyName) {
        var img = mosaicsGroupImages.get(index);
        JMenuItem menuItem = mosaicsGroup.get(img.getModel().getMosaicGroup());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (propertyName.equalsIgnoreCase(ImageHelper.PROPERTY_IMAGE)) {
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

            int i = 0;
            for (EMosaicGroup val: EMosaicGroup.values()) {
                JMenu menuItem = new JMenu(val.getDescription());// + (experimentalMenuMnemonic ?  "                      " : ""));
                for (EMosaic mosaic: val.getMosaics()) {
                    menuItem.add(getMenuItemMosaic(mosaic));
                    //menuItem.add(Box.createRigidArea(new Dimension(100,25)));
                }
//                        menuItem.setMnemonic(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val));
                var img = new MosaicGroupImg2.MosaicGroupSwingIconController(val);
                MosaicGroupModel2 imgModel = img.getModel();
                double sq = MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR;
                imgModel.setSize(new SizeDouble(sq, sq));
                mosaicsGroupImages.add(img);
                imgModel.setBorderWidth(1 * MainMenu.ZOOM_QUALITY_FACTOR);
                imgModel.setBorderColor(Color.RandomColor().darker(0.4));
                imgModel.setForegroundColor(Color.RandomColor().brighter(0.7));
                imgModel.setBackgroundColor(Color.Transparent());
                img.setPolarLightsForeground(true);
                img.setClockwise(false);
                img.setAnimatePeriod(13000);
                img.setFps(30);;
                MainMenu.setMenuItemIcon(menuItem,  img.getImage());
                int index = i;
                img.setListener(pn -> onMosaicGroupImgPropertyChanged(index, pn));

//                        if (experimentalMenuMnemonic) {
//                            menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT));
//                            menuItem.add(new JLabel("Num+111111111111"));// + (char)(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val))));
//                        }

                mosaicsGroup.put(val, menuItem);

                ++i;
            }

            recheckSelectedMosaicType();
        }
        return mosaicsGroup.get(key);
    }

    private JRadioButtonMenuItem getMenuItemMosaic(EMosaic mosaicType) {
        if (mosaics == null) {
            mosaics = new HashMap<>(EMosaic.values().length);
            mosaicsImages = new ArrayList<>(EMosaic.values().length);

            int i = 0;
            for (EMosaic val: EMosaic.values()) {
                String menuItemTxt = val.getDescription(false);
                if (EXPERIMENTAL_MENU_MNEMONIC)
                    menuItemTxt += "                      ";
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuItemTxt);
                menuItem.setMnemonic(KeyCombo.getMnemonic_Mosaic(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_Mosaic(val));
                menuItem.addActionListener(ev -> app.changeGame(val));

                var img = new MosaicImg2.MosaicSwingIconController();
                var imgModel = img.getModel();
                imgModel.setMosaicType(val);
                imgModel.setSizeField(val.sizeIcoField(true));
                imgModel.setSize(new SizeDouble(MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR, MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR));
                mosaicsImages.add(img);
                imgModel.getPenBorder().setWidth(1);// * ZoomQualityFactor);
                Color borderColor = Color.RandomColor().darker(0.4);
                imgModel.getPenBorder().setColorLight(borderColor);
                imgModel.getPenBorder().setColorShadow(borderColor);
                imgModel.setBackgroundColor(Color.Transparent());
                imgModel.setRotateMode(ERotateMode.SOME_CELLS);
                img.setAnimatePeriod(5400);
                img.setFps(30);
                MainMenu.setMenuItemIcon(menuItem, img.getImage());
                int index = i;
                img.setListener(pn -> onMosaicImgPropertyChanged(index, pn));

                if (EXPERIMENTAL_MENU_MNEMONIC) {
                    menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, MainMenu.MENU_HEIGHT_WITH_ICON/2 - 4));
                    menuItem.add(new JLabel("NumPad " + val.getFastCode()));
                }

                mosaics.put(val, menuItem);

                ++i;
            }
        }
        return mosaics.get(mosaicType);
    }

    /** Выставить верный bullet для меню мозаики */
    public void recheckSelectedMosaicType() {
        EMosaic currentMosaicType = app.getMosaicController().getModel().getMosaicType();
        getMenuItemMosaic(currentMosaicType).setSelected(true);

        mosaicsImages.forEach(img -> img.setRotateImage(img.getModel().getMosaicType() == currentMosaicType));
        mosaicsGroupImages.forEach(img -> {
            boolean isCurrentGroup = (img.getModel().getMosaicGroup() == currentMosaicType.getGroup());
            img.setRotateImage(isCurrentGroup);
        });
    }

    @Override
    public void close() {
        mosaicsGroupImages.forEach(img -> {
            img.setListener(null);
            img.close();
        });
        mosaicsImages.forEach(img -> {
            img.setListener(null);
            img.close();
        });
    }

}
