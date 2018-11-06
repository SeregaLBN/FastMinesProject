using System;
using System.Globalization;
using System.Threading.Tasks;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Storage;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Graphics.Imaging;
using fmg.core.types;

namespace fmg.uwp.utils.wbmp {

    /// <summary> Мультимедиа ресурсы программы </summary>
    public static class Resources {

        private static async Task<WriteableBitmap> GetImage(string path) {
            return await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path)) ??
                   await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
        }

        private static BitmapImage GetImageSync(string path) {
            return ImgUtils.GetImageSync(new Uri("ms-appx:///res/" + path)) ??
                   ImgUtils.GetImageSync(new Uri("ms-appx:///" + path));
        }

        public static async Task<WriteableBitmap> GetImgLogoPng(string subdir = "TileSq150", int scale = 100) {
            return await GetImage(string.Format("Logo/{0}/Logo.scale-{1}.png", subdir, scale));
        }

        /// <summary> из ресурсов </summary>
        public async static Task<WriteableBitmap> GetImgMosaicGroupPng(EMosaicGroup key) {
            return await GetImage("MosaicGroup/" + key.GetDescription() + ".png");
        }

        ///// <summary> из ресурсов </summary>
        //public static async Task<WriteableBitmap> GetImgMosaicPng(EMosaic mosaicType, bool smallIco) {
        //    return await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
        //}

        public static BitmapImage GetImgMosaicPngSync(EMosaic mosaicType, bool smallIco) {
            return GetImageSync("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
        }

        public static async Task<WriteableBitmap> getImgsLang() {
            foreach (var lang in Windows.System.UserProfile.GlobalizationPreferences.Languages) {
                var locale = new CultureInfo(lang);

                if ((lang == "EN") || ("GBR" == locale.EnglishName))
                    return await GetImage("Lang/English.png");
                if ("UKR" == locale.EnglishName)
                    return await GetImage("Lang/Ukrainian.png");
                if ("RUS" == locale.EnglishName)
                    return await GetImage("Lang/Russian.png");
            }
            return null;
        }

        public static async Task<StorageFile> SaveToFile(this WriteableBitmap writeableBitmap, string fileName, StorageFolder storageFolder = null) {
            if (storageFolder == null)
                storageFolder = KnownFolders.PicturesLibrary;
            var outputFile = await storageFolder.CreateFileAsync(fileName, CreationCollisionOption.ReplaceExisting);
            await SaveToFile(writeableBitmap, outputFile);
            return outputFile;
        }

        public static async Task SaveToFile(this WriteableBitmap writeableBitmap, StorageFile outputFile) {
            byte[] pixels;
            using (var stream = writeableBitmap.PixelBuffer.AsStream()) {
                pixels = new byte[stream.Length];
                await stream.ReadAsync(pixels, 0, pixels.Length);
            }

            using (var writeStream = await outputFile.OpenAsync(FileAccessMode.ReadWrite)) {
                var encoder = await BitmapEncoder.CreateAsync(BitmapEncoder.PngEncoderId, writeStream);
                encoder.SetPixelData(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Premultiplied,
                    (uint)writeableBitmap.PixelWidth, (uint)writeableBitmap.PixelHeight, 96, 96, pixels);
                await encoder.FlushAsync();

                using (var outputStream = writeStream.GetOutputStreamAt(0))
                    await outputStream.FlushAsync();
            }
        }

    }

}
