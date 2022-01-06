using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Generic;
using Fmg.Common;
using Fmg.Core.Types;
using Fmg.Core.App.Model;

namespace Fmg.Core.App.Serializers {

    /// <summary> Champions base (de)serializer </summary>
    public abstract class ChampionsSerializer : ISerializer {

        protected const long VERSION = 1;

        class RecordSerializer : ISerializer {

            public void Write(Champions.Record rec, BinaryWriter output) {
                output.Write(rec.userId.ToString());
                output.Write(rec.userName);
                output.Write(rec.playTime);
                output.Write(rec.clicks);
                output.Write(rec.date.Ticks);
            }

            public Champions.Record Read(BinaryReader input) {
                Champions.Record res = new Champions.Record();
                res.userId   = new Guid(input.ReadString());
                res.userName = input.ReadString();
                res.playTime = input.ReadInt64();
                res.clicks   = input.ReadInt32();
                res.date     = new DateTime(input.ReadInt64());
                return res;
            }

        }

        private void Write(Champions champions, BinaryWriter to) {
            to.Write(VERSION);
            List<Champions.Record>[,] all = champions.Records;
            RecordSerializer rs = new RecordSerializer();
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<Champions.Record> list = all[mosaic.Ordinal(), eSkill.Ordinal()];
                        to.Write(list.Count);
                        foreach (Champions.Record record in list)
                            rs.Write(record, to);
                    }
        }

        private Champions Read(BinaryReader from) {
            long version = from.ReadInt64();
            if (version != VERSION)
                throw new ArgumentException("Unsupported " + nameof(Champions) + " version " + version);

            Champions res = new Champions();
            List<Champions.Record>[,] all = res.Records;
            RecordSerializer rs = new RecordSerializer();
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<Champions.Record> list = all[mosaic.Ordinal(), eSkill.Ordinal()];
                        int size = from.ReadInt32();
                        for (int i=0; i<size; i++) {
                            Champions.Record record = rs.Read(from);
                            list.Add(record);
                        }
                    }
            return res;
        }

        /// <summary> serialize to bytes </summary>
        private async Task<byte[]> AsBytes(Champions champions) {
            using (Stream stream = new MemoryStream()) {
            using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
                Write(champions, output);
                output.Flush();

                byte[] data = new byte[stream.Length];
                stream.Position = 0;
                var readed = await stream.ReadAsync(data, 0, data.Length);
                if (readed != data.Length)
                    throw new Exception("Not readed saved data: requred " + data.Length + " bytes; read " + readed + " bytes");
                return data;
            }}
        }

        /// <summary> deserilize from bytes </summary>
        private async Task<Champions> FromBytes(byte[] data) {
            using (Stream stream = new MemoryStream()) {
                await stream.WriteAsync(data, 0, data.Length);
                stream.Position = 0;
                using (BinaryReader input = new BinaryReader(stream)) {
                    return Read(input);
                }
            }
        }

        /// <summary> write to file </summary>
        protected abstract Task Write(byte[] data, string file);

        /// <summary> read from file </summary>
        protected abstract Task<byte[]> Read(string file);

        protected virtual byte[] WriteTransform(byte[] data) {
            // defaut none
            return data;
        }

        protected virtual byte[] ReadTransform(byte[] data) {
            // defaut none
            return data;
        }

        public async Task Save(Champions champions) {
            Logger.Debug("> ChampionsSerializer::Save");
            try {
                // 1. serialize
                byte[] data = await AsBytes(champions);

                // 2. transform data
                data = WriteTransform(data);

                // 3. write to file
                await Write(data, GetChampionsFile());

            } catch (Exception ex) {
                Logger.Error("Can`t save " + nameof(Champions), ex);
            } finally {
                Logger.Debug("< ChampionsSerializer::Save");
            }
        }

        public async Task<Champions> Load() {
            Logger.Debug("> ChampionsSerializer::Load");
            try {
                string file = GetChampionsFile();
                if (!await IsFileExist(file))
                    return new Champions();

                // 1. read from file
                byte[] data = await Read(file);

                // 2. transform data
                data = ReadTransform(data);

                // 3. deserialize
                return await FromBytes(data);

            } catch (Exception ex) {
                Logger.Error("Can`t load " + nameof(Champions), ex);
                return new Champions();
            } finally {
                Logger.Debug("< ChampionsSerializer::Load");
            }
        }

        protected abstract string GetChampionsFile();
        protected abstract Task<bool> IsFileExist(string file);

    }

}
