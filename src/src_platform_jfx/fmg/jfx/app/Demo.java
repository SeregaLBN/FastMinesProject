package fmg.jfx.app;

import java.util.concurrent.ThreadLocalRandom;

import fmg.jfx.img.*;
import fmg.jfx.mosaic.MosaicCanvasController;

public class Demo {

    public static void main(String[] args) {
        switch (ThreadLocalRandom.current().nextInt(7)) {
        case 0:
            Flag.main(args);
            break;
        case 1:
            Logo.main(args);
            break;
        case 2:
            Mine.main(args);
            break;
        case 3:
            MosaicGroupImg.main(args);
            break;
        case 4:
            MosaicSkillImg.main(args);
            break;
        case 5:
            MosaicImg.main(args);
            break;
        case 6:
            MosaicCanvasController.main(args);
            break;
        default:
            break;
        }
    }

}
