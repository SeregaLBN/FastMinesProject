using System;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Storage;
using Windows.UI.Notifications;
using Windows.Data.Xml.Dom;
using Windows.ApplicationModel.Background;

namespace FastMines.Uwp.BackgroundTasks {

    /// <summary> http://msdn.microsoft.com/en-us/library/windows/apps/xaml/jj991805.aspx </summary>
    public sealed class TileUpdater : IBackgroundTask {

        public async void Run(IBackgroundTaskInstance taskInstance) {
            // Get a deferral, to prevent the task from closing prematurely while asynchronous code is still running.
            BackgroundTaskDeferral deferral = taskInstance.GetDeferral();

            // Update the live tile with the feed items.
            await UpdateTiles();

            // Inform the system that the task is finished.
            deferral.Complete();
        }

        private async Task UpdateTiles() {
            // Create a tile update manager for the specified syndication feed.
            var updater = TileUpdateManager.CreateTileUpdaterForApplication();
            updater.Clear(); // disable tile - set as default
            updater.EnableNotificationQueue(true);

            for (var i = 0; i < 5; i++) {
                var xml = await GetXmlStringImpl(i);
                if (string.IsNullOrEmpty(xml))
                    continue;

                var tile = CreateNotification(xml);
                //tile.ExpirationTime = DateTime.Now + TimeSpan.FromSeconds(10); // DateTimeOffset.UtcNow.AddSeconds(10);
                //tile.ExpirationTime = DateTime.UtcNow.AddSeconds(i*10);
                //tile.ExpirationTime = DateTime.UtcNow.AddHours(i).AddMinutes(15);
                tile.Tag = i.ToString();
                updater.Update(tile);
                //updater.StartPeriodicUpdate(new Uri(), PeriodicUpdateRecurrence.HalfHour);
            }
        }

        public static IAsyncOperation<string> GetXmlString(int part) { return GetXmlStringImpl(part).AsAsyncOperation(); }
        private static async Task<string> GetXmlStringImpl(int part) {
            try {
                var xmlFileName = GetXmlFileName(part);
                if (null == await TileUpdater.Location.TryGetItemAsync(xmlFileName))
                    return null; // file doesn't exist
                var file = await Location.GetFileAsync(xmlFileName);
                return await FileIO.ReadTextAsync(file);
            } catch (Exception ex) {
                System.Diagnostics.Debug.WriteLine(string.Format("FastMinesTileUpdater::GetXmlStringHelper: {0}: {1}", ex.GetType().Name, ex.Message));
                System.Diagnostics.Debug.Assert(false, ex.Message);
                return null;
            }
        }

        private static TileNotification CreateNotification(string xml) {
            var xmlDocument = new XmlDocument();
            xmlDocument.LoadXml(xml);
            return new TileNotification(xmlDocument);
        }

        public static StorageFolder Location { get { return ApplicationData.Current.LocalFolder; } }

        public static string GetXmlFileName(int part) { return string.Format("tiles_{0}.xml", part); }

    }

}
