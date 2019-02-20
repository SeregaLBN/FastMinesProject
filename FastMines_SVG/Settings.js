/** common settings this SVG */
var Settings = {
    width: 108,  // view width
    height: 108, // view height
    pad: 108*10/2020, // padding

    testedImageType: 0, // 0 - Logo image ; 1-MosaicSkill image

    animated: !true,
    animeDirection: true, // clockwise: ↻ or ↺
    animatePeriod: 2000, // rotate period in msec
    /** the number of parts of which the one turn */
    totalFrames: 30, // animate iterations

    borderWidth: 0,
    backgroundColor: Color.Transparent,
    foregroundAlpha: 200,  // 0..255 - foreground alpha-chanel color

    /** ESkillLevel */
    mosaicSkill: null, // ESkillLevel.eBeginner eAmateur eProfi eCrazy eCustom

    showBurgerMenu: true,

    useGradientFill: true, // for Logo

    useRotateTransforming: true,
    usePolarLightFgTransforming: true,

    useRandom: !true,

    modify: function() {
        if (!Settings.useRandom)
            return;

        var b = function() { return (Math.random() < 0.5); }; // random boolean

        Settings.width = Settings.height = 100 + random(100);
        Settings.pad = 5 + random(10);
        Settings.borderWidth = !random(3) ? 0: (0.3 + 2 * Math.random()),
        Settings.backgroundColor = !random(5) ? Color.Transparent : Color.RandomColor.brighter(0.4),
        Settings.foregroundAlpha = 150 + random(255-150), // 200  // 0..255 - foreground alpha-chanel color
        Settings.testedImageType = random(2);
        Settings.mosaicSkill = (function() {
                switch (random(6)) {
                case 0: return ESkillLevel.eBeginner;
                case 1: return ESkillLevel.eAmateur ;
                case 2: return ESkillLevel.eProfi   ;
                case 3: return ESkillLevel.eCrazy   ;
                case 4: return ESkillLevel.eCustom  ;
                case 5: return null;
                default: throw new Error('Bad value');
                }
            })();
        Settings.showBurgerMenu = b();
        Settings.useGradientFill = b();

        Settings.animated = b();
        if (Settings.animated) {
            Settings.animeDirection = b();
            Settings.animatePeriod = 1500 + random(2500);
            if (Settings.testedImageType == 1) {
                Settings.totalFrames = (Settings.mosaicSkill==null)
                    ? 120 // что бы при calcMode='discrete' не 'дёргалось'
                    : 30;
            }

            Settings.useRotateTransforming = b();
            if (!Settings.useRotateTransforming)
                Settings.usePolarLightFgTransforming = true; // при анимації мусить бути хоча б один трансформер, інакше не буде анімації
            else
                Settings.usePolarLightFgTransforming = b();
        }
    }

}
