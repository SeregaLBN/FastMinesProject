////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Base.h"
//
// Oписание базового класса CBase
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__BASE__
#define __FILE__BASE__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <set>
#include <math.h>
#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../Image.h"

struct CLeftUpReturn {
   int m_iCountFlag, m_iCountOpen, m_iCountUnknown;
   bool m_bEndGame, m_bVictory;
};

struct CRightDownReturn {
   int m_iCountFlag, m_iCountUnknown;
};

const COORD INCORRECT_COORD = {-1,-1};

namespace nsCell {
   class CBase;

   typedef std::set<const CBase*> SET_cpBase;

   enum EStatus{_Open, _Close};
   enum EOpen  {_Nil, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _Mine};
   enum EClose {_Unknown, _Clear, _Flag};

   const TCHAR SZ_CAPTION_OPEN[_Mine][3] =
   {  TEXT(" \0"), // _Nil
      TEXT("1\0"), // _1
      TEXT("2\0"), // _2
      TEXT("3\0"), // _3
      TEXT("4\0"), // _4
      TEXT("5\0"), // _5
      TEXT("6\0"), // _6
      TEXT("7\0"), // _7
      TEXT("8\0"), // _8
      TEXT("9\0"), // _9
      TEXT("10" ), // _10
      TEXT("11" ), // _11
      TEXT("12" ), // _12
      TEXT("13" ), // _13
      TEXT("14" ), // _14
      TEXT("15" ), // _15
      TEXT("16" ), // _16
      TEXT("17" ), // _17
      TEXT("18" ), // _18
      TEXT("19" ), // _19
      TEXT("20" ), // _20
      TEXT("21" )  // _21
   };

   const TCHAR SZ_CAPTION_CLOSE[_Flag][2] =
   {  TEXT("?"), // _Unknown
      TEXT(" ")  // _Clear
   };

   struct CColorText {
      COLORREF m_colorOpen [_Mine];
      COLORREF m_colorClose[_Flag];
   };

   struct CBorder{
      COLORREF m_colorShadow,
               m_colorLight;
      int m_iWidth;                    //BBGGRR
      CBorder(COLORREF colorShadow = 0x00000000,
              COLORREF colorLight  = 0x00FFFFFF,
              int iWidth = 1):
         m_colorShadow(colorShadow),
         m_colorLight (colorLight),
         m_iWidth     (iWidth)
      {}
   };

   struct CGraphicContext {
      HDC        m_hDCDst,
                 m_hDCBck,
                 m_hDCTmp;
      HPEN       m_hPenShadow,
                 m_hPenLight;
      CImage     m_ImgMine,
                 m_ImgFlag;
      CColorText m_ColorText;
      CBorder    m_Border;
      CGraphicContext(const CColorText &ColorText) :
         m_hDCDst     (NULL),
         m_hDCBck     (NULL),
         m_hDCTmp     (NULL),
         m_hPenShadow (NULL),
         m_hPenLight  (NULL),
         m_ColorText  (ColorText)
      {}
   };

   class CBase {
   private:
      mutable bool m_bPresumeFlag;
   public:
      static SET_cpBase m_SetOpenNil; // множество ячеек (нулевых  ) открытых           при последнем клике
      static SET_cpBase m_SetOpen;    // множество ячеек (ненулевых) открытых           при последнем клике
      static SET_cpBase m_SetFlag;    // множество ячеек с флажками  снятых/уставленных при последнем клике

   protected:
      static float m_a;  // базовая величина фигуры (обычно это размер одной из сторон фигуры)
      static float m_sq; // размер квадрата, вписанного в фигуру
   public:
      static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
      static int SizeInscribedSquare(int iArea, int iBorderWidth); // по площади ячейки определить размер вписанного в фигуру квадрата
   protected:
      COORD m_Coord;
      RECT  m_Square;    // вписанный в фигуру квадрат - область в которую выводится изображение/текст
      const CGraphicContext &m_GContext;
      bool m_bDown; // Нажата? Не путать с m_Cell.m_Open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки

      COORD *const m_pNeighbor;  // массив кординат соседей
      POINT *const m_pRegion;    // массив координат точек из которых состоит фигура
      const int    m_iDirection; // направление - "третья координата" ячейки
   private:
      struct{
         EStatus m_Status;
         EOpen   m_Open;
         EClose  m_Close;
      } m_Cell;
      bool m_bLockMine; // блокировать ли возможность установки на данную ячейку мины?
      const int m_iNeighborNumber; // количество соседей
      const int m_iVertexNumber;   // количество вершин у фигуры
   private:
      CBase **m_ppLinkNeighbor;
   protected:
      void VerifyNeighbor(const SIZE& sizeField);
   private:
      void Lock();
   public:
      void    Cell_SetDown(bool bDown);
      void    Cell_SetStatus(EStatus Status);
      EStatus Cell_GetStatus() const;
      void    Cell_DefineValue();
      bool    Cell_SetMine();
      EOpen   Cell_GetOpen() const;
      void    Cell_SetClose(EClose Close);
      EClose  Cell_GetClose() const;

     ~CBase();
      CBase(const COORD &Coord,
            const SIZE &sizeField,
            int iArea,
            const CGraphicContext &gContext,
            int iNeighborNumber,
            int iVertexNumber,
            int iDirection);
      void  LockNeighbor();
      void  SetNeighborLink(CBase *const *const ppLinkNeighbor, int numberNeighbor);
      int   GetNeighborNumber() const; // определить количество соседей
      COORD GetNeighborCoord(int index) const;
      COORD GetCoord() const;       // X и Y ячейки
      POINT GetCenterPixel() const; // координата центра фигуры (в пикселях)
      virtual bool PointInRegion(const POINT &point) const; // принадлежат ли эти экранные координаты ячейке
      virtual void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
      void Reset();
      virtual void Paint() const;
      void             LButtonDown();
      CLeftUpReturn    LButtonUp  (bool isMy);
      CRightDownReturn RButtonDown(EClose Close);

      void SetPresumeFlag(bool bValue) const {       m_bPresumeFlag = bValue;}
      bool GetPresumeFlag()            const {return m_bPresumeFlag;         }
   };

} // namespace nsCell

const double SQRT2   = sqrt(2);
const double SQRT3   = sqrt(3);
const double SQRT27  = sqrt(27);
const double SQRT48  = sqrt(48);
const double SQRT147 = sqrt(147);
const double PI      = 3.14159265358979323;
const double SIN75   = sin(PI/180*75);
const double SIN15   = sin(PI/180*15);
const double TAN15   = tan(PI/180*15);
const double TAN45_2 = tan(PI/180*45/2);
const double SIN135a = sin(PI/180*135-atan(8.f/3));

#endif // __FILE__BASE__
