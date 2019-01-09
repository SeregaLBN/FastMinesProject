using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier {

    public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs {

        public PropertyChangedExEventArgs(T oldValue, T newValue, [CallerMemberName] string propertyName = null) :
            base(propertyName)
        {
            OldValue = oldValue;
            NewValue = newValue;
        }

        public T OldValue { get; }
        public T NewValue { get; }

    }

}
