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

	/** коэффициент уровня сложности в зависимости от типа мозаики - чем больше, тем сложнее */
	private static final Map<EMosaic, Double> mosaicCoefficient; // skill level coefficient
	static {
		mosaicCoefficient = new HashMap<EMosaic, Double>(EMosaic.values().length);
		final int area = 200; // пох
		for (EMosaic mosaicType : EMosaic.values()) {
			BaseCell.BaseAttribute attr = CellFactory.createAttributeInstance(mosaicType, area);

			// variant 1 - сложность в зависимости от кол-ва пересечений ячеек в одной точке
//			mosaicCoefficient.put(mosaicType, attr.getVertexIntersection());

			// variant 2 - сложность в зависимости от кол-ва соседних ячеек
//			int cntDir = attr.GetDirectionCount();
//			int neighbors = 0;
//			for (int i=0; i<cntDir; i++)
//				neighbors += attr.getNeighborNumber(i);
//			mosaicCoefficient.put(mosaicType, ((double)neighbors)/cntDir);

			// variant 3 - сложность в зависимости от кол-ва соседних ячеек и кол-ва точек пересечения
//			int cntDir = attr.GetDirectionCount();
//			int totalNeighbors = java.util.stream.IntStream.range(0, cntDir).reduce(0, (accum, i) -> accum+attr.getNeighborNumber(i));
//			double neighbors = ((double)totalNeighbors)/cntDir;
			double neighbors = (double)attr.getNeighborNumber(false);
			mosaicCoefficient.put(mosaicType, attr.getVertexIntersection()/neighbors);
			
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
/*
			Sq1  Tring2	    PentT5  Trapez1   Pent24   Tring1		
Neighbor	  8     8          8       8        7        12       
VertexInter   4     3.75      3.6     3.6      3.4       6       
eBeginner    15
eAmateur     54
eProfi      126
eCrazy      281
*/
	/** коэффициент уровня сложности */
	private double getCoefficient() {
		// variant 1
//		switch (this) {
//		case eBeginner: return 0.6;
//		case eAmateur : return 0.72;
//		case eProfi   : return 0.84;
//		case eCrazy   : return 0.9991111111111111;
//		}

		// variant 2
//		switch (this) {
//		case eBeginner: return 1.2;
//		case eAmateur : return 1.44;
//		case eProfi   : return 1.68;
//		case eCrazy   : return 1.9982222222222221;
//		default: break;
//		}

		// variant 3
		switch (this) {
		case eBeginner: return 0.075;
		case eAmateur : return 0.09;
		case eProfi   : return 0.105;
		case eCrazy   : return 0.12488888888888888;
		default: break;
		}
		throw new RuntimeException("Invalid method call. Для уровня сложности '"+this+"' нет коэффициента сложности.");
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
		throw new RuntimeException("Invalid method call. Для уровня сложности '"+this+"' нет размера поля по-умолчанию.");
	}

	/** Узнать кол-во мин на размере поля по-умолчанию */
	public int GetNumberMines(EMosaic eMosaic) {
		return GetNumberMines(eMosaic, this.DefaultSize());
	}

	/** Узнать кол-во мин на заданном размере поля */
	public int GetNumberMines(EMosaic eMosaic, Size customSizeMosaic) {
		if (customSizeMosaic == null)
			throw new IllegalArgumentException("customSizeMosaic must be not null");
		if (this == eCustom)
			throw new RuntimeException("Для уровня сложности '"+this+"' кол-во мин задаётся явно, а не расчитывается...");

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
