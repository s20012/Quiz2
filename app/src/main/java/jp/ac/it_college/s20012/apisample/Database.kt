package jp.ac.it_college.s20012.apisample

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "Test1.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE Test1 (
                id INTEGER PRIMARY KEY,
                question TEXT,
                answers TEXT,
                choices1 TEXT,
                choices2 TEXT,
                choices3 TEXT,
                choices4 TEXT,
                choices5 TEXT,
                choices6 TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}