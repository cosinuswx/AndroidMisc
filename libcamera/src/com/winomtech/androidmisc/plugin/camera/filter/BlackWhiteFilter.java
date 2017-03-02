package com.winomtech.androidmisc.plugin.camera.filter;

/**
 * @author kevinhuang
 * @since 2017-03-02
 */
public class BlackWhiteFilter extends GPUImageFilter {
    private final static String BLACK_WHITE_FRAGMENT_SHADER = ""
            + "varying highp vec2 textureCoordinate;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + "void main() {\n"
            + "    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "    float color = (textureColor.r + textureColor.g + textureColor.b) / 3.0;\n"
            + "    gl_FragColor = vec4(color, color, color, 1.0);\n"
            + "}\n";

    public BlackWhiteFilter() {
        super(NO_FILTER_VERTEX_SHADER, BLACK_WHITE_FRAGMENT_SHADER);
    }
}
