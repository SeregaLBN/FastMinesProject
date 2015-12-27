package fmg.core.mosaic;

import java.lang.reflect.Constructor;

import fmg.common.geom.Coord;
import fmg.common.geom.Size;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

public final class MosaicHelper {
	private static final String getPackageName() {
		Package pkg = MosaicHelper.class.getPackage();
		if (pkg != null)
			return pkg.getName();
		String[] arr = MosaicHelper.class.getName().split("\\.");
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

	/**
	 * Поиск больше-меньше
	 * @param baseMin - стартовое значение для поиска
	 * @param baseDelta - начало дельты приращения
	 * @param func - ф-ция сравнения
	 * @return что найдено
	 */
	static int Finder(int baseMin, int baseDelta, Comparable<Integer> func) {
		double res = baseMin;
		double d = baseDelta;
		boolean deltaUp = true, lastSmall = true;
		do {
			if (deltaUp)
				d *= 2;
			else
				d /= 2;

			if (lastSmall)
				res += d;
			else
				res -= d;

			int z = func.compareTo((int)res);
			if (z == 0)
				return (int)res;
			lastSmall = (z < 0);
			deltaUp = deltaUp && lastSmall;
		} while(d > 1);
		return (int)res;
	}

	/** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в заданную область
	 * @param mosaicSizeField - интересуемый размер (в ячейках) поля мозаики
	 * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
	 * @return площадь ячейки
	 */
	private static int findAreaBySize(BaseCell.BaseAttribute cellAttr, final Size mosaicSizeField, final Size sizeClient) {
		return Finder(MosaicBase.AREA_MINIMUM, 53, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer area) {
				cellAttr.setArea(area);
				Size sizeWnd = cellAttr.getOwnerSize(mosaicSizeField);
				if ((sizeWnd.width == sizeClient.width) &&
				   (sizeWnd.height == sizeClient.height))
				  return 0;
				if ((sizeWnd.width <= sizeClient.width) &&
					(sizeWnd.height <= sizeClient.height))
					return -1;
				return +1;
			}
		});
	}

	/** узнаю max размер поля мозаики, при котором окно проекта вмещается в в заданную область
	 * @param cellAttr - метаданные ячеек
	 * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
	 * @return размер поля мозаики
	 */
	public static Size findSizeByArea(BaseCell.BaseAttribute cellAttr, final Size sizeClient) {
		final Size result = new Size();
		Finder(1, 10, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer newWidth) {
				result.width = newWidth;
				Size sizeWnd = cellAttr.getOwnerSize(result);
				if (sizeWnd.width == sizeClient.width)
					return 0;
				if (sizeWnd.width <= sizeClient.width)
					return -1;
				return +1;
			}
		});
		Finder(1, 10, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer newHeight) {
				result.height = newHeight;
				Size sizeWnd = cellAttr.getOwnerSize(result);
				if (sizeWnd.width == sizeClient.height)
					return 0;
				if (sizeWnd.height <= sizeClient.height)
					return -1;
				return +1;
			}
		});
		return result;
	}

	/** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в заданную область
	 * @param mosaicSizeField - интересуемый размер (в ячейках) поля мозаики
	 * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
	 * @return площадь ячейки
	 */
	public static int findAreaBySize(EMosaic mosaicType, Size mosaicSizeField, Size sizeClient) {
		return findAreaBySize(createAttributeInstance(mosaicType, 0), mosaicSizeField, sizeClient);
	}

	/** узнаю max размер поля мозаики, при котором окно проекта вмещается в в заданную область
	 * @param area - интересуемая площадь ячеек мозаики
	 * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
	 * @return размер поля мозаики
	 */
	public static Size findSizeByArea(EMosaic mosaicType, int area, Size sizeClient) {
		return findSizeByArea(createAttributeInstance(mosaicType, area), sizeClient);
	}

    /** get parent container (owner window) size in pixels */
    public static Size getOwnerSize(EMosaic mosaicType, int area, Size mosaicSizeField) {
       return createAttributeInstance(mosaicType, area).getOwnerSize(mosaicSizeField);
    }

}
