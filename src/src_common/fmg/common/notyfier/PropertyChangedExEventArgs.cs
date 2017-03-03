using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier
{
   public interface IPropertyChangedExEventArgs<out T>
   {
      T NewValue { get; }
      T OldValue { get; }
   }

   public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs, IPropertyChangedExEventArgs<T>
   {
      public PropertyChangedExEventArgs(T newValue, T oldValue, [CallerMemberName] string propertyName = null) :
         base(propertyName)
      {
         NewValue = newValue;
         OldValue = oldValue;
      }

      public T NewValue { get; }
      public T OldValue { get; }
   }
}
