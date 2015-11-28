using System;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using fmg.common;

namespace FastMines.Presentation.Converters
{
   /// <summary> Convert 'fmg.common.Color' to 'Windows.UI.Xaml.Media.Brush' </summary>
   public sealed class FmColorToBrushConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return new SolidColorBrush(((fmg.common.Color)value).ToWinColor());
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         var brush = value as SolidColorBrush;
         if (brush == null)
            throw new NotImplementedException("Unknown brush type: " + value.GetType().Name); // return null;
         return brush.Color.ToFmColor();
      }
   }
}
