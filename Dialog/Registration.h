////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Registration.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_REGISTRATION
#define FILE_REGISTRATION

#include "..\Preproc.h"
#include <windows.h>

namespace nsRegistration {
   BOOL CALLBACK DialogProc(HWND, UINT, WPARAM, LPARAM);
   bool isRegister(char*, char*);
}

#endif // FILE_REGISTRATION
