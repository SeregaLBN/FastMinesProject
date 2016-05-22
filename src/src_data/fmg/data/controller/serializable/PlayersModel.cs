using System;
using System.IO;
using System.Linq;
using System.Text;
using System.Collections.Generic;
using fmg.common;
using fmg.data.controller.Event;
using fmg.data.controller.types;
using fmg.core.types;


namespace fmg.data.controller.serializable {

   /// <summary>хранилище пользователей и их игровой статистики</summary>
   public abstract class PlayersModel : IExternalizable {
      //private static final long version = Main.serialVersionUID;
      private readonly long version;
   
      public PlayersModel(long version) { this.version = version; }

      private class Record : IExternalizable {
         public User user;
         public StatisticCounts[,] statistics = new StatisticCounts[EMosaicEx.GetValues().Length, ESkillLevelEx.GetValues().Length - 1];

         /// <summary>new User</summary>
          public Record(User user) {
            this.user = user;
            foreach (var mosaic in EMosaicEx.GetValues())
               foreach (var skill in ESkillLevelEx.GetValues())
                  if (skill == ESkillLevel.eCustom)
                     continue;
                  else
                     statistics[mosaic.Ordinal(), skill.Ordinal()] = new StatisticCounts();
         }
         /// <summary>from file</summary>
         public Record(BinaryReader input) {
            foreach (var mosaic in EMosaicEx.GetValues())
               foreach (var skill in ESkillLevelEx.GetValues())
                  if (skill == ESkillLevel.eCustom)
                     continue;
                  else
                     statistics[mosaic.Ordinal(), skill.Ordinal()] = new StatisticCounts();
            readExternal(input);
         }

         public void readExternal(BinaryReader input) {
            user = new User(input);
            foreach (StatisticCounts record in statistics)
               record.readExternal(input);
         }
         public void writeExternal(BinaryWriter output) {
            user.writeExternal(output);
            foreach (StatisticCounts record in statistics)
               record.writeExternal(output);
         }

         public override String ToString() {
            return user.Name;
         }
      }

      private IList<PlayersModel.Record> players = new List<PlayersModel.Record>();

      public void removePlayer(Guid userId) {
         Record rec = find(userId);
         if (rec == null)
            throw new ArgumentException("User " + userId + " not exist");
         int pos = players.IndexOf(rec);
         players.Remove(rec);

         fireChanged(new PlayerModelEventArgs(pos, PlayerModelEventArgs.DELETE));
      }
      public bool isExist(Guid userId) { return find(userId) != null; }
      public int Size { get { return players.Count; } }
      public Guid addNewPlayer(String name, String pass) {
         if (string.IsNullOrEmpty(name))
            throw new ArgumentException("Invalid player name. Need not empty.");
         foreach (Record rec in players)
            if (rec.user.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
               throw new ArgumentException("Please enter a unique name");

         User user = new User(name, pass, null);
         players.Add(new PlayersModel.Record(user));
         fireChanged(new PlayerModelEventArgs(players.Count-1, PlayerModelEventArgs.INSERT));
         return user.Guid;
      }
      public int indexOf(User user) {
         Record recFind = null;
         foreach (Record rec in players)
            if (rec.user.Guid.Equals(user.Guid)) {
               recFind = rec;
               break;
            }
         if (recFind == null)
            return -1;
         return players.IndexOf(recFind);
      }

      /// <summary>Установить статистику для игрока</summary>
      /// <param name="userId">идентификатор игрока</param>
      /// <param name="mosaic">на какой мозаике</param>
      /// <param name="skill">на каком уровне сложности</param>
      /// <param name="victory">выиграл ли?</param>
      /// <param name="countOpenField">кол-во открытых ячеек</param>
      /// <param name="playTime">время игры</param>
      /// <param name="clickCount">кол-во кликов</param>
      public void setStatistic(Guid userId, EMosaic mosaic, ESkillLevel skill, bool victory, long countOpenField, long playTime, long clickCount) {
         if (skill == ESkillLevel.eCustom)
            return;
         Record rec = find(userId);
         if (rec == null)
            throw new ArgumentException("User " + userId + " not exist");

         StatisticCounts subRec = rec.statistics[mosaic.Ordinal(), skill.Ordinal()];
         subRec.gameNumber++;
         subRec.gameWin    += victory ? 1:0;
         subRec.openField  += countOpenField;
         if (victory) {
            subRec.playTime   += playTime;
            subRec.clickCount += clickCount;
         }

         int pos = players.IndexOf(rec);
         fireChanged(new PlayerModelEventArgs(pos, PlayerModelEventArgs.CHANGE_STATISTICS, mosaic, skill));
      }
      private Record find(Guid userId) {
         if (userId != null)
            foreach (Record rec in players)
               if (rec.user.Guid.Equals(userId))
                  return rec;
         return null;
      }
      public User getUser(int pos) {
         if ((pos < 0) || (pos>=players.Count))
            throw new ArgumentException("Invalid position " + pos);
            //return null;
         return players[pos].user;
      }
      public User getUser(Guid userId) {
         Record rec = find(userId);
         if (rec == null)
            throw new ArgumentException("User " + userId + " not exist");
         return rec.user;
      }
      public StatisticCounts getInfo(Guid userId, EMosaic mosaic, ESkillLevel skillLevel) {
         Record rec = find(userId);
         if (rec == null)
            throw new ArgumentException("User " + userId + " not exist");

         return rec.statistics[mosaic.Ordinal(), skillLevel.Ordinal()].clone();
      }
      public int getPos(Guid userId) {
         if (userId == null)
            return -1;
         Record rec = find(userId);
         if (rec == null)
            return -1;
         return players.IndexOf(rec);
      }

      public void writeExternal(BinaryWriter output) {
         output.Write(version);
         output.Write(players.Count);
         foreach (PlayersModel.Record rec in players)
            rec.writeExternal(output);
      }

      public void readExternal(BinaryReader input) {
         setDefaults();
         if (version != input.ReadInt64())
            throw new Exception("Unknown version");
         int size = input.ReadInt32();
         for (int i=0; i<size; i++)
            players.Add(new PlayersModel.Record(input));

         fireChanged(new PlayerModelEventArgs(players.Count-1, PlayerModelEventArgs.INSERT_ALL));
      }

      private void setDefaults() {
         int len = players.Count;
         players.Clear();
         fireChanged(new PlayerModelEventArgs(len-1, PlayerModelEventArgs.DELETE_ALL));
      }

   #if WINDOWS_RT || WINDOWS_UWP
      /// <summary>Load STC data from file</summary>
      /// <returns><b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults</returns>
      public async System.Threading.Tasks.Task<bool> Load() {
         var files = await Windows.Storage.ApplicationData.Current.LocalFolder.GetFilesAsync(Windows.Storage.Search.CommonFileQuery.OrderByName);
         var file = files.FirstOrDefault(x => x.Name == StcFile);
         if (file == null) {
            setDefaults();
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
            //data = new Simple3DES(Long.toString(version)).decrypt(data);

            // 3. deserializable object
            using (Stream stream = new MemoryStream()) {
               stream.Write(data, 0, data.Length);
               using (BinaryReader input = new BinaryReader(stream)) {
                  this.readExternal(input);
               }
            }

            return true;
         } catch (Exception ex) {
            System.Diagnostics.Debug.WriteLine(ex.Message);
            setDefaults();
            return false;
         }
      }

      public async void Save() {
         // 1. serializable object
         using (Stream stream = new MemoryStream()) {
            using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
               this.writeExternal(output);
            }

            // 2. crypt data
            byte[] noCryptedData = new byte[stream.Length];
            stream.Position = 0;
            var readed = await stream.ReadAsync(noCryptedData, 0, noCryptedData.Length);
            if (readed != noCryptedData.Length)
               throw new Exception("Not readed saved data");
            //byte[] cryptData = new Simple3DES(Long.toString(version)).encrypt(noCryptedData);
            byte[] cryptData = noCryptedData; // @TODO: temp

            // 3. write to file
            var file = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(StcFile, Windows.Storage.CreationCollisionOption.ReplaceExisting);
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
      /// <summary>Load STC data from file</summary>
      /// <returns><b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults</returns>
      public bool Load() {
         var file = Assembly.GetExecutingAssembly().GetName().CodeBase;
         if (File.Exists(file)) {
            setDefaults();
            return false;
         }

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
            //data = new Simple3DES(Long.toString(version)).decrypt(data);

            // 3. deserializable object
            using (Stream stream = new MemoryStream()) {
               stream.Write(data, 0, data.Length);
               using (BinaryReader input = new BinaryReader(stream)) {
                  this.readExternal(input);
               }
            }

            return true;
         } catch (Exception ex) {
            System.Diagnostics.Debug.WriteLine(ex.Message);
            setDefaults();
            return false;
         }
      }

      public void Save() {
         // 1. serializable object
         using (Stream stream = new MemoryStream()) {
            using (BinaryWriter output = new BinaryWriter(stream, Encoding.UTF8)) {
               this.writeExternal(output);
            }

            // 2. crypt data
            byte[] noCryptedData = new byte[stream.Length];
            stream.Position = 0;
            var readet = await stream.ReadAsync(noCryptedData, 0, noCryptedData.Length);
            if (readet != noCryptedData.Length)
               throw new Exception("Not readet saved data");
            //byte[] cryptData = new Simple3DES(Long.toString(version)).encrypt(noCryptedData);
            byte[] cryptData = noCryptedData;

            // 3. write to file
            var file = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync(StcFile, Windows.Storage.CreationCollisionOption.ReplaceExisting);
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
   #else
      public abstract bool Load();
      public abstract void Save();
   #endif

      /// <summary>STatistiCs file name</summary>
      public static string StcFile { get {return "Mines.stc"; } }

      public event PlayerModelChangedHandler OnPlayerChanged = delegate { };
      private void fireChanged(PlayerModelEventArgs e) {
         OnPlayerChanged(this, e);
      }
      public void setUserName(int pos, String name) {
         User user = getUser(pos);
         user.Name = name;
         fireChanged(new PlayerModelEventArgs(pos, PlayerModelEventArgs.UPDATE));
      }

   }

}
