#if WINDOWS_RT || WINDOWS_UWP
using System;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Security.Cryptography;
using Windows.Security.Cryptography.Core;
using Windows.Storage.Streams;
#elif WINDOWS_FORMS
using System;
using System.Text;
using System.Security.Cryptography;
#endif

namespace fmg.common.crypt {

    public interface I3DES {
        // TODO
        byte[] Encrypt(byte[] data);
        byte[] Decrypt(byte[] data);
    }

#if WINDOWS_RT || WINDOWS_UWP
    public class TripleDESOperations {

        public BinaryStringEncoding Encoding { get; set; }
        /// <summary> Symmetric algorithm name </summary>
        public string Algorithm { get; set; }

        public IBuffer DataBuf { get; set; }
        /// <summary> Data to encription or decription </summary>
        public byte[] Data { get { return DataBuf?.ToArray(); } set { DataBuf = value?.AsBuffer(); } }
        public string DataStr { set { DataBuf = CryptographicBuffer.ConvertStringToBinary(value, Encoding); } }
        public string DataB64 { set { DataBuf = CryptographicBuffer.DecodeFromBase64String(value); } }
        public string DataHex { set { DataBuf = CryptographicBuffer.DecodeFromHexString(value); } }

        public IBuffer InitVectorBuf { get; set; }
        public byte[] InitVector { get { return InitVectorBuf?.ToArray(); } set { InitVectorBuf = value?.AsBuffer(); } }

        public Func<BinaryStringEncoding, string, byte[]> SecKeyBinTransformer { get; set; }
        public IBuffer SecurityKeyBuf { get; set; }
        public byte[] SecurityKey { get { return (SecurityKeyBuf == null) ? null : SecurityKeyBuf.ToArray(); } set { SecurityKeyBuf = value?.AsBuffer(); } }
        public string SecurityKeyBinStr { set { SecurityKeyBuf = CryptographicBuffer.ConvertStringToBinary(value, Encoding); } }
        public string SecurityKeyB64 { set { SecurityKeyBuf = CryptographicBuffer.DecodeFromBase64String(value); } }
        public string SecurityKeyHex { set { SecurityKeyBuf = CryptographicBuffer.DecodeFromHexString(value); } }
        public string SecurityKeyStr { set { SecurityKey = SecKeyBinTransformer(Encoding, value); } }

        public TripleDESOperations() {
            Encoding = BinaryStringEncoding.Utf8;
            Algorithm = SymmetricAlgorithmNames.TripleDesEcbPkcs7;
            SecKeyBinTransformer = MD5HashTransformer;
        }

        public IBuffer EncryptBuf() {
            var algProvider = SymmetricKeyAlgorithmProvider.OpenAlgorithm(Algorithm);

            IBuffer data = DataBuf;
            if (!Algorithm.Contains("PKCS7"))
                if ((data.Length % algProvider.BlockLength) != 0)
                    throw new Exception("Message buffer length must be multiple of block length.");

            var key = algProvider.CreateSymmetricKey(SecurityKeyBuf);

            if ((InitVectorBuf == null) && Algorithm.Contains("CBC"))
                InitVectorBuf = CryptographicBuffer.GenerateRandom(algProvider.BlockLength);

            return CryptographicEngine.Encrypt(key, DataBuf, InitVectorBuf);
        }
        public byte[] Encrypt() { return EncryptBuf().ToArray(); }
        public string EncryptB64() { return CryptographicBuffer.EncodeToBase64String(EncryptBuf()); }
        public string EncryptBin() { return CryptographicBuffer.ConvertBinaryToString(Encoding, EncryptBuf()); }
        public string EncryptHex() { return CryptographicBuffer.EncodeToHexString(EncryptBuf()); }

        public IBuffer DecryptBuf() {
            var algProvider = SymmetricKeyAlgorithmProvider.OpenAlgorithm(Algorithm);
            var key = algProvider.CreateSymmetricKey(SecurityKeyBuf);
            return CryptographicEngine.Decrypt(key, DataBuf, InitVectorBuf);
        }
        public byte[] Decrypt() { return DecryptBuf().ToArray(); }
        public string DecryptStr() { return CryptographicBuffer.ConvertBinaryToString(Encoding, DecryptBuf()); }
        public string DecryptHex() { return CryptographicBuffer.EncodeToHexString(DecryptBuf()); }

        public static byte[] MD5HashTransformer(BinaryStringEncoding encoding, string data) {
            IBuffer buff = CryptographicBuffer.ConvertStringToBinary(data, encoding);
            var algProvider = HashAlgorithmProvider.OpenAlgorithm(HashAlgorithmNames.Md5);

            IBuffer buffHash16 = algProvider.HashData(buff);
            IBuffer buffHash24 = (new byte[24]).AsBuffer();
            buffHash16.CopyTo(0, buffHash24, 0, 16);
            return buffHash24.ToArray();
        }

        public static IBuffer GenerateKeyBuf() { return CryptographicBuffer.GenerateRandom(16); }
        public static byte[] GenerateKey() { return GenerateKeyBuf().ToArray(); }
        public static IBuffer GenerateInitVectorBuf() { return CryptographicBuffer.GenerateRandom(8); }
        public static byte[] GenerateInitVector() { return GenerateInitVectorBuf().ToArray(); }
    }

#elif WINDOWS_FORMS

    /// <summary>
    /// Using:
    /// var encrypted = new TripleDESOperations() { SecurityKeyStr = secKey, DataStr = testString }.EncryptB64();
    /// var decrypted = new TripleDESOperations() { SecurityKeyStr = secKey, DataB64 = encrypted }.DecryptStr();
    /// System.Diagnostics.Debug.Assert(decrypted == testString, "Triple DES failed!");
    /// </summary>
    public class TripleDESOperations
    {
        public Encoding Encoding { get; set; }
        public CipherMode Mode { get; set; }
        public PaddingMode Padding { get; set; }

        /// <summary> Data to encription or decription </summary>
        public byte[] Data { get; set; }
        public string DataStr { set { Data = Encoding.GetBytes(value); } }
        public string DataB64 { set { Data = Convert.FromBase64String(value); } }

        public byte[] InitVector { get; set; }

        public Func<Encoding, string, byte[]> SecKeyBinTransformer { get; set; }
        public byte[] SecurityKey { get; set; }
        public string SecurityKeyStr { set { SecurityKey = SecKeyBinTransformer(Encoding, value); } }

        public TripleDESOperations()
        {
            Encoding = UTF8Encoding.UTF8;
            Mode = CipherMode.ECB;
            Padding = PaddingMode.PKCS7;
            SecKeyBinTransformer = MD5HashTransformer;
        }

        /// <summary>
        /// Convert data to Encrypted/Un-Readable raw binary array
        /// </summary>
        /// <returns>Cipher data</returns>
        public byte[] Encrypt()
        {
            using (var algPrpv = CreateProvider())
            {
                using (var encryptor = algPrpv.CreateEncryptor())
                {
                    return encryptor.TransformFinalBlock(Data, 0, Data.Length);
                }
            }
        }

        /// <summary>
        /// Convert data to Encrypted/Un-Readable Text
        /// </summary>
        /// <returns>Cipher Text</returns>
        public string EncryptB64()
        {
            return Convert.ToBase64String(Encrypt());
        }

        /// <summary>
        /// Convert the Cipher/Encypted data to raw data
        /// </summary>
        /// <returns>Plain/Decrypted raw data</returns>
        public byte[] Decrypt()
        {
            using (var algPrpv = CreateProvider())
            {
                using (var decryptor = algPrpv.CreateDecryptor())
                {
                    return decryptor.TransformFinalBlock(Data, 0, Data.Length);
                }
            }
        }

        /// <summary>
        /// Convert the Cipher/Encypted data to plain text
        /// </summary>
        /// <returns>Plain/Decrypted plain text</returns>
        public string DecryptStr()
        {
            return Encoding.GetString(Decrypt());
        }

        private TripleDESCryptoServiceProvider CreateProvider()
        {
            var prov = new TripleDESCryptoServiceProvider
            {
                Key = SecurityKey,
                Mode = Mode,
                Padding = Padding
            };
            if (InitVector != null)
                prov.IV = InitVector;
            return prov;
        }

        public static byte[] MD5HashTransformer(Encoding encoding, string value)
        {
            using (var objMD5CryptoService = new MD5CryptoServiceProvider())
            {
                return objMD5CryptoService.ComputeHash(encoding.GetBytes(value));
            }
        }

        public static string GenerateKeyStr(Encoding encoding) { return encoding.GetString(GenerateKey()); }
        public static byte[] GenerateKey()
        {
            using (var algPrpv = TripleDESCryptoServiceProvider.Create())
            {
                return algPrpv.Key;
            }
        }

        public static byte[] GenerateInitVector()
        {
            using (var algPrpv = new TripleDESCryptoServiceProvider())
            {
                algPrpv.GenerateIV();
                return algPrpv.IV;
            }
        }
    }
#endif

}
