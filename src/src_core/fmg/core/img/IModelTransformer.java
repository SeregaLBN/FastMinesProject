package fmg.core.img;

/** Transforming image model data. Usage for image inamations. */
public interface IModelTransformer {

   void execute(int currentFrame, int totalFrames, AnimatedImageModel model);

}
