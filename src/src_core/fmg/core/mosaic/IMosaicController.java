package fmg.core.mosaic;

import java.util.Collection;

import fmg.common.geom.Matrisize;
import fmg.core.img.IImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;

/** MVC: mosaic controller interface
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicView> mosaic view
 * @param <TMosaicModel> mosaic model
 */
public interface IMosaicController<TImage, TImageInner,
                                   TMosaicView extends IMosaicView<TImage, TImageInner, TMosaicModel>,
                                   TMosaicModel extends IMosaicDrawModel<TImageInner>>
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

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    BaseCell getCellDown();
    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    void setCellDown(BaseCell cellDown);

    /** количество мин */
    int getCountMines();
    /** количество мин */
    void setCountMines(int newCountMines);

    /** сколько ещё осталось открыть мин */
    int getCountMinesLeft(); // return getCountMines() - getCountFlag();

    int getCountOpen();
    int getCountFlag();
    int getCountUnknown();


    /** Подготовиться к началу игры - сбросить все ячейки */
    boolean gameNew();
    /** Начать игру, т.к. произошёл первый клик на поле */
    void gameBegin(BaseCell firstClickCell);
    /** Завершить игру */
    Collection<BaseCell> gameEnd(boolean victory);

    /**
     *<br> Этапы игры:
     *<br>           GameNew()      GameBegin()     GameEnd()      GameNew()
     *<br>    time      |               |               |             |
     *<br>  -------->   | eGSCreateGame |               |             |
     *<br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
     *<br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
     *<br>
     *<br> @see fmg.core.types.EGameStatus
     *<br>
     *<br> PS: При этапе gsReady поле чисто - мин нет! Мины расставляются только после первого клика
     *<br>     Так сделал только лишь потому, чтобы первый клик выполнялся не на мине. Естественно
     *<br>     это не относится к случаю, когда игра была создана пользователем или считана из файла.
     */
    EGameStatus getGameStatus();

}
