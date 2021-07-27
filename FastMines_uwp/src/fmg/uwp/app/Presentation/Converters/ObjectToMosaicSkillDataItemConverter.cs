using System;
using Windows.UI.Xaml.Data;
using Fmg.Uwp.App.Model.Items;

namespace Fmg.Uwp.App.Presentation.Converters {

    public class ObjectToMosaicSkillDataItemConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            return value;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            return value as MosaicSkillDataItem;
        }

    }

}
