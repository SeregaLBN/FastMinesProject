////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Statistics.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_STATISTICS
#define FILE_STATISTICS

#include "..\Preproc.h"
#include <windows.h>
#include "..\TcMosaic.h"
#include ".\PlayerName.h"

namespace nsStatistics {
   struct TsSttstcSubRecord {
      DWORD
         GameNumber, // количество сыграных игр
         GameWin,    // количество выиграных игр
         OpenField,  // суммарное число открытых €чеек - вывожу средний процент открыти€ пол€
         PlayTime,   // суммарное врем€ игр - вывожу сколько всреднем игрок провЄл времени за данной игрой
         ClickCount; // суммарное число кликов - вывожу среднее число кликов в данной игре
      TsSttstcSubRecord():
         GameNumber(0),
         GameWin   (0),
         OpenField (0),
         PlayTime  (0),
         ClickCount(0) {};
   };
   TsSttstcSubRecord operator+=(TsSttstcSubRecord&, const TsSttstcSubRecord&);
   struct TsSttstcRecord {
      TCHAR             playerName[nsPlayerName::maxPlayerNameLength];
      TCHAR             password  [nsPlayerName::maxPasswordLength];
      TsSttstcSubRecord record [figureNil][skillLevelCustom];
      TsSttstcRecord() {playerName[0]=TEXT('\0');
                        password  [0]=TEXT('\0');};
   };

   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void InsertResult(const TsSttstcSubRecord& insertRecord, const TeFigure& figure, const TeSkillLevel& skill, const TCHAR* playerName);
   int NumberPlayers();
   LPCTSTR GetPlayers(const int index);
   int  FindName   (LPCTSTR szPlayerName);
   bool Remove     (LPCTSTR szPlayerName);
   bool Rename     (LPCTSTR szPlayerNameOld, LPCTSTR szPlayerNameNew);
   bool SetPassword(LPCTSTR szPlayerName   , LPCTSTR szNewPassword);
   bool GetPassword(LPCTSTR szPlayerName   , LPTSTR  szPassword);
}

#endif // FILE_STATISTICS
