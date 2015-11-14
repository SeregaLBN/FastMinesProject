﻿using System;
using fmg.core.types;
using FastMines.DataModel.Items;

namespace FastMines.Presentation.Menu {

   public class MenuItem : MosaicGroupDataItem {

      public MenuItem(EMosaicGroup eMosaicGroup) :
         base(eMosaicGroup)
      { }

      private Type _pageType;
      public Type PageType {
         get { return _pageType; }
         set { SetProperty(ref _pageType, value); }
      }

   }
}
