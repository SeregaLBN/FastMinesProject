////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Skin.cpp"
// обработка диалогового окна "Change skin"
// обработка диалогового окна "Image/Color/Font/Text ..."
// обработка диалогового окна "Save Skin" ("InputText")
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Skin.h"
#include <WindowsX.h>
#include <CommCtrl.h>
#include <math.h>
#include "../ID_resource.h"
#include "../EraseBk.h"
#include "../Lang.h"
#include "../OldVersion.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

namespace nsChangeSkin {

   enum ESkinName {
      skinImageMine,
      skinImageFlag,
      skinImagePause,
      skinImageBtnPause,
      skinImageBtnNew,
      skinImageBckgrnd,
      skinColorBckgrnd,
      skinBorder,
      skinColorText,
      skinTypeFont
   };
}

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsChangeSkin {
////////////////////////////////////////////////////////////////////////////////
//                            types & variables
////////////////////////////////////////////////////////////////////////////////
CSkin       Skin;
CImageMini *pImageData;
COLORREF   *pSkinColor;
ESkinName   skinName = skinImageMine;
CImage     *pImage = NULL;
HWND hDlg, hWndField, hWndSpin=NULL;

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////
BOOL OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
void OnPaint     (HWND);                  // WM_PAINT
BOOL OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW

LRESULT CALLBACK FieldWindowProc(HWND, UINT, WPARAM, LPARAM);
void AcceptImage(bool bApplyDefaultImage);
inline void LoadDefaultImage();

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
inline void local_DeleteImage() {
   if (pImage) {
      delete pImage;
      pImage = NULL;
   }
}

inline void InvalidateFrame() {
   RECTEX redraw = ::GetWindowRect(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_FRAME));
   redraw = ::ScreenToClient(hDlg, redraw);
   ::InvalidateRect(hDlg, &redraw, TRUE);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE         , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG         , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE        , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND      , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW       , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE     , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND      , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT         , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT          , hDlg, Skin)
WNDPROC_BUTTON(ID_DIALOG_SELECTSKIN_BUTTON_BORDER             , hDlg, Skin)
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
   //return FALSE;
   //g_Logger.PutMsg(TEXT("Skin: "), msg);
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_EX_CTLCOLOR(hDlg, Skin);
   }
   return FALSE;
}

void ReInitDialog() {
   if (hWndSpin) {
      DestroyWindow(hWndSpin);
      hWndSpin = NULL;
   }

   SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_TRANSPARENT]);
   switch (skinName) {
   case skinImageMine    :
   case skinImageFlag    :
   case skinImageBtnNew  :
   case skinImageBtnPause:
   case skinImagePause   :
   case skinImageBckgrnd : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_IMAGE       ]); break;
   case skinTypeFont     : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_FONT        ]); break;
   case skinColorBckgrnd : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_APPLY_TO_ALL]);
   case skinColorText    : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_COLOR       ]); break;
   case skinBorder       : SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT             ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_LIGHT_COLOR ]);
                           SetWindowText(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_SHADOW_COLOR]); break;
   }

   switch (skinName) {
   case skinImageMine:
   case skinImageFlag:
   case skinImagePause:
   case skinImageBtnPause:
   case skinImageBtnNew:
   case skinImageBckgrnd:
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_PLACE              ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER             ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          ), SW_SHOW);

      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_STATIC_WIDHT              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_EDIT_WIDHT                ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT                ), SW_HIDE);
      break;
   case skinColorBckgrnd:
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), SW_SHOW);

      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_PLACE              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_STATIC_WIDHT              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_EDIT_WIDHT                ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT                ), SW_HIDE);
      break;
   case skinBorder:
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_PLACE              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          ), SW_HIDE);

      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_STATIC_WIDHT              ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_EDIT_WIDHT                ), SW_SHOW);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT                ), SW_SHOW);
      break;
   case skinColorText:
   case skinTypeFont:
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_PLACE              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_SELECT2            ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_STATIC_WIDHT              ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_EDIT_WIDHT                ), SW_HIDE);
      ShowWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT                ), SW_HIDE);
      break;
   }

   EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE), TRUE);
   EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), TRUE);
   switch (skinName) {
   case skinImageMine:
   case skinImageFlag:
   case skinImageBtnNew:
   case skinImageBtnPause:
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   ), FALSE);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), FALSE);
      break;
   case skinImageBckgrnd:
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   ), FALSE);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), TRUE);
      break;
   case skinImagePause:
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   ), TRUE);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), TRUE);
      break;
   case skinColorBckgrnd:
   case skinBorder:
   case skinColorText:
   case skinTypeFont:
      break;
   }

   switch (skinName) {
   case skinImageMine    : pImageData = &Skin.m_ImgMine;        break;
   case skinImageFlag    : pImageData = &Skin.m_ImgFlag;        break;
   case skinImageBtnNew  : pImageData = &Skin.m_ImgBtnNew  [0]; break;
   case skinImageBtnPause: pImageData = &Skin.m_ImgBtnPause[0]; break;
   case skinImagePause   : pImageData = &Skin.m_ImgPause;       break;
   case skinImageBckgrnd : pImageData = &Skin.m_ImgBckgrnd;     break;
   case skinColorBckgrnd : pSkinColor = &Skin.m_colorBk;        break;
   case skinBorder       :                                      break;
   case skinColorText    :                                      break;
   case skinTypeFont     :                                      break;
   }

   RECTEX rect(GetWindowRect(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_FRAME)));
   SIZE SizeOut = rect.size();
   SIZE SizeIn = {10,10};
   switch (skinName) {
   case skinImagePause  :
   case skinImageBckgrnd:
   case skinColorBckgrnd: SizeIn = gpFM2Proj->GetSizeMosaicWindow();
   }
   rect = FindInnerRect(SizeIn, SizeOut);
   MoveWindow(hWndField, rect);

   int nUpper, nLower, nPos;
   switch (skinName) {
   case skinBorder      :
      SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETRANGE, 0L, MAKELPARAM(10, 1));
      SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS  , 0L, Skin.m_Border.m_iWidth);
      break;
   case skinImageBtnPause:
   case skinImageBtnNew:      nUpper=3          ; nLower=0               ; nPos=0         ; break;
   case skinColorText:        nUpper=nsCell::_21; nLower=nsCell::_Unknown; nPos=nsCell::_1; break;
   case skinColorBckgrnd:
      Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), Skin.m_bToAll ? BST_CHECKED : BST_UNCHECKED);
      break;
   }
   switch (skinName) {
   case skinImageBtnPause:
   case skinImageBtnNew:
   case skinColorText:
      hWndSpin = CreateUpDownControl(
         WS_CHILD | WS_VISIBLE | UDS_ALIGNRIGHT | UDS_SETBUDDYINT,
         0,0,20,30,
         GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_FRAME),
         ID_DIALOG_CHANGESKIN_SPIN_TEXT,
         ghInstance,
         hWndField,
         nUpper, nLower, nPos);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
      defWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT = (WNDPROC)SetWindowLong(hWndSpin, GWL_WNDPROC, (LONG)newWndProc_ID_DIALOG_CHANGESKIN_SPIN_TEXT);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   }

   switch (skinName) {
   case skinImageBtnNew  :
      //GetWindowRect(hWndField, &rect);
      //SetWindowPos(hWndField, HWND_NOTOPMOST, 0,0, rect.width()+(rect.height()-rect.width()), rect.height(), SWP_NOMOVE);
   case skinImageBtnPause:
   case skinImageMine    :
   case skinImageFlag    :
   case skinImagePause   :
   case skinImageBckgrnd :
      local_DeleteImage();
      pImage = new CImage();
      AcceptImage(false);
      break;
   }

   InvalidateFrame();
}

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   {
      SetWindowText(GetDlgItem(hwnd, IDOK    ), CLang::m_StrArr[IDS__OK    ]);
      SetWindowText(GetDlgItem(hwnd, IDCANCEL), CLang::m_StrArr[IDS__CANCEL]);
      SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_CHANGESKIN]);

      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE    ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_MINE        ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG    ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_FLAG        ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE   ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_PAUSE       ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_PAUSE      ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW  ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_GAME       ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_BACKGROUND  ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_COLOR_BACKGROUND  ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_BORDER        ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BORDER_COLOR_WIDTH]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT    ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_CHARACTER_COLOR   ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT     ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_FONT_TYPE         ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_RESET         ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__RESET_TO_DEFAULT              ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_PLACE         ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_TYPE_IMAGE          ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_CENTER        ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_CENTER              ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_DILATE        ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_STRETCH             ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE     ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_TILE                ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_CHANGESKIN_STATIC_WIDHT         ), CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__WIDTH_BORDER                  ]);
   }

   hDlg = hwnd;

   Skin = gpFM2Proj->GetSkin();

   { // выравнивание кнопок
      RECTEX rect = ::GetWindowRect(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE));
      rect = ::ScreenToClient(hDlg, rect);

      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG    ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE   ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW  ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_BORDER        ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT    ), rect.moveY(rect.height()-1));
      MoveWindow(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT     ), rect.moveY(rect.height()-1));
   }

   ///Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE         ), BST_UNCHECKED);
   Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE+(skinName=skinImageMine)), BST_CHECKED);

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
   hWndField = CreateWindow(
      TEXT("classNameFastMinesSkinField"), NULL,
      WS_CHILD | WS_VISIBLE | WS_DLGFRAME,
      0, 0, 0, 0,
      GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_FRAME), (HMENU)0, ghInstance, NULL);

   ReInitDialog();

   return TRUE;
}

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE    :
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_FLAG    :
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_PAUSE   :
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNPAUSE:
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BTNNEW  :
   case ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_BCKGRND :
   case ID_DIALOG_SELECTSKIN_BUTTON_COLOR_BCKGRND :
   case ID_DIALOG_SELECTSKIN_BUTTON_BORDER        :
   case ID_DIALOG_SELECTSKIN_BUTTON_COLOR_TEXT    :
   case ID_DIALOG_SELECTSKIN_BUTTON_TYPE_FONT     :
      skinName = ESkinName(id-ID_DIALOG_SELECTSKIN_BUTTON_IMAGE_MINE);
      ReInitDialog();
      return;
   case ID_DIALOG_CHANGESKIN_EDIT_WIDHT:
      if (codeNotify == EN_CHANGE) {
         Skin.m_Border.m_iWidth = SendDlgItemMessage(hwnd, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_GETPOS, 0L, 0L);
         if (Skin.m_Border.m_iWidth > 10) {
            Skin.m_Border.m_iWidth -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS, 0L, Skin.m_Border.m_iWidth);
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
            OpenFileName.hInstance   = ghInstance;
            TCHAR szFilter[256];
            lstrcpy(szFilter, CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__OPENFILENAME_FILTER_ALL_IMAGES]);
            TCHAR szFilter2[] = TEXT("\0*.bmp;*.ico;*.emf;\0Windows Bitmap (*.bmp)\0*.bmp\0Icon (*.ico)\0*.ico\0Enhanced Metafile (*.emf)\0*.emf\0\0");//Windows Metafile (*.wmf)\0*.wmf\0\0");// *.wmf;\n
            memcpy(szFilter+lstrlen(szFilter), szFilter2, chDIMOF(szFilter2));
            OpenFileName.lpstrFilter = szFilter;
            OpenFileName.lpstrCustomFilter = NULL;
          //OpenFileName.nMaxCustFilter;
            OpenFileName.nFilterIndex   = 0;
            TCHAR szFile[MAX_PATH] = {0};
            lstrcpy(szFile, GetFileName(pImageData->m_szPath));
            OpenFileName.lpstrFile      = szFile;
            OpenFileName.nMaxFile       = MAX_PATH;
            OpenFileName.lpstrFileTitle = NULL;
          //OpenFileName.nMaxFileTitle;
            TCHAR szDir[MAX_PATH] = {0};
            if (pImageData->m_szPath && pImageData->m_szPath[0])
               lstrcpy(szDir, GetFileDir(pImageData->m_szPath));
            else GetCurrentDirectory(MAX_PATH, szDir);
            OpenFileName.lpstrInitialDir = szDir;
            OpenFileName.lpstrTitle      = CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__OPENFILENAME_TITLE_OPEN_IMAGE];
            OpenFileName.Flags           = OFN_EXPLORER | OFN_FILEMUSTEXIST | OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_PATHMUSTEXIST;
          //OpenFileName.nFileOffset;
          //OpenFileName.nFileExtension;
            OpenFileName.lpstrDefExt = NULL;
          //OpenFileName.lCustData;
          //OpenFileName.lpfnHook;
          //OpenFileName.lpTemplateName;
            if (GetOpenFileName(&OpenFileName)) {
               lstrcpy(pImageData->m_szPath, OpenFileName.lpstrFile);
               AcceptImage(false);
            }
         }
         break;
      case skinBorder      :
         if (id == ID_DIALOG_CHANGESKIN_BUTTON_SELECT2)
              pSkinColor = &Skin.m_Border.m_colorShadow;
         else pSkinColor = &Skin.m_Border.m_colorLight;
      case skinColorBckgrnd:
      case skinColorText   :
         {  static COLORREF custColors[16] = {
               0x000000, 0x0000FF, 0x00FF00, 0x00FFFF, 0xFF0000, 0xFF00FF, 0xFFFF00, 0xFFFFFF,
               0xFFF000, 0x00007F, 0x007F00, 0x007F7F, 0x7F0000, 0x7F007F, 0x7F7F00, 0x7F7F7F
            };
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
            LOGFONT LogFont = Skin.m_Font;
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
               Skin.m_Font = LogFont;
         }
         break;
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL:
      {
         BOOL bCheck = (BST_CHECKED == Button_GetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL)));
         if (skinName == skinColorBckgrnd) {
            Skin.m_bToAll = !!bCheck;
         } else {
            pImage->SetTransparent(pImageData->m_bTransparent = !!bCheck);
         }
         if (skinName == skinColorBckgrnd) {
            InvalidateRect(hDlg, NULL, TRUE);
         } else {
            InvalidateRect(hWndField, NULL, TRUE);
         }
         return;
      }
   case ID_DIALOG_CHANGESKIN_BUTTON_CENTER:
   case ID_DIALOG_CHANGESKIN_BUTTON_DILATE:
   case ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE:
      pImage->SetPlace(pImageData->m_Place = EPlace(id - ID_DIALOG_CHANGESKIN_BUTTON_CENTER));
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case ID_DIALOG_CHANGESKIN_BUTTON_RESET:
      switch (skinName) {
      case skinImageMine    : Skin.m_ImgMine        = CSkin().m_ImgMine       ; break;
      case skinImageFlag    : Skin.m_ImgFlag        = CSkin().m_ImgFlag       ; break;
      case skinImagePause   : Skin.m_ImgPause       = CSkin().m_ImgPause      ; break;
      case skinImageBckgrnd : Skin.m_ImgBckgrnd     = CSkin().m_ImgBckgrnd    ; break;
      case skinImageBtnNew  : Skin.m_ImgBtnNew  [0] = CSkin().m_ImgBtnNew  [0];
                              Skin.m_ImgBtnNew  [1] = CSkin().m_ImgBtnNew  [1];
                              Skin.m_ImgBtnNew  [2] = CSkin().m_ImgBtnNew  [2];
                              Skin.m_ImgBtnNew  [3] = CSkin().m_ImgBtnNew  [3]; break;
      case skinImageBtnPause: Skin.m_ImgBtnPause[0] = CSkin().m_ImgBtnPause[0];
                              Skin.m_ImgBtnPause[1] = CSkin().m_ImgBtnPause[1];
                              Skin.m_ImgBtnPause[2] = CSkin().m_ImgBtnPause[2];
                              Skin.m_ImgBtnPause[3] = CSkin().m_ImgBtnPause[3]; break;
      case skinColorBckgrnd : Skin.m_colorBk        = CSkin().m_colorBk       ;
                              Skin.m_bToAll         = CSkin().m_bToAll        ; break;
      case skinBorder       : Skin.m_Border         = CSkin().m_Border        ; break;
      case skinColorText    : Skin.m_ColorText      = CSkin().m_ColorText     ; break;
      case skinTypeFont     : Skin.m_Font           = CSkin().m_Font          ; break;
      }
      LoadDefaultImage();
      switch (skinName) {
      case skinImageMine    :
      case skinImageFlag    :
      case skinImageBtnNew  :
      case skinImageBtnPause:
      case skinImagePause   :
      case skinImageBckgrnd : AcceptImage(true); break;
      case skinBorder       : SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_SPIN_WIDHT, UDM_SETPOS, 0L, Skin.m_Border.m_iWidth); break;
      case skinColorBckgrnd : SendDlgItemMessage(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL, BM_SETCHECK, (WPARAM)(Skin.m_bToAll ? BST_CHECKED : BST_UNCHECKED), 0L);
                              InvalidateRect(hDlg, NULL, TRUE);
                              break;
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      gpFM2Proj->SetSkin(Skin);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_CLOSE
void OnClose(HWND hwnd){
   switch (skinName) {
   case skinImageBtnNew  :
   case skinImageBtnPause:
   case skinImageMine    :
   case skinImageFlag    :
   case skinImagePause   :
   case skinImageBckgrnd :
      local_DeleteImage();
   }
   EndDialog(hwnd, 0);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void OnPaint(HWND hwnd) {
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (Skin.m_bToAll)
      nsEraseBk::FillWnd(hwnd, Skin.m_colorBk, false);
}

// WM_ERASEBKGND
BOOL OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!Skin.m_bToAll) {
      //return nsEraseBk::OnEraseBkgnd(hwnd, hdc, ::GetSysColor(COLOR_BTNFACE));
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L); // 
   }
   return nsEraseBk::OnEraseBkgnd(hwnd, hdc, Skin.m_colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

LRESULT CALLBACK FieldWindowProc(HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   switch (Msg) {
   case WM_SETTEXT:
      DefWindowProc(hWnd, Msg, wParam, lParam);
      switch (skinName) {
      case skinImageBtnNew  : pImageData = &Skin.m_ImgBtnNew  [SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)];  break;
      case skinImageBtnPause: pImageData = &Skin.m_ImgBtnPause[SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L)];  break;
      }
      InvalidateRect(hWndField, NULL, TRUE);
      return 0;
   case WM_ERASEBKGND:
      return -1;
   case WM_PAINT:
      {  
         PAINTSTRUCT PaintStruct;
         const HDC hDC = BeginPaint(hWnd, &PaintStruct);
         RECT Rect; GetClientRect(hWnd, &Rect);

         //HDC hCDC = GetWindowDC(GetDesktopWindow());
         const HBITMAP hBmp = CreateCompatibleBitmap(hDC, Rect.right,Rect.bottom);//CreateBitmap(Rect.right,Rect.bottom, 1, 32, NULL);//
         //ReleaseDC(GetDesktopWindow(), hCDC);

         const HDC hCDC = CreateCompatibleDC(NULL);
         const HBITMAP hOldBmp = (HBITMAP)SelectObject(hCDC, hBmp);
         {
            HBRUSH hBrushNew;
            switch (skinName) {
            case skinImageBtnNew  :
            case skinImageBtnPause:
               if (!Skin.m_bToAll) {
                  hBrushNew = CreateSolidBrush(GetSysColor(COLOR_BTNFACE));
                  break;
               }
            default:
               hBrushNew = CreateSolidBrush(Skin.m_colorBk);
            }
            const HBRUSH hBrushOld = (HBRUSH)SelectObject(hCDC, hBrushNew);
            PatBlt(hCDC, 0,0, Rect.right,Rect.bottom, PATCOPY);
            SelectObject(hCDC, hBrushOld);
            DeleteObject(hBrushNew);
         }
         switch (skinName) {
         case skinImageBtnNew  :
         case skinImageBtnPause:
            AcceptImage(false);
         case skinImageMine    :
         case skinImageFlag    :
         case skinImagePause   :
         case skinImageBckgrnd :
            pImage->DrawPlace(hCDC, &Rect);
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
               const HPEN hPenOld = (HPEN)SelectObject(hCDC, CreatePen(PS_SOLID, Skin.m_Border.m_iWidth, Skin.m_Border.m_colorShadow));
               MoveToEx(hCDC, p[0].x, p[0].y, NULL);
               LineTo  (hCDC, p[1].x, p[1].y);
               LineTo  (hCDC, p[2].x, p[2].y);
               LineTo  (hCDC, p[3].x, p[3].y);
               DeleteObject(SelectObject(hCDC, hPenOld));
               SelectObject(hCDC, CreatePen(PS_SOLID, Skin.m_Border.m_iWidth, Skin.m_Border.m_colorLight));
               LineTo  (hCDC, p[4].x, p[4].y);
               LineTo  (hCDC, p[5].x, p[5].y);
               LineTo  (hCDC, p[0].x, p[0].y);
               DeleteObject(SelectObject(hCDC, hPenOld));
            }
            break;
         case skinColorText:
            {
               const int val = SendMessage(hWndSpin, UDM_GETPOS, 0L, 0L);
               if (val) pSkinColor = &Skin.m_ColorText.m_colorOpen [val];
                  else  pSkinColor = &Skin.m_ColorText.m_colorClose[val];
               SetTextColor(hCDC, *pSkinColor);
               SetBkMode   (hCDC, TRANSPARENT);
               GetClientRect(hWnd, &Rect);
               Skin.m_Font.lfHeight = Rect.bottom;
               const HFONT hFontOld = (HFONT)SelectObject(hCDC, CreateFontIndirect(&Skin.m_Font));
               TCHAR const *szCaption;
               if (val) {
                  szCaption = nsCell::SZ_CAPTION_OPEN[val];
               } else {
                  szCaption = nsCell::SZ_CAPTION_CLOSE[val];
               }
               DrawText(hCDC, szCaption, -1, &Rect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
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
               HFONT hFontOld;
               Skin.m_Font.lfHeight = 0;
               do {
                  Skin.m_Font.lfHeight++;
                  hFontOld = (HFONT)SelectObject(hCDC, CreateFontIndirect(&Skin.m_Font));

                  GetTextExtentPoint32(hCDC, str1, lstrlen(str1), &size1);
                  GetTextExtentPoint32(hCDC, str2, lstrlen(str2), &size2);
                  GetTextExtentPoint32(hCDC, str3, lstrlen(str3), &size3);

                  DeleteObject(SelectObject(hCDC, hFontOld));
               } while (max(max(size1.cx, size2.cx), size3.cx) < (Rect.right-Rect.left));
               Skin.m_Font.lfHeight--;
               hFontOld = (HFONT)SelectObject(hCDC, CreateFontIndirect(&Skin.m_Font));
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

void AcceptImage(bool bApplyDefaultImage) {
   if (!pImageData->m_szPath[0] || bApplyDefaultImage) {
      LoadDefaultImage();
      if (!bApplyDefaultImage) {
         pImage->SetPlace(pImageData->m_Place);
         pImage->SetTransparent(pImageData->m_bTransparent);
      }
   } else {
      pImage->SetImage(*pImageData);
   }

   if (skinName == skinImageBckgrnd) {
      BOOL bEnable = (pImage->GetImageType() != imageUnknown);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), bEnable);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE             ), bEnable);
      EnableWindow(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE          ), bEnable);
   }

   if (pImage->GetImageType() == imageBitmap) {
      Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), pImageData->m_bTransparent ? BST_CHECKED : BST_UNCHECKED);
      EnableWindow   (GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), TRUE);
   } else {
      pImage->SetTransparent(pImageData->m_bTransparent = true);
      Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), BST_CHECKED);
      EnableWindow   (GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_TRANSPARENTorSETALL), FALSE);
   }

   // uncheck all radiobuttons
   Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER   ), BST_UNCHECKED);
   Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_DILATE   ), BST_UNCHECKED);
   Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_ALONGSIDE), BST_UNCHECKED);
   // check radiobutton
   Button_SetCheck(GetDlgItem(hDlg, ID_DIALOG_CHANGESKIN_BUTTON_CENTER+pImageData->m_Place), BST_CHECKED);
}

inline void LoadDefaultImage() {
   switch (skinName) {
   case skinImageMine    : LoadDefaultImageMine   (ghInstance, *pImage); break;
   case skinImageFlag    : LoadDefaultImageFlag   (ghInstance, *pImage); break;
   case skinImagePause   : LoadDefaultImagePause  (ghInstance, *pImage); break;
   case skinImageBckgrnd : LoadDefaultImageBckgrnd(ghInstance, *pImage); break;
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
}

void SetName(ESkinName newName){
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

BOOL OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
BOOL OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_TEXT  , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_TEXT, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK                     , hDlg, gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_EX_CTLCOLOR(hDlg, gpFM2Proj->GetSkin());
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT);
   SETNEWWNDPROC(hwnd, IDOK                           );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_FILESKIN]);
   SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), CLang::m_StrArr[IDS__DIALOG_FILESKIN__FILE_NAME]);

   MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), SW_HIDE);
   ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);

   RECTEX rect(0,0,241,116);
   const SIZE sizeScreen = GetScreenSize();
   MoveWindow(hwnd,
      sizeScreen.cx/2 - rect.width ()/2,
      sizeScreen.cy/2 - rect.height()/2,
      rect.width(),
      rect.height(),
      FALSE);

   return TRUE;
}

#define SKINNAMELENGTH 30

struct CFileSkin {
   TCHAR m_szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   CSkin m_Skin;
   CFileSkin() {lstrcpy(m_szVersion, TEXT(ID_VERSIONINFO_VERSION3));}
#ifndef UNICODE
   const CFileSkin& Conversion(const nsVer210::CFileSkin &FileSkin_v210) {
      BOOL bResult = (
         placeCenter  == nsVer210::placeCenter  &&
         placeStretch == nsVer210::placeStretch &&
         placeTile    == nsVer210::placeTile    &&

         nsCell::_Nil  == nsVer210::_Nil  &&
         nsCell::_1    == nsVer210::_1  &&
         nsCell::_2    == nsVer210::_2  &&
         nsCell::_3    == nsVer210::_3  &&
         nsCell::_4    == nsVer210::_4  &&
         nsCell::_5    == nsVer210::_5  &&
         nsCell::_6    == nsVer210::_6  &&
         nsCell::_7    == nsVer210::_7  &&
         nsCell::_8    == nsVer210::_8  &&
         nsCell::_9    == nsVer210::_9  &&
         nsCell::_10   == nsVer210::_10 &&
         nsCell::_11   == nsVer210::_11 &&
         nsCell::_12   == nsVer210::_12 &&
         nsCell::_13   == nsVer210::_13 &&
         nsCell::_14   == nsVer210::_14 &&
         nsCell::_15   == nsVer210::_15 &&
         nsCell::_16   == nsVer210::_16 &&
         nsCell::_17   == nsVer210::_17 &&
         nsCell::_18   == nsVer210::_18 &&
         nsCell::_19   == nsVer210::_19 &&
         nsCell::_20   == nsVer210::_20 &&
         nsCell::_21   == nsVer210::_21 &&
         nsCell::_Mine == nsVer210::_Mine &&

         nsCell::_Unknown == nsVer210::_Unknown &&
         nsCell::_Clear   == nsVer210::_Clear &&
         nsCell::_Flag    == nsVer210::_Flag
      );
   #ifdef _DEBUG
      if (!bResult) {
         ::MessageBox_AbortProcess(TEXT("Надо переделать ф-цию"));
      }
   #endif // _DEBUG
      lstrcpy(m_szVersion, TEXT(ID_VERSIONINFO_VERSION3));
      lstrcpy(m_Skin.m_ImgMine       .m_szPath, FileSkin_v210.m_Skin.m_Mine       .m_szPath); m_Skin.m_ImgMine       .m_bTransparent = FileSkin_v210.m_Skin.m_Mine       .m_bTransparent; m_Skin.m_ImgMine       .m_Place = (EPlace)FileSkin_v210.m_Skin.m_Mine       .m_Place;
      lstrcpy(m_Skin.m_ImgFlag       .m_szPath, FileSkin_v210.m_Skin.m_Flag       .m_szPath); m_Skin.m_ImgFlag       .m_bTransparent = FileSkin_v210.m_Skin.m_Flag       .m_bTransparent; m_Skin.m_ImgFlag       .m_Place = (EPlace)FileSkin_v210.m_Skin.m_Flag       .m_Place;
      lstrcpy(m_Skin.m_ImgPause      .m_szPath, FileSkin_v210.m_Skin.m_Pause      .m_szPath); m_Skin.m_ImgPause      .m_bTransparent = FileSkin_v210.m_Skin.m_Pause      .m_bTransparent; m_Skin.m_ImgPause      .m_Place = (EPlace)FileSkin_v210.m_Skin.m_Pause      .m_Place;
      lstrcpy(m_Skin.m_ImgBckgrnd    .m_szPath, FileSkin_v210.m_Skin.m_Bckgrnd    .m_szPath); m_Skin.m_ImgBckgrnd    .m_bTransparent = FileSkin_v210.m_Skin.m_Bckgrnd    .m_bTransparent; m_Skin.m_ImgBckgrnd    .m_Place = (EPlace)FileSkin_v210.m_Skin.m_Bckgrnd    .m_Place;
      lstrcpy(m_Skin.m_ImgBtnNew  [0].m_szPath, FileSkin_v210.m_Skin.m_BtnNew  [0].m_szPath); m_Skin.m_ImgBtnNew  [0].m_bTransparent = FileSkin_v210.m_Skin.m_BtnNew  [0].m_bTransparent; m_Skin.m_ImgBtnNew  [0].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnNew  [0].m_Place;
      lstrcpy(m_Skin.m_ImgBtnNew  [1].m_szPath, FileSkin_v210.m_Skin.m_BtnNew  [1].m_szPath); m_Skin.m_ImgBtnNew  [1].m_bTransparent = FileSkin_v210.m_Skin.m_BtnNew  [1].m_bTransparent; m_Skin.m_ImgBtnNew  [1].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnNew  [1].m_Place;
      lstrcpy(m_Skin.m_ImgBtnNew  [2].m_szPath, FileSkin_v210.m_Skin.m_BtnNew  [2].m_szPath); m_Skin.m_ImgBtnNew  [2].m_bTransparent = FileSkin_v210.m_Skin.m_BtnNew  [2].m_bTransparent; m_Skin.m_ImgBtnNew  [2].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnNew  [2].m_Place;
      lstrcpy(m_Skin.m_ImgBtnNew  [3].m_szPath, FileSkin_v210.m_Skin.m_BtnNew  [3].m_szPath); m_Skin.m_ImgBtnNew  [3].m_bTransparent = FileSkin_v210.m_Skin.m_BtnNew  [3].m_bTransparent; m_Skin.m_ImgBtnNew  [3].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnNew  [3].m_Place;
      lstrcpy(m_Skin.m_ImgBtnPause[0].m_szPath, FileSkin_v210.m_Skin.m_BtnPause[0].m_szPath); m_Skin.m_ImgBtnPause[0].m_bTransparent = FileSkin_v210.m_Skin.m_BtnPause[0].m_bTransparent; m_Skin.m_ImgBtnPause[0].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnPause[0].m_Place;
      lstrcpy(m_Skin.m_ImgBtnPause[1].m_szPath, FileSkin_v210.m_Skin.m_BtnPause[1].m_szPath); m_Skin.m_ImgBtnPause[1].m_bTransparent = FileSkin_v210.m_Skin.m_BtnPause[1].m_bTransparent; m_Skin.m_ImgBtnPause[1].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnPause[1].m_Place;
      lstrcpy(m_Skin.m_ImgBtnPause[2].m_szPath, FileSkin_v210.m_Skin.m_BtnPause[2].m_szPath); m_Skin.m_ImgBtnPause[2].m_bTransparent = FileSkin_v210.m_Skin.m_BtnPause[2].m_bTransparent; m_Skin.m_ImgBtnPause[2].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnPause[2].m_Place;
      lstrcpy(m_Skin.m_ImgBtnPause[3].m_szPath, FileSkin_v210.m_Skin.m_BtnPause[3].m_szPath); m_Skin.m_ImgBtnPause[3].m_bTransparent = FileSkin_v210.m_Skin.m_BtnPause[3].m_bTransparent; m_Skin.m_ImgBtnPause[3].m_Place = (EPlace)FileSkin_v210.m_Skin.m_BtnPause[3].m_Place;
      m_Skin.m_Font = FileSkin_v210.m_Skin.m_Font;
      memcpy(m_Skin.m_ColorText.m_colorOpen , FileSkin_v210.m_Skin.m_ColorText.m_colorCaptionOpen , sizeof(m_Skin.m_ColorText.m_colorOpen ));
      memcpy(m_Skin.m_ColorText.m_colorClose, FileSkin_v210.m_Skin.m_ColorText.m_colorCaptionClose, sizeof(m_Skin.m_ColorText.m_colorClose));
      m_Skin.m_Border.m_colorShadow = FileSkin_v210.m_Skin.m_Border.m_colorShadow;
      m_Skin.m_Border.m_colorLight  = FileSkin_v210.m_Skin.m_Border.m_colorLight;
      m_Skin.m_Border.m_iWidth      = FileSkin_v210.m_Skin.m_Border.m_iWidth;
      m_Skin.m_colorBk              = FileSkin_v210.m_Skin.m_colorBk;
      m_Skin.m_bToAll               = FileSkin_v210.m_Skin.m_bToAll;
      return *this;
   }
#endif // UNICODE
};

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case IDOK:
      {
         TCHAR szSkin[SKINNAMELENGTH];
         GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szSkin, SKINNAMELENGTH);
         CString strPath(GetModuleDir(ghInstance) + TEXT("Skin"));

         // directory exist?
         CreateDirectory(strPath, NULL); // каталог создастся если не существовал

         // подготовка данных к сохранению
         CFileSkin FileSkin;
         FileSkin.m_Skin = gpFM2Proj->GetSkin();
         CString strPath2(strPath + TEXT("\\") + szSkin);
         CreateDirectory(strPath2, NULL); // каталог для рисунков данного скина
         { // преобразую абсолютные пути на рисунки в относительные и копирую рисунки
            SetCursor(LoadCursor(NULL, IDC_WAIT));
 
#define CHANGE_PATH_AND_COPY(szPath) \
            if (szPath && szPath[0]) { \
               CString strNewFile = GetFileName(szPath); \
               CString strNewPath = strPath2 + TEXT('\\'); \
               CopyFile(szPath, strNewPath + strNewFile, FALSE); \
               ZeroMemory(szPath, chDIMOF(szPath)); /*всё затираю, чтобы в файле не было видно пути*/ \
               lstrcpy(szPath, strNewFile); \
            }

            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgMine       .m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgFlag       .m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgPause      .m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBckgrnd    .m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnNew  [0].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnNew  [1].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnNew  [2].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnNew  [3].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnPause[0].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnPause[1].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnPause[2].m_szPath)
            CHANGE_PATH_AND_COPY(FileSkin.m_Skin.m_ImgBtnPause[3].m_szPath)
#undef CHANGE_PATH_AND_COPY

            SetCursor(LoadCursor(NULL, IDC_ARROW));
         }
         CString strFile(strPath2 + TEXT(".skn"));
         if (FileExist(strFile)) {
            if (IDNO == MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__OVERWRITE], CLang::m_StrArr[IDS__DIALOG_FILESKIN__FILE_COPYING], MB_ICONQUESTION | MB_YESNO | MB_DEFBUTTON2))
               return;
         }
         // save file
         HANDLE hFile = CreateFile(
            strFile,
            GENERIC_WRITE,
            0,
            NULL,
            CREATE_ALWAYS,
            FILE_ATTRIBUTE_NORMAL,
            NULL
         );
         if (hFile == INVALID_HANDLE_VALUE) {
            MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
         } else {
            DWORD dwNOBW = 0;
            if (!WriteFile(hFile, &FileSkin, sizeof(CFileSkin), &dwNOBW, NULL) ||
                (sizeof(CFileSkin) != dwNOBW)
               )
            {
               MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_WRITE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
            }
            CloseHandle(hFile);
         }
      }
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_ERASEBKGND
BOOL OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpFM2Proj->GetSkin().m_bToAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::OnEraseBkgnd(hwnd, hdc, gpFM2Proj->GetSkin().m_colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_CLOSE
void OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
}

CSkin LoadSkin(HMENU hMenu, UINT id) {
   CString strSkinName(GetMenuString(hMenu, id, MF_BYCOMMAND));
   CString strPath(GetModuleDir(ghInstance) + TEXT("Skin\\") + strSkinName);
   CString strFile(strPath + TEXT(".skn"));

   CFileSkin FileSkin;
   HANDLE hFile = CreateFile(
      strFile,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   BOOL bResult = FALSE;
   if (hFile == INVALID_HANDLE_VALUE) {
      MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
   } else {
      DWORD dwNOBR = 0;
      if (!ReadFile(hFile, &FileSkin, sizeof(CFileSkin), &dwNOBR, NULL)) {
         MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         if ((sizeof(CFileSkin) == dwNOBR) &&
             !_tcscmp(FileSkin.m_szVersion, TEXT(ID_VERSIONINFO_VERSION3))
            )
         {
            // all Ok
            bResult = TRUE;
         } else {
      #ifndef UNICODE
            // попытка переконвертировать с формата 2.10
            SetFilePointer(hFile, 0, NULL, FILE_BEGIN);
            nsVer210::CFileSkin FileSkin_v210;
            if (ReadFile(hFile, &FileSkin_v210, sizeof(nsVer210::CFileSkin), &dwNOBR, NULL) &&
               (sizeof(nsVer210::CFileSkin) == dwNOBR) &&
                !strcmp(FileSkin_v210.m_szVersion, ID_VERSIONINFO_VERSION3_v210)
               )
            {
               FileSkin.Conversion(FileSkin_v210);
               bResult = TRUE;
            } else
      #endif // UNICODE
            {
               MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_VERSION], CLang::m_StrArr[IDS__INFORMATION], MB_ICONINFORMATION | MB_OK);
               bResult = FALSE;
            }
         }
      }
   }
   CloseHandle(hFile);
   if (bResult) {
      { // преобразую относительные пути в абсолютные
#define CHANGE_PATH(szPath)  \
         if (szPath && szPath[0])   \
            lstrcpy(szPath, strPath + TEXT('\\') + szPath);
         CHANGE_PATH(FileSkin.m_Skin.m_ImgMine       .m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgFlag       .m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgPause      .m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBckgrnd    .m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnNew  [0].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnNew  [1].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnNew  [2].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnNew  [3].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnPause[0].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnPause[1].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnPause[2].m_szPath)
         CHANGE_PATH(FileSkin.m_Skin.m_ImgBtnPause[3].m_szPath)
#undef CHANGE_PATH
      }
      return FileSkin.m_Skin;
   } else {
      MessageBox(hDlg, CLang::m_StrArr[IDS__DIALOG_FILESKIN__RESTORED_DEFAULT_SKIN], CLang::m_StrArr[IDS__INFORMATION], MB_ICONSTOP | MB_OK);
      return CSkin();
   }
}

void LoadMenuList(HMENU hMenu) {
   CString strPath(GetModuleDir(ghInstance) + TEXT("Skin\\*.skn"));
   WIN32_FIND_DATA findFileData;
   memset(&findFileData, 0, sizeof(findFileData));
   HANDLE h = FindFirstFile(strPath, &findFileData);
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
         findFileData.cFileName[lstrlen(findFileData.cFileName)-4] = TEXT('\0');
         InsertMenuItem(hMenu, i, TRUE, &mii);
         i++;
      } while (FindNextFile(h, &findFileData));
      FindClose(h);
   }
}

void ReloadMenuList(HMENU hMenu){
   int count = GetMenuItemCount(hMenu);
   if (count == -1) return;
   if (count>1)
      for (int i=1; i<=count; i++)
         RemoveMenu(hMenu, 1, MF_BYPOSITION);
   LoadMenuList(hMenu);
}

} // namespace nsFileSkin
