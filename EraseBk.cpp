////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "EraseBk.h"
//
// Ф-ции для заливки фоновым цветом окон
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "EraseBk.h"
#include "FastMines2.h"
////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern CFastMines2Project *gpFM2Proj;
extern HINSTANCE ghInstance;

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
HBRUSH OnCtlColor(HWND hwnd, HDC hdc, HWND hwndChild, int type) {
   if (CTLCOLOR_SCROLLBAR == type)
      MessageBeep(0);
   if (gpFM2Proj && gpFM2Proj->GetSkin().m_bToAll) {
      static HBRUSH hBrush = NULL;
      static COLORREF oldBkColor = CLR_INVALID; // invalid color
      //SetBkMode(hdc, TRANSPARENT); // вывод текста на прозрачном фоне
      SetBkColor(hdc, gpFM2Proj->GetSkin().m_colorBk);
      if (oldBkColor != gpFM2Proj->GetSkin().m_colorBk) {
         DeleteObject(hBrush);
         hBrush = CreateSolidBrush(oldBkColor = gpFM2Proj->GetSkin().m_colorBk);
       //hBrush = CreateHatchBrush(HS_DIAGCROSS, oldBkColor = gpFM2Proj->GetSkin().m_colorBk);
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
   const SIZE size = {3*yCap, yCap}; // размер области где находятся кнопки управления окнами
   const RECT fillRect = {windowRect.right - GetSystemMetrics(SM_CXFIXEDFRAME) - size.cx,
                          windowRect.top   + GetSystemMetrics(SM_CYFIXEDFRAME),
                          windowRect.right - GetSystemMetrics(SM_CXFIXEDFRAME),
                          windowRect.top   + GetSystemMetrics(SM_CYFIXEDFRAME) + size.cy};
   FillWnd(hwnd, bkColor, false, fillRect);
}

void FillMenu(HWND hwnd, COLORREF bkColor) {
   if (!GetMenu(hwnd)) return;
   if (bkColor == GetSysColor(COLOR_MENU)) return;
   const int yCap  = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CYCAPTION);
   const int xBrdr = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CXFIXEDFRAME);
   const int yBrdr = ((GetWindowLong(hwnd, GWL_STYLE) & WS_CAPTION) ^ WS_CAPTION) ? 0 : GetSystemMetrics(SM_CYFIXEDFRAME);

   RECTEX windowRect; GetWindowRect(hwnd, &windowRect);
   RECTEX clientRect; GetClientRect(hwnd, &clientRect);
   const int heightMenu = (windowRect.height()) - 2*yBrdr -
                          (clientRect.height()) - yCap;
   const RECT fillRect = {windowRect.left  + xBrdr,
                          windowRect.top   + yBrdr + yCap,
                          windowRect.right - xBrdr,
                          windowRect.top   + yBrdr + yCap + heightMenu};
   FillWnd(hwnd, bkColor, false, fillRect);
}

void FillWnd(HWND hwnd, COLORREF bkColor, bool client, const RECT& fillRect) {
   if (bkColor == GetSysColor(COLOR_BTNFACE)) return;
   SIZE size;
   SIZE offset = {0,0};
   {
      RECTEX Rect(client ? GetClientRect(hwnd) : GetWindowRect(hwnd));
      if (fillRect == errorRect) {
         size = Rect.size();
      } else {
         size = RECTEX(fillRect).size();
         offset.cx = fillRect.left-Rect.left;
         offset.cy = fillRect.top -Rect.top;
      }
   }
   HDC hDC  = client ? GetDC(hwnd) : GetWindowDC(hwnd);
   HDC hCDC = CreateCompatibleDC(hDC);
   HBITMAP hBmp  = CreateCompatibleBitmap(hDC, size.cx, size.cy);
   HBITMAP hBmp2 = CreateCompatibleBitmap(hDC, size.cx, size.cy);
   HBITMAP hBmpOld = (HBITMAP)SelectObject(hCDC, hBmp);
   HBRUSH hBrushNew = CreateSolidBrush(bkColor);
   HBRUSH hBrushOld = (HBRUSH)SelectObject(hCDC, hBrushNew);

   BitBlt(hCDC, 0,0, size.cx, size.cy,
          hDC , offset.cx,offset.cy, SRCCOPY);
   SelectObject(hCDC, hBmp2);
   PatBlt(hCDC, 0,0, size.cx, size.cy, PATCOPY);
   DrawMaskedBitmap(hCDC, hBmp, 0, 0,
                    size.cx, size.cy,
                    size.cx, size.cy, GetSysColor(COLOR_BTNFACE));
   BitBlt(hDC, offset.cx, offset.cy, size.cx, size.cy,
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
BOOL OnEraseBkgnd(HWND hwnd, HDC hdc, COLORREF colorBk) {
   HBRUSH hBrushNew = CreateSolidBrush(colorBk);
   HBRUSH hBrushOld = (HBRUSH)SelectObject(hdc, hBrushNew);
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

#include <ImageHlp.h>
#pragma comment(lib, "ImageHlp")

#include <detours.h>
#pragma comment(lib, "detours")

BOOL ReplaceIATEntryInOneMod(PCSTR szCalleeModName, PVOID pfnCurrent, PVOID pfnNew, HMODULE hmodCaller)
{
   // Get the address of the module's import section
   ULONG ulSize;
   PIMAGE_IMPORT_DESCRIPTOR pImportDesc = (PIMAGE_IMPORT_DESCRIPTOR)
      ImageDirectoryEntryToData(hmodCaller, TRUE,
      IMAGE_DIRECTORY_ENTRY_IMPORT, &ulSize);

   if (pImportDesc == NULL)
      return FALSE;  // This module has no import section

   // Find the import descriptor containing references to callee's functions
   for (; pImportDesc->Name; pImportDesc++) {
      PSTR pszModName = (PSTR) ((PBYTE) hmodCaller + pImportDesc->Name);
      if (lstrcmpiA(pszModName, szCalleeModName) == 0)
         break;   // Found
   }

   if (pImportDesc->Name == 0)
      return FALSE;  // This module doesn't import any functions from this callee

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
         DWORD dwOldProtect = 0;
         // The addresses match, change the import section address
         BOOL bRes = VirtualProtect(ppfn, sizeof(PVOID), PAGE_READWRITE, &dwOldProtect);
         if (!bRes) return FALSE;
         DWORD dwWritten = 0;
         bRes = WriteProcessMemory(GetCurrentProcess(), ppfn, &pfnNew, sizeof(PVOID), &dwWritten); // даже если была ошибка - надо вернуть dwOldProtect
         bRes &= VirtualProtect(ppfn, sizeof(PVOID), dwOldProtect, &dwOldProtect); // restore protect
         if ((!bRes) || (dwWritten != sizeof(PVOID))) return FALSE;
         return TRUE;  // We did it, get out
      }
   }

   // If we get to here, the function is not in the caller's import section
   return  FALSE;
}

typedef DWORD (WINAPI* PF_GetSysColor)(int);
PF_GetSysColor pfGetSysColor = GetSysColor;
DWORD WINAPI MyGetSysColor (int nIndex) {
   if ((nIndex == COLOR_BTNFACE) && gpFM2Proj && gpFM2Proj->GetSkin().m_bToAll)
      return gpFM2Proj->GetSkin().m_colorBk;
   return pfGetSysColor(nIndex);
}

typedef HBRUSH (WINAPI* PF_GetSysColorBrush)(int);
PF_GetSysColorBrush pfGetSysColorBrush = GetSysColorBrush;
HBRUSH WINAPI MyGetSysColorBrush (int nIndex) {
   if ((nIndex == COLOR_BTNFACE) && gpFM2Proj && gpFM2Proj->GetSkin().m_bToAll) {
      static HBRUSH hBrush = NULL;
      static COLORREF oldBkColor = CLR_INVALID; // error color
      if (oldBkColor != gpFM2Proj->GetSkin().m_colorBk) {
         oldBkColor = gpFM2Proj->GetSkin().m_colorBk;
         DeleteObject(hBrush);
         hBrush = CreateSolidBrush(oldBkColor);
      }
      return hBrush;
   }
   return pfGetSysColorBrush(nIndex);
}

typedef FARPROC (WINAPI* PF_GetProcAddress)(HMODULE, LPCSTR);
PF_GetProcAddress pfGetProcAddress = GetProcAddress;
FARPROC WINAPI MyGetProcAddress (HMODULE hModule, LPCSTR lpProcName) {
   return pfGetProcAddress(hModule, lpProcName);
}

typedef LRESULT (WINAPI *PF_SendMessage)(HWND, UINT, WPARAM, LPARAM);
PF_SendMessage pfSendMessageA = SendMessageA;
LRESULT WINAPI MySendMessageA (HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   return pfSendMessageA(hWnd, Msg, wParam, lParam);
}

PF_SendMessage pfSendMessageW = SendMessageW;
LRESULT WINAPI MySendMessageW (HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   return pfSendMessageW(hWnd, Msg, wParam, lParam);
}

typedef BOOL (WINAPI *PF_PostMessage)(HWND, UINT, WPARAM, LPARAM);
PF_PostMessage pfPostMessageA = PostMessageA;
BOOL WINAPI MyPostMessageA (HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   return pfPostMessageA(hWnd, Msg, wParam, lParam);
}

PF_PostMessage pfPostMessageW = PostMessageW;
BOOL WINAPI MyPostMessageW (HWND hWnd, UINT Msg, WPARAM wParam, LPARAM lParam) {
   return pfPostMessageW(hWnd, Msg, wParam, lParam);
}

typedef int (WINAPI *PF_GetSystemMetrics)(int);
PF_GetSystemMetrics pfGetSystemMetrics = GetSystemMetrics;
int WINAPI MyGetSystemMetrics(int nIndex) {
   return pfGetSystemMetrics(nIndex);
}


BOOL nsEraseBk::ReplaceFunctions() {
//   const HMODULE hModule = GetModuleHandle(NULL);
   BOOL result = TRUE;

   pfGetSysColor      = (PF_GetSysColor     )DetourFunction((PBYTE)::GetSysColor     , (PBYTE)MyGetSysColor     );
   pfGetSysColorBrush = (PF_GetSysColorBrush)DetourFunction((PBYTE)::GetSysColorBrush, (PBYTE)MyGetSysColorBrush);
   pfGetProcAddress   = (PF_GetProcAddress  )DetourFunction((PBYTE)::GetProcAddress  , (PBYTE)MyGetProcAddress  );
 //pfSendMessageA     = (PF_SendMessage     )DetourFunction((PBYTE)::SendMessageA    , (PBYTE)MySendMessageA    );
 //pfSendMessageW     = (PF_SendMessage     )DetourFunction((PBYTE)::SendMessageW    , (PBYTE)MySendMessageW    );
 //pfPostMessageA     = (PF_PostMessage     )DetourFunction((PBYTE)::PostMessageA    , (PBYTE)MyPostMessageA    );
 //pfPostMessageW     = (PF_PostMessage     )DetourFunction((PBYTE)::PostMessageW    , (PBYTE)MyPostMessageW    );
 //pfGetSystemMetrics = (PF_GetSystemMetrics)DetourFunction((PBYTE)::GetSystemMetrics, (PBYTE)MyGetSystemMetrics);
/*
   result &= ReplaceIATEntryInOneMod("User32.dll"  , GetSysColor     , MyGetSysColor     , ghInstance);
   result &= ReplaceIATEntryInOneMod("User32.dll"  , GetSysColorBrush, MyGetSysColorBrush, ghInstance);
   result &= ReplaceIATEntryInOneMod("kernel32.dll", GetProcAddress  , MyGetProcAddress  , ghInstance);
   result &= ReplaceIATEntryInOneMod("User32.dll"  , SendMessageA    , MySendMessageA    , ghInstance);
   result &= ReplaceIATEntryInOneMod("User32.dll"  , SendMessageW    , MySendMessageW    , ghInstance);
   result &= ReplaceIATEntryInOneMod("User32.dll"  , PostMessageA    , MyPostMessageA    , ghInstance);
   result &= ReplaceIATEntryInOneMod("User32.dll"  , PostMessageW    , MyPostMessageW    , ghInstance);
*/
   return result;
}

namespace nsReplaceEntry_DLL {
   BOOL init = nsEraseBk::ReplaceFunctions();
}
#endif
