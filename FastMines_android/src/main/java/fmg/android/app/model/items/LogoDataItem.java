package fmg.android.app.model.items;

import fmg.android.img.Logo;
import fmg.core.img.LogoModel;

/** Logo as data model */
public class LogoDataItem extends BaseDataItem<Void, LogoModel, Logo.Bitmap, Logo.ControllerBitmap> {

    public LogoDataItem() {
        super(null);
        setTitle("Mosaics");
    }

    @Override
    public Logo.ControllerBitmap getEntity() {
        if (this.entity == null) {
            Logo.ControllerBitmap tmp = new Logo.ControllerBitmap();
            LogoModel m = tmp.getModel();
            m.setBorderWidth(3);
            m.setRotateMode(LogoModel.ERotateMode.color);
            setEntity(tmp);
        }
        return this.entity;
    }

}
