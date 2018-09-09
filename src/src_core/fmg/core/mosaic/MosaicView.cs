﻿using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> MVC: view. Base mosaic view implementation </summary>
   /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
   /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
   public abstract class MosaicView<TImage, TImageInner, TMosaicModel>
                        : ImageView<TImage, TMosaicModel>,
                        IMosaicView<TImage, TImageInner, TMosaicModel>
      where TImage : class
      where TImageInner : class
      where TMosaicModel : MosaicDrawModel<TImageInner>
   {

      protected MosaicView(TMosaicModel mosaicModel)
         : base(mosaicModel)
      { }

#if DEBUG
      public static bool _DEBUG_DRAW_FLOW = false;
#endif
      private readonly HashSet<BaseCell> _modifiedCells = new HashSet<BaseCell>();

      public virtual void Invalidate(ICollection<BaseCell> modifiedCells) {
         if (modifiedCells == null) // mark NULL if all mosaic is changed
            _modifiedCells.Clear();
         else
            _modifiedCells.UnionWith(modifiedCells);
#if DEBUG
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("MosaicView.Invalidate: " + ((modifiedCells==null) ? "all" : ("cnt=" + modifiedCells.Count) + ": " + modifiedCells.Take(5).ToList()));
#endif
         Invalidate();
      }

      /// <summary>Draw modified mosaic cells</summary>
      /// <param name="requiredCells">Cells to be redrawn. NULL - redraw the full mosaic.</param>
      protected abstract void DrawModified(ICollection<BaseCell> requiredCells);

      /// <summary> repaint all </summary>
      protected override void DrawBody() {
#if DEBUG
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("MosaicView.DrawBody: " + (!_modifiedCells.Any() ? "all" : ("cnt=" + _modifiedCells.Count) + ": " + _modifiedCells.Take(5).ToList()));
#endif
         DrawModified(!_modifiedCells.Any() ? null : _modifiedCells);
         _modifiedCells.Clear();
      }

      protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyModelChanged(sender, ev);
         switch (ev.PropertyName) {
         case nameof(MosaicGameModel.MosaicType):
            ChangeFontSize();
            break;
         case nameof(MosaicGameModel.Area):
            ChangeFontSize();
            break;
         case nameof(MosaicDrawModel<TImageInner>.PenBorder):
            ChangeFontSize();
            break;
         }
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize() {
         var model = Model;
         var penBorder = model.PenBorder;
         model.FontInfo.Size = model.CellAttr.GetSq(penBorder.Width);
      }

   }

}
