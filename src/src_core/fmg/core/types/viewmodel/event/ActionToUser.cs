using System;

namespace fmg.core.types.viewmodel.Event {

   public interface ActionToUser {
      void applyToUser(Guid userId);
   }

}
