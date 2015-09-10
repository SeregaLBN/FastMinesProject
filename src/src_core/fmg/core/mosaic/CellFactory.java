package fmg.core.mosaic;

import java.lang.reflect.Constructor;

import fmg.common.geom.Coord;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

/** Фабрика для создания экземпляров класса ячеек и их атрибутов */
public class CellFactory {
	private static final String getPackageName() {
		Package pkg = CellFactory.class.getPackage();
		if (pkg != null)
			return pkg.getName();
		String[] arr = CellFactory.class.getName().split("\\.");
		String name = "";
		for (int i=0; i<arr.length-1; i++) {
			if (!name.isEmpty())
				name += '.';
			name += arr[i];
		}
		return name;
	}

	/** Создать экземпляр атрибута для конкретного типа мозаики */
	public static final BaseCell.BaseAttribute createAttributeInstance(EMosaic mosaicType, int area) {
//		switch (mosaicType) {
//		case eMosaicTriangle1  : return new Triangle1.AttrTriangle1(area);
//		// ...
//		case eMosaicSquare1    : return new Square1.AttrSquare1(area);
//		// ...
//		}
//		throw new RuntimeException("Unknown type "+mosaicType);

		try {
			String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName() + "$Attr"+mosaicType.getMosaicClassName();
			@SuppressWarnings("unchecked")
			Class<? extends BaseCell.BaseAttribute> cellAttrClass = (Class<? extends BaseCell.BaseAttribute>)Class.forName(className);
			Constructor<? extends BaseCell.BaseAttribute> constructor = cellAttrClass.getConstructor(int.class); //(Constructor<? extends BaseAttribute>) cellClass.getConstructors()[0]; // 
			BaseCell.BaseAttribute attr = constructor.newInstance(area);
			return attr;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
		}
	}

	/** Создать экземпляр ячейки для конкретного типа мозаики */
	public static final BaseCell createCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord)
	{
//		switch (mosaicType) {
//		case eMosaicTriangle1  : return new Triangle1((Triangle1.AttrTriangle1) attr, coord);
//		//...
//		case eMosaicSquare1    : return new Square1((Square1.AttrSquare1) attr, coord);
//		//...
//		}
//		throw new RuntimeException("Unknown type "+mosaicType);

		try {
			String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName();
			@SuppressWarnings("unchecked")
			Class<? extends BaseCell> cellClass = (Class<? extends BaseCell>)Class.forName(className);

			Constructor<? extends BaseCell> constructor = cellClass.getConstructor(attr.getClass(), coord.getClass()); // cellClass.getConstructors()[0];
			BaseCell cell = constructor.newInstance(attr, coord);
			cell.Init();
			return cell;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
		}
	}
}
