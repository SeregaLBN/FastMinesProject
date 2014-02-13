using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Shapes;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using Windows.UI;

namespace ua.ksn.fmg.controller.win_rt
{
   public class MosaicExt : Mosaic
   {

      private readonly Panel _container;
      private IDictionary<BaseCell, Polygon> _dependency = new Dictionary<BaseCell, Polygon>();

      public MosaicExt(Panel container = null)
      {
         _container = container ?? new Canvas();
      }

      public MosaicExt(Panel container, Size sizeField, EMosaic mosaicType, int minesCount, int area) :
         base(sizeField, mosaicType, minesCount, area)
      {
         _container = container ?? new Canvas();
         FillDependencys();
      }

      protected override void NeedRepaint(BaseCell cell)
      {
         //throw new NotImplementedException();
      }

      private void FillDependencys()
      {
         var sizeMosaic = base.Cells.Size;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++)
            {
               var cell = base.getCell(i, j);
               var shape = new Polygon
               {
                  Points = new PointCollection(),
                  Stroke = new SolidColorBrush {
                     //Color = cell.getBackgroundFillColor()
                     Color = Colors.Purple,
                     Opacity = .5
                  },
                  StrokeThickness = 2,
                  Fill = new SolidColorBrush {
                     //Color = cell.getBackgroundFillColor()
                     Color = Colors.Green,
                     Opacity = .5
                  },
               };
               for (int p = 0; p < cell.getRegion().CountPoints; p++)
               {
                  var point = cell.getRegion().getPoint(p);
                  shape.Points.Add((Windows.Foundation.Point)point);
               }
               _dependency.Add(cell, shape);
               _container.Children.Add(shape);
            }
      }
   }
}