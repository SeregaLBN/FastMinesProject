////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Assistant.cpp"
// обработка диалогового окна "Assistant Options"
////////////////////////////////////////////////////////////////////////////////
#include ".\Assistant.h"
#include <windowsx.h>
#include <commctrl.h>
#include "..\ID_resource.h"
#include "..\TcMosaic.h"
#include "..\EraseBk.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsAssistant {

HWND hDlg;
TsAssistant Assistant;
const WORD maxTimeoutUnactive = 30000; // max = 32767 == 0x7FFF
const WORD minTimeoutUnactive = 1000;
const WORD maxTimeoutJob      = 5000;
const WORD minTimeoutJob      = 100;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_UNACTIVE     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_JOB          ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_MILISEC1     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_STATIC_MILISEC2     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_EDIT_UNACTIVE       ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_EDIT_JOB            ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_SPIN_UNACTIVE       ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ASSISTANT_SPIN_JOB            ,       gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK                                    , hDlg, gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_UNACTIVE     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_JOB          );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_MILISEC1     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ASSISTANT_STATIC_MILISEC2     );
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

   Assistant = gpMosaic->GetAssistant();

   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETRANGE, 0L, MAKELPARAM(maxTimeoutUnactive, minTimeoutUnactive));
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETPOS  , 0L, Assistant.timeoutUnactive);
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB     , UDM_SETRANGE, 0L, MAKELPARAM(maxTimeoutJob, minTimeoutJob));
   SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB     , UDM_SETPOS  , 0L, Assistant.timeoutJob);

   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , BM_SETCHECK, (WPARAM)(Assistant.autoStart   ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , BM_SETCHECK, (WPARAM)(Assistant.stopJob     ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, BM_SETCHECK, (WPARAM)(Assistant.ignorePause ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);
   SendDlgItemMessage(hDlg, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , BM_SETCHECK, (WPARAM)(Assistant.beep        ? BST_CHECKED : BST_UNCHECKED), (LPARAM)0);

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_ASSISTANT_EDIT_UNACTIVE:
      if (codeNotify == EN_CHANGE) {
         Assistant.timeoutUnactive = SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_GETPOS, 0L, 0L);
         if(Assistant.timeoutUnactive > maxTimeoutUnactive) {
            Assistant.timeoutUnactive -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_UNACTIVE, UDM_SETPOS, 0L, Assistant.timeoutUnactive);
         }
      }
      return;
   case ID_DIALOG_ASSISTANT_EDIT_JOB:
      if (codeNotify == EN_CHANGE) {
         Assistant.timeoutJob = SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB, UDM_GETPOS, 0L, 0L);
         if(Assistant.timeoutJob > maxTimeoutJob) {
            Assistant.timeoutJob -= 0x10000;
            SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_SPIN_JOB, UDM_SETPOS, 0L, Assistant.timeoutJob);
         }
      }
      return;
   case IDOK:
      Assistant.autoStart   = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_AUTOSTART  , BM_GETCHECK, 0L, 0L));
      Assistant.stopJob     = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_STOPJOB    , BM_GETCHECK, 0L, 0L));
      Assistant.ignorePause = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_IGNOREPAUSE, BM_GETCHECK, 0L, 0L));
      Assistant.beep        = (BST_CHECKED == SendDlgItemMessage(hwnd, ID_DIALOG_ASSISTANT_CHECKBOX_BEEPCLICK  , BM_GETCHECK, 0L, 0L));

      gpMosaic->SetAssistant(Assistant);
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
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

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
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

} // namespace nsAssistant
