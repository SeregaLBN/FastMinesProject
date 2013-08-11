package ua.ksn.fmg.controller.types;

import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;

import ua.ksn.fmg.model.mosaics.CellFactory;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.geom.Size;

public enum ESkillLevel {
	eBeginner,
	eAmateur,
	eProfi,
	eCrazy,
	eCustom;

	/** коэффициент уровн€ сложности в зависимости от типа мозаики - чем больше, тем сложнее */
	private static final Map<EMosaic, Double> mosaicCoefficient; // skill level coefficiennt
	static {
		mosaicCoefficient = new HashMap<EMosaic, Double>(EMosaic.values().length);
		final int area = 200; // пох
		for (EMosaic mosaicType : EMosaic.values()) {
			BaseCell.BaseAttribute attr = CellFactory.createAttributeInstance(mosaicType, area);

			// variant 1 - сложность в зависимости от кол-ва пересечений €чеек в одной точке
//			mosaicCoefficient.put(mosaicType, attr.getVertexIntersection());

			// variant 2 - сложность в зависимости от кол-ва соседних €чеек
			int cntDir = attr.GetDirectionCount();
			int neighbors = 0;
			for (int i=0; i<cntDir; i++)
				neighbors += attr.getNeighborNumber(i);
			mosaicCoefficient.put(mosaicType, ((double)neighbors)/cntDir);

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
	private double getCoefficient() {
		// variant 1
//		switch (this) {
//		case eBeginner: return 0.6;
//		case eAmateur : return 0.72;
//		case eProfi   : return 0.84;
//		case eCrazy   : return 0.9991111111111111;
//		}
		// variant 2
		switch (this) {
		case eBeginner: return 1.2;
		case eAmateur : return 1.44;
		case eProfi   : return 1.68;
		case eCrazy   : return 1.9982222222222221;
		default: break;
		}
		throw new RuntimeException("Invalid method call. ƒл€ уровн€ сложности '"+this+"' нет коэффициента сложности.");
	}

	/** размеры полей */
	public Size DefaultSize() {
		switch (this) {
		case eBeginner: return new Size(10, 10); // 15
		case eAmateur : return new Size(20, 15); // 54
		case eProfi   : return new Size(30, 20); // 126
		case eCrazy   : return new Size(45, 25); // 281
		default: break;
		}
		throw new RuntimeException("Invalid method call. ƒл€ уровн€ сложности '"+this+"' нет размера пол€ по-умолчанию.");
	}

	/** ”знать кол-во мин на размере пол€ по-умолчанию */
	public int GetNumberMines(EMosaic eMosaic) {
		return GetNumberMines(eMosaic, this.DefaultSize());
	}

	/** ”знать кол-во мин на заданном размере пол€ */
	public int GetNumberMines(EMosaic eMosaic, Size customSizeMosaic) {
		if (customSizeMosaic == null)
			throw new IllegalArgumentException("customSizeMosaic must be not null");
		if (this == eCustom)
			throw new RuntimeException("ƒл€ уровн€ сложности '"+this+"' кол-во мин задаЄтс€ €вно, а не расчитываетс€...");

		return (int) (customSizeMosaic.width * customSizeMosaic.height * getCoefficient() / mosaicCoefficient.get(eMosaic));
	}

	public String getDescription() {
		switch (this) {
		case eProfi: return "Professional";
		default    : return this.toString().substring(1);
		}
	}

	public static ESkillLevel fromOrdinal(int ordinal) {
		if ((ordinal < 0) || (ordinal >= ESkillLevel.values().length))
			throw new IndexOutOfBoundsException("Invalid ordinal");
		return ESkillLevel.values()[ordinal];
	}
}
