package fmg.core.app.model;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Players data model == all users and their statistics */
public class Players implements INotifyPropertyChanged, AutoCloseable {

    public static final String USER_NAME_UPDATED      = "UserNameUpdated";
    public static final String USER_DELETED           = "UserDeleted";
    public static final String USER_ADDED             = "UserAdded";
    public static final String USER_STATISTIC_CHANGED = UserStatisticChanged.class.getSimpleName();

    private List<Record> records = new ArrayList<>();
    private NotifyPropertyChanged notifier = new NotifyPropertyChanged(this, true);

    public static class Record {
        public final User user;
        public final Statistics statistics[][] = new Statistics[EMosaic.values().length][ESkillLevel.values().length-1];

        /** new User */
        public Record(User user) {
            this.user = user;
            for (EMosaic mosaic : EMosaic.values())
                for (ESkillLevel skill : ESkillLevel.values())
                    if (skill != ESkillLevel.eCustom)
                        statistics[mosaic.ordinal()][skill.ordinal()] = new Statistics();
        }

        @Override
        public String toString() {
            return user.getName();
        }

    }

    /** for event */
    public static class UserStatisticChanged {
        public final UUID userId;
        public final EMosaic mosaic;
        public final ESkillLevel skill;
        public UserStatisticChanged(UUID userId, EMosaic mosaic, ESkillLevel skill) {
            this.userId = userId;
            this.mosaic = mosaic;
            this.skill = skill;
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    public void removePlayer(UUID userId) {
        Record rec = find(userId);
        if (rec == null)
            throw new IllegalArgumentException("User with id=" + userId + " not exist");

        int pos = records.indexOf(rec);
        records.remove(pos);

        notifier.firePropertyChanged(pos, rec.user, USER_DELETED);
    }

    public boolean isExist(UUID userId) { return find(userId) != null; }

    public int size() { return records.size(); }

    public UUID addNewPlayer(String name, String pass) {
        if ((name == null) || name.isEmpty())
            throw new IllegalArgumentException("Invalid player name. Need not empty.");
        for (Record rec: records)
            if (rec.user.getName().equalsIgnoreCase(name))
                throw new IllegalArgumentException("Please enter a unique name");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setPassword(pass);
        records.add(new Record(user));
        notifier.firePropertyChanged(null, user, USER_ADDED);
        return user.getId();
    }

    public int indexOf(User user) {
        Record recFind = null;
        for (Record rec: records)
            if (rec.user.getId().equals(user.getId())) {
                recFind = rec;
                break;
            }
        if (recFind == null)
            return -1;
        return records.indexOf(recFind);
    }

    /**
     * Установить статистику для игрока
     * @param userId - идентификатор игрока
     * @param mosaic - на какой мозаике
     * @param skill - на каком уровне сложности
     * @param victory - выиграл ли?
     * @param countOpenField - кол-во открытых ячеек
     * @param playTime - время игры
     * @param clickCount - кол-во кликов
     */
    public void updateStatistic(UUID userId, EMosaic mosaic, ESkillLevel skill, boolean victory, long countOpenField, long playTime, long clickCount) {
        if (skill == ESkillLevel.eCustom)
            return;
        Record rec = find(userId);
        if (rec == null)
            throw new IllegalArgumentException("User with id=" + userId + " not exist");

        Statistics subRec = rec.statistics[mosaic.ordinal()][skill.ordinal()];
        subRec.gameNumber++;
        subRec.gameWin    += victory ? 1:0;
        subRec.openField  += countOpenField;
        if (victory) {
            subRec.playTime   += playTime;
            subRec.clickCount += clickCount;
        }

        notifier.firePropertyChanged(null, new UserStatisticChanged(userId, mosaic, skill), USER_STATISTIC_CHANGED);
    }

    private Record find(UUID userId) {
        if (userId != null)
            for (Record rec: records)
                if (rec.user.getId().equals(userId))
                    return rec;
        return null;
    }

    public int getUserCount() {
        return records.size();
    }

    public User getUser(int pos) {
        if ((pos < 0) || (pos >= records.size()))
            throw new IllegalArgumentException("Invalid position " + pos);

        return records.get(pos).user;
    }

    public User getUser(UUID userId) {
        Record rec = find(userId);
        if (rec == null)
            throw new IllegalArgumentException("User with id=" + userId + " not exist");

        return rec.user;
    }

    public Statistics getInfo(UUID userId, EMosaic mosaic, ESkillLevel skillLevel) {
        Record rec = find(userId);
        if (rec == null)
            throw new IllegalArgumentException("User with id=" + userId + " not exist");

        return rec.statistics[mosaic.ordinal()][skillLevel.ordinal()].getCopy();
    }

    public int getPos(UUID userId) {
        if (userId == null)
            return -1;

        Record rec = find(userId);
        if (rec == null)
            return -1;

        return records.indexOf(rec);
    }

    public void setUserName(int pos, String name) {
        User user = getUser(pos);
        user.setName(name);
        notifier.firePropertyChanged(null, user, USER_NAME_UPDATED);
    }


    @Override
    public void close() {
        notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

}
