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
      mosaicSquare1,
      mosaicSquare2,
      mosaicTriangle1,
      mosaicTriangle2,
      mosaicHexagon1,
      mosaicPentagonT24,
      mosaicPentagonT10,
      mosaicParquet1,
      mosaicParquet2,
      mosaicTrSq1,
      mosaicSqTrHex,
      mosaicTrapezoid,
      mosaicRhombus1,
      mosaicNil
   };

   enum ESkillLevel {
      skillLevelBeginner,
      skillLevelAmateur,
      skillLevelProfessional,
      skillLevelCrazy,
      skillLevelCustom  //  всегда последняя в перечислении !!!
   };

   struct CFileChmpn {
      CChmpnRecord m_Data[mosaicNil][skillLevelCustom][10];
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
      mosaicTriangle1,
      mosaicTriangle2,
      mosaicTriangle3,
      mosaicTriangle4,
      // Quadrangle
      mosaicSquare1,
      mosaicSquare2,
      mosaicParquet1,
      mosaicParquet2,
      mosaicTrapezoid1,
      mosaicTrapezoid2,
      mosaicTrapezoid3,
      mosaicRhombus1,
      mosaicQuadrangle1,
      // Pentagon
      mosaicPentagonT24,
      mosaicPentagonT5,
      mosaicPentagonT10,
      // Hexagon
      mosaicHexagon1,
      // Other
      mosaicTrSq1,
      mosaicTrSq2,
      mosaicSqTrHex,
      //
      mosaicNil  //  всегда последняя в перечислении !!!
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
      CChmpnRecord m_Data[mosaicNil][skillLevelCustom][10];
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

   const int MAX_PLAYER_NAME_LENGTH = 20;
   const int MAX_PASSWORD_LENGTH    = 20;

   struct CSttstcSubRecord {
      DWORD
         m_dwGameNumber, // количество сыграных игр
         m_dwGameWin,    // количество выиграных игр
         m_dwOpenField,  // суммарное число открытых ячеек - вывожу средний процент открытия поля
         m_dwPlayTime,   // суммарное время игр - вывожу сколько всреднем игрок провёл времени за данной игрой
         m_dwClickCount; // суммарное число кликов - вывожу среднее число кликов в данной игре
   };
   struct CSttstcRecord {
      CHAR             m_szPlayerName[MAX_PLAYER_NAME_LENGTH];
      CHAR             m_szPassword  [MAX_PASSWORD_LENGTH   ];
      CSttstcSubRecord m_Record[mosaicNil][skillLevelCustom];
   };
   struct CSttstcFile {
      CSttstcRecord m_DataStc; // данные об отдельном игроке
      CSttstcRecord m_DataBad; // мусор
      DWORD m_dwCRC;
   };

} // namespace ver210

#endif // __FILE__OLDVERSION__
