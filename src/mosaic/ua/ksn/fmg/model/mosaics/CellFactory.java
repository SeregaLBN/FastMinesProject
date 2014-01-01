package ua.ksn.fmg.model.mosaics;

import java.lang.reflect.Constructor;

import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.geom.Coord;

/** ������� ��� �������� ����������� ������ ����� � �� ��������� */
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

	/** ������� ��������� �������� ��� ����������� ���� ������� */
	public static final BaseCell.BaseAttribute createAttributeInstance(EMosaic mosaicType, int area) {
//		switch (mosaicType) {
//		case eMosaicTriangle1  : return new Triangle1.AttrTriangle1(area);
//		// ...
//		case eMosaicSquare1    : return new Square1.AttrSquare1(area);
//		// ...
//		}
//		throw new RuntimeException("Unknown type "+mosaicType);

		try {
			String className = getPackageName() + ".cell." + mosaicType.getMosaicClassName() + "$Attr"+mosaicType.getMosaicClassName();
			@SuppressWarnings("unchecked")
			Class<? extends BaseCell.BaseAttribute> cellClass = (Class<? extends BaseCell.BaseAttribute>)Class.forName(className);
			Constructor<? extends BaseCell.BaseAttribute> constructor = cellClass.getConstructor(int.class); //(Constructor<? extends BaseAttribute>) cellClass.getConstructors()[0]; // 
			BaseCell.BaseAttribute attr = constructor.newInstance(area);
			return attr;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
		}
	}

	/** ������� ��������� ������ ��� ����������� ���� ������� */
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
			String className = getPackageName() + ".cell." + mosaicType.getMosaicClassName();
			@SuppressWarnings("unchecked")
			Class<? extends BaseCell> cellClass = (Class<? extends BaseCell>)Class.forName(className);

			Constructor<? extends BaseCell> constructor = cellClass.getConstructor(attr.getClass(), coord.getClass()); // cellClass.getConstructors()[0];
			BaseCell cell = constructor.newInstance(attr, coord);
			return cell;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace(System.err);
			throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
		}
	}
}
