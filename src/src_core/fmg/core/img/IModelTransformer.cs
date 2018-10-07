using fmg.core.img;

/// <summary> Transforming image model data. Usage for image animations </summary>
public interface IModelTransformer {

    /// <summary> The handler for the frame change event </summary>
    /// <param name="model"></param>
    void Execute(IAnimatedModel model);

}
