////////////////////////////////////////////////////////////////////////////////
// File name: CommonLib.cpp
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 25 10 2004
//
// Description: Функции общего назначения
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <time.h>
#include "CommonLib.h"

#ifdef __AFX_H__
   #ifdef _DEBUG
   #undef THIS_FILE
   static char THIS_FILE[]=__FILE__;
   #define new DEBUG_NEW
   #endif
#endif

BOOL RegClass(
    UINT      style,
    WNDPROC   lpfnWndProc,
    int       cbClsExtra,
    int       cbWndExtra,
    HINSTANCE hInstance,
    HICON     hIcon,
    HCURSOR   hCursor,
    HBRUSH    hbrBackground,
    LPCTSTR   lpszMenuName,
    LPCTSTR   lpszClassName)
{   WNDCLASS wc = { style,
                   lpfnWndProc,
                   cbClsExtra,
                   cbWndExtra,
                   hInstance,
                   hIcon,
                   hCursor,
                   hbrBackground,
                   lpszMenuName,
                   lpszClassName};
   return (RegisterClass(&wc) != 0);
}

void MessageBox_AbortProcess(LPCTSTR szMessage) {
   int iRes = MessageBox(
      NULL,
      (LPCTSTR)(CString(szMessage) + TEXT("\nAbort Programm?")),
      (LPCTSTR)GetModuleFileName(),
      MB_YESNO | MB_ICONERROR
   );
   if (iRes == IDYES) {
      ExitProcess(666);
   }
}

BOOL InitializeCriticalSectionEx(PCRITICAL_SECTION pCS, DWORD dwSpinCount) {
   BOOL res = TRUE;
#if _WIN32_WINNT < 0x0500
   ::InitializeCriticalSection(pCS); // считаю что всегда выполняется успешно...
#else
   res = ::InitializeCriticalSectionAndSpinCount(pCS, dwSpinCount);
   if (!res) {
      memset(pCS, 0, sizeof(CRITICAL_SECTION));
   #ifdef _DEBUG
      MessageBox_AbortProcess(TEXT("Function InitializeCriticalSectionAndSpinCount fails."));
   #endif
   }
#endif
   return res;
}

void Base64_code(
   IN const CHAR* strIn, // на вход  - сторка, которую надо зашифровать
   OUT CString& strOut   // на выход - зашифрованная строка
) {
   const static CHAR base64ABC[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   strOut.Empty();
   const size_t len = strlen(strIn);
   for (size_t i=0; i<len; i+=3) {
      LONG l = (                 ((LONG)strIn[i  ])<<16      ) |
               (((i+1) < len) ? (((LONG)strIn[i+1])<<8 ) : 0 ) |
               (((i+2) < len) ? ( (LONG)strIn[i+2]     ) : 0 );
                     strOut += base64ABC[(l>>18) & 0x3F];
                     strOut += base64ABC[(l>>12) & 0x3F];
      if (i+1 < len) strOut += base64ABC[(l>> 6) & 0x3F];
      if (i+2 < len) strOut += base64ABC[(l    ) & 0x3F];
   }
   switch (len%3) {
   case 1:
      strOut += '=';
   case 2:
      strOut += '=';
   }
}

void Base64_decode(
   IN const CHAR* strIn, // на вход  - зашифрованная строка
   OUT CString& strOut   // на выход - расшифрованная строка
) {
   const static CHAR base64ABC[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   strOut.Empty();
   const size_t len = strlen(strIn);
   for (size_t i=0; i<len; i+=4) {
      LONG l =
         ((                 (strIn[i  ] != '=')) ? (((LONG)(strchr(base64ABC, strIn[i  ]) - base64ABC)) << 18) : 0) |
         ((((i+1) < len) && (strIn[i+1] != '=')) ? (((LONG)(strchr(base64ABC, strIn[i+1]) - base64ABC)) << 12) : 0) |
         ((((i+2) < len) && (strIn[i+2] != '=')) ? (((LONG)(strchr(base64ABC, strIn[i+2]) - base64ABC)) << 6 ) : 0) |
         ((((i+3) < len) && (strIn[i+3] != '=')) ?  ((LONG)(strchr(base64ABC, strIn[i+3]) - base64ABC))        : 0);

      BYTE ch1 = ((l>>16) & 0xFF); if (ch1) strOut += (TCHAR)ch1;
      BYTE ch2 = ((l>>8 ) & 0xFF); if (ch2) strOut += (TCHAR)ch2;
      BYTE ch3 = ( l      & 0xFF); if (ch3) strOut += (TCHAR)ch3;
   }
}

BOOL Base64_code(
   IN BYTE *pData,     // на вход  - данные, которые надо зашифровать
   IN DWORD dwSize,
   OUT CString &strRes // на выход - зашифрованная строка
) {
   if (!pData || !dwSize) {
      ::SetLastError(ERROR_INVALID_DATA); // ERROR_INVALID_PARAMETER
      return FALSE;
   }

   strRes.Empty();
   const static CHAR base64ABC[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   for (size_t i=0; i<dwSize; i+=3) {
      LONG l = (                    ((LONG)pData[i  ])<<16      ) |
               (((i+1) < dwSize) ? (((LONG)pData[i+1])<<8 ) : 0 ) |
               (((i+2) < dwSize) ? ( (LONG)pData[i+2]     ) : 0 );
                        strRes += base64ABC[(l>>18) & 0x3F];
                        strRes += base64ABC[(l>>12) & 0x3F];
      if (i+1 < dwSize) strRes += base64ABC[(l>> 6) & 0x3F];
      if (i+2 < dwSize) strRes += base64ABC[(l    ) & 0x3F];
   }
   switch (dwSize%3) {
   case 1:
      strRes += TEXT('=');
   case 2:
      strRes += TEXT('=');
   }
   return TRUE;
}

BOOL Base64_decode(
   IN const CString &strIn, // на вход  - зашифрованная строка
   OUT BYTE *pData,         // на выход - расшифрованные данные
   IN OUT DWORD &dwSize
) {
   if (strIn.IsEmpty() || !pData || !dwSize) {
      ::SetLastError(ERROR_INVALID_DATA); // ERROR_INVALID_PARAMETER
      return FALSE;
   }

   const static CString base64ABC = TEXT("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
   ZeroMemory(pData, dwSize);
   DWORD dwWrite = 0;
   for (int i=0; i<strIn.GetLength(); i+=4) {
      bool in0=true , in1=false, in2=false, in3=false; // в пределах ли буфера
      bool bE0=false, bE1=false, bE2=false, bE3=false; // не символ ли "="
      int  iF0=0    , iF1=0    , iF2=0    , iF3=0    ; // входит ли символ в Base64 алфавит

      LONG l = ((in0 = (i+0) < strIn.GetLength()) && (bE0 = (strIn[i+0] != TEXT('='))) ? LONG(iF0 = base64ABC.Find(strIn[i  ])) << 18 : 0) |
               ((in1 = (i+1) < strIn.GetLength()) && (bE1 = (strIn[i+1] != TEXT('='))) ? LONG(iF1 = base64ABC.Find(strIn[i+1])) << 12 : 0) |
               ((in2 = (i+2) < strIn.GetLength()) && (bE2 = (strIn[i+2] != TEXT('='))) ? LONG(iF2 = base64ABC.Find(strIn[i+2])) << 6  : 0) |
               ((in3 = (i+3) < strIn.GetLength()) && (bE3 = (strIn[i+3] != TEXT('='))) ? LONG(iF3 = base64ABC.Find(strIn[i+3])) << 0  : 0);
      if ((iF0 == -1) || (iF1 == -1) || (iF2 == -1) || (iF3 == -1)) { // во входную строку "затесался" символ не из Base64 алфавита...
         ZeroMemory(pData, dwSize);
         dwSize = 0;
         ::SetLastError(ERROR_INVALID_DATA); // ERROR_INVALID_PARAMETER
         return FALSE;
      }

      bool bB = true; // хватает ли буфера
      BYTE b1 = ((l>>16) & 0xFF); if (in0 && bE0 && (b1 || bE1) && (bB &= (dwSize > dwWrite))) {pData[dwWrite++] = b1;}
      BYTE b2 = ((l>>8 ) & 0xFF); if (in1 && bE1 && (b2 || bE2) && (bB &= (dwSize > dwWrite))) {pData[dwWrite++] = b2;}
      BYTE b3 = ((l>>0 ) & 0xFF); if (in2 && bE2 && (b3 || bE3) && (bB &= (dwSize > dwWrite))) {pData[dwWrite++] = b3;}
      if (!bB) {
         ::SetLastError(ERROR_INSUFFICIENT_BUFFER);
         return FALSE;
      }
   }
   dwSize = dwWrite;
   return TRUE;
}

RECTEX FindInnerRect(const SIZE &sizeInner, const SIZE &sizeOutward) {
   // Есть размер (sizeOutward) внешнего прямоугольника и
   // размер (sizeInner) прямоугольника который должен быть равномерно вписан 
   // во внешний прямоугольник, т.е. кторый должен быть или увеличен или уменьшен.
   // Относительные координаты этого вписаного прямоугольника и находятся.
   POINTFLOAT percent = {(float)sizeInner.cx/sizeOutward.cx,
                         (float)sizeInner.cy/sizeOutward.cy};
   RECTEX Rect;
   if (percent.x > percent.y) {
      Rect.right  = sizeOutward.cx;
      Rect.bottom = (long)((float)sizeInner.cy/percent.x);

      Rect.top    = (long)((float)sizeOutward.cy/2-(float)Rect.bottom/2);
      Rect.bottom = (long)((float)sizeOutward.cy/2+(float)Rect.bottom/2);
   } else {
      Rect.right  = (long)((float)sizeInner.cx/percent.y);
      Rect.bottom = sizeOutward.cy;

      Rect.left   = (long)((float)sizeOutward.cx/2-(float)Rect.right/2);
      Rect.right  = (long)((float)sizeOutward.cx/2+(float)Rect.right/2);
   }
   return Rect;
}

RECTEX FindInnerRect(const RECT &rectInner, const RECT &rectOutward) {
   return RECTEX(
      FindInnerRect(
         RECTEX(rectInner  ).size(),
         RECTEX(rectOutward).size()
      )
   ).move(rectInner.left, rectInner.top);
}

BOOL PointInRect(const POINT &point, const RECT &rect) {
   return ::PtInRect(&rect, point);
}

BOOL IntersectRect(const RECT &rect1, const RECT &rect2) {
   return ::IntersectRect(&RECTEX(), &rect1, &rect2);
}

bool PointInPolygon(const POINT& point, const POINT* const polygon, int size) {
   POINTFLOAT a = {point.x+0.01f, point.y+0.01f};
   int count = 0;
   for (int i=0; i<size; i++) {
      int j = (i+1)%size;
      if (polygon[i].y == polygon[j].y) continue;
      if (polygon[i].y > a.y && polygon[j].y > a.y) continue;
      if (polygon[i].y < a.y && polygon[j].y < a.y) continue;
      if (max(polygon[i].y, polygon[j].y) == a.y) count++;
      else
         if (min(polygon[i].y, polygon[j].y) == a.y) continue;
         else {
            float t = (float)(a.y-polygon[i].y)/(polygon[j].y-polygon[i].y);
            if (t>0 && t<1 && polygon[i].x+t*(polygon[j].x-polygon[i].x) >= a.x) count++;
         }
   }
   return count & 1;
}

POINT operator*  (LONG  val     , const POINT &a) {return POINTEX(a.x*val, a.y*val);}
POINT operator*  (const POINT &a, LONG  val     ) {return POINTEX(a.x*val, a.y*val);}
POINT operator/  (const POINT &a, LONG  val     ) {return POINTEX(a.x/val, a.y/val);}
POINT operator+  (const POINT &a, const POINT &b) {return POINTEX(a.x+b.x, a.y+b.y);}
POINT operator-  (const POINT &a, const POINT &b) {return POINTEX(a.x-b.x, a.y-b.y);}
bool  operator== (const POINT &a, const POINT &b) {return (a.x==b.x && a.y==b.y);}
bool  operator!= (const POINT &a, const POINT &b) {return (a.x!=b.x || a.y!=b.y);}

SIZE  operator*  (LONG  val     , const SIZE  &a) {return SIZEEX(a.cx*val , a.cy*val );}
SIZE  operator*  (const SIZE  &a, LONG  val     ) {return SIZEEX(a.cx*val , a.cy*val );}
SIZE  operator/  (const SIZE  &a, LONG  val     ) {return SIZEEX(a.cx/val , a.cy/val );}
SIZE  operator+  (const SIZE  &a, const SIZE  &b) {return SIZEEX(a.cx+b.cx, a.cy+b.cy);}
SIZE  operator-  (const SIZE  &a, const SIZE  &b) {return SIZEEX(a.cx-b.cx, a.cy-b.cy);}
bool  operator== (const SIZE  &a, const SIZE  &b) {return (a.cx==b.cx && a.cy==b.cy);}
bool  operator!= (const SIZE  &a, const SIZE  &b) {return (a.cx!=b.cx || a.cy!=b.cy);}

COORD operator*  (SHORT val     , const COORD &a) {return COORDEX(a.X*val, a.Y*val);}
COORD operator*  (const COORD &a, SHORT val     ) {return COORDEX(a.X*val, a.Y*val);}
COORD operator/  (const COORD &a, SHORT val     ) {return COORDEX(a.X/val, a.Y/val);}
COORD operator+  (const COORD &a, const COORD &b) {return COORDEX(a.X+b.X, a.Y+b.Y);}
COORD operator-  (const COORD &a, const COORD &b) {return COORDEX(a.X-b.X, a.Y-b.Y);}
bool  operator== (const COORD &a, const COORD &b) {return (a.X==b.X && a.Y==b.Y);}
bool  operator!= (const COORD &a, const COORD &b) {return (a.X!=b.X || a.Y!=b.Y);}

bool operator== (const RECT &a, const RECT &b) {return (a.left==b.left && a.top==b.top && a.right==b.right && a.bottom==b.bottom);}
bool operator!= (const RECT &a, const RECT &b) {return (a.left!=b.left || a.top!=b.top || a.right!=b.right || a.bottom!=b.bottom);}

BOOL MoveWindow(HWND hWnd, const RECT &rect, BOOL bRepaint) {
   return ::MoveWindow(
      hWnd,
      rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top,
      bRepaint);
}

BOOL SetWindowPos(HWND hWnd, HWND hWndInsertAfter, const RECT &rect, UINT uFlags) {
   return ::SetWindowPos(
      hWnd,
      hWndInsertAfter,
      rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top,
      uFlags);
}

RECTEX ScreenToClient(HWND hWnd, const RECT &rect) {
   return RECTEX(
      ScreenToClient(hWnd, RECTEX(rect).pointLT()),
      ScreenToClient(hWnd, RECTEX(rect).pointRB())
   );
}

RECTEX ClientToScreen(HWND hWnd, const RECT &rect) {
   return RECTEX(
      ClientToScreen(hWnd, RECTEX(rect).pointLT()),
      ClientToScreen(hWnd, RECTEX(rect).pointRB())
   );
}

POINTEX ClientToScreen(HWND hWnd, const POINT &point) {
   POINTEX pointResult = point;
   return ::ClientToScreen(hWnd, &pointResult) ? pointResult : POINTEX();
}

POINTEX ScreenToClient(HWND hWnd, const POINT &point) {
   POINTEX pointResult = point;
   return ::ScreenToClient(hWnd, &pointResult) ? pointResult : POINTEX();
}

RECTEX GetWindowRect(HWND hWnd) {
   RECTEX rect;
   return ::GetWindowRect(hWnd, &rect) ? rect : RECTEX();
}

SIZEEX GetWindowSize(HWND hWnd) {
   return (SIZEEX)GetWindowRect(hWnd);
}

RECTEX GetClientRect(HWND hWnd) {
   RECTEX rect;
   return ::GetClientRect(hWnd, &rect) ? rect : RECTEX();
}

SIZEEX GetClientSize(HWND hWnd) {
   return (SIZEEX)GetClientRect(hWnd);
}

POINTEX GetCursorPos() {
   POINTEX pointCur;
   return ::GetCursorPos(&pointCur) ? pointCur : POINTEX();
}

CString GetClassName(HWND hWnd) {
   CString strClassName;
   int iSize = 16;
   int iRes = 0;
   bool bAgain = true;
   do {
      TCHAR *szClassName = new TCHAR[iSize = iSize<<1]; szClassName[0] = 0;
      iRes = ::GetClassName(hWnd, szClassName, iSize);
      if ((iRes == 0) || ((iSize-1) > iRes))
      {
         if (iRes != 0) {
            strClassName = szClassName;
         }
         bAgain = false;
      }
      delete [] szClassName;
   } while (bAgain);
   return strClassName;
}

WNDPROC GetWindowProc(HWND hWnd) {
   return (WNDPROC)::GetWindowLong(hWnd, GWL_WNDPROC);
}

//DWORD GetWindowStyle(HWND hWnd) {
//   return (DWORD)::GetWindowLong(hWnd, GWL_STYLE);
//}

DWORD GetWindowStyleEx(HWND hWnd) {
   return GetWindowExStyle(hWnd);
 //return ::GetWindowLong(hWnd, GWL_EXSTYLE);
}

LONG GetWindowUserData(HWND hWnd) {
   return ::GetWindowLong(hWnd, GWL_USERDATA);
}

WNDPROC SetWindowProc(HWND hWnd, WNDPROC pWndProc) {
   return SubclassWindow(hWnd, pWndProc);
 //return (WNDPROC)::SetWindowLong(hWnd, GWL_WNDPROC, (LONG)pWndProc);
}

DWORD SetWindowStyle(HWND hWnd, DWORD lStyle) {
   return (DWORD)::SetWindowLong(hWnd, GWL_STYLE, (LONG)lStyle);
}

DWORD SetWindowStyleEx(HWND hWnd, DWORD lStyleEx) {
   return (DWORD)::SetWindowLong(hWnd, GWL_EXSTYLE, (LONG)lStyleEx);
}

LONG SetWindowUserData(HWND hWnd, LONG lUserData) {
   return ::SetWindowLong(hWnd, GWL_USERDATA, lUserData);
}

BOOL SetMenuText(HMENU hMenu, UINT uItem, BOOL bByPosition, LPCTSTR szNewText) {
   MENUITEMINFO miiGet = {sizeof(MENUITEMINFO), MIIM_TYPE};
   ::GetMenuItemInfo(hMenu, uItem, bByPosition, &miiGet);

   MENUITEMINFO mii = {sizeof(MENUITEMINFO), MIIM_TYPE, MFT_STRING | ((miiGet.fType & MFT_RADIOCHECK) ? MFT_RADIOCHECK : 0)};
   mii.dwTypeData = (LPTSTR)szNewText;
   return ::SetMenuItemInfo(hMenu, uItem, bByPosition, &mii);
}

SIZEEX GetScreenSize() {
   return SIZEEX(
      ::GetSystemMetrics(SM_CXSCREEN),
      ::GetSystemMetrics(SM_CYSCREEN)
   );
}

// возвращает только путь к файлу без имени файла (путь с наклонной чертой в конце)
CString GetFileDir(LPCTSTR szPathAlongWithFileName) {
   CString strResult(szPathAlongWithFileName);
   int iFind = strResult.ReverseFind(TEXT('\\'));
   if (iFind != -1) {
      strResult = strResult.Left(iFind+1);
   }
   return strResult;
}

// возвращает только имя файла без пути
CString GetFileName(LPCTSTR szPathAlongWithFileName) {
   CString strResult(szPathAlongWithFileName);
   int iFind = strResult.ReverseFind(TEXT('\\'));
   if (iFind != -1) {
      strResult = strResult.Right(strResult.GetLength()-(iFind+1));
   }
   return strResult;
}

BOOL PathExist(LPCTSTR szPath)
{
   //BOOL PathIsDirectory( LPCTSTR pszPath );
   //Header shlwapi.h
   //Import library shlwapi.lib

   DWORD dwAttr = GetFileAttributes(szPath);
   return ((dwAttr != 0xFFFFFFFF) && (dwAttr & FILE_ATTRIBUTE_DIRECTORY));
}

BOOL FileExist(LPCTSTR szPath)
{
   DWORD dwAttr = GetFileAttributes(szPath);
   return ((dwAttr != 0xFFFFFFFF) && !(dwAttr & FILE_ATTRIBUTE_DIRECTORY));
}

CString GetMenuString(HMENU hMenu, UINT uIDItem, UINT uFlag) {
   TCHAR szName[MAX_PATH] = {0};
   ::GetMenuString(hMenu, uIDItem, szName, MAX_PATH, uFlag);
   return CString(szName);
}

// retrieves the full path and filename for the executable file containing the specified module
CString GetModuleFileName(HMODULE hModule) {
   TCHAR szPath[MAX_PATH] = {0};
   ::GetModuleFileName(hModule, szPath, MAX_PATH);
   return CString(szPath);
}

// возвращает путь к модулю без имени модуля (с наклонной чертой в конце)
CString GetModuleDir(HMODULE hModule) {
   return GetFileDir(GetModuleFileName(hModule));
}

CString GetComputerName() {
   DWORD dwSize = MAX_COMPUTERNAME_LENGTH + 1;
   TCHAR szCompName[MAX_COMPUTERNAME_LENGTH + 1] = {0};
   BOOL bRes = ::GetComputerName(szCompName, &dwSize);
   return CString(szCompName);
}

CString GetUserName() {
   size_t size = 16;
   CString strUserName;
   bool bAgain = true;
   do {
      TCHAR *szUName = new TCHAR [size = size<<1]; if (!szUName) break; szUName[0] = 0; 
      DWORD dwSize = size;
      if (::GetUserName(szUName, &dwSize) && (dwSize < size))
      {
         strUserName = szUName;
         bAgain = false;
      }
      delete [] szUName;
   } while(bAgain);
   return strUserName;
}

CString GetSystemDirectory() {
   TCHAR szDir[MAX_PATH] = {0};
   ::GetSystemDirectory(szDir, MAX_PATH);
   return CString(szDir);
}

CString GetWindowsDirectory() {
   TCHAR szDir[MAX_PATH] = {0};
   ::GetWindowsDirectory(szDir, MAX_PATH);
   return CString(szDir);
}

CString GetTempPath() {
   TCHAR szDir[MAX_PATH] = {0};
   ::GetTempPath(MAX_PATH, szDir);
   return CString(szDir);
}

CString GetWindowText(HWND hWnd) {
   CString strCaption;
   int iBuff = 32;
   TCHAR *szCaption = NULL;
   int iRes = 0;
   DWORD dwErrorCode = ERROR_SUCCESS;
   bool bBreak;
   do {
      szCaption = new TCHAR[iBuff = iBuff<<1];
      if (szCaption != NULL) {
         ::SetLastError(ERROR_SUCCESS);
         iRes = ::GetWindowText(hWnd, szCaption, iBuff);
         dwErrorCode = ::GetLastError();
         bBreak = (iRes==0) || (iRes < (iBuff-1));
         if (bBreak) {
            strCaption = szCaption;
         }
         delete [] szCaption;
         szCaption = NULL;
      } else {
         dwErrorCode = ERROR_OUTOFMEMORY;
      }
   } while(!bBreak);
   ::SetLastError(dwErrorCode);
   return strCaption;
}

SIZEEX SizeBitmap(HBITMAP hBmp) {
   SIZEEX resultSize;
   BITMAP bmp = {0,0,0,0,0,0,NULL};
   int iRes = ::GetObject(hBmp, sizeof(BITMAP), &bmp);
   if (iRes == sizeof(BITMAP)) {
      resultSize.cx = bmp.bmWidth;
      resultSize.cy = bmp.bmHeight;
   }
   return resultSize;
}

SIZEEX SizeBitmap(HDC hCDC) {                 
   static HBITMAP hBmpNil = ::CreateDiscardableBitmap(hCDC, 1, 1);
   HBITMAP hBmp = (HBITMAP)::SelectObject(hCDC, hBmpNil);
   SIZEEX resultSize(SizeBitmap(hBmp));
   ::SelectObject(hCDC, hBmp);
   //::DeleteObject(hBmpNil);
   return resultSize;
}

SIZEEX SizeBitmap(HWND hWnd, BOOL bClientRect) {
   return bClientRect ? ::GetClientSize(hWnd) : ::GetWindowSize(hWnd);
}

HBITMAP CreateBitmap(int iWidth, int iHeight) {
   HWND hWnd = ::GetDesktopWindow();
   HDC hDC = ::GetDC(hWnd);
   HBITMAP hBmp = ::CreateCompatibleBitmap(hDC, iWidth, iHeight);
   ::ReleaseDC(hWnd, hDC);
   return hBmp;
}

HBITMAP CreateBitmap(const SIZE &size) {
   return ::CreateBitmap(size.cx, size.cy);
}

HBITMAP CreateMask(const SIZE &sizeBmp) {
   return ::CreateBitmap(sizeBmp.cx, sizeBmp.cy, 1, 1, NULL);
}

HBITMAP CreateMask(HBITMAP hBmp, COLORREF transparentColor) {
   BITMAP bmp; memset(&bmp, 0, sizeof(BITMAP));
   ::GetObject(hBmp, sizeof(BITMAP), &bmp);
   HBITMAP hBmpMask = ::CreateMask(SIZEEX(bmp.bmWidth, bmp.bmHeight));

   HDC hDC_Dst = ::CreateCompatibleDC(NULL);
   HDC hDC_Src = ::CreateCompatibleDC(NULL);
   HBITMAP hBmpSaveSrc = (HBITMAP)::SelectObject(hDC_Src, hBmp);
   HBITMAP hBmpSaveDst = (HBITMAP)::SelectObject(hDC_Dst, hBmpMask);

   COLORREF oldBkColor = ::SetBkColor(hDC_Src, (transparentColor == CLR_INVALID) ? GetPixel(hDC_Src, 0, 0) : transparentColor);
   ::BitBlt(hDC_Dst, 0,0, bmp.bmWidth, bmp.bmHeight, hDC_Src, 0, 0, NOTSRCCOPY);
   ::SetBkColor(hDC_Src, oldBkColor);

   ::SelectObject(hDC_Dst, hBmpSaveDst);
   ::SelectObject(hDC_Src, hBmpSaveSrc);
   ::DeleteDC(hDC_Dst);
   ::DeleteDC(hDC_Src);

   return hBmpMask;
}

// генерирует случайное число от 0 до maxDiapason, ВКЛЮЧАЯ верхнюю границу
int rand(int maxDiapason) {
   if (maxDiapason < 1) return 0;
   static bool bInitRandom = (srand((unsigned)time(NULL)), true);
   return rand()%(maxDiapason+1);
   return int(float(maxDiapason+1)*rand()/(RAND_MAX+1));
}

CString MemCopyAsString(LPCVOID pBuf, size_t size, TCHAR chSeparatorEOL) {
   CString strData;
   if (pBuf && size) {
      size_t newSize = size+sizeof(TCHAR); // + EOL
      if (sizeof(TCHAR) == sizeof(WCHAR)) {
         if (newSize&1) {
            newSize += 1; // округляю до чётного числа битов
         }
      }
      TCHAR *szData = (TCHAR*) new BYTE[newSize];
      size_t sizeLen = newSize/sizeof(TCHAR)-1;
      if (sizeLen>0) szData[sizeLen-1] = TEXT('\0'); // нужно, если было округление
      szData[sizeLen] = TEXT('\0'); // EOL
      memcpy(szData, pBuf, size);
      if (chSeparatorEOL) {
         for (size_t i=0; i<sizeLen; i++) {
            TCHAR *pCh = &szData[i];
            if (!*pCh) {
               *pCh = chSeparatorEOL;
            }
         }
      }
      strData = szData;
      delete [] (BYTE*)szData;
   }
   return strData;
}

void BeepSpeaker(DWORD dwFreq, DWORD dwDuration)
{
   dwFreq = min(max(0x25, dwFreq), 0x7FFF);
   static OSVERSIONINFO OsVersion = {
      sizeof(OSVERSIONINFO), // DWORD dwOSVersionInfoSize;
      0,                     // DWORD dwMajorVersion;
      0,                     // DWORD dwMinorVersion;
      0,                     // DWORD dwBuildNumber;
      0,                     // DWORD dwPlatformId;
      {TEXT('\0'),}          // TCHAR szCSDVersion[128];
   };
   static BOOL bRes = ::GetVersionEx(&OsVersion);

   /* We need to check if we're running on NT, because NT
   does not allow the "out" assembler command for security
   reasons and the Beep API function works via the speaker on NT,
   so that's ok.
   On Windows 9x Beep will sound the default system
   beep which is in most cases through the soundblaster.
   (frequency is not supported under Windows 9x) */

   if (OsVersion.dwPlatformId == VER_PLATFORM_WIN32_NT) {
      ::Beep(dwFreq, dwDuration);
   } else { //VER_PLATFORM_WIN32_WINDOWS
      __asm
      {
         mov al, -1
         out 61h, al
      }
      Sleep(dwDuration);
      __asm
      {
         mov al, 0
         out 61h, al
      }
   }
}

CString Format(LPCTSTR szFormat, ...) {
   va_list arglist;
   va_start(arglist, szFormat);
   CString strResult;
   strResult.FormatV(szFormat, arglist);
   va_end(arglist);
   return strResult;
}

OSVERSIONINFO GetVersionEx() {
   OSVERSIONINFO vi = {sizeof(OSVERSIONINFO), 0,0,0,0,{0}};
   BOOL bRes = ::GetVersionEx(&vi);
   if (!bRes) memset(&vi,0,sizeof(OSVERSIONINFO));
   return vi;
}

OSVERSIONINFO COSVersion::m_vi = GetVersionEx();
bool COSVersion::IsWinXP() {
   static bool bRes = ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && (m_vi.dwMajorVersion == 5) && (m_vi.dwMinorVersion == 1));
   return bRes;
}
bool COSVersion::IsWin2000() {
   static bool bRes = ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && (m_vi.dwMajorVersion == 5) && (m_vi.dwMinorVersion == 0));
   return bRes;
}
bool COSVersion::IsWinNT() {
   static bool bRes = ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && ((m_vi.dwMajorVersion == 4) || (m_vi.dwMajorVersion == 3))/* && (m_vi.dwMinorVersion == 0)*/);
   return bRes;
}
bool COSVersion::IsWin9598Me() {
   static bool bRes = ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_WINDOWS) && (m_vi.dwMajorVersion == 4)/* && (m_vi.dwMinorVersion == 0)*/);
   return bRes;
}

// Проверь свои знания C++
void TestCpp(
   int       *      pK0, //             указатель на переменную
   int       *const pK1, // константный указатель на переменную
   int const *      pK2, //             указатель на константу
   int const *const pK3, // константный указатель на константу

   int       *      *      ppL0, //             указатель на             указатель на переменную
   int       *      *const ppL1, // константный указатель на             указатель на переменную
   int       *const *      ppL2, //             указатель на константный указатель на переменную
   int       *const *const ppL3, // константный указатель на константный указатель на переменную
   int const *      *      ppL4, //             указатель на             указатель на константу
   int const *      *const ppL5, // константный указатель на             указатель на константу
   int const *const *      ppL6, //             указатель на константный указатель на константу
   int const *const *const ppL7  // константный указатель на константный указатель на константу
)
{
   pK0 = new int; // Ok
  *pK0 = 1;       // Ok

// pK1 = new int; // Error
  *pK1 = 1;       // Ok

   pK2 = new int; // Ok
//*pK2 = 1;       // Error

// pK3 = new int; // Error
//*pK3 = 1;       // Error

    ppL0 = new int*; // Ok
   *ppL0 = new int;  // Ok
  **ppL0 = 1;        // Ok

//  ppL1 = new int*; // Error
   *ppL1 = new int;  // Ok
  **ppL1 = 1;        // Ok

    ppL2 = new int*; // Ok
// *ppL2 = new int;  // Error
  **ppL2 = 1;        // Ok

//  ppL3 = new int*; // Error
// *ppL3 = new int;  // Error
  **ppL3 = 1;        // Ok

    ppL4 = (const int **)new int*; // Ok
   *ppL4 = new int;                // Ok
//**ppL4 = 1;                      // Error

//  ppL5 = (const int **)new int*; // Error
   *ppL5 = new int;                // Ok
//**ppL5 = 1;                      // Error

    ppL6 = (const int **)new int*; // Ok
// *ppL6 = new int;                // Error
//**ppL6 = 1;                      // Error

//  ppL7 = (const int **)new int*; // Error
// *ppL7 = new int;                // Error
//**ppL7 = 1;                      // Error
}
