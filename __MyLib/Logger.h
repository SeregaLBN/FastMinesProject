////////////////////////////////////////////////////////////////////////////////
// File name: Logger.h
// Author: Sergey Krivulya (Ce���� �p�����) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// ICQ: 72099167
// Date: 2005 08 31
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__LOGGER__
#define __FILE__LOGGER__

#if _MSC_VER > 1000
   #pragma once
#endif // _MSC_VER > 1000

#ifndef __AFX_H__
   #include <Windows.h>
#endif

#ifdef LOGGER_DLL_EXPORT
      #define LOGGER_API __declspec(dllexport)
#else
   #ifdef LOGGER_DLL_IMPORT
      #define LOGGER_API __declspec(dllimport)

      #ifdef _DEBUG
         #ifdef UNICODE
            #pragma comment(lib, "LoggerUD.lib")
            #pragma message("   Adding LoggerUD.lib - Logger Unicode Debug library\n")
         #else
            #pragma comment(lib, "LoggerD.lib")
            #pragma message("   Adding LoggerD.lib - Logger Debug library\n")
         #endif
      #else
         #ifdef UNICODE
            #pragma comment(lib, "LoggerU.lib")
            #pragma message("   Adding LoggerU.lib - Logger Unicode Release library\n")
         #else
            #pragma comment(lib, "Logger.lib")
            #pragma message("   Adding Logger.lib - Logger Release library\n")
         #endif
      #endif
   #else
      #define LOGGER_API
   #endif
#endif

#define LOG_PUT(pLogger, funcName, logLevel) if ((pLogger) && (CLogger::logLevel <= (pLogger)->GetLogLevel())) (pLogger)->funcName(CLogger::logLevel

class LOGGER_API CLogger
{
public:
   enum eInfoExt {
      info_NotDatail = 0,
      info_WinInet   = 1,
      info_WinHTTP   = 2
   };

   enum eLogLevel {
      LL_FATAL   = 0x01,
      LL_ERROR   = 0x02,
      LL_WARNING = 0x04,
      LL_INFO    = 0x08,
      LL_DEBUG   = 0x10
   };

protected:
   CString m_strFile, m_strUserHint;
   HANDLE m_hFile;
   CRITICAL_SECTION m_cs;
   eLogLevel m_LL;
   ISequentialStream *m_pStream;
   BOOL m_bShowDate;
   BOOL m_bShowTime;
   BOOL m_bShowLogLevel;
   BOOL m_bShowThreadId;
   BOOL m_bUnicodeWrite; // ������ � ����/����� � UNICODE
   BOOL m_bShowTimeFromFirstMessage; // ���������� ����� ��������� �� ������ ������� ��������� � ���
   FILETIME m_ftFirstMessage;

private:
   static CString GUIDs(IUnknown *pInterface, const GUID *pguid, BOOL bFromRegistry, LPCTSTR szSeparator); // ��. � public'� GUIDs() � QueryInterfaces()
   static CString ErrorCode(DWORD dwErrCode, eInfoExt extInfo, BOOL bDescription); // ��������� ��� �������� ������ ��� ��������� ������������� ������
   static CString HResult(HRESULT hRes, BOOL bDescription); // ��������� ��� �������� HRESULT'a ��� ��� ��������� �������������

public:
	CLogger(
      eLogLevel logLevel,
      LPCTSTR szFileName,         // maybe NULL
      LPCTSTR szUserHint,         // maybe NULL (max - 16 char)
      ISequentialStream *pStream, // maybe NULL
      BOOL bShowDate,
      BOOL bShowTime,
      BOOL bShowLogLevel,
      BOOL bShowThreadId,
      BOOL bUnicodeWrite,
      BOOL bShowTimeFromFirstMessage
   );
	CLogger(
      eLogLevel logLevel,
      LPCTSTR szFileName,         // maybe NULL
      LPCTSTR szUserHint,         // maybe NULL (max - 16 char)
      ISequentialStream *pStream, // maybe NULL
      BOOL bShowDate,
      BOOL bShowTime,
      BOOL bShowLogLevel,
      BOOL bShowThreadId
   );
	~CLogger();

   void ShowDate                (BOOL bShow);
   void ShowTime                (BOOL bShow);
   void ShowTimeFromFirstMessage(BOOL bShow); // ���������� ����� ��������� �� ������ ������� ��������� � ���
   void ShowLogLevel            (BOOL bShow);
   void ShowThreadId            (BOOL bShow);

   eLogLevel GetLogLevel() const;
   BOOL      SetLogLevel(eLogLevel logLevel);

   CString GetFileName() const;
   BOOL    SetFileName(IN LPCTSTR szFileName); // ��� ����������� ������ ������ � ���� ���� �������� NULL

   CString GetUserHint() const;
   BOOL    SetUserHint(IN LPCTSTR szUserHint); // ��� ����������� ������ UserHint ���� �������� NULL

   BOOL GetStream(OUT ISequentialStream **ppStream);
   BOOL SetStream(IN  ISequentialStream  * pStream); // ��� ����������� ������ ������ � ����� ���� �������� NULL

   void SetUnicodeWrite(IN BOOL bUnicodeWrite);
   BOOL GetUnicodeWrite() const;

   CString Put               (IN eLogLevel logLevel, IN LPCTSTR szStr, ...);
   CString PutV              (IN eLogLevel logLevel, IN LPCTSTR szStr, IN va_list argList);
   CString PutWindowMessage  (IN eLogLevel logLevel, IN LPCTSTR szStr, IN UINT msg);
   CString PutGUIDs          (IN eLogLevel logLevel, IN LPCTSTR szStr, IN REFGUID guid        , BOOL bFromRegistry, IN LPCTSTR szSeparator = TEXT(", "), IN LPCTSTR szLeft = TEXT("["), IN LPCTSTR szRight = TEXT("]"));
   CString PutQueryInterfaces(IN eLogLevel logLevel, IN LPCTSTR szStr, IN IUnknown *pInterface, BOOL bFromRegistry, IN LPCTSTR szSeparator = TEXT(", "), IN LPCTSTR szLeft = TEXT("["), IN LPCTSTR szRight = TEXT("]"));
   CString PutInterfaceID    (IN eLogLevel logLevel, IN LPCTSTR szStr, IN REFIID riid);
   CString PutErrorCode      (IN eLogLevel logLevel, IN LPCTSTR szStr, IN DWORD dwErrCode = ::GetLastError(), IN eInfoExt extInfo = info_NotDatail);
   CString PutHResult        (IN eLogLevel logLevel, IN LPCTSTR szStr, IN HRESULT hRes);

   ////////////////////////////////////////////////////////
   // ����������� �-��� �������������� ����-���� � ��� ��������� �������������
   ////////////////////////////////////////////////////////

   static CString WindowMessage(UINT msg);         // Window Message to string
   static CString WindowStyle  (HWND hWnd);        // Window Styles to string
   static CString WindowStyle  (DWORD dwStyle, LPCTSTR szClassName = NULL); // Window Styles to string
   static CString WindowStyleEx(HWND hWnd);        // Extended Window Styles to string
   static CString WindowStyleEx(DWORD dwStyleEx);  // Extended Window Styles to string
   static CString SetWindowPos (UINT flags);       // SetWindowPos Flags to string
   static CString DialogCode   (int iCode);        // Dialog Code to string (see WM_GETDLGCODE)
   static CString ButtonState  (int iState);       // State of the button to string (see BM_GETSTATE)

   static CString GUIDs          (REFGUID guid        , BOOL bFromRegistry, LPCTSTR szSeparator = TEXT(", ")) { return CLogger::GUIDs(NULL      , &guid, bFromRegistry, szSeparator); } // ��������� ��������� ������������� CLSID� �/��� ����������, ������ ���������� GUID�.
   static CString QueryInterfaces(IUnknown *pInterface, BOOL bFromRegistry, LPCTSTR szSeparator = TEXT(", ")) { return CLogger::GUIDs(pInterface,  NULL, bFromRegistry, szSeparator); } // ��������� ��������� ������������� �����������, ������� ����� �������� �� ���������� pInterface (� ������� pInterface->QueryInterface(..))
   static CString ProgID     (REFCLSID clsid); // ��������� ����������� ������������� CLSID� � ���� LibraryName.CoClassName.Version, ��������� � �������. ��� ������ ProgIDFromCLSID.
   static CString InterfaceID(REFIID     iid); // ��������� ��������� ������������� ����������, ��������� � �������.

   static CString ErrorCode            (DWORD dwErrCode = ::GetLastError(), eInfoExt extInfo = info_NotDatail) { return CLogger::ErrorCode(dwErrCode, extInfo, false); } // ��������� ��������� ������������� ������
   static CString ErrorCode_Description(DWORD dwErrCode = ::GetLastError(), eInfoExt extInfo = info_NotDatail) { return CLogger::ErrorCode(dwErrCode, extInfo, true ); } // ��������� �������� ������
   static HRESULT ErrorCode2HResult    (DWORD dwErrCode = ::GetLastError()) {return HRESULT_FROM_WIN32(dwErrCode);}

   static CString HResult            (HRESULT hRes) { return CLogger::HResult(hRes, false); } // ��������� ��������� ������������� HRESULT'a
   static CString HResult_Description(HRESULT hRes) { return CLogger::HResult(hRes, true ); } // ��������� �������� HRESULT'a

   static CString QueryInfoFlag           (IN DWORD dwOption);              // convert WinInetApi HTTP Query flag to string, for detail see IWinInetHttpInfo::QueryInfo
   static CString QueryOption             (IN DWORD dwOption);              // convert QUERYOPTION to string, for detail see IWinInetInfo::QueryOption or IInternetProtocolInfo::QueryInfo Method
   static CString ParseAction             (IN DWORD dwParseAction);         // convert PARSEACTION to string, for detail see IInternetProtocolInfo::ParseUrl Method
   static CString InternetCombineUrlFlags (IN DWORD dwCombineFlags);        // convert ICU_xxx flag to string, for detail see InternetCanonicalizeUrl or InternetCombineUrl or InternetCrackUrl or InternetCreateUrl or IInternetProtocolInfo::CombineUrl Method
   static CString SystemTime(const SYSTEMTIME &st, BOOL bDate, BOOL bTime); // convert to string as YYYY.MM.DD HH:MM:SS,MS
   static CString BindFlag(IN DWORD dwBINDF);                               // convert BINDF flag to string
   static CString BindStatusCallbackFlag(IN WORD grfBSCF);                  // convert BSCF flag to string
   static CString BindString(IN ULONG ulStringType);                        // convert BINDSTRING to string
   static CString BindStatus(IN ULONG ulStatusCode);                        // convert BINDSTATUS to string

   static CString StringArray(IN LPCOLESTR const *const ppwzStr, IN ULONG uSize, LPCTSTR szLeft = TEXT("["), LPCTSTR szRight = TEXT("]"), LPCTSTR szSeparator = TEXT(", ")); // convert string array to string as   [string1], [string2], [string3], ... [stringN]

   static CString InternetStatusCallback(DWORD dwInternetStatus, eInfoExt info); // convert Internet status callback flag to string
   static CString WinHTTPCallbackStatusRequestError            (DWORD dwValue);  //
   static CString WinHTTPCallbackStatusRequestError_Description(DWORD dwValue);  //

   static CString PrinterStatus    (DWORD dwStatus);
   static CString PrinterAttributes(DWORD dwAttributes);
   static CString PrinterJobStatus (DWORD dwJobStatus);
};

// Keycode Constants
#ifdef _WINUSER_
   #define vbKeyLButton        VK_LBUTTON
   #define vbKeyRButton        VK_RBUTTON
   #define vbKeyCancel         VK_CANCEL
   #define vbKeyMButton        VK_MBUTTON

   #define vbKeyBack           VK_BACK
   #define vbKeyTab            VK_TAB

   #define vbKeyClear          VK_CLEAR
   #define vbKeyReturn         VK_RETURN

   #define vbKeyShift          VK_SHIFT
   #define vbKeyControl        VK_CONTROL
   #define vbKeyMenu           VK_MENU
   #define vbKeyPause          VK_PAUSE
   #define vbKeyCapital        VK_CAPITAL

   #define vbKeyEscape         VK_ESCAPE

   #define vbKeySpace          VK_SPACE
   #define vbKeyPageUp         VK_PRIOR
   #define vbKeyPageDown       VK_NEXT
   #define vbKeyEnd            VK_END
   #define vbKeyHome           VK_HOME
   #define vbKeyLeft           VK_LEFT
   #define vbKeyUp             VK_UP
   #define vbKeyRight          VK_RIGHT
   #define vbKeyDown           VK_DOWN
   #define vbKeySelect         VK_SELECT
   #define vbKeyPrint          VK_PRINT
   #define vbKeyExecute        VK_EXECUTE
   #define vbKeySnapshot       VK_SNAPSHOT
   #define vbKeyInsert         VK_INSERT
   #define vbKeyDelete         VK_DELETE
   #define vbKeyHelp           VK_HELP

   #define vbKeyNumpad0        VK_NUMPAD0
   #define vbKeyNumpad1        VK_NUMPAD1
   #define vbKeyNumpad2        VK_NUMPAD2
   #define vbKeyNumpad3        VK_NUMPAD3
   #define vbKeyNumpad4        VK_NUMPAD4
   #define vbKeyNumpad5        VK_NUMPAD5
   #define vbKeyNumpad6        VK_NUMPAD6
   #define vbKeyNumpad7        VK_NUMPAD7
   #define vbKeyNumpad8        VK_NUMPAD8
   #define vbKeyNumpad9        VK_NUMPAD9
   #define vbKeyMultiply       VK_MULTIPLY
   #define vbKeyAdd            VK_ADD
   #define vbKeySeparator      VK_SEPARATOR
   #define vbKeySubtract       VK_SUBTRACT
   #define vbKeyDecimal        VK_DECIMAL
   #define vbKeyDivide         VK_DIVIDE

   #define vbKeyF1             VK_F1
   #define vbKeyF2             VK_F2
   #define vbKeyF3             VK_F3
   #define vbKeyF4             VK_F4
   #define vbKeyF5             VK_F5
   #define vbKeyF6             VK_F6
   #define vbKeyF7             VK_F7
   #define vbKeyF8             VK_F8
   #define vbKeyF9             VK_F9
   #define vbKeyF10            VK_F10
   #define vbKeyF11            VK_F11
   #define vbKeyF12            VK_F12
   #define vbKeyF13            VK_F13
   #define vbKeyF14            VK_F14
   #define vbKeyF15            VK_F15
   #define vbKeyF16            VK_F16

   #define vbKeyNumlock        VK_NUMLOCK
   #define vbKeyScroll         VK_SCROLL
#else

   #define vbKeyLButton        0x1  // Left mouse button
   #define vbKeyRButton        0x2  // Right mouse button
   #define vbKeyCancel         0x3  // CANCEL key
   #define vbKeyMButton        0x4  // Middle mouse button

   #define vbKeyBack           0x8  // BACKSPACE key
   #define vbKeyTab            0x9  // TAB key

   #define vbKeyClear          0xC  // CLEAR key
   #define vbKeyReturn         0xD  // ENTER key

   #define vbKeyShift          0x10 // SHIFT key
   #define vbKeyControl        0x11 // CTRL key
   #define vbKeyMenu           0x12 // MENU key
   #define vbKeyPause          0x13 // PAUSE key
   #define vbKeyCapital        0x14 // CAPS LOCK key

   #define vbKeyEscape         0x1B // ESC key

   #define vbKeySpace          0x20 // SPACEBAR key
   #define vbKeyPageUp         0x21 // PAGE UP key
   #define vbKeyPageDown       0x22 // PAGE DOWN key
   #define vbKeyEnd            0x23 // END key
   #define vbKeyHome           0x24 // HOME key
   #define vbKeyLeft           0x25 // LEFT ARROW key
   #define vbKeyUp             0x26 // UP ARROW key
   #define vbKeyRight          0x27 // RIGHT ARROW key
   #define vbKeyDown           0x28 // DOWN ARROW key
   #define vbKeySelect         0x29 // SELECT key
   #define vbKeyPrint          0x2A // PRINT SCREEN key
   #define vbKeyExecute        0x2B // EXECUTE key
   #define vbKeySnapshot       0x2C // SNAPSHOT key
   #define vbKeyInsert         0x2D // INSERT key
   #define vbKeyDelete         0x2E // DELETE key
   #define vbKeyHelp           0x2F // HELP key

   #define vbKeyNumpad0        0x60 // 0 key
   #define vbKeyNumpad1        0x61 // 1 key
   #define vbKeyNumpad2        0x62 // 2 key
   #define vbKeyNumpad3        0x63 // 3 key
   #define vbKeyNumpad4        0x64 // 4 key
   #define vbKeyNumpad5        0x65 // 5 key
   #define vbKeyNumpad6        0x66 // 6 key
   #define vbKeyNumpad7        0x67 // 7 key
   #define vbKeyNumpad8        0x68 // 8 key
   #define vbKeyNumpad9        0x69 // 9 key
   #define vbKeyMultiply       0x6A // MULTIPLICATION SIGN (*) key
   #define vbKeyAdd            0x6B // PLUS SIGN (+) key
   #define vbKeySeparator      0x6C // ENTER key
   #define vbKeySubtract       0x6D // MINUS SIGN (�) key
   #define vbKeyDecimal        0x6E // DECIMAL POINT (.) key
   #define vbKeyDivide         0x6F // DIVISION SIGN (/) key

   #define vbKeyF1             0x70 // F1 key
   #define vbKeyF2             0x71 // F2 key
   #define vbKeyF3             0x72 // F3 key
   #define vbKeyF4             0x73 // F4 key
   #define vbKeyF5             0x74 // F5 key
   #define vbKeyF6             0x75 // F6 key
   #define vbKeyF7             0x76 // F7 key
   #define vbKeyF8             0x77 // F8 key
   #define vbKeyF9             0x78 // F9 key
   #define vbKeyF10            0x79 // F10 key
   #define vbKeyF11            0x7A // F11 key
   #define vbKeyF12            0x7B // F12 key
   #define vbKeyF13            0x7C // F13 key
   #define vbKeyF14            0x7D // F14 key
   #define vbKeyF15            0x7E // F15 key
   #define vbKeyF16            0x7F // F16 key

   #define vbKeyNumlock        0x90 // NUM LOCK key
   #define vbKeyScroll         0x91 // SCROLL
#endif

#ifdef VK_OEM_1
   #define vbKeySemicolon      VK_OEM_1
   #define vbKeyPlus           VK_OEM_PLUS
   #define vbKeyComma          VK_OEM_COMMA
   #define vbKeyMinus          VK_OEM_MINUS
   #define vbKeyDot            VK_OEM_PERIOD
   #define vbKeyBackslash      VK_OEM_2
   #define vbKeyTwiddle        VK_OEM_3

   #define vbKeySquareBracketL VK_OEM_4
   #define vbKeySlash          VK_OEM_5
   #define vbKeySquareBracketR VK_OEM_6
   #define vbKeyQuotationMarks VK_OEM_7
#else

   #define vbKeySemicolon      0xBA // SEMICOLON ';:'
   #define vbKeyPlus           0xBB // PLUS      '+'
   #define vbKeyComma          0xBC // COMMA     ','
   #define vbKeyMinus          0xBD // MINUS     '-'
   #define vbKeyDot            0xBE // DOT       '.'
   #define vbKeyBackslash      0xBF // BACKLASH  '/?'
   #define vbKeyTwiddle        0xC0 // TWIDDLE   '`~'

   #define vbKeySquareBracketL 0xDB // SQUARE BRACKET  '[{'
   #define vbKeySlash          0xDC // SLASH           '\|'
   #define vbKeySquareBracketR 0xDD // SQUARE BRACKET  ']}'
   #define vbKeyQuotationMarks 0xDE // QUOTATION MARKS ''"'
#endif

#define vbKeyA 65 // A key
#define vbKeyB 66 // B key
#define vbKeyC 67 // C key
#define vbKeyD 68 // D key
#define vbKeyE 69 // E key
#define vbKeyF 70 // F key
#define vbKeyG 71 // G key
#define vbKeyH 72 // H key
#define vbKeyI 73 // I key
#define vbKeyJ 74 // J key
#define vbKeyK 75 // K key
#define vbKeyL 76 // L key
#define vbKeyM 77 // M key
#define vbKeyN 78 // N key
#define vbKeyO 79 // O key
#define vbKeyP 80 // P key
#define vbKeyQ 81 // Q key
#define vbKeyR 82 // R key
#define vbKeyS 83 // S key
#define vbKeyT 84 // T key
#define vbKeyU 85 // U key
#define vbKeyV 86 // V key
#define vbKeyW 87 // W key
#define vbKeyX 88 // X key
#define vbKeyY 89 // Y key
#define vbKeyZ 90 // Z key

#define vbKey0 48 // 0 key
#define vbKey1 49 // 1 key
#define vbKey2 50 // 2 key
#define vbKey3 51 // 3 key
#define vbKey4 52 // 4 key
#define vbKey5 53 // 5 key
#define vbKey6 54 // 6 key
#define vbKey7 55 // 7 key
#define vbKey8 56 // 8 key
#define vbKey9 57 // 9 key

#endif // __FILE__LOGGER__
