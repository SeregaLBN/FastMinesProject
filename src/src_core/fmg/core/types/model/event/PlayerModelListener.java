package fmg.core.types.model.event;

import java.util.EventListener;

public interface PlayerModelListener extends EventListener {

    public void playerChanged(PlayerModelEvent e);

}

