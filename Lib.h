////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Lib.h"
//
// набор общих ф-ций конкретно не связанных ни с каким из модулей
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_LIB
#define FILE_LIB

#include ".\Preproc.h"
#include <windows.h>
#include <tchar.h>

#define chDIMOF(Array) (sizeof(Array) / sizeof(Array[0])) // количество элементов в массиве  © Джеффри Рихтер

///////////////////////////// chBEGINTHREADEX Macro ///////////////////////////
// This macro function calls the C runtime's _beginthreadex function. 
// The C runtime library doesn't want to have any reliance on Windows' data 
// types such as HANDLE. This means that a Windows programmer needs to cast
// values when using _beginthreadex. Since this is terribly inconvenient, 
// I created this macro to perform the casting.
// © Джеффри Рихтер
typedef unsigned (__stdcall *PTHREAD_START) (void *);

#define chBEGINTHREADEX(psa, cbStack, pfnStartAddr, \
   pvParam, fdwCreate, pdwThreadId)                 \
      ((HANDLE)_beginthreadex(                      \
         (void *)        (psa),                     \
         (unsigned)      (cbStack),                 \
         (PTHREAD_START) (pfnStartAddr),            \
         (void *)        (pvParam),                 \
         (unsigned)      (fdwCreate),               \
         (unsigned *)    (pdwThreadId)))



void DelFileFromFullPath(TCHAR* szFullPath);
void DelPathFromFullPath(TCHAR* szFullPath);

BOOL RegClass(
    UINT    style,
    WNDPROC lpfnWndProc,
    int     cbClsExtra,
    int     cbWndExtra,
    HINSTANCE hInstance,
    HICON   hIcon,
    HCURSOR hCursor,
    HBRUSH  hbrBackground, 
    LPCTSTR lpszMenuName, 
    LPCTSTR lpszClassName);

RECT FindInnerRect(const POINT& inner, const POINT& outward);

bool operator== (const POINT&, const POINT&);
bool operator!= (const POINT&, const POINT&);

#endif // FILE_LIB
