package model.renderEngine;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;

public class SpecialImage extends BufferedImage {

    private static final ColorModel model = createColorModel();

    public SpecialImage(final int width, final int height) {
        super(model, model.createCompatibleWritableRaster(width, height), false, null);

    }

    private static final ColorModel createColorModel() {
        return new DirectColorModel(32,
                0x00ff0000,       // Red
                0x0000ff00,       // Green
                0x000000ff,       // Blue
                0xff000000        // Alpha
                );
//        return new DirectColorModel(16, DCM_555_RED_MASK, DCM_555_GRN_MASK, DCM_555_BLU_MASK, DCM_555_ALP_MASK);
    }
}
