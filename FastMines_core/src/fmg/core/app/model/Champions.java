package fmg.core.app.model;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Champions data model */
public class Champions {

    private static final int MAX_SIZE = 10;
    public static final String CHAMPION_ADDED = ChampionAdded.class.getSimpleName();
    public static final String CHAMPION_RENAMED = "ChampionRenamed";

    @SuppressWarnings("unchecked")
    private List<Record>[][] records = new List[EMosaic.values().length][ESkillLevel.values().length - 1];

    private Consumer<PropertyChangeEvent> changedCallback;

    public static class Record implements Comparable<Record> {
        public UUID userId;
        public String userName;
        public long playTime = Long.MAX_VALUE;
        public int clicks;
        public Date date;

        public Record() {}

        public Record(User user, long playTime, int clicks) {
            this.userId = user.getId();
            this.userName = user.getName();
            this.playTime = playTime;
            this.clicks = clicks;
            this.date = new Date();
        }

        @Override
        public String toString() {
            return userName;
        }

        @Override
        public int compareTo(Record o) {
            int res = Long.compare(this.playTime, o.playTime);
            if (res == 0)
                res = Integer.compare(this.clicks, o.clicks);
            return res;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clicks, date, playTime, userId, userName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Record other = (Record)obj;
            return (clicks == other.clicks)
                && Objects.equals(date, other.date)
                && (playTime == other.playTime)
                && Objects.equals(userId, other.userId)
                && Objects.equals(userName, other.userName);
        }

    }

    /** for event */
    public static class ChampionAdded {
        public final UUID userId;
        public final EMosaic mosaic;
        public final ESkillLevel skill;
        public final int pos;

        public ChampionAdded(UUID userId, EMosaic mosaic, ESkillLevel skill, int pos) {
            this.userId = userId;
            this.mosaic = mosaic;
            this.skill = skill;
            this.pos = pos;
        }
    }

    public Champions() {
        for (EMosaic mosaic : EMosaic.values())
            for (ESkillLevel eSkill : ESkillLevel.values())
                if (eSkill != ESkillLevel.eCustom)
                    records[mosaic.ordinal()][eSkill.ordinal()] = new ArrayList<>(MAX_SIZE);
    }

    public List<Record>[][] getRecords() {
        return records;
    }

    public int add(User user, long playTime, EMosaic mosaic, ESkillLevel eSkill, int clickCount) {
        if (eSkill == ESkillLevel.eCustom)
            return -1;

        List<Record> list = records[mosaic.ordinal()][eSkill.ordinal()];
        Record newRecord = new Record(user, playTime, clickCount);
        list.add(newRecord);

        Collections.sort(list);

        int pos = list.indexOf(newRecord);
        if (pos < 0)
            throw new RuntimeException("Where??");

        if (list.size() > MAX_SIZE)
            //list = list.subList(0, MAX_SIZE-1);
            list.subList(MAX_SIZE, list.size()).clear();

        if (pos < MAX_SIZE) {
            firePropertyChanged(null, new ChampionAdded(user.getId(), mosaic, eSkill, pos), CHAMPION_ADDED);
            return pos;
        }

        return -1;
    }

    public Record getUserRecord(int index, EMosaic mosaic, ESkillLevel eSkill) {
        if (eSkill == ESkillLevel.eCustom)
            throw new IllegalArgumentException("Invalid input data - " + eSkill);

        return records[mosaic.ordinal()][eSkill.ordinal()].get(index);
    }

    public int getUsersCount(EMosaic mosaic, ESkillLevel eSkill) {
        if (eSkill == ESkillLevel.eCustom)
            throw new IllegalArgumentException("Invalid input data - " + eSkill);

        return records[mosaic.ordinal()][eSkill.ordinal()].size();
    }

    /** Найдёт позицию лучшего результата указанного пользователя */
    public int getPos(UUID userId, EMosaic mosaic, ESkillLevel eSkill) {
        if (userId == null)
            return -1;
        if (eSkill == ESkillLevel.eCustom)
            throw new IllegalArgumentException("Invalid input data - " + eSkill);

        List<Record> list = records[mosaic.ordinal()][eSkill.ordinal()];
        int pos = 0;
        for (Record record : list) {
            if (record.userId.equals(userId))
                return pos;
            pos++;
        }
        return -1;
    }

    public void onPlayersPropertyChanged(PropertyChangeEvent ev) {
        if (!ev.getPropertyName().equals(Players.USER_NAME_UPDATED))
            return;

        // было переименование пользователя...
        // в этом случае переименовываю его имя и в чемпионах
        Players players = (Players)ev.getSource();
        User user = players.getUser((UUID)ev.getNewValue());
        for (EMosaic mosaic : EMosaic.values())
            for (ESkillLevel eSkill : ESkillLevel.values())
                if (eSkill != ESkillLevel.eCustom) {
                    List<Record> list = records[mosaic.ordinal()][eSkill.ordinal()];
                    boolean isChanged = false;
                    for (Record rec : list)
                        if ((user.getId() == rec.userId) && !user.getName().equals(rec.userName)) {
                            rec.userName = user.getName();
                            isChanged = true;
                        }
                    if (isChanged)
                        firePropertyChanged(null, user.getId(), CHAMPION_RENAMED);
                }
    }

    private void firePropertyChanged(Object oldValue, Object newValue, String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }

    public void setListener(Consumer<PropertyChangeEvent> callback) {
        if (callback == null) {
            // unset
            changedCallback = null;
        } else {
            // set
            if (changedCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedCallback = callback;
        }
    }

}
