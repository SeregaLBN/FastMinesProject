using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;
using Windows.Foundation;
using Windows.UI.Xaml.Media.Imaging;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic.wbmp {

   /// <summary>
   ///  http://stackoverflow.com/questions/5666772/how-can-i-render-text-on-a-writeablebitmap-on-a-background-thread-in-windows-ph
   /// </summary>
   internal static class BitmapFont {
      private const string DRAW_BMP_FONT_NAME = "NirmalaUI";
      private const int DRAW_BMP_FONT_SIZE = 30;

      private class FontInfo {
         public FontInfo(WriteableBitmap image, Dictionary<char, Rect> metrics, int size) {
            Image = image;
            Metrics = metrics;
            Size = size;
         }

         public WriteableBitmap Image { get; private set; }
         public Dictionary<char, Rect> Metrics { get; private set; }
         public int Size { get; private set; }
         public Rect FindMetrics(char c) {
            return Metrics.Where(p => p.Key == c)
               .DefaultIfEmpty(Metrics.Last()) // hm.. default value
               .First().Value;
         }
      }

      private static readonly Dictionary<string, List<FontInfo>> Fonts = new Dictionary<string, List<FontInfo>>();

      public static async Task RegisterFonts()
      {
         await RegisterFont(BitmapFont.DRAW_BMP_FONT_NAME, BitmapFont.DRAW_BMP_FONT_SIZE);
      }

      private static async Task RegisterFont(string name, params int[] sizes) {
         foreach (var size in sizes) {
            var fontFile = name + "_" + size + ".png";
            var fontMetricsFile = name + "_" + size + ".xml";

            var uri = new Uri("ms-appx:///res/Font/" + fontFile); // ms-appx:///res/Font/NirmalaUI_30.png
            var image = await ImgUtils.GetImage(uri);
            var metrics = XDocument.Load("./res/Font/" + fontMetricsFile);
            var dict = (from c in metrics.Root.Elements()
               let key = (char) ((int) c.Attribute("key"))
               let rect =
                  new Rect((int) c.Element("x"), (int) c.Element("y"), (int) c.Element("width"),
                     (int) c.Element("height"))
               select new {Char = key, Metrics = rect}).ToDictionary(x => x.Char, x => x.Metrics);

            var fontInfo = new FontInfo(image, dict, size);

            if (Fonts.ContainsKey(name))
               Fonts[name].Add(fontInfo);
            else
               Fonts.Add(name, new List<FontInfo> {fontInfo});
         }
      }

      private static FontInfo GetNearestFont(string fontName, int size) {
         var key= Fonts.Keys.FirstOrDefault(k => k == fontName);
         if (key == null)
            key = Fonts.Keys.First(k => k == DRAW_BMP_FONT_NAME);
         return Fonts[key].OrderBy(x => Math.Abs(x.Size - size)).First();
      }

      public static Size MeasureString(string text, string fontName, int size) {
         var font = GetNearestFont(fontName, size);

         var scale = (double) size/font.Size;

         //var letters = text.Select(x => font.Metrics[x]).ToArray();
         var letters = (from char c in text select font.Metrics[c]).ToArray();

         return new Size(letters.Sum(x => x.Width*scale), letters.Max(x => x.Height*scale));
      }

      public static void DrawString(this WriteableBitmap bmp, string text, Rect rcInto, string fontName, int size, Windows.UI.Color color) {
         var font = GetNearestFont(fontName, size);

         //var letters = text.Select(f => font.Metrics[f]).ToArray();
         var letters = (from char c in text select font.FindMetrics(c)).ToList();
         var txtW = letters.Sum(x => x.Width);
         var txtH = letters.Max(x => x.Height);

         var scale = Math.Min(rcInto.Width/txtW, rcInto.Height/txtH);

         var dstX = rcInto.Left + (rcInto.Width - txtW*scale)/2;
         var dstY = rcInto.Top + (rcInto.Height - txtH*scale)/2;

         var imgSrc = font.Image;
         var msk = new WriteableBitmap(imgSrc.PixelWidth, imgSrc.PixelHeight);
         msk.FillRectangle(0, 0, msk.PixelWidth, msk.PixelHeight, color);
         foreach (var letter in letters) {
            var rcDst = new Rect(dstX, dstY, letter.Width*scale, letter.Height*scale);
            //bmp.Blit(rcDst, imgSrc, letter, (Windows.UI.Color)Color.MAGENTA, WriteableBitmapExtensions.BlendMode.Alpha);
            //bmp.Blit(rcDst, imgSrc, letter, WriteableBitmapExtensions.BlendMode.Alpha);

            // see http://adamkinney.wordpress.com/2010/01/09/image-blitting-in-silverlight-with-writeablebitmapex/
            msk.Blit(letter, imgSrc, letter, WriteableBitmapExtensions.BlendMode.Mask);
            bmp.Blit(rcDst, msk, letter, WriteableBitmapExtensions.BlendMode.Alpha);
            dstX += rcDst.Width;
         }
      }
   }
}