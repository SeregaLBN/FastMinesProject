////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "CustomSkill.cpp"
// обработка диалогового окна "CustomSkillLevel"
////////////////////////////////////////////////////////////////////////////////
#include ".\CustomSkill.h"
#include <windowsx.h>
#include <commctrl.h>
#include "..\ID_resource.h"
#include "..\TcMosaic.h"
#include "..\EraseBk.h"
#include ".\Info.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HWND      ghWnd;

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
POINT minSizeField = {5, 5};//{1, 1};//
POINT curSizeField;
POINT maxSizeField_minSizeCell; // максимальный размер поля при минимальном размере ячеек
POINT maxSizeField_curSizeCell; // максимальный размер поля при текущем     размере ячеек

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
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_GROUPBOX   ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_X   ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_Y   ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_STATIC_MINE,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_X     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_Y     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_EDIT_MINE  ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_X     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_Y     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_CUSTOMSKILL_SPIN_MINE  ,       gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK                             , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDCANCEL                         , hDlg, gpMosaic->GetSkin())
//WNDPROC_STATIC(PopupMenu                        ,       gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   resultDialog = IDCANCEL;
   const POINT sizeScreen =
      {GetSystemMetrics(SM_CXSCREEN),
       GetSystemMetrics(SM_CYSCREEN)};

   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_GROUPBOX   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_STATIC_X   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_STATIC_Y   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_STATIC_MINE);
 //SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_X     );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y     );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_MINE  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1);
   SETNEWWNDPROC(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2);
   SETNEWWNDPROC(hwnd, IDCANCEL                         );
   SETNEWWNDPROC(hwnd, IDOK                             );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   maxSizeField_curSizeCell = maxSizeField_minSizeCell = gpMosaic->GetSizeField();

   maxSizeField_curSizeCell.x++;
   while (gpMosaic->GetSizeWindowProject(gpMosaic->GetSizeWindowField(maxSizeField_curSizeCell, gpMosaic->GetArea())).x <= sizeScreen.x) {
      maxSizeField_curSizeCell.x++;
   }
   maxSizeField_curSizeCell.x--;
   maxSizeField_curSizeCell.y++;
   while (gpMosaic->GetSizeWindowProject(gpMosaic->GetSizeWindowField(maxSizeField_curSizeCell, gpMosaic->GetArea())).y <= sizeScreen.y) {
      maxSizeField_curSizeCell.y++;
   }
   maxSizeField_curSizeCell.y--;

   maxSizeField_minSizeCell.x++;
   while (gpMosaic->GetSizeWindowProject(gpMosaic->GetSizeWindowField(maxSizeField_minSizeCell, CMinArea)).x <= sizeScreen.x) {
      maxSizeField_minSizeCell.x++;
   }
   maxSizeField_minSizeCell.x--;
   maxSizeField_minSizeCell.y++;
   while (gpMosaic->GetSizeWindowProject(gpMosaic->GetSizeWindowField(maxSizeField_minSizeCell, CMinArea)).y <= sizeScreen.y) {
      maxSizeField_minSizeCell.y++;
   }
   maxSizeField_minSizeCell.y--;

   curMines     = gpMosaic->GetMines();
   curSizeField = gpMosaic->GetSizeField();
   mustFree = gpMosaic->GetField(0,0)->GetNeighborNumber()+1;
   maxMines = curSizeField.x*curSizeField.y-mustFree;
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS  , 0L, curMines);
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X   , UDM_SETRANGE, 0L, MAKELPARAM(maxSizeField_minSizeCell.x, minSizeField.x));
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y   , UDM_SETRANGE, 0L, MAKELPARAM(maxSizeField_minSizeCell.y, minSizeField.y));
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X   , UDM_SETPOS  , 0L, curSizeField.x);
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y   , UDM_SETPOS  , 0L, curSizeField.y);

   hMenu = CreatePopupMenu();
   CreateMenuItem(hMenu, TEXT("Beginner"    ), 0, ID_MENU_GAME_BEGINNER    , NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, TEXT("Amateur"     ), 0, ID_MENU_GAME_AMATEUR     , NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, TEXT("Professional"), 0, ID_MENU_GAME_PROFESSIONAL, NULL, FALSE, MFT_STRING);
   CreateMenuItem(hMenu, TEXT("Crazy"       ), 0, ID_MENU_GAME_CRAZY       , NULL, FALSE, MFT_STRING);

 //defWndProc_PopupMenu = (WNDPROC)SetWindowLong(hMenu, GWL_WNDPROC, (LONG)newWndProc_PopupMenu);

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_CUSTOMSKILL_EDIT_X:
      if (codeNotify == EN_CHANGE) {
         curSizeField.x = SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_GETPOS, 0L, 0L);
         if (curSizeField.x > maxSizeField_minSizeCell.x) {
            curSizeField.x -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, curSizeField.x);
         }
         maxMines = curSizeField.x*curSizeField.y-mustFree;
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
         if (curMines > maxMines)
            SendMessage(hwnd, WM_COMMAND, MAKEWPARAM(ID_DIALOG_CUSTOMSKILL_EDIT_MINE, EN_CHANGE), 0L);
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_EDIT_Y:
      if (codeNotify == EN_CHANGE) {
         curSizeField.y = SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_GETPOS, 0L, 0L);
         if (curSizeField.y > maxSizeField_minSizeCell.y) {
            curSizeField.y -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, curSizeField.y);
         }
         maxMines = curSizeField.x*curSizeField.y-mustFree;
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETRANGE, 0L, MAKELPARAM(maxMines, minMines));
         if (curMines > maxMines)
            SendMessage(hwnd, WM_COMMAND, MAKEWPARAM(ID_DIALOG_CUSTOMSKILL_EDIT_MINE, EN_CHANGE), 0L);
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_EDIT_MINE:
      if (codeNotify == EN_CHANGE) {
         curMines = SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_GETPOS, 0L, 0L);
         if (curMines > maxMines) {
            curMines -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS, 0L, curMines);
         }
      }
      return;
   case ID_DIALOG_CUSTOMSKILL_FULLSCREEN1:
      if (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, BM_GETCHECK, 0L, 0L)) {
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, maxSizeField_curSizeCell.x);
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, maxSizeField_curSizeCell.y);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), false);
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, BM_SETCHECK, BST_UNCHECKED, 0L);
         return;
      }
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), true);
      return;
   case ID_DIALOG_CUSTOMSKILL_FULLSCREEN2:
      if (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN2, BM_GETCHECK, 0L, 0L)) {
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X, UDM_SETPOS, 0L, maxSizeField_minSizeCell.x);
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y, UDM_SETPOS, 0L, maxSizeField_minSizeCell.y);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), false);
         EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), false);
         SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_FULLSCREEN1, BM_SETCHECK, BST_UNCHECKED, 0L);
         return;
      }
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_X), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_Y), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_X), true);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_CUSTOMSKILL_EDIT_Y), true);
      return;
   case IDCANCEL:
      resultDialog = IDCANCEL;
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      {
      resultDialog = IDOK;
      gpMosaic->SetMines(curMines);
      gpMosaic->SetSizeField(curSizeField);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      }
      return;
   }
}

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   DestroyMenu(hMenu);
   EndDialog(hwnd, resultDialog);
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

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void Cls_OnRButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   DWORD xyPos = GetMessagePos();
   int id = TrackPopupMenu(
      hMenu,
      TPM_LEFTALIGN | TPM_TOPALIGN | TPM_RETURNCMD | TPM_LEFTBUTTON | TPM_RIGHTBUTTON,
      LOWORD(xyPos), HIWORD(xyPos), 0,
      hwnd,
      NULL);
   TeSkillLevel skill;
   switch (id) {
   case ID_MENU_GAME_BEGINNER    :
   case ID_MENU_GAME_AMATEUR     :
   case ID_MENU_GAME_PROFESSIONAL:
   case ID_MENU_GAME_CRAZY       : skill = (TeSkillLevel)(id-ID_MENU_GAME_BEGINNER); break;
   default: return;
   }
   curMines = DefineNumberMines(skill, gpMosaic->GetFigure(), curSizeField);
   SendDlgItemMessage(hwnd, ID_DIALOG_CUSTOMSKILL_SPIN_MINE, UDM_SETPOS, 0L, curMines);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void Cls_OnPaint(HWND hwnd){
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (gpMosaic->GetSkin().toAll)
      nsEraseBk::FillWnd(hwnd, gpMosaic->GetSkin().colorBk, false);
}

// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpMosaic->GetSkin().toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, gpMosaic->GetSkin().colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

HBRUSH Cls_OnCtlColorDlg(HWND hwnd, HDC hdc, HWND hwndChild, int type) {
   static HBRUSH hBrush = NULL;
   static COLORREF colorBk = 0;
   DeleteObject(hBrush);
   hBrush = CreateSolidBrush(colorBk+=100);
   Sleep(100);
   //FORWARD_WM_CTLCOLORDLG(hwnd, hdc, hwndChild, PostMessage);
   InvalidateRect(hwnd, NULL, TRUE);
   return hBrush;
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG , Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND    , Cls_OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT      , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND , Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE      , Cls_OnClose);
   HANDLE_MSG(hDlg, WM_RBUTTONDOWN, Cls_OnRButtonDown);
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

} //namespace nsCustomSkill
