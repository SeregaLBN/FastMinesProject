using System;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.data.view.draw;

namespace fmg.winrt.draw {

   public class GraphicContext : FastMines.Common.BindableBase {
      private WriteableBitmap _imgMine, _imgFlag;
      private ColorText _colorText;
      private string _fontFamilyName;
      protected PenBorder _penBorder;
      private FontStyle _fontStyle = FontStyle.Normal;
      private int _fontSize = 10;

      private readonly bool _iconicMode;
      private readonly Size _bound;

      private static readonly Random Rand = new Random();

      public GraphicContext(bool iconicMode, Size bound) {
         this._iconicMode = iconicMode;
         _bound = bound;
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
      public Size Bound { get { return _bound; } }

      public string FontFamilyName {
         get {
            if (string.IsNullOrWhiteSpace(_fontFamilyName))
               _fontFamilyName = "SansSerif";
            return _fontFamilyName;
         }
         set { this.SetProperty(ref this._fontFamilyName, value); }
      }
      public FontStyle FontStyle {
         get {
            return _fontStyle;
         }
         set { this.SetProperty(ref this._fontStyle, value); }
      }
      public int FontSize {
         get {
            return _fontSize;
         }
         set { this.SetProperty(ref this._fontSize, value); }
      }
   }
}