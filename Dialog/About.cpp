////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "About.cpp"
// обработка диалогового окна "About"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "About.h"
#include <WindowsX.h>
#include <ShellApi.h>
#include "../ID_resource.h"
#include "../Image.h"
#include "../Control/ButtonImageCheck.h"
#include "../EraseBk.h"
#include "../Lang.h"
#include "../FastMines2.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsAbout {

HWND           hDlg;
CButtonImageCheck *pBtnProj;
CImage            *pImgProj;
//CButtonImage *pBtnMail;
//CImage       *pImgMail;
bool check;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_ABOUT_PRODUCTNAME, gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_VERSION    , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_COPYRIGHT  , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_COMMENTS   , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_RESPONSE   , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICQ        , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICQADDRESS , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FIDO       , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FIDOADDRESS, gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_FMSITE     , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_UKRPOST    , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_SITE       , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_ABOUT_ICON_PROJ  , gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK, hDlg                 , gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////

inline BOOL ProductNameDialog(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   case WM_INITDIALOG:
      SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_PRODUCTNAME);
      return FALSE;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_EX_CTLCOLOR(hDlg, gpFM2Proj->GetSkin());
   }
   return FALSE;
}

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   HWND hWndProduct = CreateDialog(ghInstance, TEXT("ProductNameDialog"), hwnd, (DLGPROC)ProductNameDialog);

   {
      SetWindowText(GetDlgItem(hwnd, IDOK    ), CLang::m_StrArr[IDS__OK    ]);
      SetWindowText(GetDlgItem(hwnd, IDCANCEL), CLang::m_StrArr[IDS__CANCEL]);
      SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_ABOUT]);

      SetWindowText(GetDlgItem(hWndProduct, ID_DIALOG_ABOUT_PRODUCTNAME), CLang::m_StrArr[IDS__LOGO]);

      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_VERSION  ), CLang::m_StrArr[IDS__DIALOG_ABOUT__VERSION  ]+ TEXT(" ") + TEXT(ID_VERSIONINFO_VERSION4));
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_COPYRIGHT), CLang::m_StrArr[IDS__DIALOG_ABOUT__COPYRIGHT]+ TEXT("  © - Sergey Krivulya (KSerg)"));
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_COMMENTS ), CLang::m_StrArr[IDS__DIALOG_ABOUT__COMMEMTS ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_RESPONSE ), CLang::m_StrArr[IDS__DIALOG_ABOUT__RESPONSE ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_ABOUT_FMSITE   ), CLang::m_StrArr[IDS__DIALOG_ABOUT__SITE     ]);
   }

   hDlg = hwnd;

   pBtnProj = new CButtonImageCheck;
 //pBtnMail = new CButtonImage;
   pBtnProj->Create(hDlg, ID_DIALOG_ABOUT_ICON_PROJ);
 //pBtnMail->Create(hDlg, ID_DIALOG_ABOUT_ICON_MAIL);
   MoveWindow(pBtnProj->GetHandle(), 25,25,2+1.5f*32,2+1.5f*32, TRUE);
 //MoveWindow(pBtnMail->GetHandle(), 82,98,2+30     ,2+30     , TRUE);
   pImgProj = new CImage;
 //pImgMail = new CImage;
   pImgProj->LoadResource(ghInstance, TEXT("iconPROJECT"), imageIcon);
 //pImgMail->LoadResource(ghInstance, TEXT("Mail"       ), imageIcon);
   SendMessage(pBtnProj->GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)pImgProj);
 //SendMessage(pBtnMail->GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)pImgMail);

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_VERSION    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_COPYRIGHT  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_COMMENTS   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_RESPONSE   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICQ        );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICQADDRESS );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_FMSITE     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_UKRPOST    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_SITE       );
   SETNEWWNDPROC(hwnd, ID_DIALOG_ABOUT_ICON_PROJ  );
   SETNEWWNDPROC(hwnd, IDOK);
   pBtnProj->SetBkColor(gpFM2Proj->GetSkin().m_colorBk);
 //pBtnMail->SetBkColor(gpFM2Proj->GetSkin().m_colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   return TRUE;
}

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_ABOUT_ICON_MAIL:
   case ID_DIALOG_ABOUT_UKRPOST:
      if (codeNotify == STN_CLICKED)
         ShellExecute(hwnd, TEXT("open"), TEXT("mailto:Serg_Krivulja@UkrPost.net?subject=FastMines"), NULL, NULL, SW_RESTORE);
      return;
   case ID_DIALOG_ABOUT_SITE:
      if (codeNotify == STN_CLICKED)
         ShellExecute(hwnd, TEXT("open"), TEXT("http://kserg77.chat.ru"), NULL, NULL, SW_RESTORE);
      return;
   case ID_DIALOG_ABOUT_ICON_PROJ:
      if (codeNotify == BN_CLICKED) {
         if (pBtnProj->IsChecked()) {
            SendMessage(pBtnProj->GetHandle(), BM_SETCHECK, (WPARAM)BST_UNCHECKED, 0L);
            SendMessage(pBtnProj->GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)pImgProj);
         } else {
            SendMessage(pBtnProj->GetHandle(), BM_SETCHECK, (WPARAM)BST_CHECKED  , 0L);
            SendMessage(pBtnProj->GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)gpFM2Proj->GetImageBtnNew(3));
         }
      }
      return;
   case IDCANCEL:
   case IDOK:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_CLOSE
void OnClose(HWND hwnd){
   delete pImgProj;
 //delete pImgMail;
   delete pBtnProj;
 //delete pBtnMail;
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

} // namespace nsAbout
