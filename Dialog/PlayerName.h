////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PlayerName.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PLAYERNAME__
#define __FILE__PLAYERNAME__

#ifndef __AFX_H__
   #include <Windows.h>
#endif

namespace nsPlayerName {
   extern bool firstLoad;

   const int MAX_PLAYER_NAME_LENGTH = 20;
   const int MAX_PASSWORD_LENGTH    = 20;
   const TCHAR SZ_ASSISTANT_NAME_DEFAULT[MAX_PLAYER_NAME_LENGTH] = TEXT("Assistant");

   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
}

#endif // __FILE__PLAYERNAME__
