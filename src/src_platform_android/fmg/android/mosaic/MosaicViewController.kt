package fmg.android.mosaic

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

import fmg.common.LoggerSimple
import fmg.common.geom.PointDouble
import fmg.core.mosaic.MosaicController
import fmg.core.mosaic.MosaicDrawModel
import fmg.core.mosaic.cells.BaseCell
import fmg.core.types.ClickResult

/** MVC: controller. Android implementation  */
class MosaicViewController(private val _owner: Activity) : MosaicController<View, Bitmap, MosaicViewView, MosaicDrawModel<Bitmap>>(MosaicViewView(_owner)) {

    private val _clickInfo = ClickInfo()
    private val _gd: GestureDetector
    private val _gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(ev: MotionEvent): Boolean {
            return this@MosaicViewController.onGestureDoubleTap(ev)
        }

        override fun onLongPress(ev: MotionEvent) {
            super.onLongPress(ev)
            this@MosaicViewController.onGestureLongPress(ev)
        }

        override fun onDoubleTapEvent(ev: MotionEvent): Boolean {
            return this@MosaicViewController.onGestureDoubleTapEvent(ev)
        }

        override fun onDown(ev: MotionEvent): Boolean {
            return this@MosaicViewController.onGestureDown(ev)
        }

    }

    val viewPanel: View
        get() = view.control


    init {
        _gd = GestureDetector(_owner/*.getApplicationContext()*/, _gestureListener)
        subscribeToViewControl()
    }

    internal fun clickHandler(clickResult: ClickResult?): Boolean {
        if (clickResult == null)
            return false
        _clickInfo.cellDown = clickResult.cellDown
        _clickInfo.isLeft = clickResult.isLeft
        val handled = clickResult.isAnyChanges
        if (clickResult.isDown)
            _clickInfo.downHandled = handled
        else
            _clickInfo.upHandled = handled
        _clickInfo.released = !clickResult.isDown
        return handled
    }

    internal fun onClickLost(): Boolean {
        return clickHandler(this.mouseFocusLost())
    }

    internal fun onClickCommon(ev: MotionEvent, leftClick: Boolean, down: Boolean): Boolean {
        val point = toImagePoint(ev)
        return clickHandler(if (down)
            mousePressed(point, leftClick)
        else
            mouseReleased(point, leftClick))
    }


    protected fun onGenericMotion(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onGenericMotion", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onTouch(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onTouch", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer ->
            _gd.onTouchEvent(ev)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> handled[0] = onClickCommon(ev, true, true)
                MotionEvent.ACTION_UP -> handled[0] = onClickCommon(ev, true, false)
            }
            return handled[0]
        }
    }

    protected fun onGestureDoubleTap(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(!false)
        LoggerSimple.Tracer("Mosaic.onGestureDoubleTap", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onGestureDoubleTapEvent(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(!false)
        LoggerSimple.Tracer("Mosaic.onGestureDoubleTapEvent", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onGestureDown(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onGestureDown", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onClick() {
        LoggerSimple.Tracer("Mosaic.onClick").use { tracer -> }
    }

    protected fun onGestureLongPress(ev: MotionEvent) {
        LoggerSimple.Tracer("Mosaic.onGestureLongPress", "action=" + eventActionToString(ev.action)).use { tracer -> return }
    }

    protected fun onLongClick(): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onLongClick") { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onDrag(ev: DragEvent): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onDrag", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onHover(ev: MotionEvent): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onHover", "action=" + eventActionToString(ev.action)) { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onContextClick(): Boolean {
        val handled = booleanArrayOf(false)
        LoggerSimple.Tracer("Mosaic.onContextClick") { "handled=" + handled[0] }.use { tracer -> return handled[0] }
    }

    protected fun onScrollChange(var1: Int, var2: Int, var3: Int, var4: Int) {
        LoggerSimple.Tracer("Mosaic.onScrollChange").use { tracer -> }
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

    fun onFocusChange(hasFocus: Boolean) {
        println("Mosaic.onFocusChange: hasFocus=$hasFocus")
        if (!hasFocus)
            mouseFocusLost()
    }

    override fun checkNeedRestoreLastGame(): Boolean {
        val selectedNo = booleanArrayOf(true)
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> selectedNo[0] = true

                DialogInterface.BUTTON_NEGATIVE -> selectedNo[0] = false
            }
        }

        val builder = AlertDialog.Builder(_owner/*.getApplicationContext()*/)
        builder.setMessage("Restore last game?") // Question
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show()
        return selectedNo[0]
    }

    private fun subscribeToViewControl() {
        val control = this.view.control
        control.isFocusable = true // ? иначе не будет срабатывать FocusListener
        control.setOnFocusChangeListener { v, hasFocus -> onFocusChange(hasFocus) }
        control.setOnTouchListener { v, ev -> onTouch(ev) }
        control.setOnClickListener { v -> onClick() }
        control.setOnLongClickListener { v -> onLongClick() }
        control.setOnDragListener { v, ev -> onDrag(ev) }
        control.setOnHoverListener { v, ev -> onHover(ev) }
        //control.setOnCapturedPointerListener();
        control.setOnContextClickListener { v -> onContextClick() }
        control.setOnGenericMotionListener { v, ev -> onGenericMotion(ev) }
        control.setOnScrollChangeListener { v, _1, _2, _3, _4 -> onScrollChange(_1, _2, _3, _4) }
    }

    private fun unsubscribeToViewControl() {
        val control = this.view.control
        control.setOnDragListener(null)
        control.setOnHoverListener(null)
        //control.setOnCapturedPointerListener(null);
        control.setOnContextClickListener(null)
        control.setOnGenericMotionListener(null)
        control.setOnScrollChangeListener(null)
        control.setOnLongClickListener(null)
        control.setOnClickListener(null)
        control.setOnTouchListener(null)
        control.onFocusChangeListener = null
        control.isFocusable = false
    }

    override fun close() {
        unsubscribeToViewControl()
        view.close()
        super.close()
    }


    private fun toImagePoint(ev: MotionEvent): PointDouble {
        //      View imgControl = this.getViewPanel();
        //var o = GetOffset();
        //var point2 = new PointDouble(pagePoint.X - o.Left, pagePoint.Y - o.Top);
        //System.Diagnostics.Debug.Assert(point == point2);
        return PointDouble(ev.x.toDouble(), ev.y.toDouble())
    }

    internal inner class ClickInfo {
        var cellDown: BaseCell? = null
        var isLeft: Boolean = false
        /** pressed or released  */
        var released: Boolean = false
        var downHandled: Boolean = false
        var upHandled: Boolean = false
    }

    companion object {


        internal fun eventActionToString(eventAction: Int): String {
            when (eventAction) {
                MotionEvent.ACTION_CANCEL       -> return "Cancel"
                MotionEvent.ACTION_DOWN         -> return "Down"
                MotionEvent.ACTION_MOVE         -> return "Move"
                MotionEvent.ACTION_OUTSIDE      -> return "Outside"
                MotionEvent.ACTION_UP           -> return "Up"
                MotionEvent.ACTION_POINTER_DOWN -> return "Pointer Down"
                MotionEvent.ACTION_POINTER_UP   -> return "Pointer Up"
            }
            return "???"
        }
    }

}
