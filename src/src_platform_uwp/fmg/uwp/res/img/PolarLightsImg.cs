using System;

namespace fmg.uwp.res.img {

   public abstract class PolarLightsImg<T, TImage> : RotatedImg<T, TImage> where TImage : class
   {

      protected PolarLightsImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(entity, widthAndHeight, padding)
      { }

      private bool _polarLights;
      /// <summary> shimmering filling </summary>
      public bool PolarLights
      {
         get { return _polarLights; }
         set
         {
            if (SetProperty(ref _polarLights, value))
               Draw();
         }
      }

      protected void NextFillColor() {
         if (PolarLights) {
            Func<byte, byte> funcAddRandomBit = val => (byte) ((((_random.Next() & 1) == 1) ? 0x00 : 0x80) | (val >> 1));
            var f = _fillColor;
            switch (_random.Next()%3) {
            case 0: f.R = funcAddRandomBit(f.R); break;
            case 1: f.G = funcAddRandomBit(f.G); break;
            case 2: f.B = funcAddRandomBit(f.B); break;
            }
            _fillColor = f;
         }

      }
   }
}
