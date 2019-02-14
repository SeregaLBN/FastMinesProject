using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;

namespace fmg.core.mosaic {

    /// <summary> Mosaic data </summary>
    public class MosaicInitData : INotifyPropertyChanged {

        public const double AREA_MINIMUM = 230;

        private EMosaic   _mosaicType = EMosaic.eMosaicSquare1;
        private Matrisize _sizeField  = ESkillLevel.eBeginner.GetDefaultSize();
        private int       _minesCount = ESkillLevel.eBeginner.GetNumberMines(EMosaic.eMosaicSquare1);
        private SizeDouble size = new SizeDouble(500, 500);

        private bool _lockFireSkill = false;

        protected bool Disposed { get; private set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;


        public MosaicInitData() {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
        }


        public EMosaic MosaicType {
            get { return _mosaicType; }
            set {
                var skillOld = SkillLevel;
                if (_notifier.SetProperty(ref _mosaicType, value)) {
                    if (skillOld == ESkillLevel.eCustom) {
                        var skillNew = SkillLevel;
                        if (skillNew != skillOld)
                            _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
                    } else {
                        SkillLevel = skillOld;
                    }
                }
            }
        }

        public Matrisize SizeField {
            get { return _sizeField; }
            set {
                var skillOld = SkillLevel;
                if (_notifier.SetProperty(ref _sizeField, value)) {
                    var skillNew = SkillLevel;
                    if (!_lockFireSkill && (skillNew != skillOld))
                        _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
                }
            }
        }

        public int MinesCount {
            get { return _minesCount; }
            set {
                var skillOld = SkillLevel;
                if (_notifier.SetProperty(ref _minesCount, value)) {
                    var skillNew = SkillLevel;
                    if (!_lockFireSkill && (skillNew != skillOld))
                        _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
                }
            }
        }

        public SizeDouble Size {
            get { return size; }
            set { _notifier.SetProperty(ref size, value); }
        }

        public ESkillLevel SkillLevel {
            get {
                if ((SizeField == ESkillLevel.eBeginner.GetDefaultSize()) && (MinesCount == ESkillLevel.eBeginner.GetNumberMines(MosaicType)))
                    return ESkillLevel.eBeginner;
                if ((SizeField == ESkillLevel.eAmateur.GetDefaultSize()) && (MinesCount == ESkillLevel.eAmateur.GetNumberMines(MosaicType)))
                    return ESkillLevel.eAmateur;
                if ((SizeField == ESkillLevel.eProfi.GetDefaultSize()) && (MinesCount == ESkillLevel.eProfi.GetNumberMines(MosaicType)))
                    return ESkillLevel.eProfi;
                if ((SizeField == ESkillLevel.eCrazy.GetDefaultSize()) && (MinesCount == ESkillLevel.eCrazy.GetNumberMines(MosaicType)))
                    return ESkillLevel.eCrazy;
                return ESkillLevel.eCustom;
            }
            set {
                if (value == ESkillLevel.eCustom)
                    throw new System.ArgumentException("Custom skill level not recognized");
                var skillOld = SkillLevel;
                if (skillOld == value)
                    return;

                _lockFireSkill = true;
                {
                    MinesCount = value.GetNumberMines(MosaicType);
                    SizeField = value.GetDefaultSize();
                }
                _lockFireSkill = false;

                var skillNew = SkillLevel;
                System.Diagnostics.Debug.Assert(value == skillNew);
                System.Diagnostics.Debug.Assert(value != skillOld);
                _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
            }
        }

    }

}
