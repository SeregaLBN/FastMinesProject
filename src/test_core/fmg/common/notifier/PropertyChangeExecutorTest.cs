using System;
using System.ComponentModel;
using System.Threading.Tasks;

namespace fmg.common.notifier {

    public abstract class PropertyChangeExecutorTest {

        private class SimpleDataObj : INotifyPropertyChanged, IDisposable {

            public event PropertyChangedEventHandler PropertyChanged;
            private readonly NotifyPropertyChanged _notifier;

            internal SimpleDataObj() {
                _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
            }

            public void Dispose() { _notifier.Dispose();  }

            public bool Disposed => _notifier.Disposed;

        }

        protected abstract void AssertEqual(int expected, int actual);
        protected abstract void AssertNotNull(object anObject);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);
        protected abstract void AssertFail();

        public virtual void Setup() {
            LoggerSimple.Put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            LoggerSimple.Put(">" + nameof(PropertyChangeExecutorTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            LoggerSimple.Put("======================================================");
        }

        public virtual void After() {
            LoggerSimple.Put("======================================================");
            LoggerSimple.Put("< " + nameof(PropertyChangeExecutorTest) + " closed");
            LoggerSimple.Put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual async Task SimpleUsageTest() {
            LoggerSimple.Put("> PropertyChangeExecutorTest::SimpleUsageTest");

            SimpleDataObj d = null;
            await new PropertyChangeExecutor<SimpleDataObj>(() => d = new SimpleDataObj()).Run(1, 100,
                data => {
                    LoggerSimple.Put("    data modificator");
                }, (data, modifiedProperties) => {
                    LoggerSimple.Put("    data validator");
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertNotNull(d);
            AssertTrue(d.Disposed);
        }

        public virtual async Task ExtendedUsageTest() {
            LoggerSimple.Put("> PropertyChangeExecutorTest::ExtendedUsageTest");

            SimpleDataObj d = null;
            await new PropertyChangeExecutor<SimpleDataObj>(() => d = new SimpleDataObj(), false).Run(1, 100,
                data => {
                    LoggerSimple.Put("    data modificator");
                }, (data, modifiedProperties) => {
                    LoggerSimple.Put("    data validator");
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertNotNull(d);
            AssertFalse(d.Disposed);

            await new PropertyChangeExecutor<SimpleDataObj>(() => d).Run(1, 100,
                data => {
                    LoggerSimple.Put("    data modificator");
                }, (data, modifiedProperties) => {
                    LoggerSimple.Put("    data validator");
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertTrue(d.Disposed);
        }

        public virtual async Task CreatorFailTest() {
            LoggerSimple.Put("> PropertyChangeExecutorTest::CreatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => { throw failEx; }).Run(1, 100,
                   data => {
                       LoggerSimple.Put("    data modificator");
                       AssertFail();
                   }, (data, modifiedProperties) => {
                       LoggerSimple.Put("    data validator");
                       AssertFail();
                   });
                AssertFail();
            } catch(Exception ex) {
                //AssertEqual(failEx, ex);
                AssertTrue(ReferenceEquals(failEx, ex));
            }
        }

        public virtual async Task ModificatorFailTest() {
            LoggerSimple.Put("> PropertyChangeExecutorTest::ModificatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => new SimpleDataObj()).Run(1, 100,
                   data => {
                       LoggerSimple.Put("    data modificator");
                       throw failEx;
                   }, (data, modifiedProperties) => {
                       LoggerSimple.Put("    data validator");
                       AssertFail();
                   });
                AssertFail();
            } catch(Exception ex) {
                //AssertEqual(failEx, ex);
                AssertTrue(ReferenceEquals(failEx, ex));
            }
        }

        public virtual async Task ValidatorFailTest() {
            LoggerSimple.Put("> PropertyChangeExecutorTest::ModificatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => new SimpleDataObj()).Run(1, 100,
                   data => {
                       LoggerSimple.Put("    data modificator");
                   }, (data, modifiedProperties) => {
                       LoggerSimple.Put("    data validator");
                       throw failEx;
                   });
                AssertFail();
            } catch(Exception ex) {
                //AssertEqual(failEx, ex);
                AssertTrue(ReferenceEquals(failEx, ex));
            }
        }

    }

}
