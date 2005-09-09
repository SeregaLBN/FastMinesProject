////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Statistics.cpp"
// обработка диалогового окна "Statistics"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Statistics.h"
#include <WindowsX.h>
#include <CommCtrl.h>
#include <algorithm>
#include "../ID_resource.h"
#include "CommonLib.h"
#include "../EraseBk.h"
#include "../Lang.h"
#include "../Control/Table.h"
#include "../FastMines2.h"
#include "../OldVersion.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsStatistics {
////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
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

const TCHAR SZ_FILE_NAME_STATISTICS[] = TEXT("Mines.stc");

class CStatistics {
private:
   std::vector<CSttstcRecord*> m_Players;
   bool m_bInit;
private:
   int AddRecord(LPCTSTR);
   void Init();
#ifndef UNICODE
   BOOL Conversion(HANDLE hFile);
#endif // UNICODE
public:
   CStatistics(): m_bInit(false) {};
  ~CStatistics();
   int FindName (LPCTSTR) const;
   void Insert(const CSttstcSubRecord&, const nsMosaic::EMosaic&, const nsMosaic::ESkillLevel&, LPCTSTR playerName);
   int NumberPlayers() const; // количество игроков
   const CSttstcRecord& GetData(const int index) const;
   bool SetPassword (int index, LPCTSTR szNewPassword);
   bool RenamePlayer(int index, LPCTSTR szNewName);
   bool RemovePlayer(int index);
};

////////////////////////////////////////////////////////////////////////////////
//                       global variables this namespaces
////////////////////////////////////////////////////////////////////////////////
HWND hDlg = NULL;
HWND hTabCtrl;
HIMAGELIST hImageList;
nsMosaic::ESkillLevel localSkillLevel;
nsMosaic::EMosaic localMosaic;

CTable *pTable;
CStatistics Statistics;

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

////////////////////////////////////////////////////////////////////////////////
//                         implementation - other
////////////////////////////////////////////////////////////////////////////////
CSttstcSubRecord operator+=(CSttstcSubRecord& sr1, const CSttstcSubRecord& sr2) {
   sr1.m_dwGameNumber += sr2.m_dwGameNumber;
   sr1.m_dwGameWin    += sr2.m_dwGameWin   ;
   sr1.m_dwOpenField  += sr2.m_dwOpenField ;
   sr1.m_dwPlayTime   += sr2.m_dwPlayTime  ;
   sr1.m_dwClickCount += sr2.m_dwClickCount;
   return sr1;
};

////////////////////////////////////////////////////////////////////////////////
//                         implementation - CStatistics
////////////////////////////////////////////////////////////////////////////////
inline int CStatistics::FindName(LPCTSTR playerName) const {
   if (!m_bInit) const_cast<CStatistics*>(this)->Init();
   for (int i=0; i<m_Players.size(); i++) {
    //if (!_tcscmp (m_Players[i]->m_szPlayerName, playerName))
      if (!lstrcmpi(m_Players[i]->m_szPlayerName, playerName))
         return i;
   }
   return -1;
};

inline int CStatistics::AddRecord(LPCTSTR playerName) {
   if (!m_bInit) Init();
   CSttstcRecord* newSttstcRecord = new CSttstcRecord;
   lstrcpy(newSttstcRecord->m_szPlayerName, playerName);
   m_Players.push_back(newSttstcRecord);
   return m_Players.size()-1; // индекс вставленного (нового) элемента
};

inline void CStatistics::Init() {
   m_bInit = true;
   // load data from file
   CString strPath(GetModuleDir(ghInstance) + SZ_FILE_NAME_STATISTICS);
   HANDLE hFile = CreateFile(
      strPath,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile != INVALID_HANDLE_VALUE) {
      CString strError;
      DWORD dwNOBR = 0;
      TCHAR szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))] = {0};

      if (!ReadFile(hFile, szVersion, sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBR, NULL)) { // считываю заголовок файла
         strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_READ];
      } else {
         if (sizeof(TEXT(ID_VERSIONINFO_VERSION3)) != dwNOBR) {
            strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA]; // слишком мало данных в файле
         } else {
            if (lstrcmp(TEXT(ID_VERSIONINFO_VERSION3), szVersion)) {
#ifndef UNICODE
               Conversion(hFile);
#else
               strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_VERSION];
#endif // UNICODE
            } else {
               const DWORD dwSizeDataCode = GetFileSize(hFile, NULL)-dwNOBR; // размер данных без заголовка
               if (dwSizeDataCode > 0) {
                  TCHAR *szCodeData = (TCHAR*)new BYTE[dwSizeDataCode+sizeof(TCHAR)]; szCodeData[dwSizeDataCode] = 0; // EOL
                  //ZeroMemory(szCodeData, sizeData);
                  if (!ReadFile(hFile, szCodeData, dwSizeDataCode, &dwNOBR, NULL)) {
                     strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_READ]; // ошибка чтения
                  } else {
                     if (dwSizeDataCode != dwNOBR) {
                        strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_READ]; // считан неверный обьём данных
                     } else {
                        CString strCodeData = szCodeData;
                        DWORD dwSizeDataDecode = dwSizeDataCode;
                        VOID *pData = new BYTE[dwSizeDataDecode];
                        if (!Base64_decode(strCodeData, (BYTE*)pData, dwSizeDataDecode)) {
                           strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA];
                        } else {
                           for (int i=0; i<dwSizeDataDecode/sizeof(CSttstcRecord); i++) {
                              m_Players.push_back(new CSttstcRecord); // создаю новую запись
                              memcpy( // копирую в новую запись данные игрока из файла
                                 m_Players[i],
                                 ((CSttstcRecord*)pData)+i,
                                 sizeof(CSttstcRecord)
                              );
                           }
                           if (dwSizeDataDecode%sizeof(CSttstcRecord)) {
                              strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA]; // лишние данные ???
                           }
                        }
                        delete [] pData;
                     }
                  }
                  delete [] szCodeData;
               }
            }
         }
      }
      if (!strError.IsEmpty()) {
         MessageBox(gpFM2Proj->GetHandle(), strError, CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      }
      CloseHandle(hFile);
   }
};

inline CStatistics::~CStatistics() {
   // save data in file
   CString strPath(GetModuleDir(ghInstance) + SZ_FILE_NAME_STATISTICS);
   HANDLE hFile = CreateFile(
      strPath,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile == INVALID_HANDLE_VALUE) {
      MessageBox(gpFM2Proj->GetHandle(), CLang::m_StrArr[IDS__STATISTICS__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
   } else {
      CString strError;
      // записываю заголовок файла
      const TCHAR szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))] = {TEXT(ID_VERSIONINFO_VERSION3)};
      DWORD dwNOBW = 0;
      if (!WriteFile(hFile, szVersion, sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBW, NULL) ||
          (sizeof(TEXT(ID_VERSIONINFO_VERSION3)) != dwNOBW)
         )
      {
         strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_WRITE];
      } else {
         // записываю данные
         if (m_Players.size() > 0) {
            DWORD dwDataSizeDecode = m_Players.size() * sizeof(CSttstcRecord);
            BYTE *pData = new BYTE[dwDataSizeDecode];
            for (int i=0; i<m_Players.size(); i++) {
               memcpy(((CSttstcRecord*)pData)+i, m_Players[i], sizeof(CSttstcRecord));
            }
            CString strCode;
            if (!Base64_code(pData, dwDataSizeDecode, strCode)) {
               strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA];
            } else {
               if (!WriteFile(hFile, (LPCTSTR)strCode, strCode.GetLength(), &dwNOBW, NULL) ||
                   (strCode.GetLength() != dwNOBW)
                  )
               {
                  strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_WRITE];
               }
            }
            delete [] pData;
         }
      }
      if (!strError.IsEmpty()) {
         MessageBox(gpFM2Proj->GetHandle(), strError, CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      }
      CloseHandle(hFile);
   }

   // destroy others
   for (int i=0; i<m_Players.size(); i++)
      delete m_Players[i];
   m_Players.clear();
};

inline void CStatistics::Insert(const CSttstcSubRecord &insertRecord, const nsMosaic::EMosaic &mosaic, const nsMosaic::ESkillLevel &skill, LPCTSTR playerName) {
   if (!m_bInit) Init();
   int index = FindName(playerName);
   if (index < 0) index = AddRecord(playerName);
   m_Players[index]->m_Record[mosaic][skill] += insertRecord;
};

inline int CStatistics::NumberPlayers() const {
   if (!m_bInit) const_cast<CStatistics*>(this)->Init();
   return m_Players.size();
};

inline const CSttstcRecord& CStatistics::GetData(const int index) const {
   if (!m_bInit) const_cast<CStatistics*>(this)->Init();
   return *m_Players[index];
};

bool CStatistics::SetPassword(int index, LPCTSTR szNewPassword) {
   if (!m_bInit) Init();
   if ((index<0) || (index >= m_Players.size())) return false;
   lstrcpy(m_Players[index]->m_szPassword, szNewPassword);
   return true;
}

bool CStatistics::RenamePlayer(int index, LPCTSTR szNewName) {
   if (!m_bInit) Init();
   if ((index<0) || (index >= m_Players.size())) return false;
   lstrcpy(m_Players[index]->m_szPlayerName, szNewName);
   return true;
}

bool CStatistics::RemovePlayer(int index) {
   if (!m_bInit) Init();
   if ((index<0) || (index >= m_Players.size())) return false;
   delete m_Players[index];
   m_Players.erase(std::find(m_Players.begin(), m_Players.end(), m_Players[index]));
   return true;
}

#ifndef UNICODE
BOOL CStatistics::Conversion(HANDLE hFile) {
   CString strError;
   ::SetFilePointer(hFile, 0, NULL, FILE_BEGIN);

   // считываю заголовок файла
   DWORD dwNOBR;
   CHAR szVersion[chDIMOF(ID_VERSIONINFO_VERSION3_v210)];
   if (!ReadFile(hFile, szVersion, sizeof(ID_VERSIONINFO_VERSION3_v210), &dwNOBR, NULL)) {
      strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_READ];
   } else {
      if (sizeof(ID_VERSIONINFO_VERSION3_v210) != dwNOBR) {
         strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA]; // слишком мало данных в файле
      } else {
         if (lstrcmpA(ID_VERSIONINFO_VERSION3_v210, szVersion)) {
            strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_VERSION];
         } else {
            // считываю данные (по частям - по одному игроку)
            const DWORD dwSizeData = ::GetFileSize(hFile, NULL)-dwNOBR; // размер данных без заголовка
            nsVer210::CSttstcFile StcFile;
            for (int i=0; i<dwSizeData/sizeof(nsVer210::CSttstcFile); i++) {
               if (!ReadFile(hFile, &StcFile, sizeof(nsVer210::CSttstcFile), &dwNOBR, NULL) ||
                   (sizeof(nsVer210::CSttstcFile) != dwNOBR) 
                  )
               {
                  strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_READ]; // ошибка чтения
                  break;
               } else {
                  DWORD dwVerifyByteCRC = 0;
                  for (int j=0; j<sizeof(nsVer210::CSttstcRecord)/sizeof(DWORD); j++) {
                     *((DWORD*)(&StcFile.m_DataStc)+j) ^= *((DWORD*)(&StcFile.m_DataBad)+j); // размаскирую данные
                     dwVerifyByteCRC                   ^= *((DWORD*)(&StcFile.m_DataStc)+j); // пересчёт CRC
                     dwVerifyByteCRC                   ^= *((DWORD*)(&StcFile.m_DataBad)+j); // пересчёт CRC
                  }
                  if (dwVerifyByteCRC != StcFile.m_dwCRC) { // cравнениe CRC
                     strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA]; // CRC несовпало - ошибка
                     break;
                  }
                  const size_t k = m_Players.size();
                  m_Players.push_back(new CSttstcRecord); // создаю новую запись
                  // копирую в новую запись данные игрока из файла
                  memcpy(m_Players[k]->m_szPlayerName, (LPCTSTR)CString(StcFile.m_DataStc.m_szPlayerName), min(nsPlayerName::MAX_PLAYER_NAME_LENGTH, nsVer210::MAX_PLAYER_NAME_LENGTH)+1);
                  memcpy(m_Players[k]->m_szPassword  , (LPCTSTR)CString(StcFile.m_DataStc.m_szPassword  ), min(nsPlayerName::MAX_PASSWORD_LENGTH   , nsVer210::MAX_PASSWORD_LENGTH   )+1);
#define REPLACE_MOSAIC_SKILLLEVEL(mosaicName, skillLevel) \
                  m_Players[k]->m_Record[nsMosaic::mosaicName][nsMosaic::skillLevel].m_dwGameNumber = StcFile.m_DataStc.m_Record[nsVer210::mosaicName][nsVer210::skillLevel].m_dwGameNumber; \
                  m_Players[k]->m_Record[nsMosaic::mosaicName][nsMosaic::skillLevel].m_dwGameWin    = StcFile.m_DataStc.m_Record[nsVer210::mosaicName][nsVer210::skillLevel].m_dwGameWin   ; \
                  m_Players[k]->m_Record[nsMosaic::mosaicName][nsMosaic::skillLevel].m_dwOpenField  = StcFile.m_DataStc.m_Record[nsVer210::mosaicName][nsVer210::skillLevel].m_dwOpenField ; \
                  m_Players[k]->m_Record[nsMosaic::mosaicName][nsMosaic::skillLevel].m_dwPlayTime   = StcFile.m_DataStc.m_Record[nsVer210::mosaicName][nsVer210::skillLevel].m_dwPlayTime  ; \
                  m_Players[k]->m_Record[nsMosaic::mosaicName][nsMosaic::skillLevel].m_dwClickCount = StcFile.m_DataStc.m_Record[nsVer210::mosaicName][nsVer210::skillLevel].m_dwClickCount
#define REPLACE_MOSAIC(mosaicName) \
                  REPLACE_MOSAIC_SKILLLEVEL(mosaicName, skillLevelBeginner    ); \
                  REPLACE_MOSAIC_SKILLLEVEL(mosaicName, skillLevelAmateur     ); \
                  REPLACE_MOSAIC_SKILLLEVEL(mosaicName, skillLevelProfessional); \
                  REPLACE_MOSAIC_SKILLLEVEL(mosaicName, skillLevelCrazy       )

                  REPLACE_MOSAIC(mosaicTriangle1   );
                  REPLACE_MOSAIC(mosaicTriangle2   );
                  REPLACE_MOSAIC(mosaicTriangle3   );
                  REPLACE_MOSAIC(mosaicTriangle4   );
                  REPLACE_MOSAIC(mosaicSquare1     );
                  REPLACE_MOSAIC(mosaicSquare2     );
                  REPLACE_MOSAIC(mosaicParquet1    );
                  REPLACE_MOSAIC(mosaicParquet2    );
                  REPLACE_MOSAIC(mosaicTrapezoid1  );
                  REPLACE_MOSAIC(mosaicTrapezoid2  );
                  REPLACE_MOSAIC(mosaicTrapezoid3  );
                  REPLACE_MOSAIC(mosaicRhombus1    );
                  REPLACE_MOSAIC(mosaicQuadrangle1 );
                  REPLACE_MOSAIC(mosaicPentagonT24 );
                  REPLACE_MOSAIC(mosaicPentagonT5  );
                  REPLACE_MOSAIC(mosaicPentagonT10 );
                  REPLACE_MOSAIC(mosaicHexagon1    );
                  REPLACE_MOSAIC(mosaicTrSq1       );
                  REPLACE_MOSAIC(mosaicTrSq2       );
                  REPLACE_MOSAIC(mosaicSqTrHex     );
               }
            }
            if (dwSizeData%sizeof(nsVer210::CSttstcFile)) {
               strError = CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA]; // лишние данные ???
            }
         }
      }
   }
   if (!strError.IsEmpty()) {
      ::MessageBox(gpFM2Proj->GetHandle(), strError, CLang::m_StrArr[IDS__ERROR], MB_ICONERROR | MB_OK);
   }
   return strError.IsEmpty();
}
#endif // UNICODE

////////////////////////////////////////////////////////////////////////////////
//                        implementation - dialog function
////////////////////////////////////////////////////////////////////////////////
inline void SetCaption() {
   CString strCaption(
      CLang::m_StrArr[IDS__STATISTICS] +
      TEXT(" - ") +
      CLang::m_StrArr[IDS__MOSAIC_NAME_00+localMosaic]
   );
   SetWindowText(hDlg, strCaption);
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

   SetWindowLong(hDlg, GWL_STYLE, WS_MAXIMIZEBOX ^ GetWindowLong(hDlg, GWL_STYLE));

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
   pTable->Create(hTabCtrl, ID_DIALOG_STATISTICSorCHAMPIONS_TABLE);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetBkColor(gpFM2Proj->GetSkin().m_colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   pTable->SetText(0,0, CLang::m_StrArr[IDS__STATISTICS__PLAYER_NAME           ]);
   pTable->SetText(1,0, CLang::m_StrArr[IDS__STATISTICS__NUMBER__GAMES         ]);
   pTable->SetText(2,0, CLang::m_StrArr[IDS__STATISTICS__GAMES_WON             ]);
   CString strBuf;
   strBuf.Format(TEXT("%s (%s %d%%)"), (LPCTSTR)CLang::m_StrArr[IDS__STATISTICS__OPEN], (LPCTSTR)CLang::m_StrArr[IDS__MAX], 100-(int)nsMosaic::GetPercentMine(localSkillLevel, localMosaic));
   pTable->SetText(3,0, strBuf);
   pTable->SetText(4,0, CLang::m_StrArr[IDS__STATISTICS__AVERAGED_GAME_TIME    ]);
   pTable->SetText(5,0, CLang::m_StrArr[IDS__STATISTICS__AVERAGED_NUMBER_CLICKS]);
   pTable->SetColWidth(0, 150);
   pTable->SetColWidth(3, 120);
   pTable->SetColWidth(4, 120);
   pTable->SetColWidth(5, 140);
   pTable->SetRowNumber(Statistics.NumberPlayers()+1);
   {
      for (int i=0; i<Statistics.NumberPlayers(); i++) {
         pTable->SetText      (0, i+1, Statistics.GetData(i).m_szPlayerName);
         pTable->SetFormatText(0, i+1, DT_LEFT | DT_VCENTER | DT_SINGLELINE);
      }
      int index = Statistics.FindName(gpFM2Proj->GetPlayerName());
      if (index>=0)
         pTable->SetCurrentCell(0, index+1, true);
      index = Statistics.FindName(nsPlayerName::SZ_ASSISTANT_NAME_DEFAULT);
      if (index>=0)
         pTable->SetImage(0, index+1, gpFM2Proj->GetImageBtnPause(3));
   }
   ShowPage();
   SetCaption();

   RECTEX rect(0,0,807,300);
   const SIZE sizeScreen = GetScreenSize();
   MoveWindow(hwnd,
      sizeScreen.cx/2 - rect.width ()/2,
      sizeScreen.cy/2 - rect.height()/2,
      rect.width (),
      rect.height(),
      TRUE);

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
   delete pTable;
   EndDialog(hwnd, 0);
}

// WM_SIZE
void OnSize(HWND hwnd, UINT state, int cx, int cy) {
   RECTEX wndRect;
   const int hB = 30;                             // высота кнопок
   GetClientRect(hwnd, &wndRect);
   const int wB = wndRect.width()/4; // ширина кнопок
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_TABCONTROL), 0,0,wndRect.right,wndRect.bottom-hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_BEGINNER    ), 0*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_AMATEUR     ), 1*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_PROFESSIONAL), 2*wB,wndRect.bottom-hB,wB,hB, TRUE);
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_STATISTICSorCHAMPIONS_BUTTON_CRAZY       ), 3*wB,wndRect.bottom-hB,wB,hB, TRUE);
   GetClientRect(hTabCtrl, &wndRect);
   const RECTEX tabRect(2,39,wndRect.right-2,wndRect.bottom-3);
   MoveWindow(pTable->GetHandle(), tabRect);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch (msg){
   HANDLE_MSG(hDlg, WM_SIZE      , OnSize);
   HANDLE_MSG(hDlg, WM_INITDIALOG, OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , OnCommand);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hDlg, WM_CLOSE     , OnClose);
   HANDLE_WM_EX_CTLCOLOR(hDlg, gpFM2Proj->GetSkin());
   case WM_NOTIFY:
      OnNotify(hDlg, (int)wParam, (LPNMHDR)lParam);
   }
   return FALSE;
}

void ShowPage() {
   CString strBuf;
   strBuf.Format(TEXT("%s (%s %d%%)"), (LPCTSTR)CLang::m_StrArr[IDS__STATISTICS__OPEN], (LPCTSTR)CLang::m_StrArr[IDS__MAX], 100-(int)nsMosaic::GetPercentMine(localSkillLevel, localMosaic));
   pTable->SetText(3, 0, strBuf);
   for (int i=0; i<Statistics.NumberPlayers(); i++) {
      const CSttstcSubRecord& rec = Statistics.GetData(i).m_Record[localMosaic][localSkillLevel];
      const int gameNumber = rec.m_dwGameNumber ? rec.m_dwGameNumber : 1;
      strBuf.Format(TEXT("%d"), rec.m_dwGameNumber);
      pTable->SetText(1, i+1, strBuf);
      strBuf.Format(TEXT("%d / %.3f%%"), rec.m_dwGameWin, ((float)rec.m_dwGameWin/(float)gameNumber)*100.f);
      pTable->SetText(2, i+1, strBuf);
      strBuf.Format(TEXT("%.3f%%"), ((float)rec.m_dwOpenField /(float)gameNumber)*100.f/(float)(nsMosaic::SIZE_MOSAIC[localSkillLevel].cx*nsMosaic::SIZE_MOSAIC[localSkillLevel].cy));
      pTable->SetText(3, i+1, strBuf);
      if (rec.m_dwPlayTime)
         strBuf.Format(TEXT("%.3f %s"),  (float)rec.m_dwPlayTime/(float)rec.m_dwGameWin, (LPCTSTR)CLang::m_StrArr[IDS__SEC]);
      else
         strBuf = TEXT("???");
      pTable->SetText(4, i+1, strBuf);
      if (rec.m_dwClickCount)
         strBuf.Format(TEXT("%.3f"),  (float)rec.m_dwClickCount/(float)rec.m_dwGameWin);
      pTable->SetText(5, i+1, strBuf);
   }
}

void InsertResult(const CSttstcSubRecord& insertRecord, const nsMosaic::EMosaic& mosaic, const nsMosaic::ESkillLevel& skill, const TCHAR* playerName) {
   if (skill == nsMosaic::skillLevelCustom) return;
   Statistics.Insert(insertRecord, mosaic, skill, playerName);
}

int NumberPlayers() {
   return Statistics.NumberPlayers();
}

LPCTSTR GetPlayers(const int index) {
   return Statistics.GetData(index).m_szPlayerName;
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
   lstrcpy(szPassword, Statistics.GetData(index).m_szPassword);
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
