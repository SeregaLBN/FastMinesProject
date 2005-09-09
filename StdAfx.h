#ifndef __FILE__STDAFX_H__FASTMINES_PROJECT__
#define __FILE__STDAFX_H__FASTMINES_PROJECT__

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information

#define REPLACEBKCOLORFROMFILLWINDOW

#ifdef UNICODE
   #pragma comment(linker, "/ENTRY:wWinMainCRTStartup")
#else
   #pragma comment(linker, "/ENTRY:WinMainCRTStartup")
#endif // UNICODE

#define _WIN32_WINDOWS 0x0410
#define WIN32_LEAN_AND_MEAN // Exclude rarely-used stuff from Windows headers
//#define MICROSOFT_SDK_FEBRUARY_2003                     // установлено Microsoft SDK за February 2003
//#define HEADERS_AND_LIBRARIES_FOR_INTERNET_EXPLORER_5_5 // установлены Headers and Libraries for Internet Explorer 5.5

#include <Windows.h>
#include <WindowsX.h>
#include <ShellApi.h>
#include <CommCtrl.h>
#include <CommDlg.h>
#include <TChar.h>
#include <Time.h>
#include <map>
#include <set>
#include <vector>
#include <math.h>
#include <process.h> // _beginthread, _endthread

#include "ID_resource.h"
#include "WinDefAdd.h"
#include "CStringKS.h"
#include "CommonLib.h"
#include "Lang.h"
#include "Image.h"

#ifdef _DEBUG
   #include "Logger.h"
   extern CLogger g_Log;
#endif

#endif // __FILE__STDAFX_H__FASTMINES_PROJECT__
