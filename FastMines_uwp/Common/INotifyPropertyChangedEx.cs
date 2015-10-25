using System.ComponentModel;

namespace FastMines.Common {
    // TODO http://stackoverflow.com/questions/7677854/notifypropertychanged-event-where-event-args-contain-the-old-value

    public interface INotifyPropertyChangedEx : INotifyPropertyChanged {
        event PropertyChangedExEventHandler PropertyChangedEx;
    }

    public delegate void PropertyChangedExEventHandler(object sender, PropertyChangedExEventArgs e);

    public class PropertyChangedExEventArgs : PropertyChangedEventArgs {
        public object OldValue { get; private set; }
        public object NewValue { get; private set; }

        public PropertyChangedExEventArgs(string propertyName, object oldValue, object newValue) : base(propertyName) {
            OldValue = oldValue;
            NewValue = newValue;
        }
    }
}