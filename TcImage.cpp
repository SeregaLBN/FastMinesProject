////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcImage.cpp"
//
// Реализация класса TcImage
////////////////////////////////////////////////////////////////////////////////

#include ".\TcImage.h"
#include ".\Lib.h"
#include <stdio.h>
#include <tchar.h>

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
#define DSx  0x00660046L
#define DSna 0x00220326L

////////////////////////////////////////////////////////////////////////////////
//                            other function
////////////////////////////////////////////////////////////////////////////////
inline HBITMAP CreateMask(HBITMAP hBmp, COLORREF transparentColor = -1) {
	BITMAP bmp;
	GetObject(hBmp, sizeof (BITMAP), &bmp);
   HBITMAP hBmpMask = CreateBitmap(bmp.bmWidth, bmp.bmHeight, 1, 1, NULL);

	HDC hDC_Dst = CreateCompatibleDC(NULL);
	HDC hDC_Src = CreateCompatibleDC(NULL);
	HGDIOBJ hBmpSaveSrc = SelectObject(hDC_Src, hBmp);
	HGDIOBJ hBmpSaveDst = SelectObject(hDC_Dst, hBmpMask);

   COLORREF oldBkColor = SetBkColor(hDC_Src, (transparentColor == -1) ? GetPixel(hDC_Src, 0, 0) : transparentColor);
	BitBlt(hDC_Dst, 0,0, bmp.bmWidth, bmp.bmHeight, hDC_Src, 0, 0, NOTSRCCOPY);
	SetBkColor(hDC_Src, oldBkColor);

   SelectObject(hDC_Dst, hBmpSaveDst);
   SelectObject(hDC_Src, hBmpSaveSrc);
   DeleteDC(hDC_Dst);
   DeleteDC(hDC_Src);

   return hBmpMask;
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
	HGDIOBJ hOldBmp  = SelectObject(hCDC, hBmp);
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

TcImage::TcImage() {
   imageType = imageUnknown;
   transparent = false;
   hImage = NULL;
   hDCZoom = CreateCompatibleDC(NULL);
   hDCMask = CreateCompatibleDC(NULL);
   sizeImage.x = sizeImage.y = 0;
   sizeZoom = sizeImage;
}

TcImage::~TcImage() {
   DeleteDC(hDCZoom);
   DeleteDC(hDCMask);
   DeleteHandleImage();
}

void TcImage::DrawImage(const HDC hDC, const RECT* pRect) const {
   switch (imageType) {
   case imageUnknown: return;
   case imageBitmap:
      {
         POINT size = {pRect->right  - pRect->left,
                       pRect->bottom - pRect->top};
         if ((size.x != sizeZoom.x) ||
             (size.y != sizeZoom.y)) ZoomImage(size); // кэширую изображение
         if (transparent) {
            SetTextColor(hDC, RGB(0, 0, 0));
          //SetBkColor  (hDC, RGB(255, 255, 255));
	         BitBlt(hDC, pRect->left, pRect->top, size.x, size.y, hDCZoom, 0, 0, DSx);
	         BitBlt(hDC, pRect->left, pRect->top, size.x, size.y, hDCMask, 0, 0, DSna);
	         BitBlt(hDC, pRect->left, pRect->top, size.x, size.y, hDCZoom, 0, 0, DSx);
         } else {
            BitBlt(hDC, pRect->left, pRect->top, size.x, size.y, hDCZoom, 0, 0, SRCCOPY );
         }
      }
      break;
   case imageIcon:
      DrawIconEx(hDC,
         pRect->left, pRect->top,
         (HICON)hImage,
         pRect->right  - pRect->left,
         pRect->bottom - pRect->top,
         0,
         NULL,
         DI_NORMAL);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      PlayEnhMetaFile(hDC, (HENHMETAFILE)hImage, pRect);  
      break;
   }
}

void TcImage::DrawImagePlace(const HDC hDC, const RECT* pRect, const TePlace place) const {
   if (imageType == imageUnknown) return;
   switch (place) {
   case placeCenter:
      {
         POINT sizeDest = {pRect->right -pRect->left,
                           pRect->bottom-pRect->top};
         RECT Rect = FindInnerRect(sizeImage, sizeDest);
         Rect.left   += pRect->left;
         Rect.top    += pRect->top;
         Rect.right  += pRect->left;
         Rect.bottom += pRect->top;
         DrawImage(hDC, &Rect);
      }
      break;
   case placeStretch:
      DrawImage(hDC, pRect);
      break;
   case placeTile:
      {
         POINT sizeDest  = {pRect->right -pRect->left,
                            pRect->bottom-pRect->top};
         for (int i=0; i<=sizeDest.x/sizeImage.x; i++)
            for (int j=0; j<=sizeDest.y/sizeImage.y; j++) {
               RECT Rect = {pRect->left + sizeImage.x*i,
                            pRect->top  + sizeImage.y*j,
                            pRect->left + sizeImage.x*(i+1),
                            pRect->top  + sizeImage.y*(j+1)};
               DrawImage(hDC, &Rect);
            }
      }
      break;
   } // switch
}

void TcImage::ZoomImage(POINT& sizeZoomNew) const {
   sizeZoom = sizeZoomNew;
   switch (imageType) {
   case imageBitmap:
      {
         HDC hCDC = CreateCompatibleDC(NULL);
         HGDIOBJ hBmpOld = SelectObject(hCDC, hImage);
         COLORREF transparentColor = GetPixel(hCDC, 0, 0);
         DeleteObject(SelectObject(hDCZoom, CreateCompatibleBitmap(hCDC, sizeZoom.x, sizeZoom.y)));//CopyImage(hImage, IMAGE_BITMAP, sizeZoom.x, sizeZoom.y, LR_COPYRETURNORG)));//
         SetStretchBltMode(hDCZoom, COLORONCOLOR); // for Win2K
         StretchBlt(hDCZoom, 0, 0, sizeZoom .x, sizeZoom .y,
                    hCDC   , 0, 0, sizeImage.x, sizeImage.y,
                    SRCCOPY);
         SelectObject(hCDC, hBmpOld);
         DeleteDC(hCDC);

         //if (transparent)
         {
	         DeleteObject(SelectObject(hDCMask, CreateBitmap(sizeZoom.x, sizeZoom.y, 1, 1, NULL)));

	         COLORREF oldColor = SetBkColor(hDCZoom, transparentColor);
	         BitBlt(hDCMask, 0,0, sizeZoom.x, sizeZoom.y, hDCZoom, 0, 0, NOTSRCCOPY);
	         SetBkColor(hDCZoom, oldColor);
         }
      }
      break;
   case imageIcon:
   case imageMetafile:
   case imageEnhMetafile:
   case imageUnknown:
      sizeZoom.x = sizeZoom.y = 0;
      break;
   }
}

bool TcImage::LoadFile(LPCTSTR path) {
   HANDLE hImageNew = NULL;
   TeImageType imageTypeNew = imageUnknown;
   switch (path[_tcslen(path) - 3]) {
   case TEXT('b'): // bitmap
   case TEXT('B'): // .bmp
      imageTypeNew = imageBitmap;
      hImageNew = LoadImage(NULL, path,
         IMAGE_BITMAP, 0, 0, 
         LR_LOADFROMFILE | LR_DEFAULTCOLOR);
      break;
   case TEXT('i'): // icon
   case TEXT('I'): // .ico
      imageTypeNew = imageIcon;
      hImageNew = LoadImage(NULL, path,
         IMAGE_ICON, 0, 0, 
         LR_LOADFROMFILE);
      break;
   case TEXT('w'): // Windows metafile
   case TEXT('W'): // .wmf
      //imageTypeNew= imageMetafile;
      //hImageNew = GetMetaFile(path);
      break;
      {
         HANDLE hFile = INVALID_HANDLE_VALUE;
         PVOID pvBuf = NULL;
         __try {
            hFile = CreateFile( path, GENERIC_READ, 0, NULL,
               OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL );
            if (hFile == INVALID_HANDLE_VALUE) __leave;
            DWORD dwFS = GetFileSize(hFile, NULL);
            if (!dwFS || (dwFS == 0xFFFFFFFF)) __leave;
            pvBuf = new BYTE[dwFS];
            if (pvBuf == NULL) __leave;
            DWORD dwNOBR;
            if (!ReadFile(hFile, pvBuf, dwFS, &dwNOBR, NULL) || (dwFS != dwNOBR)) __leave;
            METAFILEPICT sMFP = {MM_TEXT,10,10,NULL};
            hImageNew = SetWinMetaFileBits(dwFS, (BYTE*)pvBuf, hDCZoom, &sMFP);
            if (hImageNew == NULL) {
               DWORD err = GetLastError();
               TCHAR szBuf[64];
               _stprintf(szBuf, TEXT("Error code = %d\0"), err);
               MessageBox(NULL, szBuf, TEXT("Error code"), MB_ICONERROR);
               __leave;
            }
            imageTypeNew= imageEnhMetafile;
         } __finally {
            if (pvBuf != NULL) delete [] pvBuf;
            if (hFile != INVALID_HANDLE_VALUE) CloseHandle(hFile);
         }
      }
      break;
   case TEXT('e'): // enhanced-format metafile
   case TEXT('E'): // .emf
      imageTypeNew= imageEnhMetafile;
      hImageNew = GetEnhMetaFile(path);
      break;
   }
   if (hImageNew != NULL) {
      DeleteHandleImage();
      hImage = hImageNew;
      imageType = imageTypeNew;

      sizeImage = GetSizeImage();
      sizeZoom.x = sizeZoom.y = 0;
      return true;
   }
   return false;
}

void TcImage::LoadResource(const HINSTANCE& hInstance, LPCTSTR resourceName, TeImageType type) {
   DeleteHandleImage();
   imageType = imageUnknown;
   if (!resourceName) return;
   switch (type) {
   case imageBitmap:
      imageType = type;
      hImage = LoadImage(hInstance, resourceName,//MAKEINTRESOURCE(ID_BITMAP_NEW1),
         IMAGE_BITMAP, 0, 0,
         LR_DEFAULTCOLOR);
      break;
   case imageIcon:
      imageType = type;
      hImage = LoadImage(hInstance, resourceName,
         IMAGE_ICON, 0, 0,
         LR_DEFAULTCOLOR);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      imageType = type;
      {
         HRSRC hRsrc = FindResource(hInstance, resourceName, RT_RCDATA);
         HGLOBAL hGlobal = ::LoadResource(hInstance, hRsrc);
         DWORD size = SizeofResource(hInstance, hRsrc);
         hImage = SetEnhMetaFileBits(size, (BYTE *)LockResource(hGlobal));
      }
      break;
   }
   sizeImage = GetSizeImage();
   sizeZoom.x = sizeZoom.y = 0;
}

POINT TcImage::GetSizeImage() {
   POINT result = {0,0};
   switch (imageType) {
   case imageBitmap:
      BITMAP bmp;
      GetObject(hImage, sizeof(BITMAP), &bmp);
      result.x = bmp.bmWidth;
      result.y = bmp.bmHeight;
      break;
   case imageIcon:
      ICONINFO iconInfo;
      if (!GetIconInfo((HICON)hImage, &iconInfo)) break;
      result.x = iconInfo.xHotspot<<1;
      result.y = iconInfo.yHotspot<<1;
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      ENHMETAHEADER EnhMetaHeader;
      GetEnhMetaFileHeader((HENHMETAFILE)hImage, sizeof(ENHMETAHEADER), &EnhMetaHeader);
      result.x = EnhMetaHeader.rclBounds.right  - EnhMetaHeader.rclBounds.left;//EnhMetaHeader.szlDevice.cx;
      result.y = EnhMetaHeader.rclBounds.bottom - EnhMetaHeader.rclBounds.top; //EnhMetaHeader.szlDevice.cy;
      break;
   }
   return result;
}

BOOL TcImage::DeleteHandleImage() {
   if (!hImage) return TRUE;
   BOOL result;
   switch (imageType) {
   case imageBitmap:
      result = DeleteObject(hImage);
      break;
   case imageIcon:
      result = DestroyIcon((HICON)hImage);
      break;
   case imageMetafile:
      break;
   case imageEnhMetafile:
      result = DeleteEnhMetaFile((HENHMETAFILE)hImage);
      break;
   case imageUnknown:
      result = FALSE;
      break;
   }
   hImage = NULL;
   return result;
}

void TcImage::SetBrush(HBRUSH hBrush) {
   DeleteObject(SelectObject(hDCZoom, hBrush));
}

void TcImage::SetTransparent(bool newTransparent) {
   transparent = newTransparent;
}

TeImageType TcImage::GetImageType() {
   return imageType;
}
