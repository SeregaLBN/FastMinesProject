using System;
using System.ComponentModel;
using System.Threading.Tasks;

namespace Fmg.Common.Notifier {

    public abstract class PropertyChangeExecutorTest {

        private class SimpleDataObj : INotifyPropertyChanged, IDisposable {

            public event PropertyChangedEventHandler PropertyChanged {
                add    { _notifier.PropertyChanged += value;  }
                remove { _notifier.PropertyChanged -= value;  }
            }
            private readonly NotifyPropertyChanged _notifier;

            internal SimpleDataObj() {
                _notifier = new NotifyPropertyChanged(this);
            }

            public void Dispose() {
                _notifier.Dispose();
            }

            public bool Disposed => _notifier.Disposed;

        }

        protected abstract void AssertEqual(int expected, int actual);
        protected abstract void AssertNotNull(object anObject);
        protected abstract void AssertTrue(bool condition);
        protected abstract void AssertFalse(bool condition);
        protected abstract void AssertFail();

        public virtual void Setup() {
            Logger.Info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Logger.Info(">" + nameof(PropertyChangeExecutorTest) + "::" + nameof(Setup));
        }

        public virtual void Before() {
            Logger.Info("======================================================");
        }

        public virtual void After() {
            Logger.Info("======================================================");
            Logger.Info("< " + nameof(PropertyChangeExecutorTest) + " closed");
            Logger.Info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

        public virtual async Task SimpleUsageTest() {
            Logger.Info("> PropertyChangeExecutorTest::SimpleUsageTest");

            SimpleDataObj d = null;
            await new PropertyChangeExecutor<SimpleDataObj>(() => d = new SimpleDataObj()).Run(10, 1000,
                data => {
                    Logger.Info("    data modificator");
                }, (data, modifiedProperties) => {
                    Logger.Info("    data validator");
                    AssertNotNull(data);
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertNotNull(d);
            AssertTrue(d.Disposed);
        }

        public virtual async Task ExtendedUsageTest() {
            Logger.Info("> PropertyChangeExecutorTest::ExtendedUsageTest");

            SimpleDataObj d = null;
            await new PropertyChangeExecutor<SimpleDataObj>(() => d = new SimpleDataObj(), false).Run(10, 1000,
                data => {
                    Logger.Info("    data modificator");
                }, (data, modifiedProperties) => {
                    Logger.Info("    data validator");
                    AssertNotNull(data);
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertNotNull(d);
            AssertFalse(d.Disposed);

            await new PropertyChangeExecutor<SimpleDataObj>(() => d).Run(10, 1000,
                data => {
                    Logger.Info("    data modificator");
                }, (data, modifiedProperties) => {
                    Logger.Info("    data validator");
                    AssertEqual(0, modifiedProperties.Count);
                    AssertFalse(data.Disposed);
                });
            AssertTrue(d.Disposed);
        }

        public virtual async Task CreatorFailTest() {
            Logger.Info("> PropertyChangeExecutorTest::CreatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => { throw failEx; }).Run(10, 1000,
                   data => {
                       Logger.Info("    data modificator");
                       AssertFail();
                   }, (data, modifiedProperties) => {
                       Logger.Info("    data validator");
                       AssertFail();
                   });
                AssertFail();
            } catch(Exception ex) {
                //AssertEqual(failEx, ex);
                AssertTrue(ReferenceEquals(failEx, ex));
            }
        }

        public virtual async Task ModificatorFailTest() {
            Logger.Info("> PropertyChangeExecutorTest::ModificatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => new SimpleDataObj()).Run(10, 1000,
                   data => {
                       Logger.Info("    data modificator");
                       throw failEx;
                   }, (data, modifiedProperties) => {
                       Logger.Info("    data validator");
                       AssertFail();
                   });
                AssertFail();
            } catch(Exception ex) {
                //AssertEqual(failEx, ex);
                AssertTrue(ReferenceEquals(failEx, ex));
            }
        }

        public virtual async Task ValidatorFailTest() {
            Logger.Info("> PropertyChangeExecutorTest::ModificatorFailTest");

            var failEx = new ArgumentException("Tested exception");
            try {
                await new PropertyChangeExecutor<SimpleDataObj>(() => new SimpleDataObj()).Run(10, 1000,
                   data => {
                       Logger.Info("    data modificator");
                   }, (data, modifiedProperties) => {
                       Logger.Info("    data validator");
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
