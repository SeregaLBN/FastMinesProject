////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "FastMines2.cpp"
//
// обработка главного окна программы
////////////////////////////////////////////////////////////////////////////////

//#pragma comment(linker, "/MERGE:.rdata=.text")
//#pragma comment(linker, "/FILEALIGN:512 /SECTION:.text,EWRX/IGNORE:4078")
//#pragma comment(linker, "/IGNORE:4089")
//#pragma comment(linker, "/ENTRY:WinMain")
//#pragma comment(linker, "/NODEFAULTLIB")
//#pragma comment(linker, "/opt:nowin98")

#include "StdAfx.h"
#include "FastMines2.h"
#include <WindowsX.h>
#include <CommCtrl.h>
//#include <time.h>
//#include <map>
//#include <set>
//#include <vector>
//#include <math.h>
#include <process.h> // _beginthread, _endthread
#include "WinDefAdd.h"
#include "EraseBk.h"
#include "Lang.h"
#include "./Mosaic/Base.h"
#include "StorageMines.h"
#include "./Control/MenuItem.h"
#include "./Dialog/About.h"
#include "./Dialog/CustomSkill.h"
#include "./Dialog/SelectMosaic.h"
#include "./Dialog/Statistics.h"
#include "./Dialog/Champions.h"
#include "./Dialog/AssistantDlg.h"
#include "./Dialog/Skin.h"
#ifdef _DEBUG
   #include "Logger.h"
#endif

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
const BYTE  HEIGHT_PANEL_TOP = 40;//
const BYTE  HEIGHT_WIDTH_BUTTON_NEW_PAUSE = 28;//36;//
const SIZE  EDIT_COUNT_TIMER = {40, 21}; // ширина и высота окон
const TCHAR SZ_CLASS_PROJECT   [] = TEXT("ClassWndFastMinesProject");
const TCHAR SZ_CLASS_WND_TOP   [] = TEXT("ClassWndFastMinesTop"); // toolbar
const TCHAR SZ_FILE_NAME_INIT  [] = TEXT("Mines.ini");

#define HSUBMENU_SKIN      GetSubMenu(GetSubMenu(GetSubMenu(m_hMenu, 2), 6), 0)
#define HSUBMENU_ASSISTANT            GetSubMenu(GetSubMenu(m_hMenu, 3), 3)
#define HSUBMENU_LANGUAGE             GetSubMenu(GetSubMenu(m_hMenu, 2), 8)
#define SET_MOSAIC(eMosaic) m_Mosaic.SetMosaic(eMosaic); \
                           CheckMenuRadioItem_Mosaic(eMosaic); \
                           {  /*
                              на случай если:
                              1. была мозаика с nsMosaic::skillLevelCustom
                              2. и её поменяли на др. мозаику
                              то надо перепроверить skillLevel */ \
                              CheckMenuRadioItem( GetSubMenu(m_hMenu, 0), \
                                 ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM, \
                                 ID_MENU_GAME_BEGINNER + m_Mosaic.GetSkillLevel(), \
                                 MF_BYCOMMAND); \
                           }

struct CFileGame {
   TCHAR             m_szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   nsMosaic::EMosaic m_eMosaic;
   SIZE              m_SizeMosaic;
   UINT              m_iMines;
   CFileGame():
      m_eMosaic(nsMosaic::mosaicSquare1),
      m_iMines(1)
   { m_SizeMosaic.cx = m_SizeMosaic.cy = 1;
     lstrcpy(m_szVersion, TEXT(ID_VERSIONINFO_VERSION3));
   }
};

////////////////////////////////////////////////////////////////////////////////
//                        global variables in all project
////////////////////////////////////////////////////////////////////////////////
HINSTANCE           ghInstance;
CFastMines2Project *gpFM2Proj;

//DWORD WINAPI ChildThread(PVOID);
#define ID_EVENT_TIMER_INACTION 1 // id события таймера, первого   срабатывания при бездействии юзера для старта робота
#define ID_EVENT_TIMER_JOB      2 // id события таймера, следующих срабатываний при бездействии юзера
HANDLE hEventJob;//, hEventJobEnd;
HANDLE hEventSetCursorBegin, hEventSetCursorEnd;

//bool bOpenCellAssistant;
////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
LRESULT CALLBACK WndProcTest(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam){
   switch(msg){
   case WM_DESTROY:
      PostQuitMessage(0);
      return 0;
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}
//#pragma comment(linker, "/SUBSYSTEM:WINDOWS")
//#pragma comment(linker, "/ENTRY:wWinMainCRTStartup")
//int WINAPI wWinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPWSTR lpszCmdLine, int nCmdShow )
int WINAPI _tWinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpszCmdLine, int nCmdShow )
{
   ghInstance = hInstance;

/**/
   InitCommonControls();
   HACCEL hAccel = LoadAccelerators(ghInstance, TEXT("FastMinesAccelerators"));

   gpFM2Proj = new CFastMines2Project();
   if (lpszCmdLine &&
      *lpszCmdLine &&
      (lpszCmdLine[0] == TEXT('"'))&&
      (lpszCmdLine[lstrlen(lpszCmdLine)-1]) == TEXT('"'))
   {
      lpszCmdLine[lstrlen(lpszCmdLine)-1] = 0;
      gpFM2Proj->Create(lpszCmdLine+1);
   } else {
      gpFM2Proj->Create();
   }

   ShowWindow(gpFM2Proj->GetHandle(), nCmdShow);
   UpdateWindow(gpFM2Proj->GetHandle());

   MSG msg;
   while ( GetMessage(&msg, NULL, 0, 0) ){
      if (!TranslateAccelerator(gpFM2Proj->GetHandle(), hAccel, &msg)) {
         TranslateMessage(&msg);
         DispatchMessage (&msg);
      }
   }
   return msg.wParam;
/**
   const WNDCLASS wc = {
      0,                                // UINT    style
      WndProcTest,                      // WNDPROC lpfnWndProc
      0,                                // int     cbClsExtra
      0,                                // int     cbWndExtra
      hInstance,                        // HANDLE  ghInstance
      LoadIcon  (NULL, IDI_APPLICATION),// HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),      // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE),  // HBRUSH  hbrBackground
      NULL,                             // LPCTSTR lpszMenuName
      TEXT("ClassWndProject")           // LPCTSTR lpszClassName
   };
   RegisterClass(&wc);
   const HWND hWnd = CreateWindow( TEXT("ClassWndProject"), TEXT("m_Mosaic Test"),
      WS_OVERLAPPED | WS_THICKFRAME | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX,
      100, 100, 500, 300, NULL, (HMENU)0, hInstance, NULL );

   ShowWindow(m_hWnd, nCmdShow);

   {
      static CMosaic m_Mosaic;
      m_Mosaic.Create(m_hWnd, ID_MOSAIC);
      ShowWindow(m_Mosaic.GetHandle(), SW_SHOW);

      SIZE sizeScreen = GetScreenSize();
      MoveWindow(m_hWnd, 0,0, sizeScreen.cx, sizeScreen.cy, TRUE);
      MoveWindow(m_Mosaic.GetHandle(), 0,0, sizeScreen.cx, sizeScreen.cy, TRUE);
   }

   MSG msg;
   while ( GetMessage(&msg, NULL, 0, 0)){
      DispatchMessage(&msg);
   }
   return msg.wParam;
/**/
}

LRESULT CALLBACK WndProcProject(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam){
/**
   switch(msg){
   case WM_MOVE:
   case WM_SETCURSOR:
   case WM_NCMOUSEMOVE:
   case WM_NCHITTEST:
      break;
   default:
      g_Logger.PutMsg(CLogger::LL_DEBUG, TEXT("Proj: "), msg);
      break;
   }
/**

   static bool inMenu = false;
   switch(msg){
   case WM_ENTERMENULOOP: inMenu = true;  break;
   case WM_EXITMENULOOP : inMenu = false; break;
   }

   if (inMenu) {
      gpFM2Proj->ResetAssistant();
   } else
      switch(msg){
      case WM_TIMER:
      case WM_SETTEXT:
      case WM_GETTEXT:
      case WM_NCHITTEST:
      case WM_MOUSEFIRST:
         break;
      // if pressed key for suggest, then - to window send next messages:
      // 1. WM_INITMENU:
      // 2. WM_INITMENUPOPUP:
      // 3. WM_COMMAND:
      // 4. WM_SETCURSOR:
      // 5. WM_KEYUP:
      { /// begin: if press key for suggest
         case WM_INITMENU:
         case WM_INITMENUPOPUP:
            break;
         case WM_COMMAND:
            if (LOWORD(wParam) == ID_MENU_HELP_ASSISTANT_SUGGEST) {
               SetEvent(hEventSetCursorBegin); // next message must by WM_SETCURSOR
               //gpFM2Proj->ResetAssistant();
            }
            break;
         case WM_KEYUP:
            if ((int)wParam != 0x48)
               gpFM2Proj->ResetAssistant();
            break;
      } /// end: if press key for suggest
      case WM_SETCURSOR:
         if (WAIT_OBJECT_0 == WaitForSingleObject(hEventSetCursorBegin, 0)) {
            SetEvent(hEventSetCursorEnd);
            break;
         }
   #ifdef USE_INFO_DIALOG
      //nsInfo::AddValue(TEXT("msg (project) = 0x"), msg, 16);
   #endif // USE_INFO_DIALOG
      default:
         gpFM2Proj->ResetAssistant();
      }
/**/
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   static bool f = false;
   LRESULT result;
#endif // REPLACEBKCOLORFROMFILLWINDOW

   switch(msg){
 //HANDLE_MSG(hwnd, WM_NCLBUTTONDBLCLK  , gpFM2Proj->OnNCLButtonDown);
 //HANDLE_MSG(hwnd, WM_NCLBUTTONDOWN    , gpFM2Proj->OnNCLButtonDown);
 //HANDLE_MSG(hwnd, WM_NCLBUTTONUP      , gpFM2Proj->OnNCLButtonUp);
 //HANDLE_MSG(hwnd, WM_ERASEBKGND       , gpFM2Proj->OnEraseBkgnd);
   HANDLE_MSG(hwnd, WM_CREATE           , gpFM2Proj->OnCreate);
 //HANDLE_MSG(hwnd, WM_TIMER            , gpFM2Proj->ProjectOnTimer);
   HANDLE_MSG(hwnd, WM_MOVE             , gpFM2Proj->OnMove);
   HANDLE_MSG(hwnd, WM_SIZE             , gpFM2Proj->OnSize);
 //HANDLE_MSG(hwnd, WM_WINDOWPOSCHANGING, gpFM2Proj->OnWindowPosChanging);
 //HANDLE_MSG(hwnd, WM_WINDOWPOSCHANGED , gpFM2Proj->OnWindowPosChanged);
   HANDLE_MSG(hwnd, WM_DESTROY          , gpFM2Proj->OnDestroy);
 //HANDLE_MSG(hwnd, WM_GETMINMAXINFO    , gpFM2Proj->OnGetMinMaxInfo);
   HANDLE_MSG(hwnd, WM_SYSCOMMAND       , gpFM2Proj->OnSysCommand);
   HANDLE_MSG(hwnd, WM_COMMAND          , gpFM2Proj->OnCommand);
   HANDLE_MSG(hwnd, WM_ACTIVATE         , gpFM2Proj->OnActivate);
   HANDLE_MSG(hwnd, WM_SYSKEYUP         , gpFM2Proj->OnSysKey); // ScreenShot
   HANDLE_MSG(hwnd, WM_KEYUP            , gpFM2Proj->OnKey);    // ScreenShot
   HANDLE_MSG(hwnd, WM_MEASUREITEM      , gpFM2Proj->OnMeasureItem);
   HANDLE_MSG(hwnd, WM_DRAWITEM         , gpFM2Proj->OnDrawItem);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hwnd, WM_PAINT            , gpFM2Proj->OnPaint);
   HANDLE_MSG(hwnd, WM_MENUSELECT       , gpFM2Proj->OnMenuSelect);
   //case WM_NCHITTEST:
   //case WM_MOUSEMOVE:
   case WM_NCMOUSEMOVE:
   case WM_NCACTIVATE:
   case WM_EXITMENULOOP: // HANDLE_MSG(hwnd, WM_EXITMENULOOP, gpFM2Proj->OnExitMenuLoop);
      result = DefWindowProc(hwnd, msg, wParam, lParam);
      if (gpFM2Proj->m_Serialize.m_Skin.m_bToAll)
         nsEraseBk::FillMenu(hwnd, gpFM2Proj->m_Serialize.m_Skin.m_colorBk);
      return result;
   case WM_NEXTMENU: f = true;   break;
   case WM_ENTERIDLE:
      result = DefWindowProc(hwnd, msg, wParam, lParam);
      if (f && gpFM2Proj->m_Serialize.m_Skin.m_bToAll)
         nsEraseBk::FillMenu(hwnd, gpFM2Proj->m_Serialize.m_Skin.m_colorBk);
      f = false;
      return result;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hwnd, WM_NOTIFYICON           , gpFM2Proj->OnNotifyIcon          );
   HANDLE_MSG(hwnd, WM_MOSAIC_ADJUSTAREA    , gpFM2Proj->OnMosaicAdjustArea    );
   HANDLE_MSG(hwnd, WM_MOSAIC_CLICK         , gpFM2Proj->OnMosaicClick         );
   HANDLE_MSG(hwnd, WM_MOSAIC_CHANGECOUNTERS, gpFM2Proj->OnMosaicChangeCounters);
   HANDLE_MSG(hwnd, WM_MOSAIC_GAMENEW       , gpFM2Proj->OnMosaicGameNew       );
   HANDLE_MSG(hwnd, WM_MOSAIC_GAMEBEGIN     , gpFM2Proj->OnMosaicGameBegin     );
   HANDLE_MSG(hwnd, WM_MOSAIC_GAMEEND       , gpFM2Proj->OnMosaicGameEnd       );
   HANDLE_MSG(hwnd, WM_MOSAIC_PAUSE         , gpFM2Proj->OnMosaicPause         );

   HANDLE_MSG(hwnd, WM_MOUSEWHEEL           , gpFM2Proj->OnMouseWheel          );
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

LRESULT CALLBACK WndProcTop(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   switch(msg){
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hwnd, WM_ERASEBKGND     , gpFM2Proj->OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hwnd, WM_COMMAND        , gpFM2Proj->TopOnCommand);
 //HANDLE_MSG(hwnd, WM_LBUTTONDOWN    , gpFM2Proj->TopOnLButtonDown);
 //HANDLE_MSG(hwnd, WM_LBUTTONDBLCLK  , gpFM2Proj->TopOnLButtonDown);
 //HANDLE_MSG(hwnd, WM_RBUTTONDOWN    , gpFM2Proj->TopOnRButtonDown);
 //HANDLE_MSG(hwnd, WM_RBUTTONDBLCLK  , gpFM2Proj->TopOnRButtonDown);
 //HANDLE_MSG(hwnd, WM_NCMBUTTONDBLCLK, gpFM2Proj->TopOnNCMButtonDown);
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

////////////////////////////////////////////////////////////////////////////////
//                               CFastMines2Project
////////////////////////////////////////////////////////////////////////////////
CFastMines2Project::CFastMines2Project() :
   m_pCaptionButton(NULL)
{
}

// constructor
void CFastMines2Project::Create(LPCTSTR szFileName) {
   m_hIconProject = LoadIcon(ghInstance, TEXT("iconPROJECT"));
   RegClass(
      0,                               // UINT    style
      WndProcProject,                  // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      m_hIconProject,                  // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      TEXT("FastMinesMenu"),           // LPCTSTR lpszMenuName
      SZ_CLASS_PROJECT                 // LPCTSTR lpszClassName
   );
   RegClass(
      CS_DBLCLKS,                      // UINT    style
      WndProcTop,                      // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      SZ_CLASS_WND_TOP                 // LPCTSTR lpszClassName
   );

   LoadDefaultImageBtnNew0  (ghInstance, m_ImgBtnNew  [0]);
   LoadDefaultImageBtnNew1  (ghInstance, m_ImgBtnNew  [1]);
   LoadDefaultImageBtnNew2  (ghInstance, m_ImgBtnNew  [2]);
   LoadDefaultImageBtnNew3  (ghInstance, m_ImgBtnNew  [3]);
   LoadDefaultImageBtnPause0(ghInstance, m_ImgBtnPause[0]);
   LoadDefaultImageBtnPause1(ghInstance, m_ImgBtnPause[1]);
   LoadDefaultImageBtnPause2(ghInstance, m_ImgBtnPause[2]);
   LoadDefaultImageBtnPause3(ghInstance, m_ImgBtnPause[3]);

   CreateWindow(SZ_CLASS_PROJECT, CLang::m_StrArr[IDS__LOGO_VERS],
      WS_CAPTION_SHOW,
      GetSystemMetrics(SM_CXSCREEN)/2-GetSystemMetrics(SM_CXMIN)/2,
      GetSystemMetrics(SM_CYSCREEN)/2-GetSystemMetrics(SM_CYMIN)/2,
      GetSystemMetrics(SM_CXMIN),
      GetSystemMetrics(SM_CYMIN),
      NULL, (HMENU)0, ghInstance, NULL );
   /*
   WINDOWPLACEMENT winPlace = {sizeof(WINDOWPLACEMENT), WPF_SETMINPOSITION,
                               SW_SHOW, {10,10}, {0,0}, {0,0,0,0}};
   if (!SetWindowPlacement(m_hWnd, &winPlace)) {
      DWORD errCode = GetLastError();
   }/**/

   m_hMenu = GetMenu(m_hWnd);

   ReloadMosaicMenu(GetSubMenu(m_hMenu, 1)); // указываю что меню для мозаик буду отрисовывать я

   if (SerializeIn()) {
      ApplySkin();
      CLang(m_Serialize.m_szLanguage);
   } else {
      SendMessage(m_hWnd, WM_COMMAND, MAKEWPARAM(ID_MENU_HELP_ABOUT, 0), 0L);
   }
   Menu_ChangeLanguage(); // т.к. меню мозаик у меня MFT_OWNERDRAW, то вызов этой ф-ции должен быть после ReloadMosaicMenu()

#ifdef REPLACEBKCOLORFROMFILLWINDOW
#else
   //ReplaceFunction_GetSysColor     (ghInstance);
   //ReplaceFunction_GetSysColorBrush(ghInstance);
   //SetClassLong(m_hWndTop, GCL_HBRBACKGROUND, (LONG)CreateSolidBrush(m_Skin.m_colorBk));
   /**
   {
      const int lpaElements[2] = {COLOR_MENU, COLOR_BTNFACE};
      const COLORREF lpaRgbValues[2] = {m_Skin.m_colorBk, m_Skin.m_colorBk};
      SetSysColors(2, lpaElements, lpaRgbValues);
   }/**/
#endif // REPLACEBKCOLORFROMFILLWINDOW

   // загружаю в меню список всех найденых скинов
   nsFileSkin::LoadMenuList(HSUBMENU_SKIN); // надо вызвать до того как уберётся меню
   // загружаю в меню список всех найденых языковых файлов
   CLang     ::LoadMenuList(HSUBMENU_LANGUAGE); // надо вызвать до того как уберётся меню

   /**
   {
      hEventJob    = CreateEvent(NULL, TRUE, FALSE, NULL);
    //hEventJobEnd = CreateEvent(NULL, TRUE, TRUE , NULL);
      hEventSetCursorBegin = CreateEvent(NULL, FALSE, FALSE, NULL);
      hEventSetCursorEnd   = CreateEvent(NULL, FALSE, FALSE, NULL);
   }
   {
      HANDLE hTreadParent;// = GetCurrentThread();
      BOOL res =
      DuplicateHandle(GetCurrentProcess(),
                      GetCurrentThread(),
                      GetCurrentProcess(),
                      &hTreadParent,
                      0, FALSE, DUPLICATE_SAME_ACCESS);
      if (!res) {
         DWORD errcode = GetLastError();
      }
      DWORD dwThreadID;
      HANDLE hTreadChild = chBEGINTHREADEX(NULL, 0, ChildThread, hTreadParent, 0, &dwThreadID);
   }/**/
   if (m_Serialize.m_bAutoloadAdmin) {
      nsPlayerName::firstLoad = true;
      PostMessage(m_hWnd, WM_COMMAND, MAKEWPARAM(ID_MENU_GAME_SELECTPLAYER,0), 0L);
   }

   if (szFileName)
      GameLoad(szFileName);
}

// WM_CREATE
BOOL CFastMines2Project::OnCreate(HWND handleWindow, LPCREATESTRUCT lpCreateStruct)
{
   m_hWnd = handleWindow;
   m_hWndTop = CreateWindow(SZ_CLASS_WND_TOP, NULL,
      WS_CHILD | WS_VISIBLE | WS_DLGFRAME,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWnd, (HMENU)0, ghInstance, NULL );
   m_BtnNew  .Create(m_hWndTop, ID_BUTTON_NEW_GAME);
   m_BtnPause.Create(m_hWndTop, ID_BUTTON_PAUSE);
   m_hWndEdtCount = CreateWindowEx( WS_EX_STATICEDGE, TEXT("edit"), NULL,
      WS_CHILD | WS_VISIBLE | //WS_BORDER | WS_DLGFRAME |
      ES_LEFT | ES_NUMBER, // | ES_READONLY,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndTop, (HMENU)ID_EDITCONTROL_COUNT, ghInstance, NULL );
   m_hWndEdtTimer = CreateWindowEx( WS_EX_STATICEDGE, TEXT("edit"), NULL,
      WS_CHILD | WS_VISIBLE | //WS_BORDER | WS_DLGFRAME |
      ES_LEFT | ES_NUMBER, // | ES_READONLY,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndTop, (HMENU)ID_EDITCONTROL_TIMER, ghInstance, NULL );
   m_Mosaic.Create(m_hWnd, ID_MOSAIC);

   return TRUE;
}

// WM_WINDOWPOSCHANGING
BOOL CFastMines2Project::OnWindowPosChanging(HWND, LPWINDOWPOS lpwpos) {
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("flags = 0x08X"), lpwpos->flags);
   if ((lpwpos->flags != (SWP_NOOWNERZORDER | SWP_NOACTIVATE | SWP_NOZORDER)) &&
       (lpwpos->flags !=  SWP_NOSIZE))
      return FORWARD_WM_WINDOWPOSCHANGING(m_hWnd, lpwpos, DefWindowProc);
   if (!IsWindowEnabled(m_hWnd) || !IsWindowVisible(m_hWnd))
      return FORWARD_WM_WINDOWPOSCHANGING(m_hWnd, lpwpos, DefWindowProc);

   const SIZE sizeScreen = GetScreenSize();
   const RECT rectMin = {sizeScreen.cx/50, sizeScreen.cy/40, //0,0,           //
                         sizeScreen.cx  -  sizeScreen.cx/50, //sizeScreen.x,  //
                         sizeScreen.cy  -  sizeScreen.cy/40};//sizeScreen.y}; //
   if (lpwpos->x+lpwpos->cx > rectMin.right ) lpwpos->x = sizeScreen.cx-lpwpos->cx;
   if (lpwpos->y+lpwpos->cy > rectMin.bottom) lpwpos->y = sizeScreen.cy-lpwpos->cy;
   if (lpwpos->x < rectMin.left) lpwpos->x = 0;
   if (lpwpos->y < rectMin.top ) lpwpos->y = 0;

   return 0;
}

// WM_WINDOWPOSCHANGED
void CFastMines2Project::OnWindowPosChanged(HWND hwnd, const LPWINDOWPOS lpwpos) {
   FORWARD_WM_WINDOWPOSCHANGED(m_hWnd, lpwpos, DefWindowProc);
}

// WM_MOVE
void CFastMines2Project::OnMove(HWND, int x, int y) {
   if (!IsWindowEnabled(m_hWnd) || !IsWindowVisible(m_hWnd)) return;
   RECTEX rectWin(GetWindowRect(m_hWnd));
   m_Serialize.m_PointCenter = rectWin.center();//GetScreenSize()/2;//
}

// WM_SIZE
void CFastMines2Project::OnSize(HWND, UINT state, int cx, int cy){
   //FORWARD_WM_SIZE(m_hWnd, state, cx, cy, DefWindowProc);
   if (!IsWindowEnabled(m_hWnd) || !IsWindowVisible(m_hWnd)) return;
   //if (state != SIZE_RESTORED) return;
   const SIZE sizeMosaic = GetSizeMosaicWindow();
   const SIZE sizeWindow = GetSize(sizeMosaic);
   const SIZE sizeScreen = GetScreenSize();
   int heightPnlTop = m_Serialize.m_bShowToolbar ? HEIGHT_PANEL_TOP : 0;
   const SIZE sizeClientPnlTop =
      {sizeMosaic.cx - 2*GetSystemMetrics(SM_CXDLGFRAME),
       heightPnlTop  - 2*GetSystemMetrics(SM_CYDLGFRAME)};

   {
      RECTEX rectWinNew(GetWindowRect(m_hWnd));
      rectWinNew.size(sizeWindow);
      POINTEX c1(rectWinNew.center());
      rectWinNew.center(m_Serialize.m_PointCenter);
      POINTEX c2(rectWinNew.center());
      {/**/
         if (rectWinNew.right  > sizeScreen.cx) rectWinNew.alignRight (sizeScreen.cx);
         if (rectWinNew.bottom > sizeScreen.cy) rectWinNew.alignBottom(sizeScreen.cy);
         if (rectWinNew.left   < 0            ) rectWinNew.alignLeft  (0);
         if (rectWinNew.top    < 0            ) rectWinNew.alignTop   (0);
         /**/
      }
      MoveWindow(m_hWnd, rectWinNew);
      //::SetWindowPos(m_hWnd, NULL, rectWinNew, SWP_NOZORDER | SWP_NOMOVE);
      //FORWARD_WM_SIZE(m_hWnd, state, cx, cy, DefWindowProc);
   }
   MoveWindow(m_Mosaic.GetHandle(),
       0, heightPnlTop,
       sizeMosaic.cx,
       sizeMosaic.cy,
      TRUE);
   MoveWindow(m_BtnPause.GetHandle(),
      sizeClientPnlTop.cx/2,
      sizeClientPnlTop.cy/2 - HEIGHT_WIDTH_BUTTON_NEW_PAUSE/2,
      HEIGHT_WIDTH_BUTTON_NEW_PAUSE,
      HEIGHT_WIDTH_BUTTON_NEW_PAUSE,
      TRUE); //
   MoveWindow(m_BtnNew.GetHandle(),
      sizeClientPnlTop.cx/2 - HEIGHT_WIDTH_BUTTON_NEW_PAUSE,
      sizeClientPnlTop.cy/2 - HEIGHT_WIDTH_BUTTON_NEW_PAUSE/2,
      HEIGHT_WIDTH_BUTTON_NEW_PAUSE,
      HEIGHT_WIDTH_BUTTON_NEW_PAUSE,
      TRUE); //
   int w = sizeMosaic.cx/2-HEIGHT_WIDTH_BUTTON_NEW_PAUSE;
   int x = w;
   if (x > EDIT_COUNT_TIMER.cx) x = EDIT_COUNT_TIMER.cx;
   int ofs =  w/2 - x/2;
   if (ofs > 10) ofs = 10;
   if (ofs < 10) x -= 3;
   MoveWindow(m_hWndEdtCount,
      ofs,
      sizeClientPnlTop.cy/2 - EDIT_COUNT_TIMER.cy/2,
      x,
      EDIT_COUNT_TIMER.cy,
      TRUE); //
   MoveWindow(m_hWndEdtTimer,
      sizeClientPnlTop.cx - x - ofs,
      sizeClientPnlTop.cy/2 - EDIT_COUNT_TIMER.cy/2,
      x,
      EDIT_COUNT_TIMER.cy,
      TRUE); //
   MoveWindow(m_hWndTop,
      0, 0,
      sizeMosaic.cx,
      heightPnlTop,
      TRUE);
   InvalidateRect(m_hWndTop, NULL, TRUE);
   {
      LONG style = GetWindowLong(m_hWnd, GWL_STYLE);
      if (GetMaximalArea() == m_Mosaic.GetArea())
         SetWindowLong(m_hWnd, GWL_STYLE, style & (WS_MAXIMIZEBOX^0xFFFFFFFFL));
      else
         SetWindowLong(m_hWnd, GWL_STYLE, style | WS_MAXIMIZEBOX);
      SendMessage(m_hWnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
   }
}

// WM_DESTROY
void CFastMines2Project::OnDestroy(HWND){
   //WaitForSingleObject(hEventJobEnd, INFINITE);
   SerializeOut();
   if (m_pCaptionButton) {
      delete m_pCaptionButton;
      m_pCaptionButton = NULL;
   }
   PostQuitMessage(0);
}

// WM_NCLBUTTONDOWN
void CFastMines2Project::OnNCLButtonDown(HWND, BOOL fDoubleClick, int x, int y, UINT codeHitTest) {
#ifdef _DEBUG
   g_Logger.Put(CLogger::LL_DEBUG, fDoubleClick ? TEXT("WM_NCLBUTTONDBLCLK") : TEXT("WM_NCLBUTTONDOWN"));
#endif
   FORWARD_WM_NCLBUTTONDOWN(m_hWnd, fDoubleClick, x, y, codeHitTest, DefWindowProc);
}

// WM_NCLBUTTONUP
void CFastMines2Project::OnNCLButtonUp(HWND, int x, int y, UINT codeHitTest) {
#ifdef _DEBUG
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("WM_NCLBUTTONUP"));
#endif
   FORWARD_WM_NCLBUTTONUP(m_hWnd, x, y, codeHitTest, DefWindowProc);
}

// WM_SYSCOMMAND
void CFastMines2Project::OnSysCommand(HWND, UINT cmd, int x, int y) {
#ifdef _DEBUG
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("cmd (WM_SYSCOMMAND) = 0x%08X"), cmd);
#endif
   switch (cmd) {
   case SC_MAXIMIZE:
 //case 0xF012: // one    click in caption
   case 0xF032: // double click in caption
      FORWARD_WM_COMMAND(m_hWnd, ID_KEY_AREA_MAX, m_hWnd, 2, SendMessage);
      return;
   }
   FORWARD_WM_SYSCOMMAND(m_hWnd, cmd, x, y, DefWindowProc);
}

// WM_GETMINMAXINFO
void CFastMines2Project::OnGetMinMaxInfo(HWND, LPMINMAXINFO lpMinMaxInfo) {
   if (!IsWindowEnabled(m_hWnd) || !IsWindowVisible(m_hWnd)) return;
 //lpMinMaxInfo->ptMaxSize      = GetSize(GetMaximalArea());
 //lpMinMaxInfo->ptMaxPosition  = POINTEX(0,0);
 //lpMinMaxInfo->ptMinTrackSize = POINTEX(0,0);
 //lpMinMaxInfo->ptMaxTrackSize = POINTEX(0,0);
   FORWARD_WM_GETMINMAXINFO(m_hWnd, lpMinMaxInfo, DefWindowProc);
}

// WM_COMMAND
void CFastMines2Project::OnCommand(HWND, int id, HWND hwndCtl, UINT codeNotify){
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("OnCommand: id=%d; hwndCtl=0x%08X; codeNotify=%d"), id, hwndCtl, codeNotify);
   switch (id){
   // menu File
   case ID_MENU_GAME_NEW_GAME:
      m_Mosaic.GameNew();
      return;
   case ID_MENU_GAME_BEGINNER:
      CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + nsMosaic::skillLevelBeginner,
         MF_BYCOMMAND);
      m_Mosaic.SetSkillLevel(nsMosaic::skillLevelBeginner);
      return;
   case ID_MENU_GAME_AMATEUR:
      CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + nsMosaic::skillLevelAmateur,
         MF_BYCOMMAND);
      m_Mosaic.SetSkillLevel(nsMosaic::skillLevelAmateur);
      return;
   case ID_MENU_GAME_PROFESSIONAL:
      CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + nsMosaic::skillLevelProfessional,
         MF_BYCOMMAND);
      m_Mosaic.SetSkillLevel(nsMosaic::skillLevelProfessional);
      return;
   case ID_MENU_GAME_CRAZY:
      CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + nsMosaic::skillLevelCrazy,
         MF_BYCOMMAND);
      m_Mosaic.SetSkillLevel(nsMosaic::skillLevelCrazy);
      return;
   case ID_MENU_GAME_CUSTOM:
      if (DialogBox(ghInstance, TEXT("CustomSkillLevel"), m_hWnd, (DLGPROC)nsCustomSkill::DialogProc) == IDOK) {
         if ((nsCustomSkill::curSizeMosaic == GetSizeMosaic()) &&
             (nsCustomSkill::curMines      == GetMines())) return;

         m_Mosaic.SetSkillLevelCustom(nsCustomSkill::curSizeMosaic, nsCustomSkill::curMines);
         CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
            ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
            ID_MENU_GAME_BEGINNER + GetSkillLevel(),
            MF_BYCOMMAND);
      }
      return;
   case ID_MENU_GAME_CREATE:
      m_Mosaic.GameCreate();
      return;
   case ID_MENU_GAME_SAVE:
      GameSave();
      return;
   case ID_MENU_GAME_LOAD:
      GameLoad();
      return;
   case ID_MENU_GAME_SELECTPLAYER:
      /**/
      m_Serialize.m_bAutoloadAdmin = (
         BST_CHECKED ==
         DialogBox(ghInstance, TEXT("SelectPlayer"), m_hWnd, (DLGPROC)nsPlayerName::DialogProc)
      );
      /**/
      return;
   case ID_MENU_GAME_EXIT:
      SendMessage(m_hWnd, WM_DESTROY, 0L, 0L);
      return;
   // menu m_Mosaic
   case ID_MENU_MOSAIC_TRIANGLE1:
   case ID_MENU_MOSAIC_TRIANGLE2:
   case ID_MENU_MOSAIC_TRIANGLE3:
   case ID_MENU_MOSAIC_TRIANGLE4:
   case ID_MENU_MOSAIC_SQUARE1:
   case ID_MENU_MOSAIC_SQUARE2:
   case ID_MENU_MOSAIC_PARQUET1:
   case ID_MENU_MOSAIC_PARQUET2:
   case ID_MENU_MOSAIC_TRAPEZOID1:
   case ID_MENU_MOSAIC_TRAPEZOID2:
   case ID_MENU_MOSAIC_TRAPEZOID3:
   case ID_MENU_MOSAIC_RHOMBUS1:
   case ID_MENU_MOSAIC_QUADRANGLE1:
   case ID_MENU_MOSAIC_PENTAGONT24:
   case ID_MENU_MOSAIC_PENTAGONT5:
   case ID_MENU_MOSAIC_PENTAGONT10:
   case ID_MENU_MOSAIC_HEXAGON1:
   case ID_MENU_MOSAIC_TRSQ1:
   case ID_MENU_MOSAIC_TRSQ2:
   case ID_MENU_MOSAIC_SQTRHEX:
      {
         nsMosaic::EMosaic eMosaic = nsMosaic::EMosaic(id-ID_MENU_MOSAIC_TRIANGLE1);
         SET_MOSAIC(eMosaic);
      }
      return;
   // menu Options
   case ID_MENU_OPTIONS_ALWAYSMAXSIZE:
      m_Serialize.m_bAlwaysMaxSize = !m_Serialize.m_bAlwaysMaxSize;
      Apply_AlwaysMaxSize();
      return;
   case ID_MENU_OPTIONS_USE_UNKNOWN:
      m_Mosaic.SetUseUnknown(!m_Mosaic.GetUseUnknown());
      Apply_UseUnknown();
      return;
   case ID_MENU_OPTIONS_SHOW_TOOLBAR:
      m_Serialize.m_bShowToolbar = !m_Serialize.m_bShowToolbar;
      Apply_ShowToolbar();
      return;
   case ID_MENU_OPTIONS_SHOW_MENU:
      m_Serialize.m_bShowMenu = !m_Serialize.m_bShowMenu;
      Apply_ShowMenu();
      return;
   case ID_MENU_OPTIONS_SHOW_CAPTION:
      m_Serialize.m_bShowCaption = !m_Serialize.m_bShowCaption;
      Apply_ShowCaption();
      return;
   case ID_MENU_OPTIONS_TO_TRAY:
      m_Serialize.m_bToTray = !m_Serialize.m_bToTray;
      Apply_ToTray();
      return;
   case ID_MENU_OPTIONS_SKIN_CHANGE:
      DialogBox(ghInstance, TEXT("ChangeSkinDialog"), m_hWnd, (DLGPROC)nsChangeSkin::DialogProc);
      return;
   case ID_MENU_OPTIONS_SKIN_SAVE:
      DialogBox(ghInstance, TEXT("InputText"), m_hWnd, (DLGPROC)nsFileSkin::DialogProc);
      nsFileSkin::ReloadMenuList(HSUBMENU_SKIN);
      return;
   case ID_MENU_OPTIONS_SKIN_LOAD:
      //FORWARD_WM_SYSKEYDOWN(m_hWnd, VK_MENU, 0x0001, 0x2038, SendMessage);
      //FORWARD_WM_SYSKEYDOWN(m_hWnd, 0x4F   , 0x0001, 0x2018, SendMessage);
      //FORWARD_WM_SYSKEYUP  (m_hWnd, VK_MENU, 0,0, SendMessage);
      //FORWARD_WM_SYSKEYUP  (m_hWnd, 0x4F   , 0,0, SendMessage);
      //FORWARD_WM_SYSCHAR   (m_hWnd, 0x6F   , 0x20180001,   SendMessage);
      //FORWARD_WM_SYSKEYUP  (m_hWnd, VK_MENU, 0,0, SendMessage);
      return;
   case ID_MENU_OPTIONS_SKIN_LOAD0:
      SetSkin(CSkin());
      DrawMenuBar(m_hWnd);
      return;
   case ID_MENU_OPTIONS_LANG_LOAD0:
      CLang::RestoreDefaultLanguage();
      lstrcpy(m_Serialize.m_szLanguage, TEXT("English.lng"));
      CLang(m_Serialize.m_szLanguage);
      Menu_ChangeLanguage();
      return;
   default:
      if (codeNotify == 0) //  from menu
      {
         if (id > ID_MENU_OPTIONS_LANG_LOAD0) {
            // определяю язык
            GetMenuString(HSUBMENU_LANGUAGE, id, m_Serialize.m_szLanguage, MAX_LANGUAGE_LENGTH, MF_BYCOMMAND);
            lstrcat(m_Serialize.m_szLanguage, TEXT(".lng"));
            CLang newLanguage(m_Serialize.m_szLanguage);
            Menu_ChangeLanguage();
         } else
         if (id > ID_MENU_OPTIONS_SKIN_LOAD0) {
            SetSkin(nsFileSkin::LoadSkin(HSUBMENU_SKIN, id));
            DrawMenuBar(m_hWnd);
         }
      }
      return;
   // menu Help
   case ID_MENU_HELP_CAMPIONS:
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), m_hWnd, (DLGPROC)nsChampions::DialogProc);
      return;
   case ID_MENU_HELP_STATISTICS:
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), m_hWnd, (DLGPROC)nsStatistics::DialogProc);
      return;
   case ID_MENU_HELP_ASSISTANT_ONOFF:
      //m_Serialize.m_AssistantInfo.m_bUse = !m_Serialize.m_AssistantInfo.m_bUse;
      Apply_UseAssistant();
      return;
   case ID_MENU_HELP_ASSISTANT_SUGGEST:
      //bOpenCellAssistant = false;
      //SetEvent(hEventJob);
      return;
   case ID_MENU_HELP_ASSISTANT_OPTIONS:
      DialogBox(ghInstance, TEXT("AssistantDialog"), m_hWnd, (DLGPROC)nsAssistant::DialogProc);
      return;
   case ID_MENU_HELP_ABOUT:
      DialogBox(ghInstance, TEXT("AboutDialog"), m_hWnd, (DLGPROC)nsAbout::DialogProc);
      return;
   // Accelerators
   case ID_MINIMIZE:
      m_Mosaic.SetPause(true);
      ShowWindow(m_hWnd, SW_MINIMIZE);
      return;
   case ID_BUTTON_PAUSE:
      m_Mosaic.SetPause(!m_Mosaic.GetPause());
      return;
   case ID_KEY_AREA_MAX:
   case ID_KEY_AREA_MIN:
   case ID_KEY_AREA_INCREMENT:
   case ID_KEY_AREA_DECREMENT:
      if (!m_Serialize.m_bAlwaysMaxSize) {
         bool noChanged;
         switch (id){
         case ID_KEY_AREA_MAX:       noChanged = AreaMax();       break;
         case ID_KEY_AREA_MIN:       noChanged = AreaMin();       break;
         case ID_KEY_AREA_INCREMENT: noChanged = AreaIncrement(); break;
         case ID_KEY_AREA_DECREMENT: noChanged = AreaDecrement(); break;
         }
         if (noChanged) MessageBeep(0);
      }
      return;
 case ID_KEY_NUM0:
/**
      {
         RECTEX wndRect; GetClientRect(m_hWnd, &wndRect);
         RECTEX fillRect(wndRect.left+10, wndRect.top+10, wndRect.right-10, wndRect.bottom-10);
         nsEraseBk::FillWnd(m_hWnd, 0, false, fillRect);
         HDC hDC = GetDC(m_hWnd);
         //CFastMines2Project::GetImageMosaic((nsMosaic::EMosaic)0)->Draw(hDC, &fillRect);
         BOOL res = DrawIconEx(hDC,
            fillRect.left, fillRect.top,
            CFastMines2Project::GetIconMosaic((nsMosaic::EMosaic)0),
            fillRect.width(),
            fillRect.height(),
            0,
            NULL,
            DI_NORMAL);
         if (!res) {
            DWORD errCode = GetLastError();
         }
         ReleaseDC(m_hWnd, hDC);
      }
/**
       {
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CXHSCROLL = %i"), GetSystemMetrics(SM_CXHSCROLL));
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CYHSCROLL = %i"), GetSystemMetrics(SM_CYHSCROLL));
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CXHTHUMB  = %i"), GetSystemMetrics(SM_CXHTHUMB ));
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CXVSCROLL = %i"), GetSystemMetrics(SM_CXVSCROLL));
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CYVSCROLL = %i"), GetSystemMetrics(SM_CYVSCROLL));
          g_Logger.Put(CLogger::LL_DEBUG, TEXT("SM_CYVTHUMB  = %i"), GetSystemMetrics(SM_CYVTHUMB ));
        }
/**/
       break;
 //case ID_KEY_NUM1:
 //case ID_KEY_NUM2:
   case ID_KEY_NUM3:
   case ID_KEY_NUM4:
   case ID_KEY_NUM5:
   case ID_KEY_NUM6:
   case ID_KEY_NUM7:
 //case ID_KEY_NUM8:
 //case ID_KEY_NUM9:
      {
         nsSelectMosaic::SetFirstKey(id-ID_KEY_NUM0);
         nsMosaic::EMosaic eMosaic =
            (nsMosaic::EMosaic) DialogBox(ghInstance, TEXT("SelectMosaic"), m_hWnd, (DLGPROC)nsSelectMosaic::DialogProc);
         if (eMosaic == nsMosaic::mosaicNil) return;
         SET_MOSAIC(eMosaic);
      }
      return;
   }
}

// WM_ACTIVATE
void CFastMines2Project::OnActivate(HWND, UINT state, HWND hwndActDeact, BOOL fMinimized){
   FORWARD_WM_ACTIVATE(m_hWnd, state, hwndActDeact, fMinimized, DefWindowProc);
//#ifndef _DEBUG
   if (state == WA_INACTIVE) {
      SetWindowPos(m_hWnd, HWND_BOTTOM , 0,0,0,0, SWP_NOSIZE | SWP_NOMOVE);
   } else {
      SetWindowPos(m_hWnd, HWND_TOPMOST, 0,0,0,0, SWP_NOSIZE | SWP_NOMOVE);
   }
//#endif
   if ((state == WA_INACTIVE) ||
       (fMinimized != false))
      m_Mosaic.SetPause(true);
   if ((fMinimized != false) && m_Serialize.m_bToTray) {
      TrayMessage(NIM_ADD, CLang::m_StrArr[IDS__LOGO_VERS]);
   }
}

// WM_KEYUP
void CFastMines2Project::OnKey(HWND, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   FORWARD_WM_KEYUP(m_hWnd, vk, cRepeat, flags, DefWindowProc);
   if ((vk == VK_SNAPSHOT) &&
        m_Mosaic.GetGameStatusIsPlay() &&
       !m_Mosaic.GetPause() &&
       !m_Mosaic.GetGameIsIgnorable())
      m_Mosaic.GameNew();
}

// WM_SYSKEYUP
void CFastMines2Project::OnSysKey(HWND, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   FORWARD_WM_SYSKEYUP(m_hWnd, vk, cRepeat, flags, DefWindowProc);
   if ((vk == VK_SNAPSHOT) &&
        m_Mosaic.GetGameStatusIsPlay() &&
       !m_Mosaic.GetPause() &&
       !m_Mosaic.GetGameIsIgnorable())
      m_Mosaic.GameNew();
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_MENUSELECT
void CFastMines2Project::OnMenuSelect(HWND, HMENU hmenu, int item, HMENU hmenuPopup, UINT flags) {
   FORWARD_WM_MENUSELECT(m_hWnd, hmenu, item, hmenuPopup, flags, DefWindowProc);
   if ((m_Serialize.m_Skin.m_bToAll) && (hmenu==m_hMenu))
      nsEraseBk::FillMenu(m_hWnd, m_Serialize.m_Skin.m_colorBk);
}

// WM_PAINT
void CFastMines2Project::OnPaint(HWND) {
   FORWARD_WM_PAINT(m_hWnd, DefWindowProc);
   if (m_Serialize.m_Skin.m_bToAll) {
    //nsEraseBk::FillCaption(m_hWnd, m_Serialize.m_Skin.m_colorBk);
      nsEraseBk::FillMenu   (m_hWnd, m_Serialize.m_Skin.m_colorBk);
   }
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_NOTIFYICON
void CFastMines2Project::OnNotifyIcon(HWND, UINT notifyMsg) {
   switch (notifyMsg) {
   case WM_LBUTTONDOWN:
   case WM_RBUTTONDOWN:
      TrayMessage(NIM_DELETE, NULL);
   }
}

// WM_MEASUREITEM
void CFastMines2Project::OnMeasureItem(HWND hwnd, MEASUREITEMSTRUCT *lpMeasureItem) {
   switch (lpMeasureItem->itemID) {
   case ID_MENU_MOSAIC_TRIANGLE1  :
   case ID_MENU_MOSAIC_TRIANGLE2  :
   case ID_MENU_MOSAIC_TRIANGLE3  :
   case ID_MENU_MOSAIC_TRIANGLE4  :
   case ID_MENU_MOSAIC_SQUARE1    :
   case ID_MENU_MOSAIC_SQUARE2    :
   case ID_MENU_MOSAIC_PARQUET1   :
   case ID_MENU_MOSAIC_PARQUET2   :
   case ID_MENU_MOSAIC_TRAPEZOID1 :
   case ID_MENU_MOSAIC_TRAPEZOID2 :
   case ID_MENU_MOSAIC_TRAPEZOID3 :
   case ID_MENU_MOSAIC_RHOMBUS1   :
   case ID_MENU_MOSAIC_QUADRANGLE1:
   case ID_MENU_MOSAIC_PENTAGONT24:
   case ID_MENU_MOSAIC_PENTAGONT5 :
   case ID_MENU_MOSAIC_PENTAGONT10:
   case ID_MENU_MOSAIC_HEXAGON1   :
   case ID_MENU_MOSAIC_TRSQ1      :
   case ID_MENU_MOSAIC_TRSQ2      :
   case ID_MENU_MOSAIC_SQTRHEX    :
      CMenuItem::OnMeasureItem(lpMeasureItem);
   }
}

// WM_DRAWITEM
void CFastMines2Project::OnDrawItem(HWND hwnd, const DRAWITEMSTRUCT *lpDrawItem) {
   switch (lpDrawItem->itemID) {
   case ID_MENU_MOSAIC_TRIANGLE1  :
   case ID_MENU_MOSAIC_TRIANGLE2  :
   case ID_MENU_MOSAIC_TRIANGLE3  :
   case ID_MENU_MOSAIC_TRIANGLE4  :
   case ID_MENU_MOSAIC_SQUARE1    :
   case ID_MENU_MOSAIC_SQUARE2    :
   case ID_MENU_MOSAIC_PARQUET1   :
   case ID_MENU_MOSAIC_PARQUET2   :
   case ID_MENU_MOSAIC_TRAPEZOID1 :
   case ID_MENU_MOSAIC_TRAPEZOID2 :
   case ID_MENU_MOSAIC_TRAPEZOID3 :
   case ID_MENU_MOSAIC_RHOMBUS1   :
   case ID_MENU_MOSAIC_QUADRANGLE1:
   case ID_MENU_MOSAIC_PENTAGONT24:
   case ID_MENU_MOSAIC_PENTAGONT5 :
   case ID_MENU_MOSAIC_PENTAGONT10:
   case ID_MENU_MOSAIC_HEXAGON1   :
   case ID_MENU_MOSAIC_TRSQ1      :
   case ID_MENU_MOSAIC_TRSQ2      :
   case ID_MENU_MOSAIC_SQTRHEX    :
      CMenuItem::OnDrawItem(lpDrawItem);
   }
}

// WM_MOSAIC_ADJUSTAREA
void CFastMines2Project::OnMosaicAdjustArea(HWND) {
   int maxArea = GetMaximalArea();
   if (m_Mosaic.GetArea() > maxArea)
      m_Mosaic.SetArea(maxArea);
}

// WM_MOSAIC_CLICK
void CFastMines2Project::OnMosaicClick(HWND, UINT keyFlags, BOOL leftClick, BOOL down) {
   if (leftClick) {
      if (down) {
         SendMessage(m_BtnNew.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnNew[1]);
      } else {
         SendMessage(m_BtnNew.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnNew[0]);
      }
   } else {
   }
}

// WM_MOSAIC_CHANGECOUNTERS
void CFastMines2Project::OnMosaicChangeCounters(HWND) {
   CString strCaption;
   if (m_Serialize.m_bShowToolbar) {
      strCaption.Format(TEXT("%i"), m_Mosaic.GetCountTimer());
      SetWindowText(m_hWndEdtTimer, strCaption);
      strCaption.Format(TEXT("%i"), m_Mosaic.GetCountMines());
      SetWindowText(m_hWndEdtCount, strCaption);
      strCaption = CLang::m_StrArr[IDS__LOGO_VERS];
   } else {
      strCaption.Format(
         TEXT("%s  -  %s[%i]  %s[%i %s]"),
         (LPCTSTR)CLang::m_StrArr[IDS__LOGO_VERS],
         (LPCTSTR)CLang::m_StrArr[IDS__MINES    ], m_Mosaic.GetCountMines(),
         (LPCTSTR)CLang::m_StrArr[IDS__TIME     ], m_Mosaic.GetCountTimer(),
         (LPCTSTR)CLang::m_StrArr[IDS__SEC      ]
      );
   }
   if (m_Mosaic.GetGameStatusIsEnd()) {
      if (m_Mosaic.GetVictory())
         strCaption += (TEXT(" - ")+CLang::m_StrArr[IDS__VICTORY]);
      else
         strCaption += (TEXT(" - ")+CLang::m_StrArr[IDS__DEFEAT ]);
   }
   SetWindowText(m_hWnd, strCaption);

   //if (m_Mosaic.GetGameStatusIsCreate())
   {
      CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + GetSkillLevel(),
         MF_BYCOMMAND);
   }
}

// WM_MOSAIC_GAMENEW
void CFastMines2Project::OnMosaicGameNew(HWND) {
   SendMessage(m_BtnNew.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnNew[0]);
   if (m_Serialize.m_bAlwaysMaxSize) AreaMax();
   EnableMenuItem(GetSubMenu(m_hMenu, 0), ID_MENU_GAME_SAVE, MF_BYCOMMAND | MF_GRAYED);
}

void CFastMines2Project::OnMosaicGameBegin(HWND hwnd) {
   EnableMenuItem(GetSubMenu(m_hMenu, 0), ID_MENU_GAME_SAVE, MF_BYCOMMAND | MF_ENABLED);
}

// WM_MOSAIC_GAMEEND
void CFastMines2Project::OnMosaicGameEnd(HWND) {
   if (m_Mosaic.GetVictory()) {
      SendMessage(m_BtnNew.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnNew[2]);
      if (!m_Mosaic.GetGameIsIgnorable() && m_Mosaic.GetPlayerIsUser())
         nsChampions::SaveResult(GetMosaic(), GetSkillLevel(), m_Mosaic.GetCountTimer(), m_Serialize.m_szPlayerName);
   } else {
      SendMessage(m_BtnNew.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnNew[3]);
   }

   if ((m_Mosaic.GetSkillLevel() != nsMosaic::skillLevelCustom) &&
       !m_Mosaic.GetGameIsIgnorable() &&
       (m_Mosaic.GetPlayerIsUser () ||
        m_Mosaic.GetPlayerIsAssistant()))
   {
      bool victory = m_Mosaic.GetVictory();
      nsStatistics::CSttstcSubRecord statisticsResult;
      statisticsResult.m_dwGameNumber = 1;
      statisticsResult.m_dwGameWin    = victory;
      statisticsResult.m_dwOpenField  = victory ?
                                       GetSizeMosaic().cx*GetSizeMosaic().cy-DefineNumberMines():
                                       m_Mosaic.GetCountOpen();
      statisticsResult.m_dwPlayTime   = victory ? m_Mosaic.GetCountTimer() : 0;
      statisticsResult.m_dwClickCount = victory ? m_Mosaic.GetCountClick() : 0;

      if (m_Mosaic.GetPlayerIsUser())
         nsStatistics::InsertResult(statisticsResult, GetMosaic(), m_Mosaic.GetSkillLevel(), m_Serialize.m_szPlayerName);
      if (m_Mosaic.GetPlayerIsAssistant())
         nsStatistics::InsertResult(statisticsResult, GetMosaic(), m_Mosaic.GetSkillLevel(), nsPlayerName::SZ_ASSISTANT_NAME_DEFAULT);
   }
}

// WM_MOSAIC_PAUSE
void CFastMines2Project::OnMosaicPause(HWND) {
   if (!(m_Mosaic.GetGameStatusIsPlay())) {
      SendMessage(m_BtnPause.GetHandle(), BM_SETCHECK, (WPARAM)BST_UNCHECKED, 0L);
      SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnPause[2]);
      EnableWindow(m_BtnPause.GetHandle(), false);
      return;
   }
   EnableWindow(m_BtnPause.GetHandle(), true);
   if (m_Mosaic.GetPause()) {
      SendMessage(m_BtnPause.GetHandle(), BM_SETCHECK, (WPARAM)BST_CHECKED, 0L);
      SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnPause[1]);
   } else {
      SendMessage(m_BtnPause.GetHandle(), BM_SETCHECK, (WPARAM)BST_UNCHECKED, 0L);
      SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, 0L, (LPARAM)&m_ImgBtnPause[0]);
   }
}

#ifndef WHEEL_DELTA
   #define WHEEL_DELTA       120       /* Value for rolling one detent */
#endif

// WM_MOUSEWHEEL
void CFastMines2Project::OnMouseWheel(HWND hWnd, WORD fwKeys, short zDelta, short xPos, short yPos) {
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("OnMouseWheel: fwKeys=0x%04X, zDelta=%d, xPos=%d, yPos=%d"),  fwKeys, zDelta, xPos, yPos);
   //const int k = abs(zDelta)/WHEEL_DELTA;
   //for (int i=0; i<k; i++)
   {
      FORWARD_WM_COMMAND(hWnd, (zDelta > 0) ? ID_KEY_AREA_INCREMENT : ID_KEY_AREA_DECREMENT, NULL, 0, PostMessage);
   }
}
////////////////////////////////////////////////////////////////////////////////
//                     обработчики сообщений hPnlTop
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_ERASEBKGND
BOOL CFastMines2Project::OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!m_Serialize.m_Skin.m_bToAll)
      return FALSE; // FORWARD_WM_ERASEBKGND(hwnd, hdc, DefWindowProc);
   return nsEraseBk::OnEraseBkgnd(hwnd, hdc, m_Serialize.m_Skin.m_colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_COMMAND
void CFastMines2Project::TopOnCommand(HWND, int id, HWND hwndCtl, UINT codeNotify){
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("TopOnCommand: id = 0x%08X"), id);
   //FORWARD_WM_COMMAND(m_hWndTop, id, hwndCtl, codeNotify, DefWindowProc);
   switch (id){
   case ID_EDITCONTROL_COUNT:
   case ID_EDITCONTROL_TIMER:
      if (codeNotify == EN_SETFOCUS) {
         EnableWindow(m_hWndEdtCount, false);
         EnableWindow(m_hWndEdtCount, true );
         EnableWindow(m_hWndEdtTimer, false);
         EnableWindow(m_hWndEdtTimer, true );
      }
      return;
   case ID_BUTTON_NEW_GAME:
      if (hwndCtl == m_BtnNew.GetHandle()){
         FORWARD_WM_COMMAND(m_hWnd, ID_BUTTON_NEW_GAME, hwndCtl, 0, SendMessage);
      }
      return;
   case ID_BUTTON_PAUSE:
      if (hwndCtl == m_BtnPause.GetHandle()){
         FORWARD_WM_COMMAND(m_hWnd, ID_BUTTON_PAUSE, hwndCtl, 0, SendMessage);
      }
      return;
   }
}
////////////////////////////////////////////////////////////////////////////////
//                             other function
////////////////////////////////////////////////////////////////////////////////
inline void CFastMines2Project::TrayMessage(UINT msg, const TCHAR *pTip) {
   NOTIFYICONDATA nid = {
      sizeof(NOTIFYICONDATA),          //DWORD cbSize
      m_hWnd,                          //HWND  hWnd
      0,                               //UINT  uID
      NIF_ICON | NIF_MESSAGE | NIF_TIP,//UINT  uFlags
      WM_NOTIFYICON,                   //UINT  uCallbackMessage
      m_hIconProject,                  //HICON hIcon
      0                                //TCHAR szTip[64]
   };
   memcpy(
      nid.szTip,
      pTip ? pTip : CLang::m_StrArr[IDS__LOGO_VERS],
      min(63*sizeof(TCHAR), lstrlen(pTip ? pTip : CLang::m_StrArr[IDS__LOGO_VERS]))
   );
   Shell_NotifyIcon(msg, &nid);
   if (msg == NIM_ADD)
      ShowWindow(m_hWnd, SW_HIDE);
   if (msg == NIM_DELETE) {
      ShowWindow(m_hWnd, SW_RESTORE);
      //FORWARD_WM_ACTIVATE(m_hWnd, WA_ACTIVE, NULL, FALSE, PostMessage);
      //FORWARD_WM_SETFOCUS(m_hWnd, NULL, PostMessage);
      //ShowWindow(m_hWnd, SW_SHOW);
      //EnableWindow(m_hWnd, TRUE);
      //SetFocus(m_hWnd);
      //SetActiveWindow(m_hWnd);
   }
}

SIZE CFastMines2Project::GetSize(const SIZE &sizeMosaicInPixel) const {
   // узнать размер окна проекта
   // при указанном размере окна мозаики
   RECTEX windowRect, clientRect;
   GetWindowRect(m_hWnd, &windowRect);
   GetClientRect(m_hWnd, &clientRect);
   SIZE result = {
      (windowRect.width()) -
      (clientRect.width()) + sizeMosaicInPixel.cx,
      (windowRect.height()) -
      (clientRect.height()) + sizeMosaicInPixel.cy + (m_Serialize.m_bShowToolbar ? HEIGHT_PANEL_TOP : 0)};
   return result;
}

int CFastMines2Project::GetMaximalArea() const {
   // узнаю мах размер площади ячеек мозаики
   // при котором окно проекта вмещается в текущее разрешение экрана.
   int result;
   const SIZE sizeScreen = GetScreenSize();
   result = nsMosaic::AREA_MINIMUM;
   result++;
   SIZE sizeWnd = GetSize(result);
   while ((sizeWnd.cx <= sizeScreen.cx) &&
          (sizeWnd.cy <= sizeScreen.cy)) {
      result++;
      sizeWnd = GetSize(result);
   }
   result--;
   return result;
}

bool CFastMines2Project::AreaIncrement() {
   // возвращает true когда отсутствовали изменения
   int maxArea = GetMaximalArea();
   if (maxArea == m_Mosaic.GetArea()) return true;
   int newArea = min(maxArea, m_Mosaic.GetArea()*105.0f/100);
   m_Mosaic.SetArea(newArea);
   return false;
}

bool CFastMines2Project::AreaDecrement() {
   // возвращает true когда отсутствовали изменения
   if (m_Mosaic.GetArea() == nsMosaic::AREA_MINIMUM) return true;
   int newArea = max(nsMosaic::AREA_MINIMUM, m_Mosaic.GetArea()*95.0f/100);
   m_Mosaic.SetArea(newArea);
   return false;
}

bool CFastMines2Project::AreaMax() {
   if (!IsWindowEnabled(m_hWnd) || !IsWindowVisible(m_hWnd)) return (true || true); // пофиг что возвращать...
   // возвращает true когда отсутствовали изменения
   int maxArea = GetMaximalArea();
   if (maxArea == m_Mosaic.GetArea()) return true;
   m_Mosaic.SetArea(maxArea);
   {
      // Если до вызова AreaMax() меню окна распологалось в две строки, то после
      // отработки этой ф-ции меню будет в одну строку, т.е., последующий вызов
      // GetMaximalArea() будет возвращать ещё бОльший результат.
      // Поэтому надо снова установить максимальное значение плошади ячеек.
      if (maxArea < GetMaximalArea())
         AreaMax(); // меню было в две  строки
      else;         // меню было в одну строку
   }
   return false;
}

bool CFastMines2Project::AreaMin() {
   // возвращает true когда отсутствовали изменения
   if (m_Mosaic.GetArea() == nsMosaic::AREA_MINIMUM) return true;
   m_Mosaic.SetArea(nsMosaic::AREA_MINIMUM);
   return false;
}

inline void CFastMines2Project::CheckMenuRadioItem_Mosaic(nsMosaic::EMosaic eMosaic) const {
   BOOL bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1), 0, 4,
      nsSelectMosaic::GetGroup(eMosaic), MF_BYPOSITION);
   bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1),
      ID_MENU_MOSAIC_TRIANGLE1, ID_MENU_MOSAIC_TRIANGLE4,
      ID_MENU_MOSAIC_TRIANGLE1 + eMosaic, MF_BYCOMMAND);
   bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1),
      ID_MENU_MOSAIC_SQUARE1, ID_MENU_MOSAIC_QUADRANGLE1,
      ID_MENU_MOSAIC_TRIANGLE1 + eMosaic, MF_BYCOMMAND);
   bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1),
      ID_MENU_MOSAIC_PENTAGONT24, ID_MENU_MOSAIC_PENTAGONT10,
      ID_MENU_MOSAIC_TRIANGLE1 + eMosaic, MF_BYCOMMAND);
   bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1),
      ID_MENU_MOSAIC_HEXAGON1, ID_MENU_MOSAIC_HEXAGON1,
      ID_MENU_MOSAIC_TRIANGLE1 + eMosaic, MF_BYCOMMAND);
   bRes = CheckMenuRadioItem(GetSubMenu(m_hMenu, 1),
      ID_MENU_MOSAIC_TRSQ1, ID_MENU_MOSAIC_SQTRHEX,
      ID_MENU_MOSAIC_TRIANGLE1 + eMosaic, MF_BYCOMMAND);
}

BOOL CFastMines2Project::SerializeIn() {
   // read init file

   HANDLE hFile = INVALID_HANDLE_VALUE;
   hFile = CreateFile(
      GetModuleDir(ghInstance) + SZ_FILE_NAME_INIT,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   BOOL bResult = FALSE;
   if (hFile == INVALID_HANDLE_VALUE) {
      bResult = FALSE;
      // file not found...
      //MessageBox(m_hWnd, CLang::m_StrArr[IDS__INI_FILE__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
   } else {
      DWORD dwNOBR;
      if (!ReadFile(hFile, &m_Serialize, sizeof(m_Serialize), &dwNOBR, NULL) ||
          (sizeof(m_Serialize) != dwNOBR)
         )
      {
         m_Serialize = CSerializeProj();
         MessageBox(m_hWnd, CLang::m_StrArr[IDS__INI_FILE__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         if (lstrcmp(m_Serialize.m_szVersion, TEXT(ID_VERSIONINFO_VERSION3))) {
            m_Serialize = CSerializeProj();
            MessageBox(m_hWnd, CLang::m_StrArr[IDS__INI_FILE__ERROR_VERSION], CLang::m_StrArr[IDS__INFORMATION], MB_ICONINFORMATION | MB_OK);
         } else {
            bResult = m_Mosaic.SerializeIn(hFile);
         }
      }
   }

   if (hFile != INVALID_HANDLE_VALUE)
      CloseHandle(hFile);

   Apply_ShowToolbar();
   Apply_ShowMenu();
   Apply_ShowCaption();
   Apply_AlwaysMaxSize();
   Apply_ToTray();
   Apply_UseAssistant();
   Apply_UseUnknown();
   CheckMenuRadioItem_Mosaic(GetMosaic());
   CheckMenuRadioItem(GetSubMenu(m_hMenu, 0),
      ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
      ID_MENU_GAME_BEGINNER + m_Mosaic.GetSkillLevel(), MF_BYCOMMAND);
   return bResult;
}

BOOL CFastMines2Project::SerializeOut() const {
   // write init file

   HANDLE hFile = INVALID_HANDLE_VALUE;
   hFile = CreateFile(
      GetModuleDir(ghInstance) + SZ_FILE_NAME_INIT,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   BOOL bResult = FALSE;
   if (hFile == INVALID_HANDLE_VALUE) {
      MessageBox(m_hWnd, CLang::m_StrArr[IDS__INI_FILE__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
   } else {
      DWORD dwNOBW = 0;
      if (!WriteFile(hFile, &m_Serialize, sizeof(m_Serialize), &dwNOBW, NULL) ||
          (sizeof(m_Serialize) != dwNOBW)
         )
      {
         MessageBox(m_hWnd, CLang::m_StrArr[IDS__INI_FILE__ERROR_WRITE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         bResult = m_Mosaic.SerializeOut(hFile);
      }
   }

   if (hFile != INVALID_HANDLE_VALUE)
      CloseHandle(hFile);

   return bResult;
}

inline void CFastMines2Project::Apply_AlwaysMaxSize() {
   CheckMenuItem(GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_ALWAYSMAXSIZE,
      m_Serialize.m_bAlwaysMaxSize ? MF_CHECKED : MF_UNCHECKED);
   if (m_Serialize.m_bAlwaysMaxSize) AreaMax();
   EnableMenuItem(GetSubMenu(m_hMenu, 2), ID_KEY_AREA_INCREMENT, m_Serialize.m_bAlwaysMaxSize ? MF_GRAYED : MF_ENABLED);//MF_DISABLED
   EnableMenuItem(GetSubMenu(m_hMenu, 2), ID_KEY_AREA_DECREMENT, m_Serialize.m_bAlwaysMaxSize ? MF_GRAYED : MF_ENABLED);//MF_DISABLED
   EnableMenuItem(GetSubMenu(m_hMenu, 2), ID_KEY_AREA_MAX      , m_Serialize.m_bAlwaysMaxSize ? MF_GRAYED : MF_ENABLED);//MF_DISABLED
   EnableMenuItem(GetSubMenu(m_hMenu, 2), ID_KEY_AREA_MIN      , m_Serialize.m_bAlwaysMaxSize ? MF_GRAYED : MF_ENABLED);//MF_DISABLED
}

inline void CFastMines2Project::Apply_ToTray() {
   CheckMenuItem(GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_TO_TRAY,
      m_Serialize.m_bToTray ? MF_CHECKED : MF_UNCHECKED);
}

inline void CFastMines2Project::Apply_ShowToolbar() {
   CheckMenuItem(GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_SHOW_TOOLBAR,
      m_Serialize.m_bShowToolbar ? MF_CHECKED : MF_UNCHECKED);
   OnMosaicChangeCounters(m_hWnd);
   if (m_Serialize.m_bAlwaysMaxSize) AreaMax();
   else if (m_Serialize.m_bShowToolbar) OnMosaicAdjustArea(m_hWnd);
   FORWARD_WM_SIZE(m_hWnd, SIZE_RESTORED, 0,0, SendMessage);
}

inline void CFastMines2Project::Apply_ShowMenu() {
   CheckMenuItem(GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_SHOW_MENU,
      m_Serialize.m_bShowMenu ? MF_CHECKED : MF_UNCHECKED);
   SetMenu(m_hWnd, m_Serialize.m_bShowMenu ? m_hMenu : NULL);
   DrawMenuBar(m_hWnd);
   if (!m_Serialize.m_bShowMenu) {
      if (!m_pCaptionButton) {
         m_pCaptionButton = new CCaptionButtonText(m_hWnd, CLang::m_StrArr[IDS__CAPTION_BUTTON_MENU], CFastMines2Project::OnClickCaptionButton, this);
      }
   } else {
      if (m_pCaptionButton) {
         delete m_pCaptionButton;
         m_pCaptionButton = NULL;
      }
   }
   if (m_Serialize.m_bAlwaysMaxSize) AreaMax();
   else if (m_Serialize.m_bShowMenu) OnMosaicAdjustArea(m_hWnd);
}

inline void CFastMines2Project::Apply_ShowCaption() {
   CheckMenuItem(GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_SHOW_CAPTION,
      m_Serialize.m_bShowCaption ? MF_CHECKED : MF_UNCHECKED);
   SetWindowLong(m_hWnd, GWL_STYLE, m_Serialize.m_bShowCaption ? WS_CAPTION_SHOW : WS_CAPTION_HIDE);
   //g_Logger.ClearEdit();
   ShowWindow(m_hWnd, SW_SHOW);
   DrawMenuBar(m_hWnd);
   if (m_Serialize.m_bAlwaysMaxSize) AreaMax();
   else if (m_Serialize.m_bShowCaption) OnMosaicAdjustArea(m_hWnd);
}

inline void CFastMines2Project::Apply_UseAssistant() {
   //CheckMenuItem(HSUBMENU_ASSISTANT, ID_MENU_HELP_ASSISTANT_ONOFF,
   //   m_Serialize.m_AssistantInfo.m_bUse ? MF_CHECKED : MF_UNCHECKED);
}

inline void CFastMines2Project::Apply_UseUnknown() {
   CheckMenuItem( GetSubMenu(m_hMenu, 2), ID_MENU_OPTIONS_USE_UNKNOWN,
      m_Mosaic.GetUseUnknown() ? MF_CHECKED : MF_UNCHECKED);
}

void CFastMines2Project::ApplySkin() {
 //SetCursor(LoadCursor(NULL, IDC_WAIT));
   m_Mosaic.ApplySkin(m_Serialize.m_Skin);

#define APPLY_IMAGE_BUTTON(name, i) \
   if (m_Serialize.m_Skin.m_ImgBtn##name[i].m_szPath[0]) { \
      m_ImgBtn##name[i].SetImage(m_Serialize.m_Skin.m_ImgBtn##name[i]); \
   } else { \
      LoadDefaultImageBtn##name##i(ghInstance, m_ImgBtn##name[i]); \
      m_ImgBtn##name[i].SetPlace      (m_Serialize.m_Skin.m_ImgBtn##name[i].m_Place); \
      m_ImgBtn##name[i].SetTransparent(m_Serialize.m_Skin.m_ImgBtn##name[i].m_bTransparent); \
   }

   APPLY_IMAGE_BUTTON(New  , 0);
   APPLY_IMAGE_BUTTON(New  , 1);
   APPLY_IMAGE_BUTTON(New  , 2);
   APPLY_IMAGE_BUTTON(New  , 3);
   APPLY_IMAGE_BUTTON(Pause, 0);
   APPLY_IMAGE_BUTTON(Pause, 1);
   APPLY_IMAGE_BUTTON(Pause, 2);
   APPLY_IMAGE_BUTTON(Pause, 3);

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   if (m_Serialize.m_Skin.m_bToAll) {
      static HBRUSH hBrushNew = NULL;
      const  HBRUSH hBrushOld = hBrushNew;
      hBrushNew = CreateSolidBrush(m_Serialize.m_Skin.m_colorBk);
      SetClassLong(m_hWndTop, GCL_HBRBACKGROUND, (LONG)hBrushNew);
      SetClassLong(m_hWnd   , GCL_HBRBACKGROUND, (LONG)hBrushNew);
      DeleteObject(hBrushOld);
      InvalidateRect(m_hWndTop, NULL, TRUE);
   } else {
      SetClassLong(m_hWndTop, GCL_HBRBACKGROUND, (LONG)GetSysColorBrush(COLOR_BTNFACE));
      SetClassLong(m_hWnd   , GCL_HBRBACKGROUND, (LONG)GetSysColorBrush(COLOR_BTNFACE));
   }

   m_BtnNew  .SetBkColor(m_Serialize.m_Skin.m_bToAll ? m_Serialize.m_Skin.m_colorBk : CLR_INVALID /**::GetSysColor(COLOR_BTNFACE)/**/);
   m_BtnPause.SetBkColor(m_Serialize.m_Skin.m_bToAll ? m_Serialize.m_Skin.m_colorBk : CLR_INVALID /**::GetSysColor(COLOR_BTNFACE)/**/);
#endif // REPLACEBKCOLORFROMFILLWINDOW

   {
      RECT Rect; GetClientRect(m_hWndEdtCount, &Rect);
      LOGFONT logFont = m_Serialize.m_Skin.m_Font;
      logFont.lfHeight = Rect.bottom;

      DeleteObject(FORWARD_WM_GETFONT(m_hWndEdtCount, SendMessage)); // ???
      DeleteObject(FORWARD_WM_GETFONT(m_hWndEdtTimer, SendMessage)); // ???
      FORWARD_WM_SETFONT(m_hWndEdtCount, CreateFontIndirect(&logFont), TRUE, SendMessage);
      FORWARD_WM_SETFONT(m_hWndEdtTimer, CreateFontIndirect(&logFont), TRUE, SendMessage);
   }

   BOOL bRes = InvalidateRect(m_hWnd, NULL, TRUE);
   bRes = InvalidateRect(m_hWndTop, NULL, TRUE); // ???
 //InvalidateRect(m_Mosaic.GetHandle(), NULL, TRUE);
 //SetCursor(LoadCursor(NULL, IDC_ARROW));
}

void CFastMines2Project::SetPlayerName(LPCTSTR szCurrentPlayerName) {
   if(m_Mosaic.GetPause())
      m_Mosaic.SetPause(!m_Mosaic.GetPause());
   if (!lstrcmp(m_Serialize.m_szPlayerName, szCurrentPlayerName)) return;
   lstrcpy(m_Serialize.m_szPlayerName, szCurrentPlayerName);
   m_Mosaic.GameNew();
}

const HICON CFastMines2Project::GetIconMosaic(nsMosaic::EMosaic eMosaic) {
   const CImage *img = GetImageMosaic(eMosaic);
   if (img->GetImageType() == imageIcon)
      return img->GetHandleIcon();
   else
      return NULL;
}

const CImage* CFastMines2Project::GetImageMosaic(nsMosaic::EMosaic eMosaic) {
   CImage *pImg;
   static CImage *ppImgMosaic[nsMosaic::mosaicNil+1] =
   { // @TODO: (отмазка) забиваю на освобождение памяти
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTriangle1"  ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTriangle2"  ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTriangle3"  ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTriangle4"  ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicSquare1"    ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicSquare2"    ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicParquet1"   ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicParquet2"   ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTrapezoid1" ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTrapezoid2" ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTrapezoid3" ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicRhombus1"   ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicQuadrangle1"), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicPentagonT24"), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicPentagonT5" ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicPentagonT10"), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicHexagon1"   ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTrSq1"      ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicTrSq2"      ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicSqTrHex"    ), imageIcon), pImg),
      (pImg = new CImage(), pImg->LoadResource(ghInstance, TEXT("icon_MosaicNull"       ), imageIcon), pImg)
   };
   return ppImgMosaic[eMosaic];
}

void CFastMines2Project::ReloadMosaicMenu(HMENU m_hMenu) const {
   for (int i=nsMosaic::mosaicTriangle1; i<nsMosaic::mosaicNil; i++) {
      HMENU hSubMenu;
      switch (i) {
      case nsMosaic::mosaicTriangle1  :
      case nsMosaic::mosaicTriangle2  :
      case nsMosaic::mosaicTriangle3  :
      case nsMosaic::mosaicTriangle4  : hSubMenu = GetSubMenu(m_hMenu, 0); break;
      case nsMosaic::mosaicSquare1    :
      case nsMosaic::mosaicSquare2    :
      case nsMosaic::mosaicParquet1   :
      case nsMosaic::mosaicParquet2   :
      case nsMosaic::mosaicTrapezoid1 :
      case nsMosaic::mosaicTrapezoid2 :
      case nsMosaic::mosaicTrapezoid3 :
      case nsMosaic::mosaicRhombus1   :
      case nsMosaic::mosaicQuadrangle1: hSubMenu = GetSubMenu(m_hMenu, 1); break;
      case nsMosaic::mosaicPentagonT24:
      case nsMosaic::mosaicPentagonT5 :
      case nsMosaic::mosaicPentagonT10: hSubMenu = GetSubMenu(m_hMenu, 2); break;
      case nsMosaic::mosaicHexagon1   : hSubMenu = GetSubMenu(m_hMenu, 3); break;
      case nsMosaic::mosaicTrSq1      :
      case nsMosaic::mosaicTrSq2      :
      case nsMosaic::mosaicSqTrHex    : hSubMenu = GetSubMenu(m_hMenu, 4); break;
      }
      CMenuItem::SetMenuOwnerDraw(
         hSubMenu,
         ID_MENU_MOSAIC_TRIANGLE1+i,
         GetImageMosaic((nsMosaic::EMosaic)(nsMosaic::mosaicTriangle1+i))
      );
   }
}

void CFastMines2Project::GameSave() {
   OPENFILENAME OpenFileName;
   OpenFileName.lStructSize = sizeof(OPENFILENAME);
   OpenFileName.hwndOwner   = m_hWnd;
   OpenFileName.hInstance   = ghInstance;
   TCHAR szFilter[128];
   lstrcpy(szFilter, CLang::m_StrArr[IDS__FASTMINES_EXTENSIONS_FILE]);
   TCHAR szFilter2[] = TEXT(" (*.fms)\0*.fms\0\0");
   memcpy(szFilter+lstrlen(szFilter), szFilter2, chDIMOF(szFilter2)*sizeof(TCHAR));
   OpenFileName.lpstrFilter = szFilter;
   OpenFileName.lpstrCustomFilter = NULL;
 //OpenFileName.nMaxCustFilter;
   OpenFileName.nFilterIndex   = 0;
   TCHAR file[MAX_PATH] = TEXT("\0");
   OpenFileName.lpstrFile      = file;
   OpenFileName.nMaxFile       = MAX_PATH;
   OpenFileName.lpstrFileTitle = NULL;
 //OpenFileName.nMaxFileTitle;
   TCHAR dir[MAX_PATH]; GetCurrentDirectory(MAX_PATH, dir);
   OpenFileName.lpstrInitialDir = dir;
   OpenFileName.lpstrTitle      = CLang::m_StrArr[IDS__SAVE_FASTMINES_GAME];
   OpenFileName.Flags           = OFN_EXPLORER | OFN_OVERWRITEPROMPT |/*OFN_FILEMUSTEXIST | */OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_PATHMUSTEXIST;
 //OpenFileName.nFileOffset;
 //OpenFileName.nFileExtension;
   TCHAR szExt[] = TEXT("fms");
   OpenFileName.lpstrDefExt = szExt;
 //OpenFileName.lCustData;
 //OpenFileName.lpfnHook;
 //OpenFileName.lpTemplateName;
   if (GetSaveFileName(&OpenFileName)) {
      CFileGame fileGame;
      fileGame.m_eMosaic    = GetMosaic();
      fileGame.m_SizeMosaic = GetSizeMosaic();
      fileGame.m_iMines     = GetMines();

      HANDLE hFile = INVALID_HANDLE_VALUE;
      hFile = CreateFile(
         OpenFileName.lpstrFile,
         GENERIC_WRITE,
         0,
         NULL,
         CREATE_ALWAYS,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      if (hFile == INVALID_HANDLE_VALUE) {
         MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         DWORD dwNOBW = 0;
         if (!WriteFile(hFile, &fileGame, sizeof(CFileGame), &dwNOBW, NULL) ||
             (sizeof(CFileGame) != dwNOBW)
            )
         {
            MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_WRITE ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
         } else {
            UINT countMines = 0;
            BOOL bBreak = FALSE;
            for (int i=0; !bBreak && (i<fileGame.m_SizeMosaic.cx); i++)
               for (int j=0; !bBreak && (j<fileGame.m_SizeMosaic.cy); j++) {
                  const nsCell::CBase *pCell = m_Mosaic.GetCell(i,j);
                  if (pCell->Cell_GetOpen() == nsCell::_Mine) {
                     COORD cellMines = {i,j};
                     countMines++;
                     if (!WriteFile(hFile, &cellMines, sizeof(COORD), &dwNOBW, NULL) ||
                         (sizeof(COORD) != dwNOBW)
                        )
                     {
                        MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_WRITE ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
                        bBreak = TRUE;
                     }
                  }
               }
         }
      }

      if (hFile != INVALID_HANDLE_VALUE)
         CloseHandle(hFile);
   }
}

void CFastMines2Project::GameLoad(LPCTSTR szFileName) {
   OPENFILENAME OpenFileName;

   TCHAR szFilter[128];
   lstrcpy(szFilter, CLang::m_StrArr[IDS__FASTMINES_EXTENSIONS_FILE]);
   TCHAR szFilter2[] = TEXT(" (*.fms)\0*.fms\0\0");
   memcpy(szFilter+lstrlen(szFilter), szFilter2, chDIMOF(szFilter2)*sizeof(TCHAR));
   if (!szFileName) {
      OpenFileName.lStructSize = sizeof(OPENFILENAME);
      OpenFileName.hwndOwner   = m_hWnd;
      OpenFileName.hInstance   = ghInstance;
      OpenFileName.lpstrFilter = szFilter;
      OpenFileName.lpstrCustomFilter = NULL;
    //OpenFileName.nMaxCustFilter;
      OpenFileName.nFilterIndex   = 0;
      TCHAR file[MAX_PATH] = TEXT("\0");
      OpenFileName.lpstrFile      = file;
      OpenFileName.nMaxFile       = MAX_PATH;
      OpenFileName.lpstrFileTitle = NULL;
    //OpenFileName.nMaxFileTitle;
      TCHAR dir[MAX_PATH]; GetCurrentDirectory(MAX_PATH, dir);
      OpenFileName.lpstrInitialDir = dir;
      OpenFileName.lpstrTitle      = CLang::m_StrArr[IDS__LOAD_FASTMINES_GAME];
      OpenFileName.Flags           = OFN_EXPLORER | OFN_FILEMUSTEXIST | OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_PATHMUSTEXIST;
    //OpenFileName.nFileOffset;
    //OpenFileName.nFileExtension;
      OpenFileName.lpstrDefExt = NULL;
    //OpenFileName.lCustData;
    //OpenFileName.lpfnHook;
    //OpenFileName.lpTemplateName;
   }
   if (szFileName || GetOpenFileName(&OpenFileName)) {
      CFileGame fileGame;

      HANDLE hFile = INVALID_HANDLE_VALUE;
      hFile = CreateFile(
         szFileName ? szFileName : OpenFileName.lpstrFile,
         GENERIC_READ,
         0,
         NULL,
         OPEN_EXISTING,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      if (hFile == INVALID_HANDLE_VALUE) {
         MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_CREATE], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
      } else {
         DWORD dwNOBR = 0;
         if (!ReadFile(hFile, &fileGame, sizeof(CFileGame), &dwNOBR, NULL) ||
             (sizeof(CFileGame) != dwNOBR) ||
             (fileGame.m_iMines == 0)
            )
         {
            MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
         } else {
            if (lstrcmp(TEXT(ID_VERSIONINFO_VERSION3), fileGame.m_szVersion)) {
               MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_VERSION], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
            } else {
               nsMosaic::CStorageMines CellMines;
               CellMines.SetSize(fileGame.m_iMines);
               BOOL bBreak = FALSE;
               for (int i=0; !bBreak && (i<fileGame.m_iMines); i++) {
                  if (!ReadFile(hFile, CellMines[i], sizeof(COORD), &dwNOBR, NULL) ||
                      (sizeof(COORD) != dwNOBR)
                     )
                  {
                     bBreak = TRUE;
                     MessageBox(m_hWnd, CLang::m_StrArr[IDS__FMS_FILE__ERROR_READ], CLang::m_StrArr[IDS__ERROR], MB_ICONSTOP | MB_OK);
                     //g_Logger.GetLastError(CLogger::LL_ERROR, TEXT("CFastMines2Project::GameSave: ReadFile: "), ::GetLastError());
                  }
               }
               if (!bBreak) {
                  m_Mosaic.SetPause(false);
                  m_Mosaic.SetGame(fileGame.m_eMosaic, fileGame.m_SizeMosaic, fileGame.m_iMines, &CellMines);
                  CheckMenuRadioItem( GetSubMenu(m_hMenu, 0),
                     ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
                     ID_MENU_GAME_BEGINNER + GetSkillLevel(),
                     MF_BYCOMMAND);
               }
            }
         }
      }
      if (hFile != INVALID_HANDLE_VALUE)
         CloseHandle(hFile);
   }
}

void CFastMines2Project::Menu_ChangeLanguage() {
   // MenuBar
   SetMenuText(m_hMenu, 0, TRUE, CLang::m_StrArr[IDS__MENU_GAME   ]);
   SetMenuText(m_hMenu, 1, TRUE, CLang::m_StrArr[IDS__MENU_MOSAIC ]);
   SetMenuText(m_hMenu, 2, TRUE, CLang::m_StrArr[IDS__MENU_OPTIONS]);
   SetMenuText(m_hMenu, 3, TRUE, CLang::m_StrArr[IDS__MENU_HELP   ]);

   ::DrawMenuBar(m_hWnd);

   // menu Game
   HMENU hSubMenu = GetSubMenu(m_hMenu, 0);
   SetMenuText(hSubMenu, ID_MENU_GAME_NEW_GAME    , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__NEW_GAME    ] + TEXT("\tF2"    )));
   SetMenuText(hSubMenu, ID_MENU_GAME_BEGINNER    , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__BEGINNER    ] + TEXT("\t1"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_AMATEUR     , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__AMATEUR     ] + TEXT("\t2"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_PROFESSIONAL, FALSE, (CLang::m_StrArr[IDS__MENU_GAME__PROFESSIONAL] + TEXT("\t3"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_CRAZY       , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__CRAZY       ] + TEXT("\t4"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_CUSTOM      , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__CUSTOM      ] + TEXT("\t5"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_CREATE      , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__CREATE      ] + TEXT("\tCtrl+C")));
   SetMenuText(hSubMenu, ID_MENU_GAME_SAVE        , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__SAVE        ] + TEXT("\tCtrl+S")));
   SetMenuText(hSubMenu, ID_MENU_GAME_LOAD        , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__LOAD        ] + TEXT("\tCtrl+O")));
   SetMenuText(hSubMenu, ID_MENU_GAME_SELECTPLAYER, FALSE, (CLang::m_StrArr[IDS__MENU_GAME__SELECTPLAYER] + TEXT("\t~"     )));
   SetMenuText(hSubMenu, ID_MENU_GAME_EXIT        , FALSE, (CLang::m_StrArr[IDS__MENU_GAME__EXIT        ] + TEXT("\tAlt+F4")));


   // menu Mosaics
   hSubMenu = GetSubMenu(m_hMenu, 1);
   SetMenuText(hSubMenu, 0, TRUE, (CLang::m_StrArr[IDS__MENU_MOSAIC__TRIANGLES     ] + TEXT("\tNum3")));
   SetMenuText(hSubMenu, 1, TRUE, (CLang::m_StrArr[IDS__MENU_MOSAIC__QUADRILATERALS] + TEXT("\tNum4")));
   SetMenuText(hSubMenu, 2, TRUE, (CLang::m_StrArr[IDS__MENU_MOSAIC__PENTAGONS     ] + TEXT("\tNum5")));
   SetMenuText(hSubMenu, 3, TRUE, (CLang::m_StrArr[IDS__MENU_MOSAIC__HEXAGONS      ] + TEXT("\tNum6")));
   SetMenuText(hSubMenu, 4, TRUE, (CLang::m_StrArr[IDS__MENU_MOSAIC__OTHER         ] + TEXT("\tNum7")));

   // т.к. меню мозаик отображаются мной (MENUITEMINFO.fType == MFT_OWNERDRAW)
   // то новый текст надо изменить по другому - в (CMenuItem*)(MENUITEMINFO.dwItemData)->m_szText
   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 1), 0);
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRIANGLE1  , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_00] + TEXT("\tNum30")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRIANGLE2  , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_01] + TEXT("\tNum31")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRIANGLE3  , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_02] + TEXT("\tNum32")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRIANGLE4  , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_03] + TEXT("\tNum33")));

   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 1), 1);
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_SQUARE1    , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_04] + TEXT("\tNum40")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_SQUARE2    , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_05] + TEXT("\tNum41")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_PARQUET1   , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_06] + TEXT("\tNum42")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_PARQUET2   , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_07] + TEXT("\tNum43")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRAPEZOID1 , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_08] + TEXT("\tNum44")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRAPEZOID2 , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_09] + TEXT("\tNum45")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRAPEZOID3 , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_10] + TEXT("\tNum46")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_RHOMBUS1   , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_11] + TEXT("\tNum47")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_QUADRANGLE1, FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_12] + TEXT("\tNum48")));

   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 1), 2);
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_PENTAGONT24, FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_13] + TEXT("\tNum50")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_PENTAGONT5 , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_14] + TEXT("\tNum51")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_PENTAGONT10, FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_15] + TEXT("\tNum52")));

   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 1), 3);
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_HEXAGON1   , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_16] + TEXT("\tNum60")));

   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 1), 4);
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRSQ1      , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_17] + TEXT("\tNum70")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_TRSQ2      , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_18] + TEXT("\tNum71")));
   CMenuItem::SetMenuText(hSubMenu, ID_MENU_MOSAIC_SQTRHEX    , FALSE, (CLang::m_StrArr[IDS__MOSAIC_NAME_19] + TEXT("\tNum72")));

   // menu Options
   hSubMenu = GetSubMenu(m_hMenu, 2);
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_ALWAYSMAXSIZE, FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__ALWAYSMAXSIZE ] + TEXT("")      ));
   SetMenuText(hSubMenu, ID_KEY_AREA_MAX              , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_MAX      ] + TEXT("\tNum*")));
   SetMenuText(hSubMenu, ID_KEY_AREA_MIN              , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_MIN      ] + TEXT("\tNum/")));
   SetMenuText(hSubMenu, ID_KEY_AREA_INCREMENT        , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_INCREMENT] + TEXT("\tNum+")));
   SetMenuText(hSubMenu, ID_KEY_AREA_DECREMENT        , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_DECREMENT] + TEXT("\tNum-")));
   SetMenuText(hSubMenu, 6                            , TRUE , (CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN          ] + TEXT("")      ));
   SetMenuText(hSubMenu, 8                            , TRUE , (CLang::m_StrArr[IDS__MENU_OPTIONS__LANGUAGE      ] + TEXT("")      ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_USE_UNKNOWN  , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__USE_UNKNOWN   ] + TEXT("")      ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_SHOW_TOOLBAR , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_TOOLBAR  ] + TEXT("\tF10") ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_SHOW_MENU    , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_MENU     ] + TEXT("\tF11") ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_SHOW_CAPTION , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_CAPTION  ] + TEXT("\tF12") ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_TO_TRAY      , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__TO_TRAY       ] + TEXT("")      ));

   hSubMenu = GetSubMenu(GetSubMenu(m_hMenu, 2), 6);
   SetMenuText(hSubMenu     , 0                     , TRUE , (CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__LOAD        ] + TEXT("")    ));
   SetMenuText(HSUBMENU_SKIN, 0                     , TRUE , (CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__LOAD_DEFAULT] + TEXT("")    ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_SKIN_SAVE  , FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__SAVE        ] + TEXT("")    ));
   SetMenuText(hSubMenu, ID_MENU_OPTIONS_SKIN_CHANGE, FALSE, (CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__CHANGE      ] + TEXT("\tF8")));

   hSubMenu = HSUBMENU_LANGUAGE;
   SetMenuText(hSubMenu, 0, TRUE , (CLang::m_StrArr[IDS__MENU_OPTIONS__LANGUAGE_ENGLISH] + TEXT("")));


   // menu Help
   hSubMenu = GetSubMenu(m_hMenu, 3);
   SetMenuText(hSubMenu, 3                      , TRUE , (CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT ] + TEXT("")    ));
   SetMenuText(hSubMenu, ID_MENU_HELP_CAMPIONS  , FALSE, (CLang::m_StrArr[IDS__MENU_HELP__CAMPIONS  ] + TEXT("\tF3")));
   SetMenuText(hSubMenu, ID_MENU_HELP_STATISTICS, FALSE, (CLang::m_StrArr[IDS__MENU_HELP__STATISTICS] + TEXT("\tF4")));
   SetMenuText(hSubMenu, ID_MENU_HELP_ABOUT     , FALSE, (CLang::m_StrArr[IDS__MENU_HELP__ABOUT     ] + TEXT("\tF1")));

   hSubMenu = HSUBMENU_ASSISTANT;
   SetMenuText(hSubMenu, ID_MENU_HELP_ASSISTANT_ONOFF  , FALSE, (CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__ONOFF  ] + TEXT("\tA" )));
   SetMenuText(hSubMenu, ID_MENU_HELP_ASSISTANT_OPTIONS, FALSE, (CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__OPTIONS] + TEXT("\tF5")));
   SetMenuText(hSubMenu, ID_MENU_HELP_ASSISTANT_SUGGEST, FALSE, (CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__SUGGEST] + TEXT("\tH" )));

   if (!m_Serialize.m_bShowMenu) {
      delete m_pCaptionButton;
      m_pCaptionButton = new CCaptionButtonText(m_hWnd, CLang::m_StrArr[IDS__CAPTION_BUTTON_MENU], CFastMines2Project::OnClickCaptionButton, this);
   }

   // change Caption
   OnMosaicChangeCounters(m_hWnd);
}
/*
// WM_TIMER
void CFastMines2Project::ProjectOnTimer(HWND hwnd, UINT id) {
   switch (id) {
   case ID_EVENT_TIMER_INACTION:
      KillTimer(hwnd, ID_EVENT_TIMER_INACTION);
      SetTimer (hwnd, ID_EVENT_TIMER_JOB, GetAssistant().m_iTimeoutJob, NULL);
   case ID_EVENT_TIMER_JOB:
      // проверка условий для работы робота (ассистента)
      if (!GetAssistant().m_bUse) return;
      if (hwnd != GetForegroundWindow()) return;
      if (GetAssistant().m_bAutoStart) {
         if (!m_Mosaic.GetGameStatusIsReady())
            m_Mosaic.GameNew();
      } else {
         if (!m_Mosaic.GetGameStatusIsPlay()) {
            return;
         }
      }
      if (m_Mosaic.GetPause()) {
         if (GetAssistant().m_bIgnorePause)
            m_Mosaic.SetPause(false);
         else return;
      }
      bOpenCellAssistant = true;
      SetEvent(hEventJob);
   }
}
/**
inline void CFastMines2Project::ResetAssistant() {
   ResetEvent(hEventJob);

   KillTimer(m_hWnd, ID_EVENT_TIMER_JOB     );
   KillTimer(m_hWnd, ID_EVENT_TIMER_INACTION);
   if (!GetAssistant().m_bUse) return;
   SetTimer(m_hWnd, ID_EVENT_TIMER_INACTION, GetAssistant().m_iTimeoutUnactive, NULL);
}

#define IMAGE_JOB_SET   SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)GetImageBtnPause(3))
#define IMAGE_JOB_RESET SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, \
                           m_Mosaic.GetGameStatusIsPlay() ?                        \
                              m_Mosaic.GetPause() ?                       \
                              (LPARAM)GetImageBtnPause(1) :      \
                              (LPARAM)GetImageBtnPause(0) :      \
                           (LPARAM)GetImageBtnPause(2))

inline void CFastMines2Project::Assistant_Job() {
   static COORD lastClick = INCORRECT_COORD;
   if (lastClick != INCORRECT_COORD) IMAGE_JOB_SET;
   nsMosaic::CClickData click;
   if (m_Assistant.IsSequentialMove()) { // выполняется ли перебор флажков?
      m_Assistant.SequentialMove(click); // да, выполняется - продолжить перебор флажков
   } else {
      if (!m_Assistant.FindCell(click)) { // найти ячейку для клика
         IMAGE_JOB_RESET;
         return; // работа была прервана
      }
      if (click.m_CoordCell == INCORRECT_COORD) {
         if (!m_Assistant.AllOkToSequentialMove()) { // проверка условий для начала перебора флажков
            IMAGE_JOB_RESET;
            return; // работа была прервана
         }
         if (m_Assistant.IsSequentialMove()) {       // выполнять ли перебор флажков?
            m_Assistant.SequentialMove(click);       // да, выполнять - начать перебор флажков
         } else {
            if (m_Mosaic.GetGameStatusIsPlay() &&
                GetAssistant().m_bStopJob &&
                bOpenCellAssistant
               )
            {
               IMAGE_JOB_RESET;
               return; // останавливать когда нет однозначного следующего хода
            }
            if (click.m_fProbability >= 0.5) { // вероятность больше 50%
               click.m_CoordCell = click.m_PrbltCell;
            #ifdef USE_INFO_DIALOG
               //nsInfo::AddValue(TEXT("coord probability = "), click.m_CoordCell, 10);
               //nsInfo::AddValue(TEXT("      probability = "), click.m_fProbability);
            #endif // USE_INFO_DIALOG
            } else {
               do {
                  click.m_CoordCell.X = rand(GetSizeMosaic().cx-1);
                  click.m_CoordCell.Y = rand(GetSizeMosaic().cy-1);
               } while ((m_Mosaic.GetCell(click.m_CoordCell.X,click.m_CoordCell.Y)->Cell_GetStatus() == nsCell::_Open) ||
                        (m_Mosaic.GetCell(click.m_CoordCell.X,click.m_CoordCell.Y)->Cell_GetClose()  == nsCell::_Flag));
            }
         }
      }
   }
   lastClick = click.m_CoordCell;
   IMAGE_JOB_RESET;
   const POINT coordCenter = m_Mosaic.GetCell(click.m_CoordCell.X,click.m_CoordCell.Y)->GetCenterPixel();

   { // set cursor to new position
      POINT coordCursor = ::ClientToScreen(m_Mosaic.GetHandle(), coordCenter);

      SetEvent(hEventSetCursorBegin);

      SetCursorPos(coordCursor.x, coordCursor.y);

      WaitForSingleObject(hEventSetCursorEnd, INFINITE);
      SetEvent(hEventSetCursorBegin); // ???

   }
   if (bOpenCellAssistant) {
      if (GetAssistant().m_bBeep) MessageBeep(0);
      if (click.m_bIsLeft) {
         FORWARD_WM_LBUTTONDOWN(m_Mosaic.GetHandle(), FALSE, coordCenter.x, coordCenter.y, MK_ASSISTANT, SendMessage);
         FORWARD_WM_LBUTTONUP  (m_Mosaic.GetHandle(),        coordCenter.x, coordCenter.y, MK_ASSISTANT, SendMessage);
      } else {
         FORWARD_WM_RBUTTONDOWN(m_Mosaic.GetHandle(), FALSE, coordCenter.x, coordCenter.y, MAKEWPARAM(MK_ASSISTANT, click.m_Close), SendMessage);
      }
   }
}

DWORD WINAPI ChildThread(PVOID pvParam) {
   const HANDLE pHandles[2]= {hEventJob, (HANDLE)pvParam};

   for (;;) {
      switch (WaitForMultipleObjects(2, pHandles, FALSE, INFINITE)) {
      case WAIT_OBJECT_0:
         //ResetEvent(hEventJobEnd);
         try {
            gpFM2Proj->Assistant_Job();
         }catch(...){}
         //SetEvent(hEventJobEnd);
         ResetEvent(hEventJob);
         break;
      case WAIT_OBJECT_0+1:
         return 0;
      case WAIT_TIMEOUT:
      case WAIT_FAILED:
         break;
      }
   }
   return 0;
}
/**/
VOID CFastMines2Project::OnClickCaptionButton(LPVOID pParam) {
   CFastMines2Project *This = (CFastMines2Project*)pParam;
   FORWARD_WM_COMMAND(This->m_hWnd, ID_MENU_OPTIONS_SHOW_MENU, NULL, 0, ::SendMessage);
}
