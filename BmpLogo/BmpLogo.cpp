// BmpLogo.cpp : Defines the entry point for the console application.
//

#include "tchar.h"
#include <crtdbg.h>
#include <windows.h>
#include <windowsX.h>
#pragma comment(lib, "msimg32")

COLOR16 toClr16(COLORREF clr, byte channel) {
   BYTE z = (BYTE)((clr >> (channel << 3)) & 0xFF); // select R or G or B
   COLOR16 res = z<<8; // cast
   return res;
}

HBITMAP CreateBitmap(UINT iWidth, UINT iHeight, COLORREF clrFill);
BOOL SaveBitmap(HBITMAP hBmp, LPCTSTR szBmpFile, BOOL bReplaceFile = TRUE);

int _tmain(int argc, _TCHAR* argv[])
{
   const float margin = 10;
   const float zoom = 10;
   const SIZE size = {200*zoom+2*margin, 200*zoom+2*margin};
   HBITMAP hBmp = CreateBitmap(size.cx, size.cy, 0xFFFFFF);

   HDC hCDC = ::GetDC(NULL);   _ASSERT_EXPR(hCDC, L"GetDC");
   HDC hDC = ::CreateCompatibleDC(hCDC);   _ASSERT_EXPR(hDC, L"CreateCompatibleDC");
   int iRes = ::ReleaseDC(NULL, hCDC);   _ASSERT_EXPR(iRes==1, L"ReleaseDC");

   HBITMAP hBmpDummy = SelectBitmap(hDC, hBmp);   _ASSERT_EXPR(hBmpDummy, L"SelectBitmap");

   { // draw star
      const int iPenWidth = 17;

      const COLORREF clrs[] = {0xFF0000, 0xFFD800, 0x4CFF00, 0x00FF90, 0x0094FF, 0x4800FF, 0xB200FF, 0xFF006E};
      const POINT rays[] = { // owner rays points
         {margin+100.0000*zoom, margin+200.0000*zoom},
         {margin+170.7107*zoom, margin+ 29.2893*zoom},
         {margin+  0.0000*zoom, margin+100.0000*zoom},
         {margin+170.7107*zoom, margin+170.7107*zoom},
         {margin+100.0000*zoom, margin+  0.0000*zoom},
         {margin+ 29.2893*zoom, margin+170.7107*zoom},
         {margin+200.0000*zoom, margin+100.0000*zoom},
         {margin+ 29.2893*zoom, margin+ 29.2893*zoom}};
      const POINT inn[] = { // inner octahedron
         {margin+100.0346*zoom, margin+141.4070*zoom},
         {margin+129.3408*zoom, margin+ 70.7320*zoom},
         {margin+ 58.5800*zoom, margin+100.0000*zoom},
         {margin+129.2500*zoom, margin+129.2500*zoom},
         {margin+ 99.9011*zoom, margin+ 58.5377*zoom},
         {margin+ 70.7233*zoom, margin+129.3198*zoom},
         {margin+141.4167*zoom, margin+100.0000*zoom},
         {margin+ 70.7500*zoom, margin+ 70.7500*zoom}};
      const POINT oct[] = { // central octahedron
         {margin+120.7053*zoom, margin+149.9897*zoom},
         {margin+120.7269*zoom, margin+ 50.0007*zoom},
         {margin+ 50.0034*zoom, margin+120.7137*zoom},
         {margin+150.0000*zoom, margin+120.6950*zoom},
         {margin+ 79.3120*zoom, margin+ 50.0007*zoom},
         {margin+ 79.2624*zoom, margin+149.9727*zoom},
         {margin+150.0000*zoom, margin+ 79.2737*zoom},
         {margin+ 50.0034*zoom, margin+ 79.3093*zoom}};

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         TRIVERTEX vert[] = {
            {
               rays[i].x,                 // LONG    x;
               rays[i].y,                 // LONG    y;
               toClr16(clrs[(i+1)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+1)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+1)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }, {
               oct[i].x,                  // LONG    x;
               oct[i].y,                  // LONG    y;   
               toClr16(clrs[(i+3)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+3)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+3)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }, {
               inn[i].x,                  // LONG    x;
               inn[i].y,                  // LONG    y;   
               toClr16(clrs[(i+6)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+6)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+6)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }, {
               oct[(i+5)%8].x,            // LONG    x;
               oct[(i+5)%8].y,            // LONG    y;   
               toClr16(clrs[(i+0)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+0)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+0)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }
         };
         GRADIENT_TRIANGLE gTri[] = {{0, 1, 2}, {0, 3, 2}};
         BOOL bRes = ::GradientFill(hDC, vert, 4, &gTri, 2, GRADIENT_FILL_TRIANGLE); _ASSERT(bRes);
      }

      // paint strar perimeter
      //::MoveToEx(hDC, rays[7].x, rays[7].y, NULL);
      //for (int i=0; i<8; i++) {
      //   HPEN hPen = ::CreatePen(PS_SOLID, iPenWidth, clrs[i]);   _ASSERT_EXPR(hPen, L"CreatePen");
      //   HPEN hPenTmp = SelectPen(hDC, hPen);   _ASSERT_EXPR(hPenTmp, L"SelectPen");
      //   ::LineTo(hDC, rays[i].x, rays[i].y);
      //   BOOL bRes = DeletePen(hPen);   _ASSERT_EXPR(bRes, L"DeletePen");
      //   hPenTmp = SelectPen(hDC, hPenTmp);   _ASSERT_EXPR(hPenTmp==hPen, L"released SelectPen");
      //}

      // paint inner gradient triangles
      for (int i=0; i<8; i++) {
         TRIVERTEX vert[] = {
            {
               inn[(i+0)%8].x,            // LONG    x;
               inn[(i+0)%8].y,            // LONG    y;
               toClr16(clrs[(i+6)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+6)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+6)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }, {
               inn[(i+3)%8].x,            // LONG    x;
               inn[(i+3)%8].y,            // LONG    y;   
               toClr16(clrs[(i+6)%8], 0), // COLOR16 Red;   0x0000..0xff00
               toClr16(clrs[(i+6)%8], 1), // COLOR16 Green;
               toClr16(clrs[(i+6)%8], 2), // COLOR16 Blue;
               0x0000                     // COLOR16 Alpha;
            }, {
               size.cx/2,  // LONG    x;
               size.cx/2,  // LONG    y;   
               (i&1)?0:0xFF00,     // COLOR16 Red;   0x0000..0xff00
               (i&1)?0:0xFF00,     // COLOR16 Green;
               (i&1)?0:0xFF00,     // COLOR16 Blue;
               0x0000      // COLOR16 Alpha;
            }
         };
         GRADIENT_TRIANGLE gTri1[] = {{0, 1, 2}};
         BOOL bRes = ::GradientFill(hDC, vert, 3, &gTri1, 1, GRADIENT_FILL_TRIANGLE); _ASSERT(bRes);
      }
   }

   hBmpDummy = SelectBitmap(hDC, hBmpDummy);   _ASSERT_EXPR(hBmpDummy==hBmp, L"released SelectBitmap");
   BOOL bRes = ::DeleteDC(hDC);   _ASSERT_EXPR(bRes, L"DeleteDC");


   bRes = ::SaveBitmap(hBmp, _T("fmLogoDemo.bmp"));   _ASSERT_EXPR(bRes, L"SaveBitmap");

   bRes = ::DeleteBitmap(hBmp);   _ASSERT_EXPR(bRes, L"DeleteBitmap");

	return 0;
}


HBITMAP CreateBitmap(UINT iWidth, UINT iHeight) {
   HDC hDC = ::GetDC(NULL);
   HBITMAP hBmp = ::CreateCompatibleBitmap(hDC, iWidth, iHeight);
   ::ReleaseDC(NULL, hDC);
   return hBmp;
}

HBITMAP CreateBitmap(UINT iWidth, UINT iHeight, COLORREF clrFill) {
   HDC hCDC = ::CreateCompatibleDC(NULL);
   HBITMAP hBmp = ::CreateBitmap(iWidth, iHeight); _ASSERT(!!hBmp);
   HBITMAP hBmpOld = (HBITMAP)::SelectObject(hCDC, hBmp);
   HBRUSH hBrush = ::CreateSolidBrush(clrFill);
   HBRUSH hBrushOld = (HBRUSH)::SelectObject(hCDC, hBrush);

   BOOL bRes = ::PatBlt(hCDC, 0,0,iWidth, iHeight, PATCOPY); _ASSERT(bRes);

   bRes = ::DeleteBrush(::SelectObject(hCDC, hBrushOld)); _ASSERT(bRes);
   ::SelectObject(hCDC, hBmpOld);
   bRes = ::DeleteDC(hCDC); _ASSERT(bRes);

   return hBmp;
}

#define WINDOWS_BITMAP_SIGNATURE 0x4D42 // Идентификатор типа файла BMP: 0x42 = "B" 0x4d = "M"

__forceinline WORD DIBNumColors(const BITMAPINFOHEADER &bih) { // Calculates the number of entries in the color table.
   if (bih.biClrUsed) {
      return (WORD)bih.biClrUsed;
   } else {
      switch (bih.biBitCount) {
      case 1:
      case 4:
      case 8:
         return 1 << bih.biBitCount;
      default: return 0;
      }
   }
}
__forceinline WORD PaletteSize(const BITMAPINFOHEADER &bih) { // Calculates the number of bytes in the color table.
   return DIBNumColors(bih) * sizeof(RGBQUAD);
}
__forceinline BYTE* FindDIBBits(const BITMAPINFOHEADER &bih) { // Locate the image bits in a CF_DIB format DIB.
   return ((BYTE*)&bih) + bih.biSize + PaletteSize(bih);
}
#define WIDTHBYTES(bits)      ((((bits) + 31)>>5)<<2) // How wide, in bytes, would this many bits be, DWORD aligned?
__forceinline DWORD BytesPerLine(const BITMAPINFOHEADER &bih) { // Calculates the number of bytes in one scan line.
   return WIDTHBYTES(bih.biWidth * bih.biPlanes * bih.biBitCount);
}
__forceinline DWORD DibSectionSize(const BITMAPINFOHEADER &bih) {
   if ((bih.biCompression == BI_RGB) && (bih.biSizeImage==0)) // biSizeImage may be set to zero for BI_RGB bitmaps.
      return BytesPerLine(bih) * bih.biHeight; // сам подсчитываю
   else
      return bih.biSizeImage;                  // беру то, что явно прописано
}
__forceinline DWORD BitmapSizeInBytes(const BITMAPINFOHEADER &bih) {
   return sizeof(BITMAPINFOHEADER) + PaletteSize(bih) + DibSectionSize(bih);
}

WORD GetBitsPixel(HBITMAP hBmp) {
   if (hBmp) {
      BITMAP bmp = {0,0,0,0,0,0,NULL};
      int iRes = ::GetObject(hBmp, sizeof(BITMAP), &bmp);
      if (iRes == sizeof(BITMAP))
         return bmp.bmBitsPixel;
   }
   return 0;
}

BOOL SaveBitmap(HDC hDC, HBITMAP hBmp, LPCTSTR szBmpFile, BOOL bReplaceFile) {
   if (!hDC || !hBmp || !szBmpFile || !szBmpFile[0]) {
      ::SetLastError(ERROR_INVALID_PARAMETER);
      return FALSE;
   }
   if (GetBitsPixel(hBmp) <= 8) {
      ::SetLastError(E_FAIL); // TODO не поддерживается %(
      return FALSE;
   }
   BOOL bRes = FALSE;
   DWORD dwErrCode = NO_ERROR;

   HANDLE hFile = ::CreateFile(
      szBmpFile,
      GENERIC_WRITE,
      FILE_SHARE_WRITE,
      NULL,
      bReplaceFile ? CREATE_ALWAYS : CREATE_NEW,
      0,
      NULL
   );
   bRes = (hFile != INVALID_HANDLE_VALUE);
   if (!bRes) {
      dwErrCode = ::GetLastError();
   } else {
      // uncompressed file struct:
      //     BITMAPFILEHEADER
      //     BITMAPINFOHEADER
      //     palette is == 0
      //     raw bitmap data

      BITMAPINFO bmpInfo;
      ZeroMemory(&bmpInfo, sizeof(BITMAPINFO));
      bmpInfo.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
      bmpInfo.bmiHeader.biBitCount = 0;

      bRes = !!::GetDIBits(hDC, hBmp, 0, 0, NULL, &bmpInfo, DIB_RGB_COLORS);
      if (!bRes) {
         dwErrCode = ::GetLastError();
      } else {

         BITMAPFILEHEADER bmpFH = {
            WINDOWS_BITMAP_SIGNATURE,                                // bfType
            sizeof(BITMAPFILEHEADER)+                                // bfSize
               BitmapSizeInBytes(bmpInfo.bmiHeader),
            0,0,                                                     // bfReserved1 bfReserved2
            sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER)+     // bfOffBits
               PaletteSize(bmpInfo.bmiHeader)};

         bmpInfo.bmiHeader.biCompression = BI_RGB; // no compression!

         DWORD dwData = DibSectionSize(bmpInfo.bmiHeader);
         BYTE *pData = new BYTE[dwData];
         bRes = !!pData;
         if (!bRes) {
            dwErrCode = ERROR_NOT_ENOUGH_MEMORY;
         } else {
            bRes = !!::GetDIBits(hDC, hBmp, 0, bmpInfo.bmiHeader.biHeight, pData, &bmpInfo, DIB_RGB_COLORS);
            if (!bRes) {
               dwErrCode = ::GetLastError();
            } else {
               // Writing Bitmap
               DWORD dwNBW = 0;
               bRes = (::WriteFile(hFile, &bmpFH            , sizeof(BITMAPFILEHEADER), &dwNBW, NULL) && (dwNBW == sizeof(BITMAPFILEHEADER))) &&
                      (::WriteFile(hFile, &bmpInfo.bmiHeader, sizeof(BITMAPINFOHEADER), &dwNBW, NULL) && (dwNBW == sizeof(BITMAPINFOHEADER))) &&
                      (::WriteFile(hFile, pData             , dwData                  , &dwNBW, NULL) && (dwNBW == dwData));
               if (!bRes)
                  dwErrCode = ::GetLastError();
            }
            delete(pData); pData = NULL;
         }
      }
      ::CloseHandle(hFile);
   }

   ::SetLastError(dwErrCode);
   return bRes;
}

BOOL SaveBitmap(HBITMAP hBmp, LPCTSTR szBmpFile, BOOL bReplaceFile) {
   BOOL bRes = FALSE;
   DWORD dwErrCode = NO_ERROR;

   HDC hDC = ::CreateCompatibleDC(NULL);
   bRes = !!hDC;
   if (!bRes) {
      dwErrCode = ::GetLastError();
   } else {
      HBITMAP hBmpOld = (HBITMAP)::SelectObject(hDC, hBmp);
      _ASSERT_EXPR(!!hBmpOld, L"Для этой ф-ции хэндл битмапы должен быть свободным (не выбранным на контекст - SelectObject(hDC))");
      bRes = !!hBmpOld;
      if (!bRes) {
         dwErrCode = ERROR_INVALID_PARAMETER;
      } else {
         bRes = SaveBitmap(hDC, hBmp, szBmpFile, bReplaceFile);
         if (!bRes)
            dwErrCode = ::GetLastError();
         ::SelectObject(hDC, hBmpOld); // restore
      }
      BOOL bRes2 = ::DeleteDC(hDC); _ASSERT(bRes2);
   }

   ::SetLastError(dwErrCode);
   return bRes;
}