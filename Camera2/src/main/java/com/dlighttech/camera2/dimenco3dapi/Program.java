package com.dlighttech.camera2.dimenco3dapi;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class encapsulating an OpenGL shader program.
 */
public class Program {

    /**
     * Initialize the program. Must be called while context is current.
     */
    public void init() {
        mProgId = GLES20.glCreateProgram();
        mVertShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        mFragShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
    }

    /**
     * Set the vertex shader source as a string.
     *
     * @param src Shader source code.
     */
    public void setVertexSrc(String vsSrc) {
    	GLES20.glShaderSource(mVertShaderId, vsSrc);
        compile(mVertShaderId);
        GLES20.glAttachShader(mProgId, mVertShaderId);
    }

    /**
     * Set the vertex shader source as a string.
     *
     * @param src Shader source code.
     */
    public void setFragmentSrc(String fsSrc) {
        GLES20.glShaderSource(mFragShaderId, fsSrc);
        compile(mFragShaderId);
        GLES20.glAttachShader(mProgId, mFragShaderId);

        
    }
    
    /**
     * Build the vertex shader from source code stored in an asset.
     *
     * @param assets AssetManager that the asset is retrieved from.
     * @param name Name of asset containing the shader source.
     */
    public void setVertexAsset(AssetManager assets, String name) {
        String vsSrc = readAsset(assets, name);
        setVertexSrc(vsSrc);
    }

    /**
     * Build the fragment shader from source code stored in an asset.
     *
     * @param assets AssetManager that the asset is retrieved from.
     * @param name Name of asset containing the shader source.
     */
    public void setFragmentAsset(AssetManager assets, String name) {
        String fsSrc = readAsset(assets, name);
        setFragmentSrc(fsSrc);
    }

    /**
     * Link the program. Logs error if link operation fails.
     */
    public void link() {
        GLES20.glLinkProgram(mProgId);

        int[] statusA = new int[1];
        GLES20.glGetProgramiv(mProgId, GLES20.GL_LINK_STATUS, statusA, 0);
        if (statusA[0] == GLES20.GL_FALSE) {
            String log = GLES20.glGetProgramInfoLog(mProgId);
            Log.e("Program", "link error: " + log);
        }
        GLES20.glShaderSource(mVertShaderId, "");
        GLES20.glShaderSource(mFragShaderId, "");
    }

    /**
     * Make the program the current program for the OpenGL pipeline.
     */
    public void use() {
        GLES20.glUseProgram(mProgId);
    }

    /**
     * Get the program id. Can be used to set attribute locations,
     * get uniform locations, etc.
     *
     * @return Program id.
     */
    public int getId() {
        return mProgId;
    }

    /**
     * Helper function for reading the content of an asset and
     * return it as a string.
     *
     * @param assets AssetManager that the asset is retrieved from.
     * @param name Name of asset containing the shader source.
     * @return Asset content as string.
     */
    private String readAsset(AssetManager assets, String name) {
        String val = "";

        try {
            InputStream strm = assets.open(name);

            for (;;) {
                int nBytes = strm.available();
                if (nBytes == 0) {
                    break;
                }

                byte[] buf = new byte[nBytes];
                strm.read(buf);

                val = val + new String(buf);
            }

            strm.close();
        } catch (IOException e) {
            Log.e("Program", "asset read error: " + name);
        }

        return val;
    }

    /**
     * Helper function for compiling a shader, and logging an error on
     * failure.
     *
     * @param shaderId Shader id.
     */
    private void compile(int shaderId) {
        GLES20.glCompileShader(shaderId);

        int[] statusA = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, statusA, 0);
        if (statusA[0] == GLES20.GL_FALSE) {
            String log = GLES20.glGetShaderInfoLog(shaderId);
            Log.e("Program", "compile error: " + log);
        }
    }

    /** Program id. */
    private int mProgId;
    /** Ids of vertex and fragment shaders. */
    private int mVertShaderId, mFragShaderId;

}
