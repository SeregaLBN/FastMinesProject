using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> MVC: model. Animated image characteristics </summary>
   public sealed class AnimatedInnerModel : IAnimatedModel {

      /// <summary> Image is animated? </summary>
      private bool _animated;
      /// <summary> Overall animation period (in milliseconds) </summary>
      private long _animatePeriod = 3000;
      /// <summary> Total frames of the animated period </summary>
      private int _totalFrames = 30;
      private int _currentFrame = 0;

      public event PropertyChangedEventHandler PropertyChanged;
      private readonly NotifyPropertyChanged _notifier;

      #region: begin unusable code
      public SizeDouble Size {
         get { throw new NotImplementedException(); }
         set { throw new NotImplementedException(); }
      }
      #endregion: end unusable code

      public AnimatedInnerModel() {
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
      }

      public bool Animated {
         get { return _animated; }
         set { _notifier.SetProperty(ref _animated, value); }
      }

      /// <summary> Overall animation period (in milliseconds) </summary>
      public long AnimatePeriod {
         get { return _animatePeriod; }
         set { _notifier.SetProperty(ref _animatePeriod, value); }
      }

      /// <summary> Total frames of the animated period </summary>
      public int TotalFrames {
         get { return _totalFrames; }
         set {
            if (_notifier.SetProperty(ref _totalFrames, value))
               CurrentFrame = 0;
         }
      }

      public int CurrentFrame {
         get { return _currentFrame; }
         set { _notifier.SetProperty(ref _currentFrame, value); }
      }

      /// <summary> off notifer </summary>
      public IDisposable Hold() {
          return _notifier.Hold();
      }

      public void Dispose() {
         _notifier.Dispose();
         GC.SuppressFinalize(this);
      }

   }

}
