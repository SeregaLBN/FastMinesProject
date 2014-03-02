using System;
using System.Collections.Generic;

namespace ua.ksn.fmg.model.mosaics {

public enum EMosaicGroup {
	eTriangles,
	eQuadrangles,
	ePentagons,
	eHexagons,
	eOthers
}

public static class EMosaicGroupEx {

	public static EMosaicGroup fromIndex(int index) {
		return (EMosaicGroup)index;
		//foreach (EMosaicGroup item in Enum.GetValues(typeof(EMosaicGroup)))
		//    if ((int)item == index)
		//        return item;
		//throw new ArgumentException("Invalid paramenter value " + index);
	}
	public static int getIndex(this EMosaicGroup self) { return (int)self; }

	public static List<EMosaic> getBind(this EMosaicGroup self) {
		List<EMosaic> mosaics = new List<EMosaic>();
		switch (self) {
		case EMosaicGroup.eTriangles:
			mosaics.Add(EMosaic.eMosaicTriangle1);
			mosaics.Add(EMosaic.eMosaicTriangle2);
			mosaics.Add(EMosaic.eMosaicTriangle3);
			mosaics.Add(EMosaic.eMosaicTriangle4);
			break;
		case EMosaicGroup.eQuadrangles:
			mosaics.Add(EMosaic.eMosaicSquare1);
			mosaics.Add(EMosaic.eMosaicSquare2);
			mosaics.Add(EMosaic.eMosaicParquet1);
			mosaics.Add(EMosaic.eMosaicParquet2);
			mosaics.Add(EMosaic.eMosaicTrapezoid1);
			mosaics.Add(EMosaic.eMosaicTrapezoid2);
			mosaics.Add(EMosaic.eMosaicTrapezoid3);
			mosaics.Add(EMosaic.eMosaicRhombus1);
			mosaics.Add(EMosaic.eMosaicQuadrangle1);
			mosaics.Add(EMosaic.eMosaicPenrousePeriodic1);
			break;
		case EMosaicGroup.ePentagons:
			mosaics.Add(EMosaic.eMosaicPentagonT24);
			mosaics.Add(EMosaic.eMosaicPentagonT5);
			mosaics.Add(EMosaic.eMosaicPentagonT10);
			break;
		case EMosaicGroup.eHexagons:
			mosaics.Add(EMosaic.eMosaicHexagon1);
			break;
		case EMosaicGroup.eOthers:
			mosaics.Add(EMosaic.eMosaicTrSq1);
			mosaics.Add(EMosaic.eMosaicTrSq2);
			mosaics.Add(EMosaic.eMosaicSqTrHex);
			break;
		}
		return mosaics;
	}

	/// <summary>
	/// Описание для пользователя
	/// </summary>
	/// <param name="self"></param>
	/// <returns></returns>
	public static String GetDescription(this EMosaicGroup self) {
		return self.ToString().Substring(1);
	}
}
}