using Fmg.Common.Geom;
using System;
using Windows.UI.Xaml.Data;

namespace Fmg.Common.Converters {

    public sealed class SizeToWidthConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            //LoggerSimple.Put($"SizeToWidthConverter::Convert: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
            System.Diagnostics.Debug.Assert(typeof(double) == targetType);
            if (targetType != typeof(double))
                throw new NotImplementedException("Not supported...");
            var sizeD = value as SizeDouble?;
            if (sizeD != null)
                return sizeD.Value.Width;
            var sizeI = value as Size?;
            if (sizeI != null)
                return System.Convert.ToDouble(sizeI.Value.Width);
            var sizeM = value as Matrisize?;
            if (sizeM != null)
                return System.Convert.ToDouble(sizeM.Value.m);
            throw new NotImplementedException("Not supported...");
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            LoggerSimple.Put($"SizeToWidthConverter::ConvertBack: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
            throw new NotImplementedException("Not supported...");
        }

    }

}
