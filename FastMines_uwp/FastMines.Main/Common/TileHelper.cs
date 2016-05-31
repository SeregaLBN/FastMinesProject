using System;
using System.Linq;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;
using Windows.ApplicationModel.Background;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.uwp.res;
using fmg.uwp.res.img;
using Size = fmg.common.geom.Size;
using Rect = Windows.Foundation.Rect;
using FastMines.BackgroundTasks.Uap;

namespace fmg {
   public static class TileHelper {
      private static readonly Random Random = new Random(Guid.NewGuid().GetHashCode());
      private static readonly string TaskName = typeof(FastMinesTileUpdater).Name;
      private static readonly string TaskEntryPoint = typeof(FastMinesTileUpdater).FullName;

      public static async void RegisterBackgroundTask() {
         try {
            var backgroundAccessStatus = await BackgroundExecutionManager.RequestAccessAsync();
            if (backgroundAccessStatus == BackgroundAccessStatus.AllowedMayUseActiveRealTimeConnectivity ||
                backgroundAccessStatus == BackgroundAccessStatus.AllowedWithAlwaysOnRealTimeConnectivity)
            {
               await RemakeXmlAnew();

               var allTasks = BackgroundTaskRegistration.AllTasks.ToList();
               foreach (var task in allTasks)
                  if (task.Value.Name == TaskName)
                     task.Value.Unregister(true);

               var taskBuilder = new BackgroundTaskBuilder {Name = TaskName, TaskEntryPoint = TaskEntryPoint};
               taskBuilder.SetTrigger(new TimeTrigger(15, false));
               var registration = taskBuilder.Register();
               System.Diagnostics.Debug.WriteLine(registration.TaskId);
               registration.Completed += OnBackgroundTaskCompleted;
            }
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message.GetType().Name + ": " + ex.Message);
         }
      }

      private static async Task MakeXml(int part) {
         var xml = await GetXmlString(part);
         if (string.IsNullOrEmpty(xml))
            return;
         var file = await FastMinesTileUpdater.Location.CreateFileAsync(FastMinesTileUpdater.GetXmlFileName(part), CreationCollisionOption.ReplaceExisting);
         await FileIO.WriteTextAsync(file, xml, UnicodeEncoding.Utf8);
      }

      /// <summary> msdn.microsoft.com/en-us/library/windows/apps/hh761491.aspx
      /// msdn.microsoft.com/en-us/library/windows/apps/dn632423.aspx
      /// </summary>
      private static async Task<string> GetXmlString(int part) {
         try {
            //return await GetImagePath(false, 75, 75); // test one save to file
            return string.Format(@"<tile>
 <visual version='2'>
    <binding template='TileSquare150x150Image' fallback='TileSquareImage'>
      <image id='1' src='{0}' alt='FastMines'/>
    </binding>  
    <binding template='TileWide310x150ImageCollection' fallback='TileWideImageCollection'>
      <image id='1' src='{1}' alt='FastMines'/>
      <image id='2' src='{2}' alt='small image, row 1, column 1'/>
      <image id='3' src='{3}' alt='small image, row 1, column 2'/>
      <image id='4' src='{4}' alt='small image, row 2, column 1'/>
      <image id='5' src='{5}' alt='small image, row 2, column 2'/>
    </binding>
    <binding template='TileSquare310x310ImageCollection'>
      <image id='1' src='{6}' alt='FastMines'/>
      <image id='2' src='{7}' alt='small image 1 (left)'/>
      <image id='3' src='{8}' alt='small image 2 (left center)'/>
      <image id='4' src='{9}' alt='small image 3 (right center)'/>
      <image id='5' src='{10}' alt='small image 4 (right)'/>
    </binding>
    <binding template='TileSquare71x71Image'>
      <image id='1' src='{11}' alt='FastMines'/>
    </binding>  
</visual>
</tile>", await GetImagePath(part, 1, 150, 150),
            await GetImagePath(part, 1, 160, 150), await GetImagePath(part, 0, 75, 75), await GetImagePath(part, 0, 75, 75), await GetImagePath(part, 0, 75, 75), await GetImagePath(part, 0, 75, 75),
            await GetImagePath(part, 2, 310, 233), await GetImagePath(part, 0, 77, 77), await GetImagePath(part, 0, 77, 77), await GetImagePath(part, 0, 77, 77), await GetImagePath(part, 0, 77, 77),
            await GetImagePath(part, 1, 71, 71));
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
         return null;
      }

      private static async Task<string> GetImagePath(int part, int primary, int w, int h) {
         StorageFile storageFile;
         if (primary != 0) {
#if false
            var clr = Resources.DefaultBkColor;
            //switch (_random.Next() % 3) {
            //case 0: clr = clr.ToFmColor().Attenuate().ToWinColor(); break;
            //case 1: clr = clr.ToFmColor().Bedraggle().ToWinColor(); break;
            //}
            const uint margin = 2u;
            var loopMix = (Random.Next() % 8);
            var z = Math.Min(w, h);
            var bmp = Resources.GetImgLogo(new Size(z, z), clr, (uint)loopMix, margin);
            if (w != h) {
               var bk = new WriteableBitmap(w, h);
               bk.FillRectangle(0, 0, w, h, bmp.GetPixel(0, 0));
               var offsetX = (w - z)/2;
               var offsetY = (h - z)/2;
               bk.Blit(new Point(offsetX, offsetY), bmp, new Rect(0, 0, z, z), Colors.White, WriteableBitmapExtensions.BlendMode.None);
               bmp = bk;
            }
            storageFile = await SaveToFileLogo(part, bmp);
#elif false
            var bmp = new WriteableBitmap(w, h);
            var img1 = CreateRandomMosaicImage(w/2, h/2);
            var img2 = CreateRandomMosaicImage(w/2, h/2);
            var img3 = CreateRandomMosaicImage(w/2, h/2);
            var bmp1 = img1.Item2;
            var bmp2 = img2.Item2;
            var bmp3 = img3.Item2;
            bmp.Blit(new Rect(0, 0, bmp1.PixelWidth, bmp1.PixelHeight), bmp1, new Rect(0, 0, bmp1.PixelWidth, bmp1.PixelHeight));
            bmp.DrawRectangle(0, 0-1, bmp1.PixelWidth, bmp1.PixelHeight, Colors.Black);
            bmp.Blit(new Rect(w-bmp2.PixelWidth, h-bmp2.PixelHeight, bmp2.PixelWidth, bmp2.PixelHeight), bmp2, new Rect(0, 0, bmp2.PixelWidth, bmp2.PixelHeight));
            bmp.DrawRectangle(w-bmp2.PixelWidth, h-bmp2.PixelHeight-1, w, h, Colors.Black);
            bmp.Blit(new Rect(w-bmp3.PixelWidth, 0, bmp3.PixelWidth, bmp3.PixelHeight), bmp3, new Rect(0, 0, bmp3.PixelWidth, bmp3.PixelHeight));
            bmp.DrawRectangle(w-bmp3.PixelWidth, 0-1, w, bmp3.PixelHeight, Colors.Black);
            storageFile = await SaveToFileMosaic(part, /*w + "x" + h, */bmp, "Combi"+img1.Item1.getIndex()+img2.Item1.getIndex()+img3.Item1.getIndex());
#else
            var bmp = new WriteableBitmap(w, h);

            {
               var bmpLogo = await (
                  (primary==2)
                     ? Resources.GetImgLogoPng()
                     : Resources.GetImgLogoPng("TileSq150", 150));
               var rcLogoRegion = new Rect {
                  X = 0, Y = 0,
                  Width  = w,
                  Height = h
               };
               if (primary==2) {
                  if (part==1) {
                     rcLogoRegion.X = w/2;
                     rcLogoRegion.Width /= 2;
                  }
                  if (part==2) {
                     rcLogoRegion.Y = h/2;
                     rcLogoRegion.Height /= 2;
                  }
                  if (part==3 || part==4) {
                     rcLogoRegion.Width /= 2;
                  }
               }

               var rcDestLogo = new Rect {
                  X = rcLogoRegion.X + Math.Max(0, (rcLogoRegion.Width-rcLogoRegion.Height)/2),
                  Y = rcLogoRegion.Y + Math.Max(0, (rcLogoRegion.Height-rcLogoRegion.Width)/2),
                  Width = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height),
                  Height = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height)
               };
               bmp.Blit(rcDestLogo, bmpLogo, new Rect(0, 0, bmpLogo.PixelWidth, bmpLogo.PixelHeight));
            }

            var img1 = CreateRandomMosaicImage(w/2, h/2);
            var img2 = CreateRandomMosaicImage(w/2, h/2);
            var img3 = CreateRandomMosaicImage(w/2, h/2);
            var bmp1 = img1.Item2;
            var bmp2 = img2.Item2;
            var bmp3 = img3.Item2;
            if (part==1 || part==2)
               bmp.Blit(new Rect(0, 0, w/2, h/2), bmp1, new Rect(0, 0, bmp1.PixelWidth, bmp1.PixelHeight));
            if (part==2 || part==3)
               bmp.Blit(new Rect(w/2, 0, w/2, h/2), bmp2, new Rect(0, 0, bmp2.PixelWidth, bmp2.PixelHeight));
            if (part==3 || part==4)
               bmp.Blit(new Rect(w/2, h/2, w/2, h/2), bmp3, new Rect(0, 0, bmp3.PixelWidth, bmp3.PixelHeight));

            storageFile = await SaveToFileMosaic(part, /*w + "x" + h, */bmp, "Combi"+img1.Item1.GetIndex()+img2.Item1.GetIndex()+img3.Item1.GetIndex());
#endif
         } else {
            var img = CreateRandomMosaicImage(w, h);
            storageFile = await SaveToFileMosaic(part, /*w + "x" + h, */img.Item2, img.Item1);
         }
         return "ms-appdata:///local/" + storageFile.DisplayName;
      }

      public static Tuple<EMosaic, WriteableBitmap> CreateRandomMosaicImage(int w, int h) {
         var mosaicType = EMosaicEx.FromOrdinal(Random.Next() % EMosaicEx.GetValues().Length);
         var bkClr = ColorExt.RandomColor(Random).Brighter(0.45);
         var sizeField = mosaicType.SizeIcoField(true);
         sizeField.m += Random.Next() % 2;
         sizeField.n += Random.Next() % 3;
         const int bound = 3;
         const int ZoomKoef = 1;
         var img = new MosaicsImg(mosaicType, sizeField, new Size(w * ZoomKoef, h * ZoomKoef), new Bound(ZoomKoef * bound)) {
            BackgroundColor = bkClr,
            OnlySyncDraw = true
         };
         var bmp = img.Image;
         var pw = bmp.PixelWidth;
         var ph = bmp.PixelHeight;
         System.Diagnostics.Debug.Assert(img.Width == pw);
         System.Diagnostics.Debug.Assert(img.Height == ph);
         System.Diagnostics.Debug.Assert(w * ZoomKoef == pw);
         System.Diagnostics.Debug.Assert(h * ZoomKoef == ph);
         return new Tuple<EMosaic, WriteableBitmap>(mosaicType, bmp);
      }

      private static async Task<StorageFile> SaveToFileLogo(int part, WriteableBitmap writeableBitmap) {
         return await SaveToFile(part, "logo", writeableBitmap);
      }
      private static async Task<StorageFile> SaveToFileMosaic(int part, /*string filePrefix, */WriteableBitmap writeableBitmap, EMosaic mosaicType) {
         return await SaveToFileMosaic(part, writeableBitmap, mosaicType.GetMosaicClassName());
      }
      private static async Task<StorageFile> SaveToFileMosaic(int part, /*string filePrefix, */WriteableBitmap writeableBitmap, string fileDescript) {
         return await SaveToFile(part, /*filePrefix + "_" + */fileDescript, writeableBitmap);
      }
      private static async Task<StorageFile> SaveToFile(int part, string filePrefix, WriteableBitmap writeableBitmap) {
         return await writeableBitmap.SaveToFile(
            string.Format("{0}_{1}_{2}x{3}.png",
               part,
               filePrefix,
               writeableBitmap.PixelWidth, writeableBitmap.PixelHeight),
               FastMinesTileUpdater.Location);
      }

      public async static void OnBackgroundTaskCompleted(BackgroundTaskRegistration sender, BackgroundTaskCompletedEventArgs args) {
         //await RemakeXmlAnew();
      }

      private async static Task RemakeXmlAnew() {
         // claen obsolete png files
         for (var i=0; i<5; i++) {
            var xml = await FastMinesTileUpdater.GetXmlString(i);
            if (string.IsNullOrEmpty(xml))
               continue;
            // ms-appdata:///local/*.png
            var rgx = new Regex(@"ms-appdata:///local/(?<filePng>.+\.png)");
            foreach (var match in rgx.Matches(xml).Cast<Match>()) {
               var filePng = match.Groups["filePng"].Value;
               if (null == await FastMinesTileUpdater.Location.TryGetItemAsync(filePng))
                  continue; // file doesn't exist
               try {
                  var file = await FastMinesTileUpdater.Location.GetFileAsync(filePng);
                  await file.DeleteAsync();
               } catch (Exception ex) {
                  System.Diagnostics.Debug.WriteLine(string.Format("TileHelper::RemakeXmlAnew: {0}: {1}", ex.GetType().Name, ex.Message));
                  System.Diagnostics.Debug.Assert(false, ex.Message);
               }
            }
         }

         for (var i=0; i<5; i++)
            await MakeXml(i);
      }
   }
}