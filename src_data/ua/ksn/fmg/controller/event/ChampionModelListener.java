package ua.ksn.fmg.controller.event;

import java.util.EventListener;

public interface ChampionModelListener extends EventListener
{
    public void championChanged(ChampionModelEvent e);
}

