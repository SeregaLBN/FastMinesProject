package fmg.core.model.mosaics;

import java.util.ArrayList;
import java.util.List;

public enum EMosaicGroup {
	eTriangles,
	eQuadrangles,
	ePentagons,
	eHexagons,
	eOthers;

	public List<EMosaic> getBind() {
		List<EMosaic> mosaics = new ArrayList<EMosaic>();
		switch (this) {
		case eTriangles:
			mosaics.add(EMosaic.eMosaicTriangle1);
			mosaics.add(EMosaic.eMosaicTriangle2);
			mosaics.add(EMosaic.eMosaicTriangle3);
			mosaics.add(EMosaic.eMosaicTriangle4);
			break;
		case eQuadrangles:
			mosaics.add(EMosaic.eMosaicSquare1);
			mosaics.add(EMosaic.eMosaicSquare2);
			mosaics.add(EMosaic.eMosaicParquet1);
			mosaics.add(EMosaic.eMosaicParquet2);
			mosaics.add(EMosaic.eMosaicTrapezoid1);
			mosaics.add(EMosaic.eMosaicTrapezoid2);
			mosaics.add(EMosaic.eMosaicTrapezoid3);
			mosaics.add(EMosaic.eMosaicRhombus1);
			mosaics.add(EMosaic.eMosaicQuadrangle1);
			mosaics.add(EMosaic.eMosaicPenrousePeriodic1);
			break;
		case ePentagons:
			mosaics.add(EMosaic.eMosaicPentagonT24);
			mosaics.add(EMosaic.eMosaicPentagonT5);
			mosaics.add(EMosaic.eMosaicPentagonT10);
			break;
		case eHexagons:
			mosaics.add(EMosaic.eMosaicHexagon1);
			break;
		case eOthers:
			mosaics.add(EMosaic.eMosaicTrSq1);
			mosaics.add(EMosaic.eMosaicTrSq2);
			mosaics.add(EMosaic.eMosaicSqTrHex);
			break;
		}
		return mosaics;
	}

	public String getDescription() {
		return this.toString().substring(1);
	}
}