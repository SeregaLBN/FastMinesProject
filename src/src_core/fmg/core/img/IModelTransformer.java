package fmg.core.img;

/** Transforming image model data. Usage for image animations. */
public interface IModelTransformer {

   /** The handler for the frame change event */
   void execute(int currentFrame, int totalFrames, AnimatedImageModel model);

}
