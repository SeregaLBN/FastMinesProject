using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.ui;

namespace fmg.common.notyfier {

    public class NotifyPropertyChangedTest {

        [SetUp]
        public void Setup() { }

        [Test]
        public void NotifyPropertyChangedSyncTest() {
            var rand = new Random(Environment.TickCount);
            int countFiredEvents = 3 + rand.Next(10);
            int countReceivedEvents = 0;

            void listener(PropertyChangedEventArgs ev) { ++countReceivedEvents; }
            using (var notifier = new NotifyPropertyChanged(null, listener, false)) {
                for (int i = 0; i < countFiredEvents; ++i)
                    notifier.OnPropertyChanged("propertyName ");
            }
            Assert.AreEqual(countFiredEvents, countReceivedEvents);
        }

        [Test]
        public void NotifyPropertyChangedAsyncTest() {
            var allTask = new List<Task>();
            Factory.DEFERR_INVOKER = doRun => {
                Task task = Task.Run(doRun); // Task.Delay(1000).ContinueWith(t => doRun() ); //
                allTask.Add(task);
            };
            int countFiredEvents = 3 + new Random(Environment.TickCount).Next(10);
            int countReceivedEvents = 0;
            object firedValue = null;

            void listener(PropertyChangedEventArgs ev) {
                ++countReceivedEvents;
                firedValue = (ev as PropertyChangedExEventArgs<string>).NewValue;
            }
            const string prefix = " Value ";
            using (var notifier = new NotifyPropertyChanged(null, listener, true)) {
                for (int i=0; i<countFiredEvents; ++i)
                    notifier.OnPropertyChanged(null, prefix + i, "propertyName");

                Task.WaitAll(allTask.ToArray());
            }

            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
        }

    }

}