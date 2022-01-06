using System.ComponentModel;

namespace Fmg.Core.Img {

    /// <summary> MVC: view of images with burger menu (where burger menu its secondary model) </summary>
    /// <typeparam name="TImage">platform specific image</typeparam>
    /// <typeparam name="TImageModel">general model of image (not burger menu model)</typeparam>
    public abstract class WithBurgerMenuView<TImage, TImageModel>
                                 : ImageView<TImage, TImageModel>
        where TImage : class
        where TImageModel : AnimatedImageModel
    {
        /// <summary> the second model of image </summary>
        private readonly BurgerMenuModel _burgerMenuModel;

        protected WithBurgerMenuView(TImageModel imageModel)
            : base(imageModel)
        {
            _burgerMenuModel = new BurgerMenuModel(imageModel);
            _burgerMenuModel.PropertyChanged += OnBurgerMenuModelPropertyChanged;
        }

        public BurgerMenuModel BurgerMenuModel => _burgerMenuModel;

        protected void OnBurgerMenuModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _burgerMenuModel));
            Invalidate();
        }

        protected override void Disposing() {
            _burgerMenuModel.PropertyChanged -= OnBurgerMenuModelPropertyChanged;
            _burgerMenuModel.Dispose();
            base.Disposing();
        }

    }

}
