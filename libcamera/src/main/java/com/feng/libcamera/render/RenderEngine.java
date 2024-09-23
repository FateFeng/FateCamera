package com.feng.libcamera.render;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.feng.libcamera.R;
import com.feng.libcamera.utils.GlUtil;
import com.feng.libcamera.utils.Util;

import java.nio.FloatBuffer;

public class RenderEngine {

    private Context mContext;

    private int mOESTextureId = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;

    private int mShaderProgram = -1;

    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int muMVPMatrixLoc = -1;

    public static final int SIZEOF_FLOAT = 4;
    public static final int COORDS_PER_VERTEX = 2;
    public static final int TEXTURE_COORD_STRIDE = 2 * SIZEOF_FLOAT;
    public static final int VERTEXTURE_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT;

    private FloatBuffer mTexCoordArray;
    private FloatBuffer mVertexArray;
    private int mVertexCount;


    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };

    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    public static final float[] sTransRotate = {
            -1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    public RenderEngine(int OESTextureId, Context context) {
        mContext = context;
        mOESTextureId = OESTextureId;
        updateVertexArray(FULL_RECTANGLE_COORDS);
        updateTexCoordArray(FULL_RECTANGLE_TEX_COORDS);
        vertexShader = loadShader(GL_VERTEX_SHADER, Util.readShaderFromResource(mContext, R.raw.vertex_shader));
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, Util.readShaderFromResource(mContext, R.raw.base_fragment_shader));
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
    }

    public static final String POSITION_ATTRIBUTE = "aPosition";
    public static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    public static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    public static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    public void updateVertexArray(float[] FULL_RECTANGLE_COORDS) {
        mVertexArray = GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
        mVertexCount = FULL_RECTANGLE_COORDS.length / COORDS_PER_VERTEX;
    }

    public void updateTexCoordArray(float[] FULL_RECTANGLE_TEX_COORDS) {
        mTexCoordArray = GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    }

    public int loadShader(int type, String shaderSource) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("Create Shader Failed!" + glGetError());
        }
        glShaderSource(shader, shaderSource);
        glCompileShader(shader);
        return shader;
    }

    public int linkProgram(int verShader, int fragShader) {
        int program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Create Program Failed!" + glGetError());
        }
        glAttachShader(program, verShader);
        glAttachShader(program, fragShader);
        glLinkProgram(program);

        glUseProgram(program);
        return program;
    }

    public void drawTexture(float[] transformMatrix, float[] mvpTransform) {
        Long t1 = System.currentTimeMillis();

        //glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        aPositionLocation = glGetAttribLocation(mShaderProgram, POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, TEXTURE_SAMPLER_UNIFORM);
        muMVPMatrixLoc = glGetUniformLocation(mShaderProgram, "uMVPMatrix");

        glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(uTextureSamplerLocation, 0);


        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);
        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpTransform, 0);


        // Connect vertexBuffer to "aPosition".
        glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, RenderEngine.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, RenderEngine.VERTEXTURE_STRIDE, mVertexArray);
        //GlUtil.checkGlError("glVertexAttribPointer");
        // Connect texBuffer to "aTextureCoord".
        glEnableVertexAttribArray(aTextureCoordLocation);
        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, RenderEngine.TEXTURE_COORD_STRIDE, mTexCoordArray);
        //GlUtil.checkGlError("glVertexAttribPointer");
        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        //GlUtil.checkGlError("glDrawArrays");


        //glDrawArrays(GL_TRIANGLES, 3, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        //Log.i("feng", "onDrawFrame: time: " + t);
    }

    public int getShaderProgram() {
        return mShaderProgram;
    }

    /**
     * Returns the array of vertices.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer vertexArray() {
        return mVertexArray;
    }

    /**
     * Returns the array of texture coordinates.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer texCoordArray() {
        return mTexCoordArray;
    }

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    public int vertexCount() {
        return mVertexCount;
    }

    public int getOESTextureId() {
        return mOESTextureId;
    }

    public void setOESTextureId(int OESTextureId) {
        mOESTextureId = OESTextureId;
    }

    public void deinit() {
        mOESTextureId = -1;
        if (mTexCoordArray != null) {
            mTexCoordArray.clear();
            mTexCoordArray = null;
        }
        if (mVertexArray != null) {
            mVertexArray.clear();
            mVertexArray = null;
        }
    }
}

