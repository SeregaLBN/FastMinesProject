////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Info.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_INFO
#define FILE_INFO

#include "..\Preproc.h"
#include <windows.h>

#ifdef USE_INFO_DIALOG
namespace nsInfo {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   void AddString(LPCTSTR prefix, LPCTSTR str = TEXT(""), LPCTSTR sufix = TEXT(""));
   void AddValue (LPCTSTR, const int  , const int=10);
   void AddValue (LPCTSTR, const POINT, const int=10);
   void AddValue (LPCTSTR, const POINT, const POINT, int=10);
   void AddValue (LPCTSTR, const double);
   void AddValue (LPCTSTR, const bool*, const int);
   HWND GetHandle(int id);
   void ClearEdit();
   void LogFilePut(LPCTSTR);
   void LogFilePut(const bool*, const int);
}
#endif // USE_INFO_DIALOG

#endif // FILE_INFO
