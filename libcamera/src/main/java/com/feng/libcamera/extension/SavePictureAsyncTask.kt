package com.feng.libcamera.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable
import com.feng.libcamera.utils.Util
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class SavePictureAsyncTask() : AsyncTask<Object, Void, File>(), Parcelable {

    var data: ByteArray? = null
    var output: File? = null
    var face: Int = 0

    constructor(parcel: Parcel) : this() {
        data = parcel.createByteArray()
    }

    constructor(data: ByteArray?, face: Int, path: String) : this() {
        this.data = data
        this.face = face
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs();
        }
        val name = "test" + System.currentTimeMillis() + ".JPG";
        output = File(dir, name)
    }

    override fun doInBackground(vararg params: Object?): File? {
        var bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        if (bitmap == null) {
            return null
        }

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(output);
            //保存之前先调整方向
            if (face == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bitmap = Util.rotate(bitmap, 90F);
            } else {
                bitmap = Util.rotate(bitmap, 270F);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);


        } catch (ex: FileNotFoundException) {
            ex.printStackTrace();
        } finally {
            fos?.close()
        }
        return output;
    }

    override fun onPostExecute(result: File?) {
        super.onPostExecute(result)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavePictureAsyncTask> {
        override fun createFromParcel(parcel: Parcel): SavePictureAsyncTask {
            return SavePictureAsyncTask(parcel)
        }

        override fun newArray(size: Int): Array<SavePictureAsyncTask?> {
            return arrayOfNulls(size)
        }
    }

}