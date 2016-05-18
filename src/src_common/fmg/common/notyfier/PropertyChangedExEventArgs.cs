using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace FastMines.Presentation.Notyfier
{
   public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs
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
