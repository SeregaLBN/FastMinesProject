using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;

namespace fmg.core.mosaic {

    /// <summary> Mosaic data </summary>
    public class MosaicInitData : INotifyPropertyChanged, IDisposable {

        public const double AREA_MINIMUM = 230;

        public const int MIN_SIZE_FIELD_M = 3;
        public const int MAX_SIZE_FIELD_M = 3000;
        public const int MIN_SIZE_FIELD_N = 3;
        public const int MAX_SIZE_FIELD_N = 3000;

        public const EMosaic       DEFAULT_MOSAIC_TYPE  = EMosaic.eMosaicSquare1;
        public const ESkillLevel   DEFAULT_SKILL_LEVEL  = ESkillLevel.eBeginner;
        public static readonly int DEFAULT_SIZE_FIELD_M = DEFAULT_SKILL_LEVEL.GetDefaultSize().m;
        public static readonly int DEFAULT_SIZE_FIELD_N = DEFAULT_SKILL_LEVEL.GetDefaultSize().n;
        public static readonly int DEFAULT_MINES_COUNT  = DEFAULT_SKILL_LEVEL.GetNumberMines(DEFAULT_MOSAIC_TYPE);

        private EMosaic   _mosaicType = DEFAULT_MOSAIC_TYPE;
        private Matrisize _sizeField  = new Matrisize(DEFAULT_SIZE_FIELD_M, DEFAULT_SIZE_FIELD_N);
        private int       _minesCount = DEFAULT_MINES_COUNT;

        private bool lockChanging = false;

        protected bool Disposed { get; private set; }
        private event PropertyChangedEventHandler PropertyChangedSync;
        public  event PropertyChangedEventHandler PropertyChanged/*Async*/;
        protected readonly NotifyPropertyChanged _notifier;
        private   readonly NotifyPropertyChanged _notifierAsync;


        public MosaicInitData() {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChangedSync?.Invoke(this, ev), false);
            _notifierAsync = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev), true);
            this.PropertyChangedSync += OnPropertyChanged;
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
                if (value.m < MIN_SIZE_FIELD_M)
                    throw new ArgumentException("Size field M must be larger " + MIN_SIZE_FIELD_M);
                if (value.n < MIN_SIZE_FIELD_N)
                    throw new ArgumentException("Size field N must be larger " + MIN_SIZE_FIELD_N);
                if (value.m > MAX_SIZE_FIELD_M)
                    throw new ArgumentException("Size field M must be less " + (MAX_SIZE_FIELD_M + 1));
                if (value.n > MAX_SIZE_FIELD_N)
                    throw new ArgumentException("Size field N must be less " + (MAX_SIZE_FIELD_N + 1));
                var skillOld = SkillLevel;
                if (_notifier.SetProperty(ref _sizeField, value)) {
                    var skillNew = SkillLevel;
                    if (!lockChanging && (skillNew != skillOld))
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
                    if (!lockChanging && (skillNew != skillOld))
                        _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
                }
            }
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

                lockChanging = true;
                {
                    MinesCount = value.GetNumberMines(MosaicType);
                    SizeField = value.GetDefaultSize();
                }
                lockChanging = false;

                var skillNew = SkillLevel;
                System.Diagnostics.Debug.Assert(value == skillNew);
                System.Diagnostics.Debug.Assert(value != skillOld);
                _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
            }
        }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);
        }

        /// <summary>  Dispose managed resources </summary>/
        protected virtual void Disposing() {
            this.PropertyChangedSync -= OnPropertyChanged;
            _notifier.Dispose();
            _notifierAsync.Dispose();
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            Disposing();
            GC.SuppressFinalize(this);
        }

    }

}
