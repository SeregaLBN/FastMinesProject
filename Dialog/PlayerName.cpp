////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PlayerName.cpp"
// обработка диалогового окна "Players Administration"
////////////////////////////////////////////////////////////////////////////////
#include ".\PlayerName.h"
#include <windowsx.h>
#include "..\ID_resource.h"
#include "..\TcMosaic.h"
#include "..\EraseBk.h"
#include ".\Statistics.h"
#include "..\Control\TcTable.h"
#include ".\Info.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsPlayerName {

HWND hDlg;
TcTable* pTable;
bool firstLoad = false;
bool closeWnd  = true;

enum {
   ePlayerSelect,
   ePlayerNew,
   ePlayerNewPassword,
   ePlayerRename,
   ePlayerRemove
} OperationAtPlayer;


namespace nsPlayerNameOperation {
   ////////////////////////////////////////////////////////////////////////////////
   //                            types & variables
   ////////////////////////////////////////////////////////////////////////////////
   HWND hDlg;
   TCHAR szPlayerName[maxPlayerNameLength];
   TCHAR szPassword  [maxPasswordLength];
   int resultDlg;

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
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_TEXT          , gpMosaic->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      , gpMosaic->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  , gpMosaic->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_TEXT        , gpMosaic->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    , gpMosaic->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION, gpMosaic->GetSkin())
   WNDPROC_BUTTON(IDOK                             , hDlg, gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

   BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
      switch (msg){
      HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
      HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
      HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
      HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
      HANDLE_WM_CTLCOLOR(hDlg);
      }
      return FALSE;
   }

   // WM_INITDIALOG
   BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
      hDlg = hwnd;
      resultDlg = IDCANCEL;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
    //SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT          );
    //SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      );
    //SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  );
      SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT        );
      SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    );
      SETNEWWNDPROC(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION);
      SETNEWWNDPROC(hwnd, IDOK                                   );
#endif // REPLACEBKCOLORFROMFILLWINDOW

      switch (OperationAtPlayer) {
      case ePlayerSelect:
         SetWindowText(hwnd, TEXT("Enter password"));
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT        ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT          ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD), 11,11, 80,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD  ), 11,28,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
         {
            RECT rect = {0,0,241,116};
            const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                                      GetSystemMetrics(SM_CYSCREEN)};
            MoveWindow(hwnd,
               sizeScreen.x/2 - (rect.right -rect.left)/2,
               sizeScreen.y/2 - (rect.bottom-rect.top )/2,
               rect.right -rect.left,
               rect.bottom-rect.top,
               FALSE);
         }
         break;
      case ePlayerNewPassword:
         SetWindowText(hwnd, TEXT("New password"));
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT  ), SW_HIDE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), 11,11, 80,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), 11,28,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), 11,58, 80,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), 11,80,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 113, 74, 24, FALSE);
         {
            RECT rect = {0,0,241,170};
            const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                                      GetSystemMetrics(SM_CYSCREEN)};
            MoveWindow(hwnd,
               sizeScreen.x/2 - (rect.right -rect.left)/2,
               sizeScreen.y/2 - (rect.bottom-rect.top )/2,
               rect.right -rect.left,
               rect.bottom-rect.top,
               FALSE);
         }
         break;
      case ePlayerNew:
         SetWindowText(hwnd, TEXT("New player"));
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), TEXT("Enter name:"));
         EnableWindow(GetDlgItem(hwnd, IDOK), FALSE);
         break;
      case ePlayerRename:
         SetWindowText(hwnd, TEXT("Enter new Name"));
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), TEXT("New name:"));
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szPlayerName);
         EnableWindow(GetDlgItem(hwnd, IDOK), FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
         {
            RECT rect = {0,0,241,116};
            const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                                      GetSystemMetrics(SM_CYSCREEN)};
            MoveWindow(hwnd,
               sizeScreen.x/2 - (rect.right -rect.left)/2,
               sizeScreen.y/2 - (rect.bottom-rect.top )/2,
               rect.right -rect.left,
               rect.bottom-rect.top,
               FALSE);
         }
         break;
      }

      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT        ), maxPlayerNameLength-1);
      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD    ), maxPasswordLength  -1);
      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION), maxPasswordLength  -1);
      return TRUE;
   }

   // WM_COMMAND
   void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
      switch (id) {
      case ID_DIALOG_INPUTTEXT_EDIT_TEXT:
         switch (codeNotify) {
         case EN_CHANGE:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szPlayerName, maxPlayerNameLength);
            EnableWindow(GetDlgItem(hwnd, IDOK), szPlayerName[0] && (0>nsStatistics::FindName(szPlayerName)));
            break;
         }
         return;
      case IDOK:
         TCHAR szPassword2[maxPasswordLength];
         switch (OperationAtPlayer) {
         case ePlayerNew:
         case ePlayerRename:
            if (!lstrcmpi(szPlayerName, szRobotNameDefault)) {
               MessageBox(hwnd, TEXT("Sorry, this name is used by a virtual!"), TEXT("Invalid name"), MB_OK | MB_ICONERROR);
               return;
            }
            if (OperationAtPlayer == ePlayerRename) break;
         case ePlayerNewPassword:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD    ), szPassword , maxPasswordLength);
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION), szPassword2, maxPasswordLength);
            if (_tcscmp(szPassword, szPassword2)) {
               MessageBox(hwnd, TEXT("Password not confirmed!"), TEXT("Error"), MB_OK | MB_ICONERROR);
               return;
            }
            break;
         case ePlayerSelect:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD    ), szPassword , maxPasswordLength);
            break;
         }
      case IDCANCEL:
         resultDlg = id;
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
      EndDialog(hwnd, resultDlg);
   }

} // nsPlayerNameOperation

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER     , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD   , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_RENAME        , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE        , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK                                        , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDCANCEL                                    , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART, hDlg, gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

inline void MoveTable() {
   RECT wndRect; GetWindowRect(GetDlgItem(hDlg, IDCANCEL), &wndRect);
   MoveWindow(pTable->GetHandle(),
      10, 10, 200, 216+2*GetSystemMetrics(SM_CYFIXEDFRAME),
      TRUE);
   RECT clientRect; GetClientRect(pTable->GetHandle(), &clientRect);
   pTable->SetColWidth(0, clientRect.right-1);
}

inline void LoadTable() {
   pTable->SetColNumber(1);
   pTable->SetRowNumber(max(7+1,nsStatistics::NumberPlayers()+1));
   pTable->SetText(0,0, TEXT("Players"));
   {
      for (int j=1; j<pTable->GetRowNumber(); j++) {
         pTable->SetText (0, j, TEXT(""));
         pTable->SetImage(0, j, NULL);
      }
      for (j=0; j<nsStatistics::NumberPlayers(); j++) {
         pTable->SetText      (0, j+1, nsStatistics::GetPlayers(j));
         pTable->SetFormatText(0, j+1, DT_LEFT | DT_VCENTER | DT_SINGLELINE);
      }
      int index = nsStatistics::FindName(gpMosaic->GetPlayerName());
      if (index>=0)
         pTable->SetCurrentCell(0, index+1, true);
      else
         pTable->SetCurrentCell(0,1);
      index = nsStatistics::FindName(nsPlayerName::szRobotNameDefault);
      if (index>=0)
         pTable->SetImage(0, index+1, gpMosaic->GetImageBtnPause(3));
   }
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_RENAME        );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE        );
   SETNEWWNDPROC(hwnd, IDOK                                        );
   SETNEWWNDPROC(hwnd, IDCANCEL                                    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   pTable = new TcTable;
   pTable->Create(hDlg, ID_DIALOG_SELECTPLAYER_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetColor(gpMosaic->GetSkin().toAll, gpMosaic->GetSkin().colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   LoadTable();
   MoveTable();

   Button_SetCheck(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART), gpMosaic->GetAutoloadAdmin() ? BST_CHECKED : BST_UNCHECKED);
   
   if (firstLoad) {
      closeWnd = false;
      SetWindowLong(hwnd, GWL_STYLE, WS_SYSMENU ^ GetWindowLong(hwnd, GWL_STYLE));
      EnableWindow(GetDlgItem(hwnd, IDCANCEL), FALSE);
   }

   PostMessage(hDlg, WM_USER+1, 0l, 0l); // SetFocus(pTable->GetHandle());

   return TRUE;
}

bool VerifyPassword() {
   if (!pTable->GetCurrentText()[0]) {
      FORWARD_WM_COMMAND(hDlg, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER, hDlg, 0, PostMessage);
      return false;
   }
   if (!lstrcmpi(pTable->GetCurrentText(), szRobotNameDefault)) {
      MessageBox(hDlg, TEXT("Sorry, this is a virtual player!"), TEXT("Error"), MB_OK | MB_ICONERROR);
      return false;
   }
   TCHAR szPassword[maxPasswordLength]; szPassword[0] = TEXT('\0');
   nsStatistics::GetPassword(pTable->GetCurrentText(), szPassword);
   if (szPassword[0]) {
      OperationAtPlayer = ePlayerSelect;
      if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hDlg, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
         if (_tcscmp(nsPlayerNameOperation::szPassword, szPassword)) {
            MessageBox(hDlg, TEXT("Incorrect password"), TEXT("Error"), MB_OK | MB_ICONERROR);
            return false;
         }
         return true;
      } else return false;
   }
   return true;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_SELECTPLAYER_TABLE:
      switch (codeNotify) {
      case NC_TABLE_SELECT_CELL: FORWARD_WM_COMMAND(hwnd, IDOK                                , hwnd, 0, PostMessage); break;
      case NC_TABLE_DELETE_CELL: FORWARD_WM_COMMAND(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE, hwnd, 0, PostMessage); break;
      }
      return;
   case ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE:
      if (!pTable->GetCurrentText()[0]) return;
      if (VerifyPassword()) {
         nsStatistics::Remove(pTable->GetCurrentText());
         LoadTable();
         MoveTable();
      }
      return;
   case ID_DIALOG_SELECTPLAYER_BUTTON_RENAME:
      if (!pTable->GetCurrentText()[0]) return;
      if (VerifyPassword()) {
         OperationAtPlayer = ePlayerRename;
         _tcscpy(nsPlayerNameOperation::szPlayerName, pTable->GetCurrentText());
         if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hwnd, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
            if (!_tcscmp(pTable->GetCurrentText(), gpMosaic->GetPlayerName()))
               gpMosaic->SetPlayerName(nsPlayerNameOperation::szPlayerName);
            nsStatistics::Rename(pTable->GetCurrentText(), nsPlayerNameOperation::szPlayerName);
            pTable->SetText(0, pTable->GetCurrentCell().y, nsPlayerNameOperation::szPlayerName);
         }
      }
      return;
   case ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD:
      if (!pTable->GetCurrentText()[0]) return;
      if (VerifyPassword()) {
         OperationAtPlayer = ePlayerNewPassword;
         if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hwnd, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
            nsStatistics::SetPassword(pTable->GetCurrentText(), nsPlayerNameOperation::szPassword);
            pTable->SetCurrentCell(0, 1+nsStatistics::FindName(pTable->GetCurrentText()), true);
         }
      }
      return;
   case ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER:
      OperationAtPlayer = ePlayerNew;
      if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hwnd, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
         nsStatistics::TsSttstcSubRecord statisticsResult;
         nsStatistics::InsertResult(
            statisticsResult,
            figureTriangle1,    // неважно какая фигура
            skillLevelBeginner, // неважно какой уровень
            nsPlayerNameOperation::szPlayerName
         );
         LoadTable();
         MoveTable();
         nsStatistics::SetPassword(nsPlayerNameOperation::szPlayerName, nsPlayerNameOperation::szPassword);
         pTable->SetCurrentCell(0, 1+nsStatistics::FindName(nsPlayerNameOperation::szPlayerName), true);
      }
      return;
   case IDOK:
      if (!VerifyPassword()) return;
      gpMosaic->SetPlayerName(pTable->GetCurrentText());
      closeWnd  = true;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
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

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   if (!closeWnd) return;
   closeWnd  = true;
   firstLoad = false;
   delete pTable;
   EndDialog(hwnd, Button_GetCheck(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART)));
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   //nsInfo::AddValue(TEXT("msg PlayerName = 0x"), msg, 16);
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
   HANDLE_WM_CTLCOLOR(hDlg);
   case WM_USER+1:
      SetFocus(pTable->GetHandle());
   }
   return FALSE;
}

} // namespace nsPlayerName
