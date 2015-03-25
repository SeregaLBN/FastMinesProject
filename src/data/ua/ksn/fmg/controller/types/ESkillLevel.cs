using System;
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
      /// коэффициент уровня сложности в зависимости от типа мозаики - чем больше, тем сложнее
      /// </summary>
      private static readonly IDictionary<EMosaic, double> mosaicCoefficient;
      static ESkillLevelEx() {
         var values = EMosaicEx.GetValues();
         mosaicCoefficient = new Dictionary<EMosaic, double>(values.Length);
         const int area = 200; // пох
         foreach (EMosaic mosaicType in values) {
            BaseCell.BaseAttribute attr = CellFactory.CreateAttributeInstance(mosaicType, area);

            // variant 1 - сложность в зависимости от кол-ва пересечений ячеек в одной точке
            //			mosaicCoefficient.put(mosaicType, attr.getVertexIntersection());

            // variant 2 - сложность в зависимости от кол-ва соседних ячеек
            //int cntDir = attr.GetDirectionCount();
            //int neighbors = 0;
            //for (int i=0; i < cntDir; i++)
            //   neighbors += attr.getNeighborNumber(i);
            //mosaicCoefficient.Add(mosaicType, ((double)neighbors) / cntDir);

            // variant 3 - сложность в зависимости от кол-ва соседних ячеек и кол-ва точек пересечения
            //int cntDir = attr.GetDirectionCount();
            //int totalNeighbors = Enumerable.Range(0, cntDir).Aggregate((accum, i) => accum+attr.getNeighborNumber(i));
            //double neighbors = ((double)totalNeighbors)/cntDir;
            var neighbors = (double)attr.getNeighborNumber(false);
            mosaicCoefficient.Add(mosaicType, attr.getVertexIntersection() / neighbors);

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

      /** коэффициент уровня сложности */
      private static double GetCoefficient(this ESkillLevel self) {
         // variant 1
         //		switch (self) {
         //		case eBeginner: return 0.6;
         //		case eAmateur : return 0.72;
         //		case eProfi   : return 0.84;
         //		case eCrazy   : return 0.9991111111111111;
         //		}

         // variant 2
         //switch (self) {
         //case ESkillLevel.eBeginner: return 1.2;
         //case ESkillLevel.eAmateur : return 1.44;
         //case ESkillLevel.eProfi   : return 1.68;
         //case ESkillLevel.eCrazy   : return 1.9982222222222221;
         //}

         // variant 3
         switch (self) {
         case ESkillLevel.eBeginner: return 0.075;
         case ESkillLevel.eAmateur : return 0.09;
         case ESkillLevel.eProfi   : return 0.105;
         case ESkillLevel.eCrazy   : return 0.12488888888888888;
         }
         throw new Exception("Invalid method call. Для уровня сложности '" + self + "' нет коэффициента сложности.");
      }

      /** размеры полей */
      public static Size DefaultSize(this ESkillLevel self) {
         switch (self) {
         case ESkillLevel.eBeginner: return new Size(10, 10); // 15
         case ESkillLevel.eAmateur : return new Size(20, 15); // 54
         case ESkillLevel.eProfi   : return new Size(30, 20); // 126
         case ESkillLevel.eCrazy   : return new Size(45, 25); // 281
         }
         throw new Exception("Invalid method call. Для уровня сложности '" + self + "' нет размера поля по-умолчанию.");
      }

      /** Узнать кол-во мин на размере поля по-умолчанию */
      public static int GetNumberMines(this ESkillLevel self, EMosaic eMosaic) {
         return GetNumberMines(self, eMosaic, self.DefaultSize());
      }

      /** Узнать кол-во мин на заданном размере поля */
      public static int GetNumberMines(this ESkillLevel self, EMosaic eMosaic, Size customSizeMosaic) {
         if (customSizeMosaic == null)
            throw new ArgumentException("customSizeMosaic must be not null");
         if (self == ESkillLevel.eCustom)
            throw new Exception("Для уровня сложности '" + self + "' кол-во мин задаётся явно, а не расчитывается...");

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