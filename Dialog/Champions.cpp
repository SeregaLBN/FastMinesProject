////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Champions.cpp"
// обработка диалогового окна "Champions"
////////////////////////////////////////////////////////////////////////////////

#include ".\Champions.h"
#include <windowsx.h>
#include <commctrl.h>
#include <tchar.h>
#include "..\ID_resource.h"
#include "..\Lib.h"
#include "..\EraseBk.h"
#include ".\PlayerName.h"
#include ".\SelectFigure.h"
#include "..\Control\TcTable.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;
extern HWND ghWnd;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsChampions {
////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
#define MAX_INTEGER 0x7FFFFFFF

struct TsChmpnRecord {
   TCHAR name[nsPlayerName::maxPlayerNameLength];
   int  time;
   TsChmpnRecord() {
      name[0] = TEXT('\0');
      time = MAX_INTEGER;
   };
};

struct TsFileChmpn {
   TCHAR szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   TsChmpnRecord data[figureNil][skillLevelCustom][10];
};

struct TsChmpnRecord_ver200 {
   char name[20];
   int  time;
};

enum TeFigure_v200 {
   figure_v200_Square1,
   figure_v200_Square2,
   figure_v200_Triangle1,
   figure_v200_Triangle2,
   figure_v200_Hexagon,
   figure_v200_Pentagon,
   figure_v200_PentagonT10,
   figure_v200_Parquet1,
   figure_v200_Parquet2,
   figure_v200_TrSq,
   figure_v200_SqTrHex,
   figure_v200_Trapezoid,
   figure_v200_Rhombus,
   figure_v200_Nil
};

struct TsFileChmpn_ver200 {
   TsChmpnRecord_ver200 data[figure_v200_Nil][skillLevelCustom][10];
};

const TC_ITEM TC_Item[figureNil] = {
   {TCIF_IMAGE, 0,0, NULL, 0, figureTriangle1  },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTriangle2  },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTriangle3  },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTriangle4  },
   {TCIF_IMAGE, 0,0, NULL, 0, figureSquare1    },
   {TCIF_IMAGE, 0,0, NULL, 0, figureSquare2    },
   {TCIF_IMAGE, 0,0, NULL, 0, figureParquet1   },
   {TCIF_IMAGE, 0,0, NULL, 0, figureParquet2   },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTrapezoid1 },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTrapezoid2 },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTrapezoid3 },
   {TCIF_IMAGE, 0,0, NULL, 0, figureRhombus    },
   {TCIF_IMAGE, 0,0, NULL, 0, figureQuadrangle1},
   {TCIF_IMAGE, 0,0, NULL, 0, figurePentagon   },
   {TCIF_IMAGE, 0,0, NULL, 0, figurePentagonT5 },
   {TCIF_IMAGE, 0,0, NULL, 0, figurePentagonT10},
   {TCIF_IMAGE, 0,0, NULL, 0, figureHexagon    },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTrSq       },
   {TCIF_IMAGE, 0,0, NULL, 0, figureTrSq2      },
   {TCIF_IMAGE, 0,0, NULL, 0, figureSqTrHex    }};

const TCHAR szCFileName[] = TEXT("Mines.bst");   

////////////////////////////////////////////////////////////////////////////////
//                       global variables this namespaces
////////////////////////////////////////////////////////////////////////////////
HWND hDlg = NULL;
HWND hTabCtrl;
HIMAGELIST hImageList;
HICON hIconField[figureNil];
TeSkillLevel localSkillLevel;
TeFigure     localFigure;
TsFileChmpn  file;
TcTable*     pTable;
int indexPlayer = -1;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    , hDlg    , gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     , hDlg    , gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL, hDlg    , gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       , hDlg    , gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL         ,           gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                             forward declaration
////////////////////////////////////////////////////////////////////////////////
void ShowPage();
void LoadFile();
void SaveFile();

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////

inline void SetCaption() {
   TCHAR caption[64+9+3];
   _tcscpy(caption, TEXT("Champions"));
   _tcscat(caption, TEXT(" - "));
   _tcscat(caption, nsSelectFigure::MosaicName[localFigure]);
   SetWindowText(hDlg, caption);
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   SetWindowLong(hDlg, GWL_STYLE, WS_SIZEBOX ^ GetWindowLong(hDlg, GWL_STYLE));
   DrawMenuBar(hwnd);

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL);
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       );
#endif // REPLACEBKCOLORFROMFILLWINDOW

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
   hIconField[figurePentagon   ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagon"   ), IMAGE_ICON, 0,0,0);
   hIconField[figurePentagonT5 ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagonT5" ), IMAGE_ICON, 0,0,0);
   hIconField[figurePentagonT10] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldPentagonT10"), IMAGE_ICON, 0,0,0);
   hIconField[figureHexagon    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldHexagon"    ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrSq       ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrSq"       ), IMAGE_ICON, 0,0,0);
   hIconField[figureTrSq2      ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldTrSq2"      ), IMAGE_ICON, 0,0,0);
   hIconField[figureSqTrHex    ] = (HICON)LoadImage(ghInstance, TEXT("icon_FieldSqTrHex"    ), IMAGE_ICON, 0,0,0);

   hImageList = ImageList_Create(48,32, ILC_MASK, figureNil, 1);
   for (int i=0; i<figureNil; i++)
      ImageList_AddIcon(hImageList, hIconField[i]);

   hTabCtrl = GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL);
   TabCtrl_SetImageList(hTabCtrl, hImageList);
   for (i=0; i<figureNil; i++)
      TabCtrl_InsertItem(hTabCtrl, i, &TC_Item[i]);

   localSkillLevel = gpMosaic->GetSkillLevel();
   if (localSkillLevel == skillLevelCustom) localSkillLevel = skillLevelBeginner;
   PostMessage(
      GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER + localSkillLevel),
      BM_SETCHECK,
      (WPARAM) BST_CHECKED,
      0);
   localFigure = gpMosaic->GetFigure();
   TabCtrl_SetCurSel(hTabCtrl, localFigure);

   pTable = new TcTable;
 //pTable->SetDefaultHeight(17);
   pTable->Create(hTabCtrl, ID_DIALOG_STATISTICSorCHAMPIONS_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetColor(gpMosaic->GetSkin().toAll, gpMosaic->GetSkin().colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetColNumber(2);
   pTable->SetRowNumber(11);
   pTable->SetText(0,0, TEXT("Player name"));
   pTable->SetText(1,0, TEXT("Game time"));
   pTable->SetColWidth(0, 200);
   pTable->SetColWidth(1, 100);
   ShowWindow(pTable->GetHandle(), SW_HIDE);
   ShowWindow(pTable->GetHandle(), SW_SHOW);
   MoveWindow(pTable->GetHandle(), 2, 39, 405, 403, TRUE);
   MoveWindow(pTable->GetHandle(), 2, 39, 305, 303, TRUE);
   for (i=1; i<11; i++)
      pTable->SetFormatText(0, i, DT_LEFT | DT_VCENTER | DT_SINGLELINE);

   LoadFile();
   SetCaption();
   ShowPage();

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER:
      if (localSkillLevel == skillLevelBeginner) break;
      localSkillLevel = skillLevelBeginner;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR:
      if (localSkillLevel == skillLevelAmateur) break;
      localSkillLevel = skillLevelAmateur;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL:
      if (localSkillLevel == skillLevelProfessional) break;
      localSkillLevel = skillLevelProfessional;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY:
      if (localSkillLevel == skillLevelCrazy) break;
      localSkillLevel = skillLevelCrazy;
      ShowPage();
      break;
   case IDOK:
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_NOTIFY
void Cls_OnNotify(HWND hwnd, int idCtrl, LPNMHDR pNMHdr) {
   switch (pNMHdr->code) {
   case TCN_SELCHANGE:
      localFigure = (TeFigure)TabCtrl_GetCurSel(hTabCtrl); 
      ShowPage();
      SetCaption();
      break;
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
   for (int i=0; i<figureNil; i++)
      DeleteObject(hIconField[i]);
   ImageList_Destroy(hImageList);
   EndDialog(hwnd, 0);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
   HANDLE_WM_CTLCOLOR(hDlg);
   case WM_NOTIFY:
      Cls_OnNotify(hDlg, (int)wParam, (LPNMHDR)lParam);
   }
   return FALSE;
}

void ShowPage() {
   if (indexPlayer < 0)
      pTable->UnselectCell();
   else {
      pTable->SetCurrentCell(0, 1+indexPlayer, true);
      indexPlayer = -1;
   }
   TCHAR buf[16];
   for (int i=0; i<10; i++) {
      pTable->SetText (0, i+1, TEXT(""));
      pTable->SetText (1, i+1, TEXT(""));
      pTable->SetImage(0, i+1, NULL);
   }
   for (i=0; i<10; i++) {
      if (file.data[localFigure][localSkillLevel][i].time == MAX_INTEGER) break;
      if (!lstrcmpi(file.data[localFigure][localSkillLevel][i].name, nsPlayerName::szRobotNameDefault))
         pTable->SetImage(0, i+1, gpMosaic->GetImageBtnPause(3));
      pTable->SetText(0, i+1, file.data[localFigure][localSkillLevel][i].name);
      _stprintf(buf, TEXT("%d sec.\0"), file.data[localFigure][localSkillLevel][i].time);
      pTable->SetText(1, i+1, buf);
   }
}

inline void CleanFileChampions(TsFileChmpn &file) {
   memset(&file, 0, sizeof(file));
   _tcscpy(file.szVersion, TEXT(ID_VERSIONINFO_VERSION3));
   for (int i=0; i<figureNil; i++)
      for (int j=0; j<skillLevelCustom; j++)
         for (int k=0; k<10; k++) {
            file.data[i][j][k].name[0] = TEXT('\0');
            file.data[i][j][k].time = MAX_INTEGER;
         }
}

BOOL ConvertFileVersion(const DWORD sizeFile, HANDLE hFile, TsFileChmpn &file) {
   //return FALSE;
/**/
   DWORD dwPointer = SetFilePointer(hFile, 0, NULL, FILE_BEGIN);
   if (dwPointer) return FALSE;

   CleanFileChampions(file);

   BOOL result = FALSE;
   DWORD dwNOBR;
   switch (sizeFile) {
   case sizeof(TsFileChmpn_ver200):
      {
         TsFileChmpn_ver200 file_v200;
         result = ReadFile(hFile, &file_v200, sizeFile, &dwNOBR, NULL);
         if (result) {
            if (sizeFile != dwNOBR) return FALSE;
            // all Ok - convert from v200 to v210
            int j, k;
            // figure_v200_Square1
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureSquare1][j][k].name,  file_v200.data[figure_v200_Square1][j][k].name);
                          file.data[figureSquare1][j][k].time = file_v200.data[figure_v200_Square1][j][k].time;
               }
            // figure_v200_Square2
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureSquare2][j][k].name,  file_v200.data[figure_v200_Square2][j][k].name);
                          file.data[figureSquare2][j][k].time = file_v200.data[figure_v200_Square2][j][k].time;
               }
            // figure_v200_Triangle1
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureTriangle1][j][k].name,  file_v200.data[figure_v200_Triangle1][j][k].name);
                          file.data[figureTriangle1][j][k].time = file_v200.data[figure_v200_Triangle1][j][k].time;
               }
            // figure_v200_Triangle2
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureTriangle2][j][k].name,  file_v200.data[figure_v200_Triangle2][j][k].name);
                          file.data[figureTriangle2][j][k].time = file_v200.data[figure_v200_Triangle2][j][k].time;
               }
            // figure_v200_Hexagon
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureHexagon][j][k].name,  file_v200.data[figure_v200_Hexagon][j][k].name);
                          file.data[figureHexagon][j][k].time = file_v200.data[figure_v200_Hexagon][j][k].time;
               }
            // figure_v200_Pentagon
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figurePentagon][j][k].name,  file_v200.data[figure_v200_Pentagon][j][k].name);
                          file.data[figurePentagon][j][k].time = file_v200.data[figure_v200_Pentagon][j][k].time;
               }
            // figure_v200_PentagonT10
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figurePentagonT10][j][k].name,  file_v200.data[figure_v200_PentagonT10][j][k].name);
                          file.data[figurePentagonT10][j][k].time = file_v200.data[figure_v200_PentagonT10][j][k].time;
               }
            // figure_v200_Parquet1
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureParquet1][j][k].name,  file_v200.data[figure_v200_Parquet1][j][k].name);
                          file.data[figureParquet1][j][k].time = file_v200.data[figure_v200_Parquet1][j][k].time;
               }
            // figure_v200_Parquet2
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureParquet2][j][k].name,  file_v200.data[figure_v200_Parquet2][j][k].name);
                          file.data[figureParquet2][j][k].time = file_v200.data[figure_v200_Parquet2][j][k].time;
               }
            // figure_v200_TrSq
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureTrSq][j][k].name,  file_v200.data[figure_v200_TrSq][j][k].name);
                          file.data[figureTrSq][j][k].time = file_v200.data[figure_v200_TrSq][j][k].time;
               }
            // figure_v200_SqTrHex
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureSqTrHex][j][k].name,  file_v200.data[figure_v200_SqTrHex][j][k].name);
                          file.data[figureSqTrHex][j][k].time = file_v200.data[figure_v200_SqTrHex][j][k].time;
               }
            // figure_v200_Trapezoid
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureTrapezoid1][j][k].name,  file_v200.data[figure_v200_Trapezoid][j][k].name);
                          file.data[figureTrapezoid1][j][k].time = file_v200.data[figure_v200_Trapezoid][j][k].time;
               }
            // figure_v200_Rhombus
            for (j=0; j<skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  _tcscpy(file.data[figureRhombus][j][k].name,  file_v200.data[figure_v200_Rhombus][j][k].name);
                          file.data[figureRhombus][j][k].time = file_v200.data[figure_v200_Rhombus][j][k].time;
               }
         }
      }
      break;
   } // end switch
   return result;
/**/
}

void LoadFile() {
   TCHAR szPath[MAX_PATH];
   GetModuleFileName(ghInstance, szPath, MAX_PATH);
   DelFileFromFullPath(szPath);
   _tcscat(szPath, szCFileName);
   HANDLE hFile = CreateFile(
      szPath,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   BOOL result = FALSE;
   if (hFile != INVALID_HANDLE_VALUE) {
      const DWORD sizeFile = GetFileSize(hFile, NULL);
      if (sizeFile != 0xFFFFFFFF) {
         switch (sizeFile) {
         case sizeof(TsFileChmpn):
            DWORD dwNOBR;
            result = ReadFile(hFile, &file, sizeof(file), &dwNOBR, NULL);
            if (result && (sizeof(file) == dwNOBR)) {
               if (_tcscmp(file.szVersion, TEXT(ID_VERSIONINFO_VERSION3)))
                  result = ConvertFileVersion(sizeFile, hFile, file); // old ver. -> new ver.
               if (!result)
                  MessageBox(ghWnd, TEXT("BST file Ц version error"), TEXT("Information"), MB_ICONINFORMATION | MB_OK);
            } else
               MessageBox(ghWnd, TEXT("Can't load Champions file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
            break;
         default:
            result = ConvertFileVersion(sizeFile, hFile, file); // old ver. -> new ver.
            if (!result)
               MessageBox(ghWnd, TEXT("BST file Ц version error"), TEXT("Information"), MB_ICONINFORMATION | MB_OK);
         }
      } else
         MessageBox(ghWnd, TEXT("Can't load Champions file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
      CloseHandle(hFile);
   }
   if (!result)
      CleanFileChampions(file);
}

void SaveFile() {
   TCHAR szPath[MAX_PATH];
   GetModuleFileName(ghInstance, szPath, MAX_PATH);
   DelFileFromFullPath(szPath);
   _tcscat(szPath, szCFileName);
   HANDLE hFile = CreateFile(
      szPath,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile != INVALID_HANDLE_VALUE) {
      DWORD dwNOBW;
      BOOL result = WriteFile(hFile, &file,
          sizeof(file), &dwNOBW, NULL);
      if (result && (sizeof(file) != dwNOBW)) result = FALSE;
      if (!result) MessageBox(ghWnd, TEXT("Can't write Champions file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
      CloseHandle(hFile);
   } else
      MessageBox(ghWnd, TEXT("Can't create Champions file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
}

int InsertRecord(const TeFigure& figure, const TeSkillLevel& skill, const TsChmpnRecord& newRecord) {
   int j = 9;
   do {
      if(file.data[figure][skill][j-1].time > newRecord.time) {
         file.data[figure][skill][j] = file.data[figure][skill][j-1];
      } else {
         file.data[figure][skill][j] = newRecord;
         break;
      }
      j--;
   } while  (j != 0);
   if (j == 0) file.data[figure][skill][j] = newRecord;
   return j;
}

void SaveResult(const TeFigure figure, const TeSkillLevel skill, const int time, LPCTSTR szName) {
   if (skill == skillLevelCustom) return;
   LoadFile();
   if (file.data[figure][skill][9].time <= time) return;
   TsChmpnRecord newRecord;
   newRecord.time = time;
   _tcscpy(newRecord.name, szName);
   const int index = InsertRecord(figure, skill, newRecord);
   SaveFile();
   if (lstrcmpi(szName, nsPlayerName::szRobotNameDefault)) {
      indexPlayer = index;
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), ghWnd, (DLGPROC)nsChampions::DialogProc);
   }
}

} // namespace nsChampions
