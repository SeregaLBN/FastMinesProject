////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PlayerName.cpp"
// обработка диалогового окна "Players Administration"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "PlayerName.h"
#include <WindowsX.h>
#include "Statistics.h"
#include "../ID_resource.h"
#include "../EraseBk.h"
#include "../Lang.h"
#include "../FastMines2.h"
#include "../Control/Table.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsPlayerName {

HWND hDlg;
CTable* pTable;
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
   TCHAR szPlayerName[MAX_PLAYER_NAME_LENGTH];
   TCHAR szPassword  [MAX_PASSWORD_LENGTH];
   int resultDlg;

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
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_TEXT          , gpFM2Proj->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      , gpFM2Proj->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  , gpFM2Proj->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_TEXT        , gpFM2Proj->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    , gpFM2Proj->GetSkin())
   WNDPROC_STATIC(ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION, gpFM2Proj->GetSkin())
   WNDPROC_BUTTON(IDOK                             , hDlg, gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

   BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
      switch (msg){
      HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
      HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
      HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
      HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
      HANDLE_WM_CTLCOLOR(hDlg);
      }
      return FALSE;
   }

   // WM_INITDIALOG
   BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
      hDlg = hwnd;
      resultDlg = IDCANCEL;

   {
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__PASSWORD    ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__CONFIRMATION]);
      SetWindowText(GetDlgItem(hwnd, IDCANCEL                               ), CLang::m_StrArr[IDS__CANCEL                           ]);
   }

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
         SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_PASSWORD]);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT        ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT          ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD), 11,11, 80,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD  ), 11,28,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
         {
            RECTEX rect(0,0,241,116);
            const SIZE sizeScreen = GetScreenSize();
            MoveWindow(hwnd,
               sizeScreen.cx/2 - rect.width ()/2,
               sizeScreen.cy/2 - rect.height()/2,
               rect.width (),
               rect.height(),
               FALSE);
         }
         break;
      case ePlayerNewPassword:
         SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPASSWORD]);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT  ), SW_HIDE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), 11,11,212,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), 11,28,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), 11,58,212,13, FALSE);
         MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), 11,80,212,23, FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 113, 74, 24, FALSE);
         {
            RECTEX rect(0,0,241,170);
            const SIZE sizeScreen = GetScreenSize();
            MoveWindow(hwnd,
               sizeScreen.cx/2 - rect.width ()/2,
               sizeScreen.cy/2 - rect.height()/2,
               rect.width (),
               rect.height(),
               FALSE);
         }
         break;
      case ePlayerNew:
         SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPLAYER]);
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_NAME]);
         EnableWindow(GetDlgItem(hwnd, IDOK), FALSE);
         break;
      case ePlayerRename:
         SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_NEW_NAME]);
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_TEXT), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEW_NAME]);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_PASSWORD    ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_STATIC_CONFIRMATION), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD      ), SW_HIDE);
         ShowWindow(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION  ), SW_HIDE);
         SetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szPlayerName);
         EnableWindow(GetDlgItem(hwnd, IDOK), FALSE);
         MoveWindow(GetDlgItem(hwnd, IDOK), 150, 60, 74, 24, FALSE);
         {
            RECTEX rect(0,0,241,116);
            const SIZE sizeScreen = GetScreenSize();
            MoveWindow(hwnd,
               sizeScreen.cx/2 - rect.width ()/2,
               sizeScreen.cy/2 - rect.height()/2,
               rect.width (),
               rect.height(),
               FALSE);
         }
         break;
      }

      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT        ), MAX_PLAYER_NAME_LENGTH-1);
      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD    ), MAX_PASSWORD_LENGTH   -1);
      Edit_LimitText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION), MAX_PASSWORD_LENGTH   -1);
      return TRUE;
   }

   // WM_COMMAND
   void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
      switch (id) {
      case ID_DIALOG_INPUTTEXT_EDIT_TEXT:
         switch (codeNotify) {
         case EN_CHANGE:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_TEXT), szPlayerName, MAX_PLAYER_NAME_LENGTH);
            EnableWindow(GetDlgItem(hwnd, IDOK), szPlayerName[0] && (0>nsStatistics::FindName(szPlayerName)));
            break;
         }
         return;
      case IDOK:
         TCHAR szPassword2[MAX_PASSWORD_LENGTH];
         switch (OperationAtPlayer) {
         case ePlayerNew:
         case ePlayerRename:
            if (!lstrcmpi(szPlayerName, SZ_ASSISTANT_NAME_DEFAULT)) {
               MessageBox(hwnd,
                  CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__CANT_USE_VIRT_NAME],
                  CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__INVALID_NAME],
                  MB_OK | MB_ICONERROR);
               return;
            }
            if (OperationAtPlayer == ePlayerRename) break;
         case ePlayerNewPassword:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD    ), szPassword , MAX_PASSWORD_LENGTH);
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_CONFIRMATION), szPassword2, MAX_PASSWORD_LENGTH);
            if (lstrcmp(szPassword, szPassword2)) {
               MessageBox(hwnd,
                  CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__PASSWORD_NOT_CONFIRMED],
                  CLang::m_StrArr[IDS__ERROR],
                  MB_OK | MB_ICONERROR);
               return;
            }
            break;
         case ePlayerSelect:
            GetWindowText(GetDlgItem(hwnd, ID_DIALOG_INPUTTEXT_EDIT_PASSWORD), szPassword , MAX_PASSWORD_LENGTH);
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
   BOOL OnEraseBkgnd(HWND hwnd, HDC hdc) {
      if (!gpFM2Proj->GetSkin().m_bToAll)
         return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
      return nsEraseBk::OnEraseBkgnd(hwnd, hdc, gpFM2Proj->GetSkin().m_colorBk);
   }
#endif // REPLACEBKCOLORFROMFILLWINDOW

   // WM_CLOSE
   void OnClose(HWND hwnd){
      EndDialog(hwnd, resultDlg);
   }

} // nsPlayerNameOperation

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER     , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD   , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_RENAME        , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE        , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK                                        , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDCANCEL                                    , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART, hDlg, gpFM2Proj->GetSkin())
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
   pTable->SetText(0,0, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__TABLE]);
   {
      for (int j=1; j<pTable->GetRowNumber(); j++) {
         pTable->SetText (0, j, TEXT(""));
         pTable->SetImage(0, j, NULL);
      }
      for (j=0; j<nsStatistics::NumberPlayers(); j++) {
         pTable->SetText      (0, j+1, nsStatistics::GetPlayers(j));
         pTable->SetFormatText(0, j+1, DT_LEFT | DT_VCENTER | DT_SINGLELINE);
      }
      int index = nsStatistics::FindName(gpFM2Proj->GetPlayerName());
      if (index>=0)
         pTable->SetCurrentCell(0, index+1, true);
      else
         pTable->SetCurrentCell(0,1);
      index = nsStatistics::FindName(nsPlayerName::SZ_ASSISTANT_NAME_DEFAULT);
      if (index>=0)
         pTable->SetImage(0, index+1, gpFM2Proj->GetImageBtnPause(3));
   }
}

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   {
      SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER]);

      SetWindowText(GetDlgItem(hwnd, IDOK                                        ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__SELECT     ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER     ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPLAYER  ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD   ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPASSWORD]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_RENAME        ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__RENAME     ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE        ), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__REMOVE     ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART), CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__AUTOSTART  ]);
      SetWindowText(GetDlgItem(hwnd, IDCANCEL                                    ), CLang::m_StrArr[IDS__CANCEL                          ]);
   }

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPLAYER     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_NEWPASSWORD   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_RENAME        );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTON_REMOVE        );
   SETNEWWNDPROC(hwnd, IDOK                                        );
   SETNEWWNDPROC(hwnd, IDCANCEL                                    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   pTable = new CTable;
   pTable->Create(hDlg, ID_DIALOG_SELECTPLAYER_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetBkColor(gpFM2Proj->GetSkin().m_colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   LoadTable();
   MoveTable();

   Button_SetCheck(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART), gpFM2Proj->GetAutoloadAdmin() ? BST_CHECKED : BST_UNCHECKED);

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
   if (!lstrcmpi(pTable->GetCurrentText(), SZ_ASSISTANT_NAME_DEFAULT)) {
      MessageBox(hDlg,
         CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__THIS_VIRTUAL_PLAYER],
         CLang::m_StrArr[IDS__ERROR],
         MB_OK | MB_ICONERROR);
      return false;
   }
   TCHAR szPassword[MAX_PASSWORD_LENGTH] = {0};
   nsStatistics::GetPassword(pTable->GetCurrentText(), szPassword);
   if (szPassword[0]) {
      OperationAtPlayer = ePlayerSelect;
      if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hDlg, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
         if (lstrcmp(nsPlayerNameOperation::szPassword, szPassword)) {
            MessageBox(hDlg,
               CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__INCORRECT_PASSWORD],
               CLang::m_StrArr[IDS__ERROR],
               MB_OK | MB_ICONERROR);
            return false;
         }
         return true;
      } else
         return false;
   }
   return true;
}

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
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
         if (!lstrcmp(pTable->GetCurrentText(), gpFM2Proj->GetPlayerName())) {
            // если удаляется текущий игрок то надо будет обязательно выбрать др. игрока
            closeWnd = false;
            SetWindowLong(hwnd, GWL_STYLE, WS_SYSMENU ^ GetWindowLong(hwnd, GWL_STYLE));
            EnableWindow(GetDlgItem(hwnd, IDCANCEL), FALSE);
            DrawMenuBar(hwnd);
         }
         nsStatistics::Remove(pTable->GetCurrentText());
         LoadTable();
         MoveTable();
      }
      return;
   case ID_DIALOG_SELECTPLAYER_BUTTON_RENAME:
      if (!pTable->GetCurrentText()[0]) return;
      if (VerifyPassword()) {
         OperationAtPlayer = ePlayerRename;
         lstrcpy(nsPlayerNameOperation::szPlayerName, pTable->GetCurrentText());
         if (IDOK == DialogBox(ghInstance, TEXT("InputText"), hwnd, (DLGPROC)nsPlayerNameOperation::DialogProc)) {
            if (!lstrcmp(pTable->GetCurrentText(), gpFM2Proj->GetPlayerName()))
               gpFM2Proj->SetPlayerName(nsPlayerNameOperation::szPlayerName);
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
         nsStatistics::CSttstcSubRecord statisticsResult;
         nsStatistics::InsertResult(
            statisticsResult,
            nsMosaic::mosaicTriangle1,    // неважно какая фигура
            nsMosaic::skillLevelBeginner, // неважно какой уровень
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
      gpFM2Proj->SetPlayerName(pTable->GetCurrentText());
      closeWnd = true;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void OnPaint(HWND hwnd){
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (gpFM2Proj->GetSkin().m_bToAll)
      nsEraseBk::FillWnd(hwnd, gpFM2Proj->GetSkin().m_colorBk, false);
}

// WM_ERASEBKGND
BOOL OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpFM2Proj->GetSkin().m_bToAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::OnEraseBkgnd(hwnd, hdc, gpFM2Proj->GetSkin().m_colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_CLOSE
void OnClose(HWND hwnd){
   if (!closeWnd) return;
   closeWnd  = true;
   firstLoad = false;
   delete pTable;
   EndDialog(hwnd, Button_GetCheck(GetDlgItem(hwnd, ID_DIALOG_SELECTPLAYER_BUTTONCHECK_AUTOSTART)));
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   //g_Logger.PutMsg(TEXT("msg PlayerName "), msg);
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
   HANDLE_WM_CTLCOLOR(hDlg);
   case WM_USER+1:
      SetFocus(pTable->GetHandle());
   }
   return FALSE;
}

} // namespace nsPlayerName
