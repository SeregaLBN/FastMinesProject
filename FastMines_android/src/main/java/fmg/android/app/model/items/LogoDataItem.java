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
    public Logo.LogoAndroidBitmapController getEntity() {
        if (this.entity == null) {
            var tmp = new Logo.LogoAndroidBitmapController();
            var m = tmp.getModel();
            m.setBorderWidth(3);

            // TODO m.setRotateMode(LogoModel.ERotateMode.color);
            m.setRotateAngle(10);
            m.setUseGradient(true);

            setEntity(tmp);
        }
        return this.entity;
    }

}
