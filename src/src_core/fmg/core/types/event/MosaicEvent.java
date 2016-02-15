package fmg.core.types.event;

import java.util.EventObject;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;

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

   public static class ChangedCountersEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;

      public ChangedCountersEvent(MosaicBase source) {
         super(source);
      }
   }

   public static class ChangedGameStatusEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;
      EGameStatus oldGameStatus;

      public ChangedGameStatusEvent(MosaicBase source, EGameStatus oldGameStatus) {
         super(source);
         this.oldGameStatus = oldGameStatus;
      }
   
      public EGameStatus getOldGameStatus() {
         return oldGameStatus;
      }
   }

   public static class ChangedAreaEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;
      double oldArea;

      public ChangedAreaEvent(MosaicBase source, double oldArea) {
         super(source);
         this.oldArea = oldArea;
      }
   
      public double getOldArea() {
         return oldArea;
      }
   }

   public static class ChangedMosaicTypeEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;
      EMosaic oldMosaic;

      public ChangedMosaicTypeEvent(MosaicBase source, EMosaic oldMosaic) {
         super(source);
         this.oldMosaic = oldMosaic;
      }
   
      public EMosaic getOldMosaic() {
         return oldMosaic;
      }
   }

   public static class ChangedMosaicSizeEvent extends MosaicEvent {
      private static final long serialVersionUID = 1L;
      Matrisize oldSize;

      public ChangedMosaicSizeEvent(MosaicBase source, Matrisize oldSize) {
         super(source);
         this.oldSize = oldSize;
      }
   
      public Matrisize getOldSize() {
         return oldSize;
      }
   }

   @Override
   public MosaicBase getSource() {
      return (MosaicBase)super.getSource();
   }
}