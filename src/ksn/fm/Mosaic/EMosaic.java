package ksn.fm.Mosaic;

public enum EMosaic {
//	// Triangle
//	eMosaicTriangle1,
//	eMosaicTriangle2,
//	eMosaicTriangle3,
//	eMosaicTriangle4,
	// Quadrangle
	eMosaicSquare1,
//	eMosaicSquare2,
//	eMosaicParquet1,
//	eMosaicParquet2,
//	eMosaicTrapezoid1,
//	eMosaicTrapezoid2,
//	eMosaicTrapezoid3,
//	eMosaicRhombus1,
//	eMosaicQuadrangle1,
//	// Pentagon
//	eMosaicPentagonT24,
//	eMosaicPentagonT5,
//	eMosaicPentagonT10,
//	// Hexagon
//	eMosaicHexagon1,
//	// Other
//	eMosaicTrSq1,
//	eMosaicTrSq2,
//	eMosaicSqTrHex,
	//
	eMosaicNil;  //  всегда последняя в перечислении !!!

	public static EMosaic valueOf(int val)
	{
		for(EMosaic msg: EMosaic.values())
			if(msg.ordinal() == val)
				return msg;
		throw new IllegalArgumentException("enum EMosaic("+val+") not found");
	}
}
