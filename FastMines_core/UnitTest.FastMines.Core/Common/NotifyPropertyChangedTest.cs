using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.ui;
using fmg.core.mosaic;

namespace fmg.common.notyfier {

    public class NotifyPropertyChangedTest {

        [OneTimeSetUp]
        public void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put(">" + nameof(NotifyPropertyChangedTest) + "::" + nameof(Setup));

            MosaicModelTest.StaticInitializer();

            //Observable.Just("UI factory inited...").Subscribe(LoggerSimple.Put);
        }

        [SetUp]
        public void Before() {
            LoggerSimple.Put("======================================================");
        }

        [OneTimeTearDown]
        public void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(NotifyPropertyChangedTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

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
        public async Task NotifyPropertyChangedAsyncTest() {
            int countFiredEvents = 3 + new Random(Environment.TickCount).Next(10);
            int countReceivedEvents = 0;
            object firedValue = null;

            var subject = new Subject<PropertyChangedEventArgs>();
            void listener(PropertyChangedEventArgs ev) {
                ++countReceivedEvents;
                firedValue = (ev as PropertyChangedExEventArgs<string>).NewValue;
                subject.OnNext(ev);
            }
            var signalWait = false;
            const string prefix = "Value ";
            using (var notifier = new NotifyPropertyChanged(null, listener, true)) {
                using (var signal = new Signal()) {
                    using (subject.Timeout(TimeSpan.FromMilliseconds(100))
                        .Subscribe(ev => {
                            LoggerSimple.Put("OnNext: ev=" + ev);
                        }, ex => {
                            LoggerSimple.Put("OnError: " + ex);
                            signal.Set();
                        }))
                    {

                        for (var i=0; i<countFiredEvents; ++i)
                            notifier.OnPropertyChanged(null, prefix + i, "propertyName");

                        signalWait = await signal.Wait(TimeSpan.FromSeconds(1));
                    }
                }

            }

            LoggerSimple.Put("  " + nameof(NotifyPropertyChangedAsyncTest) + ": checking...");
            Assert.IsTrue(signalWait);
            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
        }

    }

}
