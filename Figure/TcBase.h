////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcBase.h"
//
// O������� �������� ������ TcBase
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
   skillLevelCustom  //  ������ ��������� � ������������ !!!
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
   static SET_cpTB setOpenNil; // ��������� ����� (�������  ) ��������           ��� ��������� �����
   static SET_cpTB setOpen;    // ��������� ����� (���������) ��������           ��� ��������� �����
   static SET_cpTB setFlag;    // ��������� ����� � ��������  ������/����������� ��� ��������� �����

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
   static float a;  // ������ ����� �� ������ ������ (������ ������� �������� �������)
   static float sq; // ������ ��������, ���������� � ������
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int); // �� ������� ������ ���������� ������ ���������� � ������ ��������
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
   static bool PointInPolygon(const POINT&, const POINTFLOAT* const, int); // �������������� ����� ������
protected:
   struct{
      TeStatus Status;
      TeOpen   Open;
      TeClose  Close;
   } Cell;
   bool lockMine;
   bool down; // ������? �� ������ � Cell.Open! - ������ ����� ���� ������, �� ��� �� �������. ����� ������ ��� �-��� ����������
   POINT coord;
   RECT regionIn; // ��������� � ������ ������� - ������� � ������� ��������� �����������/�����
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
   int   GetNeighborNumber() const; // ���������� ���������� �������
   POINT GetNeighborCoord(int) const;
   POINT GetCoord() const;       // X � Y ������
   POINT GetCenterPixel() const; // ���������� ������ ������ (� ��������)
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
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
