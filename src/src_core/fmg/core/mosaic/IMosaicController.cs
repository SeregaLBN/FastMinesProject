using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

    /// <summary> MVC: mosaic controller interface </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicView">mosaic view</typeparam>
    /// <typeparam name="TMosaicModel">mosaic model</typeparam>
    public interface IMosaicController<out TImage, out TImageInner, out TMosaicView, out TMosaicModel>
                    : IImageController<    TImage,                      TMosaicView,     TMosaicModel>
        where TImage : class
        where TImageInner : class
        where TMosaicView : IMosaicView<TImage, TImageInner, TMosaicModel>
        where TMosaicModel : MosaicDrawModel<TImageInner>
    {

        /// <summary> размер поля в ячейках </summary>
        Matrisize SizeField { get; set; }

        /// <summary> узнать тип мозаики (из каких фигур состоит мозаика поля) </summary>
        EMosaic MosaicType { get; set; }

        /// <summary> количество мин </summary>
        int MinesCount { get; set; }

        /// <summary> ячейка на которой было нажато (но не обязательно что отпущено) </summary>
        BaseCell CellDown { get; set; }

    }

}
