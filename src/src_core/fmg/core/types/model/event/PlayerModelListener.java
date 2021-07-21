package fmg.core.types.viewmodel.event;

import java.util.EventListener;

public interface PlayerModelListener extends EventListener {

    public void playerChanged(PlayerModelEvent e);

}

