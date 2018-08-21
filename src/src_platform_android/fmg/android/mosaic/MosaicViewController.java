package fmg.android.mosaic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.ThreadLocalRandom;

import fmg.common.geom.PointDouble;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.android.utils.Cast;

/** MVC: controller. Android implementation */
public class MosaicViewController extends MosaicController<View, Bitmap, MosaicViewView, MosaicDrawModel<Bitmap>> {

   private final Activity _owner;
   private long _lastDown;
   private long _lastDuration;


   public MosaicViewController(Activity owner) {
      super(new MosaicViewView(owner));
      _owner = owner;
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

   protected boolean onGenericMotion(MotionEvent ev) {
      System.out.println("> Mosaic.onGenericMotion: action=" + eventActionToString(ev.getAction()));
      return !true;
   }
   protected boolean onTouch(MotionEvent ev) {
      System.out.println("> Mosaic.onTouch: action=" + eventActionToString(ev.getAction()));
      switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
         _lastDown = System.currentTimeMillis();
         mousePressed(new PointDouble(ev.getX(), ev.getY()), true);
         break;
      case MotionEvent.ACTION_UP:
         _lastDuration = System.currentTimeMillis() - _lastDown;
         mouseReleased(new PointDouble(ev.getX(), ev.getY()), true);
         break;
      }
      return !true;
   }
   protected void onClick() {
      System.out.println("> Mosaic.onClick");
   }
   protected boolean onLongClick() {
      System.out.println("> Mosaic.onLongClick");
      return !true;
   }
   protected boolean onDrag(DragEvent ev) {
      System.out.println("> Mosaic.onDrag: action=" + eventActionToString(ev.getAction()));
      return !true;
   }
   protected boolean onHover(MotionEvent ev) {
      System.out.println("> Mosaic.onHover: action=" + eventActionToString(ev.getAction()));
      return !true;
   }
   protected boolean onContextClick() {
      System.out.println("> Mosaic.onContextClick");
      return !true;
   }
   protected void onScrollChange(int var1, int var2, int var3, int var4) {
      System.out.println("> Mosaic.onScrollChange");
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

      AlertDialog.Builder builder = new AlertDialog.Builder(_owner.getApplicationContext());
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

   ////////////// TEST //////////////
   public static MosaicViewController getTestData(Activity owner) {
      MosaicView._DEBUG_DRAW_FLOW = true;
      MosaicViewController ctrllr = new MosaicViewController(owner);

      if (ThreadLocalRandom.current().nextBoolean()) {
         // unmodified controller test
      } else {
          EMosaic mosaicType = EMosaic.eMosaicTrSq1;
          ESkillLevel skill  = ESkillLevel.eBeginner;

          ctrllr.setArea(1500);
          ctrllr.setMosaicType(mosaicType);
          ctrllr.setSizeField(skill.getDefaultSize());
          ctrllr.setMinesCount(skill.getNumberMines(mosaicType));
          ctrllr.gameNew();
      }
      return ctrllr;
   }
   //////////////////////////////////

}
