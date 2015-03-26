package ua.ksn.fmg.controller.event;

import java.util.EventListener;
import java.util.UUID;

public interface ActionToUser extends EventListener {

	void applyToUser(UUID userId);
}
