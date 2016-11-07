/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * Based on the work of fadden
 * 
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dlighttech.camera2.view;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
@SuppressLint("InlinedApi")
public class TwoTextureManager {

	public final static String TAG = "TwoTextureManager";

	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	private final float[] mTriangleVerticesData = {
//			// X, Y, Z, U, V
//			-1.0f, -1.0f, 0, 0.f, 0.f,
//			0.0f, -1.0f, 0, 1.f, 0.f,
//			-1.0f,  1.0f, 0, 0.f, 1.f,
//			0.0f,  1.0f, 0, 1.f, 1.f,
//
//			// T2
//			0.0f, -1.0f, 0, 0.f, 0.f,
//			1.0f, -1.0f, 0, 1.f, 0.f,
//			0.0f,  1.0f, 0, 0.f, 1.f,
//			1.0f,  1.0f, 0, 1.f, 1.f,

			//			// X, Y, Z, U, V
			-1.0f, -1.0f, 0, 0.f, 0.f,
			0.0f, -1.0f, 0, 0.f, 1.f,
			-1.0f,  1.0f, 0, 1.f, 0.f,
			0.0f,  1.0f, 0, 1.f, 1.f,

			// T2
			0.0f, -1.0f, 0, 0.f, 0.f,
			1.0f, -1.0f, 0, 0.f, 1.f,
			0.0f,  1.0f, 0, 1.f, 0.f,
			1.0f,  1.0f, 0, 1.f, 1.f,

	};

	private FloatBuffer mTriangleVertices;

	private static final String VERTEX_SHADER =
			"uniform mat4 uMVPMatrix;\n" +
					"uniform mat4 uSTMatrixLeft;\n" +
					"uniform mat4 uSTMatrixRight;\n" +
					"attribute vec4 aPosition;\n" +
					"attribute vec4 aTextureCoord;\n" +
					"varying vec2 vTextureCoord;\n" +
					"varying float vTexture;\n" +
					"void main() {\n" +
					"  gl_Position = uMVPMatrix * aPosition;\n" +
					"  if (aPosition.x < 0.0) {\n" +
					"    vTexture = -1.0;\n" +
					"    vTextureCoord = (uSTMatrixLeft * aTextureCoord).xy;\n" +
					"  } else {\n" +
					"    vTexture = 1.0;\n" +
					"    vTextureCoord = (uSTMatrixRight * aTextureCoord).xy;\n" +
					"  }\n" +
					"}\n";

	private static final String FRAGMENT_SHADER =
			"#extension GL_OES_EGL_image_external : require\n" +
					//"precision mediump float;\n" +      // highp here doesn't seem to matter
					"precision highp float;\n" +      // highp here doesn't seem to matter
					"varying vec2 vTextureCoord;\n" +
					"varying float vTexture;\n" +
					"uniform samplerExternalOES sTextureLeft;\n" +
					"uniform samplerExternalOES sTextureRight;\n" +
					"void main() {\n" +
					"  if (vTexture < 1.0) {\n" +
					"    gl_FragColor = texture2D(sTextureLeft, vTextureCoord);\n" +
                    //"    gl_FragColor.g = 1.0;\n" +
					"  } else {\n" +
					//"    vec4 l_FragColor = texture2D(sTextureRight, vTextureCoord);\n" +
                    //"    gl_FragColor = texture2D(sTextureRight, vTextureCoord);\n" +
                    "    gl_FragColor = texture2D(sTextureRight, vTextureCoord);\n" +
                    //"    gl_FragColor.r = 1.0;\n" +
					"  }\n" +
					"}\n";

	private float[] mMVPMatrix = new float[16];
	private float[] mSTMatrixLeft = new float[16];
	private float[] mSTMatrixRight = new float[16];

	private int mProgram;
	private int mTextureIDLeft = -12345;
	private int mTextureIDRight = -12345;
	private int muMVPMatrixHandle;
	private int muSTMatrixLeftHandle;
	private int muSTMatrixRightHandle;
	private int maPositionHandle;
	private int maTextureHandle;
	private int muTextureLeftHandle;
	private int muTextureRightHandle;

	private SurfaceTexture mSurfaceTextureLeft;
	private SurfaceTexture mSurfaceTextureRight;

	public TwoTextureManager() {
		mTriangleVertices = ByteBuffer.allocateDirect(
				mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangleVertices.put(mTriangleVerticesData).position(0);

		Matrix.setIdentityM(mSTMatrixLeft, 0);
		Matrix.setIdentityM(mSTMatrixRight, 0);
	}

	public int getTextureIdLeft() {
		return mTextureIDLeft;
	}
	
	public SurfaceTexture getSurfaceTextureLeft() {
		return mSurfaceTextureLeft;
	}

	public SurfaceTexture getSurfaceTextureRight() {
		return mSurfaceTextureRight;
	}

	public void updateFrame() {
		mSurfaceTextureLeft.updateTexImage();
		mSurfaceTextureRight.updateTexImage();
	}
	
	public void drawFrame() {	
		checkGlError("onDrawFrame start");
		mSurfaceTextureLeft.getTransformMatrix(mSTMatrixLeft);
		mSurfaceTextureRight.getTransformMatrix(mSTMatrixRight);

		//GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
		//GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		GLES20.glUseProgram(mProgram);
		checkGlError("glUseProgram");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glUniform1f muTextureLeft");
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        checkGlError("glUniform1f muTextureLeft");
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIDLeft);
        checkGlError("glUniform1f muTextureLeft");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIDRight);

        GLES20.glUniform1i(muTextureLeftHandle, 0);
        checkGlError("glUniform1i muTextureLeft");
        GLES20.glUniform1i(muTextureRightHandle, 1);
        checkGlError("glUniform1i muTextureRight");

		mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
		checkGlError("glVertexAttribPointer maPosition");
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		checkGlError("glEnableVertexAttribArray maPositionHandle");

		mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
		GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
		checkGlError("glVertexAttribPointer maTextureHandle");
		GLES20.glEnableVertexAttribArray(maTextureHandle);
		checkGlError("glEnableVertexAttribArray maTextureHandle");

		Matrix.setIdentityM(mMVPMatrix, 0);

//        Matrix.setIdentityM(mSTMatrixLeft, 0);
//        Matrix.setIdentityM(mSTMatrixRight, 0);

		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(muSTMatrixLeftHandle, 1, false, mSTMatrixLeft, 0);
		GLES20.glUniformMatrix4fv(muSTMatrixRightHandle, 1, false, mSTMatrixRight, 0);

        // draw left plane
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        // draw right plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 4, 4);
        checkGlError("glDrawArrays");

		GLES20.glLineWidth(10);

		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);
		checkGlError("glDrawArrays");

		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 4, 4);
		checkGlError("glDrawArrays");

		GLES20.glFinish();
	}

	/**
	 * Initializes GL state.  Call this after the EGL surface has been created and made current.
	 */
	public void createTexture() {
		mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		if (mProgram == 0) {
			throw new RuntimeException("failed creating program");
		}
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		checkGlError("glGetAttribLocation aPosition");
		if (maPositionHandle == -1) {
			throw new RuntimeException("Could not get attrib location for aPosition");
		}
		maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
		checkGlError("glGetAttribLocation aTextureCoord");
		if (maTextureHandle == -1) {
			throw new RuntimeException("Could not get attrib location for aTextureCoord");
		}

		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		checkGlError("glGetUniformLocation uMVPMatrix");
		if (muMVPMatrixHandle == -1) {
			throw new RuntimeException("Could not get attrib location for uMVPMatrix");
		}

		muSTMatrixLeftHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrixLeft");
		checkGlError("glGetUniformLocation uSTMatrix");
		if (muSTMatrixLeftHandle == -1) {
			throw new RuntimeException("Could not get attrib location for uSTMatrix");
		}

		muSTMatrixRightHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrixRight");
		checkGlError("glGetUniformLocation uSTMatrix");
		if (muSTMatrixRightHandle == -1) {
			throw new RuntimeException("Could not get attrib location for uSTMatrix");
		}

        muTextureLeftHandle = GLES20.glGetUniformLocation(mProgram, "sTextureLeft");
        checkGlError("glGetUniformLocation sTextureLeft");
        if (muTextureLeftHandle == -1) {
            throw new RuntimeException("Could not get attrib location for sTextureLeft");
        }

        muTextureRightHandle = GLES20.glGetUniformLocation(mProgram, "sTextureRight");
        checkGlError("glGetUniformLocation sTextureRight");
        if (muTextureRightHandle == -1) {
            throw new RuntimeException("Could not get attrib location for sTextureRight");
        }

		int[] textures = new int[2];
		GLES20.glGenTextures(2, textures, 0);

		mTextureIDLeft = textures[0];
		mTextureIDRight = textures[1];

		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIDLeft);
		checkGlError("glBindTexture mTextureID");

		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		checkGlError("glTexParameter");

		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIDRight);
		checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		checkGlError("glTexParameter");

		//GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
		
		mSurfaceTextureLeft = new SurfaceTexture(mTextureIDLeft);
        mSurfaceTextureRight = new SurfaceTexture(mTextureIDRight);

		return ;
	}

	public void release() {
		mSurfaceTextureLeft = null;
        mSurfaceTextureRight = null;
	}
	
	/**
	 * Replaces the fragment shader.  Pass in null to reset to default.
	 */
	public void changeFragmentShader(String fragmentShader) {
		if (fragmentShader == null) {
			fragmentShader = FRAGMENT_SHADER;
		}
		GLES20.glDeleteProgram(mProgram);
		mProgram = createProgram(VERTEX_SHADER, fragmentShader);
		if (mProgram == 0) {
			throw new RuntimeException("failed creating program");
		}
	}

	private int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		checkGlError("glCreateShader type=" + shaderType);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(TAG, "Could not compile shader " + shaderType + ":");
			Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		return shader;
	}

	private int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0) {
			return 0;
		}
		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (pixelShader == 0) {
			return 0;
		}

		int program = GLES20.glCreateProgram();
		checkGlError("glCreateProgram");
		if (program == 0) {
			Log.e(TAG, "Could not create program");
		}
		GLES20.glAttachShader(program, vertexShader);
		checkGlError("glAttachShader");
		GLES20.glAttachShader(program, pixelShader);
		checkGlError("glAttachShader");
		GLES20.glLinkProgram(program);
		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.e(TAG, "Could not link program: ");
			Log.e(TAG, GLES20.glGetProgramInfoLog(program));
			GLES20.glDeleteProgram(program);
			program = 0;
		}
		return program;
	}

	public void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}
}
