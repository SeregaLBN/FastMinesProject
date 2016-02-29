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

      public static MosaicBase<TPaintable> getSource<TPaintable>(object sender) where TPaintable : IPaintable {
         return sender as MosaicBase<TPaintable>;
      }
   }
}