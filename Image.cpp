////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Image.cpp"
//
// Реализация класса CImage
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Image.h"
#include "CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
// raster operation code
#define DSx  0x00660046L
#define DSna 0x00220326L

////////////////////////////////////////////////////////////////////////////////
//                            other function
////////////////////////////////////////////////////////////////////////////////

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
   CONST POINT *pPoints,
   int nCount,
   // или маской
   HBITMAP hBmpMask, // чёрно-белая маска
   // или контекстом
   HDC hDCMask, // контекст c уже нанесённым регионом/маской (bitmap'ом)
   int xMask, // widtch bitmap'а из hDCMask
   int yMask, // height bitmap'а из hDCMask
   // Смещение маски hBmpMask или контекста hDCMask относительно целевого контекста
   int xMaskOffset,
   int yMaskOffset,

   // ОПИСАНИЕ ИСТОЧНИКА ИЗОБРАЖЕНИЯ. Задаётся:
   // или контекстом
   HDC hDCSrc, // source context
   // или рисуноком
   HBITMAP hBmpSrc,
   // Смещение источника изображения относительно целевого контекста
   int xSrcOffset, // смещёние влево
   int ySrcOffset  // смещёние вниз
) {
   // Если регион задан точками, то источник изображения выводится внутри! региона.
   // Если регион задан маской или контекстом, то источник изображения выводится в белых областях.

   // Как реализовано:
   // 1. Если регион задан контекстом hDCMask, то этот пункт игнорируется
   //    Иначе, для региона создаётся контекст в памяти hDCMask. В него выводится маска hBmpMask.
   //    Если маска не получена во входном параметре, она создаётся по заданным точкам pPoints.
   // 2. Источник изображения берётся из контекста hDCSrc. Если данный контекст не получен во входном параметре,
   //    то он создаётся (создаётся контекст в памяти), и в него выводится рисунок hBmpSrc.
   // 3. В целевой контекст hDCDst выводится источник изображения из hDCSrc ограниченый маской отбражённой в hDCMask.

   /////////////////////////////////////////////////////////////////
   // 1.
   HBITMAP hBmpMaskOld;// то что было в hDCMask до маски (используется только если входной параметр hDCMask не был задан)

   if (!hDCMask) {
      hDCMask = CreateCompatibleDC(NULL); // создаю контекст в памяти для маски
      if (pPoints) { // регион задаётся точками
         {
            // узнаю размеры bitmap маски и её смещение
            xMask       = pPoints[0].x;
            yMask       = pPoints[0].y;
            xMaskOffset = pPoints[0].x;
            yMaskOffset = pPoints[0].y;
            for (int i=1; i<nCount; i++) {
               xMask       = max(pPoints[i].x, xMask);
               yMask       = max(pPoints[i].y, yMask);
               xMaskOffset = min(pPoints[i].x, xMaskOffset);
               yMaskOffset = min(pPoints[i].y, yMaskOffset);
            }
            xMask -= xMaskOffset;
            yMask -= yMaskOffset;
         }
         hBmpMask = CreateBitmap(xMask, yMask, 1, 1, NULL); // создаю пустой bitmap для маски
         hBmpMaskOld = (HBITMAP)SelectObject(hDCMask, hBmpMask); // переношу ещё не нарисованную маску на контекст
         // рисую маску по заданному полигону
         PatBlt(hDCMask, 0,0, xMask, yMask, BLACKNESS);
         HBRUSH hBrushOld = (HBRUSH)SelectObject(hDCMask, GetStockObject(WHITE_BRUSH));
         HPEN   hPenOld   = (HPEN  )SelectObject(hDCMask, GetStockObject(NULL_PEN));
         {
            POINT *pPointsOffset = new POINT [nCount];
            for (int i=0; i<nCount; i++) {
               pPointsOffset[i].x = pPoints[i].x - xMaskOffset;
               pPointsOffset[i].y = pPoints[i].y - yMaskOffset;
            }
            Polygon(hDCMask, pPointsOffset, nCount);
            delete [] pPointsOffset;
         }
         SelectObject(hDCMask, hBrushOld);
         SelectObject(hDCMask, hPenOld);
      } else {
         BITMAP bmpMask; GetObject(hBmpMask, sizeof(BITMAP), &bmpMask); // узнаю размеры bitmap маски
         hBmpMaskOld = (HBITMAP)SelectObject(hDCMask, hBmpMask); // переношу готовую маску на контекст
         xMask = bmpMask.bmWidth;
         yMask = bmpMask.bmHeight;
      }
   }
   // В дальнейшем использую только hDCMask.

   /////////////////////////////////////////////////////////////////
   // 2.
   HBITMAP hBmpOldSrc = NULL;
   if (!hDCSrc) { // Ecли источник изображения задавался bitmap'ом, то создаю контекст в памяти и связываю bitmap с ним.
      hDCSrc = CreateCompatibleDC(NULL);
      hBmpOldSrc = (HBITMAP)SelectObject(hDCSrc, hBmpSrc);
   }
   // Теперь источник изображения задан в hDCSrc, даже если он и не задавался в качестве контекста во входном параметре.
   // В дальнейшем использую только hDCSrc.

   /////////////////////////////////////////////////////////////////
   // 3.
   HDC hDCDstOriginal;          // копия целевого контекста hDCDst (используется только если входной параметр hDCDst не является контекстом памяти)
   HBITMAP hBmpDst, hBmpOldDst; // используется только если входной параметр hDCDst не является контекстом памяти
   if (!compatibleDC) {
      hDCDstOriginal = hDCDst;
      hDCDst = CreateCompatibleDC(NULL); // Создаю контекст в памяти.
      // Переношу изображение со входного контекста окна hDCDstOriginal на совместимый контекст памяти hDCDst
      hBmpDst = CreateCompatibleBitmap(hDCDstOriginal, xMask, yMask);
      hBmpOldDst = (HBITMAP)SelectObject(hDCDst, hBmpDst);
      BitBlt(hDCDst, 0,0, xMask, yMask, hDCDstOriginal, xMaskOffset, yMaskOffset, SRCCOPY);
   }

   {  // То для чего всё и делалось...
      // Источник изображения выводится по региону/маске
      COLORREF oldBkColor   = SetBkColor  (hDCDst, RGB(255, 255, 255));
      COLORREF oldTextColor = SetTextColor(hDCDst, RGB(0, 0, 0));
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCSrc , xMaskOffset-xSrcOffset, yMaskOffset-ySrcOffset, DSx);
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCMask, 0   , 0   , DSna);
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCSrc , xMaskOffset-xSrcOffset, yMaskOffset-ySrcOffset, DSx);
      SetBkColor  (hDCDst, oldBkColor);
      SetTextColor(hDCDst, oldTextColor);
      if (!compatibleDC)
         // Переношу конечное изображение на заданый контекст.
         BitBlt(hDCDstOriginal, xMaskOffset,yMaskOffset, xMask, yMask, hDCDst, 0, 0, SRCCOPY);
   }

   /////////////////////////////////////////////////////////////////
   // 4. Очистка данных.
   if (hBmpMask) {
      SelectObject(hDCMask, hBmpMaskOld);
      DeleteDC(hDCMask);
      if (pPoints)
          DeleteObject(hBmpMask); // Удаляю созданную маску.
   }

   if (hBmpOldSrc) {
      SelectObject(hDCSrc, hBmpOldSrc);
      DeleteDC(hDCSrc);
   }
   if (!compatibleDC) {
      SelectObject(hDCDst, hBmpOldDst);
      DeleteDC(hDCDst);
      DeleteObject(hBmpDst);
   }
}

void DrawMaskedBitmap(HDC hDC, HBITMAP hBmp,
                      int xD, int yD,
                      int wD, int hD,
                      int wS, int hS, COLORREF transparentColor)
{
   COLORREF oldBkColor = SetBkColor(hDC, RGB(255, 255, 255));
	COLORREF oldTextColor = SetTextColor(hDC, RGB(0, 0, 0));

	HDC hCDC = CreateCompatibleDC(NULL);
   HBITMAP hBmpMask = CreateMask(hBmp, transparentColor);
	HBITMAP hOldBmp  = (HBITMAP)SelectObject(hCDC, hBmp);
	StretchBlt(hDC, xD, yD, wD, hD, hCDC, 0, 0, wS, hS, DSx);
	SelectObject(hCDC, hBmpMask);
	StretchBlt(hDC, xD, yD, wD, hD, hCDC, 0, 0, wS, hS, DSna);
	SelectObject(hCDC, hBmp);
	StretchBlt(hDC, xD, yD, wD, hD, hCDC, 0, 0, wS, hS, DSx);
	SelectObject(hCDC, hOldBmp);
   DeleteDC(hCDC);
   DeleteObject(hBmpMask);

	SetBkColor(hDC, oldBkColor);
	SetTextColor(hDC, oldTextColor);
}

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////

CImage::CImage(LPCTSTR szFileName):
   m_ImageType(imageUnknown),
   m_hImage   (NULL),
   m_hDCZoom  (CreateCompatibleDC(NULL)),
   m_hDCMask  (CreateCompatibleDC(NULL))
{
   m_SizeImage.cx = m_SizeImage.cy = 0;
   m_SizeZoom = m_SizeImage;
   if (szFileName)
      LoadFile(szFileName);
}

CImage::~CImage() {
   DeleteDC(m_hDCZoom);
   DeleteDC(m_hDCMask);
   DeleteHandle();
}

void CImage::Draw(HDC hDC, const RECT* pRect) const {
   switch (m_ImageType) {
   case imageUnknown: return;
   case imageBitmap:
      {
         SIZE size = RECTEX(*pRect).size();
         if ((size.cx != m_SizeZoom.cx) ||
             (size.cy != m_SizeZoom.cy)) Zoom(size); // кэширую изображение
         if (m_bTransparent) {
            SetTextColor(hDC, RGB(0, 0, 0));
          //SetBkColor  (hDC, RGB(255, 255, 255));
	         BitBlt(hDC, pRect->left, pRect->top, size.cx, size.cy, m_hDCZoom, 0, 0, DSx);
	         BitBlt(hDC, pRect->left, pRect->top, size.cx, size.cy, m_hDCMask, 0, 0, DSna);
	         BitBlt(hDC, pRect->left, pRect->top, size.cx, size.cy, m_hDCZoom, 0, 0, DSx);
         } else {
            BitBlt(hDC, pRect->left, pRect->top, size.cx, size.cy, m_hDCZoom, 0, 0, SRCCOPY );
         }
      }
      break;
   case imageIcon:
      DrawIconEx(hDC,
         pRect->left, pRect->top,
         (HICON)m_hImage,
         RECTEX(*pRect).width(),
         RECTEX(*pRect).height(),
         0,
         NULL,
         DI_NORMAL);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      PlayEnhMetaFile(hDC, (HENHMETAFILE)m_hImage, pRect);
      break;
   }
}

void CImage::DrawPlace(HDC hDC, const RECT* pRect) const {
   if (m_ImageType == imageUnknown) return;
   switch (m_Place) {
   case placeCenter:
      {
         SIZE sizeDest = RECTEX(*pRect).size();
         RECT Rect = FindInnerRect(m_SizeImage, sizeDest);
         Rect.left   += pRect->left;
         Rect.top    += pRect->top;
         Rect.right  += pRect->left;
         Rect.bottom += pRect->top;
         Draw(hDC, &Rect);
      }
      break;
   case placeStretch:
      Draw(hDC, pRect);
      break;
   case placeTile:
      {
         SIZE sizeDest = RECTEX(*pRect).size();
         for (int i=0; i<=sizeDest.cx/m_SizeImage.cx; i++)
            for (int j=0; j<=sizeDest.cy/m_SizeImage.cy; j++) {
               RECT Rect = {pRect->left + m_SizeImage.cx*i,
                            pRect->top  + m_SizeImage.cy*j,
                            pRect->left + m_SizeImage.cx*(i+1),
                            pRect->top  + m_SizeImage.cy*(j+1)};
               Draw(hDC, &Rect);
            }
      }
      break;
   } // switch
}

void CImage::Zoom(const SIZE& sizeZoomNew) const {
#ifdef _DEBUG
   MessageBeep(0);
#endif // _DEBUG
   m_SizeZoom = sizeZoomNew;
   switch (m_ImageType) {
   case imageBitmap:
      {
         HDC hCDC = CreateCompatibleDC(NULL);
         HBITMAP hBmpOld = (HBITMAP)SelectObject(hCDC, m_hImage);
         COLORREF transparentColor = GetPixel(hCDC, 0, 0);
         DeleteObject(SelectObject(m_hDCZoom, CreateCompatibleBitmap(hCDC, m_SizeZoom.cx, m_SizeZoom.cy)));//CopyImage(m_hImage, IMAGE_BITMAP, m_SizeZoom.cx, m_SizeZoom.cy, LR_COPYRETURNORG)));//
         SetStretchBltMode(m_hDCZoom, COLORONCOLOR); // for Win2K
         StretchBlt(m_hDCZoom, 0, 0, m_SizeZoom .cx, m_SizeZoom .cy,
                    hCDC     , 0, 0, m_SizeImage.cx, m_SizeImage.cy,
                    SRCCOPY);
         SelectObject(hCDC, hBmpOld);
         DeleteDC(hCDC);

         //if (m_bTransparent)
         {
	         DeleteObject(SelectObject(m_hDCMask, CreateBitmap(m_SizeZoom.cx, m_SizeZoom.cy, 1, 1, NULL)));

	         COLORREF oldColor = SetBkColor(m_hDCZoom, transparentColor);
	         BitBlt(m_hDCMask, 0,0, m_SizeZoom.cx, m_SizeZoom.cy, m_hDCZoom, 0, 0, NOTSRCCOPY);
	         SetBkColor(m_hDCZoom, oldColor);
         }
      }
      break;
   case imageIcon:
   case imageMetafile:
   case imageEnhMetafile:
   case imageUnknown:
      m_SizeZoom.cx = m_SizeZoom.cy = 0;
      break;
   }
}

bool CImage::LoadFile(LPCTSTR szFileName) {
   if ((!szFileName) ||
       (!szFileName[0]) ||
       (lstrlen(szFileName)<4)) return false;

   HANDLE hImageNew = NULL;
   EImageType imageTypeNew = imageUnknown;

#define FileTypeIs(fileType) (!lstrcmpi(TEXT(fileType), &szFileName[lstrlen(szFileName)-3]))

   if (FileTypeIs("bmp")) { // bitmap
      imageTypeNew = imageBitmap;
      hImageNew = LoadImage(NULL, szFileName,
         IMAGE_BITMAP, 0, 0,
         LR_LOADFROMFILE | LR_DEFAULTCOLOR);
   } else

   if (FileTypeIs("ico")) { // icon
      imageTypeNew = imageIcon;
      hImageNew = LoadImage(NULL, szFileName,
         IMAGE_ICON, 0, 0,
         LR_LOADFROMFILE);
   } else

   if (FileTypeIs("wmf")) { // Windows metafile
      //imageTypeNew= imageMetafile;
      //hImageNew = GetMetaFile(szFileName);
      /*
      {
         HANDLE hFile = INVALID_HANDLE_VALUE;
         PVOID pvBuf = NULL;
         __try {
            hFile = CreateFile(szFileName, GENERIC_READ, 0, NULL,
               OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL );
            if (hFile == INVALID_HANDLE_VALUE) __leave;
            DWORD dwFS = GetFileSize(hFile, NULL);
            if (!dwFS || (dwFS == 0xFFFFFFFF)) __leave;
            pvBuf = new BYTE[dwFS];
            if (pvBuf == NULL) __leave;
            DWORD dwNOBR;
            if (!ReadFile(hFile, pvBuf, dwFS, &dwNOBR, NULL) || (dwFS != dwNOBR)) __leave;
            METAFILEPICT sMFP = {MM_TEXT,10,10,NULL};
            hImageNew = SetWinMetaFileBits(dwFS, (BYTE*)pvBuf, m_hDCZoom, &sMFP);
            if (hImageNew == NULL) {
               DWORD err = GetLastError();
               TCHAR szBuf[64];
               _stprintf(szBuf, TEXT("Error code = %d"), err);
               MessageBox(NULL, szBuf, TEXT("Error code"), MB_ICONERROR);
               __leave;
            }
            imageTypeNew= imageEnhMetafile;
         } __finally {
            if (pvBuf != NULL) delete [] pvBuf;
            if (hFile != INVALID_HANDLE_VALUE) CloseHandle(hFile);
         }
      }
      /**/
   } else

   if (FileTypeIs("emf")) { // enhanced-format metafile
      imageTypeNew= imageEnhMetafile;
      hImageNew = GetEnhMetaFile(szFileName);
   }
#undef FileTypeIs

   if (hImageNew != NULL) {
      if (szFileName != m_szPath) {
         memset(m_szPath, 0, MAX_PATH);
         lstrcpy(m_szPath, szFileName);
      }
      DeleteHandle();
      m_hImage = hImageNew;
      m_ImageType = imageTypeNew;

      m_SizeImage = GetSizeImage();
      m_SizeZoom.cx = m_SizeZoom.cy = 0;
      return true;
   }
   return false;
}

bool CImage::LoadResource(HINSTANCE hInstance, LPCTSTR resourceName, EImageType imageTypeNew) {
   if (!resourceName ||
       !resourceName[0]) return false;

   HANDLE hImageNew = NULL;

   switch (imageTypeNew) {
   case imageBitmap:
      hImageNew = LoadImage(hInstance, resourceName,//MAKEINTRESOURCE(ID_BITMAP_NEW1),
         IMAGE_BITMAP, 0, 0,
         LR_DEFAULTCOLOR);
      break;
   case imageIcon:
      hImageNew = LoadImage(hInstance, resourceName,
         IMAGE_ICON, 0, 0,
         0);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      {
         HRSRC hRsrc = FindResource(hInstance, resourceName, RT_RCDATA);
         HGLOBAL hGlobal = ::LoadResource(hInstance, hRsrc);
         DWORD size = SizeofResource(hInstance, hRsrc);
         hImageNew = SetEnhMetaFileBits(size, (BYTE *)LockResource(hGlobal));
      }
      break;
   case imageUnknown:
      // попытаюсь найти перебором
      {
         for (int type = imageBitmap; type < imageUnknown; type++)
            if (LoadResource(hInstance, resourceName, (EImageType)type))
               return true;
         return false;
      }
      break;
   }
   if (hImageNew != NULL) {
      DeleteHandle();
      m_hImage = hImageNew;
      m_ImageType = imageTypeNew;

      m_SizeImage = GetSizeImage();
      m_SizeZoom.cx = m_SizeZoom.cy = 0;
      return true;
   }
   return false;
}

SIZE CImage::GetSizeImage() {
   SIZE result = {0,0};
   switch (m_ImageType) {
   case imageBitmap:
      BITMAP bmp;
      GetObject(m_hImage, sizeof(BITMAP), &bmp);
      result.cx = bmp.bmWidth;
      result.cy = bmp.bmHeight;
      break;
   case imageIcon:
      ICONINFO iconInfo;
      if (!GetIconInfo((HICON)m_hImage, &iconInfo)) break;
      result.cx = iconInfo.xHotspot<<1;
      result.cy = iconInfo.yHotspot<<1;
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      ENHMETAHEADER EnhMetaHeader;
      GetEnhMetaFileHeader((HENHMETAFILE)m_hImage, sizeof(ENHMETAHEADER), &EnhMetaHeader);
      result.cx = EnhMetaHeader.rclBounds.right  - EnhMetaHeader.rclBounds.left;//EnhMetaHeader.szlDevice.cx;
      result.cy = EnhMetaHeader.rclBounds.bottom - EnhMetaHeader.rclBounds.top; //EnhMetaHeader.szlDevice.cy;
      break;
   }
   return result;
}

BOOL CImage::DeleteHandle() {
   if (!m_hImage) return TRUE;
   BOOL result;
   switch (m_ImageType) {
   case imageBitmap:
      result = DeleteObject(m_hImage);
      break;
   case imageIcon:
      result = DestroyIcon((HICON)m_hImage);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      result = DeleteEnhMetaFile((HENHMETAFILE)m_hImage);
      break;
   case imageUnknown:
      result = FALSE;
      break;
   }
   m_ImageType = imageUnknown;
   m_hImage = NULL;
   return result;
}

void CImage::SetImage(const CImageMini& newData) {
   memset(m_szPath, 0, MAX_PATH);
   lstrcpy(m_szPath, newData.m_szPath);
   m_bTransparent = newData.m_bTransparent;
   m_Place       = newData.m_Place;
   ReloadFile();
}
