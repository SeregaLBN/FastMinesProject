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

#include ".\Preproc.h"
#include <windows.h>
#include <windowsx.h>
#include <commctrl.h>
#include <map>
#include <math.h>
#include <process.h>    /* _beginthread, _endthread */
#include ".\ID_resource.h"
#include ".\Lib.h"
#include ".\TcMosaic.h"
#include ".\EraseBk.h"
#include ".\TcRobot.h"
#include ".\Dialog\Info.h"
#include ".\Dialog\PlayerName.h"
#include ".\Dialog\Statistics.h"
#include ".\Dialog\Champions.h"
#include ".\Dialog\About.h"
#include ".\Dialog\Registration.h"
#include ".\Dialog\SelectFigure.h"
#include ".\Dialog\Skin.h"
#include ".\Dialog\Assistant.h"
#include ".\Figure\TcBase.h"
#include ".\Control\TcButtonImage.h"
#include ".\Control\TcButtonImageCheck.h"

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
const BYTE  CHeightPnlTop = 40;//
const BYTE  CHeightWidthButtonNewPause = 28;//36;//
const POINT CEditCountTimer = {40, 21}; // ширина и высота окон
const TCHAR szCCaptionProject[] = TEXT("FastMines2");
const TCHAR szCClassProject  [] = TEXT("ClassWndFastMinesProject");
const TCHAR szCClassWndTop   [] = TEXT("ClassWndFastMinesTop");
const TCHAR szCClassWndField [] = TEXT("ClassWndFastMinesField");

#define WM_NOTIFYICON (WM_USER+1)

#define WS_CAPTION_NO  (WS_OVERLAPPED | WS_BORDER  | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX) //WS_POPUPWINDOW //
#define WS_CAPTION_YES (WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX)

#define HSUBMENU_SKIN      GetSubMenu(GetSubMenu(GetSubMenu(GetMenu(ghWnd), 2), 5), 0)
#define HSUBMENU_ASSISTANT            GetSubMenu(GetSubMenu(GetMenu(ghWnd), 2), 7)

#define ID_EVENT_TIMER_INACTION 1 // id событи€ таймера, первого   срабатывани€ при бездействии юзера дл€ старта робота
#define ID_EVENT_TIMER_JOB      2 // id событи€ таймера, следующих срабатываний при бездействии юзера
////////////////////////////////////////////////////////////////////////////////
//                             forward declaration
////////////////////////////////////////////////////////////////////////////////
LRESULT CALLBACK WndProcProject(HWND, UINT, WPARAM, LPARAM);
LRESULT CALLBACK WndProcTop    (HWND, UINT, WPARAM, LPARAM);
LRESULT CALLBACK WndProcField  (HWND, UINT, WPARAM, LPARAM);

#ifdef ROBOT_MULTITHREAD
DWORD WINAPI ChildThread(PVOID);
#endif // ROBOT_MULTITHREAD

inline void TrayMessage(UINT, const TCHAR*);
inline void ResetTimerRobot();
inline void Robot_Job();

BOOL Cls_ProjectOnCreate    (HWND, LPCREATESTRUCT);          // WM_CREATE
void Cls_ProjectOnDestroy   (HWND);                          // WM_DESTROY
void Cls_ProjectOnActivate  (HWND, UINT, HWND, BOOL);        // WM_ACTIVATE
void Cls_ProjectOnSize      (HWND, UINT, int,  int);         // WM_SIZE
void Cls_ProjectOnSysCommand(HWND, UINT, int,  int);         // WM_SYSCOMMAND (SC_MAXIMIZE)
void Cls_ProjectOnCommand   (HWND, int , HWND, UINT);        // WM_COMMAND
void Cls_ProjectOnSysKey    (HWND, UINT, BOOL, int,   UINT); // WM_SYSKEYUP
void Cls_ProjectOnKey       (HWND, UINT, BOOL, int,   UINT); // WM_KEYUP
#ifdef REPLACEBKCOLORFROMFILLWINDOW
void Cls_ProjectOnMenuSelect(HWND, HMENU, int, HMENU, UINT); // WM_MENUSELECT
void Cls_ProjectOnPaint     (HWND);                          // WM_PAINT
#endif // REPLACEBKCOLORFROMFILLWINDOW
void Cls_ProjectOnTimer     (HWND, UINT);                    // WM_TIMER

#ifdef REPLACEBKCOLORFROMFILLWINDOW
BOOL Cls_OnEraseBkgnd(HWND, HDC); // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW

void Cls_TopOnCommand      (HWND, int, HWND, UINT);      // WM_COMMAND
void Cls_TopOnLButtonDown  (HWND, BOOL, int, int, UINT); // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void Cls_TopOnRButtonDown  (HWND, BOOL, int, int, UINT); // WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void Cls_TopOnNCMButtonDown(HWND, BOOL, int, int, UINT); // WM_NCMBUTTONDBLCLK

////////////////////////////////////////////////////////////////////////////////
//                        global variables in all project
////////////////////////////////////////////////////////////////////////////////
HINSTANCE ghInstance;
HWND      ghWnd;
TcMosaic* gpMosaic = NULL;
TcRobot   gRobot;

////////////////////////////////////////////////////////////////////////////////
//                          global variables this module
////////////////////////////////////////////////////////////////////////////////
HWND hWndTop,
     hWndEdtCount, hWndEdtTimer;
HICON hIconProject;
TcButtonImage      BtnNew;
TcButtonImageCheck BtnPause;

bool bOpenCellRobot;

#ifdef ROBOT_MULTITHREAD
   HANDLE hEventJob;//, hEventJobEnd;
   HANDLE hEventSetCursorBegin, hEventSetCursorEnd;
#else
   bool b_WM_SETCURSOR_Break = false;
#endif // ROBOT_MULTITHREAD

int hwBtnNP = CHeightWidthButtonNewPause;
int heightPnlTop = CHeightPnlTop;
RECT Border = {0, heightPnlTop, 0, 0};

#ifndef IGNORE_REGISTRATION
bool bRegister;
#endif // IGNORE_REGISTRATION

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
//#pragma comment(linker, "/SUBSYSTEM:WINDOWS")
//#pragma comment(linker, "/ENTRY:wWinMainCRTStartup")
//int WINAPI wWinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPWSTR lpszCmdLine, int nCmdShow )
int WINAPI _tWinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpszCmdLine, int nCmdShow )
{  ghInstance = hInstance;

   InitCommonControls();
   hIconProject = LoadIcon(ghInstance, TEXT("iconPROJECT"));
   RegClass(
      0,                               // UINT    style
      WndProcProject,                  // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      hIconProject,                    // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      TEXT("FastMinesMenu"),           // LPCTSTR lpszMenuName
      szCClassProject                  // LPCTSTR lpszClassName
   );
   RegClass(
      0,                               // UINT    style
      WndProcField,                    // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  c_hInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      szCClassWndField                 // LPCTSTR lpszClassName
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
      szCClassWndTop                   // LPCTSTR lpszClassName
   );

   ghWnd = CreateWindow( szCClassProject, szCCaptionProject,
      WS_CAPTION_YES,
      GetSystemMetrics(SM_CXSCREEN)/2-GetSystemMetrics(SM_CXMIN)/2,
      GetSystemMetrics(SM_CYSCREEN)/2-GetSystemMetrics(SM_CYMIN)/2,
      GetSystemMetrics(SM_CXMIN),
      GetSystemMetrics(SM_CYMIN),
      NULL, (HMENU)0, ghInstance, NULL );
   gpMosaic = new TcMosaic(
      ghInstance, ghWnd,
      BtnNew.GetHandle(), BtnPause.GetHandle(),
      hWndEdtCount, hWndEdtTimer,
      &szCCaptionProject[0],
      &szCClassWndField[0],
      &Border);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   BtnNew  .SetColor(gpMosaic->GetSkin().toAll, gpMosaic->GetSkin().colorBk);
   BtnPause.SetColor(gpMosaic->GetSkin().toAll, gpMosaic->GetSkin().colorBk);
#else
   //ReplaceFunction_GetSysColor     (ghInstance);
   //ReplaceFunction_GetSysColorBrush(ghInstance);
   //SetClassLong(hWndTop, GCL_HBRBACKGROUND, (LONG)CreateSolidBrush(gpMosaic->GetSkin().colorBk));
   /**
   {
      const int lpaElements[2] = {COLOR_MENU, COLOR_BTNFACE};
      const COLORREF lpaRgbValues[2] = {gpMosaic->GetSkin().colorBk, gpMosaic->GetSkin().colorBk};
      SetSysColors(2, lpaElements, lpaRgbValues);
   }/**/
#endif // REPLACEBKCOLORFROMFILLWINDOW

   nsFileSkin::LoadSkinList(HSUBMENU_SKIN); // надо вызвать до того как уберЄтс€ меню
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_TOOLBAR, 0,0, SendMessage);
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_TOOLBAR, 0,0, SendMessage);
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_MENU   , 0,0, SendMessage);
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_MENU   , 0,0, SendMessage);
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_CAPTION, 0,0, SendMessage);
   FORWARD_WM_COMMAND(ghWnd, ID_MENU_OPTIONS_CAPTION, 0,0, SendMessage);
   gpMosaic->FieldCreate();

#ifdef USE_INFO_DIALOG
   CreateDialog(ghInstance, TEXT("InfoDialog"), ghWnd, (DLGPROC)nsInfo::DialogProc);
   SetFocus(ghWnd);
#endif // USE_INFO_DIALOG
#ifndef IGNORE_REGISTRATION
   /**/
   if (!(bRegister = nsRegistration::isRegister(NULL, NULL))) {
      HMENU hMenu = GetMenu(ghWnd);
      EnableMenuItem(hMenu, ID_MENU_GAME_AMATEUR     , MF_GRAYED);//MF_DISABLED);//
      EnableMenuItem(hMenu, ID_MENU_GAME_PROFESSIONAL, MF_GRAYED);//MF_DISABLED);//
      EnableMenuItem(hMenu, ID_MENU_GAME_CRAZY       , MF_GRAYED);//MF_DISABLED);//
      EnableMenuItem(hMenu, ID_MENU_GAME_CUSTOM      , MF_GRAYED);//MF_DISABLED);//
      if (gpMosaic->GetSkillLevel() != skillLevelBeginner)
         MessageBox(ghWnd, TEXT("Please, re-enter registration name and key"), TEXT("Error"), MB_OK | MB_ICONSTOP);
      PostMessage(ghWnd, WM_COMMAND, MAKEWPARAM((UINT)ID_MENU_GAME_BEGINNER, 0), (LPARAM)NULL); //gpMosaic->SetSkillLevel(skillLevelBeginner);
   }/**/
#endif // IGNORE_REGISTRATION

   CheckMenuItem(HSUBMENU_ASSISTANT, ID_MENU_OPTIONS_ASSISTANT_ONOFF,
      gpMosaic->GetAssistant().use ? MF_CHECKED : MF_UNCHECKED);

   HACCEL hAccel = LoadAccelerators(ghInstance, TEXT("FastMinesAccelerators"));
   ShowWindow(ghWnd, nCmdShow);
   UpdateWindow(ghWnd);

#ifdef ROBOT_MULTITHREAD
   {
      hEventJob    = CreateEvent(NULL, TRUE, FALSE, NULL);
    //hEventJobEnd = CreateEvent(NULL, TRUE, TRUE , NULL);
      hEventSetCursorBegin = CreateEvent(NULL, FALSE, FALSE, NULL);
      hEventSetCursorEnd   = CreateEvent(NULL, FALSE, FALSE, NULL);
   }
   {
      HANDLE hTreadParent = GetCurrentThread();
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
   }
#endif // ROBOT_MULTITHREAD

   MSG msg;
   while ( GetMessage(&msg, NULL, 0, 0) ){
      if (!TranslateAccelerator(ghWnd, hAccel, &msg)) {
         TranslateMessage(&msg);
         DispatchMessage (&msg);
      }
   }
   return msg.wParam;
}

LRESULT CALLBACK WndProcProject(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam){
#ifdef USE_INFO_DIALOG
   switch(msg){
   case WM_SETTEXT:
   case WM_GETTEXT:
   case WM_SETCURSOR:
   case WM_MOUSEMOVE:
      break;
   default:
      //nsInfo::AddValue(TEXT("msg (project) = 0x"), msg, 16);
      break;
   }
#endif // USE_INFO_DIALOG

   static bool inMenu = false;
   switch(msg){
   case WM_ENTERMENULOOP: inMenu = true;  break;
   case WM_EXITMENULOOP : inMenu = false; break;
   }

   if (inMenu) {
      ResetTimerRobot();
   } else
      switch(msg){
      case WM_TIMER:
      case WM_SETTEXT:
      case WM_GETTEXT:
      case WM_NCHITTEST:
      case WM_MOUSEFIRST:
         break;
   #ifdef ROBOT_MULTITHREAD
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
            if (LOWORD(wParam) == ID_MENU_OPTIONS_ASSISTANT_SUGGEST) {
               SetEvent(hEventSetCursorBegin); // next message must by WM_SETCURSOR
               //ResetTimerRobot();
            }
            break;
         case WM_KEYUP:
            if ((int)wParam != 0x48)
               ResetTimerRobot();
            break;
      } /// end: if press key for suggest
   #endif // ROBOT_MULTITHREAD
      case WM_SETCURSOR:
   #ifdef ROBOT_MULTITHREAD
         if (WAIT_OBJECT_0 == WaitForSingleObject(hEventSetCursorBegin, 0)) {
            SetEvent(hEventSetCursorEnd);
            break;
         }
   #else
         if(b_WM_SETCURSOR_Break) {
            b_WM_SETCURSOR_Break = false;
            break;
         }
   #endif // ROBOT_MULTITHREAD
   #ifdef USE_INFO_DIALOG
      //nsInfo::AddValue(TEXT("msg (project) = 0x"), msg, 16);
   #endif // USE_INFO_DIALOG
      default:
         ResetTimerRobot();
      }

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   static bool f = false;
   LRESULT result;
#endif // REPLACEBKCOLORFROMFILLWINDOW

   switch(msg){
 //HANDLE_MSG(hwnd, WM_ERASEBKGND, Cls_OnEraseBkgnd);
   HANDLE_MSG(hwnd, WM_TIMER     , Cls_ProjectOnTimer);
   HANDLE_MSG(hwnd, WM_CREATE    , Cls_ProjectOnCreate);
   HANDLE_MSG(hwnd, WM_SIZE      , Cls_ProjectOnSize);
   HANDLE_MSG(hwnd, WM_DESTROY   , Cls_ProjectOnDestroy);
   HANDLE_MSG(hwnd, WM_SYSCOMMAND, Cls_ProjectOnSysCommand);
   HANDLE_MSG(hwnd, WM_COMMAND   , Cls_ProjectOnCommand);
   HANDLE_MSG(hwnd, WM_ACTIVATE  , Cls_ProjectOnActivate);
   HANDLE_MSG(hwnd, WM_SYSKEYUP  , Cls_ProjectOnSysKey); // ScreenShot
   HANDLE_MSG(hwnd, WM_KEYUP     , Cls_ProjectOnKey);    // ScreenShot
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hwnd, WM_PAINT     , Cls_ProjectOnPaint);
   HANDLE_MSG(hwnd, WM_MENUSELECT, Cls_ProjectOnMenuSelect);
   //case WM_NCHITTEST:
   //case WM_MOUSEMOVE:
   case WM_NCMOUSEMOVE:
   case WM_NCACTIVATE:
   case WM_EXITMENULOOP: // HANDLE_MSG(hwnd, WM_EXITMENULOOP, Cls_ProjectOnExitMenuLoop);
      result = DefWindowProc(hwnd, msg, wParam, lParam);
      if (gpMosaic->GetSkin().toAll)
         nsEraseBk::FillMenu(hwnd, gpMosaic->GetSkin().colorBk);
      return result;
   case WM_NEXTMENU: f = true;   break;
   case WM_ENTERIDLE:
      result = DefWindowProc(hwnd, msg, wParam, lParam);
      if (f && gpMosaic->GetSkin().toAll)
         nsEraseBk::FillMenu(hwnd, gpMosaic->GetSkin().colorBk);
      f = false;
      return result;
#endif // REPLACEBKCOLORFROMFILLWINDOW
   case WM_NOTIFYICON:
      switch (lParam) {
      case WM_LBUTTONDOWN:
      case WM_RBUTTONDOWN:
         TrayMessage(NIM_DELETE, NULL);
      }
      break;
   case WM_CHANGEBRUSH:
      if (gpMosaic && gpMosaic->GetSkin().toAll) {
         static HBRUSH hBrushNew = NULL;
         const  HBRUSH hBrushOld = hBrushNew;
         hBrushNew = CreateSolidBrush(gpMosaic->GetSkin().colorBk);
         SetClassLong(hWndTop, GCL_HBRBACKGROUND, (LONG)hBrushNew);
         SetClassLong(hwnd   , GCL_HBRBACKGROUND, (LONG)hBrushNew);
         DeleteObject(hBrushOld);
         InvalidateRect(hWndTop, NULL, TRUE);
      } else {
         SetClassLong(hWndTop, GCL_HBRBACKGROUND, (LONG)GetSysColorBrush(COLOR_BTNFACE));
         SetClassLong(hwnd   , GCL_HBRBACKGROUND, (LONG)GetSysColorBrush(COLOR_BTNFACE));
      }
      break;
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

LRESULT CALLBACK WndProcTop( HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam ) {
   switch(msg){
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hwnd, WM_ERASEBKGND     , Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_MSG(hwnd, WM_COMMAND        , Cls_TopOnCommand);
 //HANDLE_MSG(hwnd, WM_LBUTTONDOWN    , Cls_TopOnLButtonDown);
 //HANDLE_MSG(hwnd, WM_LBUTTONDBLCLK  , Cls_TopOnLButtonDown);
 //HANDLE_MSG(hwnd, WM_RBUTTONDOWN    , Cls_TopOnRButtonDown);
 //HANDLE_MSG(hwnd, WM_RBUTTONDBLCLK  , Cls_TopOnRButtonDown);
 //HANDLE_MSG(hwnd, WM_NCMBUTTONDBLCLK, Cls_TopOnNCMButtonDown);
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

LRESULT CALLBACK WndProcField(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   return gpMosaic->WndProcField(hwnd, msg, wParam, lParam);
}

////////////////////////////////////////////////////////////////////////////////
//                     обработчики сообщений главного окна
////////////////////////////////////////////////////////////////////////////////
// WM_CREATE
BOOL Cls_ProjectOnCreate(HWND hwnd, LPCREATESTRUCT lpCreateStruct)
{
   hWndTop = CreateWindow( szCClassWndTop, NULL, 
      WS_CHILD | WS_VISIBLE | WS_DLGFRAME,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hwnd, (HMENU)0, ghInstance, NULL );
   BtnNew  .Create(hWndTop, ID_BUTTON_NEW_GAME);
   BtnPause.Create(hWndTop, ID_BUTTON_PAUSE);
   hWndEdtCount = CreateWindowEx( WS_EX_STATICEDGE, TEXT("edit"), NULL,
      WS_CHILD | WS_VISIBLE | //WS_BORDER | WS_DLGFRAME |
      ES_LEFT | ES_NUMBER, // | ES_READONLY,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hWndTop, (HMENU)ID_EDITCONTROL_COUNT, ghInstance, NULL );
   hWndEdtTimer = CreateWindowEx( WS_EX_STATICEDGE, TEXT("edit"), NULL,
      WS_CHILD | WS_VISIBLE | //WS_BORDER | WS_DLGFRAME |
      ES_LEFT | ES_NUMBER, // | ES_READONLY,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hWndTop, (HMENU)ID_EDITCONTROL_TIMER, ghInstance, NULL );

   return TRUE;
}

// WM_SIZE
void Cls_ProjectOnSize(HWND hwnd, UINT state, int, int){
   //if (state != SIZE_RESTORED) return;
   if (!gpMosaic) return;
   Border.top = heightPnlTop;
   const POINT sizeFieldInPixel  = gpMosaic->GetSizeWindowField(gpMosaic->GetSizeField(), gpMosaic->GetArea());
   const POINT sizeWindowInPixel = gpMosaic->GetSizeWindowProject(sizeFieldInPixel);
   const POINT sizeScreen = {GetSystemMetrics(SM_CXSCREEN),
                             GetSystemMetrics(SM_CYSCREEN)};
   const POINT sizeClientPnlTop =
      {sizeFieldInPixel.x - 2*GetSystemMetrics(SM_CXDLGFRAME),
       heightPnlTop       - 2*GetSystemMetrics(SM_CYDLGFRAME)};

   RECT Rect;
   GetWindowRect(gpMosaic->GetHandleField(), &Rect);
   /*
   if (((Rect.right -Rect.left) == sizeFieldInPixel.x) &&
       ((Rect.bottom-Rect.top ) == sizeFieldInPixel.y)) return;
   /**/

   MoveWindow(hwnd,
      (sizeScreen.x/2 > sizeWindowInPixel.x/2) ? (sizeScreen.x/2 - sizeWindowInPixel.x/2) : 0,
      (sizeScreen.y/2 > sizeWindowInPixel.y/2) ? (sizeScreen.y/2 - sizeWindowInPixel.y/2) : 0,
      min(sizeWindowInPixel.x, sizeScreen.x),
      min(sizeWindowInPixel.y, sizeScreen.y),
      TRUE);
   MoveWindow(gpMosaic->GetHandleField(),
       0, heightPnlTop,
       sizeFieldInPixel.x,
       sizeFieldInPixel.y,
      TRUE);
   MoveWindow(BtnPause.GetHandle(),
      sizeClientPnlTop.x/2,
      sizeClientPnlTop.y/2 - hwBtnNP/2,
      hwBtnNP,
      hwBtnNP,
      TRUE); //
   MoveWindow(BtnNew.GetHandle(),
      sizeClientPnlTop.x/2 - hwBtnNP,
      sizeClientPnlTop.y/2 - hwBtnNP/2,
      hwBtnNP,
      hwBtnNP,
      TRUE); //
   int w = sizeFieldInPixel.x/2-hwBtnNP;
   int x = w;
   if (x > CEditCountTimer.x) x = CEditCountTimer.x;
   int ofs =  w/2 - x/2;
   if (ofs > 10) ofs = 10;
   if (ofs < 10) x -= 3;
   MoveWindow(hWndEdtCount,
      ofs,
      sizeClientPnlTop.y/2 - CEditCountTimer.y/2,
      x,
      CEditCountTimer.y,
      TRUE); //
   MoveWindow(hWndEdtTimer,
      sizeClientPnlTop.x - x - ofs,
      sizeClientPnlTop.y/2 - CEditCountTimer.y/2,
      x,
      CEditCountTimer.y,
      TRUE); //
   MoveWindow(hWndTop,
      0, 0,
      sizeFieldInPixel.x,
      heightPnlTop,
      TRUE);
   InvalidateRect(hWndTop, NULL, TRUE);
}

// WM_DESTROY
void Cls_ProjectOnDestroy(HWND hwnd){
#ifdef ROBOT_MULTITHREAD
   //WaitForSingleObject(hEventJobEnd, INFINITE);
#endif // ROBOT_MULTITHREAD
   gpMosaic->~TcMosaic();
   gpMosaic = NULL;
   PostQuitMessage(0);
}

// WM_TIMER
void Cls_ProjectOnTimer(HWND hwnd, UINT id) {
   switch (id) {
   case ID_EVENT_TIMER_INACTION:
      KillTimer(hwnd, ID_EVENT_TIMER_INACTION);
      SetTimer (hwnd, ID_EVENT_TIMER_JOB, gpMosaic->GetAssistant().timeoutJob, NULL);
   case ID_EVENT_TIMER_JOB:
      // проверка условий дл€ работы робота (ассистента)
      if (!gpMosaic->GetAssistant().use) return;
      if (hwnd != GetForegroundWindow()) return;
      if (gpMosaic->GetAssistant().autoStart) {
         if (!gpMosaic->GetGameRun()) gpMosaic->GameNew();
      } else {
         if (!gpMosaic->GetGameRun()) {
            if (!gpMosaic->IsFieldEnabled()) {
               return;
            }
         }
      }
      if (gpMosaic->GetPause()) {
         if (gpMosaic->GetAssistant().ignorePause)
            gpMosaic->SetPause(false);
         else return;
      }
      bOpenCellRobot = true;
   #ifndef ROBOT_MULTITHREAD
      Robot_Job();
   #else
      SetEvent(hEventJob);
   #endif // ROBOT_MULTITHREAD
   }
}

// WM_SYSCOMMAND
void Cls_ProjectOnSysCommand(HWND hwnd, UINT cmd, int x, int y) {
   switch (cmd) {
 //case SC_RESTORE:
   case SC_MAXIMIZE:
 //case 0xF012: // one    click in caption
   case 0xF032: // double click in caption
      if (!gpMosaic->AreaMax()) {
         LONG style = GetWindowLong(hwnd, GWL_STYLE);
         if (style & WS_MAXIMIZEBOX) {
            SetWindowLong(hwnd, GWL_STYLE, style ^ WS_MAXIMIZEBOX);
            SendMessage(hwnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
         }
      }
      return;
   }
   DefWindowProc(hwnd, WM_SYSCOMMAND, (WPARAM)cmd, MAKELPARAM(x, y));
}

// WM_COMMAND
void Cls_ProjectOnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify){
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("Cls_ProjectOnCommand: id = 0x"), id, 16);
#endif // USE_INFO_DIALOG

   //DefWindowProc(hwnd, WM_COMMAND, MAKEWPARAM((UINT)id, codeNotify), (LPARAM)hwndCtl);
/**
   switch (id){
   case ID_MENU_GAME_NEW_GAME:
   case ID_MENU_GAME_BEGINNER:
   case ID_MENU_GAME_AMATEUR:
   case ID_MENU_GAME_PROFESSIONAL:
   case ID_MENU_GAME_CRAZY:
   case ID_MENU_GAME_CUSTOM:
   case ID_MENU_FIGURE_TRIANGLE1:
   case ID_MENU_FIGURE_TRIANGLE2:
   case ID_MENU_FIGURE_TRIANGLE3:
   case ID_MENU_FIGURE_TRIANGLE4:
   case ID_MENU_FIGURE_SQUARE1:
   case ID_MENU_FIGURE_SQUARE2:
   case ID_MENU_FIGURE_PARQUET1:
   case ID_MENU_FIGURE_PARQUET2:
   case ID_MENU_FIGURE_TRAPEZOID1:
   case ID_MENU_FIGURE_TRAPEZOID2:
   case ID_MENU_FIGURE_TRAPEZOID3:
   case ID_MENU_FIGURE_RHOMBUS:
   case ID_MENU_FIGURE_QUADRANGLE1:
   case ID_MENU_FIGURE_PENTAGON:
   case ID_MENU_FIGURE_PENTAGONT5:
   case ID_MENU_FIGURE_PENTAGONT10:
   case ID_MENU_FIGURE_HEXAGON:
   case ID_MENU_FIGURE_TRSQ:
   case ID_MENU_FIGURE_TRSQ2:
   case ID_MENU_FIGURE_SQTRHEX:
   case ID_BUTTON_PAUSE:
      WaitForSingleObject(hEventJobEnd, INFINITE);
   }
/**/
   switch (id){
   // menu File
   case ID_MENU_GAME_NEW_GAME:
      gpMosaic->GameNew();
      return;
   case ID_MENU_GAME_BEGINNER:
      gpMosaic->SetSkillLevel(skillLevelBeginner);
      return;
   case ID_MENU_GAME_AMATEUR:
   case ID_MENU_GAME_PROFESSIONAL:
   case ID_MENU_GAME_CRAZY:
   case ID_MENU_GAME_CUSTOM:
#ifndef IGNORE_REGISTRATION
      if (bRegister)
#endif //IGNORE_REGISTRATION
         gpMosaic->SetSkillLevel(TeSkillLevel(id-ID_MENU_GAME_BEGINNER));
      //else MessageBox(hWnd, TEXT("Please register"), TEXT("Info"), MB_OK | MB_ICONINFORMATION);
      return;
   case ID_MENU_GAME_SELECTPLAYER:
      gpMosaic->SetAutoloadAdmin(
         BST_CHECKED ==
         DialogBox(ghInstance, TEXT("SelectPlayer"), ghWnd, (DLGPROC)nsPlayerName::DialogProc)
      );
      return;
   case ID_MENU_GAME_EXIT:
      SendMessage(hwnd, WM_DESTROY, 0L, 0L);
      return;
   // menu Figure
   case ID_MENU_FIGURE_TRIANGLE1:
   case ID_MENU_FIGURE_TRIANGLE2:
   case ID_MENU_FIGURE_TRIANGLE3:
   case ID_MENU_FIGURE_TRIANGLE4:
   case ID_MENU_FIGURE_SQUARE1:
   case ID_MENU_FIGURE_SQUARE2:
   case ID_MENU_FIGURE_PARQUET1:
   case ID_MENU_FIGURE_PARQUET2:
   case ID_MENU_FIGURE_TRAPEZOID1:
   case ID_MENU_FIGURE_TRAPEZOID2:
   case ID_MENU_FIGURE_TRAPEZOID3:
   case ID_MENU_FIGURE_RHOMBUS:
   case ID_MENU_FIGURE_QUADRANGLE1:
   case ID_MENU_FIGURE_PENTAGON:
   case ID_MENU_FIGURE_PENTAGONT5:
   case ID_MENU_FIGURE_PENTAGONT10:
   case ID_MENU_FIGURE_HEXAGON:
   case ID_MENU_FIGURE_TRSQ:
   case ID_MENU_FIGURE_TRSQ2:
   case ID_MENU_FIGURE_SQTRHEX:
      gpMosaic->SetFigure(TeFigure(id-ID_MENU_FIGURE_TRIANGLE1));
      return;
   // menu Options
   case ID_MENU_OPTIONS_UNKNOWN:
      gpMosaic->ChangeUseUnknown();
      return;
   case ID_MENU_OPTIONS_TOOLBAR:
      {  static oldHeightPnlTop = heightPnlTop;
         if (SW_SHOW == gpMosaic->ChangeShowToolbar())
            heightPnlTop = oldHeightPnlTop;
         else {
            oldHeightPnlTop = heightPnlTop;
            heightPnlTop = 0;
         }
         FORWARD_WM_SIZE(hwnd, SIZE_RESTORED, 0,0, SendMessage);
      }
      return;
   case ID_MENU_OPTIONS_MENU:
      {  static HMENU hMenu = GetMenu(hwnd);
         if (SW_SHOW == gpMosaic->ChangeShowMenu())
            SetMenu(hwnd, hMenu);
         else
            SetMenu(hwnd, NULL);
         DrawMenuBar(hwnd);
      }
      return;
   case ID_MENU_OPTIONS_CAPTION:
      if (SW_SHOW == gpMosaic->ChangeShowCaption())
         SetWindowLong(hwnd, GWL_STYLE, WS_CAPTION_YES);
      else
         SetWindowLong(hwnd, GWL_STYLE, WS_CAPTION_NO);
      ShowWindow(hwnd, SW_SHOW);
      DrawMenuBar(hwnd);
      return;
   case ID_MENU_OPTIONS_TRAY:
      gpMosaic->ChangeToTray();
      return;
   case ID_MENU_OPTIONS_SKIN_CHANGE:
      DialogBox(ghInstance, TEXT("SelectSkinDialog"), hwnd, (DLGPROC)nsSelectSkin::DialogProc);
      return;
   case ID_MENU_OPTIONS_SKIN_SAVE:
      DialogBox(ghInstance, TEXT("InputText"), hwnd, (DLGPROC)nsFileSkin::DialogProc);
      nsFileSkin::ReloadSkinList(HSUBMENU_SKIN);
      return;
   case ID_MENU_OPTIONS_SKIN_LOAD:
      //FORWARD_WM_SYSKEYDOWN(hWnd, VK_MENU, 0x0001, 0x2038, SendMessage);
      //FORWARD_WM_SYSKEYDOWN(hWnd, 0x4F   , 0x0001, 0x2018, SendMessage);
      //FORWARD_WM_SYSKEYUP  (hWnd, VK_MENU, 0,0, SendMessage);
      //FORWARD_WM_SYSKEYUP  (hWnd, 0x4F   , 0,0, SendMessage);
      //FORWARD_WM_SYSCHAR   (hWnd, 0x6F   , 0x20180001,   SendMessage);
      //FORWARD_WM_SYSKEYUP  (hWnd, VK_MENU, 0,0, SendMessage);
      return;
   case ID_MENU_OPTIONS_SKIN_LOAD0:
      gpMosaic->SetSkin(gpMosaic->GetSkinDefault());
      DrawMenuBar(hwnd);
      return;
   default:
      if (!codeNotify && (id>ID_MENU_OPTIONS_SKIN_LOAD0)) {
         gpMosaic->SetSkin(nsFileSkin::LoadSkin(HSUBMENU_SKIN, id));
         DrawMenuBar(hwnd);
      }
      return;
   case ID_MENU_OPTIONS_ASSISTANT_ONOFF:
      {
         TsAssistant newAssistant= gpMosaic->GetAssistant();
         newAssistant.use = !newAssistant.use;
         gpMosaic->SetAssistant(newAssistant);
         CheckMenuItem(HSUBMENU_ASSISTANT, ID_MENU_OPTIONS_ASSISTANT_ONOFF,
            newAssistant.use ? MF_CHECKED : MF_UNCHECKED);
      }
      return;
   case ID_MENU_OPTIONS_ASSISTANT_SUGGEST:
      {
         bOpenCellRobot = false;
      #ifndef ROBOT_MULTITHREAD
         Robot_Job();
      #else
         SetEvent(hEventJob);
      #endif // ROBOT_MULTITHREAD
      }
      return;
   case ID_MENU_OPTIONS_ASSISTANT_OPTIONS:
      DialogBox(ghInstance, TEXT("AssistantDialog"), hwnd, (DLGPROC)nsAssistant::DialogProc);
      return;
   // menu Help
   case ID_MENU_HELP_CAMPIONS:
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), hwnd, (DLGPROC)nsChampions::DialogProc);
      return;
   case ID_MENU_HELP_STATISTICS:
      DialogBox(ghInstance, TEXT("StatisticsOrChampionsDialog"), hwnd, (DLGPROC)nsStatistics::DialogProc);
      return;
   case ID_MENU_HELP_REGISTRATION:
      DialogBox(ghInstance, TEXT("RegistrationForm"), hwnd, (DLGPROC)nsRegistration::DialogProc);
#ifndef IGNORE_REGISTRATION
      if (bRegister = nsRegistration::isRegister(NULL, NULL)) {
         HMENU hMenu = GetMenu(ghWnd);
         EnableMenuItem(hMenu, ID_MENU_GAME_AMATEUR     , MF_ENABLED);
         EnableMenuItem(hMenu, ID_MENU_GAME_PROFESSIONAL, MF_ENABLED);
         EnableMenuItem(hMenu, ID_MENU_GAME_CRAZY       , MF_ENABLED);
         EnableMenuItem(hMenu, ID_MENU_GAME_CUSTOM      , MF_ENABLED);
      }
#endif // IGNORE_REGISTRATION
      return;
   case ID_MENU_HELP_ABOUT:
      DialogBox(ghInstance, TEXT("AboutDialog"), hwnd, (DLGPROC)nsAbout::DialogProc);
      return;
   // Accelerators
   case ID_MINIMIZE:
      gpMosaic->SetPause(true);
      ShowWindow(hwnd, SW_MINIMIZE);
      return;
   case ID_BUTTON_PAUSE:
      gpMosaic->SetPause(!gpMosaic->GetPause());
      return;
   case ID_KEY_AREA_INCREMENT:
      if (!gpMosaic->AreaIncrement()) {
         LONG style = GetWindowLong(hwnd, GWL_STYLE);
         if (style & WS_MAXIMIZEBOX) {
            SetWindowLong(hwnd, GWL_STYLE, style ^ WS_MAXIMIZEBOX);
            SendMessage(hwnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
         }
      }
      return;
   case ID_KEY_AREA_DECREMENT:
      gpMosaic->AreaDecrement();
      {
         LONG style = GetWindowLong(hwnd, GWL_STYLE);
         if (!(style & WS_MAXIMIZEBOX)) {
            SetWindowLong(hwnd, GWL_STYLE, style | WS_MAXIMIZEBOX);
            SendMessage(hwnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
         }
      }
      return;
   case ID_KEY_AREA_MIN:
      gpMosaic->AreaMin();
      {
         LONG style = GetWindowLong(hwnd, GWL_STYLE);
         if (!(style & WS_MAXIMIZEBOX)) {
            SetWindowLong(hwnd, GWL_STYLE, style | WS_MAXIMIZEBOX);
            SendMessage(hwnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
         }
      }
      return;
   case ID_KEY_AREA_MAX:
      if (!gpMosaic->AreaMax()) {
         LONG style = GetWindowLong(hwnd, GWL_STYLE);
         if (style & WS_MAXIMIZEBOX) {
            SetWindowLong(hwnd, GWL_STYLE, style ^ WS_MAXIMIZEBOX);
            SendMessage(hwnd, WM_NCACTIVATE, (WPARAM)TRUE, 0L);
         }
      }
      return;
 case ID_KEY_NUM0:
/**
      {
         RECT wndRect; GetWindowRect(hwnd, &wndRect);
         RECT fillRect = {wndRect.left+10, wndRect.top+10, wndRect.right-10, wndRect.bottom-10};
         nsEraseBk::FillWnd(hwnd, 0, false, fillRect);
      }
/**
       {
          nsInfo::AddValue(TEXT("SM_CXHSCROLL = "), GetSystemMetrics(SM_CXHSCROLL), 10);
          nsInfo::AddValue(TEXT("SM_CYHSCROLL = "), GetSystemMetrics(SM_CYHSCROLL), 10);
          nsInfo::AddValue(TEXT("SM_CXHTHUMB  = "), GetSystemMetrics(SM_CXHTHUMB ), 10);
          nsInfo::AddValue(TEXT("SM_CXVSCROLL = "), GetSystemMetrics(SM_CXVSCROLL), 10);
          nsInfo::AddValue(TEXT("SM_CYVSCROLL = "), GetSystemMetrics(SM_CYVSCROLL), 10);
          nsInfo::AddValue(TEXT("SM_CYVTHUMB  = "), GetSystemMetrics(SM_CYVTHUMB ), 10);
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
         nsSelectFigure::SetFirstKey(id-ID_KEY_NUM0);
         TeFigure figure =
            (TeFigure) DialogBox(ghInstance, TEXT("SelectFigure"), hwnd, (DLGPROC)nsSelectFigure::DialogProc);
         if (figure != figureNil)
            gpMosaic->SetFigure(figure);
      }
      return;
   }
}

// WM_ACTIVATE
void Cls_ProjectOnActivate(HWND hwnd, UINT state, HWND hwndActDeact, BOOL fMinimized){
   DefWindowProc(hwnd, WM_ACTIVATE, MAKEWPARAM(state, fMinimized), (LPARAM)hwndActDeact);
   if (state == WA_INACTIVE)
      SetWindowPos( hwnd, HWND_BOTTOM , 0,0,0,0, SWP_NOSIZE | SWP_NOMOVE);
   else
      SetWindowPos( hwnd, HWND_TOPMOST, 0,0,0,0, SWP_NOSIZE | SWP_NOMOVE);
   if ((state == WA_INACTIVE) ||
       (fMinimized != false))
      gpMosaic->SetPause(true);
   if ((fMinimized != false) && gpMosaic->ToTray())
      TrayMessage(NIM_ADD, &szCCaptionProject[0]);
}

// WM_KEYUP
void Cls_ProjectOnKey(HWND hwnd, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   DefWindowProc(hwnd, WM_KEYUP, (WPARAM)vk, MAKELPARAM(cRepeat, flags));
   if ((vk == VK_SNAPSHOT) && gpMosaic->GetGameRun() && !gpMosaic->GetPause())
      gpMosaic->GameNew();
}

// WM_SYSKEYUP
void Cls_ProjectOnSysKey(HWND hwnd, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   DefWindowProc(hwnd, WM_SYSKEYUP, (WPARAM)vk, MAKELPARAM(cRepeat, flags));
   if ((vk == VK_SNAPSHOT) && gpMosaic->GetGameRun() && !gpMosaic->GetPause())
      gpMosaic->GameNew();
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_MENUSELECT
void Cls_ProjectOnMenuSelect(HWND hwnd, HMENU hmenu, int item, HMENU hmenuPopup, UINT flags) {
   DefWindowProc(hwnd, WM_MENUSELECT, MAKEWPARAM(item, flags), (LPARAM)(HMENU)(hmenu ? hmenu : hmenuPopup));
   if ((gpMosaic->GetSkin().toAll) && (hmenu==GetMenu(hwnd)))
      nsEraseBk::FillMenu(hwnd, gpMosaic->GetSkin().colorBk);
}

// WM_PAINT
void Cls_ProjectOnPaint(HWND hwnd) {
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (gpMosaic->GetSkin().toAll) {
    //nsEraseBk::FillCaption(hwnd, gpMosaic->GetSkin().colorBk);
      nsEraseBk::FillMenu   (hwnd, gpMosaic->GetSkin().colorBk);
   }
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                     обработчики сообщений hPnlTop
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpMosaic->GetSkin().toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, gpMosaic->GetSkin().colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_COMMAND
void Cls_TopOnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify){
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("Cls_TopOnCommand: id = 0x"), id, 16);
#endif // USE_INFO_DIALOG
   //DefWindowProc(hwnd, WM_COMMAND, MAKEWPARAM((UINT)id, codeNotify), (LPARAM)hwndCtl);
   switch (id){
   case ID_EDITCONTROL_COUNT:
   case ID_EDITCONTROL_TIMER:
      if (codeNotify == EN_SETFOCUS) {
         EnableWindow(hWndEdtCount, false);
         EnableWindow(hWndEdtCount, true );
         EnableWindow(hWndEdtTimer, false);
         EnableWindow(hWndEdtTimer, true );
      }
      return;
   case ID_BUTTON_NEW_GAME:
      if (hwndCtl == BtnNew.GetHandle()){
         FORWARD_WM_COMMAND(ghWnd, ID_BUTTON_NEW_GAME, hwndCtl, 0, SendMessage);
      }
      return;
   case ID_BUTTON_PAUSE:
      if (hwndCtl == BtnPause.GetHandle()){
         FORWARD_WM_COMMAND(ghWnd, ID_BUTTON_PAUSE, hwndCtl, 0, SendMessage);
      }
      return;
   }
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void Cls_TopOnLButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   FORWARD_WM_LBUTTONDOWN(hwnd, fDoubleClick, x, y, keyFlags, DefWindowProc);
   heightPnlTop = min(heightPnlTop + 5, 64+16);
   hwBtnNP = heightPnlTop - (CHeightPnlTop - CHeightWidthButtonNewPause);
   FORWARD_WM_SIZE(ghWnd, SIZE_RESTORED, 0,0, SendMessage);
}

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void Cls_TopOnRButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   FORWARD_WM_RBUTTONDOWN(hwnd, fDoubleClick, x, y, keyFlags, DefWindowProc);
   heightPnlTop = max(heightPnlTop - 5, 16+16);
   hwBtnNP = heightPnlTop - (CHeightPnlTop - CHeightWidthButtonNewPause);
   FORWARD_WM_SIZE(ghWnd, SIZE_RESTORED, 0,0, SendMessage);
}

// WM_NCMBUTTONDBLCLK
void Cls_TopOnNCMButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT codeHitTest) {
   DefWindowProc(hwnd, fDoubleClick ? WM_NCMBUTTONDBLCLK : WM_NCMBUTTONDOWN, (WPARAM)codeHitTest, MAKELPARAM(x, y));
   MessageBeep(0);
   MessageBeep(0);
}

////////////////////////////////////////////////////////////////////////////////
//                             other function
////////////////////////////////////////////////////////////////////////////////
inline void TrayMessage(UINT msg, const TCHAR* pTip) {
   NOTIFYICONDATA nid;
   memset(&nid, 0, sizeof(NOTIFYICONDATA));
   nid.cbSize           = sizeof(NOTIFYICONDATA);
   nid.hWnd             = ghWnd;
   nid.uID              = 0;
   nid.uFlags           = NIF_ICON | NIF_MESSAGE | NIF_TIP;
   nid.uCallbackMessage = WM_NOTIFYICON;
   nid.hIcon            = hIconProject;
   if (pTip) _tcscpy(nid.szTip, pTip);
   Shell_NotifyIcon(msg, &nid);
   if (msg == NIM_ADD)
      ShowWindow(ghWnd, SW_HIDE);
   if (msg == NIM_DELETE) {
      ShowWindow(ghWnd, SW_RESTORE);
      //FORWARD_WM_ACTIVATE(hWnd, WA_ACTIVE, NULL, FALSE, PostMessage);
      //FORWARD_WM_SETFOCUS(hWnd, NULL, PostMessage);
      //ShowWindow(hWnd, SW_SHOW);
      //EnableWindow(hWnd, TRUE);
      //SetFocus(hWnd);
      //SetActiveWindow(hWnd);
   }
}

inline void ResetTimerRobot() {
   if (!gpMosaic) return;
#ifdef ROBOT_MULTITHREAD
   ResetEvent(hEventJob);
#endif // ROBOT_MULTITHREAD
   KillTimer(ghWnd, ID_EVENT_TIMER_JOB     );
   KillTimer(ghWnd, ID_EVENT_TIMER_INACTION);
   if (!gpMosaic->GetAssistant().use) return;
   SetTimer(ghWnd, ID_EVENT_TIMER_INACTION, gpMosaic->GetAssistant().timeoutUnactive, NULL);
}

#define IMAGE_JOB_SET   SendMessage(BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)gpMosaic->GetImageBtnPause(3))
#define IMAGE_JOB_RESET SendMessage(BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, \
                           gpMosaic->GetGameRun() ?                        \
                              gpMosaic->GetPause() ?                       \
                              (LPARAM)gpMosaic->GetImageBtnPause(1) :      \
                              (LPARAM)gpMosaic->GetImageBtnPause(0) :      \
                           (LPARAM)gpMosaic->GetImageBtnPause(2))

inline void Robot_Job() {
   static POINT lastClick = CIncorrectCoord;
   if (lastClick != CIncorrectCoord) IMAGE_JOB_SET;
   TsClickData click;
   if (gRobot.isSequentialMove()) { // выполн€етс€ ли перебор флажков?
      gRobot.SequentialMove(click); // да, выполн€етс€ - продолжить перебор флажков
   } else {
      if (!gRobot.FindCell(click)) { // найти €чейку дл€ клика
         IMAGE_JOB_RESET;
         return; // работа была прервана
      }
      if (click.coordCell == CIncorrectCoord) {
         if (!gRobot.AllOkToSequentialMove()) { // проверка условий дл€ начала перебора флажков
            IMAGE_JOB_RESET;
            return; // работа была прервана
         }
         if (gRobot.isSequentialMove()) {       // выполн€ть ли перебор флажков?
            gRobot.SequentialMove(click);       // да, выполн€ть - начать перебор флажков
         } else {
            if (gpMosaic->GetGameRun() &&
                gpMosaic->GetAssistant().stopJob &&
                bOpenCellRobot
               )
            {
               IMAGE_JOB_RESET;
               return; // останавливать когда нет однозначного следующего хода
            }
            if (click.probability >= 0.5) { // веро€тность больше 50%
               click.coordCell = click.prbltCell;
            #ifdef USE_INFO_DIALOG
               //nsInfo::AddValue(TEXT("coord probability = "), click.coordCell, 10);
               //nsInfo::AddValue(TEXT("      probability = "), click.probability);
            #endif // USE_INFO_DIALOG
            } else {
               do {
                  click.coordCell.x = (gpMosaic->GetSizeField().x*rand())/RAND_MAX; if (click.coordCell.x == gpMosaic->GetSizeField().x) click.coordCell.x--; // :(
                  click.coordCell.y = (gpMosaic->GetSizeField().y*rand())/RAND_MAX; if (click.coordCell.y == gpMosaic->GetSizeField().y) click.coordCell.y--; // :(
               } while ((gpMosaic->GetField(click.coordCell.x,click.coordCell.y)->Cell_GetStatus() == _Open) ||
                        (gpMosaic->GetField(click.coordCell.x,click.coordCell.y)->Cell_GetClose()  == _Flag));
            }
         }
      }
   }
   lastClick = click.coordCell;
   IMAGE_JOB_RESET;
   const POINT coordCenter = gpMosaic->GetField(click.coordCell.x,click.coordCell.y)->GetCenterPixel();

   { // set cursor to new position
      POINT coordCursor = coordCenter;
      ClientToScreen(gpMosaic->GetHandleField(), &coordCursor);
   #ifdef ROBOT_MULTITHREAD
      SetEvent(hEventSetCursorBegin);
   #else
      b_WM_SETCURSOR_Break = true;
   #endif // ROBOT_MULTITHREAD
      SetCursorPos(coordCursor.x, coordCursor.y);
   #ifdef ROBOT_MULTITHREAD
      WaitForSingleObject(hEventSetCursorEnd, INFINITE);
      SetEvent(hEventSetCursorBegin); // ???
   #else
      //b_WM_SETCURSOR_Break = false;
   #endif // ROBOT_MULTITHREAD
   }
   if (bOpenCellRobot) {
      if (gpMosaic->GetAssistant().beep) MessageBeep(0);
      if (click.isLeft) {
         FORWARD_WM_LBUTTONDOWN(gpMosaic->GetHandleField(), FALSE, coordCenter.x, coordCenter.y, MK_ROBOT, SendMessage);
         FORWARD_WM_LBUTTONUP  (gpMosaic->GetHandleField(),        coordCenter.x, coordCenter.y, MK_ROBOT, SendMessage);
      } else {
         FORWARD_WM_RBUTTONDOWN(gpMosaic->GetHandleField(), FALSE, coordCenter.x, coordCenter.y, MAKEWPARAM(MK_ROBOT, click.close), SendMessage);
      }
   }
}

#ifdef ROBOT_MULTITHREAD
DWORD WINAPI ChildThread(PVOID pvParam) {
   const HANDLE pHandles[2]= {hEventJob, (HANDLE)pvParam};

   for (;;) {
      switch (WaitForMultipleObjects(2, pHandles, FALSE, INFINITE)) {
      case WAIT_OBJECT_0:
         //ResetEvent(hEventJobEnd);
         try {
            Robot_Job();
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
#endif // ROBOT_MULTITHREAD
