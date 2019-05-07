using System;
using System.ComponentModel;
using System.Linq;
using System.Threading.Tasks;

namespace fmg.common.notifier {

    public abstract class NotifyPropertyChangedTest {

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
                set => notifier.SetProperty(ref property, value);
            }
            public void Dispose() { notifier.Dispose();  }
        }


        protected abstract void AssertEqual(int    expected, int    actual);
        protected abstract void AssertEqual(object expected, object actual);

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put(">" + nameof(NotifyPropertyChangedTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(NotifyPropertyChangedTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual void NotifyPropertyChangedSyncTest() {
            LoggerSimple.Put(">" + nameof(NotifyPropertyChangedTest) + "::" + nameof(NotifyPropertyChangedSyncTest));

            using (var data = new SimpleProperty(-1, false)) {
                int countFiredEvents = 3 + ThreadLocalRandom.Current.Next(10);
                int countReceivedEvents = 0;

                void listener(object sender, PropertyChangedEventArgs ev) { ++countReceivedEvents; }
                data.PropertyChanged += listener;
                for (var i = 0; i < countFiredEvents; ++i)
                    data.Property = i;
                data.PropertyChanged -= listener;

                AssertEqual(countFiredEvents, countReceivedEvents);
            }
        }

        public virtual async Task NotifyPropertyChangedAsyncTest() {
            LoggerSimple.Put(">" + nameof(NotifyPropertyChangedTest) + "::" + nameof(NotifyPropertyChangedAsyncTest));

            const int initialValue = 1;
            int countFiredEvents = 3 + ThreadLocalRandom.Current.Next(10);
            string prefix = " Value ";
            await new PropertyChangeExecutor<SimpleProperty>(() => new SimpleProperty(initialValue, true)).Run(
                200,
                1000,
                data => {
                    for (int i = 0; i < countFiredEvents; ++i)
                        data.Property = prefix + i;
                }, (data, modifiedProperties) => {
                    int countOfProperties = modifiedProperties.Count;
                    AssertEqual(1, countOfProperties);
                    int countReceivedEvents = modifiedProperties.Values.ToList()[0];
                    AssertEqual(1, countReceivedEvents);
                    object lastFiredValue = data.Property;
                    AssertEqual(prefix + (countFiredEvents - 1), lastFiredValue);
                });
        }

        public virtual async Task CheckForNoEventTest() {
            LoggerSimple.Put(">" + nameof(NotifyPropertyChangedTest) + "::" + nameof(CheckForNoEventTest));

            const int initialValue = 1;
            await new PropertyChangeExecutor<SimpleProperty>(() => new SimpleProperty(initialValue, true)).Run(
                100,
                1000,
                data => {
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                    data.Property = initialValue + 123;
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                    data.Property = initialValue; // restore original value
                    LoggerSimple.Put("    data.Property={0}", data.Property);
                }, (data, modifiedProperties) => {
                    AssertEqual(0, modifiedProperties.Count);
                });
        }

    }

}
