using System;
using System.Linq;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.Graphics.Display;
using Windows.ApplicationModel.Background;
using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.utils.win2d;
using Size = fmg.common.geom.Size;
using Rect = Windows.Foundation.Rect;
using FastMines.BackgroundTasks.Uwp;
using MosaicsCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;

namespace fmg {
   public static class TileHelper {
      private static readonly Random Random = new Random(Guid.NewGuid().GetHashCode());
      private static readonly string TaskName = typeof(FastMinesTileUpdater).Name;
      private static readonly string TaskEntryPoint = typeof(FastMinesTileUpdater).FullName;

      private static ICanvasResourceCreator Rc => CanvasDevice.GetSharedDevice();

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
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var bmp = new CanvasRenderTarget(Rc, w, h, dpi);
            using (var ds = bmp.CreateDrawingSession()) {
               {
                  var bmpLogo = await (
                     (primary == 2)
                        ? Resources.GetImgLogoPng("TileSq150", 100, Rc)
                        : Resources.GetImgLogoPng("TileSq150", 150, Rc));
                  var rcLogoRegion = new Rect {
                     X = 0,
                     Y = 0,
                     Width = w,
                     Height = h
                  };
                  if (primary == 2) {
                     if (part == 1) {
                        rcLogoRegion.X = w / 2.0;
                        rcLogoRegion.Width /= 2.0;
                     }
                     if (part == 2) {
                        rcLogoRegion.Y = h / 2.0;
                        rcLogoRegion.Height /= 2.0;
                     }
                     if (part == 3 || part == 4) {
                        rcLogoRegion.Width /= 2.0;
                     }
                  }

                  var rcDestLogo = new Rect {
                     X = rcLogoRegion.X + Math.Max(0, (rcLogoRegion.Width - rcLogoRegion.Height) / 2),
                     Y = rcLogoRegion.Y + Math.Max(0, (rcLogoRegion.Height - rcLogoRegion.Width) / 2),
                     Width = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height),
                     Height = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height)
                  };
                  ds.DrawImage(bmpLogo, rcDestLogo, new Rect(0, 0, bmpLogo.Size.Width, bmpLogo.Size.Height));
               }

               var img1 = CreateRandomMosaicImage(w / 2, h / 2);
               var img2 = CreateRandomMosaicImage(w / 2, h / 2);
               var img3 = CreateRandomMosaicImage(w / 2, h / 2);
               var bmp1 = img1.Item2;
               var bmp2 = img2.Item2;
               var bmp3 = img3.Item2;
               if (part == 1 || part == 2)
                  ds.DrawImage(bmp1, new Rect(0, 0, w / 2.0, h / 2.0), new Rect(0, 0, bmp1.Size.Width, bmp1.Size.Height));
               if (part == 2 || part == 3)
                  ds.DrawImage(bmp2, new Rect(w / 2.0, 0, w / 2.0, h / 2.0), new Rect(0, 0, bmp2.Size.Width, bmp2.Size.Height));
               if (part == 3 || part == 4)
                  ds.DrawImage(bmp3, new Rect(w / 2.0, h / 2.0, w / 2.0, h / 2.0), new Rect(0, 0, bmp3.Size.Width, bmp3.Size.Height));

               storageFile = await SaveToFileMosaic(part, /*w + "x" + h, */bmp, "Combi" + img1.Item1.GetIndex() + img2.Item1.GetIndex() + img3.Item1.GetIndex());
            }
         } else {
            var img = CreateRandomMosaicImage(w, h);
            storageFile = await SaveToFileMosaic(part, /*w + "x" + h, */img.Item2, img.Item1);
         }
         return "ms-appdata:///local/" + storageFile.DisplayName;
      }

      public static Tuple<EMosaic, CanvasBitmap> CreateRandomMosaicImage(int w, int h) {
         var mosaicType = EMosaicEx.FromOrdinal(Random.Next() % EMosaicEx.GetValues().Length);
         var bkClr = ColorExt.RandomColor(Random).Brighter(0.45);
         var sizeField = mosaicType.SizeIcoField(true);
         sizeField.m += Random.Next() % 2;
         sizeField.n += Random.Next() % 3;
         const int bound = 3;
         const int zoomKoef = 1;
         var img = new MosaicsCanvasBmp(mosaicType, sizeField, Rc) {
            Size = new Size(w * zoomKoef, h * zoomKoef),
            Padding = new Bound(zoomKoef * bound),
            BackgroundColor = bkClr
         };
         var bmp = img.Image;
         var pw = bmp.Size.Width;
         var ph = bmp.Size.Height;
         System.Diagnostics.Debug.Assert(img.Width == pw);
         System.Diagnostics.Debug.Assert(img.Height == ph);
         System.Diagnostics.Debug.Assert(w * zoomKoef == pw);
         System.Diagnostics.Debug.Assert(h * zoomKoef == ph);
         return new Tuple<EMosaic, CanvasBitmap>(mosaicType, bmp);
      }

      private static async Task<StorageFile> SaveToFileLogo(int part, CanvasBitmap CanvasBitmap) {
         return await SaveToFile(part, "logo", CanvasBitmap);
      }
      private static async Task<StorageFile> SaveToFileMosaic(int part, /*string filePrefix, */CanvasBitmap CanvasBitmap, EMosaic mosaicType) {
         return await SaveToFileMosaic(part, CanvasBitmap, mosaicType.GetMosaicClassName());
      }
      private static async Task<StorageFile> SaveToFileMosaic(int part, /*string filePrefix, */CanvasBitmap CanvasBitmap, string fileDescript) {
         return await SaveToFile(part, /*filePrefix + "_" + */fileDescript, CanvasBitmap);
      }
      private static async Task<StorageFile> SaveToFile(int part, string filePrefix, CanvasBitmap CanvasBitmap) {
         return await CanvasBitmap.SaveToFile(
            string.Format("{0}_{1}_{2}x{3}.png",
               part,
               filePrefix,
               CanvasBitmap.Size.Width, CanvasBitmap.Size.Height),
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