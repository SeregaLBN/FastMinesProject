////////////////////////////////////////////////////////////////////////////////
// File name: Logger.cpp
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 21 09 2004
//
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"

//#define MICROSOFT_SDK_FEBRUARY_2003                     // раскоментировать если стоит Microsoft SDK за February 2003
//#define HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5 // Headers and Libraries for Internet Explorer 5.5

#include "Logger.h"
#ifdef TERMINAL
   #include "LoggerNet.h"
#endif // TERMINAL

namespace nsWinInet_API {
   #include <WinInet.h>
}

#ifdef MICROSOFT_SDK_FEBRUARY_2003
   namespace nsWinHTTP_API {
      #include <WinHTTP.h>
   }
#endif // MICROSOFT_SDK_FEBRUARY_2003

//////////////////////////////////
#ifndef HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5
   #define _LPTARGETFRAME2_DEFINED
#endif
#include <WinSock.h>
#include <UrlMon.h>
#include <oleidl.h>
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   #include <htiface.h>
   #include <TCError.h>
   #include <azRoles.h>
#endif // MICROSOFT_SDK_FEBRUARY_2003
#include <MimeInfo.h>
#include <UrlHist.h>
#include <MSHTML.h>
#include <MSHTMHst.h>
#include <ShlGuid.h>
#include <OAIDL.h>
#include <ObjIDL.h>
#ifdef HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5
   #include <Htiframe.h> // Headers and Libraries for Internet Explorer 5.5
   #include <vervec.h>   // Headers and Libraries for Internet Explorer 5.5
#endif // HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5

#pragma warning(disable:4065) // switch statement contains 'default' but no 'case' labels

////////////////////////////////////////////////////////////////////////////////////////
//                               IMPLEMENTATION CLogger
////////////////////////////////////////////////////////////////////////////////////////
#ifdef __AFX_H__
void CLogger::WriteToTrace(bool bWrite) {
   m_bWriteToTrace = bWrite;
}
#endif //__AFX_H__

BOOL CLogger::WriteToFile(bool bWrite) {
   BOOL bResult = TRUE;
   m_bWriteToFile = bWrite;

   if (bWrite && (m_strFile.GetLength() == 0)) {
      bResult = m_bWriteToFile = false;
   }

   return bResult;
}

__forceinline void CLogger::SetNewFileName(LPCTSTR szFileName) {
   if (szFileName) {
      if (szFileName[1] != TEXT(':')) {
         TCHAR szPath[MAX_PATH] = {0};
         GetModuleFileName(NULL, szPath, MAX_PATH);
         {
            for (int i=_tcslen(szPath); i; i--)
               if (szPath[i] == TEXT('\\')) {
                  szPath[i+1] = TEXT('\0');
                  break;
               }
         }
         _tcscat(szPath, szFileName);
         m_strFile = szPath;
      } else {
         m_strFile = szFileName;
      }
   } else {
      m_bWriteToFile = false;
   }
}

CLogger::CLogger(
   eLogLevel logLevel,
   LPCTSTR szFileName,
   bool bShowDate,
   bool bShowTime,
   bool bShowLogLevel,
   bool bShowThreadId
#ifdef TERMINAL
   ,
   LPCTSTR szRequestURL,
   LPCTSTR szLoggerName,
   LPCTSTR szKey,
   LPCTSTR szServerLogin,
   LPCTSTR szServerPass
#endif // TERMINAL
) :
   m_LL           (logLevel),
   m_bShowDate    (bShowDate),
   m_bShowTime    (bShowTime),
   m_bShowLogLevel(bShowLogLevel),
   m_bShowThreadId(bShowThreadId),
#ifdef __AFX_H__
   m_bWriteToTrace(true),
#endif //__AFX_H__
   m_bWriteToFile (true)
{
   InitializeCriticalSection(&m_cs);

   SetNewFileName(szFileName);

#ifdef TERMINAL
   m_strRequestURL  = szRequestURL;
   m_strLoggerName  = szLoggerName;
   m_strKey         = szKey;
   m_strServerLogin = szServerLogin;
   m_strServerPass  = szServerPass;
#endif // TERMINAL

   Put(LL_INFO, TEXT("//////////////=========  Open  Log [%s] =========//////////////"), (LPCTSTR)m_strFile);
}

CLogger::~CLogger() {
   Put(LL_INFO, TEXT("//////////////=========  Close Log [%s] =========//////////////"), (LPCTSTR)m_strFile);
   DeleteCriticalSection(&m_cs);
}

void CLogger::PutToFile(LPCTSTR szStr) {
   EnterCriticalSection(&m_cs);

   HANDLE hFile = INVALID_HANDLE_VALUE;
   __try {
      hFile = CreateFile(
         (LPCTSTR)m_strFile,
         GENERIC_WRITE,
         FILE_SHARE_READ,
         NULL,
         OPEN_ALWAYS,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      if (hFile == INVALID_HANDLE_VALUE) __leave;

      if (SetFilePointer(hFile,0,NULL,FILE_END) == 0xFFFFFFFF) __leave;
      size_t size = _tcslen(szStr)*sizeof(TCHAR);
      DWORD dwNOBW;
      BOOL result = WriteFile(hFile, szStr, size, &dwNOBW, NULL);
      if (!result || (size != dwNOBW)) {
         __leave;
      }
   } __finally {
      if (hFile != INVALID_HANDLE_VALUE)
         CloseHandle(hFile);
   }

   LeaveCriticalSection(&m_cs);
}

CString CLogger::PutV(eLogLevel logLevel, LPCTSTR szStr, va_list argList) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   SYSTEMTIME tm;
   GetLocalTime(&tm);

   m_strBuf.Empty();
   m_strBuf.FormatV(szStr, argList);

   if (m_bShowThreadId) {
      DWORD dwThreadID = ::GetCurrentThreadId();
      CString strTrID; strTrID.Format(TEXT("%08X   "), dwThreadID);
      m_strBuf = strTrID + m_strBuf;
   }

   if (m_bShowLogLevel) {
      CString strLogLevel;
      switch (logLevel) {
      case LL_ERROR  : strLogLevel = TEXT("   ERROR     "); break;
      case LL_WARNING: strLogLevel = TEXT("   WARNING   "); break;
      case LL_INFO   : strLogLevel = TEXT("   INFO      "); break;
      case LL_DEBUG  : strLogLevel = TEXT("   DEBUG     "); break;
      }
      m_strBuf = strLogLevel + m_strBuf;
   }

   if (m_bShowDate || m_bShowTime) {
      m_strBuf = TEXT('[') + SystemTime(tm, m_bShowDate, m_bShowTime) + TEXT("] ") + m_strBuf;
   }

   CString strToFile(m_strBuf + TEXT("\r\n"));

   if (m_bWriteToFile)
      PutToFile((LPCTSTR)strToFile);

#ifdef __AFX_H__
   if (m_bWriteToTrace) {
      static const int MAX = 511; // @TODO: разбиваю и вывожу по кускам данного размера, иначе - глючит ::AfxTrace()
      do {
         CString strLeft = strToFile.Left(MAX);
         ::AfxTrace(strLeft);
         strToFile = strToFile.Mid(min(strToFile.GetLength(), MAX));
      } while(!strToFile.IsEmpty());
   }
#endif // __AFX_H__

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}

#ifdef TERMINAL
void CLogger::PutRequest(LPCTSTR szLogLevel, LPCTSTR szLogMessage)
{
   EnterCriticalSection(&m_cs);

   if (lstrcmp(NET_LOG_LEVEL__ERROR, szLogLevel) == 0) {
      Put(LL_ERROR, szLogMessage);
   } else
   if (lstrcmp(NET_LOG_LEVEL__WARN, szLogLevel) == 0) {
      Put(LL_WARNING, szLogMessage);
   } else
   if (lstrcmp(NET_LOG_LEVEL__INFO, szLogLevel) == 0) {
      Put(LL_INFO, szLogMessage);
   } else
   if (lstrcmp(NET_LOG_LEVEL__DEBUG, szLogLevel) == 0) {
      Put(LL_DEBUG, szLogMessage);
   }

   try {
    //CStringArray  strMIMEs; strMIMEs.Add(TEXT("*/*"));
      CStringVector strMIMEs; strMIMEs += TEXT("*/*");
      CLoggerNet::Send(
         m_strRequestURL,
         CString(TEXT("Content-Type: application/x-www-form-urlencoded; text/html;charset=windows-1251")),
         strMIMEs,
         m_strKey + TEXT("&logger_name=") + m_strLoggerName + TEXT("&log_level=") + szLogLevel + TEXT("&log_message=") + m_strBuf,
         m_strServerLogin,
         m_strServerPass,
         this
      );
   } catch (...) {
      Put(LL_ERROR, TEXT("CLogger::PutRequest - Error in CLoggerNet"));
   }

   LeaveCriticalSection(&m_cs);
}
#endif //TERMINAL

CString CLogger::PutMsg(eLogLevel logLevel, LPCTSTR szStr, UINT msg) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   CString strRes; strRes.Format(TEXT("%s0x%04X (%s)"), szStr, msg, (LPCTSTR)CLogger::WindowMessage(msg));

   Put(logLevel, (LPCTSTR)strRes);

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}

CString CLogger::PutInterface(eLogLevel logLevel, LPCTSTR szStr, REFIID riid, CString strSeparator) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   CString strRes;
   strRes.Format(
      TEXT("%s{%08x-%04x-%04x-%02x%02x-%02x%02x%02x%02x%02x%02x}\t\t%s"),
      szStr,
      riid.Data1, riid.Data2, riid.Data3,
      riid.Data4[0], riid.Data4[1], riid.Data4[2], riid.Data4[3], riid.Data4[4], riid.Data4[5], riid.Data4[6], riid.Data4[7],
      (LPCTSTR)CLogger::Interfaces(riid, strSeparator)
   );

   Put(logLevel, (LPCTSTR)strRes);

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}

CString CLogger::FindInterface(eLogLevel logLevel, LPCTSTR szStr, IUnknown *pInterface, CString strSeparator) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   CString strRes(szStr + CLogger::QueryInterfaces(pInterface, strSeparator));
   Put(logLevel, (LPCTSTR)strRes);

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}

CString CLogger::GetLastError(eLogLevel logLevel, LPCTSTR szStr, DWORD dwErrCode, eInfoExt extInfo) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   CString strRes;
   strRes.Format(
      TEXT("%sError code -> %i=%s (%s)"),
      szStr,
      dwErrCode,
      (LPCTSTR)CLogger::ErrorCode            (dwErrCode, extInfo),
      (LPCTSTR)CLogger::ErrorCode_Description(dwErrCode, extInfo)
   );

   Put(logLevel, (LPCTSTR)strRes);

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}

CString CLogger::PutHResult(eLogLevel logLevel, LPCTSTR szStr, HRESULT hRes) {
   if (logLevel > m_LL) return CString();

   EnterCriticalSection(&m_cs);

   CString strRes;
   strRes.Format(
      TEXT("%sHRESULT -> 0x%08XL=%s (%s)"),
      szStr,
      hRes,
      (LPCTSTR)CLogger::HResult            (hRes),
      (LPCTSTR)CLogger::HResult_Description(hRes)
   );

   Put(logLevel, (LPCTSTR)strRes);

   LeaveCriticalSection(&m_cs);

   return m_strBuf;
}


////////////////////////////////////////////////////////////////////////////////////////
//                                        static member CLogger
////////////////////////////////////////////////////////////////////////////////////////

#define BEGIN_CASE_ERRCODE                    switch(dwErrCode) {
#define CASE_ERRCODE(Code, Name, Description) case Code: strRes = (bDescription ? TEXT(Description) : TEXT(Name)); break;
#define END_CASE_ERRCODE                      default: if (extInfo == info_NotDatail) strRes = TEXT("Unknown Error");  }

CString CLogger::ErrorCode(DWORD dwErrCode, eInfoExt extInfo, bool bDescription) {
   CString strRes;
   if (bDescription) {
      LPTSTR lpMsgBuf = NULL;
      DWORD dwRes = FormatMessage(
         FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
         NULL,
         dwErrCode,
         MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
         (LPTSTR)&lpMsgBuf,
         0,
         NULL
      );
      if (dwRes != 0) {
         strRes = lpMsgBuf;
         strRes.Replace(TEXT("\r\n"), TEXT(" "));
         //strRes.Replace(TEXT('\r'), TEXT(' '));
         //strRes.Replace(TEXT('\n'), TEXT(' '));
         strRes.TrimRight();
      } else {
         DWORD dwErrCode2 = ::GetLastError(); // must be ERROR_MR_MID_NOT_FOUND ???
      }
      LocalFree(lpMsgBuf);
      if (!strRes.IsEmpty())
         return strRes;
   }

   BEGIN_CASE_ERRCODE
   CASE_ERRCODE(ERROR_SUCCESS                                           , "ERROR_SUCCESS"                                           , "The operation completed successfully.")
   CASE_ERRCODE(ERROR_INVALID_FUNCTION                                  , "ERROR_INVALID_FUNCTION"                                  , "Incorrect function.")
   CASE_ERRCODE(ERROR_FILE_NOT_FOUND                                    , "ERROR_FILE_NOT_FOUND"                                    , "The system cannot find the file specified.")
   CASE_ERRCODE(ERROR_PATH_NOT_FOUND                                    , "ERROR_PATH_NOT_FOUND"                                    , "The system cannot find the path specified.")
   CASE_ERRCODE(ERROR_TOO_MANY_OPEN_FILES                               , "ERROR_TOO_MANY_OPEN_FILES"                               , "The system cannot open the file.")
   CASE_ERRCODE(ERROR_ACCESS_DENIED                                     , "ERROR_ACCESS_DENIED"                                     , "Access is denied.")
   CASE_ERRCODE(ERROR_INVALID_HANDLE                                    , "ERROR_INVALID_HANDLE"                                    , "The handle is invalid. (The handle that was passed to the API has been either invalidated or closed.)")
   CASE_ERRCODE(ERROR_ARENA_TRASHED                                     , "ERROR_ARENA_TRASHED"                                     , "The storage control blocks were destroyed.")
   CASE_ERRCODE(ERROR_NOT_ENOUGH_MEMORY                                 , "ERROR_NOT_ENOUGH_MEMORY"                                 , "Windows error code. Not enough storage is available to process this command.")
   CASE_ERRCODE(ERROR_INVALID_BLOCK                                     , "ERROR_INVALID_BLOCK"                                     , "The storage control block address is invalid.")
   CASE_ERRCODE(ERROR_BAD_ENVIRONMENT                                   , "ERROR_BAD_ENVIRONMENT"                                   , "The environment is incorrect.")
   CASE_ERRCODE(ERROR_BAD_FORMAT                                        , "ERROR_BAD_FORMAT"                                        , "An attempt was made to load a program with an incorrect format.")
   CASE_ERRCODE(ERROR_INVALID_ACCESS                                    , "ERROR_INVALID_ACCESS"                                    , "The access code is invalid.")
   CASE_ERRCODE(ERROR_INVALID_DATA                                      , "ERROR_INVALID_DATA"                                      , "The data is invalid.")
   CASE_ERRCODE(ERROR_OUTOFMEMORY                                       , "ERROR_OUTOFMEMORY"                                       , "Not enough storage is available to complete this operation.")
   CASE_ERRCODE(ERROR_INVALID_DRIVE                                     , "ERROR_INVALID_DRIVE"                                     , "The system cannot find the drive specified.")
   CASE_ERRCODE(ERROR_CURRENT_DIRECTORY                                 , "ERROR_CURRENT_DIRECTORY"                                 , "The directory cannot be removed.")
   CASE_ERRCODE(ERROR_NOT_SAME_DEVICE                                   , "ERROR_NOT_SAME_DEVICE"                                   , "The system cannot move the file to a different disk drive.")
   CASE_ERRCODE(ERROR_NO_MORE_FILES                                     , "ERROR_NO_MORE_FILES"                                     , "There are no more files. (No more files have been found.)")
   CASE_ERRCODE(ERROR_WRITE_PROTECT                                     , "ERROR_WRITE_PROTECT"                                     , "The media is write protected.")
   CASE_ERRCODE(ERROR_BAD_UNIT                                          , "ERROR_BAD_UNIT"                                          , "The system cannot find the device specified.")
   CASE_ERRCODE(ERROR_NOT_READY                                         , "ERROR_NOT_READY"                                         , "The device is not ready.")
   CASE_ERRCODE(ERROR_BAD_COMMAND                                       , "ERROR_BAD_COMMAND"                                       , "The device does not recognize the command.")
   CASE_ERRCODE(ERROR_CRC                                               , "ERROR_CRC"                                               , "Data error (cyclic redundancy check).")
   CASE_ERRCODE(ERROR_BAD_LENGTH                                        , "ERROR_BAD_LENGTH"                                        , "The program issued a command but the command length is incorrect.")
   CASE_ERRCODE(ERROR_SEEK                                              , "ERROR_SEEK"                                              , "The drive cannot locate a specific area or track on the disk.")
   CASE_ERRCODE(ERROR_NOT_DOS_DISK                                      , "ERROR_NOT_DOS_DISK"                                      , "The specified disk or diskette cannot be accessed.")
   CASE_ERRCODE(ERROR_SECTOR_NOT_FOUND                                  , "ERROR_SECTOR_NOT_FOUND"                                  , "The drive cannot find the sector requested.")
   CASE_ERRCODE(ERROR_OUT_OF_PAPER                                      , "ERROR_OUT_OF_PAPER"                                      , "The printer is out of paper.")
   CASE_ERRCODE(ERROR_WRITE_FAULT                                       , "ERROR_WRITE_FAULT"                                       , "The system cannot write to the specified device.")
   CASE_ERRCODE(ERROR_READ_FAULT                                        , "ERROR_READ_FAULT"                                        , "The system cannot read from the specified device.")
   CASE_ERRCODE(ERROR_GEN_FAILURE                                       , "ERROR_GEN_FAILURE"                                       , "A device attached to the system is not functioning.")
   CASE_ERRCODE(ERROR_SHARING_VIOLATION                                 , "ERROR_SHARING_VIOLATION"                                 , "The process cannot access the file because it is being used by another process.")
   CASE_ERRCODE(ERROR_LOCK_VIOLATION                                    , "ERROR_LOCK_VIOLATION"                                    , "The process cannot access the file because another process has locked a portion of the file.")
   CASE_ERRCODE(ERROR_WRONG_DISK                                        , "ERROR_WRONG_DISK"                                        , "The wrong diskette is in the drive. Insert %2 (Volume Serial Number: %3) into drive %1.")
   CASE_ERRCODE(ERROR_SHARING_BUFFER_EXCEEDED                           , "ERROR_SHARING_BUFFER_EXCEEDED"                           , "Too many files opened for sharing.")
   CASE_ERRCODE(ERROR_HANDLE_EOF                                        , "ERROR_HANDLE_EOF"                                        , "Reached the end of the file.")
   CASE_ERRCODE(ERROR_HANDLE_DISK_FULL                                  , "ERROR_HANDLE_DISK_FULL"                                  , "The disk is full.")
   CASE_ERRCODE(ERROR_NOT_SUPPORTED                                     , "ERROR_NOT_SUPPORTED"                                     , "The request is not supported.")
   CASE_ERRCODE(ERROR_REM_NOT_LIST                                      , "ERROR_REM_NOT_LIST"                                      , "Windows cannot find the network path. Verify that the network path is correct and the destination computer is not busy or turned off. If Windows still cannot find the network path, contact your network administrator.")
   CASE_ERRCODE(ERROR_DUP_NAME                                          , "ERROR_DUP_NAME"                                          , "You were not connected because a duplicate name exists on the network. Go to System in the Control Panel to change the computer name and try again.")
   CASE_ERRCODE(ERROR_BAD_NETPATH                                       , "ERROR_BAD_NETPATH"                                       , "The network path was not found.")
   CASE_ERRCODE(ERROR_NETWORK_BUSY                                      , "ERROR_NETWORK_BUSY"                                      , "The network is busy.")
   CASE_ERRCODE(ERROR_DEV_NOT_EXIST                                     , "ERROR_DEV_NOT_EXIST"                                     , "The specified network resource or device is no longer available.")
   CASE_ERRCODE(ERROR_TOO_MANY_CMDS                                     , "ERROR_TOO_MANY_CMDS"                                     , "The network BIOS command limit has been reached.")
   CASE_ERRCODE(ERROR_ADAP_HDW_ERR                                      , "ERROR_ADAP_HDW_ERR"                                      , "A network adapter hardware error occurred.")
   CASE_ERRCODE(ERROR_BAD_NET_RESP                                      , "ERROR_BAD_NET_RESP"                                      , "The specified server cannot perform the requested operation.")
   CASE_ERRCODE(ERROR_UNEXP_NET_ERR                                     , "ERROR_UNEXP_NET_ERR"                                     , "An unexpected network error occurred.")
   CASE_ERRCODE(ERROR_BAD_REM_ADAP                                      , "ERROR_BAD_REM_ADAP"                                      , "The remote adapter is not compatible.")
   CASE_ERRCODE(ERROR_PRINTQ_FULL                                       , "ERROR_PRINTQ_FULL"                                       , "The printer queue is full.")
   CASE_ERRCODE(ERROR_NO_SPOOL_SPACE                                    , "ERROR_NO_SPOOL_SPACE"                                    , "Space to store the file waiting to be printed is not available on the server.")
   CASE_ERRCODE(ERROR_PRINT_CANCELLED                                   , "ERROR_PRINT_CANCELLED"                                   , "Your file waiting to be printed was deleted.")
   CASE_ERRCODE(ERROR_NETNAME_DELETED                                   , "ERROR_NETNAME_DELETED"                                   , "The specified network name is no longer available.")
   CASE_ERRCODE(ERROR_NETWORK_ACCESS_DENIED                             , "ERROR_NETWORK_ACCESS_DENIED"                             , "Network access is denied.")
   CASE_ERRCODE(ERROR_BAD_DEV_TYPE                                      , "ERROR_BAD_DEV_TYPE"                                      , "The network resource type is not correct.")
   CASE_ERRCODE(ERROR_BAD_NET_NAME                                      , "ERROR_BAD_NET_NAME"                                      , "The network name cannot be found.")
   CASE_ERRCODE(ERROR_TOO_MANY_NAMES                                    , "ERROR_TOO_MANY_NAMES"                                    , "The name limit for the local computer network adapter card was exceeded.")
   CASE_ERRCODE(ERROR_TOO_MANY_SESS                                     , "ERROR_TOO_MANY_SESS"                                     , "The network BIOS session limit was exceeded.")
   CASE_ERRCODE(ERROR_SHARING_PAUSED                                    , "ERROR_SHARING_PAUSED"                                    , "The remote server has been paused or is in the process of being started.")
   CASE_ERRCODE(ERROR_REQ_NOT_ACCEP                                     , "ERROR_REQ_NOT_ACCEP"                                     , "No more connections can be made to this remote computer at this time because there are already as many connections as the computer can accept.")
   CASE_ERRCODE(ERROR_REDIR_PAUSED                                      , "ERROR_REDIR_PAUSED"                                      , "The specified printer or disk device has been paused.")
   CASE_ERRCODE(ERROR_FILE_EXISTS                                       , "ERROR_FILE_EXISTS"                                       , "The file exists.")
   CASE_ERRCODE(ERROR_CANNOT_MAKE                                       , "ERROR_CANNOT_MAKE"                                       , "The directory or file cannot be created.")
   CASE_ERRCODE(ERROR_FAIL_I24                                          , "ERROR_FAIL_I24"                                          , "Fail on INT 24.")
   CASE_ERRCODE(ERROR_OUT_OF_STRUCTURES                                 , "ERROR_OUT_OF_STRUCTURES"                                 , "Storage to process this request is not available.")
   CASE_ERRCODE(ERROR_ALREADY_ASSIGNED                                  , "ERROR_ALREADY_ASSIGNED"                                  , "The local device name is already in use.")
   CASE_ERRCODE(ERROR_INVALID_PASSWORD                                  , "ERROR_INVALID_PASSWORD"                                  , "The specified network password is not correct.")
   CASE_ERRCODE(ERROR_INVALID_PARAMETER                                 , "ERROR_INVALID_PARAMETER"                                 , "The parameter is incorrect.")
   CASE_ERRCODE(ERROR_NET_WRITE_FAULT                                   , "ERROR_NET_WRITE_FAULT"                                   , "A write fault occurred on the network.")
   CASE_ERRCODE(ERROR_NO_PROC_SLOTS                                     , "ERROR_NO_PROC_SLOTS"                                     , "The system cannot start another process at this time.")
   CASE_ERRCODE(ERROR_TOO_MANY_SEMAPHORES                               , "ERROR_TOO_MANY_SEMAPHORES"                               , "Cannot create another system semaphore.")
   CASE_ERRCODE(ERROR_EXCL_SEM_ALREADY_OWNED                            , "ERROR_EXCL_SEM_ALREADY_OWNED"                            , "The exclusive semaphore is owned by another process.")
   CASE_ERRCODE(ERROR_SEM_IS_SET                                        , "ERROR_SEM_IS_SET"                                        , "The semaphore is set and cannot be closed.")
   CASE_ERRCODE(ERROR_TOO_MANY_SEM_REQUESTS                             , "ERROR_TOO_MANY_SEM_REQUESTS"                             , "The semaphore cannot be set again.")
   CASE_ERRCODE(ERROR_INVALID_AT_INTERRUPT_TIME                         , "ERROR_INVALID_AT_INTERRUPT_TIME"                         , "Cannot request exclusive semaphores at interrupt time.")
   CASE_ERRCODE(ERROR_SEM_OWNER_DIED                                    , "ERROR_SEM_OWNER_DIED"                                    , "The previous ownership of this semaphore has ended.")
   CASE_ERRCODE(ERROR_SEM_USER_LIMIT                                    , "ERROR_SEM_USER_LIMIT"                                    , "Insert the diskette for drive %1.")
   CASE_ERRCODE(ERROR_DISK_CHANGE                                       , "ERROR_DISK_CHANGE"                                       , "The program stopped because an alternate diskette was not inserted.")
   CASE_ERRCODE(ERROR_DRIVE_LOCKED                                      , "ERROR_DRIVE_LOCKED"                                      , "The disk is in use or locked by another process.")
   CASE_ERRCODE(ERROR_BROKEN_PIPE                                       , "ERROR_BROKEN_PIPE"                                       , "The pipe has been ended.")
   CASE_ERRCODE(ERROR_OPEN_FAILED                                       , "ERROR_OPEN_FAILED"                                       , "The system cannot open the device or file specified.")
   CASE_ERRCODE(ERROR_BUFFER_OVERFLOW                                   , "ERROR_BUFFER_OVERFLOW"                                   , "The file name is too long.")
   CASE_ERRCODE(ERROR_DISK_FULL                                         , "ERROR_DISK_FULL"                                         , "There is not enough space on the disk.")
   CASE_ERRCODE(ERROR_NO_MORE_SEARCH_HANDLES                            , "ERROR_NO_MORE_SEARCH_HANDLES"                            , "No more internal file identifiers available.")
   CASE_ERRCODE(ERROR_INVALID_TARGET_HANDLE                             , "ERROR_INVALID_TARGET_HANDLE"                             , "The target internal file identifier is incorrect.")
   CASE_ERRCODE(ERROR_INVALID_CATEGORY                                  , "ERROR_INVALID_CATEGORY"                                  , "The IOCTL call made by the application program is not correct.")
   CASE_ERRCODE(ERROR_INVALID_VERIFY_SWITCH                             , "ERROR_INVALID_VERIFY_SWITCH"                             , "The verify-on-write switch parameter value is not correct.")
   CASE_ERRCODE(ERROR_BAD_DRIVER_LEVEL                                  , "ERROR_BAD_DRIVER_LEVEL"                                  , "The system does not support the command requested.")
   CASE_ERRCODE(ERROR_CALL_NOT_IMPLEMENTED                              , "ERROR_CALL_NOT_IMPLEMENTED"                              , "This function is not supported on this system.")
   CASE_ERRCODE(ERROR_SEM_TIMEOUT                                       , "ERROR_SEM_TIMEOUT"                                       , "The semaphore timeout period has expired.")
   CASE_ERRCODE(ERROR_INSUFFICIENT_BUFFER                               , "ERROR_INSUFFICIENT_BUFFER"                               , "The data area passed to a system call is too small. (The size of the buffer supplied to a function was insufficient to contain the returned data. The number of bytes required is usually returned in the buffer size parameter. See the specific function for details.)")
   CASE_ERRCODE(ERROR_INVALID_NAME                                      , "ERROR_INVALID_NAME"                                      , "The filename, directory name, or volume label syntax is incorrect.")
   CASE_ERRCODE(ERROR_INVALID_LEVEL                                     , "ERROR_INVALID_LEVEL"                                     , "The system call level is not correct.")
   CASE_ERRCODE(ERROR_NO_VOLUME_LABEL                                   , "ERROR_NO_VOLUME_LABEL"                                   , "The disk has no volume label.")
   CASE_ERRCODE(ERROR_MOD_NOT_FOUND                                     , "ERROR_MOD_NOT_FOUND"                                     , "The specified module could not be found.")
   CASE_ERRCODE(ERROR_PROC_NOT_FOUND                                    , "ERROR_PROC_NOT_FOUND"                                    , "The specified procedure could not be found.")
   CASE_ERRCODE(ERROR_WAIT_NO_CHILDREN                                  , "ERROR_WAIT_NO_CHILDREN"                                  , "There are no child processes to wait for.")
   CASE_ERRCODE(ERROR_CHILD_NOT_COMPLETE                                , "ERROR_CHILD_NOT_COMPLETE"                                , "The %1 application cannot be run in Win32 mode.")
   CASE_ERRCODE(ERROR_DIRECT_ACCESS_HANDLE                              , "ERROR_DIRECT_ACCESS_HANDLE"                              , "Attempt to use a file handle to an open disk partition for an operation other than raw disk I/O.")
   CASE_ERRCODE(ERROR_NEGATIVE_SEEK                                     , "ERROR_NEGATIVE_SEEK"                                     , "An attempt was made to move the file pointer before the beginning of the file.")
   CASE_ERRCODE(ERROR_SEEK_ON_DEVICE                                    , "ERROR_SEEK_ON_DEVICE"                                    , "The file pointer cannot be set on the specified device or file.")
   CASE_ERRCODE(ERROR_IS_JOIN_TARGET                                    , "ERROR_IS_JOIN_TARGET"                                    , "A JOIN or SUBST command cannot be used for a drive that contains previously joined drives.")
   CASE_ERRCODE(ERROR_IS_JOINED                                         , "ERROR_IS_JOINED"                                         , "An attempt was made to use a JOIN or SUBST command on a drive that has already been joined.")
   CASE_ERRCODE(ERROR_IS_SUBSTED                                        , "ERROR_IS_SUBSTED"                                        , "An attempt was made to use a JOIN or SUBST command on a drive that has already been substituted.")
   CASE_ERRCODE(ERROR_NOT_JOINED                                        , "ERROR_NOT_JOINED"                                        , "The system tried to delete the JOIN of a drive that is not joined.")
   CASE_ERRCODE(ERROR_NOT_SUBSTED                                       , "ERROR_NOT_SUBSTED"                                       , "The system tried to delete the substitution of a drive that is not substituted.")
   CASE_ERRCODE(ERROR_JOIN_TO_JOIN                                      , "ERROR_JOIN_TO_JOIN"                                      , "The system tried to join a drive to a directory on a joined drive.")
   CASE_ERRCODE(ERROR_SUBST_TO_SUBST                                    , "ERROR_SUBST_TO_SUBST"                                    , "The system tried to substitute a drive to a directory on a substituted drive.")
   CASE_ERRCODE(ERROR_JOIN_TO_SUBST                                     , "ERROR_JOIN_TO_SUBST"                                     , "The system tried to join a drive to a directory on a substituted drive.")
   CASE_ERRCODE(ERROR_SUBST_TO_JOIN                                     , "ERROR_SUBST_TO_JOIN"                                     , "The system tried to SUBST a drive to a directory on a joined drive.")
   CASE_ERRCODE(ERROR_BUSY_DRIVE                                        , "ERROR_BUSY_DRIVE"                                        , "The system cannot perform a JOIN or SUBST at this time.")
   CASE_ERRCODE(ERROR_SAME_DRIVE                                        , "ERROR_SAME_DRIVE"                                        , "The system cannot join or substitute a drive to or for a directory on the same drive.")
   CASE_ERRCODE(ERROR_DIR_NOT_ROOT                                      , "ERROR_DIR_NOT_ROOT"                                      , "The directory is not a subdirectory of the root directory.")
   CASE_ERRCODE(ERROR_DIR_NOT_EMPTY                                     , "ERROR_DIR_NOT_EMPTY"                                     , "The directory is not empty.")
   CASE_ERRCODE(ERROR_IS_SUBST_PATH                                     , "ERROR_IS_SUBST_PATH"                                     , "The path specified is being used in a substitute.")
   CASE_ERRCODE(ERROR_IS_JOIN_PATH                                      , "ERROR_IS_JOIN_PATH"                                      , "Not enough resources are available to process this command.")
   CASE_ERRCODE(ERROR_PATH_BUSY                                         , "ERROR_PATH_BUSY"                                         , "The path specified cannot be used at this time.")
   CASE_ERRCODE(ERROR_IS_SUBST_TARGET                                   , "ERROR_IS_SUBST_TARGET"                                   , "An attempt was made to join or substitute a drive for which a directory on the drive is the target of a previous substitute.")
   CASE_ERRCODE(ERROR_SYSTEM_TRACE                                      , "ERROR_SYSTEM_TRACE"                                      , "System trace information was not specified in your CONFIG.SYS file, or tracing is disallowed.")
   CASE_ERRCODE(ERROR_INVALID_EVENT_COUNT                               , "ERROR_INVALID_EVENT_COUNT"                               , "The number of specified semaphore events for DosMuxSemWait is not correct.")
   CASE_ERRCODE(ERROR_TOO_MANY_MUXWAITERS                               , "ERROR_TOO_MANY_MUXWAITERS"                               , "DosMuxSemWait did not execute; too many semaphores are already set.")
   CASE_ERRCODE(ERROR_INVALID_LIST_FORMAT                               , "ERROR_INVALID_LIST_FORMAT"                               , "The DosMuxSemWait list is not correct.")
   CASE_ERRCODE(ERROR_LABEL_TOO_LONG                                    , "ERROR_LABEL_TOO_LONG"                                    , "The volume label you entered exceeds the label character limit of the target file system.")
   CASE_ERRCODE(ERROR_TOO_MANY_TCBS                                     , "ERROR_TOO_MANY_TCBS"                                     , "Cannot create another thread.")
   CASE_ERRCODE(ERROR_SIGNAL_REFUSED                                    , "ERROR_SIGNAL_REFUSED"                                    , "The recipient process has refused the signal.")
   CASE_ERRCODE(ERROR_DISCARDED                                         , "ERROR_DISCARDED"                                         , "The segment is already discarded and cannot be locked.")
   CASE_ERRCODE(ERROR_NOT_LOCKED                                        , "ERROR_NOT_LOCKED"                                        , "The segment is already unlocked.")
   CASE_ERRCODE(ERROR_BAD_THREADID_ADDR                                 , "ERROR_BAD_THREADID_ADDR"                                 , "The address for the thread ID is not correct.")
   CASE_ERRCODE(ERROR_BAD_ARGUMENTS                                     , "ERROR_BAD_ARGUMENTS"                                     , "The argument string passed to DosExecPgm is not correct.")
   CASE_ERRCODE(ERROR_BAD_PATHNAME                                      , "ERROR_BAD_PATHNAME"                                      , "The specified path is invalid.")
   CASE_ERRCODE(ERROR_SIGNAL_PENDING                                    , "ERROR_SIGNAL_PENDING"                                    , "A signal is already pending.")
   CASE_ERRCODE(ERROR_MAX_THRDS_REACHED                                 , "ERROR_MAX_THRDS_REACHED"                                 , "No more threads can be created in the system.")
   CASE_ERRCODE(ERROR_LOCK_FAILED                                       , "ERROR_LOCK_FAILED"                                       , "Unable to lock a region of a file.")
   CASE_ERRCODE(ERROR_BUSY                                              , "ERROR_BUSY"                                              , "The requested resource is in use.")
   CASE_ERRCODE(ERROR_CANCEL_VIOLATION                                  , "ERROR_CANCEL_VIOLATION"                                  , "A lock request was not outstanding for the supplied cancel region.")
   CASE_ERRCODE(ERROR_ATOMIC_LOCKS_NOT_SUPPORTED                        , "ERROR_ATOMIC_LOCKS_NOT_SUPPORTED"                        , "The file system does not support atomic changes to the lock type.")
   CASE_ERRCODE(ERROR_INVALID_SEGMENT_NUMBER                            , "ERROR_INVALID_SEGMENT_NUMBER"                            , "The system detected a segment number that was not correct.")
   CASE_ERRCODE(ERROR_INVALID_ORDINAL                                   , "ERROR_INVALID_ORDINAL"                                   , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_ALREADY_EXISTS                                    , "ERROR_ALREADY_EXISTS"                                    , "Cannot create a file when that file already exists.")
   CASE_ERRCODE(ERROR_INVALID_FLAG_NUMBER                               , "ERROR_INVALID_FLAG_NUMBER"                               , "The flag passed is not correct.")
   CASE_ERRCODE(ERROR_SEM_NOT_FOUND                                     , "ERROR_SEM_NOT_FOUND"                                     , "The specified system semaphore name was not found.")
   CASE_ERRCODE(ERROR_INVALID_STARTING_CODESEG                          , "ERROR_INVALID_STARTING_CODESEG"                          , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_INVALID_STACKSEG                                  , "ERROR_INVALID_STACKSEG"                                  , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_INVALID_MODULETYPE                                , "ERROR_INVALID_MODULETYPE"                                , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_INVALID_EXE_SIGNATURE                             , "ERROR_INVALID_EXE_SIGNATURE"                             , "Cannot run %1 in Win32 mode.")
   CASE_ERRCODE(ERROR_EXE_MARKED_INVALID                                , "ERROR_EXE_MARKED_INVALID"                                , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_BAD_EXE_FORMAT                                    , "ERROR_BAD_EXE_FORMAT"                                    , "%1 is not a valid Win32 application.")
   CASE_ERRCODE(ERROR_ITERATED_DATA_EXCEEDS_64k                         , "ERROR_ITERATED_DATA_EXCEEDS_64k"                         , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_INVALID_MINALLOCSIZE                              , "ERROR_INVALID_MINALLOCSIZE"                              , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_DYNLINK_FROM_INVALID_RING                         , "ERROR_DYNLINK_FROM_INVALID_RING"                         , "The operating system cannot run this application program.")
   CASE_ERRCODE(ERROR_IOPL_NOT_ENABLED                                  , "ERROR_IOPL_NOT_ENABLED"                                  , "The operating system is not presently configured to run this application.")
   CASE_ERRCODE(ERROR_INVALID_SEGDPL                                    , "ERROR_INVALID_SEGDPL"                                    , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_AUTODATASEG_EXCEEDS_64k                           , "ERROR_AUTODATASEG_EXCEEDS_64k"                           , "The operating system cannot run this application program.")
   CASE_ERRCODE(ERROR_RING2SEG_MUST_BE_MOVABLE                          , "ERROR_RING2SEG_MUST_BE_MOVABLE"                          , "The code segment cannot be greater than or equal to 64K.")
   CASE_ERRCODE(ERROR_RELOC_CHAIN_XEEDS_SEGLIM                          , "ERROR_RELOC_CHAIN_XEEDS_SEGLIM"                          , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_INFLOOP_IN_RELOC_CHAIN                            , "ERROR_INFLOOP_IN_RELOC_CHAIN"                            , "The operating system cannot run %1.")
   CASE_ERRCODE(ERROR_ENVVAR_NOT_FOUND                                  , "ERROR_ENVVAR_NOT_FOUND"                                  , "The system could not find the environment option that was entered.")
   CASE_ERRCODE(ERROR_NO_SIGNAL_SENT                                    , "ERROR_NO_SIGNAL_SENT"                                    , "No process in the command subtree has a signal handler.")
   CASE_ERRCODE(ERROR_FILENAME_EXCED_RANGE                              , "ERROR_FILENAME_EXCED_RANGE"                              , "The filename or extension is too long.")
   CASE_ERRCODE(ERROR_RING2_STACK_IN_USE                                , "ERROR_RING2_STACK_IN_USE"                                , "The ring 2 stack is in use.")
   CASE_ERRCODE(ERROR_META_EXPANSION_TOO_LONG                           , "ERROR_META_EXPANSION_TOO_LONG"                           , "The global filename characters, * or ?, are entered incorrectly or too many global filename characters are specified.")
   CASE_ERRCODE(ERROR_INVALID_SIGNAL_NUMBER                             , "ERROR_INVALID_SIGNAL_NUMBER"                             , "The signal being posted is not correct.")
   CASE_ERRCODE(ERROR_THREAD_1_INACTIVE                                 , "ERROR_THREAD_1_INACTIVE"                                 , "The signal handler cannot be set.")
   CASE_ERRCODE(ERROR_LOCKED                                            , "ERROR_LOCKED"                                            , "The segment is locked and cannot be reallocated.")
   CASE_ERRCODE(ERROR_TOO_MANY_MODULES                                  , "ERROR_TOO_MANY_MODULES"                                  , "Too many dynamic-link modules are attached to this program or dynamic-link module.")
   CASE_ERRCODE(ERROR_NESTING_NOT_ALLOWED                               , "ERROR_NESTING_NOT_ALLOWED"                               , "Cannot nest calls to LoadModule.")
   CASE_ERRCODE(ERROR_EXE_MACHINE_TYPE_MISMATCH                         , "ERROR_EXE_MACHINE_TYPE_MISMATCH"                         , "The image file %1 is valid, but is for a machine type other than the current machine.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_EXE_CANNOT_MODIFY_SIGNED_BINARY                   , "ERROR_EXE_CANNOT_MODIFY_SIGNED_BINARY"                   , "The image file %1 is signed, unable to modify.")
   CASE_ERRCODE(ERROR_EXE_CANNOT_MODIFY_STRONG_SIGNED_BINARY            , "ERROR_EXE_CANNOT_MODIFY_STRONG_SIGNED_BINARY"            , "The image file %1 is strong signed, unable to modify.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_BAD_PIPE                                          , "ERROR_BAD_PIPE"                                          , "The pipe state is invalid.")
   CASE_ERRCODE(ERROR_PIPE_BUSY                                         , "ERROR_PIPE_BUSY"                                         , "All pipe instances are busy.")
   CASE_ERRCODE(ERROR_NO_DATA                                           , "ERROR_NO_DATA"                                           , "The pipe is being closed.")
   CASE_ERRCODE(ERROR_PIPE_NOT_CONNECTED                                , "ERROR_PIPE_NOT_CONNECTED"                                , "No process is on the other end of the pipe.")
   CASE_ERRCODE(ERROR_MORE_DATA                                         , "ERROR_MORE_DATA"                                         , "More data is available.")
   CASE_ERRCODE(ERROR_VC_DISCONNECTED                                   , "ERROR_VC_DISCONNECTED"                                   , "The session was canceled.")
   CASE_ERRCODE(ERROR_INVALID_EA_NAME                                   , "ERROR_INVALID_EA_NAME"                                   , "The specified extended attribute name was invalid.")
   CASE_ERRCODE(ERROR_EA_LIST_INCONSISTENT                              , "ERROR_EA_LIST_INCONSISTENT"                              , "The extended attributes are inconsistent.")
   CASE_ERRCODE(WAIT_TIMEOUT                                            , "WAIT_TIMEOUT"                                            , "The wait operation timed out.")
   CASE_ERRCODE(ERROR_NO_MORE_ITEMS                                     , "ERROR_NO_MORE_ITEMS"                                     , "No more data is available. (No more items have been found.)")
   CASE_ERRCODE(ERROR_CANNOT_COPY                                       , "ERROR_CANNOT_COPY"                                       , "The copy functions cannot be used.")
   CASE_ERRCODE(ERROR_DIRECTORY                                         , "ERROR_DIRECTORY"                                         , "The directory name is invalid.")
   CASE_ERRCODE(ERROR_EAS_DIDNT_FIT                                     , "ERROR_EAS_DIDNT_FIT"                                     , "The extended attributes did not fit in the buffer.")
   CASE_ERRCODE(ERROR_EA_FILE_CORRUPT                                   , "ERROR_EA_FILE_CORRUPT"                                   , "The extended attribute file on the mounted file system is corrupt.")
   CASE_ERRCODE(ERROR_EA_TABLE_FULL                                     , "ERROR_EA_TABLE_FULL"                                     , "The extended attribute table file is full.")
   CASE_ERRCODE(ERROR_INVALID_EA_HANDLE                                 , "ERROR_INVALID_EA_HANDLE"                                 , "The specified extended attribute handle is invalid.")
   CASE_ERRCODE(ERROR_EAS_NOT_SUPPORTED                                 , "ERROR_EAS_NOT_SUPPORTED"                                 , "The mounted file system does not support extended attributes.")
   CASE_ERRCODE(ERROR_NOT_OWNER                                         , "ERROR_NOT_OWNER"                                         , "Attempt to release mutex not owned by caller.")
   CASE_ERRCODE(ERROR_TOO_MANY_POSTS                                    , "ERROR_TOO_MANY_POSTS"                                    , "Too many posts were made to a semaphore.")
   CASE_ERRCODE(ERROR_PARTIAL_COPY                                      , "ERROR_PARTIAL_COPY"                                      , "Only part of a ReadProcessMemory or WriteProcessMemory request was completed.")
   CASE_ERRCODE(ERROR_OPLOCK_NOT_GRANTED                                , "ERROR_OPLOCK_NOT_GRANTED"                                , "The oplock request is denied.")
   CASE_ERRCODE(ERROR_INVALID_OPLOCK_PROTOCOL                           , "ERROR_INVALID_OPLOCK_PROTOCOL"                           , "An invalid oplock acknowledgment was received by the system.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_DISK_TOO_FRAGMENTED                               , "ERROR_DISK_TOO_FRAGMENTED"                               , "The volume is too fragmented to complete this operation.")
   CASE_ERRCODE(ERROR_DELETE_PENDING                                    , "ERROR_DELETE_PENDING"                                    , "The file cannot be opened because it is in the process of being deleted.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_MR_MID_NOT_FOUND                                  , "ERROR_MR_MID_NOT_FOUND"                                  , "The system cannot find message text for message number 0x%1 in the message file for %2.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_SCOPE_NOT_FOUND                                   , "ERROR_SCOPE_NOT_FOUND"                                   , "The scope specified was not found.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_ADDRESS                                   , "ERROR_INVALID_ADDRESS"                                   , "Attempt to access invalid address.")
   CASE_ERRCODE(ERROR_ARITHMETIC_OVERFLOW                               , "ERROR_ARITHMETIC_OVERFLOW"                               , "Arithmetic result exceeded 32 bits.")
   CASE_ERRCODE(ERROR_PIPE_CONNECTED                                    , "ERROR_PIPE_CONNECTED"                                    , "There is a process on other end of the pipe.")
   CASE_ERRCODE(ERROR_PIPE_LISTENING                                    , "ERROR_PIPE_LISTENING"                                    , "Waiting for a process to open the other end of the pipe.")
   CASE_ERRCODE(ERROR_EA_ACCESS_DENIED                                  , "ERROR_EA_ACCESS_DENIED"                                  , "Access to the extended attribute was denied.")
   CASE_ERRCODE(ERROR_OPERATION_ABORTED                                 , "ERROR_OPERATION_ABORTED"                                 , "The I/O operation has been aborted because of either a thread exit or an application request.")
   CASE_ERRCODE(ERROR_IO_INCOMPLETE                                     , "ERROR_IO_INCOMPLETE"                                     , "Overlapped I/O event is not in a signaled state.")
   CASE_ERRCODE(ERROR_IO_PENDING                                        , "ERROR_IO_PENDING"                                        , "Overlapped I/O operation is in progress.")
   CASE_ERRCODE(ERROR_NOACCESS                                          , "ERROR_NOACCESS"                                          , "Invalid access to memory location.")
   CASE_ERRCODE(ERROR_SWAPERROR                                         , "ERROR_SWAPERROR"                                         , "Error performing inpage operation.")
   CASE_ERRCODE(ERROR_STACK_OVERFLOW                                    , "ERROR_STACK_OVERFLOW"                                    , "Recursion too deep; the stack overflowed.")
   CASE_ERRCODE(ERROR_INVALID_MESSAGE                                   , "ERROR_INVALID_MESSAGE"                                   , "The window cannot act on the sent message.")
   CASE_ERRCODE(ERROR_CAN_NOT_COMPLETE                                  , "ERROR_CAN_NOT_COMPLETE"                                  , "Cannot complete this function.")
   CASE_ERRCODE(ERROR_INVALID_FLAGS                                     , "ERROR_INVALID_FLAGS"                                     , "Invalid flags.")
   CASE_ERRCODE(ERROR_UNRECOGNIZED_VOLUME                               , "ERROR_UNRECOGNIZED_VOLUME"                               , "The volume does not contain a recognized file system. Please make sure that all required file system drivers are loaded and that the volume is not corrupted.")
   CASE_ERRCODE(ERROR_FILE_INVALID                                      , "ERROR_FILE_INVALID"                                      , "The volume for a file has been externally altered so that the opened file is no longer valid.")
   CASE_ERRCODE(ERROR_FULLSCREEN_MODE                                   , "ERROR_FULLSCREEN_MODE"                                   , "The requested operation cannot be performed in full-screen mode.")
   CASE_ERRCODE(ERROR_NO_TOKEN                                          , "ERROR_NO_TOKEN"                                          , "An attempt was made to reference a token that does not exist.")
   CASE_ERRCODE(ERROR_BADDB                                             , "ERROR_BADDB"                                             , "The configuration registry database is corrupt.")
   CASE_ERRCODE(ERROR_BADKEY                                            , "ERROR_BADKEY"                                            , "The configuration registry key is invalid.")
   CASE_ERRCODE(ERROR_CANTOPEN                                          , "ERROR_CANTOPEN"                                          , "The configuration registry key could not be opened.")
   CASE_ERRCODE(ERROR_CANTREAD                                          , "ERROR_CANTREAD"                                          , "The configuration registry key could not be read.")
   CASE_ERRCODE(ERROR_CANTWRITE                                         , "ERROR_CANTWRITE"                                         , "The configuration registry key could not be written.")
   CASE_ERRCODE(ERROR_REGISTRY_RECOVERED                                , "ERROR_REGISTRY_RECOVERED"                                , "One of the files in the registry database had to be recovered by use of a log or alternate copy. The recovery was successful.")
   CASE_ERRCODE(ERROR_REGISTRY_CORRUPT                                  , "ERROR_REGISTRY_CORRUPT"                                  , "The registry is corrupted. The structure of one of the files containing registry data is corrupted, or the system's memory image of the file is corrupted, or the file could not be recovered because the alternate copy or log was absent or corrupted.")
   CASE_ERRCODE(ERROR_REGISTRY_IO_FAILED                                , "ERROR_REGISTRY_IO_FAILED"                                , "An I/O operation initiated by the registry failed unrecoverably. The registry could not read in, or write out, or flush, one of the files that contain the system's image of the registry.")
   CASE_ERRCODE(ERROR_NOT_REGISTRY_FILE                                 , "ERROR_NOT_REGISTRY_FILE"                                 , "The system has attempted to load or restore a file into the registry, but the specified file is not in a registry file format.")
   CASE_ERRCODE(ERROR_KEY_DELETED                                       , "ERROR_KEY_DELETED"                                       , "Illegal operation attempted on a registry key that has been marked for deletion.")
   CASE_ERRCODE(ERROR_NO_LOG_SPACE                                      , "ERROR_NO_LOG_SPACE"                                      , "System could not allocate the required space in a registry log.")
   CASE_ERRCODE(ERROR_KEY_HAS_CHILDREN                                  , "ERROR_KEY_HAS_CHILDREN"                                  , "Cannot create a symbolic link in a registry key that already has subkeys or values.")
   CASE_ERRCODE(ERROR_CHILD_MUST_BE_VOLATILE                            , "ERROR_CHILD_MUST_BE_VOLATILE"                            , "Cannot create a stable subkey under a volatile parent key.")
   CASE_ERRCODE(ERROR_NOTIFY_ENUM_DIR                                   , "ERROR_NOTIFY_ENUM_DIR"                                   , "A notify change request is being completed and the information is not being returned in the caller's buffer. The caller now needs to enumerate the files to find the changes.")
   CASE_ERRCODE(ERROR_DEPENDENT_SERVICES_RUNNING                        , "ERROR_DEPENDENT_SERVICES_RUNNING"                        , "A stop control has been sent to a service that other running services are dependent on.")
   CASE_ERRCODE(ERROR_INVALID_SERVICE_CONTROL                           , "ERROR_INVALID_SERVICE_CONTROL"                           , "The requested control is not valid for this service.")
   CASE_ERRCODE(ERROR_SERVICE_REQUEST_TIMEOUT                           , "ERROR_SERVICE_REQUEST_TIMEOUT"                           , "The service did not respond to the start or control request in a timely fashion.")
   CASE_ERRCODE(ERROR_SERVICE_NO_THREAD                                 , "ERROR_SERVICE_NO_THREAD"                                 , "A thread could not be created for the service.")
   CASE_ERRCODE(ERROR_SERVICE_DATABASE_LOCKED                           , "ERROR_SERVICE_DATABASE_LOCKED"                           , "The service database is locked.")
   CASE_ERRCODE(ERROR_SERVICE_ALREADY_RUNNING                           , "ERROR_SERVICE_ALREADY_RUNNING"                           , "An instance of the service is already running.")
   CASE_ERRCODE(ERROR_INVALID_SERVICE_ACCOUNT                           , "ERROR_INVALID_SERVICE_ACCOUNT"                           , "The account name is invalid or does not exist, or the password is invalid for the account name specified.")
   CASE_ERRCODE(ERROR_SERVICE_DISABLED                                  , "ERROR_SERVICE_DISABLED"                                  , "The service cannot be started, either because it is disabled or because it has no enabled devices associated with it.")
   CASE_ERRCODE(ERROR_CIRCULAR_DEPENDENCY                               , "ERROR_CIRCULAR_DEPENDENCY"                               , "Circular service dependency was specified.")
   CASE_ERRCODE(ERROR_SERVICE_DOES_NOT_EXIST                            , "ERROR_SERVICE_DOES_NOT_EXIST"                            , "The specified service does not exist as an installed service.")
   CASE_ERRCODE(ERROR_SERVICE_CANNOT_ACCEPT_CTRL                        , "ERROR_SERVICE_CANNOT_ACCEPT_CTRL"                        , "The service cannot accept control messages at this time.")
   CASE_ERRCODE(ERROR_SERVICE_NOT_ACTIVE                                , "ERROR_SERVICE_NOT_ACTIVE"                                , "The service has not been started.")
   CASE_ERRCODE(ERROR_FAILED_SERVICE_CONTROLLER_CONNECT                 , "ERROR_FAILED_SERVICE_CONTROLLER_CONNECT"                 , "The service process could not connect to the service controller.")
   CASE_ERRCODE(ERROR_EXCEPTION_IN_SERVICE                              , "ERROR_EXCEPTION_IN_SERVICE"                              , "An exception occurred in the service when handling the control request.")
   CASE_ERRCODE(ERROR_DATABASE_DOES_NOT_EXIST                           , "ERROR_DATABASE_DOES_NOT_EXIST"                           , "The database specified does not exist.")
   CASE_ERRCODE(ERROR_SERVICE_SPECIFIC_ERROR                            , "ERROR_SERVICE_SPECIFIC_ERROR"                            , "The service has returned a service-specific error code.")
   CASE_ERRCODE(ERROR_PROCESS_ABORTED                                   , "ERROR_PROCESS_ABORTED"                                   , "The process terminated unexpectedly.")
   CASE_ERRCODE(ERROR_SERVICE_DEPENDENCY_FAIL                           , "ERROR_SERVICE_DEPENDENCY_FAIL"                           , "The dependency service or group failed to start.")
   CASE_ERRCODE(ERROR_SERVICE_LOGON_FAILED                              , "ERROR_SERVICE_LOGON_FAILED"                              , "The service did not start due to a logon failure.")
   CASE_ERRCODE(ERROR_SERVICE_START_HANG                                , "ERROR_SERVICE_START_HANG"                                , "After starting, the service hung in a start-pending state.")
   CASE_ERRCODE(ERROR_INVALID_SERVICE_LOCK                              , "ERROR_INVALID_SERVICE_LOCK"                              , "The specified service database lock is invalid.")
   CASE_ERRCODE(ERROR_SERVICE_MARKED_FOR_DELETE                         , "ERROR_SERVICE_MARKED_FOR_DELETE"                         , "The specified service has been marked for deletion.")
   CASE_ERRCODE(ERROR_SERVICE_EXISTS                                    , "ERROR_SERVICE_EXISTS"                                    , "The specified service already exists.")
   CASE_ERRCODE(ERROR_ALREADY_RUNNING_LKG                               , "ERROR_ALREADY_RUNNING_LKG"                               , "The system is currently running with the last-known-good configuration.")
   CASE_ERRCODE(ERROR_SERVICE_DEPENDENCY_DELETED                        , "ERROR_SERVICE_DEPENDENCY_DELETED"                        , "The dependency service does not exist or has been marked for deletion.")
   CASE_ERRCODE(ERROR_BOOT_ALREADY_ACCEPTED                             , "ERROR_BOOT_ALREADY_ACCEPTED"                             , "The current boot has already been accepted for use as the last-known-good control set.")
   CASE_ERRCODE(ERROR_SERVICE_NEVER_STARTED                             , "ERROR_SERVICE_NEVER_STARTED"                             , "No attempts to start the service have been made since the last boot.")
   CASE_ERRCODE(ERROR_DUPLICATE_SERVICE_NAME                            , "ERROR_DUPLICATE_SERVICE_NAME"                            , "The name is already in use as either a service name or a service display name.")
   CASE_ERRCODE(ERROR_DIFFERENT_SERVICE_ACCOUNT                         , "ERROR_DIFFERENT_SERVICE_ACCOUNT"                         , "The account specified for this service is different from the account specified for other services running in the same process.")
   CASE_ERRCODE(ERROR_CANNOT_DETECT_DRIVER_FAILURE                      , "ERROR_CANNOT_DETECT_DRIVER_FAILURE"                      , "Failure actions can only be set for Win32 services, not for drivers.")
   CASE_ERRCODE(ERROR_CANNOT_DETECT_PROCESS_ABORT                       , "ERROR_CANNOT_DETECT_PROCESS_ABORT"                       , "This service runs in the same process as the service control manager. Therefore, the service control manager cannot take action if this service's process terminates unexpectedly.")
   CASE_ERRCODE(ERROR_NO_RECOVERY_PROGRAM                               , "ERROR_NO_RECOVERY_PROGRAM"                               , "No recovery program has been configured for this service.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_SERVICE_NOT_IN_EXE                                , "ERROR_SERVICE_NOT_IN_EXE"                                , "The executable program that this service is configured to run in does not implement the service.")
   CASE_ERRCODE(ERROR_NOT_SAFEBOOT_SERVICE                              , "ERROR_NOT_SAFEBOOT_SERVICE"                              , "This service cannot be started in Safe Mode.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_END_OF_MEDIA                                      , "ERROR_END_OF_MEDIA"                                      , "The physical end of the tape has been reached.")
   CASE_ERRCODE(ERROR_FILEMARK_DETECTED                                 , "ERROR_FILEMARK_DETECTED"                                 , "A tape access reached a filemark.")
   CASE_ERRCODE(ERROR_BEGINNING_OF_MEDIA                                , "ERROR_BEGINNING_OF_MEDIA"                                , "The beginning of the tape or a partition was encountered.")
   CASE_ERRCODE(ERROR_SETMARK_DETECTED                                  , "ERROR_SETMARK_DETECTED"                                  , "A tape access reached the end of a set of files.")
   CASE_ERRCODE(ERROR_NO_DATA_DETECTED                                  , "ERROR_NO_DATA_DETECTED"                                  , "No more data is on the tape.")
   CASE_ERRCODE(ERROR_PARTITION_FAILURE                                 , "ERROR_PARTITION_FAILURE"                                 , "Tape could not be partitioned.")
   CASE_ERRCODE(ERROR_INVALID_BLOCK_LENGTH                              , "ERROR_INVALID_BLOCK_LENGTH"                              , "When accessing a new tape of a multivolume partition, the current block size is incorrect.")
   CASE_ERRCODE(ERROR_DEVICE_NOT_PARTITIONED                            , "ERROR_DEVICE_NOT_PARTITIONED"                            , "Tape partition information could not be found when loading a tape.")
   CASE_ERRCODE(ERROR_UNABLE_TO_LOCK_MEDIA                              , "ERROR_UNABLE_TO_LOCK_MEDIA"                              , "Unable to lock the media eject mechanism.")
   CASE_ERRCODE(ERROR_UNABLE_TO_UNLOAD_MEDIA                            , "ERROR_UNABLE_TO_UNLOAD_MEDIA"                            , "Unable to unload the media.")
   CASE_ERRCODE(ERROR_MEDIA_CHANGED                                     , "ERROR_MEDIA_CHANGED"                                     , "The media in the drive may have changed.")
   CASE_ERRCODE(ERROR_BUS_RESET                                         , "ERROR_BUS_RESET"                                         , "The I/O bus was reset.")
   CASE_ERRCODE(ERROR_NO_MEDIA_IN_DRIVE                                 , "ERROR_NO_MEDIA_IN_DRIVE"                                 , "No media in drive.")
   CASE_ERRCODE(ERROR_NO_UNICODE_TRANSLATION                            , "ERROR_NO_UNICODE_TRANSLATION"                            , "No mapping for the Unicode character exists in the target multi-byte code page.")
   CASE_ERRCODE(ERROR_DLL_INIT_FAILED                                   , "ERROR_DLL_INIT_FAILED"                                   , "A dynamic link library (DLL) initialization routine failed.")
   CASE_ERRCODE(ERROR_SHUTDOWN_IN_PROGRESS                              , "ERROR_SHUTDOWN_IN_PROGRESS"                              , "A system shutdown is in progress.")
   CASE_ERRCODE(ERROR_NO_SHUTDOWN_IN_PROGRESS                           , "ERROR_NO_SHUTDOWN_IN_PROGRESS"                           , "Unable to abort the system shutdown because no shutdown was in progress.")
   CASE_ERRCODE(ERROR_IO_DEVICE                                         , "ERROR_IO_DEVICE"                                         , "The request could not be performed because of an I/O device error.")
   CASE_ERRCODE(ERROR_SERIAL_NO_DEVICE                                  , "ERROR_SERIAL_NO_DEVICE"                                  , "No serial device was successfully initialized. The serial driver will unload.")
   CASE_ERRCODE(ERROR_IRQ_BUSY                                          , "ERROR_IRQ_BUSY"                                          , "Unable to open a device that was sharing an interrupt request (IRQ) with other devices. At least one other device that uses that IRQ was already opened.")
   CASE_ERRCODE(ERROR_MORE_WRITES                                       , "ERROR_MORE_WRITES"                                       , "A serial I/O operation was completed by another write to the serial port. (The IOCTL_SERIAL_XOFF_COUNTER reached zero.)")
   CASE_ERRCODE(ERROR_COUNTER_TIMEOUT                                   , "ERROR_COUNTER_TIMEOUT"                                   , "A serial I/O operation completed because the timeout period expired. (The IOCTL_SERIAL_XOFF_COUNTER did not reach zero.)")
   CASE_ERRCODE(ERROR_FLOPPY_ID_MARK_NOT_FOUND                          , "ERROR_FLOPPY_ID_MARK_NOT_FOUND"                          , "No ID address mark was found on the floppy disk.")
   CASE_ERRCODE(ERROR_FLOPPY_WRONG_CYLINDER                             , "ERROR_FLOPPY_WRONG_CYLINDER"                             , "Mismatch between the floppy disk sector ID field and the floppy disk controller track address.")
   CASE_ERRCODE(ERROR_FLOPPY_UNKNOWN_ERROR                              , "ERROR_FLOPPY_UNKNOWN_ERROR"                              , "The floppy disk controller reported an error that is not recognized by the floppy disk driver.")
   CASE_ERRCODE(ERROR_FLOPPY_BAD_REGISTERS                              , "ERROR_FLOPPY_BAD_REGISTERS"                              , "The floppy disk controller returned inconsistent results in its registers.")
   CASE_ERRCODE(ERROR_DISK_RECALIBRATE_FAILED                           , "ERROR_DISK_RECALIBRATE_FAILED"                           , "While accessing the hard disk, a recalibrate operation failed, even after retries.")
   CASE_ERRCODE(ERROR_DISK_OPERATION_FAILED                             , "ERROR_DISK_OPERATION_FAILED"                             , "While accessing the hard disk, a disk operation failed even after retries.")
   CASE_ERRCODE(ERROR_DISK_RESET_FAILED                                 , "ERROR_DISK_RESET_FAILED"                                 , "While accessing the hard disk, a disk controller reset was needed, but even that failed.")
   CASE_ERRCODE(ERROR_EOM_OVERFLOW                                      , "ERROR_EOM_OVERFLOW"                                      , "Physical end of tape encountered.")
   CASE_ERRCODE(ERROR_NOT_ENOUGH_SERVER_MEMORY                          , "ERROR_NOT_ENOUGH_SERVER_MEMORY"                          , "Not enough server storage is available to process this command.")
   CASE_ERRCODE(ERROR_POSSIBLE_DEADLOCK                                 , "ERROR_POSSIBLE_DEADLOCK"                                 , "A potential deadlock condition has been detected.")
   CASE_ERRCODE(ERROR_MAPPED_ALIGNMENT                                  , "ERROR_MAPPED_ALIGNMENT"                                  , "The base address or the file offset specified does not have the proper alignment.")
   CASE_ERRCODE(ERROR_SET_POWER_STATE_VETOED                            , "ERROR_SET_POWER_STATE_VETOED"                            , "An attempt to change the system power state was vetoed by another application or driver.")
   CASE_ERRCODE(ERROR_SET_POWER_STATE_FAILED                            , "ERROR_SET_POWER_STATE_FAILED"                            , "The system BIOS failed an attempt to change the system power state.")
   CASE_ERRCODE(ERROR_TOO_MANY_LINKS                                    , "ERROR_TOO_MANY_LINKS"                                    , "An attempt was made to create more links on a file than the file system supports.")
   CASE_ERRCODE(ERROR_OLD_WIN_VERSION                                   , "ERROR_OLD_WIN_VERSION"                                   , "The specified program requires a newer version of Windows.")
   CASE_ERRCODE(ERROR_APP_WRONG_OS                                      , "ERROR_APP_WRONG_OS"                                      , "The specified program is not a Windows or MS-DOS program.")
   CASE_ERRCODE(ERROR_SINGLE_INSTANCE_APP                               , "ERROR_SINGLE_INSTANCE_APP"                               , "Cannot start more than one instance of the specified program.")
   CASE_ERRCODE(ERROR_RMODE_APP                                         , "ERROR_RMODE_APP"                                         , "The specified program was written for an earlier version of Windows.")
   CASE_ERRCODE(ERROR_INVALID_DLL                                       , "ERROR_INVALID_DLL"                                       , "One of the library files needed to run this application is damaged.")
   CASE_ERRCODE(ERROR_NO_ASSOCIATION                                    , "ERROR_NO_ASSOCIATION"                                    , "No application is associated with the specified file for this operation.")
   CASE_ERRCODE(ERROR_DDE_FAIL                                          , "ERROR_DDE_FAIL"                                          , "An error occurred in sending the command to the application.")
   CASE_ERRCODE(ERROR_DLL_NOT_FOUND                                     , "ERROR_DLL_NOT_FOUND"                                     , "One of the library files needed to run this application cannot be found.")
   CASE_ERRCODE(ERROR_NO_MORE_USER_HANDLES                              , "ERROR_NO_MORE_USER_HANDLES"                              , "The current process has used all of its system allowance of handles for Window Manager objects.")
   CASE_ERRCODE(ERROR_MESSAGE_SYNC_ONLY                                 , "ERROR_MESSAGE_SYNC_ONLY"                                 , "The message can be used only with synchronous operations.")
   CASE_ERRCODE(ERROR_SOURCE_ELEMENT_EMPTY                              , "ERROR_SOURCE_ELEMENT_EMPTY"                              , "The indicated source element has no media.")
   CASE_ERRCODE(ERROR_DESTINATION_ELEMENT_FULL                          , "ERROR_DESTINATION_ELEMENT_FULL"                          , "The indicated destination element already contains media.")
   CASE_ERRCODE(ERROR_ILLEGAL_ELEMENT_ADDRESS                           , "ERROR_ILLEGAL_ELEMENT_ADDRESS"                           , "The indicated element does not exist.")
   CASE_ERRCODE(ERROR_MAGAZINE_NOT_PRESENT                              , "ERROR_MAGAZINE_NOT_PRESENT"                              , "The indicated element is part of a magazine that is not present.")
   CASE_ERRCODE(ERROR_DEVICE_REINITIALIZATION_NEEDED                    , "ERROR_DEVICE_REINITIALIZATION_NEEDED"                    , "The indicated device requires reinitialization due to hardware errors.")
   CASE_ERRCODE(ERROR_DEVICE_REQUIRES_CLEANING                          , "ERROR_DEVICE_REQUIRES_CLEANING"                          , "The device has indicated that cleaning is required before further operations are attempted.")
   CASE_ERRCODE(ERROR_DEVICE_DOOR_OPEN                                  , "ERROR_DEVICE_DOOR_OPEN"                                  , "The device has indicated that its door is open.")
   CASE_ERRCODE(ERROR_DEVICE_NOT_CONNECTED                              , "ERROR_DEVICE_NOT_CONNECTED"                              , "The device is not connected.")
   CASE_ERRCODE(ERROR_NOT_FOUND                                         , "ERROR_NOT_FOUND"                                         , "Element not found.")
   CASE_ERRCODE(ERROR_NO_MATCH                                          , "ERROR_NO_MATCH"                                          , "There was no match for the specified key in the index.")
   CASE_ERRCODE(ERROR_SET_NOT_FOUND                                     , "ERROR_SET_NOT_FOUND"                                     , "The property set specified does not exist on the object.")
   CASE_ERRCODE(ERROR_POINT_NOT_FOUND                                   , "ERROR_POINT_NOT_FOUND"                                   , "The point passed to GetMouseMovePointsEx is not in the buffer.")
   CASE_ERRCODE(ERROR_NO_TRACKING_SERVICE                               , "ERROR_NO_TRACKING_SERVICE"                               , "The tracking (workstation) service is not running.")
   CASE_ERRCODE(ERROR_NO_VOLUME_ID                                      , "ERROR_NO_VOLUME_ID"                                      , "The Volume ID could not be found.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_UNABLE_TO_REMOVE_REPLACED                         , "ERROR_UNABLE_TO_REMOVE_REPLACED"                         , "Unable to remove the file to be replaced.")
   CASE_ERRCODE(ERROR_UNABLE_TO_MOVE_REPLACEMENT                        , "ERROR_UNABLE_TO_MOVE_REPLACEMENT"                        , "Unable to move the replacement file to the file to be replaced. The file to be replaced has retained its original name.")
   CASE_ERRCODE(ERROR_UNABLE_TO_MOVE_REPLACEMENT_2                      , "ERROR_UNABLE_TO_MOVE_REPLACEMENT_2"                      , "Unable to move the replacement file to the file to be replaced. The file to be replaced has been renamed using the backup name.")
   CASE_ERRCODE(ERROR_JOURNAL_DELETE_IN_PROGRESS                        , "ERROR_JOURNAL_DELETE_IN_PROGRESS"                        , "The volume change journal is being deleted.")
   CASE_ERRCODE(ERROR_JOURNAL_NOT_ACTIVE                                , "ERROR_JOURNAL_NOT_ACTIVE"                                , "The volume change journal is not active.")
   CASE_ERRCODE(ERROR_POTENTIAL_FILE_FOUND                              , "ERROR_POTENTIAL_FILE_FOUND"                              , "A file was found, but it may not be the correct file.")
   CASE_ERRCODE(ERROR_JOURNAL_ENTRY_DELETED                             , "ERROR_JOURNAL_ENTRY_DELETED"                             , "The journal entry has been deleted from the journal.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_BAD_DEVICE                                        , "ERROR_BAD_DEVICE"                                        , "The specified device name is invalid.")
   CASE_ERRCODE(ERROR_CONNECTION_UNAVAIL                                , "ERROR_CONNECTION_UNAVAIL"                                , "The device is not currently connected but it is a remembered connection.")
   CASE_ERRCODE(ERROR_DEVICE_ALREADY_REMEMBERED                         , "ERROR_DEVICE_ALREADY_REMEMBERED"                         , "The local device name has a remembered connection to another network resource.")
   CASE_ERRCODE(ERROR_NO_NET_OR_BAD_PATH                                , "ERROR_NO_NET_OR_BAD_PATH"                                , "No network provider accepted the given network path.")
   CASE_ERRCODE(ERROR_BAD_PROVIDER                                      , "ERROR_BAD_PROVIDER"                                      , "The specified network provider name is invalid.")
   CASE_ERRCODE(ERROR_CANNOT_OPEN_PROFILE                               , "ERROR_CANNOT_OPEN_PROFILE"                               , "Unable to open the network connection profile.")
   CASE_ERRCODE(ERROR_BAD_PROFILE                                       , "ERROR_BAD_PROFILE"                                       , "The network connection profile is corrupted.")
   CASE_ERRCODE(ERROR_NOT_CONTAINER                                     , "ERROR_NOT_CONTAINER"                                     , "Cannot enumerate a noncontainer.")
   CASE_ERRCODE(ERROR_EXTENDED_ERROR                                    , "ERROR_EXTENDED_ERROR"                                    , "An extended error has occurred.")
   CASE_ERRCODE(ERROR_INVALID_GROUPNAME                                 , "ERROR_INVALID_GROUPNAME"                                 , "The format of the specified group name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_COMPUTERNAME                              , "ERROR_INVALID_COMPUTERNAME"                              , "The format of the specified computer name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_EVENTNAME                                 , "ERROR_INVALID_EVENTNAME"                                 , "The format of the specified event name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_DOMAINNAME                                , "ERROR_INVALID_DOMAINNAME"                                , "The format of the specified domain name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_SERVICENAME                               , "ERROR_INVALID_SERVICENAME"                               , "The format of the specified service name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_NETNAME                                   , "ERROR_INVALID_NETNAME"                                   , "The format of the specified network name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_SHARENAME                                 , "ERROR_INVALID_SHARENAME"                                 , "The format of the specified share name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_PASSWORDNAME                              , "ERROR_INVALID_PASSWORDNAME"                              , "The format of the specified password is invalid.")
   CASE_ERRCODE(ERROR_INVALID_MESSAGENAME                               , "ERROR_INVALID_MESSAGENAME"                               , "The format of the specified message name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_MESSAGEDEST                               , "ERROR_INVALID_MESSAGEDEST"                               , "The format of the specified message destination is invalid.")
   CASE_ERRCODE(ERROR_SESSION_CREDENTIAL_CONFLICT                       , "ERROR_SESSION_CREDENTIAL_CONFLICT"                       , "Multiple connections to a server or shared resource by the same user, using more than one user name, are not allowed. Disconnect all previous connections to the server or shared resource and try again.")
   CASE_ERRCODE(ERROR_REMOTE_SESSION_LIMIT_EXCEEDED                     , "ERROR_REMOTE_SESSION_LIMIT_EXCEEDED"                     , "An attempt was made to establish a session to a network server, but there are already too many sessions established to that server.")
   CASE_ERRCODE(ERROR_DUP_DOMAINNAME                                    , "ERROR_DUP_DOMAINNAME"                                    , "The workgroup or domain name is already in use by another computer on the network.")
   CASE_ERRCODE(ERROR_NO_NETWORK                                        , "ERROR_NO_NETWORK"                                        , "The network is not present or not started.")
   CASE_ERRCODE(ERROR_CANCELLED                                         , "ERROR_CANCELLED"                                         , "The operation was canceled by the user.")
   CASE_ERRCODE(ERROR_USER_MAPPED_FILE                                  , "ERROR_USER_MAPPED_FILE"                                  , "The requested operation cannot be performed on a file with a user-mapped section open.")
   CASE_ERRCODE(ERROR_CONNECTION_REFUSED                                , "ERROR_CONNECTION_REFUSED"                                , "The remote system refused the network connection.")
   CASE_ERRCODE(ERROR_GRACEFUL_DISCONNECT                               , "ERROR_GRACEFUL_DISCONNECT"                               , "The network connection was gracefully closed.")
   CASE_ERRCODE(ERROR_ADDRESS_ALREADY_ASSOCIATED                        , "ERROR_ADDRESS_ALREADY_ASSOCIATED"                        , "The network transport endpoint already has an address associated with it.")
   CASE_ERRCODE(ERROR_ADDRESS_NOT_ASSOCIATED                            , "ERROR_ADDRESS_NOT_ASSOCIATED"                            , "An address has not yet been associated with the network endpoint.")
   CASE_ERRCODE(ERROR_CONNECTION_INVALID                                , "ERROR_CONNECTION_INVALID"                                , "An operation was attempted on a nonexistent network connection.")
   CASE_ERRCODE(ERROR_CONNECTION_ACTIVE                                 , "ERROR_CONNECTION_ACTIVE"                                 , "An invalid operation was attempted on an active network connection.")
   CASE_ERRCODE(ERROR_NETWORK_UNREACHABLE                               , "ERROR_NETWORK_UNREACHABLE"                               , "The network location cannot be reached. For information about network troubleshooting, see Windows Help.")
   CASE_ERRCODE(ERROR_HOST_UNREACHABLE                                  , "ERROR_HOST_UNREACHABLE"                                  , "The network location cannot be reached. For information about network troubleshooting, see Windows Help.")
   CASE_ERRCODE(ERROR_PROTOCOL_UNREACHABLE                              , "ERROR_PROTOCOL_UNREACHABLE"                              , "The network location cannot be reached. For information about network troubleshooting, see Windows Help.")
   CASE_ERRCODE(ERROR_PORT_UNREACHABLE                                  , "ERROR_PORT_UNREACHABLE"                                  , "No service is operating at the destination network endpoint on the remote system.")
   CASE_ERRCODE(ERROR_REQUEST_ABORTED                                   , "ERROR_REQUEST_ABORTED"                                   , "The request was aborted.")
   CASE_ERRCODE(ERROR_CONNECTION_ABORTED                                , "ERROR_CONNECTION_ABORTED"                                , "The network connection was aborted by the local system.")
   CASE_ERRCODE(ERROR_RETRY                                             , "ERROR_RETRY"                                             , "The operation could not be completed. A retry should be performed.")
   CASE_ERRCODE(ERROR_CONNECTION_COUNT_LIMIT                            , "ERROR_CONNECTION_COUNT_LIMIT"                            , "A connection to the server could not be made because the limit on the number of concurrent connections for this account has been reached.")
   CASE_ERRCODE(ERROR_LOGIN_TIME_RESTRICTION                            , "ERROR_LOGIN_TIME_RESTRICTION"                            , "Attempting to log in during an unauthorized time of day for this account.")
   CASE_ERRCODE(ERROR_LOGIN_WKSTA_RESTRICTION                           , "ERROR_LOGIN_WKSTA_RESTRICTION"                           , "The account is not authorized to log in from this station.")
   CASE_ERRCODE(ERROR_INCORRECT_ADDRESS                                 , "ERROR_INCORRECT_ADDRESS"                                 , "The network address could not be used for the operation requested.")
   CASE_ERRCODE(ERROR_ALREADY_REGISTERED                                , "ERROR_ALREADY_REGISTERED"                                , "The service is already registered.")
   CASE_ERRCODE(ERROR_SERVICE_NOT_FOUND                                 , "ERROR_SERVICE_NOT_FOUND"                                 , "The specified service does not exist.")
   CASE_ERRCODE(ERROR_NOT_AUTHENTICATED                                 , "ERROR_NOT_AUTHENTICATED"                                 , "The operation being requested was not performed because the user has not been authenticated.")
   CASE_ERRCODE(ERROR_NOT_LOGGED_ON                                     , "ERROR_NOT_LOGGED_ON"                                     , "The operation being requested was not performed because the user has not logged on to the network. The specified service does not exist.")
   CASE_ERRCODE(ERROR_CONTINUE                                          , "ERROR_CONTINUE"                                          , "Continue with work in progress.")
   CASE_ERRCODE(ERROR_ALREADY_INITIALIZED                               , "ERROR_ALREADY_INITIALIZED"                               , "An attempt was made to perform an initialization operation when initialization has already been completed.")
   CASE_ERRCODE(ERROR_NO_MORE_DEVICES                                   , "ERROR_NO_MORE_DEVICES"                                   , "No more local devices.")
   CASE_ERRCODE(ERROR_NO_SUCH_SITE                                      , "ERROR_NO_SUCH_SITE"                                      , "The specified site does not exist.")
   CASE_ERRCODE(ERROR_DOMAIN_CONTROLLER_EXISTS                          , "ERROR_DOMAIN_CONTROLLER_EXISTS"                          , "A domain controller with the specified name already exists.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_ONLY_IF_CONNECTED                                 , "ERROR_ONLY_IF_CONNECTED"                                 , "This operation is supported only when you are connected to the server.")
   CASE_ERRCODE(ERROR_OVERRIDE_NOCHANGES                                , "ERROR_OVERRIDE_NOCHANGES"                                , "The group policy framework should call the extension even if there are no changes.")
   CASE_ERRCODE(ERROR_BAD_USER_PROFILE                                  , "ERROR_BAD_USER_PROFILE"                                  , "The specified user does not have a valid profile.")
   CASE_ERRCODE(ERROR_NOT_SUPPORTED_ON_SBS                              , "ERROR_NOT_SUPPORTED_ON_SBS"                              , "This operation is not supported on a Microsoft Small Business Server.")
   CASE_ERRCODE(ERROR_SERVER_SHUTDOWN_IN_PROGRESS                       , "ERROR_SERVER_SHUTDOWN_IN_PROGRESS"                       , "The server machine is shutting down.")
   CASE_ERRCODE(ERROR_HOST_DOWN                                         , "ERROR_HOST_DOWN"                                         , "The remote system is not available. For information about network troubleshooting, see Windows Help.")
   CASE_ERRCODE(ERROR_NON_ACCOUNT_SID                                   , "ERROR_NON_ACCOUNT_SID"                                   , "The security identifier provided is not from an account domain.")
   CASE_ERRCODE(ERROR_NON_DOMAIN_SID                                    , "ERROR_NON_DOMAIN_SID"                                    , "The security identifier provided does not have a domain component.")
   CASE_ERRCODE(ERROR_APPHELP_BLOCK                                     , "ERROR_APPHELP_BLOCK"                                     , "AppHelp dialog canceled thus preventing the application from starting.")
   CASE_ERRCODE(ERROR_ACCESS_DISABLED_BY_POLICY                         , "ERROR_ACCESS_DISABLED_BY_POLICY"                         , "Windows cannot open this program because it has been prevented by a software restriction policy. For more information, open Event Viewer or contact your system administrator.")
   CASE_ERRCODE(ERROR_REG_NAT_CONSUMPTION                               , "ERROR_REG_NAT_CONSUMPTION"                               , "A program attempt to use an invalid register value. Normally caused by an uninitialized register. This error is Itanium specific.")
   CASE_ERRCODE(ERROR_CSCSHARE_OFFLINE                                  , "ERROR_CSCSHARE_OFFLINE"                                  , "The share is currently offline or does not exist.")
   CASE_ERRCODE(ERROR_PKINIT_FAILURE                                    , "ERROR_PKINIT_FAILURE"                                    , "The kerberos protocol encountered an error while validating the KDC certificate during smartcard logon.")
   CASE_ERRCODE(ERROR_SMARTCARD_SUBSYSTEM_FAILURE                       , "ERROR_SMARTCARD_SUBSYSTEM_FAILURE"                       , "The kerberos protocol encountered an error while attempting to utilize the smartcard subsystem.")
   CASE_ERRCODE(ERROR_DOWNGRADE_DETECTED                                , "ERROR_DOWNGRADE_DETECTED"                                , "The system detected a possible attempt to compromise security. Please ensure that you can contact the server that authenticated you.")
   CASE_ERRCODE(SEC_E_SMARTCARD_CERT_REVOKED                            , "SEC_E_SMARTCARD_CERT_REVOKED"                            , "The smartcard certificate used for authentication has been revoked. Please contact your system administrator. There may be additional information in the event log.")
   CASE_ERRCODE(SEC_E_ISSUING_CA_UNTRUSTED                              , "SEC_E_ISSUING_CA_UNTRUSTED"                              , "An untrusted certificate authority was detected while processing the smartcard certificate used for authentication. Please contact your system administrator.")
   CASE_ERRCODE(SEC_E_REVOCATION_OFFLINE_C                              , "SEC_E_REVOCATION_OFFLINE_C"                              , "The revocation status of the smartcard certificate used for authentication could not be determined. Please contact your system administrator.")
   CASE_ERRCODE(SEC_E_PKINIT_CLIENT_FAILURE                             , "SEC_E_PKINIT_CLIENT_FAILURE"                             , "The smartcard certificate used for authentication was not trusted. Please contact your system administrator.")
   CASE_ERRCODE(SEC_E_SMARTCARD_CERT_EXPIRED                            , "SEC_E_SMARTCARD_CERT_EXPIRED"                            , "The smartcard certificate used for authentication has expired. Please contact your system administrator.")
   CASE_ERRCODE(ERROR_MACHINE_LOCKED                                    , "ERROR_MACHINE_LOCKED"                                    , "The machine is locked and cannot be shut down without the force option.")
   CASE_ERRCODE(ERROR_CALLBACK_SUPPLIED_INVALID_DATA                    , "ERROR_CALLBACK_SUPPLIED_INVALID_DATA"                    , "An application-defined callback gave invalid data when called.")
   CASE_ERRCODE(ERROR_SYNC_FOREGROUND_REFRESH_REQUIRED                  , "ERROR_SYNC_FOREGROUND_REFRESH_REQUIRED"                  , "The group policy framework should call the extension in the synchronous foreground policy refresh.")
   CASE_ERRCODE(ERROR_DRIVER_BLOCKED                                    , "ERROR_DRIVER_BLOCKED"                                    , "This driver has been blocked from loading.")
   CASE_ERRCODE(ERROR_INVALID_IMPORT_OF_NON_DLL                         , "ERROR_INVALID_IMPORT_OF_NON_DLL"                         , "A dynamic link library (DLL) referenced a module that was neither a DLL nor the process's executable image.")
   CASE_ERRCODE(ERROR_ACCESS_DISABLED_WEBBLADE                          , "ERROR_ACCESS_DISABLED_WEBBLADE"                          , "Windows cannot open this program since it has been disabled.")
   CASE_ERRCODE(ERROR_ACCESS_DISABLED_WEBBLADE_TAMPER                   , "ERROR_ACCESS_DISABLED_WEBBLADE_TAMPER"                   , "Windows cannot open this program because the license enforcement system has been tampered with or become corrupted.")
   CASE_ERRCODE(ERROR_RECOVERY_FAILURE                                  , "ERROR_RECOVERY_FAILURE"                                  , "A transaction recovery failed.")
   CASE_ERRCODE(ERROR_ALREADY_FIBER                                     , "ERROR_ALREADY_FIBER"                                     , "The current thread has already been converted to a fiber.")
   CASE_ERRCODE(ERROR_ALREADY_THREAD                                    , "ERROR_ALREADY_THREAD"                                    , "The current thread has already been converted from a fiber.")
   CASE_ERRCODE(ERROR_STACK_BUFFER_OVERRUN                              , "ERROR_STACK_BUFFER_OVERRUN"                              , "The system detected an overrun of a stack-based buffer in this application. This overrun could potentially allow a malicious user to gain control of this application.")
   CASE_ERRCODE(ERROR_PARAMETER_QUOTA_EXCEEDED                          , "ERROR_PARAMETER_QUOTA_EXCEEDED"                          , "Data present in one of the parameters is more than the function can operate on.")
   CASE_ERRCODE(ERROR_DEBUGGER_INACTIVE                                 , "ERROR_DEBUGGER_INACTIVE"                                 , "An attempt to do an operation on a debug object failed because the object is in the process of being deleted.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_NOT_ALL_ASSIGNED                                  , "ERROR_NOT_ALL_ASSIGNED"                                  , "Not all privileges referenced are assigned to the caller.")
   CASE_ERRCODE(ERROR_SOME_NOT_MAPPED                                   , "ERROR_SOME_NOT_MAPPED"                                   , "Some mapping between account names and security IDs was not done.")
   CASE_ERRCODE(ERROR_NO_QUOTAS_FOR_ACCOUNT                             , "ERROR_NO_QUOTAS_FOR_ACCOUNT"                             , "No system quota limits are specifically set for this account.")
   CASE_ERRCODE(ERROR_LOCAL_USER_SESSION_KEY                            , "ERROR_LOCAL_USER_SESSION_KEY"                            , "No encryption key is available. A well-known encryption key was returned.")
   CASE_ERRCODE(ERROR_NULL_LM_PASSWORD                                  , "ERROR_NULL_LM_PASSWORD"                                  , "The password is too complex to be converted to a LAN Manager password. The LAN Manager password returned is a NULL string.")
   CASE_ERRCODE(ERROR_UNKNOWN_REVISION                                  , "ERROR_UNKNOWN_REVISION"                                  , "The revision level is unknown.")
   CASE_ERRCODE(ERROR_REVISION_MISMATCH                                 , "ERROR_REVISION_MISMATCH"                                 , "Indicates two revision levels are incompatible.")
   CASE_ERRCODE(ERROR_INVALID_OWNER                                     , "ERROR_INVALID_OWNER"                                     , "This security ID may not be assigned as the owner of this object.")
   CASE_ERRCODE(ERROR_INVALID_PRIMARY_GROUP                             , "ERROR_INVALID_PRIMARY_GROUP"                             , "This security ID may not be assigned as the primary group of an object.")
   CASE_ERRCODE(ERROR_NO_IMPERSONATION_TOKEN                            , "ERROR_NO_IMPERSONATION_TOKEN"                            , "An attempt has been made to operate on an impersonation token by a thread that is not currently impersonating a client.")
   CASE_ERRCODE(ERROR_CANT_DISABLE_MANDATORY                            , "ERROR_CANT_DISABLE_MANDATORY"                            , "The group may not be disabled.")
   CASE_ERRCODE(ERROR_NO_LOGON_SERVERS                                  , "ERROR_NO_LOGON_SERVERS"                                  , "There are currently no logon servers available to service the logon request.")
   CASE_ERRCODE(ERROR_NO_SUCH_LOGON_SESSION                             , "ERROR_NO_SUCH_LOGON_SESSION"                             , "A specified logon session does not exist. It may already have been terminated.")
   CASE_ERRCODE(ERROR_NO_SUCH_PRIVILEGE                                 , "ERROR_NO_SUCH_PRIVILEGE"                                 , "A specified privilege does not exist.")
   CASE_ERRCODE(ERROR_PRIVILEGE_NOT_HELD                                , "ERROR_PRIVILEGE_NOT_HELD"                                , "A required privilege is not held by the client.")
   CASE_ERRCODE(ERROR_INVALID_ACCOUNT_NAME                              , "ERROR_INVALID_ACCOUNT_NAME"                              , "The name provided is not a properly formed account name.")
   CASE_ERRCODE(ERROR_USER_EXISTS                                       , "ERROR_USER_EXISTS"                                       , "The specified user already exists.")
   CASE_ERRCODE(ERROR_NO_SUCH_USER                                      , "ERROR_NO_SUCH_USER"                                      , "The specified user does not exist.")
   CASE_ERRCODE(ERROR_GROUP_EXISTS                                      , "ERROR_GROUP_EXISTS"                                      , "The specified group already exists.")
   CASE_ERRCODE(ERROR_NO_SUCH_GROUP                                     , "ERROR_NO_SUCH_GROUP"                                     , "The specified group does not exist.")
   CASE_ERRCODE(ERROR_MEMBER_IN_GROUP                                   , "ERROR_MEMBER_IN_GROUP"                                   , "Either the specified user account is already a member of the specified group, or the specified group cannot be deleted because it contains a member.")
   CASE_ERRCODE(ERROR_MEMBER_NOT_IN_GROUP                               , "ERROR_MEMBER_NOT_IN_GROUP"                               , "The specified user account is not a member of the specified group account.")
   CASE_ERRCODE(ERROR_LAST_ADMIN                                        , "ERROR_LAST_ADMIN"                                        , "The last remaining administration account cannot be disabled or deleted.")
   CASE_ERRCODE(ERROR_WRONG_PASSWORD                                    , "ERROR_WRONG_PASSWORD"                                    , "Unable to update the password. The value provided as the current password is incorrect.")
   CASE_ERRCODE(ERROR_ILL_FORMED_PASSWORD                               , "ERROR_ILL_FORMED_PASSWORD"                               , "Unable to update the password. The value provided for the new password contains values that are not allowed in passwords.")
   CASE_ERRCODE(ERROR_PASSWORD_RESTRICTION                              , "ERROR_PASSWORD_RESTRICTION"                              , "Unable to update the password. The value provided for the new password does not meet the length, complexity, or history requirement of the domain.")
   CASE_ERRCODE(ERROR_LOGON_FAILURE                                     , "ERROR_LOGON_FAILURE"                                     , "Logon failure: unknown user name or bad password.")
   CASE_ERRCODE(ERROR_ACCOUNT_RESTRICTION                               , "ERROR_ACCOUNT_RESTRICTION"                               , "Logon failure: user account restriction. Possible reasons are blank passwords not allowed, logon hour restrictions, or a policy restriction has been enforced.")
   CASE_ERRCODE(ERROR_INVALID_LOGON_HOURS                               , "ERROR_INVALID_LOGON_HOURS"                               , "Logon failure: account logon time restriction violation.")
   CASE_ERRCODE(ERROR_INVALID_WORKSTATION                               , "ERROR_INVALID_WORKSTATION"                               , "Logon failure: user not allowed to log on to this computer.")
   CASE_ERRCODE(ERROR_PASSWORD_EXPIRED                                  , "ERROR_PASSWORD_EXPIRED"                                  , "Logon failure: the specified account password has expired.")
   CASE_ERRCODE(ERROR_ACCOUNT_DISABLED                                  , "ERROR_ACCOUNT_DISABLED"                                  , "Logon failure: account currently disabled.")
   CASE_ERRCODE(ERROR_NONE_MAPPED                                       , "ERROR_NONE_MAPPED"                                       , "No mapping between account names and security IDs was done.")
   CASE_ERRCODE(ERROR_TOO_MANY_LUIDS_REQUESTED                          , "ERROR_TOO_MANY_LUIDS_REQUESTED"                          , "Too many local user identifiers (LUIDs) were requested at one time.")
   CASE_ERRCODE(ERROR_LUIDS_EXHAUSTED                                   , "ERROR_LUIDS_EXHAUSTED"                                   , "No more local user identifiers (LUIDs) are available.")
   CASE_ERRCODE(ERROR_INVALID_SUB_AUTHORITY                             , "ERROR_INVALID_SUB_AUTHORITY"                             , "The subauthority part of a security ID is invalid for this particular use.")
   CASE_ERRCODE(ERROR_INVALID_ACL                                       , "ERROR_INVALID_ACL"                                       , "The access control list (ACL) structure is invalid.")
   CASE_ERRCODE(ERROR_INVALID_SID                                       , "ERROR_INVALID_SID"                                       , "The security ID structure is invalid.")
   CASE_ERRCODE(ERROR_INVALID_SECURITY_DESCR                            , "ERROR_INVALID_SECURITY_DESCR"                            , "The security descriptor structure is invalid.")
   CASE_ERRCODE(ERROR_BAD_INHERITANCE_ACL                               , "ERROR_BAD_INHERITANCE_ACL"                               , "The inherited access control list (ACL) or access control entry (ACE) could not be built.")
   CASE_ERRCODE(ERROR_SERVER_DISABLED                                   , "ERROR_SERVER_DISABLED"                                   , "The server is currently disabled.")
   CASE_ERRCODE(ERROR_SERVER_NOT_DISABLED                               , "ERROR_SERVER_NOT_DISABLED"                               , "The server is currently enabled.")
   CASE_ERRCODE(ERROR_INVALID_ID_AUTHORITY                              , "ERROR_INVALID_ID_AUTHORITY"                              , "The value provided was an invalid value for an identifier authority.")
   CASE_ERRCODE(ERROR_ALLOTTED_SPACE_EXCEEDED                           , "ERROR_ALLOTTED_SPACE_EXCEEDED"                           , "No more memory is available for security information updates.")
   CASE_ERRCODE(ERROR_INVALID_GROUP_ATTRIBUTES                          , "ERROR_INVALID_GROUP_ATTRIBUTES"                          , "The specified attributes are invalid, or incompatible with the attributes for the group as a whole.")
   CASE_ERRCODE(ERROR_BAD_IMPERSONATION_LEVEL                           , "ERROR_BAD_IMPERSONATION_LEVEL"                           , "Either a required impersonation level was not provided, or the provided impersonation level is invalid.")
   CASE_ERRCODE(ERROR_CANT_OPEN_ANONYMOUS                               , "ERROR_CANT_OPEN_ANONYMOUS"                               , "Cannot open an anonymous level security token.")
   CASE_ERRCODE(ERROR_BAD_VALIDATION_CLASS                              , "ERROR_BAD_VALIDATION_CLASS"                              , "The validation information class requested was invalid.")
   CASE_ERRCODE(ERROR_BAD_TOKEN_TYPE                                    , "ERROR_BAD_TOKEN_TYPE"                                    , "The type of the token is inappropriate for its attempted use.")
   CASE_ERRCODE(ERROR_NO_SECURITY_ON_OBJECT                             , "ERROR_NO_SECURITY_ON_OBJECT"                             , "Unable to perform a security operation on an object that has no associated security.")
   CASE_ERRCODE(ERROR_CANT_ACCESS_DOMAIN_INFO                           , "ERROR_CANT_ACCESS_DOMAIN_INFO"                           , "Configuration information could not be read from the domain controller, either because the machine is unavailable, or access has been denied.")
   CASE_ERRCODE(ERROR_INVALID_SERVER_STATE                              , "ERROR_INVALID_SERVER_STATE"                              , "The security account manager (SAM) or local security authority (LSA) server was in the wrong state to perform the security operation.")
   CASE_ERRCODE(ERROR_INVALID_DOMAIN_STATE                              , "ERROR_INVALID_DOMAIN_STATE"                              , "The domain was in the wrong state to perform the security operation.")
   CASE_ERRCODE(ERROR_INVALID_DOMAIN_ROLE                               , "ERROR_INVALID_DOMAIN_ROLE"                               , "This operation is only allowed for the Primary Domain Controller of the domain.")
   CASE_ERRCODE(ERROR_NO_SUCH_DOMAIN                                    , "ERROR_NO_SUCH_DOMAIN"                                    , "The specified domain either does not exist or could not be contacted.")
   CASE_ERRCODE(ERROR_DOMAIN_EXISTS                                     , "ERROR_DOMAIN_EXISTS"                                     , "The specified domain already exists.")
   CASE_ERRCODE(ERROR_DOMAIN_LIMIT_EXCEEDED                             , "ERROR_DOMAIN_LIMIT_EXCEEDED"                             , "An attempt was made to exceed the limit on the number of domains per server.")
   CASE_ERRCODE(ERROR_INTERNAL_DB_CORRUPTION                            , "ERROR_INTERNAL_DB_CORRUPTION"                            , "Unable to complete the requested operation because of either a catastrophic media failure or a data structure corruption on the disk.")
   CASE_ERRCODE(ERROR_INTERNAL_ERROR                                    , "ERROR_INTERNAL_ERROR"                                    , "An internal error occurred.")
   CASE_ERRCODE(ERROR_GENERIC_NOT_MAPPED                                , "ERROR_GENERIC_NOT_MAPPED"                                , "Generic access types were contained in an access mask which should already be mapped to nongeneric types.")
   CASE_ERRCODE(ERROR_BAD_DESCRIPTOR_FORMAT                             , "ERROR_BAD_DESCRIPTOR_FORMAT"                             , "A security descriptor is not in the right format (absolute or self-relative).")
   CASE_ERRCODE(ERROR_NOT_LOGON_PROCESS                                 , "ERROR_NOT_LOGON_PROCESS"                                 , "The requested action is restricted for use by logon processes only. The calling process has not registered as a logon process.")
   CASE_ERRCODE(ERROR_LOGON_SESSION_EXISTS                              , "ERROR_LOGON_SESSION_EXISTS"                              , "Cannot start a new logon session with an ID that is already in use.")
   CASE_ERRCODE(ERROR_NO_SUCH_PACKAGE                                   , "ERROR_NO_SUCH_PACKAGE"                                   , "A specified authentication package is unknown.")
   CASE_ERRCODE(ERROR_BAD_LOGON_SESSION_STATE                           , "ERROR_BAD_LOGON_SESSION_STATE"                           , "The logon session is not in a state that is consistent with the requested operation.")
   CASE_ERRCODE(ERROR_LOGON_SESSION_COLLISION                           , "ERROR_LOGON_SESSION_COLLISION"                           , "The logon session ID is already in use.")
   CASE_ERRCODE(ERROR_INVALID_LOGON_TYPE                                , "ERROR_INVALID_LOGON_TYPE"                                , "A logon request contained an invalid logon type value.")
   CASE_ERRCODE(ERROR_CANNOT_IMPERSONATE                                , "ERROR_CANNOT_IMPERSONATE"                                , "Unable to impersonate using a named pipe until data has been read from that pipe.")
   CASE_ERRCODE(ERROR_RXACT_INVALID_STATE                               , "ERROR_RXACT_INVALID_STATE"                               , "The transaction state of a registry subtree is incompatible with the requested operation.")
   CASE_ERRCODE(ERROR_RXACT_COMMIT_FAILURE                              , "ERROR_RXACT_COMMIT_FAILURE"                              , "An internal security database corruption has been encountered.")
   CASE_ERRCODE(ERROR_SPECIAL_ACCOUNT                                   , "ERROR_SPECIAL_ACCOUNT"                                   , "Cannot perform this operation on built-in accounts.")
   CASE_ERRCODE(ERROR_SPECIAL_GROUP                                     , "ERROR_SPECIAL_GROUP"                                     , "Cannot perform this operation on this built-in special group.")
   CASE_ERRCODE(ERROR_SPECIAL_USER                                      , "ERROR_SPECIAL_USER"                                      , "Cannot perform this operation on this built-in special user.")
   CASE_ERRCODE(ERROR_MEMBERS_PRIMARY_GROUP                             , "ERROR_MEMBERS_PRIMARY_GROUP"                             , "The user cannot be removed from a group because the group is currently the user's primary group.")
   CASE_ERRCODE(ERROR_TOKEN_ALREADY_IN_USE                              , "ERROR_TOKEN_ALREADY_IN_USE"                              , "The token is already in use as a primary token.")
   CASE_ERRCODE(ERROR_NO_SUCH_ALIAS                                     , "ERROR_NO_SUCH_ALIAS"                                     , "The specified local group does not exist.")
   CASE_ERRCODE(ERROR_MEMBER_NOT_IN_ALIAS                               , "ERROR_MEMBER_NOT_IN_ALIAS"                               , "The specified account name is not a member of the local group.")
   CASE_ERRCODE(ERROR_MEMBER_IN_ALIAS                                   , "ERROR_MEMBER_IN_ALIAS"                                   , "The specified account name is already a member of the local group.")
   CASE_ERRCODE(ERROR_ALIAS_EXISTS                                      , "ERROR_ALIAS_EXISTS"                                      , "The specified local group already exists.")
   CASE_ERRCODE(ERROR_LOGON_NOT_GRANTED                                 , "ERROR_LOGON_NOT_GRANTED"                                 , "Logon failure: the user has not been granted the requested logon type at this computer.")
   CASE_ERRCODE(ERROR_TOO_MANY_SECRETS                                  , "ERROR_TOO_MANY_SECRETS"                                  , "The maximum number of secrets that may be stored in a single system has been exceeded.")
   CASE_ERRCODE(ERROR_SECRET_TOO_LONG                                   , "ERROR_SECRET_TOO_LONG"                                   , "The length of a secret exceeds the maximum length allowed.")
   CASE_ERRCODE(ERROR_INTERNAL_DB_ERROR                                 , "ERROR_INTERNAL_DB_ERROR"                                 , "The local security authority database contains an internal inconsistency.")
   CASE_ERRCODE(ERROR_TOO_MANY_CONTEXT_IDS                              , "ERROR_TOO_MANY_CONTEXT_IDS"                              , "During a logon attempt, the user's security context accumulated too many security IDs.")
   CASE_ERRCODE(ERROR_LOGON_TYPE_NOT_GRANTED                            , "ERROR_LOGON_TYPE_NOT_GRANTED"                            , "Logon failure: the user has not been granted the requested logon type at this computer.")
   CASE_ERRCODE(ERROR_NT_CROSS_ENCRYPTION_REQUIRED                      , "ERROR_NT_CROSS_ENCRYPTION_REQUIRED"                      , "A cross-encrypted password is necessary to change a user password.")
   CASE_ERRCODE(ERROR_NO_SUCH_MEMBER                                    , "ERROR_NO_SUCH_MEMBER"                                    , "A new member could not be added to or removed from the local group because the member does not exist.")
   CASE_ERRCODE(ERROR_INVALID_MEMBER                                    , "ERROR_INVALID_MEMBER"                                    , "A new member could not be added to a local group because the member has the wrong account type.")
   CASE_ERRCODE(ERROR_TOO_MANY_SIDS                                     , "ERROR_TOO_MANY_SIDS"                                     , "Too many security IDs have been specified.")
   CASE_ERRCODE(ERROR_LM_CROSS_ENCRYPTION_REQUIRED                      , "ERROR_LM_CROSS_ENCRYPTION_REQUIRED"                      , "A cross-encrypted password is necessary to change this user password.")
   CASE_ERRCODE(ERROR_NO_INHERITANCE                                    , "ERROR_NO_INHERITANCE"                                    , "Indicates an ACL contains no inheritable components.")
   CASE_ERRCODE(ERROR_FILE_CORRUPT                                      , "ERROR_FILE_CORRUPT"                                      , "The file or directory is corrupted and unreadable.")
   CASE_ERRCODE(ERROR_DISK_CORRUPT                                      , "ERROR_DISK_CORRUPT"                                      , "The disk structure is corrupted and unreadable.")
   CASE_ERRCODE(ERROR_NO_USER_SESSION_KEY                               , "ERROR_NO_USER_SESSION_KEY"                               , "There is no user session key for the specified logon session.")
   CASE_ERRCODE(ERROR_LICENSE_QUOTA_EXCEEDED                            , "ERROR_LICENSE_QUOTA_EXCEEDED"                            , "The service being accessed is licensed for a particular number of connections. No more connections can be made to the service at this time because there are already as many connections as the service can accept.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_WRONG_TARGET_NAME                                 , "ERROR_WRONG_TARGET_NAME"                                 , "Logon Failure: The target account name is incorrect.")
   CASE_ERRCODE(ERROR_MUTUAL_AUTH_FAILED                                , "ERROR_MUTUAL_AUTH_FAILED"                                , "Mutual Authentication failed. The server's password is out of date at the domain controller.")
   CASE_ERRCODE(ERROR_TIME_SKEW                                         , "ERROR_TIME_SKEW"                                         , "There is a time and/or date difference between the client and server.")
   CASE_ERRCODE(ERROR_CURRENT_DOMAIN_NOT_ALLOWED                        , "ERROR_CURRENT_DOMAIN_NOT_ALLOWED"                        , "This operation cannot be performed on the current domain.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_WINDOW_HANDLE                             , "ERROR_INVALID_WINDOW_HANDLE"                             , "Invalid window handle.")
   CASE_ERRCODE(ERROR_INVALID_MENU_HANDLE                               , "ERROR_INVALID_MENU_HANDLE"                               , "Invalid menu handle.")
   CASE_ERRCODE(ERROR_INVALID_CURSOR_HANDLE                             , "ERROR_INVALID_CURSOR_HANDLE"                             , "Invalid cursor handle.")
   CASE_ERRCODE(ERROR_INVALID_ACCEL_HANDLE                              , "ERROR_INVALID_ACCEL_HANDLE"                              , "Invalid accelerator table handle.")
   CASE_ERRCODE(ERROR_INVALID_HOOK_HANDLE                               , "ERROR_INVALID_HOOK_HANDLE"                               , "Invalid hook handle.")
   CASE_ERRCODE(ERROR_INVALID_DWP_HANDLE                                , "ERROR_INVALID_DWP_HANDLE"                                , "Invalid handle to a multiple-window position structure.")
   CASE_ERRCODE(ERROR_TLW_WITH_WSCHILD                                  , "ERROR_TLW_WITH_WSCHILD"                                  , "Cannot create a top-level child window.")
   CASE_ERRCODE(ERROR_CANNOT_FIND_WND_CLASS                             , "ERROR_CANNOT_FIND_WND_CLASS"                             , "Cannot find window class.")
   CASE_ERRCODE(ERROR_WINDOW_OF_OTHER_THREAD                            , "ERROR_WINDOW_OF_OTHER_THREAD"                            , "Invalid window; it belongs to other thread.")
   CASE_ERRCODE(ERROR_HOTKEY_ALREADY_REGISTERED                         , "ERROR_HOTKEY_ALREADY_REGISTERED"                         , "Hot key is already registered.")
   CASE_ERRCODE(ERROR_CLASS_ALREADY_EXISTS                              , "ERROR_CLASS_ALREADY_EXISTS"                              , "Class already exists.")
   CASE_ERRCODE(ERROR_CLASS_DOES_NOT_EXIST                              , "ERROR_CLASS_DOES_NOT_EXIST"                              , "Class does not exist.")
   CASE_ERRCODE(ERROR_CLASS_HAS_WINDOWS                                 , "ERROR_CLASS_HAS_WINDOWS"                                 , "Class still has open windows.")
   CASE_ERRCODE(ERROR_INVALID_INDEX                                     , "ERROR_INVALID_INDEX"                                     , "Invalid index.")
   CASE_ERRCODE(ERROR_INVALID_ICON_HANDLE                               , "ERROR_INVALID_ICON_HANDLE"                               , "Invalid icon handle.")
   CASE_ERRCODE(ERROR_PRIVATE_DIALOG_INDEX                              , "ERROR_PRIVATE_DIALOG_INDEX"                              , "Using private DIALOG window words.")
   CASE_ERRCODE(ERROR_LISTBOX_ID_NOT_FOUND                              , "ERROR_LISTBOX_ID_NOT_FOUND"                              , "The list box identifier was not found.")
   CASE_ERRCODE(ERROR_NO_WILDCARD_CHARACTERS                            , "ERROR_NO_WILDCARD_CHARACTERS"                            , "No wildcards were found.")
   CASE_ERRCODE(ERROR_CLIPBOARD_NOT_OPEN                                , "ERROR_CLIPBOARD_NOT_OPEN"                                , "Thread does not have a clipboard open.")
   CASE_ERRCODE(ERROR_HOTKEY_NOT_REGISTERED                             , "ERROR_HOTKEY_NOT_REGISTERED"                             , "Hot key is not registered.")
   CASE_ERRCODE(ERROR_WINDOW_NOT_DIALOG                                 , "ERROR_WINDOW_NOT_DIALOG"                                 , "The window is not a valid dialog window.")
   CASE_ERRCODE(ERROR_CONTROL_ID_NOT_FOUND                              , "ERROR_CONTROL_ID_NOT_FOUND"                              , "Control ID not found.")
   CASE_ERRCODE(ERROR_INVALID_COMBOBOX_MESSAGE                          , "ERROR_INVALID_COMBOBOX_MESSAGE"                          , "Invalid message for a combo box because it does not have an edit control.")
   CASE_ERRCODE(ERROR_WINDOW_NOT_COMBOBOX                               , "ERROR_WINDOW_NOT_COMBOBOX"                               , "The window is not a combo box.")
   CASE_ERRCODE(ERROR_INVALID_EDIT_HEIGHT                               , "ERROR_INVALID_EDIT_HEIGHT"                               , "Height must be less than 256.")
   CASE_ERRCODE(ERROR_DC_NOT_FOUND                                      , "ERROR_DC_NOT_FOUND"                                      , "Invalid device context (DC) handle.")
   CASE_ERRCODE(ERROR_INVALID_HOOK_FILTER                               , "ERROR_INVALID_HOOK_FILTER"                               , "Invalid hook procedure type.")
   CASE_ERRCODE(ERROR_INVALID_FILTER_PROC                               , "ERROR_INVALID_FILTER_PROC"                               , "Invalid hook procedure.")
   CASE_ERRCODE(ERROR_HOOK_NEEDS_HMOD                                   , "ERROR_HOOK_NEEDS_HMOD"                                   , "Cannot set nonlocal hook without a module handle.")
   CASE_ERRCODE(ERROR_GLOBAL_ONLY_HOOK                                  , "ERROR_GLOBAL_ONLY_HOOK"                                  , "This hook procedure can only be set globally.")
   CASE_ERRCODE(ERROR_JOURNAL_HOOK_SET                                  , "ERROR_JOURNAL_HOOK_SET"                                  , "The journal hook procedure is already installed.")
   CASE_ERRCODE(ERROR_HOOK_NOT_INSTALLED                                , "ERROR_HOOK_NOT_INSTALLED"                                , "The hook procedure is not installed.")
   CASE_ERRCODE(ERROR_INVALID_LB_MESSAGE                                , "ERROR_INVALID_LB_MESSAGE"                                , "Invalid message for single-selection list box.")
   CASE_ERRCODE(ERROR_SETCOUNT_ON_BAD_LB                                , "ERROR_SETCOUNT_ON_BAD_LB"                                , "LB_SETCOUNT sent to non-lazy list box.")
   CASE_ERRCODE(ERROR_LB_WITHOUT_TABSTOPS                               , "ERROR_LB_WITHOUT_TABSTOPS"                               , "This list box does not support tab stops.")
   CASE_ERRCODE(ERROR_DESTROY_OBJECT_OF_OTHER_THREAD                    , "ERROR_DESTROY_OBJECT_OF_OTHER_THREAD"                    , "Cannot destroy object created by another thread.")
   CASE_ERRCODE(ERROR_CHILD_WINDOW_MENU                                 , "ERROR_CHILD_WINDOW_MENU"                                 , "Child windows cannot have menus.")
   CASE_ERRCODE(ERROR_NO_SYSTEM_MENU                                    , "ERROR_NO_SYSTEM_MENU"                                    , "The window does not have a system menu.")
   CASE_ERRCODE(ERROR_INVALID_MSGBOX_STYLE                              , "ERROR_INVALID_MSGBOX_STYLE"                              , "Invalid message box style.")
   CASE_ERRCODE(ERROR_INVALID_SPI_VALUE                                 , "ERROR_INVALID_SPI_VALUE"                                 , "Invalid system-wide (SPI_*) parameter.")
   CASE_ERRCODE(ERROR_SCREEN_ALREADY_LOCKED                             , "ERROR_SCREEN_ALREADY_LOCKED"                             , "Screen already locked.")
   CASE_ERRCODE(ERROR_HWNDS_HAVE_DIFF_PARENT                            , "ERROR_HWNDS_HAVE_DIFF_PARENT"                            , "All handles to windows in a multiple-window position structure must have the same parent.")
   CASE_ERRCODE(ERROR_NOT_CHILD_WINDOW                                  , "ERROR_NOT_CHILD_WINDOW"                                  , "The window is not a child window.")
   CASE_ERRCODE(ERROR_INVALID_GW_COMMAND                                , "ERROR_INVALID_GW_COMMAND"                                , "Invalid GW_* command.")
   CASE_ERRCODE(ERROR_INVALID_THREAD_ID                                 , "ERROR_INVALID_THREAD_ID"                                 , "Invalid thread identifier.")
   CASE_ERRCODE(ERROR_NON_MDICHILD_WINDOW                               , "ERROR_NON_MDICHILD_WINDOW"                               , "Cannot process a message from a window that is not a multiple document interface (MDI) window.")
   CASE_ERRCODE(ERROR_POPUP_ALREADY_ACTIVE                              , "ERROR_POPUP_ALREADY_ACTIVE"                              , "Popup menu already active.")
   CASE_ERRCODE(ERROR_NO_SCROLLBARS                                     , "ERROR_NO_SCROLLBARS"                                     , "The window does not have scroll bars.")
   CASE_ERRCODE(ERROR_INVALID_SCROLLBAR_RANGE                           , "ERROR_INVALID_SCROLLBAR_RANGE"                           , "Scroll bar range cannot be greater than MAXLONG.")
   CASE_ERRCODE(ERROR_INVALID_SHOWWIN_COMMAND                           , "ERROR_INVALID_SHOWWIN_COMMAND"                           , "Cannot show or remove the window in the way specified.")
   CASE_ERRCODE(ERROR_NO_SYSTEM_RESOURCES                               , "ERROR_NO_SYSTEM_RESOURCES"                               , "Insufficient system resources exist to complete the requested service.")
   CASE_ERRCODE(ERROR_NONPAGED_SYSTEM_RESOURCES                         , "ERROR_NONPAGED_SYSTEM_RESOURCES"                         , "Insufficient system resources exist to complete the requested service.")
   CASE_ERRCODE(ERROR_PAGED_SYSTEM_RESOURCES                            , "ERROR_PAGED_SYSTEM_RESOURCES"                            , "Insufficient system resources exist to complete the requested service.")
   CASE_ERRCODE(ERROR_WORKING_SET_QUOTA                                 , "ERROR_WORKING_SET_QUOTA"                                 , "Insufficient quota to complete the requested service.")
   CASE_ERRCODE(ERROR_PAGEFILE_QUOTA                                    , "ERROR_PAGEFILE_QUOTA"                                    , "Insufficient quota to complete the requested service.")
   CASE_ERRCODE(ERROR_COMMITMENT_LIMIT                                  , "ERROR_COMMITMENT_LIMIT"                                  , "The paging file is too small for this operation to complete.")
   CASE_ERRCODE(ERROR_MENU_ITEM_NOT_FOUND                               , "ERROR_MENU_ITEM_NOT_FOUND"                               , "A menu item was not found.")
   CASE_ERRCODE(ERROR_INVALID_KEYBOARD_HANDLE                           , "ERROR_INVALID_KEYBOARD_HANDLE"                           , "Invalid keyboard layout handle.")
   CASE_ERRCODE(ERROR_HOOK_TYPE_NOT_ALLOWED                             , "ERROR_HOOK_TYPE_NOT_ALLOWED"                             , "Hook type not allowed.")
   CASE_ERRCODE(ERROR_REQUIRES_INTERACTIVE_WINDOWSTATION                , "ERROR_REQUIRES_INTERACTIVE_WINDOWSTATION"                , "This operation requires an interactive window station.")
   CASE_ERRCODE(ERROR_TIMEOUT                                           , "ERROR_TIMEOUT"                                           , "This operation returned because the timeout period expired.")
   CASE_ERRCODE(ERROR_INVALID_MONITOR_HANDLE                            , "ERROR_INVALID_MONITOR_HANDLE"                            , "Invalid monitor handle.")
   CASE_ERRCODE(ERROR_EVENTLOG_FILE_CORRUPT                             , "ERROR_EVENTLOG_FILE_CORRUPT"                             , "The event log file is corrupted.")
   CASE_ERRCODE(ERROR_EVENTLOG_CANT_START                               , "ERROR_EVENTLOG_CANT_START"                               , "No event log file could be opened, so the event logging service did not start.")
   CASE_ERRCODE(ERROR_LOG_FILE_FULL                                     , "ERROR_LOG_FILE_FULL"                                     , "The event log file is full.")
   CASE_ERRCODE(ERROR_EVENTLOG_FILE_CHANGED                             , "ERROR_EVENTLOG_FILE_CHANGED"                             , "The event log file has changed between read operations.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INSTALL_SERVICE_FAILURE                           , "ERROR_INSTALL_SERVICE_FAILURE"                           , "The Windows Installer service could not be accessed. This can occur if you are running Windows in safe mode, or if the Windows Installer is not correctly installed. Contact your support personnel for assistance.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INSTALL_USEREXIT                                  , "ERROR_INSTALL_USEREXIT"                                  , "User cancelled installation.")
   CASE_ERRCODE(ERROR_INSTALL_FAILURE                                   , "ERROR_INSTALL_FAILURE"                                   , "Fatal error during installation.")
   CASE_ERRCODE(ERROR_INSTALL_SUSPEND                                   , "ERROR_INSTALL_SUSPEND"                                   , "Installation suspended, incomplete.")
   CASE_ERRCODE(ERROR_UNKNOWN_PRODUCT                                   , "ERROR_UNKNOWN_PRODUCT"                                   , "This action is only valid for products that are currently installed.")
   CASE_ERRCODE(ERROR_UNKNOWN_FEATURE                                   , "ERROR_UNKNOWN_FEATURE"                                   , "Feature ID not registered.")
   CASE_ERRCODE(ERROR_UNKNOWN_COMPONENT                                 , "ERROR_UNKNOWN_COMPONENT"                                 , "Component ID not registered.")
   CASE_ERRCODE(ERROR_UNKNOWN_PROPERTY                                  , "ERROR_UNKNOWN_PROPERTY"                                  , "Unknown property.")
   CASE_ERRCODE(ERROR_INVALID_HANDLE_STATE                              , "ERROR_INVALID_HANDLE_STATE"                              , "Handle is in an invalid state.")
   CASE_ERRCODE(ERROR_BAD_CONFIGURATION                                 , "ERROR_BAD_CONFIGURATION"                                 , "The configuration data for this product is corrupt. Contact your support personnel.")
   CASE_ERRCODE(ERROR_INDEX_ABSENT                                      , "ERROR_INDEX_ABSENT"                                      , "Component qualifier not present.")
   CASE_ERRCODE(ERROR_INSTALL_SOURCE_ABSENT                             , "ERROR_INSTALL_SOURCE_ABSENT"                             , "The installation source for this product is not available. Verify that the source exists and that you can access it.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INSTALL_PACKAGE_VERSION                           , "ERROR_INSTALL_PACKAGE_VERSION"                           , "This installation package cannot be installed by the Windows Installer service. You must install a Windows service pack that contains a newer version of the Windows Installer service.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_PRODUCT_UNINSTALLED                               , "ERROR_PRODUCT_UNINSTALLED"                               , "Product is uninstalled.")
   CASE_ERRCODE(ERROR_BAD_QUERY_SYNTAX                                  , "ERROR_BAD_QUERY_SYNTAX"                                  , "SQL query syntax invalid or unsupported.")
   CASE_ERRCODE(ERROR_INVALID_FIELD                                     , "ERROR_INVALID_FIELD"                                     , "Record field does not exist.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_DEVICE_REMOVED                                    , "ERROR_DEVICE_REMOVED"                                    , "The device has been removed.")
   CASE_ERRCODE(ERROR_INSTALL_ALREADY_RUNNING                           , "ERROR_INSTALL_ALREADY_RUNNING"                           , "Another installation is already in progress. Complete that installation before proceeding with this install.")
   CASE_ERRCODE(ERROR_INSTALL_PACKAGE_OPEN_FAILED                       , "ERROR_INSTALL_PACKAGE_OPEN_FAILED"                       , "This installation package could not be opened. Verify that the package exists and that you can access it, or contact the application vendor to verify that this is a valid Windows Installer package.")
   CASE_ERRCODE(ERROR_INSTALL_PACKAGE_INVALID                           , "ERROR_INSTALL_PACKAGE_INVALID"                           , "This installation package could not be opened. Contact the application vendor to verify that this is a valid Windows Installer package.")
   CASE_ERRCODE(ERROR_INSTALL_UI_FAILURE                                , "ERROR_INSTALL_UI_FAILURE"                                , "There was an error starting the Windows Installer service user interface. Contact your support personnel.")
   CASE_ERRCODE(ERROR_INSTALL_LOG_FAILURE                               , "ERROR_INSTALL_LOG_FAILURE"                               , "Error opening installation log file. Verify that the specified log file location exists and that you can write to it.")
   CASE_ERRCODE(ERROR_INSTALL_LANGUAGE_UNSUPPORTED                      , "ERROR_INSTALL_LANGUAGE_UNSUPPORTED"                      , "The language of this installation package is not supported by your system.")
   CASE_ERRCODE(ERROR_INSTALL_TRANSFORM_FAILURE                         , "ERROR_INSTALL_TRANSFORM_FAILURE"                         , "Error applying transforms. Verify that the specified transform paths are valid.")
   CASE_ERRCODE(ERROR_INSTALL_PACKAGE_REJECTED                          , "ERROR_INSTALL_PACKAGE_REJECTED"                          , "This installation is forbidden by system policy. Contact your system administrator.")
   CASE_ERRCODE(ERROR_FUNCTION_NOT_CALLED                               , "ERROR_FUNCTION_NOT_CALLED"                               , "Function could not be executed.")
   CASE_ERRCODE(ERROR_FUNCTION_FAILED                                   , "ERROR_FUNCTION_FAILED"                                   , "Function failed during execution.")
   CASE_ERRCODE(ERROR_INVALID_TABLE                                     , "ERROR_INVALID_TABLE"                                     , "Invalid or unknown table specified.")
   CASE_ERRCODE(ERROR_DATATYPE_MISMATCH                                 , "ERROR_DATATYPE_MISMATCH"                                 , "Data supplied is of wrong type.")
   CASE_ERRCODE(ERROR_UNSUPPORTED_TYPE                                  , "ERROR_UNSUPPORTED_TYPE"                                  , "Data of this type is not supported.")
   CASE_ERRCODE(ERROR_CREATE_FAILED                                     , "ERROR_CREATE_FAILED"                                     , "The Windows Installer service failed to start. Contact your support personnel.")
   CASE_ERRCODE(ERROR_INSTALL_TEMP_UNWRITABLE                           , "ERROR_INSTALL_TEMP_UNWRITABLE"                           , "The Temp folder is on a drive that is full or inaccessible. Free up space on the drive or verify that you have write permission on the Temp folder.")
   CASE_ERRCODE(ERROR_INSTALL_PLATFORM_UNSUPPORTED                      , "ERROR_INSTALL_PLATFORM_UNSUPPORTED"                      , "This installation package is not supported by this processor type. Contact your product vendor.")
   CASE_ERRCODE(ERROR_INSTALL_NOTUSED                                   , "ERROR_INSTALL_NOTUSED"                                   , "Component not used on this computer.")
   CASE_ERRCODE(ERROR_PATCH_PACKAGE_OPEN_FAILED                         , "ERROR_PATCH_PACKAGE_OPEN_FAILED"                         , "This patch package could not be opened. Verify that the patch package exists and that you can access it, or contact the application vendor to verify that this is a valid Windows Installer patch package.")
   CASE_ERRCODE(ERROR_PATCH_PACKAGE_INVALID                             , "ERROR_PATCH_PACKAGE_INVALID"                             , "This patch package could not be opened. Contact the application vendor to verify that this is a valid Windows Installer patch package.")
   CASE_ERRCODE(ERROR_PATCH_PACKAGE_UNSUPPORTED                         , "ERROR_PATCH_PACKAGE_UNSUPPORTED"                         , "This patch package cannot be processed by the Windows Installer service. You must install a Windows service pack that contains a newer version of the Windows Installer service.")
   CASE_ERRCODE(ERROR_PRODUCT_VERSION                                   , "ERROR_PRODUCT_VERSION"                                   , "Another version of this product is already installed. Installation of this version cannot continue. To configure or remove the existing version of this product, use Add/Remove Programs on the Control Panel.")
   CASE_ERRCODE(ERROR_INVALID_COMMAND_LINE                              , "ERROR_INVALID_COMMAND_LINE"                              , "Invalid command line argument. Consult the Windows Installer SDK for detailed command line help.")
   CASE_ERRCODE(ERROR_INSTALL_REMOTE_DISALLOWED                         , "ERROR_INSTALL_REMOTE_DISALLOWED"                         , "Only administrators have permission to add, remove, or configure server software during a Terminal Services remote session. If you want to install or configure software on the server, contact your network administrator.")
   CASE_ERRCODE(ERROR_SUCCESS_REBOOT_INITIATED                          , "ERROR_SUCCESS_REBOOT_INITIATED"                          , "The requested operation completed successfully. The system will be restarted so the changes can take effect.")
   CASE_ERRCODE(ERROR_PATCH_TARGET_NOT_FOUND                            , "ERROR_PATCH_TARGET_NOT_FOUND"                            , "The upgrade patch cannot be installed by the Windows Installer service because the program to be upgraded may be missing, or the upgrade patch may update a different version of the program. Verify that the program to be upgraded exists on your computer and that you have the correct upgrade patch.")
   CASE_ERRCODE(ERROR_PATCH_PACKAGE_REJECTED                            , "ERROR_PATCH_PACKAGE_REJECTED"                            , "The patch package is not permitted by software restriction policy.")
   CASE_ERRCODE(ERROR_INSTALL_TRANSFORM_REJECTED                        , "ERROR_INSTALL_TRANSFORM_REJECTED"                        , "One or more customizations are not permitted by software restriction policy.")
   CASE_ERRCODE(ERROR_INSTALL_REMOTE_PROHIBITED                         , "ERROR_INSTALL_REMOTE_PROHIBITED"                         , "The Windows Installer does not permit installation from a Remote Desktop Connection.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(RPC_S_INVALID_STRING_BINDING                            , "RPC_S_INVALID_STRING_BINDING"                            , "The string binding is invalid.")
   CASE_ERRCODE(RPC_S_WRONG_KIND_OF_BINDING                             , "RPC_S_WRONG_KIND_OF_BINDING"                             , "The binding handle is not the correct type.")
   CASE_ERRCODE(RPC_S_INVALID_BINDING                                   , "RPC_S_INVALID_BINDING"                                   , "The binding handle is invalid.")
   CASE_ERRCODE(RPC_S_PROTSEQ_NOT_SUPPORTED                             , "RPC_S_PROTSEQ_NOT_SUPPORTED"                             , "The RPC protocol sequence is not supported.")
   CASE_ERRCODE(RPC_S_INVALID_RPC_PROTSEQ                               , "RPC_S_INVALID_RPC_PROTSEQ"                               , "The RPC protocol sequence is invalid.")
   CASE_ERRCODE(RPC_S_INVALID_STRING_UUID                               , "RPC_S_INVALID_STRING_UUID"                               , "The string universal unique identifier (UUID) is invalid.")
   CASE_ERRCODE(RPC_S_INVALID_ENDPOINT_FORMAT                           , "RPC_S_INVALID_ENDPOINT_FORMAT"                           , "The endpoint format is invalid.")
   CASE_ERRCODE(RPC_S_INVALID_NET_ADDR                                  , "RPC_S_INVALID_NET_ADDR"                                  , "The network address is invalid.")
   CASE_ERRCODE(RPC_S_NO_ENDPOINT_FOUND                                 , "RPC_S_NO_ENDPOINT_FOUND"                                 , "No endpoint was found.")
   CASE_ERRCODE(RPC_S_INVALID_TIMEOUT                                   , "RPC_S_INVALID_TIMEOUT"                                   , "The timeout value is invalid.")
   CASE_ERRCODE(RPC_S_OBJECT_NOT_FOUND                                  , "RPC_S_OBJECT_NOT_FOUND"                                  , "The object universal unique identifier (UUID) was not found.")
   CASE_ERRCODE(RPC_S_ALREADY_REGISTERED                                , "RPC_S_ALREADY_REGISTERED"                                , "The object universal unique identifier (UUID) has already been registered.")
   CASE_ERRCODE(RPC_S_TYPE_ALREADY_REGISTERED                           , "RPC_S_TYPE_ALREADY_REGISTERED"                           , "The type universal unique identifier (UUID) has already been registered.")
   CASE_ERRCODE(RPC_S_ALREADY_LISTENING                                 , "RPC_S_ALREADY_LISTENING"                                 , "The RPC server is already listening.")
   CASE_ERRCODE(RPC_S_NO_PROTSEQS_REGISTERED                            , "RPC_S_NO_PROTSEQS_REGISTERED"                            , "No protocol sequences have been registered.")
   CASE_ERRCODE(RPC_S_NOT_LISTENING                                     , "RPC_S_NOT_LISTENING"                                     , "The RPC server is not listening.")
   CASE_ERRCODE(RPC_S_UNKNOWN_MGR_TYPE                                  , "RPC_S_UNKNOWN_MGR_TYPE"                                  , "The manager type is unknown.")
   CASE_ERRCODE(RPC_S_UNKNOWN_IF                                        , "RPC_S_UNKNOWN_IF"                                        , "The interface is unknown.")
   CASE_ERRCODE(RPC_S_NO_BINDINGS                                       , "RPC_S_NO_BINDINGS"                                       , "There are no bindings.")
   CASE_ERRCODE(RPC_S_NO_PROTSEQS                                       , "RPC_S_NO_PROTSEQS"                                       , "There are no protocol sequences.")
   CASE_ERRCODE(RPC_S_CANT_CREATE_ENDPOINT                              , "RPC_S_CANT_CREATE_ENDPOINT"                              , "The endpoint cannot be created.")
   CASE_ERRCODE(RPC_S_OUT_OF_RESOURCES                                  , "RPC_S_OUT_OF_RESOURCES"                                  , "Not enough resources are available to complete this operation.")
   CASE_ERRCODE(RPC_S_SERVER_UNAVAILABLE                                , "RPC_S_SERVER_UNAVAILABLE"                                , "The RPC server is unavailable.")
   CASE_ERRCODE(RPC_S_SERVER_TOO_BUSY                                   , "RPC_S_SERVER_TOO_BUSY"                                   , "The RPC server is too busy to complete this operation.")
   CASE_ERRCODE(RPC_S_INVALID_NETWORK_OPTIONS                           , "RPC_S_INVALID_NETWORK_OPTIONS"                           , "The network options are invalid.")
   CASE_ERRCODE(RPC_S_NO_CALL_ACTIVE                                    , "RPC_S_NO_CALL_ACTIVE"                                    , "There are no remote procedure calls active on this thread.")
   CASE_ERRCODE(RPC_S_CALL_FAILED                                       , "RPC_S_CALL_FAILED"                                       , "The remote procedure call failed.")
   CASE_ERRCODE(RPC_S_CALL_FAILED_DNE                                   , "RPC_S_CALL_FAILED_DNE"                                   , "The remote procedure call failed and did not execute.")
   CASE_ERRCODE(RPC_S_PROTOCOL_ERROR                                    , "RPC_S_PROTOCOL_ERROR"                                    , "A remote procedure call (RPC) protocol error occurred.")
   CASE_ERRCODE(RPC_S_UNSUPPORTED_TRANS_SYN                             , "RPC_S_UNSUPPORTED_TRANS_SYN"                             , "The transfer syntax is not supported by the RPC server.")
   CASE_ERRCODE(RPC_S_UNSUPPORTED_TYPE                                  , "RPC_S_UNSUPPORTED_TYPE"                                  , "The universal unique identifier (UUID) type is not supported.")
   CASE_ERRCODE(RPC_S_INVALID_TAG                                       , "RPC_S_INVALID_TAG"                                       , "The tag is invalid.")
   CASE_ERRCODE(RPC_S_INVALID_BOUND                                     , "RPC_S_INVALID_BOUND"                                     , "The array bounds are invalid.")
   CASE_ERRCODE(RPC_S_NO_ENTRY_NAME                                     , "RPC_S_NO_ENTRY_NAME"                                     , "The binding does not contain an entry name.")
   CASE_ERRCODE(RPC_S_INVALID_NAME_SYNTAX                               , "RPC_S_INVALID_NAME_SYNTAX"                               , "The name syntax is invalid.")
   CASE_ERRCODE(RPC_S_UNSUPPORTED_NAME_SYNTAX                           , "RPC_S_UNSUPPORTED_NAME_SYNTAX"                           , "The name syntax is not supported.")
   CASE_ERRCODE(RPC_S_UUID_NO_ADDRESS                                   , "RPC_S_UUID_NO_ADDRESS"                                   , "No network address is available to use to construct a universal unique identifier (UUID).")
   CASE_ERRCODE(RPC_S_DUPLICATE_ENDPOINT                                , "RPC_S_DUPLICATE_ENDPOINT"                                , "The endpoint is a duplicate.")
   CASE_ERRCODE(RPC_S_UNKNOWN_AUTHN_TYPE                                , "RPC_S_UNKNOWN_AUTHN_TYPE"                                , "The authentication type is unknown.")
   CASE_ERRCODE(RPC_S_MAX_CALLS_TOO_SMALL                               , "RPC_S_MAX_CALLS_TOO_SMALL"                               , "The maximum number of calls is too small.")
   CASE_ERRCODE(RPC_S_STRING_TOO_LONG                                   , "RPC_S_STRING_TOO_LONG"                                   , "The string is too long.")
   CASE_ERRCODE(RPC_S_PROTSEQ_NOT_FOUND                                 , "RPC_S_PROTSEQ_NOT_FOUND"                                 , "The RPC protocol sequence was not found.")
   CASE_ERRCODE(RPC_S_PROCNUM_OUT_OF_RANGE                              , "RPC_S_PROCNUM_OUT_OF_RANGE"                              , "The procedure number is out of range.")
   CASE_ERRCODE(RPC_S_BINDING_HAS_NO_AUTH                               , "RPC_S_BINDING_HAS_NO_AUTH"                               , "The binding does not contain any authentication information.")
   CASE_ERRCODE(RPC_S_UNKNOWN_AUTHN_SERVICE                             , "RPC_S_UNKNOWN_AUTHN_SERVICE"                             , "The authentication service is unknown.")
   CASE_ERRCODE(RPC_S_UNKNOWN_AUTHN_LEVEL                               , "RPC_S_UNKNOWN_AUTHN_LEVEL"                               , "The authentication level is unknown.")
   CASE_ERRCODE(RPC_S_INVALID_AUTH_IDENTITY                             , "RPC_S_INVALID_AUTH_IDENTITY"                             , "The security context is invalid.")
   CASE_ERRCODE(RPC_S_UNKNOWN_AUTHZ_SERVICE                             , "RPC_S_UNKNOWN_AUTHZ_SERVICE"                             , "The authorization service is unknown.")
   CASE_ERRCODE(EPT_S_INVALID_ENTRY                                     , "EPT_S_INVALID_ENTRY"                                     , "The entry is invalid.")
   CASE_ERRCODE(EPT_S_CANT_PERFORM_OP                                   , "EPT_S_CANT_PERFORM_OP"                                   , "The server endpoint cannot perform the operation.")
   CASE_ERRCODE(EPT_S_NOT_REGISTERED                                    , "EPT_S_NOT_REGISTERED"                                    , "There are no more endpoints available from the endpoint mapper.")
   CASE_ERRCODE(RPC_S_NOTHING_TO_EXPORT                                 , "RPC_S_NOTHING_TO_EXPORT"                                 , "No interfaces have been exported.")
   CASE_ERRCODE(RPC_S_INCOMPLETE_NAME                                   , "RPC_S_INCOMPLETE_NAME"                                   , "The entry name is incomplete.")
   CASE_ERRCODE(RPC_S_INVALID_VERS_OPTION                               , "RPC_S_INVALID_VERS_OPTION"                               , "The version option is invalid.")
   CASE_ERRCODE(RPC_S_NO_MORE_MEMBERS                                   , "RPC_S_NO_MORE_MEMBERS"                                   , "There are no more members.")
   CASE_ERRCODE(RPC_S_NOT_ALL_OBJS_UNEXPORTED                           , "RPC_S_NOT_ALL_OBJS_UNEXPORTED"                           , "There is nothing to unexport.")
   CASE_ERRCODE(RPC_S_INTERFACE_NOT_FOUND                               , "RPC_S_INTERFACE_NOT_FOUND"                               , "The interface was not found.")
   CASE_ERRCODE(RPC_S_ENTRY_ALREADY_EXISTS                              , "RPC_S_ENTRY_ALREADY_EXISTS"                              , "The entry already exists.")
   CASE_ERRCODE(RPC_S_ENTRY_NOT_FOUND                                   , "RPC_S_ENTRY_NOT_FOUND"                                   , "The entry is not found.")
   CASE_ERRCODE(RPC_S_NAME_SERVICE_UNAVAILABLE                          , "RPC_S_NAME_SERVICE_UNAVAILABLE"                          , "The name service is unavailable.")
   CASE_ERRCODE(RPC_S_INVALID_NAF_ID                                    , "RPC_S_INVALID_NAF_ID"                                    , "The network address family is invalid.")
   CASE_ERRCODE(RPC_S_CANNOT_SUPPORT                                    , "RPC_S_CANNOT_SUPPORT"                                    , "The requested operation is not supported.")
   CASE_ERRCODE(RPC_S_NO_CONTEXT_AVAILABLE                              , "RPC_S_NO_CONTEXT_AVAILABLE"                              , "No security context is available to allow impersonation.")
   CASE_ERRCODE(RPC_S_INTERNAL_ERROR                                    , "RPC_S_INTERNAL_ERROR"                                    , "An internal error occurred in a remote procedure call (RPC).")
   CASE_ERRCODE(RPC_S_ZERO_DIVIDE                                       , "RPC_S_ZERO_DIVIDE"                                       , "The RPC server attempted an integer division by zero.")
   CASE_ERRCODE(RPC_S_ADDRESS_ERROR                                     , "RPC_S_ADDRESS_ERROR"                                     , "An addressing error occurred in the RPC server.")
   CASE_ERRCODE(RPC_S_FP_DIV_ZERO                                       , "RPC_S_FP_DIV_ZERO"                                       , "A floating-point operation at the RPC server caused a division by zero.")
   CASE_ERRCODE(RPC_S_FP_UNDERFLOW                                      , "RPC_S_FP_UNDERFLOW"                                      , "A floating-point underflow occurred at the RPC server.")
   CASE_ERRCODE(RPC_S_FP_OVERFLOW                                       , "RPC_S_FP_OVERFLOW"                                       , "A floating-point overflow occurred at the RPC server.")
   CASE_ERRCODE(RPC_X_NO_MORE_ENTRIES                                   , "RPC_X_NO_MORE_ENTRIES"                                   , "The list of RPC servers available for the binding of auto handles has been exhausted.")
   CASE_ERRCODE(RPC_X_SS_CHAR_TRANS_OPEN_FAIL                           , "RPC_X_SS_CHAR_TRANS_OPEN_FAIL"                           , "Unable to open the character translation table file.")
   CASE_ERRCODE(RPC_X_SS_CHAR_TRANS_SHORT_FILE                          , "RPC_X_SS_CHAR_TRANS_SHORT_FILE"                          , "The file containing the character translation table has fewer than 512 bytes.")
   CASE_ERRCODE(RPC_X_SS_IN_NULL_CONTEXT                                , "RPC_X_SS_IN_NULL_CONTEXT"                                , "A null context handle was passed from the client to the host during a remote procedure call.")
   CASE_ERRCODE(RPC_X_SS_CONTEXT_DAMAGED                                , "RPC_X_SS_CONTEXT_DAMAGED"                                , "The context handle changed during a remote procedure call.")
   CASE_ERRCODE(RPC_X_SS_HANDLES_MISMATCH                               , "RPC_X_SS_HANDLES_MISMATCH"                               , "The binding handles passed to a remote procedure call do not match.")
   CASE_ERRCODE(RPC_X_SS_CANNOT_GET_CALL_HANDLE                         , "RPC_X_SS_CANNOT_GET_CALL_HANDLE"                         , "The stub is unable to get the remote procedure call handle.")
   CASE_ERRCODE(RPC_X_NULL_REF_POINTER                                  , "RPC_X_NULL_REF_POINTER"                                  , "A null reference pointer was passed to the stub.")
   CASE_ERRCODE(RPC_X_ENUM_VALUE_OUT_OF_RANGE                           , "RPC_X_ENUM_VALUE_OUT_OF_RANGE"                           , "The enumeration value is out of range.")
   CASE_ERRCODE(RPC_X_BYTE_COUNT_TOO_SMALL                              , "RPC_X_BYTE_COUNT_TOO_SMALL"                              , "The byte count is too small.")
   CASE_ERRCODE(RPC_X_BAD_STUB_DATA                                     , "RPC_X_BAD_STUB_DATA"                                     , "The stub received bad data.")
   CASE_ERRCODE(ERROR_INVALID_USER_BUFFER                               , "ERROR_INVALID_USER_BUFFER"                               , "The supplied user buffer is not valid for the requested operation.")
   CASE_ERRCODE(ERROR_UNRECOGNIZED_MEDIA                                , "ERROR_UNRECOGNIZED_MEDIA"                                , "The disk media is not recognized. It may not be formatted.")
   CASE_ERRCODE(ERROR_NO_TRUST_LSA_SECRET                               , "ERROR_NO_TRUST_LSA_SECRET"                               , "The workstation does not have a trust secret.")
   CASE_ERRCODE(ERROR_NO_TRUST_SAM_ACCOUNT                              , "ERROR_NO_TRUST_SAM_ACCOUNT"                              , "The security database on the server does not have a computer account for this workstation trust relationship.")
   CASE_ERRCODE(ERROR_TRUSTED_DOMAIN_FAILURE                            , "ERROR_TRUSTED_DOMAIN_FAILURE"                            , "The trust relationship between the primary domain and the trusted domain failed.")
   CASE_ERRCODE(ERROR_TRUSTED_RELATIONSHIP_FAILURE                      , "ERROR_TRUSTED_RELATIONSHIP_FAILURE"                      , "The trust relationship between this workstation and the primary domain failed.")
   CASE_ERRCODE(ERROR_TRUST_FAILURE                                     , "ERROR_TRUST_FAILURE"                                     , "The network logon failed.")
   CASE_ERRCODE(RPC_S_CALL_IN_PROGRESS                                  , "RPC_S_CALL_IN_PROGRESS"                                  , "A remote procedure call is already in progress for this thread.")
   CASE_ERRCODE(ERROR_NETLOGON_NOT_STARTED                              , "ERROR_NETLOGON_NOT_STARTED"                              , "An attempt was made to logon, but the network logon service was not started.")
   CASE_ERRCODE(ERROR_ACCOUNT_EXPIRED                                   , "ERROR_ACCOUNT_EXPIRED"                                   , "The user's account has expired.")
   CASE_ERRCODE(ERROR_REDIRECTOR_HAS_OPEN_HANDLES                       , "ERROR_REDIRECTOR_HAS_OPEN_HANDLES"                       , "The redirector is in use and cannot be unloaded.")
   CASE_ERRCODE(ERROR_PRINTER_DRIVER_ALREADY_INSTALLED                  , "ERROR_PRINTER_DRIVER_ALREADY_INSTALLED"                  , "The specified printer driver is already installed.")
   CASE_ERRCODE(ERROR_UNKNOWN_PORT                                      , "ERROR_UNKNOWN_PORT"                                      , "The specified port is unknown.")
   CASE_ERRCODE(ERROR_UNKNOWN_PRINTER_DRIVER                            , "ERROR_UNKNOWN_PRINTER_DRIVER"                            , "The printer driver is unknown.")
   CASE_ERRCODE(ERROR_UNKNOWN_PRINTPROCESSOR                            , "ERROR_UNKNOWN_PRINTPROCESSOR"                            , "The print processor is unknown.")
   CASE_ERRCODE(ERROR_INVALID_SEPARATOR_FILE                            , "ERROR_INVALID_SEPARATOR_FILE"                            , "The specified separator file is invalid.")
   CASE_ERRCODE(ERROR_INVALID_PRIORITY                                  , "ERROR_INVALID_PRIORITY"                                  , "The specified priority is invalid.")
   CASE_ERRCODE(ERROR_INVALID_PRINTER_NAME                              , "ERROR_INVALID_PRINTER_NAME"                              , "The printer name is invalid.")
   CASE_ERRCODE(ERROR_PRINTER_ALREADY_EXISTS                            , "ERROR_PRINTER_ALREADY_EXISTS"                            , "The printer already exists.")
   CASE_ERRCODE(ERROR_INVALID_PRINTER_COMMAND                           , "ERROR_INVALID_PRINTER_COMMAND"                           , "The printer command is invalid.")
   CASE_ERRCODE(ERROR_INVALID_DATATYPE                                  , "ERROR_INVALID_DATATYPE"                                  , "The specified datatype is invalid.")
   CASE_ERRCODE(ERROR_INVALID_ENVIRONMENT                               , "ERROR_INVALID_ENVIRONMENT"                               , "The environment specified is invalid.")
   CASE_ERRCODE(RPC_S_NO_MORE_BINDINGS                                  , "RPC_S_NO_MORE_BINDINGS"                                  , "There are no more bindings.")
   CASE_ERRCODE(ERROR_NOLOGON_INTERDOMAIN_TRUST_ACCOUNT                 , "ERROR_NOLOGON_INTERDOMAIN_TRUST_ACCOUNT"                 , "The account used is an interdomain trust account. Use your global user account or local user account to access this server.")
   CASE_ERRCODE(ERROR_NOLOGON_WORKSTATION_TRUST_ACCOUNT                 , "ERROR_NOLOGON_WORKSTATION_TRUST_ACCOUNT"                 , "The account used is a computer account. Use your global user account or local user account to access this server.")
   CASE_ERRCODE(ERROR_NOLOGON_SERVER_TRUST_ACCOUNT                      , "ERROR_NOLOGON_SERVER_TRUST_ACCOUNT"                      , "The account used is a server trust account. Use your global user account or local user account to access this server.")
   CASE_ERRCODE(ERROR_DOMAIN_TRUST_INCONSISTENT                         , "ERROR_DOMAIN_TRUST_INCONSISTENT"                         , "The name or security ID (SID) of the domain specified is inconsistent with the trust information for that domain.")
   CASE_ERRCODE(ERROR_SERVER_HAS_OPEN_HANDLES                           , "ERROR_SERVER_HAS_OPEN_HANDLES"                           , "The server is in use and cannot be unloaded.")
   CASE_ERRCODE(ERROR_RESOURCE_DATA_NOT_FOUND                           , "ERROR_RESOURCE_DATA_NOT_FOUND"                           , "The specified image file did not contain a resource section.")
   CASE_ERRCODE(ERROR_RESOURCE_TYPE_NOT_FOUND                           , "ERROR_RESOURCE_TYPE_NOT_FOUND"                           , "The specified resource type cannot be found in the image file.")
   CASE_ERRCODE(ERROR_RESOURCE_NAME_NOT_FOUND                           , "ERROR_RESOURCE_NAME_NOT_FOUND"                           , "The specified resource name cannot be found in the image file.")
   CASE_ERRCODE(ERROR_RESOURCE_LANG_NOT_FOUND                           , "ERROR_RESOURCE_LANG_NOT_FOUND"                           , "The specified resource language ID cannot be found in the image file.")
   CASE_ERRCODE(ERROR_NOT_ENOUGH_QUOTA                                  , "ERROR_NOT_ENOUGH_QUOTA"                                  , "Not enough quota is available to process this command.")
   CASE_ERRCODE(RPC_S_NO_INTERFACES                                     , "RPC_S_NO_INTERFACES"                                     , "No interfaces have been registered.")
   CASE_ERRCODE(RPC_S_CALL_CANCELLED                                    , "RPC_S_CALL_CANCELLED"                                    , "The remote procedure call was cancelled.")
   CASE_ERRCODE(RPC_S_BINDING_INCOMPLETE                                , "RPC_S_BINDING_INCOMPLETE"                                , "The binding handle does not contain all required information.")
   CASE_ERRCODE(RPC_S_COMM_FAILURE                                      , "RPC_S_COMM_FAILURE"                                      , "A communications failure occurred during a remote procedure call.")
   CASE_ERRCODE(RPC_S_UNSUPPORTED_AUTHN_LEVEL                           , "RPC_S_UNSUPPORTED_AUTHN_LEVEL"                           , "The requested authentication level is not supported.")
   CASE_ERRCODE(RPC_S_NO_PRINC_NAME                                     , "RPC_S_NO_PRINC_NAME"                                     , "No principal name registered.")
   CASE_ERRCODE(RPC_S_NOT_RPC_ERROR                                     , "RPC_S_NOT_RPC_ERROR"                                     , "The error specified is not a valid Windows RPC error code.")
   CASE_ERRCODE(RPC_S_UUID_LOCAL_ONLY                                   , "RPC_S_UUID_LOCAL_ONLY"                                   , "A UUID that is valid only on this computer has been allocated.")
   CASE_ERRCODE(RPC_S_SEC_PKG_ERROR                                     , "RPC_S_SEC_PKG_ERROR"                                     , "A security package specific error occurred.")
   CASE_ERRCODE(RPC_S_NOT_CANCELLED                                     , "RPC_S_NOT_CANCELLED"                                     , "Thread is not canceled.")
   CASE_ERRCODE(RPC_X_INVALID_ES_ACTION                                 , "RPC_X_INVALID_ES_ACTION"                                 , "Invalid operation on the encoding/decoding handle.")
   CASE_ERRCODE(RPC_X_WRONG_ES_VERSION                                  , "RPC_X_WRONG_ES_VERSION"                                  , "Incompatible version of the serializing package.")
   CASE_ERRCODE(RPC_X_WRONG_STUB_VERSION                                , "RPC_X_WRONG_STUB_VERSION"                                , "Incompatible version of the RPC stub.")
   CASE_ERRCODE(RPC_X_INVALID_PIPE_OBJECT                               , "RPC_X_INVALID_PIPE_OBJECT"                               , "The RPC pipe object is invalid or corrupted.")
   CASE_ERRCODE(RPC_X_WRONG_PIPE_ORDER                                  , "RPC_X_WRONG_PIPE_ORDER"                                  , "An invalid operation was attempted on an RPC pipe object.")
   CASE_ERRCODE(RPC_X_WRONG_PIPE_VERSION                                , "RPC_X_WRONG_PIPE_VERSION"                                , "Unsupported RPC pipe version.")
   CASE_ERRCODE(RPC_S_GROUP_MEMBER_NOT_FOUND                            , "RPC_S_GROUP_MEMBER_NOT_FOUND"                            , "The group member was not found.")
   CASE_ERRCODE(EPT_S_CANT_CREATE                                       , "EPT_S_CANT_CREATE"                                       , "The endpoint mapper database entry could not be created.")
   CASE_ERRCODE(RPC_S_INVALID_OBJECT                                    , "RPC_S_INVALID_OBJECT"                                    , "The object universal unique identifier (UUID) is the nil UUID.")
   CASE_ERRCODE(ERROR_INVALID_TIME                                      , "ERROR_INVALID_TIME"                                      , "The specified time is invalid.")
   CASE_ERRCODE(ERROR_INVALID_FORM_NAME                                 , "ERROR_INVALID_FORM_NAME"                                 , "The specified form name is invalid.")
   CASE_ERRCODE(ERROR_INVALID_FORM_SIZE                                 , "ERROR_INVALID_FORM_SIZE"                                 , "The specified form size is invalid.")
   CASE_ERRCODE(ERROR_ALREADY_WAITING                                   , "ERROR_ALREADY_WAITING"                                   , "The specified printer handle is already being waited on")
   CASE_ERRCODE(ERROR_PRINTER_DELETED                                   , "ERROR_PRINTER_DELETED"                                   , "The specified printer has been deleted.")
   CASE_ERRCODE(ERROR_INVALID_PRINTER_STATE                             , "ERROR_INVALID_PRINTER_STATE"                             , "The state of the printer is invalid.")
   CASE_ERRCODE(ERROR_PASSWORD_MUST_CHANGE                              , "ERROR_PASSWORD_MUST_CHANGE"                              , "The user's password must be changed before logging on the first time.")
   CASE_ERRCODE(ERROR_DOMAIN_CONTROLLER_NOT_FOUND                       , "ERROR_DOMAIN_CONTROLLER_NOT_FOUND"                       , "Could not find the domain controller for this domain.")
   CASE_ERRCODE(ERROR_ACCOUNT_LOCKED_OUT                                , "ERROR_ACCOUNT_LOCKED_OUT"                                , "The referenced account is currently locked out and may not be logged on to.")
   CASE_ERRCODE(OR_INVALID_OXID                                         , "OR_INVALID_OXID"                                         , "The object exporter specified was not found.")
   CASE_ERRCODE(OR_INVALID_OID                                          , "OR_INVALID_OID"                                          , "The object specified was not found.")
   CASE_ERRCODE(OR_INVALID_SET                                          , "OR_INVALID_SET"                                          , "The object resolver set specified was not found.")
   CASE_ERRCODE(RPC_S_SEND_INCOMPLETE                                   , "RPC_S_SEND_INCOMPLETE"                                   , "Some data remains to be sent in the request buffer.")
   CASE_ERRCODE(RPC_S_INVALID_ASYNC_HANDLE                              , "RPC_S_INVALID_ASYNC_HANDLE"                              , "Invalid asynchronous remote procedure call handle.")
   CASE_ERRCODE(RPC_S_INVALID_ASYNC_CALL                                , "RPC_S_INVALID_ASYNC_CALL"                                , "Invalid asynchronous RPC call handle for this operation.")
   CASE_ERRCODE(RPC_X_PIPE_CLOSED                                       , "RPC_X_PIPE_CLOSED"                                       , "The RPC pipe object has already been closed.")
   CASE_ERRCODE(RPC_X_PIPE_DISCIPLINE_ERROR                             , "RPC_X_PIPE_DISCIPLINE_ERROR"                             , "The RPC call completed before all pipes were processed.")
   CASE_ERRCODE(RPC_X_PIPE_EMPTY                                        , "RPC_X_PIPE_EMPTY"                                        , "No more data is available from the RPC pipe.")
   CASE_ERRCODE(ERROR_NO_SITENAME                                       , "ERROR_NO_SITENAME"                                       , "No site name is available for this machine.")
   CASE_ERRCODE(ERROR_CANT_ACCESS_FILE                                  , "ERROR_CANT_ACCESS_FILE"                                  , "The file cannot be accessed by the system.")
   CASE_ERRCODE(ERROR_CANT_RESOLVE_FILENAME                             , "ERROR_CANT_RESOLVE_FILENAME"                             , "The name of the file cannot be resolved by the system.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(RPC_S_ENTRY_TYPE_MISMATCH                               , "RPC_S_ENTRY_TYPE_MISMATCH"                               , "The entry is not of the expected type.")
   CASE_ERRCODE(RPC_S_NOT_ALL_OBJS_EXPORTED                             , "RPC_S_NOT_ALL_OBJS_EXPORTED"                             , "Not all object UUIDs could be exported to the specified entry.")
   CASE_ERRCODE(RPC_S_INTERFACE_NOT_EXPORTED                            , "RPC_S_INTERFACE_NOT_EXPORTED"                            , "Interface could not be exported to the specified entry.")
   CASE_ERRCODE(RPC_S_PROFILE_NOT_ADDED                                 , "RPC_S_PROFILE_NOT_ADDED"                                 , "The specified profile entry could not be added.")
   CASE_ERRCODE(RPC_S_PRF_ELT_NOT_ADDED                                 , "RPC_S_PRF_ELT_NOT_ADDED"                                 , "The specified profile element could not be added.")
   CASE_ERRCODE(RPC_S_PRF_ELT_NOT_REMOVED                               , "RPC_S_PRF_ELT_NOT_REMOVED"                               , "The specified profile element could not be removed.")
   CASE_ERRCODE(RPC_S_GRP_ELT_NOT_ADDED                                 , "RPC_S_GRP_ELT_NOT_ADDED"                                 , "The group element could not be added.")
   CASE_ERRCODE(RPC_S_GRP_ELT_NOT_REMOVED                               , "RPC_S_GRP_ELT_NOT_REMOVED"                               , "The group element could not be removed.")
   CASE_ERRCODE(ERROR_KM_DRIVER_BLOCKED                                 , "ERROR_KM_DRIVER_BLOCKED"                                 , "The printer driver is not compatible with a policy enabled on your computer that blocks NT 4.0 drivers.")
   CASE_ERRCODE(ERROR_CONTEXT_EXPIRED                                   , "ERROR_CONTEXT_EXPIRED"                                   , "The context has expired and can no longer be used.")
   CASE_ERRCODE(ERROR_PER_USER_TRUST_QUOTA_EXCEEDED                     , "ERROR_PER_USER_TRUST_QUOTA_EXCEEDED"                     , "The current user's delegated trust creation quota has been exceeded.")
   CASE_ERRCODE(ERROR_ALL_USER_TRUST_QUOTA_EXCEEDED                     , "ERROR_ALL_USER_TRUST_QUOTA_EXCEEDED"                     , "The total delegated trust creation quota has been exceeded.")
   CASE_ERRCODE(ERROR_USER_DELETE_TRUST_QUOTA_EXCEEDED                  , "ERROR_USER_DELETE_TRUST_QUOTA_EXCEEDED"                  , "The current user's delegated trust deletion quota has been exceeded.")
   CASE_ERRCODE(ERROR_AUTHENTICATION_FIREWALL_FAILED                    , "ERROR_AUTHENTICATION_FIREWALL_FAILED"                    , "Logon Failure: The machine you are logging onto is protected by an authentication firewall.  The specified account is not allowed to authenticate to the machine.")
   CASE_ERRCODE(ERROR_REMOTE_PRINT_CONNECTIONS_BLOCKED                  , "ERROR_REMOTE_PRINT_CONNECTIONS_BLOCKED"                  , "Remote connections to the Print Spooler are blocked by a policy set on your machine.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_PIXEL_FORMAT                              , "ERROR_INVALID_PIXEL_FORMAT"                              , "The pixel format is invalid.")
   CASE_ERRCODE(ERROR_BAD_DRIVER                                        , "ERROR_BAD_DRIVER"                                        , "The specified driver is invalid.")
   CASE_ERRCODE(ERROR_INVALID_WINDOW_STYLE                              , "ERROR_INVALID_WINDOW_STYLE"                              , "The window style or class attribute is invalid for this operation.")
   CASE_ERRCODE(ERROR_METAFILE_NOT_SUPPORTED                            , "ERROR_METAFILE_NOT_SUPPORTED"                            , "The requested metafile operation is not supported.")
   CASE_ERRCODE(ERROR_TRANSFORM_NOT_SUPPORTED                           , "ERROR_TRANSFORM_NOT_SUPPORTED"                           , "The requested transformation operation is not supported.")
   CASE_ERRCODE(ERROR_CLIPPING_NOT_SUPPORTED                            , "ERROR_CLIPPING_NOT_SUPPORTED"                            , "The requested clipping operation is not supported.")
   CASE_ERRCODE(ERROR_INVALID_CMM                                       , "ERROR_INVALID_CMM"                                       , "The specified color management module is invalid.")
   CASE_ERRCODE(ERROR_INVALID_PROFILE                                   , "ERROR_INVALID_PROFILE"                                   , "The specified color profile is invalid.")
   CASE_ERRCODE(ERROR_TAG_NOT_FOUND                                     , "ERROR_TAG_NOT_FOUND"                                     , "The specified tag was not found.")
   CASE_ERRCODE(ERROR_TAG_NOT_PRESENT                                   , "ERROR_TAG_NOT_PRESENT"                                   , "A required tag is not present.")
   CASE_ERRCODE(ERROR_DUPLICATE_TAG                                     , "ERROR_DUPLICATE_TAG"                                     , "The specified tag is already present.")
   CASE_ERRCODE(ERROR_PROFILE_NOT_ASSOCIATED_WITH_DEVICE                , "ERROR_PROFILE_NOT_ASSOCIATED_WITH_DEVICE"                , "The specified color profile is not associated with any device.")
   CASE_ERRCODE(ERROR_PROFILE_NOT_FOUND                                 , "ERROR_PROFILE_NOT_FOUND"                                 , "The specified color profile was not found.")
   CASE_ERRCODE(ERROR_INVALID_COLORSPACE                                , "ERROR_INVALID_COLORSPACE"                                , "The specified color space is invalid.")
   CASE_ERRCODE(ERROR_ICM_NOT_ENABLED                                   , "ERROR_ICM_NOT_ENABLED"                                   , "Image Color Management is not enabled.")
   CASE_ERRCODE(ERROR_DELETING_ICM_XFORM                                , "ERROR_DELETING_ICM_XFORM"                                , "There was an error while deleting the color transform.")
   CASE_ERRCODE(ERROR_INVALID_TRANSFORM                                 , "ERROR_INVALID_TRANSFORM"                                 , "The specified color transform is invalid.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_COLORSPACE_MISMATCH                               , "ERROR_COLORSPACE_MISMATCH"                               , "The specified transform does not match the bitmap's color space.")
   CASE_ERRCODE(ERROR_INVALID_COLORINDEX                                , "ERROR_INVALID_COLORINDEX"                                , "The specified named color index is not present in the profile.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_CONNECTED_OTHER_PASSWORD                          , "ERROR_CONNECTED_OTHER_PASSWORD"                          , "The network connection was made successfully, but the user had to be prompted for a password other than the one originally specified.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_CONNECTED_OTHER_PASSWORD_DEFAULT                  , "ERROR_CONNECTED_OTHER_PASSWORD_DEFAULT"                  , "The network connection was made successfully using default credentials.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_BAD_USERNAME                                      , "ERROR_BAD_USERNAME"                                      , "The specified username is invalid.")
   CASE_ERRCODE(ERROR_NOT_CONNECTED                                     , "ERROR_NOT_CONNECTED"                                     , "This network connection does not exist.")
   CASE_ERRCODE(ERROR_OPEN_FILES                                        , "ERROR_OPEN_FILES"                                        , "This network connection has files open or requests pending.")
   CASE_ERRCODE(ERROR_ACTIVE_CONNECTIONS                                , "ERROR_ACTIVE_CONNECTIONS"                                , "Active connections still exist.")
   CASE_ERRCODE(ERROR_DEVICE_IN_USE                                     , "ERROR_DEVICE_IN_USE"                                     , "The device is in use by an active process and cannot be disconnected.")
   CASE_ERRCODE(ERROR_UNKNOWN_PRINT_MONITOR                             , "ERROR_UNKNOWN_PRINT_MONITOR"                             , "The specified print monitor is unknown.")
   CASE_ERRCODE(ERROR_PRINTER_DRIVER_IN_USE                             , "ERROR_PRINTER_DRIVER_IN_USE"                             , "The specified printer driver is currently in use.")
   CASE_ERRCODE(ERROR_SPOOL_FILE_NOT_FOUND                              , "ERROR_SPOOL_FILE_NOT_FOUND"                              , "The spool file was not found.")
   CASE_ERRCODE(ERROR_SPL_NO_STARTDOC                                   , "ERROR_SPL_NO_STARTDOC"                                   , "A StartDocPrinter call was not issued.")
   CASE_ERRCODE(ERROR_SPL_NO_ADDJOB                                     , "ERROR_SPL_NO_ADDJOB"                                     , "An AddJob call was not issued.")
   CASE_ERRCODE(ERROR_PRINT_PROCESSOR_ALREADY_INSTALLED                 , "ERROR_PRINT_PROCESSOR_ALREADY_INSTALLED"                 , "The specified print processor has already been installed.")
   CASE_ERRCODE(ERROR_PRINT_MONITOR_ALREADY_INSTALLED                   , "ERROR_PRINT_MONITOR_ALREADY_INSTALLED"                   , "The specified print monitor has already been installed.")
   CASE_ERRCODE(ERROR_INVALID_PRINT_MONITOR                             , "ERROR_INVALID_PRINT_MONITOR"                             , "The specified print monitor does not have the required functions.")
   CASE_ERRCODE(ERROR_PRINT_MONITOR_IN_USE                              , "ERROR_PRINT_MONITOR_IN_USE"                              , "The specified print monitor is currently in use.")
   CASE_ERRCODE(ERROR_PRINTER_HAS_JOBS_QUEUED                           , "ERROR_PRINTER_HAS_JOBS_QUEUED"                           , "The requested operation is not allowed when there are jobs queued to the printer.")
   CASE_ERRCODE(ERROR_SUCCESS_REBOOT_REQUIRED                           , "ERROR_SUCCESS_REBOOT_REQUIRED"                           , "The requested operation is successful. Changes will not be effective until the system is rebooted.")
   CASE_ERRCODE(ERROR_SUCCESS_RESTART_REQUIRED                          , "ERROR_SUCCESS_RESTART_REQUIRED"                          , "The requested operation is successful. Changes will not be effective until the service is restarted.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_PRINTER_NOT_FOUND                                 , "ERROR_PRINTER_NOT_FOUND"                                 , "No printers were found.")
   CASE_ERRCODE(ERROR_PRINTER_DRIVER_WARNED                             , "ERROR_PRINTER_DRIVER_WARNED"                             , "The printer driver is known to be unreliable.")
   CASE_ERRCODE(ERROR_PRINTER_DRIVER_BLOCKED                            , "ERROR_PRINTER_DRIVER_BLOCKED"                            , "The printer driver is known to harm the system.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_WINS_INTERNAL                                     , "ERROR_WINS_INTERNAL"                                     , "WINS encountered an error while processing the command.")
   CASE_ERRCODE(ERROR_CAN_NOT_DEL_LOCAL_WINS                            , "ERROR_CAN_NOT_DEL_LOCAL_WINS"                            , "The local WINS cannot be deleted.")
   CASE_ERRCODE(ERROR_STATIC_INIT                                       , "ERROR_STATIC_INIT"                                       , "The importation from the file failed.")
   CASE_ERRCODE(ERROR_INC_BACKUP                                        , "ERROR_INC_BACKUP"                                        , "The backup failed. Was a full backup done before?")
   CASE_ERRCODE(ERROR_FULL_BACKUP                                       , "ERROR_FULL_BACKUP"                                       , "The backup failed. Check the directory to which you are backing the database.")
   CASE_ERRCODE(ERROR_REC_NON_EXISTENT                                  , "ERROR_REC_NON_EXISTENT"                                  , "The name does not exist in the WINS database.")
   CASE_ERRCODE(ERROR_RPL_NOT_ALLOWED                                   , "ERROR_RPL_NOT_ALLOWED"                                   , "Replication with a nonconfigured partner is not allowed.")
   CASE_ERRCODE(ERROR_DHCP_ADDRESS_CONFLICT                             , "ERROR_DHCP_ADDRESS_CONFLICT"                             , "The DHCP client has obtained an IP address that is already in use on the network. The local interface will be disabled until the DHCP client can obtain a new address.")
   CASE_ERRCODE(ERROR_WMI_GUID_NOT_FOUND                                , "ERROR_WMI_GUID_NOT_FOUND"                                , "The GUID passed was not recognized as valid by a WMI data provider.")
   CASE_ERRCODE(ERROR_WMI_INSTANCE_NOT_FOUND                            , "ERROR_WMI_INSTANCE_NOT_FOUND"                            , "The instance name passed was not recognized as valid by a WMI data provider.")
   CASE_ERRCODE(ERROR_WMI_ITEMID_NOT_FOUND                              , "ERROR_WMI_ITEMID_NOT_FOUND"                              , "The data item ID passed was not recognized as valid by a WMI data provider.")
   CASE_ERRCODE(ERROR_WMI_TRY_AGAIN                                     , "ERROR_WMI_TRY_AGAIN"                                     , "The WMI request could not be completed and should be retried.")
   CASE_ERRCODE(ERROR_WMI_DP_NOT_FOUND                                  , "ERROR_WMI_DP_NOT_FOUND"                                  , "The WMI data provider could not be located.")
   CASE_ERRCODE(ERROR_WMI_UNRESOLVED_INSTANCE_REF                       , "ERROR_WMI_UNRESOLVED_INSTANCE_REF"                       , "The WMI data provider references an instance set that has not been registered.")
   CASE_ERRCODE(ERROR_WMI_ALREADY_ENABLED                               , "ERROR_WMI_ALREADY_ENABLED"                               , "The WMI data block or event notification has already been enabled.")
   CASE_ERRCODE(ERROR_WMI_GUID_DISCONNECTED                             , "ERROR_WMI_GUID_DISCONNECTED"                             , "The WMI data block is no longer available.")
   CASE_ERRCODE(ERROR_WMI_SERVER_UNAVAILABLE                            , "ERROR_WMI_SERVER_UNAVAILABLE"                            , "The WMI data service is not available.")
   CASE_ERRCODE(ERROR_WMI_DP_FAILED                                     , "ERROR_WMI_DP_FAILED"                                     , "The WMI data provider failed to carry out the request.")
   CASE_ERRCODE(ERROR_WMI_INVALID_MOF                                   , "ERROR_WMI_INVALID_MOF"                                   , "The WMI MOF information is not valid.")
   CASE_ERRCODE(ERROR_WMI_INVALID_REGINFO                               , "ERROR_WMI_INVALID_REGINFO"                               , "The WMI registration information is not valid.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_WMI_ALREADY_DISABLED                              , "ERROR_WMI_ALREADY_DISABLED"                              , "The WMI data block or event notification has already been disabled.")
   CASE_ERRCODE(ERROR_WMI_READ_ONLY                                     , "ERROR_WMI_READ_ONLY"                                     , "The WMI data item or data block is read only.")
   CASE_ERRCODE(ERROR_WMI_SET_FAILURE                                   , "ERROR_WMI_SET_FAILURE"                                   , "The WMI data item or data block could not be changed.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_MEDIA                                     , "ERROR_INVALID_MEDIA"                                     , "The media identifier does not represent a valid medium.")
   CASE_ERRCODE(ERROR_INVALID_LIBRARY                                   , "ERROR_INVALID_LIBRARY"                                   , "The library identifier does not represent a valid library.")
   CASE_ERRCODE(ERROR_INVALID_MEDIA_POOL                                , "ERROR_INVALID_MEDIA_POOL"                                , "The media pool identifier does not represent a valid media pool.")
   CASE_ERRCODE(ERROR_DRIVE_MEDIA_MISMATCH                              , "ERROR_DRIVE_MEDIA_MISMATCH"                              , "The drive and medium are not compatible or exist in different libraries.")
   CASE_ERRCODE(ERROR_MEDIA_OFFLINE                                     , "ERROR_MEDIA_OFFLINE"                                     , "The medium currently exists in an offline library and must be online to perform this operation.")
   CASE_ERRCODE(ERROR_LIBRARY_OFFLINE                                   , "ERROR_LIBRARY_OFFLINE"                                   , "The operation cannot be performed on an offline library.")
   CASE_ERRCODE(ERROR_EMPTY                                             , "ERROR_EMPTY"                                             , "The library, drive, or media pool is empty.")
   CASE_ERRCODE(ERROR_NOT_EMPTY                                         , "ERROR_NOT_EMPTY"                                         , "The library, drive, or media pool must be empty to perform this operation.")
   CASE_ERRCODE(ERROR_MEDIA_UNAVAILABLE                                 , "ERROR_MEDIA_UNAVAILABLE"                                 , "No media is currently available in this media pool or library.")
   CASE_ERRCODE(ERROR_RESOURCE_DISABLED                                 , "ERROR_RESOURCE_DISABLED"                                 , "A resource required for this operation is disabled.")
   CASE_ERRCODE(ERROR_INVALID_CLEANER                                   , "ERROR_INVALID_CLEANER"                                   , "The media identifier does not represent a valid cleaner.")
   CASE_ERRCODE(ERROR_UNABLE_TO_CLEAN                                   , "ERROR_UNABLE_TO_CLEAN"                                   , "The drive cannot be cleaned or does not support cleaning.")
   CASE_ERRCODE(ERROR_OBJECT_NOT_FOUND                                  , "ERROR_OBJECT_NOT_FOUND"                                  , "The object identifier does not represent a valid object.")
   CASE_ERRCODE(ERROR_DATABASE_FAILURE                                  , "ERROR_DATABASE_FAILURE"                                  , "Unable to read from or write to the database.")
   CASE_ERRCODE(ERROR_DATABASE_FULL                                     , "ERROR_DATABASE_FULL"                                     , "The database is full.")
   CASE_ERRCODE(ERROR_MEDIA_INCOMPATIBLE                                , "ERROR_MEDIA_INCOMPATIBLE"                                , "The medium is not compatible with the device or media pool.")
   CASE_ERRCODE(ERROR_RESOURCE_NOT_PRESENT                              , "ERROR_RESOURCE_NOT_PRESENT"                              , "The resource required for this operation does not exist.")
   CASE_ERRCODE(ERROR_INVALID_OPERATION                                 , "ERROR_INVALID_OPERATION"                                 , "The operation identifier is not valid.")
   CASE_ERRCODE(ERROR_MEDIA_NOT_AVAILABLE                               , "ERROR_MEDIA_NOT_AVAILABLE"                               , "The media is not mounted or ready for use.")
   CASE_ERRCODE(ERROR_DEVICE_NOT_AVAILABLE                              , "ERROR_DEVICE_NOT_AVAILABLE"                              , "The device is not ready for use.")
   CASE_ERRCODE(ERROR_REQUEST_REFUSED                                   , "ERROR_REQUEST_REFUSED"                                   , "The operator or administrator has refused the request.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_DRIVE_OBJECT                              , "ERROR_INVALID_DRIVE_OBJECT"                              , "The drive identifier does not represent a valid drive.")
   CASE_ERRCODE(ERROR_LIBRARY_FULL                                      , "ERROR_LIBRARY_FULL"                                      , "Library is full. No slot is available for use.")
   CASE_ERRCODE(ERROR_MEDIUM_NOT_ACCESSIBLE                             , "ERROR_MEDIUM_NOT_ACCESSIBLE"                             , "The transport cannot access the medium.")
   CASE_ERRCODE(ERROR_UNABLE_TO_LOAD_MEDIUM                             , "ERROR_UNABLE_TO_LOAD_MEDIUM"                             , "Unable to load the medium into the drive.")
   CASE_ERRCODE(ERROR_UNABLE_TO_INVENTORY_DRIVE                         , "ERROR_UNABLE_TO_INVENTORY_DRIVE"                         , "Unable to retrieve status about the drive.")
   CASE_ERRCODE(ERROR_UNABLE_TO_INVENTORY_SLOT                          , "ERROR_UNABLE_TO_INVENTORY_SLOT"                          , "Unable to retrieve status about the slot.")
   CASE_ERRCODE(ERROR_UNABLE_TO_INVENTORY_TRANSPORT                     , "ERROR_UNABLE_TO_INVENTORY_TRANSPORT"                     , "Unable to retrieve status about the transport.")
   CASE_ERRCODE(ERROR_TRANSPORT_FULL                                    , "ERROR_TRANSPORT_FULL"                                    , "Cannot use the transport because it is already in use.")
   CASE_ERRCODE(ERROR_CONTROLLING_IEPORT                                , "ERROR_CONTROLLING_IEPORT"                                , "Unable to open or close the inject/eject port.")
   CASE_ERRCODE(ERROR_UNABLE_TO_EJECT_MOUNTED_MEDIA                     , "ERROR_UNABLE_TO_EJECT_MOUNTED_MEDIA"                     , "Unable to eject the media because it is in a drive.")
   CASE_ERRCODE(ERROR_CLEANER_SLOT_SET                                  , "ERROR_CLEANER_SLOT_SET"                                  , "A cleaner slot is already reserved.")
   CASE_ERRCODE(ERROR_CLEANER_SLOT_NOT_SET                              , "ERROR_CLEANER_SLOT_NOT_SET"                              , "A cleaner slot is not reserved.")
   CASE_ERRCODE(ERROR_CLEANER_CARTRIDGE_SPENT                           , "ERROR_CLEANER_CARTRIDGE_SPENT"                           , "The cleaner cartridge has performed the maximum number of drive cleanings.")
   CASE_ERRCODE(ERROR_UNEXPECTED_OMID                                   , "ERROR_UNEXPECTED_OMID"                                   , "Unexpected on-medium identifier.")
   CASE_ERRCODE(ERROR_CANT_DELETE_LAST_ITEM                             , "ERROR_CANT_DELETE_LAST_ITEM"                             , "The last remaining item in this group or resource cannot be deleted.")
   CASE_ERRCODE(ERROR_MESSAGE_EXCEEDS_MAX_SIZE                          , "ERROR_MESSAGE_EXCEEDS_MAX_SIZE"                          , "The message provided exceeds the maximum size allowed for this parameter.")
   CASE_ERRCODE(ERROR_VOLUME_CONTAINS_SYS_FILES                         , "ERROR_VOLUME_CONTAINS_SYS_FILES"                         , "The volume contains system or paging files.")
   CASE_ERRCODE(ERROR_INDIGENOUS_TYPE                                   , "ERROR_INDIGENOUS_TYPE"                                   , "The media type cannot be removed from this library since at least one drive in the library reports it can support this media type.")
   CASE_ERRCODE(ERROR_NO_SUPPORTING_DRIVES                              , "ERROR_NO_SUPPORTING_DRIVES"                              , "This offline media cannot be mounted on this system since no enabled drives are present which can be used.")
   CASE_ERRCODE(ERROR_CLEANER_CARTRIDGE_INSTALLED                       , "ERROR_CLEANER_CARTRIDGE_INSTALLED"                       , "A cleaner cartridge is present in the tape library.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_FILE_OFFLINE                                      , "ERROR_FILE_OFFLINE"                                      , "The remote storage service was not able to recall the file.")
   CASE_ERRCODE(ERROR_REMOTE_STORAGE_NOT_ACTIVE                         , "ERROR_REMOTE_STORAGE_NOT_ACTIVE"                         , "The remote storage service is not operational at this time.")
   CASE_ERRCODE(ERROR_REMOTE_STORAGE_MEDIA_ERROR                        , "ERROR_REMOTE_STORAGE_MEDIA_ERROR"                        , "The remote storage service encountered a media error.")
   CASE_ERRCODE(ERROR_NOT_A_REPARSE_POINT                               , "ERROR_NOT_A_REPARSE_POINT"                               , "The file or directory is not a reparse point.")
   CASE_ERRCODE(ERROR_REPARSE_ATTRIBUTE_CONFLICT                        , "ERROR_REPARSE_ATTRIBUTE_CONFLICT"                        , "The reparse point attribute cannot be set because it conflicts with an existing attribute.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_INVALID_REPARSE_DATA                              , "ERROR_INVALID_REPARSE_DATA"                              , "The data present in the reparse point buffer is invalid.")
   CASE_ERRCODE(ERROR_REPARSE_TAG_INVALID                               , "ERROR_REPARSE_TAG_INVALID"                               , "The tag present in the reparse point buffer is invalid.")
   CASE_ERRCODE(ERROR_REPARSE_TAG_MISMATCH                              , "ERROR_REPARSE_TAG_MISMATCH"                              , "There is a mismatch between the tag specified in the request and the tag present in the reparse point.")
   CASE_ERRCODE(ERROR_VOLUME_NOT_SIS_ENABLED                            , "ERROR_VOLUME_NOT_SIS_ENABLED"                            , "Single Instance Storage is not available on this volume.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_DEPENDENT_RESOURCE_EXISTS                         , "ERROR_DEPENDENT_RESOURCE_EXISTS"                         , "The cluster resource cannot be moved to another group because other resources are dependent on it.")
   CASE_ERRCODE(ERROR_DEPENDENCY_NOT_FOUND                              , "ERROR_DEPENDENCY_NOT_FOUND"                              , "The cluster resource dependency cannot be found.")
   CASE_ERRCODE(ERROR_DEPENDENCY_ALREADY_EXISTS                         , "ERROR_DEPENDENCY_ALREADY_EXISTS"                         , "The cluster resource cannot be made dependent on the specified resource because it is already dependent.")
   CASE_ERRCODE(ERROR_RESOURCE_NOT_ONLINE                               , "ERROR_RESOURCE_NOT_ONLINE"                               , "The cluster resource is not online.")
   CASE_ERRCODE(ERROR_HOST_NODE_NOT_AVAILABLE                           , "ERROR_HOST_NODE_NOT_AVAILABLE"                           , "A cluster node is not available for this operation.")
   CASE_ERRCODE(ERROR_RESOURCE_NOT_AVAILABLE                            , "ERROR_RESOURCE_NOT_AVAILABLE"                            , "The cluster resource is not available.")
   CASE_ERRCODE(ERROR_RESOURCE_NOT_FOUND                                , "ERROR_RESOURCE_NOT_FOUND"                                , "The cluster resource could not be found.")
   CASE_ERRCODE(ERROR_SHUTDOWN_CLUSTER                                  , "ERROR_SHUTDOWN_CLUSTER"                                  , "The cluster is being shut down.")
   CASE_ERRCODE(ERROR_CANT_EVICT_ACTIVE_NODE                            , "ERROR_CANT_EVICT_ACTIVE_NODE"                            , "A cluster node cannot be evicted from the cluster unless the node is down.")
   CASE_ERRCODE(ERROR_OBJECT_ALREADY_EXISTS                             , "ERROR_OBJECT_ALREADY_EXISTS"                             , "The object already exists.")
   CASE_ERRCODE(ERROR_OBJECT_IN_LIST                                    , "ERROR_OBJECT_IN_LIST"                                    , "The object is already in the list.")
   CASE_ERRCODE(ERROR_GROUP_NOT_AVAILABLE                               , "ERROR_GROUP_NOT_AVAILABLE"                               , "The cluster group is not available for any new requests.")
   CASE_ERRCODE(ERROR_GROUP_NOT_FOUND                                   , "ERROR_GROUP_NOT_FOUND"                                   , "The cluster group could not be found.")
   CASE_ERRCODE(ERROR_GROUP_NOT_ONLINE                                  , "ERROR_GROUP_NOT_ONLINE"                                  , "The operation could not be completed because the cluster group is not online.")
   CASE_ERRCODE(ERROR_HOST_NODE_NOT_RESOURCE_OWNER                      , "ERROR_HOST_NODE_NOT_RESOURCE_OWNER"                      , "The cluster node is not the owner of the resource.")
   CASE_ERRCODE(ERROR_HOST_NODE_NOT_GROUP_OWNER                         , "ERROR_HOST_NODE_NOT_GROUP_OWNER"                         , "The cluster node is not the owner of the group.")
   CASE_ERRCODE(ERROR_RESMON_CREATE_FAILED                              , "ERROR_RESMON_CREATE_FAILED"                              , "The cluster resource could not be created in the specified resource monitor.")
   CASE_ERRCODE(ERROR_RESMON_ONLINE_FAILED                              , "ERROR_RESMON_ONLINE_FAILED"                              , "The cluster resource could not be brought online by the resource monitor.")
   CASE_ERRCODE(ERROR_RESOURCE_ONLINE                                   , "ERROR_RESOURCE_ONLINE"                                   , "The operation could not be completed because the cluster resource is online.")
   CASE_ERRCODE(ERROR_QUORUM_RESOURCE                                   , "ERROR_QUORUM_RESOURCE"                                   , "The cluster resource could not be deleted or brought offline because it is the quorum resource.")
   CASE_ERRCODE(ERROR_NOT_QUORUM_CAPABLE                                , "ERROR_NOT_QUORUM_CAPABLE"                                , "The cluster could not make the specified resource a quorum resource because it is not capable of being a quorum resource.")
   CASE_ERRCODE(ERROR_CLUSTER_SHUTTING_DOWN                             , "ERROR_CLUSTER_SHUTTING_DOWN"                             , "The cluster software is shutting down.")
   CASE_ERRCODE(ERROR_INVALID_STATE                                     , "ERROR_INVALID_STATE"                                     , "The group or resource is not in the correct state to perform the requested operation.")
   CASE_ERRCODE(ERROR_RESOURCE_PROPERTIES_STORED                        , "ERROR_RESOURCE_PROPERTIES_STORED"                        , "The properties were stored but not all changes will take effect until the next time the resource is brought online.")
   CASE_ERRCODE(ERROR_NOT_QUORUM_CLASS                                  , "ERROR_NOT_QUORUM_CLASS"                                  , "The cluster could not make the specified resource a quorum resource because it does not belong to a shared storage class.")
   CASE_ERRCODE(ERROR_CORE_RESOURCE                                     , "ERROR_CORE_RESOURCE"                                     , "The cluster resource could not be deleted since it is a core resource.")
   CASE_ERRCODE(ERROR_QUORUM_RESOURCE_ONLINE_FAILED                     , "ERROR_QUORUM_RESOURCE_ONLINE_FAILED"                     , "The quorum resource failed to come online.")
   CASE_ERRCODE(ERROR_QUORUMLOG_OPEN_FAILED                             , "ERROR_QUORUMLOG_OPEN_FAILED"                             , "The quorum log could not be created or mounted successfully.")
   CASE_ERRCODE(ERROR_CLUSTERLOG_CORRUPT                                , "ERROR_CLUSTERLOG_CORRUPT"                                , "The cluster log is corrupt.")
   CASE_ERRCODE(ERROR_CLUSTERLOG_RECORD_EXCEEDS_MAXSIZE                 , "ERROR_CLUSTERLOG_RECORD_EXCEEDS_MAXSIZE"                 , "The record could not be written to the cluster log since it exceeds the maximum size.")
   CASE_ERRCODE(ERROR_CLUSTERLOG_EXCEEDS_MAXSIZE                        , "ERROR_CLUSTERLOG_EXCEEDS_MAXSIZE"                        , "The cluster log exceeds its maximum size.")
   CASE_ERRCODE(ERROR_CLUSTERLOG_CHKPOINT_NOT_FOUND                     , "ERROR_CLUSTERLOG_CHKPOINT_NOT_FOUND"                     , "No checkpoint record was found in the cluster log.")
   CASE_ERRCODE(ERROR_CLUSTERLOG_NOT_ENOUGH_SPACE                       , "ERROR_CLUSTERLOG_NOT_ENOUGH_SPACE"                       , "The minimum required disk space needed for logging is not available.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_QUORUM_OWNER_ALIVE                                , "ERROR_QUORUM_OWNER_ALIVE"                                , "The cluster node failed to take control of the quorum resource because the resource is owned by another active node.")
   CASE_ERRCODE(ERROR_NETWORK_NOT_AVAILABLE                             , "ERROR_NETWORK_NOT_AVAILABLE"                             , "A cluster network is not available for this operation.")
   CASE_ERRCODE(ERROR_NODE_NOT_AVAILABLE                                , "ERROR_NODE_NOT_AVAILABLE"                                , "A cluster node is not available for this operation.")
   CASE_ERRCODE(ERROR_ALL_NODES_NOT_AVAILABLE                           , "ERROR_ALL_NODES_NOT_AVAILABLE"                           , "All cluster nodes must be running to perform this operation.")
   CASE_ERRCODE(ERROR_RESOURCE_FAILED                                   , "ERROR_RESOURCE_FAILED"                                   , "A cluster resource failed.")
   CASE_ERRCODE(ERROR_CLUSTER_INVALID_NODE                              , "ERROR_CLUSTER_INVALID_NODE"                              , "The cluster node is not valid.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_EXISTS                               , "ERROR_CLUSTER_NODE_EXISTS"                               , "The cluster node already exists.")
   CASE_ERRCODE(ERROR_CLUSTER_JOIN_IN_PROGRESS                          , "ERROR_CLUSTER_JOIN_IN_PROGRESS"                          , "A node is in the process of joining the cluster.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_NOT_FOUND                            , "ERROR_CLUSTER_NODE_NOT_FOUND"                            , "The cluster node was not found.")
   CASE_ERRCODE(ERROR_CLUSTER_LOCAL_NODE_NOT_FOUND                      , "ERROR_CLUSTER_LOCAL_NODE_NOT_FOUND"                      , "The cluster local node information was not found.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_EXISTS                            , "ERROR_CLUSTER_NETWORK_EXISTS"                            , "The cluster network already exists.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_NOT_FOUND                         , "ERROR_CLUSTER_NETWORK_NOT_FOUND"                         , "The cluster network was not found.")
   CASE_ERRCODE(ERROR_CLUSTER_NETINTERFACE_EXISTS                       , "ERROR_CLUSTER_NETINTERFACE_EXISTS"                       , "The cluster network interface already exists.")
   CASE_ERRCODE(ERROR_CLUSTER_NETINTERFACE_NOT_FOUND                    , "ERROR_CLUSTER_NETINTERFACE_NOT_FOUND"                    , "The cluster network interface was not found.")
   CASE_ERRCODE(ERROR_CLUSTER_INVALID_REQUEST                           , "ERROR_CLUSTER_INVALID_REQUEST"                           , "The cluster request is not valid for this object.")
   CASE_ERRCODE(ERROR_CLUSTER_INVALID_NETWORK_PROVIDER                  , "ERROR_CLUSTER_INVALID_NETWORK_PROVIDER"                  , "The cluster network provider is not valid.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_DOWN                                 , "ERROR_CLUSTER_NODE_DOWN"                                 , "The cluster node is down.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_UNREACHABLE                          , "ERROR_CLUSTER_NODE_UNREACHABLE"                          , "The cluster node is not reachable.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_NOT_MEMBER                           , "ERROR_CLUSTER_NODE_NOT_MEMBER"                           , "The cluster node is not a member of the cluster.")
   CASE_ERRCODE(ERROR_CLUSTER_JOIN_NOT_IN_PROGRESS                      , "ERROR_CLUSTER_JOIN_NOT_IN_PROGRESS"                      , "A cluster join operation is not in progress.")
   CASE_ERRCODE(ERROR_CLUSTER_INVALID_NETWORK                           , "ERROR_CLUSTER_INVALID_NETWORK"                           , "The cluster network is not valid.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_UP                                   , "ERROR_CLUSTER_NODE_UP"                                   , "The cluster node is up.")
   CASE_ERRCODE(ERROR_CLUSTER_IPADDR_IN_USE                             , "ERROR_CLUSTER_IPADDR_IN_USE"                             , "The cluster IP address is already in use.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_NOT_PAUSED                           , "ERROR_CLUSTER_NODE_NOT_PAUSED"                           , "The cluster node is not paused.")
   CASE_ERRCODE(ERROR_CLUSTER_NO_SECURITY_CONTEXT                       , "ERROR_CLUSTER_NO_SECURITY_CONTEXT"                       , "No cluster security context is available.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_NOT_INTERNAL                      , "ERROR_CLUSTER_NETWORK_NOT_INTERNAL"                      , "The cluster network is not configured for internal cluster communication.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_ALREADY_UP                           , "ERROR_CLUSTER_NODE_ALREADY_UP"                           , "The cluster node is already up.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_ALREADY_DOWN                         , "ERROR_CLUSTER_NODE_ALREADY_DOWN"                         , "The cluster node is already down.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_ALREADY_ONLINE                    , "ERROR_CLUSTER_NETWORK_ALREADY_ONLINE"                    , "The cluster network is already online.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_ALREADY_OFFLINE                   , "ERROR_CLUSTER_NETWORK_ALREADY_OFFLINE"                   , "The cluster network is already offline.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_ALREADY_MEMBER                       , "ERROR_CLUSTER_NODE_ALREADY_MEMBER"                       , "The cluster node is already a member of the cluster.")
   CASE_ERRCODE(ERROR_CLUSTER_LAST_INTERNAL_NETWORK                     , "ERROR_CLUSTER_LAST_INTERNAL_NETWORK"                     , "The cluster network is the only one configured for internal cluster communication between two or more active cluster nodes. The internal communication capability cannot be removed from the network.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_HAS_DEPENDENTS                    , "ERROR_CLUSTER_NETWORK_HAS_DEPENDENTS"                    , "One or more cluster resources depend on the network to provide service to clients. The client access capability cannot be removed from the network.")
   CASE_ERRCODE(ERROR_INVALID_OPERATION_ON_QUORUM                       , "ERROR_INVALID_OPERATION_ON_QUORUM"                       , "This operation cannot be performed on the cluster resource as it the quorum resource. You may not bring the quorum resource offline or modify its possible owners list.")
   CASE_ERRCODE(ERROR_DEPENDENCY_NOT_ALLOWED                            , "ERROR_DEPENDENCY_NOT_ALLOWED"                            , "The cluster quorum resource is not allowed to have any dependencies.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_PAUSED                               , "ERROR_CLUSTER_NODE_PAUSED"                               , "The cluster node is paused.")
   CASE_ERRCODE(ERROR_NODE_CANT_HOST_RESOURCE                           , "ERROR_NODE_CANT_HOST_RESOURCE"                           , "The cluster resource cannot be brought online. The owner node cannot run this resource.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_NOT_READY                            , "ERROR_CLUSTER_NODE_NOT_READY"                            , "The cluster node is not ready to perform the requested operation.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_SHUTTING_DOWN                        , "ERROR_CLUSTER_NODE_SHUTTING_DOWN"                        , "The cluster node is shutting down.")
   CASE_ERRCODE(ERROR_CLUSTER_JOIN_ABORTED                              , "ERROR_CLUSTER_JOIN_ABORTED"                              , "The cluster join operation was aborted.")
   CASE_ERRCODE(ERROR_CLUSTER_INCOMPATIBLE_VERSIONS                     , "ERROR_CLUSTER_INCOMPATIBLE_VERSIONS"                     , "The cluster join operation failed due to incompatible software versions between the joining node and its sponsor.")
   CASE_ERRCODE(ERROR_CLUSTER_MAXNUM_OF_RESOURCES_EXCEEDED              , "ERROR_CLUSTER_MAXNUM_OF_RESOURCES_EXCEEDED"              , "This resource cannot be created because the cluster has reached the limit on the number of resources it can monitor.")
   CASE_ERRCODE(ERROR_CLUSTER_SYSTEM_CONFIG_CHANGED                     , "ERROR_CLUSTER_SYSTEM_CONFIG_CHANGED"                     , "The system configuration changed during the cluster join or form operation. The join or form operation was aborted.")
   CASE_ERRCODE(ERROR_CLUSTER_RESOURCE_TYPE_NOT_FOUND                   , "ERROR_CLUSTER_RESOURCE_TYPE_NOT_FOUND"                   , "The specified resource type was not found.")
   CASE_ERRCODE(ERROR_CLUSTER_RESTYPE_NOT_SUPPORTED                     , "ERROR_CLUSTER_RESTYPE_NOT_SUPPORTED"                     , "The specified node does not support a resource of this type. This may be due to version inconsistencies or due to the absence of the resource DLL on this node.")
   CASE_ERRCODE(ERROR_CLUSTER_RESNAME_NOT_FOUND                         , "ERROR_CLUSTER_RESNAME_NOT_FOUND"                         , "The specified resource name is supported by this resource DLL. This may be due to a bad (or changed) name supplied to the resource DLL.")
   CASE_ERRCODE(ERROR_CLUSTER_NO_RPC_PACKAGES_REGISTERED                , "ERROR_CLUSTER_NO_RPC_PACKAGES_REGISTERED"                , "No authentication package could be registered with the RPC server.")
   CASE_ERRCODE(ERROR_CLUSTER_OWNER_NOT_IN_PREFLIST                     , "ERROR_CLUSTER_OWNER_NOT_IN_PREFLIST"                     , "You cannot bring the group online because the owner of the group is not in the preferred list for the group. To change the owner node for the group, move the group.")
   CASE_ERRCODE(ERROR_CLUSTER_DATABASE_SEQMISMATCH                      , "ERROR_CLUSTER_DATABASE_SEQMISMATCH"                      , "The join operation failed because the cluster database sequence number has changed or is incompatible with the locker node. This may happen during a join operation if the cluster database was changing during the join.")
   CASE_ERRCODE(ERROR_RESMON_INVALID_STATE                              , "ERROR_RESMON_INVALID_STATE"                              , "The resource monitor will not allow the fail operation to be performed while the resource is in its current state. This may happen if the resource is in a pending state.")
   CASE_ERRCODE(ERROR_CLUSTER_GUM_NOT_LOCKER                            , "ERROR_CLUSTER_GUM_NOT_LOCKER"                            , "A non locker code got a request to reserve the lock for making global updates.")
   CASE_ERRCODE(ERROR_QUORUM_DISK_NOT_FOUND                             , "ERROR_QUORUM_DISK_NOT_FOUND"                             , "The quorum disk could not be located by the cluster service.")
   CASE_ERRCODE(ERROR_DATABASE_BACKUP_CORRUPT                           , "ERROR_DATABASE_BACKUP_CORRUPT"                           , "The backup up cluster database is possibly corrupt.")
   CASE_ERRCODE(ERROR_CLUSTER_NODE_ALREADY_HAS_DFS_ROOT                 , "ERROR_CLUSTER_NODE_ALREADY_HAS_DFS_ROOT"                 , "A DFS root already exists in this cluster node.")
   CASE_ERRCODE(ERROR_RESOURCE_PROPERTY_UNCHANGEABLE                    , "ERROR_RESOURCE_PROPERTY_UNCHANGEABLE"                    , "An attempt to modify a resource property failed because it conflicts with another existing property.")
   CASE_ERRCODE(ERROR_CLUSTER_MEMBERSHIP_INVALID_STATE                  , "ERROR_CLUSTER_MEMBERSHIP_INVALID_STATE"                  , "An operation was attempted that is incompatible with the current membership state of the node.")
   CASE_ERRCODE(ERROR_CLUSTER_QUORUMLOG_NOT_FOUND                       , "ERROR_CLUSTER_QUORUMLOG_NOT_FOUND"                       , "The quorum resource does not contain the quorum log.")
   CASE_ERRCODE(ERROR_CLUSTER_MEMBERSHIP_HALT                           , "ERROR_CLUSTER_MEMBERSHIP_HALT"                           , "The membership engine requested shutdown of the cluster service on this node.")
   CASE_ERRCODE(ERROR_CLUSTER_INSTANCE_ID_MISMATCH                      , "ERROR_CLUSTER_INSTANCE_ID_MISMATCH"                      , "The join operation failed because the cluster instance ID of the joining node does not match the cluster instance ID of the sponsor node.")
   CASE_ERRCODE(ERROR_CLUSTER_NETWORK_NOT_FOUND_FOR_IP                  , "ERROR_CLUSTER_NETWORK_NOT_FOUND_FOR_IP"                  , "A matching network for the specified IP address could not be found. Please also specify a subnet mask and a cluster network.")
   CASE_ERRCODE(ERROR_CLUSTER_PROPERTY_DATA_TYPE_MISMATCH               , "ERROR_CLUSTER_PROPERTY_DATA_TYPE_MISMATCH"               , "The actual data type of the property did not match the expected data type of the property.")
   CASE_ERRCODE(ERROR_CLUSTER_EVICT_WITHOUT_CLEANUP                     , "ERROR_CLUSTER_EVICT_WITHOUT_CLEANUP"                     , "The cluster node was evicted from the cluster successfully, but the node was not cleaned up. Extended status information explaining why the node was not cleaned up is available.")
   CASE_ERRCODE(ERROR_CLUSTER_PARAMETER_MISMATCH                        , "ERROR_CLUSTER_PARAMETER_MISMATCH"                        , "Two or more parameter values specified for a resource's properties are in conflict.")
   CASE_ERRCODE(ERROR_NODE_CANNOT_BE_CLUSTERED                          , "ERROR_NODE_CANNOT_BE_CLUSTERED"                          , "This computer cannot be made a member of a cluster.")
   CASE_ERRCODE(ERROR_CLUSTER_WRONG_OS_VERSION                          , "ERROR_CLUSTER_WRONG_OS_VERSION"                          , "This computer cannot be made a member of a cluster because it does not have the correct version of Windows installed.")
   CASE_ERRCODE(ERROR_CLUSTER_CANT_CREATE_DUP_CLUSTER_NAME              , "ERROR_CLUSTER_CANT_CREATE_DUP_CLUSTER_NAME"              , "A cluster cannot be created with the specified cluster name because that cluster name is already in use. Specify a different name for the cluster.")
   CASE_ERRCODE(ERROR_CLUSCFG_ALREADY_COMMITTED                         , "ERROR_CLUSCFG_ALREADY_COMMITTED"                         , "The cluster configuration action has already been committed.")
   CASE_ERRCODE(ERROR_CLUSCFG_ROLLBACK_FAILED                           , "ERROR_CLUSCFG_ROLLBACK_FAILED"                           , "The cluster configuration action could not be rolled back.")
   CASE_ERRCODE(ERROR_CLUSCFG_SYSTEM_DISK_DRIVE_LETTER_CONFLICT         , "ERROR_CLUSCFG_SYSTEM_DISK_DRIVE_LETTER_CONFLICT"         , "The drive letter assigned to a system disk on one node conflicted with the driver letter assigned to a disk on another node.")
   CASE_ERRCODE(ERROR_CLUSTER_OLD_VERSION                               , "ERROR_CLUSTER_OLD_VERSION"                               , "One or more nodes in the cluster are running a version of Windows that does not support this operation.")
   CASE_ERRCODE(ERROR_CLUSTER_MISMATCHED_COMPUTER_ACCT_NAME             , "ERROR_CLUSTER_MISMATCHED_COMPUTER_ACCT_NAME"             , "The name of the corresponding computer account doesn't match the Network Name for this resource.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_ENCRYPTION_FAILED                                 , "ERROR_ENCRYPTION_FAILED"                                 , "The specified file could not be encrypted.")
   CASE_ERRCODE(ERROR_DECRYPTION_FAILED                                 , "ERROR_DECRYPTION_FAILED"                                 , "The specified file could not be decrypted.")
   CASE_ERRCODE(ERROR_FILE_ENCRYPTED                                    , "ERROR_FILE_ENCRYPTED"                                    , "The specified file is encrypted and the user does not have the ability to decrypt it.")
   CASE_ERRCODE(ERROR_NO_RECOVERY_POLICY                                , "ERROR_NO_RECOVERY_POLICY"                                , "There is no valid encryption recovery policy configured for this system.")
   CASE_ERRCODE(ERROR_NO_EFS                                            , "ERROR_NO_EFS"                                            , "The required encryption driver is not loaded for this system.")
   CASE_ERRCODE(ERROR_WRONG_EFS                                         , "ERROR_WRONG_EFS"                                         , "The file was encrypted with a different encryption driver than is currently loaded.")
   CASE_ERRCODE(ERROR_NO_USER_KEYS                                      , "ERROR_NO_USER_KEYS"                                      , "There are no EFS keys defined for the user.")
   CASE_ERRCODE(ERROR_FILE_NOT_ENCRYPTED                                , "ERROR_FILE_NOT_ENCRYPTED"                                , "The specified file is not encrypted.")
   CASE_ERRCODE(ERROR_NOT_EXPORT_FORMAT                                 , "ERROR_NOT_EXPORT_FORMAT"                                 , "The specified file is not in the defined EFS export format.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_FILE_READ_ONLY                                    , "ERROR_FILE_READ_ONLY"                                    , "The specified file is read only.")
   CASE_ERRCODE(ERROR_DIR_EFS_DISALLOWED                                , "ERROR_DIR_EFS_DISALLOWED"                                , "The directory has been disabled for encryption.")
   CASE_ERRCODE(ERROR_EFS_SERVER_NOT_TRUSTED                            , "ERROR_EFS_SERVER_NOT_TRUSTED"                            , "The server is not trusted for remote encryption operation.")
   CASE_ERRCODE(ERROR_BAD_RECOVERY_POLICY                               , "ERROR_BAD_RECOVERY_POLICY"                               , "Recovery policy configured for this system contains invalid recovery certificate.")
   CASE_ERRCODE(ERROR_EFS_ALG_BLOB_TOO_BIG                              , "ERROR_EFS_ALG_BLOB_TOO_BIG"                              , "The encryption algorithm used on the source file needs a bigger key buffer than the one on the destination file.")
   CASE_ERRCODE(ERROR_VOLUME_NOT_SUPPORT_EFS                            , "ERROR_VOLUME_NOT_SUPPORT_EFS"                            , "The disk partition does not support file encryption.")
   CASE_ERRCODE(ERROR_EFS_DISABLED                                      , "ERROR_EFS_DISABLED"                                      , "This machine is disabled for file encryption.")
   CASE_ERRCODE(ERROR_EFS_VERSION_NOT_SUPPORT                           , "ERROR_EFS_VERSION_NOT_SUPPORT"                           , "A newer system is required to decrypt this encrypted file.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_NO_BROWSER_SERVERS_FOUND                          , "ERROR_NO_BROWSER_SERVERS_FOUND"                          , "The list of servers for this workgroup is not currently available.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(SCHED_E_SERVICE_NOT_LOCALSYSTEM                         , "SCHED_E_SERVICE_NOT_LOCALSYSTEM"                         , "The Task Scheduler service must be configured to run in the System account to function properly. Individual tasks may be configured to run in other accounts.")
   CASE_ERRCODE(ERROR_CTX_WINSTATION_NAME_INVALID                       , "ERROR_CTX_WINSTATION_NAME_INVALID"                       , "The specified session name is invalid.")
   CASE_ERRCODE(ERROR_CTX_INVALID_PD                                    , "ERROR_CTX_INVALID_PD"                                    , "The specified protocol driver is invalid.")
   CASE_ERRCODE(ERROR_CTX_PD_NOT_FOUND                                  , "ERROR_CTX_PD_NOT_FOUND"                                  , "The specified protocol driver was not found in the system path.")
   CASE_ERRCODE(ERROR_CTX_WD_NOT_FOUND                                  , "ERROR_CTX_WD_NOT_FOUND"                                  , "The specified terminal connection driver was not found in the system path.")
   CASE_ERRCODE(ERROR_CTX_CANNOT_MAKE_EVENTLOG_ENTRY                    , "ERROR_CTX_CANNOT_MAKE_EVENTLOG_ENTRY"                    , "A registry key for event logging could not be created for this session.")
   CASE_ERRCODE(ERROR_CTX_SERVICE_NAME_COLLISION                        , "ERROR_CTX_SERVICE_NAME_COLLISION"                        , "A service with the same name already exists on the system.")
   CASE_ERRCODE(ERROR_CTX_CLOSE_PENDING                                 , "ERROR_CTX_CLOSE_PENDING"                                 , "A close operation is pending on the session.")
   CASE_ERRCODE(ERROR_CTX_NO_OUTBUF                                     , "ERROR_CTX_NO_OUTBUF"                                     , "There are no free output buffers available.")
   CASE_ERRCODE(ERROR_CTX_MODEM_INF_NOT_FOUND                           , "ERROR_CTX_MODEM_INF_NOT_FOUND"                           , "The MODEM.INF file was not found.")
   CASE_ERRCODE(ERROR_CTX_INVALID_MODEMNAME                             , "ERROR_CTX_INVALID_MODEMNAME"                             , "The modem name was not found in MODEM.INF.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_ERROR                          , "ERROR_CTX_MODEM_RESPONSE_ERROR"                          , "The modem did not accept the command sent to it. Verify that the configured modem name matches the attached modem.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_TIMEOUT                        , "ERROR_CTX_MODEM_RESPONSE_TIMEOUT"                        , "The modem did not respond to the command sent to it. Verify that the modem is properly cabled and powered on.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_NO_CARRIER                     , "ERROR_CTX_MODEM_RESPONSE_NO_CARRIER"                     , "Carrier detect has failed or carrier has been dropped due to disconnect.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_NO_DIALTONE                    , "ERROR_CTX_MODEM_RESPONSE_NO_DIALTONE"                    , "Dial tone not detected within the required time. Verify that the phone cable is properly attached and functional.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_BUSY                           , "ERROR_CTX_MODEM_RESPONSE_BUSY"                           , "Busy signal detected at remote site on callback.")
   CASE_ERRCODE(ERROR_CTX_MODEM_RESPONSE_VOICE                          , "ERROR_CTX_MODEM_RESPONSE_VOICE"                          , "Voice detected at remote site on callback.")
   CASE_ERRCODE(ERROR_CTX_TD_ERROR                                      , "ERROR_CTX_TD_ERROR"                                      , "Transport driver error")
   CASE_ERRCODE(ERROR_CTX_WINSTATION_NOT_FOUND                          , "ERROR_CTX_WINSTATION_NOT_FOUND"                          , "The specified session cannot be found.")
   CASE_ERRCODE(ERROR_CTX_WINSTATION_ALREADY_EXISTS                     , "ERROR_CTX_WINSTATION_ALREADY_EXISTS"                     , "The specified session name is already in use.")
   CASE_ERRCODE(ERROR_CTX_WINSTATION_BUSY                               , "ERROR_CTX_WINSTATION_BUSY"                               , "The requested operation cannot be completed because the terminal connection is currently busy processing a connect, disconnect, reset, or delete operation.")
   CASE_ERRCODE(ERROR_CTX_BAD_VIDEO_MODE                                , "ERROR_CTX_BAD_VIDEO_MODE"                                , "An attempt has been made to connect to a session whose video mode is not supported by the current client.")
   CASE_ERRCODE(ERROR_CTX_GRAPHICS_INVALID                              , "ERROR_CTX_GRAPHICS_INVALID"                              , "The application attempted to enable DOS graphics mode. DOS graphics mode is not supported.")
   CASE_ERRCODE(ERROR_CTX_LOGON_DISABLED                                , "ERROR_CTX_LOGON_DISABLED"                                , "Your interactive logon privilege has been disabled. Please contact your administrator.")
   CASE_ERRCODE(ERROR_CTX_NOT_CONSOLE                                   , "ERROR_CTX_NOT_CONSOLE"                                   , "The requested operation can be performed only on the system console. This is most often the result of a driver or system DLL requiring direct console access.")
   CASE_ERRCODE(ERROR_CTX_CLIENT_QUERY_TIMEOUT                          , "ERROR_CTX_CLIENT_QUERY_TIMEOUT"                          , "The client failed to respond to the server connect message.")
   CASE_ERRCODE(ERROR_CTX_CONSOLE_DISCONNECT                            , "ERROR_CTX_CONSOLE_DISCONNECT"                            , "Disconnecting the console session is not supported.")
   CASE_ERRCODE(ERROR_CTX_CONSOLE_CONNECT                               , "ERROR_CTX_CONSOLE_CONNECT"                               , "Reconnecting a disconnected session to the console is not supported.")
   CASE_ERRCODE(ERROR_CTX_SHADOW_DENIED                                 , "ERROR_CTX_SHADOW_DENIED"                                 , "The request to control another session remotely was denied.")
   CASE_ERRCODE(ERROR_CTX_WINSTATION_ACCESS_DENIED                      , "ERROR_CTX_WINSTATION_ACCESS_DENIED"                      , "The requested session access is denied.")
   CASE_ERRCODE(ERROR_CTX_INVALID_WD                                    , "ERROR_CTX_INVALID_WD"                                    , "The specified terminal connection driver is invalid.")
   CASE_ERRCODE(ERROR_CTX_SHADOW_INVALID                                , "ERROR_CTX_SHADOW_INVALID"                                , "The requested session cannot be controlled remotely. This may be because the session is disconnected or does not currently have a user logged on.")
   CASE_ERRCODE(ERROR_CTX_SHADOW_DISABLED                               , "ERROR_CTX_SHADOW_DISABLED"                               , "The requested session is not configured to allow remote control.")
   CASE_ERRCODE(ERROR_CTX_CLIENT_LICENSE_IN_USE                         , "ERROR_CTX_CLIENT_LICENSE_IN_USE"                         , "Your request to connect to this Terminal Server has been rejected. Your Terminal Server client license number is currently being used by another user. Please call your system administrator to obtain a unique license number.")
   CASE_ERRCODE(ERROR_CTX_CLIENT_LICENSE_NOT_SET                        , "ERROR_CTX_CLIENT_LICENSE_NOT_SET"                        , "Your request to connect to this Terminal Server has been rejected. Your Terminal Server client license number has not been entered for this copy of the Terminal Server client. Please contact your system administrator.")
   CASE_ERRCODE(ERROR_CTX_LICENSE_NOT_AVAILABLE                         , "ERROR_CTX_LICENSE_NOT_AVAILABLE"                         , "The system has reached its licensed logon limit. Please try again later.")
   CASE_ERRCODE(ERROR_CTX_LICENSE_CLIENT_INVALID                        , "ERROR_CTX_LICENSE_CLIENT_INVALID"                        , "The client you are using is not licensed to use this system. Your logon request is denied.")
   CASE_ERRCODE(ERROR_CTX_LICENSE_EXPIRED                               , "ERROR_CTX_LICENSE_EXPIRED"                               , "The system license has expired. Your logon request is denied.")
   CASE_ERRCODE(ERROR_CTX_SHADOW_NOT_RUNNING                            , "ERROR_CTX_SHADOW_NOT_RUNNING"                            , "Remote control could not be terminated because the specified session is not currently being remotely controlled.")
   CASE_ERRCODE(ERROR_CTX_SHADOW_ENDED_BY_MODE_CHANGE                   , "ERROR_CTX_SHADOW_ENDED_BY_MODE_CHANGE"                   , "The remote control of the console was terminated because the display mode was changed. Changing the display mode in a remote control session is not supported.")
   CASE_ERRCODE(ERROR_ACTIVATION_COUNT_EXCEEDED                         , "ERROR_ACTIVATION_COUNT_EXCEEDED"                         , "Activation has already been reset the maximum number of times for this installation. Your activation timer will not be cleared.")
   CASE_ERRCODE(FRS_ERR_INVALID_API_SEQUENCE                            , "FRS_ERR_INVALID_API_SEQUENCE"                            , "The file replication service API was called incorrectly.")
   CASE_ERRCODE(FRS_ERR_STARTING_SERVICE                                , "FRS_ERR_STARTING_SERVICE"                                , "The file replication service cannot be started.")
   CASE_ERRCODE(FRS_ERR_STOPPING_SERVICE                                , "FRS_ERR_STOPPING_SERVICE"                                , "The file replication service cannot be stopped.")
   CASE_ERRCODE(FRS_ERR_INTERNAL_API                                    , "FRS_ERR_INTERNAL_API"                                    , "The file replication service API terminated the request. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_INTERNAL                                        , "FRS_ERR_INTERNAL"                                        , "The file replication service terminated the request. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_SERVICE_COMM                                    , "FRS_ERR_SERVICE_COMM"                                    , "The file replication service cannot be contacted. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_INSUFFICIENT_PRIV                               , "FRS_ERR_INSUFFICIENT_PRIV"                               , "The file replication service cannot satisfy the request because the user has insufficient privileges. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_AUTHENTICATION                                  , "FRS_ERR_AUTHENTICATION"                                  , "The file replication service cannot satisfy the request because authenticated RPC is not available. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_PARENT_INSUFFICIENT_PRIV                        , "FRS_ERR_PARENT_INSUFFICIENT_PRIV"                        , "The file replication service cannot satisfy the request because the user has insufficient privileges on the domain controller. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_PARENT_AUTHENTICATION                           , "FRS_ERR_PARENT_AUTHENTICATION"                           , "The file replication service cannot satisfy the request because authenticated RPC is not available on the domain controller. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_CHILD_TO_PARENT_COMM                            , "FRS_ERR_CHILD_TO_PARENT_COMM"                            , "The file replication service cannot communicate with the file replication service on the domain controller. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_PARENT_TO_CHILD_COMM                            , "FRS_ERR_PARENT_TO_CHILD_COMM"                            , "The file replication service on the domain controller cannot communicate with the file replication service on this computer. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_SYSVOL_POPULATE                                 , "FRS_ERR_SYSVOL_POPULATE"                                 , "The file replication service cannot populate the system volume because of an internal error. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_SYSVOL_POPULATE_TIMEOUT                         , "FRS_ERR_SYSVOL_POPULATE_TIMEOUT"                         , "The file replication service cannot populate the system volume because of an internal timeout. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_SYSVOL_IS_BUSY                                  , "FRS_ERR_SYSVOL_IS_BUSY"                                  , "The file replication service cannot process the request. The system volume is busy with a previous request.")
   CASE_ERRCODE(FRS_ERR_SYSVOL_DEMOTE                                   , "FRS_ERR_SYSVOL_DEMOTE"                                   , "The file replication service cannot stop replicating the system volume because of an internal error. The event log may have more information.")
   CASE_ERRCODE(FRS_ERR_INVALID_SERVICE_PARAMETER                       , "FRS_ERR_INVALID_SERVICE_PARAMETER"                       , "The file replication service detected an invalid parameter.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_DS_NOT_INSTALLED                                  , "ERROR_DS_NOT_INSTALLED"                                  , "An error occurred while installing the directory service. For more information, see the event log.")
   CASE_ERRCODE(ERROR_DS_MEMBERSHIP_EVALUATED_LOCALLY                   , "ERROR_DS_MEMBERSHIP_EVALUATED_LOCALLY"                   , "The directory service evaluated group memberships locally.")
   CASE_ERRCODE(ERROR_DS_NO_ATTRIBUTE_OR_VALUE                          , "ERROR_DS_NO_ATTRIBUTE_OR_VALUE"                          , "The specified directory service attribute or value does not exist.")
   CASE_ERRCODE(ERROR_DS_INVALID_ATTRIBUTE_SYNTAX                       , "ERROR_DS_INVALID_ATTRIBUTE_SYNTAX"                       , "The attribute syntax specified to the directory service is invalid.")
   CASE_ERRCODE(ERROR_DS_ATTRIBUTE_TYPE_UNDEFINED                       , "ERROR_DS_ATTRIBUTE_TYPE_UNDEFINED"                       , "The attribute type specified to the directory service is not defined.")
   CASE_ERRCODE(ERROR_DS_ATTRIBUTE_OR_VALUE_EXISTS                      , "ERROR_DS_ATTRIBUTE_OR_VALUE_EXISTS"                      , "The specified directory service attribute or value already exists.")
   CASE_ERRCODE(ERROR_DS_BUSY                                           , "ERROR_DS_BUSY"                                           , "The directory service is busy.")
   CASE_ERRCODE(ERROR_DS_UNAVAILABLE                                    , "ERROR_DS_UNAVAILABLE"                                    , "The directory service is unavailable.")
   CASE_ERRCODE(ERROR_DS_NO_RIDS_ALLOCATED                              , "ERROR_DS_NO_RIDS_ALLOCATED"                              , "The directory service was unable to allocate a relative identifier.")
   CASE_ERRCODE(ERROR_DS_NO_MORE_RIDS                                   , "ERROR_DS_NO_MORE_RIDS"                                   , "The directory service has exhausted the pool of relative identifiers.")
   CASE_ERRCODE(ERROR_DS_INCORRECT_ROLE_OWNER                           , "ERROR_DS_INCORRECT_ROLE_OWNER"                           , "The requested operation could not be performed because the directory service is not the master for that type of operation.")
   CASE_ERRCODE(ERROR_DS_RIDMGR_INIT_ERROR                              , "ERROR_DS_RIDMGR_INIT_ERROR"                              , "The directory service was unable to initialize the subsystem that allocates relative identifiers.")
   CASE_ERRCODE(ERROR_DS_OBJ_CLASS_VIOLATION                            , "ERROR_DS_OBJ_CLASS_VIOLATION"                            , "The requested operation did not satisfy one or more constraints associated with the class of the object.")
   CASE_ERRCODE(ERROR_DS_CANT_ON_NON_LEAF                               , "ERROR_DS_CANT_ON_NON_LEAF"                               , "The directory service can perform the requested operation only on a leaf object.")
   CASE_ERRCODE(ERROR_DS_CANT_ON_RDN                                    , "ERROR_DS_CANT_ON_RDN"                                    , "The directory service cannot perform the requested operation on the RDN attribute of an object.")
   CASE_ERRCODE(ERROR_DS_CANT_MOD_OBJ_CLASS                             , "ERROR_DS_CANT_MOD_OBJ_CLASS"                             , "The directory service detected an attempt to modify the object class of an object.")
   CASE_ERRCODE(ERROR_DS_CROSS_DOM_MOVE_ERROR                           , "ERROR_DS_CROSS_DOM_MOVE_ERROR"                           , "The requested cross-domain move operation could not be performed.")
   CASE_ERRCODE(ERROR_DS_GC_NOT_AVAILABLE                               , "ERROR_DS_GC_NOT_AVAILABLE"                               , "Unable to contact the global catalog server.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(ERROR_SHARED_POLICY                                     , "ERROR_SHARED_POLICY"                                     , "The policy object is shared and can only be modified at the root.")
   CASE_ERRCODE(ERROR_POLICY_OBJECT_NOT_FOUND                           , "ERROR_POLICY_OBJECT_NOT_FOUND"                           , "The policy object does not exist.")
   CASE_ERRCODE(ERROR_POLICY_ONLY_IN_DS                                 , "ERROR_POLICY_ONLY_IN_DS"                                 , "The requested policy information is only in the directory service.")
   CASE_ERRCODE(ERROR_PROMOTION_ACTIVE                                  , "ERROR_PROMOTION_ACTIVE"                                  , "A domain controller promotion is currently active.")
   CASE_ERRCODE(ERROR_NO_PROMOTION_ACTIVE                               , "ERROR_NO_PROMOTION_ACTIVE"                               , "A domain controller promotion is not currently active")
   CASE_ERRCODE(ERROR_DS_OPERATIONS_ERROR                               , "ERROR_DS_OPERATIONS_ERROR"                               , "An operations error occurred.")
   CASE_ERRCODE(ERROR_DS_PROTOCOL_ERROR                                 , "ERROR_DS_PROTOCOL_ERROR"                                 , "A protocol error occurred.")
   CASE_ERRCODE(ERROR_DS_TIMELIMIT_EXCEEDED                             , "ERROR_DS_TIMELIMIT_EXCEEDED"                             , "The time limit for this request was exceeded.")
   CASE_ERRCODE(ERROR_DS_SIZELIMIT_EXCEEDED                             , "ERROR_DS_SIZELIMIT_EXCEEDED"                             , "The size limit for this request was exceeded.")
   CASE_ERRCODE(ERROR_DS_ADMIN_LIMIT_EXCEEDED                           , "ERROR_DS_ADMIN_LIMIT_EXCEEDED"                           , "The administrative limit for this request was exceeded.")
   CASE_ERRCODE(ERROR_DS_COMPARE_FALSE                                  , "ERROR_DS_COMPARE_FALSE"                                  , "The compare response was false.")
   CASE_ERRCODE(ERROR_DS_COMPARE_TRUE                                   , "ERROR_DS_COMPARE_TRUE"                                   , "The compare response was true.")
   CASE_ERRCODE(ERROR_DS_AUTH_METHOD_NOT_SUPPORTED                      , "ERROR_DS_AUTH_METHOD_NOT_SUPPORTED"                      , "The requested authentication method is not supported by the server.")
   CASE_ERRCODE(ERROR_DS_STRONG_AUTH_REQUIRED                           , "ERROR_DS_STRONG_AUTH_REQUIRED"                           , "A more secure authentication method is required for this server.")
   CASE_ERRCODE(ERROR_DS_INAPPROPRIATE_AUTH                             , "ERROR_DS_INAPPROPRIATE_AUTH"                             , "Inappropriate authentication.")
   CASE_ERRCODE(ERROR_DS_AUTH_UNKNOWN                                   , "ERROR_DS_AUTH_UNKNOWN"                                   , "The authentication mechanism is unknown.")
   CASE_ERRCODE(ERROR_DS_REFERRAL                                       , "ERROR_DS_REFERRAL"                                       , "A referral was returned from the server.")
   CASE_ERRCODE(ERROR_DS_UNAVAILABLE_CRIT_EXTENSION                     , "ERROR_DS_UNAVAILABLE_CRIT_EXTENSION"                     , "The server does not support the requested critical extension.")
   CASE_ERRCODE(ERROR_DS_CONFIDENTIALITY_REQUIRED                       , "ERROR_DS_CONFIDENTIALITY_REQUIRED"                       , "This request requires a secure connection.")
   CASE_ERRCODE(ERROR_DS_INAPPROPRIATE_MATCHING                         , "ERROR_DS_INAPPROPRIATE_MATCHING"                         , "Inappropriate matching.")
   CASE_ERRCODE(ERROR_DS_CONSTRAINT_VIOLATION                           , "ERROR_DS_CONSTRAINT_VIOLATION"                           , "A constraint violation occurred.")
   CASE_ERRCODE(ERROR_DS_NO_SUCH_OBJECT                                 , "ERROR_DS_NO_SUCH_OBJECT"                                 , "There is no such object on the server.")
   CASE_ERRCODE(ERROR_DS_ALIAS_PROBLEM                                  , "ERROR_DS_ALIAS_PROBLEM"                                  , "There is an alias problem.")
   CASE_ERRCODE(ERROR_DS_INVALID_DN_SYNTAX                              , "ERROR_DS_INVALID_DN_SYNTAX"                              , "An invalid dn syntax has been specified.")
   CASE_ERRCODE(ERROR_DS_IS_LEAF                                        , "ERROR_DS_IS_LEAF"                                        , "The object is a leaf object.")
   CASE_ERRCODE(ERROR_DS_ALIAS_DEREF_PROBLEM                            , "ERROR_DS_ALIAS_DEREF_PROBLEM"                            , "There is an alias dereferencing problem.")
   CASE_ERRCODE(ERROR_DS_UNWILLING_TO_PERFORM                           , "ERROR_DS_UNWILLING_TO_PERFORM"                           , "The server is unwilling to process the request.")
   CASE_ERRCODE(ERROR_DS_LOOP_DETECT                                    , "ERROR_DS_LOOP_DETECT"                                    , "A loop has been detected.")
   CASE_ERRCODE(ERROR_DS_NAMING_VIOLATION                               , "ERROR_DS_NAMING_VIOLATION"                               , "There is a naming violation.")
   CASE_ERRCODE(ERROR_DS_OBJECT_RESULTS_TOO_LARGE                       , "ERROR_DS_OBJECT_RESULTS_TOO_LARGE"                       , "The result set is too large.")
   CASE_ERRCODE(ERROR_DS_AFFECTS_MULTIPLE_DSAS                          , "ERROR_DS_AFFECTS_MULTIPLE_DSAS"                          , "The operation affects multiple DSAs")
   CASE_ERRCODE(ERROR_DS_SERVER_DOWN                                    , "ERROR_DS_SERVER_DOWN"                                    , "The server is not operational.")
   CASE_ERRCODE(ERROR_DS_LOCAL_ERROR                                    , "ERROR_DS_LOCAL_ERROR"                                    , "A local error has occurred.")
   CASE_ERRCODE(ERROR_DS_ENCODING_ERROR                                 , "ERROR_DS_ENCODING_ERROR"                                 , "An encoding error has occurred.")
   CASE_ERRCODE(ERROR_DS_DECODING_ERROR                                 , "ERROR_DS_DECODING_ERROR"                                 , "A decoding error has occurred.")
   CASE_ERRCODE(ERROR_DS_FILTER_UNKNOWN                                 , "ERROR_DS_FILTER_UNKNOWN"                                 , "The search filter cannot be recognized.")
   CASE_ERRCODE(ERROR_DS_PARAM_ERROR                                    , "ERROR_DS_PARAM_ERROR"                                    , "One or more parameters are illegal.")
   CASE_ERRCODE(ERROR_DS_NOT_SUPPORTED                                  , "ERROR_DS_NOT_SUPPORTED"                                  , "The specified method is not supported.")
   CASE_ERRCODE(ERROR_DS_NO_RESULTS_RETURNED                            , "ERROR_DS_NO_RESULTS_RETURNED"                            , "No results were returned.")
   CASE_ERRCODE(ERROR_DS_CONTROL_NOT_FOUND                              , "ERROR_DS_CONTROL_NOT_FOUND"                              , "The specified control is not supported by the server.")
   CASE_ERRCODE(ERROR_DS_CLIENT_LOOP                                    , "ERROR_DS_CLIENT_LOOP"                                    , "A referral loop was detected by the client.")
   CASE_ERRCODE(ERROR_DS_REFERRAL_LIMIT_EXCEEDED                        , "ERROR_DS_REFERRAL_LIMIT_EXCEEDED"                        , "The preset referral limit was exceeded.")
   CASE_ERRCODE(ERROR_DS_SORT_CONTROL_MISSING                           , "ERROR_DS_SORT_CONTROL_MISSING"                           , "The search requires a SORT control.")
   CASE_ERRCODE(ERROR_DS_OFFSET_RANGE_ERROR                             , "ERROR_DS_OFFSET_RANGE_ERROR"                             , "The search results exceed the offset range specified.")
   CASE_ERRCODE(ERROR_DS_ROOT_MUST_BE_NC                                , "ERROR_DS_ROOT_MUST_BE_NC"                                , "The root object must be the head of a naming context. The root object cannot have an instantiated parent.")
   CASE_ERRCODE(ERROR_DS_ADD_REPLICA_INHIBITED                          , "ERROR_DS_ADD_REPLICA_INHIBITED"                          , "The add replica operation cannot be performed. The naming context must be writeable in order to create the replica.")
   CASE_ERRCODE(ERROR_DS_ATT_NOT_DEF_IN_SCHEMA                          , "ERROR_DS_ATT_NOT_DEF_IN_SCHEMA"                          , "A reference to an attribute that is not defined in the schema occurred.")
   CASE_ERRCODE(ERROR_DS_MAX_OBJ_SIZE_EXCEEDED                          , "ERROR_DS_MAX_OBJ_SIZE_EXCEEDED"                          , "The maximum size of an object has been exceeded.")
   CASE_ERRCODE(ERROR_DS_OBJ_STRING_NAME_EXISTS                         , "ERROR_DS_OBJ_STRING_NAME_EXISTS"                         , "An attempt was made to add an object to the directory with a name that is already in use.")
   CASE_ERRCODE(ERROR_DS_NO_RDN_DEFINED_IN_SCHEMA                       , "ERROR_DS_NO_RDN_DEFINED_IN_SCHEMA"                       , "An attempt was made to add an object of a class that does not have an RDN defined in the schema.")
   CASE_ERRCODE(ERROR_DS_RDN_DOESNT_MATCH_SCHEMA                        , "ERROR_DS_RDN_DOESNT_MATCH_SCHEMA"                        , "An attempt was made to add an object using an RDN that is not the RDN defined in the schema.")
   CASE_ERRCODE(ERROR_DS_NO_REQUESTED_ATTS_FOUND                        , "ERROR_DS_NO_REQUESTED_ATTS_FOUND"                        , "None of the requested attributes were found on the objects.")
   CASE_ERRCODE(ERROR_DS_USER_BUFFER_TO_SMALL                           , "ERROR_DS_USER_BUFFER_TO_SMALL"                           , "The user buffer is too small.")
   CASE_ERRCODE(ERROR_DS_ATT_IS_NOT_ON_OBJ                              , "ERROR_DS_ATT_IS_NOT_ON_OBJ"                              , "The attribute specified in the operation is not present on the object.")
   CASE_ERRCODE(ERROR_DS_ILLEGAL_MOD_OPERATION                          , "ERROR_DS_ILLEGAL_MOD_OPERATION"                          , "Illegal modify operation. Some aspect of the modification is not permitted.")
   CASE_ERRCODE(ERROR_DS_OBJ_TOO_LARGE                                  , "ERROR_DS_OBJ_TOO_LARGE"                                  , "The specified object is too large.")
   CASE_ERRCODE(ERROR_DS_BAD_INSTANCE_TYPE                              , "ERROR_DS_BAD_INSTANCE_TYPE"                              , "The specified instance type is not valid.")
   CASE_ERRCODE(ERROR_DS_MASTERDSA_REQUIRED                             , "ERROR_DS_MASTERDSA_REQUIRED"                             , "The operation must be performed at a master DSA.")
   CASE_ERRCODE(ERROR_DS_OBJECT_CLASS_REQUIRED                          , "ERROR_DS_OBJECT_CLASS_REQUIRED"                          , "The object class attribute must be specified.")
   CASE_ERRCODE(ERROR_DS_MISSING_REQUIRED_ATT                           , "ERROR_DS_MISSING_REQUIRED_ATT"                           , "A required attribute is missing.")
   CASE_ERRCODE(ERROR_DS_ATT_NOT_DEF_FOR_CLASS                          , "ERROR_DS_ATT_NOT_DEF_FOR_CLASS"                          , "An attempt was made to modify an object to include an attribute that is not legal for its class")
   CASE_ERRCODE(ERROR_DS_ATT_ALREADY_EXISTS                             , "ERROR_DS_ATT_ALREADY_EXISTS"                             , "The specified attribute is already present on the object.")
   CASE_ERRCODE(ERROR_DS_CANT_ADD_ATT_VALUES                            , "ERROR_DS_CANT_ADD_ATT_VALUES"                            , "The specified attribute is not present, or has no values.")
   CASE_ERRCODE(ERROR_DS_SINGLE_VALUE_CONSTRAINT                        , "ERROR_DS_SINGLE_VALUE_CONSTRAINT"                        , "Multiple values were specified for an attribute that can have only one value.")
   CASE_ERRCODE(ERROR_DS_RANGE_CONSTRAINT                               , "ERROR_DS_RANGE_CONSTRAINT"                               , "A value for the attribute was not in the acceptable range of values.")
   CASE_ERRCODE(ERROR_DS_ATT_VAL_ALREADY_EXISTS                         , "ERROR_DS_ATT_VAL_ALREADY_EXISTS"                         , "The specified value already exists.")
   CASE_ERRCODE(ERROR_DS_CANT_REM_MISSING_ATT                           , "ERROR_DS_CANT_REM_MISSING_ATT"                           , "The attribute cannot be removed because it is not present on the object.")
   CASE_ERRCODE(ERROR_DS_CANT_REM_MISSING_ATT_VAL                       , "ERROR_DS_CANT_REM_MISSING_ATT_VAL"                       , "The attribute value cannot be removed because it is not present on the object.")
   CASE_ERRCODE(ERROR_DS_ROOT_CANT_BE_SUBREF                            , "ERROR_DS_ROOT_CANT_BE_SUBREF"                            , "The specified root object cannot be a subref.")
   CASE_ERRCODE(ERROR_DS_NO_CHAINING                                    , "ERROR_DS_NO_CHAINING"                                    , "Chaining is not permitted.")
   CASE_ERRCODE(ERROR_DS_NO_CHAINED_EVAL                                , "ERROR_DS_NO_CHAINED_EVAL"                                , "Chained evaluation is not permitted.")
   CASE_ERRCODE(ERROR_DS_NO_PARENT_OBJECT                               , "ERROR_DS_NO_PARENT_OBJECT"                               , "The operation could not be performed because the object's parent is either uninstantiated or deleted.")
   CASE_ERRCODE(ERROR_DS_PARENT_IS_AN_ALIAS                             , "ERROR_DS_PARENT_IS_AN_ALIAS"                             , "Having a parent that is an alias is not permitted. Aliases are leaf objects.")
   CASE_ERRCODE(ERROR_DS_CANT_MIX_MASTER_AND_REPS                       , "ERROR_DS_CANT_MIX_MASTER_AND_REPS"                       , "The object and parent must be of the same type, either both masters or both replicas.")
   CASE_ERRCODE(ERROR_DS_CHILDREN_EXIST                                 , "ERROR_DS_CHILDREN_EXIST"                                 , "The operation cannot be performed because child objects exist. This operation can only be performed on a leaf object.")
   CASE_ERRCODE(ERROR_DS_OBJ_NOT_FOUND                                  , "ERROR_DS_OBJ_NOT_FOUND"                                  , "Directory object not found.")
   CASE_ERRCODE(ERROR_DS_ALIASED_OBJ_MISSING                            , "ERROR_DS_ALIASED_OBJ_MISSING"                            , "The aliased object is missing.")
   CASE_ERRCODE(ERROR_DS_BAD_NAME_SYNTAX                                , "ERROR_DS_BAD_NAME_SYNTAX"                                , "The object name has bad syntax.")
   CASE_ERRCODE(ERROR_DS_ALIAS_POINTS_TO_ALIAS                          , "ERROR_DS_ALIAS_POINTS_TO_ALIAS"                          , "It is not permitted for an alias to refer to another alias.")
   CASE_ERRCODE(ERROR_DS_CANT_DEREF_ALIAS                               , "ERROR_DS_CANT_DEREF_ALIAS"                               , "The alias cannot be dereferenced.")
   CASE_ERRCODE(ERROR_DS_OUT_OF_SCOPE                                   , "ERROR_DS_OUT_OF_SCOPE"                                   , "The operation is out of scope.")
   CASE_ERRCODE(ERROR_DS_OBJECT_BEING_REMOVED                           , "ERROR_DS_OBJECT_BEING_REMOVED"                           , "The operation cannot continue because the object is in the process of being removed.")
   CASE_ERRCODE(ERROR_DS_CANT_DELETE_DSA_OBJ                            , "ERROR_DS_CANT_DELETE_DSA_OBJ"                            , "The DSA object cannot be deleted.")
   CASE_ERRCODE(ERROR_DS_GENERIC_ERROR                                  , "ERROR_DS_GENERIC_ERROR"                                  , "A directory service error has occurred.")
   CASE_ERRCODE(ERROR_DS_DSA_MUST_BE_INT_MASTER                         , "ERROR_DS_DSA_MUST_BE_INT_MASTER"                         , "The operation can only be performed on an internal master DSA object.")
   CASE_ERRCODE(ERROR_DS_CLASS_NOT_DSA                                  , "ERROR_DS_CLASS_NOT_DSA"                                  , "The object must be of class DSA.")
   CASE_ERRCODE(ERROR_DS_INSUFF_ACCESS_RIGHTS                           , "ERROR_DS_INSUFF_ACCESS_RIGHTS"                           , "Insufficient access rights to perform the operation.")
   CASE_ERRCODE(ERROR_DS_ILLEGAL_SUPERIOR                               , "ERROR_DS_ILLEGAL_SUPERIOR"                               , "The object cannot be added because the parent is not on the list of possible superiors.")
   CASE_ERRCODE(ERROR_DS_ATTRIBUTE_OWNED_BY_SAM                         , "ERROR_DS_ATTRIBUTE_OWNED_BY_SAM"                         , "Access to the attribute is not permitted because the attribute is owned by the Security Accounts Manager (SAM).")
   CASE_ERRCODE(ERROR_DS_NAME_TOO_MANY_PARTS                            , "ERROR_DS_NAME_TOO_MANY_PARTS"                            , "The name has too many parts.")
   CASE_ERRCODE(ERROR_DS_NAME_TOO_LONG                                  , "ERROR_DS_NAME_TOO_LONG"                                  , "The name is too long.")
   CASE_ERRCODE(ERROR_DS_NAME_VALUE_TOO_LONG                            , "ERROR_DS_NAME_VALUE_TOO_LONG"                            , "The name value is too long.")
   CASE_ERRCODE(ERROR_DS_NAME_UNPARSEABLE                               , "ERROR_DS_NAME_UNPARSEABLE"                               , "The directory service encountered an error parsing a name.")
   CASE_ERRCODE(ERROR_DS_NAME_TYPE_UNKNOWN                              , "ERROR_DS_NAME_TYPE_UNKNOWN"                              , "The directory service cannot get the attribute type for a name.")
   CASE_ERRCODE(ERROR_DS_NOT_AN_OBJECT                                  , "ERROR_DS_NOT_AN_OBJECT"                                  , "The name does not identify an object; the name identifies a phantom.")
   CASE_ERRCODE(ERROR_DS_SEC_DESC_TOO_SHORT                             , "ERROR_DS_SEC_DESC_TOO_SHORT"                             , "The security descriptor is too short.")
   CASE_ERRCODE(ERROR_DS_SEC_DESC_INVALID                               , "ERROR_DS_SEC_DESC_INVALID"                               , "The security descriptor is invalid.")
   CASE_ERRCODE(ERROR_DS_NO_DELETED_NAME                                , "ERROR_DS_NO_DELETED_NAME"                                , "Failed to create name for deleted object.")
   CASE_ERRCODE(ERROR_DS_SUBREF_MUST_HAVE_PARENT                        , "ERROR_DS_SUBREF_MUST_HAVE_PARENT"                        , "The parent of a new subref must exist.")
   CASE_ERRCODE(ERROR_DS_NCNAME_MUST_BE_NC                              , "ERROR_DS_NCNAME_MUST_BE_NC"                              , "The object must be a naming context.")
   CASE_ERRCODE(ERROR_DS_CANT_ADD_SYSTEM_ONLY                           , "ERROR_DS_CANT_ADD_SYSTEM_ONLY"                           , "It is not permitted to add an attribute which is owned by the system.")
   CASE_ERRCODE(ERROR_DS_CLASS_MUST_BE_CONCRETE                         , "ERROR_DS_CLASS_MUST_BE_CONCRETE"                         , "The class of the object must be structural; you cannot instantiate an abstract class.")
   CASE_ERRCODE(ERROR_DS_INVALID_DMD                                    , "ERROR_DS_INVALID_DMD"                                    , "The schema object could not be found.")
   CASE_ERRCODE(ERROR_DS_OBJ_GUID_EXISTS                                , "ERROR_DS_OBJ_GUID_EXISTS"                                , "A local object with this GUID (dead or alive) already exists.")
   CASE_ERRCODE(ERROR_DS_NOT_ON_BACKLINK                                , "ERROR_DS_NOT_ON_BACKLINK"                                , "The operation cannot be performed on a back link.")
   CASE_ERRCODE(ERROR_DS_NO_CROSSREF_FOR_NC                             , "ERROR_DS_NO_CROSSREF_FOR_NC"                             , "The cross reference for the specified naming context could not be found.")
   CASE_ERRCODE(ERROR_DS_SHUTTING_DOWN                                  , "ERROR_DS_SHUTTING_DOWN"                                  , "The operation could not be performed because the directory service is shutting down.")
   CASE_ERRCODE(ERROR_DS_UNKNOWN_OPERATION                              , "ERROR_DS_UNKNOWN_OPERATION"                              , "The directory service request is invalid.")
   CASE_ERRCODE(ERROR_DS_INVALID_ROLE_OWNER                             , "ERROR_DS_INVALID_ROLE_OWNER"                             , "The role owner attribute could not be read.")
   CASE_ERRCODE(ERROR_DS_COULDNT_CONTACT_FSMO                           , "ERROR_DS_COULDNT_CONTACT_FSMO"                           , "The requested FSMO operation failed. The current FSMO holder could not be reached.")
   CASE_ERRCODE(ERROR_DS_CROSS_NC_DN_RENAME                             , "ERROR_DS_CROSS_NC_DN_RENAME"                             , "Modification of a DN across a naming context is not permitted.")
   CASE_ERRCODE(ERROR_DS_CANT_MOD_SYSTEM_ONLY                           , "ERROR_DS_CANT_MOD_SYSTEM_ONLY"                           , "The attribute cannot be modified because it is owned by the system.")
   CASE_ERRCODE(ERROR_DS_REPLICATOR_ONLY                                , "ERROR_DS_REPLICATOR_ONLY"                                , "Only the replicator can perform this function.")
   CASE_ERRCODE(ERROR_DS_OBJ_CLASS_NOT_DEFINED                          , "ERROR_DS_OBJ_CLASS_NOT_DEFINED"                          , "The specified class is not defined.")
   CASE_ERRCODE(ERROR_DS_OBJ_CLASS_NOT_SUBCLASS                         , "ERROR_DS_OBJ_CLASS_NOT_SUBCLASS"                         , "The specified class is not a subclass.")
   CASE_ERRCODE(ERROR_DS_NAME_REFERENCE_INVALID                         , "ERROR_DS_NAME_REFERENCE_INVALID"                         , "The name reference is invalid.")
   CASE_ERRCODE(ERROR_DS_CROSS_REF_EXISTS                               , "ERROR_DS_CROSS_REF_EXISTS"                               , "A cross reference already exists.")
   CASE_ERRCODE(ERROR_DS_CANT_DEL_MASTER_CROSSREF                       , "ERROR_DS_CANT_DEL_MASTER_CROSSREF"                       , "It is not permitted to delete a master cross reference.")
   CASE_ERRCODE(ERROR_DS_SUBTREE_NOTIFY_NOT_NC_HEAD                     , "ERROR_DS_SUBTREE_NOTIFY_NOT_NC_HEAD"                     , "Subtree notifications are only supported on NC heads.")
   CASE_ERRCODE(ERROR_DS_NOTIFY_FILTER_TOO_COMPLEX                      , "ERROR_DS_NOTIFY_FILTER_TOO_COMPLEX"                      , "Notification filter is too complex.")
   CASE_ERRCODE(ERROR_DS_DUP_RDN                                        , "ERROR_DS_DUP_RDN"                                        , "Schema update failed: duplicate RDN.")
   CASE_ERRCODE(ERROR_DS_DUP_OID                                        , "ERROR_DS_DUP_OID"                                        , "Schema update failed: duplicate OID")
   CASE_ERRCODE(ERROR_DS_DUP_MAPI_ID                                    , "ERROR_DS_DUP_MAPI_ID"                                    , "Schema update failed: duplicate MAPI identifier.")
   CASE_ERRCODE(ERROR_DS_DUP_SCHEMA_ID_GUID                             , "ERROR_DS_DUP_SCHEMA_ID_GUID"                             , "Schema update failed: duplicate schema-id GUID.")
   CASE_ERRCODE(ERROR_DS_DUP_LDAP_DISPLAY_NAME                          , "ERROR_DS_DUP_LDAP_DISPLAY_NAME"                          , "Schema update failed: duplicate LDAP display name.")
   CASE_ERRCODE(ERROR_DS_SEMANTIC_ATT_TEST                              , "ERROR_DS_SEMANTIC_ATT_TEST"                              , "Schema update failed: range-lower less than range upper")
   CASE_ERRCODE(ERROR_DS_SYNTAX_MISMATCH                                , "ERROR_DS_SYNTAX_MISMATCH"                                , "Schema update failed: syntax mismatch")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_MUST_HAVE                            , "ERROR_DS_EXISTS_IN_MUST_HAVE"                            , "Schema deletion failed: attribute is used in must-contain")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_MAY_HAVE                             , "ERROR_DS_EXISTS_IN_MAY_HAVE"                             , "Schema deletion failed: attribute is used in may-contain")
   CASE_ERRCODE(ERROR_DS_NONEXISTENT_MAY_HAVE                           , "ERROR_DS_NONEXISTENT_MAY_HAVE"                           , "Schema update failed: attribute in may-contain does not exist")
   CASE_ERRCODE(ERROR_DS_NONEXISTENT_MUST_HAVE                          , "ERROR_DS_NONEXISTENT_MUST_HAVE"                          , "Schema update failed: attribute in must-contain does not exist")
   CASE_ERRCODE(ERROR_DS_AUX_CLS_TEST_FAIL                              , "ERROR_DS_AUX_CLS_TEST_FAIL"                              , "Schema update failed: class in aux-class list does not exist or is not an auxiliary class")
   CASE_ERRCODE(ERROR_DS_NONEXISTENT_POSS_SUP                           , "ERROR_DS_NONEXISTENT_POSS_SUP"                           , "Schema update failed: class in poss-superiors does not exist")
   CASE_ERRCODE(ERROR_DS_SUB_CLS_TEST_FAIL                              , "ERROR_DS_SUB_CLS_TEST_FAIL"                              , "Schema update failed: class in subclassof list does not exist or does not satisfy hierarchy rules")
   CASE_ERRCODE(ERROR_DS_BAD_RDN_ATT_ID_SYNTAX                          , "ERROR_DS_BAD_RDN_ATT_ID_SYNTAX"                          , "Schema update failed: Rdn-Att-Id has wrong syntax")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_AUX_CLS                              , "ERROR_DS_EXISTS_IN_AUX_CLS"                              , "Schema deletion failed: class is used as auxiliary class")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_SUB_CLS                              , "ERROR_DS_EXISTS_IN_SUB_CLS"                              , "Schema deletion failed: class is used as sub class")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_POSS_SUP                             , "ERROR_DS_EXISTS_IN_POSS_SUP"                             , "Schema deletion failed: class is used as poss superior")
   CASE_ERRCODE(ERROR_DS_RECALCSCHEMA_FAILED                            , "ERROR_DS_RECALCSCHEMA_FAILED"                            , "Schema update failed in recalculating validation cache.")
   CASE_ERRCODE(ERROR_DS_TREE_DELETE_NOT_FINISHED                       , "ERROR_DS_TREE_DELETE_NOT_FINISHED"                       , "The tree deletion is not finished.")
   CASE_ERRCODE(ERROR_DS_CANT_DELETE                                    , "ERROR_DS_CANT_DELETE"                                    , "The requested delete operation could not be performed.")
   CASE_ERRCODE(ERROR_DS_ATT_SCHEMA_REQ_ID                              , "ERROR_DS_ATT_SCHEMA_REQ_ID"                              , "Cannot read the governs class identifier for the schema record.")
   CASE_ERRCODE(ERROR_DS_BAD_ATT_SCHEMA_SYNTAX                          , "ERROR_DS_BAD_ATT_SCHEMA_SYNTAX"                          , "The attribute schema has bad syntax.")
   CASE_ERRCODE(ERROR_DS_CANT_CACHE_ATT                                 , "ERROR_DS_CANT_CACHE_ATT"                                 , "The attribute could not be cached.")
   CASE_ERRCODE(ERROR_DS_CANT_CACHE_CLASS                               , "ERROR_DS_CANT_CACHE_CLASS"                               , "The class could not be cached.")
   CASE_ERRCODE(ERROR_DS_CANT_REMOVE_ATT_CACHE                          , "ERROR_DS_CANT_REMOVE_ATT_CACHE"                          , "The attribute could not be removed from the cache.")
   CASE_ERRCODE(ERROR_DS_CANT_REMOVE_CLASS_CACHE                        , "ERROR_DS_CANT_REMOVE_CLASS_CACHE"                        , "The class could not be removed from the cache.")
   CASE_ERRCODE(ERROR_DS_CANT_RETRIEVE_DN                               , "ERROR_DS_CANT_RETRIEVE_DN"                               , "The distinguished name attribute could not be read.")
   CASE_ERRCODE(ERROR_DS_MISSING_SUPREF                                 , "ERROR_DS_MISSING_SUPREF"                                 , "No superior reference has been configured for the directory service. The directory service is therefore unable to issue referrals to objects outside this forest.")
   CASE_ERRCODE(ERROR_DS_CANT_RETRIEVE_INSTANCE                         , "ERROR_DS_CANT_RETRIEVE_INSTANCE"                         , "The instance type attribute could not be retrieved.")
   CASE_ERRCODE(ERROR_DS_CODE_INCONSISTENCY                             , "ERROR_DS_CODE_INCONSISTENCY"                             , "An internal error has occurred.")
   CASE_ERRCODE(ERROR_DS_DATABASE_ERROR                                 , "ERROR_DS_DATABASE_ERROR"                                 , "A database error has occurred.")
   CASE_ERRCODE(ERROR_DS_GOVERNSID_MISSING                              , "ERROR_DS_GOVERNSID_MISSING"                              , "The attribute GOVERNSID is missing.")
   CASE_ERRCODE(ERROR_DS_MISSING_EXPECTED_ATT                           , "ERROR_DS_MISSING_EXPECTED_ATT"                           , "An expected attribute is missing.")
   CASE_ERRCODE(ERROR_DS_NCNAME_MISSING_CR_REF                          , "ERROR_DS_NCNAME_MISSING_CR_REF"                          , "The specified naming context is missing a cross reference.")
   CASE_ERRCODE(ERROR_DS_SECURITY_CHECKING_ERROR                        , "ERROR_DS_SECURITY_CHECKING_ERROR"                        , "A security checking error has occurred.")
   CASE_ERRCODE(ERROR_DS_SCHEMA_NOT_LOADED                              , "ERROR_DS_SCHEMA_NOT_LOADED"                              , "The schema is not loaded.")
   CASE_ERRCODE(ERROR_DS_SCHEMA_ALLOC_FAILED                            , "ERROR_DS_SCHEMA_ALLOC_FAILED"                            , "Schema allocation failed. Please check if the machine is running low on memory.")
   CASE_ERRCODE(ERROR_DS_ATT_SCHEMA_REQ_SYNTAX                          , "ERROR_DS_ATT_SCHEMA_REQ_SYNTAX"                          , "Failed to obtain the required syntax for the attribute schema.")
   CASE_ERRCODE(ERROR_DS_GCVERIFY_ERROR                                 , "ERROR_DS_GCVERIFY_ERROR"                                 , "The global catalog verification failed. The global catalog is not available or does not support the operation. Some part of the directory is currently not available.")
   CASE_ERRCODE(ERROR_DS_DRA_SCHEMA_MISMATCH                            , "ERROR_DS_DRA_SCHEMA_MISMATCH"                            , "The replication operation failed because of a schema mismatch between the servers involved.")
   CASE_ERRCODE(ERROR_DS_CANT_FIND_DSA_OBJ                              , "ERROR_DS_CANT_FIND_DSA_OBJ"                              , "The DSA object could not be found.")
   CASE_ERRCODE(ERROR_DS_CANT_FIND_EXPECTED_NC                          , "ERROR_DS_CANT_FIND_EXPECTED_NC"                          , "The naming context could not be found.")
   CASE_ERRCODE(ERROR_DS_CANT_FIND_NC_IN_CACHE                          , "ERROR_DS_CANT_FIND_NC_IN_CACHE"                          , "The naming context could not be found in the cache.")
   CASE_ERRCODE(ERROR_DS_CANT_RETRIEVE_CHILD                            , "ERROR_DS_CANT_RETRIEVE_CHILD"                            , "The child object could not be retrieved.")
   CASE_ERRCODE(ERROR_DS_SECURITY_ILLEGAL_MODIFY                        , "ERROR_DS_SECURITY_ILLEGAL_MODIFY"                        , "The modification was not permitted for security reasons.")
   CASE_ERRCODE(ERROR_DS_CANT_REPLACE_HIDDEN_REC                        , "ERROR_DS_CANT_REPLACE_HIDDEN_REC"                        , "The operation cannot replace the hidden record.")
   CASE_ERRCODE(ERROR_DS_BAD_HIERARCHY_FILE                             , "ERROR_DS_BAD_HIERARCHY_FILE"                             , "The hierarchy file is invalid.")
   CASE_ERRCODE(ERROR_DS_BUILD_HIERARCHY_TABLE_FAILED                   , "ERROR_DS_BUILD_HIERARCHY_TABLE_FAILED"                   , "The attempt to build the hierarchy table failed.")
   CASE_ERRCODE(ERROR_DS_CONFIG_PARAM_MISSING                           , "ERROR_DS_CONFIG_PARAM_MISSING"                           , "The directory configuration parameter is missing from the registry.")
   CASE_ERRCODE(ERROR_DS_COUNTING_AB_INDICES_FAILED                     , "ERROR_DS_COUNTING_AB_INDICES_FAILED"                     , "The attempt to count the address book indices failed.")
   CASE_ERRCODE(ERROR_DS_HIERARCHY_TABLE_MALLOC_FAILED                  , "ERROR_DS_HIERARCHY_TABLE_MALLOC_FAILED"                  , "The allocation of the hierarchy table failed.")
   CASE_ERRCODE(ERROR_DS_INTERNAL_FAILURE                               , "ERROR_DS_INTERNAL_FAILURE"                               , "The directory service encountered an internal failure.")
   CASE_ERRCODE(ERROR_DS_UNKNOWN_ERROR                                  , "ERROR_DS_UNKNOWN_ERROR"                                  , "The directory service encountered an unknown failure.")
   CASE_ERRCODE(ERROR_DS_ROOT_REQUIRES_CLASS_TOP                        , "ERROR_DS_ROOT_REQUIRES_CLASS_TOP"                        , "A root object requires a class of 'top'.")
   CASE_ERRCODE(ERROR_DS_REFUSING_FSMO_ROLES                            , "ERROR_DS_REFUSING_FSMO_ROLES"                            , "This directory server is shutting down, and cannot take ownership of new floating single-master operation roles.")
   CASE_ERRCODE(ERROR_DS_MISSING_FSMO_SETTINGS                          , "ERROR_DS_MISSING_FSMO_SETTINGS"                          , "The directory service is missing mandatory configuration information, and is unable to determine the ownership of floating single-master operation roles.")
   CASE_ERRCODE(ERROR_DS_UNABLE_TO_SURRENDER_ROLES                      , "ERROR_DS_UNABLE_TO_SURRENDER_ROLES"                      , "The directory service was unable to transfer ownership of one or more floating single-master operation roles to other servers.")
   CASE_ERRCODE(ERROR_DS_DRA_GENERIC                                    , "ERROR_DS_DRA_GENERIC"                                    , "The replication operation failed.")
   CASE_ERRCODE(ERROR_DS_DRA_INVALID_PARAMETER                          , "ERROR_DS_DRA_INVALID_PARAMETER"                          , "An invalid parameter was specified for this replication operation.")
   CASE_ERRCODE(ERROR_DS_DRA_BUSY                                       , "ERROR_DS_DRA_BUSY"                                       , "The directory service is too busy to complete the replication operation at this time.")
   CASE_ERRCODE(ERROR_DS_DRA_BAD_DN                                     , "ERROR_DS_DRA_BAD_DN"                                     , "The distinguished name specified for this replication operation is invalid.")
   CASE_ERRCODE(ERROR_DS_DRA_BAD_NC                                     , "ERROR_DS_DRA_BAD_NC"                                     , "The naming context specified for this replication operation is invalid.")
   CASE_ERRCODE(ERROR_DS_DRA_DN_EXISTS                                  , "ERROR_DS_DRA_DN_EXISTS"                                  , "The distinguished name specified for this replication operation already exists.")
   CASE_ERRCODE(ERROR_DS_DRA_INTERNAL_ERROR                             , "ERROR_DS_DRA_INTERNAL_ERROR"                             , "The replication system encountered an internal error.")
   CASE_ERRCODE(ERROR_DS_DRA_INCONSISTENT_DIT                           , "ERROR_DS_DRA_INCONSISTENT_DIT"                           , "The replication operation encountered a database inconsistency.")
   CASE_ERRCODE(ERROR_DS_DRA_CONNECTION_FAILED                          , "ERROR_DS_DRA_CONNECTION_FAILED"                          , "The server specified for this replication operation could not be contacted.")
   CASE_ERRCODE(ERROR_DS_DRA_BAD_INSTANCE_TYPE                          , "ERROR_DS_DRA_BAD_INSTANCE_TYPE"                          , "The replication operation encountered an object with an invalid instance type.")
   CASE_ERRCODE(ERROR_DS_DRA_OUT_OF_MEM                                 , "ERROR_DS_DRA_OUT_OF_MEM"                                 , "The replication operation failed to allocate memory.")
   CASE_ERRCODE(ERROR_DS_DRA_MAIL_PROBLEM                               , "ERROR_DS_DRA_MAIL_PROBLEM"                               , "The replication operation encountered an error with the mail system.")
   CASE_ERRCODE(ERROR_DS_DRA_REF_ALREADY_EXISTS                         , "ERROR_DS_DRA_REF_ALREADY_EXISTS"                         , "The replication reference information for the target server already exists.")
   CASE_ERRCODE(ERROR_DS_DRA_REF_NOT_FOUND                              , "ERROR_DS_DRA_REF_NOT_FOUND"                              , "The replication reference information for the target server does not exist.")
   CASE_ERRCODE(ERROR_DS_DRA_OBJ_IS_REP_SOURCE                          , "ERROR_DS_DRA_OBJ_IS_REP_SOURCE"                          , "The naming context cannot be removed because it is replicated to another server.")
   CASE_ERRCODE(ERROR_DS_DRA_DB_ERROR                                   , "ERROR_DS_DRA_DB_ERROR"                                   , "The replication operation encountered a database error.")
   CASE_ERRCODE(ERROR_DS_DRA_NO_REPLICA                                 , "ERROR_DS_DRA_NO_REPLICA"                                 , "The naming context is in the process of being removed or is not replicated from the specified server.")
   CASE_ERRCODE(ERROR_DS_DRA_ACCESS_DENIED                              , "ERROR_DS_DRA_ACCESS_DENIED"                              , "Replication access was denied.")
   CASE_ERRCODE(ERROR_DS_DRA_NOT_SUPPORTED                              , "ERROR_DS_DRA_NOT_SUPPORTED"                              , "The requested operation is not supported by this version of the directory service.")
   CASE_ERRCODE(ERROR_DS_DRA_RPC_CANCELLED                              , "ERROR_DS_DRA_RPC_CANCELLED"                              , "The replication remote procedure call was cancelled.")
   CASE_ERRCODE(ERROR_DS_DRA_SOURCE_DISABLED                            , "ERROR_DS_DRA_SOURCE_DISABLED"                            , "The source server is currently rejecting replication requests.")
   CASE_ERRCODE(ERROR_DS_DRA_SINK_DISABLED                              , "ERROR_DS_DRA_SINK_DISABLED"                              , "The destination server is currently rejecting replication requests.")
   CASE_ERRCODE(ERROR_DS_DRA_NAME_COLLISION                             , "ERROR_DS_DRA_NAME_COLLISION"                             , "The replication operation failed due to a collision of object names.")
   CASE_ERRCODE(ERROR_DS_DRA_SOURCE_REINSTALLED                         , "ERROR_DS_DRA_SOURCE_REINSTALLED"                         , "The replication source has been reinstalled.")
   CASE_ERRCODE(ERROR_DS_DRA_MISSING_PARENT                             , "ERROR_DS_DRA_MISSING_PARENT"                             , "The replication operation failed because a required parent object is missing.")
   CASE_ERRCODE(ERROR_DS_DRA_PREEMPTED                                  , "ERROR_DS_DRA_PREEMPTED"                                  , "The replication operation was preempted.")
   CASE_ERRCODE(ERROR_DS_DRA_ABANDON_SYNC                               , "ERROR_DS_DRA_ABANDON_SYNC"                               , "The replication synchronization attempt was abandoned because of a lack of updates.")
   CASE_ERRCODE(ERROR_DS_DRA_SHUTDOWN                                   , "ERROR_DS_DRA_SHUTDOWN"                                   , "The replication operation was terminated because the system is shutting down.")
   CASE_ERRCODE(ERROR_DS_DRA_INCOMPATIBLE_PARTIAL_SET                   , "ERROR_DS_DRA_INCOMPATIBLE_PARTIAL_SET"                   , "The replication synchronization attempt failed as the destination partial attribute set is not a subset of source partial attribute set.")
   CASE_ERRCODE(ERROR_DS_DRA_SOURCE_IS_PARTIAL_REPLICA                  , "ERROR_DS_DRA_SOURCE_IS_PARTIAL_REPLICA"                  , "The replication synchronization attempt failed because a master replica attempted to sync from a partial replica.")
   CASE_ERRCODE(ERROR_DS_DRA_EXTN_CONNECTION_FAILED                     , "ERROR_DS_DRA_EXTN_CONNECTION_FAILED"                     , "The server specified for this replication operation was contacted, but that server was unable to contact an additional server needed to complete the operation.")
   CASE_ERRCODE(ERROR_DS_INSTALL_SCHEMA_MISMATCH                        , "ERROR_DS_INSTALL_SCHEMA_MISMATCH"                        , "The version of the Active Directory schema of the source forest is not compatible with the version of Active Directory on this computer.")
   CASE_ERRCODE(ERROR_DS_DUP_LINK_ID                                    , "ERROR_DS_DUP_LINK_ID"                                    , "Schema update failed: An attribute with the same link identifier already exists.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_RESOLVING                           , "ERROR_DS_NAME_ERROR_RESOLVING"                           , "Name translation: Generic processing error.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_NOT_FOUND                           , "ERROR_DS_NAME_ERROR_NOT_FOUND"                           , "Name translation: Could not find the name or insufficient right to see name.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_NOT_UNIQUE                          , "ERROR_DS_NAME_ERROR_NOT_UNIQUE"                          , "Name translation: Input name mapped to more than one output name.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_NO_MAPPING                          , "ERROR_DS_NAME_ERROR_NO_MAPPING"                          , "Name translation: Input name found, but not the associated output format.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_DOMAIN_ONLY                         , "ERROR_DS_NAME_ERROR_DOMAIN_ONLY"                         , "Name translation: Unable to resolve completely, only the domain was found.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_NO_SYNTACTICAL_MAPPING              , "ERROR_DS_NAME_ERROR_NO_SYNTACTICAL_MAPPING"              , "Name translation: Unable to perform purely syntactical mapping at the client without going out to the wire.")
   CASE_ERRCODE(ERROR_DS_CONSTRUCTED_ATT_MOD                            , "ERROR_DS_CONSTRUCTED_ATT_MOD"                            , "Modification of a constructed attribute is not allowed.")
   CASE_ERRCODE(ERROR_DS_WRONG_OM_OBJ_CLASS                             , "ERROR_DS_WRONG_OM_OBJ_CLASS"                             , "The OM-Object-Class specified is incorrect for an attribute with the specified syntax.")
   CASE_ERRCODE(ERROR_DS_DRA_REPL_PENDING                               , "ERROR_DS_DRA_REPL_PENDING"                               , "The replication request has been posted; waiting for reply.")
   CASE_ERRCODE(ERROR_DS_DS_REQUIRED                                    , "ERROR_DS_DS_REQUIRED"                                    , "The requested operation requires a directory service, and none was available.")
   CASE_ERRCODE(ERROR_DS_INVALID_LDAP_DISPLAY_NAME                      , "ERROR_DS_INVALID_LDAP_DISPLAY_NAME"                      , "The LDAP display name of the class or attribute contains non-ASCII characters.")
   CASE_ERRCODE(ERROR_DS_NON_BASE_SEARCH                                , "ERROR_DS_NON_BASE_SEARCH"                                , "The requested search operation is only supported for base searches.")
   CASE_ERRCODE(ERROR_DS_CANT_RETRIEVE_ATTS                             , "ERROR_DS_CANT_RETRIEVE_ATTS"                             , "The search failed to retrieve attributes from the database.")
   CASE_ERRCODE(ERROR_DS_BACKLINK_WITHOUT_LINK                          , "ERROR_DS_BACKLINK_WITHOUT_LINK"                          , "The schema update operation tried to add a backward link attribute that has no corresponding forward link.")
   CASE_ERRCODE(ERROR_DS_EPOCH_MISMATCH                                 , "ERROR_DS_EPOCH_MISMATCH"                                 , "Source and destination of a cross domain move do not agree on the object's epoch number. Either source or destination does not have the latest version of the object.")
   CASE_ERRCODE(ERROR_DS_SRC_NAME_MISMATCH                              , "ERROR_DS_SRC_NAME_MISMATCH"                              , "Source and destination of a cross domain move do not agree on the object's current name. Either source or destination does not have the latest version of the object.")
   CASE_ERRCODE(ERROR_DS_SRC_AND_DST_NC_IDENTICAL                       , "ERROR_DS_SRC_AND_DST_NC_IDENTICAL"                       , "Source and destination of a cross domain move operation are identical. Caller should use local move operation instead of cross domain move operation.")
   CASE_ERRCODE(ERROR_DS_DST_NC_MISMATCH                                , "ERROR_DS_DST_NC_MISMATCH"                                , "Source and destination for a cross domain move are not in agreement on the naming contexts in the forest. Either source or destination does not have the latest version of the Partitions container.")
   CASE_ERRCODE(ERROR_DS_NOT_AUTHORITIVE_FOR_DST_NC                     , "ERROR_DS_NOT_AUTHORITIVE_FOR_DST_NC"                     , "Destination of a cross domain move is not authoritative for the destination naming context.")
   CASE_ERRCODE(ERROR_DS_SRC_GUID_MISMATCH                              , "ERROR_DS_SRC_GUID_MISMATCH"                              , "Source and destination of a cross domain move do not agree on the identity of the source object. Either source or destination does not have the latest version of the source object.")
   CASE_ERRCODE(ERROR_DS_CANT_MOVE_DELETED_OBJECT                       , "ERROR_DS_CANT_MOVE_DELETED_OBJECT"                       , "Object being moved across domains is already known to be deleted by the destination server. The source server does not have the latest version of the source object.")
   CASE_ERRCODE(ERROR_DS_PDC_OPERATION_IN_PROGRESS                      , "ERROR_DS_PDC_OPERATION_IN_PROGRESS"                      , "Another operation which requires exclusive access to the PDC PSMO is already in progress.")
   CASE_ERRCODE(ERROR_DS_CROSS_DOMAIN_CLEANUP_REQD                      , "ERROR_DS_CROSS_DOMAIN_CLEANUP_REQD"                      , "A cross domain move operation failed such that the two versions of the moved object exist - one each in the source and destination domains. The destination object needs to be removed to restore the system to a consistent state.")
   CASE_ERRCODE(ERROR_DS_ILLEGAL_XDOM_MOVE_OPERATION                    , "ERROR_DS_ILLEGAL_XDOM_MOVE_OPERATION"                    , "This object may not be moved across domain boundaries either because cross domain moves for this class are disallowed, or the object has some special characteristics, e.g.: trust account or restricted RID, which prevent its move.")
   CASE_ERRCODE(ERROR_DS_CANT_WITH_ACCT_GROUP_MEMBERSHPS                , "ERROR_DS_CANT_WITH_ACCT_GROUP_MEMBERSHPS"                , "Can't move objects with memberships across domain boundaries as once moved, this would violate the membership conditions of the account group. Remove the object from any account group memberships and retry.")
   CASE_ERRCODE(ERROR_DS_NC_MUST_HAVE_NC_PARENT                         , "ERROR_DS_NC_MUST_HAVE_NC_PARENT"                         , "A naming context head must be the immediate child of another naming context head, not of an interior node.")
   CASE_ERRCODE(ERROR_DS_CR_IMPOSSIBLE_TO_VALIDATE                      , "ERROR_DS_CR_IMPOSSIBLE_TO_VALIDATE"                      , "The directory cannot validate the proposed naming context name because it does not hold a replica of the naming context above the proposed naming context. Please ensure that the domain naming master role is held by a server that is configured as a global catalog server, and that the server is up to date with its replication partners. (Applies only to Windows 2000 Domain Naming masters)")
   CASE_ERRCODE(ERROR_DS_DST_DOMAIN_NOT_NATIVE                          , "ERROR_DS_DST_DOMAIN_NOT_NATIVE"                          , "Destination domain must be in native mode.")
   CASE_ERRCODE(ERROR_DS_MISSING_INFRASTRUCTURE_CONTAINER               , "ERROR_DS_MISSING_INFRASTRUCTURE_CONTAINER"               , "The operation cannot be performed because the server does not have an infrastructure container in the domain of interest.")
   CASE_ERRCODE(ERROR_DS_CANT_MOVE_ACCOUNT_GROUP                        , "ERROR_DS_CANT_MOVE_ACCOUNT_GROUP"                        , "Cross-domain move of non-empty account groups is not allowed.")
   CASE_ERRCODE(ERROR_DS_CANT_MOVE_RESOURCE_GROUP                       , "ERROR_DS_CANT_MOVE_RESOURCE_GROUP"                       , "Cross-domain move of non-empty resource groups is not allowed.")
   CASE_ERRCODE(ERROR_DS_INVALID_SEARCH_FLAG                            , "ERROR_DS_INVALID_SEARCH_FLAG"                            , "The search flags for the attribute are invalid. The ANR bit is valid only on attributes of Unicode or Teletex strings.")
   CASE_ERRCODE(ERROR_DS_NO_TREE_DELETE_ABOVE_NC                        , "ERROR_DS_NO_TREE_DELETE_ABOVE_NC"                        , "Tree deletions starting at an object which has an NC head as a descendant are not allowed.")
   CASE_ERRCODE(ERROR_DS_COULDNT_LOCK_TREE_FOR_DELETE                   , "ERROR_DS_COULDNT_LOCK_TREE_FOR_DELETE"                   , "The directory service failed to lock a tree in preparation for a tree deletion because the tree was in use.")
   CASE_ERRCODE(ERROR_DS_COULDNT_IDENTIFY_OBJECTS_FOR_TREE_DELETE       , "ERROR_DS_COULDNT_IDENTIFY_OBJECTS_FOR_TREE_DELETE"       , "The directory service failed to identify the list of objects to delete while attempting a tree deletion.")
   CASE_ERRCODE(ERROR_DS_SAM_INIT_FAILURE                               , "ERROR_DS_SAM_INIT_FAILURE"                               , "Security Accounts Manager initialization failed because of the following error: %1. Error Status: 0x%2. Click OK to shut down the system and reboot into Directory Services Restore Mode. Check the event log for detailed information.")
   CASE_ERRCODE(ERROR_DS_SENSITIVE_GROUP_VIOLATION                      , "ERROR_DS_SENSITIVE_GROUP_VIOLATION"                      , "Only an administrator can modify the membership list of an administrative group.")
   CASE_ERRCODE(ERROR_DS_CANT_MOD_PRIMARYGROUPID                        , "ERROR_DS_CANT_MOD_PRIMARYGROUPID"                        , "Cannot change the primary group ID of a domain controller account.")
   CASE_ERRCODE(ERROR_DS_ILLEGAL_BASE_SCHEMA_MOD                        , "ERROR_DS_ILLEGAL_BASE_SCHEMA_MOD"                        , "An attempt is made to modify the base schema.")
   CASE_ERRCODE(ERROR_DS_NONSAFE_SCHEMA_CHANGE                          , "ERROR_DS_NONSAFE_SCHEMA_CHANGE"                          , "Adding a new mandatory attribute to an existing class, deleting a mandatory attribute from an existing class, or adding an optional attribute to the special class Top that is not a backlink attribute (directly or through inheritance, for example, by adding or deleting an auxiliary class) is not allowed.")
   CASE_ERRCODE(ERROR_DS_SCHEMA_UPDATE_DISALLOWED                       , "ERROR_DS_SCHEMA_UPDATE_DISALLOWED"                       , "Schema update is not allowed on this DC because the DC is not the schema FSMO Role Owner.")
   CASE_ERRCODE(ERROR_DS_CANT_CREATE_UNDER_SCHEMA                       , "ERROR_DS_CANT_CREATE_UNDER_SCHEMA"                       , "An object of this class cannot be created under the schema container. You can only create attribute-schema and class-schema objects under the schema container.")
   CASE_ERRCODE(ERROR_DS_INSTALL_NO_SRC_SCH_VERSION                     , "ERROR_DS_INSTALL_NO_SRC_SCH_VERSION"                     , "The replica/child install failed to get the objectVersion attribute on the schema container on the source DC. Either the attribute is missing on the schema container or the credentials supplied do not have permission to read it.")
   CASE_ERRCODE(ERROR_DS_INSTALL_NO_SCH_VERSION_IN_INIFILE              , "ERROR_DS_INSTALL_NO_SCH_VERSION_IN_INIFILE"              , "The replica/child install failed to read the objectVersion attribute in the SCHEMA section of the file schema.ini in the system32 directory.")
   CASE_ERRCODE(ERROR_DS_INVALID_GROUP_TYPE                             , "ERROR_DS_INVALID_GROUP_TYPE"                             , "The specified group type is invalid.")
   CASE_ERRCODE(ERROR_DS_NO_NEST_GLOBALGROUP_IN_MIXEDDOMAIN             , "ERROR_DS_NO_NEST_GLOBALGROUP_IN_MIXEDDOMAIN"             , "Cannot nest global groups in a mixed domain if the group is security-enabled.")
   CASE_ERRCODE(ERROR_DS_NO_NEST_LOCALGROUP_IN_MIXEDDOMAIN              , "ERROR_DS_NO_NEST_LOCALGROUP_IN_MIXEDDOMAIN"              , "Cannot nest local groups in a mixed domain if the group is security-enabled.")
   CASE_ERRCODE(ERROR_DS_GLOBAL_CANT_HAVE_LOCAL_MEMBER                  , "ERROR_DS_GLOBAL_CANT_HAVE_LOCAL_MEMBER"                  , "A global group cannot have a local group as a member.")
   CASE_ERRCODE(ERROR_DS_GLOBAL_CANT_HAVE_UNIVERSAL_MEMBER              , "ERROR_DS_GLOBAL_CANT_HAVE_UNIVERSAL_MEMBER"              , "A global group cannot have a universal group as a member.")
   CASE_ERRCODE(ERROR_DS_UNIVERSAL_CANT_HAVE_LOCAL_MEMBER               , "ERROR_DS_UNIVERSAL_CANT_HAVE_LOCAL_MEMBER"               , "A universal group cannot have a local group as a member.")
   CASE_ERRCODE(ERROR_DS_GLOBAL_CANT_HAVE_CROSSDOMAIN_MEMBER            , "ERROR_DS_GLOBAL_CANT_HAVE_CROSSDOMAIN_MEMBER"            , "A global group cannot have a cross-domain member.")
   CASE_ERRCODE(ERROR_DS_LOCAL_CANT_HAVE_CROSSDOMAIN_LOCAL_MEMBER       , "ERROR_DS_LOCAL_CANT_HAVE_CROSSDOMAIN_LOCAL_MEMBER"       , "A local group cannot have another cross-domain local group as a member.")
   CASE_ERRCODE(ERROR_DS_HAVE_PRIMARY_MEMBERS                           , "ERROR_DS_HAVE_PRIMARY_MEMBERS"                           , "A group with primary members cannot change to a security-disabled group.")
   CASE_ERRCODE(ERROR_DS_STRING_SD_CONVERSION_FAILED                    , "ERROR_DS_STRING_SD_CONVERSION_FAILED"                    , "The schema cache load failed to convert the string default SD on a class-schema object.")
   CASE_ERRCODE(ERROR_DS_NAMING_MASTER_GC                               , "ERROR_DS_NAMING_MASTER_GC"                               , "Only DSAs configured to be Global Catalog servers should be allowed to hold the Domain Naming Master FSMO role. (Applies only to Windows 2000 servers)")
   CASE_ERRCODE(ERROR_DS_DNS_LOOKUP_FAILURE                             , "ERROR_DS_DNS_LOOKUP_FAILURE"                             , "The DSA operation is unable to proceed because of a DNS lookup failure.")
   CASE_ERRCODE(ERROR_DS_COULDNT_UPDATE_SPNS                            , "ERROR_DS_COULDNT_UPDATE_SPNS"                            , "While processing a change to the DNS Host Name for an object, the Service Principal Name values could not be kept in sync.")
   CASE_ERRCODE(ERROR_DS_CANT_RETRIEVE_SD                               , "ERROR_DS_CANT_RETRIEVE_SD"                               , "The Security Descriptor attribute could not be read.")
   CASE_ERRCODE(ERROR_DS_KEY_NOT_UNIQUE                                 , "ERROR_DS_KEY_NOT_UNIQUE"                                 , "The object requested was not found, but an object with that key was found.")
   CASE_ERRCODE(ERROR_DS_WRONG_LINKED_ATT_SYNTAX                        , "ERROR_DS_WRONG_LINKED_ATT_SYNTAX"                        , "The syntax of the linked attributed being added is incorrect. Forward links can only have syntax 2.5.5.1, 2.5.5.7, and 2.5.5.14, and backlinks can only have syntax 2.5.5.1.")
   CASE_ERRCODE(ERROR_DS_SAM_NEED_BOOTKEY_PASSWORD                      , "ERROR_DS_SAM_NEED_BOOTKEY_PASSWORD"                      , "Security Account Manager needs to get the boot password.")
   CASE_ERRCODE(ERROR_DS_SAM_NEED_BOOTKEY_FLOPPY                        , "ERROR_DS_SAM_NEED_BOOTKEY_FLOPPY"                        , "Security Account Manager needs to get the boot key from floppy disk.")
   CASE_ERRCODE(ERROR_DS_CANT_START                                     , "ERROR_DS_CANT_START"                                     , "Directory Service cannot start.")
   CASE_ERRCODE(ERROR_DS_INIT_FAILURE                                   , "ERROR_DS_INIT_FAILURE"                                   , "Directory Services could not start.")
   CASE_ERRCODE(ERROR_DS_NO_PKT_PRIVACY_ON_CONNECTION                   , "ERROR_DS_NO_PKT_PRIVACY_ON_CONNECTION"                   , "The connection between client and server requires packet privacy or better.")
   CASE_ERRCODE(ERROR_DS_SOURCE_DOMAIN_IN_FOREST                        , "ERROR_DS_SOURCE_DOMAIN_IN_FOREST"                        , "The source domain may not be in the same forest as destination.")
   CASE_ERRCODE(ERROR_DS_DESTINATION_DOMAIN_NOT_IN_FOREST               , "ERROR_DS_DESTINATION_DOMAIN_NOT_IN_FOREST"               , "The destination domain must be in the forest.")
   CASE_ERRCODE(ERROR_DS_DESTINATION_AUDITING_NOT_ENABLED               , "ERROR_DS_DESTINATION_AUDITING_NOT_ENABLED"               , "The operation requires that destination domain auditing be enabled.")
   CASE_ERRCODE(ERROR_DS_CANT_FIND_DC_FOR_SRC_DOMAIN                    , "ERROR_DS_CANT_FIND_DC_FOR_SRC_DOMAIN"                    , "The operation couldn't locate a DC for the source domain.")
   CASE_ERRCODE(ERROR_DS_SRC_OBJ_NOT_GROUP_OR_USER                      , "ERROR_DS_SRC_OBJ_NOT_GROUP_OR_USER"                      , "The source object must be a group or user.")
   CASE_ERRCODE(ERROR_DS_SRC_SID_EXISTS_IN_FOREST                       , "ERROR_DS_SRC_SID_EXISTS_IN_FOREST"                       , "The source object's SID already exists in destination forest.")
   CASE_ERRCODE(ERROR_DS_SRC_AND_DST_OBJECT_CLASS_MISMATCH              , "ERROR_DS_SRC_AND_DST_OBJECT_CLASS_MISMATCH"              , "The source and destination object must be of the same type.")
   CASE_ERRCODE(ERROR_SAM_INIT_FAILURE                                  , "ERROR_SAM_INIT_FAILURE"                                  , "Security Accounts Manager initialization failed because of the following error: %1. Error Status: 0x%2. Click OK to shut down the system and reboot into Safe Mode. Check the event log for detailed information.")
   CASE_ERRCODE(ERROR_DS_DRA_SCHEMA_INFO_SHIP                           , "ERROR_DS_DRA_SCHEMA_INFO_SHIP"                           , "Schema information could not be included in the replication request.")
   CASE_ERRCODE(ERROR_DS_DRA_SCHEMA_CONFLICT                            , "ERROR_DS_DRA_SCHEMA_CONFLICT"                            , "The replication operation could not be completed due to a schema incompatibility.")
   CASE_ERRCODE(ERROR_DS_DRA_EARLIER_SCHEMA_CONFLICT                    , "ERROR_DS_DRA_EARLIER_SCHEMA_CONFLICT"                    , "The replication operation could not be completed due to a previous schema incompatibility.")
   CASE_ERRCODE(ERROR_DS_DRA_OBJ_NC_MISMATCH                            , "ERROR_DS_DRA_OBJ_NC_MISMATCH"                            , "The replication update could not be applied because either the source or the destination has not yet received information regarding a recent cross-domain move operation.")
   CASE_ERRCODE(ERROR_DS_NC_STILL_HAS_DSAS                              , "ERROR_DS_NC_STILL_HAS_DSAS"                              , "The requested domain could not be deleted because there exist domain controllers that still host this domain.")
   CASE_ERRCODE(ERROR_DS_GC_REQUIRED                                    , "ERROR_DS_GC_REQUIRED"                                    , "The requested operation can be performed only on a global catalog server.")
   CASE_ERRCODE(ERROR_DS_LOCAL_MEMBER_OF_LOCAL_ONLY                     , "ERROR_DS_LOCAL_MEMBER_OF_LOCAL_ONLY"                     , "A local group can only be a member of other local groups in the same domain.")
   CASE_ERRCODE(ERROR_DS_NO_FPO_IN_UNIVERSAL_GROUPS                     , "ERROR_DS_NO_FPO_IN_UNIVERSAL_GROUPS"                     , "Foreign security principals cannot be members of universal groups.")
   CASE_ERRCODE(ERROR_DS_CANT_ADD_TO_GC                                 , "ERROR_DS_CANT_ADD_TO_GC"                                 , "The attribute is not allowed to be replicated to the GC because of security reasons.")
   CASE_ERRCODE(ERROR_DS_NO_CHECKPOINT_WITH_PDC                         , "ERROR_DS_NO_CHECKPOINT_WITH_PDC"                         , "The checkpoint with the PDC could not be taken because there are too many modifications being processed currently.")
   CASE_ERRCODE(ERROR_DS_SOURCE_AUDITING_NOT_ENABLED                    , "ERROR_DS_SOURCE_AUDITING_NOT_ENABLED"                    , "The operation requires that source domain auditing be enabled.")
   CASE_ERRCODE(ERROR_DS_CANT_CREATE_IN_NONDOMAIN_NC                    , "ERROR_DS_CANT_CREATE_IN_NONDOMAIN_NC"                    , "Security principal objects can only be created inside domain naming contexts.")
   CASE_ERRCODE(ERROR_DS_INVALID_NAME_FOR_SPN                           , "ERROR_DS_INVALID_NAME_FOR_SPN"                           , "A Service Principal Name (SPN) could not be constructed because the provided hostname is not in the necessary format.")
   CASE_ERRCODE(ERROR_DS_FILTER_USES_CONTRUCTED_ATTRS                   , "ERROR_DS_FILTER_USES_CONTRUCTED_ATTRS"                   , "A Filter was passed that uses constructed attributes.")
   CASE_ERRCODE(ERROR_DS_UNICODEPWD_NOT_IN_QUOTES                       , "ERROR_DS_UNICODEPWD_NOT_IN_QUOTES"                       , "The unicodePwd attribute value must be enclosed in double quotes.")
   CASE_ERRCODE(ERROR_DS_MACHINE_ACCOUNT_QUOTA_EXCEEDED                 , "ERROR_DS_MACHINE_ACCOUNT_QUOTA_EXCEEDED"                 , "Your computer could not be joined to the domain. You have exceeded the maximum number of computer accounts you are allowed to create in this domain. Contact your system administrator to have this limit reset or increased.")
   CASE_ERRCODE(ERROR_DS_MUST_BE_RUN_ON_DST_DC                          , "ERROR_DS_MUST_BE_RUN_ON_DST_DC"                          , "For security reasons, the operation must be run on the destination DC.")
   CASE_ERRCODE(ERROR_DS_SRC_DC_MUST_BE_SP4_OR_GREATER                  , "ERROR_DS_SRC_DC_MUST_BE_SP4_OR_GREATER"                  , "For security reasons, the source DC must be NT4SP4 or greater.")
   CASE_ERRCODE(ERROR_DS_CANT_TREE_DELETE_CRITICAL_OBJ                  , "ERROR_DS_CANT_TREE_DELETE_CRITICAL_OBJ"                  , "Critical Directory Service System objects cannot be deleted during tree delete operations. The tree delete may have been partially performed.")
   CASE_ERRCODE(ERROR_DS_INIT_FAILURE_CONSOLE                           , "ERROR_DS_INIT_FAILURE_CONSOLE"                           , "Directory Services could not start because of the following error: %1. Error Status: 0x%2. Please click OK to shutdown the system. You can use the recovery console to diagnose the system further.")
   CASE_ERRCODE(ERROR_DS_SAM_INIT_FAILURE_CONSOLE                       , "ERROR_DS_SAM_INIT_FAILURE_CONSOLE"                       , "Security Accounts Manager initialization failed because of the following error: %1. Error Status: 0x%2. Please click OK to shutdown the system. You can use the recovery console to diagnose the system further.")
   CASE_ERRCODE(ERROR_DS_FOREST_VERSION_TOO_HIGH                        , "ERROR_DS_FOREST_VERSION_TOO_HIGH"                        , "The version of the operating system installed is incompatible with the current forest functional level. You must upgrade to a new version of the operating system before this server can become a domain controller in this forest.")
   CASE_ERRCODE(ERROR_DS_DOMAIN_VERSION_TOO_HIGH                        , "ERROR_DS_DOMAIN_VERSION_TOO_HIGH"                        , "The version of the operating system installed is incompatible with the current domain functional level. You must upgrade to a new version of the operating system before this server can become a domain controller in this domain.")
   CASE_ERRCODE(ERROR_DS_FOREST_VERSION_TOO_LOW                         , "ERROR_DS_FOREST_VERSION_TOO_LOW"                         , "This version of the operating system installed on this server no longer supports the current forest functional level. You must raise the forest functional level before this server can become a domain controller in this forest.")
   CASE_ERRCODE(ERROR_DS_DOMAIN_VERSION_TOO_LOW                         , "ERROR_DS_DOMAIN_VERSION_TOO_LOW"                         , "This version of the operating system installed on this server no longer supports the current domain functional level. You must raise the domain functional level before this server can become a domain controller in this domain.")
   CASE_ERRCODE(ERROR_DS_INCOMPATIBLE_VERSION                           , "ERROR_DS_INCOMPATIBLE_VERSION"                           , "The version of the operating system installed on this server is incompatible with the functional level of the domain or forest.")
   CASE_ERRCODE(ERROR_DS_LOW_DSA_VERSION                                , "ERROR_DS_LOW_DSA_VERSION"                                , "The functional level of the domain (or forest) cannot be raised to the requested value, because there exist one or more domain controllers in the domain (or forest) that are at a lower incompatible functional level.")
   CASE_ERRCODE(ERROR_DS_NO_BEHAVIOR_VERSION_IN_MIXEDDOMAIN             , "ERROR_DS_NO_BEHAVIOR_VERSION_IN_MIXEDDOMAIN"             , "The forest functional level cannot be raised to the requested level since one or more domains are still in mixed domain mode. All domains in the forest must be in native mode before you can raise the forest functional level.")
   CASE_ERRCODE(ERROR_DS_NOT_SUPPORTED_SORT_ORDER                       , "ERROR_DS_NOT_SUPPORTED_SORT_ORDER"                       , "The sort order requested is not supported.")
   CASE_ERRCODE(ERROR_DS_NAME_NOT_UNIQUE                                , "ERROR_DS_NAME_NOT_UNIQUE"                                , "The requested name already exists as a unique identifier.")
   CASE_ERRCODE(ERROR_DS_MACHINE_ACCOUNT_CREATED_PRENT4                 , "ERROR_DS_MACHINE_ACCOUNT_CREATED_PRENT4"                 , "The machine account was created pre-NT4. The account needs to be recreated.")
   CASE_ERRCODE(ERROR_DS_OUT_OF_VERSION_STORE                           , "ERROR_DS_OUT_OF_VERSION_STORE"                           , "The database is out of version store.")
   CASE_ERRCODE(ERROR_DS_INCOMPATIBLE_CONTROLS_USED                     , "ERROR_DS_INCOMPATIBLE_CONTROLS_USED"                     , "Unable to continue operation because multiple conflicting controls were used.")
   CASE_ERRCODE(ERROR_DS_NO_REF_DOMAIN                                  , "ERROR_DS_NO_REF_DOMAIN"                                  , "Unable to find a valid security descriptor reference domain for this partition.")
   CASE_ERRCODE(ERROR_DS_RESERVED_LINK_ID                               , "ERROR_DS_RESERVED_LINK_ID"                               , "Schema update failed: The link identifier is reserved.")
   CASE_ERRCODE(ERROR_DS_LINK_ID_NOT_AVAILABLE                          , "ERROR_DS_LINK_ID_NOT_AVAILABLE"                          , "Schema update failed: There are no link identifiers available.")
   CASE_ERRCODE(ERROR_DS_AG_CANT_HAVE_UNIVERSAL_MEMBER                  , "ERROR_DS_AG_CANT_HAVE_UNIVERSAL_MEMBER"                  , "An account group cannot have a universal group as a member.")
   CASE_ERRCODE(ERROR_DS_MODIFYDN_DISALLOWED_BY_INSTANCE_TYPE           , "ERROR_DS_MODIFYDN_DISALLOWED_BY_INSTANCE_TYPE"           , "Rename or move operations on naming context heads or read-only objects are not allowed.")
   CASE_ERRCODE(ERROR_DS_NO_OBJECT_MOVE_IN_SCHEMA_NC                    , "ERROR_DS_NO_OBJECT_MOVE_IN_SCHEMA_NC"                    , "Move operations on objects in the schema naming context are not allowed.")
   CASE_ERRCODE(ERROR_DS_MODIFYDN_DISALLOWED_BY_FLAG                    , "ERROR_DS_MODIFYDN_DISALLOWED_BY_FLAG"                    , "A system flag has been set on the object and does not allow the object to be moved or renamed.")
   CASE_ERRCODE(ERROR_DS_MODIFYDN_WRONG_GRANDPARENT                     , "ERROR_DS_MODIFYDN_WRONG_GRANDPARENT"                     , "This object is not allowed to change its grandparent container. Moves are not forbidden on this object, but are restricted to sibling containers.")
   CASE_ERRCODE(ERROR_DS_NAME_ERROR_TRUST_REFERRAL                      , "ERROR_DS_NAME_ERROR_TRUST_REFERRAL"                      , "Unable to resolve completely, a referral to another forest is generated.")
   CASE_ERRCODE(ERROR_NOT_SUPPORTED_ON_STANDARD_SERVER                  , "ERROR_NOT_SUPPORTED_ON_STANDARD_SERVER"                  , "The requested action is not supported on standard server.")
   CASE_ERRCODE(ERROR_DS_CANT_ACCESS_REMOTE_PART_OF_AD                  , "ERROR_DS_CANT_ACCESS_REMOTE_PART_OF_AD"                  , "Could not access a partition of the Active Directory located on a remote server. Make sure at least one server is running for the partition in question.")
   CASE_ERRCODE(ERROR_DS_CR_IMPOSSIBLE_TO_VALIDATE_V2                   , "ERROR_DS_CR_IMPOSSIBLE_TO_VALIDATE_V2"                   , "The directory cannot validate the proposed naming context (or partition) name because it does not hold a replica nor can it contact a replica of the naming context above the proposed naming context. Please ensure that the parent naming context is properly registered in DNS, and at least one replica of this naming context is reachable by the Domain Naming master.")
   CASE_ERRCODE(ERROR_DS_THREAD_LIMIT_EXCEEDED                          , "ERROR_DS_THREAD_LIMIT_EXCEEDED"                          , "The thread limit for this request was exceeded.")
   CASE_ERRCODE(ERROR_DS_NOT_CLOSEST                                    , "ERROR_DS_NOT_CLOSEST"                                    , "The Global catalog server is not in the closet site.")
   CASE_ERRCODE(ERROR_DS_CANT_DERIVE_SPN_WITHOUT_SERVER_REF             , "ERROR_DS_CANT_DERIVE_SPN_WITHOUT_SERVER_REF"             , "The DS cannot derive a service principal name (SPN) with which to mutually authenticate the target server because the corresponding server object in the local DS database has no serverReference attribute.")
   CASE_ERRCODE(ERROR_DS_SINGLE_USER_MODE_FAILED                        , "ERROR_DS_SINGLE_USER_MODE_FAILED"                        , "The Directory Service failed to enter single user mode.")
   CASE_ERRCODE(ERROR_DS_NTDSCRIPT_SYNTAX_ERROR                         , "ERROR_DS_NTDSCRIPT_SYNTAX_ERROR"                         , "The Directory Service cannot parse the script because of a syntax error.")
   CASE_ERRCODE(ERROR_DS_NTDSCRIPT_PROCESS_ERROR                        , "ERROR_DS_NTDSCRIPT_PROCESS_ERROR"                        , "The Directory Service cannot process the script because of an error.")
   CASE_ERRCODE(ERROR_DS_DIFFERENT_REPL_EPOCHS                          , "ERROR_DS_DIFFERENT_REPL_EPOCHS"                          , "The directory service cannot perform the requested operation because the servers involved are of different replication epochs (which is usually related to a domain rename that is in progress).")
   CASE_ERRCODE(ERROR_DS_DRS_EXTENSIONS_CHANGED                         , "ERROR_DS_DRS_EXTENSIONS_CHANGED"                         , "The directory service binding must be renegotiated due to a change in the server extensions information.")
   CASE_ERRCODE(ERROR_DS_REPLICA_SET_CHANGE_NOT_ALLOWED_ON_DISABLED_CR  , "ERROR_DS_REPLICA_SET_CHANGE_NOT_ALLOWED_ON_DISABLED_CR"  , "Operation not allowed on a disabled cross ref.")
   CASE_ERRCODE(ERROR_DS_NO_MSDS_INTID                                  , "ERROR_DS_NO_MSDS_INTID"                                  , "Schema update failed: No values for msDS-IntId are available.")
   CASE_ERRCODE(ERROR_DS_DUP_MSDS_INTID                                 , "ERROR_DS_DUP_MSDS_INTID"                                 , "Schema update failed: Duplicate msDS-INtId. Retry the operation.")
   CASE_ERRCODE(ERROR_DS_EXISTS_IN_RDNATTID                             , "ERROR_DS_EXISTS_IN_RDNATTID"                             , "Schema deletion failed: attribute is used in rDNAttID.")
   CASE_ERRCODE(ERROR_DS_AUTHORIZATION_FAILED                           , "ERROR_DS_AUTHORIZATION_FAILED"                           , "The directory service failed to authorize the request.")
   CASE_ERRCODE(ERROR_DS_INVALID_SCRIPT                                 , "ERROR_DS_INVALID_SCRIPT"                                 , "The Directory Service cannot process the script because it is invalid.")
   CASE_ERRCODE(ERROR_DS_REMOTE_CROSSREF_OP_FAILED                      , "ERROR_DS_REMOTE_CROSSREF_OP_FAILED"                      , "The remote create cross reference operation failed on the Domain Naming Master FSMO. The operation's error is in the extended data.")
   CASE_ERRCODE(ERROR_DS_CROSS_REF_BUSY                                 , "ERROR_DS_CROSS_REF_BUSY"                                 , "A cross reference is in use locally with the same name.")
   CASE_ERRCODE(ERROR_DS_CANT_DERIVE_SPN_FOR_DELETED_DOMAIN             , "ERROR_DS_CANT_DERIVE_SPN_FOR_DELETED_DOMAIN"             , "The DS cannot derive a service principal name (SPN) with which to mutually authenticate the target server because the server's domain has been deleted from the forest.")
   CASE_ERRCODE(ERROR_DS_CANT_DEMOTE_WITH_WRITEABLE_NC                  , "ERROR_DS_CANT_DEMOTE_WITH_WRITEABLE_NC"                  , "Writeable NCs prevent this DC from demoting.")
   CASE_ERRCODE(ERROR_DS_DUPLICATE_ID_FOUND                             , "ERROR_DS_DUPLICATE_ID_FOUND"                             , "The requested object has a non-unique identifier and cannot be retrieved.")
   CASE_ERRCODE(ERROR_DS_INSUFFICIENT_ATTR_TO_CREATE_OBJECT             , "ERROR_DS_INSUFFICIENT_ATTR_TO_CREATE_OBJECT"             , "Insufficient attributes were given to create an object. This object may not exist because it may have been deleted and already garbage collected.")
   CASE_ERRCODE(ERROR_DS_GROUP_CONVERSION_ERROR                         , "ERROR_DS_GROUP_CONVERSION_ERROR"                         , "The group cannot be converted due to attribute restrictions on the requested group type.")
   CASE_ERRCODE(ERROR_DS_CANT_MOVE_APP_BASIC_GROUP                      , "ERROR_DS_CANT_MOVE_APP_BASIC_GROUP"                      , "Cross-domain move of non-empty basic application groups is not allowed.")
   CASE_ERRCODE(ERROR_DS_CANT_MOVE_APP_QUERY_GROUP                      , "ERROR_DS_CANT_MOVE_APP_QUERY_GROUP"                      , "Cross-domain move on non-empty query based application groups is not allowed.")
   CASE_ERRCODE(ERROR_DS_ROLE_NOT_VERIFIED                              , "ERROR_DS_ROLE_NOT_VERIFIED"                              , "The role owner could not be verified because replication of its partition has not occurred recently.")
   CASE_ERRCODE(ERROR_DS_WKO_CONTAINER_CANNOT_BE_SPECIAL                , "ERROR_DS_WKO_CONTAINER_CANNOT_BE_SPECIAL"                , "The target container for a redirection of a well-known object container cannot already be a special container.")
   CASE_ERRCODE(ERROR_DS_DOMAIN_RENAME_IN_PROGRESS                      , "ERROR_DS_DOMAIN_RENAME_IN_PROGRESS"                      , "The Directory Service cannot perform the requested operation because a domain rename operation is in progress.")
   CASE_ERRCODE(ERROR_DS_EXISTING_AD_CHILD_NC                           , "ERROR_DS_EXISTING_AD_CHILD_NC"                           , "The Active Directory detected an Active Directory child partition below the requested new partition name. The Active Directory's partition hierarchy must be created in a top-down method.")
   CASE_ERRCODE(DNS_ERROR_RCODE_FORMAT_ERROR                            , "DNS_ERROR_RCODE_FORMAT_ERROR"                            , "DNS server unable to interpret format.")
   CASE_ERRCODE(DNS_ERROR_RCODE_SERVER_FAILURE                          , "DNS_ERROR_RCODE_SERVER_FAILURE"                          , "DNS server failure.")
   CASE_ERRCODE(DNS_ERROR_RCODE_NAME_ERROR                              , "DNS_ERROR_RCODE_NAME_ERROR"                              , "DNS name does not exist.")
   CASE_ERRCODE(DNS_ERROR_RCODE_NOT_IMPLEMENTED                         , "DNS_ERROR_RCODE_NOT_IMPLEMENTED"                         , "DNS request not supported by name server.")
   CASE_ERRCODE(DNS_ERROR_RCODE_REFUSED                                 , "DNS_ERROR_RCODE_REFUSED"                                 , "DNS operation refused.")
   CASE_ERRCODE(DNS_ERROR_RCODE_YXDOMAIN                                , "DNS_ERROR_RCODE_YXDOMAIN"                                , "DNS name that ought not exist, does exist.")
   CASE_ERRCODE(DNS_ERROR_RCODE_YXRRSET                                 , "DNS_ERROR_RCODE_YXRRSET"                                 , "DNS RR set that ought not exist, does exist.")
   CASE_ERRCODE(DNS_ERROR_RCODE_NXRRSET                                 , "DNS_ERROR_RCODE_NXRRSET"                                 , "DNS RR set that ought to exist, does not exist.")
   CASE_ERRCODE(DNS_ERROR_RCODE_NOTAUTH                                 , "DNS_ERROR_RCODE_NOTAUTH"                                 , "DNS server not authoritative for zone.")
   CASE_ERRCODE(DNS_ERROR_RCODE_NOTZONE                                 , "DNS_ERROR_RCODE_NOTZONE"                                 , "DNS name in update or prereq is not in zone.")
   CASE_ERRCODE(DNS_ERROR_RCODE_BADSIG                                  , "DNS_ERROR_RCODE_BADSIG"                                  , "DNS signature failed to verify.")
   CASE_ERRCODE(DNS_ERROR_RCODE_BADKEY                                  , "DNS_ERROR_RCODE_BADKEY"                                  , "DNS bad key.")
   CASE_ERRCODE(DNS_ERROR_RCODE_BADTIME                                 , "DNS_ERROR_RCODE_BADTIME"                                 , "DNS signature validity expired.")
   CASE_ERRCODE(DNS_INFO_NO_RECORDS                                     , "DNS_INFO_NO_RECORDS"                                     , "No records found for given DNS query.")
   CASE_ERRCODE(DNS_ERROR_BAD_PACKET                                    , "DNS_ERROR_BAD_PACKET"                                    , "Bad DNS packet.")
   CASE_ERRCODE(DNS_ERROR_NO_PACKET                                     , "DNS_ERROR_NO_PACKET"                                     , "No DNS packet.")
   CASE_ERRCODE(DNS_ERROR_RCODE                                         , "DNS_ERROR_RCODE"                                         , "DNS error, check rcode.")
   CASE_ERRCODE(DNS_ERROR_UNSECURE_PACKET                               , "DNS_ERROR_UNSECURE_PACKET"                               , "Unsecured DNS packet.")
   CASE_ERRCODE(DNS_ERROR_INVALID_TYPE                                  , "DNS_ERROR_INVALID_TYPE"                                  , "Invalid DNS type.")
   CASE_ERRCODE(DNS_ERROR_INVALID_IP_ADDRESS                            , "DNS_ERROR_INVALID_IP_ADDRESS"                            , "Invalid IP address.")
   CASE_ERRCODE(DNS_ERROR_INVALID_PROPERTY                              , "DNS_ERROR_INVALID_PROPERTY"                              , "Invalid property.")
   CASE_ERRCODE(DNS_ERROR_TRY_AGAIN_LATER                               , "DNS_ERROR_TRY_AGAIN_LATER"                               , "Try DNS operation again later.")
   CASE_ERRCODE(DNS_ERROR_NOT_UNIQUE                                    , "DNS_ERROR_NOT_UNIQUE"                                    , "Record for given name and type is not unique.")
   CASE_ERRCODE(DNS_ERROR_NON_RFC_NAME                                  , "DNS_ERROR_NON_RFC_NAME"                                  , "DNS name does not comply with RFC specifications.")
   CASE_ERRCODE(DNS_STATUS_FQDN                                         , "DNS_STATUS_FQDN"                                         , "DNS name is a fully-qualified DNS name.")
   CASE_ERRCODE(DNS_STATUS_DOTTED_NAME                                  , "DNS_STATUS_DOTTED_NAME"                                  , "DNS name is dotted (multi-label).")
   CASE_ERRCODE(DNS_STATUS_SINGLE_PART_NAME                             , "DNS_STATUS_SINGLE_PART_NAME"                             , "DNS name is a single-part name.")
   CASE_ERRCODE(DNS_ERROR_INVALID_NAME_CHAR                             , "DNS_ERROR_INVALID_NAME_CHAR"                             , "DSN name contains an invalid character.")
   CASE_ERRCODE(DNS_ERROR_NUMERIC_NAME                                  , "DNS_ERROR_NUMERIC_NAME"                                  , "DNS name is entirely numeric.")
   CASE_ERRCODE(DNS_ERROR_NOT_ALLOWED_ON_ROOT_SERVER                    , "DNS_ERROR_NOT_ALLOWED_ON_ROOT_SERVER"                    , "The operation requested is not permitted on a DNS root server.")
   CASE_ERRCODE(DNS_ERROR_NOT_ALLOWED_UNDER_DELEGATION                  , "DNS_ERROR_NOT_ALLOWED_UNDER_DELEGATION"                  , "The record could not be created because this part of the DNS namespace has been delegated to another server.")
   CASE_ERRCODE(DNS_ERROR_CANNOT_FIND_ROOT_HINTS                        , "DNS_ERROR_CANNOT_FIND_ROOT_HINTS"                        , "The DNS server could not find a set of root hints.")
   CASE_ERRCODE(DNS_ERROR_INCONSISTENT_ROOT_HINTS                       , "DNS_ERROR_INCONSISTENT_ROOT_HINTS"                       , "The DNS server found root hints but they were not consistent across all adapters.")
   CASE_ERRCODE(DNS_ERROR_ZONE_DOES_NOT_EXIST                           , "DNS_ERROR_ZONE_DOES_NOT_EXIST"                           , "DNS zone does not exist.")
   CASE_ERRCODE(DNS_ERROR_NO_ZONE_INFO                                  , "DNS_ERROR_NO_ZONE_INFO"                                  , "DNS zone information not available.")
   CASE_ERRCODE(DNS_ERROR_INVALID_ZONE_OPERATION                        , "DNS_ERROR_INVALID_ZONE_OPERATION"                        , "Invalid operation for DNS zone.")
   CASE_ERRCODE(DNS_ERROR_ZONE_CONFIGURATION_ERROR                      , "DNS_ERROR_ZONE_CONFIGURATION_ERROR"                      , "Invalid DNS zone configuration.")
   CASE_ERRCODE(DNS_ERROR_ZONE_HAS_NO_SOA_RECORD                        , "DNS_ERROR_ZONE_HAS_NO_SOA_RECORD"                        , "DNS zone has no start of authority (SOA) record.")
   CASE_ERRCODE(DNS_ERROR_ZONE_HAS_NO_NS_RECORDS                        , "DNS_ERROR_ZONE_HAS_NO_NS_RECORDS"                        , "DNS zone has no name server (NS) record.")
   CASE_ERRCODE(DNS_ERROR_ZONE_LOCKED                                   , "DNS_ERROR_ZONE_LOCKED"                                   , "DNS zone is locked.")
   CASE_ERRCODE(DNS_ERROR_ZONE_CREATION_FAILED                          , "DNS_ERROR_ZONE_CREATION_FAILED"                          , "DNS zone creation failed.")
   CASE_ERRCODE(DNS_ERROR_ZONE_ALREADY_EXISTS                           , "DNS_ERROR_ZONE_ALREADY_EXISTS"                           , "DNS zone already exists.")
   CASE_ERRCODE(DNS_ERROR_AUTOZONE_ALREADY_EXISTS                       , "DNS_ERROR_AUTOZONE_ALREADY_EXISTS"                       , "DNS automatic zone already exists.")
   CASE_ERRCODE(DNS_ERROR_INVALID_ZONE_TYPE                             , "DNS_ERROR_INVALID_ZONE_TYPE"                             , "Invalid DNS zone type.")
   CASE_ERRCODE(DNS_ERROR_SECONDARY_REQUIRES_MASTER_IP                  , "DNS_ERROR_SECONDARY_REQUIRES_MASTER_IP"                  , "Secondary DNS zone requires master IP address.")
   CASE_ERRCODE(DNS_ERROR_ZONE_NOT_SECONDARY                            , "DNS_ERROR_ZONE_NOT_SECONDARY"                            , "DNS zone not secondary.")
   CASE_ERRCODE(DNS_ERROR_NEED_SECONDARY_ADDRESSES                      , "DNS_ERROR_NEED_SECONDARY_ADDRESSES"                      , "Need secondary IP address.")
   CASE_ERRCODE(DNS_ERROR_WINS_INIT_FAILED                              , "DNS_ERROR_WINS_INIT_FAILED"                              , "WINS initialization failed.")
   CASE_ERRCODE(DNS_ERROR_NEED_WINS_SERVERS                             , "DNS_ERROR_NEED_WINS_SERVERS"                             , "Need WINS servers.")
   CASE_ERRCODE(DNS_ERROR_NBSTAT_INIT_FAILED                            , "DNS_ERROR_NBSTAT_INIT_FAILED"                            , "NBTSTAT initialization call failed.")
   CASE_ERRCODE(DNS_ERROR_SOA_DELETE_INVALID                            , "DNS_ERROR_SOA_DELETE_INVALID"                            , "Invalid delete of start of authority (SOA)")
   CASE_ERRCODE(DNS_ERROR_FORWARDER_ALREADY_EXISTS                      , "DNS_ERROR_FORWARDER_ALREADY_EXISTS"                      , "A conditional forwarding zone already exists for that name.")
   CASE_ERRCODE(DNS_ERROR_ZONE_REQUIRES_MASTER_IP                       , "DNS_ERROR_ZONE_REQUIRES_MASTER_IP"                       , "This zone must be configured with one or more master DNS server IP addresses.")
   CASE_ERRCODE(DNS_ERROR_ZONE_IS_SHUTDOWN                              , "DNS_ERROR_ZONE_IS_SHUTDOWN"                              , "The operation cannot be performed because this zone is shutdown.")
   CASE_ERRCODE(DNS_ERROR_PRIMARY_REQUIRES_DATAFILE                     , "DNS_ERROR_PRIMARY_REQUIRES_DATAFILE"                     , "Primary DNS zone requires datafile.")
   CASE_ERRCODE(DNS_ERROR_INVALID_DATAFILE_NAME                         , "DNS_ERROR_INVALID_DATAFILE_NAME"                         , "Invalid datafile name for DNS zone.")
   CASE_ERRCODE(DNS_ERROR_DATAFILE_OPEN_FAILURE                         , "DNS_ERROR_DATAFILE_OPEN_FAILURE"                         , "Failed to open datafile for DNS zone.")
   CASE_ERRCODE(DNS_ERROR_FILE_WRITEBACK_FAILED                         , "DNS_ERROR_FILE_WRITEBACK_FAILED"                         , "Failed to write datafile for DNS zone.")
   CASE_ERRCODE(DNS_ERROR_DATAFILE_PARSING                              , "DNS_ERROR_DATAFILE_PARSING"                              , "Failure while reading datafile for DNS zone.")
   CASE_ERRCODE(DNS_ERROR_RECORD_DOES_NOT_EXIST                         , "DNS_ERROR_RECORD_DOES_NOT_EXIST"                         , "DNS record does not exist.")
   CASE_ERRCODE(DNS_ERROR_RECORD_FORMAT                                 , "DNS_ERROR_RECORD_FORMAT"                                 , "DNS record format error.")
   CASE_ERRCODE(DNS_ERROR_NODE_CREATION_FAILED                          , "DNS_ERROR_NODE_CREATION_FAILED"                          , "Node creation failure in DNS.")
   CASE_ERRCODE(DNS_ERROR_UNKNOWN_RECORD_TYPE                           , "DNS_ERROR_UNKNOWN_RECORD_TYPE"                           , "Unknown DNS record type.")
   CASE_ERRCODE(DNS_ERROR_RECORD_TIMED_OUT                              , "DNS_ERROR_RECORD_TIMED_OUT"                              , "DNS record timed out.")
   CASE_ERRCODE(DNS_ERROR_NAME_NOT_IN_ZONE                              , "DNS_ERROR_NAME_NOT_IN_ZONE"                              , "Name not in DNS zone.")
   CASE_ERRCODE(DNS_ERROR_CNAME_LOOP                                    , "DNS_ERROR_CNAME_LOOP"                                    , "CNAME loop detected.")
   CASE_ERRCODE(DNS_ERROR_NODE_IS_CNAME                                 , "DNS_ERROR_NODE_IS_CNAME"                                 , "Node is a CNAME DNS record.")
   CASE_ERRCODE(DNS_ERROR_CNAME_COLLISION                               , "DNS_ERROR_CNAME_COLLISION"                               , "A CNAME record already exists for given name.")
   CASE_ERRCODE(DNS_ERROR_RECORD_ONLY_AT_ZONE_ROOT                      , "DNS_ERROR_RECORD_ONLY_AT_ZONE_ROOT"                      , "Record only at DNS zone root.")
   CASE_ERRCODE(DNS_ERROR_RECORD_ALREADY_EXISTS                         , "DNS_ERROR_RECORD_ALREADY_EXISTS"                         , "DNS record already exists.")
   CASE_ERRCODE(DNS_ERROR_SECONDARY_DATA                                , "DNS_ERROR_SECONDARY_DATA"                                , "Secondary DNS zone data error.")
   CASE_ERRCODE(DNS_ERROR_NO_CREATE_CACHE_DATA                          , "DNS_ERROR_NO_CREATE_CACHE_DATA"                          , "Could not create DNS cache data.")
   CASE_ERRCODE(DNS_ERROR_NAME_DOES_NOT_EXIST                           , "DNS_ERROR_NAME_DOES_NOT_EXIST"                           , "DNS name does not exist.")
   CASE_ERRCODE(DNS_WARNING_PTR_CREATE_FAILED                           , "DNS_WARNING_PTR_CREATE_FAILED"                           , "Could not create pointer (PTR) record.")
   CASE_ERRCODE(DNS_WARNING_DOMAIN_UNDELETED                            , "DNS_WARNING_DOMAIN_UNDELETED"                            , "DNS domain was undeleted.")
   CASE_ERRCODE(DNS_ERROR_DS_UNAVAILABLE                                , "DNS_ERROR_DS_UNAVAILABLE"                                , "The directory service is unavailable.")
   CASE_ERRCODE(DNS_ERROR_DS_ZONE_ALREADY_EXISTS                        , "DNS_ERROR_DS_ZONE_ALREADY_EXISTS"                        , "DNS zone already exists in the directory service.")
   CASE_ERRCODE(DNS_ERROR_NO_BOOTFILE_IF_DS_ZONE                        , "DNS_ERROR_NO_BOOTFILE_IF_DS_ZONE"                        , "DNS server not creating or reading the boot file for the directory service integrated DNS zone.")
   CASE_ERRCODE(DNS_INFO_AXFR_COMPLETE                                  , "DNS_INFO_AXFR_COMPLETE"                                  , "DNS AXFR (zone transfer) complete.")
   CASE_ERRCODE(DNS_ERROR_AXFR                                          , "DNS_ERROR_AXFR"                                          , "DNS zone transfer failed.")
   CASE_ERRCODE(DNS_INFO_ADDED_LOCAL_WINS                               , "DNS_INFO_ADDED_LOCAL_WINS"                               , "Added local WINS server.")
   CASE_ERRCODE(DNS_STATUS_CONTINUE_NEEDED                              , "DNS_STATUS_CONTINUE_NEEDED"                              , "Secure update call needs to continue update request.")
   CASE_ERRCODE(DNS_ERROR_NO_TCPIP                                      , "DNS_ERROR_NO_TCPIP"                                      , "TCP/IP network protocol not installed.")
   CASE_ERRCODE(DNS_ERROR_NO_DNS_SERVERS                                , "DNS_ERROR_NO_DNS_SERVERS"                                , "No DNS servers configured for local system.")
   CASE_ERRCODE(DNS_ERROR_DP_DOES_NOT_EXIST                             , "DNS_ERROR_DP_DOES_NOT_EXIST"                             , "The specified directory partition does not exist.")
   CASE_ERRCODE(DNS_ERROR_DP_ALREADY_EXISTS                             , "DNS_ERROR_DP_ALREADY_EXISTS"                             , "The specified directory partition already exists.")
   CASE_ERRCODE(DNS_ERROR_DP_NOT_ENLISTED                               , "DNS_ERROR_DP_NOT_ENLISTED"                               , "The DNS server is not enlisted in the specified directory partition.")
   CASE_ERRCODE(DNS_ERROR_DP_ALREADY_ENLISTED                           , "DNS_ERROR_DP_ALREADY_ENLISTED"                           , "The DNS server is already enlisted in the specified directory partition.")
   CASE_ERRCODE(DNS_ERROR_DP_NOT_AVAILABLE                              , "DNS_ERROR_DP_NOT_AVAILABLE"                              , "The directory partition is not available at this time. Please wait a few minutes and try again.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(WSAEINTR                                                , "WSAEINTR"                                                , "A blocking operation was interrupted by a call to WSACancelBlockingCall.")
   CASE_ERRCODE(WSAEBADF                                                , "WSAEBADF"                                                , "The file handle supplied is not valid.")
   CASE_ERRCODE(WSAEACCES                                               , "WSAEACCES"                                               , "An attempt was made to access a socket in a way forbidden by its access permissions.")
   CASE_ERRCODE(WSAEFAULT                                               , "WSAEFAULT"                                               , "The system detected an invalid pointer address in attempting to use a pointer argument in a call.")
   CASE_ERRCODE(WSAEINVAL                                               , "WSAEINVAL"                                               , "An invalid argument was supplied.")
   CASE_ERRCODE(WSAEMFILE                                               , "WSAEMFILE"                                               , "Too many open sockets.")
   CASE_ERRCODE(WSAEWOULDBLOCK                                          , "WSAEWOULDBLOCK"                                          , "A non-blocking socket operation could not be completed immediately.")
   CASE_ERRCODE(WSAEINPROGRESS                                          , "WSAEINPROGRESS"                                          , "A blocking operation is currently executing.")
   CASE_ERRCODE(WSAEALREADY                                             , "WSAEALREADY"                                             , "An operation was attempted on a non-blocking socket that already had an operation in progress.")
   CASE_ERRCODE(WSAENOTSOCK                                             , "WSAENOTSOCK"                                             , "An operation was attempted on something that is not a socket.")
   CASE_ERRCODE(WSAEDESTADDRREQ                                         , "WSAEDESTADDRREQ"                                         , "A required address was omitted from an operation on a socket.")
   CASE_ERRCODE(WSAEMSGSIZE                                             , "WSAEMSGSIZE"                                             , "A message sent on a datagram socket was larger than the internal message buffer or some other network limit, or the buffer used to receive a datagram into was smaller than the datagram itself.")
   CASE_ERRCODE(WSAEPROTOTYPE                                           , "WSAEPROTOTYPE"                                           , "A protocol was specified in the socket function call that does not support the semantics of the socket type requested.")
   CASE_ERRCODE(WSAENOPROTOOPT                                          , "WSAENOPROTOOPT"                                          , "An unknown, invalid, or unsupported option or level was specified in a getsockopt or setsockopt call.")
   CASE_ERRCODE(WSAEPROTONOSUPPORT                                      , "WSAEPROTONOSUPPORT"                                      , "The requested protocol has not been configured into the system, or no implementation for it exists.")
   CASE_ERRCODE(WSAESOCKTNOSUPPORT                                      , "WSAESOCKTNOSUPPORT"                                      , "The support for the specified socket type does not exist in this address family.")
   CASE_ERRCODE(WSAEOPNOTSUPP                                           , "WSAEOPNOTSUPP"                                           , "The attempted operation is not supported for the type of object referenced.")
   CASE_ERRCODE(WSAEPFNOSUPPORT                                         , "WSAEPFNOSUPPORT"                                         , "The protocol family has not been configured into the system or no implementation for it exists.")
   CASE_ERRCODE(WSAEAFNOSUPPORT                                         , "WSAEAFNOSUPPORT"                                         , "An address incompatible with the requested protocol was used.")
   CASE_ERRCODE(WSAEADDRINUSE                                           , "WSAEADDRINUSE"                                           , "Only one usage of each socket address (protocol/network address/port) is normally permitted.")
   CASE_ERRCODE(WSAEADDRNOTAVAIL                                        , "WSAEADDRNOTAVAIL"                                        , "The requested address is not valid in its context.")
   CASE_ERRCODE(WSAENETDOWN                                             , "WSAENETDOWN"                                             , "A socket operation encountered a dead network.")
   CASE_ERRCODE(WSAENETUNREACH                                          , "WSAENETUNREACH"                                          , "A socket operation was attempted to an unreachable network.")
   CASE_ERRCODE(WSAENETRESET                                            , "WSAENETRESET"                                            , "The connection has been broken due to keep-alive activity detecting a failure while the operation was in progress.")
   CASE_ERRCODE(WSAECONNABORTED                                         , "WSAECONNABORTED"                                         , "An established connection was aborted by the software in your host machine.")
   CASE_ERRCODE(WSAECONNRESET                                           , "WSAECONNRESET"                                           , "An existing connection was forcibly closed by the remote host.")
   CASE_ERRCODE(WSAENOBUFS                                              , "WSAENOBUFS"                                              , "An operation on a socket could not be performed because the system lacked sufficient buffer space or because a queue was full.")
   CASE_ERRCODE(WSAEISCONN                                              , "WSAEISCONN"                                              , "A connect request was made on an already connected socket.")
   CASE_ERRCODE(WSAENOTCONN                                             , "WSAENOTCONN"                                             , "A request to send or receive data was disallowed because the socket is not connected and (when sending on a datagram socket using a sendto call) no address was supplied.")
   CASE_ERRCODE(WSAESHUTDOWN                                            , "WSAESHUTDOWN"                                            , "A request to send or receive data was disallowed because the socket had already been shut down in that direction with a previous shutdown call.")
   CASE_ERRCODE(WSAETOOMANYREFS                                         , "WSAETOOMANYREFS"                                         , "Too many references to some kernel object.")
   CASE_ERRCODE(WSAETIMEDOUT                                            , "WSAETIMEDOUT"                                            , "A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond.")
   CASE_ERRCODE(WSAECONNREFUSED                                         , "WSAECONNREFUSED"                                         , "No connection could be made because the target machine actively refused it.")
   CASE_ERRCODE(WSAELOOP                                                , "WSAELOOP"                                                , "Cannot translate name.")
   CASE_ERRCODE(WSAENAMETOOLONG                                         , "WSAENAMETOOLONG"                                         , "Name component or name was too long.")
   CASE_ERRCODE(WSAEHOSTDOWN                                            , "WSAEHOSTDOWN"                                            , "A socket operation failed because the destination host was down.")
   CASE_ERRCODE(WSAEHOSTUNREACH                                         , "WSAEHOSTUNREACH"                                         , "A socket operation was attempted to an unreachable host.")
   CASE_ERRCODE(WSAENOTEMPTY                                            , "WSAENOTEMPTY"                                            , "Cannot remove a directory that is not empty.")
   CASE_ERRCODE(WSAEPROCLIM                                             , "WSAEPROCLIM"                                             , "A Windows Sockets implementation may have a limit on the number of applications that may use it simultaneously.")
   CASE_ERRCODE(WSAEUSERS                                               , "WSAEUSERS"                                               , "Ran out of quota.")
   CASE_ERRCODE(WSAEDQUOT                                               , "WSAEDQUOT"                                               , "Ran out of disk quota.")
   CASE_ERRCODE(WSAESTALE                                               , "WSAESTALE"                                               , "File handle reference is no longer available.")
   CASE_ERRCODE(WSAEREMOTE                                              , "WSAEREMOTE"                                              , "Item is not available locally.")
   CASE_ERRCODE(WSASYSNOTREADY                                          , "WSASYSNOTREADY"                                          , "WSAStartup cannot function at this time because the underlying system it uses to provide network services is currently unavailable.")
   CASE_ERRCODE(WSAVERNOTSUPPORTED                                      , "WSAVERNOTSUPPORTED"                                      , "The Windows Sockets version requested is not supported.")
   CASE_ERRCODE(WSANOTINITIALISED                                       , "WSANOTINITIALISED"                                       , "Either the application has not called WSAStartup, or WSAStartup failed.")
   CASE_ERRCODE(WSAEDISCON                                              , "WSAEDISCON"                                              , "Returned by WSARecv or WSARecvFrom to indicate the remote party has initiated a graceful shutdown sequence.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(WSAENOMORE                                              , "WSAENOMORE"                                              , "No more results can be returned by WSALookupServiceNext.")
   CASE_ERRCODE(WSAECANCELLED                                           , "WSAECANCELLED"                                           , "A call to WSALookupServiceEnd was made while this call was still processing. The call has been canceled.")
   CASE_ERRCODE(WSAEINVALIDPROCTABLE                                    , "WSAEINVALIDPROCTABLE"                                    , "The procedure call table is invalid.")
   CASE_ERRCODE(WSAEINVALIDPROVIDER                                     , "WSAEINVALIDPROVIDER"                                     , "The requested service provider is invalid.")
   CASE_ERRCODE(WSAEPROVIDERFAILEDINIT                                  , "WSAEPROVIDERFAILEDINIT"                                  , "The requested service provider could not be loaded or initialized.")
   CASE_ERRCODE(WSASYSCALLFAILURE                                       , "WSASYSCALLFAILURE"                                       , "A system call that should never fail has failed.")
   CASE_ERRCODE(WSASERVICE_NOT_FOUND                                    , "WSASERVICE_NOT_FOUND"                                    , "No such service is known. The service cannot be found in the specified name space.")
   CASE_ERRCODE(WSATYPE_NOT_FOUND                                       , "WSATYPE_NOT_FOUND"                                       , "The specified class was not found.")
   CASE_ERRCODE(WSA_E_NO_MORE                                           , "WSA_E_NO_MORE"                                           , "No more results can be returned by WSALookupServiceNext.")
   CASE_ERRCODE(WSA_E_CANCELLED                                         , "WSA_E_CANCELLED"                                         , "A call to WSALookupServiceEnd was made while this call was still processing. The call has been canceled.")
   CASE_ERRCODE(WSAEREFUSED                                             , "WSAEREFUSED"                                             , "A database query failed because it was actively refused.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(WSAHOST_NOT_FOUND                                       , "WSAHOST_NOT_FOUND"                                       , "No such host is known.")
   CASE_ERRCODE(WSATRY_AGAIN                                            , "WSATRY_AGAIN"                                            , "This is usually a temporary error during hostname resolution and means that the local server did not receive a response from an authoritative server.")
   CASE_ERRCODE(WSANO_RECOVERY                                          , "WSANO_RECOVERY"                                          , "A non-recoverable error occurred during a database lookup.")
   CASE_ERRCODE(WSANO_DATA                                              , "WSANO_DATA"                                              , "The requested name is valid, but no data of the requested type was found.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_ERRCODE(WSA_QOS_RECEIVERS                                       , "WSA_QOS_RECEIVERS"                                       , "At least one reserve has arrived.")
   CASE_ERRCODE(WSA_QOS_SENDERS                                         , "WSA_QOS_SENDERS"                                         , "At least one path has arrived.")
   CASE_ERRCODE(WSA_QOS_NO_SENDERS                                      , "WSA_QOS_NO_SENDERS"                                      , "There are no senders.")
   CASE_ERRCODE(WSA_QOS_NO_RECEIVERS                                    , "WSA_QOS_NO_RECEIVERS"                                    , "There are no receivers.")
   CASE_ERRCODE(WSA_QOS_REQUEST_CONFIRMED                               , "WSA_QOS_REQUEST_CONFIRMED"                               , "Reserve has been confirmed.")
   CASE_ERRCODE(WSA_QOS_ADMISSION_FAILURE                               , "WSA_QOS_ADMISSION_FAILURE"                               , "Error due to lack of resources.")
   CASE_ERRCODE(WSA_QOS_POLICY_FAILURE                                  , "WSA_QOS_POLICY_FAILURE"                                  , "Rejected for administrative reasons - bad credentials.")
   CASE_ERRCODE(WSA_QOS_BAD_STYLE                                       , "WSA_QOS_BAD_STYLE"                                       , "Unknown or conflicting style.")
   CASE_ERRCODE(WSA_QOS_BAD_OBJECT                                      , "WSA_QOS_BAD_OBJECT"                                      , "Problem with some part of the filterspec or providerspecific buffer in general.")
   CASE_ERRCODE(WSA_QOS_TRAFFIC_CTRL_ERROR                              , "WSA_QOS_TRAFFIC_CTRL_ERROR"                              , "Problem with some part of the flowspec.")
   CASE_ERRCODE(WSA_QOS_GENERIC_ERROR                                   , "WSA_QOS_GENERIC_ERROR"                                   , "General QOS error.")
   CASE_ERRCODE(WSA_QOS_ESERVICETYPE                                    , "WSA_QOS_ESERVICETYPE"                                    , "An invalid or unrecognized service type was found in the flowspec.")
   CASE_ERRCODE(WSA_QOS_EFLOWSPEC                                       , "WSA_QOS_EFLOWSPEC"                                       , "An invalid or inconsistent flowspec was found in the QOS structure.")
   CASE_ERRCODE(WSA_QOS_EPROVSPECBUF                                    , "WSA_QOS_EPROVSPECBUF"                                    , "Invalid QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_EFILTERSTYLE                                    , "WSA_QOS_EFILTERSTYLE"                                    , "An invalid QOS filter style was used.")
   CASE_ERRCODE(WSA_QOS_EFILTERTYPE                                     , "WSA_QOS_EFILTERTYPE"                                     , "An invalid QOS filter type was used.")
   CASE_ERRCODE(WSA_QOS_EFILTERCOUNT                                    , "WSA_QOS_EFILTERCOUNT"                                    , "An incorrect number of QOS FILTERSPECs were specified in the FLOWDESCRIPTOR.")
   CASE_ERRCODE(WSA_QOS_EOBJLENGTH                                      , "WSA_QOS_EOBJLENGTH"                                      , "An object with an invalid ObjectLength field was specified in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_EFLOWCOUNT                                      , "WSA_QOS_EFLOWCOUNT"                                      , "An incorrect number of flow descriptors was specified in the QOS structure.")
   CASE_ERRCODE(WSA_QOS_EUNKOWNPSOBJ                                    , "WSA_QOS_EUNKOWNPSOBJ"                                    , "An unrecognized object was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_EPOLICYOBJ                                      , "WSA_QOS_EPOLICYOBJ"                                      , "An invalid policy object was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_EFLOWDESC                                       , "WSA_QOS_EFLOWDESC"                                       , "An invalid QOS flow descriptor was found in the flow descriptor list.")
   CASE_ERRCODE(WSA_QOS_EPSFLOWSPEC                                     , "WSA_QOS_EPSFLOWSPEC"                                     , "An invalid or inconsistent flowspec was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_EPSFILTERSPEC                                   , "WSA_QOS_EPSFILTERSPEC"                                   , "An invalid FILTERSPEC was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_ESDMODEOBJ                                      , "WSA_QOS_ESDMODEOBJ"                                      , "An invalid shape discard mode object was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_ESHAPERATEOBJ                                   , "WSA_QOS_ESHAPERATEOBJ"                                   , "An invalid shaping rate object was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(WSA_QOS_RESERVED_PETYPE                                 , "WSA_QOS_RESERVED_PETYPE"                                 , "A reserved policy element was found in the QOS provider-specific buffer.")
   CASE_ERRCODE(ERROR_IPSEC_QM_POLICY_EXISTS                            , "ERROR_IPSEC_QM_POLICY_EXISTS"                            , "The specified quick mode policy already exists.")
   CASE_ERRCODE(ERROR_IPSEC_QM_POLICY_NOT_FOUND                         , "ERROR_IPSEC_QM_POLICY_NOT_FOUND"                         , "The specified quick mode policy was not found.")
   CASE_ERRCODE(ERROR_IPSEC_QM_POLICY_IN_USE                            , "ERROR_IPSEC_QM_POLICY_IN_USE"                            , "The specified quick mode policy is being used.")
   CASE_ERRCODE(ERROR_IPSEC_MM_POLICY_EXISTS                            , "ERROR_IPSEC_MM_POLICY_EXISTS"                            , "The specified main mode policy already exists.")
   CASE_ERRCODE(ERROR_IPSEC_MM_POLICY_NOT_FOUND                         , "ERROR_IPSEC_MM_POLICY_NOT_FOUND"                         , "The specified main mode policy was not found.")
   CASE_ERRCODE(ERROR_IPSEC_MM_POLICY_IN_USE                            , "ERROR_IPSEC_MM_POLICY_IN_USE"                            , "The specified main mode policy is being used.")
   CASE_ERRCODE(ERROR_IPSEC_MM_FILTER_EXISTS                            , "ERROR_IPSEC_MM_FILTER_EXISTS"                            , "The specified main mode filter already exists.")
   CASE_ERRCODE(ERROR_IPSEC_MM_FILTER_NOT_FOUND                         , "ERROR_IPSEC_MM_FILTER_NOT_FOUND"                         , "The specified main mode filter was not found.")
   CASE_ERRCODE(ERROR_IPSEC_TRANSPORT_FILTER_EXISTS                     , "ERROR_IPSEC_TRANSPORT_FILTER_EXISTS"                     , "The specified transport mode filter already exists.")
   CASE_ERRCODE(ERROR_IPSEC_TRANSPORT_FILTER_NOT_FOUND                  , "ERROR_IPSEC_TRANSPORT_FILTER_NOT_FOUND"                  , "The specified transport mode filter does not exist.")
   CASE_ERRCODE(ERROR_IPSEC_MM_AUTH_EXISTS                              , "ERROR_IPSEC_MM_AUTH_EXISTS"                              , "The specified main mode authentication list exists.")
   CASE_ERRCODE(ERROR_IPSEC_MM_AUTH_NOT_FOUND                           , "ERROR_IPSEC_MM_AUTH_NOT_FOUND"                           , "The specified main mode authentication list was not found.")
   CASE_ERRCODE(ERROR_IPSEC_MM_AUTH_IN_USE                              , "ERROR_IPSEC_MM_AUTH_IN_USE"                              , "The specified quick mode policy is being used.")
   CASE_ERRCODE(ERROR_IPSEC_DEFAULT_MM_POLICY_NOT_FOUND                 , "ERROR_IPSEC_DEFAULT_MM_POLICY_NOT_FOUND"                 , "The specified main mode policy was not found.")
   CASE_ERRCODE(ERROR_IPSEC_DEFAULT_MM_AUTH_NOT_FOUND                   , "ERROR_IPSEC_DEFAULT_MM_AUTH_NOT_FOUND"                   , "The specified quick mode policy was not found.")
   CASE_ERRCODE(ERROR_IPSEC_DEFAULT_QM_POLICY_NOT_FOUND                 , "ERROR_IPSEC_DEFAULT_QM_POLICY_NOT_FOUND"                 , "The manifest file contains one or more syntax errors.")
   CASE_ERRCODE(ERROR_IPSEC_TUNNEL_FILTER_EXISTS                        , "ERROR_IPSEC_TUNNEL_FILTER_EXISTS"                        , "The application attempted to activate a disabled activation context.")
   CASE_ERRCODE(ERROR_IPSEC_TUNNEL_FILTER_NOT_FOUND                     , "ERROR_IPSEC_TUNNEL_FILTER_NOT_FOUND"                     , "The requested lookup key was not found in any active activation context.")
   CASE_ERRCODE(ERROR_IPSEC_MM_FILTER_PENDING_DELETION                  , "ERROR_IPSEC_MM_FILTER_PENDING_DELETION"                  , "The Main Mode filter is pending deletion.")
   CASE_ERRCODE(ERROR_IPSEC_TRANSPORT_FILTER_PENDING_DELETION           , "ERROR_IPSEC_TRANSPORT_FILTER_PENDING_DELETION"           , "The transport filter is pending deletion.")
   CASE_ERRCODE(ERROR_IPSEC_TUNNEL_FILTER_PENDING_DELETION              , "ERROR_IPSEC_TUNNEL_FILTER_PENDING_DELETION"              , "The tunnel filter is pending deletion.")
   CASE_ERRCODE(ERROR_IPSEC_MM_POLICY_PENDING_DELETION                  , "ERROR_IPSEC_MM_POLICY_PENDING_DELETION"                  , "The Main Mode policy is pending deletion.")
   CASE_ERRCODE(ERROR_IPSEC_MM_AUTH_PENDING_DELETION                    , "ERROR_IPSEC_MM_AUTH_PENDING_DELETION"                    , "The Main Mode authentication bundle is pending deletion.")
   CASE_ERRCODE(ERROR_IPSEC_QM_POLICY_PENDING_DELETION                  , "ERROR_IPSEC_QM_POLICY_PENDING_DELETION"                  , "The Quick Mode policy is pending deletion.")
   CASE_ERRCODE(WARNING_IPSEC_MM_POLICY_PRUNED                          , "WARNING_IPSEC_MM_POLICY_PRUNED"                          , "The Main Mode policy was successfully added, but some of the requested offers are not supported.")
   CASE_ERRCODE(WARNING_IPSEC_QM_POLICY_PRUNED                          , "WARNING_IPSEC_QM_POLICY_PRUNED"                          , "The Quick Mode policy was successfully added, but some of the requested offers are not supported.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_AUTH_FAIL                               , "ERROR_IPSEC_IKE_AUTH_FAIL"                               , "IKE authentication credentials are unacceptable.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_ATTRIB_FAIL                             , "ERROR_IPSEC_IKE_ATTRIB_FAIL"                             , "IKE security attributes are unacceptable.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NEGOTIATION_PENDING                     , "ERROR_IPSEC_IKE_NEGOTIATION_PENDING"                     , "IKE Negotiation in progress.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_GENERAL_PROCESSING_ERROR                , "ERROR_IPSEC_IKE_GENERAL_PROCESSING_ERROR"                , "General processing error.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_TIMED_OUT                               , "ERROR_IPSEC_IKE_TIMED_OUT"                               , "Negotiation timed out.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_CERT                                 , "ERROR_IPSEC_IKE_NO_CERT"                                 , "IKE failed to find valid machine certificate.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SA_DELETED                              , "ERROR_IPSEC_IKE_SA_DELETED"                              , "IKE SA deleted by peer before establishment completed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SA_REAPED                               , "ERROR_IPSEC_IKE_SA_REAPED"                               , "IKE SA deleted before establishment completed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_MM_ACQUIRE_DROP                         , "ERROR_IPSEC_IKE_MM_ACQUIRE_DROP"                         , "Negotiation request sat in Queue too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_QM_ACQUIRE_DROP                         , "ERROR_IPSEC_IKE_QM_ACQUIRE_DROP"                         , "Negotiation request sat in Queue too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_QUEUE_DROP_MM                           , "ERROR_IPSEC_IKE_QUEUE_DROP_MM"                           , "Negotiation request sat in Queue too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_QUEUE_DROP_NO_MM                        , "ERROR_IPSEC_IKE_QUEUE_DROP_NO_MM"                        , "Negotiation request sat in Queue too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_DROP_NO_RESPONSE                        , "ERROR_IPSEC_IKE_DROP_NO_RESPONSE"                        , "No response from peer.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_MM_DELAY_DROP                           , "ERROR_IPSEC_IKE_MM_DELAY_DROP"                           , "Negotiation took too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_QM_DELAY_DROP                           , "ERROR_IPSEC_IKE_QM_DELAY_DROP"                           , "Negotiation took too long.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_ERROR                                   , "ERROR_IPSEC_IKE_ERROR"                                   , "Unknown error occurred.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_CRL_FAILED                              , "ERROR_IPSEC_IKE_CRL_FAILED"                              , "Certificate Revocation Check failed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_KEY_USAGE                       , "ERROR_IPSEC_IKE_INVALID_KEY_USAGE"                       , "Invalid certificate key usage.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_CERT_TYPE                       , "ERROR_IPSEC_IKE_INVALID_CERT_TYPE"                       , "Invalid certificate type.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_PRIVATE_KEY                          , "ERROR_IPSEC_IKE_NO_PRIVATE_KEY"                          , "No private key associated with machine certificate.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_DH_FAIL                                 , "ERROR_IPSEC_IKE_DH_FAIL"                                 , "Failure in Diffie-Helman computation.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_HEADER                          , "ERROR_IPSEC_IKE_INVALID_HEADER"                          , "Invalid header.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_POLICY                               , "ERROR_IPSEC_IKE_NO_POLICY"                               , "No policy configured.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_SIGNATURE                       , "ERROR_IPSEC_IKE_INVALID_SIGNATURE"                       , "Failed to verify signature.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_KERBEROS_ERROR                          , "ERROR_IPSEC_IKE_KERBEROS_ERROR"                          , "Failed to authenticate using Kerberos.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_PUBLIC_KEY                           , "ERROR_IPSEC_IKE_NO_PUBLIC_KEY"                           , "Peer's certificate did not have a public key.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR                             , "ERROR_IPSEC_IKE_PROCESS_ERR"                             , "Error processing error payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_SA                          , "ERROR_IPSEC_IKE_PROCESS_ERR_SA"                          , "Error processing SA payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_PROP                        , "ERROR_IPSEC_IKE_PROCESS_ERR_PROP"                        , "Error processing Proposal payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_TRANS                       , "ERROR_IPSEC_IKE_PROCESS_ERR_TRANS"                       , "Error processing Transform payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_KE                          , "ERROR_IPSEC_IKE_PROCESS_ERR_KE"                          , "Error processing KE payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_ID                          , "ERROR_IPSEC_IKE_PROCESS_ERR_ID"                          , "Error processing ID payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_CERT                        , "ERROR_IPSEC_IKE_PROCESS_ERR_CERT"                        , "Error processing Cert payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_CERT_REQ                    , "ERROR_IPSEC_IKE_PROCESS_ERR_CERT_REQ"                    , "Error processing Certificate Request payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_HASH                        , "ERROR_IPSEC_IKE_PROCESS_ERR_HASH"                        , "Error processing Hash payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_SIG                         , "ERROR_IPSEC_IKE_PROCESS_ERR_SIG"                         , "Error processing Signature payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_NONCE                       , "ERROR_IPSEC_IKE_PROCESS_ERR_NONCE"                       , "Error processing Nonce payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_NOTIFY                      , "ERROR_IPSEC_IKE_PROCESS_ERR_NOTIFY"                      , "Error processing Notify payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_DELETE                      , "ERROR_IPSEC_IKE_PROCESS_ERR_DELETE"                      , "Error processing Delete Payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PROCESS_ERR_VENDOR                      , "ERROR_IPSEC_IKE_PROCESS_ERR_VENDOR"                      , "Error processing VendorId payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_PAYLOAD                         , "ERROR_IPSEC_IKE_INVALID_PAYLOAD"                         , "Invalid payload received.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_LOAD_SOFT_SA                            , "ERROR_IPSEC_IKE_LOAD_SOFT_SA"                            , "Soft SA loaded.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SOFT_SA_TORN_DOWN                       , "ERROR_IPSEC_IKE_SOFT_SA_TORN_DOWN"                       , "Soft SA torn down.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_COOKIE                          , "ERROR_IPSEC_IKE_INVALID_COOKIE"                          , "Invalid cookie received..")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_PEER_CERT                            , "ERROR_IPSEC_IKE_NO_PEER_CERT"                            , "Peer failed to send valid machine certificate.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_PEER_CRL_FAILED                         , "ERROR_IPSEC_IKE_PEER_CRL_FAILED"                         , "Certification Revocation check of peer's certificate failed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_POLICY_CHANGE                           , "ERROR_IPSEC_IKE_POLICY_CHANGE"                           , "New policy invalidated SAs formed with old policy.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NO_MM_POLICY                            , "ERROR_IPSEC_IKE_NO_MM_POLICY"                            , "There is no available Main Mode IKE policy.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NOTCBPRIV                               , "ERROR_IPSEC_IKE_NOTCBPRIV"                               , "Failed to enabled TCB privilege.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SECLOADFAIL                             , "ERROR_IPSEC_IKE_SECLOADFAIL"                             , "Failed to load SECURITY.DLL.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_FAILSSPINIT                             , "ERROR_IPSEC_IKE_FAILSSPINIT"                             , "Failed to obtain security function table dispatch address from SSPI.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_FAILQUERYSSP                            , "ERROR_IPSEC_IKE_FAILQUERYSSP"                            , "Failed to query Kerberos package to obtain max token size.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SRVACQFAIL                              , "ERROR_IPSEC_IKE_SRVACQFAIL"                              , "Failed to obtain Kerberos server credentials for ISAKMP/ERROR_IPSEC_IKE service. Kerberos authentication will not function. The most likely reason for this is lack of domain membership. This is normal if your computer is a member of a workgroup.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_SRVQUERYCRED                            , "ERROR_IPSEC_IKE_SRVQUERYCRED"                            , "Failed to determine SSPI principal name for ISAKMP/ERROR_IPSEC_IKE service (QueryCredentialsAttributes).")
   CASE_ERRCODE(ERROR_IPSEC_IKE_GETSPIFAIL                              , "ERROR_IPSEC_IKE_GETSPIFAIL"                              , "Failed to obtain new SPI for the inbound SA from Ipsec driver. The most common cause for this is that the driver does not have the correct filter. Check your policy to verify the filters.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_FILTER                          , "ERROR_IPSEC_IKE_INVALID_FILTER"                          , "Given filter is invalid.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_OUT_OF_MEMORY                           , "ERROR_IPSEC_IKE_OUT_OF_MEMORY"                           , "Memory allocation failed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_ADD_UPDATE_KEY_FAILED                   , "ERROR_IPSEC_IKE_ADD_UPDATE_KEY_FAILED"                   , "Failed to add Security Association to IPSec Driver. The most common cause for this is if the IKE negotiation took too long to complete. If the problem persists, reduce the load on the faulting machine.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_POLICY                          , "ERROR_IPSEC_IKE_INVALID_POLICY"                          , "Invalid policy.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_UNKNOWN_DOI                             , "ERROR_IPSEC_IKE_UNKNOWN_DOI"                             , "Invalid DOI.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_SITUATION                       , "ERROR_IPSEC_IKE_INVALID_SITUATION"                       , "Invalid situation.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_DH_FAILURE                              , "ERROR_IPSEC_IKE_DH_FAILURE"                              , "Diffie-Hellman failure.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_GROUP                           , "ERROR_IPSEC_IKE_INVALID_GROUP"                           , "Invalid Diffie-Hellman group.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_ENCRYPT                                 , "ERROR_IPSEC_IKE_ENCRYPT"                                 , "Error encrypting payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_DECRYPT                                 , "ERROR_IPSEC_IKE_DECRYPT"                                 , "Error decrypting payload.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_POLICY_MATCH                            , "ERROR_IPSEC_IKE_POLICY_MATCH"                            , "Policy match error.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_UNSUPPORTED_ID                          , "ERROR_IPSEC_IKE_UNSUPPORTED_ID"                          , "Unsupported ID.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_HASH                            , "ERROR_IPSEC_IKE_INVALID_HASH"                            , "Hash verification failed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_HASH_ALG                        , "ERROR_IPSEC_IKE_INVALID_HASH_ALG"                        , "Invalid hash algorithm.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_HASH_SIZE                       , "ERROR_IPSEC_IKE_INVALID_HASH_SIZE"                       , "Invalid hash size.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_ENCRYPT_ALG                     , "ERROR_IPSEC_IKE_INVALID_ENCRYPT_ALG"                     , "Invalid encryption algorithm.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_AUTH_ALG                        , "ERROR_IPSEC_IKE_INVALID_AUTH_ALG"                        , "Invalid authentication algorithm.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_SIG                             , "ERROR_IPSEC_IKE_INVALID_SIG"                             , "Invalid certificate signature.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_LOAD_FAILED                             , "ERROR_IPSEC_IKE_LOAD_FAILED"                             , "Load failed.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_RPC_DELETE                              , "ERROR_IPSEC_IKE_RPC_DELETE"                              , "Deleted via RPC call.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_BENIGN_REINIT                           , "ERROR_IPSEC_IKE_BENIGN_REINIT"                           , "Temporary state created to perform reinit. This is not a real failure.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_RESPONDER_LIFETIME_NOTIFY       , "ERROR_IPSEC_IKE_INVALID_RESPONDER_LIFETIME_NOTIFY"       , "The lifetime value received in the Responder Lifetime Notify is below the Windows 2000 configured minimum value. Please fix the policy on the peer machine.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_INVALID_CERT_KEYLEN                     , "ERROR_IPSEC_IKE_INVALID_CERT_KEYLEN"                     , "Key length in certificate is too small for configured security requirements.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_MM_LIMIT                                , "ERROR_IPSEC_IKE_MM_LIMIT"                                , "Max number of established MM SAs to peer exceeded.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NEGOTIATION_DISABLED                    , "ERROR_IPSEC_IKE_NEGOTIATION_DISABLED"                    , "IKE received a policy that disables negotiation.")
   CASE_ERRCODE(ERROR_IPSEC_IKE_NEG_STATUS_END                          , "ERROR_IPSEC_IKE_NEG_STATUS_END"                          , "ERROR_IPSEC_IKE_NEG_STATUS_END")
   CASE_ERRCODE(ERROR_SXS_SECTION_NOT_FOUND                             , "ERROR_SXS_SECTION_NOT_FOUND"                             , "The requested section was not present in the activation context.")
   CASE_ERRCODE(ERROR_SXS_CANT_GEN_ACTCTX                               , "ERROR_SXS_CANT_GEN_ACTCTX"                               , "This application has failed to start because the application configuration is incorrect. Reinstalling the application may fix this problem.")
   CASE_ERRCODE(ERROR_SXS_INVALID_ACTCTXDATA_FORMAT                     , "ERROR_SXS_INVALID_ACTCTXDATA_FORMAT"                     , "The application binding data format is invalid.")
   CASE_ERRCODE(ERROR_SXS_ASSEMBLY_NOT_FOUND                            , "ERROR_SXS_ASSEMBLY_NOT_FOUND"                            , "The referenced assembly is not installed on your system.")
   CASE_ERRCODE(ERROR_SXS_MANIFEST_FORMAT_ERROR                         , "ERROR_SXS_MANIFEST_FORMAT_ERROR"                         , "The manifest file does not begin with the required tag and format information.")
   CASE_ERRCODE(ERROR_SXS_MANIFEST_PARSE_ERROR                          , "ERROR_SXS_MANIFEST_PARSE_ERROR"                          , "The manifest file contains one or more syntax errors.")
   CASE_ERRCODE(ERROR_SXS_ACTIVATION_CONTEXT_DISABLED                   , "ERROR_SXS_ACTIVATION_CONTEXT_DISABLED"                   , "The application attempted to activate a disabled activation context.")
   CASE_ERRCODE(ERROR_SXS_KEY_NOT_FOUND                                 , "ERROR_SXS_KEY_NOT_FOUND"                                 , "The requested lookup key was not found in any active activation context.")
   CASE_ERRCODE(ERROR_SXS_VERSION_CONFLICT                              , "ERROR_SXS_VERSION_CONFLICT"                              , "A component version required by the application conflicts with another component version already active.")
   CASE_ERRCODE(ERROR_SXS_WRONG_SECTION_TYPE                            , "ERROR_SXS_WRONG_SECTION_TYPE"                            , "The type requested activation context section does not match the query API used.")
   CASE_ERRCODE(ERROR_SXS_THREAD_QUERIES_DISABLED                       , "ERROR_SXS_THREAD_QUERIES_DISABLED"                       , "Lack of system resources has required isolated activation to be disabled for the current thread of execution.")
   CASE_ERRCODE(ERROR_SXS_PROCESS_DEFAULT_ALREADY_SET                   , "ERROR_SXS_PROCESS_DEFAULT_ALREADY_SET"                   , "An attempt to set the process default activation context failed because the process default activation context was already set.")
   CASE_ERRCODE(ERROR_SXS_UNKNOWN_ENCODING_GROUP                        , "ERROR_SXS_UNKNOWN_ENCODING_GROUP"                        , "The encoding group identifier specified is not recognized.")
   CASE_ERRCODE(ERROR_SXS_UNKNOWN_ENCODING                              , "ERROR_SXS_UNKNOWN_ENCODING"                              , "The encoding requested is not recognized.")
   CASE_ERRCODE(ERROR_SXS_INVALID_XML_NAMESPACE_URI                     , "ERROR_SXS_INVALID_XML_NAMESPACE_URI"                     , "The manifest contains a reference to an invalid URI.")
   CASE_ERRCODE(ERROR_SXS_ROOT_MANIFEST_DEPENDENCY_NOT_INSTALLED        , "ERROR_SXS_ROOT_MANIFEST_DEPENDENCY_NOT_INSTALLED"        , "The application manifest contains a reference to a dependent assembly which is not installed.")
   CASE_ERRCODE(ERROR_SXS_LEAF_MANIFEST_DEPENDENCY_NOT_INSTALLED        , "ERROR_SXS_LEAF_MANIFEST_DEPENDENCY_NOT_INSTALLED"        , "The manifest for an assembly used by the application has a reference to a dependent assembly which is not installed.")
   CASE_ERRCODE(ERROR_SXS_INVALID_ASSEMBLY_IDENTITY_ATTRIBUTE           , "ERROR_SXS_INVALID_ASSEMBLY_IDENTITY_ATTRIBUTE"           , "The manifest contains an attribute for the assembly identity which is not valid.")
   CASE_ERRCODE(ERROR_SXS_MANIFEST_MISSING_REQUIRED_DEFAULT_NAMESPACE   , "ERROR_SXS_MANIFEST_MISSING_REQUIRED_DEFAULT_NAMESPACE"   , "The manifest is missing the required default namespace specification on the assembly element.")
   CASE_ERRCODE(ERROR_SXS_MANIFEST_INVALID_REQUIRED_DEFAULT_NAMESPACE   , "ERROR_SXS_MANIFEST_INVALID_REQUIRED_DEFAULT_NAMESPACE"   , "The manifest has a default namespace specified on the assembly element but its value is not 'urn:schemas-microsoft-com:asm.v1'.")
   CASE_ERRCODE(ERROR_SXS_PRIVATE_MANIFEST_CROSS_PATH_WITH_REPARSE_POINT, "ERROR_SXS_PRIVATE_MANIFEST_CROSS_PATH_WITH_REPARSE_POINT", "The private manifest probe has crossed the reparse-point-associated path.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_DLL_NAME                            , "ERROR_SXS_DUPLICATE_DLL_NAME"                            , "Two or more components referenced directly or indirectly by the application manifest have files by the same name.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_WINDOWCLASS_NAME                    , "ERROR_SXS_DUPLICATE_WINDOWCLASS_NAME"                    , "Two or more components referenced directly or indirectly by the application manifest have window classes with the same name.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_CLSID                               , "ERROR_SXS_DUPLICATE_CLSID"                               , "Two or more components referenced directly or indirectly by the application manifest have the same COM server CLSIDs.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_IID                                 , "ERROR_SXS_DUPLICATE_IID"                                 , "Two or more components referenced directly or indirectly by the application manifest have proxies for the same COM interface IIDs.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_TLBID                               , "ERROR_SXS_DUPLICATE_TLBID"                               , "Two or more components referenced directly or indirectly by the application manifest have the same COM type library TLBIDs.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_PROGID                              , "ERROR_SXS_DUPLICATE_PROGID"                              , "Two or more components referenced directly or indirectly by the application manifest have the same COM ProgIDs.")
   CASE_ERRCODE(ERROR_SXS_DUPLICATE_ASSEMBLY_NAME                       , "ERROR_SXS_DUPLICATE_ASSEMBLY_NAME"                       , "Two or more components referenced directly or indirectly by the application manifest are different versions of the same component which is not permitted.")
   CASE_ERRCODE(ERROR_SXS_FILE_HASH_MISMATCH                            , "ERROR_SXS_FILE_HASH_MISMATCH"                            , "A component's file does not match the verification information present in the component manifest.")
   CASE_ERRCODE(ERROR_SXS_POLICY_PARSE_ERROR                            , "ERROR_SXS_POLICY_PARSE_ERROR"                            , "The policy manifest contains one or more syntax errors.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSINGQUOTE                            , "ERROR_SXS_XML_E_MISSINGQUOTE"                            , "Manifest Parse Error: A string literal was expected, but no opening quote character was found.")
   CASE_ERRCODE(ERROR_SXS_XML_E_COMMENTSYNTAX                           , "ERROR_SXS_XML_E_COMMENTSYNTAX"                           , "Manifest Parse Error: Incorrect syntax was used in a comment.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADSTARTNAMECHAR                        , "ERROR_SXS_XML_E_BADSTARTNAMECHAR"                        , "Manifest Parse Error: A name was started with an invalid character.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADNAMECHAR                             , "ERROR_SXS_XML_E_BADNAMECHAR"                             , "Manifest Parse Error: A name contained an invalid character.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADCHARINSTRING                         , "ERROR_SXS_XML_E_BADCHARINSTRING"                         , "Manifest Parse Error: A string literal contained an invalid character.")
   CASE_ERRCODE(ERROR_SXS_XML_E_XMLDECLSYNTAX                           , "ERROR_SXS_XML_E_XMLDECLSYNTAX"                           , "Manifest Parse Error: Invalid syntax for an XML declaration.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADCHARDATA                             , "ERROR_SXS_XML_E_BADCHARDATA"                             , "Manifest Parse Error: An invalid character was found in text content.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSINGWHITESPACE                       , "ERROR_SXS_XML_E_MISSINGWHITESPACE"                       , "Manifest Parse Error: Required white space was missing.")
   CASE_ERRCODE(ERROR_SXS_XML_E_EXPECTINGTAGEND                         , "ERROR_SXS_XML_E_EXPECTINGTAGEND"                         , "Manifest Parse Error: The character '>' was expected.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSINGSEMICOLON                        , "ERROR_SXS_XML_E_MISSINGSEMICOLON"                        , "Manifest Parse Error: A semi colon character was expected.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNBALANCEDPAREN                         , "ERROR_SXS_XML_E_UNBALANCEDPAREN"                         , "Manifest Parse Error: Unbalanced parentheses.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INTERNALERROR                           , "ERROR_SXS_XML_E_INTERNALERROR"                           , "Manifest Parse Error: Internal error.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNEXPECTED_WHITESPACE                   , "ERROR_SXS_XML_E_UNEXPECTED_WHITESPACE"                   , "Manifest Parse Error: White space is not allowed at this location.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INCOMPLETE_ENCODING                     , "ERROR_SXS_XML_E_INCOMPLETE_ENCODING"                     , "Manifest Parse Error: End of file reached in invalid state for current encoding.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSING_PAREN                           , "ERROR_SXS_XML_E_MISSING_PAREN"                           , "Manifest Parse Error: Missing parenthesis.")
   CASE_ERRCODE(ERROR_SXS_XML_E_EXPECTINGCLOSEQUOTE                     , "ERROR_SXS_XML_E_EXPECTINGCLOSEQUOTE"                     , "Manifest Parse Error: A single or double closing quote character (\' or \") is missing.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MULTIPLE_COLONS                         , "ERROR_SXS_XML_E_MULTIPLE_COLONS"                         , "Manifest Parse Error: Multiple colons are not allowed in a name.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALID_DECIMAL                         , "ERROR_SXS_XML_E_INVALID_DECIMAL"                         , "Manifest Parse Error: Invalid character for decimal digit.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALID_HEXIDECIMAL                     , "ERROR_SXS_XML_E_INVALID_HEXIDECIMAL"                     , "Manifest Parse Error: Invalid character for hexadecimal digit.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALID_UNICODE                         , "ERROR_SXS_XML_E_INVALID_UNICODE"                         , "Manifest Parse Error: Invalid Unicode character value for this platform.")
   CASE_ERRCODE(ERROR_SXS_XML_E_WHITESPACEORQUESTIONMARK                , "ERROR_SXS_XML_E_WHITESPACEORQUESTIONMARK"                , "Manifest Parse Error: Expecting white space or '?'.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNEXPECTEDENDTAG                        , "ERROR_SXS_XML_E_UNEXPECTEDENDTAG"                        , "Manifest Parse Error: End tag was not expected at this location.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDTAG                             , "ERROR_SXS_XML_E_UNCLOSEDTAG"                             , "Manifest Parse Error: The following tags were not closed: %1.")
   CASE_ERRCODE(ERROR_SXS_XML_E_DUPLICATEATTRIBUTE                      , "ERROR_SXS_XML_E_DUPLICATEATTRIBUTE"                      , "Manifest Parse Error: Duplicate attribute.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MULTIPLEROOTS                           , "ERROR_SXS_XML_E_MULTIPLEROOTS"                           , "Manifest Parse Error: Only one top level element is allowed in an XML document.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALIDATROOTLEVEL                      , "ERROR_SXS_XML_E_INVALIDATROOTLEVEL"                      , "Manifest Parse Error: Invalid at the top level of the document.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADXMLDECL                              , "ERROR_SXS_XML_E_BADXMLDECL"                              , "Manifest Parse Error: Invalid XML declaration.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSINGROOT                             , "ERROR_SXS_XML_E_MISSINGROOT"                             , "Manifest Parse Error: XML document must have a top level element.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNEXPECTEDEOF                           , "ERROR_SXS_XML_E_UNEXPECTEDEOF"                           , "Manifest Parse Error: Unexpected end of file.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADPEREFINSUBSET                        , "ERROR_SXS_XML_E_BADPEREFINSUBSET"                        , "Manifest Parse Error: Parameter entities cannot be used inside markup declarations in an internal subset.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDSTARTTAG                        , "ERROR_SXS_XML_E_UNCLOSEDSTARTTAG"                        , "Manifest Parse Error: Element was not closed.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDENDTAG                          , "ERROR_SXS_XML_E_UNCLOSEDENDTAG"                          , "Manifest Parse Error: End element was missing the character '>'.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDSTRING                          , "ERROR_SXS_XML_E_UNCLOSEDSTRING"                          , "Manifest Parse Error: A string literal was not closed.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDCOMMENT                         , "ERROR_SXS_XML_E_UNCLOSEDCOMMENT"                         , "Manifest Parse Error: A comment was not closed.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDDECL                            , "ERROR_SXS_XML_E_UNCLOSEDDECL"                            , "Manifest Parse Error: A declaration was not closed.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNCLOSEDCDATA                           , "ERROR_SXS_XML_E_UNCLOSEDCDATA"                           , "Manifest Parse Error: A CDATA section was not closed.")
   CASE_ERRCODE(ERROR_SXS_XML_E_RESERVEDNAMESPACE                       , "ERROR_SXS_XML_E_RESERVEDNAMESPACE"                       , "Manifest Parse Error: The namespace prefix is not allowed to start with the reserved string 'xml'.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALIDENCODING                         , "ERROR_SXS_XML_E_INVALIDENCODING"                         , "Manifest Parse Error: System does not support the specified encoding.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALIDSWITCH                           , "ERROR_SXS_XML_E_INVALIDSWITCH"                           , "Manifest Parse Error: Switch from current encoding to specified encoding not supported.")
   CASE_ERRCODE(ERROR_SXS_XML_E_BADXMLCASE                              , "ERROR_SXS_XML_E_BADXMLCASE"                              , "Manifest Parse Error: The name 'xml' is reserved and must be lower case.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALID_STANDALONE                      , "ERROR_SXS_XML_E_INVALID_STANDALONE"                      , "Manifest Parse Error: The standalone attribute must have the value 'yes' or 'no'.")
   CASE_ERRCODE(ERROR_SXS_XML_E_UNEXPECTED_STANDALONE                   , "ERROR_SXS_XML_E_UNEXPECTED_STANDALONE"                   , "Manifest Parse Error: The standalone attribute cannot be used in external entities.")
   CASE_ERRCODE(ERROR_SXS_XML_E_INVALID_VERSION                         , "ERROR_SXS_XML_E_INVALID_VERSION"                         , "Manifest Parse Error: Invalid version number.")
   CASE_ERRCODE(ERROR_SXS_XML_E_MISSINGEQUALS                           , "ERROR_SXS_XML_E_MISSINGEQUALS"                           , "Manifest Parse Error: Missing equals sign between attribute and attribute value.")
   CASE_ERRCODE(ERROR_SXS_PROTECTION_RECOVERY_FAILED                    , "ERROR_SXS_PROTECTION_RECOVERY_FAILED"                    , "Assembly Protection Error: Unable to recover the specified assembly.")
   CASE_ERRCODE(ERROR_SXS_PROTECTION_PUBLIC_KEY_TOO_SHORT               , "ERROR_SXS_PROTECTION_PUBLIC_KEY_TOO_SHORT"               , "Assembly Protection Error: The public key for an assembly was too short to be allowed.")
   CASE_ERRCODE(ERROR_SXS_PROTECTION_CATALOG_NOT_VALID                  , "ERROR_SXS_PROTECTION_CATALOG_NOT_VALID"                  , "Assembly Protection Error: The catalog for an assembly is not valid, or does not match the assembly's manifest.")
   CASE_ERRCODE(ERROR_SXS_UNTRANSLATABLE_HRESULT                        , "ERROR_SXS_UNTRANSLATABLE_HRESULT"                        , "An HRESULT could not be translated to a corresponding Win32 error code.")
   CASE_ERRCODE(ERROR_SXS_PROTECTION_CATALOG_FILE_MISSING               , "ERROR_SXS_PROTECTION_CATALOG_FILE_MISSING"               , "Assembly Protection Error: The catalog for an assembly is missing.")
   CASE_ERRCODE(ERROR_SXS_MISSING_ASSEMBLY_IDENTITY_ATTRIBUTE           , "ERROR_SXS_MISSING_ASSEMBLY_IDENTITY_ATTRIBUTE"           , "The supplied assembly identity is missing one or more attributes which must be present in this context.")
   CASE_ERRCODE(ERROR_SXS_INVALID_ASSEMBLY_IDENTITY_ATTRIBUTE_NAME      , "ERROR_SXS_INVALID_ASSEMBLY_IDENTITY_ATTRIBUTE_NAME"      , "The supplied assembly identity has one or more attribute names that contain characters not permitted in XML names.")

   CASE_ERRCODE(ERROR_INCOMPATIBLE_TCI_VERSION                          , "ERROR_INCOMPATIBLE_TCI_VERSION"                          , "Traffic Control error. Incompatible TC version number")
   CASE_ERRCODE(ERROR_INVALID_SERVICE_TYPE                              , "ERROR_INVALID_SERVICE_TYPE"                              , "Traffic Control error. Unspecified or bad intserv service type")
   CASE_ERRCODE(ERROR_INVALID_TOKEN_RATE                                , "ERROR_INVALID_TOKEN_RATE"                                , "Traffic Control error. Unspecified or bad TokenRate")
   CASE_ERRCODE(ERROR_INVALID_PEAK_RATE                                 , "ERROR_INVALID_PEAK_RATE"                                 , "Traffic Control error. Bad PeakBandwidth")
   CASE_ERRCODE(ERROR_INVALID_SD_MODE                                   , "ERROR_INVALID_SD_MODE"                                   , "Traffic Control error. Invalid ShapeDiscardMode")
   CASE_ERRCODE(ERROR_INVALID_QOS_PRIORITY                              , "ERROR_INVALID_QOS_PRIORITY"                              , "Traffic Control error. Invalid priority value")
   CASE_ERRCODE(ERROR_INVALID_TRAFFIC_CLASS                             , "ERROR_INVALID_TRAFFIC_CLASS"                             , "Traffic Control error. Invalid traffic class value")
   CASE_ERRCODE(ERROR_INVALID_ADDRESS_TYPE                              , "ERROR_INVALID_ADDRESS_TYPE"                              , "Traffic Control error. Invalid address type")
   CASE_ERRCODE(ERROR_DUPLICATE_FILTER                                  , "ERROR_DUPLICATE_FILTER"                                  , "Traffic Control error. Attempt to install identical filter on same flow")
   CASE_ERRCODE(ERROR_FILTER_CONFLICT                                   , "ERROR_FILTER_CONFLICT"                                   , "Traffic Control error. Attempt to install conflicting filter")
   CASE_ERRCODE(ERROR_ADDRESS_TYPE_NOT_SUPPORTED                        , "ERROR_ADDRESS_TYPE_NOT_SUPPORT"                          , "Traffic Control error. This address type is not supportedED")
   CASE_ERRCODE(ERROR_TC_SUPPORTED_OBJECTS_EXIST                        , "ERROR_TC_SUPPORTED_OBJECTS_EXI"                          , "Traffic Control error. This object can not be deleted since its suporting opened objectsST")
   CASE_ERRCODE(ERROR_INCOMPATABLE_QOS                                  , "ERROR_INCOMPATABLE_QOS"                                  , "Traffic Control error. Incompatable QoS parameters")
   CASE_ERRCODE(ERROR_TC_NOT_SUPPORTED                                  , "ERROR_TC_NOT_SUPPORTED"                                  , "Traffic Control error. Traffic Control is not supported in the system")
   CASE_ERRCODE(ERROR_TC_OBJECT_LENGTH_INVALID                          , "ERROR_TC_OBJECT_LENGTH_INVALID"                          , "Traffic Control error. TcObjectsLength is inconsistent with CfInfoSize")
   CASE_ERRCODE(ERROR_INVALID_FLOW_MODE                                 , "ERROR_INVALID_FLOW_MODE"                                 , "Traffic Control error. Adding an Intserv flow in Diffserv mode or vice versa")
   CASE_ERRCODE(ERROR_INVALID_DIFFSERV_FLOW                             , "ERROR_INVALID_DIFFSERV_FLOW"                             , "Traffic Control error. Invalid Diffserv flow")
   CASE_ERRCODE(ERROR_DS_MAPPING_EXISTS                                 , "ERROR_DS_MAPPING_EXISTS"                                 , "Traffic Control error. DS codepoint already exists")
   CASE_ERRCODE(ERROR_INVALID_SHAPE_RATE                                , "ERROR_INVALID_SHAPE_RATE"                                , "Traffic Control error. Invalid Shape Rate specified")
   CASE_ERRCODE(ERROR_INVALID_DS_CLASS                                  , "ERROR_INVALID_DS_CLASS"                                  , "Traffic Control error. Invalid DCLASS")
   CASE_ERRCODE(ERROR_TOO_MANY_CLIENTS                                  , "ERROR_TOO_MANY_CLIENTS"                                  , "Traffic Control error. Too many GPC clients")
#endif // MICROSOFT_SDK_FEBRUARY_2003

   END_CASE_ERRCODE

   if ((extInfo == info_WinInet)
      // && (dwErrCode >= INTERNET_ERROR_BASE)
      // && (dwErrCode <= INTERNET_ERROR_LAST)
      )
   {
      BEGIN_CASE_ERRCODE
      CASE_ERRCODE(INTERNET_ERROR_BASE                                     , "INTERNET_ERROR_BASE"                                     , "Internet API error")
      CASE_ERRCODE(ERROR_INTERNET_OUT_OF_HANDLES                           , "ERROR_INTERNET_OUT_OF_HANDLES"                           , "Internet API error. No more handles could be generated at this time.")
      CASE_ERRCODE(ERROR_INTERNET_TIMEOUT                                  , "ERROR_INTERNET_TIMEOUT"                                  , "Internet API error. The request has timed out.")
      CASE_ERRCODE(ERROR_INTERNET_EXTENDED_ERROR                           , "ERROR_INTERNET_EXTENDED_ERROR"                           , "Internet API error. An extended error was returned from the server. This is typically a string or buffer containing a verbose error message. Call InternetGetLastResponseInfo to retrieve the error text.")
      CASE_ERRCODE(ERROR_INTERNET_INTERNAL_ERROR                           , "ERROR_INTERNET_INTERNAL_ERROR"                           , "Internet API error. An internal error has occurred.")
      CASE_ERRCODE(ERROR_INTERNET_INVALID_URL                              , "ERROR_INTERNET_INVALID_URL"                              , "Internet API error. The URL is invalid.")
      CASE_ERRCODE(ERROR_INTERNET_UNRECOGNIZED_SCHEME                      , "ERROR_INTERNET_UNRECOGNIZED_SCHEME"                      , "Internet API error. The URL scheme could not be recognized, or is not supported.")
      CASE_ERRCODE(ERROR_INTERNET_NAME_NOT_RESOLVED                        , "ERROR_INTERNET_NAME_NOT_RESOLVED"                        , "Internet API error. The server name could not be resolved.")
      CASE_ERRCODE(ERROR_INTERNET_PROTOCOL_NOT_FOUND                       , "ERROR_INTERNET_PROTOCOL_NOT_FOUND"                       , "Internet API error. The requested protocol could not be located.")
      CASE_ERRCODE(ERROR_INTERNET_INVALID_OPTION                           , "ERROR_INTERNET_INVALID_OPTION"                           , "Internet API error. A request to InternetQueryOption or InternetSetOption specified an invalid option value.")
      CASE_ERRCODE(ERROR_INTERNET_BAD_OPTION_LENGTH                        , "ERROR_INTERNET_BAD_OPTION_LENGTH"                        , "Internet API error. The length of an option supplied to InternetQueryOption or InternetSetOption is incorrect for the type of option specified.")
      CASE_ERRCODE(ERROR_INTERNET_OPTION_NOT_SETTABLE                      , "ERROR_INTERNET_OPTION_NOT_SETTABLE"                      , "Internet API error. The requested option cannot be set, only queried.")
      CASE_ERRCODE(ERROR_INTERNET_SHUTDOWN                                 , "ERROR_INTERNET_SHUTDOWN"                                 , "Internet API error. WinINet support is being shut down or unloaded.")
      CASE_ERRCODE(ERROR_INTERNET_INCORRECT_USER_NAME                      , "ERROR_INTERNET_INCORRECT_USER_NAME"                      , "Internet API error. The request to connect and log on to an FTP server could not be completed because the supplied user name is incorrect.")
      CASE_ERRCODE(ERROR_INTERNET_INCORRECT_PASSWORD                       , "ERROR_INTERNET_INCORRECT_PASSWORD"                       , "Internet API error. The request to connect and log on to an FTP server could not be completed because the supplied password is incorrect.")
      CASE_ERRCODE(ERROR_INTERNET_LOGIN_FAILURE                            , "ERROR_INTERNET_LOGIN_FAILURE"                            , "Internet API error. The request to connect and log on to an FTP server failed.")
      CASE_ERRCODE(ERROR_INTERNET_INVALID_OPERATION                        , "ERROR_INTERNET_INVALID_OPERATION"                        , "Internet API error. The requested operation is invalid.")
      CASE_ERRCODE(ERROR_INTERNET_OPERATION_CANCELLED                      , "ERROR_INTERNET_OPERATION_CANCELLED"                      , "Internet API error. The operation was canceled, usually because the handle on which the request was operating was closed before the operation completed.")
      CASE_ERRCODE(ERROR_INTERNET_INCORRECT_HANDLE_TYPE                    , "ERROR_INTERNET_INCORRECT_HANDLE_TYPE"                    , "Internet API error. The type of handle supplied is incorrect for this operation.")
      CASE_ERRCODE(ERROR_INTERNET_INCORRECT_HANDLE_STATE                   , "ERROR_INTERNET_INCORRECT_HANDLE_STATE"                   , "Internet API error. The requested operation cannot be carried out because the handle supplied is not in the correct state.")
      CASE_ERRCODE(ERROR_INTERNET_NOT_PROXY_REQUEST                        , "ERROR_INTERNET_NOT_PROXY_REQUEST"                        , "Internet API error. The request cannot be made via a proxy.")
      CASE_ERRCODE(ERROR_INTERNET_REGISTRY_VALUE_NOT_FOUND                 , "ERROR_INTERNET_REGISTRY_VALUE_NOT_FOUND"                 , "Internet API error. A required registry value could not be located.")
      CASE_ERRCODE(ERROR_INTERNET_BAD_REGISTRY_PARAMETER                   , "ERROR_INTERNET_BAD_REGISTRY_PARAMETER"                   , "Internet API error. A required registry value was located but is an incorrect type or has an invalid value.")
      CASE_ERRCODE(ERROR_INTERNET_NO_DIRECT_ACCESS                         , "ERROR_INTERNET_NO_DIRECT_ACCESS"                         , "Internet API error. Direct network access cannot be made at this time.")
      CASE_ERRCODE(ERROR_INTERNET_NO_CONTEXT                               , "ERROR_INTERNET_NO_CONTEXT"                               , "Internet API error. An asynchronous request could not be made because a zero context value was supplied.")
      CASE_ERRCODE(ERROR_INTERNET_NO_CALLBACK                              , "ERROR_INTERNET_NO_CALLBACK"                              , "Internet API error. An asynchronous request could not be made because a callback function has not been set.")
      CASE_ERRCODE(ERROR_INTERNET_REQUEST_PENDING                          , "ERROR_INTERNET_REQUEST_PENDING"                          , "Internet API error. The required operation could not be completed because one or more requests are pending.")
      CASE_ERRCODE(ERROR_INTERNET_INCORRECT_FORMAT                         , "ERROR_INTERNET_INCORRECT_FORMAT"                         , "Internet API error. The format of the request is invalid.")
      CASE_ERRCODE(ERROR_INTERNET_ITEM_NOT_FOUND                           , "ERROR_INTERNET_ITEM_NOT_FOUND"                           , "Internet API error. The requested item could not be located.")
      CASE_ERRCODE(ERROR_INTERNET_CANNOT_CONNECT                           , "ERROR_INTERNET_CANNOT_CONNECT"                           , "Internet API error. The attempt to connect to the server failed.")
      CASE_ERRCODE(ERROR_INTERNET_CONNECTION_ABORTED                       , "ERROR_INTERNET_CONNECTION_ABORTED"                       , "Internet API error. The connection with the server has been terminated.")
      CASE_ERRCODE(ERROR_INTERNET_CONNECTION_RESET                         , "ERROR_INTERNET_CONNECTION_RESET"                         , "Internet API error. The connection with the server has been reset.")
      CASE_ERRCODE(ERROR_INTERNET_FORCE_RETRY                              , "ERROR_INTERNET_FORCE_RETRY"                              , "Internet API error. The function needs to redo the request.")
      CASE_ERRCODE(ERROR_INTERNET_INVALID_PROXY_REQUEST                    , "ERROR_INTERNET_INVALID_PROXY_REQUEST"                    , "Internet API error. The request to the proxy was invalid.")
      CASE_ERRCODE(ERROR_INTERNET_NEED_UI                                  , "ERROR_INTERNET_NEED_UI"                                  , "Internet API error. A user interface or other blocking operation has been requested.")
      CASE_ERRCODE(ERROR_INTERNET_HANDLE_EXISTS                            , "ERROR_INTERNET_HANDLE_EXISTS"                            , "Internet API error. The request failed because the handle already exists.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_DATE_INVALID                    , "ERROR_INTERNET_SEC_CERT_DATE_INVALID"                    , "Internet API error. SSL certificate date that was received from the server is bad. The certificate is expired.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_CN_INVALID                      , "ERROR_INTERNET_SEC_CERT_CN_INVALID"                      , "Internet API error. SSL certificate common name (host name field) is incorrect—for example, if you entered www.server.com and the common name on the certificate says www.different.com.")
      CASE_ERRCODE(ERROR_INTERNET_HTTP_TO_HTTPS_ON_REDIR                   , "ERROR_INTERNET_HTTP_TO_HTTPS_ON_REDIR"                   , "Internet API error. The application is moving from a non-SSL to an SSL connection because of a redirect.")
      CASE_ERRCODE(ERROR_INTERNET_HTTPS_TO_HTTP_ON_REDIR                   , "ERROR_INTERNET_HTTPS_TO_HTTP_ON_REDIR"                   , "Internet API error. The application is moving from an SSL to an non-SSL connection because of a redirect.")
      CASE_ERRCODE(ERROR_INTERNET_MIXED_SECURITY                           , "ERROR_INTERNET_MIXED_SECURITY"                           , "Internet API error. The content is not entirely secure. Some of the content being viewed may have come from unsecured servers.")
      CASE_ERRCODE(ERROR_INTERNET_CHG_POST_IS_NON_SECURE                   , "ERROR_INTERNET_CHG_POST_IS_NON_SECURE"                   , "Internet API error. The application is posting and attempting to change multiple lines of text on a server that is not secure.")
      CASE_ERRCODE(ERROR_INTERNET_POST_IS_NON_SECURE                       , "ERROR_INTERNET_POST_IS_NON_SECURE"                       , "Internet API error. The application is posting data to a server that is not secure.")
      CASE_ERRCODE(ERROR_INTERNET_CLIENT_AUTH_CERT_NEEDED                  , "ERROR_INTERNET_CLIENT_AUTH_CERT_NEEDED"                  , "Internet API error. The server is requesting client authentication.")
      CASE_ERRCODE(ERROR_INTERNET_INVALID_CA                               , "ERROR_INTERNET_INVALID_CA"                               , "Internet API error. The function is unfamiliar with the Certificate Authority that generated the server's certificate.")
      CASE_ERRCODE(ERROR_INTERNET_CLIENT_AUTH_NOT_SETUP                    , "ERROR_INTERNET_CLIENT_AUTH_NOT_SETUP"                    , "Internet API error. Client authorization is not set up on this computer.")
      CASE_ERRCODE(ERROR_INTERNET_ASYNC_THREAD_FAILED                      , "ERROR_INTERNET_ASYNC_THREAD_FAILED"                      , "Internet API error. The application could not start an asynchronous thread.")
      CASE_ERRCODE(ERROR_INTERNET_REDIRECT_SCHEME_CHANGE                   , "ERROR_INTERNET_REDIRECT_SCHEME_CHANGE"                   , "Internet API error. The function could not handle the redirection, because the scheme changed (for example, HTTP to FTP).")
      CASE_ERRCODE(ERROR_INTERNET_DIALOG_PENDING                           , "ERROR_INTERNET_DIALOG_PENDING"                           , "Internet API error. Another thread has a password dialog box in progress.")
      CASE_ERRCODE(ERROR_INTERNET_RETRY_DIALOG                             , "ERROR_INTERNET_RETRY_DIALOG"                             , "Internet API error. The dialog box should be retried.")
      CASE_ERRCODE(ERROR_INTERNET_HTTPS_HTTP_SUBMIT_REDIR                  , "ERROR_INTERNET_HTTPS_HTTP_SUBMIT_REDIR"                  , "Internet API error. The data being submitted to an SSL connection is being redirected to a non-SSL connection.")
      CASE_ERRCODE(ERROR_INTERNET_INSERT_CDROM                             , "ERROR_INTERNET_INSERT_CDROM"                             , "Internet API error. The request requires a CD-ROM to be inserted in the CD-ROM drive to locate the resource requested.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
      CASE_ERRCODE(ERROR_INTERNET_FORTEZZA_LOGIN_NEEDED                    , "ERROR_INTERNET_FORTEZZA_LOGIN_NEEDED"                    , "Internet API error. The requested resource requires Fortezza authentication.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_ERRORS                          , "ERROR_INTERNET_SEC_CERT_ERRORS"                          , "Internet API error. The SSL certificate contains errors.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_NO_REV                          , "ERROR_INTERNET_SEC_CERT_NO_REV"                          , "Internet API error")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_REV_FAILED                      , "ERROR_INTERNET_SEC_CERT_REV_FAILED"                      , "Internet API error")
      CASE_ERRCODE(ERROR_FTP_TRANSFER_IN_PROGRESS                          , "ERROR_FTP_TRANSFER_IN_PROGRESS"                          , "FTP API errors. The requested operation cannot be made on the FTP session handle because an operation is already in progress.")
      CASE_ERRCODE(ERROR_FTP_DROPPED                                       , "ERROR_FTP_DROPPED"                                       , "FTP API errors. The FTP operation was not completed because the session was aborted.")
      CASE_ERRCODE(ERROR_FTP_NO_PASSIVE_MODE                               , "ERROR_FTP_NO_PASSIVE_MODE"                               , "FTP API errors. Passive mode is not available on the server.")
      CASE_ERRCODE(ERROR_GOPHER_PROTOCOL_ERROR                             , "ERROR_GOPHER_PROTOCOL_ERROR"                             , "Gopher API errors. An error was detected while parsing data returned from the Gopher server.")
      CASE_ERRCODE(ERROR_GOPHER_NOT_FILE                                   , "ERROR_GOPHER_NOT_FILE"                                   , "Gopher API errors. The request must be made for a file locator.")
      CASE_ERRCODE(ERROR_GOPHER_DATA_ERROR                                 , "ERROR_GOPHER_DATA_ERROR"                                 , "Gopher API errors. An error was detected while receiving data from the Gopher server.")
      CASE_ERRCODE(ERROR_GOPHER_END_OF_DATA                                , "ERROR_GOPHER_END_OF_DATA"                                , "Gopher API errors. The end of the data has been reached.")
      CASE_ERRCODE(ERROR_GOPHER_INVALID_LOCATOR                            , "ERROR_GOPHER_INVALID_LOCATOR"                            , "Gopher API errors. The supplied locator is not valid. ")
      CASE_ERRCODE(ERROR_GOPHER_INCORRECT_LOCATOR_TYPE                     , "ERROR_GOPHER_INCORRECT_LOCATOR_TYPE"                     , "Gopher API errors. The type of the locator is not correct for this operation.")
      CASE_ERRCODE(ERROR_GOPHER_NOT_GOPHER_PLUS                            , "ERROR_GOPHER_NOT_GOPHER_PLUS"                            , "Gopher API errors. The requested operation can be made only against a Gopher+ server, or with a locator that specifies a Gopher+ operation.")
      CASE_ERRCODE(ERROR_GOPHER_ATTRIBUTE_NOT_FOUND                        , "ERROR_GOPHER_ATTRIBUTE_NOT_FOUND"                        , "Gopher API errors. The requested attribute could not be located.")
      CASE_ERRCODE(ERROR_GOPHER_UNKNOWN_LOCATOR                            , "ERROR_GOPHER_UNKNOWN_LOCATOR"                            , "Gopher API errors. The locator type is unknown.")
      CASE_ERRCODE(ERROR_HTTP_HEADER_NOT_FOUND                             , "ERROR_HTTP_HEADER_NOT_FOUND"                             , "HTTP API errors. The requested header could not be located.")
      CASE_ERRCODE(ERROR_HTTP_DOWNLEVEL_SERVER                             , "ERROR_HTTP_DOWNLEVEL_SERVER"                             , "HTTP API errors. The server did not return any headers.")
      CASE_ERRCODE(ERROR_HTTP_INVALID_SERVER_RESPONSE                      , "ERROR_HTTP_INVALID_SERVER_RESPONSE"                      , "HTTP API errors. The server response could not be parsed.")
      CASE_ERRCODE(ERROR_HTTP_INVALID_HEADER                               , "ERROR_HTTP_INVALID_HEADER"                               , "HTTP API errors. The supplied header is invalid.")
      CASE_ERRCODE(ERROR_HTTP_INVALID_QUERY_REQUEST                        , "ERROR_HTTP_INVALID_QUERY_REQUEST"                        , "HTTP API errors. The request made to HttpQueryInfo is invalid.")
      CASE_ERRCODE(ERROR_HTTP_HEADER_ALREADY_EXISTS                        , "ERROR_HTTP_HEADER_ALREADY_EXISTS"                        , "HTTP API errors. The header could not be added because it already exists.")
      CASE_ERRCODE(ERROR_HTTP_REDIRECT_FAILED                              , "ERROR_HTTP_REDIRECT_FAILED"                              , "HTTP API errors. The redirection failed because either the scheme changed (for example, HTTP to FTP) or all attempts made to redirect failed (default is five attempts).")
      CASE_ERRCODE(ERROR_HTTP_NOT_REDIRECTED                               , "ERROR_HTTP_NOT_REDIRECTED"                               , "HTTP API errors. The HTTP request was not redirected.")
      CASE_ERRCODE(ERROR_HTTP_COOKIE_NEEDS_CONFIRMATION                    , "ERROR_HTTP_COOKIE_NEEDS_CONFIRMATION"                    , "HTTP API errors. The HTTP cookie requires confirmation.")
      CASE_ERRCODE(ERROR_HTTP_COOKIE_DECLINED                              , "ERROR_HTTP_COOKIE_DECLINED"                              , "HTTP API errors. The HTTP cookie was declined by the server.")
      CASE_ERRCODE(ERROR_HTTP_REDIRECT_NEEDS_CONFIRMATION                  , "ERROR_HTTP_REDIRECT_NEEDS_CONFIRMATION"                  , "HTTP API errors. The redirection requires user confirmation.")
      CASE_ERRCODE(ERROR_INTERNET_SECURITY_CHANNEL_ERROR                   , "ERROR_INTERNET_SECURITY_CHANNEL_ERROR"                   , "Additional Internet API error codes. The application experienced an internal error loading the SSL libraries.")
      CASE_ERRCODE(ERROR_INTERNET_UNABLE_TO_CACHE_FILE                     , "ERROR_INTERNET_UNABLE_TO_CACHE_FILE"                     , "Additional Internet API error codes. The function was unable to cache the file.")
      CASE_ERRCODE(ERROR_INTERNET_TCPIP_NOT_INSTALLED                      , "ERROR_INTERNET_TCPIP_NOT_INSTALLED"                      , "Additional Internet API error codes. The required protocol stack is not loaded and the application cannot start WinSock.")
      CASE_ERRCODE(ERROR_INTERNET_DISCONNECTED                             , "ERROR_INTERNET_DISCONNECTED"                             , "Additional Internet API error codes. The Internet connection has been lost.")
      CASE_ERRCODE(ERROR_INTERNET_SERVER_UNREACHABLE                       , "ERROR_INTERNET_SERVER_UNREACHABLE"                       , "Additional Internet API error codes. The Web site or server indicated is unreachable.")
      CASE_ERRCODE(ERROR_INTERNET_PROXY_SERVER_UNREACHABLE                 , "ERROR_INTERNET_PROXY_SERVER_UNREACHABLE"                 , "Additional Internet API error codes. The designated proxy server cannot be reached.")
      CASE_ERRCODE(ERROR_INTERNET_BAD_AUTO_PROXY_SCRIPT                    , "ERROR_INTERNET_BAD_AUTO_PROXY_SCRIPT"                    , "Additional Internet API error codes. There was an error in the automatic proxy configuration script.")
      CASE_ERRCODE(ERROR_INTERNET_UNABLE_TO_DOWNLOAD_SCRIPT                , "ERROR_INTERNET_UNABLE_TO_DOWNLOAD_SCRIPT"                , "Additional Internet API error codes. The automatic proxy configuration script could not be downloaded. The INTERNET_FLAG_MUST_CACHE_REQUEST flag was set.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_INVALID_CERT                         , "ERROR_INTERNET_SEC_INVALID_CERT"                         , "Additional Internet API error codes. SSL certificate is invalid.")
      CASE_ERRCODE(ERROR_INTERNET_SEC_CERT_REVOKED                         , "ERROR_INTERNET_SEC_CERT_REVOKED"                         , "Additional Internet API error codes. SSL certificate was revoked.")
      CASE_ERRCODE(ERROR_INTERNET_FAILED_DUETOSECURITYCHECK                , "ERROR_INTERNET_FAILED_DUETOSECURITYCHECK"                , "InternetAutodial specific errors. The function failed due to a security check.")
      CASE_ERRCODE(ERROR_INTERNET_NOT_INITIALIZED                          , "ERROR_INTERNET_NOT_INITIALIZED"                          , "InternetAutodial specific errors. Initialization of the WinINet API has not occurred. Indicates that a higher-level function, such as InternetOpen, has not been called yet.")
      CASE_ERRCODE(ERROR_INTERNET_NEED_MSN_SSPI_PKG                        , "ERROR_INTERNET_NEED_MSN_SSPI_PKG"                        , "InternetAutodial specific errors. Not currently implemented (2003)")
      CASE_ERRCODE(ERROR_INTERNET_LOGIN_FAILURE_DISPLAY_ENTITY_BODY        , "ERROR_INTERNET_LOGIN_FAILURE_DISPLAY_ENTITY_BODY"        , "InternetAutodial specific errors. The MS-Logoff digest header has been returned from the Web site. This header specifically instructs the digest package to purge credentials for the associated realm. This error will only be returned if INTERNET_ERROR_MASK_LOGIN_FAILURE_DISPLAY_ENTITY_BODY has been set.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
      }
   }

#ifdef MICROSOFT_SDK_FEBRUARY_2003
   if ((extInfo == info_WinHTTP)
      // && (dwErrCode >= WINHTTP_ERROR_BASE)
      // && (dwErrCode <= WINHTTP_ERROR_LAST)
      )
   {
      BEGIN_CASE_ERRCODE
      CASE_ERRCODE(WINHTTP_ERROR_BASE                                      , "WINHTTP_ERROR_BASE"                                      , "WinHttp API error. ")
      CASE_ERRCODE(ERROR_WINHTTP_OUT_OF_HANDLES                            , "ERROR_WINHTTP_OUT_OF_HANDLES"                            , "WinHttp API error. Obsolete; no longer used.")
      CASE_ERRCODE(ERROR_WINHTTP_TIMEOUT                                   , "ERROR_WINHTTP_TIMEOUT"                                   , "WinHttp API error. The request has timed out.")
      CASE_ERRCODE(ERROR_WINHTTP_INTERNAL_ERROR                            , "ERROR_WINHTTP_INTERNAL_ERROR"                            , "WinHttp API error. An internal error has occurred.")
      CASE_ERRCODE(ERROR_WINHTTP_INVALID_URL                               , "ERROR_WINHTTP_INVALID_URL"                               , "WinHttp API error. The URL is invalid.")
      CASE_ERRCODE(ERROR_WINHTTP_UNRECOGNIZED_SCHEME                       , "ERROR_WINHTTP_UNRECOGNIZED_SCHEME"                       , "WinHttp API error. The URL specified a scheme other than 'http:' or 'https:'.")
      CASE_ERRCODE(ERROR_WINHTTP_NAME_NOT_RESOLVED                         , "ERROR_WINHTTP_NAME_NOT_RESOLVED"                         , "WinHttp API error. The server name could not be resolved.")
      CASE_ERRCODE(ERROR_WINHTTP_INVALID_OPTION                            , "ERROR_WINHTTP_INVALID_OPTION"                            , "WinHttp API error. A request to WinHttpQueryOption or WinHttpSetOption specified an invalid option value.")
      CASE_ERRCODE(ERROR_WINHTTP_OPTION_NOT_SETTABLE                       , "ERROR_WINHTTP_OPTION_NOT_SETTABLE"                       , "WinHttp API error. The requested option cannot be set, only queried.")
      CASE_ERRCODE(ERROR_WINHTTP_SHUTDOWN                                  , "ERROR_WINHTTP_SHUTDOWN"                                  , "WinHttp API error. The WinHTTP function support is being shut down or unloaded.")
      CASE_ERRCODE(ERROR_WINHTTP_LOGIN_FAILURE                             , "ERROR_WINHTTP_LOGIN_FAILURE"                             , "WinHttp API error. The login attempt failed. When this error is encountered, the request handle should be closed with WinHttpCloseHandle. A new request handle must be created before retrying the function that originally produced this error.")
      CASE_ERRCODE(ERROR_WINHTTP_OPERATION_CANCELLED                       , "ERROR_WINHTTP_OPERATION_CANCELLED"                       , "WinHttp API error. The operation was canceled, usually because the handle on which the request was operating was closed before the operation completed.")
      CASE_ERRCODE(ERROR_WINHTTP_INCORRECT_HANDLE_TYPE                     , "ERROR_WINHTTP_INCORRECT_HANDLE_TYPE"                     , "WinHttp API error. The type of handle supplied is incorrect for this operation.")
      CASE_ERRCODE(ERROR_WINHTTP_INCORRECT_HANDLE_STATE                    , "ERROR_WINHTTP_INCORRECT_HANDLE_STATE"                    , "WinHttp API error. The requested operation cannot be carried out because the handle supplied is not in the correct state.")
      CASE_ERRCODE(ERROR_WINHTTP_CANNOT_CONNECT                            , "ERROR_WINHTTP_CANNOT_CONNECT"                            , "WinHttp API error. Returned if connection to the server failed.")
      CASE_ERRCODE(ERROR_WINHTTP_CONNECTION_ERROR                          , "ERROR_WINHTTP_CONNECTION_ERROR"                          , "WinHttp API error. The connection with the server has been reset or terminated, or an incompatible SSL protocol was encountered. For example, WinHTTP version 5.1 does not support SSL2 unless the client specifically enables it.")
      CASE_ERRCODE(ERROR_WINHTTP_RESEND_REQUEST                            , "ERROR_WINHTTP_RESEND_REQUEST"                            , "WinHttp API error. The WinHTTP function failed. The desired function can be retried on the same request handle.")
      CASE_ERRCODE(ERROR_WINHTTP_CLIENT_AUTH_CERT_NEEDED                   , "ERROR_WINHTTP_CLIENT_AUTH_CERT_NEEDED"                   , "WinHttp API error. Returned by WinHttpReceiveResponse when the server requests client authentication.")
      CASE_ERRCODE(ERROR_WINHTTP_CANNOT_CALL_BEFORE_OPEN                   , "ERROR_WINHTTP_CANNOT_CALL_BEFORE_OPEN"                   , "WinHttpRequest Component errors. Returned by the HttpRequest object if a requested operation cannot be performed before calling the Open method.")
      CASE_ERRCODE(ERROR_WINHTTP_CANNOT_CALL_BEFORE_SEND                   , "ERROR_WINHTTP_CANNOT_CALL_BEFORE_SEND"                   , "WinHttpRequest Component errors. Returned by the HttpRequest object if a requested operation cannot be performed before calling the Send method.")
      CASE_ERRCODE(ERROR_WINHTTP_CANNOT_CALL_AFTER_SEND                    , "ERROR_WINHTTP_CANNOT_CALL_AFTER_SEND"                    , "WinHttpRequest Component errors. Returned by the HttpRequest object if a requested operation cannot be performed after calling the Send method.")
      CASE_ERRCODE(ERROR_WINHTTP_CANNOT_CALL_AFTER_OPEN                    , "ERROR_WINHTTP_CANNOT_CALL_AFTER_OPEN"                    , "WinHttpRequest Component errors. Returned by the HttpRequest object if a specified option cannot be requested after the Open method has been called.")
      CASE_ERRCODE(ERROR_WINHTTP_HEADER_NOT_FOUND                          , "ERROR_WINHTTP_HEADER_NOT_FOUND"                          , "HTTP API errors. The requested header could not be located.")
      CASE_ERRCODE(ERROR_WINHTTP_INVALID_SERVER_RESPONSE                   , "ERROR_WINHTTP_INVALID_SERVER_RESPONSE"                   , "HTTP API errors. The server response could not be parsed.")
      CASE_ERRCODE(ERROR_WINHTTP_INVALID_QUERY_REQUEST                     , "ERROR_WINHTTP_INVALID_QUERY_REQUEST"                     , "HTTP API errors. Obsolete; no longer used.")
      CASE_ERRCODE(ERROR_WINHTTP_HEADER_ALREADY_EXISTS                     , "ERROR_WINHTTP_HEADER_ALREADY_EXISTS"                     , "HTTP API errors. Obsolete; no longer used.")
      CASE_ERRCODE(ERROR_WINHTTP_REDIRECT_FAILED                           , "ERROR_WINHTTP_REDIRECT_FAILED"                           , "HTTP API errors. The redirection failed because either the scheme changed or all attempts made to redirect failed (default is five attempts).")
      CASE_ERRCODE(ERROR_WINHTTP_AUTO_PROXY_SERVICE_ERROR                  , "ERROR_WINHTTP_AUTO_PROXY_SERVICE_ERROR"                  , "Additional WinHttp API error codes. Returned by WinHttpGetProxyForUrl when a proxy for the specified URL cannot be located.")
      CASE_ERRCODE(ERROR_WINHTTP_BAD_AUTO_PROXY_SCRIPT                     , "ERROR_WINHTTP_BAD_AUTO_PROXY_SCRIPT"                     , "Additional WinHttp API error codes. An error occurred executing the script code in the Proxy Auto-Configuration (PAC) file.")
      CASE_ERRCODE(ERROR_WINHTTP_UNABLE_TO_DOWNLOAD_SCRIPT                 , "ERROR_WINHTTP_UNABLE_TO_DOWNLOAD_SCRIPT"                 , "Additional WinHttp API error codes. The PAC file could not be downloaded. For example, the server referenced by the PAC URL may not have been reachable, or the server returned a 404 NOT FOUND response.")
      CASE_ERRCODE(ERROR_WINHTTP_NOT_INITIALIZED                           , "ERROR_WINHTTP_NOT_INITIALIZED"                           , "Additional WinHttp API error codes. Obsolete; no longer used.")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_FAILURE                            , "ERROR_WINHTTP_SECURE_FAILURE"                            , "Additional WinHttp API error codes. One or more errors were found in the Secure Sockets Layer (SSL) certificate sent by the server. To determine what type of error was encountered, check for a WINHTTP_CALLBACK_STATUS_SECURE_FAILURE notification in a status callback function. For more information, see WINHTTP_STATUS_CALLBACK.")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CERT_DATE_INVALID                  , "ERROR_WINHTTP_SECURE_CERT_DATE_INVALID"                  , "Certificate security errors. Indicates that a required certificate is not within its validity period when verifying against the current system clock or the timestamp in the signed file, or that the validity periods of the certification chain do not nest correctly (equivalent to a CERT_E_EXPIRED or a CERT_E_VALIDITYPERIODNESTING error).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CERT_CN_INVALID                    , "ERROR_WINHTTP_SECURE_CERT_CN_INVALID"                    , "Certificate security errors. Returned when a certificate's CN name does not match the passed value (equivalent to a CERT_E_CN_NO_MATCH error).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_INVALID_CA                         , "ERROR_WINHTTP_SECURE_INVALID_CA"                         , "Certificate security errors. Indicates that a certificate chain was processed but terminated in a root certificate that is not trusted by the trust provider (equivalent to CERT_E_UNTRUSTEDROOT).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CERT_REV_FAILED                    , "ERROR_WINHTTP_SECURE_CERT_REV_FAILED"                    , "Certificate security errors. Indicates that revocation could not be checked because the revocation server was offline (equivalent to CRYPT_E_REVOCATION_OFFLINE).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CHANNEL_ERROR                      , "ERROR_WINHTTP_SECURE_CHANNEL_ERROR"                      , "Certificate security errors. Indicates that an error occurred having to do with a secure channel (equivalent to error codes that begin with 'SEC_E_' and 'SEC_I_' listed in the winerror.h header file ).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_INVALID_CERT                       , "ERROR_WINHTTP_SECURE_INVALID_CERT"                       , "Certificate security errors. Indicates that a certificate is invalid (equivalent to errors such as CERT_E_ROLE, CERT_E_PATHLENCONST, CERT_E_CRITICAL, CERT_E_PURPOSE, CERT_E_ISSUERCHAINING, CERT_E_MALFORMED and CERT_E_CHAINING).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CERT_REVOKED                       , "ERROR_WINHTTP_SECURE_CERT_REVOKED"                       , "Certificate security errors. Indicates that a certificate has been revoked (equivalent to CRYPT_E_REVOKED).")
      CASE_ERRCODE(ERROR_WINHTTP_SECURE_CERT_WRONG_USAGE                   , "ERROR_WINHTTP_SECURE_CERT_WRONG_USAGE"                   , "Certificate security errors. Indicates that a certificate is not valid for the requested usage (equivalent to CERT_E_WRONG_USAGE).")
      CASE_ERRCODE(ERROR_WINHTTP_AUTODETECTION_FAILED                      , "ERROR_WINHTTP_AUTODETECTION_FAILED"                      , "Additional WinHttp API error codes. Returned by WinHttpDetectAutoProxyConfigUrl if WinHTTP was unable to discover the URL of the Proxy Auto-Configuration (PAC) file.")
      CASE_ERRCODE(ERROR_WINHTTP_HEADER_COUNT_EXCEEDED                     , "ERROR_WINHTTP_HEADER_COUNT_EXCEEDED"                     , "Additional WinHttp API error codes. Returned by WinHttpReceiveResponse when a larger number of headers were present in a response than WinHTTP could receive.")
      CASE_ERRCODE(ERROR_WINHTTP_HEADER_SIZE_OVERFLOW                      , "ERROR_WINHTTP_HEADER_SIZE_OVERFLOW"                      , "Additional WinHttp API error codes. Returned by WinHttpReceiveResponse when the size of headers received exceeds the limit for the request handle.")
      CASE_ERRCODE(ERROR_WINHTTP_CHUNKED_ENCODING_HEADER_SIZE_OVERFLOW     , "ERROR_WINHTTP_CHUNKED_ENCODING_HEADER_SIZE_OVERFLOW"     , "Additional WinHttp API error codes. Returned by WinHttpReceiveResponse when an overflow condition is encountered in the course of parsing chunked encoding.")
      CASE_ERRCODE(ERROR_WINHTTP_RESPONSE_DRAIN_OVERFLOW                   , "ERROR_WINHTTP_RESPONSE_DRAIN_OVERFLOW"                   , "Additional WinHttp API error codes. Returned when an incoming response exceeds an internal WinHTTP size limit.")
      CASE_ERRCODE(ERROR_WINHTTP_PROXY_AUTH_REQUIRED                       , "ERROR_WINHTTP_PROXY_AUTH_REQUIRED"                       , "Returned by WinHttpSendRequest on Windows Server 2003 family if an HTTP status code of 407 is returned, indicating that proxy authentication is required. This error was added to handle the case where the client is trying to establish an SSL connection to a target server via a proxy server, but the proxy server requires authentication. This error is not returned on Windows XP SP1 and Windows 2000 SP3, which leaves the client unaware that the SSL connection failed. In this case, a subsequent call to WinHttpWriteData to send some POST data, or to WinHttpReceiveResponse to get the response, fails with an INCORRECT_HANDLE_STATE error.")
      }
   }
#endif // MICROSOFT_SDK_FEBRUARY_2003
   if (strRes.IsEmpty()) {
      if (bDescription) {
         strRes = TEXT("Unknown Error.");
         if ((dwErrCode >=  1300L) && (dwErrCode <=  1399L)) strRes += TEXT(" Maybe  Security Status Codes."                );
         if ((dwErrCode >=  1400L) && (dwErrCode <=  1499L)) strRes += TEXT(" Maybe  WinUser Error Codes."                  );
         if ((dwErrCode >=  1500L) && (dwErrCode <=  1599L)) strRes += TEXT(" Maybe  Eventlog Status Codes."                );
         if ((dwErrCode >=  1600L) && (dwErrCode <=  1699L)) strRes += TEXT(" Maybe  MSI Error Codes."                      );
         if ((dwErrCode >=  1700L) && (dwErrCode <=  1999L)) strRes += TEXT(" Maybe  RPC Status Codes."                     );
         if ((dwErrCode >=  2000L) && (dwErrCode <=  2009L)) strRes += TEXT(" Maybe  OpenGL Error Code."                    );
         if ((dwErrCode >=  2010L) && (dwErrCode <=  2099L)) strRes += TEXT(" Maybe  Image Color Management Error Code."    );
         if ((dwErrCode >=  2100L) && (dwErrCode <=  2999L)) strRes += TEXT(" Winnet32 Status Codes."                       );
         if ((dwErrCode >=  3000L) && (dwErrCode <=  3014L)) strRes += TEXT(" Maybe  Win32 Spooler Error Codes."            );
         if ((dwErrCode >=  4000L) && (dwErrCode <=  4099L)) strRes += TEXT(" Maybe  Wins Error Codes."                     );
         if ((dwErrCode >=  4100L) && (dwErrCode <=  4199L)) strRes += TEXT(" Maybe  DHCP Error Codes."                     );
         if ((dwErrCode >=  4200L) && (dwErrCode <=  4299L)) strRes += TEXT(" Maybe  WMI Error Codes."                      );
         if ((dwErrCode >=  4300L) && (dwErrCode <=  4349L)) strRes += TEXT(" Maybe  NT Media Services (RSM) Error Codes."  );
         if ((dwErrCode >=  4350L) && (dwErrCode <=  4389L)) strRes += TEXT(" Maybe  NT Remote Storage Service Error Codes.");
         if ((dwErrCode >=  4390L) && (dwErrCode <=  4399L)) strRes += TEXT(" Maybe  NT Reparse Points Error Codes."        );
         if ((dwErrCode >=  4500L) && (dwErrCode <=  4600L)) strRes += TEXT(" Maybe  NT Single Instance Store Error Codes." );
         if ((dwErrCode >=  5000L) && (dwErrCode <=  5999L)) strRes += TEXT(" Maybe  Cluster Error Codes."                  );
         if ((dwErrCode >=  6000L) && (dwErrCode <=  6199L)) strRes += TEXT(" Maybe  EFS Error Codes."                      );
         if ((dwErrCode >=  6200L) && (dwErrCode <=  6300L)) strRes += TEXT(" Maybe  Task Scheduler Error Codes."           );
         if ((dwErrCode >=  7000L) && (dwErrCode <=  7499L)) strRes += TEXT(" Maybe  Terminal Server Error Codes."          );
         if ((dwErrCode >=  7500L) && (dwErrCode <=  7999L)) strRes += TEXT(" Traffic Control Error Codes."                 );
         if ((dwErrCode >=  8000L) && (dwErrCode <=  8999L)) strRes += TEXT(" Active Directory Error Codes."                );
         if ((dwErrCode >=  9000L) && (dwErrCode <=  9999L)) strRes += TEXT(" DNS Error Codes."                             );
         if ((dwErrCode >= 10000L) && (dwErrCode <= 11999L)) strRes += TEXT(" WinSock Error Codes."                         );
         if ((dwErrCode >= 13000L) && (dwErrCode <= 13999L)) strRes += TEXT(" Start of IPSec Error codes."                  );
         if ((dwErrCode >= 14000L) && (dwErrCode <= 14999L)) strRes += TEXT(" Side By Side Error Codes."                    );
      } else {
         strRes.Format(TEXT("%i"), dwErrCode);
      }
   }
   return strRes;
}

#undef BEGIN_CASE_ERRCODE
#undef CASE_ERRCODE
#undef END_CASE_ERRCODE


#define BEGIN_CASE_HRESULT                    switch(hRes) {
#define CASE_HRESULT(hRes, Name, MessageText) case hRes: strRes = (bDescription ? TEXT(MessageText) : TEXT(Name)); break;
#define END_CASE_HRESULT                      default: strRes += TEXT("Unknown HRESULT "); }

CString CLogger::HResult(HRESULT hRes, bool bDescription) {
   CString strRes;

   BEGIN_CASE_HRESULT
   CASE_HRESULT(E_UNEXPECTED                                            , "E_UNEXPECTED"                                               , "Catastrophic failure")
   CASE_HRESULT(E_NOTIMPL                                               , "E_NOTIMPL"                                                  , "Not implemented")
   CASE_HRESULT(E_OUTOFMEMORY                                           , "E_OUTOFMEMORY"                                              , "Ran out of memory")
   CASE_HRESULT(E_INVALIDARG                                            , "E_INVALIDARG"                                               , "One or more arguments are invalid")
   CASE_HRESULT(E_NOINTERFACE                                           , "E_NOINTERFACE"                                              , "No such interface supported")
   CASE_HRESULT(E_POINTER                                               , "E_POINTER"                                                  , "Invalid pointer")
   CASE_HRESULT(E_HANDLE                                                , "E_HANDLE"                                                   , "Invalid handle")
   CASE_HRESULT(E_ABORT                                                 , "E_ABORT"                                                    , "Operation aborted")
   CASE_HRESULT(E_FAIL                                                  , "E_FAIL"                                                     , "Unspecified error")
   CASE_HRESULT(E_ACCESSDENIED                                          , "E_ACCESSDENIED"                                             , "General access denied error")
   CASE_HRESULT(E_PENDING                                               , "E_PENDING"                                                  , "The read operation is pending.");//The data necessary to complete this operation is not yet available.")
   CASE_HRESULT(CO_E_INIT_TLS                                           , "CO_E_INIT_TLS"                                              , "Thread local storage failure")
   CASE_HRESULT(CO_E_INIT_SHARED_ALLOCATOR                              , "CO_E_INIT_SHARED_ALLOCATOR"                                 , "Get shared memory allocator failure")
   CASE_HRESULT(CO_E_INIT_MEMORY_ALLOCATOR                              , "CO_E_INIT_MEMORY_ALLOCATOR"                                 , "Get memory allocator failure")
   CASE_HRESULT(CO_E_INIT_CLASS_CACHE                                   , "CO_E_INIT_CLASS_CACHE"                                      , "Unable to initialize class cache")
   CASE_HRESULT(CO_E_INIT_RPC_CHANNEL                                   , "CO_E_INIT_RPC_CHANNEL"                                      , "Unable to initialize RPC services")
   CASE_HRESULT(CO_E_INIT_TLS_SET_CHANNEL_CONTROL                       , "CO_E_INIT_TLS_SET_CHANNEL_CONTROL"                          , "Cannot set thread local storage channel control")
   CASE_HRESULT(CO_E_INIT_TLS_CHANNEL_CONTROL                           , "CO_E_INIT_TLS_CHANNEL_CONTROL"                              , "Could not allocate thread local storage channel control")
   CASE_HRESULT(CO_E_INIT_UNACCEPTED_USER_ALLOCATOR                     , "CO_E_INIT_UNACCEPTED_USER_ALLOCATOR"                        , "The user supplied memory allocator is unacceptable")
   CASE_HRESULT(CO_E_INIT_SCM_MUTEX_EXISTS                              , "CO_E_INIT_SCM_MUTEX_EXISTS"                                 , "The OLE service mutex already exists")
   CASE_HRESULT(CO_E_INIT_SCM_FILE_MAPPING_EXISTS                       , "CO_E_INIT_SCM_FILE_MAPPING_EXISTS"                          , "The OLE service file mapping already exists")
   CASE_HRESULT(CO_E_INIT_SCM_MAP_VIEW_OF_FILE                          , "CO_E_INIT_SCM_MAP_VIEW_OF_FILE"                             , "Unable to map view of file for OLE service")
   CASE_HRESULT(CO_E_INIT_SCM_EXEC_FAILURE                              , "CO_E_INIT_SCM_EXEC_FAILURE"                                 , "Failure attempting to launch OLE service")
   CASE_HRESULT(CO_E_INIT_ONLY_SINGLE_THREADED                          , "CO_E_INIT_ONLY_SINGLE_THREADED"                             , "There was an attempt to call CoInitialize a second time while single threaded")
   CASE_HRESULT(CO_E_CANT_REMOTE                                        , "CO_E_CANT_REMOTE"                                           , "A Remote activation was necessary but was not allowed")
   CASE_HRESULT(CO_E_BAD_SERVER_NAME                                    , "CO_E_BAD_SERVER_NAME"                                       , "A Remote activation was necessary but the server name provided was invalid")
   CASE_HRESULT(CO_E_WRONG_SERVER_IDENTITY                              , "CO_E_WRONG_SERVER_IDENTITY"                                 , "The class is configured to run as a security id different from the caller")
   CASE_HRESULT(CO_E_OLE1DDE_DISABLED                                   , "CO_E_OLE1DDE_DISABLED"                                      , "Use of Ole1 services requiring DDE windows is disabled")
   CASE_HRESULT(CO_E_RUNAS_SYNTAX                                       , "CO_E_RUNAS_SYNTAX"                                          , "A RunAs specification must be <domain name>\\<user name> or simply <user name>")
   CASE_HRESULT(CO_E_CREATEPROCESS_FAILURE                              , "CO_E_CREATEPROCESS_FAILURE"                                 , "The server process could not be started.  The pathname may be incorrect.")
   CASE_HRESULT(CO_E_RUNAS_CREATEPROCESS_FAILURE                        , "CO_E_RUNAS_CREATEPROCESS_FAILURE"                           , "The server process could not be started as the configured identity.  The pathname may be incorrect or unavailable.")
   CASE_HRESULT(CO_E_RUNAS_LOGON_FAILURE                                , "CO_E_RUNAS_LOGON_FAILURE"                                   , "The server process could not be started because the configured identity is incorrect.  Check the username and password.")
   CASE_HRESULT(CO_E_LAUNCH_PERMSSION_DENIED                            , "CO_E_LAUNCH_PERMSSION_DENIED"                               , "The client is not allowed to launch this server.")
   CASE_HRESULT(CO_E_START_SERVICE_FAILURE                              , "CO_E_START_SERVICE_FAILURE"                                 , "The service providing this server could not be started.")
   CASE_HRESULT(CO_E_REMOTE_COMMUNICATION_FAILURE                       , "CO_E_REMOTE_COMMUNICATION_FAILURE"                          , "This computer was unable to communicate with the computer providing the server.")
   CASE_HRESULT(CO_E_SERVER_START_TIMEOUT                               , "CO_E_SERVER_START_TIMEOUT"                                  , "The server did not respond after being launched.")
   CASE_HRESULT(CO_E_CLSREG_INCONSISTENT                                , "CO_E_CLSREG_INCONSISTENT"                                   , "The registration information for this server is inconsistent or incomplete.")
   CASE_HRESULT(CO_E_IIDREG_INCONSISTENT                                , "CO_E_IIDREG_INCONSISTENT"                                   , "The registration information for this interface is inconsistent or incomplete.")
   CASE_HRESULT(CO_E_NOT_SUPPORTED                                      , "CO_E_NOT_SUPPORTED"                                         , "The operation attempted is not supported.")
   CASE_HRESULT(CO_E_RELOAD_DLL                                         , "CO_E_RELOAD_DLL"                                            , "A dll must be loaded.")
   CASE_HRESULT(CO_E_MSI_ERROR                                          , "CO_E_MSI_ERROR"                                             , "A Microsoft Software Installer error was encountered.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CO_E_ATTEMPT_TO_CREATE_OUTSIDE_CLIENT_CONTEXT           , "CO_E_ATTEMPT_TO_CREATE_OUTSIDE_CLIENT_CONTEXT"              , "The specified activation could not occur in the client context as specified.")
   CASE_HRESULT(CO_E_SERVER_PAUSED                                      , "CO_E_SERVER_PAUSED"                                         , "Activations on the server are paused.")
   CASE_HRESULT(CO_E_SERVER_NOT_PAUSED                                  , "CO_E_SERVER_NOT_PAUSED"                                     , "Activations on the server are not paused.")
   CASE_HRESULT(CO_E_CLASS_DISABLED                                     , "CO_E_CLASS_DISABLED"                                        , "The component or application containing the component has been disabled.")
   CASE_HRESULT(CO_E_CLRNOTAVAILABLE                                    , "CO_E_CLRNOTAVAILABLE"                                       , "The common language runtime is not available")
   CASE_HRESULT(CO_E_ASYNC_WORK_REJECTED                                , "CO_E_ASYNC_WORK_REJECTED"                                   , "The thread-pool rejected the submitted asynchronous work.")
   CASE_HRESULT(CO_E_SERVER_INIT_TIMEOUT                                , "CO_E_SERVER_INIT_TIMEOUT"                                   , "The server started, but did not finish initializing in a timely fashion.")
   CASE_HRESULT(CO_E_NO_SECCTX_IN_ACTIVATE                              , "CO_E_NO_SECCTX_IN_ACTIVATE"                                 , "Unable to complete the call since there is no COM+ security context inside IObjectControl.Activate.")
   CASE_HRESULT(CO_E_TRACKER_CONFIG                                     , "CO_E_TRACKER_CONFIG"                                        , "The provided tracker configuration is invalid")
   CASE_HRESULT(CO_E_THREADPOOL_CONFIG                                  , "CO_E_THREADPOOL_CONFIG"                                     , "The provided thread pool configuration is invalid")
   CASE_HRESULT(CO_E_SXS_CONFIG                                         , "CO_E_SXS_CONFIG"                                            , "The provided side-by-side configuration is invalid")
   CASE_HRESULT(CO_E_MALFORMED_SPN                                      , "CO_E_MALFORMED_SPN"                                         , "The server principal name (SPN) obtained during security negotiation is malformed.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(S_OK                                                    , "S_OK"                                                       , "Success codes")
   CASE_HRESULT(S_FALSE                                                 , "S_FALSE"                                                    , "Success codes")
   CASE_HRESULT(OLE_E_OLEVERB                                           , "OLE_E_OLEVERB"                                              , "Invalid OLEVERB structure")
   CASE_HRESULT(OLE_E_ADVF                                              , "OLE_E_ADVF"                                                 , "Invalid advise flags")
   CASE_HRESULT(OLE_E_ENUM_NOMORE                                       , "OLE_E_ENUM_NOMORE"                                          , "Can't enumerate any more, because the associated data is missing")
   CASE_HRESULT(OLE_E_ADVISENOTSUPPORTED                                , "OLE_E_ADVISENOTSUPPORTED"                                   , "This implementation doesn't take advises")
   CASE_HRESULT(OLE_E_NOCONNECTION                                      , "OLE_E_NOCONNECTION"                                         , "There is no connection for this connection ID")
   CASE_HRESULT(OLE_E_NOTRUNNING                                        , "OLE_E_NOTRUNNING"                                           , "Need to run the object to perform this operation")
   CASE_HRESULT(OLE_E_NOCACHE                                           , "OLE_E_NOCACHE"                                              , "There is no cache to operate on")
   CASE_HRESULT(OLE_E_BLANK                                             , "OLE_E_BLANK"                                                , "Uninitialized object")
   CASE_HRESULT(OLE_E_CLASSDIFF                                         , "OLE_E_CLASSDIFF"                                            , "Linked object's source class has changed")
   CASE_HRESULT(OLE_E_CANT_GETMONIKER                                   , "OLE_E_CANT_GETMONIKER"                                      , "Not able to get the moniker of the object")
   CASE_HRESULT(OLE_E_CANT_BINDTOSOURCE                                 , "OLE_E_CANT_BINDTOSOURCE"                                    , "Not able to bind to the source")
   CASE_HRESULT(OLE_E_STATIC                                            , "OLE_E_STATIC"                                               , "Object is static; operation not allowed")
   CASE_HRESULT(OLE_E_PROMPTSAVECANCELLED                               , "OLE_E_PROMPTSAVECANCELLED"                                  , "User canceled out of save dialog")
   CASE_HRESULT(OLE_E_INVALIDRECT                                       , "OLE_E_INVALIDRECT"                                          , "Invalid rectangle")
   CASE_HRESULT(OLE_E_WRONGCOMPOBJ                                      , "OLE_E_WRONGCOMPOBJ"                                         , "compobj.dll is too old for the ole2.dll initialized")
   CASE_HRESULT(OLE_E_INVALIDHWND                                       , "OLE_E_INVALIDHWND"                                          , "Invalid window handle")
   CASE_HRESULT(OLE_E_NOT_INPLACEACTIVE                                 , "OLE_E_NOT_INPLACEACTIVE"                                    , "Object is not in any of the inplace active states")
   CASE_HRESULT(OLE_E_CANTCONVERT                                       , "OLE_E_CANTCONVERT"                                          , "Not able to convert object")
   CASE_HRESULT(OLE_E_NOSTORAGE                                         , "OLE_E_NOSTORAGE"                                            , "Not able to perform the operation because object is not given storage yet")
   CASE_HRESULT(DV_E_FORMATETC                                          , "DV_E_FORMATETC"                                             , "Invalid FORMATETC structure")
   CASE_HRESULT(DV_E_DVTARGETDEVICE                                     , "DV_E_DVTARGETDEVICE"                                        , "Invalid DVTARGETDEVICE structure")
   CASE_HRESULT(DV_E_STGMEDIUM                                          , "DV_E_STGMEDIUM"                                             , "Invalid STDGMEDIUM structure")
   CASE_HRESULT(DV_E_STATDATA                                           , "DV_E_STATDATA"                                              , "Invalid STATDATA structure")
   CASE_HRESULT(DV_E_LINDEX                                             , "DV_E_LINDEX"                                                , "Invalid lindex")
   CASE_HRESULT(DV_E_TYMED                                              , "DV_E_TYMED"                                                 , "Invalid tymed")
   CASE_HRESULT(DV_E_CLIPFORMAT                                         , "DV_E_CLIPFORMAT"                                            , "Invalid clipboard format")
   CASE_HRESULT(DV_E_DVASPECT                                           , "DV_E_DVASPECT"                                              , "Invalid aspect(s)")
   CASE_HRESULT(DV_E_DVTARGETDEVICE_SIZE                                , "DV_E_DVTARGETDEVICE_SIZE"                                   , "tdSize parameter of the DVTARGETDEVICE structure is invalid")
   CASE_HRESULT(DV_E_NOIVIEWOBJECT                                      , "DV_E_NOIVIEWOBJECT"                                         , "Object doesn't support IViewObject interface")
   CASE_HRESULT(DRAGDROP_E_NOTREGISTERED                                , "DRAGDROP_E_NOTREGISTERED"                                   , "Trying to revoke a drop target that has not been registered")
   CASE_HRESULT(DRAGDROP_E_ALREADYREGISTERED                            , "DRAGDROP_E_ALREADYREGISTERED"                               , "This window has already been registered as a drop target")
   CASE_HRESULT(DRAGDROP_E_INVALIDHWND                                  , "DRAGDROP_E_INVALIDHWND"                                     , "Invalid window handle")
   CASE_HRESULT(CLASS_E_NOAGGREGATION                                   , "CLASS_E_NOAGGREGATION"                                      , "Class does not support aggregation (or class object is remote)")
   CASE_HRESULT(CLASS_E_CLASSNOTAVAILABLE                               , "CLASS_E_CLASSNOTAVAILABLE"                                  , "ClassFactory cannot supply requested class")
   CASE_HRESULT(CLASS_E_NOTLICENSED                                     , "CLASS_E_NOTLICENSED"                                        , "Class is not licensed for use")
   CASE_HRESULT(VIEW_E_DRAW                                             , "VIEW_E_DRAW"                                                , "Error drawing view")
   CASE_HRESULT(REGDB_E_READREGDB                                       , "REGDB_E_READREGDB"                                          , "Could not read key from registry")
   CASE_HRESULT(REGDB_E_WRITEREGDB                                      , "REGDB_E_WRITEREGDB"                                         , "Could not write key to registry")
   CASE_HRESULT(REGDB_E_KEYMISSING                                      , "REGDB_E_KEYMISSING"                                         , "Could not find the key in the registry")
   CASE_HRESULT(REGDB_E_INVALIDVALUE                                    , "REGDB_E_INVALIDVALUE"                                       , "Invalid value for registry")
   CASE_HRESULT(REGDB_E_CLASSNOTREG                                     , "REGDB_E_CLASSNOTREG"                                        , "Class not registered")
   CASE_HRESULT(REGDB_E_IIDNOTREG                                       , "REGDB_E_IIDNOTREG"                                          , "Interface not registered")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(REGDB_E_BADTHREADINGMODEL                               , "REGDB_E_BADTHREADINGMODEL"                                  , "Threading model entry is not valid")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CAT_E_CATIDNOEXIST                                      , "CAT_E_CATIDNOEXIST"                                         , "CATID does not exist")
   CASE_HRESULT(CAT_E_NODESCRIPTION                                     , "CAT_E_NODESCRIPTION"                                        , "Description not found")
   CASE_HRESULT(CS_E_PACKAGE_NOTFOUND                                   , "CS_E_PACKAGE_NOTFOUND"                                      , "No package in the software installation data in the Active Directory meets this criteria.")
   CASE_HRESULT(CS_E_NOT_DELETABLE                                      , "CS_E_NOT_DELETABLE"                                         , "Deleting this will break the referential integrity of the software installation data in the Active Directory.")
   CASE_HRESULT(CS_E_CLASS_NOTFOUND                                     , "CS_E_CLASS_NOTFOUND"                                        , "The CLSID was not found in the software installation data in the Active Directory.")
   CASE_HRESULT(CS_E_INVALID_VERSION                                    , "CS_E_INVALID_VERSION"                                       , "The software installation data in the Active Directory is corrupt.")
   CASE_HRESULT(CS_E_NO_CLASSSTORE                                      , "CS_E_NO_CLASSSTORE"                                         , "There is no software installation data in the Active Directory.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CS_E_OBJECT_NOTFOUND                                    , "CS_E_OBJECT_NOTFOUND"                                       , "There is no software installation data object in the Active Directory.")
   CASE_HRESULT(CS_E_OBJECT_ALREADY_EXISTS                              , "CS_E_OBJECT_ALREADY_EXISTS"                                 , "The software installation data object in the Active Directory already exists.")
   CASE_HRESULT(CS_E_INVALID_PATH                                       , "CS_E_INVALID_PATH"                                          , "The path to the software installation data in the Active Directory is not correct.")
   CASE_HRESULT(CS_E_NETWORK_ERROR                                      , "CS_E_NETWORK_ERROR"                                         , "A network error interrupted the operation.")
   CASE_HRESULT(CS_E_ADMIN_LIMIT_EXCEEDED                               , "CS_E_ADMIN_LIMIT_EXCEEDED"                                  , "The size of this object exceeds the maximum size set by the Administrator.")
   CASE_HRESULT(CS_E_SCHEMA_MISMATCH                                    , "CS_E_SCHEMA_MISMATCH"                                       , "The schema for the software installation data in the Active Directory does not match the required schema.")
   CASE_HRESULT(CS_E_INTERNAL_ERROR                                     , "CS_E_INTERNAL_ERROR"                                        , "An error occurred in the software installation data in the Active Directory.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CACHE_E_NOCACHE_UPDATED                                 , "CACHE_E_NOCACHE_UPDATED"                                    , "Cache not updated")
   CASE_HRESULT(OLEOBJ_E_NOVERBS                                        , "OLEOBJ_E_NOVERBS"                                           , "No verbs for OLE object")
   CASE_HRESULT(OLEOBJ_E_INVALIDVERB                                    , "OLEOBJ_E_INVALIDVERB"                                       , "Invalid verb for OLE object")
   CASE_HRESULT(INPLACE_E_NOTUNDOABLE                                   , "INPLACE_E_NOTUNDOABLE"                                      , "Undo is not available")
   CASE_HRESULT(INPLACE_E_NOTOOLSPACE                                   , "INPLACE_E_NOTOOLSPACE"                                      , "Space for tools is not available")
   CASE_HRESULT(CONVERT10_E_OLESTREAM_GET                               , "CONVERT10_E_OLESTREAM_GET"                                  , "OLESTREAM Get method failed")
   CASE_HRESULT(CONVERT10_E_OLESTREAM_PUT                               , "CONVERT10_E_OLESTREAM_PUT"                                  , "OLESTREAM Put method failed")
   CASE_HRESULT(CONVERT10_E_OLESTREAM_FMT                               , "CONVERT10_E_OLESTREAM_FMT"                                  , "Contents of the OLESTREAM not in correct format")
   CASE_HRESULT(CONVERT10_E_OLESTREAM_BITMAP_TO_DIB                     , "CONVERT10_E_OLESTREAM_BITMAP_TO_DIB"                        , "There was an error in a Windows GDI call while converting the bitmap to a DIB")
   CASE_HRESULT(CONVERT10_E_STG_FMT                                     , "CONVERT10_E_STG_FMT"                                        , "Contents of the IStorage not in correct format")
   CASE_HRESULT(CONVERT10_E_STG_NO_STD_STREAM                           , "CONVERT10_E_STG_NO_STD_STREAM"                              , "Contents of IStorage is missing one of the standard streams")
   CASE_HRESULT(CONVERT10_E_STG_DIB_TO_BITMAP                           , "CONVERT10_E_STG_DIB_TO_BITMAP"                              , "There was an error in a Windows GDI call while converting the DIB to a bitmap.")
   CASE_HRESULT(CLIPBRD_E_CANT_OPEN                                     , "CLIPBRD_E_CANT_OPEN"                                        , "OpenClipboard Failed")
   CASE_HRESULT(CLIPBRD_E_CANT_EMPTY                                    , "CLIPBRD_E_CANT_EMPTY"                                       , "EmptyClipboard Failed")
   CASE_HRESULT(CLIPBRD_E_CANT_SET                                      , "CLIPBRD_E_CANT_SET"                                         , "SetClipboard Failed")
   CASE_HRESULT(CLIPBRD_E_BAD_DATA                                      , "CLIPBRD_E_BAD_DATA"                                         , "Data on clipboard is invalid")
   CASE_HRESULT(CLIPBRD_E_CANT_CLOSE                                    , "CLIPBRD_E_CANT_CLOSE"                                       , "CloseClipboard Failed")
   CASE_HRESULT(MK_E_CONNECTMANUALLY                                    , "MK_E_CONNECTMANUALLY"                                       , "Moniker needs to be connected manually")
   CASE_HRESULT(MK_E_EXCEEDEDDEADLINE                                   , "MK_E_EXCEEDEDDEADLINE"                                      , "Operation exceeded deadline")
   CASE_HRESULT(MK_E_NEEDGENERIC                                        , "MK_E_NEEDGENERIC"                                           , "Moniker needs to be generic")
   CASE_HRESULT(MK_E_UNAVAILABLE                                        , "MK_E_UNAVAILABLE"                                           , "Operation unavailable")
   CASE_HRESULT(MK_E_SYNTAX                                             , "MK_E_SYNTAX"                                                , "Invalid syntax")
   CASE_HRESULT(MK_E_NOOBJECT                                           , "MK_E_NOOBJECT"                                              , "No object for moniker")
   CASE_HRESULT(MK_E_INVALIDEXTENSION                                   , "MK_E_INVALIDEXTENSION"                                      , "Bad extension for file")
   CASE_HRESULT(MK_E_INTERMEDIATEINTERFACENOTSUPPORTED                  , "MK_E_INTERMEDIATEINTERFACENOTSUPPORTED"                     , "Intermediate operation failed")
   CASE_HRESULT(MK_E_NOTBINDABLE                                        , "MK_E_NOTBINDABLE"                                           , "Moniker is not bindable")
   CASE_HRESULT(MK_E_NOTBOUND                                           , "MK_E_NOTBOUND"                                              , "Moniker is not bound")
   CASE_HRESULT(MK_E_CANTOPENFILE                                       , "MK_E_CANTOPENFILE"                                          , "Moniker cannot open file")
   CASE_HRESULT(MK_E_MUSTBOTHERUSER                                     , "MK_E_MUSTBOTHERUSER"                                        , "User input required for operation to succeed")
   CASE_HRESULT(MK_E_NOINVERSE                                          , "MK_E_NOINVERSE"                                             , "Moniker class has no inverse")
   CASE_HRESULT(MK_E_NOSTORAGE                                          , "MK_E_NOSTORAGE"                                             , "Moniker does not refer to storage")
   CASE_HRESULT(MK_E_NOPREFIX                                           , "MK_E_NOPREFIX"                                              , "No common prefix")
   CASE_HRESULT(MK_E_ENUMERATION_FAILED                                 , "MK_E_ENUMERATION_FAILED"                                    , "Moniker could not be enumerated")
   CASE_HRESULT(CO_E_NOTINITIALIZED                                     , "CO_E_NOTINITIALIZED"                                        , "CoInitialize has not been called.")
   CASE_HRESULT(CO_E_ALREADYINITIALIZED                                 , "CO_E_ALREADYINITIALIZED"                                    , "CoInitialize has already been called.")
   CASE_HRESULT(CO_E_CANTDETERMINECLASS                                 , "CO_E_CANTDETERMINECLASS"                                    , "Class of object cannot be determined")
   CASE_HRESULT(CO_E_CLASSSTRING                                        , "CO_E_CLASSSTRING"                                           , "Invalid class string")
   CASE_HRESULT(CO_E_IIDSTRING                                          , "CO_E_IIDSTRING"                                             , "Invalid interface string")
   CASE_HRESULT(CO_E_APPNOTFOUND                                        , "CO_E_APPNOTFOUND"                                           , "Application not found")
   CASE_HRESULT(CO_E_APPSINGLEUSE                                       , "CO_E_APPSINGLEUSE"                                          , "Application cannot be run more than once")
   CASE_HRESULT(CO_E_ERRORINAPP                                         , "CO_E_ERRORINAPP"                                            , "Some error in application program")
   CASE_HRESULT(CO_E_DLLNOTFOUND                                        , "CO_E_DLLNOTFOUND"                                           , "DLL for class not found")
   CASE_HRESULT(CO_E_ERRORINDLL                                         , "CO_E_ERRORINDLL"                                            , "Error in the DLL")
   CASE_HRESULT(CO_E_WRONGOSFORAPP                                      , "CO_E_WRONGOSFORAPP"                                         , "Wrong OS or OS version for application")
   CASE_HRESULT(CO_E_OBJNOTREG                                          , "CO_E_OBJNOTREG"                                             , "Object is not registered")
   CASE_HRESULT(CO_E_OBJISREG                                           , "CO_E_OBJISREG"                                              , "Object is already registered")
   CASE_HRESULT(CO_E_OBJNOTCONNECTED                                    , "CO_E_OBJNOTCONNECTED"                                       , "Object is not connected to server")
   CASE_HRESULT(CO_E_APPDIDNTREG                                        , "CO_E_APPDIDNTREG"                                           , "Application was launched but it didn't register a class factory")
   CASE_HRESULT(CO_E_RELEASED                                           , "CO_E_RELEASED"                                              , "Object has been released")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(EVENT_S_SOME_SUBSCRIBERS_FAILED                         , "EVENT_S_SOME_SUBSCRIBERS_FAILED"                            , "An event was able to invoke some but not all of the subscribers")
   CASE_HRESULT(EVENT_E_ALL_SUBSCRIBERS_FAILED                          , "EVENT_E_ALL_SUBSCRIBERS_FAILED"                             , "An event was unable to invoke any of the subscribers")
   CASE_HRESULT(EVENT_S_NOSUBSCRIBERS                                   , "EVENT_S_NOSUBSCRIBERS"                                      , "An event was delivered but there were no subscribers")
   CASE_HRESULT(EVENT_E_QUERYSYNTAX                                     , "EVENT_E_QUERYSYNTAX"                                        , "A syntax error occurred trying to evaluate a query string")
   CASE_HRESULT(EVENT_E_QUERYFIELD                                      , "EVENT_E_QUERYFIELD"                                         , "An invalid field name was used in a query string")
   CASE_HRESULT(EVENT_E_INTERNALEXCEPTION                               , "EVENT_E_INTERNALEXCEPTION"                                  , "An unexpected exception was raised")
   CASE_HRESULT(EVENT_E_INTERNALERROR                                   , "EVENT_E_INTERNALERROR"                                      , "An unexpected internal error was detected")
   CASE_HRESULT(EVENT_E_INVALID_PER_USER_SID                            , "EVENT_E_INVALID_PER_USER_SID"                               , "The owner SID on a per-user subscription doesn't exist")
   CASE_HRESULT(EVENT_E_USER_EXCEPTION                                  , "EVENT_E_USER_EXCEPTION"                                     , "A user-supplied component or subscriber raised an exception")
   CASE_HRESULT(EVENT_E_TOO_MANY_METHODS                                , "EVENT_E_TOO_MANY_METHODS"                                   , "An interface has too many methods to fire events from")
   CASE_HRESULT(EVENT_E_MISSING_EVENTCLASS                              , "EVENT_E_MISSING_EVENTCLASS"                                 , "A subscription cannot be stored unless its event class already exists")
   CASE_HRESULT(EVENT_E_NOT_ALL_REMOVED                                 , "EVENT_E_NOT_ALL_REMOVED"                                    , "Not all the objects requested could be removed")
   CASE_HRESULT(EVENT_E_COMPLUS_NOT_INSTALLED                           , "EVENT_E_COMPLUS_NOT_INSTALLED"                              , "COM+ is required for this operation, but is not installed")
   CASE_HRESULT(EVENT_E_CANT_MODIFY_OR_DELETE_UNCONFIGURED_OBJECT       , "EVENT_E_CANT_MODIFY_OR_DELETE_UNCONFIGURED_OBJECT"          , "Cannot modify or delete an object that was not added using the COM+ Admin SDK")
   CASE_HRESULT(EVENT_E_CANT_MODIFY_OR_DELETE_CONFIGURED_OBJECT         , "EVENT_E_CANT_MODIFY_OR_DELETE_CONFIGURED_OBJECT"            , "Cannot modify or delete an object that was added using the COM+ Admin SDK")
   CASE_HRESULT(EVENT_E_INVALID_EVENT_CLASS_PARTITION                   , "EVENT_E_INVALID_EVENT_CLASS_PARTITION"                      , "The event class for this subscription is in an invalid partition")
   CASE_HRESULT(EVENT_E_PER_USER_SID_NOT_LOGGED_ON                      , "EVENT_E_PER_USER_SID_NOT_LOGGED_ON"                         , "The owner of the PerUser subscription is not logged on to the system specified")
   CASE_HRESULT(XACT_E_ALREADYOTHERSINGLEPHASE                          , "XACT_E_ALREADYOTHERSINGLEPHASE"                             , "Another single phase resource manager has already been enlisted in this transaction.")
   CASE_HRESULT(XACT_E_CANTRETAIN                                       , "XACT_E_CANTRETAIN"                                          , "A retaining commit or abort is not supported")
   CASE_HRESULT(XACT_E_COMMITFAILED                                     , "XACT_E_COMMITFAILED"                                        , "The transaction failed to commit for an unknown reason. The transaction was aborted.")
   CASE_HRESULT(XACT_E_COMMITPREVENTED                                  , "XACT_E_COMMITPREVENTED"                                     , "Cannot call commit on this transaction object because the calling application did not initiate the transaction.")
   CASE_HRESULT(XACT_E_HEURISTICABORT                                   , "XACT_E_HEURISTICABORT"                                      , "Instead of committing, the resource heuristically aborted.")
   CASE_HRESULT(XACT_E_HEURISTICCOMMIT                                  , "XACT_E_HEURISTICCOMMIT"                                     , "Instead of aborting, the resource heuristically committed.")
   CASE_HRESULT(XACT_E_HEURISTICDAMAGE                                  , "XACT_E_HEURISTICDAMAGE"                                     , "Some of the states of the resource were committed while others were aborted, likely because of heuristic decisions.")
   CASE_HRESULT(XACT_E_HEURISTICDANGER                                  , "XACT_E_HEURISTICDANGER"                                     , "Some of the states of the resource may have been committed while others may have been aborted, likely because of heuristic decisions.")
   CASE_HRESULT(XACT_E_ISOLATIONLEVEL                                   , "XACT_E_ISOLATIONLEVEL"                                      , "The requested isolation level is not valid or supported.")
   CASE_HRESULT(XACT_E_NOASYNC                                          , "XACT_E_NOASYNC"                                             , "The transaction manager doesn't support an asynchronous operation for this method.")
   CASE_HRESULT(XACT_E_NOENLIST                                         , "XACT_E_NOENLIST"                                            , "Unable to enlist in the transaction.")
   CASE_HRESULT(XACT_E_NOISORETAIN                                      , "XACT_E_NOISORETAIN"                                         , "The requested semantics of retention of isolation across retaining commit and abort boundaries cannot be supported by this transaction implementation, or isoFlags was not equal to zero.")
   CASE_HRESULT(XACT_E_NORESOURCE                                       , "XACT_E_NORESOURCE"                                          , "There is no resource presently associated with this enlistment")
   CASE_HRESULT(XACT_E_NOTCURRENT                                       , "XACT_E_NOTCURRENT"                                          , "The transaction failed to commit due to the failure of optimistic concurrency control in at least one of the resource managers.")
   CASE_HRESULT(XACT_E_NOTRANSACTION                                    , "XACT_E_NOTRANSACTION"                                       , "The transaction has already been implicitly or explicitly committed or aborted")
   CASE_HRESULT(XACT_E_NOTSUPPORTED                                     , "XACT_E_NOTSUPPORTED"                                        , "An invalid combination of flags was specified")
   CASE_HRESULT(XACT_E_UNKNOWNRMGRID                                    , "XACT_E_UNKNOWNRMGRID"                                       , "The resource manager id is not associated with this transaction or the transaction manager.")
   CASE_HRESULT(XACT_E_WRONGSTATE                                       , "XACT_E_WRONGSTATE"                                          , "This method was called in the wrong state")
   CASE_HRESULT(XACT_E_WRONGUOW                                         , "XACT_E_WRONGUOW"                                            , "The indicated unit of work does not match the unit of work expected by the resource manager.")
   CASE_HRESULT(XACT_E_XTIONEXISTS                                      , "XACT_E_XTIONEXISTS"                                         , "An enlistment in a transaction already exists.")
   CASE_HRESULT(XACT_E_NOIMPORTOBJECT                                   , "XACT_E_NOIMPORTOBJECT"                                      , "An import object for the transaction could not be found.")
   CASE_HRESULT(XACT_E_INVALIDCOOKIE                                    , "XACT_E_INVALIDCOOKIE"                                       , "The transaction cookie is invalid.")
   CASE_HRESULT(XACT_E_INDOUBT                                          , "XACT_E_INDOUBT"                                             , "The transaction status is in doubt. A communication failure occurred, or a transaction manager or resource manager has failed")
   CASE_HRESULT(XACT_E_NOTIMEOUT                                        , "XACT_E_NOTIMEOUT"                                           , "A time-out was specified, but time-outs are not supported.")
   CASE_HRESULT(XACT_E_ALREADYINPROGRESS                                , "XACT_E_ALREADYINPROGRESS"                                   , "The requested operation is already in progress for the transaction.")
   CASE_HRESULT(XACT_E_ABORTED                                          , "XACT_E_ABORTED"                                             , "The transaction has already been aborted.")
   CASE_HRESULT(XACT_E_LOGFULL                                          , "XACT_E_LOGFULL"                                             , "The Transaction Manager returned a log full error.")
   CASE_HRESULT(XACT_E_TMNOTAVAILABLE                                   , "XACT_E_TMNOTAVAILABLE"                                      , "The Transaction Manager is not available.")
   CASE_HRESULT(XACT_E_CONNECTION_DOWN                                  , "XACT_E_CONNECTION_DOWN"                                     , "A connection with the transaction manager was lost.")
   CASE_HRESULT(XACT_E_CONNECTION_DENIED                                , "XACT_E_CONNECTION_DENIED"                                   , "A request to establish a connection with the transaction manager was denied.")
   CASE_HRESULT(XACT_E_REENLISTTIMEOUT                                  , "XACT_E_REENLISTTIMEOUT"                                     , "Resource manager reenlistment to determine transaction status timed out.")
   CASE_HRESULT(XACT_E_TIP_CONNECT_FAILED                               , "XACT_E_TIP_CONNECT_FAILED"                                  , "This transaction manager failed to establish a connection with another TIP transaction manager.")
   CASE_HRESULT(XACT_E_TIP_PROTOCOL_ERROR                               , "XACT_E_TIP_PROTOCOL_ERROR"                                  , "This transaction manager encountered a protocol error with another TIP transaction manager.")
   CASE_HRESULT(XACT_E_TIP_PULL_FAILED                                  , "XACT_E_TIP_PULL_FAILED"                                     , "This transaction manager could not propagate a transaction from another TIP transaction manager.")
   CASE_HRESULT(XACT_E_DEST_TMNOTAVAILABLE                              , "XACT_E_DEST_TMNOTAVAILABLE"                                 , "The Transaction Manager on the destination machine is not available.")
   CASE_HRESULT(XACT_E_TIP_DISABLED                                     , "XACT_E_TIP_DISABLED"                                        , "The Transaction Manager has disabled its support for TIP.")
   CASE_HRESULT(XACT_E_NETWORK_TX_DISABLED                              , "XACT_E_NETWORK_TX_DISABLED"                                 , "The transaction manager has disabled its support for remote/network transactions.")
   CASE_HRESULT(XACT_E_PARTNER_NETWORK_TX_DISABLED                      , "XACT_E_PARTNER_NETWORK_TX_DISABLED"                         , "The partner transaction manager has disabled its support for remote/network transactions.")
   CASE_HRESULT(XACT_E_XA_TX_DISABLED                                   , "XACT_E_XA_TX_DISABLED"                                      , "The transaction manager has disabled its support for XA transactions.")
   CASE_HRESULT(XACT_E_UNABLE_TO_READ_DTC_CONFIG                        , "XACT_E_UNABLE_TO_READ_DTC_CONFIG"                           , "MSDTC was unable to read its configuration information.")
   CASE_HRESULT(XACT_E_UNABLE_TO_LOAD_DTC_PROXY                         , "XACT_E_UNABLE_TO_LOAD_DTC_PROXY"                            , "MSDTC was unable to load the dtc proxy dll.")
   CASE_HRESULT(XACT_E_ABORTING                                         , "XACT_E_ABORTING"                                            , "The local transaction has aborted.")
   CASE_HRESULT(XACT_E_CLERKNOTFOUND                                    , "XACT_E_CLERKNOTFOUND"                                       , "XACT_E_CLERKNOTFOUND")
   CASE_HRESULT(XACT_E_CLERKEXISTS                                      , "XACT_E_CLERKEXISTS"                                         , "XACT_E_CLERKEXISTS")
   CASE_HRESULT(XACT_E_RECOVERYINPROGRESS                               , "XACT_E_RECOVERYINPROGRESS"                                  , "XACT_E_RECOVERYINPROGRESS")
   CASE_HRESULT(XACT_E_TRANSACTIONCLOSED                                , "XACT_E_TRANSACTIONCLOSED"                                   , "XACT_E_TRANSACTIONCLOSED")
   CASE_HRESULT(XACT_E_INVALIDLSN                                       , "XACT_E_INVALIDLSN"                                          , "XACT_E_INVALIDLSN")
   CASE_HRESULT(XACT_E_REPLAYREQUEST                                    , "XACT_E_REPLAYREQUEST"                                       , "XACT_E_REPLAYREQUEST")
   CASE_HRESULT(XACT_S_ASYNC                                            , "XACT_S_ASYNC"                                               , "An asynchronous operation was specified. The operation has begun, but its outcome is not known yet.")
   CASE_HRESULT(XACT_S_DEFECT                                           , "XACT_S_DEFECT"                                              , "XACT_S_DEFECT")
   CASE_HRESULT(XACT_S_READONLY                                         , "XACT_S_READONLY"                                            , "The method call succeeded because the transaction was read-only.")
   CASE_HRESULT(XACT_S_SOMENORETAIN                                     , "XACT_S_SOMENORETAIN"                                        , "The transaction was successfully aborted. However, this is a coordinated transaction, and some number of enlisted resources were aborted outright because they could not support abort-retaining semantics")
   CASE_HRESULT(XACT_S_OKINFORM                                         , "XACT_S_OKINFORM"                                            , "No changes were made during this call, but the sink wants another chance to look if any other sinks make further changes.")
   CASE_HRESULT(XACT_S_MADECHANGESCONTENT                               , "XACT_S_MADECHANGESCONTENT"                                  , "The sink is content and wishes the transaction to proceed. Changes were made to one or more resources during this call.")
   CASE_HRESULT(XACT_S_MADECHANGESINFORM                                , "XACT_S_MADECHANGESINFORM"                                   , "The sink is for the moment and wishes the transaction to proceed, but if other changes are made following this return by other event sinks then this sink wants another chance to look")
   CASE_HRESULT(XACT_S_ALLNORETAIN                                      , "XACT_S_ALLNORETAIN"                                         , "The transaction was successfully aborted. However, the abort was non-retaining.")
   CASE_HRESULT(XACT_S_ABORTING                                         , "XACT_S_ABORTING"                                            , "An abort operation was already in progress.")
   CASE_HRESULT(XACT_S_SINGLEPHASE                                      , "XACT_S_SINGLEPHASE"                                         , "The resource manager has performed a single-phase commit of the transaction.")
   CASE_HRESULT(XACT_S_LOCALLY_OK                                       , "XACT_S_LOCALLY_OK"                                          , "The local transaction has not aborted.")
   CASE_HRESULT(XACT_S_LASTRESOURCEMANAGER                              , "XACT_S_LASTRESOURCEMANAGER"                                 , "The resource manager has requested to be the coordinator (last resource manager) for the transaction.")
   CASE_HRESULT(CONTEXT_E_ABORTED                                       , "CONTEXT_E_ABORTED"                                          , "The root transaction wanted to commit, but transaction aborted")
   CASE_HRESULT(CONTEXT_E_ABORTING                                      , "CONTEXT_E_ABORTING"                                         , "You made a method call on a COM+ component that has a transaction that has already aborted or in the process of aborting.")
   CASE_HRESULT(CONTEXT_E_NOCONTEXT                                     , "CONTEXT_E_NOCONTEXT"                                        , "There is no MTS object context")
   CASE_HRESULT(CONTEXT_E_WOULD_DEADLOCK                                , "CONTEXT_E_WOULD_DEADLOCK"                                   , "The component is configured to use synchronization and this method call would cause a deadlock to occur.")
   CASE_HRESULT(CONTEXT_E_SYNCH_TIMEOUT                                 , "CONTEXT_E_SYNCH_TIMEOUT"                                    , "The component is configured to use synchronization and a thread has timed out waiting to enter the context.")
   CASE_HRESULT(CONTEXT_E_OLDREF                                        , "CONTEXT_E_OLDREF"                                           , "You made a method call on a COM+ component that has a transaction that has already committed or aborted.")
   CASE_HRESULT(CONTEXT_E_ROLENOTFOUND                                  , "CONTEXT_E_ROLENOTFOUND"                                     , "The specified role was not configured for the application")
   CASE_HRESULT(CONTEXT_E_TMNOTAVAILABLE                                , "CONTEXT_E_TMNOTAVAILABLE"                                   , "COM+ was unable to talk to the Microsoft Distributed Transaction Coordinator")
   CASE_HRESULT(CO_E_ACTIVATIONFAILED                                   , "CO_E_ACTIVATIONFAILED"                                      , "An unexpected error occurred during COM+ Activation.")
   CASE_HRESULT(CO_E_ACTIVATIONFAILED_EVENTLOGGED                       , "CO_E_ACTIVATIONFAILED_EVENTLOGGED"                          , "COM+ Activation failed. Check the event log for more information")
   CASE_HRESULT(CO_E_ACTIVATIONFAILED_CATALOGERROR                      , "CO_E_ACTIVATIONFAILED_CATALOGERROR"                         , "COM+ Activation failed due to a catalog or configuration error.")
   CASE_HRESULT(CO_E_ACTIVATIONFAILED_TIMEOUT                           , "CO_E_ACTIVATIONFAILED_TIMEOUT"                              , "COM+ activation failed because the activation could not be completed in the specified amount of time.")
   CASE_HRESULT(CO_E_INITIALIZATIONFAILED                               , "CO_E_INITIALIZATIONFAILED"                                  , "COM+ Activation failed because an initialization function failed.  Check the event log for more information.")
   CASE_HRESULT(CONTEXT_E_NOJIT                                         , "CONTEXT_E_NOJIT"                                            , "The requested operation requires that JIT be in the current context and it is not")
   CASE_HRESULT(CONTEXT_E_NOTRANSACTION                                 , "CONTEXT_E_NOTRANSACTION"                                    , "The requested operation requires that the current context have a Transaction, and it does not")
   CASE_HRESULT(CO_E_THREADINGMODEL_CHANGED                             , "CO_E_THREADINGMODEL_CHANGED"                                , "The components threading model has changed after install into a COM+ Application.  Please re-install component.")
   CASE_HRESULT(CO_E_NOIISINTRINSICS                                    , "CO_E_NOIISINTRINSICS"                                       , "IIS intrinsics not available.  Start your work with IIS.")
   CASE_HRESULT(CO_E_NOCOOKIES                                          , "CO_E_NOCOOKIES"                                             , "An attempt to write a cookie failed.")
   CASE_HRESULT(CO_E_DBERROR                                            , "CO_E_DBERROR"                                               , "An attempt to use a database generated a database specific error.")
   CASE_HRESULT(CO_E_NOTPOOLED                                          , "CO_E_NOTPOOLED"                                             , "The COM+ component you created must use object pooling to work.")
   CASE_HRESULT(CO_E_NOTCONSTRUCTED                                     , "CO_E_NOTCONSTRUCTED"                                        , "The COM+ component you created must use object construction to work correctly.")
   CASE_HRESULT(CO_E_NOSYNCHRONIZATION                                  , "CO_E_NOSYNCHRONIZATION"                                     , "The COM+ component requires synchronization, and it is not configured for it.")
   CASE_HRESULT(CO_E_ISOLEVELMISMATCH                                   , "CO_E_ISOLEVELMISMATCH"                                      , "The TxIsolation Level property for the COM+ component being created is stronger than the TxIsolationLevel for the 'root' component for the transaction.  The creation failed.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(OLE_S_USEREG                                            , "OLE_S_USEREG"                                               , "Use the registry database to provide the requested information")
   CASE_HRESULT(OLE_S_STATIC                                            , "OLE_S_STATIC"                                               , "Success, but static")
   CASE_HRESULT(OLE_S_MAC_CLIPFORMAT                                    , "OLE_S_MAC_CLIPFORMAT"                                       , "Macintosh clipboard format")
   CASE_HRESULT(DRAGDROP_S_DROP                                         , "DRAGDROP_S_DROP"                                            , "Successful drop took place")
   CASE_HRESULT(DRAGDROP_S_CANCEL                                       , "DRAGDROP_S_CANCEL"                                          , "Drag-drop operation canceled")
   CASE_HRESULT(DRAGDROP_S_USEDEFAULTCURSORS                            , "DRAGDROP_S_USEDEFAULTCURSORS"                               , "Use the default cursor")
   CASE_HRESULT(DATA_S_SAMEFORMATETC                                    , "DATA_S_SAMEFORMATETC"                                       , "Data has same FORMATETC")
   CASE_HRESULT(VIEW_S_ALREADY_FROZEN                                   , "VIEW_S_ALREADY_FROZEN"                                      , "View is already frozen")
   CASE_HRESULT(CACHE_S_FORMATETC_NOTSUPPORTED                          , "CACHE_S_FORMATETC_NOTSUPPORTED"                             , "FORMATETC not supported")
   CASE_HRESULT(CACHE_S_SAMECACHE                                       , "CACHE_S_SAMECACHE"                                          , "Same cache")
   CASE_HRESULT(CACHE_S_SOMECACHES_NOTUPDATED                           , "CACHE_S_SOMECACHES_NOTUPDATED"                              , "Some cache(s) not updated")
   CASE_HRESULT(OLEOBJ_S_INVALIDVERB                                    , "OLEOBJ_S_INVALIDVERB"                                       , "Invalid verb for OLE object")
   CASE_HRESULT(OLEOBJ_S_CANNOT_DOVERB_NOW                              , "OLEOBJ_S_CANNOT_DOVERB_NOW"                                 , "Verb number is valid but verb cannot be done now")
   CASE_HRESULT(OLEOBJ_S_INVALIDHWND                                    , "OLEOBJ_S_INVALIDHWND"                                       , "Invalid window handle passed")
   CASE_HRESULT(INPLACE_S_TRUNCATED                                     , "INPLACE_S_TRUNCATED"                                        , "Message is too long; some of it had to be truncated before displaying")
   CASE_HRESULT(CONVERT10_S_NO_PRESENTATION                             , "CONVERT10_S_NO_PRESENTATION"                                , "Unable to convert OLESTREAM to IStorage")
   CASE_HRESULT(MK_S_REDUCED_TO_SELF                                    , "MK_S_REDUCED_TO_SELF"                                       , "Moniker reduced to itself")
   CASE_HRESULT(MK_S_ME                                                 , "MK_S_ME"                                                    , "Common prefix is this moniker")
   CASE_HRESULT(MK_S_HIM                                                , "MK_S_HIM"                                                   , "Common prefix is input moniker")
   CASE_HRESULT(MK_S_US                                                 , "MK_S_US"                                                    , "Common prefix is both monikers")
   CASE_HRESULT(MK_S_MONIKERALREADYREGISTERED                           , "MK_S_MONIKERALREADYREGISTERED"                              , "Moniker is already registered in running object table")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SCHED_S_TASK_READY                                      , "SCHED_S_TASK_READY"                                         , "The task is ready to run at its next scheduled time.")
   CASE_HRESULT(SCHED_S_TASK_RUNNING                                    , "SCHED_S_TASK_RUNNING"                                       , "The task is currently running.")
   CASE_HRESULT(SCHED_S_TASK_DISABLED                                   , "SCHED_S_TASK_DISABLED"                                      , "The task will not run at the scheduled times because it has been disabled.")
   CASE_HRESULT(SCHED_S_TASK_HAS_NOT_RUN                                , "SCHED_S_TASK_HAS_NOT_RUN"                                   , "The task has not yet run.")
   CASE_HRESULT(SCHED_S_TASK_NO_MORE_RUNS                               , "SCHED_S_TASK_NO_MORE_RUNS"                                  , "There are no more runs scheduled for this task.")
   CASE_HRESULT(SCHED_S_TASK_NOT_SCHEDULED                              , "SCHED_S_TASK_NOT_SCHEDULED"                                 , "One or more of the properties that are needed to run this task on a schedule have not been set.")
   CASE_HRESULT(SCHED_S_TASK_TERMINATED                                 , "SCHED_S_TASK_TERMINATED"                                    , "The last run of the task was terminated by the user.")
   CASE_HRESULT(SCHED_S_TASK_NO_VALID_TRIGGERS                          , "SCHED_S_TASK_NO_VALID_TRIGGERS"                             , "Either the task has no triggers or the existing triggers are disabled or not set.")
   CASE_HRESULT(SCHED_S_EVENT_TRIGGER                                   , "SCHED_S_EVENT_TRIGGER"                                      , "Event triggers don't have set run times.")
   CASE_HRESULT(SCHED_E_TRIGGER_NOT_FOUND                               , "SCHED_E_TRIGGER_NOT_FOUND"                                  , "Trigger not found.")
   CASE_HRESULT(SCHED_E_TASK_NOT_READY                                  , "SCHED_E_TASK_NOT_READY"                                     , "One or more of the properties that are needed to run this task have not been set.")
   CASE_HRESULT(SCHED_E_TASK_NOT_RUNNING                                , "SCHED_E_TASK_NOT_RUNNING"                                   , "There is no running instance of the task to terminate.")
   CASE_HRESULT(SCHED_E_SERVICE_NOT_INSTALLED                           , "SCHED_E_SERVICE_NOT_INSTALLED"                              , "The Task Scheduler Service is not installed on this computer.")
   CASE_HRESULT(SCHED_E_CANNOT_OPEN_TASK                                , "SCHED_E_CANNOT_OPEN_TASK"                                   , "The task object could not be opened.")
   CASE_HRESULT(SCHED_E_INVALID_TASK                                    , "SCHED_E_INVALID_TASK"                                       , "The object is either an invalid task object or is not a task object.")
   CASE_HRESULT(SCHED_E_ACCOUNT_INFORMATION_NOT_SET                     , "SCHED_E_ACCOUNT_INFORMATION_NOT_SET"                        , "No account information could be found in the Task Scheduler security database for the task indicated.")
   CASE_HRESULT(SCHED_E_ACCOUNT_NAME_NOT_FOUND                          , "SCHED_E_ACCOUNT_NAME_NOT_FOUND"                             , "Unable to establish existence of the account specified.")
   CASE_HRESULT(SCHED_E_ACCOUNT_DBASE_CORRUPT                           , "SCHED_E_ACCOUNT_DBASE_CORRUPT"                              , "Corruption was detected in the Task Scheduler security database; the database has been reset.")
   CASE_HRESULT(SCHED_E_NO_SECURITY_SERVICES                            , "SCHED_E_NO_SECURITY_SERVICES"                               , "Task Scheduler security services are available only on Windows NT.")
   CASE_HRESULT(SCHED_E_UNKNOWN_OBJECT_VERSION                          , "SCHED_E_UNKNOWN_OBJECT_VERSION"                             , "The task object version is either unsupported or invalid.")
   CASE_HRESULT(SCHED_E_UNSUPPORTED_ACCOUNT_OPTION                      , "SCHED_E_UNSUPPORTED_ACCOUNT_OPTION"                         , "The task has been configured with an unsupported combination of account settings and run time options.")
   CASE_HRESULT(SCHED_E_SERVICE_NOT_RUNNING                             , "SCHED_E_SERVICE_NOT_RUNNING"                                , "The Task Scheduler Service is not running.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CO_E_CLASS_CREATE_FAILED                                , "CO_E_CLASS_CREATE_FAILED"                                   , "Attempt to create a class object failed")
   CASE_HRESULT(CO_E_SCM_ERROR                                          , "CO_E_SCM_ERROR"                                             , "OLE service could not bind object")
   CASE_HRESULT(CO_E_SCM_RPC_FAILURE                                    , "CO_E_SCM_RPC_FAILURE"                                       , "RPC communication failed with OLE service")
   CASE_HRESULT(CO_E_BAD_PATH                                           , "CO_E_BAD_PATH"                                              , "Bad path to object")
   CASE_HRESULT(CO_E_SERVER_EXEC_FAILURE                                , "CO_E_SERVER_EXEC_FAILURE"                                   , "Server execution failed")
   CASE_HRESULT(CO_E_OBJSRV_RPC_FAILURE                                 , "CO_E_OBJSRV_RPC_FAILURE"                                    , "OLE service could not communicate with the object server")
   CASE_HRESULT(MK_E_NO_NORMALIZED                                      , "MK_E_NO_NORMALIZED"                                         , "Moniker path could not be normalized")
   CASE_HRESULT(CO_E_SERVER_STOPPING                                    , "CO_E_SERVER_STOPPING"                                       , "Object server is stopping when OLE service contacts it")
   CASE_HRESULT(MEM_E_INVALID_ROOT                                      , "MEM_E_INVALID_ROOT"                                         , "An invalid root block pointer was specified")
   CASE_HRESULT(MEM_E_INVALID_LINK                                      , "MEM_E_INVALID_LINK"                                         , "An allocation chain contained an invalid link pointer")
   CASE_HRESULT(MEM_E_INVALID_SIZE                                      , "MEM_E_INVALID_SIZE"                                         , "The requested allocation size was too large")
   CASE_HRESULT(CO_S_NOTALLINTERFACES                                   , "CO_S_NOTALLINTERFACES"                                      , "Not all the requested interfaces were available")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CO_S_MACHINENAMENOTFOUND                                , "CO_S_MACHINENAMENOTFOUND"                                   , "The specified machine name was not found in the cache.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(DISP_E_UNKNOWNINTERFACE                                 , "DISP_E_UNKNOWNINTERFACE"                                    , "Unknown interface.")
   CASE_HRESULT(DISP_E_MEMBERNOTFOUND                                   , "DISP_E_MEMBERNOTFOUND"                                      , "Member not found.")
   CASE_HRESULT(DISP_E_PARAMNOTFOUND                                    , "DISP_E_PARAMNOTFOUND"                                       , "Parameter not found.")
   CASE_HRESULT(DISP_E_TYPEMISMATCH                                     , "DISP_E_TYPEMISMATCH"                                        , "Type mismatch.")
   CASE_HRESULT(DISP_E_UNKNOWNNAME                                      , "DISP_E_UNKNOWNNAME"                                         , "Unknown name.")
   CASE_HRESULT(DISP_E_NONAMEDARGS                                      , "DISP_E_NONAMEDARGS"                                         , "No named arguments.")
   CASE_HRESULT(DISP_E_BADVARTYPE                                       , "DISP_E_BADVARTYPE"                                          , "Bad variable type.")
   CASE_HRESULT(DISP_E_EXCEPTION                                        , "DISP_E_EXCEPTION"                                           , "Exception occurred.")
   CASE_HRESULT(DISP_E_OVERFLOW                                         , "DISP_E_OVERFLOW"                                            , "Out of present range.")
   CASE_HRESULT(DISP_E_BADINDEX                                         , "DISP_E_BADINDEX"                                            , "Invalid index.")
   CASE_HRESULT(DISP_E_UNKNOWNLCID                                      , "DISP_E_UNKNOWNLCID"                                         , "Unknown language.")
   CASE_HRESULT(DISP_E_ARRAYISLOCKED                                    , "DISP_E_ARRAYISLOCKED"                                       , "Memory is locked.")
   CASE_HRESULT(DISP_E_BADPARAMCOUNT                                    , "DISP_E_BADPARAMCOUNT"                                       , "Invalid number of parameters.")
   CASE_HRESULT(DISP_E_PARAMNOTOPTIONAL                                 , "DISP_E_PARAMNOTOPTIONAL"                                    , "Parameter not optional.")
   CASE_HRESULT(DISP_E_BADCALLEE                                        , "DISP_E_BADCALLEE"                                           , "Invalid callee.")
   CASE_HRESULT(DISP_E_NOTACOLLECTION                                   , "DISP_E_NOTACOLLECTION"                                      , "Does not support a collection.")
   CASE_HRESULT(DISP_E_DIVBYZERO                                        , "DISP_E_DIVBYZERO"                                           , "Division by zero.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(DISP_E_BUFFERTOOSMALL                                   , "DISP_E_BUFFERTOOSMALL"                                      , "Buffer too small")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(TYPE_E_BUFFERTOOSMALL                                   , "TYPE_E_BUFFERTOOSMALL"                                      , "Buffer too small.")
   CASE_HRESULT(TYPE_E_FIELDNOTFOUND                                    , "TYPE_E_FIELDNOTFOUND"                                       , "Field name not defined in the record.")
   CASE_HRESULT(TYPE_E_INVDATAREAD                                      , "TYPE_E_INVDATAREAD"                                         , "Old format or invalid type library.")
   CASE_HRESULT(TYPE_E_UNSUPFORMAT                                      , "TYPE_E_UNSUPFORMAT"                                         , "Old format or invalid type library.")
   CASE_HRESULT(TYPE_E_REGISTRYACCESS                                   , "TYPE_E_REGISTRYACCESS"                                      , "Error accessing the OLE registry.")
   CASE_HRESULT(TYPE_E_LIBNOTREGISTERED                                 , "TYPE_E_LIBNOTREGISTERED"                                    , "Library not registered.")
   CASE_HRESULT(TYPE_E_UNDEFINEDTYPE                                    , "TYPE_E_UNDEFINEDTYPE"                                       , "Bound to unknown type.")
   CASE_HRESULT(TYPE_E_QUALIFIEDNAMEDISALLOWED                          , "TYPE_E_QUALIFIEDNAMEDISALLOWED"                             , "Qualified name disallowed.")
   CASE_HRESULT(TYPE_E_INVALIDSTATE                                     , "TYPE_E_INVALIDSTATE"                                        , "Invalid forward reference, or reference to uncompiled type.")
   CASE_HRESULT(TYPE_E_WRONGTYPEKIND                                    , "TYPE_E_WRONGTYPEKIND"                                       , "Type mismatch.")
   CASE_HRESULT(TYPE_E_ELEMENTNOTFOUND                                  , "TYPE_E_ELEMENTNOTFOUND"                                     , "Element not found.")
   CASE_HRESULT(TYPE_E_AMBIGUOUSNAME                                    , "TYPE_E_AMBIGUOUSNAME"                                       , "Ambiguous name.")
   CASE_HRESULT(TYPE_E_NAMECONFLICT                                     , "TYPE_E_NAMECONFLICT"                                        , "Name already exists in the library.")
   CASE_HRESULT(TYPE_E_UNKNOWNLCID                                      , "TYPE_E_UNKNOWNLCID"                                         , "Unknown LCID.")
   CASE_HRESULT(TYPE_E_DLLFUNCTIONNOTFOUND                              , "TYPE_E_DLLFUNCTIONNOTFOUND"                                 , "Function not defined in specified DLL.")
   CASE_HRESULT(TYPE_E_BADMODULEKIND                                    , "TYPE_E_BADMODULEKIND"                                       , "Wrong module kind for the operation.")
   CASE_HRESULT(TYPE_E_SIZETOOBIG                                       , "TYPE_E_SIZETOOBIG"                                          , "Size may not exceed 64K.")
   CASE_HRESULT(TYPE_E_DUPLICATEID                                      , "TYPE_E_DUPLICATEID"                                         , "Duplicate ID in inheritance hierarchy.")
   CASE_HRESULT(TYPE_E_INVALIDID                                        , "TYPE_E_INVALIDID"                                           , "Incorrect inheritance depth in standard OLE hmember.")
   CASE_HRESULT(TYPE_E_TYPEMISMATCH                                     , "TYPE_E_TYPEMISMATCH"                                        , "Type mismatch.")
   CASE_HRESULT(TYPE_E_OUTOFBOUNDS                                      , "TYPE_E_OUTOFBOUNDS"                                         , "Invalid number of arguments.")
   CASE_HRESULT(TYPE_E_IOERROR                                          , "TYPE_E_IOERROR"                                             , "I/O Error.")
   CASE_HRESULT(TYPE_E_CANTCREATETMPFILE                                , "TYPE_E_CANTCREATETMPFILE"                                   , "Error creating unique tmp file.")
   CASE_HRESULT(TYPE_E_CANTLOADLIBRARY                                  , "TYPE_E_CANTLOADLIBRARY"                                     , "Error loading type library/DLL.")
   CASE_HRESULT(TYPE_E_INCONSISTENTPROPFUNCS                            , "TYPE_E_INCONSISTENTPROPFUNCS"                               , "Inconsistent property functions.")
   CASE_HRESULT(TYPE_E_CIRCULARTYPE                                     , "TYPE_E_CIRCULARTYPE"                                        , "Circular dependency between types/modules.")
   CASE_HRESULT(STG_E_INVALIDFUNCTION                                   , "STG_E_INVALIDFUNCTION"                                      , "Unable to perform requested operation.")
   CASE_HRESULT(STG_E_FILENOTFOUND                                      , "STG_E_FILENOTFOUND"                                         , "%1 could not be found.")
   CASE_HRESULT(STG_E_PATHNOTFOUND                                      , "STG_E_PATHNOTFOUND"                                         , "The path %1 could not be found.")
   CASE_HRESULT(STG_E_TOOMANYOPENFILES                                  , "STG_E_TOOMANYOPENFILES"                                     , "There are insufficient resources to open another file.")
   CASE_HRESULT(STG_E_ACCESSDENIED                                      , "STG_E_ACCESSDENIED"                                         , "Access Denied.")
   CASE_HRESULT(STG_E_INVALIDHANDLE                                     , "STG_E_INVALIDHANDLE"                                        , "Attempted an operation on an invalid object.")
   CASE_HRESULT(STG_E_INSUFFICIENTMEMORY                                , "STG_E_INSUFFICIENTMEMORY"                                   , "There is insufficient memory available to complete operation.")
   CASE_HRESULT(STG_E_INVALIDPOINTER                                    , "STG_E_INVALIDPOINTER"                                       , "Invalid pointer error.")
   CASE_HRESULT(STG_E_NOMOREFILES                                       , "STG_E_NOMOREFILES"                                          , "There are no more entries to return.")
   CASE_HRESULT(STG_E_DISKISWRITEPROTECTED                              , "STG_E_DISKISWRITEPROTECTED"                                 , "Disk is write-protected.")
   CASE_HRESULT(STG_E_SEEKERROR                                         , "STG_E_SEEKERROR"                                            , "An error occurred during a seek operation.")
   CASE_HRESULT(STG_E_WRITEFAULT                                        , "STG_E_WRITEFAULT"                                           , "A disk error occurred during a write operation.")
   CASE_HRESULT(STG_E_READFAULT                                         , "STG_E_READFAULT"                                            , "A disk error occurred during a read operation.")
   CASE_HRESULT(STG_E_SHAREVIOLATION                                    , "STG_E_SHAREVIOLATION"                                       , "A share violation has occurred.")
   CASE_HRESULT(STG_E_LOCKVIOLATION                                     , "STG_E_LOCKVIOLATION"                                        , "A lock violation has occurred.")
   CASE_HRESULT(STG_E_FILEALREADYEXISTS                                 , "STG_E_FILEALREADYEXISTS"                                    , "%1 already exists.")
   CASE_HRESULT(STG_E_INVALIDPARAMETER                                  , "STG_E_INVALIDPARAMETER"                                     , "Invalid parameter error.")
   CASE_HRESULT(STG_E_MEDIUMFULL                                        , "STG_E_MEDIUMFULL"                                           , "There is insufficient disk space to complete operation.")
   CASE_HRESULT(STG_E_PROPSETMISMATCHED                                 , "STG_E_PROPSETMISMATCHED"                                    , "Illegal write of non-simple property to simple property set.")
   CASE_HRESULT(STG_E_ABNORMALAPIEXIT                                   , "STG_E_ABNORMALAPIEXIT"                                      , "An API call exited abnormally.")
   CASE_HRESULT(STG_E_INVALIDHEADER                                     , "STG_E_INVALIDHEADER"                                        , "The file %1 is not a valid compound file.")
   CASE_HRESULT(STG_E_INVALIDNAME                                       , "STG_E_INVALIDNAME"                                          , "The name %1 is not valid.")
   CASE_HRESULT(STG_E_UNKNOWN                                           , "STG_E_UNKNOWN"                                              , "An unexpected error occurred.")
   CASE_HRESULT(STG_E_UNIMPLEMENTEDFUNCTION                             , "STG_E_UNIMPLEMENTEDFUNCTION"                                , "That function is not implemented.")
   CASE_HRESULT(STG_E_INVALIDFLAG                                       , "STG_E_INVALIDFLAG"                                          , "Invalid flag error.")
   CASE_HRESULT(STG_E_INUSE                                             , "STG_E_INUSE"                                                , "Attempted to use an object that is busy.")
   CASE_HRESULT(STG_E_NOTCURRENT                                        , "STG_E_NOTCURRENT"                                           , "The storage has been changed since the last commit.")
   CASE_HRESULT(STG_E_REVERTED                                          , "STG_E_REVERTED"                                             , "Attempted to use an object that has ceased to exist.")
   CASE_HRESULT(STG_E_CANTSAVE                                          , "STG_E_CANTSAVE"                                             , "Can't save.")
   CASE_HRESULT(STG_E_OLDFORMAT                                         , "STG_E_OLDFORMAT"                                            , "The compound file %1 was produced with an incompatible version of storage.")
   CASE_HRESULT(STG_E_OLDDLL                                            , "STG_E_OLDDLL"                                               , "The compound file %1 was produced with a newer version of storage.")
   CASE_HRESULT(STG_E_SHAREREQUIRED                                     , "STG_E_SHAREREQUIRED"                                        , "Share.exe or equivalent is required for operation.")
   CASE_HRESULT(STG_E_NOTFILEBASEDSTORAGE                               , "STG_E_NOTFILEBASEDSTORAGE"                                  , "Illegal operation called on non-file based storage.")
   CASE_HRESULT(STG_E_EXTANTMARSHALLINGS                                , "STG_E_EXTANTMARSHALLINGS"                                   , "Illegal operation called on object with extant marshallings.")
   CASE_HRESULT(STG_E_DOCFILECORRUPT                                    , "STG_E_DOCFILECORRUPT"                                       , "The docfile has been corrupted.")
   CASE_HRESULT(STG_E_BADBASEADDRESS                                    , "STG_E_BADBASEADDRESS"                                       , "OLE32.DLL has been loaded at the wrong address.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(STG_E_DOCFILETOOLARGE                                   , "STG_E_DOCFILETOOLARGE"                                      , "The compound file is too large for the current implementation")
   CASE_HRESULT(STG_E_NOTSIMPLEFORMAT                                   , "STG_E_NOTSIMPLEFORMAT"                                      , "The compound file was not created with the STGM_SIMPLE flag")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(STG_E_INCOMPLETE                                        , "STG_E_INCOMPLETE"                                           , "The file download was aborted abnormally.  The file is incomplete.")
   CASE_HRESULT(STG_E_TERMINATED                                        , "STG_E_TERMINATED"                                           , "The file download has been terminated.")
   CASE_HRESULT(STG_S_CONVERTED                                         , "STG_S_CONVERTED"                                            , "The underlying file was converted to compound file format.")
   CASE_HRESULT(STG_S_BLOCK                                             , "STG_S_BLOCK"                                                , "The storage operation should block until more data is available.")
   CASE_HRESULT(STG_S_RETRYNOW                                          , "STG_S_RETRYNOW"                                             , "The storage operation should retry immediately.")
   CASE_HRESULT(STG_S_MONITORING                                        , "STG_S_MONITORING"                                           , "The notified event sink will not influence the storage operation.")
   CASE_HRESULT(STG_S_MULTIPLEOPENS                                     , "STG_S_MULTIPLEOPENS"                                        , "Multiple opens prevent consolidated. (commit succeeded).")
   CASE_HRESULT(STG_S_CONSOLIDATIONFAILED                               , "STG_S_CONSOLIDATIONFAILED"                                  , "Consolidation of the storage file failed. (commit succeeded).")
   CASE_HRESULT(STG_S_CANNOTCONSOLIDATE                                 , "STG_S_CANNOTCONSOLIDATE"                                    , "Consolidation of the storage file is inappropriate. (commit succeeded).")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(STG_E_STATUS_COPY_PROTECTION_FAILURE                    , "STG_E_STATUS_COPY_PROTECTION_FAILURE"                       , "Generic Copy Protection Error.")
   CASE_HRESULT(STG_E_CSS_AUTHENTICATION_FAILURE                        , "STG_E_CSS_AUTHENTICATION_FAILURE"                           , "Copy Protection Error - DVD CSS Authentication failed.")
   CASE_HRESULT(STG_E_CSS_KEY_NOT_PRESENT                               , "STG_E_CSS_KEY_NOT_PRESENT"                                  , "Copy Protection Error - The given sector does not have a valid CSS key.")
   CASE_HRESULT(STG_E_CSS_KEY_NOT_ESTABLISHED                           , "STG_E_CSS_KEY_NOT_ESTABLISHED"                              , "Copy Protection Error - DVD session key not established.")
   CASE_HRESULT(STG_E_CSS_SCRAMBLED_SECTOR                              , "STG_E_CSS_SCRAMBLED_SECTOR"                                 , "Copy Protection Error - The read failed because the sector is encrypted.")
   CASE_HRESULT(STG_E_CSS_REGION_MISMATCH                               , "STG_E_CSS_REGION_MISMATCH"                                  , "Copy Protection Error - The current DVD's region does not correspond to the region setting of the drive.")
   CASE_HRESULT(STG_E_RESETS_EXHAUSTED                                  , "STG_E_RESETS_EXHAUSTED"                                     , "Copy Protection Error - The drive's region setting may be permanent or the number of user resets has been exhausted.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(RPC_E_CALL_REJECTED                                     , "RPC_E_CALL_REJECTED"                                        , "Call was rejected by callee.")
   CASE_HRESULT(RPC_E_CALL_CANCELED                                     , "RPC_E_CALL_CANCELED"                                        , "Call was canceled by the message filter.")
   CASE_HRESULT(RPC_E_CANTPOST_INSENDCALL                               , "RPC_E_CANTPOST_INSENDCALL"                                  , "The caller is dispatching an intertask SendMessage call and cannot call out via PostMessage.")
   CASE_HRESULT(RPC_E_CANTCALLOUT_INASYNCCALL                           , "RPC_E_CANTCALLOUT_INASYNCCALL"                              , "The caller is dispatching an asynchronous call and cannot make an outgoing call on behalf of this call.")
   CASE_HRESULT(RPC_E_CANTCALLOUT_INEXTERNALCALL                        , "RPC_E_CANTCALLOUT_INEXTERNALCALL"                           , "It is illegal to call out while inside message filter.")
   CASE_HRESULT(RPC_E_CONNECTION_TERMINATED                             , "RPC_E_CONNECTION_TERMINATED"                                , "The connection terminated or is in a bogus state and cannot be used any more. Other connections are still valid.")
   CASE_HRESULT(RPC_E_SERVER_DIED                                       , "RPC_E_SERVER_DIED"                                          , "The callee (server [not server application]) is not available and disappeared; all connections are invalid. The call may have executed.")
   CASE_HRESULT(RPC_E_CLIENT_DIED                                       , "RPC_E_CLIENT_DIED"                                          , "The caller (client) disappeared while the callee (server) was processing a call.")
   CASE_HRESULT(RPC_E_INVALID_DATAPACKET                                , "RPC_E_INVALID_DATAPACKET"                                   , "The data packet with the marshalled parameter data is incorrect.")
   CASE_HRESULT(RPC_E_CANTTRANSMIT_CALL                                 , "RPC_E_CANTTRANSMIT_CALL"                                    , "The call was not transmitted properly; the message queue was full and was not emptied after yielding.")
   CASE_HRESULT(RPC_E_CLIENT_CANTMARSHAL_DATA                           , "RPC_E_CLIENT_CANTMARSHAL_DATA"                              , "The client (caller) cannot marshall the parameter data - low memory, etc.")
   CASE_HRESULT(RPC_E_CLIENT_CANTUNMARSHAL_DATA                         , "RPC_E_CLIENT_CANTUNMARSHAL_DATA"                            , "The client (caller) cannot unmarshall the return data - low memory, etc.")
   CASE_HRESULT(RPC_E_SERVER_CANTMARSHAL_DATA                           , "RPC_E_SERVER_CANTMARSHAL_DATA"                              , "The server (callee) cannot marshall the return data - low memory, etc.")
   CASE_HRESULT(RPC_E_SERVER_CANTUNMARSHAL_DATA                         , "RPC_E_SERVER_CANTUNMARSHAL_DATA"                            , "The server (callee) cannot unmarshall the parameter data - low memory, etc.")
   CASE_HRESULT(RPC_E_INVALID_DATA                                      , "RPC_E_INVALID_DATA"                                         , "Received data is invalid; could be server or client data.")
   CASE_HRESULT(RPC_E_INVALID_PARAMETER                                 , "RPC_E_INVALID_PARAMETER"                                    , "A particular parameter is invalid and cannot be (un)marshalled.")
   CASE_HRESULT(RPC_E_CANTCALLOUT_AGAIN                                 , "RPC_E_CANTCALLOUT_AGAIN"                                    , "There is no second outgoing call on same channel in DDE conversation.")
   CASE_HRESULT(RPC_E_SERVER_DIED_DNE                                   , "RPC_E_SERVER_DIED_DNE"                                      , "The callee (server [not server application]) is not available and disappeared; all connections are invalid. The call did not execute.")
   CASE_HRESULT(RPC_E_SYS_CALL_FAILED                                   , "RPC_E_SYS_CALL_FAILED"                                      , "System call failed.")
   CASE_HRESULT(RPC_E_OUT_OF_RESOURCES                                  , "RPC_E_OUT_OF_RESOURCES"                                     , "Could not allocate some required resource (memory, events, ...)")
   CASE_HRESULT(RPC_E_ATTEMPTED_MULTITHREAD                             , "RPC_E_ATTEMPTED_MULTITHREAD"                                , "Attempted to make calls on more than one thread in single threaded mode.")
   CASE_HRESULT(RPC_E_NOT_REGISTERED                                    , "RPC_E_NOT_REGISTERED"                                       , "The requested interface is not registered on the server object.")
   CASE_HRESULT(RPC_E_FAULT                                             , "RPC_E_FAULT"                                                , "RPC could not call the server or could not return the results of calling the server.")
   CASE_HRESULT(RPC_E_SERVERFAULT                                       , "RPC_E_SERVERFAULT"                                          , "The server threw an exception.")
   CASE_HRESULT(RPC_E_CHANGED_MODE                                      , "RPC_E_CHANGED_MODE"                                         , "Cannot change thread mode after it is set.")
   CASE_HRESULT(RPC_E_INVALIDMETHOD                                     , "RPC_E_INVALIDMETHOD"                                        , "The method called does not exist on the server.")
   CASE_HRESULT(RPC_E_DISCONNECTED                                      , "RPC_E_DISCONNECTED"                                         , "The object invoked has disconnected from its clients.")
   CASE_HRESULT(RPC_E_RETRY                                             , "RPC_E_RETRY"                                                , "The object invoked chose not to process the call now.  Try again later.")
   CASE_HRESULT(RPC_E_SERVERCALL_RETRYLATER                             , "RPC_E_SERVERCALL_RETRYLATER"                                , "The message filter indicated that the application is busy.")
   CASE_HRESULT(RPC_E_SERVERCALL_REJECTED                               , "RPC_E_SERVERCALL_REJECTED"                                  , "The message filter rejected the call.")
   CASE_HRESULT(RPC_E_INVALID_CALLDATA                                  , "RPC_E_INVALID_CALLDATA"                                     , "A call control interfaces was called with invalid data.")
   CASE_HRESULT(RPC_E_CANTCALLOUT_ININPUTSYNCCALL                       , "RPC_E_CANTCALLOUT_ININPUTSYNCCALL"                          , "An outgoing call cannot be made since the application is dispatching an input-synchronous call.")
   CASE_HRESULT(RPC_E_WRONG_THREAD                                      , "RPC_E_WRONG_THREAD"                                         , "The application called an interface that was marshalled for a different thread.")
   CASE_HRESULT(RPC_E_THREAD_NOT_INIT                                   , "RPC_E_THREAD_NOT_INIT"                                      , "CoInitialize has not been called on the current thread.")
   CASE_HRESULT(RPC_E_VERSION_MISMATCH                                  , "RPC_E_VERSION_MISMATCH"                                     , "The version of OLE on the client and server machines does not match.")
   CASE_HRESULT(RPC_E_INVALID_HEADER                                    , "RPC_E_INVALID_HEADER"                                       , "OLE received a packet with an invalid header.")
   CASE_HRESULT(RPC_E_INVALID_EXTENSION                                 , "RPC_E_INVALID_EXTENSION"                                    , "OLE received a packet with an invalid extension.")
   CASE_HRESULT(RPC_E_INVALID_IPID                                      , "RPC_E_INVALID_IPID"                                         , "The requested object or interface does not exist.")
   CASE_HRESULT(RPC_E_INVALID_OBJECT                                    , "RPC_E_INVALID_OBJECT"                                       , "The requested object does not exist.")
   CASE_HRESULT(RPC_S_CALLPENDING                                       , "RPC_S_CALLPENDING"                                          , "OLE has sent a request and is waiting for a reply.")
   CASE_HRESULT(RPC_S_WAITONTIMER                                       , "RPC_S_WAITONTIMER"                                          , "OLE is waiting before retrying a request.")
   CASE_HRESULT(RPC_E_CALL_COMPLETE                                     , "RPC_E_CALL_COMPLETE"                                        , "Call context cannot be accessed after call completed.")
   CASE_HRESULT(RPC_E_UNSECURE_CALL                                     , "RPC_E_UNSECURE_CALL"                                        , "Impersonate on unsecure calls is not supported.")
   CASE_HRESULT(RPC_E_TOO_LATE                                          , "RPC_E_TOO_LATE"                                             , "Security must be initialized before any interfaces are marshalled or unmarshalled. It cannot be changed once initialized.")
   CASE_HRESULT(RPC_E_NO_GOOD_SECURITY_PACKAGES                         , "RPC_E_NO_GOOD_SECURITY_PACKAGES"                            , "No security packages are installed on this machine or the user is not logged on or there are no compatible security packages between the client and server.")
   CASE_HRESULT(RPC_E_ACCESS_DENIED                                     , "RPC_E_ACCESS_DENIED"                                        , "Access is denied.")
   CASE_HRESULT(RPC_E_REMOTE_DISABLED                                   , "RPC_E_REMOTE_DISABLED"                                      , "Remote calls are not allowed for this process.")
   CASE_HRESULT(RPC_E_INVALID_OBJREF                                    , "RPC_E_INVALID_OBJREF"                                       , "The marshaled interface data packet (OBJREF) has an invalid or unknown format.")
   CASE_HRESULT(RPC_E_NO_CONTEXT                                        , "RPC_E_NO_CONTEXT"                                           , "No context is associated with this call. This happens for some custom marshalled calls and on the client side of the call.")
   CASE_HRESULT(RPC_E_TIMEOUT                                           , "RPC_E_TIMEOUT"                                              , "This operation returned because the timeout period expired.")
   CASE_HRESULT(RPC_E_NO_SYNC                                           , "RPC_E_NO_SYNC"                                              , "There are no synchronize objects to wait on.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(RPC_E_FULLSIC_REQUIRED                                  , "RPC_E_FULLSIC_REQUIRED"                                     , "Full subject issuer chain SSL principal name expected from the server.")
   CASE_HRESULT(RPC_E_INVALID_STD_NAME                                  , "RPC_E_INVALID_STD_NAME"                                     , "Principal name is not a valid MSSTD name.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CO_E_FAILEDTOIMPERSONATE                                , "CO_E_FAILEDTOIMPERSONATE"                                   , "Unable to impersonate DCOM client")
   CASE_HRESULT(CO_E_FAILEDTOGETSECCTX                                  , "CO_E_FAILEDTOGETSECCTX"                                     , "Unable to obtain server's security context")
   CASE_HRESULT(CO_E_FAILEDTOOPENTHREADTOKEN                            , "CO_E_FAILEDTOOPENTHREADTOKEN"                               , "Unable to open the access token of the current thread")
   CASE_HRESULT(CO_E_FAILEDTOGETTOKENINFO                               , "CO_E_FAILEDTOGETTOKENINFO"                                  , "Unable to obtain user info from an access token")
   CASE_HRESULT(CO_E_TRUSTEEDOESNTMATCHCLIENT                           , "CO_E_TRUSTEEDOESNTMATCHCLIENT"                              , "The client who called IAccessControl::IsAccessPermitted was not the trustee provided to the method")
   CASE_HRESULT(CO_E_FAILEDTOQUERYCLIENTBLANKET                         , "CO_E_FAILEDTOQUERYCLIENTBLANKET"                            , "Unable to obtain the client's security blanket")
   CASE_HRESULT(CO_E_FAILEDTOSETDACL                                    , "CO_E_FAILEDTOSETDACL"                                       , "Unable to set a discretionary ACL into a security descriptor")
   CASE_HRESULT(CO_E_ACCESSCHECKFAILED                                  , "CO_E_ACCESSCHECKFAILED"                                     , "The system function, AccessCheck, returned false")
   CASE_HRESULT(CO_E_NETACCESSAPIFAILED                                 , "CO_E_NETACCESSAPIFAILED"                                    , "Either NetAccessDel or NetAccessAdd returned an error code.")
   CASE_HRESULT(CO_E_WRONGTRUSTEENAMESYNTAX                             , "CO_E_WRONGTRUSTEENAMESYNTAX"                                , "One of the trustee strings provided by the user did not conform to the <Domain>\\<Name> syntax and it was not the '*' string")
   CASE_HRESULT(CO_E_INVALIDSID                                         , "CO_E_INVALIDSID"                                            , "One of the security identifiers provided by the user was invalid")
   CASE_HRESULT(CO_E_CONVERSIONFAILED                                   , "CO_E_CONVERSIONFAILED"                                      , "Unable to convert a wide character trustee string to a multibyte trustee string")
   CASE_HRESULT(CO_E_NOMATCHINGSIDFOUND                                 , "CO_E_NOMATCHINGSIDFOUND"                                    , "Unable to find a security identifier that corresponds to a trustee string provided by the user")
   CASE_HRESULT(CO_E_LOOKUPACCSIDFAILED                                 , "CO_E_LOOKUPACCSIDFAILED"                                    , "The system function, LookupAccountSID, failed")
   CASE_HRESULT(CO_E_NOMATCHINGNAMEFOUND                                , "CO_E_NOMATCHINGNAMEFOUND"                                   , "Unable to find a trustee name that corresponds to a security identifier provided by the user")
   CASE_HRESULT(CO_E_LOOKUPACCNAMEFAILED                                , "CO_E_LOOKUPACCNAMEFAILED"                                   , "The system function, LookupAccountName, failed")
   CASE_HRESULT(CO_E_SETSERLHNDLFAILED                                  , "CO_E_SETSERLHNDLFAILED"                                     , "Unable to set or reset a serialization handle")
   CASE_HRESULT(CO_E_FAILEDTOGETWINDIR                                  , "CO_E_FAILEDTOGETWINDIR"                                     , "Unable to obtain the Windows directory")
   CASE_HRESULT(CO_E_PATHTOOLONG                                        , "CO_E_PATHTOOLONG"                                           , "Path too long")
   CASE_HRESULT(CO_E_FAILEDTOGENUUID                                    , "CO_E_FAILEDTOGENUUID"                                       , "Unable to generate a uuid.")
   CASE_HRESULT(CO_E_FAILEDTOCREATEFILE                                 , "CO_E_FAILEDTOCREATEFILE"                                    , "Unable to create file")
   CASE_HRESULT(CO_E_FAILEDTOCLOSEHANDLE                                , "CO_E_FAILEDTOCLOSEHANDLE"                                   , "Unable to close a serialization handle or a file handle.")
   CASE_HRESULT(CO_E_EXCEEDSYSACLLIMIT                                  , "CO_E_EXCEEDSYSACLLIMIT"                                     , "The number of ACEs in an ACL exceeds the system limit.")
   CASE_HRESULT(CO_E_ACESINWRONGORDER                                   , "CO_E_ACESINWRONGORDER"                                      , "Not all the DENY_ACCESS ACEs are arranged in front of the GRANT_ACCESS ACEs in the stream.")
   CASE_HRESULT(CO_E_INCOMPATIBLESTREAMVERSION                          , "CO_E_INCOMPATIBLESTREAMVERSION"                             , "The version of ACL format in the stream is not supported by this implementation of IAccessControl")
   CASE_HRESULT(CO_E_FAILEDTOOPENPROCESSTOKEN                           , "CO_E_FAILEDTOOPENPROCESSTOKEN"                              , "Unable to open the access token of the server process")
   CASE_HRESULT(CO_E_DECODEFAILED                                       , "CO_E_DECODEFAILED"                                          , "Unable to decode the ACL in the stream provided by the user")
   CASE_HRESULT(CO_E_ACNOTINITIALIZED                                   , "CO_E_ACNOTINITIALIZED"                                      , "The COM IAccessControl object is not initialized")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CO_E_CANCEL_DISABLED                                    , "CO_E_CANCEL_DISABLED"                                       , "Call Cancellation is disabled")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(RPC_E_UNEXPECTED                                        , "RPC_E_UNEXPECTED"                                           , "An internal error occurred.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(ERROR_AUDITING_DISABLED                                 , "ERROR_AUDITING_DISABLED"                                    , "The specified event is currently not being audited.")
   CASE_HRESULT(ERROR_ALL_SIDS_FILTERED                                 , "ERROR_ALL_SIDS_FILTERED"                                    , "The SID filtering operation removed all SIDs.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(NTE_BAD_UID                                             , "NTE_BAD_UID"                                                , "Bad UID.")
   CASE_HRESULT(NTE_BAD_HASH                                            , "NTE_BAD_HASH"                                               , "Bad Hash.")
   CASE_HRESULT(NTE_BAD_KEY                                             , "NTE_BAD_KEY"                                                , "Bad Key.")
   CASE_HRESULT(NTE_BAD_LEN                                             , "NTE_BAD_LEN"                                                , "Bad Length.")
   CASE_HRESULT(NTE_BAD_DATA                                            , "NTE_BAD_DATA"                                               , "Bad Data.")
   CASE_HRESULT(NTE_BAD_SIGNATURE                                       , "NTE_BAD_SIGNATURE"                                          , "Invalid Signature.")
   CASE_HRESULT(NTE_BAD_VER                                             , "NTE_BAD_VER"                                                , "Bad Version of provider.")
   CASE_HRESULT(NTE_BAD_ALGID                                           , "NTE_BAD_ALGID"                                              , "Invalid algorithm specified.")
   CASE_HRESULT(NTE_BAD_FLAGS                                           , "NTE_BAD_FLAGS"                                              , "Invalid flags specified.")
   CASE_HRESULT(NTE_BAD_TYPE                                            , "NTE_BAD_TYPE"                                               , "Invalid type specified.")
   CASE_HRESULT(NTE_BAD_KEY_STATE                                       , "NTE_BAD_KEY_STATE"                                          , "Key not valid for use in specified state.")
   CASE_HRESULT(NTE_BAD_HASH_STATE                                      , "NTE_BAD_HASH_STATE"                                         , "Hash not valid for use in specified state.")
   CASE_HRESULT(NTE_NO_KEY                                              , "NTE_NO_KEY"                                                 , "Key does not exist.")
   CASE_HRESULT(NTE_NO_MEMORY                                           , "NTE_NO_MEMORY"                                              , "Insufficient memory available for the operation.")
   CASE_HRESULT(NTE_EXISTS                                              , "NTE_EXISTS"                                                 , "Object already exists.")
   CASE_HRESULT(NTE_PERM                                                , "NTE_PERM"                                                   , "Access denied.")
   CASE_HRESULT(NTE_NOT_FOUND                                           , "NTE_NOT_FOUND"                                              , "Object was not found.")
   CASE_HRESULT(NTE_DOUBLE_ENCRYPT                                      , "NTE_DOUBLE_ENCRYPT"                                         , "Data already encrypted.")
   CASE_HRESULT(NTE_BAD_PROVIDER                                        , "NTE_BAD_PROVIDER"                                           , "Invalid provider specified.")
   CASE_HRESULT(NTE_BAD_PROV_TYPE                                       , "NTE_BAD_PROV_TYPE"                                          , "Invalid provider type specified.")
   CASE_HRESULT(NTE_BAD_PUBLIC_KEY                                      , "NTE_BAD_PUBLIC_KEY"                                         , "Provider's public key is invalid.")
   CASE_HRESULT(NTE_BAD_KEYSET                                          , "NTE_BAD_KEYSET"                                             , "Keyset does not exist")
   CASE_HRESULT(NTE_PROV_TYPE_NOT_DEF                                   , "NTE_PROV_TYPE_NOT_DEF"                                      , "Provider type not defined.")
   CASE_HRESULT(NTE_PROV_TYPE_ENTRY_BAD                                 , "NTE_PROV_TYPE_ENTRY_BAD"                                    , "Provider type as registered is invalid.")
   CASE_HRESULT(NTE_KEYSET_NOT_DEF                                      , "NTE_KEYSET_NOT_DEF"                                         , "The keyset is not defined.")
   CASE_HRESULT(NTE_KEYSET_ENTRY_BAD                                    , "NTE_KEYSET_ENTRY_BAD"                                       , "Keyset as registered is invalid.")
   CASE_HRESULT(NTE_PROV_TYPE_NO_MATCH                                  , "NTE_PROV_TYPE_NO_MATCH"                                     , "Provider type does not match registered value.")
   CASE_HRESULT(NTE_SIGNATURE_FILE_BAD                                  , "NTE_SIGNATURE_FILE_BAD"                                     , "The digital signature file is corrupt.")
   CASE_HRESULT(NTE_PROVIDER_DLL_FAIL                                   , "NTE_PROVIDER_DLL_FAIL"                                      , "Provider DLL failed to initialize correctly.")
   CASE_HRESULT(NTE_PROV_DLL_NOT_FOUND                                  , "NTE_PROV_DLL_NOT_FOUND"                                     , "Provider DLL could not be found.")
   CASE_HRESULT(NTE_BAD_KEYSET_PARAM                                    , "NTE_BAD_KEYSET_PARAM"                                       , "The Keyset parameter is invalid.")
   CASE_HRESULT(NTE_FAIL                                                , "NTE_FAIL"                                                   , "An internal error occurred.")
   CASE_HRESULT(NTE_SYS_ERR                                             , "NTE_SYS_ERR"                                                , "A base error occurred.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(NTE_SILENT_CONTEXT                                      , "NTE_SILENT_CONTEXT"                                         , "Provider could not perform the action since the context was acquired as silent.")
   CASE_HRESULT(NTE_TOKEN_KEYSET_STORAGE_FULL                           , "NTE_TOKEN_KEYSET_STORAGE_FULL"                              , "The security token does not have storage space available for an additional container.")
   CASE_HRESULT(NTE_TEMPORARY_PROFILE                                   , "NTE_TEMPORARY_PROFILE"                                      , "The profile for the user is a temporary profile.")
   CASE_HRESULT(NTE_FIXEDPARAMETER                                      , "NTE_FIXEDPARAMETER"                                         , "The key parameters could not be set because the CSP uses fixed parameters.")
   CASE_HRESULT(SEC_E_INSUFFICIENT_MEMORY                               , "SEC_E_INSUFFICIENT_MEMORY"                                  , "Not enough memory is available to complete this request")
   CASE_HRESULT(SEC_E_INVALID_HANDLE                                    , "SEC_E_INVALID_HANDLE"                                       , "The handle specified is invalid")
   CASE_HRESULT(SEC_E_UNSUPPORTED_FUNCTION                              , "SEC_E_UNSUPPORTED_FUNCTION"                                 , "The function requested is not supported")
   CASE_HRESULT(SEC_E_TARGET_UNKNOWN                                    , "SEC_E_TARGET_UNKNOWN"                                       , "The specified target is unknown or unreachable")
   CASE_HRESULT(SEC_E_INTERNAL_ERROR                                    , "SEC_E_INTERNAL_ERROR"                                       , "The Local Security Authority cannot be contacted")
   CASE_HRESULT(SEC_E_SECPKG_NOT_FOUND                                  , "SEC_E_SECPKG_NOT_FOUND"                                     , "The requested security package does not exist")
   CASE_HRESULT(SEC_E_NOT_OWNER                                         , "SEC_E_NOT_OWNER"                                            , "The caller is not the owner of the desired credentials")
   CASE_HRESULT(SEC_E_CANNOT_INSTALL                                    , "SEC_E_CANNOT_INSTALL"                                       , "The security package failed to initialize, and cannot be installed")
   CASE_HRESULT(SEC_E_INVALID_TOKEN                                     , "SEC_E_INVALID_TOKEN"                                        , "The token supplied to the function is invalid")
   CASE_HRESULT(SEC_E_CANNOT_PACK                                       , "SEC_E_CANNOT_PACK"                                          , "The security package is not able to marshall the logon buffer, so the logon attempt has failed")
   CASE_HRESULT(SEC_E_QOP_NOT_SUPPORTED                                 , "SEC_E_QOP_NOT_SUPPORTED"                                    , "The per-message Quality of Protection is not supported by the security package")
   CASE_HRESULT(SEC_E_NO_IMPERSONATION                                  , "SEC_E_NO_IMPERSONATION"                                     , "The security context does not allow impersonation of the client")
   CASE_HRESULT(SEC_E_LOGON_DENIED                                      , "SEC_E_LOGON_DENIED"                                         , "The logon attempt failed")
   CASE_HRESULT(SEC_E_UNKNOWN_CREDENTIALS                               , "SEC_E_UNKNOWN_CREDENTIALS"                                  , "The credentials supplied to the package were not recognized")
   CASE_HRESULT(SEC_E_NO_CREDENTIALS                                    , "SEC_E_NO_CREDENTIALS"                                       , "No credentials are available in the security package")
   CASE_HRESULT(SEC_E_MESSAGE_ALTERED                                   , "SEC_E_MESSAGE_ALTERED"                                      , "The message or signature supplied for verification has been altered")
   CASE_HRESULT(SEC_E_OUT_OF_SEQUENCE                                   , "SEC_E_OUT_OF_SEQUENCE"                                      , "The message supplied for verification is out of sequence")
   CASE_HRESULT(SEC_E_NO_AUTHENTICATING_AUTHORITY                       , "SEC_E_NO_AUTHENTICATING_AUTHORITY"                          , "No authority could be contacted for authentication.")
   CASE_HRESULT(SEC_I_CONTINUE_NEEDED                                   , "SEC_I_CONTINUE_NEEDED"                                      , "The function completed successfully, but must be called again to complete the context")
   CASE_HRESULT(SEC_I_COMPLETE_NEEDED                                   , "SEC_I_COMPLETE_NEEDED"                                      , "The function completed successfully, but CompleteToken must be called")
   CASE_HRESULT(SEC_I_COMPLETE_AND_CONTINUE                             , "SEC_I_COMPLETE_AND_CONTINUE"                                , "The function completed successfully, but both CompleteToken and this function must be called to complete the context")
   CASE_HRESULT(SEC_I_LOCAL_LOGON                                       , "SEC_I_LOCAL_LOGON"                                          , "The logon was completed, but no network authority was available. The logon was made using locally known information")
   CASE_HRESULT(SEC_E_BAD_PKGID                                         , "SEC_E_BAD_PKGID"                                            , "The requested security package does not exist")
   CASE_HRESULT(SEC_E_CONTEXT_EXPIRED                                   , "SEC_E_CONTEXT_EXPIRED"                                      , "The context has expired and can no longer be used.")
   CASE_HRESULT(SEC_I_CONTEXT_EXPIRED                                   , "SEC_I_CONTEXT_EXPIRED"                                      , "The context has expired and can no longer be used.")
   CASE_HRESULT(SEC_E_INCOMPLETE_MESSAGE                                , "SEC_E_INCOMPLETE_MESSAGE"                                   , "The supplied message is incomplete.  The signature was not verified.")
   CASE_HRESULT(SEC_E_INCOMPLETE_CREDENTIALS                            , "SEC_E_INCOMPLETE_CREDENTIALS"                               , "The credentials supplied were not complete, and could not be verified. The context could not be initialized.")
   CASE_HRESULT(SEC_E_BUFFER_TOO_SMALL                                  , "SEC_E_BUFFER_TOO_SMALL"                                     , "The buffers supplied to a function was too small.")
   CASE_HRESULT(SEC_I_INCOMPLETE_CREDENTIALS                            , "SEC_I_INCOMPLETE_CREDENTIALS"                               , "The credentials supplied were not complete, and could not be verified. Additional information can be returned from the context.")
   CASE_HRESULT(SEC_I_RENEGOTIATE                                       , "SEC_I_RENEGOTIATE"                                          , "The context data must be renegotiated with the peer.")
   CASE_HRESULT(SEC_E_WRONG_PRINCIPAL                                   , "SEC_E_WRONG_PRINCIPAL"                                      , "The target principal name is incorrect.")
   CASE_HRESULT(SEC_I_NO_LSA_CONTEXT                                    , "SEC_I_NO_LSA_CONTEXT"                                       , "There is no LSA mode context associated with this context.")
   CASE_HRESULT(SEC_E_TIME_SKEW                                         , "SEC_E_TIME_SKEW"                                            , "The clocks on the client and server machines are skewed.")
   CASE_HRESULT(SEC_E_UNTRUSTED_ROOT                                    , "SEC_E_UNTRUSTED_ROOT"                                       , "The certificate chain was issued by an authority that is not trusted.")
   CASE_HRESULT(SEC_E_ILLEGAL_MESSAGE                                   , "SEC_E_ILLEGAL_MESSAGE"                                      , "The message received was unexpected or badly formatted.")
   CASE_HRESULT(SEC_E_CERT_UNKNOWN                                      , "SEC_E_CERT_UNKNOWN"                                         , "An unknown error occurred while processing the certificate.")
   CASE_HRESULT(SEC_E_CERT_EXPIRED                                      , "SEC_E_CERT_EXPIRED"                                         , "The received certificate has expired.")
   CASE_HRESULT(SEC_E_ENCRYPT_FAILURE                                   , "SEC_E_ENCRYPT_FAILURE"                                      , "The specified data could not be encrypted.")
   CASE_HRESULT(SEC_E_DECRYPT_FAILURE                                   , "SEC_E_DECRYPT_FAILURE"                                      , "The specified data could not be decrypted.")
   CASE_HRESULT(SEC_E_ALGORITHM_MISMATCH                                , "SEC_E_ALGORITHM_MISMATCH"                                   , "The client and server cannot communicate, because they do not possess a common algorithm.")
   CASE_HRESULT(SEC_E_SECURITY_QOS_FAILED                               , "SEC_E_SECURITY_QOS_FAILED"                                  , "The security context could not be established due to a failure in the requested quality of service (e.g. mutual authentication or delegation).")
   CASE_HRESULT(SEC_E_UNFINISHED_CONTEXT_DELETED                        , "SEC_E_UNFINISHED_CONTEXT_DELETED"                           , "A security context was deleted before the context was completed.  This is considered a logon failure.")
   CASE_HRESULT(SEC_E_NO_TGT_REPLY                                      , "SEC_E_NO_TGT_REPLY"                                         , "The client is trying to negotiate a context and the server requires user-to-user but didn't send a TGT reply.")
   CASE_HRESULT(SEC_E_NO_IP_ADDRESSES                                   , "SEC_E_NO_IP_ADDRESSES"                                      , "Unable to accomplish the requested task because the local machine does not have any IP addresses.")
   CASE_HRESULT(SEC_E_WRONG_CREDENTIAL_HANDLE                           , "SEC_E_WRONG_CREDENTIAL_HANDLE"                              , "The supplied credential handle does not match the credential associated with the security context.")
   CASE_HRESULT(SEC_E_CRYPTO_SYSTEM_INVALID                             , "SEC_E_CRYPTO_SYSTEM_INVALID"                                , "The crypto system or checksum function is invalid because a required function is unavailable.")
   CASE_HRESULT(SEC_E_MAX_REFERRALS_EXCEEDED                            , "SEC_E_MAX_REFERRALS_EXCEEDED"                               , "The number of maximum ticket referrals has been exceeded.")
   CASE_HRESULT(SEC_E_MUST_BE_KDC                                       , "SEC_E_MUST_BE_KDC"                                          , "The local machine must be a Kerberos KDC (domain controller) and it is not.")
   CASE_HRESULT(SEC_E_STRONG_CRYPTO_NOT_SUPPORTED                       , "SEC_E_STRONG_CRYPTO_NOT_SUPPORTED"                          , "The other end of the security negotiation is requires strong crypto but it is not supported on the local machine.")
   CASE_HRESULT(SEC_E_TOO_MANY_PRINCIPALS                               , "SEC_E_TOO_MANY_PRINCIPALS"                                  , "The KDC reply contained more than one principal name.")
   CASE_HRESULT(SEC_E_NO_PA_DATA                                        , "SEC_E_NO_PA_DATA"                                           , "Expected to find PA data for a hint of what etype to use, but it was not found.")
   CASE_HRESULT(SEC_E_PKINIT_NAME_MISMATCH                              , "SEC_E_PKINIT_NAME_MISMATCH"                                 , "The client certificate does not contain a valid UPN, or does not match the client name in the logon request.  Please contact your administrator.")
   CASE_HRESULT(SEC_E_SMARTCARD_LOGON_REQUIRED                          , "SEC_E_SMARTCARD_LOGON_REQUIRED"                             , "Smartcard logon is required and was not used.")
   CASE_HRESULT(SEC_E_SHUTDOWN_IN_PROGRESS                              , "SEC_E_SHUTDOWN_IN_PROGRESS"                                 , "A system shutdown is in progress.")
   CASE_HRESULT(SEC_E_KDC_INVALID_REQUEST                               , "SEC_E_KDC_INVALID_REQUEST"                                  , "An invalid request was sent to the KDC.")
   CASE_HRESULT(SEC_E_KDC_UNABLE_TO_REFER                               , "SEC_E_KDC_UNABLE_TO_REFER"                                  , "The KDC was unable to generate a referral for the service requested.")
   CASE_HRESULT(SEC_E_KDC_UNKNOWN_ETYPE                                 , "SEC_E_KDC_UNKNOWN_ETYPE"                                    , "The encryption type requested is not supported by the KDC.")
   CASE_HRESULT(SEC_E_UNSUPPORTED_PREAUTH                               , "SEC_E_UNSUPPORTED_PREAUTH"                                  , "An unsupported preauthentication mechanism was presented to the kerberos package.")
   CASE_HRESULT(SEC_E_DELEGATION_REQUIRED                               , "SEC_E_DELEGATION_REQUIRED"                                  , "The requested operation cannot be completed.  The computer must be trusted for delegation and the current user account must be configured to allow delegation.")
   CASE_HRESULT(SEC_E_BAD_BINDINGS                                      , "SEC_E_BAD_BINDINGS"                                         , "Client's supplied SSPI channel bindings were incorrect.")
   CASE_HRESULT(SEC_E_MULTIPLE_ACCOUNTS                                 , "SEC_E_MULTIPLE_ACCOUNTS"                                    , "The received certificate was mapped to multiple accounts.")
   CASE_HRESULT(SEC_E_NO_KERB_KEY                                       , "SEC_E_NO_KERB_KEY"                                          , "SEC_E_NO_KERB_KEY")
   CASE_HRESULT(SEC_E_CERT_WRONG_USAGE                                  , "SEC_E_CERT_WRONG_USAGE"                                     , "The certificate is not valid for the requested usage.")
   CASE_HRESULT(SEC_E_DOWNGRADE_DETECTED                                , "SEC_E_DOWNGRADE_DETECTED"                                   , "The system detected a possible attempt to compromise security.  Please ensure that you can contact the server that authenticated you.")
   CASE_HRESULT(SEC_E_SMARTCARD_CERT_REVOKED                            , "SEC_E_SMARTCARD_CERT_REVOKED"                               , "The smartcard certificate used for authentication has been revoked. Please contact your system administrator.  There may be additional information in the event log.")
   CASE_HRESULT(SEC_E_ISSUING_CA_UNTRUSTED                              , "SEC_E_ISSUING_CA_UNTRUSTED"                                 , "An untrusted certificate authority was detected While processing the smartcard certificate used for authentication.  Please contact your system administrator.")
   CASE_HRESULT(SEC_E_REVOCATION_OFFLINE_C                              , "SEC_E_REVOCATION_OFFLINE_C"                                 , "The revocation status of the smartcard certificate used for authentication could not be determined. Please contact your system administrator.")
   CASE_HRESULT(SEC_E_PKINIT_CLIENT_FAILURE                             , "SEC_E_PKINIT_CLIENT_FAILURE"                                , "The smartcard certificate used for authentication was not trusted.  Please contact your system administrator.")
   CASE_HRESULT(SEC_E_SMARTCARD_CERT_EXPIRED                            , "SEC_E_SMARTCARD_CERT_EXPIRED"                               , "The smartcard certificate used for authentication has expired.  Please contact your system administrator.")
   CASE_HRESULT(SEC_E_NO_S4U_PROT_SUPPORT                               , "SEC_E_NO_S4U_PROT_SUPPORT"                                  , "The Kerberos subsystem encountered an error.  A service for user protocol request was made against a domain controller which does not support service for user.")
   CASE_HRESULT(SEC_E_CROSSREALM_DELEGATION_FAILURE                     , "SEC_E_CROSSREALM_DELEGATION_FAILURE"                        , "An attempt was made by this server to make a Kerberos constrained delegation request for a target outside of the server's realm.  This is not supported, and indicates a misconfiguration on this server's allowed to delegate to list.  Please contact your administrator.")
   CASE_HRESULT(SEC_E_REVOCATION_OFFLINE_KDC                            , "SEC_E_REVOCATION_OFFLINE_KDC"                               , "The revocation status of the domain controller certificate used for smartcard authentication could not be determined.  There is additional information in the system event log. Please contact your system administrator.")
   CASE_HRESULT(SEC_E_ISSUING_CA_UNTRUSTED_KDC                          , "SEC_E_ISSUING_CA_UNTRUSTED_KDC"                             , "An untrusted certificate authority was detected while processing the domain controller certificate used for authentication.  There is additional information in the system event log.  Please contact your system administrator.")
   CASE_HRESULT(SEC_E_KDC_CERT_EXPIRED                                  , "SEC_E_KDC_CERT_EXPIRED"                                     , "The domain controller certificate used for smartcard logon has expired. Please contact your system administrator with the contents of your system event log.")
   CASE_HRESULT(SEC_E_KDC_CERT_REVOKED                                  , "SEC_E_KDC_CERT_REVOKED"                                     , "The domain controller certificate used for smartcard logon has been revoked. Please contact your system administrator with the contents of your system event log.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CRYPT_E_MSG_ERROR                                       , "CRYPT_E_MSG_ERROR"                                          , "An error occurred while performing an operation on a cryptographic message.")
   CASE_HRESULT(CRYPT_E_UNKNOWN_ALGO                                    , "CRYPT_E_UNKNOWN_ALGO"                                       , "Unknown cryptographic algorithm.")
   CASE_HRESULT(CRYPT_E_OID_FORMAT                                      , "CRYPT_E_OID_FORMAT"                                         , "The object identifier is poorly formatted.")
   CASE_HRESULT(CRYPT_E_INVALID_MSG_TYPE                                , "CRYPT_E_INVALID_MSG_TYPE"                                   , "Invalid cryptographic message type.")
   CASE_HRESULT(CRYPT_E_UNEXPECTED_ENCODING                             , "CRYPT_E_UNEXPECTED_ENCODING"                                , "Unexpected cryptographic message encoding.")
   CASE_HRESULT(CRYPT_E_AUTH_ATTR_MISSING                               , "CRYPT_E_AUTH_ATTR_MISSING"                                  , "The cryptographic message does not contain an expected authenticated attribute.")
   CASE_HRESULT(CRYPT_E_HASH_VALUE                                      , "CRYPT_E_HASH_VALUE"                                         , "The hash value is not correct.")
   CASE_HRESULT(CRYPT_E_INVALID_INDEX                                   , "CRYPT_E_INVALID_INDEX"                                      , "The index value is not valid.")
   CASE_HRESULT(CRYPT_E_ALREADY_DECRYPTED                               , "CRYPT_E_ALREADY_DECRYPTED"                                  , "The content of the cryptographic message has already been decrypted.")
   CASE_HRESULT(CRYPT_E_NOT_DECRYPTED                                   , "CRYPT_E_NOT_DECRYPTED"                                      , "The content of the cryptographic message has not been decrypted yet.")
   CASE_HRESULT(CRYPT_E_RECIPIENT_NOT_FOUND                             , "CRYPT_E_RECIPIENT_NOT_FOUND"                                , "The enveloped-data message does not contain the specified recipient.")
   CASE_HRESULT(CRYPT_E_CONTROL_TYPE                                    , "CRYPT_E_CONTROL_TYPE"                                       , "Invalid control type.")
   CASE_HRESULT(CRYPT_E_ISSUER_SERIALNUMBER                             , "CRYPT_E_ISSUER_SERIALNUMBER"                                , "Invalid issuer and/or serial number.")
   CASE_HRESULT(CRYPT_E_SIGNER_NOT_FOUND                                , "CRYPT_E_SIGNER_NOT_FOUND"                                   , "Cannot find the original signer.")
   CASE_HRESULT(CRYPT_E_ATTRIBUTES_MISSING                              , "CRYPT_E_ATTRIBUTES_MISSING"                                 , "The cryptographic message does not contain all of the requested attributes.")
   CASE_HRESULT(CRYPT_E_STREAM_MSG_NOT_READY                            , "CRYPT_E_STREAM_MSG_NOT_READY"                               , "The streamed cryptographic message is not ready to return data.")
   CASE_HRESULT(CRYPT_E_STREAM_INSUFFICIENT_DATA                        , "CRYPT_E_STREAM_INSUFFICIENT_DATA"                           , "The streamed cryptographic message requires more data to complete the decode operation.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CRYPT_I_NEW_PROTECTION_REQUIRED                         , "CRYPT_I_NEW_PROTECTION_REQUIRED"                            , "The protected data needs to be re-protected.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CRYPT_E_BAD_LEN                                         , "CRYPT_E_BAD_LEN"                                            , "The length specified for the output data was insufficient.")
   CASE_HRESULT(CRYPT_E_BAD_ENCODE                                      , "CRYPT_E_BAD_ENCODE"                                         , "An error occurred during encode or decode operation.")
   CASE_HRESULT(CRYPT_E_FILE_ERROR                                      , "CRYPT_E_FILE_ERROR"                                         , "An error occurred while reading or writing to a file.")
   CASE_HRESULT(CRYPT_E_NOT_FOUND                                       , "CRYPT_E_NOT_FOUND"                                          , "Cannot find object or property.")
   CASE_HRESULT(CRYPT_E_EXISTS                                          , "CRYPT_E_EXISTS"                                             , "The object or property already exists.")
   CASE_HRESULT(CRYPT_E_NO_PROVIDER                                     , "CRYPT_E_NO_PROVIDER"                                        , "No provider was specified for the store or object.")
   CASE_HRESULT(CRYPT_E_SELF_SIGNED                                     , "CRYPT_E_SELF_SIGNED"                                        , "The specified certificate is self signed.")
   CASE_HRESULT(CRYPT_E_DELETED_PREV                                    , "CRYPT_E_DELETED_PREV"                                       , "The previous certificate or CRL context was deleted.")
   CASE_HRESULT(CRYPT_E_NO_MATCH                                        , "CRYPT_E_NO_MATCH"                                           , "Cannot find the requested object.")
   CASE_HRESULT(CRYPT_E_UNEXPECTED_MSG_TYPE                             , "CRYPT_E_UNEXPECTED_MSG_TYPE"                                , "The certificate does not have a property that references a private key.")
   CASE_HRESULT(CRYPT_E_NO_KEY_PROPERTY                                 , "CRYPT_E_NO_KEY_PROPERTY"                                    , "Cannot find the certificate and private key for decryption.")
   CASE_HRESULT(CRYPT_E_NO_DECRYPT_CERT                                 , "CRYPT_E_NO_DECRYPT_CERT"                                    , "Cannot find the certificate and private key to use for decryption.")
   CASE_HRESULT(CRYPT_E_BAD_MSG                                         , "CRYPT_E_BAD_MSG"                                            , "Not a cryptographic message or the cryptographic message is not formatted correctly.")
   CASE_HRESULT(CRYPT_E_NO_SIGNER                                       , "CRYPT_E_NO_SIGNER"                                          , "The signed cryptographic message does not have a signer for the specified signer index.")
   CASE_HRESULT(CRYPT_E_PENDING_CLOSE                                   , "CRYPT_E_PENDING_CLOSE"                                      , "Final closure is pending until additional frees or closes.")
   CASE_HRESULT(CRYPT_E_REVOKED                                         , "CRYPT_E_REVOKED"                                            , "The certificate is revoked.")
   CASE_HRESULT(CRYPT_E_NO_REVOCATION_DLL                               , "CRYPT_E_NO_REVOCATION_DLL"                                  , "No Dll or exported function was found to verify revocation.")
   CASE_HRESULT(CRYPT_E_NO_REVOCATION_CHECK                             , "CRYPT_E_NO_REVOCATION_CHECK"                                , "The revocation function was unable to check revocation for the certificate.")
   CASE_HRESULT(CRYPT_E_REVOCATION_OFFLINE                              , "CRYPT_E_REVOCATION_OFFLINE"                                 , "The revocation function was unable to check revocation because the revocation server was offline.")
   CASE_HRESULT(CRYPT_E_NOT_IN_REVOCATION_DATABASE                      , "CRYPT_E_NOT_IN_REVOCATION_DATABASE"                         , "The certificate is not in the revocation server's database.")
   CASE_HRESULT(CRYPT_E_INVALID_NUMERIC_STRING                          , "CRYPT_E_INVALID_NUMERIC_STRING"                             , "The string contains a non-numeric character.")
   CASE_HRESULT(CRYPT_E_INVALID_PRINTABLE_STRING                        , "CRYPT_E_INVALID_PRINTABLE_STRING"                           , "The string contains a non-printable character.")
   CASE_HRESULT(CRYPT_E_INVALID_IA5_STRING                              , "CRYPT_E_INVALID_IA5_STRING"                                 , "The string contains a character not in the 7 bit ASCII character set.")
   CASE_HRESULT(CRYPT_E_INVALID_X500_STRING                             , "CRYPT_E_INVALID_X500_STRING"                                , "The string contains an invalid X500 name attribute key, oid, value or delimiter.")
   CASE_HRESULT(CRYPT_E_NOT_CHAR_STRING                                 , "CRYPT_E_NOT_CHAR_STRING"                                    , "The dwValueType for the CERT_NAME_VALUE is not one of the character strings.  Most likely it is either a CERT_RDN_ENCODED_BLOB or CERT_TDN_OCTED_STRING.")
   CASE_HRESULT(CRYPT_E_FILERESIZED                                     , "CRYPT_E_FILERESIZED"                                        , "The Put operation can not continue.  The file needs to be resized.  However, there is already a signature present.  A complete signing operation must be done.")
   CASE_HRESULT(CRYPT_E_SECURITY_SETTINGS                               , "CRYPT_E_SECURITY_SETTINGS"                                  , "The cryptographic operation failed due to a local security option setting.")
   CASE_HRESULT(CRYPT_E_NO_VERIFY_USAGE_DLL                             , "CRYPT_E_NO_VERIFY_USAGE_DLL"                                , "No DLL or exported function was found to verify subject usage.")
   CASE_HRESULT(CRYPT_E_NO_VERIFY_USAGE_CHECK                           , "CRYPT_E_NO_VERIFY_USAGE_CHECK"                              , "The called function was unable to do a usage check on the subject.")
   CASE_HRESULT(CRYPT_E_VERIFY_USAGE_OFFLINE                            , "CRYPT_E_VERIFY_USAGE_OFFLINE"                               , "Since the server was offline, the called function was unable to complete the usage check.")
   CASE_HRESULT(CRYPT_E_NOT_IN_CTL                                      , "CRYPT_E_NOT_IN_CTL"                                         , "The subject was not found in a Certificate Trust List (CTL).")
   CASE_HRESULT(CRYPT_E_NO_TRUSTED_SIGNER                               , "CRYPT_E_NO_TRUSTED_SIGNER"                                  , "None of the signers of the cryptographic message or certificate trust list is trusted.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CRYPT_E_MISSING_PUBKEY_PARA                             , "CRYPT_E_MISSING_PUBKEY_PARA"                                , "The public key's algorithm parameters are missing.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CRYPT_E_OSS_ERROR                                       , "CRYPT_E_OSS_ERROR"                                          , "OSS Certificate encode/decode error code base. See asn1code.h for a definition of the OSS runtime errors. The OSS error values are offset by CRYPT_E_OSS_ERROR.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(OSS_MORE_BUF                                            , "OSS_MORE_BUF"                                               , "OSS ASN.1 Error: Output Buffer is too small.")
   CASE_HRESULT(OSS_NEGATIVE_UINTEGER                                   , "OSS_NEGATIVE_UINTEGER"                                      , "OSS ASN.1 Error: Signed integer is encoded as a unsigned integer.")
   CASE_HRESULT(OSS_PDU_RANGE                                           , "OSS_PDU_RANGE"                                              , "OSS ASN.1 Error: Unknown ASN.1 data type.")
   CASE_HRESULT(OSS_MORE_INPUT                                          , "OSS_MORE_INPUT"                                             , "OSS ASN.1 Error: Output buffer is too small, the decoded data has been truncated.")
   CASE_HRESULT(OSS_DATA_ERROR                                          , "OSS_DATA_ERROR"                                             , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_BAD_ARG                                             , "OSS_BAD_ARG"                                                , "OSS ASN.1 Error: Invalid argument.")
   CASE_HRESULT(OSS_BAD_VERSION                                         , "OSS_BAD_VERSION"                                            , "OSS ASN.1 Error: Encode/Decode version mismatch.")
   CASE_HRESULT(OSS_OUT_MEMORY                                          , "OSS_OUT_MEMORY"                                             , "OSS ASN.1 Error: Out of memory.")
   CASE_HRESULT(OSS_PDU_MISMATCH                                        , "OSS_PDU_MISMATCH"                                           , "OSS ASN.1 Error: Encode/Decode Error.")
   CASE_HRESULT(OSS_LIMITED                                             , "OSS_LIMITED"                                                , "OSS ASN.1 Error: Internal Error.")
   CASE_HRESULT(OSS_BAD_PTR                                             , "OSS_BAD_PTR"                                                , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_BAD_TIME                                            , "OSS_BAD_TIME"                                               , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_INDEFINITE_NOT_SUPPORTED                            , "OSS_INDEFINITE_NOT_SUPPORTED"                               , "OSS ASN.1 Error: Unsupported BER indefinite-length encoding.")
   CASE_HRESULT(OSS_MEM_ERROR                                           , "OSS_MEM_ERROR"                                              , "OSS ASN.1 Error: Access violation.")
   CASE_HRESULT(OSS_BAD_TABLE                                           , "OSS_BAD_TABLE"                                              , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_TOO_LONG                                            , "OSS_TOO_LONG"                                               , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_CONSTRAINT_VIOLATED                                 , "OSS_CONSTRAINT_VIOLATED"                                    , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_FATAL_ERROR                                         , "OSS_FATAL_ERROR"                                            , "OSS ASN.1 Error: Internal Error.")
   CASE_HRESULT(OSS_ACCESS_SERIALIZATION_ERROR                          , "OSS_ACCESS_SERIALIZATION_ERROR"                             , "OSS ASN.1 Error: Multi-threading conflict.")
   CASE_HRESULT(OSS_NULL_TBL                                            , "OSS_NULL_TBL"                                               , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_NULL_FCN                                            , "OSS_NULL_FCN"                                               , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_BAD_ENCRULES                                        , "OSS_BAD_ENCRULES"                                           , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_UNAVAIL_ENCRULES                                    , "OSS_UNAVAIL_ENCRULES"                                       , "OSS ASN.1 Error: Encode/Decode function not implemented.")
   CASE_HRESULT(OSS_CANT_OPEN_TRACE_WINDOW                              , "OSS_CANT_OPEN_TRACE_WINDOW"                                 , "OSS ASN.1 Error: Trace file error.")
   CASE_HRESULT(OSS_UNIMPLEMENTED                                       , "OSS_UNIMPLEMENTED"                                          , "OSS ASN.1 Error: Function not implemented.")
   CASE_HRESULT(OSS_OID_DLL_NOT_LINKED                                  , "OSS_OID_DLL_NOT_LINKED"                                     , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_CANT_OPEN_TRACE_FILE                                , "OSS_CANT_OPEN_TRACE_FILE"                                   , "OSS ASN.1 Error: Trace file error.")
   CASE_HRESULT(OSS_TRACE_FILE_ALREADY_OPEN                             , "OSS_TRACE_FILE_ALREADY_OPEN"                                , "OSS ASN.1 Error: Trace file error.")
   CASE_HRESULT(OSS_TABLE_MISMATCH                                      , "OSS_TABLE_MISMATCH"                                         , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_TYPE_NOT_SUPPORTED                                  , "OSS_TYPE_NOT_SUPPORTED"                                     , "OSS ASN.1 Error: Invalid data.")
   CASE_HRESULT(OSS_REAL_DLL_NOT_LINKED                                 , "OSS_REAL_DLL_NOT_LINKED"                                    , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_REAL_CODE_NOT_LINKED                                , "OSS_REAL_CODE_NOT_LINKED"                                   , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_OUT_OF_RANGE                                        , "OSS_OUT_OF_RANGE"                                           , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_COPIER_DLL_NOT_LINKED                               , "OSS_COPIER_DLL_NOT_LINKED"                                  , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_CONSTRAINT_DLL_NOT_LINKED                           , "OSS_CONSTRAINT_DLL_NOT_LINKED"                              , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_COMPARATOR_DLL_NOT_LINKED                           , "OSS_COMPARATOR_DLL_NOT_LINKED"                              , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_COMPARATOR_CODE_NOT_LINKED                          , "OSS_COMPARATOR_CODE_NOT_LINKED"                             , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_MEM_MGR_DLL_NOT_LINKED                              , "OSS_MEM_MGR_DLL_NOT_LINKED"                                 , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_PDV_DLL_NOT_LINKED                                  , "OSS_PDV_DLL_NOT_LINKED"                                     , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_PDV_CODE_NOT_LINKED                                 , "OSS_PDV_CODE_NOT_LINKED"                                    , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_API_DLL_NOT_LINKED                                  , "OSS_API_DLL_NOT_LINKED"                                     , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_BERDER_DLL_NOT_LINKED                               , "OSS_BERDER_DLL_NOT_LINKED"                                  , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_PER_DLL_NOT_LINKED                                  , "OSS_PER_DLL_NOT_LINKED"                                     , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_OPEN_TYPE_ERROR                                     , "OSS_OPEN_TYPE_ERROR"                                        , "OSS ASN.1 Error: Program link error.")
   CASE_HRESULT(OSS_MUTEX_NOT_CREATED                                   , "OSS_MUTEX_NOT_CREATED"                                      , "OSS ASN.1 Error: System resource error.")
   CASE_HRESULT(OSS_CANT_CLOSE_TRACE_FILE                               , "OSS_CANT_CLOSE_TRACE_FILE"                                  , "OSS ASN.1 Error: Trace file error.")
   CASE_HRESULT(CRYPT_E_ASN1_ERROR                                      , "CRYPT_E_ASN1_ERROR"                                         , "ASN1 Certificate encode/decode error code base. The ASN1 error values are offset by CRYPT_E_ASN1_ERROR.")
   CASE_HRESULT(CRYPT_E_ASN1_INTERNAL                                   , "CRYPT_E_ASN1_INTERNAL"                                      , "ASN1 internal encode or decode error.")
   CASE_HRESULT(CRYPT_E_ASN1_EOD                                        , "CRYPT_E_ASN1_EOD"                                           , "ASN1 unexpected end of data.")
   CASE_HRESULT(CRYPT_E_ASN1_CORRUPT                                    , "CRYPT_E_ASN1_CORRUPT"                                       , "ASN1 corrupted data.")
   CASE_HRESULT(CRYPT_E_ASN1_LARGE                                      , "CRYPT_E_ASN1_LARGE"                                         , "ASN1 value too large.")
   CASE_HRESULT(CRYPT_E_ASN1_CONSTRAINT                                 , "CRYPT_E_ASN1_CONSTRAINT"                                    , "ASN1 constraint violated.")
   CASE_HRESULT(CRYPT_E_ASN1_MEMORY                                     , "CRYPT_E_ASN1_MEMORY"                                        , "ASN1 out of memory.")
   CASE_HRESULT(CRYPT_E_ASN1_OVERFLOW                                   , "CRYPT_E_ASN1_OVERFLOW"                                      , "ASN1 buffer overflow.")
   CASE_HRESULT(CRYPT_E_ASN1_BADPDU                                     , "CRYPT_E_ASN1_BADPDU"                                        , "ASN1 function not supported for this PDU.")
   CASE_HRESULT(CRYPT_E_ASN1_BADARGS                                    , "CRYPT_E_ASN1_BADARGS"                                       , "ASN1 bad arguments to function call.")
   CASE_HRESULT(CRYPT_E_ASN1_BADREAL                                    , "CRYPT_E_ASN1_BADREAL"                                       , "ASN1 bad real value.")
   CASE_HRESULT(CRYPT_E_ASN1_BADTAG                                     , "CRYPT_E_ASN1_BADTAG"                                        , "ASN1 bad tag value met.")
   CASE_HRESULT(CRYPT_E_ASN1_CHOICE                                     , "CRYPT_E_ASN1_CHOICE"                                        , "ASN1 bad choice value.")
   CASE_HRESULT(CRYPT_E_ASN1_RULE                                       , "CRYPT_E_ASN1_RULE"                                          , "ASN1 bad encoding rule.")
   CASE_HRESULT(CRYPT_E_ASN1_UTF8                                       , "CRYPT_E_ASN1_UTF8"                                          , "ASN1 bad unicode (UTF8).")
   CASE_HRESULT(CRYPT_E_ASN1_PDU_TYPE                                   , "CRYPT_E_ASN1_PDU_TYPE"                                      , "ASN1 bad PDU type.")
   CASE_HRESULT(CRYPT_E_ASN1_NYI                                        , "CRYPT_E_ASN1_NYI"                                           , "ASN1 not yet implemented.")
   CASE_HRESULT(CRYPT_E_ASN1_EXTENDED                                   , "CRYPT_E_ASN1_EXTENDED"                                      , "ASN1 skipped unknown extension(s).")
   CASE_HRESULT(CRYPT_E_ASN1_NOEOD                                      , "CRYPT_E_ASN1_NOEOD"                                         , "ASN1 end of data expected")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CERTSRV_E_BAD_REQUESTSUBJECT                            , "CERTSRV_E_BAD_REQUESTSUBJECT"                               , "The request subject name is invalid or too long.")
   CASE_HRESULT(CERTSRV_E_NO_REQUEST                                    , "CERTSRV_E_NO_REQUEST"                                       , "The request does not exist.")
   CASE_HRESULT(CERTSRV_E_BAD_REQUESTSTATUS                             , "CERTSRV_E_BAD_REQUESTSTATUS"                                , "The request's current status does not allow this operation.")
   CASE_HRESULT(CERTSRV_E_PROPERTY_EMPTY                                , "CERTSRV_E_PROPERTY_EMPTY"                                   , "The requested property value is empty.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(CERTSRV_E_INVALID_CA_CERTIFICATE                        , "CERTSRV_E_INVALID_CA_CERTIFICATE"                           , "The certification authority's certificate contains invalid data.")
   CASE_HRESULT(CERTSRV_E_SERVER_SUSPENDED                              , "CERTSRV_E_SERVER_SUSPENDED"                                 , "Certificate service has been suspended for a database restore operation.")
   CASE_HRESULT(CERTSRV_E_ENCODING_LENGTH                               , "CERTSRV_E_ENCODING_LENGTH"                                  , "The certificate contains an encoded length that is potentially incompatible with older enrollment software.")
   CASE_HRESULT(CERTSRV_E_ROLECONFLICT                                  , "CERTSRV_E_ROLECONFLICT"                                     , "The operation is denied. The user has multiple roles assigned and the certification authority is configured to enforce role separation.")
   CASE_HRESULT(CERTSRV_E_RESTRICTEDOFFICER                             , "CERTSRV_E_RESTRICTEDOFFICER"                                , "The operation is denied. It can only be performed by a certificate manager that is allowed to manage certificates for the current requester.")
   CASE_HRESULT(CERTSRV_E_KEY_ARCHIVAL_NOT_CONFIGURED                   , "CERTSRV_E_KEY_ARCHIVAL_NOT_CONFIGURED"                      , "Cannot archive private key.  The certification authority is not configured for key archival.")
   CASE_HRESULT(CERTSRV_E_NO_VALID_KRA                                  , "CERTSRV_E_NO_VALID_KRA"                                     , "Cannot archive private key.  The certification authority could not verify one or more key recovery certificates.")
   CASE_HRESULT(CERTSRV_E_BAD_REQUEST_KEY_ARCHIVAL                      , "CERTSRV_E_BAD_REQUEST_KEY_ARCHIVAL"                         , "The request is incorrectly formatted.  The encrypted private key must be in an unauthenticated attribute in an outermost signature.")
   CASE_HRESULT(CERTSRV_E_NO_CAADMIN_DEFINED                            , "CERTSRV_E_NO_CAADMIN_DEFINED"                               , "At least one security principal must have the permission to manage this CA.")
   CASE_HRESULT(CERTSRV_E_BAD_RENEWAL_CERT_ATTRIBUTE                    , "CERTSRV_E_BAD_RENEWAL_CERT_ATTRIBUTE"                       , "The request contains an invalid renewal certificate attribute.")
   CASE_HRESULT(CERTSRV_E_NO_DB_SESSIONS                                , "CERTSRV_E_NO_DB_SESSIONS"                                   , "An attempt was made to open a Certification Authority database session, but there are already too many active sessions.  The server may need to be configured to allow additional sessions.")
   CASE_HRESULT(CERTSRV_E_ALIGNMENT_FAULT                               , "CERTSRV_E_ALIGNMENT_FAULT"                                  , "A memory reference caused a data alignment fault.")
   CASE_HRESULT(CERTSRV_E_ENROLL_DENIED                                 , "CERTSRV_E_ENROLL_DENIED"                                    , "The permissions on this certification authority do not allow the current user to enroll for certificates.")
   CASE_HRESULT(CERTSRV_E_TEMPLATE_DENIED                               , "CERTSRV_E_TEMPLATE_DENIED"                                  , "The permissions on the certificate template do not allow the current user to enroll for this type of certificate.")
   CASE_HRESULT(CERTSRV_E_DOWNLEVEL_DC_SSL_OR_UPGRADE                   , "CERTSRV_E_DOWNLEVEL_DC_SSL_OR_UPGRADE"                      , "The contacted domain controller cannot support signed LDAP traffic.  Update the domain controller or configure Certificate Services to use SSL for Active Directory access.")
   CASE_HRESULT(CERTSRV_E_UNSUPPORTED_CERT_TYPE                         , "CERTSRV_E_UNSUPPORTED_CERT_TYPE"                            , "The requested certificate template is not supported by this CA.")
   CASE_HRESULT(CERTSRV_E_NO_CERT_TYPE                                  , "CERTSRV_E_NO_CERT_TYPE"                                     , "The request contains no certificate template information.")
   CASE_HRESULT(CERTSRV_E_TEMPLATE_CONFLICT                             , "CERTSRV_E_TEMPLATE_CONFLICT"                                , "The request contains conflicting template information.")
   CASE_HRESULT(CERTSRV_E_SUBJECT_ALT_NAME_REQUIRED                     , "CERTSRV_E_SUBJECT_ALT_NAME_REQUIRED"                        , "The request is missing a required Subject Alternate name extension.")
   CASE_HRESULT(CERTSRV_E_ARCHIVED_KEY_REQUIRED                         , "CERTSRV_E_ARCHIVED_KEY_REQUIRED"                            , "The request is missing a required private key for archival by the server.")
   CASE_HRESULT(CERTSRV_E_SMIME_REQUIRED                                , "CERTSRV_E_SMIME_REQUIRED"                                   , "The request is missing a required SMIME capabilities extension.")
   CASE_HRESULT(CERTSRV_E_BAD_RENEWAL_SUBJECT                           , "CERTSRV_E_BAD_RENEWAL_SUBJECT"                              , "The request was made on behalf of a subject other than the caller.  The certificate template must be configured to require at least one signature to authorize the request.")
   CASE_HRESULT(CERTSRV_E_BAD_TEMPLATE_VERSION                          , "CERTSRV_E_BAD_TEMPLATE_VERSION"                             , "The request template version is newer than the supported template version.")
   CASE_HRESULT(CERTSRV_E_TEMPLATE_POLICY_REQUIRED                      , "CERTSRV_E_TEMPLATE_POLICY_REQUIRED"                         , "The template is missing a required signature policy attribute.")
   CASE_HRESULT(CERTSRV_E_SIGNATURE_POLICY_REQUIRED                     , "CERTSRV_E_SIGNATURE_POLICY_REQUIRED"                        , "The request is missing required signature policy information.")
   CASE_HRESULT(CERTSRV_E_SIGNATURE_COUNT                               , "CERTSRV_E_SIGNATURE_COUNT"                                  , "The request is missing one or more required signatures.")
   CASE_HRESULT(CERTSRV_E_SIGNATURE_REJECTED                            , "CERTSRV_E_SIGNATURE_REJECTED"                               , "One or more signatures did not include the required application or issuance policies.  The request is missing one or more required valid signatures.")
   CASE_HRESULT(CERTSRV_E_ISSUANCE_POLICY_REQUIRED                      , "CERTSRV_E_ISSUANCE_POLICY_REQUIRED"                         , "The request is missing one or more required signature issuance policies.")
   CASE_HRESULT(CERTSRV_E_SUBJECT_UPN_REQUIRED                          , "CERTSRV_E_SUBJECT_UPN_REQUIRED"                             , "The UPN is unavailable and cannot be added to the Subject Alternate name.")
   CASE_HRESULT(CERTSRV_E_SUBJECT_DIRECTORY_GUID_REQUIRED               , "CERTSRV_E_SUBJECT_DIRECTORY_GUID_REQUIRED"                  , "The Active Directory GUID is unavailable and cannot be added to the Subject Alternate name.")
   CASE_HRESULT(CERTSRV_E_SUBJECT_DNS_REQUIRED                          , "CERTSRV_E_SUBJECT_DNS_REQUIRED"                             , "The DNS name is unavailable and cannot be added to the Subject Alternate name.")
   CASE_HRESULT(CERTSRV_E_ARCHIVED_KEY_UNEXPECTED                       , "CERTSRV_E_ARCHIVED_KEY_UNEXPECTED"                          , "The request includes a private key for archival by the server, but key archival is not enabled for the specified certificate template.")
   CASE_HRESULT(CERTSRV_E_KEY_LENGTH                                    , "CERTSRV_E_KEY_LENGTH"                                       , "The public key does not meet the minimum size required by the specified certificate template.")
   CASE_HRESULT(CERTSRV_E_SUBJECT_EMAIL_REQUIRED                        , "CERTSRV_E_SUBJECT_EMAIL_REQUIRED"                           , "The EMail name is unavailable and cannot be added to the Subject or Subject Alternate name.")
   CASE_HRESULT(CERTSRV_E_UNKNOWN_CERT_TYPE                             , "CERTSRV_E_UNKNOWN_CERT_TYPE"                                , "One or more certificate templates to be enabled on this certification authority could not be found.")
   CASE_HRESULT(CERTSRV_E_CERT_TYPE_OVERLAP                             , "CERTSRV_E_CERT_TYPE_OVERLAP"                                , "The certificate template renewal period is longer than the certificate validity period.  The template should be reconfigured or the CA certificate renewed.")
   CASE_HRESULT(XENROLL_E_KEY_NOT_EXPORTABLE                            , "XENROLL_E_KEY_NOT_EXPORTABLE"                               , "The key is not exportable.")
   CASE_HRESULT(XENROLL_E_CANNOT_ADD_ROOT_CERT                          , "XENROLL_E_CANNOT_ADD_ROOT_CERT"                             , "You cannot add the root CA certificate into your local store.")
   CASE_HRESULT(XENROLL_E_RESPONSE_KA_HASH_NOT_FOUND                    , "XENROLL_E_RESPONSE_KA_HASH_NOT_FOUND"                       , "The key archival hash attribute was not found in the response.")
   CASE_HRESULT(XENROLL_E_RESPONSE_UNEXPECTED_KA_HASH                   , "XENROLL_E_RESPONSE_UNEXPECTED_KA_HASH"                      , "An unexpected key archival hash attribute was found in the response.")
   CASE_HRESULT(XENROLL_E_RESPONSE_KA_HASH_MISMATCH                     , "XENROLL_E_RESPONSE_KA_HASH_MISMATCH"                        , "There is a key archival hash mismatch between the request and the response.")
   CASE_HRESULT(XENROLL_E_KEYSPEC_SMIME_MISMATCH                        , "XENROLL_E_KEYSPEC_SMIME_MISMATCH"                           , "Signing certificate cannot include SMIME extension.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(TRUST_E_SYSTEM_ERROR                                    , "TRUST_E_SYSTEM_ERROR"                                       , "A system-level error occurred while verifying trust.")
   CASE_HRESULT(TRUST_E_NO_SIGNER_CERT                                  , "TRUST_E_NO_SIGNER_CERT"                                     , "The certificate for the signer of the message is invalid or not found.")
   CASE_HRESULT(TRUST_E_COUNTER_SIGNER                                  , "TRUST_E_COUNTER_SIGNER"                                     , "One of the counter signatures was invalid.")
   CASE_HRESULT(TRUST_E_CERT_SIGNATURE                                  , "TRUST_E_CERT_SIGNATURE"                                     , "The signature of the certificate can not be verified.")
   CASE_HRESULT(TRUST_E_TIME_STAMP                                      , "TRUST_E_TIME_STAMP"                                         , "The timestamp signature and/or certificate could not be verified or is malformed.")
   CASE_HRESULT(TRUST_E_BAD_DIGEST                                      , "TRUST_E_BAD_DIGEST"                                         , "The digital signature of the object did not verify.")
   CASE_HRESULT(TRUST_E_BASIC_CONSTRAINTS                               , "TRUST_E_BASIC_CONSTRAINTS"                                  , "A certificate's basic constraint extension has not been observed.")
   CASE_HRESULT(TRUST_E_FINANCIAL_CRITERIA                              , "TRUST_E_FINANCIAL_CRITERIA"                                 , "The certificate does not meet or contain the Authenticode(tm) financial extensions.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(MSSIPOTF_E_OUTOFMEMRANGE                                , "MSSIPOTF_E_OUTOFMEMRANGE"                                   , "Tried to reference a part of the file outside the proper range.")
   CASE_HRESULT(MSSIPOTF_E_CANTGETOBJECT                                , "MSSIPOTF_E_CANTGETOBJECT"                                   , "Could not retrieve an object from the file.")
   CASE_HRESULT(MSSIPOTF_E_NOHEADTABLE                                  , "MSSIPOTF_E_NOHEADTABLE"                                     , "Could not find the head table in the file.")
   CASE_HRESULT(MSSIPOTF_E_BAD_MAGICNUMBER                              , "MSSIPOTF_E_BAD_MAGICNUMBER"                                 , "The magic number in the head table is incorrect.")
   CASE_HRESULT(MSSIPOTF_E_BAD_OFFSET_TABLE                             , "MSSIPOTF_E_BAD_OFFSET_TABLE"                                , "The offset table has incorrect values.")
   CASE_HRESULT(MSSIPOTF_E_TABLE_TAGORDER                               , "MSSIPOTF_E_TABLE_TAGORDER"                                  , "Duplicate table tags or tags out of alphabetical order.")
   CASE_HRESULT(MSSIPOTF_E_TABLE_LONGWORD                               , "MSSIPOTF_E_TABLE_LONGWORD"                                  , "A table does not start on a long word boundary.")
   CASE_HRESULT(MSSIPOTF_E_BAD_FIRST_TABLE_PLACEMENT                    , "MSSIPOTF_E_BAD_FIRST_TABLE_PLACEMENT"                       , "First table does not appear after header information.")
   CASE_HRESULT(MSSIPOTF_E_TABLES_OVERLAP                               , "MSSIPOTF_E_TABLES_OVERLAP"                                  , "Two or more tables overlap.")
   CASE_HRESULT(MSSIPOTF_E_TABLE_PADBYTES                               , "MSSIPOTF_E_TABLE_PADBYTES"                                  , "Too many pad bytes between tables or pad bytes are not 0.")
   CASE_HRESULT(MSSIPOTF_E_FILETOOSMALL                                 , "MSSIPOTF_E_FILETOOSMALL"                                    , "File is too small to contain the last table.")
   CASE_HRESULT(MSSIPOTF_E_TABLE_CHECKSUM                               , "MSSIPOTF_E_TABLE_CHECKSUM"                                  , "A table checksum is incorrect.")
   CASE_HRESULT(MSSIPOTF_E_FILE_CHECKSUM                                , "MSSIPOTF_E_FILE_CHECKSUM"                                   , "The file checksum is incorrect.")
   CASE_HRESULT(MSSIPOTF_E_FAILED_POLICY                                , "MSSIPOTF_E_FAILED_POLICY"                                   , "The signature does not have the correct attributes for the policy.")
   CASE_HRESULT(MSSIPOTF_E_FAILED_HINTS_CHECK                           , "MSSIPOTF_E_FAILED_HINTS_CHECK"                              , "The file did not pass the hints check.")
   CASE_HRESULT(MSSIPOTF_E_NOT_OPENTYPE                                 , "MSSIPOTF_E_NOT_OPENTYPE"                                    , "The file is not an OpenType file.")
   CASE_HRESULT(MSSIPOTF_E_FILE                                         , "MSSIPOTF_E_FILE"                                            , "Failed on a file operation (open, map, read, write).")
   CASE_HRESULT(MSSIPOTF_E_CRYPT                                        , "MSSIPOTF_E_CRYPT"                                           , "A call to a CryptoAPI function failed.")
   CASE_HRESULT(MSSIPOTF_E_BADVERSION                                   , "MSSIPOTF_E_BADVERSION"                                      , "There is a bad version number in the file.")
   CASE_HRESULT(MSSIPOTF_E_DSIG_STRUCTURE                               , "MSSIPOTF_E_DSIG_STRUCTURE"                                  , "The structure of the DSIG table is incorrect.")
   CASE_HRESULT(MSSIPOTF_E_PCONST_CHECK                                 , "MSSIPOTF_E_PCONST_CHECK"                                    , "A check failed in a partially constant table.")
   CASE_HRESULT(MSSIPOTF_E_STRUCTURE                                    , "MSSIPOTF_E_STRUCTURE"                                       , "Some kind of structural error.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(TRUST_E_PROVIDER_UNKNOWN                                , "TRUST_E_PROVIDER_UNKNOWN"                                   , "Unknown trust provider.")
   CASE_HRESULT(TRUST_E_ACTION_UNKNOWN                                  , "TRUST_E_ACTION_UNKNOWN"                                     , "The trust verification action specified is not supported by the specified trust provider.")
   CASE_HRESULT(TRUST_E_SUBJECT_FORM_UNKNOWN                            , "TRUST_E_SUBJECT_FORM_UNKNOWN"                               , "The form specified for the subject is not one supported or known by the specified trust provider.")
   CASE_HRESULT(TRUST_E_SUBJECT_NOT_TRUSTED                             , "TRUST_E_SUBJECT_NOT_TRUSTED"                                , "The subject is not trusted for the specified action.")
   CASE_HRESULT(DIGSIG_E_ENCODE                                         , "DIGSIG_E_ENCODE"                                            , "Error due to problem in ASN.1 encoding process.")
   CASE_HRESULT(DIGSIG_E_DECODE                                         , "DIGSIG_E_DECODE"                                            , "Error due to problem in ASN.1 decoding process.")
   CASE_HRESULT(DIGSIG_E_EXTENSIBILITY                                  , "DIGSIG_E_EXTENSIBILITY"                                     , "Reading / writing Extensions where Attributes are appropriate, and visa versa.")
   CASE_HRESULT(DIGSIG_E_CRYPTO                                         , "DIGSIG_E_CRYPTO"                                            , "Unspecified cryptographic failure.")
   CASE_HRESULT(PERSIST_E_SIZEDEFINITE                                  , "PERSIST_E_SIZEDEFINITE"                                     , "The size of the data could not be determined.")
   CASE_HRESULT(PERSIST_E_SIZEINDEFINITE                                , "PERSIST_E_SIZEINDEFINITE"                                   , "The size of the indefinite-sized data could not be determined.")
   CASE_HRESULT(PERSIST_E_NOTSELFSIZING                                 , "PERSIST_E_NOTSELFSIZING"                                    , "This object does not read and write self-sizing data.")
   CASE_HRESULT(TRUST_E_NOSIGNATURE                                     , "TRUST_E_NOSIGNATURE"                                        , "No signature was present in the subject.")
   CASE_HRESULT(CERT_E_EXPIRED                                          , "CERT_E_EXPIRED"                                             , "A required certificate is not within its validity period when verifying against the current system clock or the timestamp in the signed file.")
   CASE_HRESULT(CERT_E_VALIDITYPERIODNESTING                            , "CERT_E_VALIDITYPERIODNESTING"                               , "The validity periods of the certification chain do not nest correctly.")
   CASE_HRESULT(CERT_E_ROLE                                             , "CERT_E_ROLE"                                                , "A certificate that can only be used as an end-entity is being used as a CA or visa versa.")
   CASE_HRESULT(CERT_E_PATHLENCONST                                     , "CERT_E_PATHLENCONST"                                        , "A path length constraint in the certification chain has been violated.")
   CASE_HRESULT(CERT_E_CRITICAL                                         , "CERT_E_CRITICAL"                                            , "A certificate contains an unknown extension that is marked 'critical'.")
   CASE_HRESULT(CERT_E_PURPOSE                                          , "CERT_E_PURPOSE"                                             , "A certificate being used for a purpose other than the ones specified by its CA.")
   CASE_HRESULT(CERT_E_ISSUERCHAINING                                   , "CERT_E_ISSUERCHAINING"                                      , "A parent of a given certificate in fact did not issue that child certificate.")
   CASE_HRESULT(CERT_E_MALFORMED                                        , "CERT_E_MALFORMED"                                           , "A certificate is missing or has an empty value for an important field, such as a subject or issuer name.")
   CASE_HRESULT(CERT_E_UNTRUSTEDROOT                                    , "CERT_E_UNTRUSTEDROOT"                                       , "A certificate chain processed, but terminated in a root certificate which is not trusted by the trust provider.")
   CASE_HRESULT(CERT_E_CHAINING                                         , "CERT_E_CHAINING"                                            , "A certificate chain could not be built to a trusted root authority.")
   CASE_HRESULT(TRUST_E_FAIL                                            , "TRUST_E_FAIL"                                               , "Generic trust failure.")
   CASE_HRESULT(CERT_E_REVOKED                                          , "CERT_E_REVOKED"                                             , "A certificate was explicitly revoked by its issuer.")
   CASE_HRESULT(CERT_E_UNTRUSTEDTESTROOT                                , "CERT_E_UNTRUSTEDTESTROOT"                                   , "The certification path terminates with the test root which is not trusted with the current policy settings.")
   CASE_HRESULT(CERT_E_REVOCATION_FAILURE                               , "CERT_E_REVOCATION_FAILURE"                                  , "The revocation process could not continue - the certificate(s) could not be checked.")
   CASE_HRESULT(CERT_E_CN_NO_MATCH                                      , "CERT_E_CN_NO_MATCH"                                         , "The certificate's CN name does not match the passed value.")
   CASE_HRESULT(CERT_E_WRONG_USAGE                                      , "CERT_E_WRONG_USAGE"                                         , "The certificate is not valid for the requested usage.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(TRUST_E_EXPLICIT_DISTRUST                               , "TRUST_E_EXPLICIT_DISTRUST"                                  , "The certificate was explicitly marked as untrusted by the user.")
   CASE_HRESULT(CERT_E_UNTRUSTEDCA                                      , "CERT_E_UNTRUSTEDCA"                                         , "A certification chain processed correctly, but one of the CA certificates is not trusted by the policy provider.")
   CASE_HRESULT(CERT_E_INVALID_POLICY                                   , "CERT_E_INVALID_POLICY"                                      , "The certificate has invalid policy.")
   CASE_HRESULT(CERT_E_INVALID_NAME                                     , "CERT_E_INVALID_NAME"                                        , "The certificate has an invalid name. The name is not included in the permitted list or is explicitly excluded.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SPAPI_E_EXPECTED_SECTION_NAME                           , "SPAPI_E_EXPECTED_SECTION_NAME"                              , "A non-empty line was encountered in the INF before the start of a section.")
   CASE_HRESULT(SPAPI_E_BAD_SECTION_NAME_LINE                           , "SPAPI_E_BAD_SECTION_NAME_LINE"                              , "A section name marker in the INF is not complete, or does not exist on a line by itself.")
   CASE_HRESULT(SPAPI_E_SECTION_NAME_TOO_LONG                           , "SPAPI_E_SECTION_NAME_TOO_LONG"                              , "An INF section was encountered whose name exceeds the maximum section name length.")
   CASE_HRESULT(SPAPI_E_GENERAL_SYNTAX                                  , "SPAPI_E_GENERAL_SYNTAX"                                     , "The syntax of the INF is invalid.")
   CASE_HRESULT(SPAPI_E_WRONG_INF_STYLE                                 , "SPAPI_E_WRONG_INF_STYLE"                                    , "The style of the INF is different than what was requested.")
   CASE_HRESULT(SPAPI_E_SECTION_NOT_FOUND                               , "SPAPI_E_SECTION_NOT_FOUND"                                  , "The required section was not found in the INF.")
   CASE_HRESULT(SPAPI_E_LINE_NOT_FOUND                                  , "SPAPI_E_LINE_NOT_FOUND"                                     , "The required line was not found in the INF.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SPAPI_E_NO_BACKUP                                       , "SPAPI_E_NO_BACKUP"                                          , "The files affected by the installation of this file queue have not been backed up for uninstall.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SPAPI_E_NO_ASSOCIATED_CLASS                             , "SPAPI_E_NO_ASSOCIATED_CLASS"                                , "The INF or the device information set or element does not have an associated install class.")
   CASE_HRESULT(SPAPI_E_CLASS_MISMATCH                                  , "SPAPI_E_CLASS_MISMATCH"                                     , "The INF or the device information set or element does not match the specified install class.")
   CASE_HRESULT(SPAPI_E_DUPLICATE_FOUND                                 , "SPAPI_E_DUPLICATE_FOUND"                                    , "An existing device was found that is a duplicate of the device being manually installed.")
   CASE_HRESULT(SPAPI_E_NO_DRIVER_SELECTED                              , "SPAPI_E_NO_DRIVER_SELECTED"                                 , "There is no driver selected for the device information set or element.")
   CASE_HRESULT(SPAPI_E_KEY_DOES_NOT_EXIST                              , "SPAPI_E_KEY_DOES_NOT_EXIST"                                 , "The requested device registry key does not exist.")
   CASE_HRESULT(SPAPI_E_INVALID_DEVINST_NAME                            , "SPAPI_E_INVALID_DEVINST_NAME"                               , "The device instance name is invalid.")
   CASE_HRESULT(SPAPI_E_INVALID_CLASS                                   , "SPAPI_E_INVALID_CLASS"                                      , "The install class is not present or is invalid.")
   CASE_HRESULT(SPAPI_E_DEVINST_ALREADY_EXISTS                          , "SPAPI_E_DEVINST_ALREADY_EXISTS"                             , "The device instance cannot be created because it already exists.")
   CASE_HRESULT(SPAPI_E_DEVINFO_NOT_REGISTERED                          , "SPAPI_E_DEVINFO_NOT_REGISTERED"                             , "The operation cannot be performed on a device information element that has not been registered.")
   CASE_HRESULT(SPAPI_E_INVALID_REG_PROPERTY                            , "SPAPI_E_INVALID_REG_PROPERTY"                               , "The device property code is invalid.")
   CASE_HRESULT(SPAPI_E_NO_INF                                          , "SPAPI_E_NO_INF"                                             , "The INF from which a driver list is to be built does not exist.")
   CASE_HRESULT(SPAPI_E_NO_SUCH_DEVINST                                 , "SPAPI_E_NO_SUCH_DEVINST"                                    , "The device instance does not exist in the hardware tree.")
   CASE_HRESULT(SPAPI_E_CANT_LOAD_CLASS_ICON                            , "SPAPI_E_CANT_LOAD_CLASS_ICON"                               , "The icon representing this install class cannot be loaded.")
   CASE_HRESULT(SPAPI_E_INVALID_CLASS_INSTALLER                         , "SPAPI_E_INVALID_CLASS_INSTALLER"                            , "The class installer registry entry is invalid.")
   CASE_HRESULT(SPAPI_E_DI_DO_DEFAULT                                   , "SPAPI_E_DI_DO_DEFAULT"                                      , "The class installer has indicated that the default action should be performed for this installation request.")
   CASE_HRESULT(SPAPI_E_DI_NOFILECOPY                                   , "SPAPI_E_DI_NOFILECOPY"                                      , "The operation does not require any files to be copied.")
   CASE_HRESULT(SPAPI_E_INVALID_HWPROFILE                               , "SPAPI_E_INVALID_HWPROFILE"                                  , "The specified hardware profile does not exist.")
   CASE_HRESULT(SPAPI_E_NO_DEVICE_SELECTED                              , "SPAPI_E_NO_DEVICE_SELECTED"                                 , "There is no device information element currently selected for this device information set.")
   CASE_HRESULT(SPAPI_E_DEVINFO_LIST_LOCKED                             , "SPAPI_E_DEVINFO_LIST_LOCKED"                                , "The operation cannot be performed because the device information set is locked.")
   CASE_HRESULT(SPAPI_E_DEVINFO_DATA_LOCKED                             , "SPAPI_E_DEVINFO_DATA_LOCKED"                                , "The operation cannot be performed because the device information element is locked.")
   CASE_HRESULT(SPAPI_E_DI_BAD_PATH                                     , "SPAPI_E_DI_BAD_PATH"                                        , "The specified path does not contain any applicable device INFs.")
   CASE_HRESULT(SPAPI_E_NO_CLASSINSTALL_PARAMS                          , "SPAPI_E_NO_CLASSINSTALL_PARAMS"                             , "No class installer parameters have been set for the device information set or element.")
   CASE_HRESULT(SPAPI_E_FILEQUEUE_LOCKED                                , "SPAPI_E_FILEQUEUE_LOCKED"                                   , "The operation cannot be performed because the file queue is locked.")
   CASE_HRESULT(SPAPI_E_BAD_SERVICE_INSTALLSECT                         , "SPAPI_E_BAD_SERVICE_INSTALLSECT"                            , "A service installation section in this INF is invalid.")
   CASE_HRESULT(SPAPI_E_NO_CLASS_DRIVER_LIST                            , "SPAPI_E_NO_CLASS_DRIVER_LIST"                               , "There is no class driver list for the device information element.")
   CASE_HRESULT(SPAPI_E_NO_ASSOCIATED_SERVICE                           , "SPAPI_E_NO_ASSOCIATED_SERVICE"                              , "The installation failed because a function driver was not specified for this device instance.")
   CASE_HRESULT(SPAPI_E_NO_DEFAULT_DEVICE_INTERFACE                     , "SPAPI_E_NO_DEFAULT_DEVICE_INTERFACE"                        , "There is presently no default device interface designated for this interface class.")
   CASE_HRESULT(SPAPI_E_DEVICE_INTERFACE_ACTIVE                         , "SPAPI_E_DEVICE_INTERFACE_ACTIVE"                            , "The operation cannot be performed because the device interface is currently active.")
   CASE_HRESULT(SPAPI_E_DEVICE_INTERFACE_REMOVED                        , "SPAPI_E_DEVICE_INTERFACE_REMOVED"                           , "The operation cannot be performed because the device interface has been removed from the system.")
   CASE_HRESULT(SPAPI_E_BAD_INTERFACE_INSTALLSECT                       , "SPAPI_E_BAD_INTERFACE_INSTALLSECT"                          , "An interface installation section in this INF is invalid.")
   CASE_HRESULT(SPAPI_E_NO_SUCH_INTERFACE_CLASS                         , "SPAPI_E_NO_SUCH_INTERFACE_CLASS"                            , "This interface class does not exist in the system.")
   CASE_HRESULT(SPAPI_E_INVALID_REFERENCE_STRING                        , "SPAPI_E_INVALID_REFERENCE_STRING"                           , "The reference string supplied for this interface device is invalid.")
   CASE_HRESULT(SPAPI_E_INVALID_MACHINENAME                             , "SPAPI_E_INVALID_MACHINENAME"                                , "The specified machine name does not conform to UNC naming conventions.")
   CASE_HRESULT(SPAPI_E_REMOTE_COMM_FAILURE                             , "SPAPI_E_REMOTE_COMM_FAILURE"                                , "A general remote communication error occurred.")
   CASE_HRESULT(SPAPI_E_MACHINE_UNAVAILABLE                             , "SPAPI_E_MACHINE_UNAVAILABLE"                                , "The machine selected for remote communication is not available at this time.")
   CASE_HRESULT(SPAPI_E_NO_CONFIGMGR_SERVICES                           , "SPAPI_E_NO_CONFIGMGR_SERVICES"                              , "The Plug and Play service is not available on the remote machine.")
   CASE_HRESULT(SPAPI_E_INVALID_PROPPAGE_PROVIDER                       , "SPAPI_E_INVALID_PROPPAGE_PROVIDER"                          , "The property page provider registry entry is invalid.")
   CASE_HRESULT(SPAPI_E_NO_SUCH_DEVICE_INTERFACE                        , "SPAPI_E_NO_SUCH_DEVICE_INTERFACE"                           , "The requested device interface is not present in the system.")
   CASE_HRESULT(SPAPI_E_DI_POSTPROCESSING_REQUIRED                      , "SPAPI_E_DI_POSTPROCESSING_REQUIRED"                         , "The device's co-installer has additional work to perform after installation is complete.")
   CASE_HRESULT(SPAPI_E_INVALID_COINSTALLER                             , "SPAPI_E_INVALID_COINSTALLER"                                , "The device's co-installer is invalid.")
   CASE_HRESULT(SPAPI_E_NO_COMPAT_DRIVERS                               , "SPAPI_E_NO_COMPAT_DRIVERS"                                  , "There are no compatible drivers for this device.")
   CASE_HRESULT(SPAPI_E_NO_DEVICE_ICON                                  , "SPAPI_E_NO_DEVICE_ICON"                                     , "There is no icon that represents this device or device type.")
   CASE_HRESULT(SPAPI_E_INVALID_INF_LOGCONFIG                           , "SPAPI_E_INVALID_INF_LOGCONFIG"                              , "A logical configuration specified in this INF is invalid.")
   CASE_HRESULT(SPAPI_E_DI_DONT_INSTALL                                 , "SPAPI_E_DI_DONT_INSTALL"                                    , "The class installer has denied the request to install or upgrade this device.")
   CASE_HRESULT(SPAPI_E_INVALID_FILTER_DRIVER                           , "SPAPI_E_INVALID_FILTER_DRIVER"                              , "One of the filter drivers installed for this device is invalid.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SPAPI_E_NON_WINDOWS_NT_DRIVER                           , "SPAPI_E_NON_WINDOWS_NT_DRIVER"                              , "The driver selected for this device does not support Windows XP.")
   CASE_HRESULT(SPAPI_E_NON_WINDOWS_DRIVER                              , "SPAPI_E_NON_WINDOWS_DRIVER"                                 , "The driver selected for this device does not support Windows.")
   CASE_HRESULT(SPAPI_E_NO_CATALOG_FOR_OEM_INF                          , "SPAPI_E_NO_CATALOG_FOR_OEM_INF"                             , "The third-party INF does not contain digital signature information.")
   CASE_HRESULT(SPAPI_E_DEVINSTALL_QUEUE_NONNATIVE                      , "SPAPI_E_DEVINSTALL_QUEUE_NONNATIVE"                         , "An invalid attempt was made to use a device installation file queue for verification of digital signatures relative to other platforms.")
   CASE_HRESULT(SPAPI_E_NOT_DISABLEABLE                                 , "SPAPI_E_NOT_DISABLEABLE"                                    , "The device cannot be disabled.")
   CASE_HRESULT(SPAPI_E_CANT_REMOVE_DEVINST                             , "SPAPI_E_CANT_REMOVE_DEVINST"                                , "The device could not be dynamically removed.")
   CASE_HRESULT(SPAPI_E_INVALID_TARGET                                  , "SPAPI_E_INVALID_TARGET"                                     , "Cannot copy to specified target.")
   CASE_HRESULT(SPAPI_E_DRIVER_NONNATIVE                                , "SPAPI_E_DRIVER_NONNATIVE"                                   , "Driver is not intended for this platform.")
   CASE_HRESULT(SPAPI_E_IN_WOW64                                        , "SPAPI_E_IN_WOW64"                                           , "Operation not allowed in WOW64.")
   CASE_HRESULT(SPAPI_E_SET_SYSTEM_RESTORE_POINT                        , "SPAPI_E_SET_SYSTEM_RESTORE_POINT"                           , "The operation involving unsigned file copying was rolled back, so that a system restore point could be set.")
   CASE_HRESULT(SPAPI_E_INCORRECTLY_COPIED_INF                          , "SPAPI_E_INCORRECTLY_COPIED_INF"                             , "An INF was copied into the Windows INF directory in an improper manner.")
   CASE_HRESULT(SPAPI_E_SCE_DISABLED                                    , "SPAPI_E_SCE_DISABLED"                                       , "The Security Configuration Editor (SCE) APIs have been disabled on this Embedded product.")
   CASE_HRESULT(SPAPI_E_UNKNOWN_EXCEPTION                               , "SPAPI_E_UNKNOWN_EXCEPTION"                                  , "An unknown exception was encountered.")
   CASE_HRESULT(SPAPI_E_PNP_REGISTRY_ERROR                              , "SPAPI_E_PNP_REGISTRY_ERROR"                                 , "A problem was encountered when accessing the Plug and Play registry database.")
   CASE_HRESULT(SPAPI_E_REMOTE_REQUEST_UNSUPPORTED                      , "SPAPI_E_REMOTE_REQUEST_UNSUPPORTED"                         , "The requested operation is not supported for a remote machine.")
   CASE_HRESULT(SPAPI_E_NOT_AN_INSTALLED_OEM_INF                        , "SPAPI_E_NOT_AN_INSTALLED_OEM_INF"                           , "The specified file is not an installed OEM INF.")
   CASE_HRESULT(SPAPI_E_INF_IN_USE_BY_DEVICES                           , "SPAPI_E_INF_IN_USE_BY_DEVICES"                              , "One or more devices are presently installed using the specified INF.")
   CASE_HRESULT(SPAPI_E_DI_FUNCTION_OBSOLETE                            , "SPAPI_E_DI_FUNCTION_OBSOLETE"                               , "The requested device install operation is obsolete.")
   CASE_HRESULT(SPAPI_E_NO_AUTHENTICODE_CATALOG                         , "SPAPI_E_NO_AUTHENTICODE_CATALOG"                            , "A file could not be verified because it does not have an associated catalog signed via Authenticode(tm).")
   CASE_HRESULT(SPAPI_E_AUTHENTICODE_DISALLOWED                         , "SPAPI_E_AUTHENTICODE_DISALLOWED"                            , "Authenticode(tm) signature verification is not supported for the specified INF.")
   CASE_HRESULT(SPAPI_E_AUTHENTICODE_TRUSTED_PUBLISHER                  , "SPAPI_E_AUTHENTICODE_TRUSTED_PUBLISHER"                     , "The INF was signed with an Authenticode(tm) catalog from a trusted publisher.")
   CASE_HRESULT(SPAPI_E_AUTHENTICODE_TRUST_NOT_ESTABLISHED              , "SPAPI_E_AUTHENTICODE_TRUST_NOT_ESTABLISHED"                 , "The publisher of an Authenticode(tm) signed catalog has not yet been established as trusted.")
   CASE_HRESULT(SPAPI_E_AUTHENTICODE_PUBLISHER_NOT_TRUSTED              , "SPAPI_E_AUTHENTICODE_PUBLISHER_NOT_TRUSTED"                 , "The publisher of an Authenticode(tm) signed catalog was not established as trusted.")
   CASE_HRESULT(SPAPI_E_SIGNATURE_OSATTRIBUTE_MISMATCH                  , "SPAPI_E_SIGNATURE_OSATTRIBUTE_MISMATCH"                     , "The software was tested for compliance with Windows Logo requirements on a different version of Windows, and may not be compatible with this version.")
   CASE_HRESULT(SPAPI_E_ONLY_VALIDATE_VIA_AUTHENTICODE                  , "SPAPI_E_ONLY_VALIDATE_VIA_AUTHENTICODE"                     , "The file may only be validated by a catalog signed via Authenticode(tm).")
   CASE_HRESULT(SPAPI_E_UNRECOVERABLE_STACK_OVERFLOW                    , "SPAPI_E_UNRECOVERABLE_STACK_OVERFLOW"                       , "An unrecoverable stack overflow was encountered.")
#endif // MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(SPAPI_E_ERROR_NOT_INSTALLED                             , "SPAPI_E_ERROR_NOT_INSTALLED"                                , "No installed components were detected.")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
 //CASE_HRESULT(SCARD_S_SUCCESS                                         , "SCARD_S_SUCCESS"                                            , "no error")
   CASE_HRESULT(SCARD_F_INTERNAL_ERROR                                  , "SCARD_F_INTERNAL_ERROR"                                     , "An internal consistency check failed.")
   CASE_HRESULT(SCARD_E_CANCELLED                                       , "SCARD_E_CANCELLED"                                          , "The action was cancelled by an SCardCancel request.")
   CASE_HRESULT(SCARD_E_INVALID_HANDLE                                  , "SCARD_E_INVALID_HANDLE"                                     , "The supplied handle was invalid.")
   CASE_HRESULT(SCARD_E_INVALID_PARAMETER                               , "SCARD_E_INVALID_PARAMETER"                                  , "One or more of the supplied parameters could not be properly interpreted.")
   CASE_HRESULT(SCARD_E_INVALID_TARGET                                  , "SCARD_E_INVALID_TARGET"                                     , "Registry startup information is missing or invalid.")
   CASE_HRESULT(SCARD_E_NO_MEMORY                                       , "SCARD_E_NO_MEMORY"                                          , "Not enough memory available to complete this command.")
   CASE_HRESULT(SCARD_F_WAITED_TOO_LONG                                 , "SCARD_F_WAITED_TOO_LONG"                                    , "An internal consistency timer has expired.")
   CASE_HRESULT(SCARD_E_INSUFFICIENT_BUFFER                             , "SCARD_E_INSUFFICIENT_BUFFER"                                , "The data buffer to receive returned data is too small for the returned data.")
   CASE_HRESULT(SCARD_E_UNKNOWN_READER                                  , "SCARD_E_UNKNOWN_READER"                                     , "The specified reader name is not recognized.")
   CASE_HRESULT(SCARD_E_TIMEOUT                                         , "SCARD_E_TIMEOUT"                                            , "The user-specified timeout value has expired.")
   CASE_HRESULT(SCARD_E_SHARING_VIOLATION                               , "SCARD_E_SHARING_VIOLATION"                                  , "The smart card cannot be accessed because of other connections outstanding.")
   CASE_HRESULT(SCARD_E_NO_SMARTCARD                                    , "SCARD_E_NO_SMARTCARD"                                       , "The operation requires a Smart Card, but no Smart Card is currently in the device.")
   CASE_HRESULT(SCARD_E_UNKNOWN_CARD                                    , "SCARD_E_UNKNOWN_CARD"                                       , "The specified smart card name is not recognized.")
   CASE_HRESULT(SCARD_E_CANT_DISPOSE                                    , "SCARD_E_CANT_DISPOSE"                                       , "The system could not dispose of the media in the requested manner.")
   CASE_HRESULT(SCARD_E_PROTO_MISMATCH                                  , "SCARD_E_PROTO_MISMATCH"                                     , "The requested protocols are incompatible with the protocol currently in use with the smart card.")
   CASE_HRESULT(SCARD_E_NOT_READY                                       , "SCARD_E_NOT_READY"                                          , "The reader or smart card is not ready to accept commands.")
   CASE_HRESULT(SCARD_E_INVALID_VALUE                                   , "SCARD_E_INVALID_VALUE"                                      , "One or more of the supplied parameters values could not be properly interpreted.")
   CASE_HRESULT(SCARD_E_SYSTEM_CANCELLED                                , "SCARD_E_SYSTEM_CANCELLED"                                   , "The action was cancelled by the system, presumably to log off or shut down.")
   CASE_HRESULT(SCARD_F_COMM_ERROR                                      , "SCARD_F_COMM_ERROR"                                         , "An internal communications error has been detected.")
   CASE_HRESULT(SCARD_F_UNKNOWN_ERROR                                   , "SCARD_F_UNKNOWN_ERROR"                                      , "An internal error has been detected, but the source is unknown.")
   CASE_HRESULT(SCARD_E_INVALID_ATR                                     , "SCARD_E_INVALID_ATR"                                        , "An ATR obtained from the registry is not a valid ATR string.")
   CASE_HRESULT(SCARD_E_NOT_TRANSACTED                                  , "SCARD_E_NOT_TRANSACTED"                                     , "An attempt was made to end a non-existent transaction.")
   CASE_HRESULT(SCARD_E_READER_UNAVAILABLE                              , "SCARD_E_READER_UNAVAILABLE"                                 , "The specified reader is not currently available for use.")
   CASE_HRESULT(SCARD_P_SHUTDOWN                                        , "SCARD_P_SHUTDOWN"                                           , "The operation has been aborted to allow the server application to exit.")
   CASE_HRESULT(SCARD_E_PCI_TOO_SMALL                                   , "SCARD_E_PCI_TOO_SMALL"                                      , "The PCI Receive buffer was too small.")
   CASE_HRESULT(SCARD_E_READER_UNSUPPORTED                              , "SCARD_E_READER_UNSUPPORTED"                                 , "The reader driver does not meet minimal requirements for support.")
   CASE_HRESULT(SCARD_E_DUPLICATE_READER                                , "SCARD_E_DUPLICATE_READER"                                   , "The reader driver did not produce a unique reader name.")
   CASE_HRESULT(SCARD_E_CARD_UNSUPPORTED                                , "SCARD_E_CARD_UNSUPPORTED"                                   , "The smart card does not meet minimal requirements for support.")
   CASE_HRESULT(SCARD_E_NO_SERVICE                                      , "SCARD_E_NO_SERVICE"                                         , "The Smart card resource manager is not running.")
   CASE_HRESULT(SCARD_E_SERVICE_STOPPED                                 , "SCARD_E_SERVICE_STOPPED"                                    , "The Smart card resource manager has shut down.")
   CASE_HRESULT(SCARD_E_UNEXPECTED                                      , "SCARD_E_UNEXPECTED"                                         , "An unexpected card error has occurred.")
   CASE_HRESULT(SCARD_E_ICC_INSTALLATION                                , "SCARD_E_ICC_INSTALLATION"                                   , "No Primary Provider can be found for the smart card.")
   CASE_HRESULT(SCARD_E_ICC_CREATEORDER                                 , "SCARD_E_ICC_CREATEORDER"                                    , "The requested order of object creation is not supported.")
   CASE_HRESULT(SCARD_E_UNSUPPORTED_FEATURE                             , "SCARD_E_UNSUPPORTED_FEATURE"                                , "This smart card does not support the requested feature.")
   CASE_HRESULT(SCARD_E_DIR_NOT_FOUND                                   , "SCARD_E_DIR_NOT_FOUND"                                      , "The identified directory does not exist in the smart card.")
   CASE_HRESULT(SCARD_E_FILE_NOT_FOUND                                  , "SCARD_E_FILE_NOT_FOUND"                                     , "The identified file does not exist in the smart card.")
   CASE_HRESULT(SCARD_E_NO_DIR                                          , "SCARD_E_NO_DIR"                                             , "The supplied path does not represent a smart card directory.")
   CASE_HRESULT(SCARD_E_NO_FILE                                         , "SCARD_E_NO_FILE"                                            , "The supplied path does not represent a smart card file.")
   CASE_HRESULT(SCARD_E_NO_ACCESS                                       , "SCARD_E_NO_ACCESS"                                          , "Access is denied to this file.")
   CASE_HRESULT(SCARD_E_WRITE_TOO_MANY                                  , "SCARD_E_WRITE_TOO_MANY"                                     , "The smartcard does not have enough memory to store the information.")
   CASE_HRESULT(SCARD_E_BAD_SEEK                                        , "SCARD_E_BAD_SEEK"                                           , "There was an error trying to set the smart card file object pointer.")
   CASE_HRESULT(SCARD_E_INVALID_CHV                                     , "SCARD_E_INVALID_CHV"                                        , "The supplied PIN is incorrect.")
   CASE_HRESULT(SCARD_E_UNKNOWN_RES_MNG                                 , "SCARD_E_UNKNOWN_RES_MNG"                                    , "An unrecognized error code was returned from a layered component.")
   CASE_HRESULT(SCARD_E_NO_SUCH_CERTIFICATE                             , "SCARD_E_NO_SUCH_CERTIFICATE"                                , "The requested certificate does not exist.")
   CASE_HRESULT(SCARD_E_CERTIFICATE_UNAVAILABLE                         , "SCARD_E_CERTIFICATE_UNAVAILABLE"                            , "The requested certificate could not be obtained.")
   CASE_HRESULT(SCARD_E_NO_READERS_AVAILABLE                            , "SCARD_E_NO_READERS_AVAILABLE"                               , "Cannot find a smart card reader.")
   CASE_HRESULT(SCARD_E_COMM_DATA_LOST                                  , "SCARD_E_COMM_DATA_LOST"                                     , "A communications error with the smart card has been detected.  Retry the operation.")
   CASE_HRESULT(SCARD_E_NO_KEY_CONTAINER                                , "SCARD_E_NO_KEY_CONTAINER"                                   , "The requested key container does not exist on the smart card.")
   CASE_HRESULT(SCARD_E_SERVER_TOO_BUSY                                 , "SCARD_E_SERVER_TOO_BUSY"                                    , "The Smart card resource manager is too busy to complete this operation.")
   CASE_HRESULT(SCARD_W_UNSUPPORTED_CARD                                , "SCARD_W_UNSUPPORTED_CARD"                                   , "The reader cannot communicate with the smart card, due to ATR configuration conflicts.")
   CASE_HRESULT(SCARD_W_UNRESPONSIVE_CARD                               , "SCARD_W_UNRESPONSIVE_CARD"                                  , "The smart card is not responding to a reset.")
   CASE_HRESULT(SCARD_W_UNPOWERED_CARD                                  , "SCARD_W_UNPOWERED_CARD"                                     , "Power has been removed from the smart card, so that further communication is not possible.")
   CASE_HRESULT(SCARD_W_RESET_CARD                                      , "SCARD_W_RESET_CARD"                                         , "The smart card has been reset, so any shared state information is invalid.")
   CASE_HRESULT(SCARD_W_REMOVED_CARD                                    , "SCARD_W_REMOVED_CARD"                                       , "The smart card has been removed, so that further communication is not possible.")
   CASE_HRESULT(SCARD_W_SECURITY_VIOLATION                              , "SCARD_W_SECURITY_VIOLATION"                                 , "Access was denied because of a security violation.")
   CASE_HRESULT(SCARD_W_WRONG_CHV                                       , "SCARD_W_WRONG_CHV"                                          , "The card cannot be accessed because the wrong PIN was presented.")
   CASE_HRESULT(SCARD_W_CHV_BLOCKED                                     , "SCARD_W_CHV_BLOCKED"                                        , "The card cannot be accessed because the maximum number of PIN entry attempts has been reached.")
   CASE_HRESULT(SCARD_W_EOF                                             , "SCARD_W_EOF"                                                , "The end of the smart card file has been reached.")
   CASE_HRESULT(SCARD_W_CANCELLED_BY_USER                               , "SCARD_W_CANCELLED_BY_USER"                                  , "The action was cancelled by the user.")
   CASE_HRESULT(SCARD_W_CARD_NOT_AUTHENTICATED                          , "SCARD_W_CARD_NOT_AUTHENTICATED"                             , "No PIN was presented to the smart card.")
   CASE_HRESULT(COMADMIN_E_OBJECTERRORS                                 , "COMADMIN_E_OBJECTERRORS"                                    , "Errors occurred accessing one or more objects - the ErrorInfo collection may have more detail")
   CASE_HRESULT(COMADMIN_E_OBJECTINVALID                                , "COMADMIN_E_OBJECTINVALID"                                   , "One or more of the object's properties are missing or invalid")
   CASE_HRESULT(COMADMIN_E_KEYMISSING                                   , "COMADMIN_E_KEYMISSING"                                      , "The object was not found in the catalog")
   CASE_HRESULT(COMADMIN_E_ALREADYINSTALLED                             , "COMADMIN_E_ALREADYINSTALLED"                                , "The object is already registered")
   CASE_HRESULT(COMADMIN_E_APP_FILE_WRITEFAIL                           , "COMADMIN_E_APP_FILE_WRITEFAIL"                              , "Error occurred writing to the application file")
   CASE_HRESULT(COMADMIN_E_APP_FILE_READFAIL                            , "COMADMIN_E_APP_FILE_READFAIL"                               , "Error occurred reading the application file")
   CASE_HRESULT(COMADMIN_E_APP_FILE_VERSION                             , "COMADMIN_E_APP_FILE_VERSION"                                , "Invalid version number in application file")
   CASE_HRESULT(COMADMIN_E_BADPATH                                      , "COMADMIN_E_BADPATH"                                         , "The file path is invalid")
   CASE_HRESULT(COMADMIN_E_APPLICATIONEXISTS                            , "COMADMIN_E_APPLICATIONEXISTS"                               , "The application is already installed")
   CASE_HRESULT(COMADMIN_E_ROLEEXISTS                                   , "COMADMIN_E_ROLEEXISTS"                                      , "The role already exists")
   CASE_HRESULT(COMADMIN_E_CANTCOPYFILE                                 , "COMADMIN_E_CANTCOPYFILE"                                    , "An error occurred copying the file")
   CASE_HRESULT(COMADMIN_E_NOUSER                                       , "COMADMIN_E_NOUSER"                                          , "One or more users are not valid")
   CASE_HRESULT(COMADMIN_E_INVALIDUSERIDS                               , "COMADMIN_E_INVALIDUSERIDS"                                  , "One or more users in the application file are not valid")
   CASE_HRESULT(COMADMIN_E_NOREGISTRYCLSID                              , "COMADMIN_E_NOREGISTRYCLSID"                                 , "The component's CLSID is missing or corrupt")
   CASE_HRESULT(COMADMIN_E_BADREGISTRYPROGID                            , "COMADMIN_E_BADREGISTRYPROGID"                               , "The component's progID is missing or corrupt")
   CASE_HRESULT(COMADMIN_E_AUTHENTICATIONLEVEL                          , "COMADMIN_E_AUTHENTICATIONLEVEL"                             , "Unable to set required authentication level for update request")
   CASE_HRESULT(COMADMIN_E_USERPASSWDNOTVALID                           , "COMADMIN_E_USERPASSWDNOTVALID"                              , "The identity or password set on the application is not valid")
   CASE_HRESULT(COMADMIN_E_CLSIDORIIDMISMATCH                           , "COMADMIN_E_CLSIDORIIDMISMATCH"                              , "Application file CLSIDs or IIDs do not match corresponding DLLs")
   CASE_HRESULT(COMADMIN_E_REMOTEINTERFACE                              , "COMADMIN_E_REMOTEINTERFACE"                                 , "Interface information is either missing or changed")
   CASE_HRESULT(COMADMIN_E_DLLREGISTERSERVER                            , "COMADMIN_E_DLLREGISTERSERVER"                               , "DllRegisterServer failed on component install")
   CASE_HRESULT(COMADMIN_E_NOSERVERSHARE                                , "COMADMIN_E_NOSERVERSHARE"                                   , "No server file share available")
   CASE_HRESULT(COMADMIN_E_DLLLOADFAILED                                , "COMADMIN_E_DLLLOADFAILED"                                   , "DLL could not be loaded")
   CASE_HRESULT(COMADMIN_E_BADREGISTRYLIBID                             , "COMADMIN_E_BADREGISTRYLIBID"                                , "The registered TypeLib ID is not valid")
   CASE_HRESULT(COMADMIN_E_APPDIRNOTFOUND                               , "COMADMIN_E_APPDIRNOTFOUND"                                  , "Application install directory not found")
   CASE_HRESULT(COMADMIN_E_REGISTRARFAILED                              , "COMADMIN_E_REGISTRARFAILED"                                 , "Errors occurred while in the component registrar")
   CASE_HRESULT(COMADMIN_E_COMPFILE_DOESNOTEXIST                        , "COMADMIN_E_COMPFILE_DOESNOTEXIST"                           , "The file does not exist")
   CASE_HRESULT(COMADMIN_E_COMPFILE_LOADDLLFAIL                         , "COMADMIN_E_COMPFILE_LOADDLLFAIL"                            , "The DLL could not be loaded")
   CASE_HRESULT(COMADMIN_E_COMPFILE_GETCLASSOBJ                         , "COMADMIN_E_COMPFILE_GETCLASSOBJ"                            , "GetClassObject failed in the DLL")
   CASE_HRESULT(COMADMIN_E_COMPFILE_CLASSNOTAVAIL                       , "COMADMIN_E_COMPFILE_CLASSNOTAVAIL"                          , "The DLL does not support the components listed in the TypeLib")
   CASE_HRESULT(COMADMIN_E_COMPFILE_BADTLB                              , "COMADMIN_E_COMPFILE_BADTLB"                                 , "The TypeLib could not be loaded")
   CASE_HRESULT(COMADMIN_E_COMPFILE_NOTINSTALLABLE                      , "COMADMIN_E_COMPFILE_NOTINSTALLABLE"                         , "The file does not contain components or component information")
   CASE_HRESULT(COMADMIN_E_NOTCHANGEABLE                                , "COMADMIN_E_NOTCHANGEABLE"                                   , "Changes to this object and its sub-objects have been disabled")
   CASE_HRESULT(COMADMIN_E_NOTDELETEABLE                                , "COMADMIN_E_NOTDELETEABLE"                                   , "The delete function has been disabled for this object")
   CASE_HRESULT(COMADMIN_E_SESSION                                      , "COMADMIN_E_SESSION"                                         , "The server catalog version is not supported")
   CASE_HRESULT(COMADMIN_E_COMP_MOVE_LOCKED                             , "COMADMIN_E_COMP_MOVE_LOCKED"                                , "The component move was disallowed, because the source or destination application is either a system application or currently locked against changes")
   CASE_HRESULT(COMADMIN_E_COMP_MOVE_BAD_DEST                           , "COMADMIN_E_COMP_MOVE_BAD_DEST"                              , "The component move failed because the destination application no longer exists")
   CASE_HRESULT(COMADMIN_E_REGISTERTLB                                  , "COMADMIN_E_REGISTERTLB"                                     , "The system was unable to register the TypeLib")
   CASE_HRESULT(COMADMIN_E_SYSTEMAPP                                    , "COMADMIN_E_SYSTEMAPP"                                       , "This operation can not be performed on the system application")
   CASE_HRESULT(COMADMIN_E_COMPFILE_NOREGISTRAR                         , "COMADMIN_E_COMPFILE_NOREGISTRAR"                            , "The component registrar referenced in this file is not available")
   CASE_HRESULT(COMADMIN_E_COREQCOMPINSTALLED                           , "COMADMIN_E_COREQCOMPINSTALLED"                              , "A component in the same DLL is already installed")
   CASE_HRESULT(COMADMIN_E_SERVICENOTINSTALLED                          , "COMADMIN_E_SERVICENOTINSTALLED"                             , "The service is not installed")
   CASE_HRESULT(COMADMIN_E_PROPERTYSAVEFAILED                           , "COMADMIN_E_PROPERTYSAVEFAILED"                              , "One or more property settings are either invalid or in conflict with each other")
   CASE_HRESULT(COMADMIN_E_OBJECTEXISTS                                 , "COMADMIN_E_OBJECTEXISTS"                                    , "The object you are attempting to add or rename already exists")
   CASE_HRESULT(COMADMIN_E_COMPONENTEXISTS                              , "COMADMIN_E_COMPONENTEXISTS"                                 , "The component already exists")
   CASE_HRESULT(COMADMIN_E_REGFILE_CORRUPT                              , "COMADMIN_E_REGFILE_CORRUPT"                                 , "The registration file is corrupt")
   CASE_HRESULT(COMADMIN_E_PROPERTY_OVERFLOW                            , "COMADMIN_E_PROPERTY_OVERFLOW"                               , "The property value is too large")
   CASE_HRESULT(COMADMIN_E_NOTINREGISTRY                                , "COMADMIN_E_NOTINREGISTRY"                                   , "Object was not found in registry")
   CASE_HRESULT(COMADMIN_E_OBJECTNOTPOOLABLE                            , "COMADMIN_E_OBJECTNOTPOOLABLE"                               , "This object is not poolable")
   CASE_HRESULT(COMADMIN_E_APPLID_MATCHES_CLSID                         , "COMADMIN_E_APPLID_MATCHES_CLSID"                            , "A CLSID with the same GUID as the new application ID is already installed on this machine")
   CASE_HRESULT(COMADMIN_E_ROLE_DOES_NOT_EXIST                          , "COMADMIN_E_ROLE_DOES_NOT_EXIST"                             , "A role assigned to a component, interface, or method did not exist in the application")
   CASE_HRESULT(COMADMIN_E_START_APP_NEEDS_COMPONENTS                   , "COMADMIN_E_START_APP_NEEDS_COMPONENTS"                      , "You must have components in an application in order to start the application")
   CASE_HRESULT(COMADMIN_E_REQUIRES_DIFFERENT_PLATFORM                  , "COMADMIN_E_REQUIRES_DIFFERENT_PLATFORM"                     , "This operation is not enabled on this platform")
   CASE_HRESULT(COMADMIN_E_CAN_NOT_EXPORT_APP_PROXY                     , "COMADMIN_E_CAN_NOT_EXPORT_APP_PROXY"                        , "Application Proxy is not exportable")
   CASE_HRESULT(COMADMIN_E_CAN_NOT_START_APP                            , "COMADMIN_E_CAN_NOT_START_APP"                               , "Failed to start application because it is either a library application or an application proxy")
   CASE_HRESULT(COMADMIN_E_CAN_NOT_EXPORT_SYS_APP                       , "COMADMIN_E_CAN_NOT_EXPORT_SYS_APP"                          , "System application is not exportable")
   CASE_HRESULT(COMADMIN_E_CANT_SUBSCRIBE_TO_COMPONENT                  , "COMADMIN_E_CANT_SUBSCRIBE_TO_COMPONENT"                     , "Can not subscribe to this component (the component may have been imported)")
   CASE_HRESULT(COMADMIN_E_EVENTCLASS_CANT_BE_SUBSCRIBER                , "COMADMIN_E_EVENTCLASS_CANT_BE_SUBSCRIBER"                   , "An event class cannot also be a subscriber component")
   CASE_HRESULT(COMADMIN_E_LIB_APP_PROXY_INCOMPATIBLE                   , "COMADMIN_E_LIB_APP_PROXY_INCOMPATIBLE"                      , "Library applications and application proxies are incompatible")
   CASE_HRESULT(COMADMIN_E_BASE_PARTITION_ONLY                          , "COMADMIN_E_BASE_PARTITION_ONLY"                             , "This function is valid for the base partition only")
   CASE_HRESULT(COMADMIN_E_START_APP_DISABLED                           , "COMADMIN_E_START_APP_DISABLED"                              , "You cannot start an application that has been disabled")
   CASE_HRESULT(COMADMIN_E_CAT_DUPLICATE_PARTITION_NAME                 , "COMADMIN_E_CAT_DUPLICATE_PARTITION_NAME"                    , "The specified partition name is already in use on this computer")
   CASE_HRESULT(COMADMIN_E_CAT_INVALID_PARTITION_NAME                   , "COMADMIN_E_CAT_INVALID_PARTITION_NAME"                      , "The specified partition name is invalid. Check that the name contains at least one visible character")
   CASE_HRESULT(COMADMIN_E_CAT_PARTITION_IN_USE                         , "COMADMIN_E_CAT_PARTITION_IN_USE"                            , "The partition cannot be deleted because it is the default partition for one or more users")
   CASE_HRESULT(COMADMIN_E_FILE_PARTITION_DUPLICATE_FILES               , "COMADMIN_E_FILE_PARTITION_DUPLICATE_FILES"                  , "The partition cannot be exported, because one or more components in the partition have the same file name")
   CASE_HRESULT(COMADMIN_E_CAT_IMPORTED_COMPONENTS_NOT_ALLOWED          , "COMADMIN_E_CAT_IMPORTED_COMPONENTS_NOT_ALLOWED"             , "Applications that contain one or more imported components cannot be installed into a non-base partition")
   CASE_HRESULT(COMADMIN_E_AMBIGUOUS_APPLICATION_NAME                   , "COMADMIN_E_AMBIGUOUS_APPLICATION_NAME"                      , "The application name is not unique and cannot be resolved to an application id")
   CASE_HRESULT(COMADMIN_E_AMBIGUOUS_PARTITION_NAME                     , "COMADMIN_E_AMBIGUOUS_PARTITION_NAME"                        , "The partition name is not unique and cannot be resolved to a partition id")
   CASE_HRESULT(COMADMIN_E_REGDB_NOTINITIALIZED                         , "COMADMIN_E_REGDB_NOTINITIALIZED"                            , "The COM+ registry database has not been initialized")
   CASE_HRESULT(COMADMIN_E_REGDB_NOTOPEN                                , "COMADMIN_E_REGDB_NOTOPEN"                                   , "The COM+ registry database is not open")
   CASE_HRESULT(COMADMIN_E_REGDB_SYSTEMERR                              , "COMADMIN_E_REGDB_SYSTEMERR"                                 , "The COM+ registry database detected a system error")
   CASE_HRESULT(COMADMIN_E_REGDB_ALREADYRUNNING                         , "COMADMIN_E_REGDB_ALREADYRUNNING"                            , "The COM+ registry database is already running")
   CASE_HRESULT(COMADMIN_E_MIG_VERSIONNOTSUPPORTED                      , "COMADMIN_E_MIG_VERSIONNOTSUPPORTED"                         , "This version of the COM+ registry database cannot be migrated")
   CASE_HRESULT(COMADMIN_E_MIG_SCHEMANOTFOUND                           , "COMADMIN_E_MIG_SCHEMANOTFOUND"                              , "The schema version to be migrated could not be found in the COM+ registry database")
   CASE_HRESULT(COMADMIN_E_CAT_BITNESSMISMATCH                          , "COMADMIN_E_CAT_BITNESSMISMATCH"                             , "There was a type mismatch between binaries")
   CASE_HRESULT(COMADMIN_E_CAT_UNACCEPTABLEBITNESS                      , "COMADMIN_E_CAT_UNACCEPTABLEBITNESS"                         , "A binary of unknown or invalid type was provided")
   CASE_HRESULT(COMADMIN_E_CAT_WRONGAPPBITNESS                          , "COMADMIN_E_CAT_WRONGAPPBITNESS"                             , "There was a type mismatch between a binary and an application")
   CASE_HRESULT(COMADMIN_E_CAT_PAUSE_RESUME_NOT_SUPPORTED               , "COMADMIN_E_CAT_PAUSE_RESUME_NOT_SUPPORTED"                  , "The application cannot be paused or resumed")
   CASE_HRESULT(COMADMIN_E_CAT_SERVERFAULT                              , "COMADMIN_E_CAT_SERVERFAULT"                                 , "The COM+ Catalog Server threw an exception during execution")
   CASE_HRESULT(COMQC_E_APPLICATION_NOT_QUEUED                          , "COMQC_E_APPLICATION_NOT_QUEUED"                             , "Only COM+ Applications marked 'queued' can be invoked using the 'queue' moniker")
   CASE_HRESULT(COMQC_E_NO_QUEUEABLE_INTERFACES                         , "COMQC_E_NO_QUEUEABLE_INTERFACES"                            , "At least one interface must be marked 'queued' in order to create a queued component instance with the 'queue' moniker")
   CASE_HRESULT(COMQC_E_QUEUING_SERVICE_NOT_AVAILABLE                   , "COMQC_E_QUEUING_SERVICE_NOT_AVAILABLE"                      , "MSMQ is required for the requested operation and is not installed")
   CASE_HRESULT(COMQC_E_NO_IPERSISTSTREAM                               , "COMQC_E_NO_IPERSISTSTREAM"                                  , "Unable to marshal an interface that does not support IPersistStream")
   CASE_HRESULT(COMQC_E_BAD_MESSAGE                                     , "COMQC_E_BAD_MESSAGE"                                        , "The message is improperly formatted or was damaged in transit")
   CASE_HRESULT(COMQC_E_UNAUTHENTICATED                                 , "COMQC_E_UNAUTHENTICATED"                                    , "An unauthenticated message was received by an application that accepts only authenticated messages")
   CASE_HRESULT(COMQC_E_UNTRUSTED_ENQUEUER                              , "COMQC_E_UNTRUSTED_ENQUEUER"                                 , "The message was requeued or moved by a user not in the 'QC Trusted User' role")
   CASE_HRESULT(MSDTC_E_DUPLICATE_RESOURCE                              , "MSDTC_E_DUPLICATE_RESOURCE"                                 , "Cannot create a duplicate resource of type Distributed Transaction Coordinator")
   CASE_HRESULT(COMADMIN_E_OBJECT_PARENT_MISSING                        , "COMADMIN_E_OBJECT_PARENT_MISSING"                           , "One of the objects being inserted or updated does not belong to a valid parent collection")
   CASE_HRESULT(COMADMIN_E_OBJECT_DOES_NOT_EXIST                        , "COMADMIN_E_OBJECT_DOES_NOT_EXIST"                           , "One of the specified objects cannot be found")
   CASE_HRESULT(COMADMIN_E_APP_NOT_RUNNING                              , "COMADMIN_E_APP_NOT_RUNNING"                                 , "The specified application is not currently running")
   CASE_HRESULT(COMADMIN_E_INVALID_PARTITION                            , "COMADMIN_E_INVALID_PARTITION"                               , "The partition(s) specified are not valid.")
   CASE_HRESULT(COMADMIN_E_SVCAPP_NOT_POOLABLE_OR_RECYCLABLE            , "COMADMIN_E_SVCAPP_NOT_POOLABLE_OR_RECYCLABLE"               , "COM+ applications that run as NT service may not be pooled or recycled")
   CASE_HRESULT(COMADMIN_E_USER_IN_SET                                  , "COMADMIN_E_USER_IN_SET"                                     , "One or more users are already assigned to a local partition set.")
   CASE_HRESULT(COMADMIN_E_CANTRECYCLELIBRARYAPPS                       , "COMADMIN_E_CANTRECYCLELIBRARYAPPS"                          , "Library applications may not be recycled.")
   CASE_HRESULT(COMADMIN_E_CANTRECYCLESERVICEAPPS                       , "COMADMIN_E_CANTRECYCLESERVICEAPPS"                          , "Applications running as NT services may not be recycled.")
   CASE_HRESULT(COMADMIN_E_PROCESSALREADYRECYCLED                       , "COMADMIN_E_PROCESSALREADYRECYCLED"                          , "The process has already been recycled.")
   CASE_HRESULT(COMADMIN_E_PAUSEDPROCESSMAYNOTBERECYCLED                , "COMADMIN_E_PAUSEDPROCESSMAYNOTBERECYCLED"                   , "A paused process may not be recycled.")
   CASE_HRESULT(COMADMIN_E_CANTMAKEINPROCSERVICE                        , "COMADMIN_E_CANTMAKEINPROCSERVICE"                           , "Library applications may not be NT services.")
   CASE_HRESULT(COMADMIN_E_PROGIDINUSEBYCLSID                           , "COMADMIN_E_PROGIDINUSEBYCLSID"                              , "The ProgID provided to the copy operation is invalid. The ProgID is in use by another registered CLSID.")
   CASE_HRESULT(COMADMIN_E_DEFAULT_PARTITION_NOT_IN_SET                 , "COMADMIN_E_DEFAULT_PARTITION_NOT_IN_SET"                    , "The partition specified as default is not a member of the partition set.")
   CASE_HRESULT(COMADMIN_E_RECYCLEDPROCESSMAYNOTBEPAUSED                , "COMADMIN_E_RECYCLEDPROCESSMAYNOTBEPAUSED"                   , "A recycled process may not be paused.")
   CASE_HRESULT(COMADMIN_E_PARTITION_ACCESSDENIED                       , "COMADMIN_E_PARTITION_ACCESSDENIED"                          , "Access to the specified partition is denied.")
   CASE_HRESULT(COMADMIN_E_PARTITION_MSI_ONLY                           , "COMADMIN_E_PARTITION_MSI_ONLY"                              , "Only Application Files (*.MSI files) can be installed into partitions.")
   CASE_HRESULT(COMADMIN_E_LEGACYCOMPS_NOT_ALLOWED_IN_1_0_FORMAT        , "COMADMIN_E_LEGACYCOMPS_NOT_ALLOWED_IN_1_0_FORMAT"           , "Applications containing one or more legacy components may not be exported to 1.0 format.")
   CASE_HRESULT(COMADMIN_E_LEGACYCOMPS_NOT_ALLOWED_IN_NONBASE_PARTITIONS, "COMADMIN_E_LEGACYCOMPS_NOT_ALLOWED_IN_NONBASE_PARTITIONS"   , "Legacy components may not exist in non-base partitions.")
   CASE_HRESULT(COMADMIN_E_COMP_MOVE_SOURCE                             , "COMADMIN_E_COMP_MOVE_SOURCE"                                , "A component cannot be moved (or copied) from the System Application, an application proxy or a non-changeable application")
   CASE_HRESULT(COMADMIN_E_COMP_MOVE_DEST                               , "COMADMIN_E_COMP_MOVE_DEST"                                  , "A component cannot be moved (or copied) to the System Application, an application proxy or a non-changeable application")
   CASE_HRESULT(COMADMIN_E_COMP_MOVE_PRIVATE                            , "COMADMIN_E_COMP_MOVE_PRIVATE"                               , "A private component cannot be moved (or copied) to a library application or to the base partition")
   CASE_HRESULT(COMADMIN_E_BASEPARTITION_REQUIRED_IN_SET                , "COMADMIN_E_BASEPARTITION_REQUIRED_IN_SET"                   , "The Base Application Partition exists in all partition sets and cannot be removed.")
   CASE_HRESULT(COMADMIN_E_CANNOT_ALIAS_EVENTCLASS                      , "COMADMIN_E_CANNOT_ALIAS_EVENTCLASS"                         , "Alas, Event Class components cannot be aliased.")
   CASE_HRESULT(COMADMIN_E_PRIVATE_ACCESSDENIED                         , "COMADMIN_E_PRIVATE_ACCESSDENIED"                            , "Access is denied because the component is private.")
   CASE_HRESULT(COMADMIN_E_SAFERINVALID                                 , "COMADMIN_E_SAFERINVALID"                                    , "The specified SAFER level is invalid.")
   CASE_HRESULT(COMADMIN_E_REGISTRY_ACCESSDENIED                        , "COMADMIN_E_REGISTRY_ACCESSDENIED"                           , "The specified user cannot write to the system registry")
   CASE_HRESULT(COMADMIN_E_PARTITIONS_DISABLED                          , "COMADMIN_E_PARTITIONS_DISABLED"                             , "COM+ partitions are currently disabled.")
#endif // MICROSOFT_SDK_FEBRUARY_2003

   CASE_HRESULT(INET_E_USE_DEFAULT_PROTOCOLHANDLER                      , "INET_E_USE_DEFAULT_PROTOCOLHANDLER == INET_E_DEFAULT_ACTION", "Use the default protocol handler/action.")
   CASE_HRESULT(INET_E_USE_DEFAULT_SETTING                              , "INET_E_USE_DEFAULT_SETTING"                                 , "Use the default settings.")
   CASE_HRESULT(INET_E_QUERYOPTION_UNKNOWN                              , "INET_E_QUERYOPTION_UNKNOWN"                                 , "The requested option is unknown.")
   CASE_HRESULT(INET_E_REDIRECTING                                      , "INET_E_REDIRECTING == INET_E_REDIRECT_FAILED"               , "The request is being redirected. (Attempt to redirect the navigation failed.)")
   CASE_HRESULT(INET_E_INVALID_URL                                      , "INET_E_INVALID_URL == INET_E_ERROR_FIRST"                   , "The URL could not be parsed. (URL string is not valid.)")
   CASE_HRESULT(INET_E_NO_SESSION                                       , "INET_E_NO_SESSION"                                          , "No Internet session was established. (No session found.)")
   CASE_HRESULT(INET_E_CANNOT_CONNECT                                   , "INET_E_CANNOT_CONNECT"                                      , "The attempt to connect to the Internet has failed. (Unable to connect to server.)")
   CASE_HRESULT(INET_E_RESOURCE_NOT_FOUND                               , "INET_E_RESOURCE_NOT_FOUND"                                  , "The server or proxy was not found. (Requested resource is not found.)")
   CASE_HRESULT(INET_E_OBJECT_NOT_FOUND                                 , "INET_E_OBJECT_NOT_FOUND"                                    , "The object was not found. (Requested object is not found.)")
   CASE_HRESULT(INET_E_DATA_NOT_AVAILABLE                               , "INET_E_DATA_NOT_AVAILABLE"                                  , "An Internet connection was established, but the data cannot be retrieved. (Requested data is not available.)")
   CASE_HRESULT(INET_E_DOWNLOAD_FAILURE                                 , "INET_E_DOWNLOAD_FAILURE"                                    , "The download has failed (the connection was interrupted). (Failure occurred during download.)")
   CASE_HRESULT(INET_E_AUTHENTICATION_REQUIRED                          , "INET_E_AUTHENTICATION_REQUIRED"                             , "Authentication is needed to access the object. (Requested navigation requires authentication.)")
   CASE_HRESULT(INET_E_NO_VALID_MEDIA                                   , "INET_E_NO_VALID_MEDIA"                                      , "The object is not in one of the acceptable Multipurpose Internet Mail Extensions (MIME) types. (Required media not available or valid.)")
   CASE_HRESULT(INET_E_CONNECTION_TIMEOUT                               , "INET_E_CONNECTION_TIMEOUT"                                  , "The Internet connection has timed out. (Connection timed out.)")
   CASE_HRESULT(INET_E_INVALID_REQUEST                                  , "INET_E_INVALID_REQUEST"                                     , "The request was invalid. (Request is invalid.)")
   CASE_HRESULT(INET_E_UNKNOWN_PROTOCOL                                 , "INET_E_UNKNOWN_PROTOCOL"                                    , "The protocol is not known and no pluggable protocols have been entered that match. (Protocol is not recognized.)")
   CASE_HRESULT(INET_E_SECURITY_PROBLEM                                 , "INET_E_SECURITY_PROBLEM"                                    , "A security problem was encountered. (Navigation request has encountered a security issue.)")
   CASE_HRESULT(INET_E_CANNOT_LOAD_DATA                                 , "INET_E_CANNOT_LOAD_DATA"                                    , "The object could not be loaded. (Unable to load data from the server.)")
   CASE_HRESULT(INET_E_CANNOT_INSTANTIATE_OBJECT                        , "INET_E_CANNOT_INSTANTIATE_OBJECT"                           , "CoCreateInstance failed. (Unable to create an instance of the object.)")
   CASE_HRESULT(INET_E_REDIRECT_TO_DIR                                  , "INET_E_REDIRECT_TO_DIR"                                     , "The request is being redirected to a directory. (Navigation redirected to a directory.)")
   CASE_HRESULT(INET_E_CANNOT_LOCK_REQUEST                              , "INET_E_CANNOT_LOCK_REQUEST"                                 , "The requested resource could not be locked. (Unable to lock request with the server.)")
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(INET_E_USE_EXTEND_BINDING                               , "INET_E_USE_EXTEND_BINDING"                                  , "Reissue request with extended binding.")
   CASE_HRESULT(INET_E_TERMINATED_BIND                                  , "INET_E_TERMINATED_BIND"                                     , "Binding is terminated.")
   CASE_HRESULT(INET_E_CODE_DOWNLOAD_DECLINED                           , "INET_E_CODE_DOWNLOAD_DECLINED"                              , "The component download was declined by the user. (Permission to download is declined.)")
   CASE_HRESULT(INET_E_RESULT_DISPATCHED                                , "INET_E_RESULT_DISPATCHED"                                   , "The binding has already been completed and the result has been dispatched, so your abort call has been canceled. (Result is dispatched.)")
   CASE_HRESULT(INET_E_CANNOT_REPLACE_SFP_FILE                          , "INET_E_CANNOT_REPLACE_SFP_FILE == INET_E_ERROR_LAST"        , "The exact version requested by a component download cannot be found. (Cannot replace a protected System File Protection (SFP) file.)")
#endif // MICROSOFT_SDK_FEBRUARY_2003

#ifdef MICROSOFT_SDK_FEBRUARY_2003
   CASE_HRESULT(OLESCRIPT_E_SYNTAX                                      , "OLESCRIPT_E_SYNTAX"                                         , "An unspecified syntax error occurred in the scriptlet/procedure.")
#endif // MICROSOFT_SDK_FEBRUARY_2003

   CASE_HRESULT(MK_S_ASYNCHRONOUS                                       , "MK_S_ASYNCHRONOUS"                                          , "")

   END_CASE_HRESULT

   return strRes;
}

#undef BEGIN_CASE_HRESULT
#undef CASE_HRESULT
#undef END_CASE_HRESULT

/*
ms-help://MS.PSDK.1033/wininet/wininet/http_status_codes.htm
HTTP Status Codes

HTTP_STATUS_BAD_REQUEST       400 Invalid syntax.
HTTP_STATUS_DENIED            401 Access denied.
HTTP_STATUS_PAYMENT_REQ       402 Payment required.
HTTP_STATUS_FORBIDDEN         403 Request forbidden.
HTTP_STATUS_NOT_FOUND         404 Object not found.
HTTP_STATUS_BAD_METHOD        405 Method is not allowed.
HTTP_STATUS_NONE_ACCEPTABLE   406 No response acceptable to client found.
HTTP_STATUS_PROXY_AUTH_REQ    407 Proxy authentication required.
HTTP_STATUS_REQUEST_TIMEOUT   408 Server timed out waiting for request.
HTTP_STATUS_CONFLICT          409 User should resubmit with more info.
HTTP_STATUS_GONE              410 Resource is no longer available.
HTTP_STATUS_LENGTH_REQUIRED   411 Server refused to accept request without a length.
HTTP_STATUS_PRECOND_FAILED    412 Precondition given in request failed.
HTTP_STATUS_REQUEST_TOO_LARGE 413 Request entity was too large.
HTTP_STATUS_URI_TOO_LONG      414 Request Uniform Resource Identifier (URI) too long.
HTTP_STATUS_UNSUPPORTED_MEDIA 415 Unsupported media type.
HTTP_STATUS_RETRY_WITH        449 Retry after doing the appropriate action.
HTTP_STATUS_SERVER_ERROR      500 Internal server error.
HTTP_STATUS_NOT_SUPPORTED     501 Server does not support the functionality required to fulfill the request.
HTTP_STATUS_BAD_GATEWAY       502 Error response received from gateway.
HTTP_STATUS_SERVICE_UNAVAIL   503 Temporarily overloaded.
HTTP_STATUS_GATEWAY_TIMEOUT   504 Timed out waiting for gateway.
HTTP_STATUS_VERSION_NOT_SUP   505 HTTP version not supported.
*/

CString CLogger::QueryInfoFlag_WinInetApi(IN DWORD dwOption) {
   CString strRes;
   if (dwOption == HTTP_QUERY_CUSTOM) {
      strRes = TEXT("HTTP_QUERY_CUSTOM");
      return strRes;
   }
   if (dwOption & HTTP_QUERY_FLAG_SYSTEMTIME     ) { strRes = TEXT("HTTP_QUERY_FLAG_SYSTEMTIME | "     ); dwOption ^= HTTP_QUERY_FLAG_SYSTEMTIME     ;}
   if (dwOption & HTTP_QUERY_FLAG_NUMBER         ) { strRes = TEXT("HTTP_QUERY_FLAG_NUMBER | "         ); dwOption ^= HTTP_QUERY_FLAG_NUMBER         ;}
   if (dwOption & HTTP_QUERY_FLAG_REQUEST_HEADERS) { strRes = TEXT("HTTP_QUERY_FLAG_REQUEST_HEADERS | "); dwOption ^= HTTP_QUERY_FLAG_REQUEST_HEADERS;}
   if (dwOption & HTTP_QUERY_FLAG_COALESCE       ) { strRes = TEXT("HTTP_QUERY_FLAG_COALESCE | "       ); dwOption ^= HTTP_QUERY_FLAG_COALESCE       ;}

   switch (dwOption) {
   case HTTP_QUERY_ACCEPT                   : strRes += TEXT("HTTP_QUERY_ACCEPT"                   ); break;
   case HTTP_QUERY_ACCEPT_CHARSET           : strRes += TEXT("HTTP_QUERY_ACCEPT_CHARSET"           ); break;
   case HTTP_QUERY_ACCEPT_ENCODING          : strRes += TEXT("HTTP_QUERY_ACCEPT_ENCODING"          ); break;
   case HTTP_QUERY_ACCEPT_LANGUAGE          : strRes += TEXT("HTTP_QUERY_ACCEPT_LANGUAGE"          ); break;
   case HTTP_QUERY_ACCEPT_RANGES            : strRes += TEXT("HTTP_QUERY_ACCEPT_RANGES"            ); break;
   case HTTP_QUERY_AGE                      : strRes += TEXT("HTTP_QUERY_AGE"                      ); break;
   case HTTP_QUERY_ALLOW                    : strRes += TEXT("HTTP_QUERY_ALLOW"                    ); break;
   case HTTP_QUERY_AUTHORIZATION            : strRes += TEXT("HTTP_QUERY_AUTHORIZATION"            ); break;
   case HTTP_QUERY_CACHE_CONTROL            : strRes += TEXT("HTTP_QUERY_CACHE_CONTROL"            ); break;
   case HTTP_QUERY_CONNECTION               : strRes += TEXT("HTTP_QUERY_CONNECTION"               ); break;
   case HTTP_QUERY_CONTENT_BASE             : strRes += TEXT("HTTP_QUERY_CONTENT_BASE"             ); break;
   case HTTP_QUERY_CONTENT_DESCRIPTION      : strRes += TEXT("HTTP_QUERY_CONTENT_DESCRIPTION"      ); break;
   case HTTP_QUERY_CONTENT_DISPOSITION      : strRes += TEXT("HTTP_QUERY_CONTENT_DISPOSITION"      ); break;
   case HTTP_QUERY_CONTENT_ENCODING         : strRes += TEXT("HTTP_QUERY_CONTENT_ENCODING"         ); break;
   case HTTP_QUERY_CONTENT_ID               : strRes += TEXT("HTTP_QUERY_CONTENT_ID"               ); break;
   case HTTP_QUERY_CONTENT_LANGUAGE         : strRes += TEXT("HTTP_QUERY_CONTENT_LANGUAGE"         ); break;
   case HTTP_QUERY_CONTENT_LENGTH           : strRes += TEXT("HTTP_QUERY_CONTENT_LENGTH"           ); break;
   case HTTP_QUERY_CONTENT_LOCATION         : strRes += TEXT("HTTP_QUERY_CONTENT_LOCATION"         ); break;
   case HTTP_QUERY_CONTENT_MD5              : strRes += TEXT("HTTP_QUERY_CONTENT_MD5"              ); break;
   case HTTP_QUERY_CONTENT_RANGE            : strRes += TEXT("HTTP_QUERY_CONTENT_RANGE"            ); break;
   case HTTP_QUERY_CONTENT_TRANSFER_ENCODING: strRes += TEXT("HTTP_QUERY_CONTENT_TRANSFER_ENCODING"); break;
   case HTTP_QUERY_CONTENT_TYPE             : strRes += TEXT("HTTP_QUERY_CONTENT_TYPE"             ); break;
   case HTTP_QUERY_COOKIE                   : strRes += TEXT("HTTP_QUERY_COOKIE"                   ); break;
   case HTTP_QUERY_COST                     : strRes += TEXT("HTTP_QUERY_COST"                     ); break;
   case HTTP_QUERY_CUSTOM                   : strRes += TEXT("HTTP_QUERY_CUSTOM"                   ); break;
   case HTTP_QUERY_DATE                     : strRes += TEXT("HTTP_QUERY_DATE"                     ); break;
   case HTTP_QUERY_DERIVED_FROM             : strRes += TEXT("HTTP_QUERY_DERIVED_FROM"             ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_ECHO_HEADERS             : strRes += TEXT("HTTP_QUERY_ECHO_HEADERS"             ); break;
   case HTTP_QUERY_ECHO_HEADERS_CRLF        : strRes += TEXT("HTTP_QUERY_ECHO_HEADERS_CRLF"        ); break;
   case HTTP_QUERY_ECHO_REPLY               : strRes += TEXT("HTTP_QUERY_ECHO_REPLY"               ); break;
   case HTTP_QUERY_ECHO_REQUEST             : strRes += TEXT("HTTP_QUERY_ECHO_REQUEST"             ); break;
   case HTTP_QUERY_EXPECT                   : strRes += TEXT("HTTP_QUERY_EXPECT"                   ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_ETAG                     : strRes += TEXT("HTTP_QUERY_ETAG"                     ); break;
   case HTTP_QUERY_EXPIRES                  : strRes += TEXT("HTTP_QUERY_EXPIRES"                  ); break;
   case HTTP_QUERY_FORWARDED                : strRes += TEXT("HTTP_QUERY_FORWARDED"                ); break;
   case HTTP_QUERY_FROM                     : strRes += TEXT("HTTP_QUERY_FROM"                     ); break;
   case HTTP_QUERY_HOST                     : strRes += TEXT("HTTP_QUERY_HOST"                     ); break;
   case HTTP_QUERY_IF_MATCH                 : strRes += TEXT("HTTP_QUERY_IF_MATCH"                 ); break;
   case HTTP_QUERY_IF_MODIFIED_SINCE        : strRes += TEXT("HTTP_QUERY_IF_MODIFIED_SINCE"        ); break;
   case HTTP_QUERY_IF_NONE_MATCH            : strRes += TEXT("HTTP_QUERY_IF_NONE_MATCH"            ); break;
   case HTTP_QUERY_IF_RANGE                 : strRes += TEXT("HTTP_QUERY_IF_RANGE"                 ); break;
   case HTTP_QUERY_IF_UNMODIFIED_SINCE      : strRes += TEXT("HTTP_QUERY_IF_UNMODIFIED_SINCE"      ); break;
   case HTTP_QUERY_LAST_MODIFIED            : strRes += TEXT("HTTP_QUERY_LAST_MODIFIED"            ); break;
   case HTTP_QUERY_LINK                     : strRes += TEXT("HTTP_QUERY_LINK"                     ); break;
   case HTTP_QUERY_LOCATION                 : strRes += TEXT("HTTP_QUERY_LOCATION"                 ); break;
   case HTTP_QUERY_MAX_FORWARDS             : strRes += TEXT("HTTP_QUERY_MAX_FORWARDS"             ); break;
   case HTTP_QUERY_MESSAGE_ID               : strRes += TEXT("HTTP_QUERY_MESSAGE_ID"               ); break;
   case HTTP_QUERY_MIME_VERSION             : strRes += TEXT("HTTP_QUERY_MIME_VERSION"             ); break;
   case HTTP_QUERY_ORIG_URI                 : strRes += TEXT("HTTP_QUERY_ORIG_URI"                 ); break;
   case HTTP_QUERY_PRAGMA                   : strRes += TEXT("HTTP_QUERY_PRAGMA"                   ); break;
   case HTTP_QUERY_PROXY_AUTHENTICATE       : strRes += TEXT("HTTP_QUERY_PROXY_AUTHENTICATE"       ); break;
   case HTTP_QUERY_PROXY_AUTHORIZATION      : strRes += TEXT("HTTP_QUERY_PROXY_AUTHORIZATION"      ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_PROXY_CONNECTION         : strRes += TEXT("HTTP_QUERY_PROXY_CONNECTION"         ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_PUBLIC                   : strRes += TEXT("HTTP_QUERY_PUBLIC"                   ); break;
   case HTTP_QUERY_RANGE                    : strRes += TEXT("HTTP_QUERY_RANGE"                    ); break;
   case HTTP_QUERY_RAW_HEADERS              : strRes += TEXT("HTTP_QUERY_RAW_HEADERS"              ); break;
   case HTTP_QUERY_RAW_HEADERS_CRLF         : strRes += TEXT("HTTP_QUERY_RAW_HEADERS_CRLF"         ); break;
   case HTTP_QUERY_REFERER                  : strRes += TEXT("HTTP_QUERY_REFERER"                  ); break;
   case HTTP_QUERY_REFRESH                  : strRes += TEXT("HTTP_QUERY_REFRESH"                  ); break;
   case HTTP_QUERY_REQUEST_METHOD           : strRes += TEXT("HTTP_QUERY_REQUEST_METHOD"           ); break;
   case HTTP_QUERY_RETRY_AFTER              : strRes += TEXT("HTTP_QUERY_RETRY_AFTER"              ); break;
   case HTTP_QUERY_SERVER                   : strRes += TEXT("HTTP_QUERY_SERVER"                   ); break;
   case HTTP_QUERY_SET_COOKIE               : strRes += TEXT("HTTP_QUERY_SET_COOKIE"               ); break;
   case HTTP_QUERY_STATUS_CODE              : strRes += TEXT("HTTP_QUERY_STATUS_CODE"              ); break;
   case HTTP_QUERY_STATUS_TEXT              : strRes += TEXT("HTTP_QUERY_STATUS_TEXT"              ); break;
   case HTTP_QUERY_TITLE                    : strRes += TEXT("HTTP_QUERY_TITLE"                    ); break;
   case HTTP_QUERY_TRANSFER_ENCODING        : strRes += TEXT("HTTP_QUERY_TRANSFER_ENCODING"        ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_UNLESS_MODIFIED_SINCE    : strRes += TEXT("HTTP_QUERY_UNLESS_MODIFIED_SINCE"    ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_UPGRADE                  : strRes += TEXT("HTTP_QUERY_UPGRADE"                  ); break;
   case HTTP_QUERY_URI                      : strRes += TEXT("HTTP_QUERY_URI"                      ); break;
   case HTTP_QUERY_USER_AGENT               : strRes += TEXT("HTTP_QUERY_USER_AGENT"               ); break;
   case HTTP_QUERY_VARY                     : strRes += TEXT("HTTP_QUERY_VARY"                     ); break;
   case HTTP_QUERY_VERSION                  : strRes += TEXT("HTTP_QUERY_VERSION"                  ); break;
   case HTTP_QUERY_VIA                      : strRes += TEXT("HTTP_QUERY_VIA"                      ); break;
   case HTTP_QUERY_WARNING                  : strRes += TEXT("HTTP_QUERY_WARNING"                  ); break;
   case HTTP_QUERY_WWW_AUTHENTICATE         : strRes += TEXT("HTTP_QUERY_WWW_AUTHENTICATE"         ); break;
   case HTTP_QUERY_FLAG_COALESCE            : strRes += TEXT("HTTP_QUERY_FLAG_COALESCE"            ); break;
   case HTTP_QUERY_FLAG_NUMBER              : strRes += TEXT("HTTP_QUERY_FLAG_NUMBER"              ); break;
   case HTTP_QUERY_FLAG_REQUEST_HEADERS     : strRes += TEXT("HTTP_QUERY_FLAG_REQUEST_HEADERS"     ); break;
   case HTTP_QUERY_FLAG_SYSTEMTIME          : strRes += TEXT("HTTP_QUERY_FLAG_SYSTEMTIME"          ); break;
   case HTTP_QUERY_MODIFIER_FLAGS_MASK      : strRes += TEXT("HTTP_QUERY_MODIFIER_FLAGS_MASK"      ); break;
   case HTTP_QUERY_HEADER_MASK              : strRes += TEXT("HTTP_QUERY_HEADER_MASK"              ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case HTTP_QUERY_PROXY_SUPPORT            : strRes += TEXT("HTTP_QUERY_PROXY_SUPPORT"            ); break;
   case HTTP_QUERY_PASSPORT_URLS            : strRes += TEXT("HTTP_QUERY_PASSPORT_URLS"            ); break;
   case HTTP_QUERY_PASSPORT_CONFIG          : strRes += TEXT("HTTP_QUERY_PASSPORT_CONFIG"          ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   default:
      {
         CString strTemp; strTemp.Format(TEXT("0x%08x"), dwOption);
         strRes += strTemp;
      }
   }
   return strRes;
}

CString CLogger::QueryOption(IN DWORD dwOption) {
   CString strRes;
   switch (dwOption) {
   case QUERY_EXPIRATION_DATE    : strRes = TEXT("QUERY_EXPIRATION_DATE"    ); break;
   case QUERY_TIME_OF_LAST_CHANGE: strRes = TEXT("QUERY_TIME_OF_LAST_CHANGE"); break;
   case QUERY_CONTENT_ENCODING   : strRes = TEXT("QUERY_CONTENT_ENCODING"   ); break;
   case QUERY_CONTENT_TYPE       : strRes = TEXT("QUERY_CONTENT_TYPE"       ); break;
   case QUERY_REFRESH            : strRes = TEXT("QUERY_REFRESH"            ); break;
   case QUERY_RECOMBINE          : strRes = TEXT("QUERY_RECOMBINE"          ); break;
   case QUERY_CAN_NAVIGATE       : strRes = TEXT("QUERY_CAN_NAVIGATE"       ); break;
   case QUERY_USES_NETWORK       : strRes = TEXT("QUERY_USES_NETWORK"       ); break;
   case QUERY_IS_CACHED          : strRes = TEXT("QUERY_IS_CACHED"          ); break;
   case QUERY_IS_INSTALLEDENTRY  : strRes = TEXT("QUERY_IS_INSTALLEDENTRY"  ); break;
   case QUERY_IS_CACHED_OR_MAPPED: strRes = TEXT("QUERY_IS_CACHED_OR_MAPPED"); break;
   case QUERY_USES_CACHE         : strRes = TEXT("QUERY_USES_CACHE"         ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case QUERY_IS_SECURE          : strRes = TEXT("QUERY_IS_SECURE"          ); break;
   case QUERY_IS_SAFE            : strRes = TEXT("QUERY_IS_SAFE"            ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   default                       : strRes.Format(TEXT("0x%08x"), dwOption);
   }
   return strRes;
}

__forceinline CString CLogger::SystemTime(const SYSTEMTIME &st, bool bDate, bool bTime) {
   CString strRes;
   if (bDate || bTime) {
      CString strDate, strTime;
      if (bDate) {
         strDate.Format(TEXT("%04i.%02i.%02i"), st.wYear,st.wMonth,st.wDay);
      }
      if (bTime) {
         strTime.Format(TEXT("%02i:%02i:%02i,%03i"), st.wHour,st.wMinute,st.wSecond,st.wMilliseconds);
      }
      strRes = strDate;
      if (bDate && bTime) {
         strRes += TEXT(' ');
      }
      strRes += strTime;
   }
   return strRes;
}

CString CLogger::BindFlag(IN DWORD dwBINDF) {
   CString strRes;
   strRes.Empty();
   if (dwBINDF & BINDF_ASYNCHRONOUS            ) { strRes += TEXT(" | BINDF_ASYNCHRONOUS"            );}
   if (dwBINDF & BINDF_ASYNCSTORAGE            ) { strRes += TEXT(" | BINDF_ASYNCSTORAGE"            );}
   if (dwBINDF & BINDF_NOPROGRESSIVERENDERING  ) { strRes += TEXT(" | BINDF_NOPROGRESSIVERENDERING"  );}
   if (dwBINDF & BINDF_OFFLINEOPERATION        ) { strRes += TEXT(" | BINDF_OFFLINEOPERATION"        );}
   if (dwBINDF & BINDF_GETNEWESTVERSION        ) { strRes += TEXT(" | BINDF_GETNEWESTVERSION"        );}
   if (dwBINDF & BINDF_NOWRITECACHE            ) { strRes += TEXT(" | BINDF_NOWRITECACHE"            );}
   if (dwBINDF & BINDF_NEEDFILE                ) { strRes += TEXT(" | BINDF_NEEDFILE"                );}
   if (dwBINDF & BINDF_PULLDATA                ) { strRes += TEXT(" | BINDF_PULLDATA"                );}
   if (dwBINDF & BINDF_IGNORESECURITYPROBLEM   ) { strRes += TEXT(" | BINDF_IGNORESECURITYPROBLEM"   );}
   if (dwBINDF & BINDF_RESYNCHRONIZE           ) { strRes += TEXT(" | BINDF_RESYNCHRONIZE"           );}
   if (dwBINDF & BINDF_HYPERLINK               ) { strRes += TEXT(" | BINDF_HYPERLINK"               );}
   if (dwBINDF & BINDF_NO_UI                   ) { strRes += TEXT(" | BINDF_NO_UI"                   );}
   if (dwBINDF & BINDF_SILENTOPERATION         ) { strRes += TEXT(" | BINDF_SILENTOPERATION"         );}
   if (dwBINDF & BINDF_PRAGMA_NO_CACHE         ) { strRes += TEXT(" | BINDF_PRAGMA_NO_CACHE"         );}
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   if (dwBINDF & BINDF_GETCLASSOBJECT          ) { strRes += TEXT(" | BINDF_GETCLASSOBJECT"          );}
   if (dwBINDF & BINDF_RESERVED_1              ) { strRes += TEXT(" | BINDF_RESERVED_1"              );}
#endif // MICROSOFT_SDK_FEBRUARY_2003
   if (dwBINDF & BINDF_FREE_THREADED           ) { strRes += TEXT(" | BINDF_FREE_THREADED"           );}
   if (dwBINDF & BINDF_DIRECT_READ             ) { strRes += TEXT(" | BINDF_DIRECT_READ"             );}
   if (dwBINDF & BINDF_FORMS_SUBMIT            ) { strRes += TEXT(" | BINDF_FORMS_SUBMIT"            );}
   if (dwBINDF & BINDF_GETFROMCACHE_IF_NET_FAIL) { strRes += TEXT(" | BINDF_GETFROMCACHE_IF_NET_FAIL");}
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   if (dwBINDF & BINDF_FROMURLMON              ) { strRes += TEXT(" | BINDF_FROMURLMON"              );}
   if (dwBINDF & BINDF_FWD_BACK                ) { strRes += TEXT(" | BINDF_FWD_BACK"                );}
   if (dwBINDF & BINDF_PREFERDEFAULTHANDLER    ) { strRes += TEXT(" | BINDF_PREFERDEFAULTHANDLER"    );}
   if (dwBINDF & BINDF_ENFORCERESTRICTED       ) { strRes += TEXT(" | BINDF_ENFORCERESTRICTED"       );}
#endif // MICROSOFT_SDK_FEBRUARY_2003
   strRes = (LPCTSTR)strRes+3;
   return strRes;
}

CString CLogger::BindString(IN ULONG ulStringType) {
   CString strRes;
   strRes.Empty();
   switch (ulStringType) {
   case BINDSTRING_HEADERS            : strRes = TEXT("BINDSTRING_HEADERS"            ); break;
   case BINDSTRING_ACCEPT_MIMES       : strRes = TEXT("BINDSTRING_ACCEPT_MIMES"       ); break;
   case BINDSTRING_EXTRA_URL          : strRes = TEXT("BINDSTRING_EXTRA_URL"          ); break;
   case BINDSTRING_LANGUAGE           : strRes = TEXT("BINDSTRING_LANGUAGE"           ); break;
   case BINDSTRING_USERNAME           : strRes = TEXT("BINDSTRING_USERNAME"           ); break;
   case BINDSTRING_PASSWORD           : strRes = TEXT("BINDSTRING_PASSWORD"           ); break;
   case BINDSTRING_UA_PIXELS          : strRes = TEXT("BINDSTRING_UA_PIXELS"          ); break;
   case BINDSTRING_UA_COLOR           : strRes = TEXT("BINDSTRING_UA_COLOR"           ); break;
   case BINDSTRING_OS                 : strRes = TEXT("BINDSTRING_OS"                 ); break;
   case BINDSTRING_USER_AGENT         : strRes = TEXT("BINDSTRING_USER_AGENT"         ); break;
   case BINDSTRING_ACCEPT_ENCODINGS   : strRes = TEXT("BINDSTRING_ACCEPT_ENCODINGS"   ); break;
   case BINDSTRING_POST_COOKIE        : strRes = TEXT("BINDSTRING_POST_COOKIE"        ); break;
   case BINDSTRING_POST_DATA_MIME     : strRes = TEXT("BINDSTRING_POST_DATA_MIME"     ); break;
   case BINDSTRING_URL                : strRes = TEXT("BINDSTRING_URL"                ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case BINDSTRING_IID                : strRes = TEXT("BINDSTRING_IID"                ); break;
   case BINDSTRING_FLAG_BIND_TO_OBJECT: strRes = TEXT("BINDSTRING_FLAG_BIND_TO_OBJECT"); break;
   case BINDSTRING_PTR_BIND_CONTEXT   : strRes = TEXT("BINDSTRING_PTR_BIND_CONTEXT"   ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   default                            : strRes.Format(TEXT("0x%08x"), ulStringType);
   }
   return strRes;
}

CString CLogger::BindStatusCallbackFlag(IN WORD grfBSCF) {
   CString strRes;
   if (grfBSCF & BSCF_FIRSTDATANOTIFICATION       ) { strRes += TEXT(" | BSCF_FIRSTDATANOTIFICATION"       );}
   if (grfBSCF & BSCF_INTERMEDIATEDATANOTIFICATION) { strRes += TEXT(" | BSCF_INTERMEDIATEDATANOTIFICATION");}
   if (grfBSCF & BSCF_LASTDATANOTIFICATION        ) { strRes += TEXT(" | BSCF_LASTDATANOTIFICATION"        );}
   if (grfBSCF & BSCF_DATAFULLYAVAILABLE          ) { strRes += TEXT(" | BSCF_DATAFULLYAVAILABLE"          );}
   if (grfBSCF & BSCF_AVAILABLEDATASIZEUNKNOWN    ) { strRes += TEXT(" | BSCF_AVAILABLEDATASIZEUNKNOWN"    );}
   strRes = (LPCTSTR)strRes+3;
   return strRes;
}

CString CLogger::BindStatus(IN ULONG ulStatusCode) {
   CString strRes;
   switch (ulStatusCode) {
   case BINDSTATUS_FINDINGRESOURCE           : strRes = TEXT("BINDSTATUS_FINDINGRESOURCE"           ); break;
   case BINDSTATUS_CONNECTING                : strRes = TEXT("BINDSTATUS_CONNECTING"                ); break;
   case BINDSTATUS_REDIRECTING               : strRes = TEXT("BINDSTATUS_REDIRECTING"               ); break;
   case BINDSTATUS_BEGINDOWNLOADDATA         : strRes = TEXT("BINDSTATUS_BEGINDOWNLOADDATA"         ); break;
   case BINDSTATUS_DOWNLOADINGDATA           : strRes = TEXT("BINDSTATUS_DOWNLOADINGDATA"           ); break;
   case BINDSTATUS_ENDDOWNLOADDATA           : strRes = TEXT("BINDSTATUS_ENDDOWNLOADDATA"           ); break;
   case BINDSTATUS_BEGINDOWNLOADCOMPONENTS   : strRes = TEXT("BINDSTATUS_BEGINDOWNLOADCOMPONENTS"   ); break;
   case BINDSTATUS_INSTALLINGCOMPONENTS      : strRes = TEXT("BINDSTATUS_INSTALLINGCOMPONENTS"      ); break;
   case BINDSTATUS_ENDDOWNLOADCOMPONENTS     : strRes = TEXT("BINDSTATUS_ENDDOWNLOADCOMPONENTS"     ); break;
   case BINDSTATUS_USINGCACHEDCOPY           : strRes = TEXT("BINDSTATUS_USINGCACHEDCOPY"           ); break;
   case BINDSTATUS_SENDINGREQUEST            : strRes = TEXT("BINDSTATUS_SENDINGREQUEST"            ); break;
   case BINDSTATUS_CLASSIDAVAILABLE          : strRes = TEXT("BINDSTATUS_CLASSIDAVAILABLE"          ); break;
   case BINDSTATUS_MIMETYPEAVAILABLE         : strRes = TEXT("BINDSTATUS_MIMETYPEAVAILABLE"         ); break;
   case BINDSTATUS_CACHEFILENAMEAVAILABLE    : strRes = TEXT("BINDSTATUS_CACHEFILENAMEAVAILABLE"    ); break;
   case BINDSTATUS_BEGINSYNCOPERATION        : strRes = TEXT("BINDSTATUS_BEGINSYNCOPERATION"        ); break;
   case BINDSTATUS_ENDSYNCOPERATION          : strRes = TEXT("BINDSTATUS_ENDSYNCOPERATION"          ); break;
   case BINDSTATUS_BEGINUPLOADDATA           : strRes = TEXT("BINDSTATUS_BEGINUPLOADDATA"           ); break;
   case BINDSTATUS_UPLOADINGDATA             : strRes = TEXT("BINDSTATUS_UPLOADINGDATA"             ); break;
   case BINDSTATUS_ENDUPLOADDATA             : strRes = TEXT("BINDSTATUS_ENDUPLOADDATA"             ); break;
   case BINDSTATUS_PROTOCOLCLASSID           : strRes = TEXT("BINDSTATUS_PROTOCOLCLASSID"           ); break;
   case BINDSTATUS_ENCODING                  : strRes = TEXT("BINDSTATUS_ENCODING"                  ); break;
   case BINDSTATUS_VERIFIEDMIMETYPEAVAILABLE : strRes = TEXT("BINDSTATUS_VERIFIEDMIMETYPEAVAILABLE" ); break;
   case BINDSTATUS_CLASSINSTALLLOCATION      : strRes = TEXT("BINDSTATUS_CLASSINSTALLLOCATION"      ); break;
   case BINDSTATUS_DECODING                  : strRes = TEXT("BINDSTATUS_DECODING"                  ); break;
   case BINDSTATUS_LOADINGMIMEHANDLER        : strRes = TEXT("BINDSTATUS_LOADINGMIMEHANDLER"        ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case BINDSTATUS_CONTENTDISPOSITIONATTACH  : strRes = TEXT("BINDSTATUS_CONTENTDISPOSITIONATTACH"  ); break;
   case BINDSTATUS_FILTERREPORTMIMETYPE      : strRes = TEXT("BINDSTATUS_FILTERREPORTMIMETYPE"      ); break;
   case BINDSTATUS_CLSIDCANINSTANTIATE       : strRes = TEXT("BINDSTATUS_CLSIDCANINSTANTIATE"       ); break;
   case BINDSTATUS_IUNKNOWNAVAILABLE         : strRes = TEXT("BINDSTATUS_IUNKNOWNAVAILABLE"         ); break;
   case BINDSTATUS_DIRECTBIND                : strRes = TEXT("BINDSTATUS_DIRECTBIND"                ); break;
   case BINDSTATUS_RAWMIMETYPE               : strRes = TEXT("BINDSTATUS_RAWMIMETYPE"               ); break;
   case BINDSTATUS_PROXYDETECTING            : strRes = TEXT("BINDSTATUS_PROXYDETECTING"            ); break;
   case BINDSTATUS_ACCEPTRANGES              : strRes = TEXT("BINDSTATUS_ACCEPTRANGES"              ); break;
   case BINDSTATUS_COOKIE_SENT               : strRes = TEXT("BINDSTATUS_COOKIE_SENT"               ); break;
   case BINDSTATUS_COMPACT_POLICY_RECEIVED   : strRes = TEXT("BINDSTATUS_COMPACT_POLICY_RECEIVED"   ); break;
   case BINDSTATUS_COOKIE_SUPPRESSED         : strRes = TEXT("BINDSTATUS_COOKIE_SUPPRESSED"         ); break;
   case BINDSTATUS_COOKIE_STATE_UNKNOWN      : strRes = TEXT("BINDSTATUS_COOKIE_STATE_UNKNOWN"      ); break;
   case BINDSTATUS_COOKIE_STATE_ACCEPT       : strRes = TEXT("BINDSTATUS_COOKIE_STATE_ACCEPT"       ); break;
   case BINDSTATUS_COOKIE_STATE_REJECT       : strRes = TEXT("BINDSTATUS_COOKIE_STATE_REJECT"       ); break;
   case BINDSTATUS_COOKIE_STATE_PROMPT       : strRes = TEXT("BINDSTATUS_COOKIE_STATE_PROMPT"       ); break;
   case BINDSTATUS_COOKIE_STATE_LEASH        : strRes = TEXT("BINDSTATUS_COOKIE_STATE_LEASH"        ); break;
   case BINDSTATUS_COOKIE_STATE_DOWNGRADE    : strRes = TEXT("BINDSTATUS_COOKIE_STATE_DOWNGRADE"    ); break;
   case BINDSTATUS_POLICY_HREF               : strRes = TEXT("BINDSTATUS_POLICY_HREF"               ); break;
   case BINDSTATUS_P3P_HEADER                : strRes = TEXT("BINDSTATUS_P3P_HEADER"                ); break;
   case BINDSTATUS_SESSION_COOKIE_RECEIVED   : strRes = TEXT("BINDSTATUS_SESSION_COOKIE_RECEIVED"   ); break;
   case BINDSTATUS_PERSISTENT_COOKIE_RECEIVED: strRes = TEXT("BINDSTATUS_PERSISTENT_COOKIE_RECEIVED"); break;
   case BINDSTATUS_SESSION_COOKIES_ALLOWED   : strRes = TEXT("BINDSTATUS_SESSION_COOKIES_ALLOWED"   ); break;
   case BINDSTATUS_CACHECONTROL              : strRes = TEXT("BINDSTATUS_CACHECONTROL"              ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   default                                   : strRes.Format(TEXT("0x%08x"), ulStatusCode);
   }
   return strRes;
}

CString CLogger::StringArray(IN LPCOLESTR const *const ppwzStr, IN ULONG uSize, CString strLeft, CString strRight, CString strSeparator) {
   CString strRes;
   for (ULONG i=0; i<uSize; i++) {
      strRes += strSeparator;
      strRes += strLeft;
      strRes += ppwzStr[i];
      strRes += strRight;
   }
   if (strRes.GetLength()) {
      strRes = (LPCTSTR)strRes + strSeparator.GetLength();
   }
   return strRes;
}

CString CLogger::InternetStatusCallback(DWORD dwInternetStatus, eInfoExt info) {
   CString strRes;
   if (info == info_WinInet) {
      switch (dwInternetStatus) {
      case INTERNET_STATUS_RESOLVING_NAME               : strRes = TEXT("INTERNET_STATUS_RESOLVING_NAME"               ); break;
      case INTERNET_STATUS_NAME_RESOLVED                : strRes = TEXT("INTERNET_STATUS_NAME_RESOLVED"                ); break;
      case INTERNET_STATUS_CONNECTING_TO_SERVER         : strRes = TEXT("INTERNET_STATUS_CONNECTING_TO_SERVER"         ); break;
      case INTERNET_STATUS_CONNECTED_TO_SERVER          : strRes = TEXT("INTERNET_STATUS_CONNECTED_TO_SERVER"          ); break;
      case INTERNET_STATUS_SENDING_REQUEST              : strRes = TEXT("INTERNET_STATUS_SENDING_REQUEST"              ); break;
      case INTERNET_STATUS_REQUEST_SENT                 : strRes = TEXT("INTERNET_STATUS_REQUEST_SENT"                 ); break;
      case INTERNET_STATUS_RECEIVING_RESPONSE           : strRes = TEXT("INTERNET_STATUS_RECEIVING_RESPONSE"           ); break;
      case INTERNET_STATUS_RESPONSE_RECEIVED            : strRes = TEXT("INTERNET_STATUS_RESPONSE_RECEIVED"            ); break;
      case INTERNET_STATUS_CTL_RESPONSE_RECEIVED        : strRes = TEXT("INTERNET_STATUS_CTL_RESPONSE_RECEIVED"        ); break;
      case INTERNET_STATUS_PREFETCH                     : strRes = TEXT("INTERNET_STATUS_PREFETCH"                     ); break;
      case INTERNET_STATUS_CLOSING_CONNECTION           : strRes = TEXT("INTERNET_STATUS_CLOSING_CONNECTION"           ); break;
      case INTERNET_STATUS_CONNECTION_CLOSED            : strRes = TEXT("INTERNET_STATUS_CONNECTION_CLOSED"            ); break;
      case INTERNET_STATUS_HANDLE_CREATED               : strRes = TEXT("INTERNET_STATUS_HANDLE_CREATED"               ); break;
      case INTERNET_STATUS_HANDLE_CLOSING               : strRes = TEXT("INTERNET_STATUS_HANDLE_CLOSING"               ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
      case INTERNET_STATUS_DETECTING_PROXY              : strRes = TEXT("INTERNET_STATUS_DETECTING_PROXY"              ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
      case INTERNET_STATUS_REQUEST_COMPLETE             : strRes = TEXT("INTERNET_STATUS_REQUEST_COMPLETE"             ); break;
      case INTERNET_STATUS_REDIRECT                     : strRes = TEXT("INTERNET_STATUS_REDIRECT"                     ); break;
      case INTERNET_STATUS_INTERMEDIATE_RESPONSE        : strRes = TEXT("INTERNET_STATUS_INTERMEDIATE_RESPONSE"        ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
      case INTERNET_STATUS_USER_INPUT_REQUIRED          : strRes = TEXT("INTERNET_STATUS_USER_INPUT_REQUIRED"          ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
      case INTERNET_STATUS_STATE_CHANGE                 : strRes = TEXT("INTERNET_STATUS_STATE_CHANGE"                 ); break;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
      case INTERNET_STATUS_COOKIE_SENT                  : strRes = TEXT("INTERNET_STATUS_COOKIE_SENT"                  ); break;
      case INTERNET_STATUS_COOKIE_RECEIVED              : strRes = TEXT("INTERNET_STATUS_COOKIE_RECEIVED"              ); break;
      case INTERNET_STATUS_PRIVACY_IMPACTED             : strRes = TEXT("INTERNET_STATUS_PRIVACY_IMPACTED"             ); break;
      case INTERNET_STATUS_P3P_HEADER                   : strRes = TEXT("INTERNET_STATUS_P3P_HEADER"                   ); break;
      case INTERNET_STATUS_P3P_POLICYREF                : strRes = TEXT("INTERNET_STATUS_P3P_POLICYREF"                ); break;
      case INTERNET_STATUS_COOKIE_HISTORY               : strRes = TEXT("INTERNET_STATUS_COOKIE_HISTORY"               ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
      default: strRes.Format(TEXT("0x%08X"), dwInternetStatus);
      }
   } else {
      switch (dwInternetStatus) {
#ifdef MICROSOFT_SDK_FEBRUARY_2003
      case WINHTTP_CALLBACK_STATUS_RESOLVING_NAME       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_RESOLVING_NAME"       ); break;
      case WINHTTP_CALLBACK_STATUS_NAME_RESOLVED        : strRes = TEXT("WINHTTP_CALLBACK_STATUS_NAME_RESOLVED"        ); break;
      case WINHTTP_CALLBACK_STATUS_CONNECTING_TO_SERVER : strRes = TEXT("WINHTTP_CALLBACK_STATUS_CONNECTING_TO_SERVER" ); break;
      case WINHTTP_CALLBACK_STATUS_CONNECTED_TO_SERVER  : strRes = TEXT("WINHTTP_CALLBACK_STATUS_CONNECTED_TO_SERVER"  ); break;
      case WINHTTP_CALLBACK_STATUS_SENDING_REQUEST      : strRes = TEXT("WINHTTP_CALLBACK_STATUS_SENDING_REQUEST"      ); break;
      case WINHTTP_CALLBACK_STATUS_REQUEST_SENT         : strRes = TEXT("WINHTTP_CALLBACK_STATUS_REQUEST_SENT"         ); break;
      case WINHTTP_CALLBACK_STATUS_RECEIVING_RESPONSE   : strRes = TEXT("WINHTTP_CALLBACK_STATUS_RECEIVING_RESPONSE"   ); break;
      case WINHTTP_CALLBACK_STATUS_RESPONSE_RECEIVED    : strRes = TEXT("WINHTTP_CALLBACK_STATUS_RESPONSE_RECEIVED"    ); break;
      case WINHTTP_CALLBACK_STATUS_CLOSING_CONNECTION   : strRes = TEXT("WINHTTP_CALLBACK_STATUS_CLOSING_CONNECTION"   ); break;
      case WINHTTP_CALLBACK_STATUS_CONNECTION_CLOSED    : strRes = TEXT("WINHTTP_CALLBACK_STATUS_CONNECTION_CLOSED"    ); break;
      case WINHTTP_CALLBACK_STATUS_HANDLE_CREATED       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_HANDLE_CREATED"       ); break;
      case WINHTTP_CALLBACK_STATUS_HANDLE_CLOSING       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_HANDLE_CLOSING"       ); break;
      case WINHTTP_CALLBACK_STATUS_DETECTING_PROXY      : strRes = TEXT("WINHTTP_CALLBACK_STATUS_DETECTING_PROXY"      ); break;
      case WINHTTP_CALLBACK_STATUS_REDIRECT             : strRes = TEXT("WINHTTP_CALLBACK_STATUS_REDIRECT"             ); break;
      case WINHTTP_CALLBACK_STATUS_INTERMEDIATE_RESPONSE: strRes = TEXT("WINHTTP_CALLBACK_STATUS_INTERMEDIATE_RESPONSE"); break;
      case WINHTTP_CALLBACK_STATUS_SECURE_FAILURE       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_SECURE_FAILURE"       ); break;
      case WINHTTP_CALLBACK_STATUS_HEADERS_AVAILABLE    : strRes = TEXT("WINHTTP_CALLBACK_STATUS_HEADERS_AVAILABLE"    ); break;
      case WINHTTP_CALLBACK_STATUS_DATA_AVAILABLE       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_DATA_AVAILABLE"       ); break;
      case WINHTTP_CALLBACK_STATUS_READ_COMPLETE        : strRes = TEXT("WINHTTP_CALLBACK_STATUS_READ_COMPLETE"        ); break;
      case WINHTTP_CALLBACK_STATUS_WRITE_COMPLETE       : strRes = TEXT("WINHTTP_CALLBACK_STATUS_WRITE_COMPLETE"       ); break;
      case WINHTTP_CALLBACK_STATUS_REQUEST_ERROR        : strRes = TEXT("WINHTTP_CALLBACK_STATUS_REQUEST_ERROR"        ); break;
      case WINHTTP_CALLBACK_STATUS_SENDREQUEST_COMPLETE : strRes = TEXT("WINHTTP_CALLBACK_STATUS_SENDREQUEST_COMPLETE" ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
      default: strRes.Format(TEXT("0x%08X"), dwInternetStatus);
      }
   }
   return strRes;
}

CString CLogger::WinHTTPCallbackStatusRequestError(DWORD dwValue) {
   CString strRes;
   switch (dwValue) {
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   case API_RECEIVE_RESPONSE    : strRes = TEXT("API_RECEIVE_RESPONSE"    ); break;
   case API_QUERY_DATA_AVAILABLE: strRes = TEXT("API_QUERY_DATA_AVAILABLE"); break;
   case API_READ_DATA           : strRes = TEXT("API_READ_DATA"           ); break;
   case API_WRITE_DATA          : strRes = TEXT("API_WRITE_DATA"          ); break;
   case API_SEND_REQUEST        : strRes = TEXT("API_SEND_REQUEST"        ); break;
#endif // MICROSOFT_SDK_FEBRUARY_2003
   default: strRes.Format(TEXT("%i"), dwValue);
   }
   return strRes;
}

CString CLogger::WinHTTPCallbackStatusRequestError_Description(DWORD dwValue) {
   CString strRes;
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   switch (dwValue) {
   case API_RECEIVE_RESPONSE    : strRes = TEXT("The error occurred during a call to WinHttpReceiveResponse."   ); break;
   case API_QUERY_DATA_AVAILABLE: strRes = TEXT("The error occurred during a call to WinHttpQueryDataAvailable."); break;
   case API_READ_DATA           : strRes = TEXT("The error occurred during a call to WinHttpReadData."          ); break;
   case API_WRITE_DATA          : strRes = TEXT("The error occurred during a call to WinHttpWriteData."         ); break;
   case API_SEND_REQUEST        : strRes = TEXT("The error occurred during a call to WinHttpSendRequest."       ); break;
   }
#endif // MICROSOFT_SDK_FEBRUARY_2003
   return strRes;
}

CString CLogger::WindowMessage(UINT msg) {
   CString strRes;
   switch (msg) {
   case WM_NULL                  : strRes = TEXT("WM_NULL"                  ); break;
   case WM_CREATE                : strRes = TEXT("WM_CREATE"                ); break;
   case WM_DESTROY               : strRes = TEXT("WM_DESTROY"               ); break;
   case WM_MOVE                  : strRes = TEXT("WM_MOVE"                  ); break;
   case WM_SIZE                  : strRes = TEXT("WM_SIZE"                  ); break;
   case WM_ACTIVATE              : strRes = TEXT("WM_ACTIVATE"              ); break;
   case WM_SETFOCUS              : strRes = TEXT("WM_SETFOCUS"              ); break;
   case WM_KILLFOCUS             : strRes = TEXT("WM_KILLFOCUS"             ); break;
   case WM_ENABLE                : strRes = TEXT("WM_ENABLE"                ); break;
   case WM_SETREDRAW             : strRes = TEXT("WM_SETREDRAW"             ); break;
   case WM_SETTEXT               : strRes = TEXT("WM_SETTEXT"               ); break;
   case WM_GETTEXT               : strRes = TEXT("WM_GETTEXT"               ); break;
   case WM_GETTEXTLENGTH         : strRes = TEXT("WM_GETTEXTLENGTH"         ); break;
   case WM_PAINT                 : strRes = TEXT("WM_PAINT"                 ); break;
   case WM_CLOSE                 : strRes = TEXT("WM_CLOSE"                 ); break;
   case WM_QUERYENDSESSION       : strRes = TEXT("WM_QUERYENDSESSION"       ); break;
   case WM_QUIT                  : strRes = TEXT("WM_QUIT"                  ); break;
   case WM_QUERYOPEN             : strRes = TEXT("WM_QUERYOPEN"             ); break;
   case WM_ERASEBKGND            : strRes = TEXT("WM_ERASEBKGND"            ); break;
   case WM_SYSCOLORCHANGE        : strRes = TEXT("WM_SYSCOLORCHANGE"        ); break;
   case WM_ENDSESSION            : strRes = TEXT("WM_ENDSESSION"            ); break;
   case WM_SHOWWINDOW            : strRes = TEXT("WM_SHOWWINDOW"            ); break;
   case WM_SETTINGCHANGE         : strRes = TEXT("WM_SETTINGCHANGE"         ); break;
   case WM_DEVMODECHANGE         : strRes = TEXT("WM_DEVMODECHANGE"         ); break;
   case WM_ACTIVATEAPP           : strRes = TEXT("WM_ACTIVATEAPP"           ); break;
   case WM_FONTCHANGE            : strRes = TEXT("WM_FONTCHANGE"            ); break;
   case WM_TIMECHANGE            : strRes = TEXT("WM_TIMECHANGE"            ); break;
   case WM_CANCELMODE            : strRes = TEXT("WM_CANCELMODE"            ); break;
   case WM_SETCURSOR             : strRes = TEXT("WM_SETCURSOR"             ); break;
   case WM_MOUSEACTIVATE         : strRes = TEXT("WM_MOUSEACTIVATE"         ); break;
   case WM_CHILDACTIVATE         : strRes = TEXT("WM_CHILDACTIVATE"         ); break;
   case WM_QUEUESYNC             : strRes = TEXT("WM_QUEUESYNC"             ); break;
   case WM_GETMINMAXINFO         : strRes = TEXT("WM_GETMINMAXINFO"         ); break;
   case WM_PAINTICON             : strRes = TEXT("WM_PAINTICON"             ); break;
   case WM_ICONERASEBKGND        : strRes = TEXT("WM_ICONERASEBKGND"        ); break;
   case WM_NEXTDLGCTL            : strRes = TEXT("WM_NEXTDLGCTL"            ); break;
   case WM_SPOOLERSTATUS         : strRes = TEXT("WM_SPOOLERSTATUS"         ); break;
   case WM_DRAWITEM              : strRes = TEXT("WM_DRAWITEM"              ); break;
   case WM_MEASUREITEM           : strRes = TEXT("WM_MEASUREITEM"           ); break;
   case WM_DELETEITEM            : strRes = TEXT("WM_DELETEITEM"            ); break;
   case WM_VKEYTOITEM            : strRes = TEXT("WM_VKEYTOITEM"            ); break;
   case WM_CHARTOITEM            : strRes = TEXT("WM_CHARTOITEM"            ); break;
   case WM_SETFONT               : strRes = TEXT("WM_SETFONT"               ); break;
   case WM_GETFONT               : strRes = TEXT("WM_GETFONT"               ); break;
   case WM_SETHOTKEY             : strRes = TEXT("WM_SETHOTKEY"             ); break;
   case WM_GETHOTKEY             : strRes = TEXT("WM_GETHOTKEY"             ); break;
   case WM_QUERYDRAGICON         : strRes = TEXT("WM_QUERYDRAGICON"         ); break;
   case WM_COMPAREITEM           : strRes = TEXT("WM_COMPAREITEM"           ); break;
   case WM_COMPACTING            : strRes = TEXT("WM_COMPACTING"            ); break;
   case WM_COMMNOTIFY            : strRes = TEXT("WM_COMMNOTIFY"            ); break;
   case WM_WINDOWPOSCHANGING     : strRes = TEXT("WM_WINDOWPOSCHANGING"     ); break;
   case WM_WINDOWPOSCHANGED      : strRes = TEXT("WM_WINDOWPOSCHANGED"      ); break;
   case WM_POWER                 : strRes = TEXT("WM_POWER"                 ); break;
   case WM_COPYDATA              : strRes = TEXT("WM_COPYDATA"              ); break;
   case WM_CANCELJOURNAL         : strRes = TEXT("WM_CANCELJOURNAL"         ); break;
#if(WINVER >= 0x0400)
   case WM_NOTIFY                : strRes = TEXT("WM_NOTIFY"                ); break;
   case WM_INPUTLANGCHANGEREQUEST: strRes = TEXT("WM_INPUTLANGCHANGEREQUEST"); break;
   case WM_INPUTLANGCHANGE       : strRes = TEXT("WM_INPUTLANGCHANGE"       ); break;
   case WM_TCARD                 : strRes = TEXT("WM_TCARD"                 ); break;
   case WM_HELP                  : strRes = TEXT("WM_HELP"                  ); break;
   case WM_USERCHANGED           : strRes = TEXT("WM_USERCHANGED"           ); break;
   case WM_NOTIFYFORMAT          : strRes = TEXT("WM_NOTIFYFORMAT"          ); break;
   case WM_CONTEXTMENU           : strRes = TEXT("WM_CONTEXTMENU"           ); break;
   case WM_STYLECHANGING         : strRes = TEXT("WM_STYLECHANGING"         ); break;
   case WM_STYLECHANGED          : strRes = TEXT("WM_STYLECHANGED"          ); break;
   case WM_DISPLAYCHANGE         : strRes = TEXT("WM_DISPLAYCHANGE"         ); break;
   case WM_GETICON               : strRes = TEXT("WM_GETICON"               ); break;
   case WM_SETICON               : strRes = TEXT("WM_SETICON"               ); break;
#endif /* WINVER >= 0x0400 */
   case WM_NCCREATE              : strRes = TEXT("WM_NCCREATE"              ); break;
   case WM_NCDESTROY             : strRes = TEXT("WM_NCDESTROY"             ); break;
   case WM_NCCALCSIZE            : strRes = TEXT("WM_NCCALCSIZE"            ); break;
   case WM_NCHITTEST             : strRes = TEXT("WM_NCHITTEST"             ); break;
   case WM_NCPAINT               : strRes = TEXT("WM_NCPAINT"               ); break;
   case WM_NCACTIVATE            : strRes = TEXT("WM_NCACTIVATE"            ); break;
   case WM_GETDLGCODE            : strRes = TEXT("WM_GETDLGCODE"            ); break;
   case WM_SYNCPAINT             : strRes = TEXT("WM_SYNCPAINT"             ); break;
   case WM_NCMOUSEMOVE           : strRes = TEXT("WM_NCMOUSEMOVE"           ); break;
   case WM_NCLBUTTONDOWN         : strRes = TEXT("WM_NCLBUTTONDOWN"         ); break;
   case WM_NCLBUTTONUP           : strRes = TEXT("WM_NCLBUTTONUP"           ); break;
   case WM_NCLBUTTONDBLCLK       : strRes = TEXT("WM_NCLBUTTONDBLCLK"       ); break;
   case WM_NCRBUTTONDOWN         : strRes = TEXT("WM_NCRBUTTONDOWN"         ); break;
   case WM_NCRBUTTONUP           : strRes = TEXT("WM_NCRBUTTONUP"           ); break;
   case WM_NCRBUTTONDBLCLK       : strRes = TEXT("WM_NCRBUTTONDBLCLK"       ); break;
   case WM_NCMBUTTONDOWN         : strRes = TEXT("WM_NCMBUTTONDOWN"         ); break;
   case WM_NCMBUTTONUP           : strRes = TEXT("WM_NCMBUTTONUP"           ); break;
   case WM_NCMBUTTONDBLCLK       : strRes = TEXT("WM_NCMBUTTONDBLCLK"       ); break;
   case WM_KEYDOWN/*WM_KEYFIRST*/: strRes = TEXT("WM_KEYDOWN"/*WM_KEYFIRST*/); break;
   case WM_KEYUP                 : strRes = TEXT("WM_KEYUP"                 ); break;
   case WM_CHAR                  : strRes = TEXT("WM_CHAR"                  ); break;
   case WM_DEADCHAR              : strRes = TEXT("WM_DEADCHAR"              ); break;
   case WM_SYSKEYDOWN            : strRes = TEXT("WM_SYSKEYDOWN"            ); break;
   case WM_SYSKEYUP              : strRes = TEXT("WM_SYSKEYUP"              ); break;
   case WM_SYSCHAR               : strRes = TEXT("WM_SYSCHAR"               ); break;
   case WM_SYSDEADCHAR           : strRes = TEXT("WM_SYSDEADCHAR"           ); break;
   case WM_KEYLAST               : strRes = TEXT("WM_KEYLAST"               ); break;
#if(WINVER >= 0x0400)
   case WM_IME_STARTCOMPOSITION  : strRes = TEXT("WM_IME_STARTCOMPOSITION"  ); break;
   case WM_IME_ENDCOMPOSITION    : strRes = TEXT("WM_IME_ENDCOMPOSITION"    ); break;
   case WM_IME_COMPOSITION       : strRes = TEXT("WM_IME_COMPOSITION"       ); break;
#endif /* WINVER >= 0x0400 */
   case WM_INITDIALOG            : strRes = TEXT("WM_INITDIALOG"            ); break;
   case WM_COMMAND               : strRes = TEXT("WM_COMMAND"               ); break;
   case WM_SYSCOMMAND            : strRes = TEXT("WM_SYSCOMMAND"            ); break;
   case WM_TIMER                 : strRes = TEXT("WM_TIMER"                 ); break;
   case WM_HSCROLL               : strRes = TEXT("WM_HSCROLL"               ); break;
   case WM_VSCROLL               : strRes = TEXT("WM_VSCROLL"               ); break;
   case WM_INITMENU              : strRes = TEXT("WM_INITMENU"              ); break;
   case WM_INITMENUPOPUP         : strRes = TEXT("WM_INITMENUPOPUP"         ); break;
   case WM_MENUSELECT            : strRes = TEXT("WM_MENUSELECT"            ); break;
   case WM_MENUCHAR              : strRes = TEXT("WM_MENUCHAR"              ); break;
   case WM_ENTERIDLE             : strRes = TEXT("WM_ENTERIDLE"             ); break;
#if (WINVER >= 0x0500)
   case WM_MENURBUTTONUP         : strRes = TEXT("WM_MENURBUTTONUP"  ); break;
   case WM_MENUDRAG              : strRes = TEXT("WM_MENUDRAG"       ); break;
   case WM_MENUGETOBJECT         : strRes = TEXT("WM_MENUGETOBJECT"  ); break;
   case WM_UNINITMENUPOPUP       : strRes = TEXT("WM_UNINITMENUPOPUP"); break;
   case WM_MENUCOMMAND           : strRes = TEXT("WM_MENUCOMMAND"    ); break;
#endif /* WINVER >= 0x0500 */
#ifdef __AFX_H__
   case WM_CTLCOLOR              : strRes = TEXT("WM_CTLCOLOR"              ); break;
#endif // __AFX_H__
   case WM_CTLCOLORMSGBOX        : strRes = TEXT("WM_CTLCOLORMSGBOX"        ); break;
   case WM_CTLCOLOREDIT          : strRes = TEXT("WM_CTLCOLOREDIT"          ); break;
   case WM_CTLCOLORLISTBOX       : strRes = TEXT("WM_CTLCOLORLISTBOX"       ); break;
   case WM_CTLCOLORBTN           : strRes = TEXT("WM_CTLCOLORBTN"           ); break;
   case WM_CTLCOLORDLG           : strRes = TEXT("WM_CTLCOLORDLG"           ); break;
   case WM_CTLCOLORSCROLLBAR     : strRes = TEXT("WM_CTLCOLORSCROLLBAR"     ); break;
   case WM_CTLCOLORSTATIC        : strRes = TEXT("WM_CTLCOLORSTATIC"        ); break;
   case WM_MOUSEMOVE             : strRes = TEXT("WM_MOUSEMOVE"             ); break;
   case WM_LBUTTONDOWN           : strRes = TEXT("WM_LBUTTONDOWN"           ); break;
   case WM_LBUTTONUP             : strRes = TEXT("WM_LBUTTONUP"             ); break;
   case WM_LBUTTONDBLCLK         : strRes = TEXT("WM_LBUTTONDBLCLK"         ); break;
   case WM_RBUTTONDOWN           : strRes = TEXT("WM_RBUTTONDOWN"           ); break;
   case WM_RBUTTONUP             : strRes = TEXT("WM_RBUTTONUP"             ); break;
   case WM_RBUTTONDBLCLK         : strRes = TEXT("WM_RBUTTONDBLCLK"         ); break;
   case WM_MBUTTONDOWN           : strRes = TEXT("WM_MBUTTONDOWN"           ); break;
   case WM_MBUTTONUP             : strRes = TEXT("WM_MBUTTONUP"             ); break;
   case WM_MBUTTONDBLCLK         : strRes = TEXT("WM_MBUTTONDBLCLK"         ); break;
#if(_WIN32_WINNT >= 0x0400)
   case WM_MOUSEWHEEL            : strRes = TEXT("WM_MOUSEWHEEL"            ); break;
#endif /* _WIN32_WINNT >= 0x0400 */
   case WM_PARENTNOTIFY          : strRes = TEXT("WM_PARENTNOTIFY"          ); break;
   case WM_ENTERMENULOOP         : strRes = TEXT("WM_ENTERMENULOOP"         ); break;
   case WM_EXITMENULOOP          : strRes = TEXT("WM_EXITMENULOOP"          ); break;
#if(WINVER >= 0x0400)
   case WM_NEXTMENU              : strRes = TEXT("WM_NEXTMENU"              ); break;
   case WM_SIZING                : strRes = TEXT("WM_SIZING"                ); break;
   case WM_CAPTURECHANGED        : strRes = TEXT("WM_CAPTURECHANGED"        ); break;
   case WM_MOVING                : strRes = TEXT("WM_MOVING"                ); break;
   case WM_POWERBROADCAST        : strRes = TEXT("WM_POWERBROADCAST"        ); break;
   case WM_DEVICECHANGE          : strRes = TEXT("WM_DEVICECHANGE"          ); break;
   case WM_IME_SETCONTEXT        : strRes = TEXT("WM_IME_SETCONTEXT"        ); break;
   case WM_IME_NOTIFY            : strRes = TEXT("WM_IME_NOTIFY"            ); break;
   case WM_IME_CONTROL           : strRes = TEXT("WM_IME_CONTROL"           ); break;
   case WM_IME_COMPOSITIONFULL   : strRes = TEXT("WM_IME_COMPOSITIONFULL"   ); break;
   case WM_IME_SELECT            : strRes = TEXT("WM_IME_SELECT"            ); break;
   case WM_IME_CHAR              : strRes = TEXT("WM_IME_CHAR"              ); break;
   case WM_IME_KEYDOWN           : strRes = TEXT("WM_IME_KEYDOWN"           ); break;
   case WM_IME_KEYUP             : strRes = TEXT("WM_IME_KEYUP"             ); break;
#endif /* WINVER >= 0x0400 */
   case WM_MDICREATE             : strRes = TEXT("WM_MDICREATE"             ); break;
   case WM_MDIDESTROY            : strRes = TEXT("WM_MDIDESTROY"            ); break;
   case WM_MDIACTIVATE           : strRes = TEXT("WM_MDIACTIVATE"           ); break;
   case WM_MDIRESTORE            : strRes = TEXT("WM_MDIRESTORE"            ); break;
   case WM_MDINEXT               : strRes = TEXT("WM_MDINEXT"               ); break;
   case WM_MDIMAXIMIZE           : strRes = TEXT("WM_MDIMAXIMIZE"           ); break;
   case WM_MDITILE               : strRes = TEXT("WM_MDITILE"               ); break;
   case WM_MDICASCADE            : strRes = TEXT("WM_MDICASCADE"            ); break;
   case WM_MDIICONARRANGE        : strRes = TEXT("WM_MDIICONARRANGE"        ); break;
   case WM_MDIGETACTIVE          : strRes = TEXT("WM_MDIGETACTIVE"          ); break;
   case WM_MDISETMENU            : strRes = TEXT("WM_MDISETMENU"            ); break;
   case WM_ENTERSIZEMOVE         : strRes = TEXT("WM_ENTERSIZEMOVE"         ); break;
   case WM_EXITSIZEMOVE          : strRes = TEXT("WM_EXITSIZEMOVE"          ); break;
   case WM_DROPFILES             : strRes = TEXT("WM_DROPFILES"             ); break;
   case WM_MDIREFRESHMENU        : strRes = TEXT("WM_MDIREFRESHMENU"        ); break;
#if(_WIN32_WINNT >= 0x0400)
   case WM_MOUSEHOVER            : strRes = TEXT("WM_MOUSEHOVER"            ); break;
   case WM_MOUSELEAVE            : strRes = TEXT("WM_MOUSELEAVE"            ); break;
#endif /* _WIN32_WINNT >= 0x0400 */
   case WM_CUT                   : strRes = TEXT("WM_CUT"                   ); break;
   case WM_COPY                  : strRes = TEXT("WM_COPY"                  ); break;
   case WM_PASTE                 : strRes = TEXT("WM_PASTE"                 ); break;
   case WM_CLEAR                 : strRes = TEXT("WM_CLEAR"                 ); break;
   case WM_UNDO                  : strRes = TEXT("WM_UNDO"                  ); break;
   case WM_RENDERFORMAT          : strRes = TEXT("WM_RENDERFORMAT"          ); break;
   case WM_RENDERALLFORMATS      : strRes = TEXT("WM_RENDERALLFORMATS"      ); break;
   case WM_DESTROYCLIPBOARD      : strRes = TEXT("WM_DESTROYCLIPBOARD"      ); break;
   case WM_DRAWCLIPBOARD         : strRes = TEXT("WM_DRAWCLIPBOARD"         ); break;
   case WM_PAINTCLIPBOARD        : strRes = TEXT("WM_PAINTCLIPBOARD"        ); break;
   case WM_VSCROLLCLIPBOARD      : strRes = TEXT("WM_VSCROLLCLIPBOARD"      ); break;
   case WM_SIZECLIPBOARD         : strRes = TEXT("WM_SIZECLIPBOARD"         ); break;
   case WM_ASKCBFORMATNAME       : strRes = TEXT("WM_ASKCBFORMATNAME"       ); break;
   case WM_CHANGECBCHAIN         : strRes = TEXT("WM_CHANGECBCHAIN"         ); break;
   case WM_HSCROLLCLIPBOARD      : strRes = TEXT("WM_HSCROLLCLIPBOARD"      ); break;
   case WM_QUERYNEWPALETTE       : strRes = TEXT("WM_QUERYNEWPALETTE"       ); break;
   case WM_PALETTEISCHANGING     : strRes = TEXT("WM_PALETTEISCHANGING"     ); break;
   case WM_PALETTECHANGED        : strRes = TEXT("WM_PALETTECHANGED"        ); break;
   case WM_HOTKEY                : strRes = TEXT("WM_HOTKEY"                ); break;
#if(WINVER >= 0x0400)
   case WM_PRINT                 : strRes = TEXT("WM_PRINT"                 ); break;
   case WM_PRINTCLIENT           : strRes = TEXT("WM_PRINTCLIENT"           ); break;
   case WM_HANDHELDFIRST         : strRes = TEXT("WM_HANDHELDFIRST"         ); break;
   case WM_HANDHELDLAST          : strRes = TEXT("WM_HANDHELDLAST"          ); break;
   case WM_AFXFIRST              : strRes = TEXT("WM_AFXFIRST"              ); break;
   case WM_AFXLAST               : strRes = TEXT("WM_AFXLAST"               ); break;
#endif /* WINVER >= 0x0400 */
   case WM_PENWINFIRST           : strRes = TEXT("WM_PENWINFIRST"           ); break;
   case WM_PENWINLAST            : strRes = TEXT("WM_PENWINLAST"            ); break;
#if(WINVER >= 0x0400)
   case WM_APP                   : strRes = TEXT("WM_APP"                   ); break;
#endif /* WINVER >= 0x0400 */
   case WM_USER                  : strRes = TEXT("WM_USER"                  ); break;
 //case 0x20A                    : strRes = TEXT("mouse_scrolling"          ); break;
   }
   if (strRes.IsEmpty()) {
#if(WINVER >= 0x0400)
      if ((msg>WM_HANDHELDFIRST) && (msg<WM_HANDHELDLAST)) {
         strRes.Format(TEXT("WM_HANDHELDFIRST+%d"), msg-WM_HANDHELDFIRST);
      } else
      if ((msg>WM_AFXFIRST) && (msg<WM_AFXLAST)) {
         strRes.Format(TEXT("WM_AFXFIRST+%d"), msg-WM_AFXFIRST);
      } else
#endif /* WINVER >= 0x0400 */
      if ((msg>WM_PENWINFIRST) && (msg<WM_PENWINFIRST)) {
         strRes.Format(TEXT("WM_PENWINFIRST+%d"), msg-WM_PENWINLAST);
      } else
      {
         strRes.Format(TEXT("0x%04X"), msg);
      }
   }
   return strRes;
}

#define IF_IID(interface_ID, interface_name) \
   if (pInterface) { \
      IUnknown *pFindInterface = NULL; \
      if (SUCCEEDED(pInterface->QueryInterface(interface_ID, (void**)&pFindInterface))) { \
         pFindInterface->Release(); \
         strRes += TEXT(interface_name); \
         strRes += strSeparator; \
      } \
   } else { \
      if (riid==interface_ID) { \
         strRes += TEXT(interface_name); \
         strRes += strSeparator; \
      } \
   }

CString CLogger::Interfaces(IUnknown *pInterface, REFIID riid, CString strSeparator) {
   // если указатель на интерфейс не NULL - Возвращаю строку содержащую GUIDы всех интерфейсов, которые можно получить с помощью pInterface->QueryInterface(..)
   // иначе просто возвращаю строковое представление GUID'a которое совпало с riid
   CString strRes;

   IF_IID(IID_IOleAdviseHolder                , "IID_IOleAdviseHolder"                )
   IF_IID(IID_IOleCache                       , "IID_IOleCache"                       )
   IF_IID(IID_IOleCache2                      , "IID_IOleCache2"                      )
   IF_IID(IID_IOleCacheControl                , "IID_IOleCacheControl"                )
   IF_IID(IID_IParseDisplayName               , "IID_IParseDisplayName"               )
   IF_IID(IID_IOleContainer                   , "IID_IOleContainer"                   )
   IF_IID(IID_IOleClientSite                  , "IID_IOleClientSite"                  )
   IF_IID(IID_IOleObject                      , "IID_IOleObject"                      )
   IF_IID(IID_IOleWindow                      , "IID_IOleWindow"                      )
   IF_IID(IID_IOleLink                        , "IID_IOleLink"                        )
   IF_IID(IID_IOleItemContainer               , "IID_IOleItemContainer"               )
   IF_IID(IID_IOleInPlaceUIWindow             , "IID_IOleInPlaceUIWindow"             )
   IF_IID(IID_IOleInPlaceActiveObject         , "IID_IOleInPlaceActiveObject"         )
   IF_IID(IID_IOleInPlaceFrame                , "IID_IOleInPlaceFrame"                )
   IF_IID(IID_IOleInPlaceObject               , "IID_IOleInPlaceObject"               )
   IF_IID(IID_IOleInPlaceSite                 , "IID_IOleInPlaceSite"                 )
   IF_IID(IID_IContinue                       , "IID_IContinue"                       )
   IF_IID(IID_IViewObject                     , "IID_IViewObject"                     )
   IF_IID(IID_IViewObject2                    , "IID_IViewObject2"                    )
   IF_IID(IID_IDropSource                     , "IID_IDropSource"                     )
   IF_IID(IID_IDropTarget                     , "IID_IDropTarget"                     )
   IF_IID(IID_IEnumOLEVERB                    , "IID_IEnumOLEVERB"                    )
   IF_IID(IID_IOleDocument                    , "IID_IOleDocument"                    )
   IF_IID(IID_IOleDocumentSite                , "IID_IOleDocumentSite"                )
   IF_IID(IID_IOleDocumentView                , "IID_IOleDocumentView"                )
   IF_IID(IID_IEnumOleDocumentViews           , "IID_IEnumOleDocumentViews"           )
   IF_IID(IID_IContinueCallback               , "IID_IContinueCallback"               )
   IF_IID(IID_IPrint                          , "IID_IPrint"                          )
   IF_IID(IID_IOleCommandTarget               , "IID_IOleCommandTarget"               )
   IF_IID(IID_IEnumConnections                , "IID_IEnumConnections"                )
   IF_IID(IID_IConnectionPoint                , "IID_IConnectionPoint"                )
   IF_IID(IID_IEnumConnectionPoints           , "IID_IEnumConnectionPoints"           )
   IF_IID(IID_IConnectionPointContainer       , "IID_IConnectionPointContainer"       )
   IF_IID(IID_IClassFactory                   , "IID_IClassFactory"                   )
   IF_IID(IID_IClassFactory2                  , "IID_IClassFactory2"                  )
   IF_IID(IID_IProvideClassInfo               , "IID_IProvideClassInfo"               )
   IF_IID(IID_IProvideClassInfo2              , "IID_IProvideClassInfo2"              )
   IF_IID(IID_IProvideMultipleClassInfo       , "IID_IProvideMultipleClassInfo"       )
   IF_IID(IID_IOleControl                     , "IID_IOleControl"                     )
   IF_IID(IID_IOleControlSite                 , "IID_IOleControlSite"                 )
   IF_IID(IID_IPropertyPage                   , "IID_IPropertyPage"                   )
   IF_IID(IID_IPropertyPage2                  , "IID_IPropertyPage2"                  )
   IF_IID(IID_IPropertyPageSite               , "IID_IPropertyPageSite"               )
   IF_IID(IID_IPropertyNotifySink             , "IID_IPropertyNotifySink"             )
   IF_IID(IID_ISpecifyPropertyPages           , "IID_ISpecifyPropertyPages"           )
   IF_IID(IID_IPersistMemory                  , "IID_IPersistMemory"                  )
   IF_IID(IID_IPersistStreamInit              , "IID_IPersistStreamInit"              )
   IF_IID(IID_ISimpleFrameSite                , "IID_ISimpleFrameSite"                )
   IF_IID(IID_IFont                           , "IID_IFont"                           )
   IF_IID(IID_IPicture                        , "IID_IPicture"                        )
   IF_IID(IID_IFontEventsDisp                 , "IID_IFontEventsDisp"                 )
   IF_IID(IID_IFontDisp                       , "IID_IFontDisp"                       )
   IF_IID(IID_IPictureDisp                    , "IID_IPictureDisp"                    )
   IF_IID(IID_IOleInPlaceObjectWindowless     , "IID_IOleInPlaceObjectWindowless"     )
   IF_IID(IID_IOleInPlaceSiteEx               , "IID_IOleInPlaceSiteEx"               )
   IF_IID(IID_IOleInPlaceSiteWindowless       , "IID_IOleInPlaceSiteWindowless"       )
   IF_IID(IID_IViewObjectEx                   , "IID_IViewObjectEx"                   )
   IF_IID(IID_IOleUndoUnit                    , "IID_IOleUndoUnit"                    )
   IF_IID(IID_IOleParentUndoUnit              , "IID_IOleParentUndoUnit"              )
   IF_IID(IID_IEnumOleUndoUnits               , "IID_IEnumOleUndoUnits"               )
   IF_IID(IID_IOleUndoManager                 , "IID_IOleUndoManager"                 )
   IF_IID(IID_IPointerInactive                , "IID_IPointerInactive"                )
   IF_IID(IID_IObjectWithSite                 , "IID_IObjectWithSite"                 )
   IF_IID(IID_IPerPropertyBrowsing            , "IID_IPerPropertyBrowsing"            )
   IF_IID(IID_IPropertyBag                    , "IID_IPropertyBag"                    )
   IF_IID(IID_IPropertyBag2                   , "IID_IPropertyBag2"                   )
   IF_IID(IID_IPersistPropertyBag             , "IID_IPersistPropertyBag"             )
   IF_IID(IID_IPersistPropertyBag2            , "IID_IPersistPropertyBag2"            )
   IF_IID(IID_IAdviseSinkEx                   , "IID_IAdviseSinkEx"                   )
   IF_IID(IID_IQuickActivate                  , "IID_IQuickActivate"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_AsyncIUnknown                   , "IID_AsyncIUnknown"                   )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IServiceProvider                , "IID_IServiceProvider"                )
   IF_IID(CLSID_ShellDesktop                  , "CLSID_ShellDesktop"                  )
   IF_IID(CLSID_ShellLink                     , "CLSID_ShellLink"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_NetworkPlaces                 , "CLSID_NetworkPlaces"                 )
   IF_IID(CLSID_NetworkDomain                 , "CLSID_NetworkDomain"                 )
   IF_IID(CLSID_NetworkServer                 , "CLSID_NetworkServer"                 )
   IF_IID(CLSID_NetworkShare                  , "CLSID_NetworkShare"                  )
   IF_IID(CLSID_MyComputer                    , "CLSID_MyComputer"                    )
   IF_IID(CLSID_Internet                      , "CLSID_Internet"                      )
   IF_IID(CLSID_ShellFSFolder                 , "CLSID_ShellFSFolder"                 )
   IF_IID(CLSID_RecycleBin                    , "CLSID_RecycleBin"                    )
   IF_IID(CLSID_ControlPanel                  , "CLSID_ControlPanel"                  )
   IF_IID(CLSID_Printers                      , "CLSID_Printers"                      )
   IF_IID(CLSID_MyDocuments                   , "CLSID_MyDocuments"                   )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CATID_BrowsableShellExt             , "CATID_BrowsableShellExt"             )
   IF_IID(CATID_BrowseInPlace                 , "CATID_BrowseInPlace"                 )
   IF_IID(CATID_DeskBand                      , "CATID_DeskBand"                      )
   IF_IID(CATID_InfoBand                      , "CATID_InfoBand"                      )
   IF_IID(CATID_CommBand                      , "CATID_CommBand"                      )
   IF_IID(FMTID_Intshcut                      , "FMTID_Intshcut"                      )
   IF_IID(FMTID_InternetSite                  , "FMTID_InternetSite"                  )
   IF_IID(CGID_Explorer                       , "CGID_Explorer"                       )
   IF_IID(CGID_ShellDocView                   , "CGID_ShellDocView"                   )
   IF_IID(CGID_ShellServiceObject             , "CGID_ShellServiceObject"             )
   IF_IID(CGID_ExplorerBarDoc                 , "CGID_ExplorerBarDoc"                 )
   IF_IID(IID_IShellBrowser                   , "IID_IShellBrowser"                   )
   IF_IID(IID_IShellIcon                      , "IID_IShellIcon"                      )
   IF_IID(IID_IShellExtInit                   , "IID_IShellExtInit"                   )
   IF_IID(IID_IShellPropSheetExt              , "IID_IShellPropSheetExt"              )
   IF_IID(IID_IPersistFolder                  , "IID_IPersistFolder"                  )
   IF_IID(IID_IShellDetails                   , "IID_IShellDetails"                   )
   IF_IID(IID_IDelayedRelease                 , "IID_IDelayedRelease"                 )
   IF_IID(IID_IEnumIDList                     , "IID_IEnumIDList"                     )
   IF_IID(IID_IFileViewerSite                 , "IID_IFileViewerSite"                 )
   IF_IID(IID_IContextMenu                    , "IID_IContextMenu"                    )
   IF_IID(IID_IContextMenu2                   , "IID_IContextMenu2"                   )
   IF_IID(IID_IContextMenu3                   , "IID_IContextMenu3"                   )
   IF_IID(IID_IPropSheetPage                  , "IID_IPropSheetPage"                  )
   IF_IID(IID_IFileViewerW                    , "IID_IFileViewerW"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IRemoteComputer                 , "IID_IRemoteComputer"                 )
   IF_IID(IID_ICopyHookA                      , "IID_ICopyHookA"                      )
   IF_IID(IID_ICopyHookW                      , "IID_ICopyHookW"                      )
   IF_IID(SID_LinkSite                        , "SID_LinkSite"                        )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IQueryInfo                      , "IID_IQueryInfo"                      )
   IF_IID(IID_IBriefcaseStg                   , "IID_IBriefcaseStg"                   )
   IF_IID(IID_IShellView                      , "IID_IShellView"                      )
   IF_IID(IID_IShellView2                     , "IID_IShellView2"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IShellLinkDataList              , "IID_IShellLinkDataList"              )
   IF_IID(IID_IResolveShellLink               , "IID_IResolveShellLink"               )
   IF_IID(IID_ISearchContext                  , "IID_ISearchContext"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IURLSearchHook                  , "IID_IURLSearchHook"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IURLSearchHook2                 , "IID_IURLSearchHook2"                 )
   IF_IID(IID_IDefViewID                      , "IID_IDefViewID"                      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IUnknown                        , "IID_IUnknown"                        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_FolderShortcut                , "CLSID_FolderShortcut"                )
   IF_IID(CLSID_StgFolder                     , "CLSID_StgFolder"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IInputObject                    , "IID_IInputObject"                    )
   IF_IID(IID_IInputObjectSite                , "IID_IInputObjectSite"                )
   IF_IID(IID_IDockingWindowSite              , "IID_IDockingWindowSite"              )
   IF_IID(IID_IDockingWindowFrame             , "IID_IDockingWindowFrame"             )
   IF_IID(IID_IShellIconOverlay               , "IID_IShellIconOverlay"               )
   IF_IID(IID_IShellIconOverlayIdentifier     , "IID_IShellIconOverlayIdentifier"     )
   IF_IID(IID_ICommDlgBrowser                 , "IID_ICommDlgBrowser"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ICommDlgBrowser2                , "IID_ICommDlgBrowser2"                )
   IF_IID(SID_ShellFolderViewCB               , "SID_ShellFolderViewCB"               )
   IF_IID(IID_IShellFolderViewCB              , "IID_IShellFolderViewCB"              )
   IF_IID(IID_IPersistFolder3                 , "IID_IPersistFolder3"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_CFSIconOverlayManager         , "CLSID_CFSIconOverlayManager"         )
   IF_IID(IID_IShellIconOverlayManager        , "IID_IShellIconOverlayManager"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IRunnableTask                   , "IID_IRunnableTask"                   )
   IF_IID(IID_IThumbnailCapture               , "IID_IThumbnailCapture"               )
   IF_IID(IID_IShellImageStore                , "IID_IShellImageStore"                )
   IF_IID(IID_IEnumShellImageStore            , "IID_IEnumShellImageStore"            )
   IF_IID(CLSID_ShellThumbnailDiskCache       , "CLSID_ShellThumbnailDiskCache"       )
   IF_IID(SID_DefView                         , "SID_DefView"                         )
   IF_IID(CGID_DefView                        , "CGID_DefView"                        )
   IF_IID(CLSID_MenuBand                      , "CLSID_MenuBand"                      )
   IF_IID(IID_IShellFolderBand                , "IID_IShellFolderBand"                )
   IF_IID(IID_IDefViewFrame                   , "IID_IDefViewFrame"                   )
   IF_IID(VID_LargeIcons                      , "VID_LargeIcons"                      )
   IF_IID(VID_SmallIcons                      , "VID_SmallIcons"                      )
   IF_IID(VID_List                            , "VID_List"                            )
   IF_IID(VID_Details                         , "VID_Details"                         )
   IF_IID(VID_Tile                            , "VID_Tile"                            )
   IF_IID(VID_Thumbnails                      , "VID_Thumbnails"                      )
   IF_IID(VID_ThumbStrip                      , "VID_ThumbStrip"                      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_SShellBrowser                   , "SID_SShellBrowser"                   )
   IF_IID(SID_SShellDesktop                   , "SID_SShellDesktop"                   )
   IF_IID(IID_IDiscardableBrowserProperty     , "IID_IDiscardableBrowserProperty"     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IShellChangeNotify              , "IID_IShellChangeNotify"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IFileViewer                     , "IID_IFileViewer"                     )
   IF_IID(IID_IFileViewerA                    , "IID_IFileViewerA"                    )
   IF_IID(IID_IShellLink                      , "IID_IShellLink"                      )
   IF_IID(IID_IShellLinkW                     , "IID_IShellLinkW"                     )
   IF_IID(IID_IShellLinkA                     , "IID_IShellLinkA"                     )
   IF_IID(IID_IExtractIcon                    , "IID_IExtractIcon"                    )
   IF_IID(IID_IExtractIconW                   , "IID_IExtractIconW"                   )
   IF_IID(IID_IExtractIconA                   , "IID_IExtractIconA"                   )
   IF_IID(IID_IShellCopyHook                  , "IID_IShellCopyHook"                  )
   IF_IID(IID_IShellCopyHookW                 , "IID_IShellCopyHookW"                 )
   IF_IID(IID_IShellCopyHookA                 , "IID_IShellCopyHookA"                 )
   IF_IID(IID_IShellExecuteHook               , "IID_IShellExecuteHook"               )
   IF_IID(IID_IShellExecuteHookW              , "IID_IShellExecuteHookW"              )
   IF_IID(IID_IShellExecuteHookA              , "IID_IShellExecuteHookA"              )
   IF_IID(IID_INewShortcutHook                , "IID_INewShortcutHook"                )
   IF_IID(IID_INewShortcutHookW               , "IID_INewShortcutHookW"               )
   IF_IID(IID_INewShortcutHookA               , "IID_INewShortcutHookA"               )
   IF_IID(SID_SUrlHistory                     , "SID_SUrlHistory"                     )
   IF_IID(CLSID_CUrlHistory                   , "CLSID_CUrlHistory"                   )
   IF_IID(CLSID_CURLSearchHook                , "CLSID_CURLSearchHook"                )
   IF_IID(SID_SInternetExplorer               , "SID_SInternetExplorer"               )
   IF_IID(SID_SWebBrowserApp                  , "SID_SWebBrowserApp"                  )
   IF_IID(IID_IWebBrowserApp                  , "IID_IWebBrowserApp"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IAutoCompList                   , "IID_IAutoCompList"                   )
   IF_IID(IID_IObjMgr                         , "IID_IObjMgr"                         )
   IF_IID(IID_IACList                         , "IID_IACList"                         )
   IF_IID(IID_IACList2                        , "IID_IACList2"                        )
   IF_IID(IID_ICurrentWorkingDirectory        , "IID_ICurrentWorkingDirectory"        )
   IF_IID(CLSID_AutoComplete                  , "CLSID_AutoComplete"                  )
   IF_IID(CLSID_ACLHistory                    , "CLSID_ACLHistory"                    )
   IF_IID(CLSID_ACListISF                     , "CLSID_ACListISF"                     )
   IF_IID(CLSID_ACLMRU                        , "CLSID_ACLMRU"                        )
   IF_IID(CLSID_ACLMulti                      , "CLSID_ACLMulti"                      )
#if (_WIN32_IE >= 0x0600)
   IF_IID(CLSID_ACLCustomMRU                  , "CLSID_ACLCustomMRU"                  )
#endif
   IF_IID(CLSID_ProgressDialog                , "CLSID_ProgressDialog"                )
   IF_IID(IID_IProgressDialog                 , "IID_IProgressDialog"                 )
   IF_IID(SID_SProgressUI                     , "SID_SProgressUI"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_STopLevelBrowser                , "SID_STopLevelBrowser"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_FileTypes                     , "CLSID_FileTypes"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_ActiveDesktop                 , "CLSID_ActiveDesktop"                 )
   IF_IID(IID_IActiveDesktop                  , "IID_IActiveDesktop"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IActiveDesktopP                 , "IID_IActiveDesktopP"                 )
   IF_IID(IID_IADesktopP2                     , "IID_IADesktopP2"                     )
   IF_IID(IID_ISynchronizedCallBack           , "IID_ISynchronizedCallBack"           )
   IF_IID(IID_IShellDetails3                  , "IID_IShellDetails3"                  )
   IF_IID(IID_IQueryAssociations              , "IID_IQueryAssociations"              )
   IF_IID(CLSID_QueryAssociations             , "CLSID_QueryAssociations"             )
   IF_IID(IID_IColumnProvider                 , "IID_IColumnProvider"                 )
   IF_IID(CLSID_DocFileColumnProvider         , "CLSID_DocFileColumnProvider"         )
   IF_IID(CLSID_LinkColumnProvider            , "CLSID_LinkColumnProvider"            )
   IF_IID(CLSID_FileSysColumnProvider         , "CLSID_FileSysColumnProvider"         )
   IF_IID(CGID_ShortCut                       , "CGID_ShortCut"                       )
   IF_IID(IID_INamedPropertyBag               , "IID_INamedPropertyBag"               )
   IF_IID(CLSID_InternetButtons               , "CLSID_InternetButtons"               )
   IF_IID(CLSID_MSOButtons                    , "CLSID_MSOButtons"                    )
   IF_IID(CLSID_ToolbarExtButtons             , "CLSID_ToolbarExtButtons"             )
   IF_IID(CLSID_DarwinAppPublisher            , "CLSID_DarwinAppPublisher"            )
   IF_IID(CLSID_DocHostUIHandler              , "CLSID_DocHostUIHandler"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IShellFolder                    , "IID_IShellFolder"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IShellFolder2                   , "IID_IShellFolder2"                   )
   IF_IID(FMTID_ShellDetails                  , "FMTID_ShellDetails"                  )
   IF_IID(FMTID_Storage                       , "FMTID_Storage"                       )
   IF_IID(FMTID_ImageProperties               , "FMTID_ImageProperties"               )
   IF_IID(FMTID_Displaced                     , "FMTID_Displaced"                     )
   IF_IID(FMTID_Briefcase                     , "FMTID_Briefcase"                     )
   IF_IID(FMTID_Misc                          , "FMTID_Misc"                          )
   IF_IID(FMTID_WebView                       , "FMTID_WebView"                       )
   IF_IID(FMTID_MUSIC                         , "FMTID_MUSIC"                         )
   IF_IID(FMTID_DRM                           , "FMTID_DRM"                           )
   IF_IID(FMTID_Volume                        , "FMTID_Volume"                        )
   IF_IID(FMTID_Query                         , "FMTID_Query"                         )
   IF_IID(IID_IEnumExtraSearch                , "IID_IEnumExtraSearch"                )
   IF_IID(CLSID_MountedVolume                 , "CLSID_MountedVolume"                 )
   IF_IID(CLSID_HWShellExecute                , "CLSID_HWShellExecute"                )
   IF_IID(IID_IMountedVolume                  , "IID_IMountedVolume"                  )
   IF_IID(CLSID_DragDropHelper                , "CLSID_DragDropHelper"                )
   IF_IID(IID_IDropTargetHelper               , "IID_IDropTargetHelper"               )
   IF_IID(IID_IDragSourceHelper               , "IID_IDragSourceHelper"               )
   IF_IID(CLSID_CAnchorBrowsePropertyPage     , "CLSID_CAnchorBrowsePropertyPage"     )
   IF_IID(CLSID_CImageBrowsePropertyPage      , "CLSID_CImageBrowsePropertyPage"      )
   IF_IID(CLSID_CDocBrowsePropertyPage        , "CLSID_CDocBrowsePropertyPage"        )
   IF_IID(IID_IFileSystemBindData             , "IID_IFileSystemBindData"             )
   IF_IID(SID_STopWindow                      , "SID_STopWindow"                      )
   IF_IID(SID_SGetViewFromViewDual            , "SID_SGetViewFromViewDual"            )
   IF_IID(CLSID_FolderItem                    , "CLSID_FolderItem"                    )
   IF_IID(CLSID_FolderItemsFDF                , "CLSID_FolderItemsFDF"                )
   IF_IID(CLSID_NewMenu                       , "CLSID_NewMenu"                       )
   IF_IID(BHID_SFObject                       , "BHID_SFObject"                       )
   IF_IID(BHID_SFUIObject                     , "BHID_SFUIObject"                     )
   IF_IID(BHID_SFViewObject                   , "BHID_SFViewObject"                   )
   IF_IID(BHID_Storage                        , "BHID_Storage"                        )
   IF_IID(BHID_Stream                         , "BHID_Stream"                         )
   IF_IID(BHID_LinkTargetItem                 , "BHID_LinkTargetItem"                 )
   IF_IID(BHID_StorageEnum                    , "BHID_StorageEnum"                    )
#if _WIN32_IE >= 0x0600
   IF_IID(SID_CtxQueryAssociations            , "SID_CtxQueryAssociations"            )
#endif // _WIN32_IE >= 0x0600
   IF_IID(IID_IDocViewSite                    , "IID_IDocViewSite"                    )
   IF_IID(CLSID_QuickLinks                    , "CLSID_QuickLinks"                    )
   IF_IID(CLSID_ISFBand                       , "CLSID_ISFBand"                       )
   IF_IID(IID_CDefView                        , "IID_CDefView"                        )
   IF_IID(CLSID_ShellFldSetExt                , "CLSID_ShellFldSetExt"                )
   IF_IID(SID_SMenuBandChild                  , "SID_SMenuBandChild"                  )
   IF_IID(SID_SMenuBandParent                 , "SID_SMenuBandParent"                 )
   IF_IID(SID_SMenuPopup                      , "SID_SMenuPopup"                      )
   IF_IID(SID_SMenuBandBottomSelected         , "SID_SMenuBandBottomSelected"         )
   IF_IID(SID_SMenuBandBottom                 , "SID_SMenuBandBottom"                 )
   IF_IID(SID_MenuShellFolder                 , "SID_MenuShellFolder"                 )
   IF_IID(CGID_MENUDESKBAR                    , "CGID_MENUDESKBAR"                    )
   IF_IID(SID_SMenuBandTop                    , "SID_SMenuBandTop"                    )
   IF_IID(CLSID_MenuToolbarBase               , "CLSID_MenuToolbarBase"               )
   IF_IID(IID_IBanneredBar                    , "IID_IBanneredBar"                    )
   IF_IID(CLSID_MenuBandSite                  , "CLSID_MenuBandSite"                  )
   IF_IID(SID_SCommDlgBrowser                 , "SID_SCommDlgBrowser"                 )
   IF_IID(IID_ITargetFrame                    , "IID_ITargetFrame"                    )
   IF_IID(IID_ITargetEmbedding                , "IID_ITargetEmbedding"                )
   IF_IID(IID_ITargetFramePriv                , "IID_ITargetFramePriv"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IMimeInfo                       , "IID_IMimeInfo"                       )
   IF_IID(IID_IEnumSTATURL                    , "IID_IEnumSTATURL"                    )
   IF_IID(IID_IUrlHistoryStg                  , "IID_IUrlHistoryStg"                  )
   IF_IID(IID_IUrlHistoryStg2                 , "IID_IUrlHistoryStg2"                 )
   IF_IID(IID_IUrlHistoryNotify               , "IID_IUrlHistoryNotify"               )
   IF_IID(IID_IHTMLFiltersCollection          , "IID_IHTMLFiltersCollection"          )
   IF_IID(LIBID_MSHTML                        , "LIBID_MSHTML"                        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IElementBehaviorSite            , "IID_IElementBehaviorSite"            )
   IF_IID(IID_IElementBehavior                , "IID_IElementBehavior"                )
   IF_IID(IID_IElementBehaviorFactory         , "IID_IElementBehaviorFactory"         )
   IF_IID(IID_IElementBehaviorRender          , "IID_IElementBehaviorRender"          )
   IF_IID(IID_IElementBehaviorSiteRender      , "IID_IElementBehaviorSiteRender"      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyle                      , "IID_IHTMLStyle"                      )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyle2                     , "IID_IHTMLStyle2"                     )
   IF_IID(IID_IHTMLStyle3                     , "IID_IHTMLStyle3"                     )
   IF_IID(IID_IHTMLStyle4                     , "IID_IHTMLStyle4"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLRuleStyle                  , "IID_IHTMLRuleStyle"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLRuleStyle2                 , "IID_IHTMLRuleStyle2"                 )
   IF_IID(IID_IHTMLRuleStyle3                 , "IID_IHTMLRuleStyle3"                 )
   IF_IID(IID_IHTMLRuleStyle4                 , "IID_IHTMLRuleStyle4"                 )
   IF_IID(DIID_DispHTMLStyle                  , "DIID_DispHTMLStyle"                  )
   IF_IID(DIID_DispHTMLRuleStyle              , "DIID_DispHTMLRuleStyle"              )
   IF_IID(IID_IHTMLRenderStyle                , "IID_IHTMLRenderStyle"                )
   IF_IID(DIID_DispHTMLRenderStyle            , "DIID_DispHTMLRenderStyle"            )
   IF_IID(IID_IHTMLCurrentStyle               , "IID_IHTMLCurrentStyle"               )
   IF_IID(IID_IHTMLCurrentStyle2              , "IID_IHTMLCurrentStyle2"              )
   IF_IID(IID_IHTMLCurrentStyle3              , "IID_IHTMLCurrentStyle3"              )
   IF_IID(DIID_DispHTMLCurrentStyle           , "DIID_DispHTMLCurrentStyle"           )
   IF_IID(IID_IHTMLRect                       , "IID_IHTMLRect"                       )
   IF_IID(IID_IHTMLRectCollection             , "IID_IHTMLRectCollection"             )
   IF_IID(IID_IHTMLDOMNode                    , "IID_IHTMLDOMNode"                    )
   IF_IID(IID_IHTMLDOMNode2                   , "IID_IHTMLDOMNode2"                   )
   IF_IID(IID_IHTMLDOMAttribute               , "IID_IHTMLDOMAttribute"               )
   IF_IID(IID_IHTMLDOMAttribute2              , "IID_IHTMLDOMAttribute2"              )
   IF_IID(IID_IHTMLDOMTextNode                , "IID_IHTMLDOMTextNode"                )
   IF_IID(IID_IHTMLDOMTextNode2               , "IID_IHTMLDOMTextNode2"               )
   IF_IID(IID_IHTMLDOMImplementation          , "IID_IHTMLDOMImplementation"          )
   IF_IID(DIID_DispHTMLDOMAttribute           , "DIID_DispHTMLDOMAttribute"           )
   IF_IID(DIID_DispHTMLDOMTextNode            , "DIID_DispHTMLDOMTextNode"            )
   IF_IID(DIID_DispHTMLDOMImplementation      , "DIID_DispHTMLDOMImplementation"      )
   IF_IID(IID_IHTMLAttributeCollection        , "IID_IHTMLAttributeCollection"        )
   IF_IID(IID_IHTMLAttributeCollection2       , "IID_IHTMLAttributeCollection2"       )
   IF_IID(IID_IHTMLDOMChildrenCollection      , "IID_IHTMLDOMChildrenCollection"      )
   IF_IID(DIID_DispHTMLAttributeCollection    , "DIID_DispHTMLAttributeCollection"    )
   IF_IID(DIID_DispDOMChildrenCollection      , "DIID_DispDOMChildrenCollection"      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLElementEvents              , "DIID_HTMLElementEvents"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLElementEvents2             , "DIID_HTMLElementEvents2"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLElement                    , "IID_IHTMLElement"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLElement2                   , "IID_IHTMLElement2"                   )
   IF_IID(IID_IHTMLElement3                   , "IID_IHTMLElement3"                   )
   IF_IID(IID_IHTMLElement4                   , "IID_IHTMLElement4"                   )
   IF_IID(IID_IHTMLElementRender              , "IID_IHTMLElementRender"              )
   IF_IID(IID_IHTMLUniqueName                 , "IID_IHTMLUniqueName"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDatabinding                , "IID_IHTMLDatabinding"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLElementDefaults            , "IID_IHTMLElementDefaults"            )
   IF_IID(DIID_DispHTMLDefaults               , "DIID_DispHTMLDefaults"               )
   IF_IID(IID_IHTCDefaultDispatch             , "IID_IHTCDefaultDispatch"             )
   IF_IID(IID_IHTCPropertyBehavior            , "IID_IHTCPropertyBehavior"            )
   IF_IID(IID_IHTCMethodBehavior              , "IID_IHTCMethodBehavior"              )
   IF_IID(IID_IHTCEventBehavior               , "IID_IHTCEventBehavior"               )
   IF_IID(IID_IHTCAttachBehavior              , "IID_IHTCAttachBehavior"              )
   IF_IID(IID_IHTCAttachBehavior2             , "IID_IHTCAttachBehavior2"             )
   IF_IID(IID_IHTCDescBehavior                , "IID_IHTCDescBehavior"                )
   IF_IID(DIID_DispHTCDefaultDispatch         , "DIID_DispHTCDefaultDispatch"         )
   IF_IID(DIID_DispHTCPropertyBehavior        , "DIID_DispHTCPropertyBehavior"        )
   IF_IID(DIID_DispHTCMethodBehavior          , "DIID_DispHTCMethodBehavior"          )
   IF_IID(DIID_DispHTCEventBehavior           , "DIID_DispHTCEventBehavior"           )
   IF_IID(DIID_DispHTCAttachBehavior          , "DIID_DispHTCAttachBehavior"          )
   IF_IID(DIID_DispHTCDescBehavior            , "DIID_DispHTCDescBehavior"            )
   IF_IID(IID_IHTMLUrnCollection              , "IID_IHTMLUrnCollection"              )
   IF_IID(IID_IHTMLGenericElement             , "IID_IHTMLGenericElement"             )
   IF_IID(DIID_DispHTMLGenericElement         , "DIID_DispHTMLGenericElement"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleSheetRule             , "IID_IHTMLStyleSheetRule"             )
   IF_IID(IID_IHTMLStyleSheetRulesCollection  , "IID_IHTMLStyleSheetRulesCollection"  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleSheetPage             , "IID_IHTMLStyleSheetPage"             )
   IF_IID(IID_IHTMLStyleSheetPagesCollection  , "IID_IHTMLStyleSheetPagesCollection"  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleSheetsCollection      , "IID_IHTMLStyleSheetsCollection"      )
   IF_IID(IID_IHTMLStyleSheet                 , "IID_IHTMLStyleSheet"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleSheet2                , "IID_IHTMLStyleSheet2"                )
   IF_IID(DIID_DispHTMLStyleSheet             , "DIID_DispHTMLStyleSheet"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLLinkElementEvents          , "DIID_HTMLLinkElementEvents"          )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLLinkElementEvents2         , "DIID_HTMLLinkElementEvents2"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLinkElement                , "IID_IHTMLLinkElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLinkElement2               , "IID_IHTMLLinkElement2"               )
   IF_IID(IID_IHTMLLinkElement3               , "IID_IHTMLLinkElement3"               )
   IF_IID(DIID_DispHTMLLinkElement            , "DIID_DispHTMLLinkElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTxtRange                   , "IID_IHTMLTxtRange"                   )
   IF_IID(IID_IHTMLTextRangeMetrics           , "IID_IHTMLTextRangeMetrics"           )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTextRangeMetrics2          , "IID_IHTMLTextRangeMetrics2"          )
   IF_IID(IID_IHTMLTxtRangeCollection         , "IID_IHTMLTxtRangeCollection"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLFormElementEvents          , "DIID_HTMLFormElementEvents"          )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLFormElementEvents2         , "DIID_HTMLFormElementEvents2"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFormElement                , "IID_IHTMLFormElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFormElement2               , "IID_IHTMLFormElement2"               )
   IF_IID(IID_IHTMLFormElement3               , "IID_IHTMLFormElement3"               )
   IF_IID(IID_IHTMLSubmitData                 , "IID_IHTMLSubmitData"                 )
   IF_IID(DIID_DispHTMLFormElement            , "DIID_DispHTMLFormElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLControlElementEvents       , "DIID_HTMLControlElementEvents"       )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLControlElementEvents2      , "DIID_HTMLControlElementEvents2"      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLControlElement             , "IID_IHTMLControlElement"             )
   IF_IID(IID_IHTMLTextElement                , "IID_IHTMLTextElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLTextElement            , "DIID_DispHTMLTextElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLTextContainerEvents        , "DIID_HTMLTextContainerEvents"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLTextContainerEvents2       , "DIID_HTMLTextContainerEvents2"       )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTextContainer              , "IID_IHTMLTextContainer"              )
   IF_IID(IID_IHTMLControlRange               , "IID_IHTMLControlRange"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLControlRange2              , "IID_IHTMLControlRange2"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLImgEvents                  , "DIID_HTMLImgEvents"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLImgEvents2                 , "DIID_HTMLImgEvents2"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLImgElement                 , "IID_IHTMLImgElement"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLImgElement2                , "IID_IHTMLImgElement2"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLImageElementFactory        , "IID_IHTMLImageElementFactory"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLImg                    , "DIID_DispHTMLImg"                    )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBodyElement                , "IID_IHTMLBodyElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBodyElement2               , "IID_IHTMLBodyElement2"               )
   IF_IID(DIID_DispHTMLBody                   , "DIID_DispHTMLBody"                   )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFontElement                , "IID_IHTMLFontElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLFontElement            , "DIID_DispHTMLFontElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLAnchorEvents               , "DIID_HTMLAnchorEvents"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
  IF_IID(DIID_HTMLAnchorEvents2              , "DIID_HTMLAnchorEvents2"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLAnchorElement              , "IID_IHTMLAnchorElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLAnchorElement2             , "IID_IHTMLAnchorElement2"             )
   IF_IID(DIID_DispHTMLAnchorElement          , "DIID_DispHTMLAnchorElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLLabelEvents                , "DIID_HTMLLabelEvents"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLLabelEvents2               , "DIID_HTMLLabelEvents2"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLabelElement               , "IID_IHTMLLabelElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLabelElement2              , "IID_IHTMLLabelElement2"              )
   IF_IID(DIID_DispHTMLLabelElement           , "DIID_DispHTMLLabelElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLListElement                , "IID_IHTMLListElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLListElement2               , "IID_IHTMLListElement2"               )
   IF_IID(DIID_DispHTMLListElement            , "DIID_DispHTMLListElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLUListElement               , "IID_IHTMLUListElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLUListElement           , "DIID_DispHTMLUListElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLOListElement               , "IID_IHTMLOListElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLOListElement           , "DIID_DispHTMLOListElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLIElement                  , "IID_IHTMLLIElement"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLLIElement              , "DIID_DispHTMLLIElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBlockElement               , "IID_IHTMLBlockElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBlockElement2              , "IID_IHTMLBlockElement2"              )
   IF_IID(DIID_DispHTMLBlockElement           , "DIID_DispHTMLBlockElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDivElement                 , "IID_IHTMLDivElement"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLDivElement             , "DIID_DispHTMLDivElement"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDDElement                  , "IID_IHTMLDDElement"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLDDElement              , "DIID_DispHTMLDDElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDTElement                  , "IID_IHTMLDTElement"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLDTElement              , "DIID_DispHTMLDTElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBRElement                  , "IID_IHTMLBRElement"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLBRElement              , "DIID_DispHTMLBRElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDListElement               , "IID_IHTMLDListElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLDListElement           , "DIID_DispHTMLDListElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLHRElement                  , "IID_IHTMLHRElement"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLHRElement              , "DIID_DispHTMLHRElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLParaElement                , "IID_IHTMLParaElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLParaElement            , "DIID_DispHTMLParaElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLElementCollection          , "IID_IHTMLElementCollection"          )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLElementCollection2         , "IID_IHTMLElementCollection2"         )
   IF_IID(IID_IHTMLElementCollection3         , "IID_IHTMLElementCollection3"         )
   IF_IID(DIID_DispHTMLElementCollection      , "DIID_DispHTMLElementCollection"      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLHeaderElement              , "IID_IHTMLHeaderElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLHeaderElement          , "DIID_DispHTMLHeaderElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLSelectElementEvents        , "DIID_HTMLSelectElementEvents"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLSelectElementEvents2       , "DIID_HTMLSelectElementEvents2"       )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSelectElement              , "IID_IHTMLSelectElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSelectElement2             , "IID_IHTMLSelectElement2"             )
   IF_IID(IID_IHTMLSelectElement4             , "IID_IHTMLSelectElement4"             )
   IF_IID(DIID_DispHTMLSelectElement          , "DIID_DispHTMLSelectElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSelectionObject            , "IID_IHTMLSelectionObject"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSelectionObject2           , "IID_IHTMLSelectionObject2"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLOptionElement              , "IID_IHTMLOptionElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLOptionElement3             , "IID_IHTMLOptionElement3"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLOptionElementFactory       , "IID_IHTMLOptionElementFactory"       )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLOptionElement          , "DIID_DispHTMLOptionElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLButtonElementEvents        , "DIID_HTMLButtonElementEvents"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLButtonElementEvents2       , "DIID_HTMLButtonElementEvents2"       )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputTextElementEvents     , "DIID_HTMLInputTextElementEvents"     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputTextElementEvents2    , "DIID_HTMLInputTextElementEvents2"    )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLOptionButtonElementEvents  , "DIID_HTMLOptionButtonElementEvents"  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLOptionButtonElementEvents2 , "DIID_HTMLOptionButtonElementEvents2" )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputFileElementEvents     , "DIID_HTMLInputFileElementEvents"     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputFileElementEvents2    , "DIID_HTMLInputFileElementEvents2"    )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputImageEvents           , "DIID_HTMLInputImageEvents"           )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLInputImageEvents2          , "DIID_HTMLInputImageEvents2"          )
   IF_IID(IID_IHTMLInputElement               , "IID_IHTMLInputElement"               )
   IF_IID(IID_IHTMLInputElement2              , "IID_IHTMLInputElement2"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLInputButtonElement         , "IID_IHTMLInputButtonElement"         )
   IF_IID(IID_IHTMLInputHiddenElement         , "IID_IHTMLInputHiddenElement"         )
   IF_IID(IID_IHTMLInputTextElement           , "IID_IHTMLInputTextElement"           )
   IF_IID(IID_IHTMLInputFileElement           , "IID_IHTMLInputFileElement"           )
   IF_IID(IID_IHTMLOptionButtonElement        , "IID_IHTMLOptionButtonElement"        )
   IF_IID(IID_IHTMLInputImage                 , "IID_IHTMLInputImage"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLInputElement           , "DIID_DispHTMLInputElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTextAreaElement            , "IID_IHTMLTextAreaElement"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLTextAreaElement        , "DIID_DispHTMLTextAreaElement"        )
   IF_IID(DIID_DispHTMLRichtextElement        , "DIID_DispHTMLRichtextElement"        )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLButtonElement              , "IID_IHTMLButtonElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLButtonElement          , "DIID_DispHTMLButtonElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLMarqueeElementEvents       , "DIID_HTMLMarqueeElementEvents"       )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLMarqueeElementEvents2      , "DIID_HTMLMarqueeElementEvents2"      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLMarqueeElement             , "IID_IHTMLMarqueeElement"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLMarqueeElement         , "DIID_DispHTMLMarqueeElement"         )
   IF_IID(IID_IHTMLHtmlElement                , "IID_IHTMLHtmlElement"                )
   IF_IID(IID_IHTMLHeadElement                , "IID_IHTMLHeadElement"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTitleElement               , "IID_IHTMLTitleElement"               )
   IF_IID(IID_IHTMLMetaElement                , "IID_IHTMLMetaElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLMetaElement2               , "IID_IHTMLMetaElement2"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBaseElement                , "IID_IHTMLBaseElement"                )
   IF_IID(IID_IHTMLIsIndexElement             , "IID_IHTMLIsIndexElement"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLIsIndexElement2            , "IID_IHTMLIsIndexElement2"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLNextIdElement              , "IID_IHTMLNextIdElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLHtmlElement            , "DIID_DispHTMLHtmlElement"            )
   IF_IID(DIID_DispHTMLHeadElement            , "DIID_DispHTMLHeadElement"            )
   IF_IID(DIID_DispHTMLTitleElement           , "DIID_DispHTMLTitleElement"           )
   IF_IID(DIID_DispHTMLMetaElement            , "DIID_DispHTMLMetaElement"            )
   IF_IID(DIID_DispHTMLBaseElement            , "DIID_DispHTMLBaseElement"            )
   IF_IID(DIID_DispHTMLIsIndexElement         , "DIID_DispHTMLIsIndexElement"         )
   IF_IID(DIID_DispHTMLNextIdElement          , "DIID_DispHTMLNextIdElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBaseFontElement            , "IID_IHTMLBaseFontElement"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLBaseFontElement        , "DIID_DispHTMLBaseFontElement"        )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLUnknownElement             , "IID_IHTMLUnknownElement"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLUnknownElement         , "DIID_DispHTMLUnknownElement"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IOmHistory                      , "IID_IOmHistory"                      )
   IF_IID(IID_IHTMLMimeTypesCollection        , "IID_IHTMLMimeTypesCollection"        )
   IF_IID(IID_IHTMLPluginsCollection          , "IID_IHTMLPluginsCollection"          )
   IF_IID(IID_IHTMLOpsProfile                 , "IID_IHTMLOpsProfile"                 )
   IF_IID(IID_IOmNavigator                    , "IID_IOmNavigator"                    )
   IF_IID(IID_IHTMLLocation                   , "IID_IHTMLLocation"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBookmarkCollection         , "IID_IHTMLBookmarkCollection"         )
   IF_IID(IID_IHTMLDataTransfer               , "IID_IHTMLDataTransfer"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLEventObj                   , "IID_IHTMLEventObj"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLEventObj2                  , "IID_IHTMLEventObj2"                  )
   IF_IID(IID_IHTMLEventObj3                  , "IID_IHTMLEventObj3"                  )
   IF_IID(IID_IHTMLEventObj4                  , "IID_IHTMLEventObj4"                  )
   IF_IID(DIID_DispCEventObj                  , "DIID_DispCEventObj"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLWindowEvents               , "DIID_HTMLWindowEvents"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLWindowEvents2              , "DIID_HTMLWindowEvents2"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDocument                   , "IID_IHTMLDocument"                   )
   IF_IID(IID_IHTMLDocument2                  , "IID_IHTMLDocument2"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDocument3                  , "IID_IHTMLDocument3"                  )
   IF_IID(IID_IHTMLDocument4                  , "IID_IHTMLDocument4"                  )
   IF_IID(IID_IHTMLDocument5                  , "IID_IHTMLDocument5"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFramesCollection2          , "IID_IHTMLFramesCollection2"          )
   IF_IID(IID_IHTMLWindow2                    , "IID_IHTMLWindow2"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLWindow3                    , "IID_IHTMLWindow3"                    )
   IF_IID(IID_IHTMLWindow4                    , "IID_IHTMLWindow4"                    )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLScreen                     , "IID_IHTMLScreen"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLScreen2                    , "IID_IHTMLScreen2"                    )
   IF_IID(DIID_DispHTMLScreen                 , "DIID_DispHTMLScreen"                 )
   IF_IID(DIID_DispHTMLWindow2                , "DIID_DispHTMLWindow2"                )
   IF_IID(DIID_DispHTMLWindowProxy            , "DIID_DispHTMLWindowProxy"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLDocumentEvents             , "DIID_HTMLDocumentEvents"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLDocumentEvents2            , "DIID_HTMLDocumentEvents2"            )
   IF_IID(DIID_DispHTMLDocument               , "DIID_DispHTMLDocument"               )
   IF_IID(DIID_DWebBridgeEvents               , "DIID_DWebBridgeEvents"               )
   IF_IID(IID_IWebBridge                      , "IID_IWebBridge"                      )
   IF_IID(IID_IWBScriptControl                , "IID_IWBScriptControl"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLEmbedElement               , "IID_IHTMLEmbedElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLEmbed                  , "DIID_DispHTMLEmbed"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLMapEvents                  , "DIID_HTMLMapEvents"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLMapEvents2                 , "DIID_HTMLMapEvents2"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLAreasCollection            , "IID_IHTMLAreasCollection"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLAreasCollection2           , "IID_IHTMLAreasCollection2"           )
   IF_IID(IID_IHTMLAreasCollection3           , "IID_IHTMLAreasCollection3"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLMapElement                 , "IID_IHTMLMapElement"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLAreasCollection        , "DIID_DispHTMLAreasCollection"        )
   IF_IID(DIID_DispHTMLMapElement             , "DIID_DispHTMLMapElement"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLAreaEvents                 , "DIID_HTMLAreaEvents"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLAreaEvents2                , "DIID_HTMLAreaEvents2"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLAreaElement                , "IID_IHTMLAreaElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLAreaElement            , "DIID_DispHTMLAreaElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableCaption               , "IID_IHTMLTableCaption"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLTableCaption           , "DIID_DispHTMLTableCaption"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLCommentElement             , "IID_IHTMLCommentElement"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLCommentElement2            , "IID_IHTMLCommentElement2"            )
   IF_IID(DIID_DispHTMLCommentElement         , "DIID_DispHTMLCommentElement"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLPhraseElement              , "IID_IHTMLPhraseElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLPhraseElement2             , "IID_IHTMLPhraseElement2"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSpanElement                , "IID_IHTMLSpanElement"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLPhraseElement          , "DIID_DispHTMLPhraseElement"          )
   IF_IID(DIID_DispHTMLSpanElement            , "DIID_DispHTMLSpanElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLTableEvents                , "DIID_HTMLTableEvents"                )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLTableEvents2               , "DIID_HTMLTableEvents2"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTable                      , "IID_IHTMLTable"                      )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTable2                     , "IID_IHTMLTable2"                     )
   IF_IID(IID_IHTMLTable3                     , "IID_IHTMLTable3"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableCol                   , "IID_IHTMLTableCol"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableCol2                  , "IID_IHTMLTableCol2"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableSection               , "IID_IHTMLTableSection"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableSection2              , "IID_IHTMLTableSection2"              )
   IF_IID(IID_IHTMLTableSection3              , "IID_IHTMLTableSection3"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableRow                   , "IID_IHTMLTableRow"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableRow2                  , "IID_IHTMLTableRow2"                  )
   IF_IID(IID_IHTMLTableRow3                  , "IID_IHTMLTableRow3"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableCell                  , "IID_IHTMLTableCell"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLTableCell2                 , "IID_IHTMLTableCell2"                 )
   IF_IID(DIID_DispHTMLTable                  , "DIID_DispHTMLTable"                  )
   IF_IID(DIID_DispHTMLTableCol               , "DIID_DispHTMLTableCol"               )
   IF_IID(DIID_DispHTMLTableSection           , "DIID_DispHTMLTableSection"           )
   IF_IID(DIID_DispHTMLTableRow               , "DIID_DispHTMLTableRow"               )
   IF_IID(DIID_DispHTMLTableCell              , "DIID_DispHTMLTableCell"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLScriptEvents               , "DIID_HTMLScriptEvents"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLScriptEvents2              , "DIID_HTMLScriptEvents2"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLScriptElement              , "IID_IHTMLScriptElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLScriptElement2             , "IID_IHTMLScriptElement2"             )
   IF_IID(DIID_DispHTMLScriptElement          , "DIID_DispHTMLScriptElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLNoShowElement              , "IID_IHTMLNoShowElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLNoShowElement          , "DIID_DispHTMLNoShowElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLObjectElementEvents        , "DIID_HTMLObjectElementEvents"        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLObjectElementEvents2       , "DIID_HTMLObjectElementEvents2"       )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLObjectElement              , "IID_IHTMLObjectElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLObjectElement2             , "IID_IHTMLObjectElement2"             )
   IF_IID(IID_IHTMLObjectElement3             , "IID_IHTMLObjectElement3"             )
   IF_IID(IID_IHTMLParamElement               , "IID_IHTMLParamElement"               )
   IF_IID(DIID_DispHTMLObjectElement          , "DIID_DispHTMLObjectElement"          )
   IF_IID(DIID_DispHTMLParamElement           , "DIID_DispHTMLParamElement"           )
   IF_IID(DIID_HTMLFrameSiteEvents            , "DIID_HTMLFrameSiteEvents"            )
   IF_IID(DIID_HTMLFrameSiteEvents2           , "DIID_HTMLFrameSiteEvents2"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameBase                  , "IID_IHTMLFrameBase"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameBase2                 , "IID_IHTMLFrameBase2"                 )
   IF_IID(IID_IHTMLFrameBase3                 , "IID_IHTMLFrameBase3"                 )
   IF_IID(DIID_DispHTMLFrameBase              , "DIID_DispHTMLFrameBase"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameElement               , "IID_IHTMLFrameElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameElement2              , "IID_IHTMLFrameElement2"              )
   IF_IID(DIID_DispHTMLFrameElement           , "DIID_DispHTMLFrameElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLIFrameElement              , "IID_IHTMLIFrameElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLIFrameElement2             , "IID_IHTMLIFrameElement2"             )
   IF_IID(DIID_DispHTMLIFrame                 , "DIID_DispHTMLIFrame"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDivPosition                , "IID_IHTMLDivPosition"                )
   IF_IID(IID_IHTMLFieldSetElement            , "IID_IHTMLFieldSetElement"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFieldSetElement2           , "IID_IHTMLFieldSetElement2"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLegendElement              , "IID_IHTMLLegendElement"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLLegendElement2             , "IID_IHTMLLegendElement2"             )
   IF_IID(DIID_DispHTMLDivPosition            , "DIID_DispHTMLDivPosition"            )
   IF_IID(DIID_DispHTMLFieldSetElement        , "DIID_DispHTMLFieldSetElement"        )
   IF_IID(DIID_DispHTMLLegendElement          , "DIID_DispHTMLLegendElement"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLSpanFlow                   , "IID_IHTMLSpanFlow"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLSpanFlow               , "DIID_DispHTMLSpanFlow"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameSetElement            , "IID_IHTMLFrameSetElement"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFrameSetElement2           , "IID_IHTMLFrameSetElement2"           )
   IF_IID(DIID_DispHTMLFrameSetSite           , "DIID_DispHTMLFrameSetSite"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLBGsound                    , "IID_IHTMLBGsound"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLBGsound                , "DIID_DispHTMLBGsound"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLFontNamesCollection        , "IID_IHTMLFontNamesCollection"        )
   IF_IID(IID_IHTMLFontSizesCollection        , "IID_IHTMLFontSizesCollection"        )
   IF_IID(IID_IHTMLOptionsHolder              , "IID_IHTMLOptionsHolder"              )
   IF_IID(DIID_HTMLStyleElementEvents         , "DIID_HTMLStyleElementEvents"         )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_HTMLStyleElementEvents2        , "DIID_HTMLStyleElementEvents2"        )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleElement               , "IID_IHTMLStyleElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispHTMLStyleElement           , "DIID_DispHTMLStyleElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLStyleFontFace              , "IID_IHTMLStyleFontFace"              )
   IF_IID(IID_ICSSFilterSite                  , "IID_ICSSFilterSite"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IMarkupContainer                , "IID_IMarkupContainer"                )
   IF_IID(IID_IMarkupContainer2               , "IID_IMarkupContainer2"               )
   IF_IID(IID_IHTMLChangeLog                  , "IID_IHTMLChangeLog"                  )
   IF_IID(IID_IHTMLChangeSink                 , "IID_IHTMLChangeSink"                 )
   IF_IID(IID_IActiveIMMApp                   , "IID_IActiveIMMApp"                   )
   IF_IID(IID_ISegmentList                    , "IID_ISegmentList"                    )
   IF_IID(IID_ISegmentListIterator            , "IID_ISegmentListIterator"            )
   IF_IID(IID_IHTMLCaret                      , "IID_IHTMLCaret"                      )
   IF_IID(IID_ISegment                        , "IID_ISegment"                        )
   IF_IID(IID_IElementSegment                 , "IID_IElementSegment"                 )
   IF_IID(IID_IHighlightSegment               , "IID_IHighlightSegment"               )
   IF_IID(IID_IHighlightRenderingServices     , "IID_IHighlightRenderingServices"     )
   IF_IID(IID_ILineInfo                       , "IID_ILineInfo"                       )
   IF_IID(IID_IDisplayPointer                 , "IID_IDisplayPointer"                 )
   IF_IID(IID_IDisplayServices                , "IID_IDisplayServices"                )
   IF_IID(IID_IHtmlDlgSafeHelper              , "IID_IHtmlDlgSafeHelper"              )
   IF_IID(IID_IBlockFormats                   , "IID_IBlockFormats"                   )
   IF_IID(IID_IFontNames                      , "IID_IFontNames"                      )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ICSSFilter                      , "IID_ICSSFilter"                      )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISecureUrlHost                  , "IID_ISecureUrlHost"                  )
   IF_IID(IID_IMarkupServices                 , "IID_IMarkupServices"                 )
   IF_IID(IID_IMarkupServices2                , "IID_IMarkupServices2"                )
   IF_IID(IID_IHTMLChangePlayback             , "IID_IHTMLChangePlayback"             )
   IF_IID(IID_IMarkupPointer                  , "IID_IMarkupPointer"                  )
   IF_IID(IID_IMarkupPointer2                 , "IID_IMarkupPointer2"                 )
   IF_IID(IID_IMarkupTextFrags                , "IID_IMarkupTextFrags"                )
   IF_IID(IID_IXMLGenericParse                , "IID_IXMLGenericParse"                )
   IF_IID(IID_IHTMLEditHost                   , "IID_IHTMLEditHost"                   )
   IF_IID(IID_IHTMLEditHost2                  , "IID_IHTMLEditHost2"                  )
   IF_IID(IID_ISequenceNumber                 , "IID_ISequenceNumber"                 )
   IF_IID(IID_IIMEServices                    , "IID_IIMEServices"                    )
   IF_IID(IID_ISelectionServicesListener      , "IID_ISelectionServicesListener"      )
   IF_IID(IID_ISelectionServices              , "IID_ISelectionServices"              )
   IF_IID(IID_IHTMLEditDesigner               , "IID_IHTMLEditDesigner"               )
   IF_IID(IID_IHTMLEditServices               , "IID_IHTMLEditServices"               )
   IF_IID(IID_IHTMLEditServices2              , "IID_IHTMLEditServices2"              )
   IF_IID(DIID_HTMLNamespaceEvents            , "DIID_HTMLNamespaceEvents"            )
   IF_IID(IID_IHTMLNamespace                  , "IID_IHTMLNamespace"                  )
   IF_IID(IID_IHTMLNamespaceCollection        , "IID_IHTMLNamespaceCollection"        )
   IF_IID(IID_IHTMLPainter                    , "IID_IHTMLPainter"                    )
   IF_IID(IID_IHTMLPaintSite                  , "IID_IHTMLPaintSite"                  )
   IF_IID(IID_IHTMLPainterEventInfo           , "IID_IHTMLPainterEventInfo"           )
   IF_IID(IID_IHTMLPainterOverlay             , "IID_IHTMLPainterOverlay"             )
   IF_IID(IID_IHTMLIPrintCollection           , "IID_IHTMLIPrintCollection"           )
   IF_IID(IID_IEnumPrivacyRecords             , "IID_IEnumPrivacyRecords"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDialog                     , "IID_IHTMLDialog"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHTMLDialog2                    , "IID_IHTMLDialog2"                    )
   IF_IID(IID_IHTMLDialog3                    , "IID_IHTMLDialog3"                    )
   IF_IID(IID_IHTMLModelessInit               , "IID_IHTMLModelessInit"               )
   IF_IID(IID_IHTMLPopup                      , "IID_IHTMLPopup"                      )
   IF_IID(DIID_DispHTMLPopup                  , "DIID_DispHTMLPopup"                  )
   IF_IID(IID_IHTMLAppBehavior                , "IID_IHTMLAppBehavior"                )
   IF_IID(IID_IHTMLAppBehavior2               , "IID_IHTMLAppBehavior2"               )
   IF_IID(IID_IHTMLAppBehavior3               , "IID_IHTMLAppBehavior3"               )
   IF_IID(DIID_DispHTMLAppBehavior            , "DIID_DispHTMLAppBehavior"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DispIHTMLInputButtonElement    , "DIID_DispIHTMLInputButtonElement"    )
   IF_IID(DIID_DispIHTMLInputTextElement      , "DIID_DispIHTMLInputTextElement"      )
   IF_IID(DIID_DispIHTMLInputFileElement      , "DIID_DispIHTMLInputFileElement"      )
   IF_IID(DIID_DispIHTMLOptionButtonElement   , "DIID_DispIHTMLOptionButtonElement"   )
   IF_IID(DIID_DispIHTMLInputImage            , "DIID_DispIHTMLInputImage"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IElementNamespace               , "IID_IElementNamespace"               )
   IF_IID(IID_IElementNamespaceTable          , "IID_IElementNamespaceTable"          )
   IF_IID(IID_IElementNamespaceFactory        , "IID_IElementNamespaceFactory"        )
   IF_IID(IID_IElementNamespaceFactory2       , "IID_IElementNamespaceFactory2"       )
   IF_IID(IID_IElementNamespaceFactoryCallback, "IID_IElementNamespaceFactoryCallback")
   IF_IID(IID_IElementBehaviorSiteOM          , "IID_IElementBehaviorSiteOM"          )
   IF_IID(IID_IElementBehaviorSiteOM2         , "IID_IElementBehaviorSiteOM2"         )
   IF_IID(IID_IElementBehaviorCategory        , "IID_IElementBehaviorCategory"        )
   IF_IID(IID_IElementBehaviorSiteCategory    , "IID_IElementBehaviorSiteCategory"    )
   IF_IID(IID_IElementBehaviorSubmit          , "IID_IElementBehaviorSubmit"          )
   IF_IID(IID_IElementBehaviorFocus           , "IID_IElementBehaviorFocus"           )
   IF_IID(IID_IElementBehaviorLayout          , "IID_IElementBehaviorLayout"          )
   IF_IID(IID_IElementBehaviorLayout2         , "IID_IElementBehaviorLayout2"         )
   IF_IID(IID_IElementBehaviorSiteLayout      , "IID_IElementBehaviorSiteLayout"      )
   IF_IID(IID_IElementBehaviorSiteLayout2     , "IID_IElementBehaviorSiteLayout2"     )
   IF_IID(IID_IHostBehaviorInit               , "IID_IHostBehaviorInit"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IAsyncMoniker                   , "IID_IAsyncMoniker"                   )
   IF_IID(CLSID_StdURLMoniker                 , "CLSID_StdURLMoniker"                 )
   IF_IID(CLSID_HttpProtocol                  , "CLSID_HttpProtocol"                  )
   IF_IID(CLSID_FtpProtocol                   , "CLSID_FtpProtocol"                   )
   IF_IID(CLSID_GopherProtocol                , "CLSID_GopherProtocol"                )
   IF_IID(CLSID_HttpSProtocol                 , "CLSID_HttpSProtocol"                 )
   IF_IID(CLSID_FileProtocol                  , "CLSID_FileProtocol"                  )
   IF_IID(CLSID_MkProtocol                    , "CLSID_MkProtocol"                    )
   IF_IID(CLSID_StdURLProtocol                , "CLSID_StdURLProtocol"                )
   IF_IID(CLSID_UrlMkBindCtx                  , "CLSID_UrlMkBindCtx"                  )
   IF_IID(CLSID_StdEncodingFilterFac          , "CLSID_StdEncodingFilterFac"          )
   IF_IID(CLSID_DeCompMimeFilter              , "CLSID_DeCompMimeFilter"              )
   IF_IID(CLSID_CdlProtocol                   , "CLSID_CdlProtocol"                   )
   IF_IID(CLSID_ClassInstallFilter            , "CLSID_ClassInstallFilter"            )
   IF_IID(IID_IAsyncBindCtx                   , "IID_IAsyncBindCtx"                   )
   IF_IID(IID_IPersistMoniker                 , "IID_IPersistMoniker"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IMonikerProp                    , "IID_IMonikerProp"                    )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IBindProtocol                   , "IID_IBindProtocol"                   )
   IF_IID(IID_IBinding                        , "IID_IBinding"                        )
   IF_IID(IID_IBindStatusCallback             , "IID_IBindStatusCallback"             )
   IF_IID(IID_IAuthenticate                   , "IID_IAuthenticate"                   )
   IF_IID(IID_IHttpNegotiate                  , "IID_IHttpNegotiate"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IHttpNegotiate2                 , "IID_IHttpNegotiate2"                 )
   IF_IID(IID_IWinInetFileStream              , "IID_IWinInetFileStream"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IWindowForBindingUI             , "IID_IWindowForBindingUI"             )
   IF_IID(IID_ICodeInstall                    , "IID_ICodeInstall"                    )
   IF_IID(IID_IWinInetInfo                    , "IID_IWinInetInfo"                    )
   IF_IID(IID_IHttpSecurity                   , "IID_IHttpSecurity"                   )
   IF_IID(IID_IWinInetHttpInfo                , "IID_IWinInetHttpInfo"                )
   IF_IID(IID_IBindHost                       , "IID_IBindHost"                       )
   IF_IID(IID_IInternet                       , "IID_IInternet"                       )
   IF_IID(IID_IInternetBindInfo               , "IID_IInternetBindInfo"               )
   IF_IID(IID_IInternetProtocolRoot           , "IID_IInternetProtocolRoot"           )
   IF_IID(IID_IInternetProtocol               , "IID_IInternetProtocol"               )
   IF_IID(IID_IInternetProtocolSink           , "IID_IInternetProtocolSink"           )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IInternetProtocolSinkStackable  , "IID_IInternetProtocolSinkStackable"  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IInternetSession                , "IID_IInternetSession"                )
   IF_IID(IID_IInternetThreadSwitch           , "IID_IInternetThreadSwitch"           )
   IF_IID(IID_IInternetPriority               , "IID_IInternetPriority"               )
   IF_IID(IID_IInternetProtocolInfo           , "IID_IInternetProtocolInfo"           )
   IF_IID(CLSID_InternetSecurityManager       , "CLSID_InternetSecurityManager"       )
   IF_IID(CLSID_InternetZoneManager           , "CLSID_InternetZoneManager"           )
   IF_IID(SID_SInternetSecurityManager        , "SID_SInternetSecurityManager"        )
   IF_IID(SID_SInternetHostSecurityManager    , "SID_SInternetHostSecurityManager"    )
   IF_IID(IID_IInternetSecurityMgrSite        , "IID_IInternetSecurityMgrSite"        )
   IF_IID(IID_IInternetSecurityManager        , "IID_IInternetSecurityManager"        )
   IF_IID(IID_IInternetHostSecurityManager    , "IID_IInternetHostSecurityManager"    )
   IF_IID(IID_IInternetZoneManager            , "IID_IInternetZoneManager"            )
   IF_IID(CLSID_SoftDistExt                   , "CLSID_SoftDistExt"                   )
   IF_IID(IID_ISoftDistExt                    , "IID_ISoftDistExt"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ICatalogFileInfo                , "IID_ICatalogFileInfo"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IDataFilter                     , "IID_IDataFilter"                     )
   IF_IID(IID_IEncodingFilterFactory          , "IID_IEncodingFilterFactory"          )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IWrappedProtocol                , "IID_IWrappedProtocol"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_IBindHost                       , "SID_IBindHost"                       )
   IF_IID(SID_SBindHost                       , "SID_SBindHost"                       )
   IF_IID(LIBID_SHDocVw                       , "LIBID_SHDocVw"                       )
   IF_IID(IID_IWebBrowser                     , "IID_IWebBrowser"                     )
   IF_IID(IID_IWebBrowser2                    , "IID_IWebBrowser2"                    )
   IF_IID(DIID_DWebBrowserEvents              , "DIID_DWebBrowserEvents"              )
   IF_IID(DIID_DWebBrowserEvents2             , "DIID_DWebBrowserEvents2"             )
   IF_IID(DIID_DShellWindowsEvents            , "DIID_DShellWindowsEvents"            )
   IF_IID(IID_IShellWindows                   , "IID_IShellWindows"                   )
   IF_IID(IID_IShellUIHelper                  , "IID_IShellUIHelper"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(DIID_DShellNameSpaceEvents          , "DIID_DShellNameSpaceEvents"          )
   IF_IID(IID_IShellFavoritesNameSpace        , "IID_IShellFavoritesNameSpace"        )
   IF_IID(IID_IShellNameSpace                 , "IID_IShellNameSpace"                 )
   IF_IID(IID_IScriptErrorList                , "IID_IScriptErrorList"                )
   IF_IID(IID_ISearch                         , "IID_ISearch"                         )
   IF_IID(IID_ISearches                       , "IID_ISearches"                       )
   IF_IID(IID_ISearchAssistantOC              , "IID_ISearchAssistantOC"              )
   IF_IID(IID_ISearchAssistantOC2             , "IID_ISearchAssistantOC2"             )
   IF_IID(IID_ISearchAssistantOC3             , "IID_ISearchAssistantOC3"             )
   IF_IID(DIID__SearchAssistantEvents         , "DIID__SearchAssistantEvents"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_WebBrowser_V1                 , "CLSID_WebBrowser_V1"                 )
   IF_IID(CLSID_WebBrowser                    , "CLSID_WebBrowser"                    )
   IF_IID(CLSID_InternetExplorer              , "CLSID_InternetExplorer"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_ShellBrowserWindow            , "CLSID_ShellBrowserWindow"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_ShellWindows                  , "CLSID_ShellWindows"                  )
   IF_IID(CLSID_ShellUIHelper                 , "CLSID_ShellUIHelper"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_ShellNameSpace                , "CLSID_ShellNameSpace"                )
   IF_IID(CLSID_CScriptErrorList              , "CLSID_CScriptErrorList"              )
   IF_IID(CLSID_SearchAssistantOC             , "CLSID_SearchAssistantOC"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003

   IF_IID(CMDSETID_Forms3                     , "CMDSETID_Forms3"                     )
   IF_IID(CLSID_MHTMLDocument                 , "CLSID_MHTMLDocument"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTADocument                   , "CLSID_HTADocument"                   )
   IF_IID(CLSID_HTMLApplication               , "CLSID_HTMLApplication"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLPluginDocument            , "CLSID_HTMLPluginDocument"            )

 //IF_IID(SID_SEditCommandTarget              , "SID_SEditCommandTarget"              ) // см. ниже
 //IF_IID(CGID_EditStateCommands              , "CGID_EditStateCommands"              ) // см. ниже
 //IF_IID(SID_SHTMLEditHost                   , "SID_SHTMLEditHost"                   ) // см. ниже
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_SHTMLEditServices               , "SID_SHTMLEditServices"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_SHTMLWindow                     , "SID_SHTMLWindow"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(SID_SElementBehaviorFactory         , "SID_SElementBehaviorFactory"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003

 //IF_IID(CLSID_CStyle                        , "CLSID_CStyle"                        )
 //IF_IID(CLSID_CRuleStyle                    , "CLSID_CRuleStyle"                    )
 //IF_IID(CLSID_CRenderStyle                  , "CLSID_CRenderStyle"                  )
 //IF_IID(CLSID_CCurrentStyle                 , "CLSID_CCurrentStyle"                 )
 //IF_IID(CLSID_CAttribute                    , "CLSID_CAttribute"                    )
 //IF_IID(CLSID_CDOMTextNode                  , "CLSID_CDOMTextNode"                  )
 //IF_IID(CLSID_CDOMImplementation            , "CLSID_CDOMImplementation"            )
 //IF_IID(CLSID_CAttrCollectionator           , "CLSID_CAttrCollectionator"           )
 //IF_IID(CLSID_CDOMChildrenCollection        , "CLSID_CDOMChildrenCollection"        )
 //IF_IID(CLSID_CDefaults                     , "CLSID_CDefaults"                     )
 //IF_IID(CLSID_CHtmlComponentDD              , "CLSID_CHtmlComponentDD"              )
 //IF_IID(CLSID_CHtmlComponentProperty        , "CLSID_CHtmlComponentProperty"        )
 //IF_IID(CLSID_CHtmlComponentMethod          , "CLSID_CHtmlComponentMethod"          )
 //IF_IID(CLSID_CHtmlComponentEvent           , "CLSID_CHtmlComponentEvent"           )
 //IF_IID(CLSID_CHtmlComponentAttach          , "CLSID_CHtmlComponentAttach"          )
 //IF_IID(CLSID_CHtmlComponentDesc            , "CLSID_CHtmlComponentDesc"            )
 //IF_IID(CLSID_CPeerUrnCollection            , "CLSID_CPeerUrnCollection"            )
 //IF_IID(CLSID_CGenericElement               , "CLSID_CGenericElement"               )
 //IF_IID(CLSID_CStyleSheetRule               , "CLSID_CStyleSheetRule"               )
 //IF_IID(CLSID_CStyleSheetRuleArray          , "CLSID_CStyleSheetRuleArray"          )
 //IF_IID(CLSID_CStyleSheetPage               , "CLSID_CStyleSheetPage"               )
 //IF_IID(CLSID_CStyleSheetPageArray          , "CLSID_CStyleSheetPageArray"          )
 //IF_IID(CLSID_CStyleSheet                   , "CLSID_CStyleSheet"                   )
 //IF_IID(CLSID_CStyleSheetArray              , "CLSID_CStyleSheetArray"              )
 //IF_IID(CLSID_CLinkElement                  , "CLSID_CLinkElement"                  )
 //IF_IID(CLSID_CFormElement                  , "CLSID_CFormElement"                  )
 //IF_IID(CLSID_CTextElement                  , "CLSID_CTextElement"                  )
 //IF_IID(CLSID_CImgElement                   , "CLSID_CImgElement"                   )
 //IF_IID(CLSID_CImageElementFactory          , "CLSID_CImageElementFactory"          )
 //IF_IID(CLSID_CBodyElement                  , "CLSID_CBodyElement"                  )
 //IF_IID(CLSID_CFontElement                  , "CLSID_CFontElement"                  )
 //IF_IID(CLSID_CAnchorElement                , "CLSID_CAnchorElement"                )
 //IF_IID(CLSID_CLabelElement                 , "CLSID_CLabelElement"                 )
 //IF_IID(CLSID_CListElement                  , "CLSID_CListElement"                  )
 //IF_IID(CLSID_CUListElement                 , "CLSID_CUListElement"                 )
 //IF_IID(CLSID_COListElement                 , "CLSID_COListElement"                 )
 //IF_IID(CLSID_CLIElement                    , "CLSID_CLIElement"                    )
 //IF_IID(CLSID_CBlockElement                 , "CLSID_CBlockElement"                 )
 //IF_IID(CLSID_CDivElement                   , "CLSID_CDivElement"                   )
 //IF_IID(CLSID_CDDElement                    , "CLSID_CDDElement"                    )
 //IF_IID(CLSID_CDTElement                    , "CLSID_CDTElement"                    )
 //IF_IID(CLSID_CBRElement                    , "CLSID_CBRElement"                    )
 //IF_IID(CLSID_CDListElement                 , "CLSID_CDListElement"                 )
 //IF_IID(CLSID_CHRElement                    , "CLSID_CHRElement"                    )
 //IF_IID(CLSID_CParaElement                  , "CLSID_CParaElement"                  )
 //IF_IID(CLSID_CElementCollection            , "CLSID_CElementCollection"            )
 //IF_IID(CLSID_CHeaderElement                , "CLSID_CHeaderElement"                )
 //IF_IID(CLSID_CSelectElement                , "CLSID_CSelectElement"                )
 //IF_IID(CLSID_COptionElement                , "CLSID_COptionElement"                )
 //IF_IID(CLSID_COptionElementFactory         , "CLSID_COptionElementFactory"         )
 //IF_IID(CLSID_CInput                        , "CLSID_CInput"                        )
 //IF_IID(CLSID_CTextArea                     , "CLSID_CTextArea"                     )
 //IF_IID(CLSID_CRichtext                     , "CLSID_CRichtext"                     )
 //IF_IID(CLSID_CButton                       , "CLSID_CButton"                       )
 //IF_IID(CLSID_CMarquee                      , "CLSID_CMarquee"                      )
 //IF_IID(CLSID_CHtmlElement                  , "CLSID_CHtmlElement"                  )
 //IF_IID(CLSID_CHeadElement                  , "CLSID_CHeadElement"                  )
 //IF_IID(CLSID_CTitleElement                 , "CLSID_CTitleElement"                 )
 //IF_IID(CLSID_CMetaElement                  , "CLSID_CMetaElement"                  )
 //IF_IID(CLSID_CBaseElement                  , "CLSID_CBaseElement"                  )
 //IF_IID(CLSID_CIsIndexElement               , "CLSID_CIsIndexElement"               )
 //IF_IID(CLSID_CNextIdElement                , "CLSID_CNextIdElement"                )
 //IF_IID(CLSID_CBaseFontElement              , "CLSID_CBaseFontElement"              )
 //IF_IID(CLSID_CUnknownElement               , "CLSID_CUnknownElement"               )
 //IF_IID(CLSID_COmHistory                    , "CLSID_COmHistory"                    )
 //IF_IID(CLSID_COmNavigator                  , "CLSID_COmNavigator"                  )
 //IF_IID(CLSID_COmLocation                   , "CLSID_COmLocation"                   )
 //IF_IID(CLSID_CFramesCollection             , "CLSID_CFramesCollection"             )
 //IF_IID(CLSID_CScreen                       , "CLSID_CScreen"                       )
 //IF_IID(CLSID_CWindow                       , "CLSID_CWindow"                       )
 //IF_IID(CLSID_COmWindowProxy                , "CLSID_COmWindowProxy"                )
 //IF_IID(CLSID_CDocument                     , "CLSID_CDocument"                     )
 //IF_IID(CLSID_CScriptlet                    , "CLSID_CScriptlet"                    )
 //IF_IID(CLSID_CPluginSite                   , "CLSID_CPluginSite"                   )
 //IF_IID(CLSID_CAreasCollection              , "CLSID_CAreasCollection"              )
 //IF_IID(CLSID_CMapElement                   , "CLSID_CMapElement"                   )
 //IF_IID(CLSID_CAreaElement                  , "CLSID_CAreaElement"                  )
 //IF_IID(CLSID_CTableCaption                 , "CLSID_CTableCaption"                 )
 //IF_IID(CLSID_CCommentElement               , "CLSID_CCommentElement"               )
 //IF_IID(CLSID_CPhraseElement                , "CLSID_CPhraseElement"                )
 //IF_IID(CLSID_CSpanElement                  , "CLSID_CSpanElement"                  )
 //IF_IID(CLSID_CTable                        , "CLSID_CTable"                        )
 //IF_IID(CLSID_CTableCol                     , "CLSID_CTableCol"                     )
 //IF_IID(CLSID_CTableSection                 , "CLSID_CTableSection"                 )
 //IF_IID(CLSID_CTableRow                     , "CLSID_CTableRow"                     )
 //IF_IID(CLSID_CTableCell                    , "CLSID_CTableCell"                    )
 //IF_IID(CLSID_CScriptElement                , "CLSID_CScriptElement"                )
 //IF_IID(CLSID_CNoShowElement                , "CLSID_CNoShowElement"                )
 //IF_IID(CLSID_CObjectElement                , "CLSID_CObjectElement"                )
 //IF_IID(CLSID_CParamElement                 , "CLSID_CParamElement"                 )
 //IF_IID(CLSID_CFrameSite                    , "CLSID_CFrameSite"                    )
 //IF_IID(CLSID_CFrameElement                 , "CLSID_CFrameElement"                 )
 //IF_IID(CLSID_CIFrameElement                , "CLSID_CIFrameElement"                )
 //IF_IID(CLSID_C1DElement                    , "CLSID_C1DElement"                    )
 //IF_IID(CLSID_CFieldSetElement              , "CLSID_CFieldSetElement"              )
 //IF_IID(CLSID_CLegendElement                , "CLSID_CLegendElement"                )
 //IF_IID(CLSID_CSpanSite                     , "CLSID_CSpanSite"                     )
 //IF_IID(CLSID_CFrameSetSite                 , "CLSID_CFrameSetSite"                 )
 //IF_IID(CLSID_CBGsound                      , "CLSID_CBGsound"                      )
 //IF_IID(CLSID_CStyleElement                 , "CLSID_CStyleElement"                 )
 //IF_IID(CLSID_CFontFace                     , "CLSID_CFontFace"                     )
 //IF_IID(CLSID_CHTMLNamespace                , "CLSID_CHTMLNamespace"                )
 //IF_IID(CLSID_CHTMLNamespaceCollection      , "CLSID_CHTMLNamespaceCollection"      )
 //IF_IID(CLSID_CThreadDialogProcParam        , "CLSID_CThreadDialogProcParam"        )
 //IF_IID(CLSID_CHTMLDlg                      , "CLSID_CHTMLDlg"                      )
 //IF_IID(CLSID_CHTMLPopup                    , "CLSID_CHTMLPopup"                    )
 //IF_IID(CLSID_CAppBehavior                  , "CLSID_CAppBehavior"                  )

 //IF_IID(SID_BindHost                        , "SID_BindHost"                        )

 //IF_IID(CLSID_SBS_StdURLMoniker             , "CLSID_SBS_StdURLMoniker"             )
 //IF_IID(CLSID_SBS_HttpProtocol              , "CLSID_SBS_HttpProtocol"              )
 //IF_IID(CLSID_SBS_FtpProtocol               , "CLSID_SBS_FtpProtocol"               )
 //IF_IID(CLSID_SBS_GopherProtocol            , "CLSID_SBS_GopherProtocol"            )
 //IF_IID(CLSID_SBS_HttpSProtocol             , "CLSID_SBS_HttpSProtocol"             )
 //IF_IID(CLSID_SBS_FileProtocol              , "CLSID_SBS_FileProtocol"              )
 //IF_IID(CLSID_SBS_MkProtocol                , "CLSID_SBS_MkProtocol"                )
 //IF_IID(CLSID_SBS_UrlMkBindCtx              , "CLSID_SBS_UrlMkBindCtx"              )
 //IF_IID(CLSID_SBS_SoftDistExt               , "CLSID_SBS_SoftDistExt"               )
 //IF_IID(CLSID_SBS_StdEncodingFilterFac      , "CLSID_SBS_StdEncodingFilterFac"      )
 //IF_IID(CLSID_SBS_DeCompMimeFilter          , "CLSID_SBS_DeCompMimeFilter"          )
 //IF_IID(CLSID_SBS_CdlProtocol               , "CLSID_SBS_CdlProtocol"               )
 //IF_IID(CLSID_SBS_ClassInstallFilter        , "CLSID_SBS_ClassInstallFilter"        )
 //IF_IID(CLSID_SBS_InternetSecurityManager   , "CLSID_SBS_InternetSecurityManager"   )
 //IF_IID(CLSID_SBS_InternetZoneManager       , "CLSID_SBS_InternetZoneManager"       )
 //IF_IID(CLSID_LocalCopyHelper               , "CLSID_LocalCopyHelper"               )
 //IF_IID(BHID_LocalCopyHelper                , "BHID_LocalCopyHelper"                )

   IF_IID(CGID_MSHTML                         , "CGID_MSHTML"                         )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HostDialogHelper              , "CLSID_HostDialogHelper"              )
   IF_IID(IID_IHostDialogHelper               , "IID_IHostDialogHelper"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IDocHostUIHandler               , "IID_IDocHostUIHandler"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IDocHostUIHandler2              , "IID_IDocHostUIHandler2"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ICustomDoc                      , "IID_ICustomDoc"                      )
   IF_IID(IID_IDocHostShowUI                  , "IID_IDocHostShowUI"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IClassFactoryEx                 , "IID_IClassFactoryEx"                 )
#endif // MICROSOFT_SDK_FEBRUARY_2003

   IF_IID(IID_ICreateTypeInfo                 , "IID_ICreateTypeInfo"                 )
   IF_IID(IID_ICreateTypeInfo2                , "IID_ICreateTypeInfo2"                )
   IF_IID(IID_ICreateTypeLib                  , "IID_ICreateTypeLib"                  )
   IF_IID(IID_ICreateTypeLib2                 , "IID_ICreateTypeLib2"                 )
   IF_IID(IID_IDispatch                       , "IID_IDispatch"                       )
   IF_IID(IID_IEnumVARIANT                    , "IID_IEnumVARIANT"                    )
   IF_IID(IID_ITypeComp                       , "IID_ITypeComp"                       )
   IF_IID(IID_ITypeInfo                       , "IID_ITypeInfo"                       )
   IF_IID(IID_ITypeInfo2                      , "IID_ITypeInfo2"                      )
   IF_IID(IID_ITypeLib                        , "IID_ITypeLib"                        )
   IF_IID(IID_ITypeLib2                       , "IID_ITypeLib2"                       )
   IF_IID(IID_ITypeChangeEvents               , "IID_ITypeChangeEvents"               )
   IF_IID(IID_IErrorInfo                      , "IID_IErrorInfo"                      )
   IF_IID(IID_ICreateErrorInfo                , "IID_ICreateErrorInfo"                )
   IF_IID(IID_ISupportErrorInfo               , "IID_ISupportErrorInfo"               )
   IF_IID(IID_ITypeFactory                    , "IID_ITypeFactory"                    )
   IF_IID(IID_ITypeMarshal                    , "IID_ITypeMarshal"                    )
   IF_IID(IID_IRecordInfo                     , "IID_IRecordInfo"                     )
   IF_IID(IID_IErrorLog                       , "IID_IErrorLog"                       )

   IF_IID(CLSID_HTMLStyle                     , "CLSID_HTMLStyle"                     )
   IF_IID(CLSID_HTMLRuleStyle                 , "CLSID_HTMLRuleStyle"                 )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLRenderStyle               , "CLSID_HTMLRenderStyle"               )
   IF_IID(CLSID_HTMLCurrentStyle              , "CLSID_HTMLCurrentStyle"              )
   IF_IID(CLSID_HTMLDOMAttribute              , "CLSID_HTMLDOMAttribute"              )
   IF_IID(CLSID_HTMLDOMTextNode               , "CLSID_HTMLDOMTextNode"               )
   IF_IID(CLSID_HTMLDOMImplementation         , "CLSID_HTMLDOMImplementation"         )
   IF_IID(CLSID_HTMLAttributeCollection       , "CLSID_HTMLAttributeCollection"       )
   IF_IID(CLSID_DOMChildrenCollection         , "CLSID_DOMChildrenCollection"         )
   IF_IID(CLSID_HTMLDefaults                  , "CLSID_HTMLDefaults"                  )
   IF_IID(CLSID_HTCDefaultDispatch            , "CLSID_HTCDefaultDispatch"            )
   IF_IID(CLSID_HTCPropertyBehavior           , "CLSID_HTCPropertyBehavior"           )
   IF_IID(CLSID_HTCMethodBehavior             , "CLSID_HTCMethodBehavior"             )
   IF_IID(CLSID_HTCEventBehavior              , "CLSID_HTCEventBehavior"              )
   IF_IID(CLSID_HTCAttachBehavior             , "CLSID_HTCAttachBehavior"             )
   IF_IID(CLSID_HTCDescBehavior               , "CLSID_HTCDescBehavior"               )
   IF_IID(CLSID_HTMLUrnCollection             , "CLSID_HTMLUrnCollection"             )
   IF_IID(CLSID_HTMLGenericElement            , "CLSID_HTMLGenericElement"            )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLStyleSheetRule            , "CLSID_HTMLStyleSheetRule"            )
   IF_IID(CLSID_HTMLStyleSheetRulesCollection , "CLSID_HTMLStyleSheetRulesCollection" )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLStyleSheetPage            , "CLSID_HTMLStyleSheetPage"            )
   IF_IID(CLSID_HTMLStyleSheetPagesCollection , "CLSID_HTMLStyleSheetPagesCollection" )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLStyleSheet                , "CLSID_HTMLStyleSheet"                )
   IF_IID(CLSID_HTMLStyleSheetsCollection     , "CLSID_HTMLStyleSheetsCollection"     )
   IF_IID(CLSID_HTMLLinkElement               , "CLSID_HTMLLinkElement"               )
   IF_IID(CLSID_HTMLFormElement               , "CLSID_HTMLFormElement"               )
   IF_IID(CLSID_HTMLTextElement               , "CLSID_HTMLTextElement"               )
   IF_IID(CLSID_HTMLImg                       , "CLSID_HTMLImg"                       )
   IF_IID(CLSID_HTMLImageElementFactory       , "CLSID_HTMLImageElementFactory"       )
   IF_IID(CLSID_HTMLBody                      , "CLSID_HTMLBody"                      )
   IF_IID(CLSID_HTMLFontElement               , "CLSID_HTMLFontElement"               )
   IF_IID(CLSID_HTMLAnchorElement             , "CLSID_HTMLAnchorElement"             )
   IF_IID(CLSID_HTMLLabelElement              , "CLSID_HTMLLabelElement"              )
   IF_IID(CLSID_HTMLListElement               , "CLSID_HTMLListElement"               )
   IF_IID(CLSID_HTMLUListElement              , "CLSID_HTMLUListElement"              )
   IF_IID(CLSID_HTMLOListElement              , "CLSID_HTMLOListElement"              )
   IF_IID(CLSID_HTMLLIElement                 , "CLSID_HTMLLIElement"                 )
   IF_IID(CLSID_HTMLBlockElement              , "CLSID_HTMLBlockElement"              )
   IF_IID(CLSID_HTMLDivElement                , "CLSID_HTMLDivElement"                )
   IF_IID(CLSID_HTMLDDElement                 , "CLSID_HTMLDDElement"                 )
   IF_IID(CLSID_HTMLDTElement                 , "CLSID_HTMLDTElement"                 )
   IF_IID(CLSID_HTMLBRElement                 , "CLSID_HTMLBRElement"                 )
   IF_IID(CLSID_HTMLDListElement              , "CLSID_HTMLDListElement"              )
   IF_IID(CLSID_HTMLHRElement                 , "CLSID_HTMLHRElement"                 )
   IF_IID(CLSID_HTMLParaElement               , "CLSID_HTMLParaElement"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLElementCollection         , "CLSID_HTMLElementCollection"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLHeaderElement             , "CLSID_HTMLHeaderElement"             )
   IF_IID(CLSID_HTMLSelectElement             , "CLSID_HTMLSelectElement"             )
   IF_IID(CLSID_HTMLOptionElement             , "CLSID_HTMLOptionElement"             )
   IF_IID(CLSID_HTMLOptionElementFactory      , "CLSID_HTMLOptionElementFactory"      )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLInputElement              , "CLSID_HTMLInputElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLTextAreaElement           , "CLSID_HTMLTextAreaElement"           )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLRichtextElement           , "CLSID_HTMLRichtextElement"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLButtonElement             , "CLSID_HTMLButtonElement"             )
   IF_IID(CLSID_HTMLMarqueeElement            , "CLSID_HTMLMarqueeElement"            )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLHtmlElement               , "CLSID_HTMLHtmlElement"               )
   IF_IID(CLSID_HTMLHeadElement               , "CLSID_HTMLHeadElement"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLTitleElement              , "CLSID_HTMLTitleElement"              )
   IF_IID(CLSID_HTMLMetaElement               , "CLSID_HTMLMetaElement"               )
   IF_IID(CLSID_HTMLBaseElement               , "CLSID_HTMLBaseElement"               )
   IF_IID(CLSID_HTMLIsIndexElement            , "CLSID_HTMLIsIndexElement"            )
   IF_IID(CLSID_HTMLNextIdElement             , "CLSID_HTMLNextIdElement"             )
   IF_IID(CLSID_HTMLBaseFontElement           , "CLSID_HTMLBaseFontElement"           )
   IF_IID(CLSID_HTMLUnknownElement            , "CLSID_HTMLUnknownElement"            )
   IF_IID(CLSID_HTMLHistory                   , "CLSID_HTMLHistory"                   )
   IF_IID(CLSID_CMimeTypes                    , "CLSID_CMimeTypes"                    )
   IF_IID(CLSID_CPlugins                      , "CLSID_CPlugins"                      )
   IF_IID(CLSID_COpsProfile                   , "CLSID_COpsProfile"                   )
   IF_IID(CLSID_HTMLNavigator                 , "CLSID_HTMLNavigator"                 )
   IF_IID(CLSID_HTMLLocation                  , "CLSID_HTMLLocation"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_CEventObj                     , "CLSID_CEventObj"                     )
   IF_IID(CLSID_FramesCollection              , "CLSID_FramesCollection"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLScreen                    , "CLSID_HTMLScreen"                    )
   IF_IID(CLSID_HTMLWindow2                   , "CLSID_HTMLWindow2"                   )
   IF_IID(CLSID_HTMLWindowProxy               , "CLSID_HTMLWindowProxy"               )
   IF_IID(CLSID_HTMLDocument                  , "CLSID_HTMLDocument"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_Scriptlet                     , "CLSID_Scriptlet"                     )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLEmbed                     , "CLSID_HTMLEmbed"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLAreasCollection           , "CLSID_HTMLAreasCollection"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLMapElement                , "CLSID_HTMLMapElement"                )
   IF_IID(CLSID_HTMLAreaElement               , "CLSID_HTMLAreaElement"               )
   IF_IID(CLSID_HTMLTableCaption              , "CLSID_HTMLTableCaption"              )
   IF_IID(CLSID_HTMLCommentElement            , "CLSID_HTMLCommentElement"            )
   IF_IID(CLSID_HTMLPhraseElement             , "CLSID_HTMLPhraseElement"             )
   IF_IID(CLSID_HTMLSpanElement               , "CLSID_HTMLSpanElement"               )
   IF_IID(CLSID_HTMLTable                     , "CLSID_HTMLTable"                     )
   IF_IID(CLSID_HTMLTableCol                  , "CLSID_HTMLTableCol"                  )
   IF_IID(CLSID_HTMLTableSection              , "CLSID_HTMLTableSection"              )
   IF_IID(CLSID_HTMLTableRow                  , "CLSID_HTMLTableRow"                  )
   IF_IID(CLSID_HTMLTableCell                 , "CLSID_HTMLTableCell"                 )
   IF_IID(CLSID_HTMLScriptElement             , "CLSID_HTMLScriptElement"             )
   IF_IID(CLSID_HTMLNoShowElement             , "CLSID_HTMLNoShowElement"             )
   IF_IID(CLSID_HTMLObjectElement             , "CLSID_HTMLObjectElement"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLParamElement              , "CLSID_HTMLParamElement"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLFrameBase                 , "CLSID_HTMLFrameBase"                 )
   IF_IID(CLSID_HTMLFrameElement              , "CLSID_HTMLFrameElement"              )
   IF_IID(CLSID_HTMLIFrame                    , "CLSID_HTMLIFrame"                    )
   IF_IID(CLSID_HTMLDivPosition               , "CLSID_HTMLDivPosition"               )
   IF_IID(CLSID_HTMLFieldSetElement           , "CLSID_HTMLFieldSetElement"           )
   IF_IID(CLSID_HTMLLegendElement             , "CLSID_HTMLLegendElement"             )
   IF_IID(CLSID_HTMLSpanFlow                  , "CLSID_HTMLSpanFlow"                  )
   IF_IID(CLSID_HTMLFrameSetSite              , "CLSID_HTMLFrameSetSite"              )
   IF_IID(CLSID_HTMLBGsound                   , "CLSID_HTMLBGsound"                   )
   IF_IID(CLSID_HTMLStyleElement              , "CLSID_HTMLStyleElement"              )
   IF_IID(CLSID_HTMLStyleFontFace             , "CLSID_HTMLStyleFontFace"             )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HtmlDlgSafeHelper             , "CLSID_HtmlDlgSafeHelper"             )
   IF_IID(CLSID_BlockFormats                  , "CLSID_BlockFormats"                  )
   IF_IID(CLSID_FontNames                     , "CLSID_FontNames"                     )
   IF_IID(CLSID_HTMLNamespace                 , "CLSID_HTMLNamespace"                 )
   IF_IID(CLSID_HTMLNamespaceCollection       , "CLSID_HTMLNamespaceCollection"       )
   IF_IID(CLSID_ThreadDialogProcParam         , "CLSID_ThreadDialogProcParam"         )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLDialog                    , "CLSID_HTMLDialog"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_HTMLPopup                     , "CLSID_HTMLPopup"                     )
   IF_IID(CLSID_HTMLAppBehavior               , "CLSID_HTMLAppBehavior"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(CLSID_OldHTMLDocument               , "CLSID_OldHTMLDocument"               )
   IF_IID(CLSID_OldHTMLFormElement            , "CLSID_OldHTMLFormElement"            )
   IF_IID(CLSID_HTMLInputButtonElement        , "CLSID_HTMLInputButtonElement"        )
   IF_IID(CLSID_HTMLInputTextElement          , "CLSID_HTMLInputTextElement"          )
   IF_IID(CLSID_HTMLInputFileElement          , "CLSID_HTMLInputFileElement"          )
   IF_IID(CLSID_HTMLOptionButtonElement       , "CLSID_HTMLOptionButtonElement"       )
   IF_IID(CLSID_HTMLInputImage                , "CLSID_HTMLInputImage"                )

#ifdef HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5
   IF_IID(IID_ITargetNotify                   , "IID_ITargetNotify"                   )
   IF_IID(IID_ITargetNotify2                  , "IID_ITargetNotify2"                  )
   IF_IID(IID_ITargetFrame2                   , "IID_ITargetFrame2"                   )
   IF_IID(IID_ITargetContainer                , "IID_ITargetContainer"                )

   IF_IID(IID_IVersionVector                  , "IID_IVersionVector"                  )
   IF_IID(SID_SVersionHost                    , "SID_SVersionHost"                    )
   IF_IID(IID_IVersionHost                    , "IID_IVersionHost"                    )
#endif // HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5

   IF_IID(IID_IMarshal                        , "IID_IMarshal"                        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IMarshal2                       , "IID_IMarshal2"                       )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IMalloc                         , "IID_IMalloc"                         )
   IF_IID(IID_IMallocSpy                      , "IID_IMallocSpy"                      )
   IF_IID(IID_IStdMarshalInfo                 , "IID_IStdMarshalInfo"                 )
   IF_IID(IID_IExternalConnection             , "IID_IExternalConnection"             )
   IF_IID(IID_IMultiQI                        , "IID_IMultiQI"                        )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_AsyncIMultiQI                   , "IID_AsyncIMultiQI"                   )
   IF_IID(IID_IInternalUnknown                , "IID_IInternalUnknown"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IEnumUnknown                    , "IID_IEnumUnknown"                    )
   IF_IID(IID_IBindCtx                        , "IID_IBindCtx"                        )
   IF_IID(IID_IEnumMoniker                    , "IID_IEnumMoniker"                    )
   IF_IID(IID_IRunnableObject                 , "IID_IRunnableObject"                 )
   IF_IID(IID_IRunningObjectTable             , "IID_IRunningObjectTable"             )
   IF_IID(IID_IPersist                        , "IID_IPersist"                        )
   IF_IID(IID_IPersistStream                  , "IID_IPersistStream"                  )
   IF_IID(IID_IMoniker                        , "IID_IMoniker"                        )
   IF_IID(IID_IROTData                        , "IID_IROTData"                        )
   IF_IID(IID_IEnumString                     , "IID_IEnumString"                     )
   IF_IID(IID_ISequentialStream               , "IID_ISequentialStream"               )
   IF_IID(IID_IStream                         , "IID_IStream"                         )
   IF_IID(IID_IEnumSTATSTG                    , "IID_IEnumSTATSTG"                    )
   IF_IID(IID_IStorage                        , "IID_IStorage"                        )
   IF_IID(IID_IPersistFile                    , "IID_IPersistFile"                    )
   IF_IID(IID_IPersistStorage                 , "IID_IPersistStorage"                 )
   IF_IID(IID_ILockBytes                      , "IID_ILockBytes"                      )
   IF_IID(IID_IEnumFORMATETC                  , "IID_IEnumFORMATETC"                  )
   IF_IID(IID_IEnumSTATDATA                   , "IID_IEnumSTATDATA"                   )
   IF_IID(IID_IRootStorage                    , "IID_IRootStorage"                    )
   IF_IID(IID_IAdviseSink                     , "IID_IAdviseSink"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_AsyncIAdviseSink                , "IID_AsyncIAdviseSink"                )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IAdviseSink2                    , "IID_IAdviseSink2"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_AsyncIAdviseSink2               , "IID_AsyncIAdviseSink2"               )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IDataObject                     , "IID_IDataObject"                     )
   IF_IID(IID_IDataAdviseHolder               , "IID_IDataAdviseHolder"               )
   IF_IID(IID_IMessageFilter                  , "IID_IMessageFilter"                  )
   IF_IID(IID_IRpcChannelBuffer               , "IID_IRpcChannelBuffer"               )
   IF_IID(IID_IRpcChannelBuffer2              , "IID_IRpcChannelBuffer2"              )
   IF_IID(IID_IRpcChannelBuffer3              , "IID_IRpcChannelBuffer3"              )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IAsyncRpcChannelBuffer          , "IID_IAsyncRpcChannelBuffer"          )
   IF_IID(IID_IRpcSyntaxNegotiate             , "IID_IRpcSyntaxNegotiate"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IRpcProxyBuffer                 , "IID_IRpcProxyBuffer"                 )
   IF_IID(IID_IRpcStubBuffer                  , "IID_IRpcStubBuffer"                  )
   IF_IID(IID_IPSFactoryBuffer                , "IID_IPSFactoryBuffer"                )
 //IF_IID(IID_IChannelHook                    , "IID_IChannelHook"                    )
 //IF_IID(IID_IClientSecurity                 , "IID_IClientSecurity"                 )
 //IF_IID(IID_IServerSecurity                 , "IID_IServerSecurity"                 )
 //IF_IID(IID_IClassActivator                 , "IID_IClassActivator"                 )
 //IF_IID(IID_IRpcOptions                     , "IID_IRpcOptions"                     )
   IF_IID(IID_IFillLockBytes                  , "IID_IFillLockBytes"                  )
   IF_IID(IID_IProgressNotify                 , "IID_IProgressNotify"                 )
   IF_IID(IID_ILayoutStorage                  , "IID_ILayoutStorage"                  )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IBlockingLock                   , "IID_IBlockingLock"                   )
   IF_IID(IID_ITimeAndNoticeControl           , "IID_ITimeAndNoticeControl"           )
   IF_IID(IID_IOplockStorage                  , "IID_IOplockStorage"                  )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISurrogate                      , "IID_ISurrogate"                      )
   IF_IID(IID_IGlobalInterfaceTable           , "IID_IGlobalInterfaceTable"           )
   IF_IID(IID_IDirectWriterLock               , "IID_IDirectWriterLock"               )
   IF_IID(IID_ISynchronize                    , "IID_ISynchronize"                    )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISynchronizeHandle              , "IID_ISynchronizeHandle"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISynchronizeEvent               , "IID_ISynchronizeEvent"               )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISynchronizeContainer           , "IID_ISynchronizeContainer"           )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ISynchronizeMutex               , "IID_ISynchronizeMutex"               )
   IF_IID(IID_ICancelMethodCalls              , "IID_ICancelMethodCalls"              )
   IF_IID(IID_IAsyncManager                   , "IID_IAsyncManager"                   )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_ICallFactory                    , "IID_ICallFactory"                    )
   IF_IID(IID_IRpcHelper                      , "IID_IRpcHelper"                      )
   IF_IID(IID_IReleaseMarshalBuffers          , "IID_IReleaseMarshalBuffers"          )
#endif // MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IWaitMultiple                   , "IID_IWaitMultiple"                   )
   IF_IID(IID_IUrlMon                         , "IID_IUrlMon"                         )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IForegroundTransfer             , "IID_IForegroundTransfer"             )
   IF_IID(IID_IAddrTrackingControl            , "IID_IAddrTrackingControl"            )
   IF_IID(IID_IAddrExclusionControl           , "IID_IAddrExclusionControl"           )
   IF_IID(IID_IPipeByte                       , "IID_IPipeByte"                       )
   IF_IID(IID_AsyncIPipeByte                  , "IID_AsyncIPipeByte"                  )
   IF_IID(IID_IPipeLong                       , "IID_IPipeLong"                       )
   IF_IID(IID_AsyncIPipeLong                  , "IID_AsyncIPipeLong"                  )
   IF_IID(IID_IPipeDouble                     , "IID_IPipeDouble"                     )
   IF_IID(IID_AsyncIPipeDouble                , "IID_AsyncIPipeDouble"                )
   IF_IID(IID_IThumbnailExtractor             , "IID_IThumbnailExtractor"             )
   IF_IID(IID_IDummyHICONIncluder             , "IID_IDummyHICONIncluder"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
 //IF_IID(IID_IEnumContextProps               , "IID_IEnumContextProps"               )
 //IF_IID(IID_IContext                        , "IID_IContext"                        )
 //IF_IID(IID_IObjContext                     , "IID_IObjContext"                     )
#ifdef MICROSOFT_SDK_FEBRUARY_2003
   IF_IID(IID_IProcessLock                    , "IID_IProcessLock"                    )
   IF_IID(IID_ISurrogateService               , "IID_ISurrogateService"               )
   IF_IID(IID_IComThreadingInfo               , "IID_IComThreadingInfo"               )
   IF_IID(IID_IProcessInitControl             , "IID_IProcessInitControl"             )
#endif // MICROSOFT_SDK_FEBRUARY_2003
 //IF_IID(IID_IInitializeSpy                  , "IID_IInitializeSpy"                  )

 //IF_IID(StdMarshal                          , "StdMarshal"                          )
 //IF_IID(IdentityUnmarshal                   , "IdentityUnmarshal"                   )
 //IF_IID(InProcFreeMarshaler                 , "InProcFreeMarshaler"                 )
 //IF_IID(PSGenObject                         , "PSGenObject"                         )
 //IF_IID(PSClientSite                        , "PSClientSite"                        )
 //IF_IID(PSClassObject                       , "PSClassObject"                       )
 //IF_IID(PSInPlaceActive                     , "PSInPlaceActive"                     )
 //IF_IID(PSInPlaceFrame                      , "PSInPlaceFrame"                      )
 //IF_IID(PSDragDrop                          , "PSDragDrop"                          )
 //IF_IID(PSBindCtx                           , "PSBindCtx"                           )
 //IF_IID(PSEnumerators                       , "PSEnumerators"                       )
 //IF_IID(Picture_Metafile                    , "Picture_Metafile"                    )
 //IF_IID(StaticMetafile                      , "StaticMetafile"                      )
 //IF_IID(Picture_Dib                         , "Picture_Dib"                         )
 //IF_IID(StaticDib                           , "StaticDib"                           )
 //IF_IID(Picture_EnhMetafile                 , "Picture_EnhMetafile"                 )
 //IF_IID(DCOMAccessControl                   , "DCOMAccessControl"                   )
 //IF_IID(ShellDesktop                        , "ShellDesktop"                        )
 //IF_IID(ShellLink                           , "ShellLink"                           )
 //IF_IID(InternetExplorer                    , "InternetExplorer"                    )
 //IF_IID(StdComponentCategoriesMgr           , "StdComponentCategoriesMgr"           )
 //IF_IID(WebCrawlerAgent                     , "WebCrawlerAgent"                     )
 //IF_IID(ShellDispatchInproc                 , "ShellDispatchInproc"                 )
 //IF_IID(OldHTMLFormElement                  , "OldHTMLFormElement"                  )
 //IF_IID(CFontPropPage                       , "CFontPropPage"                       )
 //IF_IID(CColorPropPage                      , "CColorPropPage"                      )
 //IF_IID(CPicturePropPage                    , "CPicturePropPage"                    )
 //IF_IID(StdFont                             , "StdFont"                             )
 //IF_IID(StdPicture                          , "StdPicture"                          )
 //IF_IID(ShellLinkObject                     , "ShellLinkObject"                     )
 //IF_IID(HTMLLocation                        , "HTMLLocation"                        )
 //IF_IID(WebViewFolderContents               , "WebViewFolderContents"               )
 //IF_IID(HTMLDocument                        , "HTMLDocument"                        )
 //IF_IID(HTMLPluginDocument                  , "HTMLPluginDocument"                  )
 //IF_IID(CMultiLanguage                      , "CMultiLanguage"                      )
 //IF_IID(HTMLImg                             , "HTMLImg"                             )
 //IF_IID(HTMLSelectElement                   , "HTMLSelectElement"                   )
 //IF_IID(HTMLTableCell                       , "HTMLTableCell"                       )
 //IF_IID(HTMLAnchorElement                   , "HTMLAnchorElement"                   )
 //IF_IID(HTMLDivPosition                     , "HTMLDivPosition"                     )
 //IF_IID(HTMLBody                            , "HTMLBody"                            )
 //IF_IID(HTMLOptionElement                   , "HTMLOptionElement"                   )
 //IF_IID(HTMLObjectElement                   , "HTMLObjectElement"                   )
 //IF_IID(HTMLFormElement                     , "HTMLFormElement"                     )
 //IF_IID(HTMLHRElement                       , "HTMLHRElement"                       )
 //IF_IID(HTMLEmbed                           , "HTMLEmbed"                           )
 //IF_IID(HTMLUnknownElement                  , "HTMLUnknownElement"                  )
 //IF_IID(HTMLUListElement                    , "HTMLUListElement"                    )
 //IF_IID(HTMLTextElement                     , "HTMLTextElement"                     )
 //IF_IID(HTMLTable                           , "HTMLTable"                           )
 //IF_IID(HTMLTableCol                        , "HTMLTableCol"                        )
 //IF_IID(HTMLTableRow                        , "HTMLTableRow"                        )
 //IF_IID(HTMLPhraseElement                   , "HTMLPhraseElement"                   )
 //IF_IID(HTMLParaElement                     , "HTMLParaElement"                     )
 //IF_IID(HTMLOListElement                    , "HTMLOListElement"                    )
 //IF_IID(HTMLMapElement                      , "HTMLMapElement"                      )
 //IF_IID(HTMLListElement                     , "HTMLListElement"                     )
 //IF_IID(HTMLLIElement                       , "HTMLLIElement"                       )
 //IF_IID(HTMLMetaElement                     , "HTMLMetaElement"                     )
 //IF_IID(HTMLBaseElement                     , "HTMLBaseElement"                     )
 //IF_IID(HTMLLinkElement                     , "HTMLLinkElement"                     )
 //IF_IID(HTMLIsIndexElement                  , "HTMLIsIndexElement"                  )
 //IF_IID(HTMLNextIdElement                   , "HTMLNextIdElement"                   )
 //IF_IID(HTMLHeaderElement                   , "HTMLHeaderElement"                   )
 //IF_IID(HTMLFontElement                     , "HTMLFontElement"                     )
 //IF_IID(HTMLDTElement                       , "HTMLDTElement"                       )
 //IF_IID(HTMLDListElement                    , "HTMLDListElement"                    )
 //IF_IID(HTMLDivElement                      , "HTMLDivElement"                      )
 //IF_IID(HTMLDDElement                       , "HTMLDDElement"                       )
 //IF_IID(HTMLBRElement                       , "HTMLBRElement"                       )
 //IF_IID(HTMLBlockElement                    , "HTMLBlockElement"                    )
 //IF_IID(HTMLBaseFontElement                 , "HTMLBaseFontElement"                 )
 //IF_IID(HTMLAreaElement                     , "HTMLAreaElement"                     )
 //IF_IID(HTMLTitleElement                    , "HTMLTitleElement"                    )
 //IF_IID(HTMLStyle                           , "HTMLStyle"                           )
 //IF_IID(HTMLDialog                          , "HTMLDialog"                          )
 //IF_IID(HTMLScriptElement                   , "HTMLScriptElement"                   )
 //IF_IID(HTMLInputTextElement                , "HTMLInputTextElement"                )
 //IF_IID(HTMLTextAreaElement                 , "HTMLTextAreaElement"                 )
 //IF_IID(HTMLInputFileElement                , "HTMLInputFileElement"                )
 //IF_IID(HTMLInputButtonElement              , "HTMLInputButtonElement"              )
 //IF_IID(HTMLMarqueeElement                  , "HTMLMarqueeElement"                  )
 //IF_IID(HTMLOptionButtonElement             , "HTMLOptionButtonElement"             )
 //IF_IID(HTMLInputImage                      , "HTMLInputImage"                      )
 //IF_IID(HTMLButtonElement                   , "HTMLButtonElement"                   )
 //IF_IID(HTMLStyleSheet                      , "HTMLStyleSheet"                      )
 //IF_IID(HTMLTableSection                    , "HTMLTableSection"                    )
 //IF_IID(HTMLTableCaption                    , "HTMLTableCaption"                    )
 //IF_IID(HTMLFrameBase                       , "HTMLFrameBase"                       )
 //IF_IID(HTMLFrameElement                    , "HTMLFrameElement"                    )
 //IF_IID(HTMLIFrame                          , "HTMLIFrame"                          )
 //IF_IID(HTMLCommentElement                  , "HTMLCommentElement"                  )
 //IF_IID(HTMLFrameSetSite                    , "HTMLFrameSetSite"                    )
 //IF_IID(HTMLLabelElement                    , "HTMLLabelElement"                    )
 //IF_IID(HTMLScreen                          , "HTMLScreen"                          )
 //IF_IID(HTMLBGsound                         , "HTMLBGsound"                         )
 //IF_IID(HTMLStyleElement                    , "HTMLStyleElement"                    )
 //IF_IID(HTMLStyleSheetsCollection           , "HTMLStyleSheetsCollection"           )
 //IF_IID(HTMLNoShowElement                   , "HTMLNoShowElement"                   )
 //IF_IID(HTMLOptionElementFactory            , "HTMLOptionElementFactory"            )
 //IF_IID(HTMLImageElementFactory             , "HTMLImageElementFactory"             )
 //IF_IID(HTMLWindowProxy                     , "HTMLWindowProxy"                     )
 //IF_IID(HTMLStyleSheetRulesCollection       , "HTMLStyleSheetRulesCollection"       )
 //IF_IID(HTMLStyleSheetRule                  , "HTMLStyleSheetRule"                  )
 //IF_IID(HTMLRuleStyle                       , "HTMLRuleStyle"                       )
 //IF_IID(HTMLStyleFontFace                   , "HTMLStyleFontFace"                   )
 //IF_IID(HTMLSpanFlow                        , "HTMLSpanFlow"                        )
 //IF_IID(HTMLFieldSetElement                 , "HTMLFieldSetElement"                 )
 //IF_IID(HTMLLegendElement                   , "HTMLLegendElement"                   )
 //IF_IID(HTMLFiltersCollection               , "HTMLFiltersCollection"               )
 //IF_IID(HTMLSpanElement                     , "HTMLSpanElement"                     )
 //IF_IID(CMimeTypes                          , "CMimeTypes"                          )
 //IF_IID(CPlugins                            , "CPlugins"                            )
 //IF_IID(COpsProfile                         , "COpsProfile"                         )
 //IF_IID(MHTMLDocument                       , "MHTMLDocument"                       )
 //IF_IID(ClassInstallFilter                  , "ClassInstallFilter"                  )
 //IF_IID(CUrlHistory                         , "CUrlHistory"                         )
 //IF_IID(CdlProtocol                         , "CdlProtocol"                         )
 //IF_IID(StdEncodingFilterFac                , "StdEncodingFilterFac"                )
 //IF_IID(TaskbarList                         , "TaskbarList"                         )
 //IF_IID(ShellFolderView                     , "ShellFolderView"                     )
 //IF_IID(CFSIconOverlayManager               , "CFSIconOverlayManager"               )
 //IF_IID(ShellUIHelper                       , "ShellUIHelper"                       )
 //IF_IID(ActiveDesktop                       , "ActiveDesktop"                       )
 //IF_IID(StdHlink                            , "StdHlink"                            )
 //IF_IID(StdHlinkBrowseContext               , "StdHlinkBrowseContext"               )
 //IF_IID(StdURLMoniker                       , "StdURLMoniker"                       )
 //IF_IID(StdURLProtocol                      , "StdURLProtocol"                      )
 //IF_IID(HttpProtocol                        , "HttpProtocol"                        )
 //IF_IID(FtpProtocol                         , "FtpProtocol"                         )
 //IF_IID(GopherProtocol                      , "GopherProtocol"                      )
 //IF_IID(HttpSProtocol                       , "HttpSProtocol"                       )
 //IF_IID(MkProtocol                          , "MkProtocol"                          )
 //IF_IID(FileProtocol                        , "FileProtocol"                        )
 //IF_IID(UrlMkBindCtx                        , "UrlMkBindCtx"                        )
 //IF_IID(InternetSecurityManager             , "InternetSecurityManager"             )
 //IF_IID(InternetZoneManager                 , "InternetZoneManager"                 )
 //IF_IID(CDLAgent                            , "CDLAgent"                            )
 //IF_IID(OverlayIdentifier_SlowFile          , "OverlayIdentifier_SlowFile"          )
 //IF_IID(StockFontPage                       , "StockFontPage"                       )
 //IF_IID(StockColorPage                      , "StockColorPage"                      )
 //IF_IID(StockPicturePage                    , "StockPicturePage"                    )
 //IF_IID(WebBrowser                          , "WebBrowser"                          )
 //IF_IID(DeCompMimeFilter                    , "DeCompMimeFilter"                    )
 //IF_IID(ShellFolderViewOC                   , "ShellFolderViewOC"                   )
 //IF_IID(ShellWindows                        , "ShellWindows"                        )
 //IF_IID(SubscriptionMgr                     , "SubscriptionMgr"                     )
 //IF_IID(ChannelMgr                          , "ChannelMgr"                          )
 //IF_IID(CMLangString                        , "CMLangString"                        )
 //IF_IID(HTMLWindow2                         , "HTMLWindow2"                         )
 //IF_IID(OldHTMLDocument                     , "OldHTMLDocument"                     )
 //IF_IID(CMLangConvertCharset                , "CMLangConvertCharset"                )
 //IF_IID(WebBrowser_V1                       , "WebBrowser_V1"                       )
 //IF_IID(HTMLHistory                         , "HTMLHistory"                         )
 //IF_IID(HTMLNavigator                       , "HTMLNavigator"                       )
 //IF_IID(SoftDistExt                         , "SoftDistExt"                         )
 //IF_IID(CURLSearchHook                      , "CURLSearchHook"                      )
 //IF_IID(ChannelAgent                        , "ChannelAgent"                        )
 //IF_IID(PersistPropset                      , "PersistPropset"                      )
 //IF_IID(ConvertVBX                          , "ConvertVBX"                          )
 //IF_IID(InternetShortcut                    , "InternetShortcut"                    )

#ifdef MICROSOFT_SDK_FEBRUARY_2003
 //IF_IID(IID_IAzAuthorizationStore           , "IID_IAzAuthorizationStore"           )
 //IF_IID(CLSID_AzAuthorizationStore          , "CLSID_AzAuthorizationStore"          )
 //IF_IID(IID_IAzBizRuleContext               , "IID_IAzBizRuleContext"               )
 //IF_IID(CLSID_AzBizRuleContext              , "CLSID_AzBizRuleContext"              )
 //IF_IID(IID_IAzAuthorizationStore           , "IID_IAzAuthorizationStore"           )
 //IF_IID(IID_IAzApplication                  , "IID_IAzApplication"                  )
 //IF_IID(IID_IAzApplications                 , "IID_IAzApplications"                 )
 //IF_IID(IID_IAzOperation                    , "IID_IAzOperation"                    )
 //IF_IID(IID_IAzOperations                   , "IID_IAzOperations"                   )
 //IF_IID(IID_IAzTask                         , "IID_IAzTask"                         )
 //IF_IID(IID_IAzTasks                        , "IID_IAzTasks"                        )
 //IF_IID(IID_IAzScope                        , "IID_IAzScope"                        )
 //IF_IID(IID_IAzScopes                       , "IID_IAzScopes"                       )
 //IF_IID(IID_IAzApplicationGroup             , "IID_IAzApplicationGroup"             )
 //IF_IID(IID_IAzApplicationGroups            , "IID_IAzApplicationGroups"            )
 //IF_IID(IID_IAzRole                         , "IID_IAzRole"                         )
 //IF_IID(IID_IAzRoles                        , "IID_IAzRoles"                        )
 //IF_IID(IID_IAzClientContext                , "IID_IAzClientContext"                )
 //IF_IID(IID_IAzBizRuleContext               , "IID_IAzBizRuleContext"               )
 //IF_IID(LIBID_AZROLESLib                    , "LIBID_AZROLESLib"                    )
 //IF_IID(CLSID_AzAuthorizationStore          , "CLSID_AzAuthorizationStore"          )
 //IF_IID(CLSID_AzBizRuleContext              , "CLSID_AzBizRuleContext"              )
#endif // MICROSOFT_SDK_FEBRUARY_2003

#ifdef MICROSOFT_SDK_FEBRUARY_2003
   {GUID guid = PSGUID_INTERNETSHORTCUT           ;   IF_IID(guid, "PSGUID_INTERNETSHORTCUT"           )}
   {GUID guid = PSGUID_INTERNETSITE               ;   IF_IID(guid, "PSGUID_INTERNETSITE"               )}
   {GUID guid = PSGUID_SHELLDETAILS               ;   IF_IID(guid, "PSGUID_SHELLDETAILS"               )}
   {GUID guid = PSGUID_IMAGEPROPERTIES            ;   IF_IID(guid, "PSGUID_IMAGEPROPERTIES"            )}
   {GUID guid = PSGUID_DISPLACED                  ;   IF_IID(guid, "PSGUID_DISPLACED"                  )}
   {GUID guid = PSGUID_BRIEFCASE                  ;   IF_IID(guid, "PSGUID_BRIEFCASE"                  )}
   {GUID guid = PSGUID_MISC                       ;   IF_IID(guid, "PSGUID_MISC"                       )}
   {GUID guid = PSGUID_WEBVIEW                    ;   IF_IID(guid, "PSGUID_WEBVIEW"                    )}
   {GUID guid = PSGUID_MUSIC                      ;   IF_IID(guid, "PSGUID_MUSIC"                      )}
   {GUID guid = PSGUID_DRM                        ;   IF_IID(guid, "PSGUID_DRM"                        )}
   {GUID guid = PSGUID_VIDEO                      ;   IF_IID(guid, "PSGUID_VIDEO"                      )}
   {GUID guid = PSGUID_AUDIO                      ;   IF_IID(guid, "PSGUID_AUDIO"                      )}
   {GUID guid = PSGUID_CONTROLPANEL               ;   IF_IID(guid, "PSGUID_CONTROLPANEL"               )}
   {GUID guid = PSGUID_VOLUME                     ;   IF_IID(guid, "PSGUID_VOLUME"                     )}
   {GUID guid = PSGUID_SHARE                      ;   IF_IID(guid, "PSGUID_SHARE"                      )}
   {GUID guid = PSGUID_LINK                       ;   IF_IID(guid, "PSGUID_LINK"                       )}
   {GUID guid = PSGUID_QUERY_D                    ;   IF_IID(guid, "PSGUID_QUERY_D"                    )}
   {GUID guid = PSGUID_SUMMARYINFORMATION         ;   IF_IID(guid, "PSGUID_SUMMARYINFORMATION"         )}
   {GUID guid = PSGUID_DOCUMENTSUMMARYINFORMATION ;   IF_IID(guid, "PSGUID_DOCUMENTSUMMARYINFORMATION" )}
   {GUID guid = PSGUID_MEDIAFILESUMMARYINFORMATION;   IF_IID(guid, "PSGUID_MEDIAFILESUMMARYINFORMATION")}
   {GUID guid = PSGUID_IMAGESUMMARYINFORMATION    ;   IF_IID(guid, "PSGUID_IMAGESUMMARYINFORMATION"    )}

   {GUID guid = {0x3050f4b5,0x98b5,0x11cf,0xbb,0x82,0x00,0xaa,0x00,0xbd,0xce,0x0b};   IF_IID(guid, "SID_SEditCommandTarget")}
   {GUID guid = {0x3050f4b6,0x98b5,0x11cf,0xbb,0x82,0x00,0xaa,0x00,0xbd,0xce,0x0b};   IF_IID(guid, "CGID_EditStateCommands")}
   {GUID guid = {0x3050f6a0,0x98b5,0x11cf,0xbb,0x82,0x00,0xaa,0x00,0xbd,0xce,0x0b};   IF_IID(guid, "SID_SHTMLEditHost"     )}
#endif // MICROSOFT_SDK_FEBRUARY_2003

   strRes = strRes.Left(strRes.GetLength()-strSeparator.GetLength());
   return strRes;
}

#undef IF_IID

// Window Styles to string
CString CLogger::WindowStyle(HWND hWnd) {
   CString strRes;

   LONG lStyle = ::GetWindowLong(hWnd, GWL_STYLE);
   if ((lStyle == 0) && (::GetLastError() != ERROR_SUCCESS)) {
      return strRes;
   }

#define ADD_STYLE(strStyle) {strRes += TEXT(" | ")TEXT(strStyle);}
   // Dialog Styles
   if (lStyle &  DS_ABSALIGN     ) ADD_STYLE("DS_ABSALIGN"     );
   if (lStyle &  DS_SYSMODAL     ) ADD_STYLE("DS_SYSMODAL"     );
   if (lStyle &  DS_LOCALEDIT    ) ADD_STYLE("DS_LOCALEDIT"    );
   if (lStyle &  DS_SETFONT      ) ADD_STYLE("DS_SETFONT"      );
   if (lStyle &  DS_MODALFRAME   ) ADD_STYLE("DS_MODALFRAME"   );
   if (lStyle &  DS_NOIDLEMSG    ) ADD_STYLE("DS_NOIDLEMSG"    );
   if (lStyle &  DS_SETFOREGROUND) ADD_STYLE("DS_SETFOREGROUND");
#if (WINVER >= 0x0400)
   if (lStyle &  DS_3DLOOK       ) ADD_STYLE("DS_3DLOOK"       );
   if (lStyle &  DS_FIXEDSYS     ) ADD_STYLE("DS_FIXEDSYS"     );
   if (lStyle &  DS_NOFAILCREATE ) ADD_STYLE("DS_NOFAILCREATE" );
   if (lStyle &  DS_CONTROL      ) ADD_STYLE("DS_CONTROL"      );
   if (lStyle &  DS_CENTER       ) ADD_STYLE("DS_CENTER"       );
   if (lStyle &  DS_CENTERMOUSE  ) ADD_STYLE("DS_CENTERMOUSE"  );
   if (lStyle &  DS_CONTEXTHELP  ) ADD_STYLE("DS_CONTEXTHELP"  );
#endif

   // Window Styles
   if (lStyle & WS_OVERLAPPED  ) ADD_STYLE("WS_OVERLAPPED"  );
   if (lStyle & WS_POPUP       ) ADD_STYLE("WS_POPUP"       );
   if (lStyle & WS_CHILD       ) ADD_STYLE("WS_CHILD"       );
   if (lStyle & WS_MINIMIZE    ) ADD_STYLE("WS_MINIMIZE"    );
   if (lStyle & WS_VISIBLE     ) ADD_STYLE("WS_VISIBLE"     );
   if (lStyle & WS_DISABLED    ) ADD_STYLE("WS_DISABLED"    );
   if (lStyle & WS_CLIPSIBLINGS) ADD_STYLE("WS_CLIPSIBLINGS");
   if (lStyle & WS_CLIPCHILDREN) ADD_STYLE("WS_CLIPCHILDREN");
   if (lStyle & WS_MAXIMIZE    ) ADD_STYLE("WS_MAXIMIZE"    );
   if ((lStyle & WS_CAPTION) == WS_CAPTION) {
      ADD_STYLE("WS_CAPTION");
   } else {
      if (lStyle & WS_BORDER   ) ADD_STYLE("WS_BORDER"      );
      if (lStyle & WS_DLGFRAME ) ADD_STYLE("WS_DLGFRAME"    );
   }
   if (lStyle & WS_VSCROLL     ) ADD_STYLE("WS_VSCROLL"     );
   if (lStyle & WS_HSCROLL     ) ADD_STYLE("WS_HSCROLL"     );
   if (lStyle & WS_SYSMENU     ) ADD_STYLE("WS_SYSMENU"     );
   if (lStyle & WS_THICKFRAME  ) ADD_STYLE("WS_THICKFRAME"  );
   if (lStyle & WS_CHILD) {
      if (lStyle & WS_GROUP       ) ADD_STYLE("WS_GROUP"       );
      if (lStyle & WS_TABSTOP     ) ADD_STYLE("WS_TABSTOP"     );
   } else {
      if (lStyle & WS_MINIMIZEBOX ) ADD_STYLE("WS_MINIMIZEBOX" );
      if (lStyle & WS_MAXIMIZEBOX ) ADD_STYLE("WS_MAXIMIZEBOX" );
   }
#undef ADD_STYLE

   strRes = (LPCTSTR)strRes+3;
   return strRes;
}

CString CLogger::WindowStyleEx(HWND hWnd) {
   CString strRes;

   LONG lStyleEx = ::GetWindowLong(hWnd, GWL_EXSTYLE);
   if ((lStyleEx == 0) && (::GetLastError() != ERROR_SUCCESS)) {
      return strRes;
   }

#define ADD_STYLEEX(strStyleEx) {strRes += TEXT(" | ")TEXT(strStyleEx);}
   if (lStyleEx & WS_EX_DLGMODALFRAME ) ADD_STYLEEX("WS_EX_DLGMODALFRAME" );
   if (lStyleEx & WS_EX_NOPARENTNOTIFY) ADD_STYLEEX("WS_EX_NOPARENTNOTIFY");
   if (lStyleEx & WS_EX_TOPMOST       ) ADD_STYLEEX("WS_EX_TOPMOST"       );
   if (lStyleEx & WS_EX_ACCEPTFILES   ) ADD_STYLEEX("WS_EX_ACCEPTFILES"   );
   if (lStyleEx & WS_EX_TRANSPARENT   ) ADD_STYLEEX("WS_EX_TRANSPARENT"   );
#if (WINVER >= 0x0400)
   if (lStyleEx & WS_EX_MDICHILD      ) ADD_STYLEEX("WS_EX_MDICHILD"      );
   if (lStyleEx & WS_EX_TOOLWINDOW    ) ADD_STYLEEX("WS_EX_TOOLWINDOW"    );
   if (lStyleEx & WS_EX_WINDOWEDGE    ) ADD_STYLEEX("WS_EX_WINDOWEDGE"    );
   if (lStyleEx & WS_EX_CLIENTEDGE    ) ADD_STYLEEX("WS_EX_CLIENTEDGE"    );
   if (lStyleEx & WS_EX_CONTEXTHELP   ) ADD_STYLEEX("WS_EX_CONTEXTHELP"   );

   if (lStyleEx & WS_EX_RIGHT         ) ADD_STYLEEX("WS_EX_RIGHT"         );
 //if (lStyleEx & WS_EX_LEFT          ) ADD_STYLEEX("WS_EX_LEFT"          );
   if (lStyleEx & WS_EX_RTLREADING    ) ADD_STYLEEX("WS_EX_RTLREADING"    );
 //if (lStyleEx & WS_EX_LTRREADING    ) ADD_STYLEEX("WS_EX_LTRREADING"    );
   if (lStyleEx & WS_EX_LEFTSCROLLBAR ) ADD_STYLEEX("WS_EX_LEFTSCROLLBAR" );
 //if (lStyleEx & WS_EX_RIGHTSCROLLBAR) ADD_STYLEEX("WS_EX_RIGHTSCROLLBAR");

   if (lStyleEx & WS_EX_CONTROLPARENT ) ADD_STYLEEX("WS_EX_CONTROLPARENT" );
   if (lStyleEx & WS_EX_STATICEDGE    ) ADD_STYLEEX("WS_EX_STATICEDGE"    );
   if (lStyleEx & WS_EX_APPWINDOW     ) ADD_STYLEEX("WS_EX_APPWINDOW"     );

   //#define WS_EX_OVERLAPPEDWINDOW (WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE)
   //#define WS_EX_PALETTEWINDOW    (WS_EX_WINDOWEDGE | WS_EX_TOOLWINDOW | WS_EX_TOPMOST)
#endif
#undef ADD_STYLEEX

   strRes = (LPCTSTR)strRes+3;
   return strRes;
}
