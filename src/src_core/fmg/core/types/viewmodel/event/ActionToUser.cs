using System;

namespace Fmg.Core.Types.Viewmodel.Event {

    public interface ActionToUser {

        void ApplyToUser(Guid userId);

    }

}
