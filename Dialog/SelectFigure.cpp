////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SelectFigure.cpp"
// обработка диалогового окна "Select Figure"
////////////////////////////////////////////////////////////////////////////////
#include ".\SelectFigure.h"
#include <windowsx.h>
#include <commctrl.h>
#include "..\ID_resource.h"
#include "..\EraseBk.h"
#include "..\FigureName.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsSelectFigure {

const TCHAR MosaicName[][64] = {
   MosaicName00,
   MosaicName01,
   MosaicName02,
   MosaicName03,
   MosaicName04,
   MosaicName05,
   MosaicName06,
   MosaicName07,
   MosaicName08,
   MosaicName09,
   MosaicName10,
   MosaicName11,
   MosaicName12,
   MosaicName13,
   MosaicName14,
   MosaicName15,
   MosaicName16,
   MosaicName17,
   MosaicName18,
   MosaicName19,
   TEXT("!!! Invalid name !!!")
};

int first_key;
int resultDialog = figureNil;
int numberFigure;
TeFigure nameFigure;
HICON hIconField[figureNil+1];

HWND hDlg;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_SELECTFIGURE_STATIC_NUMBER, gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTFIGURE_EDIT         , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTFIGURE_SPIN         , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTFIGURE_STATIC_NAME  , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_SELECTFIGURE_COMBOBOX     , gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK, hDlg                          , gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

TeFigure NumberFigureToNameFigure(int);
int NameFigureToNumberFigure(TeFigure);
 
// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTFIGURE_STATIC_NUMBER);
 //SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTFIGURE_EDIT         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTFIGURE_SPIN         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTFIGURE_STATIC_NAME  );
   SETNEWWNDPROC(hwnd, ID_DIALOG_SELECTFIGURE_COMBOBOX     );
   SETNEWWNDPROC(hwnd, IDOK                                );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   numberFigure = first_key * 10;
   nameFigure = NumberFigureToNameFigure(numberFigure);

   hIconField[figureTriangle1  ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTriangle1"  ), IMAGE_ICON, 0,0,0);
   hIconField[figureTriangle2  ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTriangle2"  ), IMAGE_ICON, 0,0,0);
   hIconField[figureTriangle3  ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTriangle3"  ), IMAGE_ICON, 0,0,0);
   hIconField[figureTriangle4  ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTriangle4"  ), IMAGE_ICON, 0,0,0);
   hIconField[figureSquare1    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldSquare1"    ), IMAGE_ICON, 0,0,0);
   hIconField[figureSquare2    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldSquare2"    ), IMAGE_ICON, 0,0,0);
   hIconField[figureParquet1   ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldParquet1"   ), IMAGE_ICON, 0,0,0);
   hIconField[figureParquet2   ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldParquet2"   ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrapezoid1 ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrapezoid1" ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrapezoid2 ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrapezoid2" ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrapezoid3 ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrapezoid3" ), IMAGE_ICON, 0,0,0);
   hIconField[figureRhombus    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldRhombus"    ), IMAGE_ICON, 0,0,0);
   hIconField[figureQuadrangle1] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldQuadrangle1"), IMAGE_ICON, 0,0,0);
   hIconField[figureNil        ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldNull"       ), IMAGE_ICON, 0,0,0);
   hIconField[figurePentagon   ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagon"   ), IMAGE_ICON, 0,0,0);
   hIconField[figurePentagonT5 ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagonT5" ), IMAGE_ICON, 0,0,0);
   hIconField[figurePentagonT10] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagonT10"), IMAGE_ICON, 0,0,0);
   hIconField[figureHexagon    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldHexagon"    ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrSq       ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrSq"       ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrSq2      ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrSq2"      ), IMAGE_ICON, 0,0,0);
   hIconField[figureSqTrHex    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldSqTrHex"    ), IMAGE_ICON, 0,0,0);
   //SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_BUTTON_OK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)hIconField[nameFigure]);

   for (int i=0; i<=figureNil; i++)
      SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_COMBOBOX, CB_ADDSTRING, 0L, (LPARAM)MosaicName[i]);
   //SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_COMBOBOX, CB_SETCURSEL, (WPARAM)nameFigure, 0L);

   SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_SPIN, UDM_SETRANGE, 0L, MAKELPARAM(72, 30));
   SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_SPIN, UDM_SETPOS  , 0L, numberFigure);
   PostMessage(GetDlgItem(hwnd, ID_DIALOG_SELECTFIGURE_EDIT), EM_SETSEL, 1L, 2L);

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_SELECTFIGURE_COMBOBOX:
      if (codeNotify == CBN_SELCHANGE) {
         nameFigure = (TeFigure)SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_COMBOBOX, CB_GETCURSEL, 0L, 0L);
         numberFigure = NameFigureToNumberFigure(nameFigure);
         SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_SPIN, UDM_SETPOS, 0L, numberFigure);
         SendDlgItemMessage(hwnd, IDOK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)hIconField[nameFigure]);
      }
      return;
   case ID_DIALOG_SELECTFIGURE_EDIT:
      if (codeNotify == EN_CHANGE) {
         numberFigure = SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_SPIN, UDM_GETPOS, 0L, 0L);
         nameFigure = NumberFigureToNameFigure(numberFigure);
         SendDlgItemMessage(hwnd, ID_DIALOG_SELECTFIGURE_COMBOBOX, CB_SETCURSEL, (WPARAM)nameFigure, 0L);
         SendDlgItemMessage(hwnd, IDOK, BM_SETIMAGE, IMAGE_ICON, (LPARAM)hIconField[nameFigure]);
      }
      return;
   case IDCANCEL:
      resultDialog = figureNil;
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   case IDOK:
      resultDialog = nameFigure;
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
   for (int i=0; i<=figureNil; i++)
      DeleteObject(hIconField[i]);
   EndDialog(hwnd, resultDialog);
}

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

int GetGroup(TeFigure figure) {
   switch (figure) {
   case figureTriangle1:
   case figureTriangle2:
   case figureTriangle3:
   case figureTriangle4:
      return 0;
   case figureSquare1:
   case figureSquare2:
   case figureParquet1:
   case figureParquet2:
   case figureTrapezoid1:
   case figureTrapezoid2:
   case figureTrapezoid3:
   case figureRhombus:
   case figureQuadrangle1:
      return 1;
   case figurePentagon:
   case figurePentagonT5:
   case figurePentagonT10:
      return 2;
   case figureHexagon:
      return 3;
   case figureTrSq:
   case figureTrSq2:
   case figureSqTrHex:
      return 4;
   }
   return 5;
}

TeFigure NumberFigureToNameFigure(int numberFigure) {
   switch (numberFigure) {
   case 30: return figureTriangle1;
   case 31: return figureTriangle2;
   case 32: return figureTriangle3;
   case 33: return figureTriangle4;
   case 40: return figureSquare1;
   case 41: return figureSquare2;
   case 42: return figureParquet1;
   case 43: return figureParquet2;
   case 44: return figureTrapezoid1;
   case 45: return figureTrapezoid2;
   case 46: return figureTrapezoid3;
   case 47: return figureRhombus;
   case 48: return figureQuadrangle1;
   case 50: return figurePentagon;
   case 51: return figurePentagonT5;
   case 52: return figurePentagonT10;
   case 60: return figureHexagon;
   case 70: return figureTrSq;
   case 71: return figureTrSq2;
   case 72: return figureSqTrHex;
   }
   return figureNil;
}

int NameFigureToNumberFigure(TeFigure nameFigure) {
   switch (nameFigure) {
   case figureTriangle1  : return 30;
   case figureTriangle2  : return 31;
   case figureTriangle3  : return 32;
   case figureTriangle4  : return 33;
   case figureSquare1    : return 40;
   case figureSquare2    : return 41;
   case figureParquet1   : return 42;
   case figureParquet2   : return 43;
   case figureTrapezoid1 : return 44;
   case figureTrapezoid2 : return 45;
   case figureTrapezoid3 : return 46;
   case figureRhombus    : return 47;
   case figureQuadrangle1: return 48;
   case figurePentagon   : return 50;
   case figurePentagonT5 : return 51;
   case figurePentagonT10: return 52;
   case figureHexagon    : return 60;
   case figureTrSq       : return 70;
   case figureTrSq2      : return 71;
   case figureSqTrHex    : return 72;
   }
   return 0;
}

void SetFirstKey(int key) {
   first_key = key;
}

} // namespace nsSelectFigure
