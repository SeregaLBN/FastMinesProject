////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Statistics.cpp"
// обработка диалогового окна "Statistics"
////////////////////////////////////////////////////////////////////////////////

#include ".\Statistics.h"
#include <windowsx.h>
#include <commctrl.h>
#include <tchar.h>
#include "..\ID_resource.h"
#include "..\Lib.h"
#include "..\EraseBk.h"
#include "..\TcMosaic.h"
#include ".\PlayerName.h"
#include ".\SelectFigure.h"
#include ".\Info.h"
#include "..\Control\TcTable.h"
#include ".\PlayerName.h"
#include <time.h>
#include <algorithm>

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;
extern HWND ghWnd;
extern const POINT CSizeField[];
////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsStatistics {
////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
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

const TCHAR szCFileName[] = TEXT("Mines.stc");   

class TcStatistics {
private:
   std::vector<TsSttstcRecord*> players;
private:
   int AddRecord(LPCTSTR);
public:
   TcStatistics();
  ~TcStatistics();
   int FindName (LPCTSTR) const;
   void Insert(const TsSttstcSubRecord&, const TeFigure&, const TeSkillLevel&, LPCTSTR playerName);
   int NumberPlayers() const; // количество игроков
   const TsSttstcRecord& GetData(const int index) const;
   bool SetPassword (const int index, LPCTSTR szNewPassword);
   bool RenamePlayer(const int index, LPCTSTR szNewName);
   bool RemovePlayer(const int index);
};

struct TsSttstcFile {
   TsSttstcRecord dataStc; // данные об отдельном игроке
   TsSttstcRecord dataBad; // мусор
   DWORD bCRC;
   TsSttstcFile(): bCRC(0) {
      srand((unsigned)time(NULL));
      for (DWORD* i=(DWORD*)&dataBad; i<&bCRC; i++)
         *i = (rand() << 16) | rand();
   }
};
////////////////////////////////////////////////////////////////////////////////
//                       global variables this namespaces
////////////////////////////////////////////////////////////////////////////////
HWND hDlg = NULL;
HWND hTabCtrl;
HIMAGELIST hImageList;
HICON hIconField[figureNil];
TeSkillLevel localSkillLevel;
TeFigure     localFigure;

TcTable* pTable;
TcStatistics Statistics;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL, hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       , hDlg, gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL         ,       gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                             forward declaration
////////////////////////////////////////////////////////////////////////////////
void ShowPage();

////////////////////////////////////////////////////////////////////////////////
//                         implementation - other
////////////////////////////////////////////////////////////////////////////////
TsSttstcSubRecord operator+=(TsSttstcSubRecord& sr1, const TsSttstcSubRecord& sr2) {
   sr1.GameNumber += sr2.GameNumber;
   sr1.GameWin    += sr2.GameWin   ;
   sr1.OpenField  += sr2.OpenField ;
   sr1.PlayTime   += sr2.PlayTime  ;
   sr1.ClickCount += sr2.ClickCount;
   return sr1;
};

////////////////////////////////////////////////////////////////////////////////
//                         implementation - TcStatistics
////////////////////////////////////////////////////////////////////////////////
inline int TcStatistics::FindName(LPCTSTR playerName) const {
   for (int i=0; i<players.size(); i++) {
    //if (!_tcscmp (players[i]->playerName, playerName))
      if (!lstrcmpi(players[i]->playerName, playerName))
         return i;
   }
   return -1;
};

inline int TcStatistics::AddRecord(LPCTSTR playerName) {
   TsSttstcRecord* newSttstcRecord = new TsSttstcRecord;
   _tcscpy(newSttstcRecord->playerName, playerName);
   players.push_back(newSttstcRecord);
   return players.size()-1; // индекс вставленного (нового) элемента
};

inline TcStatistics::TcStatistics() {
   // load data from file
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
   BOOL error = FALSE;
   if (hFile != INVALID_HANDLE_VALUE) {
      try {
         // считываю заголовок файла
         DWORD dwNOBR;
         TCHAR version[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
         if (error = !ReadFile(hFile, version, sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBR, NULL))
            throw TEXT("Can't load Statistics file");
         if (error = (sizeof(TEXT(ID_VERSIONINFO_VERSION3)) != dwNOBR))
            throw TEXT("STC file - data error"); // слишком мало данных в файле
         if (error = _tcscmp(TEXT(ID_VERSIONINFO_VERSION3), version))
            throw TEXT("STC file - version error");
         // считываю данные (по частям - по одному игроку)
         const DWORD sizeData = GetFileSize(hFile, NULL)-dwNOBR; // размер данных без заголовка
         TsSttstcFile StcFile;
         for (int i=0; i<sizeData/sizeof(TsSttstcFile); i++) {
            if (error = !ReadFile(hFile, &StcFile, sizeof(TsSttstcFile), &dwNOBR, NULL))
               throw TEXT("Can't load Statistics file"); // ошибка чтения
            if (error = (sizeof(TsSttstcFile) != dwNOBR))
               throw TEXT("STC file - read error"); // считан неверный обьём данных
            DWORD verifyByteCRC = 0;
            for (int j=0; j<sizeof(TsSttstcRecord)/sizeof(DWORD); j++) {
               *((DWORD*)(&StcFile.dataStc)+j) ^= *((DWORD*)(&StcFile.dataBad)+j); // размаскирую данные
               verifyByteCRC                   ^= *((DWORD*)(&StcFile.dataStc)+j); // пересчёт CRC
               verifyByteCRC                   ^= *((DWORD*)(&StcFile.dataBad)+j); // пересчёт CRC
            }
            if (error = (verifyByteCRC != StcFile.bCRC)) // cравнениe CRC
               throw TEXT("STC file - data error");      // CRC несовпало - ошибка
            players.push_back(new TsSttstcRecord);                        // создаю новую запись
            memcpy(players[i], &StcFile.dataStc, sizeof(TsSttstcRecord)); // копирую в новую запись данные игрока из файла
         }
         if (error = (sizeData%sizeof(TsSttstcFile)))
            throw TEXT("STC file - unknown error"); // лишние данные ???
      } catch (TCHAR* szErrorMsg) {
         MessageBox(ghWnd, szErrorMsg, TEXT("Error"), MB_ICONSTOP | MB_OK);
      }
      CloseHandle(hFile);
   }
};

inline TcStatistics::~TcStatistics() {
   // save data in file
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
   BOOL error = FALSE;
   if (hFile != INVALID_HANDLE_VALUE) {
      try {
         // записываю заголовок файла
         const TCHAR version[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))] = {TEXT(ID_VERSIONINFO_VERSION3)};
         DWORD dwNOBW;
         if (error = !WriteFile(hFile, version, sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBW, NULL))
            throw TEXT("Can't write Statistics file");
         if (error = (sizeof(TEXT(ID_VERSIONINFO_VERSION3)) != dwNOBW))
            throw TEXT("STC file - write error");
         // записываю данные
         for (int i=0; i<players.size(); i++) {
            TsSttstcFile StcFile;
            memcpy(&StcFile.dataStc, players[i], sizeof(TsSttstcRecord));
            for (int j=0; j<sizeof(TsSttstcRecord)/sizeof(DWORD); j++) {
               StcFile.bCRC                    ^= *((DWORD*)(&StcFile.dataStc)+j); // подсчёт CRC
               StcFile.bCRC                    ^= *((DWORD*)(&StcFile.dataBad)+j); // подсчёт CRC
               *((DWORD*)(&StcFile.dataStc)+j) ^= *((DWORD*)(&StcFile.dataBad)+j); // маскирую данные
            }
            if (error = !WriteFile(hFile, &StcFile, sizeof(TsSttstcFile), &dwNOBW, NULL))
               throw TEXT("Can't write Statistics file"); // ошибка записи
            if (error = (sizeof(TsSttstcFile) != dwNOBW))
               throw TEXT("STC file - write error"); // записан неверный обьём данных
         }
      } catch (TCHAR* szErrorMsg) {
         MessageBox(ghWnd, szErrorMsg, TEXT("Error"), MB_ICONSTOP | MB_OK);
      }
      CloseHandle(hFile);
   } else
      MessageBox(ghWnd, TEXT("Can't create Statistics file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
   // destroy others
   for (int i=0; i<players.size(); i++)
      delete players[i];
   players.clear();
};

inline void TcStatistics::Insert(const TsSttstcSubRecord& insertRecord, const TeFigure& figure, const TeSkillLevel& skill, LPCTSTR playerName) {
   int index = FindName(playerName);
   if (index < 0) index = AddRecord(playerName);
   players[index]->record[figure][skill] += insertRecord;
};

inline int TcStatistics::NumberPlayers() const {
   return players.size();
};

inline const TsSttstcRecord& TcStatistics::GetData(const int index) const {
   return *players[index];
};

bool TcStatistics::SetPassword(const int index, LPCTSTR szNewPassword) {
   if ((index<0) || (index >= players.size())) return false;
   _tcscpy(players[index]->password, szNewPassword);
   return true;
}

bool TcStatistics::RenamePlayer(const int index, LPCTSTR szNewName) {
   if ((index<0) || (index >= players.size())) return false;
   _tcscpy(players[index]->playerName, szNewName);
   return true;
}

bool TcStatistics::RemovePlayer(const int index) {
   if ((index<0) || (index >= players.size())) return false;
   delete players[index];
   players.erase(std::find(players.begin(), players.end(), players[index]));
   return true;
}

////////////////////////////////////////////////////////////////////////////////
//                        implementation - dialog function
////////////////////////////////////////////////////////////////////////////////
inline void SetCaption() {
   TCHAR caption[64+9+3];
   _tcscpy(caption, TEXT("Statistics"));
   _tcscat(caption, TEXT(" - "));
   _tcscat(caption, nsSelectFigure::MosaicName[localFigure]);
   SetWindowText(hDlg, caption);
}

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

   SetWindowLong(hDlg, GWL_STYLE, WS_MAXIMIZEBOX ^ GetWindowLong(hDlg, GWL_STYLE));

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
   pTable->Create(hTabCtrl, ID_DIALOG_STATISTICSorCHAMPIONS_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetColor(gpMosaic->GetSkin().toAll, gpMosaic->GetSkin().colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetText(0,0, TEXT("Player name"));
   pTable->SetText(1,0, TEXT("Number of games"));
   pTable->SetText(2,0, TEXT("Games won"));
   pTable->SetText(3,0, TEXT("Open (max ???%)"));
   pTable->SetText(4,0, TEXT("Averaged game time"));
   pTable->SetText(5,0, TEXT("Averaged number of clicks"));
   pTable->SetColWidth(0, 150);
   pTable->SetColWidth(3, 120);
   pTable->SetColWidth(4, 120);
   pTable->SetColWidth(5, 140);
   pTable->SetRowNumber(Statistics.NumberPlayers()+1);
   {
      for (int i=0; i<Statistics.NumberPlayers(); i++) {
         pTable->SetText      (0, i+1, Statistics.GetData(i).playerName);
         pTable->SetFormatText(0, i+1, DT_LEFT | DT_VCENTER | DT_SINGLELINE);
      }
      int index = Statistics.FindName(gpMosaic->GetPlayerName());
      if (index>=0)
         pTable->SetCurrentCell(0, index+1, true);
      index = Statistics.FindName(nsPlayerName::szRobotNameDefault);
      if (index>=0)
         pTable->SetImage(0, index+1, gpMosaic->GetImageBtnPause(3));
   }
   ShowPage();
   SetCaption();

   RECT rect = {0,0,776,300};
   const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                             GetSystemMetrics(SM_CYSCREEN)};
   MoveWindow(hwnd,
      sizeScreen.x/2 - (rect.right -rect.left)/2,
      sizeScreen.y/2 - (rect.bottom-rect.top )/2,
      rect.right -rect.left,
      rect.bottom-rect.top,
      TRUE);

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
   delete pTable;
   EndDialog(hwnd, 0);
}

// WM_SIZE
void Cls_OnSize(HWND hwnd, UINT state, int cx, int cy) {
   RECT wndRect;
   const int hB = 30;                             // высота кнопок
   GetClientRect(hwnd, &wndRect);
   const int wB = (wndRect.right-wndRect.left)/4; // ширина кнопок
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL), 0,0,wndRect.right,wndRect.bottom-hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    ), 0*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     ), 1*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL), 2*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       ), 3*wB,wndRect.bottom-hB,wB,hB, TRUE);
   GetClientRect(hTabCtrl, &wndRect);
   const RECT tabRect = {2,39,wndRect.right-2,wndRect.bottom-3};
   MoveWindow(pTable->GetHandle(),
      tabRect.left,
      tabRect.top,
      tabRect.right-tabRect.left,
      tabRect.bottom-tabRect.top,
      TRUE);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_SIZE      , Cls_OnSize);
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
   TCHAR buf[64];
   _stprintf(buf, TEXT("Open (max %d%%)\0"), 100-(int)GetPercentMine(localSkillLevel, localFigure));
   pTable->SetText(3, 0, buf);
   for (int i=0; i<Statistics.NumberPlayers(); i++) {
      const TsSttstcSubRecord& rec = Statistics.GetData(i).record[localFigure][localSkillLevel];
      const int gameNumber = rec.GameNumber ? rec.GameNumber : 1;
      _stprintf(buf, TEXT("%d\0"), rec.GameNumber);
      pTable->SetText(1, i+1, buf);
      _stprintf(buf, TEXT("%d / %.3f%%\0"), rec.GameWin, ((float)rec.GameWin/(float)gameNumber)*100.f);
      pTable->SetText(2, i+1, buf);
      _stprintf(buf, TEXT("%.3f%%\0"), ((float)rec.OpenField /(float)gameNumber)*100.f/(float)(CSizeField[localSkillLevel].x*CSizeField[localSkillLevel].y));
      pTable->SetText(3, i+1, buf);
      if (rec.PlayTime)
         _stprintf(buf, TEXT("%.3f sec\0"),  (float)rec.PlayTime  /(float)rec.GameWin);
      else
         _tcscpy(buf, TEXT("???"));
      pTable->SetText(4, i+1, buf);
      if (rec.ClickCount)
         _stprintf(buf, TEXT("%.3f\0"),  (float)rec.ClickCount/(float)rec.GameWin);
      pTable->SetText(5, i+1, buf);
   }
}

void InsertResult(const TsSttstcSubRecord& insertRecord, const TeFigure& figure, const TeSkillLevel& skill, const TCHAR* playerName) {
   if (skill == skillLevelCustom) return;
   Statistics.Insert(insertRecord, figure, skill, playerName);
}

int NumberPlayers() {
   return Statistics.NumberPlayers();
}

LPCTSTR GetPlayers(const int index) {
   return Statistics.GetData(index).playerName;
}

int FindName(LPCTSTR szPlayerName) {
   return Statistics.FindName(szPlayerName);
}

bool SetPassword(LPCTSTR szPlayerName, LPCTSTR szNewPassword) {
   const index = Statistics.FindName(szPlayerName);
   if (index < 0) return false;
   return Statistics.SetPassword(index, szNewPassword);
}

bool GetPassword(LPCTSTR szPlayerName, LPTSTR szPassword) {
   const index = Statistics.FindName(szPlayerName);
   if (index < 0) return false;
   _tcscpy(szPassword, Statistics.GetData(index).password);
   return true;
}

bool Rename(LPCTSTR szPlayerNameOld, LPCTSTR szPlayerNameNew) {
   const index = Statistics.FindName(szPlayerNameOld);
   if (index < 0) return false;
   Statistics.RenamePlayer(index, szPlayerNameNew);
   return true;
}

bool Remove(LPCTSTR szPlayerName) {
   const index = Statistics.FindName(szPlayerName);
   if (index < 0) return false;
   Statistics.RemovePlayer(index);
   return true;
}

} // namespace nsStatistics
