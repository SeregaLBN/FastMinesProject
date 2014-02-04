using System;
using System.Collections.Generic;
using System.Linq;
using System.Xml.Linq;
using Windows.Foundation;
using Windows.UI.Xaml.Media.Imaging;

namespace ua.ksn.fmg.view.win_rt.draw
{
   /// <summary>
   ///  http://stackoverflow.com/questions/5666772/how-can-i-render-text-on-a-writeablebitmap-on-a-background-thread-in-windows-ph
   /// </summary>
   public static class BitmapFont
   {
      private class FontInfo
      {
         public FontInfo(WriteableBitmap image, Dictionary<char, Rect> metrics, int size)
         {
            Image = image;
            Metrics = metrics;
            Size = size;
         }

         public WriteableBitmap Image { get; private set; }
         public Dictionary<char, Rect> Metrics { get; private set; }
         public int Size { get; private set; }
      }

      private static readonly Dictionary<string, List<FontInfo>> Fonts = new Dictionary<string, List<FontInfo>>();

      public static void RegisterFont(string name, params int[] sizes)
      {
         foreach (var size in sizes)
         {
            var fontFile = name + "_" + size + ".png";
            var fontMetricsFile = name + "_" + size + ".xml";

            var baseUri = new Uri("ms-appx:///");
            var image = new WriteableBitmap(1, 1).FromContent(new Uri(baseUri, fontFile)).Result;
            var metrics = XDocument.Load(fontMetricsFile);
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

      private static FontInfo GetNearestFont(string fontName, int size)
      {
         return Fonts[fontName].OrderBy(x => Math.Abs(x.Size - size)).First();
      }

      public static Size MeasureString(string text, string fontName, int size)
      {
         var font = GetNearestFont(fontName, size);

         var scale = (double) size/font.Size;

         //var letters = text.Select(x => font.Metrics[x]).ToArray();
         var letters = (from char c in text select font.Metrics[c]).ToArray();

         return new Size(letters.Sum(x => x.Width*scale), letters.Max(x => x.Height*scale));
      }

      public static void DrawString(this WriteableBitmap bmp, string text, int x, int y, string fontName, int size,
         Windows.UI.Color color)
      {
         var font = GetNearestFont(fontName, size);

         //var letters = text.Select(f => font.Metrics[f]).ToArray();
         var letters = from char c in text select font.Metrics[c];

         var scale = (double) size/font.Size;

         double destX = x;
         foreach (var letter in letters)
         {
            var destRect = new Rect(destX, y, letter.Width*scale, letter.Height*scale);
            bmp.Blit(destRect, font.Image, letter, color, WriteableBitmapExtensions.BlendMode.Alpha);
            destX += destRect.Width;
         }
      }
   }
}