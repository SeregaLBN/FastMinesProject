////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcButtonImage.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_BUTTONIMAGE
#define FILE_BUTTONIMAGE

#include "..\TcImage.h"

namespace nsControlButtonImage {
   LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
}

class TcButtonImage {
protected:
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   const bool    * pToAll;
   const COLORREF* pColorBk;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HWND hWnd, hWndParent;
   bool down;
   TcImage* pImage;
   int idControl;
protected:
   void Cls_OnPaint      (HWND) const;                 // WM_PAINT
   void Cls_OnSetImage   (HWND, TcImage*);             // BM_SETIMAGE
   void Cls_OnLButtonUp  (HWND, int , int, UINT);      // WM_LBUTTONUP
   void Cls_OnLButtonDown(HWND, BOOL, int, int, UINT); // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
   void Cls_OnMouseMove  (HWND, int , int, UINT);      // WM_MOUSEMOVE
   friend LRESULT CALLBACK nsControlButtonImage::WndProc(HWND, UINT, WPARAM, LPARAM);
public:
   TcButtonImage();
   HWND GetHandle() const {return hWnd;}
   void Create(const HWND, const int);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   void SetColor(const bool&, const COLORREF&);
#endif // REPLACEBKCOLORFROMFILLWINDOW
};

#endif // FILE_BUTTONIMAGE
