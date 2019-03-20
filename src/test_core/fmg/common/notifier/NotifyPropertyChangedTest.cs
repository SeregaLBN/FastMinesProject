using System;
using System.ComponentModel;
using System.Linq;
using System.Threading.Tasks;
using NUnit.Framework;
using fmg.core.mosaic;

namespace fmg.common.notifier {

    public class NotifyPropertyChangedTest {

        class SimpleProperty : INotifyPropertyChanged, IDisposable {
            public event PropertyChangedEventHandler PropertyChanged;
            private readonly NotifyPropertyChanged notifier;
            internal SimpleProperty(object initValueOfProperty, bool deferredNotifications) {
                notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev), deferredNotifications);
                this.property = initValueOfProperty;
            }
            private object property;
            public object Property {
                get => property;
                set { notifier.SetProperty(ref property, value); }
            }
            public void Dispose() { notifier.Dispose();  }
        }

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
            using (var data = new SimpleProperty(-1, false)) {
                int countFiredEvents = 3 + ThreadLocalRandom.Current.Next(10);
                int countReceivedEvents = 0;

                void listener(object sender, PropertyChangedEventArgs ev) { ++countReceivedEvents; }
                data.PropertyChanged += listener;
                for (int i = 0; i < countFiredEvents; ++i)
                    data.Property = i;
                data.PropertyChanged -= listener;

                Assert.AreEqual(countFiredEvents, countReceivedEvents);
            }
        }

        [Test]
        public async Task NotifyPropertyChangedAsyncTest() {
            const int initialValue = 1;
            using (var data = new SimpleProperty(initialValue, true)) {
                int countFiredEvents = 3 + ThreadLocalRandom.Current.Next(10);
                string prefix = " Value ";
                await new PropertyChangeExecutor<SimpleProperty>(data).Run(
                    200,
                    1000,
                    () => {
                        for (int i = 0; i < countFiredEvents; ++i)
                            data.Property = prefix + i;
                    }, modifiedProperties => {
                        LoggerSimple.Put("  checking...");
                        int countOfProperties = modifiedProperties.Count;
                        Assert.AreEqual(1, countOfProperties);
                        int countReceivedEvents = modifiedProperties.Values.ToList()[0];
                        Assert.AreEqual(1, countReceivedEvents);
                        object lastFiredValue = data.Property;
                        Assert.AreEqual(prefix + (countFiredEvents - 1), lastFiredValue);
                    });
            }
        }

        [Test]
        public async Task CheckForNoEventTest() {
            LoggerSimple.Put("> " + nameof(CheckForNoEventTest));

            const int initialValue = 1;
            using (var data = new SimpleProperty(initialValue, true)) {
                await new PropertyChangeExecutor<SimpleProperty>(data).Run(
                    100,
                    1000,
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

}
