package fmg.core.img;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.geom.*;
import fmg.core.img.IMosaicAnimatedModel.EMosaicRotateMode;
import fmg.core.mosaic.IMosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;

public class TestDrawing2 {

    public Random getRandom() { return ThreadLocalRandom.current(); }
    public int r(int max) { return getRandom().nextInt(max); }
    public boolean bl() { return getRandom().nextBoolean(); } // random bool
    public int np() { return (bl() ? -1 : +1); } // negative or positive

    private final String titlePrefix;

    public TestDrawing2(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }

    public void changeSettings(IImageModel2 model, boolean testTransparent) {
        testTransparent = testTransparent || bl(); // probability 75%

        if (!(model instanceof BurgerMenuModel2)) {
            double pad = Math.min(model.getSize().height/3, model.getSize().width/3);
            model.setPadding(new BoundDouble(-pad/4 + r((int)pad)));
        }

        Color bkClr = Color.RandomColor();
        if (testTransparent)
            bkClr = bkClr.updateA(50 + r(10));


        if (model instanceof LogoModel2) {
            var m = (LogoModel2)model;
            m.setBorderColor(Color.RandomColor());
            m.setBorderWidth(r(4));
            m.setUseGradient(bl());
        } else
        if (model instanceof BurgerMenuModel2) {
            var m = (BurgerMenuModel2)model;
            if (bl())
                m.setLayers(4);
            m.setHorizontal(bl());
        } else
        if (model instanceof MosaicGroupModel2) {
            MosaicGroupModel2 m = (MosaicGroupModel2)model;
            m.setBorderColor(Color.RandomColor());
            m.setBorderWidth(r(3));
            m.setBackgroundColor(bkClr);

            var fgColor = Color.RandomColor();//.brighter();
            if (testTransparent) {
                // test transparent
                if ((m.getBorderWidth() > 0) && (r(4) == 0)) {
                    fgColor = fgColor.updateA(Color.Transparent().getA());
                } else {
                    fgColor = fgColor.updateA(150 + r(105));
                }
            }
            m.setForegroundColor(fgColor);
        } else
        if (model instanceof MosaicGameModel) {
            MosaicGameModel mgm = (MosaicGameModel)model;
            mgm.setSizeField(new Matrisize(3+r(2), 3 + r(2)));

            if (model instanceof IMosaicDrawModel) {
                IMosaicDrawModel<?> mdm = (IMosaicDrawModel<?>)model;
                mdm.setBackgroundColor(bkClr);

                mdm.getCellFill().setMode(1 + r(mdm.getShape().getMaxCellFillModeValue()));

                mdm.getPenBorder().setWidth(1 + r(2));
                SizeDouble size = mdm.getSize();
                double padLeftRight = r((int)(size.width /3));
                double padTopBottom = r((int)(size.height/3));
                mdm.setPadding(new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom));

                if (model instanceof IMosaicAnimatedModel) {
                    IMosaicAnimatedModel<?> mam = (IMosaicAnimatedModel<?>)model;

                    EMosaicRotateMode[] eRotateModes = EMosaicRotateMode.values();
                    mam.setRotateMode(eRotateModes[r(eRotateModes.length)]);
                }
            }
        }
    }

    public static class CellTilingInfo {
        /** index of column */
        public int i;
        /** index of row */
        public int j;
        public PointDouble imageOffset;
    }

    public static class CellTilingResult2 {
        public SizeDouble imageSize;
        public Size tableSize;
        public Function<IImageController2<?,?> /* imageControllers */, CellTilingInfo> itemCallback;
    }

    public CellTilingResult2 cellTiling(RectDouble rc, List<IImageController2<?,?>> images, boolean tileIntersection) {
        int len = images.size();

        // max tiles in one column
        IntUnaryOperator mtoc = colsTotal -> (int)Math.ceil(len / (double)colsTotal);

        // для предполагаемого кол-ва рядков нахожу макс кол-во плиток в строке
        // и возвращаю отношение меньшей стороны к большей
        IntToDoubleFunction f = colsTotal -> {
            int mCnt = mtoc.applyAsInt(colsTotal);
            double tailW = rc.width / colsTotal;
            double tailH = rc.height / mCnt;
            return (tailW < tailH)
                ? tailW/tailH
                : tailH/tailW;
        };

        int colsOpt = 1;
        {
            double xToY = 0; // отношение меньшей стороны к большей
            // ищу оптимальное кол-во рядков для расположения плиток. Оптимальным считаю такое расположение,
            // при котором плитки будут наибольше похожими на квадрат (т.е. отношение меньшей стороны к большей будет максимальней)
            for (int i=1; i<=len; ++i) {
                double xy = f.applyAsDouble(i);
                if (xy < xToY)
                    break;
                colsOpt = i;
                xToY = xy;
            }
        }

        int cols = colsOpt;
        int rows = (int)Math.ceil(len/(double)cols);
        double dx = rc.width  / cols; // cell tile width
        double dy = rc.height / rows; // cell tile height

        int pad = 2; // cell padding
        double addonX = (cols==1) ? 0 : !tileIntersection ? 0 : dx/4; // test intersection
        double addonY = (rows==1) ? 0 : !tileIntersection ? 0 : dy/4; // test intersection
        SizeDouble imgSize = new SizeDouble(dx - 2*pad + addonX,  // dx - 2*pad;
                                            dy - 2*pad + addonY); // dy - 2*pad;

        Function<IImageController2<?,?>, CellTilingInfo> itemCallback = item -> {
            int pos = images.indexOf(item);
            if (pos == -1)
                throw new RuntimeException("Illegal usage...");

            int i = pos % cols;
            int j = pos / cols;
            PointDouble offset = new PointDouble(rc.x + i*dx + pad,
                                                 rc.y + j*dy + pad);
            if (i == (cols-1))
                offset.x -= addonX;
            if (j == (rows-1))
                offset.y -= addonY;

            CellTilingInfo cti = new CellTilingInfo();
            cti.i = i;
            cti.j = j;
            cti.imageOffset = offset;
            return cti;
        };

        CellTilingResult2 ctr = new CellTilingResult2();
        ctr.imageSize = imgSize;
        ctr.tableSize = new Size(cols, rows);
        ctr.itemCallback = itemCallback;
        return ctr;
    }

    public String getTitle(List<IImageController2<?,?>> images) {
        return titlePrefix + " test paints: " + images.stream()
            .map(i -> i.getClass().getName())
            .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replace("$", ".") )
            .collect(Collectors.groupingBy(z -> z))
            .entrySet().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" & "));
    }

}
