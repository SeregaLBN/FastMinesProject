////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Lib.c"
// функции общего назначения
////////////////////////////////////////////////////////////////////////////////

#include ".\Lib.h"

void DelFileFromFullPath(TCHAR* szFullPath) {
   for (int i=_tcslen(szFullPath); i; i--)
      if (szFullPath[i] == TEXT('\\')) {
         szFullPath[i+1] = TEXT('\0');
         return;
      }
}

void DelPathFromFullPath(TCHAR* szFullPath) {
   TCHAR* szPath = szFullPath;
   TCHAR* szFile = szFullPath;
   for (; *szPath; szPath++)
      if (*szPath == TEXT('\\')) {
         szFile = szPath+1;
      }
   if (szFile != szFullPath)
      _tcscpy(szFullPath, szFile);
}

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
    LPCTSTR lpszClassName)
{	WNDCLASS wc;

   wc.style         = style;
   wc.lpfnWndProc   = lpfnWndProc;
   wc.cbClsExtra    = cbClsExtra;
   wc.cbWndExtra    = cbWndExtra;
   wc.hInstance     = hInstance;
   wc.hIcon         = hIcon;
   wc.hCursor       = hCursor;
   wc.hbrBackground = hbrBackground;
   wc.lpszMenuName  = lpszMenuName;
   wc.lpszClassName = lpszClassName;
   return (RegisterClass(&wc) != 0);
}

RECT FindInnerRect(const POINT& inner, const POINT& outward) {
   POINTFLOAT percent = {(float)inner.x/outward.x,
                         (float)inner.y/outward.y};
   RECT Rect = {0,0,0,0};
   if (percent.x > percent.y) {
      Rect.right  = outward.x;
      Rect.bottom = (long)((float)inner.y/percent.x);

      Rect.top    = (long)((float)outward.y/2-(float)Rect.bottom/2);
      Rect.bottom = (long)((float)outward.y/2+(float)Rect.bottom/2);
   } else {
      Rect.right  = (long)((float)inner.x/percent.y);
      Rect.bottom = outward.y;

      Rect.left   = (long)((float)outward.x/2-(float)Rect.right/2);
      Rect.right  = (long)((float)outward.x/2+(float)Rect.right/2);
   }
   return Rect;
}

bool operator== (const POINT &a, const POINT &b) {
   return (a.x==b.x &&
           a.y==b.y);
}

bool operator!= (const POINT &a, const POINT &b) {
   return (a.x!=b.x ||
           a.y!=b.y);
}
