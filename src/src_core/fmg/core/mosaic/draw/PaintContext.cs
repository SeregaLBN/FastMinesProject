using System;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.data.view.draw;
using fmg.common.notyfier;

namespace fmg.core.mosaic.draw {

   /// <summary>Information required for drawing the entire mosaic and cells</summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public class PaintContext<TImage> : NotifyPropertyChanged, IDisposable
      where TImage : class
   {
      private TImage _imgMine, _imgFlag;
      private ColorText _colorText;
      private PenBorder _penBorder;
      private FontInfo _fontInfo;
      private BoundDouble _padding;
      private Color _backgroundColor;
      private TImage _imgBckgrnd;

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется классом-наследником. </summary>
      public static Color DefaultBackgroundColor { get; protected set; } = Color.Gray;

      public PaintContext(bool iconicMode) {
         IconicMode = iconicMode;
         _backgroundColor = DefaultBackgroundColor.Darker(0.4);
      }

      public TImage ImgMine {
         get { return _imgMine; }
         set { SetProperty(ref _imgMine, value); }
      }
      public TImage ImgFlag {
         get { return _imgFlag; }
         set { SetProperty(ref _imgFlag, value); }
      }

      public ColorText ColorText {
         get {
            if (_colorText == null)
               ColorText = new ColorText();
            return _colorText;
         }
         set {
            var old = _colorText;
            if (!SetProperty(ref _colorText, value))
               return;
            if (old != null)
               old.PropertyChanged -= OnColorTextPropertyChanged;
            if (_colorText != null)
               _colorText.PropertyChanged += OnColorTextPropertyChanged;
         }
      }

      public PenBorder PenBorder {
         get {
            if (_penBorder == null)
               PenBorder = new PenBorder();
            return _penBorder;
         }
         set {
            var old = _penBorder;
            if (!SetProperty(ref _penBorder, value))
               return;
            if (old != null)
               old.PropertyChanged -= OnPenBorderPropertyChanged;
            if (_penBorder != null)
               _penBorder.PropertyChanged += OnPenBorderPropertyChanged;
         }
      }

      /// <summary> всё что относиться к заливке фоном ячееек </summary>
      public class BackgroundFill : NotifyPropertyChanged {
         /// <summary> режим заливки фона ячеек </summary>
         private int _mode = 0;

         /// <summary> кэшированные цвета фона ячеек </summary>
         private readonly IDictionary<int, Color> _colors = new Dictionary<int, Color>();

         /// <summary> режим заливки фона ячеек:
         /// 0 - цвет заливки фона по-умолчанию
         /// not 0 - радуга %)
         /// </summary>
         public int Mode {
            get { return _mode; }
            set {
               if (SetProperty(ref _mode, value))
                  _colors.Clear();
            }
         }

         /// <summary> кэшированные цвета фона ячеек
         /// Нет цвета? - создасться с нужной интенсивностью! */
         /// </summary>
         public Color GetColor(int index) {
            if (_colors.ContainsKey(index))
               return _colors[index];

            var res = ColorExt.RandomColor(Rand).Brighter(0.45);
            _colors.Add(index, res);
            return res;
         }
      }
      private static readonly Random Rand = new Random(Guid.NewGuid().GetHashCode());

      private BackgroundFill _backgroundFill;
      public BackgroundFill BkFill {
         get {
            if (_backgroundFill == null)
               BkFill = new BackgroundFill(); // call setter
            return _backgroundFill;
         }
         set {
            var old = _backgroundFill;
            if (!SetProperty(ref _backgroundFill, value))
               return;
            if (old != null)
               old.PropertyChanged -= OnBackgroundFillPropertyChanged;
            if (_backgroundFill != null)
               _backgroundFill.PropertyChanged += OnBackgroundFillPropertyChanged;
         }
      }

      public bool IconicMode { get; }

      public BoundDouble Padding {
         get { return _padding; }
         set { SetProperty(ref _padding, value); }
      }

      public FontInfo FontInfo {
         get { return _fontInfo; }
         set {
            var old = _fontInfo;
            if (!SetProperty(ref _fontInfo, value))
               return;
            if (old != null)
               old.PropertyChanged -= OnFontInfoPropertyChanged;
            if (_fontInfo != null)
               _fontInfo.PropertyChanged += OnFontInfoPropertyChanged;
         }
      }

      public Color BackgroundColor {
         get { return _backgroundColor; }
         set { SetProperty(ref _backgroundColor, value); }
      }

      public TImage ImgBckgrnd {
         get { return _imgBckgrnd; }
         set { SetProperty(ref _imgBckgrnd, value); }
      }

      private void OnBackgroundFillPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         OnPropertyChanged("BkFill");
         OnPropertyChanged("BkFill." + ev.PropertyName);
      }
      private void OnColorTextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         OnPropertyChanged("ColorText");
         OnPropertyChanged("ColorText." + ev.PropertyName);
      }
      private void OnPenBorderPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         OnPropertyChanged("PenBorder");
         OnPropertyChanged("PenBorder." + ev.PropertyName);
      }
      private void OnFontInfoPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         OnPropertyChanged("FontInfo");
         OnPropertyChanged("FontInfo." + ev.PropertyName);
      }

      public void Dispose() {
         // unsubscribe from
         BkFill = null;
         ColorText = null;
         PenBorder = null;
         FontInfo = null;
      }
   }

}
