package com.feng.libcamera.utils;

import android.opengl.GLES20;


public class FBOUtils {
    //--------------------------------------FBO绘制----------------------------------------
    private ProgramTextureOES programTextureOES;
    private int[] mOriginViewPort = new int[4];
    private int[] mFboId = new int[1];

    Program mMimojiRender = new ProgramTexture2d();

    public int drawFBO(int texId, int width, int height, float[] texMtx, float[] mvp) {
        createFBO(width, height);
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFboId, 0);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mOriginViewPort, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
        GLES20.glViewport(0, 0, width, height);
        if (programTextureOES == null) {
            programTextureOES = new ProgramTextureOES();
        }
        programTextureOES.drawFrame(texId, texMtx, mvp);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
        GLES20.glViewport(mOriginViewPort[0], mOriginViewPort[1], mOriginViewPort[2], mOriginViewPort[3]);

        return fboTex[0];
    }

   /* public int drawFBO(DrawMimoijTexAttribute mMimoijTexAttribute) {
        createFBO(mMimoijTexAttribute.mViewWidth, mMimoijTexAttribute.mViewHeight);
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFboId, 0);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mOriginViewPort, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
        GLES20.glViewport(0, 0, mMimoijTexAttribute.mViewWidth, mMimoijTexAttribute.mViewHeight);
        if (mMimojiRender == null) {
            mMimojiRender = new MimojiRender(null);
        }
        mMimojiRender.draw(mMimoijTexAttribute);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
        GLES20.glViewport(mOriginViewPort[0], mOriginViewPort[1], mOriginViewPort[2], mOriginViewPort[3]);

        return fboTex[0];
    }*/

    private int fboId[];
    private int fboTex[];
    private int renderBufferId[];

    private int fboWidth, fboHeight;
    private int num = 1;

    public void createFBO(int width, int height) {
        if (fboTex != null && (fboWidth != width || fboHeight != height)) {
            deleteFBO();
        }

        fboWidth = width;
        fboHeight = height;

        if (fboTex == null) {
            fboId = new int[num];
            fboTex = new int[num];
            renderBufferId = new int[num];

//generate fbo id
            GLES20.glGenFramebuffers(num, fboId, 0);
//generate texture
            GLES20.glGenTextures(num, fboTex, 0);
//generate render buffer
            GLES20.glGenRenderbuffers(num, renderBufferId, 0);

            for (int i = 0; i < fboId.length; i++) {
//Bind Frame buffer
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[i]);
//Bind texture
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTex[i]);
//Define texture parameters
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//Bind render buffer and define buffer dimension
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId[i]);
                GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
//Attach texture FBO color attachment
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex[i], 0);
//Attach render buffer to depth attachment
                GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId[i]);
//we are done, reset
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }

    public void bindFrameBufferInfo() {
        // 1. 绑定FrameBuffer到当前的绘制环境上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
        // 2. 将纹理对象挂载到FrameBuffer上，存储颜色信息
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTex[0], 0);
    }

    public void unbindFrameBufferInfo() {
        // 解绑FrameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        deleteFBO();
    }


    public void deleteFBO() {
        if (fboId == null || fboTex == null || renderBufferId == null) {
            return;
        }
        GLES20.glDeleteFramebuffers(num, fboId, 0);
        GLES20.glDeleteTextures(num, fboTex, 0);
        GLES20.glDeleteRenderbuffers(num, renderBufferId, 0);
        fboId = null;
        fboTex = null;
        renderBufferId = null;
    }

    public void release() {
        deleteFBO();
        if (mMimojiRender != null) {
            mMimojiRender.release();
            mMimojiRender = null;
        }
        if (programTextureOES != null) {
            programTextureOES.release();
            programTextureOES = null;
        }
    }
}
