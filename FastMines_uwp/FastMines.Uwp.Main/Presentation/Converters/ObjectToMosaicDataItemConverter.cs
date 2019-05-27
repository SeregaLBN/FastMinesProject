using Fmg.DataModel.Items;
using System;
using Windows.UI.Xaml.Data;

namespace Fmg.Common.Converters {

    public class ObjectToMosaicDataItemConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            return value;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            return value as MosaicDataItem;
        }

    }

}
