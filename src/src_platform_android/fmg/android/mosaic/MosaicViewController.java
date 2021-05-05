package fmg.android.mosaic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import fmg.android.app.DrawableView;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicInitData;
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
    private final ClickInfo clickInfo = new ClickInfo();
    /** <li>true : bind Control.SizeProperty to Model.Size
     *  <li>false: bind Model.Size to Control.SizeProperty */
    private boolean bindSizeDirection = true;
    private boolean extendedManipulation = false;

    // #region if ExtendedManipulation
    /// <summary> мин отступ от краев экрана для мозаики </summary>
    private final double minIndent = Cast.dpToPx(30.0f);
    private final boolean deferredZoom = !true;
    private boolean manipulationStarted;
    private boolean turnX;
    private boolean turnY;
    private long dtInertiaStarting;
    private static Double baseWheelDelta;
    private Object/*IDisposable*/ areaScaleObservable;
    private Object/*Transform*/ originalTransform;
    private Object/*CompositeTransform*/ scaleTransform;
    private double deferredArea;
    private MotionEvent latestOnTouchEv;

    private PointDouble lastScrollPosition;

    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedControlSize = new Size(-1, -1);

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MosaicViewController.this.onGestureScaleBegin();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return MosaicViewController.this.onGestureScale();
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            MosaicViewController.this.onGestureScaleEnd();
        }
    };

    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

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
        public PointDouble devicePosition;
        public SizeDouble mosaicSize;
        public SizeDouble mosaicOffset;
    }
    private ZoomStartInfo zoomStartInfo;
    // #endregion

    public MosaicViewController(Context context) {
        super(new MosaicViewView(context));

        if (context == null)
            return;

        this.context = context;

        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector      = new      GestureDetector(context,      gestureListener);

        subscribeToViewControl();
    }

    public void setViewControl(DrawableView view) {
        unsubscribeToViewControl();

        getView().setControl(view);
        context = (view==null) ? null : view.getContext();
        scaleGestureDetector = (context==null) ? null : new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector      = (context==null) ? null : new      GestureDetector(context,      gestureListener);

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
        return extendedManipulation;
    }
    public void setExtendedManipulation(boolean value) {
        if (_notifier.setProperty(extendedManipulation, value, PROPERTY_EXTENDED_MANIPULATION)) {
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
        control.setOnKeyListener((v, kc, ev) -> onKey(kc, ev));

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

    /** узнаю мах размер площади ячеек мозаики (для размера поля 3x3) так, чтобы поле влазило в текущий размер Control'а
      * @return макс площадь ячейки */
    private double calcMaxArea() {
        if (cachedMaxArea != null)
            return cachedMaxArea;
        Matrisize mosaicSizeField = new Matrisize(3, 3);
        SizeDouble sizeIn = getModel().getSize();
        SizeDouble sizeOut = new SizeDouble();
        double area = MosaicHelper.findAreaBySize(this.getMosaicType(), mosaicSizeField, sizeIn, sizeOut);
        //logger.debug("MosaicFrameworkElementController.CalcMaxArea: area="+area);
        cachedMaxArea = area; // caching value
        return area;
    }
    private Double cachedMaxArea; // cached value

    private void beforeZoom(PointDouble mouseDevicePosition) {
        if (mouseDevicePosition != null) {
            if (zoomStartInfo == null)
                zoomStartInfo = new ZoomStartInfo();
            zoomStartInfo.devicePosition = mouseDevicePosition;
            zoomStartInfo.mosaicOffset = getOffset();
            zoomStartInfo.mosaicSize = getModel().getMosaicSize();
        } else {
            zoomStartInfo = null;
        }
    }

    private void afterZoom() {
        if (zoomStartInfo == null)
            return;

        PointDouble devicePos = zoomStartInfo.devicePosition;
        SizeDouble mosaicSizeNew = getModel().getMosaicSize();//GetMosaicSize(Model.SizeField, Model.Area);
        SizeDouble offsetNew = new SizeDouble(
                    devicePos.x - (devicePos.x - zoomStartInfo.mosaicOffset.width ) * mosaicSizeNew.width  / zoomStartInfo.mosaicSize.width,
                    devicePos.y - (devicePos.y - zoomStartInfo.mosaicOffset.height) * mosaicSizeNew.height / zoomStartInfo.mosaicSize.height);
        setOffset(offsetNew);
    }

    /** Zoom + */
    void zoomInc(double zoomPower/* = 1.3 */, PointDouble mouseDevicePosition/* = null */) {
        beforeZoom(mouseDevicePosition);
        if (deferredZoom) {
            scale(1.01 * zoomPower);
        } else {
            double newArea = getModel().getArea() * 1.01 * zoomPower;
            getModel().setArea(Math.min(newArea, calcMaxArea()));
            afterZoom();
        }
    }

    /** Zoom - */
    void zoomDec(double zoomPower/* = 1.3 */, PointDouble mouseDevicePosition/* = null */) {
        beforeZoom(mouseDevicePosition);
        if (deferredZoom) {
            scale(0.99 / zoomPower);
        } else {
            double newArea = getModel().getArea() * 0.99 / zoomPower;
            getModel().setArea(Math.max(newArea, MosaicInitData.AREA_MINIMUM));
            afterZoom();
        }
    }

    private void scale(double scaleMul) {
        View ctrl = getViewControl();
        throw new RuntimeException("Not implemented...");
        /*
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
        */
    }

    /** Zoom minimum */
    private void zoomMin() {
        getModel().setArea(MosaicInitData.AREA_MINIMUM);
    }

    /** Zoom maximum */
    private void zoomMax() {
        double maxArea = calcMaxArea();
        getModel().setArea(maxArea);
    }

    @Override
    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        super.onModelPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicDrawModel.PROPERTY_SIZE:
            onModelSizeChanged(ev);
            break;
        case MosaicDrawModel.PROPERTY_AREA:
            onModelAreaChanged(ev);
            break;
        }
    }


    private void onControlSizeChanged(Size newSize) {
        if (!bindSizeDirection)
            getModel().setSize(Cast.toSizeDouble(newSize));
    }

    private void onModelSizeChanged(PropertyChangeEvent ev) {
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

    private void onModelAreaChanged(PropertyChangeEvent ev) {
        if (!extendedManipulation)
            return;
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onModelAreaChanged", String.format("newArea=%.2f, oldValue=%.2f", ev.getNewValue(), ev.getOldValue()))) {
            setOffset(getOffset()); // implicit call RecheckOffset
        }
    }

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
        clickInfo.cellDown = clickResult.getCellDown();
        clickInfo.isLeft = clickResult.isLeft();
        boolean handled = clickResult.isAnyChanges();
        if (clickResult.isDown())
            clickInfo.downHandled = handled;
        else
            clickInfo.upHandled = handled;
        clickInfo.released = !clickResult.isDown();
        //Logger.info(">>>>>>> clickInfo=" + clickInfo + "\n" + Arrays.asList(Thread.currentThread().getStackTrace()).stream().map(x -> x.toString()).filter(x -> x.startsWith("fmg.")).collect(Collectors.joining("\n")));
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


    private boolean onGenericMotion(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGenericMotion", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }

    private boolean onTouch(MotionEvent ev) {
        latestOnTouchEv = ev;
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onTouch", "ev=" + motionEventToString(ev), () -> "handled="+handled[0] + "\n-----------------------------")) {
            handled[0] = scaleGestureDetector.onTouchEvent(ev);
            if (ev.getPointerCount() == 1)
                handled[0] = gestureDetector.onTouchEvent(ev) || handled[0];

            if ((ev.getAction() == MotionEvent.ACTION_UP) && !clickInfo.released)
                handled[0] = onClickCommon(ev, true, false);

            //return handled[0];
        }

        return true; // !! always return true
    }

    private boolean onKey(int keyCode, KeyEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onKey", "keyCode=" + keyCode + "; ev=" + ev)) {
            return handled[0];
        }
    }

    ///////////////// begin Gesture
    /// interface OnGestureListener
    private boolean onGestureDown(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDown", "ev=" + motionEventToString(ev), () -> "handled=" + handled[0])) {
            turnX = turnY = false;
            dtInertiaStarting = 0;
            lastScrollPosition = null;

            if (!clickInfo.isDoubleTap)
                return handled[0] = onClickCommon(ev, true, true);

            return handled[0];
        }
    }
    private void onGestureShowPress(MotionEvent ev) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureShowPress", "ev=" + motionEventToString(ev)))
        {
        }
    }
    private boolean onGestureSingleTapUp(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureSingleTapUp", "ev=" + motionEventToString(ev), () -> "handled=" + handled[0])) {
            return handled[0] = onClickCommon(ev, true, false);
        }
    }

    private boolean onGestureScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
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

    private void onGestureLongPress(MotionEvent ev) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureLongPress", "ev=" + motionEventToString(ev)))
        {
            if ((clickInfo.cellDown != null) && (clickInfo.cellDown.getState().getStatus() == EState._Close)) {
                // imitate right mouse click - to (un)set flag
                mouseReleased(null, true);
                onClickCommon(ev, false, true);
                onClickCommon(ev, false, false);
            }
        }
    }
    private boolean onGestureFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
        // I spied the inertia in Windows UWP.
        // Chose the formula of inertia by similarity.
        //  https://www.rapidtables.com/tools/scatter-plot.html
        // Open in browser as URL: data:image/png;base64,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
        // The formula is how long the total distance will be covered.
        // (x - time in seconds; y - distance)
        //    y = x*x * 1320
        // x from y
        //    x = sqrt(y/1320)
        //  https://www.wolframalpha.com/input/?i=graph+y+%3D+x*x+*+1320
        //  https://www.wolframalpha.com/input/?i=posterize+image+y+%3D+x*x+*+1320
        //  https://www.mathway.com/Algebra

        double totalDistance = Math.sqrt((double)velocityX * velocityX + (double)velocityY * velocityY);
        double totalTime = Math.sqrt(totalDistance / 1320) * 1000; // from seconds to millisecods

        double sin = velocityY / totalDistance;
        double cos = velocityX / totalDistance;

        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureFling", "ev1=" + motionEventToString(ev1) + "; ev2=" + motionEventToString(ev2) + "; velocityX=" + velocityX + "; velocityY=" + velocityY, () -> "handled=" + handled[0]))
        {
            dtInertiaStarting = new Date().getTime();
            onManipulationDelta(new PointDouble(ev2.getX(), ev2.getY()), true, 0, 0);
            return handled[0] = true;
        } finally {
            double[] deltaTranslationTotalX = { 0 };
            double[] deltaTranslationTotalY = { 0 };
            long[] currTime = { 0 };
            AsyncRunner.Repeat(() -> {
                    double currDistance = Math.sin(currTime[0] * Math.PI/2 / totalTime) * totalDistance; // Distance function in current inertia - sine decay function from 0 to Pi/2
                    double currX = currDistance * cos;
                    double currY = currDistance * sin;
                    double deltaTransX = currX - deltaTranslationTotalX[0];
                    double deltaTransY = currY - deltaTranslationTotalY[0];
                    deltaTranslationTotalX[0] = (float)currX;
                    deltaTranslationTotalY[0] = (float)currY;
                    PointDouble evCurrPosition = new PointDouble(ev2.getX() + currX, ev2.getY() + currY);
                    onManipulationDelta(evCurrPosition, true, (float)deltaTransX, (float)deltaTransY);
                },
        5,
                () -> {
                    currTime[0] = new Date().getTime() - dtInertiaStarting;
                    return currTime[0] > totalTime;
                });
        }
    }

    /// interface OnDoubleTapListener
    private boolean onGestureSingleTapConfirmed(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureSingleTapConfirmed", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    private boolean onGestureDoubleTap(MotionEvent ev) {
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
                zoomStartInfo = null;

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
    private boolean onGestureDoubleTapEvent(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDoubleTapEvent", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            clickInfo.isDoubleTap = (ev.getAction() == MotionEvent.ACTION_DOWN);
            return handled[0];
        }
    }
    /// interface OnContextClickListener
    private boolean onGestureContextClick(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureContextClick", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }

    /// interface OnScaleGestureListener
    private boolean onGestureScaleBegin() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureScaleBegin", () -> "handled="+handled[0])) {
            return handled[0] = true;
        }
    }

    private boolean onGestureScale() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureScale", () -> "handled="+handled[0])) {
//            assert (latestOnTouchEv.getPointerCount() >= 2);
            if (latestOnTouchEv.getPointerCount() == 2) {

                MotionEvent ev = latestOnTouchEv;
                PointDouble evPosition = new PointDouble((ev.getX(0) + ev.getX(1)) / 2,
                                                         (ev.getY(0) + ev.getY(1)) / 2);

                double deltaScale = scaleGestureDetector.getScaleFactor();
                tracer.put("scaleFactor={0}; evPosition={1}", deltaScale, evPosition);

                if (deltaScale > 0)
                    zoomInc(deltaScale, evPosition);
                else
                    zoomDec(2 + deltaScale, evPosition);
            }
            return handled[0] = true;
        }
    }

    private void onGestureScaleEnd() {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureScaleEnd")) {
            return;
        }
    }

    ///////////////// end Gesture

    private void onManipulationDelta(PointDouble evCurrPosition, boolean isInertial, float deltaTransX, float deltaTransY) {
        //var deltaScale = delta.Scale;
        //try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onManipulationDelta", "pos=" + evCurrPosition + "; isInertial=" + isInertial + "; deltaTransX=" + deltaTransX + "; deltaTransY=" + deltaTransY))
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
                if (clickInfo.cellDown != null) {
                    PointDouble startPoint = evCurrPosition;
                    //var inCellRegion = _tmpClickedCell.PointInRegion(startPoint.ToFmRect());
                    //this._contentRoot.Background = new SolidColorBrush(inCellRegion ? Colors.Aquamarine : Colors.DeepPink);
                    RectDouble rcOuter = clickInfo.cellDown.getRcOuter().moveXY(offset);
                    float min = Cast.dpToPx(25);
                    rcOuter = rcOuter.moveXY(-min, -min);
                    rcOuter.width  += min * 2;
                    rcOuter.height += min * 2;
                    needDrag = !rcOuter.contains(startPoint);
                }
                //#endregion check possibility dragging

                if (needDrag) {
                    // #region Compound motion

                    if (turnX)
                        deltaTransX *= -1;
                    if (turnY)
                        deltaTransY *= -1;

                    if (isInertial) {
                        double totalSeconds = (new Date().getTime() - dtInertiaStarting) / 1000.0; // TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - _dtInertiaStarting);
                        double coefFading = Math.max(0, 1 - 0.32 * totalSeconds);
                        deltaTransX *= coefFading;
                        deltaTransY *= coefFading;

                        int fullX = (int)(deltaTransX / size.width);
                        int fullY = (int)(deltaTransY / size.height);
                        if (fullX != 0) {
                            deltaTransX -=  fullX * size.width;
                            if ((fullX & 1) == 1)
                                turnX = !turnX;
                        }
                        if (fullY != 0) {
                            deltaTransY -=  fullY * size.height;
                            if ((fullY & 1) == 1)
                                turnY = !turnY;
                        }

                        //tracer.put("inertial coeff fading = " + coefFading + "; deltaTrans={" + deltaTransX + ", " + deltaTransY + "}");
                    }

                    SizeDouble mosaicSize = getModel().getMosaicSize();
                    if ((offset.width + mosaicSize.width + deltaTransX) < minIndent) { // правый край мозаики пересёк левую сторону контрола?
                        if (isInertial) {
                            turnX = !turnX; // разворачиваю по оси X

                            double dx1 = mosaicSize.width + offset.width - minIndent;  // часть deltaTrans.X которая не вылезла за границу
                            double dx2 = deltaTransX +  dx1;                           // часть deltaTrans.X которая вылезла за границу
                            deltaTransX -= 2 * dx2;                                    // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.width = minIndent - mosaicSize.width - deltaTransX; // привязываю к левой стороне контрола
                        }
                    } else
                    if ((offset.width + deltaTransX) > (size.width - minIndent)) { // левый край мозаики пересёк правую сторону контрола?
                        if (isInertial) {
                            turnX = !turnX; // разворачиваю по оси X

                            double dx1 = size.width - offset.width - minIndent;    // часть deltaTrans.X которая не вылезла за границу
                            double dx2 = deltaTransX - dx1;                        // часть deltaTrans.X которая вылезла за границу
                            deltaTransX -= 2 * dx2;                                // та часть deltaTrans.X которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.width = size.width - minIndent - deltaTransX;   // привязываю к правой стороне контрола
                        }
                    }

                    if ((offset.height + mosaicSize.height + deltaTransY) < minIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
                        if (isInertial) {
                            turnY = !turnY; // разворачиваю по оси Y

                            double dy1 = mosaicSize.height + offset.height - minIndent;  // часть deltaTrans.Y которая не вылезла за границу
                            double dy2 = deltaTransY + dy1;                              // часть deltaTrans.Y которая вылезла за границу
                            deltaTransY -= 2 * dy2;                                      // та часть deltaTrans.Y которая залезла за границу, разворачиваю обратно
                        } else {
                            offset.height = minIndent - mosaicSize.height - deltaTransY; // привязываю к верхней стороне контрола
                        }
                    } else
                    if ((offset.height + deltaTransY) > (size.height - minIndent)) { // вержний край мозаики пересёк нижнюю сторону контрола?
                        if (isInertial) {
                            turnY = !turnY; // разворачиваю по оси Y

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

    private void onClick() {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onClick"))
        {
        }
    }
    private boolean onLongClick() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onLongClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    private boolean onDrag(DragEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onDrag", "ev=" + dragEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    private boolean onHover(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onHover", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    private boolean onContextClick() {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onContextClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    private void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onScrollChange"))
        {
        }
    }

    public void onFocusChange(boolean hasFocus) {
        Logger.info("Mosaic.onFocusChange: hasFocus=" + hasFocus);
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
        dtInertiaStarting = 0;
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
