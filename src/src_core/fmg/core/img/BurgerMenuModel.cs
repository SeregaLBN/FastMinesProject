using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> MVC: model of representable menu as horizontal or vertical lines </summary>
   public class BurgerMenuModel : IImageModel {

      private ImageModel _generalModel;
      private bool _show = true;
      private bool _horizontal = true;
      private int  _layers = 3;
      private bool _rotate;
      private BoundDouble _padding;

      private bool _disposed;
      public event PropertyChangedEventHandler PropertyChanged;
      protected readonly NotifyPropertyChanged _notifier;

      /// <summary> ctor </summary>
      /// <param name="generalModel">another basic model</param>
      internal BurgerMenuModel(ImageModel generalModel) {
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
         _generalModel = generalModel;
         _generalModel.PropertyChanged += OnPropertyGeneralModelChanged;
      }

      private void OnPropertyGeneralModelChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _generalModel));
         if (nameof(ImageModel.Size) == ev.PropertyName) {
            if (ev is PropertyChangedExEventArgs<SizeDouble> evEx)
               RecalcPadding(evEx.OldValue);
            else
               throw new Exception();
         }
      }

      /// <summary> image width and height in pixel </summary>
      public SizeDouble Size {
         get { return _generalModel.Size; }
         set { _generalModel.Size = value; }
      }

      public bool Show {
         get { return _show; }
         set { _notifier.SetProperty(ref _show, value); }
      }

      public bool Horizontal {
         get { return _horizontal; }
         set { _notifier.SetProperty(ref _horizontal, value); }
      }

      public int Layers {
         get { return _layers; }
         set { _notifier.SetProperty(ref _layers, value); }
      }

      public bool Rotate {
         get { return _rotate; }
         set { _notifier.SetProperty(ref _rotate, value); }
      }

      /// <summary> inside padding </summary>
      public BoundDouble Padding{
         get {
            if (_padding == null)
               RecalcPadding(default(SizeDouble));
            return _padding;
         }
         set {
            if (value.LeftAndRight >= Size.Width)
               throw new ArgumentException("Padding size is very large. Should be less than Width.");
            if (value.TopAndBottom >= Size.Height)
               throw new ArgumentException("Padding size is very large. Should be less than Height.");
            var paddingNew = new BoundDouble(value.Left, value.Top, value.Right, value.Bottom);
            _notifier.SetProperty(ref _padding, paddingNew);
         }
      }
      private void RecalcPadding(SizeDouble old) {
         SizeDouble size = Size;
         var paddingNew = (_padding == null)
               ? new BoundDouble(size.Width / 2,
                                 size.Height / 2,
                                 _generalModel.Padding.Right,
                                 _generalModel.Padding.Bottom)
               : ImageModel.RecalcPadding(_padding, size, old);
         _notifier.SetProperty(ref _padding, paddingNew, nameof(this.Padding));
      }

      protected struct LineInfo {
         public Color clr;
         public double penWidht;
         public PointDouble from; // start coord
         public PointDouble to;   // end   coord
      }

      /// <summary> get paint information of drawing burger menu model image </summary>
      protected IEnumerable<LineInfo> Coords { get {
         if (!Show)
            return Enumerable.Empty<LineInfo>();

         bool horizontal = Horizontal;
         int layers = Layers;
         var pad = Padding;
         var rc = new RectDouble(pad.Left,
                                 pad.Top,
                                 Size.Width - pad.LeftAndRight,
                                 Size.Height - pad.TopAndBottom); ;
         double penWidth = Math.Max(1, (horizontal ? rc.Height : rc.Width) / (2.0 * layers));
         double rotateAngle = Rotate ? _generalModel.RotateAngle : 0;
         double stepAngle = 360.0 / layers;

         return Enumerable.Range(0, layers)
            .Select(layerNum => {
               double layerAlignmentAngle = ImageModel.FixAngle(layerNum * stepAngle + rotateAngle);
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
      } }

      public void Dispose() {
         if (_disposed)
            return;
         _disposed = true;

         _generalModel.PropertyChanged -= OnPropertyGeneralModelChanged;
         _notifier.Dispose();
         _generalModel = null;

         GC.SuppressFinalize(this);
      }

   }

}
