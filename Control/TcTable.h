////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTable.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTABLE
#define FILE_TCTABLE

#include "..\TcImage.h"
#include <vector>
#include <string>

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#pragma warning(disable:4804) // больше меньше для строк

namespace nsControlTable {
   LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
}

#define NC_TABLE_SELECT_CELL 0 // notification code
#define NC_TABLE_DELETE_CELL 1 // notification code

struct TsTableData {
#ifdef UNICODE
   std::wstring szText;
#else
   std::string szText;
#endif // UNICODE
   UINT uFormatText;
   const TcImage* pImage;
   TsTableData(): pImage(NULL), uFormatText(DT_CENTER | DT_VCENTER | DT_SINGLELINE) {};
};

class TcTable {
protected:
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   const bool    * pToAll;
   const COLORREF* pColorBk;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HWND hWnd, hWndParent;
   int idControl;
   int defW; // default width  cell
   int defH; // default height cell
   int staticCol; // кол-во статичных колонок
   int staticRow; // кол-во статичных строк
   POINT currentCell;  // текущая (выделенная рамкой)  ячейка (не может быть статичной) POINT.y - строка
   bool select; // залита ли текущая ячейка
   std::vector<int> colW; // набор широт колонок
   std::vector<int> rowH; // набор высот строк
   std::vector<std::vector<TsTableData*> > data; // [колонка][строка]
   std::vector<bool> image;
   //mutable bool wm_size;
protected:
   void Cls_OnPaint      (HWND) const;                  // WM_PAINT
   void Cls_OnLButtonDown(HWND, BOOL, int, int, UINT);  // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
   void Cls_OnRButtonDown(HWND, BOOL, int, int, UINT);  // WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
   void Cls_OnLButtonUp  (HWND, int, int, UINT);        // WM_LBUTTONUP
   void Cls_OnMouseMove  (HWND, int, int, UINT);        // WM_MOUSEMOVE
   void Cls_OnSize       (HWND, UINT, int, int);        // WM_SIZE
   void Cls_OnHScroll    (HWND, HWND, UINT, int);       // WM_HSCROLL
   void Cls_OnVScroll    (HWND, HWND, UINT, int);       // WM_VSCROLL
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   BOOL Cls_OnEraseBkgnd (HWND, HDC);                   // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW
   void Cls_OnKey        (HWND, UINT, BOOL, int, UINT); // WM_KEYDOWN & WM_KEYUP
   friend LRESULT CALLBACK nsControlTable::WndProc(HWND, UINT, WPARAM, LPARAM);
   POINT FindCell(const int xWin, const int yWin) const;
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   void DrawHScroll() const;
   void DrawVScroll() const;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   void RecountScrollInfo();
   void SetVisibleCell(const int indexCol, const int indexRow); // сделать ячейку видимой (отображаемой) на экране
   bool    VisibleCell(const int indexCol, const int indexRow) const; // видимa ли ячейка? (отображается на экране?)
public:
   int Width (const int col) const; // ширина первых col колонок
   int Height(const int row) const; // высота первых row строк
   int  GetColNumber() const {return colW.size();}; // узнать число колонок
   int  GetRowNumber() const {return rowH.size();}; // узнать число строк
   void SetColNumber(const int nCol); // установить заданное число колонок
   void SetRowNumber(const int nRow); // установить заданное число строк
   void SetColWidth (const int indexCol, const int width); // установить ширину заданной колонки
   void SetRowHeight(const int indexRow, const int height); // установить высоту заданной строки
   TcTable();
  ~TcTable();
   void Create(const HWND hParent, const int id);
   HWND    GetHandle() const {return hWnd;}
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   void    SetColor(const bool& setColorToAll, const COLORREF& setColorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   void    SetText       (const int indexCol, const int indexRow, LPCTSTR);
   void    SetFormatText (const int indexCol, const int indexRow, UINT uFormatText);
   void    SetImage      (const int indexCol, const int indexRow, const TcImage*);
   void    SetCurrentCell(const int indexCol, const int indexRow, const bool isSelect = false);
   POINT   GetCurrentCell() const {return currentCell;};
   LPCTSTR GetCurrentText() const;
   int  GetDefaultWidth () const {return defW;};
   int  GetDefaultHeight() const {return defH;};
   void SetDefaultWidth (const int newDefW) {defW = newDefW;};
   void SetDefaultHeight(const int newDefH) {defH = newDefH;};
   void UnselectCell();
};

#endif // FILE_TCTABLE
