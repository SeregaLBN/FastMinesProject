using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;

namespace fmg.core.img {

   /**
    * Abstract representable menu as horizontal or vertical lines
    * @param <TImage> plaform specific image
    */
   public abstract class BurgerMenuImg<TImage> : PolarLightsImg<TImage>
      where TImage : class
   {
      protected BurgerMenuImg() {
         _showBurgerMenu = true;
         _layersInBurgerMenu = 3;
         _horizontalBurgerMenu = true;
         _rotateBurgerMenu = true;
      }

      private bool _showBurgerMenu;
      public bool ShowBurgerMenu {
         get { return _showBurgerMenu; }
         set { SetProperty(ref _showBurgerMenu, value); }
      }

      private bool _horizontalBurgerMenu;
      public bool HorizontalBurgerMenu {
         get { return _horizontalBurgerMenu; }
         set { SetProperty(ref _horizontalBurgerMenu, value); }
      }

      private int _layersInBurgerMenu;
      public int LayersInBurgerMenu {
         get { return _layersInBurgerMenu; }
         set { SetProperty(ref _layersInBurgerMenu, value); }
      }

      private bool _rotateBurgerMenu;
      public bool RotateBurgerMenu {
         get { return _rotateBurgerMenu; }
         set { SetProperty(ref _rotateBurgerMenu, value); }
      }

      protected virtual RectDouble BurgerMenuRegion => new RectDouble(
         Size.Width  / 2.0,
         Size.Height / 2.0,
         Size.Width  / 2.0 - Padding.Right,
         Size.Height / 2.0 - Padding.Bottom);

      protected struct LineInfo {
         public Color clr;
         public double penWidht;
         public PointDouble from; // start coord
         public PointDouble to;   // end   coord
      }

      protected IEnumerable<LineInfo> GetCoordsBurgerMenu() {
         if (!ShowBurgerMenu)
            return Enumerable.Empty<LineInfo>();

         bool horizontal = HorizontalBurgerMenu;
         int layers = LayersInBurgerMenu;
         RectDouble rc = BurgerMenuRegion;
         double penWidth = Math.Max(1, (horizontal ? rc.Height : rc.Width) / (2 * layers));
         double rotateAngle = RotateBurgerMenu ? RotateAngle : 0;
         double stepAngle = 360.0 / layers;

         return Enumerable.Range(0, layers)
            .Select(layerNum => {
               double layerAlignmentAngle = FixAngle(layerNum * stepAngle + rotateAngle);
               double offsetTop = !horizontal ? 0 : layerAlignmentAngle * rc.Height / 360;
               double offsetLeft = horizontal ? 0 : layerAlignmentAngle * rc.Width / 360;
               PointDouble start = new PointDouble(rc.Left() + offsetLeft,
                                                   rc.Top() + offsetTop);
               PointDouble end = new PointDouble((horizontal ? rc.Right() : rc.Left()) + offsetLeft,
                                                 (horizontal ? rc.Top() : rc.Bottom()) + offsetTop);

               HSV hsv = new HSV(Color.Gray);
               hsv.v *= Math.Sin(layerNum * stepAngle / layers);

               LineInfo li = new LineInfo();
               li.clr = hsv.ToColor();
               li.penWidht = penWidth;
               li.from = start;
               li.to = end;
               return li;
            });
      }

   }
}
