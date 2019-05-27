using System;
using System.Linq;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.Graphics.Display;
using Windows.ApplicationModel.Background;
using Microsoft.Graphics.Canvas;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Utils.Win2d;
using Fmg.Uwp.Img.Win2d;
using Rect = Windows.Foundation.Rect;
using FastMines.Uwp.BackgroundTasks;
using MosaicsCanvasBmp = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpController;

namespace Fmg {

   public static class TileHelper {

        private const int MaxXmls = 5; // TileUpdater.MaxXmls;

        private static readonly string TaskName = typeof(TileUpdater).Name;
        private static readonly string TaskEntryPoint = typeof(TileUpdater).FullName;

        private static ICanvasResourceCreator Rc => CanvasDevice.GetSharedDevice();

        public static async Task RegisterBackgroundTask() {
            try {
                var backgroundAccessStatus = await BackgroundExecutionManager.RequestAccessAsync();
                if (backgroundAccessStatus == BackgroundAccessStatus.AlwaysAllowed ||
                    backgroundAccessStatus == BackgroundAccessStatus.AllowedSubjectToSystemPolicy)
                {
                    await RemakeXmlAnew();

                    var allTasks = BackgroundTaskRegistration.AllTasks.ToList();
                    foreach (var task in allTasks)
                        if (task.Value.Name == TaskName)
                            task.Value.Unregister(true);

                    var taskBuilder = new BackgroundTaskBuilder {Name = TaskName, TaskEntryPoint = TaskEntryPoint};
                    taskBuilder.Name = typeof(TileUpdater).Name;
                    taskBuilder.TaskEntryPoint = typeof(TileUpdater).FullName;
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
            var xml = await await AsyncRunner.ExecuteFromUiLaterAsync(() => GetXmlString(part));
            if (string.IsNullOrEmpty(xml))
                return;
            var file = await TileUpdater.Location.CreateFileAsync(TileUpdater.GetXmlFileName(part), CreationCollisionOption.ReplaceExisting);
            await FileIO.WriteTextAsync(file, xml, UnicodeEncoding.Utf8);
        }

        /// <summary> msdn.microsoft.com/en-us/library/windows/apps/hh761491.aspx
        /// msdn.microsoft.com/en-us/library/windows/apps/dn632423.aspx
        /// </summary>
        private static async Task<string> GetXmlString(int part) {
            try {
                string deviceFamilyVersion = Windows.System.Profile.AnalyticsInfo.VersionInfo.DeviceFamilyVersion;
                ulong version = ulong.Parse(deviceFamilyVersion);
                //ulong major = (version & 0xFFFF000000000000L) >> 48;
                //ulong minor = (version & 0x0000FFFF00000000L) >> 32;
                ulong build = (version & 0x00000000FFFF0000L) >> 16;
                //ulong revision = (version & 0x000000000000FFFFL);
                //var osVersion = $"{major}.{minor}.{build}.{revision}";
                var neew = build >= 10572; // https://docs.microsoft.com/ca-es/windows/uwp/design/shell/tiles-and-notifications/special-tile-templates-catalog#people-tile-template

                //return await GetImagePath(false, 75, 75); // test one save to file
                return string.Format(
@"<tile>
    <visual version='2'>
        <binding template='{0}'>
            <image id='1' src='{1}' alt='FastMines'/>
        </binding>
        <binding template='{2}'>
            <image id='1' src='{3}' alt='FastMines'/>
        </binding>
        <binding template='{4}'>
            <image id='1' src='{5}' alt='FastMines'/>
            <image id='2' src='{6}' alt='small image, row 1, column 1'/>
            <image id='3' src='{7}' alt='small image, row 1, column 2'/>
            <image id='4' src='{8}' alt='small image, row 2, column 1'/>
            <image id='5' src='{9}' alt='small image, row 2, column 2'/>
        </binding>
        <binding template='{10}'>
            <image id='1' src='{11}' alt='FastMines'/>
            <image id='2' src='{12}' alt='small image 1 (left)'/>
            <image id='3' src='{13}' alt='small image 2 (left center)'/>
            <image id='4' src='{14}' alt='small image 3 (right center)'/>
            <image id='5' src='{15}' alt='small image 4 (right)'/>
        </binding>
    </visual>
</tile>",
                    neew ? "TileSmall" : "TileSquare71x71Image",
                    await GetImagePath(part, 0, -1, 1, 71, 71),

                    neew ? "TileMedium" : "TileSquare150x150Image", // TileSquareImage
                    await GetImagePath(part, 1, -1, 1, 150, 150),

                    neew ? "TileWide" : "TileWide310x150ImageCollection", // TileWideImageCollection
                    await GetImagePath(part, 2, 0, 1, 160, 150),
                    await GetImagePath(part, 2, 1, 0, 75, 75),
                    await GetImagePath(part, 2, 2, 0, 75, 75),
                    await GetImagePath(part, 2, 3, 0, 75, 75),
                    await GetImagePath(part, 2, 4, 0, 75, 75),

                    neew ? "TileLarge" : "TileSquare310x310ImageCollection",
                    await GetImagePath(part, 3, 0, 2, 310, 233),
                    await GetImagePath(part, 3, 1, 0, 77, 77),
                    await GetImagePath(part, 3, 2, 0, 77, 77),
                    await GetImagePath(part, 3, 3, 0, 77, 77),
                    await GetImagePath(part, 3, 4, 0, 77, 77)
                );
            } catch (Exception ex) {
                System.Diagnostics.Debug.Assert(false, ex.Message);
            }
            return null;
        }

        private static async Task<string> GetImagePath(int part, int templateIndex, int imageIndex, int primary, int w, int h) {
            StorageFile storageFile;
            string makeFileDescription() {
                return part + "_" + templateIndex + ((imageIndex < 0) ? "" : ("_" + imageIndex));
            }
            if (primary != 0) {
                Rect rcDestLogo;
                {
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

                    rcDestLogo = new Rect {
                        X = rcLogoRegion.X + Math.Max(0, (rcLogoRegion.Width - rcLogoRegion.Height) / 2),
                        Y = rcLogoRegion.Y + Math.Max(0, (rcLogoRegion.Height - rcLogoRegion.Width) / 2),
                        Width = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height),
                        Height = Math.Min(rcLogoRegion.Width, rcLogoRegion.Height)
                    };
                }

                float dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                using (var bmp = new CanvasRenderTarget(Rc, w, h, dpi)) {
                using (var bmpLogo = new Logo.CanvasBmpController(Rc)) {
                using (var bmp1 = CreateRandomMosaicImage(w / 2, h / 2)) {
                using (var bmp2 = CreateRandomMosaicImage(w / 2, h / 2)) {
                using (var bmp3 = CreateRandomMosaicImage(w / 2, h / 2)) {
                    var td = new TestDrawing("FastMines tiles");
                    td.ApplySettings(bmpLogo, true);
                    int size = (primary == 2)
                                        ? 150  // hint: see image size ./res/Logo/TileSq150/Logo.scale-100.png
                                        : 225; // hint: see image size ./res/Logo/TileSq150/Logo.scale-150.png
                    bmpLogo.Model.Size = new SizeDouble(size, size);
                    bmpLogo.Model.BackgroundColor = Color.Transparent;
                    var p = bmpLogo.Model.Padding;
                    bmpLogo.Model.Padding = new BoundDouble(Math.Abs(p.Left), Math.Abs(p.Top), Math.Abs(p.Right), Math.Abs(p.Bottom));

                    using (var ds = bmp.CreateDrawingSession()) {
                        ds.Clear(Color.Transparent.ToWinColor());
                        //ds.FillRectangle(0, 0, w, h, Color.Red.ToWinColor());
                        ds.DrawImage(bmpLogo.Image, rcDestLogo, new Rect(0, 0, bmpLogo.Size.Width, bmpLogo.Size.Height));

                        if (part == 1 || part == 2)
                            ds.DrawImage(bmp1.Image, new Rect(      0,       0, w / 2.0, h / 2.0), new Rect(0, 0, bmp1.Size.Width, bmp1.Size.Height));
                        if (part == 2 || part == 3)
                            ds.DrawImage(bmp2.Image, new Rect(w / 2.0,       0, w / 2.0, h / 2.0), new Rect(0, 0, bmp2.Size.Width, bmp2.Size.Height));
                        if (part == 3 || part == 4)
                            ds.DrawImage(bmp3.Image, new Rect(w / 2.0, h / 2.0, w / 2.0, h / 2.0), new Rect(0, 0, bmp3.Size.Width, bmp3.Size.Height));
#if DEBUG

                        ds.DrawText(makeFileDescription(), 0, 0, Color.Red.ToWinColor());
#endif
                    }
                    storageFile = await SaveToFileMosaic(bmp, makeFileDescription());
                }}}}}
            } else {
                using (var img = CreateRandomMosaicImage(w, h)) {
#if DEBUG
                    float dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
                    using (var bmp = new CanvasRenderTarget(Rc, w, h, dpi)) {
                    using (var ds = bmp.CreateDrawingSession()) {
                        ds.Clear(Color.Transparent.ToWinColor());
                        ds.DrawImage(img.Image, new Rect(0, 0, w, h), new Rect(0, 0, img.Size.Width, img.Size.Height));
                        ds.DrawText(makeFileDescription(), 0, 0, Color.Red.ToWinColor());
                        storageFile = await SaveToFileMosaic(bmp, makeFileDescription());
                    }}
#else
                    storageFile = await SaveToFileMosaic(img, makeFileDescription());
#endif
                }
            }
            return "ms-appdata:///local/" + storageFile.DisplayName;
        }

        public static MosaicsCanvasBmp CreateRandomMosaicImage(int w, int h) {
            Random rnd = ThreadLocalRandom.Current;
            var mosaicType = EMosaicEx.FromOrdinal(rnd.Next() % EMosaicEx.GetValues().Length);
            var sizeField = mosaicType.SizeIcoField(true);
            sizeField.m += rnd.Next() % 2;
            sizeField.n += rnd.Next() % 3;
            const int bound = 3;
            const double zoomKoef = 2;
            var img = new MosaicsCanvasBmp(Rc);
            var td = new TestDrawing("FastMines tiles");
            td.ApplySettings(img, true);
            var m = img.Model;
            m.MosaicType = mosaicType;
            m.SizeField = sizeField;
            m.Size = new SizeDouble(w * zoomKoef, h * zoomKoef);
            m.Padding = new BoundDouble(zoomKoef * bound);
            m.BackgroundColor = Color.Transparent;
            var bmp = img.Image;
            var pw = bmp.Size.Width;
            var ph = bmp.Size.Height;
            System.Diagnostics.Debug.Assert(img.Size.Width == pw);
            System.Diagnostics.Debug.Assert(img.Size.Height == ph);
            System.Diagnostics.Debug.Assert(w * zoomKoef == pw);
            System.Diagnostics.Debug.Assert(h * zoomKoef == ph);
            return img;
        }

        private static async Task<StorageFile> SaveToFileMosaic(MosaicsCanvasBmp img, string fileDescript) {
            return await SaveToFileMosaic(
                await AsyncRunner.ExecuteFromUiLaterAsync(() => img.Image, Windows.UI.Core.CoreDispatcherPriority.Low),
                fileDescript);
        }
        private static async Task<StorageFile> SaveToFileMosaic(CanvasBitmap canvasBitmap, string fileDescript) {
            return await SaveToFile(fileDescript, canvasBitmap);
        }
        private static async Task<StorageFile> SaveToFile(string filePrefix, CanvasBitmap canvasBitmap) {
            return await canvasBitmap.SaveToFile(string.Format("{0}.png", filePrefix), TileUpdater.Location);
        }

        public static void OnBackgroundTaskCompleted(BackgroundTaskRegistration sender, BackgroundTaskCompletedEventArgs args) {
            //await RemakeXmlAnew();
        }

        private async static Task RemakeXmlAnew() {
            // claen obsolete png files
            for (var i = 0; i < MaxXmls; i++) {
                var xml = await TileUpdater.GetXmlString(i);
                if (string.IsNullOrEmpty(xml))
                    continue;
                // ms-appdata:///local/*.png
                var rgx = new Regex(@"ms-appdata:///local/(?<filePng>.+\.png)");
                foreach (var match in rgx.Matches(xml).Cast<Match>()) {
                    var filePng = match.Groups["filePng"].Value;
                    if (null == await TileUpdater.Location.TryGetItemAsync(filePng))
                        continue; // file doesn't exist
                    try {
                        var file = await TileUpdater.Location.GetFileAsync(filePng);
                        await file.DeleteAsync();
                    } catch (Exception ex) {
                        System.Diagnostics.Debug.WriteLine(string.Format("TileHelper::RemakeXmlAnew: {0}: {1}", ex.GetType().Name, ex.Message));
                        System.Diagnostics.Debug.Assert(false, ex.Message);
                    }
                }
            }

            for (var i = 0; i < MaxXmls; i++)
                await MakeXml(i);
        }

    }

}
