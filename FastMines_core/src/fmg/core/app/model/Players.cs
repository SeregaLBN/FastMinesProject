using System;
using System.Collections.Generic;
using System.ComponentModel;
using Fmg.Common.Notifier;
using Fmg.Core.Types;

namespace Fmg.Core.App.Model {

    /// <summary>Players data model == all users and their statistics</summary>
    public class Players : INotifyPropertyChanged, IDisposable {

        public const string USER_NAME_UPDATED      = "UserNameUpdated";
        public const string USER_DELETED           = "UserDeleted";
        public const string USER_ADDED             = "UserAdded";
        public const string USER_STATISTIC_CHANGED = nameof(UserStatisticChanged);

        public IList<Record> Records { get; } = new List<Record>();
        public event PropertyChangedEventHandler PropertyChanged {
            add    { notifier.PropertyChanged += value; }
            remove { notifier.PropertyChanged -= value; }
        }
        private readonly NotifyPropertyChanged notifier;
        private bool Disposed { get; set; }

        public Players() {
            notifier = new NotifyPropertyChanged(this, true);
        }

        public class Record {
            public User user;
            public Statistics[,] statistics = new Statistics[EMosaicEx.GetValues().Length, ESkillLevelEx.GetValues().Length - 1];

            /// <summary>new User</summary>
             public Record(User user) {
                this.user = user;
                foreach (var mosaic in EMosaicEx.GetValues())
                    foreach (var skill in ESkillLevelEx.GetValues())
                        if (skill != ESkillLevel.eCustom)
                            statistics[mosaic.Ordinal(), skill.Ordinal()] = new Statistics();
            }

            public override string ToString() {
                return user.Name;
            }
        }

        /// <summary>for event</summary>
        public class UserStatisticChanged {
            public Guid UserId { get; }
            public EMosaic Mosaic { get; }
            public ESkillLevel Skill { get; }
            public UserStatisticChanged(Guid userId, EMosaic mosaic, ESkillLevel skill) {
                UserId = userId;
                Mosaic = mosaic;
                Skill = skill;
            }
        }


        public void RemovePlayer(Guid userId) {
            Record rec = Find(userId);
            if (rec == null)
                throw new ArgumentException("User wit id=" + userId + " not exist");
            int pos = Records.IndexOf(rec);
            Records.Remove(rec);

            object oldVal = pos; // ;(
            object newVal = rec.user;
            notifier.FirePropertyChanged(oldVal, newVal, USER_DELETED);
        }

        public bool IsExist(Guid userId) { return Find(userId) != null; }

        public int Size { get { return Records.Count; } }

        public Guid AddNewPlayer(string name, string pass) {
            if (string.IsNullOrEmpty(name))
                throw new ArgumentException("Invalid player name. Need not empty.");
            foreach (Record rec in Records)
                if (rec.user.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
                    throw new ArgumentException("Please enter a unique name");

            User user = new User() {
                Id = Guid.NewGuid(),
                Name = name,
                Password = pass,
                ImgAvatar = null
            };
            Records.Add(new Record(user));
            notifier.FirePropertyChanged(null, user, USER_ADDED);
            return user.Id;
        }

        public int IndexOf(User user) {
            Record recFind = null;
                foreach (Record rec in Records)
                if (rec.user.Id == user.Id) {
                    recFind = rec;
                    break;
                }
            if (recFind == null)
                return -1;
            return Records.IndexOf(recFind);
        }

        /// <summary>Установить статистику для игрока</summary>
        /// <param name="userId">идентификатор игрока</param>
        /// <param name="mosaic">на какой мозаике</param>
        /// <param name="skill">на каком уровне сложности</param>
        /// <param name="victory">выиграл ли?</param>
        /// <param name="countOpenField">кол-во открытых ячеек</param>
        /// <param name="playTime">время игры</param>
        /// <param name="clickCount">кол-во кликов</param>
        public void SetStatistic(Guid userId, EMosaic mosaic, ESkillLevel skill, bool victory, long countOpenField, long playTime, int clickCount) {
            if (skill == ESkillLevel.eCustom)
                return;
            Record rec = Find(userId);
            if (rec == null)
                throw new ArgumentException("User " + userId + " not exist");

            Statistics subRec = rec.statistics[mosaic.Ordinal(), skill.Ordinal()];
            subRec.gameNumber++;
            subRec.gameWin    += victory ? 1:0;
            subRec.openField  += countOpenField;
            if (victory) {
                subRec.playTime   += playTime;
                subRec.clickCount += clickCount;
            }

            notifier.FirePropertyChanged(null, new UserStatisticChanged(userId, mosaic, skill), USER_STATISTIC_CHANGED);
        }

        private Record Find(Guid userId) {
            if (userId != null)
                foreach (Record rec in Records)
                    if (rec.user.Id.Equals(userId))
                        return rec;
            return null;
        }

        public User GetUser(int pos) {
            if ((pos < 0) || (pos>=Records.Count))
                throw new ArgumentException("Invalid position " + pos);

            return Records[pos].user;
        }

        public User GetUser(Guid userId) {
            Record rec = Find(userId);
            if (rec == null)
                throw new ArgumentException("User " + userId + " not exist");

            return rec.user;
        }

        public Statistics GetInfo(Guid userId, EMosaic mosaic, ESkillLevel skillLevel) {
            Record rec = Find(userId);
            if (rec == null)
                throw new ArgumentException("User " + userId + " not exist");

            return rec.statistics[mosaic.Ordinal(), skillLevel.Ordinal()].Copy;
        }

        public int GetPos(Guid userId) {
            if (userId == null)
                return -1;

            Record rec = Find(userId);
            if (rec == null)
                return -1;

            return Records.IndexOf(rec);
        }

        public void SetUserName(int pos, string name) {
            User user = GetUser(pos);
            user.Name = name;
            notifier.FirePropertyChanged(null, user, USER_NAME_UPDATED);
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
