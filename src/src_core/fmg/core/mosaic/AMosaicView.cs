﻿using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using fmg.common;
using fmg.common.notyfier;
using fmg.common.geom;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.data.view.draw;

namespace fmg.core.mosaic {

   /// <summary> MVC: view. Base mosaic view implementation </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImage2">image type of flag/mine into mosaic field</typeparam>
   /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
   public abstract class AMosaicView<TImage, TImage2, TMosaicModel>
                         : ImageView<TImage, TMosaicModel>,
                         IMosaicView<TImage, TImage2, TMosaicModel>
      where TImage : class
      where TImage2 : class
      where TMosaicModel : MosaicDrawModel<TImage2>>
   {

      protected AMosaicView(TMosaicModel mosaicModel)
         : super(mosaicModel)
      { }

      public static boolean _DEBUG_DRAW_FLOW = false;
      private readonly Set<BaseCell> _modifiedCells = new HashSet<BaseCell>();

      public override void Invalidate(IEnumerable<BaseCell> modifiedCells) {
         if (modifiedCells == null) // mark NULL if all mosaic is changed
            _modifiedCells.Clear();
         else
            _modifiedCells.AddAll(modifiedCells);
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("AMosaicView.Invalidate: " + ((modifiedCells==null) ? "all" : ("cnt=" + modifiedCells.Count) + ": " + modifiedCells.Limit(5).ToList()));
         Invalidate();
      }

      /// <summary> repaint all </summary>
      protected override void DrawBody() {
         if (_DEBUG_DRAW_FLOW)
            LoggerSimple.Put("AMosaicView.DrawBody: " + (!_modifiedCells.Any() ? "all" : ("cnt=" + _modifiedCells.Count) + ": " + _modifiedCells.Limit(5).ToList()));
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
         case nameof(MosaicDrawModel.PenBorder):
            ChangeFontSize();
            break;
         }
      }

      /// <summary> пересчитать и установить новую высоту шрифта </summary>
      private void ChangeFontSize() {
         var model = Model;
         var penBorder = model.PenBorder;
         model.FontInfo.Size = model.CellAttr.getSq((int)penBorder.Width);
      }

   }

}
