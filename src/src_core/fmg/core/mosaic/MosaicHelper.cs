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

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>макс площадь ячейки</returns>
      public static int FindAreaBySize(BaseCell.BaseAttribute cellAttr, Size mosaicSizeField, Size sizeClient) {
         return cellAttr.CalcOptimalArea(MosaicBase<IPaintable>.AREA_MINIMUM, mosaicSizeField, sizeClient);
      }

      /// <summary> узнаю мах размер площади ячеек мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="mosaicSizeField">интересуемый размер (в ячейках) поля мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>макс площадь ячейки</returns>
      public static int FindAreaBySize(EMosaic mosaicType, Size mosaicSizeField, Size sizeClient) {
         return FindAreaBySize(CreateAttributeInstance(mosaicType, 0), mosaicSizeField, sizeClient);
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>max размер поля мозаики</returns>
      public static Size FindSizeByArea(BaseCell.BaseAttribute cellAttr, int area, Size sizeClient) {
         return cellAttr.CalcOptimalMosaicSize(area, sizeClient);
      }

      /// <summary> узнаю max размер поля мозаики, при котором вся мозаика помещается в заданную область </summary>
      /// <param name="area">интересуемая площадь ячеек мозаики</param>
      /// <param name="sizeClient">размер окна/области (в пикселях) в которую должна вписаться мозаика</param>
      /// <returns>max размер поля мозаики</returns>
      public static Size FindSizeByArea(EMosaic mosaicType, int area, Size sizeClient) {
         return FindSizeByArea(CreateAttributeInstance(mosaicType, 0), area, sizeClient);
      }

   }
}