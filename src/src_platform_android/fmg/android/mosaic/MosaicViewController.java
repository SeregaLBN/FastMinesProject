package fmg.android.mosaic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.function.Consumer;

import fmg.common.LoggerSimple;
import fmg.common.geom.PointDouble;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.ClickResult;

/** MVC: controller. Android implementation */
public class MosaicViewController extends MosaicController<View, Bitmap, MosaicViewView, MosaicDrawModel<Bitmap>> {

    private final Context context;
    private final ClickInfo _clickInfo = new ClickInfo();
    private final GestureDetector _gd;
    private final GestureDetector.SimpleOnGestureListener _gestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onDoubleTap(MotionEvent ev) {
            return MosaicViewController.this.onGestureDoubleTap(ev);
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            super.onLongPress(ev);
            MosaicViewController.this.onGestureLongPress(ev);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent ev) {
            return MosaicViewController.this.onGestureDoubleTapEvent(ev);
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            return MosaicViewController.this.onGestureDown(ev);
        }

    };


    public MosaicViewController(Context context) {
        super(new MosaicViewView(context));
        this.context = context;
        _gd = new GestureDetector(context, _gestureListener);
        subscribeToViewControl();
    }

    public MosaicViewController(View view, Consumer<Consumer<Canvas>> viewDrawMethod) {
        super(new MosaicViewView(view, viewDrawMethod));
        this.context = view.getContext();
        _gd = new GestureDetector(context, _gestureListener);
        subscribeToViewControl();
    }

    public View getViewPanel() {
        return getView().getControl();
    }


    static String eventActionToString(int eventAction) {
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
        PointDouble point = toImagePoint(ev);
        return clickHandler(down
            ? mousePressed(point, leftClick)
            : mouseReleased(point, leftClick));
    }


    protected boolean onGenericMotion(MotionEvent ev) {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onGenericMotion", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onTouch(MotionEvent ev) {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onTouch", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            _gd.onTouchEvent(ev);
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handled[0] = onClickCommon(ev, true, true);
                break;
            case MotionEvent.ACTION_UP:
                handled[0] = onClickCommon(ev, true, false);
                break;
            }
            return handled[0];
        }
    }
    protected boolean onGestureDoubleTap(MotionEvent ev) {
        boolean[] handled = { !false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onGestureDoubleTap", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onGestureDoubleTapEvent(MotionEvent ev) {
        boolean[] handled = { !false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onGestureDoubleTapEvent", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onGestureDown(MotionEvent ev) {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onGestureDown", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected void onClick() {
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onClick"))
        {
        }
    }
    protected void onGestureLongPress(MotionEvent ev) {
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onGestureLongPress", "action=" + eventActionToString(ev.getAction())))
        {
            return;
        }
    }
    protected boolean onLongClick() {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onLongClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onDrag(DragEvent ev) {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onDrag", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onHover(MotionEvent ev) {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onHover", "action=" + eventActionToString(ev.getAction()), () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected boolean onContextClick() {
        boolean[] handled = { false };
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onContextClick", () -> "handled="+handled[0]))
        {
            return handled[0];
        }
    }
    protected void onScrollChange(int var1, int var2, int var3, int var4) {
        try (LoggerSimple.Tracer tracer = new LoggerSimple.Tracer("Mosaic.onScrollChange"))
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
        View control = this.getView().getControl();
        control.setFocusable(true); // ? иначе не будет срабатывать FocusListener
        control.setOnFocusChangeListener((v, hasFocus) -> onFocusChange(hasFocus));
        control.setOnTouchListener((v, ev) -> onTouch(ev));
        control.setOnClickListener(v -> onClick());
        control.setOnLongClickListener(v -> onLongClick());
        control.setOnDragListener((v, ev) -> onDrag(ev));
        control.setOnHoverListener((v, ev) -> onHover(ev));
      //control.setOnCapturedPointerListener();
        control.setOnContextClickListener(v -> onContextClick());
        control.setOnGenericMotionListener((v, ev) -> onGenericMotion(ev));
        control.setOnScrollChangeListener((v, _1, _2, _3, _4) -> onScrollChange(_1, _2, _3, _4));
    }

    private void unsubscribeToViewControl() {
        View control = this.getView().getControl();
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

    @Override
    public void close() {
        unsubscribeToViewControl();
        getView().close();
        super.close();
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
        public BaseCell cellDown;
        public boolean isLeft;
        /** pressed or released */
        public boolean released;
        public boolean downHandled;
        public boolean upHandled;
    }

}
