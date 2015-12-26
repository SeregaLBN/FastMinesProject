using System;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.data.view.draw;
using FastMines.Presentation.Notyfier;

namespace fmg.uwp.draw {

   public class GraphicContext : NotifyPropertyChanged {
      public const string    DEFAULT_FONT_NAME  = "Arial"; // Times New Roman // Verdana // Courier New // SansSerif
      public const FontStyle DEFAULT_FONT_STYLE = FontStyle.Normal;
      public const int       DEFAULT_FONT_SIZE  = 10;

      private WriteableBitmap _imgMine, _imgFlag;
      private ColorText _colorText;
      protected PenBorder _penBorder;
      private FontFamily _fontFamily = new FontFamily(DEFAULT_FONT_NAME);
      private FontStyle _fontStyle = DEFAULT_FONT_STYLE;
      private int _fontSize = DEFAULT_FONT_SIZE;

      private Bound _padding;

      private static readonly Random Rand = new Random(Guid.NewGuid().GetHashCode());

      public GraphicContext(bool iconicMode) {
         this.IconicMode = iconicMode;
      }

      public WriteableBitmap ImgMine {
         get { return this._imgMine; }
         set { this.SetProperty(ref this._imgMine, value); }
      }
      public WriteableBitmap ImgFlag {
         get { return this._imgFlag; }
         set { this.SetProperty(ref this._imgFlag, value); }
      }

      public ColorText ColorText {
         get {
            if (_colorText == null)
               ColorText = new ColorText();
            return _colorText;
         }
         set {
            this.SetProperty(ref this._colorText, value);
         }
      }

      public PenBorder PenBorder {
         get {
            if (_penBorder == null)
               PenBorder = new PenBorder();
            return _penBorder;
         }
         set {
            this.SetProperty(ref this._penBorder, value);
         }
      }

      /// <summary> всё что относиться к заливке фоном ячееек </summary>
      public class BackgroundFill {
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
               this._mode = value;
               _colors.Clear();
            }
         }

         /// <summary> кэшированные цвета фона ячеек
         /// Нет цвета? - создасться с нужной интенсивностью! */
         /// </summary>
         public Color GetColor(int index) {
            if (_colors.ContainsKey(index))
               return _colors[index];

            var res = ColorExt.RandomColor(Rand).Attenuate();
            _colors.Add(index, res);
            return res;
         }
      }

      private BackgroundFill _backgroundFill;
      public BackgroundFill BkFill {
         get {
            if (_backgroundFill == null)
               _backgroundFill = new BackgroundFill();
            return _backgroundFill;
         }
      }

      public bool IconicMode { get; }

      public Bound Padding {
         get { return _padding; }
         set { this.SetProperty(ref this._padding, value); }
      }

      public FontFamily FontFamily {
         get { return _fontFamily; }
         set { this.SetProperty(ref this._fontFamily, value); }
      }
      public FontStyle FontStyle {
         get { return _fontStyle; }
         set { this.SetProperty(ref this._fontStyle, value); }
      }
      public int FontSize {
         get { return _fontSize; }
         set { this.SetProperty(ref this._fontSize, value); }
      }

      /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера </summary>
      public static Color DefaultBackgroundFillColor { get; }
      public static Color DefaultBackgroundWindowColor { get; }

      static GraphicContext() {
         try {
            var uiSettings = new UISettings();

            Color clrBtn, clrWin;
            try {
               // desktop
               clrBtn = uiSettings.UIElementColor(UIElementType.ButtonFace).ToFmColor();
               clrWin = uiSettings.UIElementColor(UIElementType.Window).ToFmColor();
            } catch (ArgumentException) {
               try {
                  // mobile
                  clrBtn = uiSettings.UIElementColor(1000 + UIElementType.ButtonFace).ToFmColor();
                  clrWin = uiSettings.UIElementColor(1000 + UIElementType.Window).ToFmColor();
               } catch (Exception) {
                  clrBtn = clrWin = Color.Gray; // wtf??
               }
            }
            DefaultBackgroundFillColor = clrBtn;
            DefaultBackgroundWindowColor = clrWin;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Fail(ex.Message);
         }
      }

   }
}