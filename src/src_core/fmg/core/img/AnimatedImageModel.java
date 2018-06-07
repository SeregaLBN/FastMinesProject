package fmg.core.img;

/** MVC: model. Animated image characteristics. */
public class AnimatedImageModel extends ImageModel {

   public static final String PROPERTY_POLAR_LIGHTS    = "PolarLights";
   public static final String PROPERTY_ANIME_DIRECTION = "AnimeDirection";

   /** animation of polar lights */
   private boolean _polarLights = true;

   /** animation direction (example: clockwise or counterclockwise for simple rotation) */
   private boolean _animeDirection = true;

   public boolean isPolarLights() { return _polarLights; }

   public boolean getAnimeDirection() { return _animeDirection; }

   public void setPolarLights(boolean polarLights) {
      _notifier.setProperty(_polarLights, polarLights, PROPERTY_POLAR_LIGHTS);
   }

   public void setAnimeDirection(boolean animeDirection) {
      _notifier.setProperty(_animeDirection, animeDirection, PROPERTY_ANIME_DIRECTION);
   }

}
