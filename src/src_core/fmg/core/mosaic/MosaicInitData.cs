using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notifier;
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
        private event PropertyChangedEventHandler PropertyChangedSync {
            add    { _notifier/*Sync*/.PropertyChanged += value;  }
            remove { _notifier/*Sync*/.PropertyChanged -= value;  }
        }
        public event PropertyChangedEventHandler PropertyChanged/*Async*/ {
            add    { _notifierAsync.PropertyChanged += value;  }
            remove { _notifierAsync.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier/*Sync*/;
        private   readonly NotifyPropertyChanged _notifierAsync;


        public MosaicInitData() {
            _notifier      = new NotifyPropertyChanged(this, false);
            _notifierAsync = new NotifyPropertyChanged(this, true);
            this.PropertyChangedSync += OnPropertyChanged;
        }


        public void CopyFrom(MosaicInitData from) {
            if (ReferenceEquals(from, this))
                return;
            this.MosaicType = from.MosaicType;
            this.SizeField  = from.SizeField;
            this.MinesCount = from.MinesCount;
        }

        public EMosaic MosaicType {
            get { return _mosaicType; }
            set { _notifier.SetProperty(ref _mosaicType, value); }
        }

        public EMosaicGroup MosaicGroup {
            get { return _mosaicType.GetGroup(); }
            set {
                if (_mosaicType.GetGroup() == value)
                    return;
                var ordinalInOldGroup = _mosaicType.GetOrdinalInGroup();
                var ordinalInNewGroup = Math.Min(ordinalInOldGroup, value.GetMosaics().Count - 1);
                MosaicType = value.GetMosaics()[ordinalInNewGroup];
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
                _notifier.SetProperty(ref _sizeField, value);
            }
        }

        public int MinesCount {
            get { return _minesCount; }
            set { _notifier.SetProperty(ref _minesCount, value); }
        }

        public ESkillLevel SkillLevel {
            get {
                return ESkillLevelEx.CalcSkillLevel(MosaicType, SizeField, MinesCount);
            }
            set {
                if (value == ESkillLevel.eCustom)
                    throw new ArgumentException("Custom skill level not recognized");
                if (lockChanging)
                    throw new InvalidOperationException("Illegal usage");

                var skillOld = SkillLevel;
                if (skillOld == value)
                    return;

                lockChanging = true;
                try {
                    MinesCount = value.GetNumberMines(MosaicType);
                    SizeField = value.GetDefaultSize();
                } finally {
                    lockChanging = false;
                }

                var skillNew = SkillLevel;
                System.Diagnostics.Debug.Assert(value == skillNew);
                System.Diagnostics.Debug.Assert(value != skillOld);
                _notifier.FirePropertyChanged(skillOld, skillNew, nameof(SkillLevel));
            }
        }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire as async event
            _notifierAsync.FirePropertyChanged(ev);

            if (lockChanging)
                return;
            lockChanging = true;
            try {
                switch(ev.PropertyName) {
                case nameof(this.MosaicType):
                    {
                        var old = (ev as PropertyChangedExEventArgs<EMosaic>).OldValue;
                        if (old.GetGroup() != MosaicType.GetGroup())
                            _notifier.FirePropertyChanged(old.GetGroup(), MosaicType.GetGroup(), nameof(this.MosaicGroup));

                        var skillOld = ESkillLevelEx.CalcSkillLevel(old, SizeField, MinesCount);
                        if (skillOld == ESkillLevel.eCustom) {
                            var skillNew = SkillLevel;
                            if (skillNew != skillOld)
                                _notifier.FirePropertyChanged(skillOld, skillNew, nameof(this.SkillLevel));
                        } else {
                            // restore mines count for new mosaic type
                            MinesCount = skillOld.GetNumberMines(MosaicType);
                        }
                    }
                    break;
                case nameof(this.SizeField):
                    {
                        var skillOld = ESkillLevelEx.CalcSkillLevel(MosaicType, (ev as PropertyChangedExEventArgs<Matrisize>).OldValue, MinesCount);
                        var skillNew = SkillLevel;
                        if (skillNew != skillOld)
                            _notifier.FirePropertyChanged(skillOld, skillNew, nameof(this.SkillLevel));
                    }
                    break;
                case nameof(this.MinesCount):
                    {
                        var skillOld = ESkillLevelEx.CalcSkillLevel(MosaicType, SizeField, (ev as PropertyChangedExEventArgs<int>).OldValue);
                        var skillNew = SkillLevel;
                        if (skillNew != skillOld)
                            _notifier.FirePropertyChanged(skillOld, skillNew, nameof(this.SkillLevel));
                    }
                    break;
                }
            } finally {
                lockChanging = false;
            }
        }

        public override string ToString() {
            return nameof(MosaicInitData)
                + "{MosaicType:" + _mosaicType
                + ", SizeField:" + _sizeField
                + ", MinesCount:" + _minesCount
                + ", SkillLevel:" + SkillLevel
                + "}";
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
