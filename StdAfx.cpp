#include "stdafx.h"

#ifdef _DEBUG
   #include "Logger.h"
   CLogger g_Logger(
      #ifdef _DEBUG
         CLogger::LL_DEBUG,
      #else
         CLogger::LL_INFO,
      #endif
         TEXT("___FastMines_Log.txt")
   );
#endif

