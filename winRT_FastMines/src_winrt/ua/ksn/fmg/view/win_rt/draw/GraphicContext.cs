using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.geom;
using ua.ksn.fmg.view.draw;

namespace ua.ksn.fmg.view.win_rt.draw {

   public class GraphicContext : FastMines.Common.BindableBase {
      private WriteableBitmap imgMine, imgFlag;
      private ColorText colorText;
      protected PenBorder penBorder;
      private readonly bool iconicMode;
      private readonly Size _bound;

      public GraphicContext(bool iconicMode, Size bound) {
         this.iconicMode = iconicMode;
         _bound = bound;
      }

      public WriteableBitmap ImgMine {
         get { return this.imgMine; }
         set { this.SetProperty(ref this.imgMine, value); }
      }
      public WriteableBitmap ImgFlag {
         get { return this.imgFlag; }
         set { this.SetProperty(ref this.imgFlag, value); }
      }

      public ColorText ColorText {
         get {
            if (colorText == null)
               ColorText = new ColorText();
            return colorText;
         }
         set {
            this.SetProperty(ref this.colorText, value);
         }
      }

      public PenBorder PenBorder {
         get {
            if (penBorder == null)
               PenBorder = new PenBorder();
            return penBorder;
         }
         set {
            this.SetProperty(ref this.penBorder, value);
         }
      }

      /// <summary> всЄ что относитьс€ к заливке фоном €чееек </summary>
      public class BackgroundFill {
         /// <summary> режим заливки фона €чеек </summary>
         private int mode = 0;

         /// <summary> кэшированные цвета фона €чеек </summary>
         private IDictionary<int, Color> colors;

         /// <summary> режим заливки фона €чеек:
         /// 0 - цвет заливки фона по-умолчанию
         /// not 0 - радуга %)
         /// </summary>
         public int Mode {
            get { return mode; }
            set {
               this.mode = value;
               Colors.Clear();
            }
         }

         /// <summary> кэшированные цвета фона €чеек
         /// Ќет цвета? - создастьс€ с нужной интенсивностью! */
         /// </summary>
         public IDictionary<int, Color> Colors {
            get {
               if (colors == null)
                  colors = new Dictionary<int, Color>();
               return colors;
            }
         }
         public Color getColor(int index) {
            if (colors.ContainsKey(index))
               return colors[index];

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

      public bool IconicMode { get { return iconicMode; } }
      public Size Bound { get { return _bound; } }
   }
}