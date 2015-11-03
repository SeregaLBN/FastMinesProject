using System.ComponentModel;

namespace FastMines.Presentation.Notyfier
{
   public class PropertyChangedExEventArgs<T> : PropertyChangedEventArgs
   {
      public PropertyChangedExEventArgs(string propertyName, T newValue, T oldValue) :
         base(propertyName)
      {
         NewValue = newValue;
         OldValue = oldValue;
      }

      public T NewValue { get; }
      public T OldValue { get; }
   }
}
