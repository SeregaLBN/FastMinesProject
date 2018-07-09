package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model of representable menu as horizontal or vertical lines */
public final class BurgerMenuModel implements IImageModel {

   private AnimatedImageModel _generalModel;
   private boolean _show = true;
   private boolean _horizontal = true;
   private int     _layers = 3;
   private BoundDouble _padding;
   private PropertyChangeListener _generalModelListener;
   private NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

   /**
    * @param generalModel another basic model
    */
   protected BurgerMenuModel(AnimatedImageModel generalModel) {
      _generalModel = generalModel;
      _generalModelListener = event -> {
         assert event.getSource() == _generalModel; // by reference
         if (IImageModel.PROPERTY_SIZE.equals(event.getPropertyName()))
            recalcPadding((SizeDouble)event.getOldValue());
      };
      _generalModel.addListener(_generalModelListener);
   }

   public static final String PROPERTY_SHOW       = "Show";
   public static final String PROPERTY_HORIZONTAL = "Horizontal";
   public static final String PROPERTY_LAYERS     = "Layers";
   public static final String PROPERTY_PADDING    = "Padding";

   /** image width and height in pixel */
   @Override
   public SizeDouble getSize() { return _generalModel.getSize(); }
   @Override
   public void setSize(SizeDouble size) { _generalModel.setSize(size); }

   public boolean isShow() { return _show; }
   public void   setShow(boolean value) { _notifier.setProperty(_show, value, PROPERTY_SHOW); }

   public boolean isHorizontal() { return _horizontal; }
   public void   setHorizontal(boolean value) { _notifier.setProperty(_horizontal, value, PROPERTY_HORIZONTAL); }

   public int  getLayers() { return _layers; }
   public void setLayers(int value) { _notifier.setProperty(_layers, value, PROPERTY_LAYERS); }

   /** inside padding */
   public BoundDouble getPadding() {
      if (_padding == null)
         recalcPadding(null);
      return _padding;
   }
   public void setPadding(BoundDouble value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      BoundDouble paddingNew = new BoundDouble(value.left, value.top, value.right, value.bottom);
      _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }
   private void recalcPadding(SizeDouble old) {
      SizeDouble size = getSize();
      BoundDouble paddingNew = (_padding == null)
            ? new BoundDouble(size.width / 2,
                              size.height / 2,
                              _generalModel.getPadding().right,
                              _generalModel.getPadding().bottom)
            : AnimatedImageModel.recalcPadding(_padding, size, old);
      _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }

   public static class LineInfo {
      public Color clr;
      public double penWidht;
      public PointDouble from; // start coord
      public PointDouble to;   // end   coord
   }

   /** get paint information of drawing burger menu model image */
   public Stream<LineInfo> getCoords() {
      if (!isShow())
         return Stream.empty();

      boolean horizontal = isHorizontal();
      int layers = getLayers();
      BoundDouble pad = getPadding();
      RectDouble rc = new RectDouble(pad.left,
                                     pad.top,
                                     getSize().width  - pad.getLeftAndRight(),
                                     getSize().height - pad.getTopAndBottom());
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
      double rotateAngle = _generalModel.getRotateAngle();
      double stepAngle = 360.0 / layers;

      return IntStream.range(0, layers)
         .mapToObj(layerNum -> {
            double layerAlignmentAngle = AnimatedImageModel.fixAngle(layerNum*stepAngle + rotateAngle);
            double offsetTop  = !horizontal ? 0 : layerAlignmentAngle*rc.height/360;
            double offsetLeft =  horizontal ? 0 : layerAlignmentAngle*rc.width /360;
            PointDouble start = new PointDouble(rc.left() + offsetLeft,
                                                rc.top()  + offsetTop);
            PointDouble end   = new PointDouble((horizontal ? rc.right() : rc.left()) + offsetLeft,
                                                (horizontal ? rc.top() : rc.bottom()) + offsetTop);

            HSV hsv = new HSV(Color.Gray());
            hsv.v *= Math.sin(layerNum*stepAngle / layers);

            LineInfo li = new LineInfo();
            li.clr = hsv.toColor();
            li.penWidht = penWidth;
            li.from = start;
            li.to = end;
            return li;
         });
   }

   @Override
   public void close() {
      _generalModel.removeListener(_generalModelListener);
      _notifier.close();
      _generalModelListener = null;
      _generalModel = null;
   }

   @Override
   public void addListener(PropertyChangeListener listener) {
      _notifier.addListener(listener);
   }
   @Override
   public void removeListener(PropertyChangeListener listener) {
      _notifier.removeListener(listener);
   }

}
