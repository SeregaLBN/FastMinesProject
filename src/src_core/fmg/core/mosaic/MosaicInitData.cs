using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.types;

namespace fmg.core.mosaic {

   /// <summary> Mosaic data </summary>
   public class MosaicInitData : NotifyPropertyChanged {

      public const double AREA_MINIMUM = 230;

      private EMosaic   _mosaicType;
      private Matrisize _sizeField;
      private int       _minesCount;
      private double    _area;

      private bool _lock;

      public MosaicInitData() {
         MosaicType = EMosaic.eMosaicSquare1;
         SizeField = ESkillLevel.eBeginner.GetDefaultSize();
         MinesCount = ESkillLevel.eBeginner.GetNumberMines(MosaicType);
         Area = AREA_MINIMUM * 10;
      }

      public EMosaic MosaicType {
         get { return _mosaicType; }
         set {
            var skillOld = SkillLevel;
            if (SetProperty(ref _mosaicType, value)) {
               if (skillOld == ESkillLevel.eCustom) {
                  var skillNew = SkillLevel;
                  if (skillNew != skillOld)
                     OnSelfPropertyChanged(skillOld, skillNew, nameof(SkillLevel));
               }  else {
                  SkillLevel = skillOld;
               }
            }
         }
      }

      public Matrisize SizeField {
         get { return _sizeField; }
         set {
            var skillOld = SkillLevel;
            if (SetProperty(ref _sizeField, value)) {
               var skillNew= SkillLevel;
               if (!_lock && (skillNew != skillOld))
                  OnSelfPropertyChanged(skillOld, skillNew, nameof(SkillLevel));
            }
         }
      }

      public int MinesCount {
         get { return _minesCount; }
         set {
            var skillOld = SkillLevel;
            if (SetProperty(ref _minesCount, value)) {
               var skillNew = SkillLevel;
               if (!_lock && (skillNew != skillOld))
                  OnSelfPropertyChanged(skillOld, skillNew, nameof(SkillLevel));
            }
         }
      }

      public double Area {
         get { return _area; }
         set { SetProperty(ref _area, value); }
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
            var skillOld = SkillLevel;
            _lock = true;
            {
               MinesCount = value.GetNumberMines(MosaicType);
               SizeField = value.GetDefaultSize();
            }
            _lock = false;
            var skillNew = SkillLevel;
            if (skillNew != skillOld)
               OnSelfPropertyChanged(skillOld, skillNew, nameof(SkillLevel));
         }
      }

   }
}