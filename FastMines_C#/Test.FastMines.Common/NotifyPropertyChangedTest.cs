using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading.Tasks;
using NUnit.Framework;

namespace fmg.common.notyfier {

   public class Tests {

      [SetUp]
      public void Setup() { }

      [Test]
      public void NotifyPropertyChangedSyncTest() {
         using (var notifyPropertyChanged = new NotifyPropertyChanged(null, false)) {
            var rand = new Random(Environment.TickCount);
            int countFiredEvents = 3 + rand.Next(10);
            int countReceivedEvents = 0;

            PropertyChangedEventHandler listener = (sender, ev) => { ++countReceivedEvents; };
            notifyPropertyChanged.PropertyChanged += listener;
            for (int i = 0; i < countFiredEvents; ++i)
               notifyPropertyChanged.OnPropertyChanged("propertyName ");
            notifyPropertyChanged.PropertyChanged -= listener;

            Assert.AreEqual(countFiredEvents, countReceivedEvents);
         }
      }

      [Test]
      public void NotifyPropertyChangedAsyncTest() {
         //ExecutorService scheduler = Executors.newScheduledThreadPool(1);
         NotifyPropertyChanged.DEFERR_INVOKER = async doRun => {
            await Task.Run(doRun);
         };
         using (NotifyPropertyChanged notifyPropertyChanged = new NotifyPropertyChanged(null, true)) {
            var rand = new Random(Environment.TickCount);
            int countFiredEvents = 3 + rand.Next(10);
            int countReceivedEvents = 0;
            object firedValue = null;

            PropertyChangedEventHandler listener = (sender, ev) => {
               ++countReceivedEvents;
               firedValue = (ev as PropertyChangedExEventArgs<string>).NewValue;
            };
            notifyPropertyChanged.PropertyChanged += listener;
            const string prefix = " Value ";
            for (int i=0; i<countFiredEvents; ++i)
               notifyPropertyChanged.OnPropertyChanged(null, prefix + i, "propertyName");

            // scheduler.awaitTermination(100, TimeUnit.MILLISECONDS);
            notifyPropertyChanged.PropertyChanged -= listener;

            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
         }
      }
   }

}