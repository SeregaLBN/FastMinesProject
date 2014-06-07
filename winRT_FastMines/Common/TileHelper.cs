using System;
using System.Net;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI;
using Windows.UI.Xaml.Media.Imaging;
using Windows.ApplicationModel.Background;
using ua.ksn;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.res;
using Size = ua.ksn.geom.Size;

namespace FastMines {
   public class TileHelper {
      private static readonly Random Random = new Random();
      private static readonly string TaskName = typeof(BackgroundTasks.TileUpdater).Name;
      private static readonly string TaskEntryPoint = typeof(BackgroundTasks.TileUpdater).FullName;

      public static async void RegisterBackgroundTask() {
         try {
            await TileXmlMaker();

            var backgroundAccessStatus = await BackgroundExecutionManager.RequestAccessAsync();
            if (backgroundAccessStatus == BackgroundAccessStatus.AllowedMayUseActiveRealTimeConnectivity ||
                backgroundAccessStatus == BackgroundAccessStatus.AllowedWithAlwaysOnRealTimeConnectivity) {
               foreach (var task in BackgroundTaskRegistration.AllTasks) {
                  //if (task.Value.Name == taskName)
                     task.Value.Unregister(true);
               }

               var taskBuilder = new BackgroundTaskBuilder {Name = TaskName, TaskEntryPoint = TaskEntryPoint};
               taskBuilder.SetTrigger(new TimeTrigger(15, false));
               var registration = taskBuilder.Register();
               System.Diagnostics.Debug.WriteLine(registration.TaskId);
            }
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message.GetType().Name + ": " + ex.Message);
         }
      }

      private static async Task TileXmlMaker() {
         var xml = await GetXmlString();
         var file = await ApplicationData.Current.TemporaryFolder.CreateFileAsync("tiles.xml", CreationCollisionOption.ReplaceExisting);
         await FileIO.WriteTextAsync(file, xml, UnicodeEncoding.Utf8);
      }

      /// <summary> msdn.microsoft.com/en-us/library/windows/apps/hh761491.aspx </summary>
      private static async Task<string> GetXmlString() {
         try {
            //return await GetImagePath(false, 75, 75); // test one save to file
            return string.Format(@"<tile>
 <visual version='2'>
    <binding template='TileSquare150x150Image' fallback='TileSquareImage'>
      <image id='1' src='{0}' alt='FastMines'/>
    </binding>  
    <binding template='TileWide310x150Image' fallback='TileWideImage'>
      <image id='1' src='{1}' alt='mineswepper'/>
    </binding>  
    <binding template='TileWide310x150ImageCollection' fallback='TileWideImageCollection'>
      <image id='1' src='{2}' alt='FastMines'/>
      <image id='2' src='{3}' alt='small image, row 1, column 1'/>
      <image id='3' src='{4}' alt='small image, row 1, column 2'/>
      <image id='4' src='{5}' alt='small image, row 2, column 1'/>
      <image id='5' src='{6}' alt='small image, row 2, column 2'/>
    </binding>  
    <binding template='TileSquare310x310Image'>
      <image id='1' src='{7}' alt='alt text'/>
    </binding>  
    <binding template='TileSquare310x310ImageCollection'>
      <image id='1' src='{8}' alt='FastMines'/>
      <image id='2' src='{9}' alt='small image 1 (left)'/>
      <image id='3' src='{10}' alt='small image 2 (left center)'/>
      <image id='4' src='{11}' alt='small image 3 (right center)'/>
      <image id='5' src='{12}' alt='small image 4 (right)'/>
    </binding>  
    <binding template='TileSquare71x71Image'>
      <image id='1' src='{13}' alt='FastMines'/>
    </binding>  
</visual>
</tile>", await GetImagePath(true, 150, 150),
        await GetImagePath(true, 310, 150),
        await GetImagePath(true, 160, 150), await GetImagePath(false, 75, 75), await GetImagePath(false, 75, 75), await GetImagePath(false, 75, 75), await GetImagePath(false, 75, 75),
        await GetImagePath(true, 310, 310),
        await GetImagePath(true, 310, 233), await GetImagePath(false, 77, 77), await GetImagePath(false, 77, 77), await GetImagePath(false, 77, 77), await GetImagePath(false, 77, 77),
        await GetImagePath(true, 71, 71));

         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
            return string.Format(@"<tile>
  <visual>
    <binding template='TileSquareBlock'>
      <text id='1'>{0}</text>
      <text id='2'>{1}</text>
    </binding> 
    <binding template='TileWideText03'>
      <text id='1'>{0}: {1}</text>
    </binding>  
  </visual>
</tile>", ex.GetType().Name, WebUtility.UrlEncode(ex.Message));
         }
      }

      private static async Task<string> GetImagePath(bool primary, int w, int h) {
         StorageFile storageFile;
         if (primary) {
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
            storageFile = await SaveToFileLogo(bmp);
         } else {
            var mosaicType = EMosaicEx.fromOrdinal(Random.Next() % Enum.GetValues(typeof(EMosaic)).Length);
            var clr = ColorExt.RandomColor(Random).Attenuate();
            var sizeField = mosaicType.SizeIcoField(true);
            const int bound = 3;
            var area = CellFactory.CreateAttributeInstance(mosaicType, 0).CalcOptimalArea(250, sizeField, new Size(w - bound*2, h - bound*2));
            var img = Resources.GetImgMosaic(mosaicType, sizeField, area, clr.ToWinColor(), new Size(bound, bound));
            var bmp = img.GetImage(false);
            storageFile = await SaveToFileMosaic(/*w + "x" + h, */bmp, mosaicType);
         }
         return "ms-appdata:///temp/" + storageFile.DisplayName;
      }

      private static async Task<StorageFile> SaveToFileLogo(WriteableBitmap writeableBitmap) {
         return await SaveToFile("logo", writeableBitmap);
      }
      private static async Task<StorageFile> SaveToFileMosaic(/*string filePrefix, */WriteableBitmap writeableBitmap, EMosaic mosaicType) {
         return await SaveToFile(/*filePrefix + "_" + */mosaicType.getMosaicClassName(), writeableBitmap);
      }
      private static async Task<StorageFile> SaveToFile(string filePrefix, WriteableBitmap writeableBitmap) {
         return await writeableBitmap.SaveToFile(
            string.Format("{0}_{1}x{2}.png",
               filePrefix,
               writeableBitmap.PixelWidth, writeableBitmap.PixelHeight),
               ApplicationData.Current.TemporaryFolder);
      }
   }
}