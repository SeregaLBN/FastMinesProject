using System;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.common.notifier;
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
                    notifier.FirePropertyChanged("propertyName ");
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
                            notifier.FirePropertyChanged(null, prefix + i, "propertyName");

                        signalWait = await signal.Wait(TimeSpan.FromSeconds(1));
                    }
                }

            }

            LoggerSimple.Put("  " + nameof(NotifyPropertyChangedAsyncTest) + ": checking...");
            Assert.IsTrue(signalWait);
            Assert.AreEqual(1, countReceivedEvents);
            Assert.AreEqual(prefix + (countFiredEvents-1), firedValue);
        }

        class SomeProperty<T> : INotifyPropertyChanged {
            public event PropertyChangedEventHandler PropertyChanged;
            private readonly NotifyPropertyChanged _notifier;
            internal SomeProperty(T property) {
                this.property = property;
                _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev), true);
            }
            private T  property;
            public  T  Property {
                get => property;
                set { _notifier.SetProperty(ref property, value); }
            }
        }

        [Test]
        public async Task CheckForNoEventTest() {
            LoggerSimple.Put("> " + nameof(CheckForNoEventTest));

            const int initialValue = 1;
            var data = new SomeProperty<int>(initialValue);
            await new PropertyChangeExecutor<SomeProperty<int>>(data).Run(TimeSpan.FromMilliseconds(100), TimeSpan.FromMilliseconds(1000),
                () => {
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                    data.Property = initialValue + 123;
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                    data.Property = initialValue; // restore original value
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                }, modifiedProperties => {
                    LoggerSimple.Put("  checking...");
                    Assert.AreEqual(0, modifiedProperties.Count);
                });
        }

    }

}
