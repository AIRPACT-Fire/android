// Copyright © 2017,
// Laboratory for Atmospheric Research at Washington State University,
// All rights reserved.

package edu.wsu.lar.airpact_fire.data.realm.interface_object;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import edu.wsu.lar.airpact_fire.data.manager.DataManager;
import edu.wsu.lar.airpact_fire.data.interface_object.ImageInterfaceObject;
import edu.wsu.lar.airpact_fire.data.interface_object.TargetInterfaceObject;
import edu.wsu.lar.airpact_fire.data.realm.model.Coordinate;
import edu.wsu.lar.airpact_fire.data.realm.model.Image;
import edu.wsu.lar.airpact_fire.data.realm.model.Target;
import edu.wsu.lar.airpact_fire.debug.manager.DebugManager;
import edu.wsu.lar.airpact_fire.util.Util;

/**
 * Realm implementation of the {@link ImageInterfaceObject}.
 */
public class RealmImageInterfaceObject implements ImageInterfaceObject {

    private Realm mRealm;
    private Image mImage;
    private DataManager mDataManager;
    private DebugManager mDebugManager;

    public RealmImageInterfaceObject(Realm realm, Image image, DataManager dataManager,
                                     DebugManager debugManager) {
        mRealm = realm;
        mImage = image;
        mDataManager = dataManager;
        mDebugManager = debugManager;
    }

    @Override
    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(mImage.rawImageLocation);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public Bitmap getThumbnail() {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(mImage.compressedImageLocation);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public File createImageFile() {
        return null;
    }

    @Override
    public void wipeRawImage() {
        Util.deleteLocalFile(mImage.rawImageLocation);
    }

    @Override
    public File createImageFile(File storageDir) {

        File rawImage = Util.createPublicImageFile(storageDir, mDebugManager);
        File compressedImage = Util.createPublicImageFile(storageDir, mDebugManager);

        // Save image location
        mRealm.beginTransaction();
        mImage.rawImageLocation = rawImage.getAbsolutePath();
        mImage.compressedImageLocation = compressedImage.getAbsolutePath();
        mRealm.commitTransaction();

        return rawImage;
    }

    @Override
    public File getImageFile() {
        return new File(mImage.rawImageLocation);
    }

    @Override
    public void setImage(Bitmap raw, Bitmap compressed) {

        // TODO: Move the compression to the {@link ImageManager}.

        File rawFile = new File(mImage.rawImageLocation);
        File compressedFile = new File(mImage.compressedImageLocation);

        ByteArrayOutputStream rawBos = new ByteArrayOutputStream();
        ByteArrayOutputStream compressedBos = new ByteArrayOutputStream();

        // Compress one version, preserve on the other
        raw.compress(Bitmap.CompressFormat.PNG, 100, rawBos);
        compressed.compress(Bitmap.CompressFormat.JPEG, 50, compressedBos);

        byte[] rawBitmapData = rawBos.toByteArray();
        byte[] compressedBitmapData = compressedBos.toByteArray();
        try {
            FileOutputStream rawFos = new FileOutputStream(rawFile);
            FileOutputStream compressedFos = new FileOutputStream(compressedFile);
            rawFos.write(rawBitmapData);
            compressedFos.write(compressedBitmapData);

            //
            rawFile = new File(mImage.rawImageLocation);
            compressedFile = new File(mImage.compressedImageLocation);
            Log.d("FileLengths", String.valueOf(rawFile.length()));
            Log.d("FileLengths", String.valueOf(compressedFile.length()));
            //

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setGps(double[] values) {
        mRealm.beginTransaction();
        Coordinate coordinate = mRealm.createObject(Coordinate.class);
        coordinate.x = values[0];
        coordinate.y = values[1];
        mImage.gpsCoordinate = coordinate;
        mRealm.commitTransaction();
    }

    @Override
    public double[] getGps() {
        Coordinate coordinate = mImage.gpsCoordinate;
        return new double[] { coordinate.x, coordinate.y };
    }

    @Override
    public TargetInterfaceObject createTargetObject() {
        mRealm.beginTransaction();
        Target targetModel = mRealm.createObject(Target.class, mDataManager.generateTargetId());
        targetModel.postId = mImage.postId;
        targetModel.imageId = mImage.imageId;
        mRealm.commitTransaction();
        return new RealmTargetInterfaceObject(mRealm, targetModel, mDataManager, mDebugManager);
    }

    @Override
    public List<TargetInterfaceObject> getTargetObjects() {
        RealmResults<Target> targetResults = mRealm.where(Target.class)
                .equalTo("imageId", mImage.imageId).findAllSorted("targetId");
        List targetList = new ArrayList<TargetInterfaceObject>();
        for (Target target : targetResults) {
            targetList.add(new RealmTargetInterfaceObject(mRealm, target, mDataManager, mDebugManager));
        }
        return targetList;
    }

    @Override
    public void delete() {

        if (!mRealm.isInTransaction()) mRealm.beginTransaction();

        // Delete associated targets
        RealmResults<Target> targetResults = mRealm.where(Target.class)
                .equalTo("imageId", mImage.imageId).findAll();
        for (Target target : targetResults) {
            (new RealmTargetInterfaceObject(mRealm, target, mDataManager, mDebugManager)).delete();
        }

        // Delete image
        RealmResults<Image> imageResults = mRealm.where(Image.class)
                .equalTo("imageId", mImage.imageId).findAll();
        imageResults.deleteAllFromRealm();

        if (!mRealm.isInTransaction()) mRealm.commitTransaction();
    }

    @Override
    public Object getRaw() {
        return mImage;
    }
}
