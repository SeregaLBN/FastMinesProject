namespace fmg.core.img {

   /// <summary> MVC: model. Animated image characteristics </summary>
   public class AnimatedImageModel : ImageModel {

      /** animation of polar lights */
      private boolean _polarLights = true;

      /** animation direction (example: clockwise or counterclockwise for simple rotation) */
      private boolean _animeDirection = true;

      public boolean PolarLights {
         get { return _polarLights; }
         set { _notifier.SetProperty(_polarLights, value); }
      }

      public boolean AnimeDirection {
         get { return _animeDirection; }
         set { _notifier.SetProperty(_animeDirection, value); }
      }

   }

}
