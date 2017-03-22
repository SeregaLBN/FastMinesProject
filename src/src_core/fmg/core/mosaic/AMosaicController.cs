using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic.cells;
using fmg.core.types.click;

namespace fmg.core.mosaic {

   /// <summary> MVC: controller. Base implementation </summary>
   public abstract class AMosaicController<TMosaicView> : Disposable
      where TMosaicView : class, IMosaicView
   {
      /// <summary> MVC: model </summary>
      protected Mosaic _mosaic;
      /// <summary> MVC: view </summary>
      protected TMosaicView _view;

      /// <summary> MVC: model </summary>
      public virtual Mosaic Mosaic {
         get {
            if (_mosaic == null)
               Mosaic = new Mosaic(); // call setter
            return _mosaic;
         }
         protected set {
            if (_mosaic != null) {
               _mosaic.Dispose();
            }
            _mosaic = value;
         }
      }

      /// <summary> MVC: view </summary>
      public virtual TMosaicView View { get; protected set; }

      /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
      private BaseCell CursorPointToCell(PointDouble point) {
         return Mosaic.Matrix.FirstOrDefault(cell =>
            //cell.getRcOuter().Contains(point) && // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            cell.PointInRegion(point));
      }

      public ClickResult MousePressed(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer(GetCallerName(), "clickPoint" + clickPoint + "; isLeftMouseButton=" + isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonDown(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonDown(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
         using (new Tracer(GetCallerName(), "isLeftMouseButton=" + isLeftMouseButton)) {
            return isLeftMouseButton
               ? Mosaic.OnLeftButtonUp(CursorPointToCell(clickPoint))
               : Mosaic.OnRightButtonUp(CursorPointToCell(clickPoint));
         }
      }

      public ClickResult MouseFocusLost() {
         if (Mosaic.CellDown == null)
            return null;
         bool isLeft = Mosaic.CellDown.State.Down; // hint: State.Down used only for the left click
         using (new Tracer(GetCallerName(), string.Format("CellDown.Coord={0}; isLeft={1}", Mosaic.CellDown.getCoord(), isLeft))) {
            return isLeft
               ? Mosaic.OnLeftButtonUp(null)
               : Mosaic.OnRightButtonUp(null);
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing) {
            Mosaic = null; // call setter - unsubscribe & dispose
            View = null; // call setter - unsubscribe & dispose
         }
      }

      static string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) { return "MosaicContrllr::" + callerName; }

   }

}
