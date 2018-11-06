using System;
using Windows.UI.Xaml.Data;
using fmg.common.geom;

namespace fmg.common.Converters {

    public sealed class SizeToHeightConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            //LoggerSimple.Put($"SizeToHeightConverter::Convert: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
            var size = value as Size?;
            if (size != null)
                return System.Convert.ToDouble(size.Value.Height);
            var sizeD = value as SizeDouble?;
            if (sizeD != null)
                return sizeD.Value.Height;
            var sizeM = value as Matrisize?;
            if (sizeM != null)
                return System.Convert.ToDouble(sizeM.Value.n);
            throw new NotImplementedException("Not supported...");
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            throw new NotImplementedException("Not supported...");
        }

    }

}
