////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "WinDefAdd.h"
////////////////////////////////////////////////////////////////////////////////

/*
 * My Window Styles
 */

#define WS_CAPTION_HIDE (WS_CLIPCHILDREN | WS_OVERLAPPED | WS_BORDER  | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX) //WS_POPUPWINDOW //
#define WS_CAPTION_SHOW (WS_CLIPCHILDREN | WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX | WS_MAXIMIZEBOX)

/*
 * My Window Messages
 */

#define MK_ASSISTANT 0x0100 // my Key State Mask for Mouse Messages

#define WM_NOTIFYICON            (WM_USER+1)
#define WM_MOSAIC_ADJUSTAREA     (WM_USER+2)
#define WM_MOSAIC_CLICK          (WM_USER+3) // мозаика уведомляет родительское окно о том, что на ней (мозаике) был произведён клик
#define WM_MOSAIC_CHANGECOUNTERS (WM_USER+4) // мозаика уведомляет родительское окно о изменениях счётчика таймера/мин
#define WM_MOSAIC_GAMENEW        (WM_USER+5) // мозаика уведомляет родительское окно о начале новой игры
#define WM_MOSAIC_GAMEBEGIN      (WM_USER+6) // мозаика уведомляет родительское окно о старте новой игры
#define WM_MOSAIC_GAMEEND        (WM_USER+7) // мозаика уведомляет родительское окно о конце игры
#define WM_MOSAIC_PAUSE          (WM_USER+8) // мозаика уведомляет родительское окно о изменении состояния паузы

/****** My Message crackers ****************************************************/

/* void Cls_OnNotifyIcon(HWND hwnd, UINT notifyMsg) */
#define HANDLE_WM_NOTIFYICON(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd), (UINT)(lParam)), 0L)
#define FORWARD_WM_NOTIFYICON(hwnd, notifyMsg, fn) \
    (void)(fn)((hwnd), WM_NOTIFYICON, 0L, (LPARAM)(UINT)(notifyMsg))

/* void Cls_OnMosaicAdjustArea(HWND hwnd) */
#define HANDLE_WM_MOSAIC_ADJUSTAREA(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_ADJUSTAREA(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_ADJUSTAREA, 0L, 0L)

/* void Cls_OnMosaicClick(HWND hwnd, UINT keyFlags, BOOL leftClick, BOOL down) */
#define HANDLE_WM_MOSAIC_CLICK(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd), (UINT)(wParam), (BOOL)LOWORD(lParam), (BOOL)HIWORD(lParam)), 0L)
#define FORWARD_WM_MOSAIC_CLICK(hwnd, keyFlags, leftClick, down, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_CLICK, (WPARAM)(UINT)(keyFlags), MAKELPARAM((leftClick), (down)))

/* void Cls_OnMosaicChangeCounters(HWND hwnd) */
#define HANDLE_WM_MOSAIC_CHANGECOUNTERS(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_CHANGECOUNTERS(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_CHANGECOUNTERS, 0L, 0L)

/* void Cls_OnMosaicGameNew(HWND hwnd) */
#define HANDLE_WM_MOSAIC_GAMENEW(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_GAMENEW(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_GAMENEW, 0L, 0L)

/* void Cls_OnMosaicGameBegin(HWND hwnd) */
#define HANDLE_WM_MOSAIC_GAMEBEGIN(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_GAMEBEGIN(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_GAMEBEGIN, 0L, 0L)

/* void Cls_OnMosaicGameEnd(HWND hwnd) */
#define HANDLE_WM_MOSAIC_GAMEEND(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_GAMEEND(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_GAMEEND, 0L, 0L)

/* void Cls_OnMosaicPause(HWND hwnd) */
#define HANDLE_WM_MOSAIC_PAUSE(hwnd, wParam, lParam, fn) \
    ((fn)((hwnd)), 0L)
#define FORWARD_WM_MOSAIC_PAUSE(hwnd, fn) \
    (void)(fn)((hwnd), WM_MOSAIC_PAUSE, 0L, 0L)
