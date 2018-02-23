package fmg.core.img;

/** Transforming image model data */
public interface IModelTransformer {

   void execute(int currentFrame, int totalFrames, IImageModel model);

}
