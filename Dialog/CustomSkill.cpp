////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "CustomSkill.cpp"
// обработка диалогового окна "CustomSkillLevel"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "CustomSkill.h"
#include <WindowsX.h>
#include <CommCtrl.h>
#include "../ID_resource.h"
#include "../EraseBk.h"
#include "../Lang.h"
#include "../FastMines2.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsCustomSkill {
////////////////////////////////////////////////////////////////////////////////
//                            types & variables
////////////////////////////////////////////////////////////////////////////////
int resultDialog;

HWND hDlg;
HMENU hMenu;

int mustFree;

const
COORD minSizeMosaic = {5, 5};//{1, 1};//
COORD curSizeMosaic;
COORD maxSizeMosaic_minSizeCell; // максимальный размер поля при минимальном размере ячеек
COORD maxSizeMosaic_curSizeCell; // максимальный размер поля при текущем     размере ячеек

const
int minMines = 1;
int maxMines, curMines;

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////
BOOL CreateMenuItem(HMENU, LPTSTR, UINT, UINT, HMENU, BOOL, UINT);

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_GROUPBOX   ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_X   ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_Y   ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_MINE,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_X     ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_Y     ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_MINE  ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_X     ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_Y     ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_MINE  ,       gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK                             , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDCANCEL                         , hDlg, gpFM2Proj->GetSkin())
//WNDPROC_STATIC(PopupMenu                        ,       gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_INITDIALOG
BOOL OnInitDialog(HWND hWnd, HWND hwndFocus, LPARAM lParam) {

   {
      SetWindowText(GetDlgItem(hWnd, IDOK    ), CLang::m_StrArr[IDS__OK    ]);
      SetWindowText(GetDlgItem(hWnd, IDCANCEL), CLang::m_StrArr[IDS__CANCEL]);

      SetWindowText(hWnd, CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL]);

      SetWindowText(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_X   ), CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__X_WIDTH                  ]);
      SetWindowText(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_Y   ), CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__Y_HEIGHT                 ]);
      SetWindowText(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_MINE), CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__NUMBER_MINES             ]);
      SetWindowText(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1), CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__CURRENT_SIZE]);
      SetWindowText(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2), CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__MINIMAL_SIZE]);
   }

   resultDialog = IDCANCEL;
   const SIZE sizeScreen = GetScreenSize();

   hDlg = hWnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_GROUPBOX   );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_X   );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_Y   );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_STATIC_MINE);
 //SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_X     );
 //SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y     );
 //SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_MINE  );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X     );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y     );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE  );
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1);
   SETNEWWNDPROC(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2);
   SETNEWWNDPROC(hWnd, IDCANCEL                         );
   SETNEWWNDPROC(hWnd, IDOK                             );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   maxSizeMosaic_curSizeCell = maxSizeMosaic_minSizeCell = gpFM2Proj->GetSizeMosaic();

   maxSizeMosaic_curSizeCell.X++;
   while (gpFM2Proj->GetSize(maxSizeMosaic_curSizeCell, gpFM2Proj->GetArea()  ).cx <= sizeScreen.cx) maxSizeMosaic_curSizeCell.X++;
   maxSizeMosaic_curSizeCell.X--;
   maxSizeMosaic_curSizeCell.Y++;
   while (gpFM2Proj->GetSize(maxSizeMosaic_curSizeCell, gpFM2Proj->GetArea()  ).cy <= sizeScreen.cy) maxSizeMosaic_curSizeCell.Y++;
   maxSizeMosaic_curSizeCell.Y--;

   maxSizeMosaic_minSizeCell.X++;
   while (gpFM2Proj->GetSize(maxSizeMosaic_minSizeCell, nsMosaic::AREA_MINIMUM).cx <= sizeScreen.cx) maxSizeMosaic_minSizeCell.X++;
   maxSizeMosaic_minSizeCell.X--;
   maxSizeMosaic_minSizeCell.Y++;
   while (gpFM2Proj->GetSize(maxSizeMosaic_minSizeCell, nsMosaic::AREA_MINIMUM).cy <= sizeScreen.cy) maxSizeMosaic_minSizeCell.Y++;
   maxSizeMosaic_minSizeCell.Y--;

   curMines     = gpFM2Proj->GetMines();
   curSizeMosaic = gpFM2Proj->GetSizeMosaic();
   mustFree = gpFM2Proj->GetMosaicNeighborNumber()+1;
   maxMines = curSizeMosaic.X*curSizeMosaic.Y-mustFree;
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS  , 0L, curMines);
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X   , UDM_SETRANGE, 0L, MAKELPARAM(maxSizeMosaic_minSizeCell.X, minSizeMosaic.X));
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y   , UDM_SETRANGE, 0L, MAKELPARAM(maxSizeMosaic_minSizeCell.Y, minSizeMosaic.Y));
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X   , UDM_SETPOS  , 0L, curSizeMosaic.X);
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y   , UDM_SETPOS  , 0L, curSizeMosaic.Y);

   hMenu = CreatePopupMenu();
   CreateMenuItem(hMenu, (LPTSTR)(LPCTSTR)CLang::m_StrArr[IDS__MENU_GAME__BEGINNER    ], 0, ID_MENU_GAME_BEGINNER    , NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, (LPTSTR)(LPCTSTR)CLang::m_StrArr[IDS__MENU_GAME__AMATEUR     ], 0, ID_MENU_GAME_AMATEUR     , NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, (LPTSTR)(LPCTSTR)CLang::m_StrArr[IDS__MENU_GAME__PROFESSIONAL], 0, ID_MENU_GAME_PROFESSIONAL, NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, (LPTSTR)(LPCTSTR)CLang::m_StrArr[IDS__MENU_GAME__CRAZY       ], 0, ID_MENU_GAME_CRAZY       , NULL, FALSE, MFT_STRING);

 //defWndProc_PopupMenu = (WNDPROC)SetWindowLong(hMenu, GWL_WNDPROC, (LONG)newWndProc_PopupMenu);

   return TRUE;
}

// WM_COMMAND
void OnCommand(HWND hWnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_CUSTOMSKILL_EDIT_X:
      if (codeNotify == EN_CHANGE) {
         LONG lX = SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_GETPOS, 0L, 0L);
         if (lX > maxSizeMosaic_minSizeCell.X) {
            lX -= 0x10000;
            SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, lX);
         }
         curSizeMosaic.X = lX;
         maxMines = curSizeMosaic.X*curSizeMosaic.Y-mustFree;
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
         if (curMines > maxMines)
            SendMessage(hWnd, WM_COMMAND, MAKEWPARAM(ID_DIALOG_CUSTOMSKILL_EDIT_MINE, EN_CHANGE), 0L);
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_EDIT_Y:
      if (codeNotify == EN_CHANGE) {
         LONG lY = SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_GETPOS, 0L, 0L);
         if (lY > maxSizeMosaic_minSizeCell.Y) {
            lY -= 0x10000;
            SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, lY);
         }
         curSizeMosaic.Y = lY;
         maxMines = curSizeMosaic.X*curSizeMosaic.Y-mustFree;
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
         if (curMines > maxMines)
            SendMessage(hWnd, WM_COMMAND, MAKEWPARAM(ID_DIALOG_CUSTOMSKILL_EDIT_MINE, EN_CHANGE), 0L);
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_EDIT_MINE:
      if (codeNotify == EN_CHANGE) {
         curMines = SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_GETPOS, 0L, 0L);
         if (curMines > maxMines) {
            curMines -= 0x10000;
            SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS, 0L, curMines);
         }
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_FULLSCREEN1:
      if (BST_CHECKED == SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, BM_GETCHECK, 0L, 0L)) {
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, maxSizeMosaic_curSizeCell.X);
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, maxSizeMosaic_curSizeCell.Y);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), false);
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, BM_SETCHECK, BST_UNCHECKED, 0L);
         return;
      }
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), true);
      return;
   case ID_DIALOG_CUSTOMSKILL_FULLSCREEN2:
      if (BST_CHECKED == SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, BM_GETCHECK, 0L, 0L)) {
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, maxSizeMosaic_minSizeCell.X);
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, maxSizeMosaic_minSizeCell.Y);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), false);
         EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), false);
         SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, BM_SETCHECK, BST_UNCHECKED, 0L);
         return;
      }
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), true);
      EnableWindow(GetDlgItem(hWnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), true);
      return;
   case IDCANCEL:
      resultDialog = IDCANCEL;
      SendMessage(hWnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      {
      resultDialog = IDOK;
      SendMessage(hWnd, WM_CLOSE, 0L, 0L);
      }
      return;
   }
}

// WM_CLOSE
void OnClose(HWND hWnd){
   DestroyMenu(hMenu);
   EndDialog(hWnd, resultDialog);
}

BOOL CreateMenuItem(HMENU hMenu, LPTSTR str, UINT uIns, UINT uCom, HMENU hSubMenu, BOOL flag, UINT fType) {
   MENUITEMINFO mii;
   mii.cbSize = sizeof(MENUITEMINFO);
   mii.fMask = MIIM_STATE | MIIM_TYPE | MIIM_SUBMENU | MIIM_ID;
   mii.fType = fType;
   mii.fState = MFS_ENABLED;
   mii.dwTypeData = str;
   mii.cch = sizeof(str);
   mii.wID = uCom;
   mii.hSubMenu = hSubMenu;
   return InsertMenuItem(hMenu, uIns, flag, &mii);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void OnPaint(HWND hWnd){
   DefWindowProc(hWnd, WM_PAINT, 0L, 0L);
   if (gpFM2Proj->GetSkin().m_bToAll)
      nsEraseBk::FillWnd(hWnd, gpFM2Proj->GetSkin().m_colorBk, false);
}

// WM_ERASEBKGND
BOOL OnEraseBkgnd(HWND hWnd, HDC hdc) {
   if (!gpFM2Proj->GetSkin().m_bToAll)
      return FALSE; // DefWindowProc(hWnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::OnEraseBkgnd(hWnd, hdc, gpFM2Proj->GetSkin().m_colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

HBRUSH OnCtlColorDlg(HWND hWnd, HDC hdc, HWND hwndChild, int type) {
   static HBRUSH hBrush = NULL;
   static COLORREF colorBk = 0;
   DeleteObject(hBrush);
   hBrush = CreateSolidBrush(colorBk+=100);
   Sleep(100);
   //FORWARD_WM_CTLCOLORDLG(hWnd, hdc, hwndChild, PostMessage);
   InvalidateRect(hWnd, NULL, TRUE);
   return hBrush;
}

// WM_CONTEXTMENU
void OnContextMenu(HWND hWnd, HWND hWndContext, UINT xPos, UINT yPos) {
 //DWORD xyPos = GetMessagePos();
   int id = TrackPopupMenu(
      hMenu,
      TPM_LEFTALIGN | TPM_TOPALIGN | TPM_RETURNCMD | TPM_LEFTBUTTON | TPM_RIGHTBUTTON,
      xPos, yPos, 0,
    //LOWORD(xyPos), HIWORD(xyPos), 0,
      hWnd,
      NULL);
   nsMosaic::ESkillLevel skill;
   switch (id) {
   case ID_MENU_GAME_BEGINNER    :
   case ID_MENU_GAME_AMATEUR     :
   case ID_MENU_GAME_PROFESSIONAL:
   case ID_MENU_GAME_CRAZY       : skill = (nsMosaic::ESkillLevel)(id-ID_MENU_GAME_BEGINNER); break;
   default: return;
   }
   curMines = gpFM2Proj->DefineNumberMines(skill, curSizeMosaic);
   SendDlgItemMessage(hWnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS, 0L, curMines);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
#ifdef _DEBUG
   switch (msg) {
   case WM_SETCURSOR:
   case WM_NCHITTEST:
   case WM_NCMOUSEMOVE:
   case WM_MOUSEMOVE:
   case WM_CTLCOLORBTN:
   case WM_CTLCOLORSTATIC:
   case WM_CTLCOLOREDIT:
   case WM_ENTERIDLE:
      break;
   default:
      g_Logger.PutMsg(CLogger::LL_DEBUG, TEXT("   About Dlg: DialogProc:"), msg);
   }
#endif
   switch (msg){
   HANDLE_MSG(hDlg, WM_CONTEXTMENU, OnContextMenu);
   HANDLE_MSG(hDlg, WM_INITDIALOG , OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND    , OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT      , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND , OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE      , OnClose);
   HANDLE_WM_EX_CTLCOLOR(hDlg, gpFM2Proj->GetSkin());
   }
   return FALSE;
}

} //namespace nsCustomSkill
