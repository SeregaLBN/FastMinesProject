////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Preproc.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_PREPROCESSOR
#define FILE_PREPROCESSOR

//#define UNICODE
#ifdef UNICODE
   #define _UNICODE
#endif

#define REPLACEBKCOLORFROMFILLWINDOW

#define ROBOT_MULTITHREAD

#ifdef _DEBUG
   #define DEBUG_REGISTRATION
   #define IGNORE_REGISTRATION
   #define USE_INFO_DIALOG // only for debug information
#endif // _DEBUG

#define MINE_IS_BITMAP // default image "mine" is bitmap
#ifndef MINE_IS_BITMAP
   #define MINE_IS_ENHMETAFILE // default image "mine" is enhanced-format metafile
#endif // MINE_IS_BITMAP

#define FLAG_IS_BITMAP // default image "flag" is bitmap
#ifndef FLAG_IS_BITMAP
   #define FLAG_IS_ENHMETAFILE // default image "flag" is enhanced-format metafile
#endif // FLAG_IS_BITMAP

//#define PAUSE_IS_BITMAP // default image "pause" is bitmap
#ifndef PAUSE_IS_BITMAP
   #define PAUSE_IS_ENHMETAFILE // default image "pause" is enhanced-format metafile
#endif // PAUSE_IS_BITMAP

#endif // FILE_PREPROCESSOR
