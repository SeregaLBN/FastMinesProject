using fmg.common.geom;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.core.mosaic.draw;

namespace fmg.core.types.Event
{

   public class MosaicEvent : System.EventArgs
   {

      public class ClickEventArgs : MosaicEvent
      {
         BaseCell cell;
         bool leftClick;
         bool down;

         public ClickEventArgs(BaseCell clickedCell, bool leftClick, bool down)
         {
            this.cell = clickedCell;
            this.leftClick = leftClick;
            this.down = down;
         }

         public bool isLeftClick()
         {
            return leftClick;
         }
         public bool isDown()
         {
            return down;
         }
         public BaseCell getCell()
         {
            return cell;
         }
      }

      public class ChangedCountersEventArgs : MosaicEvent
      {
      }

      public class ChangedGameStatusEventArgs : MosaicEvent
      {
         EGameStatus oldGameStatus;

         public ChangedGameStatusEventArgs(EGameStatus oldGameStatus)
         {
            this.oldGameStatus = oldGameStatus;
         }

         public EGameStatus getOldGameStatus()
         {
            return oldGameStatus;
         }
      }

      public class ChangedAreaEventArgs : MosaicEvent
      {
         int oldArea;

         public ChangedAreaEventArgs(int oldArea)
         {
            this.oldArea = oldArea;
         }

         public int getOldArea()
         {
            return oldArea;
         }
      }

      public class ChangedMosaicTypeEventArgs : MosaicEvent
      {
         EMosaic oldMosaic;

         public ChangedMosaicTypeEventArgs(EMosaic oldMosaic)
         {
            this.oldMosaic = oldMosaic;
         }

         public EMosaic getOldMosaic()
         {
            return oldMosaic;
         }
      }

      public class ChangedMosaicSizeEventArgs : MosaicEvent
      {
         Matrisize oldSize;

         public ChangedMosaicSizeEventArgs(Matrisize oldSize)
         {
            this.oldSize = oldSize;
         }

         public Matrisize getOldSize()
         {
            return oldSize;
         }
      }

      public static MosaicBase<TPaintable> getSource<TPaintable>(object sender) where TPaintable : IPaintable {
         return sender as MosaicBase<TPaintable>;
      }
   }
}