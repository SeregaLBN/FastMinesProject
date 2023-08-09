package fmg.android.app.model.items;

import fmg.android.img.AndroidBitmapView;
import fmg.android.img.Logo2;
import fmg.core.img.LogoModel2;

/** Logo as data model */
public class LogoDataItem extends BaseDataItem<Void, LogoModel2, AndroidBitmapView<LogoModel2>, Logo2.LogoAndroidBitmapController> {

    public LogoDataItem() {
        super(null);
        setTitle("Mosaics");
    }

    @Override
    public Logo2.LogoAndroidBitmapController getEntity() {
        if (this.entity == null) {
            var tmp = new Logo2.LogoAndroidBitmapController();
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
