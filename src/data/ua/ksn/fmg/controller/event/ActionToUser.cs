using System;

namespace ua.ksn.fmg.controller.Event {

   public interface ActionToUser {
      void applyToUser(Guid userId);
   }
}