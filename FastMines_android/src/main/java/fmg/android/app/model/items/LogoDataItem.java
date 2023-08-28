package fmg.android.app.model.items;

import fmg.android.img.AndroidBitmapView;
import fmg.android.img.Logo;
import fmg.core.img.LogoModel;

/** Logo as data model */
public class LogoDataItem extends BaseDataItem<Void, LogoModel, AndroidBitmapView<LogoModel>, Logo.LogoAndroidBitmapController> {

    public LogoDataItem() {
        super(null);
        setTitle("Mosaics");
    }

    @Override
    public Logo.LogoAndroidBitmapController getController() {
        if (this.controller == null) {
            var ctrl = new Logo.LogoAndroidBitmapController();
            var m = ctrl.getModel();
            m.setBorderWidth(3);

            // TODO m.setRotateMode(LogoModel.ERotateMode.color);
            m.setRotateAngle(10);
            m.setUseGradient(true);

            setController(ctrl);
        }
        return this.controller;
    }

}
