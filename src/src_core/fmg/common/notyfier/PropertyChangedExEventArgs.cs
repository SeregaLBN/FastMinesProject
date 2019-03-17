using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier {

    public interface IPropertyValuesComparable {

        PropertyChangedEventArgs CombineValues(PropertyChangedEventArgs old);
        bool IsValuesEqual();

    }

    public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs, IPropertyValuesComparable {

        public PropertyChangedExEventArgs(T oldValue, T newValue, [CallerMemberName] string propertyName = null) :
            base(propertyName)
        {
            OldValue = oldValue;
            NewValue = newValue;
        }

        public T OldValue { get; }
        public T NewValue { get; }


        public PropertyChangedEventArgs CombineValues(PropertyChangedEventArgs old) {
            if (!(old is PropertyChangedExEventArgs<T> oldEx))
                return null;
            return new PropertyChangedExEventArgs<T>(oldEx.OldValue, this.NewValue, this.PropertyName);
        }

        public bool IsValuesEqual() {
            if (OldValue == null)
                return false;
            return OldValue.Equals(NewValue);
        }

        public override string ToString() {
            return string.Format($"{GetType().Name}=[PropertyName={PropertyName}; type={typeof(T).Name}; OldValue={OldValue}; NewValue={NewValue}]");
        }

    }

}
