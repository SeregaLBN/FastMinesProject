////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "About.cpp"
// обработка диалогового окна "About"
////////////////////////////////////////////////////////////////////////////////
#include ".\About.h"
#include <windowsx.h>
#include "..\ID_resource.h"
#include "..\TcMosaic.h"
#include "..\EraseBk.h"
#include ".\Registration.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsAbout {

HWND hDlg;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_ABOUT_PRODUCTNAME, gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_VERSION    , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_COPYRIGHT  , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_COMMENTS   , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_RESPONSE   , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICQ        , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICQADDRESS , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FIDO       , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FIDOADDRESS, gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FMSITE     , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_UKRPOST    , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_YAHOO      , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_SITE       , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICON       , gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK, hDlg                 , gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////

inline BOOL ProductNameDialog(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   case WM_INITDIALOG:
      SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_PRODUCTNAME);
      return FALSE;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   CreateDialog(ghInstance, TEXT("ProductNameDialog"), hwnd, (DLGPROC)ProductNameDialog);

   if (nsRegistration::isRegister(NULL, NULL))
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_COMMENTS), TEXT("            Registered"));
   else
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_COMMENTS), TEXT("           Unregistered"));

   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_VERSION    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_COPYRIGHT  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_COMMENTS   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_RESPONSE   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICQ        );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICQADDRESS );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_FIDO       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_FIDOADDRESS);
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_FMSITE     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_UKRPOST    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_YAHOO      );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_SITE       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICON       );
   SETNEWWNDPROC(hwnd, IDOK);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_ABOUT_UKRPOST:
      ShellExecute(hwnd, TEXT("open"), TEXT("mailto:Serg_Krivulja@UkrPost.net"), NULL, NULL, SW_RESTORE);
      return;
   case ID_DIALOG_ABOUT_YAHOO:
      ShellExecute(hwnd, TEXT("open"), TEXT("mailto:Serg_Krivulja@Yahoo.com"), NULL, NULL, SW_RESTORE);
      return;
   case ID_DIALOG_ABOUT_FIDOADDRESS:
      ShellExecute(hwnd, TEXT("open"), TEXT("mailto:Krivulja.Serg@p271.f444.n463.z2.fidonet.org"), NULL, NULL, SW_RESTORE);
      return;
   case ID_DIALOG_ABOUT_SITE:
      ShellExecute(hwnd, TEXT("open"), TEXT("http://kserg77.chat.ru"), NULL, NULL, SW_RESTORE);
      return;
   case IDCANCEL:
   case IDOK:
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

} // namespace nsAbout
