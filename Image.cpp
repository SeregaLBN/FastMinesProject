////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Image.cpp"
//
// ���������� ������ CImage
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
   CONST POINT *pPoints,
   int nCount,
   // ��� ������
   HBITMAP hBmpMask, // �����-����� �����
   // ��� ����������
   HDC hDCMask, // �������� c ��� ��������� ��������/������ (bitmap'��)
   int xMask, // widtch bitmap'� �� hDCMask
   int yMask, // height bitmap'� �� hDCMask
   // �������� ����� hBmpMask ��� ��������� hDCMask ������������ �������� ���������
   int xMaskOffset,
   int yMaskOffset,

   // �������� ��������� �����������. �������:
   // ��� ����������
   HDC hDCSrc, // source context
   // ��� ���������
   HBITMAP hBmpSrc,
   // �������� ��������� ����������� ������������ �������� ���������
   int xSrcOffset, // �������� �����
   int ySrcOffset  // �������� ����
) {
   // ���� ������ ����� �������, �� �������� ����������� ��������� ������! �������.
   // ���� ������ ����� ������ ��� ����������, �� �������� ����������� ��������� � ����� ��������.

   // ��� �����������:
   // 1. ���� ������ ����� ���������� hDCMask, �� ���� ����� ������������
   //    �����, ��� ������� �������� �������� � ������ hDCMask. � ���� ��������� ����� hBmpMask.
   //    ���� ����� �� �������� �� ������� ���������, ��� �������� �� �������� ������ pPoints.
   // 2. �������� ����������� ������ �� ��������� hDCSrc. ���� ������ �������� �� ������� �� ������� ���������,
   //    �� �� �������� (�������� �������� � ������), � � ���� ��������� ������� hBmpSrc.
   // 3. � ������� �������� hDCDst ��������� �������� ����������� �� hDCSrc ����������� ������ ���������� � hDCMask.

   /////////////////////////////////////////////////////////////////
   // 1.
   HBITMAP hBmpMaskOld;// �� ��� ���� � hDCMask �� ����� (������������ ������ ���� ������� �������� hDCMask �� ��� �����)

   if (!hDCMask) {
      hDCMask = CreateCompatibleDC(NULL); // ������ �������� � ������ ��� �����
      if (pPoints) { // ������ ������� �������
         {
            // ����� ������� bitmap ����� � � ��������
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
         hBmpMask = CreateBitmap(xMask, yMask, 1, 1, NULL); // ������ ������ bitmap ��� �����
         hBmpMaskOld = (HBITMAP)SelectObject(hDCMask, hBmpMask); // �������� ��� �� ������������ ����� �� ��������
         // ����� ����� �� ��������� ��������
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
         BITMAP bmpMask; GetObject(hBmpMask, sizeof(BITMAP), &bmpMask); // ����� ������� bitmap �����
         hBmpMaskOld = (HBITMAP)SelectObject(hDCMask, hBmpMask); // �������� ������� ����� �� ��������
         xMask = bmpMask.bmWidth;
         yMask = bmpMask.bmHeight;
      }
   }
   // � ���������� ��������� ������ hDCMask.

   /////////////////////////////////////////////////////////////////
   // 2.
   HBITMAP hBmpOldSrc = NULL;
   if (!hDCSrc) { // Ec�� �������� ����������� ��������� bitmap'��, �� ������ �������� � ������ � �������� bitmap � ���.
      hDCSrc = CreateCompatibleDC(NULL);
      hBmpOldSrc = (HBITMAP)SelectObject(hDCSrc, hBmpSrc);
   }
   // ������ �������� ����������� ����� � hDCSrc, ���� ���� �� � �� ��������� � �������� ��������� �� ������� ���������.
   // � ���������� ��������� ������ hDCSrc.

   /////////////////////////////////////////////////////////////////
   // 3.
   HDC hDCDstOriginal;          // ����� �������� ��������� hDCDst (������������ ������ ���� ������� �������� hDCDst �� �������� ���������� ������)
   HBITMAP hBmpDst, hBmpOldDst; // ������������ ������ ���� ������� �������� hDCDst �� �������� ���������� ������
   if (!compatibleDC) {
      hDCDstOriginal = hDCDst;
      hDCDst = CreateCompatibleDC(NULL); // ������ �������� � ������.
      // �������� ����������� �� �������� ��������� ���� hDCDstOriginal �� ����������� �������� ������ hDCDst
      hBmpDst = CreateCompatibleBitmap(hDCDstOriginal, xMask, yMask);
      hBmpOldDst = (HBITMAP)SelectObject(hDCDst, hBmpDst);
      BitBlt(hDCDst, 0,0, xMask, yMask, hDCDstOriginal, xMaskOffset, yMaskOffset, SRCCOPY);
   }

   {  // �� ��� ���� �� � ��������...
      // �������� ����������� ��������� �� �������/�����
      COLORREF oldBkColor   = SetBkColor  (hDCDst, RGB(255, 255, 255));
      COLORREF oldTextColor = SetTextColor(hDCDst, RGB(0, 0, 0));
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCSrc , xMaskOffset-xSrcOffset, yMaskOffset-ySrcOffset, DSx);
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCMask, 0   , 0   , DSna);
      BitBlt(hDCDst, compatibleDC ? xMaskOffset : 0, compatibleDC ? yMaskOffset : 0, xMask, yMask, hDCSrc , xMaskOffset-xSrcOffset, yMaskOffset-ySrcOffset, DSx);
      SetBkColor  (hDCDst, oldBkColor);
      SetTextColor(hDCDst, oldTextColor);
      if (!compatibleDC)
         // �������� �������� ����������� �� ������� ��������.
         BitBlt(hDCDstOriginal, xMaskOffset,yMaskOffset, xMask, yMask, hDCDst, 0, 0, SRCCOPY);
   }

   /////////////////////////////////////////////////////////////////
   // 4. ������� ������.
   if (hBmpMask) {
      SelectObject(hDCMask, hBmpMaskOld);
      DeleteDC(hDCMask);
      if (pPoints)
          DeleteObject(hBmpMask); // ������ ��������� �����.
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
             (size.cy != m_SizeZoom.cy)) Zoom(size); // ������� �����������
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
      // ��������� ����� ���������
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
