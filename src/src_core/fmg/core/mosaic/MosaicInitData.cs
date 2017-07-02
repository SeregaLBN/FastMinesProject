using fmg.common.geom;
using fmg.core.types;

namespace fmg.core.mosaic {

   /** Mosaic data */
   public class MosaicInitData {

      public Matrisize SizeField { get; set; }
      public EMosaic MosaicType { get; set; }
      public int MinesCount { get; set; }
      public double Area { get; set; }

      public MosaicInitData() {
         MosaicType = EMosaic.eMosaicSquare1;
         SizeField = ESkillLevel.eBeginner.GetDefaultSize();
         MinesCount = ESkillLevel.eBeginner.GetNumberMines(MosaicType);
         Area = 2300;
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
            MinesCount = value.GetNumberMines(MosaicType);
            SizeField = value.GetDefaultSize();
         }
      }

   }
}