////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Image.h"
//
// �������� ������ CImage
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CIMAGE__
#define __FILE__CIMAGE__

#ifndef __AFX_H__
   #include <Windows.h>
#endif

// ��� ������ �������:
// �� ������� ��������� ����� ������ (�������  ���  ������ �� ���������  ���  ���������� �� ���������).
// ������ ����� ������� ��������� (��������� �����-���� ������������ �������� ���������) �������� �����������.
void RegionDraw(
   HDC  hDCDst, // ������� �������� (destination context)
   bool compatibleDC, // hDCDst �������� � ������? ���� �� (�������� � ������), �� ������ ����� � ����;
                      // ����� ������ ����������� �������� � ������ (� �� ������ � ����),
                      // � � ����� ���������� ��������� ������ ������� � hDCDst.
                      // �������� ����� ��� ��������� �������.

   // �������� �������. �������:
   // ��� �������
   CONST POINT *pPoints = NULL,
   int nCount = 0,
   // ��� ������
   HBITMAP hBmpMask = NULL, // �����-����� �����
   // ��� ����������
   HDC hDCMask = NULL, // �������� c ��� ��������� ��������/������ (bitmap'��)
   int xMask = 0, // widtch bitmap'� �� hDCMask
   int yMask = 0, // height bitmap'� �� hDCMask
   // �������� ����� hBmpMask ��� ��������� hDCMask ������������ �������� ���������
   int xMaskOffset = 0,
   int yMaskOffset = 0,

   // �������� ��������� �����������. �������:
   // ��� ����������
   HDC hDCSrc  = NULL, // source context
   // ��� ���������
   HBITMAP hBmpSrc = NULL,
   // �������� ��������� ����������� ������������ �������� ���������
   int xSrcOffset = 0, // �������� �����
   int ySrcOffset = 0  // �������� ����
);

void DrawMaskedBitmap(HDC hDC, HBITMAP hBmp,
                      int xD, int yD,
                      int wD, int hD,
                      int wS, int hS,
                      COLORREF transparentColor = -1);

enum EPlace {
   placeCenter,
   placeStretch, // p��������
   placeTile     // ���������
};

enum EImageType {
   imageBitmap,
   imageIcon,
   imageMetafile,
   imageEnhMetafile,
   imageUnknown
};

struct CImageMini{ // minimal characteristics (�������, ������� ��� ���� ����� � �������)
   TCHAR m_szPath[MAX_PATH];
   bool m_bTransparent; // image is transpared ? (only for .bmp image)
   EPlace m_Place;
   CImageMini():
      m_bTransparent(true),
      m_Place       (placeStretch)
   {
      memset(m_szPath, 0, MAX_PATH);
   }
};

class CImage: protected CImageMini{
private:
   EImageType   m_ImageType;
   HANDLE       m_hImage;    // original image
   HDC          m_hDCZoom;   // zoomed image
   HDC          m_hDCMask;   // mask for zoomed image
   SIZE         m_SizeImage; // original size image
   mutable SIZE m_SizeZoom;  // zoomed size image
private:
   SIZE GetSizeImage();
   void Zoom(const SIZE& newSizeZoom) const;
   BOOL DeleteHandle();
   bool ReloadFile() {return LoadFile(m_szPath);}
public:
   CImage(LPCTSTR fileName = NULL);
  ~CImage();
   void Reset() {memset(m_szPath, 0, MAX_PATH); DeleteHandle();}

   EImageType        GetImageType() const {return m_ImageType;}
   const CImageMini* GetImage    () const {return this;}
   HBITMAP        GetHandleBitmap     () const {return (m_ImageType == imageBitmap     ) ? (HBITMAP     )m_hImage: NULL;}
   HICON          GetHandleIcon       () const {return (m_ImageType == imageIcon       ) ? (HICON       )m_hImage: NULL;}
   HENHMETAFILE   GetHandleEnhMetafile() const {return (m_ImageType == imageEnhMetafile) ? (HENHMETAFILE)m_hImage: NULL;}

   void Draw     (HDC hDC, const RECT* pRect) const;
   void DrawPlace(HDC hDC, const RECT* pRect) const;

   bool LoadFile(LPCTSTR szFileName);
   bool LoadResource(HINSTANCE hInstance, LPCTSTR resourceName, EImageType imageTypeNew);

   void SetImage(const CImageMini&);
   void SetTransparent(bool newTransparent) {m_bTransparent = newTransparent;}
   void SetPlace(EPlace newPlace) {m_Place = newPlace;}
   void SetBrush(HBRUSH hBrush) {DeleteObject(SelectObject(m_hDCZoom, hBrush));}
};

#endif // __FILE__CIMAGE__
