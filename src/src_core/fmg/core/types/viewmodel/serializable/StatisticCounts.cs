using System;
using System.IO;
using Fmg.Common;

namespace Fmg.Core.Types.Viewmodel.Serializable {

    public class StatisticCounts : IExternalizable {

        private const long VERSION = 1;

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

        public void ReadExternal(BinaryReader input) {
            var version = input.ReadInt64();
            if (version != VERSION)
                throw new Exception("Unsupported " + nameof(StatisticCounts) + " version " + version);

            gameNumber = input.ReadInt64();
            gameWin = input.ReadInt64();
            openField = input.ReadInt64();
            playTime = input.ReadInt64();
            clickCount = input.ReadInt64();
        }

        public void WriteExternal(BinaryWriter output) {
            output.Write(VERSION);
            output.Write(gameNumber);
            output.Write(gameWin);
            output.Write(openField);
            output.Write(playTime);
            output.Write(clickCount);
        }

        public StatisticCounts Clone() {
            return (StatisticCounts)this.MemberwiseClone();
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
