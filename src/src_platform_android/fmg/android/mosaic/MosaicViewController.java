package fmg.android.mosaic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;
import java.sql.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fmg.android.app.DrawableView;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.common.Logger;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.ClickResult;
import fmg.core.types.EGameStatus;
import fmg.core.types.EState;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/** MVC: controller. Android implementation */
public class MosaicViewController extends MosaicController<DrawableView, Bitmap, MosaicViewView, MosaicDrawModel<Bitmap>> {

    public static final String PROPERTY_EXTENDED_MANIPULATION = "ExtendedManipulation";

    private Context context;
    private final ClickInfo _clickInfo = new ClickInfo();
    /** <li>true : bind Control.SizeProperty to Model.Size
     *  <li>false: bind Model.Size to Control.SizeProperty */
    private boolean bindSizeDirection = true;
    private boolean _extendedManipulation = false;

    // #region if ExtendedManipulation
    /// <summary> мин отступ от краев экрана для мозаики </summary>
    private final double minIndent = Cast.dpToPx(30.0f);
    private final boolean DeferredZoom = true;
    private boolean _manipulationStarted;
    private boolean _turnX;
    private boolean _turnY;
    private long _dtInertiaStarting;
    private static Double _baseWheelDelta;
    private Object/*IDisposable*/ _areaScaleObservable;
    private Object/*Transform*/ _originalTransform;
    private Object/*CompositeTransform*/ _scaleTransform;
    private double _deferredArea;

    private PointDouble lastScrollPosition;

    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedControlSize = new Size(-1, -1);

    private GestureDetector _gd;
    private final GestureDetector.OnGestureListener _gestureListener = new GestureDetector.SimpleOnGestureListener() {

        /// interface OnGestureListener
        @Override
        public boolean onDown(MotionEvent ev) {
            return MosaicViewController.this.onGestureDown(ev);
        }

        @Override
        public void onShowPress(MotionEvent ev) {
            MosaicViewController.this.onGestureShowPress(ev);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            return MosaicViewController.this.onGestureSingleTapUp(ev);
        }

        @Override
        public boolean onScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
            if (!getExtendedManipulation())
                return false;

            return MosaicViewController.this.onGestureScroll(ev1, ev2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            MosaicViewController.this.onGestureLongPress(ev);
        }

        @Override
        public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
            if (!getExtendedManipulation())
                return false;

            return MosaicViewController.this.onGestureFling(ev1, ev2, velocityX, velocityY);
        }

        /// interface OnDoubleTapListener

        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            return MosaicViewController.this.onGestureSingleTapConfirmed(ev);
        }

        @Override
        public boolean onDoubleTap(MotionEvent ev) {
            return MosaicViewController.this.onGestureDoubleTap(ev);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent ev) {
            return MosaicViewController.this.onGestureDoubleTapEvent(ev);
        }

        // interface OnContextClickListener
        @Override
        public boolean onContextClick(MotionEvent ev) {
            return MosaicViewController.this.onGestureContextClick(ev);
        }
    };


    private static class ZoomStartInfo {
        public Point _devicePosition;
        public SizeDouble _mosaicSize;
        public SizeDouble _mosaicOffset;
    }
    private ZoomStartInfo _zoomStartInfo;
    // #endregion

    public MosaicViewController(Context context) {
        super(new MosaicViewView(context));
        this.context = context;
        _gd = new GestureDetector(context, _gestureListener);
        subscribeToViewControl();
    }

    public void setViewControl(DrawableView view) {
        unsubscribeToViewControl();

        getView().setControl(view);
        context = (view==null) ? null : view.getContext();
        _gd = (context==null) ? null : new GestureDetector(context, _gestureListener);

        subscribeToViewControl();
    }

    public View getViewControl() {
        return getView().getControl();
    }

    public boolean getBindSizeDirection() {
        return bindSizeDirection;
    }
    public void setBindSizeDirection(boolean bindSizeDirection) {
        this.bindSizeDirection = bindSizeDirection;
    }

    public boolean getExtendedManipulation() {
        return _extendedManipulation;
    }
    public void setExtendedManipulation(boolean value) {
        if (_notifier.setProperty(_extendedManipulation, value, PROPERTY_EXTENDED_MANIPULATION)) {
            unsubscribeToViewControl();
            subscribeToViewControl();
        }
    }

    private void subscribeToViewControl() {
        View control = getViewControl();
        if (control == null)
            return;

        { // onControlSizeChanged(newSize);
            subjSizeChanged = PublishSubject.create();
            sizeChangedObservable = subjSizeChanged.debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
//                        Logger.info("  MosaicViewController::onGlobalLayoutListener: Debounce: onNext: ev=" + ev);
                        UiInvoker.DEFERRED.accept(() -> onControlSizeChanged(ev));
                    }, ex -> {
                        Logger.info("  MosaicViewController: sizeChangedObservable: Debounce: onError: " + ex);
                    });
            control.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);
        }

        control.setFocusable(true); // ? иначе не будет срабатывать FocusListener
        control.setOnFocusChangeListener((v, hasFocus) -> onFocusChange(hasFocus));
        control.setOnTouchListener((v, ev) -> onTouch(ev));

//        control.setOnClickListener(v -> onClick());
//        control.setOnLongClickListener(v -> onLongClick());
//        control.setOnDragListener((v, ev) -> onDrag(ev));
//        control.setOnHoverListener((v, ev) -> onHover(ev));
//        control.setOnCapturedPointerListener();

        if (!getExtendedManipulation())
            return;

        control.setOnScrollChangeListener((v, _1, _2, _3, _4) -> onScrollChange(_1, _2, _3, _4));
        control.setOnGenericMotionListener((v, ev) -> onGenericMotion(ev));
        control.setOnContextClickListener(v -> onContextClick());
    }

    private void unsubscribeToViewControl() {
        View control = getViewControl();
        if (control == null)
            return;

        control.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        sizeChangedObservable.dispose();
        subjSizeChanged = null;

        control.setFocusable(false);
        control.setOnFocusChangeListener(null);
        control.setOnTouchListener(null);

//        control.setOnClickListener(null);
//        control.setOnLongClickListener(null);
//        control.setOnDragListener(null);
//        control.setOnHoverListener(null);
//        control.setOnCapturedPointerListener(null);

        if (!getExtendedManipulation())
            return;

        control.setOnScrollChangeListener(null);
        control.setOnGenericMotionListener(null);
        control.setOnContextClickListener(null);
    }

    private SizeDouble getOffset() {
        return getModel().getMosaicOffset();
    }
    private void setOffset(SizeDouble offset) {
        getModel().setMosaicOffset(recheckOffset(offset));
    }

    private SizeDouble recheckOffset(SizeDouble offset) {
        SizeDouble size = getModel().getSize();
        SizeDouble mosaicSize = getModel().getMosaicSize();
        if ((offset.width + mosaicSize.width) < minIndent) { // правый край мозаики пересёк левую сторону контрола?
            offset.width = minIndent - mosaicSize.width; // привязываю к левой стороне контрола
        } else {
            if (offset.width > (size.width - minIndent)) // левый край мозаики пересёк правую сторону контрола?
                offset.width = size.width - minIndent; // привязываю к правой стороне контрола
        }

        if ((offset.height + mosaicSize.height) < minIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
            offset.height = minIndent - mosaicSize.height; // привязываю к верхней стороне контрола
        } else {
            if (offset.height > (size.height - minIndent)) // вержний край мозаики пересёк нижнюю сторону контрола?
                offset.height = size.height - minIndent; // привязываю к нижней стороне контрола
        }

        return offset;
    }

    private void onGlobalLayoutListener() {
        View control = getViewControl();
        if (control == null)
            return;

        int w = control.getWidth();
        int h = control.getHeight();
        if ((w <= 0) || (h <= 0))
            return;

        Size newSize = new Size(w, h);

        if (cachedControlSize.equals(newSize))
            return;
        cachedControlSize = newSize;
        subjSizeChanged.onNext(newSize);
    }
/*

        /// <summary> узнаю мах размер площади ячеек мозаики (для размера поля 3x3) так, чтобы поле влазило в текущий размер Control'а </summary>
        /// <returns>макс площадь ячейки</returns>
        private double CalcMaxArea() {
            if (_maxArea.HasValue)
                return _maxArea.Value;
            var mosaicSizeField = new Matrisize(3, 3);
            var size = Model.Size;
            double area = MosaicHelper.FindAreaBySize(this.MosaicType, mosaicSizeField, ref size);
            //System.Diagnostics.Debug.WriteLine("MosaicFrameworkElementController.CalcMaxArea: area="+area);
            _maxArea = area; // caching value
            return area;
        }
        double? _maxArea; // cached value

        private void BeforeZoom(Windows.Foundation.Point? mouseDevicePosition) {
            if (mouseDevicePosition.HasValue) {
                if (_zoomStartInfo == null)
                    _zoomStartInfo = new ZoomStartInfo();
                _zoomStartInfo._devicePosition = mouseDevicePosition.Value;
                _zoomStartInfo._mosaicOffset = Offset;
                _zoomStartInfo._mosaicSize = Model.MosaicSize;
            } else {
                _zoomStartInfo = null;
            }
        }

        private void AfterZoom() {
            if (_zoomStartInfo == null)
                return;

            var devicePos = _zoomStartInfo._devicePosition;
            var mosaicSizeNew = Model.MosaicSize;//GetMosaicSize(Model.SizeField, Model.Area);
            var offsetNew = new SizeDouble(
                        devicePos.X - (devicePos.X - _zoomStartInfo._mosaicOffset.Width ) * mosaicSizeNew.Width  / _zoomStartInfo._mosaicSize.Width,
                        devicePos.Y - (devicePos.Y - _zoomStartInfo._mosaicOffset.Height) * mosaicSizeNew.Height / _zoomStartInfo._mosaicSize.Height);
            Offset = offsetNew;
        }

        /// <summary> Zoom + </summary>
        void ZoomInc(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            BeforeZoom(mouseDevicePosition);
            if (DeferredZoom) {
                Scale(1.01 * zoomPower);
            } else {
                Model.Area *= 1.01 * zoomPower;
                AfterZoom();
            }
        }

        /// <summary> Zoom - </summary>
        void ZoomDec(double zoomPower = 1.3, Windows.Foundation.Point? mouseDevicePosition = null) {
            BeforeZoom(mouseDevicePosition);
            if (DeferredZoom) {
                Scale(0.99 / zoomPower);
            } else {
                Model.Area *= 0.99 / zoomPower;
                AfterZoom();
            }
        }

        private void Scale(double scaleMul) {
            var ctrl = Control;
            if (!(ctrl.RenderTransform is CompositeTransform)) {
                if (_scaleTransform == null) {
                    _scaleTransform = new CompositeTransform();
                    _originalTransform = ctrl.RenderTransform;
                }
                ctrl.RenderTransform = _scaleTransform;
                _deferredArea = Model.Area;
            }


            _deferredArea *= scaleMul;
            _deferredArea = Math.Min(Math.Max(MosaicInitData.AREA_MINIMUM, _deferredArea), CalcMaxArea()); // recheck

            var deferredMosaicSize = GetMosaicSize(Model.SizeField, _deferredArea);
            var currentMosaicSize = Model.MosaicSize;

            if (_zoomStartInfo != null) {
                var p = _zoomStartInfo._devicePosition;
                _scaleTransform.CenterX = p.X;
                _scaleTransform.CenterY = p.Y;
            }

            _scaleTransform.ScaleX = deferredMosaicSize.Width / currentMosaicSize.Width;
            _scaleTransform.ScaleY = deferredMosaicSize.Height / currentMosaicSize.Height;

            NeedAreaChanging(this, null); // fire event
        }

        private void OnDeferredAreaChanging(object sender, NeedAreaChangingEventArgs ev) {
            Model.Area = _deferredArea;
            AfterZoom();

            // restore
            _scaleTransform.CenterX = _scaleTransform.CenterY = 0;
            _scaleTransform.ScaleX = _scaleTransform.ScaleY = 1;
            Control.RenderTransform = _originalTransform;
        }

        /// <summary> Zoom minimum </summary>
        private void ZoomMin() {
            Model.Area = MosaicInitData.AREA_MINIMUM;
        }

        /// <summary> Zoom maximum </summary>
        private void ZoomMax() {
            var maxArea = CalcMaxArea();
            Model.Area = maxArea;
        }
*/
    @Override
    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        super.onModelPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicDrawModel.PROPERTY_SIZE:
            onSizeChanged(ev);
            break;
        case MosaicDrawModel.PROPERTY_AREA:
//            onAreaChanged(ev as PropertyChangedExEventArgs<double>);
            break;
        }
    }


    private void onControlSizeChanged(Size newSize) {
        if (!bindSizeDirection)
            getModel().setSize(Cast.toSizeDouble(newSize));
    }

    private void onSizeChanged(PropertyChangeEvent ev) {
        if (!bindSizeDirection)
            return;
        View control = getViewControl();
        ViewGroup.LayoutParams lp = control.getLayoutParams();
        if (lp == null)
            return;

        SizeDouble newSize = (SizeDouble)ev.getNewValue();
        if (newSize == null)
            newSize = getModel().getSize();
        lp.width  = (int)newSize.width;
        lp.height = (int)newSize.height;
    }

    /*
    private void OnAreaChanged(PropertyChangedExEventArgs<double> ev) {
        if (!ExtendedManipulation)
            return;
        using (var tracer = CreateTracer(GetCallerName(), string.Format("newArea={0:0.00}, oldValue={1:0.00}", ev.NewValue, ev.OldValue))) {
            Offset = Offset; // implicit call RecheckOffset
        }
    }
    */

//    #region control handlers

    static String dragEventToString(DragEvent ev) {
        switch (ev.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED : return "Started";
        case DragEvent.ACTION_DRAG_ENTERED : return "Entered";
        case DragEvent.ACTION_DRAG_LOCATION: return "Location";
        case DragEvent.ACTION_DROP         : return "Drop";
        case DragEvent.ACTION_DRAG_EXITED  : return "Exited";
        case DragEvent.ACTION_DRAG_ENDED   : return "Ended";
        }
        return "???";
    }

    static String motionEventActionToString(int eventAction) {
        switch (eventAction) {
        case MotionEvent.ACTION_CANCEL      : return "Cancel";
        case MotionEvent.ACTION_DOWN        : return "Down";
        case MotionEvent.ACTION_MOVE        : return "Move";
        case MotionEvent.ACTION_OUTSIDE     : return "Outside";
        case MotionEvent.ACTION_UP          : return "Up";
        case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
        case MotionEvent.ACTION_POINTER_UP  : return "Pointer Up";
        }
        return "???";
    }

    static String motionEventToString(MotionEvent ev) {
        /** /
        switch (ev.getAction()) {
        case MotionEvent.ACTION_CANCEL      : return "Cancel";
        case MotionEvent.ACTION_DOWN        : return "Down";
        case MotionEvent.ACTION_MOVE        : return "Move";
        case MotionEvent.ACTION_OUTSIDE     : return "Outside";
        case MotionEvent.ACTION_UP          : return "Up";
        case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
        case MotionEvent.ACTION_POINTER_UP  : return "Pointer Up";
        }
        return "???";
        /**/
        return ev.toString();
    }

    boolean clickHandler(ClickResult clickResult) {
        if (clickResult == null)
            return false;
        _clickInfo.cellDown = clickResult.getCellDown();
        _clickInfo.isLeft = clickResult.isLeft();
        boolean handled = clickResult.isAnyChanges();
        if (clickResult.isDown())
            _clickInfo.downHandled = handled;
        else
            _clickInfo.upHandled = handled;
        _clickInfo.released = !clickResult.isDown();
        Logger.info(">>>>>>> _clickInfo=" + _clickInfo + "\n" + Arrays.asList(Thread.currentThread().getStackTrace()).stream().map(x -> x.toString()).filter(x -> x.startsWith("fmg.")).collect(Collectors.joining("\n")));
        return handled;
    }

    boolean onClickLost() {
        return clickHandler(this.mouseFocusLost());
    }

    boolean onClickCommon(MotionEvent ev, boolean leftClick, boolean down) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onClickCommon", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            PointDouble point = new PointDouble(ev.getX(), ev.getY());
            handled[0] = clickHandler(down
                    ? mousePressed(point, leftClick)
                    : mouseReleased(point, leftClick));
            return handled[0];
        }
    }


    protected boolean onGenericMotion(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGenericMotion", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onTouch(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onTouch", "ev=" + motionEventToString(ev), () -> "handled="+handled[0] + "\n-----------------------------")) {
            handled[0] = _gd.onTouchEvent(ev);

            if ((ev.getAction() == MotionEvent.ACTION_UP) && !_clickInfo.released)
                handled[0] = onClickCommon(ev, true, false);

            //return handled[0];
        }

        return true; // !! always return true
    }

    ///////////////// begin Gesture
    protected boolean onGestureDown(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDown", "ev=" + motionEventToString(ev), () -> "handled=" + handled[0])) {
            _turnX = _turnY = false;
            _dtInertiaStarting = 0;
            lastScrollPosition = null;

            if (!_clickInfo.isDoubleTap)
                return handled[0] = onClickCommon(ev, true, true);

            return handled[0];
        }
    }
    protected void onGestureShowPress(MotionEvent ev) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureShowPress", "ev=" + motionEventToString(ev)))
        {
        }
    }
    protected boolean onGestureSingleTapUp(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureSingleTapUp", "ev=" + motionEventToString(ev), () -> "handled=" + handled[0])) {
            return handled[0] = onClickCommon(ev, true, false);
        }
    }

    protected boolean onGestureScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureScroll", "ev1=" + motionEventToString(ev1) + "; ev2=" + motionEventToString(ev2) + "; distX=" + distanceX + "; distY=" + distanceY, () -> "handled=" + handled[0]))
        {
            PointDouble evPrevPosition = lastScrollPosition;
            lastScrollPosition = new PointDouble(ev2.getX(), ev2.getY());
            if (evPrevPosition != null)
                onManipulationDelta(evPrevPosition, false, -distanceX, -distanceY);
            return handled[0] = true;
        }
    }

    protected void onGestureLongPress(MotionEvent ev) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureLongPress", "ev=" + motionEventToString(ev)))
        {
            if ((_clickInfo.cellDown != null) && (_clickInfo.cellDown.getState().getStatus() == EState._Close)) {
                // imitate right mouse click - to (un)set flag
                mouseReleased(null, true);
                onClickCommon(ev, false, true);
                onClickCommon(ev, false, false);
            }
        }
    }
    protected boolean onGestureFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
        PointDouble evCurrPosition = new PointDouble(ev2.getX(), ev2.getY());
        boolean[] handled = { !false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureFling", "ev1=" + motionEventToString(ev1) + "; ev2=" + motionEventToString(ev2) + "; velocityX=" + velocityX + "; velocityY=" + velocityY, () -> "handled=" + handled[0]))
        {
            _dtInertiaStarting = new Date().getTime();
            onManipulationDelta(evCurrPosition, true, -velocityX, -velocityY);
            return handled[0];
        } finally {
            AsyncRunner.Repeat(() -> onManipulationDelta(evCurrPosition, true, -velocityX, -velocityY), 20, () -> (_dtInertiaStarting == 0));
        }
    }
    /// interface OnDoubleTapListener
    protected boolean onGestureSingleTapConfirmed(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureSingleTapConfirmed", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onGestureDoubleTap(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDoubleTap", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            MosaicDrawModel<Bitmap> model = getModel();
            SizeDouble mosaicSize = model.getMosaicSize();
            SizeDouble offset = getOffset();
            RectDouble rcMosaic = new RectDouble(offset.width, offset.height, mosaicSize.width, mosaicSize.height);
            if (rcMosaic.contains(ev.getX(), ev.getY())) {
                if (this.getGameStatus() == EGameStatus.eGSEnd) {
                    this.gameNew();
                    handled[0] = true;
                }
            } else {
                _zoomStartInfo = null;

                // centered mosaic
                SizeDouble size = model.getSize();

                // 1. modify area
                model.setArea(MosaicHelper.findAreaBySize(model.getMosaicType(), model.getSizeField(), size, new SizeDouble()));

                // 2. modify offset
                mosaicSize = model.getMosaicSize(); // ! reload value
                offset.width  = (size.width  - mosaicSize.width ) / 2;
                offset.height = (size.height - mosaicSize.height) / 2;
                setOffset(offset);

                handled[0] = true;
            }
            return handled[0];
        }
    }
    protected boolean onGestureDoubleTapEvent(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDoubleTapEvent", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            _clickInfo.isDoubleTap = (ev.getAction() == MotionEvent.ACTION_DOWN);
            return handled[0];
        }
    }
    /// interface OnContextClickListener
    protected boolean onGestureContextClick(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureContextClick", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    ///////////////// end Gesture

    private void onManipulationDelta(PointDouble evCurrPosition, boolean isInertial, float deltaTransX, float deltaTransY) {
        //var deltaScale = delta.Scale;
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onManipulationDelta", "pos=" + evCurrPosition + "; isInertial=" + isInertial + "; deltaTransX=" + deltaTransX + "; deltaTransY=" + deltaTransY))
        {
//            if (Math.Abs(1 - deltaScale) > 0.009) {
//                #region scale / zoom
//                if (deltaScale > 0)
//                    ZoomInc(deltaScale, ev.Position);
//                else
//                    ZoomDec(2 + deltaScale, ev.Position);
//                #endregion
//            } else
            {
                // #region drag
                boolean needDrag = true;
                SizeDouble offset = getOffset();
                SizeDouble size = getModel().getSize();
                // #region check possibility dragging
                if (_clickInfo.cellDown != null) {
                    PointDouble startPoint = evCurrPosition;
                    //var inCellRegion = _tmpClickedCell.PointInRegion(startPoint.ToFmRect());
                    //this._contentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                    RectDouble rcOuter = _clickInfo.cellDown.getRcOuter().moveXY(offset);
                    float min = Cast.dpToPx(25);
                    rcOuter = rcOuter.moveXY(-min, -min);
                    rcOuter.width  += min * 2;
                    rcOuter.height += min * 2;
                    needDrag = !rcOuter.contains(startPoint);
                }
                //#endregion check possibility dragging

                if (needDrag) {
                    // #region Compound motion

                    if (_turnX)
                        deltaTransX *= -1;
                    if (_turnY)
                        deltaTransY *= -1;

                    if (isInertial) {
                        double totalSeconds = (new Date().getTime() - _dtInertiaStarting) / 1000.0; // TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - _dtInertiaStarting);
                        //double coefFading = Math.Max(0.05, 1 - 0.32 * totalSeconds);
                        double coefFading = Math.max(0, 1 - 0.32 * totalSeconds);
                        if (coefFading == 0)
                            _dtInertiaStarting = 0; // stop loop

                        deltaTransX *= coefFading;
                        deltaTransY *= coefFading;

                        int fullX = (int)(deltaTransX / size.width);
                        int fullY = (int)(deltaTransY / size.height);
                        if (fullX != 0) {
                            deltaTransX -=  fullX * size.width;
                            if ((fullX & 1) == 1)
                                _turnX = !_turnX;
                        }
                        if (fullY != 0) {
                            deltaTransY -=  fullY * size.height;
                            if ((fullY & 1) == 1)
                                _turnY = !_turnY;
                        }

                        tracer.put("inertial coeff fading = " + coefFading + "; deltaTrans={" + deltaTransX + ", " + deltaTransY + "}");
                    }

                    SizeDouble mosaicSize = getModel().getMosaicSize();
                    if ((offset.width + mosaicSize.width + deltaTransX) < minIndent) { // правый край мозаики пересёк левую сторону контрола?
                        if (isInertial) {
                            _turnX = !_turnX; // разворачиваю по оси X

                            double dx1 = mosaicSize.width + offset.width - minIndent;  // часть deltaTrans.X которая не вылезла за границу
                            double dx2 = deltaTransX +  dx1;                           // часть deltaTrans.X которая вылезла за границу
                            deltaTransX -= 2 * dx2;                                    // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.width = minIndent - mosaicSize.width - deltaTransX; // привязываю к левой стороне контрола
                        }
                    } else
                    if ((offset.width + deltaTransX) > (size.width - minIndent)) { // левый край мозаики пересёк правую сторону контрола?
                        if (isInertial) {
                            _turnX = !_turnX; // разворачиваю по оси X

                            double dx1 = size.width - offset.width - minIndent;    // часть deltaTrans.X которая не вылезла за границу
                            double dx2 = deltaTransX - dx1;                        // часть deltaTrans.X которая вылезла за границу
                            deltaTransX -= 2 * dx2;                                // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.width = size.width - minIndent - deltaTransX;   // привязываю к правой стороне контрола
                        }
                    }

                    if ((offset.height + mosaicSize.height + deltaTransY) < minIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
                        if (isInertial) {
                            _turnY = !_turnY; // разворачиваю по оси Y

                            double dy1 = mosaicSize.height + offset.height - minIndent;  // часть deltaTrans.Y которая не вылезла за границу
                            double dy2 = deltaTransY + dy1;                              // часть deltaTrans.Y которая вылезла за границу
                            deltaTransY -= 2 * dy2;                                      // та часть deltaTrans.Y которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.height = minIndent - mosaicSize.height - deltaTransY; // привязываю к верхней стороне контрола
                        }
                    } else
                    if ((offset.height + deltaTransY) > (size.height - minIndent)) { // вержний край мозаики пересёк нижнюю сторону контрола?
                        if (isInertial) {
                            _turnY = !_turnY; // разворачиваю по оси Y

                            double dy1 = size.height - offset.height - minIndent;    // часть deltaTrans.Y которая не вылезла за границу
                            double dy2 = deltaTransY - dy1;                          // часть deltaTrans.Y которая вылезла за границу
                            deltaTransY -= 2 * dy2;                                  // та часть deltaTrans.Y которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.height = size.height - minIndent - deltaTransY;   // привязываю к нижней стороне контрола
                        }
                    }
                    // #endregion Compound motion
                    offset.width  += deltaTransX;
                    offset.height += deltaTransY;
                    assert offset == recheckOffset(offset);
                    setOffset(offset);
                }
                //#endregion drag
                /**/
            }
        }
    }

    protected void onClick() {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onClick"))
        {
        }
    }
    protected boolean onLongClick() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onLongClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onDrag(DragEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onDrag", "ev=" + dragEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onHover(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onHover", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onContextClick() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onContextClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onScrollChange"))
        {
        }
    }

    public void onFocusChange(boolean hasFocus) {
        System.out.println("Mosaic.onFocusChange: hasFocus=" + hasFocus);
        if (!hasFocus)
            mouseFocusLost();
    }

//    #endregion control handlers

    @Override
    protected boolean checkNeedRestoreLastGame() {
        boolean[] selectedNo = { true };
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    selectedNo[0] = true;
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    selectedNo[0] = false;
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Restore last game?") // Question
               .setPositiveButton("Yes", dialogClickListener)
               .setNegativeButton("No", dialogClickListener)
               .show();
        return selectedNo[0];
    }

    @Override
    public void close() {
        _dtInertiaStarting = 0;
        unsubscribeToViewControl();
        super.close();
        getView().close();
    }


    class ClickInfo {
        boolean isDoubleTap;
        public BaseCell cellDown;
        public boolean isLeft;
        /** pressed or released */
        public boolean released;
        public boolean downHandled;
        public boolean upHandled;

        @Override
        public String toString() {
            return "{ isLeft=" + isLeft
                + ", released=" + released
                + ", cellDown=" + cellDown
                + " }";
        }
    }

}
