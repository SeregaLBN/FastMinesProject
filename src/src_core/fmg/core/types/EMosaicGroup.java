package fmg.core.types;

import java.util.ArrayList;
import java.util.List;

public enum EMosaicGroup {

    eTriangles,
    eQuadrangles,
    ePentagons,
    eHexagons,
    eOthers;

    /** return mosaics in this group */
    public List<EMosaic> getBind() {
        List<EMosaic> mosaics = new ArrayList<EMosaic>();
        for (EMosaic mosaic : EMosaic.values())
            if (mosaic.getGroup() == this)
                mosaics.add(mosaic);
        return mosaics;
    }

    public String getDescription() {
        return this.toString().substring(1);
    }

    public static EMosaicGroup fromOrdinal(int ordinal) {
        if ((ordinal < 0) || (ordinal >= EMosaicGroup.values().length))
            throw new IndexOutOfBoundsException("Invalid ordinal");
        return EMosaicGroup.values()[ordinal];
    }

}