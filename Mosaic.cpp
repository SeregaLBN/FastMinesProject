////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Mosaic.cpp"
//
// реализация алгоритма
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Mosaic.h"
#include <WindowsX.h>
#include "WinDefAdd.h"
#include "Image.h"
#include "CommonLib.h"
#include "Lang.h"
#include "./Mosaic/Triangle1.h"
#include "./Mosaic/Triangle2.h"
#include "./Mosaic/Triangle3.h"
#include "./Mosaic/Triangle4.h"
#include "./Mosaic/Square1.h"
#include "./Mosaic/Square2.h"
#include "./Mosaic/Parquet1.h"
#include "./Mosaic/Parquet2.h"
#include "./Mosaic/Trapezoid1.h"
#include "./Mosaic/Trapezoid2.h"
#include "./Mosaic/Trapezoid3.h"
#include "./Mosaic/Rhombus1.h"
#include "./Mosaic/Quadrangle1.h"
#include "./Mosaic/PentagonT24.h"
#include "./Mosaic/PentagonT5.h"
#include "./Mosaic/PentagonT10.h"
#include "./Mosaic/Hexagon1.h"
#include "./Mosaic/TrSq1.h"
#include "./Mosaic/TrSq2.h"
#include "./Mosaic/SqTrHex.h"
#ifdef _DEBUG
   #include "Logger.h"
#endif

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                                   other
////////////////////////////////////////////////////////////////////////////////
const TCHAR nsMosaic::CMosaic::SZ_CLASS_WND[] = TEXT("ClassWndFastMinesMosaic");

float nsMosaic::GetPercentMine(const ESkillLevel skill, const EMosaic mosaic) { // процент мин на заданном уровне сложности
   switch (mosaic) {
#define PERCENT(A, B, C, D)  switch (skill) { case skillLevelBeginner: return A; case skillLevelAmateur: return B; case skillLevelProfessional: return C; case skillLevelCrazy: return D; }
   case mosaicTriangle1  : PERCENT(16.5f  , 19.5f  , 22.5f  , 26.5f  );
   case mosaicTriangle2  : PERCENT(15.f   , 18.f   , 21.f   , 25.f   );
   case mosaicTriangle3  : PERCENT(17.143f, 20.143f, 23.143f, 27.143f);
   case mosaicTriangle4  : PERCENT(19.762f, 22.762f, 25.762f, 29.762f);
   case mosaicSquare1    : PERCENT(15.f   , 18.f   , 21.f   , 25.f   );
   case mosaicSquare2    : PERCENT(14.f   , 17.f   , 20.f   , 24.f   );
   case mosaicParquet1   : PERCENT(14.f   , 17.f   , 20.f   , 24.f   );
   case mosaicParquet2   : PERCENT(14.429f, 17.429f, 20.429f, 24.429f);
   case mosaicTrapezoid1 : PERCENT(14.5f  , 17.5f  , 20.5f  , 24.5f  );
   case mosaicTrapezoid2 : PERCENT(15.222f, 18.222f, 21.222f, 25.222f);
   case mosaicTrapezoid3 : PERCENT(16.015f, 19.015f, 22.015f, 26.015f);
   case mosaicRhombus1   : PERCENT(15.6f  , 18.6f  , 21.6f  , 25.6f  );
   case mosaicQuadrangle1: PERCENT(15.222f, 18.222f, 21.222f, 25.222f);
   case mosaicPentagonT24: PERCENT(14.429f, 17.429f, 20.429f, 24.429f);
   case mosaicPentagonT5 : PERCENT(14.5f  , 17.5f  , 20.5f  , 24.5f  );
   case mosaicPentagonT10: PERCENT(14.151f, 17.151f, 20.151f, 24.151f);
   case mosaicHexagon1   : PERCENT(14.f   , 17.f   , 20.f   , 24.f   );
   case mosaicTrSq1      : PERCENT(15.852f, 18.852f, 21.852f, 25.852f);
   case mosaicTrSq2      : PERCENT(15.809f, 18.809f, 21.809f, 25.809f);
   case mosaicSqTrHex    : PERCENT(15.5f  , 18.5f  , 21.5f  , 25.5f  );
   default /*mosaicNil*/ : return 1;
   }
#undef PERCENT
}

int nsMosaic::DefineNumberMines(ESkillLevel skill, EMosaic mosaic, const COORD& sizeMosaic) {
   if (skill == skillLevelCustom)
      return 0; // error
   return sizeMosaic.X *
          sizeMosaic.Y * GetPercentMine(skill, mosaic) / 100;
}

void nsMosaic::LoadDefaultImageMine   (HINSTANCE hInstance, CImage &ImgMine   ) {ImgMine   .LoadResource(hInstance , TEXT("Mine" ), imageBitmap     ); ImgMine   .SetTransparent(true); ImgMine   .SetPlace(placeStretch);}
void nsMosaic::LoadDefaultImageFlag   (HINSTANCE hInstance, CImage &ImgFlag   ) {ImgFlag   .LoadResource(hInstance , TEXT("Flag" ), imageBitmap     ); ImgFlag   .SetTransparent(true); ImgFlag   .SetPlace(placeStretch);}
void nsMosaic::LoadDefaultImagePause  (HINSTANCE hInstance, CImage &ImgPause  ) {ImgPause  .LoadResource(hInstance , TEXT("Pause"), imageEnhMetafile); ImgPause  .SetTransparent(true); ImgPause  .SetPlace(placeCenter );}
void nsMosaic::LoadDefaultImageBckgrnd(HINSTANCE hInstance, CImage &ImgBckgrnd) {ImgBckgrnd.Reset();                                                   ImgBckgrnd.SetTransparent(true); ImgBckgrnd.SetPlace(placeStretch);}

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
LRESULT nsMosaic::CMosaic::WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   //g_Logger.PutMsg(CLogger::LL_DEBUG, TEXT(""), msg);
   CMosaic *const This = (CMosaic *)GetWindowLong(hwnd, GWL_USERDATA);
   if (This) {
      switch(msg){
      HANDLE_MSG(hwnd, WM_PAINT        , This->OnPaint);
      HANDLE_MSG(hwnd, WM_LBUTTONUP    , This->OnLButtonUp);
      HANDLE_MSG(hwnd, WM_LBUTTONDOWN  , This->OnLButtonDown);
      HANDLE_MSG(hwnd, WM_LBUTTONDBLCLK, This->OnLButtonDown);
      HANDLE_MSG(hwnd, WM_RBUTTONDOWN  , This->OnRButtonDown);
      HANDLE_MSG(hwnd, WM_RBUTTONDBLCLK, This->OnRButtonDown);
      HANDLE_MSG(hwnd, WM_SIZE         , This->OnSize);
      case WM_ERASEBKGND: return -1;
      }
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

// constructor
nsMosaic::CMosaic::CMosaic():
   m_hWndParent(NULL),
   m_idControl (0),
   m_iOldMines (1)
{
   RegClass(
      CS_DBLCLKS,                      // UINT    style
      WndProc,                         // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  c_hInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      SZ_CLASS_WND                     // LPCTSTR lpszClassName
   );
}

BOOL nsMosaic::CMosaic::Create(HWND hWindowParent, int id) {
   m_hWndParent = hWindowParent;
   m_idControl  = id;

   m_hWnd = CreateWindow(SZ_CLASS_WND, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndParent, (HMENU)0, ghInstance, NULL);
   if (!m_hWnd) {
      return FALSE;
   }
   SetWindowLong(m_hWnd, GWL_USERDATA, (LONG)this);

   //
   m_GContext.m_hDCWnd   = GetWindowDC(m_hWnd);
   m_GContext.m_hDCDst   = CreateCompatibleDC(m_GContext.m_hDCWnd);//m_GContext.m_hDCWnd;//
   m_GContext.m_hDCBck   = CreateCompatibleDC(m_GContext.m_hDCWnd);
   m_GContext.m_hDCTmp   = CreateCompatibleDC(m_GContext.m_hDCWnd);
   m_GContext.m_hDCPause = CreateCompatibleDC(m_GContext.m_hDCWnd);
   SetBkMode (m_GContext.m_hDCTmp, TRANSPARENT); // вывод текста на прозрачном фоне
 //SetBkColor(m_GContext.m_hDCTmp, RGB(255, 255, 255));
   m_GContext.m_hPenShadow = NULL;
   m_GContext.m_hPenLight  = NULL;
   m_GContext.m_hBrush     = NULL;

   m_GContext.m_SizeBitmap.cx = m_GContext.m_SizeBitmap.cy = 0;

   // load default images
   LoadDefaultImageMine   (ghInstance, m_GContext.m_ImgMine   );
   LoadDefaultImageFlag   (ghInstance, m_GContext.m_ImgFlag   );
   LoadDefaultImagePause  (ghInstance, m_GContext.m_ImgPause  );
   LoadDefaultImageBckgrnd(ghInstance, m_GContext.m_ImgBckgrnd);

   SetClasureToMosaic();
   SetBorder(m_GContext.m_Border);
   SetBrush(m_GContext.m_colorBk);

   m_GameStatus = gsEnd;
   m_bPause = false;
   MosaicCreate();
   return TRUE;
}

// destructor
nsMosaic::CMosaic::~CMosaic(){
   MosaicDestroy(m_SerializeData.m_SizeMosaic);
   //m_Mosaic.~vector();
   DeleteDC(m_GContext.m_hDCDst);
   DeleteDC(m_GContext.m_hDCBck);
   DeleteDC(m_GContext.m_hDCTmp);
   ReleaseDC(m_hWnd, m_GContext.m_hDCWnd);
   DeleteObject(m_GContext.m_hBrush);
   DeleteObject(m_GContext.m_hPenShadow);
   DeleteObject(m_GContext.m_hPenLight);
   DeleteObject(m_GContext.m_hFont);
}

////////////////////////////////////////////////////////////////////////////////
//                     обработчики сообщений "минного поля"
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
void nsMosaic::CMosaic::OnPaint(HWND hwnd){
   {
      //static int qqq = 0;
      //if (!qqq++) return;
   }

   DefWindowProc(hwnd, WM_PAINT, 0L, 0L); // это чтобы не писать обработчик WM_PAINT как принято - BeginPaint ... EndPaint

   if (m_bPause) {
      if (!m_GContext.m_isRefreshPause) { // restore bitmap
         BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
                m_GContext.m_hDCPause, 0, 0, SRCCOPY);
         return;
      }
      PatBlt(m_GContext.m_hDCPause, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy, PATCOPY);
      RECT Rect = {0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy};
      SetCursor(LoadCursor(NULL, IDC_WAIT));
      m_GContext.m_ImgPause.DrawPlace(m_GContext.m_hDCPause, &Rect);
      SetCursor(LoadCursor(NULL, IDC_ARROW));
      BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
             m_GContext.m_hDCPause, 0, 0, SRCCOPY);
    //SelectObject(m_GContext.m_hDCWnd, m_GContext.m_hBrush); // востанавливаю фоновый цвет паузы
      m_GContext.m_isRefreshPause = false;
      return;
   }

   if (!m_GContext.m_isRefreshMosaic) { // restore bitmap
      BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
             m_GContext.m_hDCDst, 0, 0, SRCCOPY);
      return;
   }

   SetCursor(LoadCursor(NULL, IDC_WAIT));
   // background color
   PatBlt(m_GContext.m_hDCDst, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy, PATCOPY);
   PatBlt(m_GContext.m_hDCBck, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy, PATCOPY);
   // background image
   {
      RECT Rect = {0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy};
      m_GContext.m_ImgBckgrnd.DrawPlace(m_GContext.m_hDCBck, &Rect);
   }
   BitBlt(m_GContext.m_hDCTmp, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy, m_GContext.m_hDCBck, 0,0, SRCCOPY);
   //BitBlt(m_GContext.m_hDCDst, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy, m_GContext.m_hDCBck, 0,0, SRCCOPY);
   // paint cells
   for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
         m_Mosaic[i][j]->Paint();
   BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
          m_GContext.m_hDCDst, 0, 0, SRCCOPY);
   SetCursor(LoadCursor(NULL, IDC_ARROW));
   m_GContext.m_isRefreshMosaic = false;
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void nsMosaic::CMosaic::OnLButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, fDoubleClick ? WM_LBUTTONDBLCLK : WM_LBUTTONDOWN, (WPARAM)keyFlags, MAKELPARAM(x,y));
   if (m_GameStatus == gsEnd) return;
   if (GetPause()) return;
   SetCapture(hwnd);
   m_CoordDown = WinToArray(x,y);
   if (m_CoordDown.X < 0) return;
   FORWARD_WM_MOSAIC_CLICK(m_hWndParent, keyFlags, TRUE, TRUE, SendMessage);
   if (m_GameStatus == gsCreateGame) {
      if (m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->Cell_GetOpen() != nsCell::_Mine) {
         m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->Cell_SetStatus(nsCell::_Open);
         m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->Cell_SetMine();
         m_SerializeData.m_iMines++;
         m_RepositoryMines.Add(m_CoordDown);
      } else {
         m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->Reset();
         m_SerializeData.m_iMines--;
         m_RepositoryMines.Del(m_CoordDown);
      }
      m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->Paint();
      FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, PostMessage);
   } else {
      m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->LButtonDown();
   }
   BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
          m_GContext.m_hDCDst, 0, 0, SRCCOPY);
}

// WM_LBUTTONUP
void nsMosaic::CMosaic::OnLButtonUp(HWND hwnd, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, WM_LBUTTONUP, (WPARAM)keyFlags, MAKELPARAM(x,y));
   if (m_GameStatus == gsEnd) return;
   if (GetPause()) return;
   ReleaseCapture();
   if (m_CoordDown.X < 0) return;
   FORWARD_WM_MOSAIC_CLICK(m_hWndParent, keyFlags, TRUE, FALSE, SendMessage);
   if (m_GameStatus == gsCreateGame) return;
   COORD upCoord = WinToArray(x,y);
   if ((m_GameStatus == gsReady) &&
       (upCoord == m_CoordDown)) GameBegin(m_CoordDown);
   const CLeftUpReturn result =
      m_Mosaic[m_CoordDown.X][m_CoordDown.Y]->LButtonUp(upCoord==m_CoordDown);
   m_iCountOpen    += result.m_iCountOpen;
   m_iCountFlag    += result.m_iCountFlag;
   m_iCountUnknown += result.m_iCountUnknown;
   if (result.m_iCountOpen || result.m_iCountFlag || result.m_iCountUnknown) { // клик со смыслом (были изменения на поле)
      m_iCountClick++;
      if (keyFlags != MK_ASSISTANT) {   // клик пришёл не от робота
         m_PlayInfo |= piPlayerUser;  // юзер играл
         //gAssistant.ResetSequentialMove(); // окончить перебор флажков, если он был...
      }
   }
   FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, SendMessage);
   if (result.m_bEndGame)
      GameEnd(result.m_bVictory);
   else
      if (m_iCountOpen+m_SerializeData.m_iMines == m_SerializeData.m_SizeMosaic.X*m_SerializeData.m_SizeMosaic.Y)
         GameEnd(true);
      else
         VerifyFlag();
   BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
          m_GContext.m_hDCDst, 0, 0, SRCCOPY);
}

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void nsMosaic::CMosaic::OnRButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, fDoubleClick ? WM_RBUTTONDBLCLK : WM_RBUTTONDOWN, (WPARAM)keyFlags, MAKELPARAM(x,y));
   if (m_GameStatus == gsEnd) {
      if (!fDoubleClick)
         GameNew();
      else {
         MessageBeep(0);
      }
      return;
   }
   if (m_GameStatus == gsReady) return;
   if (m_GameStatus == gsCreateGame) return;
   if (GetPause()) {
      SetPause(false);
      return;
   }
   COORD coord = WinToArray(x,y);
   if (coord.X < 0) return;
   nsCell::EClose setClose;
   if (LOWORD(keyFlags) == MK_ASSISTANT) { // клик пришёл от робота
      m_PlayInfo |= piPlayerAssistant; // робот играл
      setClose = (nsCell::EClose)HIWORD(keyFlags);
   } else {
      switch (m_Mosaic[coord.X][coord.Y]->Cell_GetClose()) {
      case nsCell::_Clear  : setClose = nsCell::_Flag; break;
      case nsCell::_Flag   : setClose = m_SerializeData.m_useUnknown ? nsCell::_Unknown :
                                                                       nsCell::_Clear; break;
      case nsCell::_Unknown: setClose = nsCell::_Clear;
      }
   }
   const CRightDownReturn result = m_Mosaic[coord.X][coord.Y]->RButtonDown(setClose);
   if (result.m_iCountFlag || result.m_iCountUnknown) { // клик со смыслом (были изменения на поле)
      m_iCountClick++;
      if (LOWORD(keyFlags) != MK_ASSISTANT) { // клик пришёл не от робота
         m_PlayInfo |= piPlayerUser; // то считаю что юзер играл
         //gAssistant.ResetSequentialMove(); // окончить перебор флажков, если он был...
      }
   }
   m_iCountFlag    += result.m_iCountFlag;
   m_iCountUnknown += result.m_iCountUnknown;
   FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, SendMessage);
   VerifyFlag();
   BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
          m_GContext.m_hDCDst, 0, 0, SRCCOPY);
}

// WM_SIZE
void nsMosaic::CMosaic::OnSize(HWND hwnd, UINT state, int cx, int cy) {
   if ((m_GContext.m_SizeBitmap.cx != cx) || (m_GContext.m_SizeBitmap.cy != cy)) {
      m_GContext.m_SizeBitmap.cx = cx;
      m_GContext.m_SizeBitmap.cy = cy;
      DeleteObject(SelectObject(m_GContext.m_hDCDst  , CreateCompatibleBitmap(m_GContext.m_hDCWnd, m_GContext.m_SizeBitmap.cx,m_GContext.m_SizeBitmap.cy)));
      DeleteObject(SelectObject(m_GContext.m_hDCBck  , CreateCompatibleBitmap(m_GContext.m_hDCWnd, m_GContext.m_SizeBitmap.cx,m_GContext.m_SizeBitmap.cy)));
      DeleteObject(SelectObject(m_GContext.m_hDCTmp  , CreateCompatibleBitmap(m_GContext.m_hDCWnd, m_GContext.m_SizeBitmap.cx,m_GContext.m_SizeBitmap.cy)));
      DeleteObject(SelectObject(m_GContext.m_hDCPause, CreateCompatibleBitmap(m_GContext.m_hDCWnd, m_GContext.m_SizeBitmap.cx,m_GContext.m_SizeBitmap.cy)));
   }
   m_GContext.m_isRefreshMosaic = m_GContext.m_isRefreshPause = true;
   //FORWARD_WM_SIZE(hwnd, state, cx, cy, DefWindowProc);
}

////////////////////////////////////////////////////////////////////////////////
//                             other function
////////////////////////////////////////////////////////////////////////////////
void nsMosaic::CMosaic::MosaicDestroy(const COORD& SizeMosaic) {
   //delete [] m_Mosaic;
   for (int i = 0; i < SizeMosaic.X; i++)
      for (int j = 0; j < SizeMosaic.Y; j++) {
         //m_Mosaic[i][j]->~TcBase();
         //g_Logger.Put(CLogger::LL_DEBUG, TEXT("delete [%i,%i]"),i,j);
         delete m_Mosaic[i][j];
      }
}

void nsMosaic::CMosaic::MosaicCreate() {
   //m_Mosaic = new TCell[m_SerializeData.m_SizeMosaic.X*m_SerializeData.m_SizeMosaic.Y];
   m_Mosaic.resize(m_SerializeData.m_SizeMosaic.X);
   for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++){
      m_Mosaic[i].resize(m_SerializeData.m_SizeMosaic.Y);
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++){
         COORD coord = {i,j};
         switch (m_SerializeData.m_Mosaic) {
         case mosaicTriangle1  : m_Mosaic[i][j] = new nsCell::CTriangle1  (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTriangle2  : m_Mosaic[i][j] = new nsCell::CTriangle2  (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTriangle3  : m_Mosaic[i][j] = new nsCell::CTriangle3  (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTriangle4  : m_Mosaic[i][j] = new nsCell::CTriangle4  (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicSquare1    : m_Mosaic[i][j] = new nsCell::CSquare1    (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicSquare2    : m_Mosaic[i][j] = new nsCell::CSquare2    (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicParquet1   : m_Mosaic[i][j] = new nsCell::CParquet1   (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicParquet2   : m_Mosaic[i][j] = new nsCell::CParquet2   (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTrapezoid1 : m_Mosaic[i][j] = new nsCell::CTrapezoid1 (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTrapezoid2 : m_Mosaic[i][j] = new nsCell::CTrapezoid2 (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTrapezoid3 : m_Mosaic[i][j] = new nsCell::CTrapezoid3 (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicRhombus1   : m_Mosaic[i][j] = new nsCell::CRhombus1   (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicQuadrangle1: m_Mosaic[i][j] = new nsCell::CQuadrangle1(coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicPentagonT24: m_Mosaic[i][j] = new nsCell::CPentagonT24(coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicPentagonT5 : m_Mosaic[i][j] = new nsCell::CPentagonT5 (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicPentagonT10: m_Mosaic[i][j] = new nsCell::CPentagonT10(coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicHexagon1   : m_Mosaic[i][j] = new nsCell::CHexagon1   (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTrSq1      : m_Mosaic[i][j] = new nsCell::CTrSq1      (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicTrSq2      : m_Mosaic[i][j] = new nsCell::CTrSq2      (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         case mosaicSqTrHex    : m_Mosaic[i][j] = new nsCell::CSqTrHex    (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext); break;
         default /*mosaicNil*/ : m_Mosaic[i][j] = new nsCell::CBase       (coord, m_SerializeData.m_SizeMosaic, m_SerializeData.m_iArea, m_GContext,0,0,0); break;
         }
      }
   }
   int numberNeighbor = GetNeighborNumber();
   nsCell::CBase**const ppLinkNeighbor = new nsCell::CBase*[numberNeighbor];
   for (i = 0; i < m_SerializeData.m_SizeMosaic.X; i++){
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++){
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = m_Mosaic[i][j]->GetNeighborCoord(k);
            if (coordNeighbor != INCORRECT_COORD)
                  ppLinkNeighbor[k] = m_Mosaic[coordNeighbor.X][coordNeighbor.Y];
            else  ppLinkNeighbor[k] = NULL;
         }
         m_Mosaic[i][j]->SetNeighborLink(ppLinkNeighbor, numberNeighbor);
      }
   }
   delete [] ppLinkNeighbor;

   SendMessage(m_hWndParent, WM_SIZE, 0L, 0L);
   FORWARD_WM_MOSAIC_ADJUSTAREA(m_hWndParent, SendMessage);

   SetPause(false);
   m_GameStatus = gsEnd;
   GameNew();
}

void nsMosaic::CMosaic::GameNew() {
   if (m_GameStatus == gsReady) return;
   if (m_bPause) {
      SetPause(false);
      return;
   }
   if (m_RepositoryMines.GetSize()) {
      if (m_GameStatus == gsCreateGame) {
      } else {
         if (IDNO == MessageBox(m_hWnd, CLang::m_StrArr[IDS__RESTORE_LAST_GAME], CLang::m_StrArr[IDS__QUESTION], MB_YESNO | MB_ICONQUESTION)) {
            m_RepositoryMines.Reset();
         }
         SetPause(false);
      }
   }
   //g_Logger.ClearEdit();
   FORWARD_WM_MOSAIC_GAMENEW(m_hWndParent, SendMessage);
   //EnableWindow(m_hWnd, true); // enable all cell

   SetPause((m_GameStatus = gsReady, false));

   m_iCountTimer = m_iCountClick = m_iCountFlag = m_iCountOpen = m_iCountUnknown = 0;
   m_GContext.m_isRefreshMosaic = m_GContext.m_isRefreshPause = true;
   FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, SendMessage);

   for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
         m_Mosaic[i][j]->Reset();

   InvalidateRect(m_hWnd, NULL, FALSE);
   m_PlayInfo = piPlayerUnknown; // пока не знаю кто играл
}

void nsMosaic::CMosaic::GameBegin(COORD firstClick) {
   int count;
   int i, j;

   SetPause((m_GameStatus = gsPlay, false));

   FORWARD_WM_MOSAIC_GAMEBEGIN(m_hWndParent, SendMessage);

   // set mines
   if (m_RepositoryMines.GetSize()) {
      m_PlayInfo |= piIgnorable;
      size_t size = m_RepositoryMines.GetSize();
      for (int i=0; i<size; i++) {
         if (!m_Mosaic[m_RepositoryMines[i]->X][m_RepositoryMines[i]->Y]->Cell_SetMine()) {
#ifdef _DEBUG
            MessageBox(NULL, TEXT("Проблемы с установкой мин... :("), CLang::m_StrArr[IDS__ERROR], MB_OK | MB_ICONERROR);
            break;
#endif
         }
      }
   } else {
      if (!m_SerializeData.m_iMines)
         m_SerializeData.m_iMines = m_iOldMines;
      // set random mines
      m_Mosaic[firstClick.X][firstClick.Y]->LockNeighbor(); // запрещаю установку мин у соседей и у себя
      count = 0;
      do {
         i = rand(m_SerializeData.m_SizeMosaic.X-1);
         j = rand(m_SerializeData.m_SizeMosaic.Y-1);
         if (m_Mosaic[i][j]->Cell_SetMine())
            count++;
      } while (count < m_SerializeData.m_iMines);
   }
   // set other CellOpen and set all Caption
   for (i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
      for (j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
         m_Mosaic[i][j]->Cell_DefineValue();
}

void nsMosaic::CMosaic::GameCreate() {
   SetPause(false);
   GameNew();
   if (!m_RepositoryMines.GetSize()) {
      if (m_SerializeData.m_iMines)
         m_iOldMines = m_SerializeData.m_iMines;
      m_SerializeData.m_iMines = 0;
      m_GameStatus = gsCreateGame;
      FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, PostMessage);
   }
}

void nsMosaic::CMosaic::GameEnd(bool Victory) {
   if (m_GameStatus == gsEnd) return;

   int i, j;

   SetPause((m_GameStatus = gsEnd, false));
   // открыть оставшeеся
   {
   SetCursor(LoadCursor(NULL, IDC_WAIT));
   if (Victory) {
      for (i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
         for (j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++) {
            if (m_Mosaic[i][j]->Cell_GetStatus() == nsCell::_Close) {
               if(m_Mosaic[i][j]->Cell_GetOpen() == nsCell::_Mine)
                  m_Mosaic[i][j]->Cell_SetClose(nsCell::_Flag);
               else {
                  m_Mosaic[i][j]->Cell_SetStatus(nsCell::_Open);
                  m_Mosaic[i][j]->Cell_SetDown(true);
               }
               m_Mosaic[i][j]->Paint();
            }
         }
      m_iCountFlag = m_SerializeData.m_iMines;
   } else
      for (i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
         for (j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
            if (m_Mosaic[i][j]->Cell_GetStatus() == nsCell::_Close) {
               if ((m_Mosaic[i][j]->Cell_GetClose() == nsCell::_Flag) &&
                   (m_Mosaic[i][j]->Cell_GetOpen()  == nsCell::_Mine))
                    m_Mosaic[i][j]->Cell_SetStatus(nsCell::_Close);
               else m_Mosaic[i][j]->Cell_SetStatus(nsCell::_Open);
               m_Mosaic[i][j]->Paint();
            }
   SetCursor(LoadCursor(NULL, IDC_ARROW));
   }
   MessageBeep(0);

   FORWARD_WM_MOSAIC_CHANGECOUNTERS(m_hWndParent, PostMessage);
   FORWARD_WM_MOSAIC_GAMEEND       (m_hWndParent, PostMessage);
}

inline void nsMosaic::CMosaic::VerifyFlag() {
   if (m_GameStatus == gsEnd) return;
   if (m_SerializeData.m_iMines == m_iCountFlag) {
      for (int i=0; i < m_SerializeData.m_SizeMosaic.X; i++)
         for (int j=0; j < m_SerializeData.m_SizeMosaic.Y; j++)
            if ((m_Mosaic[i][j]->Cell_GetClose() == nsCell::_Flag) &&
                (m_Mosaic[i][j]->Cell_GetOpen () != nsCell::_Mine))
               return; // неверно проставленный флажок - на выход
      GameEnd(true);
   } else
      if (m_SerializeData.m_iMines == m_iCountFlag+m_iCountUnknown) {
         for (int i=0; i < m_SerializeData.m_SizeMosaic.X; i++)
            for (int j=0; j < m_SerializeData.m_SizeMosaic.Y; j++)
               if (((m_Mosaic[i][j]->Cell_GetClose() == nsCell::_Unknown) ||
                    (m_Mosaic[i][j]->Cell_GetClose() == nsCell::_Flag)) &&
                   ( m_Mosaic[i][j]->Cell_GetOpen () != nsCell::_Mine))
                  return; // неверно проставленный флажок или '?'- на выход
         GameEnd(true);
      }
   return;
}

void nsMosaic::CMosaic::SetPause(bool newPause) {
   FORWARD_WM_MOSAIC_PAUSE(m_hWndParent, PostMessage);
   if (!(m_GameStatus == gsPlay)) {
      KillTimer(m_hWnd, (UINT)this);
      return;
   }
   m_bPause = newPause;
   if (m_bPause) {
      KillTimer(m_hWnd, (UINT)this);
   } else {
      SetTimer(m_hWnd, (UINT)this, 1000, (TIMERPROC)TimerProc);
   }
   InvalidateRect(m_hWnd, NULL, FALSE);
   //EnableWindow(m_hWnd, !m_bPause);
}

void nsMosaic::CMosaic::SetMosaic(EMosaic mosaic) {
   m_RepositoryMines.Reset();
   if (m_SerializeData.m_Mosaic == mosaic) {
      GameNew();
      return;
   }
   ESkillLevel skill = GetSkillLevel(); // skill level для СТАРОЙ фигуры!!!
   MosaicDestroy(m_SerializeData.m_SizeMosaic);
   m_SerializeData.m_Mosaic = mosaic;
   SetClasureToMosaic();
   if (skill == skillLevelCustom) { // skill level для НОВОЙ фигуры!!!
      int maxMines = m_SerializeData.m_SizeMosaic.X*m_SerializeData.m_SizeMosaic.Y-(GetNeighborNumber()+1);
      if(m_SerializeData.m_iMines > maxMines) {
         m_SerializeData.m_iMines = maxMines;
      }
   } else {
      m_SerializeData.m_iMines = DefineNumberMines(skill, m_SerializeData.m_Mosaic, m_SerializeData.m_SizeMosaic);
   }
   MosaicCreate();
}

void nsMosaic::CMosaic::SetGame(EMosaic mosaic, const COORD& newSizeMosaic, int numberMines, const CStorageMines *pStorageCoordMines) {
   SetMosaic(mosaic);
   SetSkillLevelCustom(newSizeMosaic, numberMines);
   m_RepositoryMines = *pStorageCoordMines;
}

void nsMosaic::CMosaic::SetSkillLevelCustom(const COORD& newSizeMosaic, int numberMines) {
   if ((m_SerializeData.m_SizeMosaic == newSizeMosaic) &&
       (m_SerializeData.m_iMines      == numberMines)) return;
   MosaicDestroy(m_SerializeData.m_SizeMosaic);
   m_SerializeData.m_SizeMosaic = newSizeMosaic;
   m_SerializeData.m_iMines      = numberMines;
   MosaicCreate();
   m_RepositoryMines.Reset();
}

void nsMosaic::CMosaic::SetSkillLevel(ESkillLevel skill) {
   if (skill == skillLevelCustom) {
      return;
   }
   if (GetSkillLevel() == skill) {
      GameNew();
      return;
   }
   m_RepositoryMines.Reset();
   MosaicDestroy(m_SerializeData.m_SizeMosaic);
   m_SerializeData.m_SizeMosaic = SIZE_MOSAIC[skill];
   m_SerializeData.m_iMines = DefineNumberMines(skill, m_SerializeData.m_Mosaic, m_SerializeData.m_SizeMosaic);
   MosaicCreate();
}

nsMosaic::EMosaic nsMosaic::CMosaic::GetMosaic() const {
   return m_SerializeData.m_Mosaic;
}

nsMosaic::ESkillLevel nsMosaic::CMosaic::GetSkillLevel() const {
   for (BYTE i = skillLevelBeginner; i < skillLevelCustom; i++)
      if ((m_SerializeData.m_SizeMosaic == SIZE_MOSAIC[i]) &&
          (m_SerializeData.m_iMines     == DefineNumberMines((ESkillLevel)i, m_SerializeData.m_Mosaic, SIZE_MOSAIC[i])))
      {
         return (ESkillLevel)i;
      }
   return skillLevelCustom;
}

void nsMosaic::CMosaic::TimerProc(HWND hwnd, UINT uMsg, UINT idEvent, DWORD dwTime) {
   CMosaic* const This = (CMosaic*)idEvent;
   This->m_iCountTimer++;
   FORWARD_WM_MOSAIC_CHANGECOUNTERS(This->m_hWndParent, SendMessage);
}

COORD nsMosaic::CMosaic::WinToArray(int x, int y) const { // преобразовать экранные координаты в координаты m_Mosaic'a
   COORD result = INCORRECT_COORD;
   for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
         if (m_Mosaic[i][j]->PointInRegion(POINTEX(x,y))) {
            result = m_Mosaic[i][j]->GetCoord();
            return result;
         }
   return result;
}

void nsMosaic::CMosaic::SetArea(int newArea) {
   if (m_SerializeData.m_iArea == newArea) return;
   m_SerializeData.m_iArea = newArea;

   for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
      for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
         m_Mosaic[i][j]->SetPoint(m_SerializeData.m_iArea);
   SetDCFont();
   SendMessage(m_hWndParent, WM_SIZE, 0L, 0L);
   InvalidateRect(m_hWnd, NULL, FALSE);
}

void nsMosaic::CMosaic::SetClasureToMosaic() {
   switch (m_SerializeData.m_Mosaic) {
   case mosaicTriangle1  : GetSizeWindow = nsCell::CTriangle1  ::GetSizeInPixel; break;
   case mosaicTriangle2  : GetSizeWindow = nsCell::CTriangle2  ::GetSizeInPixel; break;
   case mosaicTriangle3  : GetSizeWindow = nsCell::CTriangle3  ::GetSizeInPixel; break;
   case mosaicTriangle4  : GetSizeWindow = nsCell::CTriangle4  ::GetSizeInPixel; break;
   case mosaicSquare1    : GetSizeWindow = nsCell::CSquare1    ::GetSizeInPixel; break;
   case mosaicSquare2    : GetSizeWindow = nsCell::CSquare2    ::GetSizeInPixel; break;
   case mosaicParquet1   : GetSizeWindow = nsCell::CParquet1   ::GetSizeInPixel; break;
   case mosaicParquet2   : GetSizeWindow = nsCell::CParquet2   ::GetSizeInPixel; break;
   case mosaicTrapezoid1 : GetSizeWindow = nsCell::CTrapezoid1 ::GetSizeInPixel; break;
   case mosaicTrapezoid2 : GetSizeWindow = nsCell::CTrapezoid2 ::GetSizeInPixel; break;
   case mosaicTrapezoid3 : GetSizeWindow = nsCell::CTrapezoid3 ::GetSizeInPixel; break;
   case mosaicRhombus1   : GetSizeWindow = nsCell::CRhombus1   ::GetSizeInPixel; break;
   case mosaicQuadrangle1: GetSizeWindow = nsCell::CQuadrangle1::GetSizeInPixel; break;
   case mosaicPentagonT24: GetSizeWindow = nsCell::CPentagonT24::GetSizeInPixel; break;
   case mosaicPentagonT5 : GetSizeWindow = nsCell::CPentagonT5 ::GetSizeInPixel; break;
   case mosaicPentagonT10: GetSizeWindow = nsCell::CPentagonT10::GetSizeInPixel; break;
   case mosaicHexagon1   : GetSizeWindow = nsCell::CHexagon1   ::GetSizeInPixel; break;
   case mosaicTrSq1      : GetSizeWindow = nsCell::CTrSq1      ::GetSizeInPixel; break;
   case mosaicTrSq2      : GetSizeWindow = nsCell::CTrSq2      ::GetSizeInPixel; break;
   case mosaicSqTrHex    : GetSizeWindow = nsCell::CSqTrHex    ::GetSizeInPixel; break;
   default /*mosaicNil*/ : GetSizeWindow = nsCell::CBase       ::GetSizeInPixel; break;
   }

   switch (m_SerializeData.m_Mosaic) {
   case mosaicTriangle1  : GetSizeInscribedSquare = nsCell::CTriangle1  ::SizeInscribedSquare; break;
   case mosaicTriangle2  : GetSizeInscribedSquare = nsCell::CTriangle2  ::SizeInscribedSquare; break;
   case mosaicTriangle3  : GetSizeInscribedSquare = nsCell::CTriangle3  ::SizeInscribedSquare; break;
   case mosaicTriangle4  : GetSizeInscribedSquare = nsCell::CTriangle4  ::SizeInscribedSquare; break;
   case mosaicSquare1    : GetSizeInscribedSquare = nsCell::CSquare1    ::SizeInscribedSquare; break;
   case mosaicSquare2    : GetSizeInscribedSquare = nsCell::CSquare2    ::SizeInscribedSquare; break;
   case mosaicParquet1   : GetSizeInscribedSquare = nsCell::CParquet1   ::SizeInscribedSquare; break;
   case mosaicParquet2   : GetSizeInscribedSquare = nsCell::CParquet2   ::SizeInscribedSquare; break;
   case mosaicTrapezoid1 : GetSizeInscribedSquare = nsCell::CTrapezoid1 ::SizeInscribedSquare; break;
   case mosaicTrapezoid2 : GetSizeInscribedSquare = nsCell::CTrapezoid2 ::SizeInscribedSquare; break;
   case mosaicTrapezoid3 : GetSizeInscribedSquare = nsCell::CTrapezoid3 ::SizeInscribedSquare; break;
   case mosaicRhombus1   : GetSizeInscribedSquare = nsCell::CRhombus1   ::SizeInscribedSquare; break;
   case mosaicQuadrangle1: GetSizeInscribedSquare = nsCell::CQuadrangle1::SizeInscribedSquare; break;
   case mosaicPentagonT24: GetSizeInscribedSquare = nsCell::CPentagonT24::SizeInscribedSquare; break;
   case mosaicPentagonT5 : GetSizeInscribedSquare = nsCell::CPentagonT5 ::SizeInscribedSquare; break;
   case mosaicPentagonT10: GetSizeInscribedSquare = nsCell::CPentagonT10::SizeInscribedSquare; break;
   case mosaicHexagon1   : GetSizeInscribedSquare = nsCell::CHexagon1   ::SizeInscribedSquare; break;
   case mosaicTrSq1      : GetSizeInscribedSquare = nsCell::CTrSq1      ::SizeInscribedSquare; break;
   case mosaicTrSq2      : GetSizeInscribedSquare = nsCell::CTrSq2      ::SizeInscribedSquare; break;
   case mosaicSqTrHex    : GetSizeInscribedSquare = nsCell::CSqTrHex    ::SizeInscribedSquare; break;
   default /*mosaicNil*/ : GetSizeInscribedSquare = nsCell::CBase       ::SizeInscribedSquare; break;
   }
   SetDCFont();
}

inline void nsMosaic::CMosaic::SetBrush(COLORREF colorSolidBrush) {
   DeleteObject(m_GContext.m_hBrush);
   m_GContext.m_hBrush = CreateSolidBrush(colorSolidBrush);
   m_GContext.m_colorBk = colorSolidBrush;
   DeleteObject(SelectObject(m_GContext.m_hDCDst  , m_GContext.m_hBrush)); // ???
   DeleteObject(SelectObject(m_GContext.m_hDCBck  , m_GContext.m_hBrush)); // ???
   DeleteObject(SelectObject(m_GContext.m_hDCPause, m_GContext.m_hBrush)); // ???
   m_GContext.m_ImgPause  .SetBrush(m_GContext.m_hBrush);
   m_GContext.m_ImgBckgrnd.SetBrush(m_GContext.m_hBrush);
}

inline void nsMosaic::CMosaic::SetDCFont() {
   m_GContext.m_Font.lfHeight = GetSizeInscribedSquare(m_SerializeData.m_iArea, m_GContext.m_Border.m_iWidth);
   m_GContext.m_hFont = CreateFontIndirect(&m_GContext.m_Font);
   //DeleteObject(SelectObject(m_GContext.m_hDCWnd, m_GContext.m_hFont));
   DeleteObject(SelectObject(m_GContext.m_hDCTmp, m_GContext.m_hFont));
}

inline void nsMosaic::CMosaic::SetFont(const LOGFONT& newFont) {
   m_GContext.m_Font = newFont;
   SetDCFont();
}

inline void nsMosaic::CMosaic::SetCellColorText(const nsCell::CColorText& newColorText) {
   m_GContext.m_ColorText = newColorText;
}

inline void nsMosaic::CMosaic::SetBorder(const nsCell::CBorder& newBorder) {
   int oldWidth = m_GContext.m_Border.m_iWidth;
   if (&(m_GContext.m_Border) != &newBorder) {
      m_GContext.m_Border = newBorder; // @TODO - пересмотреть...
   }
   DeleteObject(m_GContext.m_hPenShadow);
   m_GContext.m_hPenShadow = CreatePen(PS_SOLID, 2*newBorder.m_iWidth, newBorder.m_colorShadow); // Shadow pen
   DeleteObject(m_GContext.m_hPenLight);
   m_GContext.m_hPenLight  = CreatePen(PS_SOLID, 2*newBorder.m_iWidth, newBorder.m_colorLight ); // Light pen
   if(oldWidth != newBorder.m_iWidth) {
      if (!m_Mosaic.empty())
         for (int i = 0; i < m_SerializeData.m_SizeMosaic.X; i++)
            for (int j = 0; j < m_SerializeData.m_SizeMosaic.Y; j++)
               m_Mosaic[i][j]->SetPoint(m_SerializeData.m_iArea); // вообщето надо пересчитать только nsCell::CBase::square (т.е. те точки которые зависят от ширины пера w)
      SetDCFont();
   }
}

void nsMosaic::CMosaic::ApplySkin(const CSkinMosaic& newSkin) {

#define APPLY_IMAGE(name) \
   if (newSkin.m_Img##name.m_szPath[0]) { \
      m_GContext.m_Img##name.SetImage(newSkin.m_Img##name); \
   } else { \
      LoadDefaultImage##name(ghInstance, m_GContext.m_Img##name); \
      m_GContext.m_Img##name.SetPlace      (newSkin.m_Img##name.m_Place); \
      m_GContext.m_Img##name.SetTransparent(newSkin.m_Img##name.m_bTransparent); \
   }

   APPLY_IMAGE(Mine   );
   APPLY_IMAGE(Flag   );
   APPLY_IMAGE(Pause  );
   APPLY_IMAGE(Bckgrnd);

   m_GContext.m_isRefreshMosaic = m_GContext.m_isRefreshPause = true;
   SetFont            (newSkin.m_Font      );
   SetCellColorText   (newSkin.m_ColorText );
   SetBorder          (newSkin.m_Border    );
   SetBrush           (newSkin.m_colorBk   );
}

BOOL nsMosaic::CMosaic::SerializeIn(HANDLE hFile) {
   // read .ini file
   DWORD dwNOBR = 0;
   BOOL result =
      ReadFile(hFile, &m_SerializeData, sizeof(m_SerializeData), &dwNOBR, NULL) &&
      (sizeof(m_SerializeData) == dwNOBR);
   if (result) {
      SetClasureToMosaic();
      MosaicCreate();
   }
   return result;
}

BOOL nsMosaic::CMosaic::SerializeOut(HANDLE hFile) const {
   // write .ini file
   DWORD dwNOBW = 0;
   return
      WriteFile(hFile, &m_SerializeData, sizeof(m_SerializeData), &dwNOBW, NULL) &&
      (sizeof(m_SerializeData) == dwNOBW);
}
