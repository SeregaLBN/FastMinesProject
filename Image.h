////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Image.h"
//
// Описание класса CImage
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CIMAGE__
#define __FILE__CIMAGE__

#ifndef __AFX_H__
   #include <Windows.h>
#endif

// Что делает функция:
// На целевом контексте задан регион (точками  или  маской со смещением  или  контекстом со смещением).
// Внутрь этого региона выводится (смещённый влево-вниз относительно целевого контекста) источник изображения.
void RegionDraw(
   HDC  hDCDst, // целевой контекст (destination context)
   bool compatibleDC, // hDCDst контекст в памяти? Если да (контекст в памяти), то вывожу сразу в него;
                      // иначе создаю совместимый контекст в памяти (и всё вывожу в него),
                      // и в конце содержимое контекста памяти копирую в hDCDst.
                      // Параметр нужен для избежания мигания.

   // ОПИСАНИЕ РЕГИОНА. Задаётся:
   // или точками
   CONST POINT *pPoints = NULL,
   int nCount = 0,
   // или маской
   HBITMAP hBmpMask = NULL, // чёрно-белая маска
   // или контекстом
   HDC hDCMask = NULL, // контекст c уже нанесённым регионом/маской (bitmap'ом)
   int xMask = 0, // widtch bitmap'а из hDCMask
   int yMask = 0, // height bitmap'а из hDCMask
   // Смещение маски hBmpMask или контекста hDCMask относительно целевого контекста
   int xMaskOffset = 0,
   int yMaskOffset = 0,

   // ОПИСАНИЕ ИСТОЧНИКА ИЗОБРАЖЕНИЯ. Задаётся:
   // или контекстом
   HDC hDCSrc  = NULL, // source context
   // или рисуноком
   HBITMAP hBmpSrc = NULL,
   // Смещение источника изображения относительно целевого контекста
   int xSrcOffset = 0, // смещёние влево
   int ySrcOffset = 0  // смещёние вниз
);

void DrawMaskedBitmap(HDC hDC, HBITMAP hBmp,
                      int xD, int yD,
                      int wD, int hD,
                      int wS, int hS,
                      COLORREF transparentColor = -1);

enum EPlace {
   placeCenter,
   placeStretch, // pастянуть
   placeTile     // замостить
};

enum EImageType {
   imageBitmap,
   imageIcon,
   imageMetafile,
   imageEnhMetafile,
   imageUnknown
};

struct CImageMini{ // minimal characteristics (минимум, который мне надо знать о рисунке)
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
