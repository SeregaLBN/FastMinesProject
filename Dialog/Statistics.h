////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Statistics.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__STATISTICS__
#define __FILE__STATISTICS__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "PlayerName.h"
#include "../Mosaic.h"

namespace nsStatistics {
   struct CSttstcSubRecord {
      DWORD
         m_dwGameNumber, // ���������� �������� ���
         m_dwGameWin,    // ���������� ��������� ���
         m_dwOpenField,  // ��������� ����� �������� ����� - ������ ������� ������� �������� ����
         m_dwPlayTime,   // ��������� ����� ��� - ������ ������� �������� ����� ����� ������� �� ������ �����
         m_dwClickCount; // ��������� ����� ������ - ������ ������� ����� ������ � ������ ����
      CSttstcSubRecord():
         m_dwGameNumber(0),
         m_dwGameWin   (0),
         m_dwOpenField (0),
         m_dwPlayTime  (0),
         m_dwClickCount(0) {};
   };
   CSttstcSubRecord operator+=(CSttstcSubRecord&, const CSttstcSubRecord&);
   struct CSttstcRecord {
      TCHAR            m_szPlayerName[nsPlayerName::MAX_PLAYER_NAME_LENGTH];
      TCHAR            m_szPassword  [nsPlayerName::MAX_PASSWORD_LENGTH];
      CSttstcSubRecord m_Record[nsMosaic::mosaicNil][nsMosaic::skillLevelCustom];
      CSttstcRecord() {m_szPlayerName[0]=TEXT('\0');
                       m_szPassword  [0]=TEXT('\0');};
   };

   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void InsertResult(const CSttstcSubRecord& insertRecord, const nsMosaic::EMosaic &mosaic, const nsMosaic::ESkillLevel &skill, const TCHAR* szPlayerName);
   int NumberPlayers();
   LPCTSTR GetPlayers(const int index);
   int  FindName   (LPCTSTR szPlayerName);
   bool Remove     (LPCTSTR szPlayerName);
   bool Rename     (LPCTSTR szPlayerNameOld, LPCTSTR szPlayerNameNew);
   bool SetPassword(LPCTSTR szPlayerName   , LPCTSTR szNewPassword);
   bool GetPassword(LPCTSTR szPlayerName   , LPTSTR  szPassword);
}

#endif // __FILE__STATISTICS__
