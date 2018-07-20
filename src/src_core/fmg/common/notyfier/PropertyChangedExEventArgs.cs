using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier {

   public interface IPropertyChangedExEventArgs<out T> {

      T OldValue { get; }
      T NewValue { get; }

   }

   public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs, IPropertyChangedExEventArgs<T> {

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
