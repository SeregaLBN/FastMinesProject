using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Shapes;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.view.win_rt.draw;
using ua.ksn.fmg.view.win_rt.draw.mosaics;

namespace ua.ksn.fmg.controller.win_rt
{
   public class MosaicExt : Mosaic
   {
      private IDictionary<BaseCell, Tuple<Polygon, TextBlock, Image>> _xamlBinder = new Dictionary<BaseCell, Tuple<Polygon, TextBlock, Image>>();
      private GraphicContext _gContext;
      private CellPaint _cellPaint;
      private Panel _container;

      public MosaicExt()
      {
      }

      public MosaicExt(Size sizeField, EMosaic mosaicType, int minesCount, int area) :
         base(sizeField, mosaicType, minesCount, area)
      {
         BindXamlToMosaic();
      }

      public Panel Container
      {
         get { return _container ?? (_container = new Canvas()); }
      }

      private CellPaint CellPaint
      {
         get { return _cellPaint ?? (_cellPaint = new CellPaint(GContext)); }
      }

      private GraphicContext GContext
      {
         get { return _gContext ?? (_gContext = new GraphicContext(false, new Size())); }
      }

      private IDictionary<BaseCell, Tuple<Polygon, TextBlock, Image>> XamlBinder
      {
         get { return _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, Tuple<Polygon, TextBlock, Image>>()); }
      }

      protected override void NeedRepaint(BaseCell cell)
      {
         //throw new NotImplementedException();
      }

      private void BindXamlToMosaic()
      {
         var sizeMosaic = base.Cells.Size;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++)
            {
               var cell = base.getCell(i, j);
               var shape = new Polygon();
               var txt = new TextBlock();
               var img = new Image();
               XamlBinder.Add(cell, new Tuple<Polygon, TextBlock, Image>(shape, txt, img));
               Container.Children.Add(shape);
               Container.Children.Add(txt);
               Container.Children.Add(img);
            }
      }

      public void Repaint() {
         RepaintComponent(true);
      }

      private void RepaintComponent(bool areaChanged)
      {
         var sizeMosaic = base.Cells.Size;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++)
            {
               var cell = base.getCell(i, j);
               CellPaint.Paint(cell, XamlBinder[cell]);
            }
      }
   }
}