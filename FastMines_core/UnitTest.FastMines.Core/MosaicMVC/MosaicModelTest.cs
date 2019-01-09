using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using NUnit.Framework;
using fmg.common.geom;
using DummyMosaicImageType = System.Object;

namespace fmg.core.mosaic {

    public class MosaicModelTest {

        //[SetUp]
        //public void Setup() { }

        [Test]
        public void MosaicGameModelPropertyChangedTest() {
            using (var model = new MosaicGameModel()) {
                Assert.IsTrue(model.Matrix.Any());
                Assert.IsTrue(ReferenceEquals(model.CellAttr, model.Matrix.First().Attr));

                var modifiedProperties = new List<string>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    modifiedProperties.Add(ev.PropertyName);
                }

                model.PropertyChanged += onModelPropertyChanged;

                modifiedProperties.Clear();
                model.SizeField = new Matrisize(15, 10);
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.SizeField)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.Matrix)));

                modifiedProperties.Clear();
                model.Area = 12345;
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.Area)));
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.CellAttr)));

                model.PropertyChanged -= onModelPropertyChanged;
            }
        }

        [Test]
        public void MosaicDrawModelPropertyChangedTest() {
            using (var model = new MosaicDrawModel<DummyMosaicImageType>()) {
                var modifiedProperties = new List<string>();
                void onModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                    modifiedProperties.Add(ev.PropertyName);
                }

                model.PropertyChanged += onModelPropertyChanged;

                modifiedProperties.Clear();
                model.Size = new SizeDouble(123, 456);
                Assert.IsTrue(modifiedProperties.Contains(nameof(model.Size)));

                model.PropertyChanged -= onModelPropertyChanged;
            }
        }

        [Test]
        public void MosaicDrawModelTest() {
            using (var model = new MosaicDrawModel<DummyMosaicImageType>()) {
                Assert.AreEqual(model.CellAttr.GetSize(model.SizeField), model.Size);
            }
        }

    }

}
