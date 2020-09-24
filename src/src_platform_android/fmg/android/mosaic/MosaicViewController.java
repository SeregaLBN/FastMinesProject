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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import fmg.android.app.DrawableView;
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

    private Context context;
    private final ClickInfo _clickInfo = new ClickInfo();
    /** true : bind Control.SizeProperty to Model.Size
     *  false: bind Model.Size to Control.SizeProperty */
    private boolean bindSizeDirection = true;

    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedControlSize = new Size(-1, -1);

    private GestureDetector _gd;
    private final GestureDetector.OnGestureListener _gestureListener = new GestureDetector.SimpleOnGestureListener(){

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
            return MosaicViewController.this.onGestureScroll(ev1, ev2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            MosaicViewController.this.onGestureLongPress(ev);
        }

        @Override
        public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
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

    // #region if ExtendedManipulation
    /// <summary> мин отступ от краев экрана для мозаики </summary>
    private final double minIndent = Cast.dpToPx(30.0f);
    private final boolean DeferredZoom = true;
    private boolean _manipulationStarted;
    private boolean _turnX;
    private boolean _turnY;
    private Date _dtInertiaStarting;
    private static Double _baseWheelDelta;
    private Object/*IDisposable*/ _areaScaleObservable;
    private Object/*Transform*/ _originalTransform;
    private Object/*CompositeTransform*/ _scaleTransform;
    private double _deferredArea;

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


    @Override
    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
        super.onPropertyModelChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicDrawModel.PROPERTY_SIZE:
            onSizeChanged(ev);
            break;
        case MosaicDrawModel.PROPERTY_AREA:
//            onAreaChanged(ev as PropertyChangedExEventArgs<double>);
            break;
        }
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

    private void onControlSizeChanged(Size newSize) {
        if (!bindSizeDirection)
            getModel().setSize(Cast.toSizeDouble(newSize));

//        if (_extendedManipulation)
//            RecheckLocation();
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
        return handled;
    }

    boolean onClickLost() {
        return clickHandler(this.mouseFocusLost());
    }

    boolean onClickCommon(MotionEvent ev, boolean leftClick, boolean down) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onClickCommon", "ev=" + motionEventToString(ev), () -> "handled="+handled[0]))
        {
            PointDouble point = toImagePoint(ev);
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
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onTouch", "ev=" + motionEventToString(ev), () -> "handled="+handled[0])) {
            handled[0] = _gd.onTouchEvent(ev);

            if (!handled[0]) {
                switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (_clickInfo.isLongPress)
                        handled[0] = onClickCommon(ev, true, false);
                    break;
                }
            }

            return handled[0];
        }
    }

    ///////////////// begin Gesture
    protected boolean onGestureDown(MotionEvent ev) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureDown", "ev=" + motionEventToString(ev), () -> "handled=" + handled[0])) {
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
            _clickInfo.isLongPress = false;
            return handled[0] = onClickCommon(ev, true, false);
        }
    }
    protected boolean onGestureScroll(MotionEvent ev1, MotionEvent ev2, float distanceX, float distanceY) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureScroll", "ev1=" + motionEventToString(ev1) + "; ev2=" + motionEventToString(ev2) + "; distX=" + distanceX + "; distY=" + distanceY, () -> "handled=" + handled[0]))
        {
            return handled[0];
        }
    }
    protected void onGestureLongPress(MotionEvent ev) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureLongPress", "ev=" + motionEventToString(ev)))
        {
            if (_clickInfo.cellDown.getState().getStatus() == EState._Close) {
                // imitate right mouse click - to (un)set flag
                mouseReleased(null, true);
                onClickCommon(ev, false, true);
                onClickCommon(ev, false, false);
            } else {
                _clickInfo.isLongPress = true;
            }
        }
    }
    protected boolean onGestureFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
        boolean[] handled = { false };
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onGestureFling", "ev1=" + motionEventToString(ev1) + "; ev2=" + motionEventToString(ev2) + "; velocityX=" + velocityX + "; velocityY=" + velocityY, () -> "handled=" + handled[0]))
        {
            return handled[0];
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
    protected void onScrollChange(int var1, int var2, int var3, int var4) {
        try (Logger.Tracer tracer = new Logger.Tracer("Mosaic.onScrollChange"))
        {
        }
    }

    /*
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mousePressed(Cast.toPointDouble(e.getPoint()), true);
        } else
        if (SwingUtilities.isRightMouseButton(e)) {
            mousePressed(Cast.toPointDouble(e.getPoint()), false);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            mouseReleased(Cast.toPointDouble(e.getPoint()), true);
        } else
        if (SwingUtilities.isRightMouseButton(e)) {
            mouseReleased(Cast.toPointDouble(e.getPoint()), false);
        }
    }
    */

    public void onFocusChange(boolean hasFocus) {
        System.out.println("Mosaic.onFocusChange: hasFocus=" + hasFocus);
        if (!hasFocus)
            mouseFocusLost();
    }

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
      //control.setOnCapturedPointerListener();
        control.setOnContextClickListener(v -> onContextClick());
        control.setOnGenericMotionListener((v, ev) -> onGenericMotion(ev));
        control.setOnScrollChangeListener((v, _1, _2, _3, _4) -> onScrollChange(_1, _2, _3, _4));
    }

    private void unsubscribeToViewControl() {
        View control = getViewControl();
        if (control == null)
            return;

        control.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        sizeChangedObservable.dispose();
        subjSizeChanged = null;

        control.setOnDragListener(null);
        control.setOnHoverListener(null);
      //control.setOnCapturedPointerListener(null);
        control.setOnContextClickListener(null);
        control.setOnGenericMotionListener(null);
        control.setOnScrollChangeListener(null);
        control.setOnLongClickListener(null);
        control.setOnClickListener(null);
        control.setOnTouchListener(null);
        control.setOnFocusChangeListener(null);
        control.setFocusable(false);
    }

    private SizeDouble getOffset() { return getModel().getMosaicOffset(); }
    private void setOffset(SizeDouble offset) { getModel().setMosaicOffset(recheckOffset(offset)); }

    private SizeDouble recheckOffset(SizeDouble offset) {
        /* TODO
        var size = Model.Size;
        var mosaicSize = Model.MosaicSize;
        if ((offset.Width + mosaicSize.Width) < MinIndent) { // правый край мозаики пересёк левую сторону контрола?
            offset.Width = MinIndent - mosaicSize.Width; // привязываю к левой стороне контрола
        } else {
            if (offset.Width > (size.Width - MinIndent)) // левый край мозаики пересёк правую сторону контрола?
                offset.Width = size.Width - MinIndent; // привязываю к правой стороне контрола
        }

        if ((offset.Height + mosaicSize.Height) < MinIndent) { // нижний край мозаики пересёк верхнюю сторону контрола?
            offset.Height = MinIndent - mosaicSize.Height; // привязываю к верхней стороне контрола
        } else {
            if (offset.Height > (size.Height - MinIndent)) // вержний край мозаики пересёк нижнюю сторону контрола?
                offset.Height = size.Height - MinIndent; // привязываю к нижней стороне контрола
        }
        */

        return offset;
    }

    @Override
    public void close() {
        unsubscribeToViewControl();
        super.close();
        getView().close();
    }


    private PointDouble toImagePoint(MotionEvent ev) {
//        View imgControl = this.getViewPanel();
        PointDouble point = new PointDouble(ev.getX(), ev.getY()); // imgControl.TransformToVisual(imgControl).TransformPoint(pagePoint).ToFmPointDouble();
        //var o = GetOffset();
        //var point2 = new PointDouble(pagePoint.X - o.Left, pagePoint.Y - o.Top);
        //System.Diagnostics.Debug.Assert(point == point2);
        return point;
    }

    class ClickInfo {
        boolean isLongPress;
        boolean isDoubleTap;
        public BaseCell cellDown;
        public boolean isLeft;
        /** pressed or released */
        public boolean released;
        public boolean downHandled;
        public boolean upHandled;
    }

}
