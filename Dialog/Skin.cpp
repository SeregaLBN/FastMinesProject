////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Skin.cpp"
// обработка диалогового окна "Change skin"
// обработка диалогового окна "Image/Color/Font/Text ..."
// обработка диалогового окна "Save Skin" ("InputText")
////////////////////////////////////////////////////////////////////////////////
#include ".\Skin.h"
#include <windowsx.h>
#include <commctrl.h>
#include <stdio.h>
#include <tchar.h>
#include <math.h>
#include "..\ID_resource.h"
#include "..\EraseBk.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsSelectSkin {
////////////////////////////////////////////////////////////////////////////////
//                            types & variables
////////////////////////////////////////////////////////////////////////////////
//int resultDialog;
TsSkin Skin;
HWND hDlg;

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////

BOOL Cls_OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void Cls_OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void Cls_OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
void Cls_OnPaint     (HWND);                  // WM_PAINT
BOOL Cls_OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE    , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG    , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE   , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW  , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE, hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT    , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT     , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_BORDER        , hDlg, Skin)
WNDPROC_BUTTON(IDOK                                      , hDlg, Skin)
WNDPROC_BUTTON(IDCANCEL                                  , hDlg, Skin)
#endif // REPLACEBKCOLORFROMFILLWINDOW

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   //resultDialog = IDCANCEL;
   Skin = gpMosaic->GetSkin();

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE);
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_BORDER        );
   SETNEWWNDPROC(hwnd, IDOK                                      );
   SETNEWWNDPROC(hwnd, IDCANCEL                                  );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE:
      nsChangeSkin::SetName(nsChangeSkin::skinImageMine);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG:
      nsChangeSkin::SetName(nsChangeSkin::skinImageFlag);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE:
      nsChangeSkin::SetName(nsChangeSkin::skinImagePause);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW:
      nsChangeSkin::SetName(nsChangeSkin::skinImageBtnNew);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE:
      nsChangeSkin::SetName(nsChangeSkin::skinImageBtnPause);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND:
      nsChangeSkin::SetName(nsChangeSkin::skinImageBckgrnd);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND:
      nsChangeSkin::SetName(nsChangeSkin::skinColorBckgrnd);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT:
      nsChangeSkin::SetName(nsChangeSkin::skinColorText);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT:
      nsChangeSkin::SetName(nsChangeSkin::skinTypeFont);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_DIALOG_SELECTSKIN_BUTTON_BORDER:
      nsChangeSkin::SetName(nsChangeSkin::skinBorder);
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), hwnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case IDCANCEL:
      //resultDialog = IDCANCEL;
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      //resultDialog = IDOK;
      gpMosaic->SetSkin(Skin);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void Cls_OnPaint(HWND hwnd) {
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (Skin.toAll)
      nsEraseBk::FillWnd(hwnd, Skin.colorBk, false);
}

// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!Skin.toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, Skin.colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   EndDialog(hwnd, 0);//resultDialog);
}

TsSkin GetSkin() {
   return Skin;
}

void SetSkin(TsSkin newSkin) {
   Skin = newSkin;
}

HWND GetDialog() {
   return hDlg;
}

} //namespace nsSelectSkin


////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsChangeSkin {
////////////////////////////////////////////////////////////////////////////////
//                            types & variables
////////////////////////////////////////////////////////////////////////////////
//int resultDialog;
TsSkin Skin;
TsSkin::TsSkinImage* pSkinImage;
COLORREF* pSkinColor;
TeSkinName skinName;
TcImage* pImage;
HWND hDlg, hWndField, hWndSpin;

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////
BOOL Cls_OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void Cls_OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void Cls_OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
void Cls_OnPaint     (HWND);                  // WM_PAINT
BOOL Cls_OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW

LRESULT CALLBACK FieldWindowProc(HWND, UINT, WPARAM, LPARAM);
void AcceptSkinImage();

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_SELECT             , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_CENTER             , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_DILATE             , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_RESET              , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_CHANGESKIN_BUTTON_FRAME              , hDlg, Skin)
WNDPROC_BUTTON(IDOK                                           , hDlg, Skin)
WNDPROC_BUTTON(IDCANCEL                                       , hDlg, Skin)
WNDPROC_STATIC(ID_DIALOG_CHANGESKIN_BUTTON_PLACE              ,       Skin)
WNDPROC_STATIC(ID_DIALOG_CHANGESKIN_STATIC_WIDHT              ,       Skin)
WNDPROC_STATIC(ID_DIALOG_CHANGESKIN_SPIN_WIDHT                ,       Skin)
WNDPROC_STATIC(ID_DIALOG_CHANGESKIN_SPIN_TEXT                 ,       Skin)
#endif // REPLACEBKCOLORFROMFILLWINDOW

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   //resultDialog = IDCANCEL;
   hDlg = hwnd;

   if (skinName != skinBorder) {
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_STATIC_WIDHT  ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_EDIT_WIDHT    ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT    ), SW_HIDE);
   }
   switch (skinName) {
   case skinImageBtnNew  :
   case skinImageBtnPause:// EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), FALSE); break;
   case skinImageMine    :
   case skinImageFlag    : EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), FALSE);
   case skinImageBckgrnd : EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   ), FALSE); break;
   case skinColorText    :
   case skinBorder       :
   case skinTypeFont     :
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), SW_HIDE);
   case skinColorBckgrnd:
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_PLACE      ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER     ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE     ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE  ), SW_HIDE);
   }
   switch (skinName) {
   case skinImageMine    : SetWindowText(hDlg, TEXT("'Mine' image"          ));  break;
   case skinImageFlag    : SetWindowText(hDlg, TEXT("'Flag' image"          ));  break;
   case skinImagePause   : SetWindowText(hDlg, TEXT("'Pause' image"         ));  break;
   case skinImageBtnNew  : SetWindowText(hDlg, TEXT("'Game' button image"   ));  break;
   case skinImageBtnPause: SetWindowText(hDlg, TEXT("'Pause' button image"  ));  break;
   case skinImageBckgrnd : SetWindowText(hDlg, TEXT("Background image"      ));  break;
   case skinColorBckgrnd : SetWindowText(hDlg, TEXT("Background color"      ));  break;
   case skinBorder       : SetWindowText(hDlg, TEXT("Border color and width"));  break;
   case skinColorText    : SetWindowText(hDlg, TEXT("Character colors"      ));  break;
   case skinTypeFont     : SetWindowText(hDlg, TEXT("Font Type"             ));  break;
   }
   switch (skinName) {
   case skinImageMine    :
   case skinImageFlag    :
   case skinImageBtnNew  :
   case skinImageBtnPause:
   case skinImagePause   :
   case skinImageBckgrnd : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), TEXT("Image..."       ));break;
   case skinTypeFont     : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), TEXT("Font..."        )); break;
   case skinColorBckgrnd : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), TEXT("Apply to all windows" ));
   case skinColorText    : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), TEXT("Color..."       )); break;
   case skinBorder       : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), TEXT("Light  color..."));
                           SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), TEXT("Shadow color...")); break;
   }
   Skin = nsSelectSkin::GetSkin();

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL);
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_PLACE              );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_CENTER             );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_RESET              );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME              );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_STATIC_WIDHT              );
   SETNEWWNDPROC(hwnd, IDOK                                           );
   SETNEWWNDPROC(hwnd, IDCANCEL                                       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_SPIN_WIDHT                );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_CHANGESKIN_SPIN_TEXT                 ); // устанавлено ниже !!!
#endif // REPLACEBKCOLORFROMFILLWINDOW

   switch (skinName) {
   case skinImageMine    : pSkinImage = &Skin.Mine;        break;
   case skinImageFlag    : pSkinImage = &Skin.Flag;        break;
   case skinImageBtnNew  : pSkinImage = &Skin.BtnNew  [0]; break;
   case skinImageBtnPause: pSkinImage = &Skin.BtnPause[0]; break;
   case skinImagePause   : pSkinImage = &Skin.Pause;       break;
   case skinImageBckgrnd : pSkinImage = &Skin.Bckgrnd;     break;
   case skinColorBckgrnd : pSkinColor = &Skin.colorBk;     break;
   case skinBorder       :                                 break;
   case skinColorText    :                                 break;
   case skinTypeFont     :                                 break;
   }

   RegClass(
      0,                                  // UINT    style
      FieldWindowProc,                    // WNDPROC lpfnWndProc
      0,                                  // int     cbClsExtra
      0,                                  // int     cbWndExtra
      ghInstance,                         // HANDLE  c_hInstance
      (HICON)0,                           // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),        // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE),    // HBRUSH  hbrBackground
      NULL,                               // LPCTSTR lpszMenuName
      TEXT("classNameFastMinesSkinField") // LPCTSTR lpszClassName
   );
   RECT rect;
   GetWindowRect(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME), &rect);
   POINT PointOut = {rect.right -rect.left,
                     rect.bottom-rect.top};
   POINT PointIn = {10,10};
   switch (skinName) {
   case skinImagePause  :
   case skinImageBckgrnd:
   case skinColorBckgrnd: PointIn = gpMosaic->GetSizeBitmap();
   }
   rect = FindInnerRect(PointIn, PointOut);
   hWndField = CreateWindow(
      TEXT("classNameFastMinesSkinField"), NULL,
      WS_CHILD | WS_VISIBLE | WS_DLGFRAME,
      rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top,
      GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME), (HMENU)0, ghInstance, NULL);

   switch (skinName) {
   case skinImageBtnNew  :
      hWndSpin = CreateUpDownControl(
         WS_CHILD | WS_VISIBLE | UDS_ALIGNRIGHT | UDS_SETBUDDYINT,
         0,0,20,30,
         GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME),
         ID_DIALOG_CHANGESKIN_SPIN_TEXT,
         ghInstance,
         hWndField,
         3,0,0);
      defWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT = (WNDPROC)SetWindowLong(hWndSpin, GWL_WNDPROC, (LONG)newWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT);
      GetWindowRect(hWndField, &rect);
      SetWindowPos(hWndField, HWND_NOTOPMOST, 0,0, rect.right-rect.left+((rect.bottom-rect.top)-(rect.right-rect.left)), rect.bottom-rect.top, SWP_NOMOVE);
      pImage = new TcImage();
      AcceptSkinImage();
      break;
   case skinImageBtnPause:
      hWndSpin = CreateUpDownControl(
         WS_CHILD | WS_VISIBLE | UDS_ALIGNRIGHT | UDS_SETBUDDYINT,
         0,0,20,30,
         GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME),
         ID_DIALOG_CHANGESKIN_SPIN_TEXT,
         ghInstance,
         hWndField,
         3,0,0);
      defWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT = (WNDPROC)SetWindowLong(hWndSpin, GWL_WNDPROC, (LONG)newWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT);
      pImage = new TcImage();
      AcceptSkinImage();
      break;
   case skinImageMine    :
   case skinImageFlag    :
   case skinImagePause   :
   case skinImageBckgrnd :
      pImage = new TcImage();
      AcceptSkinImage();
      break;
   case skinColorBckgrnd:
      SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_SETCHECK, (WPARAM)(Skin.toAll ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
      break;
   case skinBorder      :
      SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETRANGE, 0L, MAKELPARAM(2, 1));
      SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS  , 0L, Skin.Border.width);
      break;
   case skinColorText    :
      hWndSpin = CreateUpDownControl(
         WS_CHILD | WS_VISIBLE | UDS_ALIGNRIGHT | UDS_SETBUDDYINT,
         0,0,20,30,
         GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_FRAME),
         ID_DIALOG_CHANGESKIN_SPIN_TEXT,
         ghInstance,
         hWndField,
         _21,_Unknown,_1);
      defWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT = (WNDPROC)SetWindowLong(hWndSpin, GWL_WNDPROC, (LONG)newWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT);
      break;
   }
   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_CHANGESKIN_EDIT_WIDHT:
      if (codeNotify == EN_CHANGE) {
         Skin.Border.width = SendDlgItemMessage(hwnd, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_GETPOS, 0L, 0L);
         if (Skin.Border.width > 4) {
            Skin.Border.width -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS, 0L, Skin.Border.width);
         }
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_SELECT2:
   case ID_DIALOG_CHANGESKIN_BUTTON_SELECT:
      switch (skinName) {
      case skinImageMine    :
      case skinImageFlag    :
      case skinImagePause   :
      case skinImageBtnNew  :
      case skinImageBtnPause:
      case skinImageBckgrnd : {
            OPENFILENAME OpenFileName;
            OpenFileName.lStructSize = sizeof(OPENFILENAME);
            OpenFileName.hwndOwner   = hDlg;
            OpenFileName.hInstance   = ghInstance;         // *.wmf;\n
            TCHAR strFilter[] = TEXT("All images\0*.bmp;*.ico;*.emf;\0Windows Bitmap (*.bmp)\0*.bmp\0Icon (*.ico)\0*.ico\0Enhanced Metafile (*.emf)\0*.emf\0\0");//Windows Metafile (*.wmf)\0*.wmf\0\0");//
            OpenFileName.lpstrFilter = strFilter;
            OpenFileName.lpstrCustomFilter = NULL;
          //OpenFileName.nMaxCustFilter;
            OpenFileName.nFilterIndex   = 0;
            TCHAR file[MAX_PATH] = TEXT("\0");
            if (_tcslen(pSkinImage->path))
               DelPathFromFullPath(_tcscpy(file, pSkinImage->path));
            OpenFileName.lpstrFile      = file;
            OpenFileName.nMaxFile       = MAX_PATH;
            OpenFileName.lpstrFileTitle = NULL;
          //OpenFileName.nMaxFileTitle;
            TCHAR dir[MAX_PATH];
            if (_tcslen(pSkinImage->path))
               DelFileFromFullPath(_tcscpy(dir, pSkinImage->path));
            else GetCurrentDirectory(MAX_PATH, dir);
            OpenFileName.lpstrInitialDir = dir;
            OpenFileName.lpstrTitle      = TEXT("Open image");
            OpenFileName.Flags           = OFN_EXPLORER | OFN_FILEMUSTEXIST | OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_PATHMUSTEXIST;
          //OpenFileName.nFileOffset;
          //OpenFileName.nFileExtension;
            OpenFileName.lpstrDefExt = NULL;
          //OpenFileName.lCustData;
          //OpenFileName.lpfnHook;
          //OpenFileName.lpTemplateName;
            if (GetOpenFileName(&OpenFileName)) {
               _tcscpy(pSkinImage->path, OpenFileName.lpstrFile);
               AcceptSkinImage();
            }
         }
         break;
      case skinBorder      :
         if (id == ID_DIALOG_CHANGESKIN_BUTTON_SELECT2)
              pSkinColor = &Skin.Border.shadow;
         else pSkinColor = &Skin.Border.light;
      case skinColorBckgrnd:
      case skinColorText   :
         {  COLORREF custColors[16];
            CHOOSECOLOR sChooseColor;
            sChooseColor.lStructSize  = sizeof(CHOOSECOLOR);
            sChooseColor.hwndOwner    = hDlg;
            sChooseColor.hInstance    = (HWND)ghInstance;
            sChooseColor.rgbResult    = *pSkinColor;
            sChooseColor.lpCustColors = custColors;
            sChooseColor.Flags        = CC_RGBINIT | CC_FULLOPEN | CC_SOLIDCOLOR;
          //sChooseColor.lCustData;
          //sChooseColor.lpfnHook;
          //sChooseColor.lpTemplateName;
            if (ChooseColor(&sChooseColor))
               (*pSkinColor) = sChooseColor.rgbResult;
         }
         break;
      case skinTypeFont:
         {
            CHOOSEFONT sChooseFont;
            LOGFONT LogFont = Skin.Font;
            sChooseFont.lStructSize = sizeof(CHOOSEFONT);
            sChooseFont.hwndOwner   = hDlg;
            sChooseFont.hDC         = NULL;
            sChooseFont.lpLogFont   = &LogFont;
            sChooseFont.iPointSize  = 0;
            sChooseFont.Flags       = CF_SCREENFONTS | CF_INITTOLOGFONTSTRUCT;// CF_BOTH | CF_SCREENFONTS | CF_INITTOLOGFONTSTRUCT | CF_NOSCRIPTSEL | CF_NOSTYLESEL | CF_NOSIZESEL | CF_NOVERTFONTS | CF_SCREENFONTS | CF_SELECTSCRIPT;// | CF_LIMITSIZE;
          //sChooseFont.rgbColors   = 0;
          //sChooseFont.lCustData
          //sChooseFont.lpfnHook
          //sChooseFont.lpTemplateName
            sChooseFont.hInstance   = ghInstance;
          //sChooseFont.lpszStyle
            sChooseFont.nFontType   = SCREEN_FONTTYPE;
          //sChooseFont.___MISSING_ALIGNMENT__
          //sChooseFont.nSizeMin
          //sChooseFont.nSizeMax
            if (ChooseFont(&sChooseFont)) 
               Skin.Font = LogFont;
         }
         break;
      }
      InvalidateRect(hDlg, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL:
      if (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_GETCHECK, 0L, 0L))
         if (skinName == skinColorBckgrnd) Skin.toAll = true;
            else pImage->SetTransparent(pSkinImage->transparent = true);
      else
         if (skinName == skinColorBckgrnd) Skin.toAll = false;
            else pImage->SetTransparent(pSkinImage->transparent = false);
      InvalidateRect(hDlg, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_CENTER:
   case ID_DIALOG_CHANGESKIN_BUTTON_DILATE:
   case ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE:
      pSkinImage->place = TePlace(id - ID_DIALOG_CHANGESKIN_BUTTON_CENTER);
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_RESET:
      switch (skinName) {
      case skinImageMine    : Skin.Mine        = gpMosaic->GetSkinDefault().Mine       ; break;
      case skinImageFlag    : Skin.Flag        = gpMosaic->GetSkinDefault().Flag       ; break;
      case skinImagePause   : Skin.Pause       = gpMosaic->GetSkinDefault().Pause      ; break;
      case skinImageBtnNew  : Skin.BtnNew  [0] = gpMosaic->GetSkinDefault().BtnNew  [0];
                              Skin.BtnNew  [1] = gpMosaic->GetSkinDefault().BtnNew  [1];
                              Skin.BtnNew  [2] = gpMosaic->GetSkinDefault().BtnNew  [2];
                              Skin.BtnNew  [3] = gpMosaic->GetSkinDefault().BtnNew  [3]; break;
      case skinImageBtnPause: Skin.BtnPause[0] = gpMosaic->GetSkinDefault().BtnPause[0];
                              Skin.BtnPause[1] = gpMosaic->GetSkinDefault().BtnPause[1];
                              Skin.BtnPause[2] = gpMosaic->GetSkinDefault().BtnPause[2];
                              Skin.BtnPause[3] = gpMosaic->GetSkinDefault().BtnPause[3]; break;
      case skinImageBckgrnd : Skin.Bckgrnd     = gpMosaic->GetSkinDefault().Bckgrnd    ; break;
      case skinColorBckgrnd : Skin.colorBk     = gpMosaic->GetSkinDefault().colorBk    ;
                              Skin.toAll       = gpMosaic->GetSkinDefault().toAll      ; break;
      case skinBorder       : Skin.Border      = gpMosaic->GetSkinDefault().Border     ; break;
      case skinColorText    : Skin.colorText   = gpMosaic->GetSkinDefault().colorText  ; break;
      case skinTypeFont     : Skin.Font        = gpMosaic->GetSkinDefault().Font       ; break;
      }
      switch (skinName) {
      case skinImageMine    : LoadDefaultImageMine    (ghInstance, *pImage); break;
      case skinImageFlag    : LoadDefaultImageFlag    (ghInstance, *pImage); break;
      case skinImagePause   : LoadDefaultImagePause   (ghInstance, *pImage); break;
      case skinImageBckgrnd : LoadDefaultImageBckgrnd (ghInstance, *pImage); break;
      case skinImageBtnNew  :
         switch (SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)) {
         case 0: LoadDefaultImageBtnNew0(ghInstance, *pImage); break;
         case 1: LoadDefaultImageBtnNew1(ghInstance, *pImage); break;
         case 2: LoadDefaultImageBtnNew2(ghInstance, *pImage); break;
         case 3: LoadDefaultImageBtnNew3(ghInstance, *pImage); break;
         }
         break;
      case skinImageBtnPause:
         switch (SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)) {
         case 0: LoadDefaultImageBtnPause0(ghInstance, *pImage); break;
         case 1: LoadDefaultImageBtnPause1(ghInstance, *pImage); break;
         case 2: LoadDefaultImageBtnPause2(ghInstance, *pImage); break;
         case 3: LoadDefaultImageBtnPause3(ghInstance, *pImage); break;
         }
         break;
      }
      switch (skinName) {
      case skinImageMine    :
      case skinImageFlag    :
      case skinImageBtnNew  :
      case skinImageBtnPause:
      case skinImagePause   :
      case skinImageBckgrnd : AcceptSkinImage(); break;
      case skinBorder       : SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS, 0L, Skin.Border.width); break;
      case skinColorBckgrnd : SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_SETCHECK, (WPARAM)(Skin.toAll ? BST_CHECKED : BST_UNCHECKED), 0L);
                              InvalidateRect(hDlg, NULL, TRUE);
                              break;
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case IDCANCEL:
      //resultDialog = IDCANCEL;
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      //resultDialog = IDOK;
      nsSelectSkin::SetSkin(Skin);
      InvalidateRect(nsSelectSkin::GetDialog(), NULL, FALSE);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   switch (skinName) {
   case skinImageBtnNew  :
   case skinImageBtnPause:
   case skinImageMine    :
   case skinImageFlag    :
   case skinImagePause   :
   case skinImageBckgrnd : delete pImage;
   }
   EndDialog(hwnd, 0);//resultDialog);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void Cls_OnPaint(HWND hwnd) {
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (Skin.toAll)
      nsEraseBk::FillWnd(hwnd, Skin.colorBk, false);
}

// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!Skin.toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, Skin.colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

LRESULT CALLBACK FieldWindowProc(HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   switch (Msg) {
   case WM_SETTEXT:
      DefWindowProc(hWnd, Msg, wParam, lParam);
      switch (skinName) {
      case skinImageBtnNew  : pSkinImage = &Skin.BtnNew  [SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)];  break;
      case skinImageBtnPause: pSkinImage = &Skin.BtnPause[SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)];  break;
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return 0;
   case WM_PAINT:
      {  
         PAINTSTRUCT PaintStruct;
         const HDC hDC = BeginPaint(hWnd, &PaintStruct);
         RECT Rect; GetClientRect(hWnd, &Rect);

         //HDC hCDC = GetWindowDC(GetDesktopWindow());
         const HBITMAP hBmp = CreateCompatibleBitmap(hDC, Rect.right,Rect.bottom);//CreateBitmap(Rect.right,Rect.bottom, 1, 32, NULL);//
         //ReleaseDC(GetDesktopWindow(), hCDC);

         const HDC hCDC = CreateCompatibleDC(NULL);
         const HGDIOBJ hOldBmp = SelectObject(hCDC, hBmp);
         {
            HBRUSH hBrushNew;
            switch (skinName) {
            case skinImageBtnNew  :
            case skinImageBtnPause:
               if (!Skin.toAll) {
                  hBrushNew = CreateSolidBrush(GetSysColor(COLOR_BTNFACE));
                  break;
               }
            default:
               hBrushNew = CreateSolidBrush(Skin.colorBk);
            }
            const HGDIOBJ hBrushOld = SelectObject(hCDC, hBrushNew);
            PatBlt(hCDC, 0,0, Rect.right,Rect.bottom, PATCOPY);
            SelectObject(hCDC, hBrushOld);
            DeleteObject(hBrushNew);
         }
         switch (skinName) {
         case skinImageBtnNew  :
         case skinImageBtnPause:
            AcceptSkinImage();
         case skinImageMine    :
         case skinImageFlag    :
         case skinImagePause   :
         case skinImageBckgrnd :
            pImage->DrawImagePlace(hCDC, &Rect, pSkinImage->place);
            break;
         case skinBorder     :
            {
               const float a = (float)Rect.right*4/10;
               const float h = a*(float)sqrt(3)/2;
               const float c = (float)Rect.right/2; // center
               const POINT p[6] = {{(int) c   ,(int)(c-a  )},
                                   {(int)(c+h),(int)(c-a/2)},
                                   {(int)(c+h),(int)(c+a/2)},
                                   {(int) c   ,(int)(c+a  )},
                                   {(int)(c-h),(int)(c+a/2)},
                                   {(int)(c-h),(int)(c-a/2)}};
               const HGDIOBJ hPenOld = SelectObject(hCDC, CreatePen(PS_SOLID, 4*Skin.Border.width, Skin.Border.shadow));
               MoveToEx(hCDC, p[0].x, p[0].y, NULL);
               LineTo  (hCDC, p[1].x, p[1].y);
               LineTo  (hCDC, p[2].x, p[2].y);
               LineTo  (hCDC, p[3].x, p[3].y);
               DeleteObject(SelectObject(hCDC, hPenOld));
               SelectObject(hCDC, CreatePen(PS_SOLID, 4*Skin.Border.width, Skin.Border.light));
               LineTo  (hCDC, p[4].x, p[4].y);
               LineTo  (hCDC, p[5].x, p[5].y);
               LineTo  (hCDC, p[0].x, p[0].y);
               DeleteObject(SelectObject(hCDC, hPenOld));
            }
            break;
         case skinColorText:
            {
               const int val = SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L);
               if (val) pSkinColor = &Skin.colorText.captionOpen [val];
                  else  pSkinColor = &Skin.colorText.captionClose[val];
               SetTextColor(hCDC, *pSkinColor);
               SetBkMode   (hCDC, TRANSPARENT);
               GetClientRect(hWnd, &Rect);
               Skin.Font.lfHeight = Rect.bottom;
               const HGDIOBJ hFontOld = SelectObject(hCDC, CreateFontIndirect(&Skin.Font));
               TCHAR str[3] = {TEXT('\0'), TEXT('\0'), TEXT('\0')};
               if (val) {
                  str[0] = nsFigure::CCaptionOpen [val][0];
                  str[1] = nsFigure::CCaptionOpen [val][1];
               } else {
                  str[0] = nsFigure::CCaptionClose[val];
               }
               DrawText(hCDC, str, -1, &Rect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
               DeleteObject(SelectObject(hCDC, hFontOld));
            }
            break;
         case skinTypeFont:
            {
               const TCHAR str [] = TEXT("\n\nFastMines - best\nMinesweeper clone!\n\n0 1 2 3 4 5 6 7 8 9 ?");
               const TCHAR str1[] = TEXT("FastMines - best");
               const TCHAR str2[] = TEXT("Minesweeper clone!");
               const TCHAR str3[] = TEXT("0 1 2 3 4 5 6 7 8 9 ?");
               SIZE size1, size2, size3;

               SetBkMode   (hCDC, TRANSPARENT);
               SetTextColor(hCDC, RGB(0, 0, 0));

               GetClientRect(hWnd, &Rect);
               HGDIOBJ hFontOld;
               Skin.Font.lfHeight = 0;
               do {
                  Skin.Font.lfHeight++;
                  hFontOld = SelectObject(hCDC, CreateFontIndirect(&Skin.Font));

                  GetTextExtentPoint32(hCDC, str1, _tcslen(str1), &size1);
                  GetTextExtentPoint32(hCDC, str2, _tcslen(str2), &size2);
                  GetTextExtentPoint32(hCDC, str3, _tcslen(str3), &size3);

                  DeleteObject(SelectObject(hCDC, hFontOld));
               } while (max(max(size1.cx, size2.cx), size3.cx) < (Rect.right-Rect.left));
               Skin.Font.lfHeight--;
               hFontOld = SelectObject(hCDC, CreateFontIndirect(&Skin.Font));
               DrawText(hCDC, str, -1, &Rect, DT_CENTER); // ExtTextOut(hCDC, 0,0, ETO_CLIPPED, &Rect, str1, strlen(str1), NULL);
               DeleteObject(SelectObject(hCDC, hFontOld));
            }
            break;
         }
         BitBlt(hDC, 0,0, Rect.right,Rect.bottom,
                hCDC,0,0, SRCCOPY);
         SelectObject(hCDC, hOldBmp);
         DeleteDC(hCDC);
         DeleteObject(hBmp);
         EndPaint(hWnd, &PaintStruct);
         return 0;
      }
   }
   return DefWindowProc(hWnd, Msg, wParam, lParam);
}

void AcceptSkinImage(){
   pImage->SetTransparent(pSkinImage->transparent);
   bool isLoad = false;
   if (_tcscmp(pSkinImage->path,TEXT("")) > 0) {
      isLoad = pImage->LoadFile(pSkinImage->path);
      if (!isLoad)
         MessageBox(hDlg, TEXT("CouldnТt open image.\nThe default image has been restored."), TEXT("Error"), MB_OK | MB_ICONERROR);
   }
   if (!isLoad)
      switch (skinName) {
      case skinImageMine    : LoadDefaultImageMine    (ghInstance, *pImage); break;
      case skinImageFlag    : LoadDefaultImageFlag    (ghInstance, *pImage); break;
      case skinImagePause   : LoadDefaultImagePause   (ghInstance, *pImage); break;
      case skinImageBckgrnd : LoadDefaultImageBckgrnd (ghInstance, *pImage); break;
      case skinImageBtnNew  :
         switch (SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)) {
         case 0: LoadDefaultImageBtnNew0(ghInstance, *pImage); break;
         case 1: LoadDefaultImageBtnNew1(ghInstance, *pImage); break;
         case 2: LoadDefaultImageBtnNew2(ghInstance, *pImage); break;
         case 3: LoadDefaultImageBtnNew3(ghInstance, *pImage); break;
         }
         break;
      case skinImageBtnPause:
         switch (SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)) {
         case 0: LoadDefaultImageBtnPause0(ghInstance, *pImage); break;
         case 1: LoadDefaultImageBtnPause1(ghInstance, *pImage); break;
         case 2: LoadDefaultImageBtnPause2(ghInstance, *pImage); break;
         case 3: LoadDefaultImageBtnPause3(ghInstance, *pImage); break;
         }
         break;
      }

   if (skinName == skinImageBckgrnd)
      if (pImage->GetImageType() == imageUnknown) {
         // disable all
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), FALSE);
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE     ), FALSE);
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE  ), FALSE);
      } else {
         // enable all
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), TRUE);
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE     ), TRUE);
         EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE  ), TRUE);
      }

   if (pImage->GetImageType() != imageBitmap) {
      pImage->SetTransparent(pSkinImage->transparent = true);
      SendDlgItemMessage     (hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_SETCHECK, (WPARAM)BST_CHECKED, (LPARAM)0);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), FALSE);
   } else {
      SendDlgItemMessage     (hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_SETCHECK, (WPARAM)((pSkinImage->transparent)? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), TRUE);
   }
   // unchech all radiobuttons
   SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   , BM_SETCHECK, (WPARAM)BST_UNCHECKED, (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE   , BM_SETCHECK, (WPARAM)BST_UNCHECKED, (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE, BM_SETCHECK, (WPARAM)BST_UNCHECKED, (LPARAM)0);
   // chech radiobutton
   SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER+pSkinImage->place, (WPARAM)BM_SETCHECK, (WPARAM)BST_CHECKED, (LPARAM)0);
}

void SetName(TeSkinName newName){
   skinName = newName;
}

} //namespace nsChangeSkin

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsFileSkin {
////////////////////////////////////////////////////////////////////////////////
//                            types & variables
////////////////////////////////////////////////////////////////////////////////
HWND hDlg;

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////

BOOL Cls_OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void Cls_OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void Cls_OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
BOOL Cls_OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_TEXT  , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_TEXT, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK                     , hDlg, gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT);
   SETNEWWNDPROC(hwnd, IDOK                           );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   SetWindowText(hwnd, TEXT("Save skin"));
   SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), TEXT("File name"));

   MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);

   RECT rect = {0,0,241,116};
   const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                             GetSystemMetrics(SM_CYSCREEN)};
   MoveWindow(hwnd,
      sizeScreen.x/2 - (rect.right -rect.left)/2,
      sizeScreen.y/2 - (rect.bottom-rect.top )/2,
      rect.right -rect.left,
      rect.bottom-rect.top,
      FALSE);

   return TRUE;
}

#define SKINNAMELENGTH 30
struct TsFileSkin {
   TCHAR szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   TsSkin Skin;
};

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case IDOK:
      TCHAR szSkin[SKINNAMELENGTH];
      if (!GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szSkin, SKINNAMELENGTH)) {
         MessageBox(hwnd, TEXT("Invalid name. Please, retype."), TEXT("Error"), MB_ICONSTOP | MB_OK);
         return;
      }
      TCHAR szPath[MAX_PATH];
      { // directory exist?
         GetModuleFileName(ghInstance, szPath, MAX_PATH);
         DelFileFromFullPath(szPath);
         _tcscat(szPath, TEXT("\\Skin"));
         CreateDirectory(szPath, NULL); // каталог создастс€ если не существовал
      }
      { // подготовка данных к сохранению
         TsFileSkin file = {TEXT(ID_VERSIONINFO_VERSION3)};
         file.Skin = gpMosaic->GetSkin();
         TCHAR szPath2[MAX_PATH];
         _tcscat(_tcscat(_tcscpy(szPath2, szPath), TEXT("\\")), szSkin);
         CreateDirectory(szPath2, NULL); // каталог дл€ рисунков данного скина
         { // преобразую абсолютные пути на рисунки в относительные и копирую рисунки
            SetCursor(LoadCursor(NULL, IDC_WAIT));
            TCHAR szOldPath[MAX_PATH];
            TCHAR szNewPath[MAX_PATH];

#define CHANGE_PATH_AND_COPY(path)  \
            _tcscpy(szOldPath, path); \
            if (_tcslen(szOldPath)) {  \
               DelPathFromFullPath(path);   \
               _tcscat(_tcscat(_tcscpy(szNewPath, szPath2), TEXT("\\")), path); \
               CopyFile(szOldPath, szNewPath, FALSE); \
            }

            CHANGE_PATH_AND_COPY(file.Skin.Mine       .path)
            CHANGE_PATH_AND_COPY(file.Skin.Flag       .path)
            CHANGE_PATH_AND_COPY(file.Skin.Pause      .path)
            CHANGE_PATH_AND_COPY(file.Skin.Bckgrnd    .path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnNew  [0].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnNew  [1].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnNew  [2].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnNew  [3].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnPause[0].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnPause[1].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnPause[2].path)
            CHANGE_PATH_AND_COPY(file.Skin.BtnPause[3].path)
#undef CHANGE_PATH_AND_COPY

            SetCursor(LoadCursor(NULL, IDC_ARROW));
         }
         TCHAR szFile[MAX_PATH];
         _tcscat(_tcscpy(szFile, szPath2), TEXT(".skn"));
         HANDLE hFile;
         { // file exist?
            hFile = CreateFile(
               szFile,
               GENERIC_READ,
               0,
               NULL,
               OPEN_EXISTING,
               FILE_ATTRIBUTE_NORMAL,
               NULL
            );
            if (hFile != INVALID_HANDLE_VALUE) {
               CloseHandle(hFile);
               if (IDNO == MessageBox(hDlg, TEXT("File exists. Overwrite?"), TEXT("File copying"), MB_ICONQUESTION | MB_YESNO | MB_DEFBUTTON2))
                  return;
            }
         }
         { // save file
            hFile = CreateFile(
               szFile,
               GENERIC_WRITE,
               0,
               NULL,
               CREATE_ALWAYS,
               FILE_ATTRIBUTE_NORMAL,
               NULL
            );
            if (hFile != INVALID_HANDLE_VALUE) {
               DWORD dwNOBW;
               BOOL result = WriteFile(hFile, &file,
                   sizeof(file), &dwNOBW, NULL);
               if (result && (sizeof(file) != dwNOBW)) result = FALSE;
               if (!result) MessageBox(hDlg, TEXT("Can't write file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
               CloseHandle(hFile);
            } else
               MessageBox(hDlg, TEXT("Can't create file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
         }
      }
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpMosaic->GetSkin().toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, gpMosaic->GetSkin().colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
}

TsSkin LoadSkin(HMENU hMenu, UINT id) {
   // определ€ю им€ skin'a
   TCHAR szName[MAX_PATH];
   GetMenuString(hMenu, id, szName, MAX_PATH, MF_BYCOMMAND);
   // load skin
   TCHAR szPath[MAX_PATH];
   GetModuleFileName(ghInstance, szPath, MAX_PATH);
   DelFileFromFullPath(szPath);
   _tcscat(_tcscat(szPath, TEXT("Skin\\")), szName);
   TCHAR szFile[MAX_PATH];
   _tcscpy(szFile, szPath);
   _tcscat(szFile, TEXT(".skn"));

   TsFileSkin file;
   HANDLE hFile = CreateFile(
      szFile,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   BOOL result = FALSE;
   if (hFile != INVALID_HANDLE_VALUE) {
      DWORD dwNOBR;
      result = ReadFile(hFile, &file, sizeof(file), &dwNOBR, NULL);
      if (sizeof(file) != dwNOBR) result = FALSE;
      if (result) {
         if (_tcscmp(file.szVersion, TEXT(ID_VERSIONINFO_VERSION3))) {
            MessageBox(hDlg, TEXT("SKN file - version error"), TEXT("Information"), MB_ICONINFORMATION | MB_OK);
            result = FALSE;
         }
      } else
         MessageBox(hDlg, TEXT("Can't load file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
      CloseHandle(hFile);
   }
   if (result) {
      { // преобразую относительные пути в абсолютные
         TCHAR szAbsPath[MAX_PATH];
#define CHANGE_PATH(path)  \
         if (_tcslen(path))   \
            _tcscpy(path, _tcscat(_tcscat(_tcscpy(szAbsPath, szPath), TEXT("\\")), path));
         CHANGE_PATH(file.Skin.Mine       .path)
         CHANGE_PATH(file.Skin.Flag       .path)
         CHANGE_PATH(file.Skin.Pause      .path)
         CHANGE_PATH(file.Skin.Bckgrnd    .path)
         CHANGE_PATH(file.Skin.BtnNew  [0].path)
         CHANGE_PATH(file.Skin.BtnNew  [1].path)
         CHANGE_PATH(file.Skin.BtnNew  [2].path)
         CHANGE_PATH(file.Skin.BtnNew  [3].path)
         CHANGE_PATH(file.Skin.BtnPause[0].path)
         CHANGE_PATH(file.Skin.BtnPause[1].path)
         CHANGE_PATH(file.Skin.BtnPause[2].path)
         CHANGE_PATH(file.Skin.BtnPause[3].path)
#undef CHANGE_PATH
      }
      return file.Skin;
   } else {
      MessageBox(hDlg, TEXT("The default skin has been restored"), TEXT("Error"), MB_ICONSTOP | MB_OK);
      return gpMosaic->GetSkinDefault();
   }
}

void LoadSkinList(HMENU hMenu) {
   TCHAR szPath[MAX_PATH];
   GetModuleFileName(ghInstance, szPath, MAX_PATH);
   DelFileFromFullPath(szPath);
   _tcscat(szPath, TEXT("\\Skin\\*.skn"));
   WIN32_FIND_DATA findFileData;
   memset(&findFileData, 0, sizeof(findFileData));
   HANDLE h = FindFirstFile(szPath, &findFileData);
   if (h != INVALID_HANDLE_VALUE) {
      MENUITEMINFO mii = { sizeof(MENUITEMINFO),             // cbSize
                           MIIM_TYPE | MIIM_STATE | MIIM_ID, // fMask
                           MFT_STRING,                       // fType
                           MFS_ENABLED,                      // fState
                           0,                                // wID
                           NULL,                             // hSubMenu
                           NULL,                             // hbmpChecked
                           NULL,                             // hbmpUnchecked
                           0,                                // dwItemData
                           findFileData.cFileName,           // dwTypeData
                           MAX_PATH };                       // cch
      int i=1;
      do {
         mii.wID = ID_MENU_OPTIONS_SKIN_LOAD0+i;
         findFileData.cFileName[_tcslen(findFileData.cFileName)-4] = TEXT('\0');
         InsertMenuItem(hMenu, i, TRUE, &mii);
         i++;
      } while (FindNextFile(h, &findFileData));
      FindClose(h);
   }
}

void ReloadSkinList(HMENU hMenu){
   int count = GetMenuItemCount(hMenu);
   if (count == -1) return;
   if (count>1)
      for (int i=1; i<=count; i++)
         RemoveMenu(hMenu, 1, MF_BYPOSITION);
   LoadSkinList(hMenu);
}

} // namespace nsFileSkin
