using System;
using System.Reflection;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics.cell;

namespace ua.ksn.fmg.model.mosaics {

/// <summary>Фабрика для создания экземпляров класса ячеек и их атрибутов</summary>
public static class CellFactory {
	private static string getPackageName() {
      return typeof(CellFactory).GetType().AssemblyQualifiedName;
	}

	/// <summary>Создать экземпляр атрибута для конкретного типа мозаики</summary>
	public static BaseCell.BaseAttribute createAttributeInstance(EMosaic mosaicType, int area) {
//		switch (mosaicType) {
//		case eMosaicTriangle1  : return new Triangle1.AttrTriangle1(area);
//		// ...
//		case eMosaicSquare1    : return new Square1.AttrSquare1(area);
//		// ...
//		}
//		throw new Exception("Unknown type "+mosaicType);

      try {
         string className = getPackageName() + ".cell." + mosaicType.getMosaicClassName() + "$Attr"+mosaicType.getMosaicClassName();
         Type t = Type.GetType(className);
         Object[] args = { area };
         BaseCell.BaseAttribute attr = (BaseCell.BaseAttribute)Activator.CreateInstance(t, args);
         return attr;
      } catch (Exception ex) {
         System.Diagnostics.Debug.Assert(false, ex.Message);
         throw new Exception("Unknown type "+mosaicType + ": "+ex.Message, ex);
      }
	}

	/** Создать экземпляр ячейки для конкретного типа мозаики */
	public static BaseCell createCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord)
	{
//		switch (mosaicType) {
//		case eMosaicTriangle1  : return new Triangle1((Triangle1.AttrTriangle1) attr, coord);
//		//...
//		case eMosaicSquare1    : return new Square1((Square1.AttrSquare1) attr, coord);
//		//...
//		}
//		throw new RuntimeException("Unknown type "+mosaicType);

      try {
         string className = getPackageName() + ".cell." + mosaicType.getMosaicClassName();
         Type t = Type.GetType(className);
         Object[] args = { attr, coord };
         BaseCell cell = (BaseCell)Activator.CreateInstance(t, args);
         return cell;
      } catch (Exception ex) {
         System.Diagnostics.Debug.Assert(false, ex.Message);
         throw new Exception("Unknown type " + mosaicType + ": " + ex.Message, ex);
      }
	}
}
}