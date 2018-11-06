package fmg.core.types.viewmodel.event;

import java.util.EventListener;
import java.util.UUID;

public interface ActionToUser extends EventListener {

    void applyToUser(UUID userId);

}
