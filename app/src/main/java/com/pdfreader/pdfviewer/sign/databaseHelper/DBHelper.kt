package com.example.pdfreader.databaseHelper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pdfreader.modalClas.SignList

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SIGNATURE_TABLE"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "sign_table"
        const val ID_COL = "id"
        const val SIGN_COl = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                SIGN_COl + " TEXT" + ")")

        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addSign(sign: String) {
        val values = ContentValues()
        values.put(SIGN_COl, sign)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getSign(): ArrayList<SignList> {
        val sql = "select * from $TABLE_NAME"
        val db = this.readableDatabase
        val storeSign =
            ArrayList<SignList>()
        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(0).toInt()
                val image = cursor.getString(1)
                storeSign.add(SignList(image))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return storeSign
    }

    fun deleteSign(signImage: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "name=?", arrayOf(signImage))
        db.close()
    }
}