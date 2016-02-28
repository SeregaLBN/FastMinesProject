package fmg.core.types.event;

import java.util.EventObject;

import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.cells.BaseCell;

public class MosaicEvent extends EventObject {
   private static final long serialVersionUID = 1L;

   private MosaicEvent(MosaicBase source) {
      super(source);
   }

   public static class ClickEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;
      BaseCell cell;
      boolean leftClick;
      boolean down;

      public ClickEvent(MosaicBase source, BaseCell clickedCell, boolean leftClick, boolean down) {
         super(source);
         this.cell = clickedCell;
         this.leftClick  = leftClick;
         this.down = down;
      }

      public boolean isLeftClick() {
         return leftClick;
      }
      public boolean isDown() {
         return down;
      }
      public BaseCell getCell() {
         return cell;
      }
   }

   @Override
   public MosaicBase getSource() {
      return (MosaicBase)super.getSource();
   }
}