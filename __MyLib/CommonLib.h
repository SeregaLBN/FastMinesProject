////////////////////////////////////////////////////////////////////////////////
// File name: CommonLib.h
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 2005 06 26
//
// Description: Функции общего назначения.
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__KSERG_COMMON_LIB__
#define __FILE__KSERG_COMMON_LIB__

#if _MSC_VER > 1000
   #pragma once
#endif // _MSC_VER > 1000

#ifndef __AFX_H__
   #include <Windows.h>
   #include "CStringKS.h"
#endif
#include <WindowsX.h>
#include <ShlObj.h>

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
void ShowCursor();
void HideCursor();

CString GetClassName     (HWND);
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

CString SelectFile(BOOL bOpenDialog, HWND hWndOwner, LPCTSTR szDefSelectedFile = NULL, LPCTSTR szDefExt = NULL, LPCTSTR szFilter = _T("All files (*.*)\0*.*\0\0"), LPCTSTR szTitle = NULL, LPCTSTR szInitialDir = NULL, DWORD dwFlags = OFN_EXPLORER | OFN_FILEMUSTEXIST | OFN_HIDEREADONLY | OFN_LONGNAMES | OFN_PATHMUSTEXIST);
CString SelectFolder(
   HWND hwndOwner  = NULL,                      // родительское окно для диалога
   LPCTSTR szTitle = _T("Open folder"),      // текст на окне
   DWORD dwFlags   = BIF_BROWSEFORCOMPUTER | BIF_DONTGOBELOWDOMAIN | BIF_EDITBOX,
   int nFolder     = /** /CSIDL_DRIVES/**/-1/**/); // если -1 - то вызов стандартного диалога выбора папки
                                             // иначе спецпапка - see CSIDL_... (func SHGetSpecialFolderLocation)


DWORD   GetFileSize(LPCTSTR szFileName); // размер файла
CString GetFileDir (LPCTSTR szPathAlongWithFileName); // возвращает только путь к файлу без имени файла (путь с наклонной чертой в конце)
CString GetFileName(LPCTSTR szPathAlongWithFileName); // возвращает только имя файла без пути
CString GetFileVersion(LPCTSTR szFilename);
BOOL PathExist(LPCTSTR szPath);
BOOL FileExist(LPCTSTR szPath);
CString GetMenuString(HMENU hMenu, UINT uIDItem, UINT uFlag);
CString GetModuleFileName(HMODULE hModule = NULL); // retrieves the full path and filename for the executable file containing the specified module
CString GetModuleDir     (HMODULE hModule); // возвращает путь к модулю без имени модуля (с наклонной чертой в конце)
CString GetWindowText(HWND hWnd);

CString GetComputerName();
CString GetUserName();

CString GetCurrentDirectory();
CString GetSystemDirectory();
CString GetWindowsDirectory();
CString GetTempPath();

SIZEEX SizeIcon(HICON hIcon);
SIZEEX SizeBitmap(HWND hWnd, BOOL bClientRect = FALSE); // размер битмапы у окна
SIZEEX SizeBitmap(HDC hCDC); // only for compatible device context
SIZEEX SizeBitmap(HBITMAP hBmp); // hBmp не должен быть связан с контекстом устройства

HBITMAP CreateBitmap(int iWidth, int iHeight);  // create compatible bitmap at desktop
HBITMAP CreateBitmap(const SIZE &size);         // create compatible bitmap at desktop
HBITMAP CreateMask(const SIZE &sizeBmp);        // create monohrome bitmap
HBITMAP CreateMask(HBITMAP hBmp,
      COLORREF transparentColor = CLR_INVALID); // create monohrome bitmap from color bitmap

int rand(int maxDiapason); // генерирует случайное число от 0 до maxDiapason, ВКЛЮЧАЯ верхнюю границу

CString MemCopyAsString(LPCVOID pData, size_t iSize, TCHAR chSeparatorEOL = TEXT('\n'));
CString MemCopyAsHex   (LPCVOID pData, size_t iSize, BOOL bUsePrefix = FALSE, LPCTSTR szSeparator = NULL);

void BeepSpeaker(DWORD dwFreq = 500, DWORD dwDuration = 0x25);

CString Format(LPCTSTR szFormat, ...);
#ifndef _UNICODE
   CString AnsiToOem(LPCTSTR);
   CString OemToAnsi(LPCTSTR);
#endif // _UNICODE

class COSVersion {
private:
   OSVERSIONINFO m_vi;
public:
   COSVersion() {
      m_vi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
      BOOL bRes = ::GetVersionEx(&m_vi);
      if (!bRes) memset(&m_vi,0,sizeof(OSVERSIONINFO));
   }
   bool IsWinXP()       const {return ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && (m_vi.dwMajorVersion == 5) && (m_vi.dwMinorVersion == 1));}
   bool IsWin2000()     const {return ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && (m_vi.dwMajorVersion == 5) && (m_vi.dwMinorVersion == 0));}
   bool IsWinNT()       const {return ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_NT) && ((m_vi.dwMajorVersion == 4) || (m_vi.dwMajorVersion == 3))/* && (m_vi.dwMinorVersion == 0)*/);}
   bool IsWin9598Me()   const {return ((m_vi.dwPlatformId == VER_PLATFORM_WIN32_WINDOWS) && (m_vi.dwMajorVersion == 4)/* && (m_vi.dwMinorVersion == 0)*/);}
};

SYSTEMTIME GetSystemTime();
FILETIME   GetSystemTimeAsFileTime();
SYSTEMTIME FileTimeToSystemTime(const FILETIME   &FileTime);
FILETIME   SystemTimeToFileTime(const SYSTEMTIME &SysTime);

BOOL BitBlt    (HDC, const RECT&, HDC, const SIZE&, DWORD);
BOOL BitBlt    (HDC, const RECT&, HDC, /* ={0,0} */ DWORD);
BOOL BitBlt    (HDC, const SIZE&, HDC, const SIZE&, DWORD);
BOOL BitBlt    (HDC, const SIZE&, HDC, /* ={0,0} */ DWORD);
BOOL StretchBlt(HDC, const RECT&, HDC, const RECT&, DWORD);
BOOL StretchBlt(HDC, const RECT&, HDC, const SIZE&, DWORD);
BOOL StretchBlt(HDC, const SIZE&, HDC, const SIZE&, DWORD);

class CRegistry {
public:
   // read methods
   static CString GetString(IN HKEY hKey, IN LPCTSTR szParameter                      , OUT PDWORD pdwErrCode = NULL);
   static DWORD   GetDWord (IN HKEY hKey, IN LPCTSTR szParameter                      , OUT PDWORD pdwErrCode = NULL);
   // write methods
   static DWORD   SetString(IN HKEY hKey, IN LPCTSTR szParameter, IN LPCTSTR szValue); // return error code
   static DWORD   SetDWord (IN HKEY hKey, IN LPCTSTR szParameter, IN DWORD   dwValue); // return error code

   // read methods
   static CString GetString(IN LPCTSTR szKeyName                                      , OUT PDWORD pdwErrCode = NULL); // example - "HKEY_CURRENT_USER\\SOFTWARE\\strParam"
   static DWORD   GetDWord (IN LPCTSTR szKeyName                                      , OUT PDWORD pdwErrCode = NULL); // example - "HKEY_CURRENT_USER\\SOFTWARE\\dwParam"
   // write methods
   static DWORD   SetString(IN LPCTSTR szKeyName, IN LPCTSTR szValue                ); // example - "HKEY_CURRENT_USER\\SOFTWARE\\strParam", "value"     , return error code
   static DWORD   SetDWord (IN LPCTSTR szKeyName, IN DWORD   dwValue                ); // example - "HKEY_CURRENT_USER\\SOFTWARE\\dwParam", 123          , return error code

   // find methods
   static CString GetSubKeyName(IN HKEY hKey        , IN int iIndex                   , OUT PDWORD pdwErrCode = NULL);
   static CString GetSubKeyName(IN LPCTSTR szKeyName, IN int iIndex                   , OUT PDWORD pdwErrCode = NULL); // example - "HKEY_CURRENT_USER\\SOFTWARE", 1  or  "HKEY_CURRENT_USER\\SOFTWARE\\", 1
   static CString GetValueName (IN HKEY hKey        , IN int iIndex, OUT DWORD &dwType, OUT PDWORD pdwErrCode = NULL);
   static CString GetValueName (IN LPCTSTR szKeyName, IN int iIndex, OUT DWORD &dwType, OUT PDWORD pdwErrCode = NULL); // example - "HKEY_CURRENT_USER\\SOFTWARE", 1  or  "HKEY_CURRENT_USER\\SOFTWARE\\", 1
};

CString MIMEType2FileType(LPCTSTR szMIMEType);
CString FileType2MIMEType(LPCTSTR szFileType);

CString Replace(IN const CString &str, IN LPCTSTR szOld, IN LPCTSTR szNew);
CString Replace(IN const CString &str, IN TCHAR    cOld, IN TCHAR    cNew);

CString GetIPAdress();
CString GetHostName();

LPVOID GetResource( // возвращаю указатель на данные ресурса
   IN HINSTANCE hInstance,
   IN LPCTSTR szResourceName,
   IN LPCTSTR szResourceType,
   OUT DWORD &dwSize,
   OUT PDWORD pdwErrCode = NULL
);
BOOL SaveResourceAsFile( // читаю из ресурсов и сохраняю в файл если его ещё нет
   IN HINSTANCE hInstance,
   IN LPCTSTR szFileName,
   IN LPCTSTR szResourceName,
   IN LPCTSTR szResourceType,
   OUT PDWORD pdwErrCode = NULL
);

CString GUID2String(IN REFGUID guid);
BOOL    String2GUID(IN LPCTSTR szGUID, OUT GUID &guid); // example - {00000000-0000-0000-C000-000000000046} to IID_IUnknown

#endif // __FILE__KSERG_COMMON_LIB__
