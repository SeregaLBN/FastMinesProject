////////////////////////////////////////////////////////////////////////////////
// File name: CommonLib.h
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 15 09 2004
//
// Description: Функции общего назначения.
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__LIB__
#define __FILE__LIB__

#ifndef __AFX_H__
   #include <Windows.h>
   #include "CStringKS.h"
#endif
#include <WindowsX.h>

#define chDIMOF(Array) (sizeof(Array) / sizeof(Array[0])) // количество элементов в массиве

typedef unsigned (__stdcall *PTHREAD_START) (void *); // © Джеффри Рихтер
#define chBEGINTHREADEX(psa, cbStack, pfnStartAddr,   /* © Джеффри Рихтер*/ \
   pvParam, fdwCreate, pdwThreadId)                 \
      ((HANDLE)_beginthreadex(                      \
         (void *)        (psa),                     \
         (unsigned)      (cbStack),                 \
         (PTHREAD_START) (pfnStartAddr),            \
         (void *)        (pvParam),                 \
         (unsigned)      (fdwCreate),               \
         (unsigned *)    (pdwThreadId)))

BOOL RegClass(
    UINT      style,
    WNDPROC   lpfnWndProc,
    int       cbClsExtra,
    int       cbWndExtra,
    HINSTANCE hInstance,
    HICON     hIcon,
    HCURSOR   hCursor,
    HBRUSH    hbrBackground,
    LPCTSTR   lpszMenuName,
    LPCTSTR   lpszClassName
);

void MessageBox_AbortProcess(LPCTSTR szMessage);
BOOL InitializeCriticalSectionEx(PCRITICAL_SECTION pCS, DWORD dwSpinCount = 4000);

void Base64_code  (IN const CHAR* strIn, OUT CString& strOut); // Base 64 кодирование
void Base64_decode(IN const CHAR* strIn, OUT CString& strOut); // Base 64 декодирование
BOOL Base64_code(
   IN BYTE *pData,     // на вход  - данные, которые надо зашифровать
   IN DWORD dwSize,
   OUT CString &strRes // на выход - зашифрованная строка
);
BOOL Base64_decode(
   IN const CString &strIn, // на вход  - зашифрованная строка
   OUT BYTE *pData,         // на выход - расшифрованные данные
   IN OUT DWORD &dwSize
);

struct POINTEX: public POINT {
   POINTEX()                 {x=      y=0   ;}
   POINTEX(const POINT &p)   {x=p. x; y=p. y;}
   POINTEX(const SIZE  &p)   {x=p.cx; y=p.cy;}
   POINTEX(LONG nx, LONG ny) {x=  nx; y=  ny;}
   POINTEX& operator+= (const POINT &a) {x+=a.x; y+=a.y; return *this;}
   POINTEX& operator-= (const POINT &a) {x-=a.x; y-=a.y; return *this;}
   POINTEX& operator*= (LONG val      ) {x*=val; y*=val; return *this;}
   POINTEX& operator/= (LONG val      ) {x/=val; y/=val; return *this;}
   operator SIZE() const {SIZE result =   {x,y}; return result;}
   operator RECT() const {RECT rect = {0,0,x,y}; return rect;}
};

POINT operator*  (LONG  val     , const POINT &a);
POINT operator*  (const POINT &a, LONG  val     );
POINT operator/  (const POINT &a, LONG  val     );
POINT operator+  (const POINT &a, const POINT &b);
POINT operator-  (const POINT &a, const POINT &b);
bool  operator== (const POINT &a, const POINT &b);
bool  operator!= (const POINT &a, const POINT &b);

struct SIZEEX: public SIZE {
   SIZEEX()                 {cx=      cy=0   ;}
   SIZEEX(const SIZE  &p)   {cx=p.cx; cy=p.cy;}
   SIZEEX(const POINT &p)   {cx=p. x; cy=p. y;}
   SIZEEX(const RECT  &p)   {cx=p.right - p.left; cy=p.bottom-p.top;}
   SIZEEX(LONG nX, LONG nY) {cx=nX  ; cy=nY  ;}
   SIZEEX& operator+= (const SIZE &a) {cx+=a.cx; cy+=a.cy; return *this;}
   SIZEEX& operator-= (const SIZE &a) {cx-=a.cx; cy-=a.cy; return *this;}
   SIZEEX& operator*= (LONG val     ) {cx*=val ; cy*=val ; return *this;}
   SIZEEX& operator/= (LONG val     ) {cx/=val ; cy/=val ; return *this;}
   operator POINT() const {POINT result =   {cx,cy}; return result;}
   operator RECT () const {RECT  rect = {0,0,cx,cy}; return rect;}
};

SIZE operator*  (LONG  val    , const SIZE &a);
SIZE operator*  (const SIZE &a, LONG  val    );
SIZE operator/  (const SIZE &a, LONG  val    );
SIZE operator+  (const SIZE &a, const SIZE &b);
SIZE operator-  (const SIZE &a, const SIZE &b);
bool operator== (const SIZE &a, const SIZE &b);
bool operator!= (const SIZE &a, const SIZE &b);

struct COORDEX: public COORD {
   COORDEX()                   {X=     Y=0  ;}
   COORDEX(const COORD& p)     {X=p.X; Y=p.Y;}
   COORDEX(SHORT nX, SHORT nY) {X= nX; Y= nY;}
   COORDEX& operator+= (const COORD &a) {X+=a.X; Y+=a.Y; return *this;}
   COORDEX& operator-= (const COORD &a) {X-=a.X; Y-=a.Y; return *this;}
   COORDEX& operator*= (SHORT val     ) {X*=val; Y*=val; return *this;}
   COORDEX& operator/= (SHORT val     ) {X/=val; Y/=val; return *this;}
};

COORD operator*  (SHORT val     , const COORD &a);
COORD operator*  (const COORD &a, SHORT val     );
COORD operator/  (const COORD &a, SHORT val     );
COORD operator+  (const COORD &a, const COORD &b);
COORD operator-  (const COORD &a, const COORD &b);
bool  operator== (const COORD &a, const COORD &b);
bool  operator!= (const COORD &a, const COORD &b);

struct RECTEX: public RECT {
    // Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
   RECTEX& moveX(LONG x)         {left += x; right += x; return *this;}
   RECTEX& moveY(LONG y)         {top  += y; bottom+= y; return *this;}
   RECTEX& move (LONG x, LONG y) {moveX(x); return moveY(y);}
   RECTEX& move (const SIZE  &p) {return move(p.cx, p.cy);}

   // Выравнивание прямоугольника (без изменений размеров прямоугольника)
   RECTEX& alignLeft  (LONG l)         {right  += l-left  ; left   = l; return *this;} // выровнять прямоугольник по левой   стороне к заданному значению
   RECTEX& alignRight (LONG r)         {left   += r-right ; right  = r; return *this;} // выровнять прямоугольник по правой  стороне к заданному значению
   RECTEX& alignTop   (LONG t)         {bottom += t-top   ; top    = t; return *this;} // выровнять прямоугольник по верхней стороне к заданному значению
   RECTEX& alignBottom(LONG b)         {top    += b-bottom; bottom = b; return *this;} // выровнять прямоугольник по нижней  стороне к заданному значению
   RECTEX& alignLT    (LONG x, LONG y) {alignLeft (x); return alignTop   (y);}
   RECTEX& alignRT    (LONG x, LONG y) {alignRight(x); return alignTop   (y);}
   RECTEX& alignLB    (LONG x, LONG y) {alignLeft (x); return alignBottom(y);}
   RECTEX& alignRB    (LONG x, LONG y) {alignRight(x); return alignBottom(y);}
   RECTEX& alignLT    (const POINT &p) {return alignLT(p.x, p.y);}
   RECTEX& alignRT    (const POINT &p) {return alignRT(p.x, p.y);}
   RECTEX& alignLB    (const POINT &p) {return alignLB(p.x, p.y);}
   RECTEX& alignRB    (const POINT &p) {return alignRB(p.x, p.y);}

   RECTEX& alignCenter(const POINT &c) {return move(SIZEEX(c-center()));}        // совместить центр прямоугольника с заданной точкой центра
   RECTEX& alignCenter(const RECT  &r) {return alignCenter(RECTEX(r).center());} // совместить центр прямоугольника с центром заданного прямоугольника
   RECTEX& alignCenter(LONG x, LONG y) {return alignCenter(POINTEX(x,y));}       // совместить центр прямоугольника с заданнымм координатами

   // get/set metods
   LONG    width () const {return right -left;}
   LONG    height() const {return bottom-top ;}
   RECTEX& width (LONG w) {right  = left+w; return *this;}
   RECTEX& height(LONG h) {bottom = top +h; return *this;}

   POINTEX pointLT() const           {return POINTEX(left , top   );}
   POINTEX pointRT() const           {return POINTEX(right, top   );}
   POINTEX pointLB() const           {return POINTEX(left , bottom);}
   POINTEX pointRB() const           {return POINTEX(right, bottom);}
   RECTEX& pointLT(LONG x, LONG y)   {left  = x; top    = y; return *this;}
   RECTEX& pointRT(LONG x, LONG y)   {right = x; top    = y; return *this;}
   RECTEX& pointLB(LONG x, LONG y)   {left  = x; bottom = y; return *this;}
   RECTEX& pointRB(LONG x, LONG y)   {right = x; bottom = y; return *this;}
   RECTEX& pointLT(const POINT& pLT) {return pointLT(pLT.x, pLT.y);}
   RECTEX& pointRT(const POINT& pRT) {return pointRT(pRT.x, pRT.y);}
   RECTEX& pointLB(const POINT& pLB) {return pointLB(pLB.x, pLB.y);}
   RECTEX& pointRB(const POINT& pRB) {return pointRB(pRB.x, pRB.y);}

   POINTEX center()const          {return POINTEX(left+width()/2, top+height()/2);}
   RECTEX& center( const POINT&c) {return alignCenter( c );} // совместить центр прямоугольника с заданной точкой центра
   RECTEX& center( const RECT &r) {return alignCenter( r );} // совместить центр прямоугольника с центром заданного прямоугольника
   RECTEX& center(LONG x, LONG y) {return alignCenter(x,y);} // совместить центр прямоугольника с заданными координатами
   SIZEEX  size  ()const          {return SIZEEX(width(), height());}
   RECTEX& size  ( const SIZE &s) {width(s.cx); return height(s.cy);}

   operator SIZE  () {return size();}
   operator SIZEEX() {return size();}

   // constructors
   RECTEX(const POINT &pLT, const POINT pRB) {left=pLT.x  ; top=pLT.y ; right=pRB.x   ; bottom=pRB.y   ;}
   RECTEX(const RECT &r)                     {left=r.left ; top=r.top ; right=r.right ; bottom=r.bottom;}
   RECTEX(LONG l, LONG t, LONG r, LONG b)    {left=l      ; top=t     ; right=r       ; bottom=b       ;}
   RECTEX(const SIZE &size)                  {left=         top=0     ; right=size.cx ; bottom=size.cy ;}
   RECTEX(LONG width, LONG height)           {left=         top=0     ; right=width   ; bottom=height  ;}
   RECTEX()                                  {left=         top=        right=          bottom=0       ;}
};

bool operator== (const RECT &a, const RECT &b);
bool operator!= (const RECT &a, const RECT &b);

RECTEX FindInnerRect (const SIZE  &sizeInner, const SIZE &sizeOutward);
RECTEX FindInnerRect (const RECT  &rectInner, const RECT &rectOutward);
BOOL   PointInRect   (const POINT &point, const RECT &rect   ); // точка внутри прямоугольника?
BOOL   IntersectRect (const RECT  &rect1, const RECT &rect2  ); // пересекаются ли прямоугольники?
bool   PointInPolygon(const POINT &point, const POINT *const polygon, int size); // принадлежность точки фигуре

BOOL MoveWindow(HWND, const RECT &rect, BOOL bRepaint = TRUE);
BOOL SetWindowPos(HWND hWnd, HWND hWndInsertAfter, const RECT &rect, UINT uFlags);

RECTEX  ScreenToClient(HWND, const RECT&);
RECTEX  ClientToScreen(HWND, const RECT&);
POINTEX ClientToScreen(HWND, const POINT&);
POINTEX ScreenToClient(HWND, const POINT&);

RECTEX GetWindowRect(HWND hWnd);
SIZEEX GetWindowSize(HWND hWnd);
RECTEX GetClientRect(HWND hWnd);
SIZEEX GetClientSize(HWND hWnd);

POINTEX GetCursorPos();

WNDPROC GetWindowProc    (HWND);
WNDPROC SetWindowProc    (HWND, WNDPROC pWndProc);
//DWORD GetWindowStyle   (HWND); // определено в 'WindowsX.h'
DWORD   SetWindowStyle   (HWND, DWORD lStyle);
DWORD   GetWindowStyleEx (HWND);
DWORD   SetWindowStyleEx (HWND, DWORD lStyleEx);
LONG    GetWindowUserData(HWND);
LONG    SetWindowUserData(HWND, LONG lUserData);

BOOL SetMenuText(HMENU hMenu, UINT uItem, BOOL bByPosition, LPCTSTR szNewText);

SIZEEX GetScreenSize();

CString GetFileDir (LPCTSTR szPathAlongWithFileName); // возвращает только путь к файлу без имени файла (путь с наклонной чертой в конце)
CString GetFileName(LPCTSTR szPathAlongWithFileName); // возвращает только имя файла без пути
BOOL PathExist(LPCTSTR szPath);
BOOL FileExist(LPCTSTR szPath);
CString GetMenuString(HMENU hMenu, UINT uIDItem, UINT uFlag);
CString GetModuleFileName(HMODULE hModule = NULL); // retrieves the full path and filename for the executable file containing the specified module
CString GetModuleDir     (HMODULE hModule); // возвращает путь к модулю без имени модуля (с наклонной чертой в конце)
CString GetWindowText(HWND hWnd);

CString GetComputerName();
CString GetUserName();

CString GetSystemDirectory();
CString GetWindowsDirectory();
CString GetTempPath();

SIZEEX SizeBitmap(HWND hWnd, BOOL bClientRect = FALSE); // размер битмапы у окна
SIZEEX SizeBitmap(HDC hCDC); // only for compatible device context
SIZEEX SizeBitmap(HBITMAP hBmp); // hBmp не должен быть связан с контекстом устройства

HBITMAP CreateBitmap(SIZE size);  // create compatible bitmap at desktop
HBITMAP CreateMask(SIZE sizeBmp); // create monohrome bitmap
HBITMAP CreateMask(HBITMAP hBmp, COLORREF transparentColor = CLR_INVALID); // create monohrome bitmap from color bitmap

int rand(int maxDiapason); // генерирует случайное число от 0 до maxDiapason, ВКЛЮЧАЯ верхнюю границу

CString MemCopyAsString(LPCVOID pBuf, size_t size, TCHAR chSeparatorEOL = TEXT('\n'));

void BeepSpeaker(DWORD dwFreq = 500, DWORD dwDuration = 0x25);

CString Format(LPCTSTR szFormat, ...);

#endif // __FILE__LIB__
