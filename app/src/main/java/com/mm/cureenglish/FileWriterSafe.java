package com.mm.cureenglish;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dingyu on 17-12-25.
 */

public class FileWriterSafe {
    private FileOutputStream mFileOutputStream;
    private String mFileName;

    public FileWriterSafe() {
        mFileName = null;
    }

    public void write(byte[] data, int len) {
        try {
            if (mFileOutputStream != null) {
                mFileOutputStream.write(data, 0, len);
                mFileOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        mFileName = null;
        try {
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
                mFileOutputStream = null;
                LogUtil.d("close file output stream.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open(String filePath) {
        File f = new File(filePath);
        open(f);
    }

    public void open(File f) {
        try {
            mFileName = f.getName();
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            mFileOutputStream = new FileOutputStream(f);
            LogUtil.d("create  file output stream.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return mFileName;
    }
}
