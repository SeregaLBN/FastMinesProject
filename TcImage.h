////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcImage.h"
//
// Описание класса TcImage
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCIMAGE
#define FILE_TCIMAGE

#include ".\Preproc.h"
#include <windows.h>

void DrawMaskedBitmap(HDC hDC, HBITMAP hBmp,
                      int xD, int yD,
                      int wD, int hD,
                      int wS, int hS,
                      COLORREF transparentColor = -1);

enum TePlace {
   placeCenter,
   placeStretch, // pастянуть
   placeTile     // замостить
};

enum TeImageType {
   imageBitmap,
   imageIcon,
   imageMetafile,
   imageEnhMetafile,
   imageUnknown
};

class TcImage {
private:
   TeImageType imageType;
   HANDLE hImage;       // original image
   HDC hDCZoom;         // zoomed image
   HDC hDCMask;         // mask for zoomed image
   bool transparent;    // image is transpared ?
   POINT sizeImage;     // original size image
   mutable POINT sizeZoom;      // zoomed size image
private:
   POINT GetSizeImage();
   void ZoomImage(POINT& newSizeZoom) const;
   BOOL DeleteHandleImage();
public:
   TcImage();
   ~TcImage();

   TeImageType GetImageType();

   void SetBrush(HBRUSH hBrush);
   void SetTransparent(bool newTransparent);

   void DrawImage     (const HDC hDC, const RECT* pRect) const;
   void DrawImagePlace(const HDC hDC, const RECT* pRect, const TePlace place) const;

   bool LoadFile(LPCTSTR path);
   void LoadResource(const HINSTANCE& hInstance, LPCTSTR resourceName, TeImageType type);
};

#endif // FILE_TCIMAGE
