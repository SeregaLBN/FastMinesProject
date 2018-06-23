using fmg.core.img;

/// <summary> Transforming image model data. Usage for image animations </summary>
public interface IModelTransformer {

   /// <summary> The handler for the frame change event </summary>
   /// <param name="currentFrame"></param>
   /// <param name="totalFrames"></param>
   /// <param name="model"></param>
   void Execute(int currentFrame, int totalFrames, IImageModel model);

}
