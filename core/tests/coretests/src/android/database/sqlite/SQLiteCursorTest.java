/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.platform.test.annotations.Presubmit;
import android.test.AndroidTestCase;

import androidx.test.filters.LargeTest;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Presubmit
@LargeTest
public class SQLiteCursorTest extends AndroidTestCase {
    private static final String TABLE_NAME = "testCursor";
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        File dbDir = getContext().getDir(this.getClass().getName(), Context.MODE_PRIVATE);
        mDatabaseFile = new File(dbDir, "sqlitecursor_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
        // create a test table
        mDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (i int, j int);");
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }

    /**
     * this test could take a while to execute. so, designate it as LargeTest
     */
    public void testFillWindow() {
        // create schema
        final String testTable = "testV";
        mDatabase.beginTransaction();
        mDatabase.execSQL("CREATE TABLE " + testTable + " (col1 int, desc text not null);");
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        // populate the table with data
        // create a big string that will almost fit a page but not quite.
        // since sqlite wants to make sure each row is in a page, this string will allocate
        // a new database page for each row.
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            buff.append(i % 10 + "");
        }
        ContentValues values = new ContentValues();
        values.put("desc", buff.toString());

        // insert more than 1MB of data in the table. this should ensure that the entire tabledata
        // will need more than one CursorWindow
        int N = 5000;
        Set<Integer> rows = new HashSet<Integer>();
        mDatabase.beginTransaction();
        for (int j = 0; j < N; j++) {
            values.put("col1", j);
            mDatabase.insert(testTable, null, values);
            rows.add(j); // store in a hashtable so we can verify the results from cursor later on
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertEquals(N, rows.size());
        Cursor c1 = mDatabase.rawQuery("select * from " + testTable, null);
        assertEquals(N, c1.getCount());
        c1.close();

        // scroll through ALL data in the table using a cursor. should cause multiple calls to
        // native_fill_window (and re-fills of the CursorWindow object)
        Cursor c = mDatabase.query(testTable, new String[]{"col1", "desc"},
                null, null, null, null, null);
        int i = 0;
        while (c.moveToNext()) {
            int val = c.getInt(0);
            assertTrue(rows.contains(val));
            assertTrue(rows.remove(val));
        }
        // did I see all the rows in the table?
        assertTrue(rows.isEmpty());

        // change data and make sure the cursor picks up new data & count
        rows = new HashSet<Integer>();
        mDatabase.beginTransaction();
        int M = N + 1000;
        for (int j = 0; j < M; j++) {
            rows.add(j);
            if (j < N) {
                continue;
            }
            values.put("col1", j);
            mDatabase.insert(testTable, null, values);
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        assertEquals(M, rows.size());
        c.requery();
        i = 0;
        while (c.moveToNext()) {
            int val = c.getInt(0);
            assertTrue(rows.contains(val));
            assertTrue(rows.remove(val));
        }
        // did I see all data from the modified table
        assertTrue(rows.isEmpty());

        // move cursor back to 1st row and scroll to about halfway in the result set
        // and then delete 75% of data - and then do requery
        c.moveToFirst();
        int K = N / 2;
        for (int p = 0; p < K && c.moveToNext(); p++) {
            // nothing to do - just scrolling to about half-point in the resultset
        }
        mDatabase.beginTransaction();
        mDatabase.delete(testTable, "col1 < ?", new String[]{(3 * M / 4) + ""});
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        c.requery();
        assertEquals(M / 4, c.getCount());
        while (c.moveToNext()) {
            // just move the cursor to next row - to make sure it can go through the entire
            // resultset without any problems
        }
        c.close();
    }

    public void testCustomWindowSize() {
        mDatabase.execSQL("CREATE TABLE Tst (Txt BLOB NOT NULL);");
        byte[] testArr = new byte[10000];
        Arrays.fill(testArr, (byte) 1);
        for (int i = 0; i < 10; i++) {
            mDatabase.execSQL("INSERT INTO Tst VALUES (?)", new Object[]{testArr});
        }
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM TST", null);
        // With default window size, all rows should fit in RAM
        AbstractWindowedCursor ac = (AbstractWindowedCursor) cursor;
        int n = 0;
        while (ac.moveToNext()) {
            n++;
            assertEquals(10, ac.getWindow().getNumRows());
        }
        assertEquals("All rows should be visited", 10, n);

        // Now reduce window size, so that only 1 row can fit
        cursor = mDatabase.rawQuery("SELECT * FROM TST", null);
        CursorWindow cw = new CursorWindow("test", 11000);
        ac = (AbstractWindowedCursor) cursor;
        ac.setWindow(cw);
        ac.move(-10);
        n = 0;
        while (ac.moveToNext()) {
            n++;
            assertEquals(1, cw.getNumRows());
        }
        assertEquals("All rows should be visited", 10, n);
    }

    // Return the number of columns associated with a new cursor.
    private int columnCount() {
        final String query = "SELECT * FROM t1";
        try (Cursor c = mDatabase.rawQuery(query, null)) {
            return c.getColumnCount();
        }
    }

    /**
     * Verify that a cursor that is created after the database schema is updated, sees the updated
     * schema.
     */
    public void testSchemaChangeCursor() {
        // Create the t1 table and put some data in it.
        mDatabase.beginTransaction();
        try {
            mDatabase.execSQL("CREATE TABLE t1 (i int);");
            mDatabase.execSQL("INSERT INTO t1 (i) VALUES (2)");
            mDatabase.execSQL("INSERT INTO t1 (i) VALUES (3)");
            mDatabase.execSQL("INSERT INTO t1 (i) VALUES (5)");
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }

        mDatabase.beginTransaction();
        try {
            assertEquals(1, columnCount());
            mDatabase.execSQL("ALTER TABLE t1 ADD COLUMN j int");
            assertEquals(2, columnCount());
        } finally {
            mDatabase.endTransaction();
        }
    }
}
