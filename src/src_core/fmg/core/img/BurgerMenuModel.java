package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.*;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model of representable menu as horizontal or vertical lines */
public class BurgerMenuModel implements IImageModel {

   protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

   /**
    * @param generalModel another basic model
    */
   protected BurgerMenuModel(ImageModel generalModel) {
      _show = true;
      _layers = 3;
      _horizontal = true;
      _rotate = true;

      _generalModel = generalModel;
      _generalModelListener = event -> {
         assert event.getSource() == _generalModel; // by reference
         if (ImageModel.PROPERTY_SIZE.equals(event.getPropertyName()))
            recalcPadding((Size)event.getOldValue());
      };
      _generalModel.addListener(_generalModelListener);
   }

   private ImageModel _generalModel;
   private PropertyChangeListener _generalModelListener;


   public static final String PROPERTY_SHOW       = "Show";
   public static final String PROPERTY_HORIZONTAL = "Horizontal";
   public static final String PROPERTY_LAYERS     = "Layers";
   public static final String PROPERTY_ROTATE     = "Rotate";
   public static final String PROPERTY_PADDING    = "Padding";

   /** image width and height in pixel */
   @Override
   public Size getSize() { return _generalModel.getSize(); }
   @Override
   public void setSize(Size size) { _generalModel.setSize(size); }

   private boolean _show;
   public boolean isShow() { return _show; }
   public void   setShow(boolean value) { _notifier.setProperty(_show, value, PROPERTY_SHOW); }

   private boolean _horizontal = true;
   public boolean isHorizontal() { return _horizontal; }
   public void   setHorizontal(boolean value) { _notifier.setProperty(_horizontal, value, PROPERTY_HORIZONTAL); }

   private int   _layers = 3;
   public int  getLayers() { return _layers; }
   public void setLayers(int value) { _notifier.setProperty(_layers, value, PROPERTY_LAYERS); }

   private boolean _rotate;
   public boolean isRotate() { return _rotate; }
   public void   setRotate(boolean value) { _notifier.setProperty(_rotate, value, PROPERTY_ROTATE); }

   private BoundDouble _padding;
   /** inside padding */
   public Bound getPadding() {
      if (_padding == null)
         recalcPadding(null);
      return new Bound((int)_padding.left, (int)_padding.top, (int)_padding.right, (int)_padding.bottom);
   }
   public void setPadding(Bound value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      BoundDouble paddingNew = new BoundDouble(value.left, value.top, value.right, value.bottom);
      _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
   }
   private void recalcPadding(Size old) {
      Size size = getSize();
      BoundDouble paddingNew = (_padding == null)
            ? new BoundDouble(size.width / 2,
                              size.height / 2,
                              _generalModel.getPadding().right,
                              _generalModel.getPadding().bottom)
            : ImageModel.recalcPadding(_padding, size, old);
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
      Bound pad = getPadding();
      Rect rc = new Rect(pad.left,
                         pad.top,
                         getSize().width  - pad.getLeftAndRight(),
                         getSize().height - pad.getTopAndBottom());
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
      double rotateAngle = isRotate() ? _generalModel.getRotateAngle() : 0;
      double stepAngle = 360.0 / layers;

      return IntStream.range(0, layers)
         .mapToObj(layerNum -> {
            double layerAlignmentAngle = ImageModel.fixAngle(layerNum*stepAngle + rotateAngle);
            double offsetTop  = !horizontal ? 0 : layerAlignmentAngle*rc.height/360;
            double offsetLeft =  horizontal ? 0 : layerAlignmentAngle*rc.width /360;
            PointDouble start = new PointDouble(rc.left() + offsetLeft,
                                                rc.top()  + offsetTop);
            PointDouble end   = new PointDouble((horizontal ? rc.right() : rc.left()) + offsetLeft,
                                                (horizontal ? rc.top() : rc.bottom()) + offsetTop);

            HSV hsv = new HSV(Color.Gray);
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
