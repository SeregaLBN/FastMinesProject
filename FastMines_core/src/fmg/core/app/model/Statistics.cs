namespace Fmg.Core.App.Model {

    public class Statistics {

        /// <summary>количество сыгранных игр</summary>
        public long gameNumber;
        /// <summary>количество выиграных игр</summary>
        public long gameWin;
        /// <summary>суммарное число открытых ячеек - вывожу средний процент открытия поля</summary>
        public long openField;
        /// <summary>суммарное время игр (milliseconds) - вывожу сколько всреднем игрок провёл времени за данной игрой</summary>
        public long playTime;
        /// <summary>суммарное число кликов - вывожу среднее число кликов в данной игре</summary>
        public long clickCount;

        public Statistics Copy {
            get {
                return (Statistics)this.MemberwiseClone();
                //StatisticCounts clone = new StatisticCounts();
                //clone.gameNumber = this.gameNumber;
                //clone.gameWin    = this.gameWin;
                //clone.openField  = this.openField;
                //clone.playTime   = this.playTime;
                //clone.clickCount = this.clickCount;
                //return clone;
            }
        }

    }

}
