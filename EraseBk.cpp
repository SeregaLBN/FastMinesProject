////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "EraseBk.h"
//
// ‘-ции дл€ заливки фоновым цветом окон
////////////////////////////////////////////////////////////////////////////////

#include ".\EraseBk.h"
#include ".\TcImage.h"
#include ".\TcMosaic.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsEraseBk {

// WM_CTLCOLORMSGBOX
// WM_CTLCOLOREDIT
// WM_CTLCOLORLISTBOX
// WM_CTLCOLORBTN
// WM_CTLCOLORDLG
// WM_CTLCOLORSCROLLBAR
// WM_CTLCOLORSTATIC
HBRUSH Cls_OnCtlColor(HWND hwnd, HDC hdc, HWND hwndChild, int type) {
   if (CTLCOLOR_SCROLLBAR == type)
      MessageBeep(0);
   if (gpMosaic && gpMosaic->GetSkin().toAll) {
      static HBRUSH hBrush = NULL;
      static COLORREF oldBkColor = CLR_INVALID; // invalid color
      //SetBkMode(hdc, TRANSPARENT); // вывод текста на прозрачном фоне
      SetBkColor(hdc, gpMosaic->GetSkin().colorBk);
      if (oldBkColor != gpMosaic->GetSkin().colorBk) {
         DeleteObject(hBrush);
         hBrush = CreateSolidBrush(oldBkColor = gpMosaic->GetSkin().colorBk);
       //hBrush = CreateHatchBrush(HS_DIAGCROSS, oldBkColor = gpMosaic->GetSkin().colorBk);
      }
      return hBrush;
   }
   return NULL;//(HBRUSH)DefWindowProc(hwnd, WM_CTLCOLORMSGBOX+type, (WPARAM)hdc, (LPARAM)hwndChild);//GetSysColorBrush(COLOR_BTNFACE);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
void FillCaption(HWND hwnd, COLORREF bkColor) { // кнопки заголовка окнами (minimize, maximaze/restore, close)
   if (bkColor == GetSysColor(COLOR_BTNFACE)) return;
   if ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) return;

   RECT windowRect; GetWindowRect(hwnd, &windowRect);
   const int yCap = GetSystemMetrics(SM_CYCAPTION);
   const POINT size = {3*yCap, yCap}; // размер области где наход€тс€ кнопки управлени€ окнами
   const RECT fillRect = {windowRect.right - GetSystemMetrics(SM_CXFIXEDFRAME) - size.x,
                          windowRect.top   + GetSystemMetrics(SM_CYFIXEDFRAME),
                          windowRect.right - GetSystemMetrics(SM_CXFIXEDFRAME),
                          windowRect.top   + GetSystemMetrics(SM_CYFIXEDFRAME) + size.y};
   FillWnd(hwnd, bkColor, false, fillRect);
}

void FillMenu(HWND hwnd, COLORREF bkColor) {
   if (!GetMenu(hwnd)) return;
   if (bkColor == GetSysColor(COLOR_MENU)) return;
   const int yCap  = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CYCAPTION);
   const int xBrdr = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CXFIXEDFRAME);
   const int yBrdr = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CYFIXEDFRAME);

   RECT windowRect; GetWindowRect(hwnd, &windowRect);
   RECT clientRect; GetClientRect(hwnd, &clientRect);
   const int heightMenu = (windowRect.bottom-windowRect.top) - 2*yBrdr -
                          (clientRect.bottom-clientRect.top) - yCap;
   const RECT fillRect = {windowRect.left  + xBrdr,
                          windowRect.top   + yBrdr + yCap,
                          windowRect.right - xBrdr,
                          windowRect.top   + yBrdr + yCap + heightMenu};
   FillWnd(hwnd, bkColor, false, fillRect);
}

bool operator== (const RECT& a, const RECT& b) {
   return (a.left   == b.left   &&
           a.right  == b.right  &&
           a.top    == b.top    &&
           a.bottom == b.bottom);
}

void FillWnd(HWND hwnd, COLORREF bkColor, bool client, RECT fillRect) {
   if (bkColor == GetSysColor(COLOR_BTNFACE)) return;
   POINT size;
   POINT offset = {0,0};
   {
      RECT Rect;
      client ? GetClientRect(hwnd, &Rect) : GetWindowRect(hwnd, &Rect);
      if (fillRect == errorRect) {
         size.x = Rect.right -Rect.left;
         size.y = Rect.bottom-Rect.top;
      } else {
         size.x = fillRect.right -fillRect.left;
         size.y = fillRect.bottom-fillRect.top;
         offset.x = fillRect.left-Rect.left;
         offset.y = fillRect.top -Rect.top;
      }
   }
   HDC hDC  = client ? GetDC(hwnd) : GetWindowDC(hwnd);
   HDC hCDC = CreateCompatibleDC(hDC);
   HBITMAP hBmp  = CreateCompatibleBitmap(hDC, size.x, size.y);
   HBITMAP hBmp2 = CreateCompatibleBitmap(hDC, size.x, size.y);
   HBITMAP hBmpOld = SelectObject(hCDC, hBmp);
   HBRUSH hBrushNew = CreateSolidBrush(bkColor);
   HBRUSH hBrushOld = SelectObject(hCDC, hBrushNew);

   BitBlt(hCDC, 0,0, size.x, size.y,
          hDC , offset.x,offset.y, SRCCOPY);
   SelectObject(hCDC, hBmp2);
   PatBlt(hCDC, 0,0, size.x, size.y, PATCOPY);
   DrawMaskedBitmap(hCDC, hBmp, 0, 0,
                    size.x, size.y,
                    size.x, size.y, GetSysColor(COLOR_BTNFACE));
   BitBlt(hDC, offset.x, offset.y, size.x, size.y,
          hCDC,0,0, SRCCOPY);
   SelectObject(hCDC, hBrushOld);
   SelectObject(hCDC, hBmpOld);
   DeleteDC(hCDC);
   ReleaseDC(hwnd, hDC);
   DeleteObject(hBrushNew);
   DeleteObject(hBmp);
   DeleteObject(hBmp2);
}

// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc, COLORREF colorBk) {
   HBRUSH hBrushNew = CreateSolidBrush(colorBk);
   HBRUSH hBrushOld = SelectObject(hdc, hBrushNew);
   RECT Rect;
   GetClientRect(hwnd, &Rect);
   PatBlt(hdc, 0,0, Rect.right, Rect.bottom, PATCOPY);
   SelectObject(hdc, hBrushOld);
   DeleteObject(hBrushNew);
   return TRUE;
}

#endif // REPLACEBKCOLORFROMFILLWINDOW
} // namespace nsEraseBk


#if 0
#pragma comment( lib, "Imagehlp" )

#include <ImageHlp.h>

void ReplaceIATEntryInOneMod(PCSTR pszCalleeModName, PVOID pfnCurrent, PVOID pfnNew, HMODULE hmodCaller) {

   // Get the address of the module's import section
   ULONG ulSize;
   PIMAGE_IMPORT_DESCRIPTOR pImportDesc = (PIMAGE_IMPORT_DESCRIPTOR)
      ImageDirectoryEntryToData(hmodCaller, TRUE, 
      IMAGE_DIRECTORY_ENTRY_IMPORT, &ulSize);

   if (pImportDesc == NULL)
      return;  // This module has no import section


   // Find the import descriptor containing references to callee's functions
   for (; pImportDesc->Name; pImportDesc++) {
      PSTR pszModName = (PSTR) ((PBYTE) hmodCaller + pImportDesc->Name);
      if (lstrcmpiA(pszModName, pszCalleeModName) == 0) 
         break;   // Found
   }

   if (pImportDesc->Name == 0)
      return;  // This module doesn't import any functions from this callee

   // Get caller's import address table (IAT) for the callee's functions
   PIMAGE_THUNK_DATA pThunk = (PIMAGE_THUNK_DATA) 
      (/*(PBYTE)*/(int)hmodCaller + (int)pImportDesc->FirstThunk);

   // Replace current function address with new function address
   for (; pThunk->u1.Function; pThunk++) {

      // Get the address of the function address
      PROC* ppfn = (PROC*) &pThunk->u1.Function;

      // Is this the function we're looking for?
      BOOL fFound = (*ppfn == pfnCurrent);
      /*
      if (!fFound && (*ppfn > sm_pvMaxAppAddr)) {

         // If this is not the function and the address is in a shared DLL, 
         // then maybe we're running under a debugger on Windows 98. In this 
         // case, this address points to an instruction that may have the 
         // correct address.

         PBYTE pbInFunc = (PBYTE) *ppfn;
         if (pbInFunc[0] == cPushOpCode) {
            // We see the PUSH instruction, the real function address follows
            ppfn = (PROC*) &pbInFunc[1];

            // Is this the function we're looking for?
            fFound = (*ppfn == pfnCurrent);
         }
      }
      */
      if (fFound) {
         // The addresses match, change the import section address
         WriteProcessMemory(GetCurrentProcess(), ppfn, &pfnNew, 
            sizeof(pfnNew), NULL);
         return;  // We did it, get out
      }
   }

   // If we get to here, the function is not in the caller's import section
}

DWORD (WINAPI* pfGetSysColor)(int) = GetSysColor;
DWORD  WINAPI  MyGetSysColor (int nIndex) {
   if ((nIndex == COLOR_BTNFACE) && gpMosaic && gpMosaic->GetSkin().toAll)
      return gpMosaic->GetSkin().colorBk;
   return pfGetSysColor(nIndex);
}

void ReplaceFunction_GetSysColor(HMODULE hModule) {
   ReplaceIATEntryInOneMod(
      "User32.dll",
      GetSysColor,
      MyGetSysColor,
      hModule
   );
}

HBRUSH (WINAPI* pfGetSysColorBrush)(int) = GetSysColorBrush;
HBRUSH  WINAPI  MyGetSysColorBrush (int nIndex) {
   if ((nIndex == COLOR_BTNFACE) && gpMosaic && gpMosaic->GetSkin().toAll) {
      static HBRUSH hBrush = NULL;
      static COLORREF oldBkColor = 0xFF000000; // error color
      if (oldBkColor != gpMosaic->GetSkin().colorBk) {
         oldBkColor = gpMosaic->GetSkin().colorBk;
         DeleteObject(hBrush);
         hBrush = CreateSolidBrush(oldBkColor);
      }
      return hBrush;
   }
   return pfGetSysColorBrush(nIndex);
}

void ReplaceFunction_GetSysColorBrush(HMODULE hModule) {
   ReplaceIATEntryInOneMod(
      "User32.dll",
      GetSysColorBrush,
      MyGetSysColorBrush,
      hModule
   );
}
#endif
