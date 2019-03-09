package fmg.core.mosaic;

import fmg.common.geom.Matrisize;
import fmg.core.img.IImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

/** MVC: mosaic controller interface
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicView> mosaic view
 * @param <TMosaicModel> mosaic model
 */
public interface IMosaicController<TImage, TImageInner,
                                   TMosaicView extends IMosaicView<TImage, TImageInner, TMosaicModel>,
                                   TMosaicModel extends MosaicDrawModel<TImageInner>>
          extends IImageController<TImage, TMosaicView, TMosaicModel>
{

    /** размер поля в ячейках */
    Matrisize getSizeField();
    /** размер поля в ячейках */
    void setSizeField(Matrisize newSizeField);

    /** узнать тип мозаики
     * (из каких фигур состоит мозаика поля) */
    EMosaic getMosaicType();
    /** установить тип мозаики */
    void setMosaicType(EMosaic newMosaicType);

    /** количество мин */
    int getMinesCount();
    /** количество мин */
    void setMinesCount(int newMinesCount);

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    BaseCell getCellDown();
    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    void setCellDown(BaseCell cellDown);


}
