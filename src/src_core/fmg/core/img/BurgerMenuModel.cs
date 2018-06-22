using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;

namespace fmg.core.img {

   /// <summary>
   /// Abstract representable menu as horizontal or vertical lines
   /// </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
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
         set {
            if (SetProperty(ref _showBurgerMenu, value)) {
               Invalidate();
            }
         }
      }

      private bool _horizontalBurgerMenu;
      public bool HorizontalBurgerMenu {
         get { return _horizontalBurgerMenu; }
         set {
            if (SetProperty(ref _horizontalBurgerMenu, value)) {
               Invalidate();
            }
         }
      }

      private int _layersInBurgerMenu;
      public int LayersInBurgerMenu {
         get { return _layersInBurgerMenu; }
         set {
            if (SetProperty(ref _layersInBurgerMenu, value)) {
               Invalidate();
            }
         }
      }

      private bool _rotateBurgerMenu;
      public bool RotateBurgerMenu {
         get { return _rotateBurgerMenu; }
         set {
            if (SetProperty(ref _rotateBurgerMenu, value)) {
               Invalidate();
            }
         }
      }

      private Bound? _paddingBurgerMenu;
      public Bound PaddingBurgerMenu {
         get {
            if (_paddingBurgerMenu == null)
               // call this setter
               PaddingBurgerMenu = new Bound(Size.Width / 2,
                                             Size.Height / 2,
                                             Padding.Right,
                                             Padding.Bottom);
            return _paddingBurgerMenu.Value;
         }
         set {
            if (value.LeftAndRight >= Size.Width)
               throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (value.TopAndBottom >= Size.Height)
               throw new ArgumentException("Padding size is very large. Should be less than Height.");
            if (SetProperty(ref _paddingBurgerMenu, value)) {
               Invalidate();
            }
         }
      }
      public void ResetPaddingBurgerMenu() {
         if (_paddingBurgerMenu == null)
            return;
         _paddingBurgerMenu = null;
         OnPropertyChanged(nameof(this.PaddingBurgerMenu));
         Invalidate();
      }

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
         var pad = PaddingBurgerMenu;
         Rect rc = new Rect(pad.Left,
                            pad.Top,
                            Size.Width  - pad.LeftAndRight,
                            Size.Height - pad.TopAndBottom);;
         double penWidth = Math.Max(1, (horizontal ? rc.Height : rc.Width) / (2.0 * layers));
         double rotateAngle = RotateBurgerMenu ? RotateAngle : 0;
         double stepAngle = 360.0 / layers;

         return Enumerable.Range(0, layers)
            .Select(layerNum => {
               double layerAlignmentAngle = FixAngle(layerNum * stepAngle + rotateAngle);
               double offsetTop = !horizontal ? 0 : layerAlignmentAngle * rc.Height / 360;
               double offsetLeft = horizontal ? 0 : layerAlignmentAngle * rc.Width / 360;
               PointDouble start = new PointDouble(rc.Left() + offsetLeft,
                                                   rc.Top() + offsetTop);
               PointDouble end = new PointDouble((horizontal ? rc.Right() : rc.Left()  ) + offsetLeft,
                                                 (horizontal ? rc.Top()   : rc.Bottom()) + offsetTop);

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
