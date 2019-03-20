using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using fmg.common.geom;
using fmg.common.notifier;
using fmg.common.ui;

namespace fmg.core.img {

    /// <summary> MVC controller. Base animation controller. </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageView">MVC view</typeparam>
    /// <typeparam name="TImageModel">MVC model</typeparam>
    public sealed class AnimatedInnerController<TImage, TImageView, TImageModel>
                          : IAnimatedController<TImage, TImageView, TImageModel>
        where TImage : class
        where TImageView : IImageView<TImage, TImageModel>
        where TImageModel : IAnimatedModel
    {
        private readonly TImageModel _model;
        private IDictionary<Type, IModelTransformer> _transformers = new Dictionary<Type, IModelTransformer>();
#pragma warning disable CS0067 // warning CS0067: The event is never used
        public event PropertyChangedEventHandler PropertyChanged; // TODO unusable
#pragma warning restore CS0067
        private bool _animationWasUsed = false;

        public AnimatedInnerController(TImageModel model) {
            _model = model;
            _model.PropertyChanged += OnPropertyModelChanged;
        }


        #region: begin unusable code
        public TImageModel Model { get { throw new NotImplementedException(); } }
        public TImage      Image { get { throw new NotImplementedException(); } }
        public SizeDouble  Size  { get { throw new NotImplementedException(); } }
        public void UseRotateTransforming(bool enable)       { throw new NotImplementedException(); }
        public void UsePolarLightFgTransforming(bool enable) { throw new NotImplementedException(); }
        #endregion: end unusable code


        public void RemoveModelTransformer(Type /* extends IModelTransformer */ transformerClass) {
            if (_transformers.ContainsKey(transformerClass))
                _transformers.Remove(transformerClass);
        }
        public void AddModelTransformer(IModelTransformer transformer) {
            if (!_transformers.ContainsKey(transformer.GetType()))
                _transformers.Add(transformer.GetType(), transformer);
        }

        private void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(IAnimatedModel.Animated):
                _animationWasUsed = true;
                if ((ev as PropertyChangedExEventArgs<bool>).NewValue) {
                    TImageModel model = _model;
                    UiInvoker.Animator().Subscribe(this, timeFromStartSubscribe => {
                        var mod = timeFromStartSubscribe.TotalMilliseconds % model.AnimatePeriod;
                        var frame = mod * model.TotalFrames / model.AnimatePeriod;
                        model.CurrentFrame = (int)frame;
                        //System.Diagnostics.Debug.WriteLine("ANIMATOR : CurrentFrame" + frame + "/" + model.TotalFrames);
                    });
                } else {
                    UiInvoker.Animator().Pause(this);
                }
                break;
            case nameof(IAnimatedModel.CurrentFrame):
                if (!_transformers.Any())
                    System.Diagnostics.Debug.WriteLine("No any transformer! " + GetType().Name); // зачем работать анимации если нет трансформеров модели
                foreach (var item in _transformers)
                    item.Value.Execute(_model);
                break;
            }
        }

        public void Dispose() {
            _model.PropertyChanged -= OnPropertyModelChanged;
            if (_animationWasUsed) // do not call UiInvoker.Animator if it is not already used
                UiInvoker.Animator().Unsubscribe(this);
            _transformers.Clear();
            GC.SuppressFinalize(this);
        }

    }

}
