////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcBase.h"
//
// Oписание базового класса TcBase
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCBASE
#define FILE_TCBASE

#include ".\TcVirtual.h"
#include "..\TcImage.h"
#include <set>

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information

#define pi 3.1415926535 //const double pi = 3.1415926535;//8979323

enum TeSkillLevel {
   skillLevelBeginner,
   skillLevelAmateur,
   skillLevelProfessional,
   skillLevelCrazy,
   skillLevelCustom  //  всегда последняя в перечислении !!!
};

namespace nsFigure {
   class TcBase;
}
typedef nsFigure::TcBase TB;
typedef std::set<const TB*> SET_cpTB;

namespace nsFigure {

class TcBase: public TcVirtual{
private:
   mutable bool presumeFlag;
public:
   static SET_cpTB setOpenNil; // множество ячеек (нулевых  ) открытых           при последнем клике
   static SET_cpTB setOpen;    // множество ячеек (ненулевых) открытых           при последнем клике
   static SET_cpTB setFlag;    // множество ячеек с флажками  снятых/уставленных при последнем клике

   static HANDLE  hDC;
   static HANDLE  hCDC;
   static HANDLE  hPenBlack;
   static HANDLE  hPenWhite;
 //static int     w; // pen width
   static TcImage * pImageMine;
   static TcImage * pImageFlag;
   static COLORREF* pColorClose;
   static COLORREF* pColorOpen;
protected:
   static float a;  // размер одной из сторон фигуры (данная сторона является базовой)
   static float sq; // размер квадрата, вписанного в фигуру
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int); // по площади ячейки определить размер вписанного в фигуру квадрата
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
   static bool PointInPolygon(const POINT&, const POINTFLOAT* const, int); // принадлежность точки фигуре
protected:
   struct{
      TeStatus Status;
      TeOpen   Open;
      TeClose  Close;
   } Cell;
   bool lockMine;
   bool down; // Нажата? Не путать с Cell.Open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки
   POINT coord;
   RECT regionIn; // вписанный в фигуру квадрат - область в которую выводится изображение/текст
   TcBase** ppLinkNeighbor;
public:
   void     Cell_SetDown(bool);
   void     Cell_SetStatus(TeStatus);
   TeStatus Cell_GetStatus() const;
   void     Cell_DefineValue();
   bool     Cell_SetMine();
   TeOpen   Cell_GetOpen() const;
   void     Cell_SetClose(TeClose);
   TeClose  Cell_GetClose() const;

  ~TcBase();
   TcBase() {}
   TcBase(const POINT&, const POINT&, const int&);
   void Lock();
   void LockNeighbor();
   void  SetNeighborLink(TcBase*const*const, int);
   int   GetNeighborNumber() const; // определить количество соседей
   POINT GetNeighborCoord(int) const;
   POINT GetCoord() const;       // X и Y ячейки
   POINT GetCenterPixel() const; // координата центра фигуры (в пикселях)
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Reset();
   void Paint() const;
   void          LButtonDown();
   TsLUpReturn   LButtonUp  (bool);
   TsRDownReturn RButtonDown(TeClose);

   void SetPresumeFlag(bool value) const {       presumeFlag = value;}
   bool GetPresumeFlag()           const {return presumeFlag;        }
};

} // namespace nsFigure

#endif // FILE_TCBASE
