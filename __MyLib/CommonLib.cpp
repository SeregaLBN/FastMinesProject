////////////////////////////////////////////////////////////////////////////////
// File name: CommonLib.cpp
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 2005 08 31
//
// Description: Функции общего назначения
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <time.h>
#include "CommonLib.h"

#include <WinSock.h>
#pragma comment(lib, "ws2_32")

#include <CommDlg.h>
#pragma comment(lib, "ComDlg32")

#include <CDErr.h> // Common dialog error return codes

#if (_WIN32_WINNT >= 0x0400 ) || defined(_WIN32_DCOM) // DCOM
   #include <ObjBase.h> // CoInitializeEx
   #pragma comment(lib, "ole32")
#endif // DCOM

#include <WinVer.h>
#pragma comment(lib, "Version")

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
   ).move(rectOutward.left, rectOutward.top);
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

void ShowCursor() {
   while (::ShowCursor(TRUE)<0) {}
}

void HideCursor() {
   while (::ShowCursor(FALSE)>=0) {}
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

CString SelectFile(BOOL bOpenDialog, HWND hWndOwner, LPCTSTR szDefSelectedFile, LPCTSTR szDefExt, LPCTSTR szFilter, LPCTSTR szTitle, LPCTSTR szInitialDir, DWORD dwFlags) {
   CString strFileName;
   DWORD nMaxFile = max(MAX_PATH, (szDefSelectedFile ? lstrlen(szDefSelectedFile)+1 : 0));
anew1:
   TCHAR *szFile = new TCHAR [nMaxFile];
anew2:
   if (szFile == NULL) {
      ::SetLastError(ERROR_OUTOFMEMORY);
   } else {
      szFile[0] = 0;
      if (szDefSelectedFile) {
         lstrcpyn(szFile, szDefSelectedFile, min(nMaxFile, (DWORD)lstrlen(szDefSelectedFile)+1));
      }
      OPENFILENAME OpenFileName = {
         sizeof(OPENFILENAME)    , // DWORD         lStructSize; 
         hWndOwner               , // HWND          hwndOwner; 
         NULL                    , // HINSTANCE     hInstance; 
         szFilter                , // LPCTSTR       lpstrFilter; 
         NULL                    , // LPTSTR        lpstrCustomFilter; 
         0                       , // DWORD         nMaxCustFilter; 
         0                       , // DWORD         nFilterIndex; 
         szFile                  , // LPTSTR        lpstrFile; 
         nMaxFile                , // DWORD         nMaxFile; 
         NULL                    , // LPTSTR        lpstrFileTitle; 
         0                       , // DWORD         nMaxFileTitle; 
         szInitialDir            , // LPCTSTR       lpstrInitialDir; 
         szTitle                 , // LPCTSTR       lpstrTitle; 
         dwFlags                 , // DWORD         Flags; 
         0                       , // WORD          nFileOffset; 
         0                       , // WORD          nFileExtension; 
         szDefExt                , // LPCTSTR       lpstrDefExt; 
         0                       , // LPARAM        lCustData; 
         NULL                    , // LPOFNHOOKPROC lpfnHook; 
         NULL                      // LPCTSTR       lpTemplateName; 
      };
      if (bOpenDialog ? ::GetOpenFileName(&OpenFileName) : ::GetSaveFileName(&OpenFileName)) {
         strFileName = szFile;
      } else {
         DWORD dwError = ::CommDlgExtendedError();
         switch (dwError) {
         case FNERR_BUFFERTOOSMALL:
            {
               delete [] szFile; szFile = NULL;
               nMaxFile = nMaxFile << 4;
               goto anew1;
            } break;
         case FNERR_INVALIDFILENAME:
            {
               szDefSelectedFile = NULL;
               goto anew2;
            } break;
         }
      }
      delete [] szFile; szFile = NULL;
   }
   return strFileName;
}

CString SelectFolder(
   HWND hWndOwner,   // родительское окно для диалога
   LPCTSTR szTitle,
   DWORD dwFlags,
   int nFolder)      // если -1 - то вызов стандартного диалога выбора папки
                     // иначе CSIDL_... (специальная папка, see SHGetSpecialFolderLocation) -возвращает путь к спецпапке
{
   CString strFolder;
   IMalloc *pMalloc = NULL;
#if (_WIN32_WINNT >= 0x0400 ) || defined(_WIN32_DCOM) // DCOM
   HRESULT hRes = ::CoInitializeEx(NULL, COINIT_APARTMENTTHREADED);
#endif // DCOM
   if (::SHGetMalloc(&pMalloc) == NOERROR) {
      ITEMIDLIST *pIIdL = NULL;
      TCHAR szFolder[MAX_PATH] = {0};
      if (nFolder == -1) {
         BROWSEINFO bi = {
            hWndOwner      , // HWND            hwndOwner;
            NULL           , // LPCITEMIDLIST   pidlRoot;
            szFolder       , // LPTSTR          pszDisplayName;
            szTitle        , // LPCTSTR         lpszTitle;
            dwFlags        , // UINT            ulFlags;
            NULL           , // BFFCALLBACK     lpfn;
            0              , // LPARAM          lParam;
            0                // int             iImage;
         };
         ITEMIDLIST *pIIdL = ::SHBrowseForFolder(&bi);
         if (pIIdL != NULL) {
            strFolder = szFolder; // только имя папки
            if (::SHGetPathFromIDList(pIIdL, szFolder)) {
               strFolder = szFolder; // полный путь
            }
         }
      } else {
         if (::SHGetSpecialFolderLocation(hWndOwner, nFolder, &pIIdL) == NOERROR) {
            if (::SHGetPathFromIDList(pIIdL, szFolder)) {
               strFolder = szFolder; // полный путь
            }
         }
      }
      if (pIIdL) {
         pMalloc->Free(pIIdL); pIIdL = NULL;
      }
      pMalloc->Release(); pMalloc = NULL;
   }
   return strFolder;
}

// размер файла
DWORD GetFileSize(LPCTSTR szFileName) {
   DWORD dwRes = INVALID_FILE_SIZE;
   if (FileExist(szFileName)) {
      HANDLE hFile = ::CreateFile(
         szFileName,
         GENERIC_READ,
         FILE_SHARE_READ,
         NULL,
         OPEN_EXISTING,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      if (hFile != INVALID_HANDLE_VALUE) {
         dwRes = ::GetFileSize(hFile, NULL);
         ::CloseHandle(hFile); hFile = NULL;
      }
   }
   return dwRes;
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

CString GetFileVersion(LPCTSTR szFilename) {
   CString strRes;
   DWORD dwErrCode = ERROR_SUCCESS;
   DWORD dwSize = ::GetFileVersionInfoSize(szFilename, 0);
   if (dwSize == 0) {
      dwErrCode = ::GetLastError();
   } else {
      BYTE *pData = new BYTE [dwSize];
      if (pData == NULL) {
         dwErrCode = ERROR_OUTOFMEMORY;
      } else {
         BOOL bRes = ::GetFileVersionInfo(szFilename, 0, dwSize, pData);
         if (!bRes ) {
            dwErrCode = ::GetLastError();
         } else {
            VS_FIXEDFILEINFO *pFixeFileInfo = NULL;
            UINT uLen = 0;
            bRes = ::VerQueryValue(pData, _T("\\"), (void**)&pFixeFileInfo, &uLen);
            if (!bRes) {
               dwErrCode = ::GetLastError();
            } else {
               int v1 = HIWORD(pFixeFileInfo->dwFileVersionMS);
               int v2 = LOWORD(pFixeFileInfo->dwFileVersionMS);
               int v3 = HIWORD(pFixeFileInfo->dwFileVersionLS);
               int v4 = LOWORD(pFixeFileInfo->dwFileVersionLS);
               strRes.Format(_T("%i.%i.%i.%i"), v1,v2,v3,v4);
            }
         }
         delete [] pData; pData = NULL;
      }
   }
   ::SetLastError(dwErrCode);
   return strRes;
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

CString GetCurrentDirectory() {
   CString strRes;
   DWORD dwErrCode = ERROR_SUCCESS;
   DWORD dwSize = MAX_PATH;
   DWORD dwRes = 0;
   do {
      TCHAR *szDir = new TCHAR [dwSize];
      if (szDir == NULL) {
         dwErrCode = ERROR_OUTOFMEMORY;
      } else {
         szDir[0] = 0;
         dwRes = ::GetCurrentDirectory(dwSize, szDir);
         if (dwRes == 0) {
            dwErrCode = ::GetLastError();
         } else {
            if (dwRes < dwSize) {
               strRes = szDir;
               dwRes = 0;
            } else {
               dwSize = dwRes;
            }
         }
         delete [] szDir; szDir = NULL;
      }
   } while(dwRes != 0);
   ::SetLastError(dwErrCode);
   return strRes;
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
   // Существует ли функция, возвращающая сабж. Как GetTempPath для временных файлов?
   // char sz_temp[MAX_PATH] = {0};
   // SHGetFolderPath( 0, CSIDL_INTERNET_CACHE, 0, 0, sz_temp );
   TCHAR szDir[MAX_PATH] = {0};
   ::GetTempPath(MAX_PATH, szDir);
   return CString(szDir);
}

CString GetWindowText(HWND hWnd) {
   CString strCaption;
   int iBuff = 32;
   TCHAR *szCaption = NULL;
   int iRes = 0;
   DWORD dwErrCode = ERROR_SUCCESS;
   bool bBreak;
   do {
      szCaption = new TCHAR[iBuff = iBuff<<1];
      if (szCaption != NULL) {
         ::SetLastError(ERROR_SUCCESS);
         iRes = ::GetWindowText(hWnd, szCaption, iBuff);
         dwErrCode = ::GetLastError();
         bBreak = (iRes==0) || (iRes < (iBuff-1));
         if (bBreak) {
            strCaption = szCaption;
         }
         delete [] szCaption;
         szCaption = NULL;
      } else {
         dwErrCode = ERROR_OUTOFMEMORY;
      }
   } while(!bBreak);
   ::SetLastError(dwErrCode);
   return strCaption;
}

SIZEEX SizeIcon(HICON hIcon) {
   SIZEEX resSize;
   ICONINFO iconInfo;
   if (::GetIconInfo(hIcon, &iconInfo)) {
      resSize.cx = iconInfo.xHotspot<<1;
      resSize.cy = iconInfo.yHotspot<<1;
   }
   return resSize;
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

CString MemCopyAsString(LPCVOID pData, size_t iSize, TCHAR chSeparatorEOL) {
   CString strData;
   if (pData && iSize) {
      size_t newSize = iSize+sizeof(TCHAR); // + EOL
      if (sizeof(TCHAR) == sizeof(WCHAR)) {
         if (newSize&1) {
            newSize += 1; // округляю до чётного числа битов
         }
      }
      TCHAR *szData = (TCHAR*) new BYTE[newSize];
      size_t sizeLen = newSize/sizeof(TCHAR)-1;
      if (sizeLen>0) szData[sizeLen-1] = TEXT('\0'); // нужно, если было округление
      szData[sizeLen] = TEXT('\0'); // EOL
      memcpy(szData, pData, iSize);
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


CString MemCopyAsHex(LPCVOID pData, size_t iSize, BOOL bUsePrefix, LPCTSTR szSeparator)
{
   CString strRes;
   for (size_t i=0; i<iSize; i++) {
      if (szSeparator && szSeparator[0]) {
         strRes += szSeparator;
      }
      strRes += ::Format(bUsePrefix ? TEXT("0x%02X") : TEXT("%02X"), int(((BYTE*)pData)[i]));
   }
   if (!strRes.IsEmpty() && szSeparator && szSeparator[0]) {
      strRes = strRes.Mid(lstrlen(szSeparator));
   }
   return strRes;
}


#ifndef _UNICODE
CString AnsiToOem(LPCTSTR szStr) {
   CString str(szStr);
   str.AnsiToOem();
   return str;
}

CString OemToAnsi(LPCTSTR szStr) {
   CString str(szStr);
   str.OemToAnsi();
   return str;
}
#endif // _UNICODE

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

SYSTEMTIME GetSystemTime() {
   SYSTEMTIME SysTime = {0,0,0,0,0,0,0,0};
   ::GetSystemTime(&SysTime);
   return SysTime;
}

FILETIME GetSystemTimeAsFileTime() {
   FILETIME FileTime = {0,0};
   ::GetSystemTimeAsFileTime(&FileTime);
   return FileTime;
}

SYSTEMTIME FileTimeToSystemTime(const FILETIME &FileTime) {
   SYSTEMTIME SysTime = {0,0,0,0,0,0,0,0};
   BOOL bRes = ::FileTimeToSystemTime(&FileTime, &SysTime);
   return SysTime;
}

FILETIME SystemTimeToFileTime(const SYSTEMTIME &SysTime) {
   FILETIME FileTime = {0,0};
   BOOL bRes = ::SystemTimeToFileTime(&SysTime, &FileTime);
   return FileTime;
}

BOOL BitBlt(HDC hDCDst, const RECT &rectDst, HDC hDCSrc, const SIZE &sizeSrc, DWORD dwROp) {
   return ::BitBlt(hDCDst, rectDst.left, rectDst.top, rectDst.right-rectDst.left, rectDst.bottom-rectDst.top, hDCSrc, sizeSrc.cx, sizeSrc.cy, dwROp);
}

BOOL BitBlt(HDC hDCDst, const RECT &rectDst, HDC hDCSrc, DWORD dwROp) {
   return ::BitBlt(hDCDst, rectDst.left, rectDst.top, rectDst.right-rectDst.left, rectDst.bottom-rectDst.top, hDCSrc, 0, 0, dwROp);
}

BOOL BitBlt(HDC hDCDst, const SIZE &sizeDst, HDC hDCSrc, const SIZE &sizeSrc, DWORD dwROp) {
   return ::BitBlt(hDCDst, 0, 0, sizeDst.cx, sizeDst.cy, hDCSrc, sizeSrc.cx, sizeSrc.cy, dwROp);
}

BOOL BitBlt(HDC hDCDst, const SIZE &sizeDst, HDC hDCSrc, DWORD dwROp) {
   return ::BitBlt(hDCDst, 0, 0, sizeDst.cx, sizeDst.cy, hDCSrc, 0, 0, dwROp);
}

BOOL StretchBlt(HDC hDCDst, const RECT &rectDst, HDC hDCSrc, const RECT &rectSrc, DWORD dwROp) {
   return ::StretchBlt(hDCDst, rectDst.left, rectDst.top, rectDst.right-rectDst.left, rectDst.bottom-rectDst.top, hDCSrc, rectSrc.left, rectSrc.top, rectSrc.right-rectSrc.left, rectSrc.bottom-rectSrc.top, dwROp);
}

BOOL StretchBlt(HDC hDCDst, const RECT &rectDst, HDC hDCSrc, const SIZE &sizeSrc, DWORD dwROp) {
   return ::StretchBlt(hDCDst, rectDst.left, rectDst.top, rectDst.right-rectDst.left, rectDst.bottom-rectDst.top, hDCSrc, 0, 0, sizeSrc.cx, sizeSrc.cy, dwROp);
}

BOOL StretchBlt(HDC hDCDst, const SIZE &sizeDst, HDC hDCSrc, const SIZE &sizeSrc, DWORD dwROp) {
   return ::StretchBlt(hDCDst, 0, 0, sizeDst.cx, sizeDst.cy, hDCSrc, 0, 0, sizeSrc.cx, sizeSrc.cy, dwROp);
}

// Прочитать из реестра значение заданного параметра
/* static */ CString CRegistry::GetString(IN HKEY hKey, IN LPCTSTR szParameter, OUT PDWORD pdwErrCode) {
   CString strResult;
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || !szParameter) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      int i=0;
      while (TRUE) {
         TCHAR szValueName[1024] = {0};
         DWORD dwSizeValueName=1024,
               dwType,
               dwSizeData=1024;
         BYTE pbData[1024] = {0};

         dwErrCode = ::RegEnumValue(hKey, i++, szValueName, &dwSizeValueName, NULL, &dwType, pbData, &dwSizeData);

         if (dwErrCode == ERROR_SUCCESS) {
            if ((dwType == REG_SZ) && (lstrcmpi(szParameter, szValueName) == 0)) {
               strResult = (LPCTSTR)pbData;
               break;
            }
         } else {
            break;
         }
      }
   }
   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

// Прочитать из реестра значение заданного параметра
/* static */ DWORD CRegistry::GetDWord(IN HKEY hKey, IN LPCTSTR szParameter, OUT PDWORD pdwErrCode) {
   DWORD dwResult = 0;
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || !szParameter) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      int i=0;
      while (TRUE) {
         TCHAR szValueName[1024] = {0};
         DWORD dwSizeValueName=1024,
               dwType,
               dwSizeData=1024;
         BYTE pbData[1024] = {0};

         dwErrCode = ::RegEnumValue(hKey, i++, szValueName, &dwSizeValueName, NULL, &dwType, pbData, &dwSizeData);

         if (dwErrCode == ERROR_SUCCESS) {
            if ((dwType == REG_DWORD) && (lstrcmpi(szParameter, szValueName) == 0)) {
               dwResult = *((int*)pbData);
               break;
            }
         } else {
            break;
         }
      }
   }
   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return dwResult;
}

// Записать в реестр  значение заданного параметра
/* static */ DWORD CRegistry::SetString(IN HKEY hKey, IN LPCTSTR szParameter, IN LPCTSTR szValue) {
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || !szParameter) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      dwErrCode = ::RegSetValueEx(hKey, szParameter, 0, REG_SZ, (LPBYTE)szValue, (lstrlen(szValue)+1)<<(sizeof(TCHAR)-1));
   }
   return dwErrCode;
}

// Записать в реестр  значение заданного параметра
/* static */ DWORD CRegistry::SetDWord(IN HKEY hKey, IN LPCTSTR szParameter, IN DWORD dwValue) {
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || !szParameter) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      dwErrCode = ::RegSetValueEx(hKey, szParameter, 0, REG_DWORD, (LPBYTE)&dwValue, sizeof(DWORD));                                       
   }
   return dwErrCode;
}

/* static */ CString CRegistry::GetString(IN LPCTSTR szKeyName, OUT PDWORD pdwErrCode) {
   DWORD dwErrCode = ERROR_SUCCESS;
   CString strResult;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));

   HKEY hKey = NULL;

   do {
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_PATH_NOT_FOUND;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegOpenKeyEx(hKey, strKey, 0, KEY_READ, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         strResult = CRegistry::GetString(hKey, strKeyName, pdwErrCode);
         if (pdwErrCode) {
            dwErrCode = *pdwErrCode;
         } else {
            dwErrCode = ::GetLastError();
         }
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);

   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

/* static */ DWORD CRegistry::GetDWord(IN LPCTSTR szKeyName, OUT PDWORD pdwErrCode) {
   DWORD dwErrCode = ERROR_SUCCESS;
   DWORD dwResult = 0;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));

   HKEY hKey = NULL;

   do {
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_PATH_NOT_FOUND;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegOpenKeyEx(hKey, strKey, 0, KEY_READ, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         dwResult = CRegistry::GetDWord(hKey, strKeyName, pdwErrCode);
         if (pdwErrCode) {
            dwErrCode = *pdwErrCode;
         } else {
            dwErrCode = ::GetLastError();
         }
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);

   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return dwResult;
}

/* static */ DWORD CRegistry::SetString(IN LPCTSTR szKeyName, IN LPCTSTR szValue) {
   DWORD dwErrCode = ERROR_SUCCESS;
   CString strResult;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));

   HKEY hKey = NULL;   

   do { 
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_BAD_PATHNAME;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegCreateKey(hKey, strKey, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         dwErrCode = CRegistry::SetString(hKey, strKeyName, szValue);
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);
   return dwErrCode;
}

/* static */ DWORD CRegistry::SetDWord(IN LPCTSTR szKeyName, IN DWORD dwValue) {
   DWORD dwErrCode = ERROR_SUCCESS;
   CString strResult;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));

   HKEY hKey = NULL;   

   do { 
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_BAD_PATHNAME;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegCreateKey(hKey, strKey, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         dwErrCode = CRegistry::SetDWord(hKey, strKeyName, dwValue);
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);
   return dwErrCode;
}

// find methods
/* static */ CString CRegistry::GetSubKeyName(IN HKEY hKey, IN int iIndex, OUT PDWORD pdwErrCode) {
   CString strResult;
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || (iIndex<0)) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      TCHAR szKeyName[1024] = {0};
      DWORD dwSizeKeyName=1024;
      PFILETIME pFileTime = NULL;

      dwErrCode = ::RegEnumKeyEx(hKey, iIndex, szKeyName, &dwSizeKeyName, NULL, NULL, NULL, pFileTime);

      if (dwErrCode == ERROR_SUCCESS) {
         strResult = szKeyName;
      }
   }

   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

// find methods
/* static */ CString CRegistry::GetSubKeyName(IN LPCTSTR szKeyName, IN int iIndex, OUT PDWORD pdwErrCode) {
   DWORD dwErrCode = ERROR_SUCCESS;
   CString strResult;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));
   strKeyName += TEXT('\\');

   HKEY hKey = NULL;

   do {
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_PATH_NOT_FOUND;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegOpenKeyEx(hKey, strKey, 0, KEY_READ | KEY_ENUMERATE_SUB_KEYS, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         strResult = CRegistry::GetSubKeyName(hKey, iIndex, pdwErrCode);
         if (pdwErrCode) {
            dwErrCode = *pdwErrCode;
         } else {
            dwErrCode = ::GetLastError();
         }
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);

   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

/* static */ CString CRegistry::GetValueName(IN HKEY hKey, IN int iIndex, OUT DWORD &dwType, OUT PDWORD pdwErrCode) {
   CString strResult;
   DWORD dwErrCode = ERROR_SUCCESS;
   if (!hKey || (iIndex<0)) {
      dwErrCode = ERROR_INVALID_PARAMETER;
   } else {
      TCHAR szValueName[1024] = {0};
      DWORD dwSizeValueName=1024,
            dwSizeData=1024;
      BYTE pbData[1024] = {0};

      dwErrCode = ::RegEnumValue(hKey, iIndex, szValueName, &dwSizeValueName, NULL, &dwType, pbData, &dwSizeData);

      if (dwErrCode == ERROR_SUCCESS) {
         strResult = szValueName;
      }
   }
   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

/* static */ CString CRegistry::GetValueName(IN LPCTSTR szKeyName, IN int iIndex, OUT DWORD &dwType, OUT PDWORD pdwErrCode) {
   DWORD dwErrCode = ERROR_SUCCESS;
   CString strResult;

   CString strKeyName(szKeyName);
   strKeyName.TrimLeft(TEXT('\\'));
   strKeyName += TEXT('\\');

   HKEY hKey = NULL;

   do {
      int iPos = strKeyName.Find(TEXT('\\'));
      if (iPos != -1) {
         CString strKey = strKeyName.Left(iPos);
         if (hKey == NULL) {
            if (strKey.CompareNoCase(TEXT("HKEY_CLASSES_ROOT"  )) == 0) hKey = HKEY_CLASSES_ROOT  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_USER"  )) == 0) hKey = HKEY_CURRENT_USER  ; else
            if (strKey.CompareNoCase(TEXT("HKEY_LOCAL_MACHINE" )) == 0) hKey = HKEY_LOCAL_MACHINE ; else
            if (strKey.CompareNoCase(TEXT("HKEY_USERS"         )) == 0) hKey = HKEY_USERS         ; else
            if (strKey.CompareNoCase(TEXT("HKEY_CURRENT_CONFIG")) == 0) hKey = HKEY_CURRENT_CONFIG; else {
               dwErrCode = ERROR_PATH_NOT_FOUND;
               break;
            }
         } else {
            HKEY hKeyOld = hKey;
            dwErrCode = ::RegOpenKeyEx(hKey, strKey, 0, KEY_READ, &hKey);
            ::RegCloseKey(hKeyOld);
            if (dwErrCode != ERROR_SUCCESS) {
               break;
            }
         }
         strKeyName = strKeyName.Mid(iPos+1);
      } else {
         strResult = CRegistry::GetValueName(hKey, iIndex, dwType, pdwErrCode);
         if (pdwErrCode) {
            dwErrCode = *pdwErrCode;
         } else {
            dwErrCode = ::GetLastError();
         }
         break;
      }
   } while (TRUE);
   ::RegCloseKey(hKey);

   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   ::SetLastError(dwErrCode);
   return strResult;
}

CString MIMEType2FileType(LPCTSTR szMIMEType) {
   CString strRes = CRegistry::GetString(TEXT("HKEY_CLASSES_ROOT\\MIME\\Database\\Content Type\\") + CString(szMIMEType) + TEXT("\\Extension"));
   if (!strRes.IsEmpty()) {
      strRes = strRes.Mid(1);
   }
   return strRes;
}

CString FileType2MIMEType(LPCTSTR szFileType) {
   return CRegistry::GetString(TEXT("HKEY_CLASSES_ROOT\\.") + CString(szFileType) + TEXT("\\Content Type"));
}

CString Replace(IN const CString &str, IN LPCTSTR szOld, IN LPCTSTR szNew) {
   CString strRes = str;
   strRes.Replace(szOld, szNew);
   return strRes;
}

CString Replace(IN const CString &str, IN TCHAR cOld, IN TCHAR cNew) {
   CString strRes = str;
   strRes.Replace(cOld, cNew);
   return strRes;
}

CString GetIPAdress() {
   HOSTENT *he = NULL;
   BYTE *IP = NULL;
   char name[0xFF];

   gethostname(name, sizeof(name));
   he = gethostbyname(name);
   if(he) {
      IP = (BYTE *)he->h_addr_list[0];
   }
   if (IP)
      return ::Format(TEXT("%i.%i.%i.%i"), IP[0], IP[1], IP[2], IP[3]);
   else
      return CString();
}

CString GetHostName() {
   HOSTENT *he = NULL;
   BYTE *IP = NULL;
   char name[0xFF];

   gethostname(name, sizeof(name));
   he = gethostbyname(name);
   if(he)
      return CString(he->h_name);
   else
      return CString();
}

// возвращаю указатель на данные ресурса
LPVOID GetResource(
   IN HINSTANCE hInstance,
   IN LPCTSTR szResourceName,
   IN LPCTSTR szResourceType,
   OUT DWORD &dwSize,
   OUT PDWORD pdwErrCode
) {
   dwSize = 0;
   DWORD dwErrCode = ERROR_SUCCESS;
   LPVOID pRes = NULL;
   HRSRC hRsrc = ::FindResource(hInstance, szResourceName, szResourceType);
   if (!hRsrc) {
      dwErrCode = ::GetLastError();
   } else {
      HGLOBAL hGlobal = ::LoadResource(hInstance, hRsrc);
      if (!hGlobal) {
         dwErrCode = ::GetLastError();
      } else {
         dwSize = ::SizeofResource(hInstance, hRsrc);
         pRes = ::LockResource(hGlobal);
         if (!pRes) {
            dwErrCode = ::GetLastError();
            dwSize = 0;
         }
      }
   }
   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   return pRes;
}

// читаю из ресурсов и сохраняю в файл если его ещё нет
BOOL SaveResourceAsFile(
   IN HINSTANCE hInstance,
   IN LPCTSTR szFileName,
   IN LPCTSTR szResourceName,
   IN LPCTSTR szResourceType,
   OUT PDWORD pdwErrCode
) {
   DWORD dwErrCode = ERROR_SUCCESS;
   BOOL bRes = FALSE;
   DWORD dwSize = 0;
   BYTE *pResource = (BYTE*)GetResource(hInstance, szResourceName, szResourceType, dwSize, &dwErrCode);
   if (pResource && (dwErrCode == ERROR_SUCCESS)) {
      if (dwSize == GetFileSize(szFileName)) {
         bRes = TRUE;
      } else {
         HANDLE hFile = ::CreateFile(
            szFileName,
            GENERIC_WRITE,
            FILE_SHARE_READ,
            NULL,
            CREATE_ALWAYS,
            FILE_ATTRIBUTE_NORMAL,
            NULL
         );
         if (hFile == INVALID_HANDLE_VALUE) {
            dwErrCode = ::GetLastError();
         } else {
            DWORD dwNOBW = 0;
            if (!::WriteFile(hFile, pResource, dwSize, &dwNOBW, NULL) || (dwSize != dwNOBW)) {
               dwErrCode = ::GetLastError();
            } else {
               bRes = TRUE;
            }
            ::CloseHandle(hFile); hFile = NULL;
         }
      }
   }
   if (pdwErrCode) {
      *pdwErrCode = dwErrCode;
   }
   return bRes;
}

CString GUID2String(IN REFGUID guid) {
   CString strRes;
   strRes.Format(_T("{%08X-%04X-%04X-%02X%02X-%02X%02X%02X%02X%02X%02X}"),
      int(guid.Data1), int(guid.Data2), int(guid.Data3), int(guid.Data4[0]), int(guid.Data4[1]), int(guid.Data4[2]), int(guid.Data4[3]), int(guid.Data4[4]), int(guid.Data4[5]), int(guid.Data4[6]), int(guid.Data4[7]));
   return strRes;
}

BOOL String2GUID(IN LPCTSTR szGUID, OUT GUID &guid) {
   BOOL bRes = FALSE;
   DWORD dwErrCode = ERROR_INVALID_PARAMETER;
   if (szGUID) {
      dwErrCode = ERROR_INVALID_DATA;
      // убеждаюсь что это GUID
      // 0         1         2         3
      // 01234567890123456789012345678901234567
      // {01234567-0123-0123-0123-0123456789AB}
      CString strGUID(szGUID);
      strGUID.MakeUpper();
      static TCHAR szABC[] = TEXT("0123456789ABCDEF");
      if ((strGUID.GetLength() != 38) ||
          (szGUID[ 0] != TEXT('{')) ||
          (szGUID[ 9] != TEXT('-')) ||
          (szGUID[14] != TEXT('-')) ||
          (szGUID[19] != TEXT('-')) ||
          (szGUID[24] != TEXT('-')) ||
          (szGUID[37] != TEXT('}')) /**/||
          (!strGUID.Mid( 1, 8).SpanExcluding(szABC).IsEmpty()) ||
          (!strGUID.Mid(10, 4).SpanExcluding(szABC).IsEmpty()) ||
          (!strGUID.Mid(15, 4).SpanExcluding(szABC).IsEmpty()) ||
          (!strGUID.Mid(20, 4).SpanExcluding(szABC).IsEmpty()) ||
          (!strGUID.Mid(25,12).SpanExcluding(szABC).IsEmpty())/**/
         )
      {
      } else {
         strGUID.Replace(TEXT('{'), TEXT(' '));
         strGUID.Replace(TEXT('-'), TEXT(' '));
         strGUID.Replace(TEXT('}'), TEXT(' '));
         /**
         strGUID = strGUID.Mid( 1, 8) + //            
                   strGUID.Mid(10, 4) + // + TEXT(" ")
                   strGUID.Mid(15, 4) + // + TEXT(" ")
                   strGUID.Mid(20, 2) + // + TEXT(" ")
                   strGUID.Mid(22, 2) + // + TEXT(" ")
                   strGUID.Mid(25, 2) + // + TEXT(" ")
                   strGUID.Mid(27, 2) + // + TEXT(" ")
                   strGUID.Mid(29, 2) + // + TEXT(" ")
                   strGUID.Mid(31, 2) + // + TEXT(" ")
                   strGUID.Mid(33, 2) + // + TEXT(" ")
                   strGUID.Mid(35, 2);  // + TEXT(" ")
         /**/
         int iData1=0, iData2=0, iData3=0, iData4[8] = {0,0,0,0,0,0,0,0};
         if (::_stscanf((LPCTSTR)strGUID, TEXT("%08X%04X%04X%02X%02X%02X%02X%02X%02X%02X%02X"),
                        &iData1, &iData2, &iData3,
                        &iData4[0], &iData4[1], &iData4[2], &iData4[3],
                        &iData4[4], &iData4[5], &iData4[6], &iData4[7]) == 11)
         {
            guid.Data1 = iData1; guid.Data2 = iData2; guid.Data3 = iData3;
            guid.Data4[0] = iData4[0];
            guid.Data4[1] = iData4[1];
            guid.Data4[2] = iData4[2];
            guid.Data4[3] = iData4[3];
            guid.Data4[4] = iData4[4];
            guid.Data4[5] = iData4[5];
            guid.Data4[6] = iData4[6];
            guid.Data4[7] = iData4[7];
            bRes = TRUE;
            dwErrCode = ERROR_SUCCESS;
         } else {
            dwErrCode = ERROR_INVALID_DATA; //::GetLastError();
         }
      }
   }
   ::SetLastError(dwErrCode);
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
