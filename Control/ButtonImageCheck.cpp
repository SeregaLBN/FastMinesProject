////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "ButtonImageCheck.cpp"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <WindowsX.h>
#include "ButtonImageCheck.h"
#include "../CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

const TCHAR CButtonImageCheck::SZ_CLASS_WND[] = TEXT("ClassWndFastMinesButtonImageCheck");

LRESULT CALLBACK CButtonImageCheck::WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   CButtonImageCheck* const This = (CButtonImageCheck*)GetWindowUserData(hWnd);
   if (This) {
      switch(msg){
      HANDLE_MSG(hWnd, WM_PAINT      , This->OnPaint);
      HANDLE_MSG(hWnd, WM_ERASEBKGND , This->OnEraseBkgnd);
      HANDLE_MSG(hWnd, WM_LBUTTONUP  , This->OnLButtonUp);
      HANDLE_MSG(hWnd, WM_LBUTTONDOWN, This->OnLButtonDown);
      HANDLE_MSG(hWnd, WM_MOUSEMOVE  , This->OnMouseMove);
      case BM_SETIMAGE:
         return (This->OnSetImage(hWnd, (CImage*)lParam), 0L);
      case BM_SETCHECK:
         return (This->OnSetCheck(hWnd, wParam), 0L);
      case BM_GETCHECK:
         if (This->IsChecked())
            return BST_CHECKED;
         else
            return BST_UNCHECKED;
      }
   }
   return DefWindowProc(hWnd, msg, wParam, lParam);
}

CButtonImageCheck::CButtonImageCheck():
   m_bCheck(false)
{
   static BOOL bInit =
   ::RegClass(
      0,                               // UINT    style
      WndProc,                         // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      SZ_CLASS_WND                     // LPCTSTR lpszClassName
   );
}

void CButtonImageCheck::Create(const HWND hParent, const int id) {
   m_hWndParent = hParent;
   m_iIdControl = id;
   m_hWnd = CreateWindow(SZ_CLASS_WND, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndParent, HMENU(id), ghInstance, NULL);
   ::SetWindowUserData(m_hWnd, (LONG)this);
}

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
void CButtonImageCheck::OnPaint(HWND hWnd) const {
   PAINTSTRUCT PaintStruct;
   HDC hDC = BeginPaint(hWnd, &PaintStruct);
   {
      RECTEX Rect = ::GetClientRect(hWnd);
      Rect.left   += 2+!!m_bDown;
      Rect.top    += 2+!!m_bDown;
      Rect.right  -= 2-!!m_bDown;
      Rect.bottom -= 2-!!m_bDown;
      if (m_pImage)
         m_pImage->Draw(hDC, &Rect);

      Rect = ::GetClientRect(hWnd);

      HPEN hPenNew1 = ::CreatePen(PS_SOLID, 2, (m_bDown || m_bCheck) ? 0x00FFFFFF : 0);
      HPEN hPenOld = (HPEN)SelectObject(hDC, hPenNew1);
      MoveToEx(hDC, Rect.right-1, Rect.top   +1, NULL);
      LineTo  (hDC, Rect.right-1, Rect.bottom-1);
      LineTo  (hDC, Rect.left +1, Rect.bottom-1);

      HPEN hPenNew2 = CreatePen(PS_SOLID, 2, (m_bDown || m_bCheck) ? 0 : 0x00FFFFFF);
      SelectObject(hDC, hPenNew2);
      MoveToEx(hDC, Rect.left +1, Rect.bottom-2, NULL);
      LineTo  (hDC, Rect.left +1, Rect.top   +1);
      LineTo  (hDC, Rect.right-2, Rect.top   +1);

      SelectObject(hDC, hPenOld);
      DeleteObject(hPenNew1);
      DeleteObject(hPenNew2);
   }
   EndPaint(hWnd, &PaintStruct);
}

// BM_SETCHECK
void CButtonImageCheck::OnSetCheck(HWND hWnd, UINT fCheck) {
   m_bCheck = (fCheck == BST_CHECKED);
   InvalidateRect(hWnd, NULL, TRUE);
}
