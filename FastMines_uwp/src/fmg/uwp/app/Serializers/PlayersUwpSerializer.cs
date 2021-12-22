using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using Windows.Storage;
using Fmg.Common.Crypt;
using Fmg.Core.App;
using Fmg.Core.App.Model;
using Fmg.Core.App.Serializers;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> Players (de)serializer </summary>
    public class PlayersUwpSerializer : PlayersSerializer {


        /// <summary> write to file </summary>
        protected override Task Write(byte[] data, string file) {
            return WriteAsync(data, file);
        }

        private async Task WriteAsync(byte[] data, string file) {
            var storageFile = await ApplicationData.Current.LocalFolder.CreateFileAsync(file, CreationCollisionOption.ReplaceExisting);
            await Write(data, storageFile);
        }

        /// <summary> read from file </summary>
        protected override Task<byte[]> Read(string file) {
            return ReadAsync(file);
        }

        private async Task<byte[]> ReadAsync(string file) {
            var storageFile = await ApplicationData.Current.LocalFolder.CreateFileAsync(file, CreationCollisionOption.OpenIfExists);
            return await Read(storageFile);
        }

        /// <summary> write to file </summary>
        private async Task Write(byte[] data, StorageFile file) {
            using (var streamFile = await file.OpenStreamForWriteAsync()) {
                using (BinaryWriter output = new BinaryWriter(streamFile, Encoding.UTF8)) {
                    output.Write(VERSION);
                    int len = data.Length;
                    output.Write(len);
                    output.Write(data);
                }
            }
        }

        /// <summary> read from file </summary>
        private async Task<byte[]> Read(StorageFile file) {
            using (Stream stream = (await file.OpenReadAsync()).AsStreamForRead()) {
                using (BinaryReader input = new BinaryReader(stream, Encoding.UTF8)) {
                    long version = input.ReadInt64();
                    if (version != VERSION)
                        throw new Exception("Invalid file data. Unsupported " + nameof(Players) + " version " + version);

                    byte[] data = new byte[input.ReadInt32()];
                    int read = 0;
                    do {
                        int curr = input.Read(data, read, data.Length - read);
                        if (curr < 0)
                            break;
                        read += curr;
                    } while (read < data.Length);
                    if (read != data.Length)
                        throw new IOException("Invalid data length. Required " + data.Length + " bytes; read " + read + " bytes.");

                    return data;
                }
            }
        }

        protected override byte[] WriteTransform(byte[] data) {
            return new TripleDESOperations() {
                Algorithm = Windows.Security.Cryptography.Core.SymmetricAlgorithmNames.TripleDesCbcPkcs7,
                InitVector = new byte[8],
                SecurityKeyStr = VERSION.ToString(),
                Data = data
            }.Encrypt();
        }

        protected override byte[] ReadTransform(byte[] data) {
            return new TripleDESOperations() {
                Algorithm = Windows.Security.Cryptography.Core.SymmetricAlgorithmNames.TripleDesCbcPkcs7,
                InitVector = new byte[8],
                SecurityKeyStr = VERSION.ToString(),
                Data = data
            }.Decrypt();
        }

        protected override string GetPlayersFile() {
            return AProjSettings.PlayersFileName;
        }

        private static async Task<bool> IsFileExistAsync(string fileName) {
            var item = await ApplicationData.Current.LocalFolder.TryGetItemAsync(fileName);
            return item != null;
        }

        protected override Task<bool> IsFileExist(string file) {
            return IsFileExistAsync(file);
        }

    }

}
