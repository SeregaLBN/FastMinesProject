////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTable.cpp"
////////////////////////////////////////////////////////////////////////////////
#include ".\TcTable.h"
#include <windowsx.h>
#include "..\Lib.h"
#include "..\Dialog\Info.h"
#include "..\EraseBk.h"

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   #define IFTOALL *pToAll ? *pColorBk :
#else
   #define IFTOALL
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

namespace nsControlTable {
   const TCHAR szCClassWnd[] = TEXT("ClassWndFastMinesTable");
   const POINT CCellInvalid = {-1,-1};

   LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
      TcTable * const pGrid = (TcTable*)GetWindowLong(hwnd, GWL_USERDATA);
      if (pGrid)
         switch(msg){
         case WM_GETDLGCODE:
            //nsInfo::AddValue("msg WM_GETDLGCODE: keyState = 0x", keyState, 2);
            if (GetKeyState(VK_SPACE) & 0x80000000) {
               static int count = 0;
               if (++count == 10) FORWARD_WM_KEYUP(hwnd, VK_SPACE, count = 0, 0, SendMessage);
            }
            if (GetKeyState(VK_UP   ) & 0x80000000) {
               static int count = 0;
               if (++count == 10) FORWARD_WM_KEYUP(hwnd, VK_UP   , count = 0, 0, SendMessage);
            }
            if (GetKeyState(VK_DOWN ) & 0x80000000) {
               static int count = 0;
               if (++count == 10) FORWARD_WM_KEYUP(hwnd, VK_DOWN , count = 0, 0, SendMessage);
            }
            if (GetKeyState(VK_LEFT ) & 0x80000000) {
               static int count = 0;
               if (++count == 10) FORWARD_WM_KEYUP(hwnd, VK_LEFT , count = 0, 0, SendMessage);
            }
            if (GetKeyState(VK_RIGHT) & 0x80000000) {
               static int count = 0;
               if (++count == 10) FORWARD_WM_KEYUP(hwnd, VK_RIGHT, count = 0, 0, SendMessage);
            }
            break;
#ifdef REPLACEBKCOLORFROMFILLWINDOW
         HANDLE_MSG(hwnd, WM_ERASEBKGND   , pGrid->Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
         HANDLE_MSG(hwnd, WM_PAINT        , pGrid->Cls_OnPaint);
         HANDLE_MSG(hwnd, WM_MOUSEMOVE    , pGrid->Cls_OnMouseMove);
         HANDLE_MSG(hwnd, WM_LBUTTONDOWN  , pGrid->Cls_OnLButtonDown);
         HANDLE_MSG(hwnd, WM_LBUTTONDBLCLK, pGrid->Cls_OnLButtonDown);
         HANDLE_MSG(hwnd, WM_RBUTTONDOWN  , pGrid->Cls_OnRButtonDown);
         HANDLE_MSG(hwnd, WM_LBUTTONUP    , pGrid->Cls_OnLButtonUp);
         HANDLE_MSG(hwnd, WM_SIZE         , pGrid->Cls_OnSize);
         HANDLE_MSG(hwnd, WM_HSCROLL      , pGrid->Cls_OnHScroll);
         HANDLE_MSG(hwnd, WM_VSCROLL      , pGrid->Cls_OnVScroll);
         HANDLE_MSG(hwnd, WM_KEYUP        , pGrid->Cls_OnKey);
       //HANDLE_WM_CTLCOLOR(hwnd);
/*
         case WM_USER+1: // ~ WM_SIZE
            {
               if (pGrid->wm_size) {
                  PostMessage(hwnd, WM_USER, wParam, lParam);
                  break;
               }
               const int cx = wParam;
               const int cy = lParam;
               static POINT oldSize = {0,0};
               const RECT invalidateRect1 = {min(oldSize.x, cx), 0                 , cx, cy};
               const RECT invalidateRect2 = {0                 , min(oldSize.y, cy), cx, cy};
               InvalidateRect(hwnd, &invalidateRect1, FALSE);
               InvalidateRect(hwnd, &invalidateRect2, FALSE);
               oldSize.x = cx;
               oldSize.y = cy;
            }
            break;
/**/
         }
      return DefWindowProc(hwnd, msg, wParam, lParam);
   }
} // namespace nsControlTable

TcTable::~TcTable() {
   DestroyWindow(hWnd);
   for (int i=0; i<colW.size(); i++)
      for (int j=0; j<rowH.size(); j++)
         delete data[i][j];
}

TcTable::TcTable():
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pToAll      (NULL),
   pColorBk    (NULL),
#endif // REPLACEBKCOLORFROMFILLWINDOW
   hWnd        (NULL),
   hWndParent  (NULL),
   idControl   (0),
   defH        (27), // высота подобрана так, чтобы рисунок робота отображался без искажений
   defW        (100),
   currentCell (nsControlTable::CCellInvalid),
   select      (false),
   staticCol   (0),
   staticRow   (1)//,
 //wm_size     (false)
{
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   { static bool     garbage = false   ; pToAll   = &garbage;}
   { static COLORREF garbage = 0xFFFFFF; pColorBk = &garbage;}
#endif // REPLACEBKCOLORFROMFILLWINDOW
   RegClass(
      CS_DBLCLKS,                      // UINT    style
      nsControlTable::WndProc,         // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      nsControlTable::szCClassWnd      // LPCTSTR lpszClassName
   );
}

void TcTable::Create(const HWND hParent, const int id) {
   hWndParent = hParent;
   idControl = id;
   hWnd = CreateWindowEx(
      WS_EX_CLIENTEDGE,
      nsControlTable::szCClassWnd, NULL,
      WS_GROUP | WS_CHILD | WS_VISIBLE | WS_TABSTOP | WS_VSCROLL | WS_HSCROLL,// | WS_DLGFRAME,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hWndParent, HMENU(id), ghInstance, NULL);
   SetWindowLong(hWnd, GWL_USERDATA, (LONG)this);
   SetRowNumber(11);
   SetColNumber(6);

   RECT clientRect; GetClientRect(hWnd, &clientRect);
   const SCROLLINFO scInfoHorz = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0, Width(colW.size())+1,
      min(clientRect.right-clientRect.left, Width(colW.size())),
      0, 0
   };
   SetScrollInfo(hWnd, SB_HORZ, &scInfoHorz, TRUE);
   const SCROLLINFO scInfoVert = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0, Height(rowH.size())+1,
      min(clientRect.bottom-clientRect.top, Height(rowH.size())),
      0, 0
   };
   SetScrollInfo(hWnd, SB_VERT, &scInfoVert, TRUE);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
void TcTable::SetColor(const bool& setColorToAll, const COLORREF& setColorBk) {
   pToAll   = &setColorToAll;
   pColorBk = &setColorBk;
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

void TcTable::SetColNumber(const int nCol) {
   if (nCol < 0) return;
   const int oldCol = colW.size();
   if (nCol == oldCol) return;
   { // таблица уменьшается - удалить данные
      for (int i=nCol; i<oldCol; i++)
         for (int j=0; j<rowH.size(); j++)
            delete data[i][j];
   }
   const int oldMaxW = Width(oldCol);
   { // изменить размер таблицы
      colW.resize(nCol);
      data.resize(nCol);
      for (int i=oldCol; i<nCol; i++) {
         colW[i] = defW;
         data[i].resize(rowH.size());
      }
   }
   { // таблица расширяется - добавить данные
      for (int i=oldCol; i<nCol; i++)
         for (int j=0; j<rowH.size(); j++)
            data[i][j] = new TsTableData;
   }

   if (currentCell.x > nCol-1) SetCurrentCell(nCol-1, currentCell.y, select);

   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(hWnd, SB_HORZ)+min(oldMaxW, Width(colW.size())),
                                -GetScrollPos(hWnd, SB_VERT),
                                -GetScrollPos(hWnd, SB_HORZ)+max(oldMaxW, Width(colW.size()))+1,
                                -GetScrollPos(hWnd, SB_VERT)+Height(rowH.size())+1};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetRowNumber(const int nRow) {
   if (nRow < 0) return;
   const int oldRow = rowH.size();
   if (nRow == oldRow) return;
   { // таблица уменьшается - удалить данные
      for (int i=0; i<colW.size(); i++)
         for (int j=nRow; j<oldRow; j++)
            delete data[i][j];
   }
   const int oldMaxH = Height(oldRow);
   { // изменить размер таблицы
      rowH.resize(nRow);
      for (int i=0; i<colW.size(); i++)
         data[i].resize(nRow);
      for (int j=oldRow; j<nRow; j++)
         rowH[j] = defH;
   }
   { // таблица расширяется - добавить данные
      for (int i=0; i<colW.size(); i++)
         for (int j=oldRow; j<nRow; j++)
            data[i][j] = new TsTableData;
   }

   if (currentCell.y > nRow-1) SetCurrentCell(currentCell.x, nRow-1, select);

   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(hWnd, SB_HORZ),
                                -GetScrollPos(hWnd, SB_VERT)+min(oldMaxH, Height(rowH.size())),
                                -GetScrollPos(hWnd, SB_HORZ)+Width(colW.size())+1,
                                -GetScrollPos(hWnd, SB_VERT)+max(oldMaxH, Height(rowH.size()))+1};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetColWidth(const int indexCol, const int width) {
   if ((indexCol < 0) || (indexCol >= colW.size())) return;
   const int oldMaxW = Width(colW.size());
   colW[indexCol] = width;
   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(hWnd, SB_HORZ)+Width(indexCol),
                                -GetScrollPos(hWnd, SB_VERT),
                                -GetScrollPos(hWnd, SB_HORZ)+max(oldMaxW, Width(colW.size()))+1,
                                -GetScrollPos(hWnd, SB_VERT)+Height(rowH.size())+1};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetRowHeight(const int indexRow, const int height) {
   if ((indexRow < 0) || (indexRow >= rowH.size())) return;
   const int oldMaxH = Height(rowH.size());
   rowH[indexRow] = height;
   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(hWnd, SB_HORZ),
                                -GetScrollPos(hWnd, SB_VERT)+Height(indexRow),
                                -GetScrollPos(hWnd, SB_HORZ)+Width(colW.size())+1,
                                -GetScrollPos(hWnd, SB_VERT)+max(oldMaxH, Height(rowH.size()))+1};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

inline int TcTable::Width(const int col) const { // ширина первых col колонок
   int result = 0;
   for (int i=0; i<min(colW.size(), col); i++)
      result += colW[i];
   return result;
}

inline int TcTable::Height(const int row) const { // высота первых row строк
   int result = 0;
   for (int j=0; j<min(rowH.size(), row); j++)
      result += rowH[j];
   return result;
}

void TcTable::SetText(const int indexCol, const int indexRow, LPCTSTR str) {
   if ((indexCol < 0) || (indexCol >= colW.size()) ||
       (indexRow < 0) || (indexRow >= rowH.size())) return;
   data[indexCol][indexRow]->szText.assign(str);
   const x = (indexCol < staticCol) ? 0 : GetScrollPos(hWnd, SB_HORZ);
   const y = (indexRow < staticRow) ? 0 : GetScrollPos(hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetFormatText(const int indexCol, const int indexRow, UINT uFormatText) {
   if ((indexCol < 0) || (indexCol >= colW.size()) ||
       (indexRow < 0) || (indexRow >= rowH.size())) return;
   data[indexCol][indexRow]->uFormatText = uFormatText;
   const x = (indexCol < staticCol) ? 0 : GetScrollPos(hWnd, SB_HORZ);
   const y = (indexRow < staticRow) ? 0 : GetScrollPos(hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetImage(const int indexCol, const int indexRow, const TcImage* pImage) {
   if ((indexCol < 0) || (indexCol >= colW.size()) ||
       (indexRow < 0) || (indexRow >= rowH.size())) return;
   data[indexCol][indexRow]->pImage = pImage;
   const x = (indexCol < staticCol) ? 0 : GetScrollPos(hWnd, SB_HORZ);
   const y = (indexRow < staticRow) ? 0 : GetScrollPos(hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

void TcTable::SetCurrentCell(const int indexCol, const int indexRow, const bool isSelect) {
   if ((indexCol < staticCol) || (indexCol >= colW.size()) ||
       (indexRow < staticRow) || (indexRow >= rowH.size())) return;
   const POINT newCell = {indexCol, indexRow};
   if ((currentCell  == newCell) && (select == isSelect)) return;
   const RECT invalidateRectOld = {Width (currentCell.x  ), Height(currentCell.y  ),
                                   Width (currentCell.x+1), Height(currentCell.y+1)};
   select = isSelect;
   currentCell = newCell;
   const RECT invalidateRectNew = {Width (currentCell.x  ), Height(currentCell.y  ),
                                   Width (currentCell.x+1), Height(currentCell.y+1)};
   const RECT invalidateRect = {min(invalidateRectOld.left  ,invalidateRectNew.left  )-GetScrollPos(hWnd, SB_HORZ),
                                min(invalidateRectOld.top   ,invalidateRectNew.top   )-GetScrollPos(hWnd, SB_VERT),
                                max(invalidateRectOld.right ,invalidateRectNew.right )-GetScrollPos(hWnd, SB_HORZ),
                                max(invalidateRectOld.bottom,invalidateRectNew.bottom)-GetScrollPos(hWnd, SB_VERT)};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
   SetVisibleCell(indexCol, indexRow);
}

LPCTSTR TcTable::GetCurrentText() const {
   if (currentCell == nsControlTable::CCellInvalid) {
      static const TCHAR szNull[] = TEXT("");
      return szNull;
   }
   return data[currentCell.x][currentCell.y]->szText.c_str();
}

void TcTable::UnselectCell() {
   if (currentCell == nsControlTable::CCellInvalid) return;
   const RECT invalidateRect = {Width (currentCell.x  ), Height(currentCell.y  ),
                                Width (currentCell.x+1), Height(currentCell.y+1)};
   currentCell = nsControlTable::CCellInvalid;
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

POINT TcTable::FindCell(const int xWin, const int yWin) const {
   const int x = xWin+GetScrollPos(hWnd, SB_HORZ);
   const int y = yWin+GetScrollPos(hWnd, SB_VERT);
   POINT resultCell = nsControlTable::CCellInvalid;
   int result = 0;
   for (int i=0; i<colW.size(); i++) {
      if ((x >= result) && (x < result+colW[i])) {
         resultCell.x = i;
         break;
      }
      result += colW[i];
   }
   result = 0;
   for (i=0; i<rowH.size(); i++) {
      if ((y >= result) && (y < result+rowH[i])) {
         resultCell.y = i;
         break;
      }
      result += rowH[i];
   }
   return ((resultCell.x == nsControlTable::CCellInvalid.x) ||
           (resultCell.y == nsControlTable::CCellInvalid.y)) ?
           nsControlTable::CCellInvalid :
           resultCell;
}


#ifdef REPLACEBKCOLORFROMFILLWINDOW
inline void TcTable::DrawHScroll() const {
   if (!*pToAll) return;
   RECT wndRect; GetWindowRect(hWnd, &wndRect);
   const RECT scrollRect = {
      +GetSystemMetrics(SM_CXBORDER)+wndRect.left,
      -GetSystemMetrics(SM_CYBORDER)+wndRect.bottom-GetSystemMetrics(SM_CYHSCROLL),
      -GetSystemMetrics(SM_CXBORDER)+wndRect.right,
      -GetSystemMetrics(SM_CYBORDER)+wndRect.bottom};
   nsEraseBk::FillWnd(hWnd, *pColorBk, false, scrollRect);
}

inline void TcTable::DrawVScroll() const {
   if (!*pToAll) return;
   RECT wndRect; GetWindowRect(hWnd, &wndRect);
   const RECT scrollRect = {
      -GetSystemMetrics(SM_CXBORDER)+wndRect.right-GetSystemMetrics(SM_CXVSCROLL),
      +GetSystemMetrics(SM_CYBORDER)+wndRect.top,
      -GetSystemMetrics(SM_CXBORDER)+wndRect.right,
      -GetSystemMetrics(SM_CYBORDER)+wndRect.bottom};
   nsEraseBk::FillWnd(hWnd, *pColorBk, false, scrollRect);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

inline void TcTable::RecountScrollInfo() {
   RECT clientRect; GetClientRect(hWnd, &clientRect);
   const int x = Width (colW.size());
   const int y = Height(rowH.size());
   const int h = GetScrollPos(hWnd, SB_HORZ);
   const int v = GetScrollPos(hWnd, SB_VERT);
   const SCROLLINFO scInfoHorz = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0,  x-1,
      min(x,     clientRect.right),
      min(x, min(clientRect.right, h)),
   };
   SetScrollInfo(hWnd, SB_HORZ, &scInfoHorz, TRUE);
   static bool oldShowSBHorz = (clientRect.right < x);
          bool newShowSBHorz = (clientRect.right < x);
   if (oldShowSBHorz != newShowSBHorz) {
      oldShowSBHorz = newShowSBHorz;
      ShowScrollBar(hWnd, SB_HORZ, newShowSBHorz); // изменяю размер клиентской области на экране, а это значит...
      RecountScrollInfo();                         // ... что надо заново пересчитать скролинг
   }
   //EnableScrollBar(hWnd, SB_HORZ, (clientRect.right < x) ? ESB_ENABLE_BOTH : ESB_DISABLE_BOTH);
   const SCROLLINFO scInfoVert = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0,  y-1,
      min(y,     clientRect.bottom),
      min(y, min(clientRect.bottom, v)),
   };
   SetScrollInfo(hWnd, SB_VERT, &scInfoVert, TRUE);
   static bool oldShowSBVert = (clientRect.bottom < y);
          bool newShowSBVert = (clientRect.bottom < y);
   if (oldShowSBVert != newShowSBVert) {
      oldShowSBVert = newShowSBVert;
      ShowScrollBar(hWnd, SB_VERT, newShowSBVert); // изменяю размер клиентской области на экране, а это значит...
      RecountScrollInfo();                         // ... что надо заново пересчитать скролинг
   }
   //EnableScrollBar(hWnd, SB_VERT, (clientRect.bottom < y) ? ESB_ENABLE_BOTH : ESB_DISABLE_BOTH);
}

bool TcTable::VisibleCell(const int indexCol, const int indexRow) const { // видимa ли ячейка? (отображается на экране?)
   if ((indexCol < 0) || (indexCol >= colW.size()) ||
       (indexRow < 0) || (indexRow >= rowH.size())) return false; // неверный индекс
   RECT clientRect; GetClientRect(hWnd, &clientRect);
   clientRect.left += Width (staticCol);
   clientRect.top  += Height(staticRow);
   if ((clientRect.left   <= Width (indexCol  )-GetScrollPos(hWnd, SB_HORZ)) &&
       (clientRect.right  >= Width (indexCol+1)-GetScrollPos(hWnd, SB_HORZ)) &&
       (clientRect.top    <= Height(indexRow  )-GetScrollPos(hWnd, SB_VERT)) &&
       (clientRect.bottom >= Height(indexRow+1)-GetScrollPos(hWnd, SB_VERT)))
      return true;
   else
      return false;
}

void TcTable::SetVisibleCell(const int indexCol, const int indexRow) { // сделать ячейку видимой (отображаемой) на экране
   if ((indexCol < 0) || (indexCol >= colW.size()) ||
       (indexRow < 0) || (indexRow >= rowH.size())) return;
   if (VisibleCell(indexCol, indexRow)) {
      return; // ячейка и так уже полностью видима
   }
   RECT clientRect; GetClientRect(hWnd, &clientRect);
   if(GetScrollPos(hWnd, SB_HORZ)<(Width (indexCol+1)-clientRect.right))
      SetScrollPos(hWnd, SB_HORZ,  Width (indexCol+1)-clientRect.right,  TRUE);
   if(GetScrollPos(hWnd, SB_VERT)<(Height(indexRow+1)-clientRect.bottom))
      SetScrollPos(hWnd, SB_VERT,  Height(indexRow+1)-clientRect.bottom, TRUE);
   if(GetScrollPos(hWnd, SB_HORZ)>(Width (indexCol  )-Width (staticCol)))
      SetScrollPos(hWnd, SB_HORZ,  Width (indexCol  )-Width (staticCol), TRUE);
   if(GetScrollPos(hWnd, SB_VERT)>(Height(indexRow  )-Height(staticRow)))
      SetScrollPos(hWnd, SB_VERT,  Height(indexRow  )-Height(staticRow), TRUE);

   InvalidateRect(hWnd, NULL, FALSE);
}

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
#define DrawCell { \
   const POINT cellPoint = {i,j}; \
   MoveToEx(hCDC, cellRect.left , cellRect.bottom-1, NULL); \
   LineTo  (hCDC, cellRect.left , cellRect.top); \
   LineTo  (hCDC, cellRect.right, cellRect.top); \
   RECT rectText = {cellRect.left+2+4, cellRect.top+2, cellRect.right-1, cellRect.bottom-1}; \
   if (data[i][j]->pImage) { \
      const int m = min(rectText.right-rectText.left, rectText.bottom-rectText.top); \
      const RECT rectImage = {rectText.left, rectText.top, rectText.left+m, rectText.top+m}; \
      data[i][j]->pImage->DrawImage(hCDC, &rectImage); \
      rectText.left += m+4; \
   } \
   if (data[i][j]->szText.size()) { \
      COLORREF oldTextColor; \
      if ((cellPoint == currentCell) && select) { \
         oldTextColor = SetTextColor(hCDC, GetSysColor(COLOR_HIGHLIGHTTEXT)); \
      } \
      DrawText(hCDC, data[i][j]->szText.c_str(), \
          -1, &rectText, data[i][j]->uFormatText); \
      if ((cellPoint == currentCell) && select) { \
         SetTextColor(hCDC, oldTextColor); \
      } \
   } \
}

#define DrawCellField { \
   if (cellPoint == currentCell) { \
      if (select) { \
         const HBRUSH  hBrushNew = CreateSolidBrush(GetSysColor(COLOR_HIGHLIGHT)); \
         const HGDIOBJ hBrushOld = SelectObject(hCDC, hBrushNew); \
         Rectangle(hCDC, cellRect.left, cellRect.top, cellRect.right+1, cellRect.bottom+1); \
         SelectObject(hCDC, hBrushOld); \
         DeleteObject(hBrushNew); \
      } else { \
         const HPEN    hPenNew = CreatePen(PS_DOT, 1, IFTOALL GetSysColor(COLOR_BTNFACE)); \
         const HGDIOBJ hPenOld = SelectObject(hCDC, hPenNew); \
         MoveToEx(hCDC, cellRect.left +1, cellRect.top   +1, NULL); \
         LineTo  (hCDC, cellRect.left +1, cellRect.bottom-1); \
         LineTo  (hCDC, cellRect.right-1, cellRect.bottom-1); \
         LineTo  (hCDC, cellRect.right-1, cellRect.top   +1); \
         LineTo  (hCDC, cellRect.left +1, cellRect.top   +1); \
         SelectObject(hCDC, hPenOld); \
         DeleteObject(hPenNew); \
      } \
   } \
   DrawCell; \
}

#define DrawCellCaption { \
   Rectangle(hCDC, cellRect.left, cellRect.top, cellRect.right+1, cellRect.bottom+1); \
   { \
      const HPEN hPenB = CreatePen(PS_SOLID, 1, 0); \
      const HPEN hPenW = CreatePen(PS_SOLID, 1, 0xFFFFFF); \
      const HGDIOBJ hPenOld = SelectObject(hCDC, hPenW); \
      MoveToEx(hCDC, cellRect.left +1, cellRect.bottom-1, NULL); \
      LineTo  (hCDC, cellRect.left +1, cellRect.top   +1); \
      LineTo  (hCDC, cellRect.right-1, cellRect.top   +1); \
      SelectObject(hCDC, hPenB); \
      LineTo  (hCDC, cellRect.right-1, cellRect.bottom-1); \
      LineTo  (hCDC, cellRect.left +1, cellRect.bottom-1); \
      SelectObject(hCDC, hPenOld); \
      DeleteObject(hPenB); \
      DeleteObject(hPenW); \
   } \
   DrawCell;\
}

void TcTable::Cls_OnPaint(HWND hwnd) const {
/*   if (wm_size) {
      wm_size = false;
      PAINTSTRUCT PaintStruct;
      BeginPaint(hwnd, &PaintStruct);
      PaintStruct.rcPaint.left   =
      PaintStruct.rcPaint.top    =
      PaintStruct.rcPaint.right  =
      PaintStruct.rcPaint.bottom = 0;
      EndPaint  (hwnd, &PaintStruct);
      return;
   }
/**/
   PAINTSTRUCT PaintStruct;
   const HDC hODC = BeginPaint(hwnd, &PaintStruct); // original DC
   const HDC hCDC = CreateCompatibleDC(hODC);
   RECT clientRect; GetClientRect(hwnd, &clientRect);
   const HBITMAP hBmpNew = CreateCompatibleBitmap(hODC, clientRect.right, clientRect.bottom);
   const HGDIOBJ hBmpOld = SelectObject(hCDC, hBmpNew);
   const x = GetScrollPos(hwnd, SB_HORZ);
   const y = GetScrollPos(hwnd, SB_VERT);
   { // фон
      const HBRUSH hBrushNew = CreateSolidBrush(GetSysColor(COLOR_WINDOW));
      const HGDIOBJ hBrushOld = SelectObject(hCDC, hBrushNew);
      PatBlt(hCDC, 0,0, clientRect.right, clientRect.bottom, PATCOPY);
      SelectObject(hCDC, hBrushOld);
      DeleteObject(hBrushNew);
   }
   { // вывод данных на ...
      const HPEN hPenNew = CreatePen(PS_SOLID, 1, IFTOALL GetSysColor(COLOR_BTNFACE));
      const HGDIOBJ hPenOld = SelectObject(hCDC, hPenNew);
      const int oldBkMode = SetBkMode(hCDC, TRANSPARENT); // вывод текста на прозрачном фоне
      const LOGFONT Font = {8,0,0,0,
                            FW_NORMAL,//FW_BOLD,//
                            0,0,0,
                            DEFAULT_CHARSET,
                            OUT_DEFAULT_PRECIS,
                            CLIP_DEFAULT_PRECIS,
                            DEFAULT_QUALITY,
                            DEFAULT_PITCH | FF_DONTCARE,
                            TEXT("MS Sans Serif")};// TEXT("Times New Roman")
      const HFONT hFont = CreateFontIndirect(&Font);
      const HGDIOBJ hFontOld = SelectObject(hCDC, hFont);
      { // ... на нестaтичном поле
         for (int i=staticCol; i<colW.size(); i++)
            for (int j=staticRow; j<rowH.size(); j++) {
               const POINT cellPoint = {i,j};
               const RECT cellRect = {-x+Width(i),-y+Height(j),-x+Width(i+1),-y+Height(j+1)};
               DrawCellField;
         }
      }
      { // ... на стaтичном поле
         const HBRUSH  hBrushNew = CreateSolidBrush(IFTOALL GetSysColor(COLOR_BTNFACE));
         const HGDIOBJ hBrushOld = SelectObject(hCDC, hBrushNew);
         { // верхний заголовок
            for (int i=staticCol; i<colW.size(); i++)
               for (int j=0; j<min(staticRow, rowH.size()); j++) {
                  const RECT cellRect = {-x+Width(i),Height(j),-x+Width(i+1),Height(j+1)};
                  DrawCellCaption;
               }
         }
         { // левый заголовок
            for (int i=0; i<min(staticCol, colW.size()); i++)
               for (int j=staticRow; j<rowH.size(); j++) {
                  const RECT cellRect = {Width(i),-y+Height(j),Width(i+1),-y+Height(j+1)};
                  DrawCellCaption;
               }
         }
         { // верхний левый заголовок
            for (int i=0; i<min(staticCol, colW.size()); i++)
               for (int j=0; j<min(staticRow, rowH.size()); j++) {
                  const RECT cellRect = {Width(i),Height(j),Width(i+1),Height(j+1)};
                  DrawCellCaption;
               }
         }/**/
         SelectObject(hCDC, hBrushOld);
         DeleteObject(hBrushNew);
      }
      MoveToEx(hCDC, -x+Width(colW.size()), -y+0, NULL);
      LineTo  (hCDC, -x+Width(colW.size()), -y+Height(rowH.size()));
      LineTo  (hCDC, -x-1                 , -y+Height(rowH.size()));
      SelectObject(hCDC, hPenOld);
      DeleteObject(hPenNew);
      SetBkMode(hCDC, oldBkMode);
      SelectObject(hCDC, hFontOld);
      DeleteObject(hFont);
   }
   BitBlt(hODC, PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.top,
                PaintStruct.rcPaint.right -PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.bottom-PaintStruct.rcPaint.top,
          hCDC, PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.top,
          SRCCOPY);
   SelectObject(hCDC, hBmpOld);
   DeleteObject(hBmpNew);
   DeleteDC(hCDC);
   EndPaint(hwnd, &PaintStruct);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   DrawHScroll();
   DrawVScroll();
#endif // REPLACEBKCOLORFROMFILLWINDOW
}

// WM_MOUSEMOVE
void TcTable::Cls_OnMouseMove(HWND hwnd, int x, int y, UINT keyFlags) {
   if (!(keyFlags & MK_LBUTTON)) return;
   const POINT cell = FindCell(x, y);
   if (cell == nsControlTable::CCellInvalid) return;
   SetCurrentCell(cell.x, cell.y);
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void TcTable::Cls_OnLButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetFocus(hWnd); 
   SetCapture(hWnd);
   const POINT cell = FindCell(x, y);
   if (cell == nsControlTable::CCellInvalid) return;
   SetCurrentCell(cell.x, cell.y, !!fDoubleClick);
   if (fDoubleClick)
      FORWARD_WM_COMMAND(hWndParent, idControl, hWnd, NC_TABLE_SELECT_CELL, PostMessage);
}

// WM_LBUTTONUP
void TcTable::Cls_OnLButtonUp(HWND hwnd, int x, int y, UINT keyFlags) {
   ReleaseCapture();
}

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void TcTable::Cls_OnRButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetFocus(hWnd); 
}

// WM_SIZE
void TcTable::Cls_OnSize(HWND hwnd, UINT state, int cx, int cy) {
   RecountScrollInfo();
//   InvalidateRect(hWnd, NULL, FALSE);
 //wm_size = true;
 //PostMessage(hwnd, WM_USER+1, cx, cy);
}

// WM_HSCROLL
void TcTable::Cls_OnHScroll(HWND hwnd, HWND hwndCtl, UINT code, int pos) {
   SCROLLINFO scInfo = {sizeof(SCROLLINFO), SIF_RANGE | SIF_PAGE | SIF_POS};
   GetScrollInfo(hwnd, SB_HORZ, &scInfo);
   switch (code) {
   case SB_TOP          : scInfo.nPos = scInfo.nMin; break;
   case SB_BOTTOM       : scInfo.nPos = scInfo.nMax; break;
   case SB_LINELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage/10); break;
   case SB_LINERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage/10); break;
   case SB_PAGELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage); break;
   case SB_PAGERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage); break;
   case SB_THUMBPOSITION:
   case SB_THUMBTRACK   : scInfo.nPos = pos; break;
   case SB_ENDSCROLL    : break;
   }
   scInfo.fMask = SIF_POS;
   SetScrollInfo(hwnd, SB_HORZ, &scInfo, TRUE);
   RECT clientRect; GetClientRect(hwnd, &clientRect);
   const RECT invalidateRect = {Width(staticCol), 0, clientRect.right, clientRect.bottom};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

// WM_VSCROLL
void TcTable::Cls_OnVScroll(HWND hwnd, HWND hwndCtl, UINT code, int pos) {
   SCROLLINFO scInfo = {sizeof(SCROLLINFO), SIF_RANGE | SIF_PAGE | SIF_POS};
   GetScrollInfo(hwnd, SB_VERT, &scInfo);
   switch (code) {
   case SB_TOP          : scInfo.nPos = scInfo.nMin; break;
   case SB_BOTTOM       : scInfo.nPos = scInfo.nMax; break;
   case SB_LINELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage/10); break;
   case SB_LINERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage/10); break;
   case SB_PAGELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage); break;
   case SB_PAGERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage); break;
   case SB_THUMBPOSITION:
   case SB_THUMBTRACK   : scInfo.nPos = pos; break;
   case SB_ENDSCROLL    : break;
   }
   scInfo.fMask = SIF_POS;
   SetScrollInfo(hwnd, SB_VERT, &scInfo, TRUE);
   RECT clientRect; GetClientRect(hwnd, &clientRect);
   const RECT invalidateRect = {0, Height(staticRow), clientRect.right, clientRect.bottom};
   InvalidateRect(hWnd, &invalidateRect, FALSE);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_ERASEBKGND
BOOL TcTable::Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   return TRUE;
   if (*pToAll) return FALSE;
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, *pColorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_KEYDOWN & WM_KEYUP
void TcTable::Cls_OnKey(HWND hwnd, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   //nsInfo::AddValue("vk = 0x", vk, 16);
   if (fDown) return;
   switch (vk) {
   case VK_LEFT  : SetCurrentCell(currentCell.x-1, currentCell.y  ); break;
   case VK_UP    : SetCurrentCell(currentCell.x  , currentCell.y-1); break;
   case VK_RIGHT : SetCurrentCell(currentCell.x+1, currentCell.y  ); break;
   case VK_DOWN  : SetCurrentCell(currentCell.x  , currentCell.y+1); break;
   case VK_HOME  : SetCurrentCell(currentCell.x  , staticRow      ); break;
   case VK_END   : SetCurrentCell(currentCell.x  , rowH.size()  -1); break;
   case VK_PRIOR : SetCurrentCell(currentCell.x  , max(staticRow    , currentCell.y-7)); break;
   case VK_NEXT  : SetCurrentCell(currentCell.x  , min(rowH.size()-1, currentCell.y+7)); break;
   case VK_SPACE : SetCurrentCell(currentCell.x  , currentCell.y, !select); break;
   case VK_DELETE: FORWARD_WM_COMMAND(hWndParent, idControl, hWnd, NC_TABLE_DELETE_CELL, PostMessage); break;
   }
}
