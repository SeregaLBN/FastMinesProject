using System;
using System.ComponentModel;
using Windows.UI.Core;
using FastMines.Common;
using fmg.common;
using fmg.common.geom;
using FastMines.Presentation.Notyfier;

namespace fmg.uwp.res.img
{
   public abstract class StaticImg<T, TImage> : NotifyPropertyChanged
      where TImage : class
   {
      public static readonly Windows.UI.Color DefaultBkColor = Resources.DefaultBkColor;
      public const int DefaultImageSize = 100;

      protected StaticImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null) {
         Entity = entity;
         _size = widthAndHeight;
         if (!padding.HasValue)
            _padding = (int)(widthAndHeight * 0.05); // 5%
      }

      private int _size;
      /// <summary> width and height in pixel </summary>
      public int Size {
         get { return _size; }
         set {
            if (SetProperty(ref _size, value)) {
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
      public int Padding {
         get { return _padding; }
         set {
            if (value * 2 >= Size)
               throw new ArgumentException("Padding size is very large. Should be less than Size / 2.");
            if (SetProperty(ref _padding, value)) {
               _image = null;
               MakeCoords();
            }
         }
      }

      public T Entity { get; private set; }
      protected PointDouble[] _points;

      private TImage _image;
      public TImage Image {
         get {
            //if (_image == null)
            //   DrawAsync();
            return _image;
         }
         protected set {
            SetPropertyForce(ref _image, value);
         }
      }

      private Color _bkColor = DefaultBkColor.ToFmColor();
      /// <summary> background fill color </summary>
      public Color BkColor {
         get { return _bkColor; }
         set {
            if (SetProperty(ref _bkColor, value))
               DrawAsync();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor {
         get { return _borderColor; }
         set {
            if (SetProperty(ref _borderColor, value))
               DrawAsync();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth {
         get { return _borderWidth; }
         set {
            if (SetProperty(ref _borderWidth, value))
               DrawAsync();
         }
      }

      protected double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set {
            if (SetProperty(ref _rotateAngle, value))
               DrawAsync();
         }
      }

      protected Color _fillColor = Color.Aqua;
      public Color FillColor {
         get { return _fillColor; }
         set {
            if (SetProperty(ref _fillColor, value)) {
               //OnPropertyChanged(this, new PropertyChangedExEventArgs<Color>("FillColorAttenuate", ..., ...));
               OnPropertyChanged(this, new PropertyChangedEventArgs("FillColorAttenuate"));
               DrawAsync();
            }
         }
      }

      public Color FillColorAttenuate => FillColor.Attenuate(160);

      private bool _coordinateMaked;
      protected virtual void MakeCoords() {
         // ... see child class
         _coordinateMaked = true;
         DrawAsync();
      }

      protected bool _scheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      protected void DrawAsync() {
         //if (Entity is data.controller.types.ESkillLevel) {
         //   var skill = (data.controller.types.ESkillLevel)(object)Entity;
         //   if (skill == data.controller.types.ESkillLevel.eBeginner) {
         //      System.Diagnostics.Debug.WriteLine("DrawAsync: " + skill);
         //   }
         //}

         if (_scheduledDraw)
            return;

         _scheduledDraw = true;
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         AsyncRunner.InvokeLater(DrawSync, CoreDispatcherPriority.Low);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
      }

      protected virtual void DrawSync() {
         if (!_coordinateMaked) {
            //LoggerSimple.Put("> DrawSync: MakeCoords: {0}", Entity);
            MakeCoords();
         } else {
            //LoggerSimple.Put("> DrawSync: {0}",  Entity);
            DrawBegin();
            DrawBody();
            DrawEnd();
         }
      }

      protected virtual void DrawBegin() {
         //LoggerSimple.Put(" DrawSync: " + MosaicGroup + ": " + BkColor);
         _scheduledDraw = false;
      }

      protected abstract void DrawBody();

      protected virtual void DrawEnd() { }

   }
}