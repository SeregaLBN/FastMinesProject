using System;
using System.Linq;
using System.Text;
using System.Collections.Generic;
using System.IO;
using Fmg.Common;
using Fmg.Common.Crypt;
using Fmg.Core.App;
using Fmg.Core.Types.Viewmodel.Event;

namespace Fmg.Core.Types.Viewmodel.Serializable {

    /// <summary>хранилище чемпионов</summary>
    public abstract class ChampionsModel : IExternalizable {
        private readonly long version;

        private const int MAX_SIZE = 10;

        class Record : IExternalizable, IComparable<Record> {
            public Guid userId;
            public string userName;
            public long playTime = long.MaxValue;

            public Record() {}
            public Record(User user, long playTime) {
                this.userId = user.Guid;
                this.userName = user.Name;
                this.playTime  = playTime;
            }

            public void ReadExternal(BinaryReader input) {
                userId = new Guid(input.ReadString());
                userName = input.ReadString();
                playTime = input.ReadInt64();
            }
            public void WriteExternal(BinaryWriter output) {
                output.Write(userId.ToString());
                output.Write(userName);
                output.Write((Int64)playTime);
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

        private List<ChampionsModel.Record>[,] champions = new List<ChampionsModel.Record>[EMosaicEx.GetValues().Length, ESkillLevelEx.GetValues().Length - 1];

        public void OnPlayerChanged(object sender, PlayerModelEventArgs e) {
            if (e.Type == PlayerModelEventArgs.UPDATE) {
                // если был UPDATE, то это, возможно что, было переименование пользователя...
                // в этом случае переименовываю его имя и в чемпионах
                PlayersModel players = sender as PlayersModel;
                User user = players.GetUser(e.Pos);
                foreach (var mosaic in EMosaicEx.GetValues())
                    foreach (var eSkill in ESkillLevelEx.GetValues())
                        if (eSkill != ESkillLevel.eCustom) {
                            IList<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
                            bool isChanged = false;
                            foreach (Record record in list)
                                if ((user.Guid == record.userId) && !user.Name.Equals(record.userName)) {
                                    record.userName = user.Name;
                                    isChanged = true;
                                }
                            if (isChanged)
                                FireChanged(new ChampionModelEventArgs(mosaic, eSkill, ChampionModelEventArgs.POS_ALL, ChampionModelEventArgs.UPDATE));
                        }
            }
        }
        public ChampionsModel(long version, PlayersModel players) {
            this.version = version;
            if (players != null)
                players.OnPlayerChanged += OnPlayerChanged;

            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom)
                        champions[mosaic.Ordinal(), eSkill.Ordinal()] = new List<ChampionsModel.Record>(MAX_SIZE);
        }

        public int Add(User user, long playTime, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                return -1;

            List<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
            Record newRecord = new Record(user, playTime);
            list.Add(newRecord);

            list.Sort();

            int pos = list.IndexOf(newRecord);
            if (pos == -1)
                throw new Exception("Where??");

            if (list.Count > MAX_SIZE)
                list = list.Take(MAX_SIZE).ToList();

            FireChanged(new ChampionModelEventArgs(mosaic, eSkill, ChampionModelEventArgs.POS_ALL, ChampionModelEventArgs.UPDATE));
            if (pos < MAX_SIZE) {
                FireChanged(new ChampionModelEventArgs(mosaic, eSkill, pos, ChampionModelEventArgs.INSERT));
                return pos;
            }
            return -1;
        }

        public void WriteExternal(BinaryWriter output) {
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
                        output.Write(list.Count);
                        foreach (Record record in list)
                            record.WriteExternal(output);
                    }
        }

        public void ReadExternal(BinaryReader input) {
            SetDefaults();
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
                        int size = input.ReadInt32();
                        for (int i=0; i<size; i++) {
                            Record record = new Record();
                            record.ReadExternal(input);
                            list.Add(record);
                        }
                        FireChanged(new ChampionModelEventArgs(mosaic, eSkill, ChampionModelEventArgs.POS_ALL, ChampionModelEventArgs.INSERT));
                    }
        }

        private void SetDefaults() {
            foreach (var mosaic in EMosaicEx.GetValues())
                foreach (var eSkill in ESkillLevelEx.GetValues())
                    if (eSkill != ESkillLevel.eCustom) {
                        IList<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
                        list.Clear();
                        FireChanged(new ChampionModelEventArgs(mosaic, eSkill, ChampionModelEventArgs.POS_ALL, ChampionModelEventArgs.DELETE));
                    }
        }

    #if WINDOWS_RT || WINDOWS_UWP
        // Load BST data from file
        // return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
        public async System.Threading.Tasks.Task<bool> Load() {
            var files = await Windows.Storage.ApplicationData.Current.LocalFolder.GetFilesAsync(Windows.Storage.Search.CommonFileQuery.OrderByName);
            var file = files.FirstOrDefault(x => x.Name == ChampFile);
            if (file == null) {
                SetDefaults();
                return false;
            }

            //file = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(StcFile, Windows.Storage.CreationCollisionOption.OpenIfExists);
            try {
                // 1. read from file
                var content = await Windows.Storage.FileIO.ReadBufferAsync(file);
                byte[] data;
                using (Stream stream = (await file.OpenReadAsync()).AsStreamForRead()) {
                    using (BinaryReader input = new BinaryReader(stream, Encoding.UTF8)) {
                        if (input.ReadInt64() != version)
                            throw new Exception("Invalid file data - unknown version");
                        data = new byte[input.ReadInt32()];
                        int read = 0;
                        do {
                            int curr = input.Read(data, read, data.Length - read);
                            if (curr == -1)
                            break;
                            read += curr;
                        } while (read < data.Length);
                        if (read != data.Length)
                            throw new IOException("Invalid data length. Ожидалось " + data.Length + " байт, а прочитано " + read + " байт.");
                    }
                }

                // 2. decrypt data
                data = new TripleDESOperations() {
                    Algorithm = Windows.Security.Cryptography.Core.SymmetricAlgorithmNames.TripleDesCbcPkcs7,
                    InitVector = new byte[8],
                    SecurityKeyStr = version.ToString(),
                    Data = data
                }.Decrypt();

                // 3. deserializable object
                using (Stream stream = new MemoryStream()) {
                    stream.Write(data, 0, data.Length);
                    using (BinaryReader input = new BinaryReader(stream)) {
                        this.ReadExternal(input);
                    }
                }

                return true;
            } catch (Exception ex) {
                System.Diagnostics.Debug.WriteLine(ex.Message);
                SetDefaults();
                return false;
            }
        }

        public async void Save() {
            // 1. serializable object
            using (Stream stream = new MemoryStream()) {
                using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
                    this.WriteExternal(output);
                }

                // 2. crypt data
                byte[] noCryptedData = new byte[stream.Length];
                stream.Position = 0;
                var readed = await stream.ReadAsync(noCryptedData, 0, noCryptedData.Length);
                if (readed != noCryptedData.Length)
                    throw new Exception("Not readed saved data");
                byte[] cryptData = new TripleDESOperations() {
                    Algorithm = Windows.Security.Cryptography.Core.SymmetricAlgorithmNames.TripleDesCbcPkcs7,
                    InitVector = new byte[8],
                    SecurityKeyStr = version.ToString(),
                    Data = noCryptedData
                }.Encrypt();

                // 3. write to file
                var file = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(ChampFile, Windows.Storage.CreationCollisionOption.ReplaceExisting);
                using (var streamFile = await file.OpenStreamForWriteAsync()) {
                    using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
                        output.Write(version); // save version and decrypt key
                        int len = cryptData.Length;
                        output.Write(len);
                        output.Write(cryptData);
                    }
                }
            }
        }
#elif WINDOWS_FORMS
        ///**
        // * Load BST data from file
        // * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
        // */
        //public boolean Load() {
        //    File file = getChampFile();
        //    if (!file.exists()) {
        //       setDefaults();
        //       return false;
        //    }

        //    try {
        //       // 1. read from file
        //       ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        //       if (in.readLong() != version)
        //           throw new RuntimeException("Invalid file data - unknown version");
        //       byte[] data = new byte[in.readInt()];
        //       int read = 0;
        //       do {
        //           int curr = in.read(data, read, data.length-read);
        //           if (curr == -1)
        //               break;
        //           read += curr;
        //       } while(read < data.length);
        //       if (read != data.length)
        //           throw new IOException("Invalid data length. Ожидалось " + data.length + " байт, а прочитано " + read + " байт.");
        //       in.close();

        //       // 2. decrypt data
        //       try {
        //           data = new Simple3DES(Long.toString(version)).decrypt(data);
        //       } catch (Exception ex) {
        //           throw new RuntimeException(ex);
        //       }

        //       // 3. deserializable object
        //       in = new ObjectInputStream(new ByteArrayInputStream(data));
        //       this.readExternal(in);

        //       in.close();

        //       return true;
        //    } catch (Exception ex) {
        //       ex.printStackTrace();
        //       setDefaults();
        //       return false;
        //    }
        //}

        //public void Save() throws FileNotFoundException, IOException {
        //    // 1. serializable object
        //    ByteArrayOutputStream byteRaw = new ByteArrayOutputStream();
        //    ObjectOutputStream out = new ObjectOutputStream(byteRaw);
        //    this.writeExternal(out);
        //    out.flush();

        //    // 2. crypt data
        //    byte[] cryptData;
        //    try {
        //        cryptData = new Simple3DES(Long.toString(version)).encrypt(byteRaw.toByteArray());
        //    } catch (Exception ex) {
        //        throw new RuntimeException(ex);
        //    }

        //    // 3. write to file
        //    out = new ObjectOutputStream(new FileOutputStream(getChampFile()));
        //    out.writeLong(version); // save version and decrypt key
        //    int len = cryptData.length;
        //    out.writeInt(len);
        //    out.write(cryptData);

        //    out.flush();
        //    out.close();
        //}
#else
         public abstract bool Load();
         public abstract void Save();
#endif

        public static string ChampFile => AProjSettings.ChampionsFileName;

        public event ChampionModelChangedHandler OnChampionChanged = delegate {};
        private void FireChanged(ChampionModelEventArgs e) {
            OnChampionChanged(this, e);
        }

        public string GetUserName(int index, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);
            return champions[mosaic.Ordinal(), eSkill.Ordinal()][index].userName;
        }
        public long GetUserPlayTime(int index, EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);
            return champions[mosaic.Ordinal(), eSkill.Ordinal()][index].playTime;
        }
        public int GetUsersCount(EMosaic mosaic, ESkillLevel eSkill) {
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);
            return champions[mosaic.Ordinal(), eSkill.Ordinal()].Count;
        }

        /// <summary>Найдёт позицию лучшего результата указанного пользователя</summary>
        public int GetPos(Guid userId, EMosaic mosaic, ESkillLevel eSkill) {
            if (userId == null)
                return -1;
            if (eSkill == ESkillLevel.eCustom)
                throw new ArgumentException("Invalid input data - " + eSkill);

            IList<ChampionsModel.Record> list = champions[mosaic.Ordinal(), eSkill.Ordinal()];
            int pos = 0;
            foreach (Record record in list) {
                if (record.userId.Equals(userId))
                    return pos;
                pos++;
            }
            return -1;
        }

    }

}
