using System;
using System.Linq;
using System.Collections;
using System.Collections.Generic;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.geom;

namespace ua.ksn.fmg.controller.types {

   public enum ESkillLevel {
      eBeginner,
      eAmateur,
      eProfi,
      eCrazy,
      eCustom
   }

   public static class ESkillLevelEx {
      private static readonly ESkillLevel[] ESkillLevelValues = (ESkillLevel[])Enum.GetValues(typeof(ESkillLevel));
      public static ESkillLevel[] GetValues() { return ESkillLevelValues; }

      /// <summary>
      /// skill level coefficient
      /// коэффициент уровн€ сложности в зависимости от типа мозаики - чем больше, тем сложнее
      /// </summary>
      private static readonly IDictionary<EMosaic, double> mosaicCoefficient;
      static ESkillLevelEx() {
         var values = EMosaicEx.GetValues();
         mosaicCoefficient = new Dictionary<EMosaic, double>(values.Length);
         const int area = 200; // пох
         foreach (EMosaic mosaicType in values) {
            BaseCell.BaseAttribute attr = CellFactory.CreateAttributeInstance(mosaicType, area);

            // variant 1 - сложность в зависимости от кол-ва пересечений €чеек в одной точке
            //			mosaicCoefficient.put(mosaicType, attr.getVertexIntersection());

            // variant 2 - сложность в зависимости от кол-ва соседних €чеек
            int cntDir = attr.GetDirectionCount();
            int neighbors = 0;
            for (int i=0; i < cntDir; i++)
               neighbors += attr.getNeighborNumber(i);
            mosaicCoefficient.Add(mosaicType, ((double)neighbors) / cntDir);

            //			System.out.println(attr.getClass().getSimpleName() + ": " + mosaicCoefficient.get(mosaicType));
         }

         // x*y * coefficient / mosaicCoefficient  = 15
         // 15*mosaicCoefficient/(x*y)  = coefficient
         //		System.out.println(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 15  / (10*10));
         //		System.out.println(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 54  / (20*15));
         //		System.out.println(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 126 / (30*20));
         //		System.out.println(mosaicCoefficient.get(EMosaic.eMosaicSquare1) * 281 / (45*25));

         //		System.exit(0);
      }

      /** коэффициент уровн€ сложности */
      private static double GetCoefficient(this ESkillLevel self) {
         // variant 1
         //		switch (self) {
         //		case eBeginner: return 0.6;
         //		case eAmateur : return 0.72;
         //		case eProfi   : return 0.84;
         //		case eCrazy   : return 0.9991111111111111;
         //		}
         // variant 2
         switch (self) {
         case ESkillLevel.eBeginner:
            return 1.2;
         case ESkillLevel.eAmateur:
            return 1.44;
         case ESkillLevel.eProfi:
            return 1.68;
         case ESkillLevel.eCrazy:
            return 1.9982222222222221;
         }

         throw new Exception("Invalid method call. ƒл€ уровн€ сложности '" + self + "' нет коэффициента сложности.");
      }

      /** размеры полей */
      public static Size DefaultSize(this ESkillLevel self) {
         switch (self) {
         case ESkillLevel.eBeginner: return new Size(10, 10); // 15
         case ESkillLevel.eAmateur : return new Size(20, 15); // 54
         case ESkillLevel.eProfi   : return new Size(30, 20); // 126
         case ESkillLevel.eCrazy   : return new Size(45, 25); // 281
         }
         throw new Exception("Invalid method call. ƒл€ уровн€ сложности '" + self + "' нет размера пол€ по-умолчанию.");
      }

      /** ”знать кол-во мин на размере пол€ по-умолчанию */
      public static int GetNumberMines(this ESkillLevel self, EMosaic eMosaic) {
         return GetNumberMines(self, eMosaic, self.DefaultSize());
      }

      /** ”знать кол-во мин на заданном размере пол€ */
      public static int GetNumberMines(this ESkillLevel self, EMosaic eMosaic, Size customSizeMosaic) {
         if (customSizeMosaic == null)
            throw new ArgumentException("customSizeMosaic must be not null");
         if (self == ESkillLevel.eCustom)
            throw new Exception("ƒл€ уровн€ сложности '" + self + "' кол-во мин задаЄтс€ €вно, а не расчитываетс€...");

         return (int)(customSizeMosaic.width * customSizeMosaic.height * GetCoefficient(self) / mosaicCoefficient[eMosaic]);
      }

      public static String GetDescription(this ESkillLevel self) {
         switch (self) {
         case ESkillLevel.eProfi: return "Professional";
         default: return self.ToString().Substring(1);
         }
      }

      public static int Ordinal(this ESkillLevel self) {
         var values = GetValues();
         for (var i = 0; i < values.Length; i++)
            if (values[i] == self)
               return i;
         throw new ArgumentException("Index not found");
      }

      public static ESkillLevel FromOrdinal(int ordinal) {
         var values = GetValues();
         if ((ordinal < 0) || (ordinal >= values.Length))
            throw new IndexOutOfRangeException("Invalid ordinal");
         return values[ordinal];
      }
   }

}