////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "AssistantDlg.cpp"
// обработка диалогового окна "Assistant Options"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "AssistantDlg.h"
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
namespace nsAssistant {

HWND hDlg;
CAssistantInfo Assistant; // здесь таймауты задаются в миллисекундах
const WORD maxTimeoutUnactive = 3600; // в секундах! max = 32767 == 0x7FFF
const WORD minTimeoutUnactive = 1;    // в секундах!
const WORD maxTimeoutJob      = 5000; // в милисекундах!
const WORD minTimeoutJob      = 100;  // в милисекундах!

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_UNACTIVE     ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_JOB          ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_SECOND       ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_MILISECOND   ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_EDIT_UNACTIVE       ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_EDIT_JOB            ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_SPIN_UNACTIVE       ,       gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_SPIN_JOB            ,       gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK                                    , hDlg, gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   {
      SetWindowText(GetDlgItem(hwnd, IDOK), CLang::m_StrArr[IDS__OK    ]);
      SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_ASSISTANT]);

      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_STATIC_UNACTIVE     ), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__TIMEOUT_USER_UNACTIVE]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_STATIC_JOB          ), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__TIMEOUT_JOB          ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_STATIC_SECOND       ), CLang::m_StrArr[IDS__SECOND                                 ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_STATIC_MILISECOND   ), CLang::m_StrArr[IDS__MILISECOND                             ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  ), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__NEW_GAME_AUTOSTART   ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    ), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__STOPJOB              ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__IGNOREPAUSE          ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  ), CLang::m_StrArr[IDS__DIALOG_ASSISTANT__BEEPCLICK            ]);
   }
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_UNACTIVE     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_JOB          );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_SECOND       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_MILISECOND   );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_EDIT_UNACTIVE       );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_EDIT_JOB            );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB            );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE);
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  );
   SETNEWWNDPROC(hwnd, IDOK);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   Assistant = gpFM2Proj->GetAssistant();

   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETRANGE, 0L, MAKELPARAM(maxTimeoutUnactive, minTimeoutUnactive));
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETPOS  , 0L, Assistant.m_iTimeoutUnactive/100); // из миллисекунд в секунды
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB     , UDM_SETRANGE, 0L, MAKELPARAM(maxTimeoutJob, minTimeoutJob));
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB     , UDM_SETPOS  , 0L, Assistant.m_iTimeoutJob);

   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , BM_SETCHECK, (WPARAM)(Assistant.m_bAutoStart   ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , BM_SETCHECK, (WPARAM)(Assistant.m_bStopJob     ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, BM_SETCHECK, (WPARAM)(Assistant.m_bIgnorePause ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , BM_SETCHECK, (WPARAM)(Assistant.m_bBeep        ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);

   return TRUE;
}

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_ASSISTANT_EDIT_UNACTIVE:
      if (codeNotify == EN_CHANGE) {
         int iTimeoutUnactive = SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_GETPOS, 0L, 0L);
         if(iTimeoutUnactive > maxTimeoutUnactive) {
            iTimeoutUnactive  -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETPOS, 0L, iTimeoutUnactive);
         }
         Assistant.m_iTimeoutUnactive = iTimeoutUnactive*100; // из секунд в миллисекунды
      }
      return;
   case ID_DIALOG_ASSISTANT_EDIT_JOB:
      if (codeNotify == EN_CHANGE) {
         Assistant.m_iTimeoutJob = SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB, UDM_GETPOS, 0L, 0L);
         if(Assistant.m_iTimeoutJob > maxTimeoutJob) {
            Assistant.m_iTimeoutJob -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB, UDM_SETPOS, 0L, Assistant.m_iTimeoutJob);
         }
      }
      return;
   case IDOK:
      Assistant.m_bAutoStart   = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , BM_GETCHECK, 0L, 0L));
      Assistant.m_bStopJob     = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , BM_GETCHECK, 0L, 0L));
      Assistant.m_bIgnorePause = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, BM_GETCHECK, 0L, 0L));
      Assistant.m_bBeep        = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , BM_GETCHECK, 0L, 0L));

      gpFM2Proj->SetAssistant(Assistant);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_CLOSE
void OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
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

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_EX_CTLCOLOR(hDlg, gpFM2Proj->GetSkin());
   }
   return FALSE;
}

} // namespace nsAssistant
