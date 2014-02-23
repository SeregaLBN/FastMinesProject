using System;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.geom;
using ua.ksn.fmg.view.draw;

namespace ua.ksn.fmg.view.win_rt.draw {

   public class GraphicContext : FastMines.Common.BindableBase {
      private WriteableBitmap _imgMine, _imgFlag;
      private ColorText _colorText;
      private string _fontFamilyName;
      private FontStyle _fontStyle = FontStyle.Normal;
      private int _fontSize = 10;
      protected PenBorder _penBorder;

      private readonly bool _iconicMode;
      private readonly Size _bound;

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
         private IDictionary<int, Color> _colors;

         /// <summary> режим заливки фона €чеек:
         /// 0 - цвет заливки фона по-умолчанию
         /// not 0 - радуга %)
         /// </summary>
         public int Mode {
            get { return _mode; }
            set {
               this._mode = value;
               Colors.Clear();
            }
         }

         /// <summary> кэшированные цвета фона €чеек
         /// Ќет цвета? - создастьс€ с нужной интенсивностью! */
         /// </summary>
         public IDictionary<int, Color> Colors {
            get {
               if (_colors == null)
                  _colors = new Dictionary<int, Color>();
               return _colors;
            }
         }
         public Color getColor(int index) {
            if (_colors.ContainsKey(index))
               return _colors[index];

            int basic = 120; // от заданной границы светлости буду создавать новый цвет
            Random rand = new Random();
            int r = basic + rand.Next(0xFF - basic);
            int g = basic + rand.Next(0xFF - basic);
            int b = basic + rand.Next(0xFF - basic);
            var res = new Color((byte)r, (byte)g, (byte)b);
            Colors.Add(index, res);
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