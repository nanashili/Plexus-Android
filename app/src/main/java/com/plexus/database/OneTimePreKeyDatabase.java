package com.plexus.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.Nullable;

import com.plexus.database.helpers.SQLCipherOpenHelper;
import com.plexus.protocol.InvalidKeyException;
import com.plexus.protocol.ecc.Curve;
import com.plexus.protocol.ecc.ECKeyPair;
import com.plexus.protocol.ecc.ECPrivateKey;
import com.plexus.protocol.ecc.ECPublicKey;
import com.plexus.protocol.state.PreKeyRecord;
import com.plexus.utils.Base64;
import com.plexus.utils.logging.Log;

import java.io.IOException;

public class OneTimePreKeyDatabase extends Database {

    public static final String TABLE_NAME = "one_time_prekeys";
    public static final String KEY_ID = "key_id";
    public static final String PUBLIC_KEY = "public_key";
    public static final String PRIVATE_KEY = "private_key";
    private static final String TAG = OneTimePreKeyDatabase.class.getSimpleName();
    private static final String ID = "_id";
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + ID + " INTEGER PRIMARY KEY, " +
            KEY_ID + " INTEGER UNIQUE, " +
            PUBLIC_KEY + " TEXT NOT NULL, " +
            PRIVATE_KEY + " TEXT NOT NULL);";

    OneTimePreKeyDatabase(Context context, SQLCipherOpenHelper databaseHelper) {
        super(context, databaseHelper);
    }

    public @Nullable
    PreKeyRecord getPreKey(int keyId) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        try (Cursor cursor = database.query(TABLE_NAME, null, KEY_ID + " = ?",
                new String[]{String.valueOf(keyId)},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    ECPublicKey publicKey = Curve.decodePoint(Base64.decode(cursor.getString(cursor.getColumnIndexOrThrow(PUBLIC_KEY))), 0);
                    ECPrivateKey privateKey = Curve.decodePrivatePoint(Base64.decode(cursor.getString(cursor.getColumnIndexOrThrow(PRIVATE_KEY))));

                    return new PreKeyRecord(keyId, new ECKeyPair(publicKey, privateKey));
                } catch (InvalidKeyException | IOException e) {
                    Log.w(TAG, e);
                }
            }
        }

        return null;
    }

    public void insertPreKey(int keyId, PreKeyRecord record) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, keyId);
        contentValues.put(PUBLIC_KEY, Base64.encodeBytes(record.getKeyPair().getPublicKey().serialize()));
        contentValues.put(PRIVATE_KEY, Base64.encodeBytes(record.getKeyPair().getPrivateKey().serialize()));

        database.replace(TABLE_NAME, null, contentValues);
    }

    public void removePreKey(int keyId) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{String.valueOf(keyId)});
    }

}
