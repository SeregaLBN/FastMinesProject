using System.Collections.Generic;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Core.Mosaic.Cells;
using Fmg.Core.App.Model;

namespace Fmg.Core.Mosaic
{

    /// <summary> MVC: mosaic controller interface </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicView">mosaic view</typeparam>
    /// <typeparam name="TMosaicModel">mosaic model</typeparam>
    public interface IMosaicController<out TImage, out TImageInner, out TMosaicView, out TMosaicModel>
                    : IImageController<TImage, TMosaicView, TMosaicModel>
        where TImage : class
        where TImageInner : class
        where TMosaicView : IMosaicView<TImage, TImageInner, TMosaicModel>
        where TMosaicModel : IMosaicDrawModel<TImageInner>
    {

        /// <summary> размер поля в ячейках </summary>
        Matrisize SizeField { get; set; }

        /// <summary> узнать тип мозаики (из каких фигур состоит мозаика поля) </summary>
        EMosaic MosaicType { get; set; }

        /// <summary> ячейка на которой было нажато (но не обязательно что отпущено) </summary>
        BaseCell CellDown { get; set; }


        /// <summary> действительно лишь когда gameStatus == gsEnd </summary>
        bool IsVictory { get; }

        /// <summary> количество мин </summary>
        int CountMines { get; set; }

        /// <summary>сколько ещё осталось открыть мин</summary>
        int CountMinesLeft { get; } // => CountMines - CountFlag
        int CountClick { get; }

        int CountOpen { get; }
        int CountFlag { get; }
        int CountUnknown { get; }


        /// <summary> Retrieve mosaic backup data </summary>
        MosaicBackupData GameBackup();
        /// <summary> Restore mosaic field from backup </summary>
        void GameRestore(MosaicBackupData backup);
        /// <summary> Подготовиться к началу игры - сбросить все ячейки </summary>
        bool GameNew();
        /// <summary> Начать игру, т.к. произошёл первый клик на поле </summary>
        void GameBegin(BaseCell firstClickCell);
        /// <summary> Завершить игру </summary>
        IEnumerable<BaseCell> GameEnd(bool victory);


        /// <summary>
        // <br> Этапы игры:
        // <br>           GameNew()      GameBegin()     GameEnd()      GameNew()
        // <br>    time      |               |               |             |
        // <br>  -------->   | eGSCreateGame |               |             |
        // <br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
        // <br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
        // <br>
        // <br> @see Fmg.Core.Types.EGameStatus
        // <br>
        // <br> PS: При этапе gsReady поле чисто - мин нет! Мины расставляются только после первого клика
        // <br>     Так сделал только лишь потому, чтобы первый клик выполнялся не на мине. Естественно
        // <br>     это не относится к случаю, когда игра была создана пользователем или считана из файла.
        /// </summary>
        EGameStatus GameStatus { get; }

    }

}
