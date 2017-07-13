using System;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   public static class MosaicHelper {
      private static string GetPackageName() { return typeof(MosaicHelper).Namespace; }

      /// <summary>Создать экземпляр атрибута для конкретного типа мозаики</summary>
      public static BaseCell.BaseAttribute CreateAttributeInstance(EMosaic mosaicType) {
         //switch (mosaicType) {
         //case eMosaicTriangle1: return new Triangle1.AttrTriangle1(area);
         //   // ...
         //case eMosaicSquare1: return new Square1.AttrSquare1(area);
         //   // ...
         //}
         //throw new Exception("Unknown type " + mosaicType);

         try {
            var className = GetPackageName() + ".cells." + mosaicType.GetMosaicClassName() + "+Attr" + mosaicType.GetMosaicClassName();
            var cellAttrClass = Type.GetType(className);
            var attr = (BaseCell.BaseAttribute)Activator.CreateInstance(cellAttrClass, null);
            return attr;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
            throw new Exception("Unknown type " + mosaicType + ": " + ex.Message, ex);
         }
      }

      /// <summary>Создать экземпляр ячейки для конкретного типа мозаики</summary>
      public static BaseCell CreateCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord) {
         //switch (mosaicType) {
         //case eMosaicTriangle1  : return new Triangle1((Triangle1.AttrTriangle1) attr, coord);
         //   // ...
         //case eMosaicSquare1    : return new Square1((Square1.AttrSquare1) attr, coord);
         //   // ...
         //}
         //throw new RuntimeException("Unknown type "+mosaicType);

         try {
            var className = GetPackageName() + ".cells." + mosaicType.GetMosaicClassName();
            var cellClass = Type.GetType(className);
            object[] args = { attr, coord };
            var cell = (BaseCell)Activator.CreateInstance(cellClass, args);
            cell.Init();
            return cell;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
            throw new Exception("Unknown type " + mosaicType + ": " + ex.Message, ex);
         }
      }


      #region calc dimension window region

      /// <summary> Поиск больше-меньше </summary>
      /// <param name="baseDelta">начало дельты приращения</param>
      /// <param name="comparable">ф-ция сравнения</param>
      /// <returns>что найдено</returns>
      private static int Finder(int baseDelta, Func<int, int> comparable) {
         double res = baseDelta;
         double d = baseDelta;
         bool deltaUp = true;
         do {
            var cmp = comparable((int)res);
            //System.Diagnostics.Debug.WriteLine("d={0}{1}; res={2}; cmp={3}", deltaUp ? '↑' : '↓', d, res, cmp);
            // Example:
            // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
            // init data:  res=100 d=100
            //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
            //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
            //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
            //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
            //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
            //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
            if (cmp == 0)
               break;
            if ((d < 1) && (cmp == -1))
               break;

            var resultUp = (cmp < 0);
            deltaUp = deltaUp && resultUp;
            if (deltaUp)
               d *= 2;
            else
               d /= 2;
            if (resultUp)
               res += d;
            else
               res -= d;
         } while (true);
         return (int)res;
      }

      private static double Finder(double baseDelta, Func<double, double> comparable) {
         double res = baseDelta;
         double d = baseDelta;
         bool deltaUp = true;
         do {
            var cmp = comparable(res);
            //System.Diagnostics.Debug.WriteLine("d={0}{1}; res={2}; cmp={3}", deltaUp ? '↑' : '↓', d, res, cmp);
            // Example:
            // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
            // init data:  res=100 d=100
            //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
            //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
            //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
            //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
            //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
            //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
            if (cmp.HasMinDiff(0))
               break;
            if ((d < 1) && cmp.HasMinDiff(-1))
               break;

            var resultUp = (cmp < 0);
            deltaUp = deltaUp && resultUp;
            if (deltaUp)
               d *= 2;
            else
               d /= 2;
            if (resultUp)
               res += d;
            else
               res -= d;
         } while (true);
         return res;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient"> размер окна/области (в пикселях):
      /// in - в которую должна вписаться мозаика;
      /// out - в которую реально впишется мозаика;
      /// </param>
      /// <returns>площадь ячейки</returns>
      private static double FindAreaBySize(BaseCell.BaseAttribute cellAttr, Matrisize mosaicSizeField, ref SizeDouble sizeClient) {
         // сделал приватным, т.к. неявно меняет свойства параметра 'cellAttr'

         if (sizeClient.Height <= 0 || sizeClient.Width <= 0)
            throw new ArgumentException("sizeClient must be positive");

         var sizeClientCopy = sizeClient;
         var sizeIter = new SizeDouble();
         var res = Finder(2000,
            area => {
               cellAttr.Area = area;
               sizeIter = cellAttr.GetOwnerSize(mosaicSizeField);
               if ((sizeIter.Width  <= sizeClientCopy.Width ) && sizeIter.Width .HasMinDiff(sizeClientCopy.Width) && // меньше с минимальными различиями
                   (sizeIter.Height <= sizeClientCopy.Height) && sizeIter.Height.HasMinDiff(sizeClientCopy.Height))  // less with minimal differences
                  return 0;
               if ((sizeIter.Width < sizeClientCopy.Width) &&
                   (sizeIter.Height < sizeClientCopy.Height))
                  return -1;
               return +1;
            });
         System.Diagnostics.Debug.Assert(sizeIter.Width <= sizeClient.Width);
         System.Diagnostics.Debug.Assert(sizeIter.Height <= sizeClient.Height);
         sizeClient = sizeIter;
         return res;
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicType">mosaic type</param>
      /// <param name="area">cell area of interest</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>размер поля мозаики</returns>
      public static Matrisize FindSizeByArea(EMosaic mosaicType, double area, SizeDouble sizeClient) {
         var cellAttr = CreateAttributeInstance(mosaicType);
         cellAttr.Area = area;
         var result = new Matrisize();
         Finder(2000, newWidth => {
            result.m = newWidth;
            var sizeWnd = cellAttr.GetOwnerSize(result);
            if (sizeWnd.Width.HasMinDiff(sizeClient.Width))
               return 0;
            if (sizeWnd.Width < sizeClient.Width)
               return -1;
            return +1;
         });
         Finder(2000, newHeight => {
            result.n = newHeight;
            var sizeWnd = cellAttr.GetOwnerSize(result);
            if (sizeWnd.Height.HasMinDiff(sizeClient.Height))
               return 0;
            if (sizeWnd.Height < sizeClient.Height)
               return -1;
            return +1;
         });
         return result;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient"> размер окна/области (в пикселях):
      /// in - в которую должна вписаться мозаика;
      /// out - в которую реально впишется мозаика;
      /// </param>
      /// <returns>площадь ячейки</returns>
      public static double FindAreaBySize(EMosaic mosaicType, Matrisize mosaicSizeField, ref SizeDouble sizeClient) {
         return FindAreaBySize(CreateAttributeInstance(mosaicType), mosaicSizeField, ref sizeClient);
      }

      /// <summary>get parent container (owner window) size in pixels</summary>
      public static SizeDouble GetOwnerSize(EMosaic mosaicType, double area, Matrisize mosaicSizeField) {
         var attr = CreateAttributeInstance(mosaicType);
         attr.Area = area;
         return attr.GetOwnerSize(mosaicSizeField);
      }

      #endregion

   }
}