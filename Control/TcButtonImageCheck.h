////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcButtonImageCheck.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_BUTTONIMAGECHECK
#define FILE_BUTTONIMAGECHECK

#include ".\TcButtonImage.h"

namespace nsControlButtonImageCheck {
   LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
}

class TcButtonImageCheck: public TcButtonImage {
protected:
   bool check;
protected:
   void Cls_OnPaint   (HWND) const; // WM_PAINT
   void Cls_OnSetCheck(HWND, UINT); // BM_SETCHECK
   friend LRESULT CALLBACK nsControlButtonImageCheck::WndProc(HWND, UINT, WPARAM, LPARAM);
public:
   TcButtonImageCheck();
   void Create(const HWND, const int);
};

#endif // FILE_BUTTONIMAGECHECK
