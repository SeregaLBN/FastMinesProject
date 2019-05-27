namespace Fmg.Core.Img {

    /// <summary>
    /// Image MVC: model.
    /// Model of animated image data/properties/characteristics
    /// </summary>
    public interface IAnimatedModel : IImageModel {

        bool Animated { get; set; }

        /// <summary>Overall animation period (in milliseconds) </summary>
        long AnimatePeriod { get; set; }

        /// <summary>Total frames of the animated period (animate iterations) </summary>
        int TotalFrames { get; set; }

        int CurrentFrame { get; set; }

    }

}
