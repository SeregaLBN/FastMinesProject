using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Generic;
using Fmg.Common;
using Fmg.Core.App.Model;

namespace Fmg.Core.App.Serializers {

    /// <summary> Players base (de)serializer  </summary>
    public abstract class PlayersSerializer : ISerializer {

        protected const long VERSION = 1;

        private class RecordSerializer : ISerializer {

            public void Write(Players.Record rec, BinaryWriter to) {
                new UserSerializer().Write(rec.user, to);
                StatisticsSerializer ss = new StatisticsSerializer();
                foreach (Statistics record in rec.statistics)
                    ss.Write(record, to);
            }

            public Players.Record Read(BinaryReader from) {
                User user = new UserSerializer().Read(from);
                Players.Record res = new Players.Record(user);
                StatisticsSerializer ss = new StatisticsSerializer();
                foreach (Statistics record in res.statistics)
                    ss.Read(record, from);
                return res;
            }

        }

        private void Write(Players players, BinaryWriter to) {
            to.Write(VERSION);
            to.Write(players.Size);
            RecordSerializer rs = new RecordSerializer();
            foreach (Players.Record rec in players.Records)
                rs.Write(rec, to);
        }

        private Players Read(BinaryReader from) {
            long version = from.ReadInt64();
            if (version != VERSION)
                throw new ArgumentException("Unsupported " + nameof(Players) + " version " + version);

            int size = from.ReadInt32();
            RecordSerializer rs = new RecordSerializer();
            Players res = new Players();
            IList<Players.Record> all = res.Records;
            for (int i=0; i<size; i++)
                all.Add(rs.Read(from));

            return res;
        }

        /// <summary> serialize to bytes </summary>
        private async Task<byte[]> AsBytes(Players players) {
            using (Stream stream = new MemoryStream()) {
                using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
                    Write(players, output);
                }

                byte[] data = new byte[stream.Length];
                stream.Position = 0;
                var readed = await stream.ReadAsync(data, 0, data.Length);
                if (readed != data.Length)
                    throw new Exception("Not readed saved data: requred " + data.Length + " bytes; read " + readed + " bytes");
                return data;
            }
        }

        /// <summary> deserilize from bytes </summary>
        private async Task<Players> FromBytes(byte[] data) {
            using (Stream stream = new MemoryStream()) {
                await stream.WriteAsync(data, 0, data.Length);
                using (BinaryReader input = new BinaryReader(stream)) {
                    return Read(input);
                }
            }
        }

        /// <summary> write to file </summary>
        protected abstract void Write(byte[] data, string file);

        /// <summary> read from file </summary>
        protected abstract byte[] Read(string file);

        protected virtual byte[] WriteTransform(byte[] data) {
            // defaut none
            return data;
        }

        protected virtual byte[] ReadTransform(byte[] data) {
            // defaut none
            return data;
        }

        public async Task Save(Players players) {
            try {
                // 1. serialize
                byte[] data = await AsBytes(players);

                // 2. transform data
                data = WriteTransform(data);

                // 3. write to file
                Write(data, GetPlayersFile());

            } catch (Exception ex) {
                Logger.Error("Can`t save " + nameof(Players), ex);
            }
        }

        public async Task<Players> Load() {
            string file = GetPlayersFile();
            if (!IsFileExist(file))
                return new Players();

            try {
                // 1. read from file
                byte[] data = Read(file);

                // 2. transform data
                data = ReadTransform(data);

                // 3. deserialize
                return await FromBytes(data);

            } catch (Exception ex) {
                Logger.Error("Can`t load " + nameof(Players), ex);
                return new Players();
            }
        }

        protected abstract string GetPlayersFile();
        protected abstract bool IsFileExist(string file);

    }

}
