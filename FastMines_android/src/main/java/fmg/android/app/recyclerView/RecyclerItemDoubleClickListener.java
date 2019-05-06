package fmg.android.app.recyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.function.BiConsumer;

public class RecyclerItemDoubleClickListener implements RecyclerView.OnItemTouchListener {

    private final GestureDetector gesturator;
    private BiConsumer<View, Integer> onItemDoubleClick;

    public RecyclerItemDoubleClickListener(Context context) {
        gesturator = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent ev) {
                return true;
            }
        });
    }

    public void setOnItemDoubleClick(BiConsumer<View, Integer> onItemDoubleClick) {
        this.onItemDoubleClick = onItemDoubleClick;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent ev) {
        if (!gesturator.onTouchEvent(ev))
            return false;

        View childView = view.findChildViewUnder(ev.getX(), ev.getY());
        if (childView == null)
            return false;

        if (onItemDoubleClick == null)
            return false;

        onItemDoubleClick.accept(childView, view.getChildAdapterPosition(childView));
        return true;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent ev) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }

}
