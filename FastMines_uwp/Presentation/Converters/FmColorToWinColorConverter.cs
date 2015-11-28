using System;
using Windows.UI.Xaml.Data;
using fmg.common;

namespace FastMines.Presentation.Converters
{
   /// <summary> Convert 'fmg.common.Color' to 'Windows.UI.Color' </summary>
   public sealed class FmColorToWinColorConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return ((fmg.common.Color)value).ToWinColor();
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         return ((Windows.UI.Color)value).ToFmColor();
      }
   }
}
