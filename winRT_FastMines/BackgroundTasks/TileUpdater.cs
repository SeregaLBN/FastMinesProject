using System;
using System.IO;
using System.Threading.Tasks;
using Windows.Storage;
using Windows.UI.Notifications;
using Windows.Data.Xml.Dom;
using Windows.ApplicationModel.Background;

namespace BackgroundTasks {
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
         var xml = await GetXmlString();
         if (string.IsNullOrEmpty(xml))
            return;

         // Create a tile update manager for the specified syndication feed.
         var updater = TileUpdateManager.CreateTileUpdaterForApplication();
         updater.Clear(); // disable tile - set as default

         updater.EnableNotificationQueue(true);

         var tile = CreateNotification(xml);
         TileUpdateManager.CreateTileUpdaterForApplication().Update(tile);
      }

      /// <summary> msdn.microsoft.com/en-us/library/windows/apps/hh761491.aspx </summary>
      private async Task<string> GetXmlString() {
         try {
            var file = await ApplicationData.Current.TemporaryFolder.GetFileAsync("tiles.xml");
            return await FileIO.ReadTextAsync(file);
         }
         catch (FileNotFoundException) {
            return null;
         }
      }

      private static TileNotification CreateNotification(string xml) {
         var xmlDocument = new XmlDocument();
         xmlDocument.LoadXml(xml);
         return new TileNotification(xmlDocument);
      }
   }
}