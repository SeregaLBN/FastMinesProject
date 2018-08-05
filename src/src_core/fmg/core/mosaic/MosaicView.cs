﻿using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> MVC: view. Base mosaic view implementation </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
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

      public static bool _DEBUG_DRAW_FLOW = false;
      private readonly HashSet<BaseCell> _modifiedCells = new HashSet<BaseCell>();

      public virtual void Invalidate(IEnumerable<BaseCell> modifiedCells) {
         if (modifiedCells == null) // mark NULL if all mosaic is changed
            _modifiedCells.Clear();
         else
            _modifiedCells.UnionWith(modifiedCells);
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("MosaicView.Invalidate: " + ((modifiedCells==null) ? "all" : ("cnt=" + modifiedCells.Count()) + ": " + modifiedCells.Take(5).ToList()));
         Invalidate();
      }

      public abstract void Draw(IEnumerable<BaseCell> modifiedCells);

      /// <summary> repaint all </summary>
      protected override void DrawBody() {
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("MosaicView.DrawBody: " + (!_modifiedCells.Any() ? "all" : ("cnt=" + _modifiedCells.Count()) + ": " + _modifiedCells.Take(5).ToList()));
         Draw(!_modifiedCells.Any() ? null : _modifiedCells);
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
