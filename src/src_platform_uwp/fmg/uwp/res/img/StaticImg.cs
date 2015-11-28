using System;
using System.ComponentModel;
using Windows.UI.Core;
using FastMines.Common;
using fmg.common;
using FastMines.Presentation.Notyfier;
using Point = Windows.Foundation.Point;

namespace fmg.uwp.res.img
{
   public abstract class StaticImg<T, TImage> : NotifyPropertyChanged
      where TImage : class
   {
      public static readonly Windows.UI.Color DefaultBkColor = Resources.DefaultBkColor;
      public const int DefaultImageSize = 100;

      protected StaticImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
      {
         Entity = entity;
         _size = widthAndHeight;
         if (!padding.HasValue)
            _padding = (int)(widthAndHeight * 0.05); // 5%

#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         AsyncRunner.InvokeLater(MakeCoords, CoreDispatcherPriority.High); // call async virtual method
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
      }

      private int _size;
      /// <summary> width and height in pixel </summary>
      public int Size
      {
         get { return _size; }
         set
         {
            if (SetProperty(ref _size, value))
            {
               _image = null;
               MakeCoords();
            }
         }
      }

      /// <summary> width image </summary>
      public int Width => Size;
      /// <summary> height image </summary>
      public int Height => Size;

      private int _padding;
      /// <summary> inside padding </summary>
      public int Padding
      {
         get { return _padding; }
         set
         {
            if (value*2 >= Size)
               throw new ArgumentException("Padding size is very large. Should be less than Size / 2.");
            if (SetProperty(ref _padding, value))
            {
               _image = null;
               MakeCoords();
            }
         }
      }

      public T Entity { get; private set; }
      protected Point[] _points;

      private TImage _image;
      public TImage Image { get { return _image; } protected set { SetPropertyForce(ref _image, value); } }

      private Color _bkColor = DefaultBkColor.ToFmColor();
      /// <summary> background fill color </summary>
      public Color BkColor
      {
         get { return _bkColor; }
         set
         {
            if (SetProperty(ref _bkColor, value))
               Draw();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor
      {
         get { return _borderColor; }
         set
         {
            if (SetProperty(ref _borderColor, value))
               Draw();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth
      {
         get { return _borderWidth; }
         set
         {
            if (SetProperty(ref _borderWidth, value))
               Draw();
         }
      }

      protected double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double RotateAngle
      {
         get { return _rotateAngle; }
         set
         {
            if (SetProperty(ref _rotateAngle, value))
               Draw();
         }
      }

      protected Color _fillColor = Color.Aqua;
      public Color FillColor
      {
         get { return _fillColor; }
         set
         {
            if (SetProperty(ref _fillColor, value)) {
               //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>("FillColorAttenuate", ..., ...));
               OnPropertyChanged(this, new PropertyChangedEventArgs("FillColorAttenuate"));
               Draw();
            }
         }
      }

      public Color FillColorAttenuate => FillColor.Attenuate(160);

      protected virtual void MakeCoords()
      {
         // ... see child class
         Draw();
      }

      protected bool _scheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      protected void Draw()
      {
          if (_scheduledDraw)
            return;
         _scheduledDraw = true;
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         AsyncRunner.InvokeLater(DrawSync, CoreDispatcherPriority.Low);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
      }

      protected virtual void DrawSync() {
         DrawBegin();
         DrawBody();
         DrawEnd();
      }

      protected virtual void DrawBegin() {
         //LoggerSimple.Put(" DrawSync: " + MosaicGroup + ": " + BkColor);
         _scheduledDraw = false;
      }

      protected abstract void DrawBody();

      protected virtual void DrawEnd() {}

   }
}