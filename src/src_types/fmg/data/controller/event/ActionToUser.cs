using System;

namespace fmg.data.controller.Event {

   public interface ActionToUser {
      void applyToUser(Guid userId);
   }
}