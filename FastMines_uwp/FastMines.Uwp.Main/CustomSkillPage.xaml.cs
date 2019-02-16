using System;
using System.Linq;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg {

    public sealed partial class CustomSkillPage : Page {

        /// <summary> Model (a common model between all the pages in the application) </summary>
        public MosaicInitData MosaicData { get; set; }

        public SolidColorBrush BorderColorStartBttn;
        private bool _closed;


        public CustomSkillPage() {
            this.InitializeComponent();
            //MosaicData = new MosaicInitData();

            this.Loaded   += OnPageLoaded;
            this.Unloaded += OnPageUnloaded;

            {
                HSV hsv = new HSV(AnimatedImageModelConst.DefaultForegroundColor) {
                    s = 80,
                    v = 70,
                    a = 170
                };
                BorderColorStartBttn = new SolidColorBrush(hsv.ToColor().ToWinColor());

                Action run = () => {
                    hsv.h += 10;
                    BorderColorStartBttn.Color = hsv.ToColor().ToWinColor();
                };
                run.RepeatNoWait(TimeSpan.FromMilliseconds(100), () => _closed);
            }
        }

        private void OnPageLoaded(object sender, RoutedEventArgs e) {
            this.Loaded -= OnPageLoaded;

            SliderWidth .Minimum = 5;
            SliderHeight.Minimum = 5;
            SliderMines .Minimum = 1;

            var maxSizeField = CalcMaxMosaicSize(MosaicInitData.AREA_MINIMUM);
            SliderWidth .Maximum = maxSizeField.m;
            SliderHeight.Maximum = maxSizeField.n;

            MosaicData.PropertyChanged += OnMosaicDataPropertyChanged;

            SliderWidth .Value = MosaicData.SizeField.m;
            SliderHeight.Value = MosaicData.SizeField.n;
            SliderMines .Value = MosaicData.MinesCount;
            CheckSkillSizeRadioButtons();
            CheckSkillMinesRadioButtons();
        }

        private void OnPageUnloaded(object sender, RoutedEventArgs ev) {
            this.Loaded -= OnPageUnloaded;
            MosaicData.PropertyChanged -= OnMosaicDataPropertyChanged;
            MosaicData = null;
            _closed = true;
            Bindings.StopTracking();
        }

        private void StartNewGame() {
            //Frame frame = this.Frame;
            Frame frame = Window.Current.Content as Frame;
            System.Diagnostics.Debug.Assert(frame != null);

            frame.Navigate(typeof(MosaicPage2), MosaicData);

            //Window.Current.Content = new MosaicPage();
            //// Ensure the current window is active
            //Window.Current.Activate();
        }

        private void OnClickBttnStartGame(object sender, RoutedEventArgs ev) {
            LoggerSimple.Put("OnClickBttnStartGame");
            StartNewGame();
        }

        private void OnSliderValueChangedSizeFieldWidth(object sender, Windows.UI.Xaml.Controls.Primitives.RangeBaseValueChangedEventArgs ev) {
            MosaicData.SizeField = new Matrisize(Convert.ToInt32(ev.NewValue), MosaicData.SizeField.n);
        }

        private void OnSliderValueChangedSizeFieldHeight(object sender, Windows.UI.Xaml.Controls.Primitives.RangeBaseValueChangedEventArgs ev) {
            MosaicData.SizeField = new Matrisize(MosaicData.SizeField.m, Convert.ToInt32(ev.NewValue));
        }

        private void OnMosaicDataPropertyChanged(object sender, System.ComponentModel.PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(MosaicData.SizeField):
                CalcSliderMinesMax();
                CheckSkillSizeRadioButtons();
                CheckSkillMinesRadioButtons();
                break;
            case nameof(MosaicData.MosaicType):
                CalcSliderMinesMax();
                CheckSkillMinesRadioButtons();
                break;
            case nameof(MosaicData.MinesCount):
                CheckSkillMinesRadioButtons();
                break;
            }
        }

        private void CalcSliderMinesMax() {
            int max = MosaicData.SizeField.m * MosaicData.SizeField.n - GetNeighborNumber();
            SliderMines.Maximum = max;
            if (SliderMines.Value > max)
                SliderMines.Value = max;

            //radioGroup.clearSelection();
        }

        private int GetNeighborNumber() {
            var attr = MosaicHelper.CreateAttributeInstance(MosaicData.MosaicType);
            int max = Enumerable.Range(0, attr.GetDirectionCount())
                .Select(i => attr.GetNeighborNumber(i))
                .Max();
            return max + 1; // +thisCell
        }

        /// <summary> узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана </summary>
        /// <param name="area">интересуемая площадь ячеек мозаики</param>
        /// <returns>max размер поля мозаики</returns>
        private Matrisize CalcMaxMosaicSize(double area) {
            var sizeMosaic = CalcMosaicWindowSize(ScreenResolutionHelper.GetDesktopSize());
            return MosaicHelper.FindSizeByArea(MosaicData.MosaicType, area, sizeMosaic);
        }
        /// <summary> узнать размер окна мозаики при указанном размере окна проекта </summary>
        SizeDouble CalcMosaicWindowSize(Size sizeMainWindow) {
            var mosaicMargin = GetMosaicMargin();
            SizeDouble res = new SizeDouble(
                    sizeMainWindow.Width - mosaicMargin.LeftAndRight,
                    sizeMainWindow.Height - mosaicMargin.TopAndBottom);
            if (res.Height < 0 || res.Width < 0)
                throw new Exception("Bad algorithm... :(");
            return res;
        }
        /// <summary> get margin around mosaic control </summary>
        Bound GetMosaicMargin() {
            // @TODO: not implemented...
            return new Bound();
        }

        private void OnRadioButtonSkillMinesChecked(object sender, RoutedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is RadioButton);
            var rb = (RadioButton)sender;
            var skillLevel = ESkillLevelEx.FromOrdinal(Convert.ToInt32(rb.Tag.ToString()));
            MosaicData.MinesCount = skillLevel.GetNumberMines(MosaicData.MosaicType, MosaicData.SizeField);
        }

        private void OnRadioButtonSkillSizeChecked(object sender, RoutedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is RadioButton);
            var rb = (RadioButton)sender;
            var skillLevel = ESkillLevelEx.FromOrdinal(Convert.ToInt32(rb.Tag.ToString()));
            MosaicData.SizeField = skillLevel.GetDefaultSize();
        }

        private void CheckSkillSizeRadioButtons() {
            var size = MosaicData.SizeField;
            rbSizeBeginner    .IsChecked = (size == ESkillLevel.eBeginner.GetDefaultSize());
            rbSizeAmateur     .IsChecked = (size == ESkillLevel.eAmateur .GetDefaultSize());
            rbSizeProfessional.IsChecked = (size == ESkillLevel.eProfi   .GetDefaultSize());
            rbSizeCrazy       .IsChecked = (size == ESkillLevel.eCrazy   .GetDefaultSize());
        }

        private void CheckSkillMinesRadioButtons() {
            var mines = MosaicData.MinesCount;
            var type  = MosaicData.MosaicType;
            var size  = MosaicData.SizeField;
            rbMinesBeginner    .IsChecked = (mines == ESkillLevel.eBeginner.GetNumberMines(type, size));
            rbMinesAmateur     .IsChecked = (mines == ESkillLevel.eAmateur.GetNumberMines(type, size));
            rbMinesProfessional.IsChecked = (mines == ESkillLevel.eProfi.GetNumberMines(type, size));
            rbMinesCrazy       .IsChecked = (mines == ESkillLevel.eCrazy.GetNumberMines(type, size));
        }

    }

}
