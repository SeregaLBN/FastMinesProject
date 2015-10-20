using System;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.data.view.draw;

namespace fmg.winrt.draw {

   public class GraphicContext : FastMines.Common.BindableBase {
      public const string    DEFAULT_FONT_NAME  = "Arial"; // Times New Roman // Verdana // Courier New // SansSerif
      public const FontStyle DEFAULT_FONT_STYLE = FontStyle.Normal;
      public const int       DEFAULT_FONT_SIZE  = 10;

      private WriteableBitmap _imgMine, _imgFlag;
      private ColorText _colorText;
      protected PenBorder _penBorder;
      private FontFamily _fontFamily = new FontFamily(DEFAULT_FONT_NAME);
      private FontStyle _fontStyle = DEFAULT_FONT_STYLE;
      private int _fontSize = DEFAULT_FONT_SIZE;

      private readonly bool _iconicMode;
      private Size _bound;

      private static readonly Random Rand = new Random();

      public GraphicContext(bool iconicMode) {
         this._iconicMode = iconicMode;
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

      /// <summary> всЄ что относитьс€ к заливке фоном €чееек </summary>
      public class BackgroundFill {
         /// <summary> режим заливки фона €чеек </summary>
         private int _mode = 0;

         /// <summary> кэшированные цвета фона €чеек </summary>
         private readonly IDictionary<int, Color> _colors = new Dictionary<int, Color>();

         /// <summary> режим заливки фона €чеек:
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

         /// <summary> кэшированные цвета фона €чеек
         /// Ќет цвета? - создастьс€ с нужной интенсивностью! */
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

      public bool IconicMode { get { return _iconicMode; } }

      public Size Bound {
         get { return _bound; }
         set { this.SetProperty(ref this._bound, value); }
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
   }
}