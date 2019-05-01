using System;
using Windows.UI.Xaml.Data;
using fmg.common.geom;

namespace fmg.common.Converters {

    public sealed class SizeToHeightConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            //LoggerSimple.Put($"SizeToHeightConverter::Convert: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
            System.Diagnostics.Debug.Assert(typeof(double) == targetType);
            if (targetType != typeof(double))
                throw new NotImplementedException("Not supported...");
            var sizeD = value as SizeDouble?;
            if (sizeD != null)
                return sizeD.Value.Height;
            var sizeI = value as Size?;
            if (sizeI != null)
                return System.Convert.ToDouble(sizeI.Value.Height);
            var sizeM = value as Matrisize?;
            if (sizeM != null)
                return System.Convert.ToDouble(sizeM.Value.n);
            throw new NotImplementedException("Not supported...");
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            LoggerSimple.Put($"SizeToHeightConverter::ConvertBack: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
            throw new NotImplementedException("Not supported...");
        }

    }

}
