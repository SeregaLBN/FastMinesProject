using System;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

public static class MosaicHelper {
   private static string GetPackageName() { return typeof (MosaicHelper).Namespace; }

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
         var className = GetPackageName() + ".cells." + mosaicType.GetMosaicClassName() + "+Attr"+mosaicType.GetMosaicClassName();
         var cellAttrClass = Type.GetType(className);
         object[] args = { area };
         var attr = (BaseCell.BaseAttribute)Activator.CreateInstance(cellAttrClass, args);
         return attr;
      } catch (Exception ex) {
         System.Diagnostics.Debug.Assert(false, ex.Message);
         throw new Exception("Unknown type "+mosaicType + ": "+ex.Message, ex);
      }
	}

	/** Создать экземпляр ячейки для конкретного типа мозаики */
	public static BaseCell CreateCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord)
	{
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
}
}