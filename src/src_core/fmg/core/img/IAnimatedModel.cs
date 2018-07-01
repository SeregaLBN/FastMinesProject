namespace fmg.core.img {

   /// <summary>
   /// Image MVC: model.
   /// Model of animated image data/properties/characteristics
   /// </summary>
   public interface IAnimatedModel : IImageModel {

      bool Animated { get; set; }

      /** Overall animation period (in milliseconds) */
      long AnimatePeriod { get; set; }

      /** Total frames of the animated period */
      int TotalFrames { get; set; }

      int CurrentFrame { get; set; }

   }

}
