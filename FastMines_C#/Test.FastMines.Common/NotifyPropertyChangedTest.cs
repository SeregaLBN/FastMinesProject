using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.ui;

namespace fmg.common.notyfier {

   public class Tests {

      [SetUp]
      public void Setup() { }

      [Test]
      public void NotifyPropertyChangedSyncTest() {
         var rand = new Random(Environment.TickCount);
         int countFiredEvents = 3 + rand.Next(10);
         int countReceivedEvents = 0;

         void listener(PropertyChangedEventArgs ev) { ++countReceivedEvents; }
         using (var notifyPropertyChanged = new NotifyPropertyChanged(null, listener, false)) {
            for (int i = 0; i < countFiredEvents; ++i)
               notifyPropertyChanged.OnPropertyChanged("propertyName ");

            Assert.AreEqual(countFiredEvents, countReceivedEvents);
         }
      }

      [Test]
      public void NotifyPropertyChangedAsyncTest() {
         var allTask = new List<Task>();
         Factory.DEFERR_INVOKER = doRun => {
            Task task = Task.Run(doRun); // Task.Delay(1).ContinueWith(t => doRun() );
            allTask.Add(task);
         };
         var rand = new Random(Environment.TickCount);
         int countFiredEvents = 3 + rand.Next(10);
         int countReceivedEvents = 0;
         object firedValue = null;

         void listener(PropertyChangedEventArgs ev) {
            ++countReceivedEvents;
            firedValue = (ev as PropertyChangedExEventArgs<string>).NewValue;
         }
         using (NotifyPropertyChanged notifyPropertyChanged = new NotifyPropertyChanged(null, listener, true)) {
            const string prefix = " Value ";
            for (int i=0; i<countFiredEvents; ++i)
               notifyPropertyChanged.OnPropertyChanged(null, prefix + i, "propertyName");

            Task.WaitAll(allTask.ToArray());

            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
         }
      }
   }

}