using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI;
using Windows.UI.Popups;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Shapes;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.view.win_rt.draw;
using ua.ksn.fmg.view.win_rt.draw.mosaics;
using ua.ksn.fmg.view.swing.draw.mosaics;
using System.ComponentModel;
using ua.ksn.fmg.view.draw;

namespace ua.ksn.fmg.controller.win_rt {
   public class MosaicExt : Mosaic {
      private IDictionary<BaseCell, Tuple<Polygon, TextBlock, Image>> _xamlBinder;
      private MosaicGraphicContext _gContext;
      private CellPaint _cellPaint;
      private Panel _container;

      public MosaicExt() {}

      public MosaicExt(Size sizeField, EMosaic mosaicType, int minesCount, int area) :
         base(sizeField, mosaicType, minesCount, area) {
         BindXamlToMosaic();
      }

      private void BindXamlToMosaic() {
         var sizeMosaic = Cells.Size;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++) {
               var cell = base.getCell(i, j);
               var shape = new Polygon();
               var txt = new TextBlock();
               var img = new Image();
               XamlBinder.Add(cell, new Tuple<Polygon, TextBlock, Image>(shape, txt, img));
               Container.Children.Add(shape);
               Container.Children.Add(txt);
               Container.Children.Add(img);
            }
      }

      protected class MatrixCellsExt : MatrixCells {
         public MatrixCellsExt(Mosaic mosaic) : base(mosaic) {}

         protected override void OnError(string msg) {
#if DEBUG
            System.Diagnostics.Debug.Assert(false, msg);
#else
				base.OnError(msg);
#endif
         }

         public override void setParams(Size? newSizeField, EMosaic? newMosaicType, int? newMinesCount) {
            var mosaic = _mosaic as MosaicExt;
            System.Diagnostics.Debug.Assert(mosaic != null);

            if (this._mosaicType != newMosaicType)
               mosaic._cellPaint = null;

            base.setParams(newSizeField, newMosaicType, newMinesCount);

            mosaic.Repaint();
            //mosaic.Container.InvalidateArrange(); // Revalidate();
         }
      }

      public Panel Container {
         get { return _container ?? (_container = new Canvas()); }
      }

      protected override MatrixCells Cells {
         get { return _cells ?? (_cells = new MatrixCellsExt(this)); }
      }

      public MosaicGraphicContext GraphicContext {
         get {
            if (_gContext == null) {
               _gContext = new MosaicGraphicContext();
               //changeFontSize(_gContext.PenBorder, Area);
               _gContext.PropertyChanged += OnPropertyChange; // изменение контекста -> перерисовка мозаики
            }
            return _gContext;
         }
      }

      public CellPaint CellPaint {
         get { return _cellPaint ?? (_cellPaint = new CellPaint(GraphicContext)); }
      }

      private IDictionary<BaseCell, Tuple<Polygon, TextBlock, Image>> XamlBinder {
         get { return _xamlBinder ?? (_xamlBinder = new Dictionary<BaseCell, Tuple<Polygon, TextBlock, Image>>()); }
      }

      protected override void NeedRepaint(BaseCell cell) {
         if (!XamlBinder.Any()) // TODO избавиться
            return;
         if (cell == null)
            Repaint();
         else
            CellPaint.Paint(cell, XamlBinder[cell]);
      }

      public void Repaint() {
         RepaintComponent(true);
      }

      private void RepaintComponent(bool areaChanged) {
         if (!XamlBinder.Any()) // TODO избавиться
            return;
         var sizeMosaic = base.Cells.Size;
         for (var i = 0; i < sizeMosaic.width; i++)
            for (var j = 0; j < sizeMosaic.height; j++) {
               var cell = base.getCell(i, j);
               CellPaint.Paint(cell, XamlBinder[cell]);
            }
      }

      protected override void GameBegin(Coord firstClick) {
         GraphicContext.BkFill.Mode = 0;
         base.GameBegin(firstClick);
      }

      /// <summary> преобразовать экранные координаты в координаты mosaic'a </summary>
      private Coord CursorPointToMosaicCoord(Point point) {
         throw new NotImplementedException();
      }

      protected override async Task<bool> RequestToUser_RestoreLastGame() {
         var dlg = new MessageDialog("Restore last game?", "Question");
         const string okLabel = "Ok";
         dlg.Commands.Add(new UICommand(okLabel));
         dlg.Commands.Add(new UICommand("Cancel"));
         dlg.DefaultCommandIndex = 0;
         dlg.CancelCommandIndex = 1;

         var cmd = await dlg.ShowAsync();
         return okLabel.Equals(cmd.Label);
      }

      public override int Area {
         //get { return base.Area; }
         set {
            var oldVal = Area;
            base.Area = value;
            var newVal = Area;
            if (oldVal != newVal) {
               // см. комент - сноску 1
               ChangeFontSize(GraphicContext.PenBorder, value);
               Repaint();
            }
         }
      }

      public override async Task GameNew() {
         var rnd = Windows.Security.Cryptography.CryptographicBuffer.GenerateRandomNumber();
         var mode = 1 + rnd%CellFactory.CreateAttributeInstance(MosaicType, Area).getMaxBackgroundFillModeValue();
         GraphicContext.BkFill.Mode = (int) mode;
         await base.GameNew();
         Repaint();
      }

   
	    public void MousePressed(Point clickPoint, bool isLeftMouseButton, bool isRightMouseButton) {
			if (isLeftMouseButton)
				OnLeftButtonDown(CursorPointToMosaicCoord(clickPoint));
			else
			if (isRightMouseButton)
				OnRightButtonDown(CursorPointToMosaicCoord(clickPoint));
		}

		public void MouseReleased(Point clickPoint, bool isLeftMouseButton, bool isRightMouseButton) {
			if (isLeftMouseButton) {
            var frame = (Frame)Windows.UI.Xaml.Window.Current.Content;
            var page = (Windows.UI.Xaml.Controls.Page)frame.Content;
	    		var rootFrameActive = _container.Parent == page;
	    		if (rootFrameActive)
	    			OnLeftButtonUp(CursorPointToMosaicCoord(clickPoint));
			} else
			if (isRightMouseButton)
            OnRightButtonUp(/*CursorPointToMosaicCoord(clickPoint)*/);
	    }

		public void MouseFocusLost() {
         //System.Diagnostics.Debug.WriteLine("Mosaic::MosaicFocusLost: ");
			if (CoordDown != Coord.INCORRECT_COORD)
				OnLeftButtonUp(Coord.INCORRECT_COORD);
		}

      private void OnPropertyChange(object sender, PropertyChangedEventArgs e) {
		   if ((sender is GraphicContext) && "PenBorder".Equals(e.PropertyName)) {
			   // см. комент - сноску 1
		      var gc = sender as GraphicContext;
			   ChangeFontSize(gc.PenBorder, Area);
		   }

		   if (sender is GraphicContext)
			   Repaint();
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      public void ChangeFontSize() { ChangeFontSize(GraphicContext.PenBorder, Area); }
      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize(PenBorder penBorder, int area) {
         GraphicContext.FontSize = (int)CellAttr.CalcSq(area, penBorder.Width);
      }
   }
}