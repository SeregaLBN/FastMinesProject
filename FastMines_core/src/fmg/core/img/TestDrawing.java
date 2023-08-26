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
import fmg.core.mosaic.MosaicModel;

public class TestDrawing {

    public static Random getRandom() { return ThreadLocalRandom.current(); }
    public static int r(int max) { return getRandom().nextInt(max); }
    public <E extends Enum<?>> E en(Class<E> clazz) { // random enum value
        var all = clazz.getEnumConstants();
        return all[r(all.length)];
    }
    public static boolean bl() { return getRandom().nextBoolean(); } // random bool
    public static int np() { return (bl() ? -1 : +1); } // negative or positive

    private final String titlePrefix;

    public TestDrawing(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }

    public void changeSettings(IImageController<?,?> ctrller, boolean testTransparent) {
        changeSettings(ctrller.getModel(), testTransparent);

        if (ctrller instanceof MosaicImageController) {
            var c = (MosaicImageController<?, ?>)ctrller;
            c.setAnimatePeriod(1000L + r(3000));
            c.setFps(30 + r(30));
            c.setClockwise(bl());
            c.setRotateImage(bl());
            c.setPolarLightsBackground(bl());
        } else
        if (ctrller instanceof LogoController) {
            var c = (LogoController<?, ?>)ctrller;
            c.setAnimatePeriod(2000L + r(7000));
            c.setFps(30 + r(30));
            c.setClockwise(bl());
            c.setRotateImage(bl());
            c.setPolarLights(bl());
        } else
        if (ctrller instanceof MosaicGroupController) {
            var c = (MosaicGroupController<?, ?>)ctrller;
            changeSettings(c.getBurgerModel(), testTransparent);
            c.setAnimatePeriod(2000L + r(7000));
            c.setFps(30 + r(30));
            c.setClockwise(bl());
            c.setRotateImage(bl());
            c.setPolarLightsBackground(bl());
            c.setPolarLightsForeground(bl());
        } else
        if (ctrller instanceof MosaicSkillController) {
            var c = (MosaicSkillController<?, ?>)ctrller;
            changeSettings(c.getBurgerModel(), testTransparent);
            c.setAnimatePeriod(2000L + r(7000));
            c.setFps(30 + r(30));
            c.setClockwise(bl());
            c.setRotateImage(bl());
            c.setPolarLightsBackground(bl());
            c.setPolarLightsForeground(bl());
        }
    }

    public void changeSettings(IImageModel model, boolean testTransparent) {
        testTransparent = testTransparent || bl(); // probability 75%

        if (!(model instanceof BurgerMenuModel)) {
            double pad = Math.min(model.getSize().height/3, model.getSize().width/3);
            model.setPadding(new BoundDouble(-pad/4 + r((int)pad)));
        }

        Color bkClr = Color.RandomColor();
        if (testTransparent)
            bkClr = bkClr.updateA(50 + r(10));


        if (model instanceof LogoModel) {
            var m = (LogoModel)model;
            m.setBorderColor(Color.RandomColor());
            m.setBorderWidth(r(4));
            m.setUseGradient(bl());
        } else
        if (model instanceof BurgerMenuModel) {
            var m = (BurgerMenuModel)model;
            if (bl())
                m.setLayers(4);
            m.setHorizontal(bl());
        } else
        if (model instanceof MosaicGroupModel) {
            MosaicGroupModel m = (MosaicGroupModel)model;
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
        if (model instanceof MosaicSkillModel) {
            MosaicSkillModel m = (MosaicSkillModel)model;
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
        if (model instanceof MosaicModel) {
            MosaicModel m = (MosaicModel)model;
            m.setSizeField(new Matrisize(3+r(2), 3 + r(2)));

            if (model instanceof MosaicImageModel) {
                MosaicImageModel mim = (MosaicImageModel)m;
                mim.setBackgroundColor(bkClr);

//                mim.setMosaicType(en(EMosaic.class));
                mim.setFillMode(1 + r(mim.getMaxCellFillMode()));

                mim.getPenBorder().setWidth(1. + r(2));
                SizeDouble size = mim.getSize();
                double padLeftRight = r((int)(size.width /3));
                double padTopBottom = r((int)(size.height/3));
                mim.setPadding(new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom));

                mim.setRotateMode(en(MosaicImageModel.ERotateMode.class));
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
        public Function<IImageController<?,?> /* imageControllers */, CellTilingInfo> itemCallback;
    }

    public CellTilingResult2 cellTiling(RectDouble rc, List<IImageController<?,?>> images, boolean tileIntersection) {
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

        Function<IImageController<?,?>, CellTilingInfo> itemCallback = item -> {
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

    public String getTitle(List<IImageController<?,?>> images) {
        return titlePrefix + " test paints: " + images.stream()
            .map(i -> i.getClass().getName())
            .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replace("$", ".") )
            .collect(Collectors.groupingBy(z -> z))
            .entrySet().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" & "));
    }

}
