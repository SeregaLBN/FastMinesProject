////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Skin.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_SKIN
#define FILE_SKIN

#include "..\Preproc.h"
#include <windows.h>
#include "..\TcMosaic.h"

namespace nsSelectSkin {

   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);

   HWND   GetDialog();
   TsSkin GetSkin();
   void   SetSkin(TsSkin);
}

namespace nsChangeSkin {

   enum TeSkinName {
      skinImageMine,
      skinImageFlag,
      skinImagePause,
      skinImageBtnNew,
      skinImageBtnPause,
      skinImageBckgrnd,
      skinColorBckgrnd,
      skinBorder,
      skinColorText,
      skinTypeFont
   };

   void SetName(TeSkinName);
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
}

namespace nsFileSkin {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void LoadSkinList  (HMENU);
   void ReloadSkinList(HMENU);
   TsSkin LoadSkin    (HMENU, UINT);
}

#endif // FILE_SKIN
