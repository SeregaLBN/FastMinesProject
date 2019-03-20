using System;
using System.ComponentModel;
using System.Threading.Tasks;
using Windows.UI.Core;
using Windows.ApplicationModel.Core;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using fmg.uwp.utils;

namespace fmg.common.notifier {

    [TestClass]
    public class NotifyPropertyChangedUwpTest {

        [TestInitialize]
        public void Setup() {
            StaticInitializer.Init();
        }

        [TestMethod]
        public async Task NotifyPropertyChangedAsyncTest() {
            int countFiredEvents = 3 + new Random(Environment.TickCount).Next(10);
            int countReceivedEvents = 0;
            object firedValue = null;

            void listener(PropertyChangedEventArgs ev) {
                ++countReceivedEvents;
                firedValue = (ev as PropertyChangedExEventArgs<string>).NewValue;
            }
            const string prefix = "Value ";
            using (var notifier = new NotifyPropertyChanged(null, listener, true)) {
                for (int i=0; i<countFiredEvents; ++i)
                    notifier.FirePropertyChanged(null, prefix + i, "propertyName");

                await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                    CoreDispatcherPriority.Low,
                    () => {
                        // none
                    });
            }


            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
        }

    }

}
