package fmg.data.controller.event;

import java.util.EventListener;

public interface PlayerModelListener extends EventListener
{
    public void playerChanged(PlayerModelEvent e);
}

