////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "OldVersion.h"
// типы данных с прошлых версий
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__OLDVERSION__
#define __FILE__OLDVERSION__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "CommonLib.h"

#define MAX_INTEGER 0x7FFFFFFF

namespace nsVer200 {
   struct CChmpnRecord {
      char m_szName[20];
      int  m_iTime;
   };

   enum EMosaic {
      mosaic_Square1,
      mosaic_Square2,
      mosaic_Triangle1,
      mosaic_Triangle2,
      mosaic_Hexagon1,
      mosaic_PentagonT24,
      mosaic_PentagonT10,
      mosaic_Parquet1,
      mosaic_Parquet2,
      mosaic_TrSq1,
      mosaic_SqTrHex,
      mosaic_Trapezoid,
      mosaic_Rhombus1,
      mosaic_Nil
   };

   enum ESkillLevel {
      skillLevelBeginner,
      skillLevelAmateur,
      skillLevelProfessional,
      skillLevelCrazy,
      skillLevelCustom  //  всегда последняя в перечислении !!!
   };

   struct CFileChmpn {
      CChmpnRecord m_Data[mosaic_Nil][skillLevelCustom][10];
   };
} // namespace ver200

namespace nsVer210 {
   #define ID_VERSIONINFO_VERSION3_v210 "FastMines Project - version 2.1.0."

   struct CChmpnRecord {
      char m_szName[20];
      int  m_iTime;
      CChmpnRecord() {
         m_szName[0] = 0;
         m_iTime = MAX_INTEGER;
      };
   };

   enum EMosaic {
      // Triangle
      mosaic_Triangle1,
      mosaic_Triangle2,
      mosaic_Triangle3,
      mosaic_Triangle4,
      // Quadrangle
      mosaic_Square1,
      mosaic_Square2,
      mosaic_Parquet1,
      mosaic_Parquet2,
      mosaic_Trapezoid1,
      mosaic_Trapezoid2,
      mosaic_Trapezoid3,
      mosaic_Rhombus,
      mosaic_Quadrangle1,
      // Pentagon
      mosaic_Pentagon,
      mosaic_PentagonT5,
      mosaic_PentagonT10,
      // Hexagon
      mosaic_Hexagon,
      // Other
      mosaic_TrSq,
      mosaic_TrSq2,
      mosaic_SqTrHex,
      //
      mosaic_Nil  //  всегда последняя в перечислении !!!
   };

   enum ESkillLevel {
      skillLevelBeginner,
      skillLevelAmateur,
      skillLevelProfessional,
      skillLevelCrazy,
      skillLevelCustom  //  всегда последняя в перечислении !!!
   };

   struct CFileChmpn {
      char m_szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3_v210))];
      CChmpnRecord m_Data[mosaic_Nil][skillLevelCustom][10];
      CFileChmpn() {
         strcpy(m_szVersion, ID_VERSIONINFO_VERSION3_v210);
      }
   };

   enum EPlace {
      placeCenter,
      placeStretch, // pастянуть
      placeTile     // замостить
   };

   enum EOpen {_Nil, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _Mine};
   enum EClose{_Unknown, _Clear, _Flag};

   struct CFileSkin {
      char m_szVersion[chDIMOF(ID_VERSIONINFO_VERSION3_v210)];
      struct {
         struct {
            char m_szPath[MAX_PATH];
            bool m_bTransparent;
            EPlace m_Place;
         } m_Mine, m_Flag, m_Pause, m_Bckgrnd, m_BtnNew[4], m_BtnPause[4];
         COLORREF m_colorBk;
         bool m_bToAll;
         LOGFONT m_Font;
         struct {
            COLORREF m_colorCaptionOpen [_Mine];
            COLORREF m_colorCaptionClose[_Flag];
         } m_ColorText;
         struct {
            COLORREF m_colorShadow,
                     m_colorLight;
            int m_iWidth;
         } m_Border;
      } m_Skin;
      CFileSkin () {
         memset(this, 0, sizeof(*this));
         strcpy(m_szVersion, ID_VERSIONINFO_VERSION3_v210);
      }
   };
} // namespace ver210

#endif // __FILE__OLDVERSION__
