package com.thepegeekapps.easypd.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageStorage {
	
	public static final String FILENAME_DEFAULT = "image";
	
	protected static ImageStorage instance;
	protected Context context;
	protected String filename;
	
	protected ImageStorage(Context context) {
		this(context, FILENAME_DEFAULT);
	}
	
	protected ImageStorage(Context context, String filename) {
		setContext(context);
		setFilename(filename);
	}
	
	public static ImageStorage getInstance(Context context) {
		if (instance == null) {
			instance = new ImageStorage(context);
		}
		return instance;
	}
    
    public Context getContext() {
    	return context;
    }
    
    public void setContext(Context context) {
    	this.context = context;
    }
    
    public String getFilename() {
    	return filename;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public boolean saveBitmap(Bitmap bitmap, String filename) {
		if (bitmap == null) {
			if (context != null) {
				return context.deleteFile(filename);
			}
    	} else {
	    	FileOutputStream fos = null;
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try {
	        	bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
	            fos = context.openFileOutput(filename, 0);
	            fos.write(baos.toByteArray());
	            fos.getFD().sync();
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	            	if (baos != null)
	            		baos.close();
	            	if (fos != null)
	            		fos.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
    	}
		return false;
	}
    
    public Bitmap loadBitmap(String filename) {
		FileInputStream fis = null;
        try {
        	fis = context.openFileInput(filename);
        	return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                	fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public File loadImageFile(String filename) {
    	File file = null;
    	try {
    		file = context.getFileStreamPath(filename);
    		return file;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

}
