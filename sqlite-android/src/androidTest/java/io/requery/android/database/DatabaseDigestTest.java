package io.requery.android.database;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.database.Cursor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import io.requery.android.database.sqlite.SQLiteDatabase;

import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class DatabaseDigestTest {
    private static final int CURRENT_DATABASE_VERSION = 42;
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;

    @Before
    public void setUp() {
        File dbDir = ApplicationProvider.getApplicationContext().getDir("tests", Context.MODE_PRIVATE);
        mDatabaseFile = new File(dbDir, "database_test.db");

        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }

        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
        mDatabase.setVersion(CURRENT_DATABASE_VERSION);
    }

    @After
    public void tearDown() {
        mDatabase.close();
        mDatabaseFile.delete();
    }

    @MediumTest
    @Test
    public void testDigest() {
        mDatabase.execSQL("CREATE TABLE tests (input TEXT, sha TEXT, md TEXT);");
        mDatabase.execSQL("INSERT INTO tests VALUES \n" +
                "\t(\"abc\", \"A9993E364706816ABA3E25717850C26C9CD0D89D\", \"900150983CD24FB0D6963F7D28E17F72\"),\n" +
                "\t(\"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq\", \"84983E441C3BD26EBAAE4AA1F95129E5E54670F1\", \"8215EF0796A20BCAAAE116D3876C664A\");");

        try {
//            mDatabase.query("select load_modules(\"digest.so\")").close();
            Cursor cur = mDatabase.query("select sha256(\"gian\")");
            cur.moveToFirst();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Cursor cursor = mDatabase.query("SELECT input FROM tests WHERE\n" +
                "hex(sha1(input)) != sha OR\n" +
                "hex(md5(input)) != md OR\n" +
                "hex(digest(\"sha1\", input)) != sha OR\n" +
                "hex(digest(\"md5\", input)) != md;");

        assertTrue(cursor.moveToFirst());
    }
}
