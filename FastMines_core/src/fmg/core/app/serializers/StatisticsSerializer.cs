using System;
using System.IO;
using Fmg.Core.App.Model;

namespace Fmg.Core.App.Serializers {

    /// <summary> Statistics (de)serializer </summary>
    public class StatisticsSerializer : ISerializer {

        private const long VERSION = 1;

        public void Write(Statistics data, BinaryWriter to) {
            to.Write(VERSION);
            to.Write(data.gameNumber);
            to.Write(data.gameWin);
            to.Write(data.openField);
            to.Write(data.playTime);
            to.Write(data.clickCount);
        }

        public void Read(Statistics to, BinaryReader from) {
            long version = from.ReadInt64();
            if (version != VERSION)
                throw new ArgumentException("Unsupported " + nameof(Statistics) + " version " + version);

            to.gameNumber = from.ReadInt64();
            to.gameWin    = from.ReadInt64();
            to.openField  = from.ReadInt64();
            to.playTime   = from.ReadInt64();
            to.clickCount = from.ReadInt64();
        }

}

}
