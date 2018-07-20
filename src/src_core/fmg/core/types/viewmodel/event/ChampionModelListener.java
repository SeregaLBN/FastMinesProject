package fmg.core.types.viewmodel.event;

import java.util.EventListener;

public interface ChampionModelListener extends EventListener
{
    public void championChanged(ChampionModelEvent e);
}

