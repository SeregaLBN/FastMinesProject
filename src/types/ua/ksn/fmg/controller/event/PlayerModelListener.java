package ua.ksn.fmg.controller.event;

import java.util.EventListener;

public interface PlayerModelListener extends EventListener
{
    public void playerChanged(PlayerModelEvent e);
}

