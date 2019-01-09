using System;
using System.Collections.Generic;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;

namespace fmg.core.img {

    public abstract class ATestDrawing {

        public Random GetRandom => ThreadLocalRandom.Current;
        public int R(int max) { return GetRandom.Next(max); }
        public bool Bl => GetRandom.Next(2) == 1; // random bool
        public int NP => Bl ? -1 : +1; // negative or positive

        private readonly string titlePrefix;

        protected ATestDrawing(string titlePrefix) {
            this.titlePrefix = titlePrefix;
        }

        public void ApplySettings<TImage,
                                  TMosaicImageInner,
                                  TImageView,
                                  TAImageView, 
                                  TImageModel,
                                  TAnimatedModel>
                        (IImageController<TImage, TImageView, TImageModel> ctrller, bool testTransparent)
            where TImage : class
            where TMosaicImageInner : class
            where TImageView : IImageView<TImage, TImageModel>
            where TAImageView : IImageView<TImage, TAnimatedModel>
            where TImageModel : IImageModel
            where TAnimatedModel : IAnimatedModel
        {

            ///////////////////////
            //                   //
            //  manual settings  //
            //                   //
            ///////////////////////
            IImageModel model = ctrller.Model;
            #region manual settings
            {
                //model.Size = new SizeDouble(600, 600);
                model.Padding = new BoundDouble(10);

                if (model is IAnimatedModel am) {
                    am.Animated = true;
                    if (am.Animated) {
                        am.AnimatePeriod = 2000; // rotate period
                        am.TotalFrames = 100; // animate iterations
                    }
                }
                if (model is AnimatedImageModel aim) {
                    aim.BorderWidth = 0;
                    aim.BackgroundColor = testTransparent ? new Color(0xC8FFFFFF) : Color.White;
                    aim.ForegroundColor = new Color(aim.ForegroundColor).UpdateA(200); // 0..255 - foreground alpha-chanel color
                }
                if (model is LogoModel lm) {
                    lm.UseGradient = true;
                }

                if (ctrller is AnimatedImgController<TImage, TAImageView, TAnimatedModel> aic) {
                    aic.UseRotateTransforming(true);
                    aic.UsePolarLightFgTransforming(true);
                }
            }
            #endregion

            bool useRandom = true;
            ///////////////////////
            //                   //
            //  random settings  //
            //                   //
            ///////////////////////
            if (!useRandom)
                return;
            #region random settings
            {
                testTransparent = testTransparent || Bl;

                if (model is IAnimatedModel am) {
                    am.Animated = Bl || Bl;
                    if (am.Animated) {
                        am.AnimatePeriod = 1000 + R(2000);
                        am.TotalFrames = 40 + R(20);
                    }
                }
                if (ctrller is AnimatedImgController<TImage, TAImageView, TAnimatedModel> aCtrller) {
                    //if (IsSubClassOfGeneric(typeof(AnimatedImgController<>), ctrller.GetType())) {
                    if (aCtrller.Model.Animated) {
                        aCtrller.UseRotateTransforming(Bl);
                        aCtrller.UsePolarLightFgTransforming(Bl);
                        if (Bl)
                            aCtrller.AddModelTransformer(new PolarLightBkTransformer());
                    }
                }


                Color bkClr = Color.RandomColor();
                if (testTransparent)
                    bkClr = bkClr.UpdateA((byte)(50 + R(10)));

                if (model is AnimatedImageModel aim) {
                    aim.BorderWidth = R(3);

                    double pad = Math.Min(aim.Size.Height / 3, aim.Size.Width / 3);
                    model.Padding = new BoundDouble(-pad / 4 + R((int)pad));

                    aim.BackgroundColor = bkClr;

                    aim.ForegroundColor = Color.RandomColor();//.brighter()
                    if (testTransparent) {
                        // test transparent
                        Color clr = aim.ForegroundColor;
                        if ((aim.BorderWidth > 0) && (R(4) == 0)) {
                            clr = clr.UpdateA(Color.Transparent.A);
                        } else {
                            clr = clr.UpdateA((byte)(150 + R(255 - 150)));
                        }
                        aim.ForegroundColor = clr;
                    }

                    aim.PolarLights = Bl;
                    aim.AnimeDirection = Bl;

                    if (model is LogoModel lm) {
                        lm.UseGradient = Bl;
                    }
                }
                if (model is MosaicGameModel mgm) {
                    mgm.SizeField = new Matrisize(3 + R(2), 3 + R(2));

                    if (model is MosaicDrawModel<TMosaicImageInner> mdm) {
                        mdm.BackgroundColor = bkClr;

                        mdm.BkFill.Mode = 1 + R(mdm.CellAttr.GetMaxBackgroundFillModeValue());

                        mdm.PenBorder.Width = 1 + R(2);
                        SizeDouble size = mdm.Size;
                        double padLeftRight = R((int)(size.Width / 3));
                        double padTopBottom = R((int)(size.Height / 3));
                        mdm.Padding = new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom);

                        if (model is MosaicAnimatedModel<TMosaicImageInner> mam) {
                            Type clazzERotateMode = typeof(MosaicAnimatedModel<TMosaicImageInner>.ERotateMode);
                            var arr = Enum.GetValues(clazzERotateMode);
                            var val = arr.GetValue(R(arr.Length));
                            mam.RotateMode = (MosaicAnimatedModel<TMosaicImageInner>.ERotateMode)val;
                        }
                    }
                }
            }
            #endregion
        }

        public class CellTilingInfo {
            public int i; // index of column
            public int j; // index of row
            public PointDouble imageOffset;
        }

        public class CellTilingResult<TImage, TImageController, TImageView, TImageModel>
            where TImage : class
            where TImageController : IImageController<TImage, TImageView, TImageModel>
            where TImageView : IImageView<TImage, TImageModel>
            where TImageModel : IImageModel
        {
            public SizeDouble imageSize;
            public Size tableSize;
            public Func<TImageController /* image */, CellTilingInfo> itemCallback;
        }

        public CellTilingResult<TImage, TImageController, TImageView, TImageModel> CellTiling<TImage, TImageController, TImageView, TImageModel>(RectDouble rc, IList<TImageController> images, bool testTransparent)
            where TImage : class
            where TImageController : IImageController<TImage, TImageView, TImageModel>
            where TImageView : IImageView<TImage, TImageModel>
            where TImageModel : IImageModel
        {
            int len = images.Count;

            // max tiles in one column
            int mtoc(int colsTotal) {
                return (int)Math.Ceiling(len / (double)colsTotal);
            }

            // для предполагаемого кол-ва рядков нахожу макс кол-во плиток в строке
            // и возвращаю отношение меньшей стороны к большей
            double f(int colsTotal) {
                int mCnt = mtoc(colsTotal);
                double tailW = rc.Width / colsTotal;
                double tailH = rc.Height / mCnt;
                return (tailW < tailH)
                      ? tailW / tailH
                      : tailH / tailW;
            }

            int colsOpt = 0;
            {
                double xToY = 0; // отношение меньшей стороны к большей
                                 // ищу оптимальное кол-во рядков для расположения плиток. Оптимальным считаю такое расположение,
                                 // при котором плитки будут наибольше похожими на квадрат (т.е. отношение меньшей стороны к большей будет максимальней)
                for (int i = 1; i <= len; ++i) {
                    double xy = f(i);
                    if (xy < xToY)
                        break;
                    colsOpt = i;
                    xToY = xy;
                }
            }

            int cols = colsOpt;
            int rows = (int)Math.Ceiling(len / (double)cols);
            double dx = rc.Width / cols; // cell tile width
            double dy = rc.Height / rows; // cell tile height

            int pad = 2; // cell padding
            double addonX = (cols == 1) ? 0 : !testTransparent ? 0 : dx / 4; // test intersection
            double addonY = (rows == 1) ? 0 : !testTransparent ? 0 : dy / 4; // test intersection
            var imgSize = new SizeDouble(dx - 2 * pad + addonX,  // dx - 2*pad;
                                         dy - 2 * pad + addonY); // dy - 2*pad;

            CellTilingInfo itemCallback(TImageController item) {
                int pos = images.IndexOf(item);
                if (pos == -1)
                    throw new Exception("Illegal usage...");

                int i = pos % cols;
                int j = pos / cols;
                var offset = new PointDouble(rc.X + i * dx + pad,
                                             rc.Y + j * dy + pad);
                if (i == (cols - 1))
                    offset.X -= addonX;
                if (j == (rows - 1))
                    offset.Y -= addonY;

                return new CellTilingInfo {
                    i = i,
                    j = j,
                    imageOffset = offset
                };
            }

            return new CellTilingResult<TImage, TImageController, TImageView, TImageModel> {
                imageSize = imgSize,
                tableSize = new Size(cols, rows),
                itemCallback = itemCallback
            };
        }

        public string GetTitle<TImage, TImageController, TImageView, TImageModel>(List<TImageController> images)
            where TImage : class
            where TImageController : IImageController<TImage, TImageView, TImageModel>
            where TImageView : IImageView<TImage, TImageModel>
            where TImageModel : IImageModel
        {
            string friendlyName(Type type) {
                var all = type.FullName.Split('.');
                return string.Join(".", all.Skip(all.Length - 2)
                   .Select(s => s.Replace('+', '.')));
            }

            return titlePrefix + " test paints: " + string.Join(" & ", images
               .Select(i => friendlyName(i.GetType()))
               .GroupBy(x => x)
               .Select(x => x.First()));
        }

    }

}
