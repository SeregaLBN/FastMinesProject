////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Champions.cpp"
// обработка диалогового окна "Champions"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Champions.h"
#include <WindowsX.h>
#include <CommCtrl.h>
#include "PlayerName.h"
#include "../ID_resource.h"
#include "../CommonLib.h"
#include "../EraseBk.h"
#include "../Control/Table.h"
#include "../Lang.h"
#include "../OldVersion.h"
#include "../FastMines2.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsChampions {
////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
struct CChmpnRecord {
   TCHAR m_szName[nsPlayerName::MAX_PLAYER_NAME_LENGTH];
   int  m_iTime;
   CChmpnRecord() {
      m_szName[0] = TEXT('\0');
      m_iTime = MAX_INTEGER;
   };
};

struct CFileChmpn {
   TCHAR m_szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   CChmpnRecord m_Data[nsMosaic::mosaicNil][nsMosaic::skillLevelCustom][10];
   CFileChmpn() {
      lstrcpy(m_szVersion, TEXT(ID_VERSIONINFO_VERSION3));
   }
};

const TC_ITEM TC_Item[nsMosaic::mosaicNil] = {
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTriangle1  },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTriangle2  },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTriangle3  },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTriangle4  },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicSquare1    },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicSquare2    },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicParquet1   },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicParquet2   },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTrapezoid1 },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTrapezoid2 },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTrapezoid3 },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicRhombus1   },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicQuadrangle1},
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicPentagonT24},
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicPentagonT5 },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicPentagonT10},
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicHexagon1   },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTrSq1      },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicTrSq2      },
   {TCIF_IMAGE, 0,0, NULL, 0, nsMosaic::mosaicSqTrHex    }};

const TCHAR SZ_FILE_NAME_CHAMPIONS[] = TEXT("Mines.bst");

////////////////////////////////////////////////////////////////////////////////
//                       global variables this namespaces
////////////////////////////////////////////////////////////////////////////////
HWND hDlg = NULL;
HWND hTabCtrl;
HIMAGELIST hImageList;
nsMosaic::ESkillLevel localSkillLevel;
nsMosaic::EMosaic     localMosaic;
CFileChmpn  FileChmpn;
CTable*     pTable;
int indexPlayer = -1;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     , hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL, hDlg, gpFM2Proj->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       , hDlg, gpFM2Proj->GetSkin())
WNDPROC_STATIC(ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL         ,       gpFM2Proj->GetSkin())
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
   SetWindowText(hDlg, CLang::m_StrArr[IDS__CHAMPIONS] + TEXT(" - ") + CLang::m_StrArr[IDS__MOSAIC_NAME_00+localMosaic]);
}

// WM_INITDIALOG
BOOL OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   {
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    ), CLang::m_StrArr[IDS__MENU_GAME__BEGINNER    ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     ), CLang::m_StrArr[IDS__MENU_GAME__AMATEUR     ]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL), CLang::m_StrArr[IDS__MENU_GAME__PROFESSIONAL]);
      SetWindowText(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       ), CLang::m_StrArr[IDS__MENU_GAME__CRAZY       ]);
   }

   SetWindowLong(hDlg, GWL_STYLE, WS_SIZEBOX ^ GetWindowLong(hDlg, GWL_STYLE));
   DrawMenuBar(hwnd);

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL         );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     );
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL);
   SETNEWWNDPROC(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   hImageList = ImageList_Create(32,32, ILC_MASK, nsMosaic::mosaicNil, 1);
   for (int i=0; i<nsMosaic::mosaicNil; i++) {
      ImageList_AddIcon(hImageList, CFastMines2Project::GetIconMosaic((nsMosaic::EMosaic)i));
   }

   hTabCtrl = GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL);
   TabCtrl_SetImageList(hTabCtrl, hImageList);
   for (i=0; i<nsMosaic::mosaicNil; i++)
      TabCtrl_InsertItem(hTabCtrl, i, &TC_Item[i]);

   localSkillLevel = gpFM2Proj->GetSkillLevel();
   if (localSkillLevel == nsMosaic::skillLevelCustom) localSkillLevel = nsMosaic::skillLevelBeginner;
   PostMessage(
      GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER + localSkillLevel),
      BM_SETCHECK,
      (WPARAM) BST_CHECKED,
      0);
   localMosaic = gpFM2Proj->GetMosaic();
   TabCtrl_SetCurSel(hTabCtrl, localMosaic);

   pTable = new CTable;
 //pTable->SetDefaultHeight(17);
   pTable->Create(hTabCtrl, ID_DIALOG_STATISTICSorCHAMPIONS_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetBkColor(gpFM2Proj->GetSkin().m_colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetColNumber(2);
   pTable->SetRowNumber(11);
   pTable->SetText(0,0, CLang::m_StrArr[IDS__CHAMPIONS__PLAYER_NAME]);
   pTable->SetText(1,0, CLang::m_StrArr[IDS__CHAMPIONS__GAME_TIME  ]);
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
void OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER:
      if (localSkillLevel == nsMosaic::skillLevelBeginner) break;
      localSkillLevel = nsMosaic::skillLevelBeginner;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR:
      if (localSkillLevel == nsMosaic::skillLevelAmateur) break;
      localSkillLevel = nsMosaic::skillLevelAmateur;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL:
      if (localSkillLevel == nsMosaic::skillLevelProfessional) break;
      localSkillLevel = nsMosaic::skillLevelProfessional;
      ShowPage();
      break;
   case ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY:
      if (localSkillLevel == nsMosaic::skillLevelCrazy) break;
      localSkillLevel = nsMosaic::skillLevelCrazy;
      ShowPage();
      break;
   case IDOK:
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      return;
   }
}

// WM_NOTIFY
void OnNotify(HWND hwnd, int idCtrl, LPNMHDR pNMHdr) {
   switch (pNMHdr->code) {
   case TCN_SELCHANGE:
      localMosaic = (nsMosaic::EMosaic)TabCtrl_GetCurSel(hTabCtrl);
      ShowPage();
      SetCaption();
      break;
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
   ImageList_Destroy(hImageList);
   EndDialog(hwnd, 0);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
   HANDLE_WM_CTLCOLOR(hDlg);
   case WM_NOTIFY:
      OnNotify(hDlg, (int)wParam, (LPNMHDR)lParam);
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
   for (int i=0; i<10; i++) {
      pTable->SetText (0, i+1, TEXT(""));
      pTable->SetText (1, i+1, TEXT(""));
      pTable->SetImage(0, i+1, NULL);
   }
   for (i=0; i<10; i++) {
      if (FileChmpn.m_Data[localMosaic][localSkillLevel][i].m_iTime == MAX_INTEGER) break;
      if (!lstrcmpi(FileChmpn.m_Data[localMosaic][localSkillLevel][i].m_szName, nsPlayerName::SZ_ASSISTANT_NAME_DEFAULT))
         pTable->SetImage(0, i+1, gpFM2Proj->GetImageBtnPause(3));
      pTable->SetText(0, i+1, FileChmpn.m_Data[localMosaic][localSkillLevel][i].m_szName);
      CString strTimeGame;
      strTimeGame.Format(TEXT("%d %s"), FileChmpn.m_Data[localMosaic][localSkillLevel][i].m_iTime, (LPCTSTR)CLang::m_StrArr[IDS__SEC]);
      pTable->SetText(1, i+1, strTimeGame);
   }
}

inline void CleanFileChampions(CFileChmpn &localFile) {
   memset(&localFile, 0, sizeof(CFileChmpn));
   lstrcpy(localFile.m_szVersion, TEXT(ID_VERSIONINFO_VERSION3));
   for (int i=0; i<nsMosaic::mosaicNil; i++)
      for (int j=0; j<nsMosaic::skillLevelCustom; j++)
         for (int k=0; k<10; k++) {
            localFile.m_Data[i][j][k].m_szName[0] = TEXT('\0');
            localFile.m_Data[i][j][k].m_iTime = MAX_INTEGER;
         }
}

#ifndef UNICODE
BOOL Convert(HANDLE hFile, CFileChmpn &localFile) {
   //return FALSE;
   DWORD dwPointer = SetFilePointer(hFile, 0, NULL, FILE_BEGIN);
   if (dwPointer) return FALSE;

   CleanFileChampions(localFile);

   BOOL bResult = FALSE;
   DWORD dwNOBR = 0;
   const DWORD dwSizeFile = GetFileSize(hFile, NULL);
   switch (dwSizeFile) {
   case sizeof(nsVer210::CFileChmpn):
      {
         nsVer210::CFileChmpn FileChmpn_v210;
         bResult = (
            ReadFile(hFile, &FileChmpn_v210, dwSizeFile, &dwNOBR, NULL) &&
            (dwSizeFile == dwNOBR) &&
            (0 == strcmp(FileChmpn_v210.m_szVersion, ID_VERSIONINFO_VERSION3_v210))
         );
         if (bResult) {
            bResult &= (
               nsMosaic::skillLevelBeginner     == nsVer210::skillLevelBeginner     &&
               nsMosaic::skillLevelAmateur      == nsVer210::skillLevelAmateur      &&
               nsMosaic::skillLevelProfessional == nsVer210::skillLevelProfessional &&
               nsMosaic::skillLevelCrazy        == nsVer210::skillLevelCrazy        &&

               placeCenter  == nsVer210::placeCenter  &&
               placeStretch == nsVer210::placeStretch &&
               placeTile    == nsVer210::placeTile    &&

               nsCell::_Nil  == nsVer210::_Nil  &&
               nsCell::_1    == nsVer210::_1  &&
               nsCell::_2    == nsVer210::_2  &&
               nsCell::_3    == nsVer210::_3  &&
               nsCell::_4    == nsVer210::_4  &&
               nsCell::_5    == nsVer210::_5  &&
               nsCell::_6    == nsVer210::_6  &&
               nsCell::_7    == nsVer210::_7  &&
               nsCell::_8    == nsVer210::_8  &&
               nsCell::_9    == nsVer210::_9  &&
               nsCell::_10   == nsVer210::_10 &&
               nsCell::_11   == nsVer210::_11 &&
               nsCell::_12   == nsVer210::_12 &&
               nsCell::_13   == nsVer210::_13 &&
               nsCell::_14   == nsVer210::_14 &&
               nsCell::_15   == nsVer210::_15 &&
               nsCell::_16   == nsVer210::_16 &&
               nsCell::_17   == nsVer210::_17 &&
               nsCell::_18   == nsVer210::_18 &&
               nsCell::_19   == nsVer210::_19 &&
               nsCell::_20   == nsVer210::_20 &&
               nsCell::_21   == nsVer210::_21 &&
               nsCell::_Mine == nsVer210::_Mine &&

               nsCell::_Unknown == nsVer210::_Unknown &&
               nsCell::_Clear   == nsVer210::_Clear &&
               nsCell::_Flag    == nsVer210::_Flag
            );
         #ifdef _DEBUG
            if (!bResult) {
               ::MessageBox_AbortProcess(TEXT("Ќадо переделать ф-цию"));
            }
         #endif // _DEBUG
         }
         if (bResult) {
            // all Ok - convert from v210
            int j, k;
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle2][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle2][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle3][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle3][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle3][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle3][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle4][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle4][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle4][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Triangle4][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSquare1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Square1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSquare1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Square1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSquare2][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Square2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSquare2][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Square2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicParquet1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Parquet1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicParquet1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Parquet1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicParquet2][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Parquet2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicParquet2][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Parquet2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrapezoid1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrapezoid1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrapezoid2][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrapezoid2][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrapezoid3][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid3][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrapezoid3][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Trapezoid3][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicRhombus1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Rhombus][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicRhombus1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Rhombus][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicQuadrangle1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Quadrangle1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicQuadrangle1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Quadrangle1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicPentagonT24][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Pentagon][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicPentagonT24][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Pentagon][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicPentagonT5][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_PentagonT5][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicPentagonT5][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_PentagonT5][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicPentagonT10][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_PentagonT10][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicPentagonT10][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_PentagonT10][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicHexagon1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_Hexagon][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicHexagon1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_Hexagon][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrSq1][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_TrSq][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrSq1][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_TrSq][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrSq2][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_TrSq2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrSq2][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_TrSq2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSqTrHex][j][k].m_szName, FileChmpn_v210.m_Data[nsVer210::mosaic_SqTrHex][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSqTrHex][j][k].m_iTime = FileChmpn_v210.m_Data[nsVer210::mosaic_SqTrHex][j][k].m_iTime;
               }
         }
      }
      break;
   case sizeof(nsVer200::CFileChmpn):
      {
         nsVer200::CFileChmpn FileChmpn_v200;
         bResult = ReadFile(hFile, &FileChmpn_v200, dwSizeFile, &dwNOBR, NULL) && (dwSizeFile == dwNOBR);
         if (bResult) {
            // all Ok - convert from v200
            int j, k;
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSquare1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Square1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSquare1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Square1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSquare2][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Square2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSquare2][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Square2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Triangle1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Triangle1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTriangle2][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Triangle2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTriangle2][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Triangle2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicHexagon1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Hexagon1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicHexagon1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Hexagon1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicPentagonT24][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_PentagonT24][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicPentagonT24][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_PentagonT24][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicPentagonT10][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_PentagonT10][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicPentagonT10][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_PentagonT10][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicParquet1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Parquet1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicParquet1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Parquet1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicParquet2][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Parquet2][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicParquet2][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Parquet2][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrSq1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_TrSq1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrSq1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_TrSq1][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicSqTrHex][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_SqTrHex][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicSqTrHex][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_SqTrHex][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicTrapezoid1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Trapezoid][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicTrapezoid1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Trapezoid][j][k].m_iTime;
               }
            for (j=0; j<nsMosaic::skillLevelCustom; j++)
               for (k=0; k<10; k++) {
                  lstrcpy(localFile.m_Data[nsMosaic::mosaicRhombus1][j][k].m_szName, FileChmpn_v200.m_Data[nsVer200::mosaic_Rhombus1][j][k].m_szName);
                          localFile.m_Data[nsMosaic::mosaicRhombus1][j][k].m_iTime = FileChmpn_v200.m_Data[nsVer200::mosaic_Rhombus1][j][k].m_iTime;
               }
         }
      }
      break;
   } // end switch
   return bResult;
}
#endif // UNICODE

void LoadFile() {
   HANDLE hFile = CreateFile(
      GetModuleDir(ghInstance) + SZ_FILE_NAME_CHAMPIONS,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile != INVALID_HANDLE_VALUE) {
      const DWORD dwSizeFile = GetFileSize(hFile, NULL);
      DWORD dwNOBR = 0;
      if (dwSizeFile == 0xFFFFFFFF) {
         MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__CHAMPIONS__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         switch (dwSizeFile) {
         case sizeof(CFileChmpn):
            if (!ReadFile(hFile, &FileChmpn, sizeof(CFileChmpn), &dwNOBR, NULL) ||
                (sizeof(CFileChmpn) != dwNOBR)
               )
            {
               CleanFileChampions(FileChmpn);
               MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__CHAMPIONS__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
            } else {
               if (lstrcmp(FileChmpn.m_szVersion, TEXT(ID_VERSIONINFO_VERSION3))) {
            #ifndef UNICODE
                  goto switch_default;
            #else
                  NULL;
            #endif // UNICODE
               }
            }
            break;
         default:
      #ifndef UNICODE
  switch_default:
            if (!Convert(hFile, FileChmpn)) { // old ver. -> new ver.
               CleanFileChampions(FileChmpn);
               MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__CHAMPIONS__ERROR_VERSION], CLang::m_StrArr[IDS__INFORMATION], MB_ICONINFORMATION | MB_OK);
            }
      #endif // UNICODE
            break;
         }
      }
      CloseHandle(hFile);
   }
}

void SaveFile() {
   HANDLE hFile = CreateFile(
      GetModuleDir(ghInstance) + SZ_FILE_NAME_CHAMPIONS,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile == INVALID_HANDLE_VALUE) {
      MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__CHAMPIONS__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
   } else {
      DWORD dwNOBW = 0;
      if (!WriteFile(hFile, &FileChmpn, sizeof(CFileChmpn), &dwNOBW, NULL) ||
          (sizeof(CFileChmpn) != dwNOBW)
         )
      {
         MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__CHAMPIONS__ERROR_WRITE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      }
      CloseHandle(hFile);
   }
}

int InsertRecord(const nsMosaic::EMosaic& mosaic, const nsMosaic::ESkillLevel& skill, const CChmpnRecord& newRecord) {
   int j = 9;
   do {
      if(FileChmpn.m_Data[mosaic][skill][j-1].m_iTime > newRecord.m_iTime) {
         FileChmpn.m_Data[mosaic][skill][j] = FileChmpn.m_Data[mosaic][skill][j-1];
      } else {
         FileChmpn.m_Data[mosaic][skill][j] = newRecord;
         break;
      }
      j--;
   } while(j != 0);
   if (j == 0) FileChmpn.m_Data[mosaic][skill][j] = newRecord;
   return j;
}

void SaveResult(const nsMosaic::EMosaic mosaic, const nsMosaic::ESkillLevel skill, const int time, LPCTSTR szName) {
   if (skill == nsMosaic::skillLevelCustom) return;
   LoadFile();
   if (FileChmpn.m_Data[mosaic][skill][9].m_iTime <= time) return;
   CChmpnRecord newRecord;
   newRecord.m_iTime = time;
   lstrcpy(newRecord.m_szName, szName);
   const int index = InsertRecord(mosaic, skill, newRecord);
   SaveFile();
   if (lstrcmpi(szName, nsPlayerName::SZ_ASSISTANT_NAME_DEFAULT)) {
      indexPlayer = index;
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), gpFM2Proj->GetHandle(), (DLGPROC)nsChampions::DialogProc);
   }
}

} // namespace nsChampions
