////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SelectMosaic.cpp"
// обработка диалогового окна "Select Mosaic"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "SelectMosaic.h"
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
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsSelectMosaic {

bool InitMosaicName();

int first_key;
int resultDialog = nsMosaic::mosaicNil;
int numberMosaic;
nsMosaic::EMosaic nameMosaic;

HWND hDlg;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_SELECTMOSAIC_STATIC_NUMBER, gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTMOSAIC_EDIT         , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTMOSAIC_SPIN         , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTMOSAIC_STATIC_NAME  , gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTMOSAIC_COMBOBOX     , gpFM2Proj->GetSkin())
WNDPROC_BUTTON(IDOK, hDlg                          , gpFM2Proj->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

nsMosaic::EMosaic NumberMosaicToNameMosaic(int);
int NameMosaicToNumberMosaic(nsMosaic::EMosaic);
 
// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   {
      SetWindowText(hwnd, CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC]);

      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTMOSAIC_STATIC_NUMBER), CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC__NUMBER]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_SELECTMOSAIC_STATIC_NAME  ), CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC__NAME  ]);
   }

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTMOSAIC_STATIC_NUMBER);
 //SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTMOSAIC_EDIT         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTMOSAIC_SPIN         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTMOSAIC_STATIC_NAME  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTMOSAIC_COMBOBOX     );
   SETNEWWNDPROC(hwnd, IDOK                                );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   numberMosaic = first_key * 10;
   nameMosaic = NumberMosaicToNameMosaic(numberMosaic);

   //SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_BUTTON_OK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)hIconMosaic[nameMosaic]);

   for (int i=0; i<=nsMosaic::mosaicNil; i++)
      SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_COMBOBOX, CB_ADDSTRING, 0L, (LPARAM)(LPCTSTR)CLang::m_StrArr[IDS__MOSAIC_NAME_00+i]);
   //SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_COMBOBOX, CB_SETCURSEL, (WPARAM)nameMosaic, 0L);

   SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_SPIN, UDM_SETRANGE, 0L, MAKELPARAM(72, 30));
   SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_SPIN, UDM_SETPOS  , 0L, numberMosaic);
   PostMessage(GetDlgItem(hwnd, ID_DIALOG_SELECTMOSAIC_EDIT), EM_SETSEL, 1L, 2L);

   return TRUE;
}

// WM_COMMAND
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_SELECTMOSAIC_COMBOBOX:
      if (codeNotify == CBN_SELCHANGE) {
         nameMosaic = (nsMosaic::EMosaic)SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_COMBOBOX, CB_GETCURSEL, 0L, 0L);
         numberMosaic = NameMosaicToNumberMosaic(nameMosaic);
         SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_SPIN, UDM_SETPOS, 0L, numberMosaic);
         SendDlgItemMessage(hwnd, IDOK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)CFastMines2Project::GetIconMosaic((nsMosaic::EMosaic)nameMosaic));
      }
      return;
   case ID_DIALOG_SELECTMOSAIC_EDIT:
      if (codeNotify == EN_CHANGE) {
         numberMosaic = SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_SPIN, UDM_GETPOS, 0L, 0L);
         nameMosaic = NumberMosaicToNameMosaic(numberMosaic);
         SendDlgItemMessage(hwnd, ID_DIALOG_SELECTMOSAIC_COMBOBOX, CB_SETCURSEL, (WPARAM)nameMosaic, 0L);
         SendDlgItemMessage(hwnd, IDOK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)CFastMines2Project::GetIconMosaic((nsMosaic::EMosaic)nameMosaic));
      }
      return;
   case IDCANCEL:
      resultDialog = nsMosaic::mosaicNil;
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      resultDialog = nameMosaic;
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
   EndDialog(hwnd, resultDialog);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

int GetGroup(nsMosaic::EMosaic mosaic) {
   switch (mosaic) {
   case nsMosaic::mosaicTriangle1:
   case nsMosaic::mosaicTriangle2:
   case nsMosaic::mosaicTriangle3:
   case nsMosaic::mosaicTriangle4:
      return 0;
   case nsMosaic::mosaicSquare1:
   case nsMosaic::mosaicSquare2:
   case nsMosaic::mosaicParquet1:
   case nsMosaic::mosaicParquet2:
   case nsMosaic::mosaicTrapezoid1:
   case nsMosaic::mosaicTrapezoid2:
   case nsMosaic::mosaicTrapezoid3:
   case nsMosaic::mosaicRhombus1:
   case nsMosaic::mosaicQuadrangle1:
      return 1;
   case nsMosaic::mosaicPentagonT24:
   case nsMosaic::mosaicPentagonT5:
   case nsMosaic::mosaicPentagonT10:
      return 2;
   case nsMosaic::mosaicHexagon1:
      return 3;
   case nsMosaic::mosaicTrSq1:
   case nsMosaic::mosaicTrSq2:
   case nsMosaic::mosaicSqTrHex:
      return 4;
   }
   return 5;
}

nsMosaic::EMosaic NumberMosaicToNameMosaic(int numberMosaic) {
   switch (numberMosaic) {
   case 30: return nsMosaic::mosaicTriangle1;
   case 31: return nsMosaic::mosaicTriangle2;
   case 32: return nsMosaic::mosaicTriangle3;
   case 33: return nsMosaic::mosaicTriangle4;
   case 40: return nsMosaic::mosaicSquare1;
   case 41: return nsMosaic::mosaicSquare2;
   case 42: return nsMosaic::mosaicParquet1;
   case 43: return nsMosaic::mosaicParquet2;
   case 44: return nsMosaic::mosaicTrapezoid1;
   case 45: return nsMosaic::mosaicTrapezoid2;
   case 46: return nsMosaic::mosaicTrapezoid3;
   case 47: return nsMosaic::mosaicRhombus1;
   case 48: return nsMosaic::mosaicQuadrangle1;
   case 50: return nsMosaic::mosaicPentagonT24;
   case 51: return nsMosaic::mosaicPentagonT5;
   case 52: return nsMosaic::mosaicPentagonT10;
   case 60: return nsMosaic::mosaicHexagon1;
   case 70: return nsMosaic::mosaicTrSq1;
   case 71: return nsMosaic::mosaicTrSq2;
   case 72: return nsMosaic::mosaicSqTrHex;
   }
   return nsMosaic::mosaicNil;
}

int NameMosaicToNumberMosaic(nsMosaic::EMosaic nameMosaic) {
   switch (nameMosaic) {
   case nsMosaic::mosaicTriangle1  : return 30;
   case nsMosaic::mosaicTriangle2  : return 31;
   case nsMosaic::mosaicTriangle3  : return 32;
   case nsMosaic::mosaicTriangle4  : return 33;
   case nsMosaic::mosaicSquare1    : return 40;
   case nsMosaic::mosaicSquare2    : return 41;
   case nsMosaic::mosaicParquet1   : return 42;
   case nsMosaic::mosaicParquet2   : return 43;
   case nsMosaic::mosaicTrapezoid1 : return 44;
   case nsMosaic::mosaicTrapezoid2 : return 45;
   case nsMosaic::mosaicTrapezoid3 : return 46;
   case nsMosaic::mosaicRhombus1   : return 47;
   case nsMosaic::mosaicQuadrangle1: return 48;
   case nsMosaic::mosaicPentagonT24: return 50;
   case nsMosaic::mosaicPentagonT5 : return 51;
   case nsMosaic::mosaicPentagonT10: return 52;
   case nsMosaic::mosaicHexagon1   : return 60;
   case nsMosaic::mosaicTrSq1      : return 70;
   case nsMosaic::mosaicTrSq2      : return 71;
   case nsMosaic::mosaicSqTrHex    : return 72;
   }
   return 0;
}

void SetFirstKey(int key) {
   first_key = key;
}

} // namespace nsSelectMosaic
