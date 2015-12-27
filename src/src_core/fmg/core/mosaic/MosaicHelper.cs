using System;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.core.mosaic.draw;

namespace fmg.core.mosaic {

   public static class MosaicHelper {
      private static string GetPackageName() { return typeof(MosaicHelper).Namespace; }

      /// <summary>Создать экземпляр атрибута для конкретного типа мозаики</summary>
      public static BaseCell.BaseAttribute CreateAttributeInstance(EMosaic mosaicType, int area) {
         //		switch (mosaicType) {
         //		case eMosaicTriangle1  : return new Triangle1.AttrTriangle1(area);
         //		// ...
         //		case eMosaicSquare1    : return new Square1.AttrSquare1(area);
         //		// ...
         //		}
         //		throw new Exception("Unknown type "+mosaicType);

         try {
            var className = GetPackageName() + ".cells." + mosaicType.GetMosaicClassName() + "+Attr" + mosaicType.GetMosaicClassName();
            var cellAttrClass = Type.GetType(className);
            object[] args = { area };
            var attr = (BaseCell.BaseAttribute)Activator.CreateInstance(cellAttrClass, args);
            return attr;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
            throw new Exception("Unknown type " + mosaicType + ": " + ex.Message, ex);
         }
      }

      /// <summary>Создать экземпляр ячейки для конкретного типа мозаики</summary>
      public static BaseCell CreateCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord) {
         //		switch (mosaicType) {
         //		case eMosaicTriangle1  : return new Triangle1((Triangle1.AttrTriangle1) attr, coord);
         //		//...
         //		case eMosaicSquare1    : return new Square1((Square1.AttrSquare1) attr, coord);
         //		//...
         //		}
         //		throw new RuntimeException("Unknown type "+mosaicType);

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
      /// <param name="baseMin">стартовое значение для поиска</param>
      /// <param name="baseDelta">начало дельты приращения</param>
      /// <param name="comparable">ф-ция сравнения</param>
      /// <returns>что найдено</returns>
      private static int Finder(int baseMin, int baseDelta, Func<int, int> comparable) {
         double res = baseMin;
         double d = baseDelta;
         bool deltaUp = true, lastSmall = true;
         do {
            if (deltaUp)
               d *= 2;
            else
               d /= 2;

            if (lastSmall)
               res += d;
            else
               res -= d;

            var z = comparable((int)res);
            if (z == 0)
               return (int)res;
            lastSmall = (z < 0);
            deltaUp = deltaUp && lastSmall;
         } while (d > 1);
         return (int)res;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>площадь ячейки</returns>
      private static int FindAreaBySize(BaseCell.BaseAttribute cellAttr, Size mosaicSizeField, Size sizeClient) {
         // сделал приватным, т.к. неявно меняет свойства параметра 'cellAttr'
         return Finder(MosaicBase<IPaintable>.AREA_MINIMUM, 53,
            area => {
               cellAttr.Area = area;
               var sizeWnd = cellAttr.GetOwnerSize(mosaicSizeField);
               if ((sizeWnd.width == sizeClient.width) &&
                   (sizeWnd.height == sizeClient.height))
                  return 0;
               if ((sizeWnd.width <= sizeClient.width) &&
                   (sizeWnd.height <= sizeClient.height))
                  return -1;
               return +1;
            });
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="cellAttr">метаданные ячеек</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>размер поля мозаики</returns>
      public static Size FindSizeByArea(BaseCell.BaseAttribute cellAttr, Size sizeClient) {
         var result = new Size();
         Finder(1, 10, newWidth => {
            result.width = newWidth;
            var sizeWnd = cellAttr.GetOwnerSize(result);
            if (sizeWnd.width == sizeClient.width)
               return 0;
            if (sizeWnd.width <= sizeClient.width)
               return -1;
            return +1;
         });
         Finder(1, 10, newHeight => {
            result.height = newHeight;
            var sizeWnd = cellAttr.GetOwnerSize(result);
            if (sizeWnd.width == sizeClient.height)
               return 0;
            if (sizeWnd.height <= sizeClient.height)
               return -1;
            return +1;
         });
         return result;
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>площадь ячейки</returns>
      public static int FindAreaBySize(EMosaic mosaicType, Size mosaicSizeField, Size sizeClient) {
         return FindAreaBySize(CreateAttributeInstance(mosaicType, 0), mosaicSizeField, sizeClient);
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>размер поля мозаики</returns>
      public static Size FindSizeByArea(EMosaic mosaicType, int area, Size sizeClient) {
         return FindSizeByArea(CreateAttributeInstance(mosaicType, area), sizeClient);
      }

      /// <summary>get parent container (owner window) size in pixels</summary>
      public static Size GetOwnerSize(EMosaic mosaicType, int area, Size mosaicSizeField) {
         return CreateAttributeInstance(mosaicType, area).GetOwnerSize(mosaicSizeField);
      }

      #endregion

   }
}