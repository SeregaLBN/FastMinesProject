namespace fmg.core.img {

   /// <summary> MVC: model. Animated image characteristics </summary>
   public class AnimatedImageModel : ImageModel {

      /** animation of polar lights */
      private bool _polarLights = true;

      /** animation direction (example: clockwise or counterclockwise for simple rotation) */
      private bool _animeDirection = true;

      public bool PolarLights {
         get { return _polarLights; }
         set { _notifier.SetProperty(ref _polarLights, value); }
      }

      public bool AnimeDirection {
         get { return _animeDirection; }
         set { _notifier.SetProperty(ref _animeDirection, value); }
      }

   }

}
