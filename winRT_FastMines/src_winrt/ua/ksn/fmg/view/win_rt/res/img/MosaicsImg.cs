using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.view.win_rt.draw;
using ua.ksn.fmg.view.win_rt.draw.mosaics;

namespace ua.ksn.fmg.view.win_rt.res.img {

   /// <summary>
   /// �������� ���� ���������� �������. ������������ ��� ����, ������, etc... 
   /// </summary>
   public class MosaicsImg {
      private readonly BaseCell.BaseAttribute attr;
      private readonly CellPaint gInfo;
      private readonly List<BaseCell> arrCell;
      private readonly static GraphicContext gContext;
	   private readonly Size sizeField;
      private readonly int area;

      static MosaicsImg() {
         gContext = new GraphicContext(true);
         gContext.PenBorder.Width = 2;
         gContext.PenBorder.ColorLight = gContext.PenBorder.ColorShadow;
      }

      public MosaicsImg(EMosaic mosaicType, bool smallIco) : this(mosaicType, smallIco, 300) { }
      public MosaicsImg(EMosaic mosaicType, bool smallIco, int area) {
         this.area = area;
         attr = CellFactory.createAttributeInstance(mosaicType, area);
         arrCell = new List<BaseCell>();
         gInfo = new CellPaint(gContext);
         sizeField = attr.sizeIcoField(smallIco);
         for (int i=0; i < sizeField.width; i++)
            for (int j=0; j < sizeField.height; j++)
               arrCell.Add(CellFactory.createCellInstance(attr, mosaicType, new Coord(i, j)));
      }

      private WriteableBitmap _image = null;
      public ImageSource Image {
         get {
            if (_image == null) {
               var pixelSize = attr.CalcOwnerSize(sizeField, area);
               _image = BitmapFactory.New(pixelSize.width, pixelSize.height);
               foreach (var cell in arrCell)
                  gInfo.Paint(cell, _image);
            }
            return this._image;
         }
      }
   }
}