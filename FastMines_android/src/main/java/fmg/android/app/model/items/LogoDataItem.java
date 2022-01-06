package fmg.android.app.model.items;

import fmg.android.img.Logo;
import fmg.core.img.LogoModel;

/** Logo as data model */
public class LogoDataItem extends BaseDataItem<Void, LogoModel, Logo.BitmapView, Logo.BitmapController> {

    public LogoDataItem() {
        super(null);
        setTitle("Mosaics");
    }

    @Override
    public Logo.BitmapController getEntity() {
        if (this.entity == null) {
            Logo.BitmapController tmp = new Logo.BitmapController();
            LogoModel m = tmp.getModel();
            m.setBorderWidth(3);
            m.setRotateMode(LogoModel.ERotateMode.color);
            setEntity(tmp);
        }
        return this.entity;
    }

}
