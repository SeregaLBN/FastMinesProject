package fmg.core.types;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum EMosaicGroup {

    eTriangles,
    eQuadrangles,
    ePentagons,
    eHexagons,
    eOthers;

    /** Описание для пользователя */
    public String getDescription() {
        return this.toString().substring(1);
    }

    public static EMosaicGroup fromOrdinal(int ordinal) {
        if ((ordinal < 0) || (ordinal >= EMosaicGroup.values().length))
            throw new IndexOutOfBoundsException("Invalid ordinal");
        return EMosaicGroup.values()[ordinal];
    }

    /** return mosaics in this group */
    public List<EMosaic> getMosaics() {
        return Stream.of(EMosaic.values())
                .filter(m -> m.getGroup() == this)
                .collect(Collectors.toList());
    }

    public char unicodeChar(boolean dark) {
        switch (this) {
        case eTriangles  : return dark ? '\u25B2' : '\u25B3'; // http://unicode-table.com/en/search/?q=triangle
        case eQuadrangles: return dark ? '\u25AE' : '\u25AF'; // http://unicode-table.com/en/search/?q=rectangle
        case ePentagons  : return dark ? '\u2B1F' : '\u2B20'; // http://unicode-table.com/en/search/?q=pentagon
        case eHexagons   : return dark ? '\u2B22' : '\u2B21'; // http://unicode-table.com/en/search/?q=hexagon
        case eOthers     : return dark ? '\u2605' : '\u2606'; // http://unicode-table.com/en/blocks/miscellaneous-symbols/
                         //return dark ? '\u25E9' : '\u2B15'; // http://unicode-table.com/en/search/?q=Square+with+Left+Diagonal+Half+Black
        }
        throw new IllegalArgumentException("Invalid parameter value " + this);
    }

}