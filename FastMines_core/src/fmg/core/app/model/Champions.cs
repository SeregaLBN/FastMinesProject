using System;
using System.Collections.Generic;
using System.ComponentModel;
using Fmg.Common.Notifier;

namespace Fmg.Core.Types.Model {

    /// <summary>Champions data model</summary>
    public abstract class Champions : INotifyPropertyChanged, IDisposable {

        private const int MAX_SIZE = 10;
        public const string CHAMPION_ADDED = nameof(ChampionAdded);
        public const string CHAMPION_RENAMED = "ChampionRenamed";

        private List<Record>[,] Records { get; } = new List<Record>[EMosaicEx.GetValues().Length, ESkillLevelEx.GetValues().Length - 1];
        private Action unsubscriber;

        public event PropertyChangedEventHandler PropertyChanged {
            add    { notifier.PropertyChanged += value; }
            remove { notifier.PropertyChanged -= value; }
        }
        private readonly NotifyPropertyChanged notifier;
        private bool Disposed { get; set; }


        public class Record : IComparable<Record> {
            public Guid userId;
            public string userName;
            public long playTime = long.MaxValue;

            public Record() {}
            public Record(User user, long playTime) {
                this.userId = user.Id;
                this.userName = user.Name;
                this.playTime  = playTime;
            }

            public override string ToString() {
                return userName;
            }

            public int CompareTo(Record o) {
                long thisVal = this.playTime;
                long anotherVal = o.playTime;
                return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
            }
        }

        /// <summary> for event </summary>
        public class ChampionAdded {
            public Guid UserId { get; }
            public EMosaic Mosaic { get; }
            public ESkillLevel Skill { get; }
            public int Pos { get; }
            public ChampionAdded(Guid userId, EMosaic mosaic, ESkillLevel skill, int pos) {
                UserId = userId;
                Mosaic = mosaic;
                Skill = skill;
                Pos = pos;
            }
        }

        public Champions() {
            notifier = new NotifyPropertyChanged(this, true);
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom)
                        Records[mosaic.Ordinal(), eSkill.Ordinal()] = new List<Record>(MAX_SIZE);
        }

        public void SubscribeTo(Players players) {
            if (unsubscriber != null) {
                unsubscriber();
                unsubscriber = null;
            }

            if (players != null) {
                players.PropertyChanged += OnPlayersPropertyChanged;
                unsubscriber = () => players.PropertyChanged -= OnPlayersPropertyChanged;
            }
        }

        public int Add(User user, long playTime, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                return -1;

            List<Record> list = Records[mosaic.Ordinal(), eSkill.Ordinal()];
            Record newRecord = new Record(user, playTime);
            list.Add(newRecord);

            list.Sort();

            int pos = list.IndexOf(newRecord);
            if (pos == -1)
                throw new Exception("Where??");

            if (list.Count > MAX_SIZE)
                list.RemoveRange(MAX_SIZE, list.Count - MAX_SIZE);

            if (pos < MAX_SIZE) {
                notifier.FirePropertyChanged(null, new ChampionAdded(user.Id, mosaic, eSkill, pos), CHAMPION_ADDED);
                return pos;
            }
            return -1;
        }

        public string GetUserName(int index, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);

            return Records[mosaic.Ordinal(), eSkill.Ordinal()][index].userName;
        }

        public long GetUserPlayTime(int index, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);

            return Records[mosaic.Ordinal(), eSkill.Ordinal()][index].playTime;
        }

        public int GetUsersCount(EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);

            return Records[mosaic.Ordinal(), eSkill.Ordinal()].Count;
        }

        /// <summary>Найдёт позицию лучшего результата указанного пользователя</summary>
        public int GetPos(Guid userId, EMosaic mosaic, ESkillLevel eSkill) {
            if (userId == null)
                return -1;
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);

            IList<Record> list = Records[mosaic.Ordinal(), eSkill.Ordinal()];
            int pos = 0;
            foreach (Record record in list) {
                if (record.userId.Equals(userId))
                    return pos;
                pos++;
            }
            return -1;
        }

        private void OnPlayersPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            if (ev.PropertyName != Players.USER_NAME_UPDATED)
                return;

            // было переименование пользователя...
            // в этом случае переименовываю его имя и в чемпионах
            Players players = sender as Players;
            PropertyChangedExEventArgs<int> ev2 = (PropertyChangedExEventArgs<int>)ev;
            User user = players.GetUser(ev2.NewValue);
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<Record> list = Records[mosaic.Ordinal(), eSkill.Ordinal()];
                        bool isChanged = false;
                        foreach (Record record in list)
                            if ((user.Id == record.userId) && (user.Name != record.userName)) {
                                record.userName = user.Name;
                                isChanged = true;
                            }
                        if (isChanged)
                            notifier.FirePropertyChanged(Guid.Empty, user.Id, CHAMPION_RENAMED);
                    }
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            if (unsubscriber != null)
                unsubscriber();
            notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
