package com.example.assignment1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MessageDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "messages.db"
        private const val DATABASE_VERSION = 1
        
        // Messages table
        private const val TABLE_MESSAGES = "messages"
        private const val COL_ID = "id"
        private const val COL_SENDER_ID = "sender_id"
        private const val COL_RECEIVER_ID = "receiver_id"
        private const val COL_MESSAGE_TEXT = "message_text"
        private const val COL_MEDIA_TYPE = "media_type"
        private const val COL_MEDIA_URL = "media_url"
        private const val COL_IS_SEEN = "is_seen"
        private const val COL_IS_VANISH_MODE = "is_vanish_mode"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_UPDATED_AT = "updated_at"
        private const val COL_SENDER_USERNAME = "sender_username"
        private const val COL_SENDER_FIRST_NAME = "sender_first_name"
        private const val COL_SENDER_LAST_NAME = "sender_last_name"
        private const val COL_SENDER_PROFILE_PHOTO_URL = "sender_profile_photo_url"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_MESSAGES (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_SENDER_ID INTEGER NOT NULL,
                $COL_RECEIVER_ID INTEGER NOT NULL,
                $COL_MESSAGE_TEXT TEXT,
                $COL_MEDIA_TYPE TEXT DEFAULT 'text',
                $COL_MEDIA_URL TEXT,
                $COL_IS_SEEN INTEGER DEFAULT 0,
                $COL_IS_VANISH_MODE INTEGER DEFAULT 0,
                $COL_CREATED_AT TEXT NOT NULL,
                $COL_UPDATED_AT TEXT,
                $COL_SENDER_USERNAME TEXT,
                $COL_SENDER_FIRST_NAME TEXT,
                $COL_SENDER_LAST_NAME TEXT,
                $COL_SENDER_PROFILE_PHOTO_URL TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
        Log.d("MessageDatabaseHelper", "Database created")
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        onCreate(db)
    }
    
    // Insert or update message
    fun insertOrUpdateMessage(message: ChatMessage): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, message.id)
            put(COL_SENDER_ID, message.sender_id)
            put(COL_RECEIVER_ID, message.receiver_id)
            put(COL_MESSAGE_TEXT, message.message_text)
            put(COL_MEDIA_TYPE, message.media_type)
            put(COL_MEDIA_URL, message.media_url)
            put(COL_IS_SEEN, if (message.is_seen) 1 else 0)
            put(COL_IS_VANISH_MODE, if (message.is_vanish_mode) 1 else 0)
            put(COL_CREATED_AT, message.created_at)
            put(COL_UPDATED_AT, message.updated_at)
            put(COL_SENDER_USERNAME, message.sender_username)
            put(COL_SENDER_FIRST_NAME, message.sender_first_name)
            put(COL_SENDER_LAST_NAME, message.sender_last_name)
            put(COL_SENDER_PROFILE_PHOTO_URL, message.sender_profile_photo_url)
        }
        
        val result = db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result
    }
    
    // Get messages between two users
    fun getMessages(user1Id: Int, user2Id: Int): List<ChatMessage> {
        val db = readableDatabase
        val messages = mutableListOf<ChatMessage>()
        
        val query = """
            SELECT * FROM $TABLE_MESSAGES 
            WHERE (($COL_SENDER_ID = ? AND $COL_RECEIVER_ID = ?) OR ($COL_SENDER_ID = ? AND $COL_RECEIVER_ID = ?))
            ORDER BY $COL_CREATED_AT ASC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(user1Id.toString(), user2Id.toString(), user2Id.toString(), user1Id.toString()))
        
        while (cursor.moveToNext()) {
            messages.add(
                ChatMessage(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    sender_id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SENDER_ID)),
                    receiver_id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECEIVER_ID)),
                    message_text = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE_TEXT)),
                    media_type = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)),
                    media_url = cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_URL)),
                    is_seen = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SEEN)) == 1,
                    is_vanish_mode = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_VANISH_MODE)) == 1,
                    created_at = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                    updated_at = cursor.getString(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)),
                    sender_username = cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_USERNAME)) ?: "",
                    sender_first_name = cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_FIRST_NAME)) ?: "",
                    sender_last_name = cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_LAST_NAME)) ?: "",
                    sender_profile_photo_url = cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_PROFILE_PHOTO_URL))
                )
            )
        }
        
        cursor.close()
        db.close()
        return messages
    }
    
    // Delete vanish mode messages
    fun deleteVanishMessages(user1Id: Int, user2Id: Int) {
        val db = writableDatabase
        db.delete(
            TABLE_MESSAGES,
            "$COL_IS_VANISH_MODE = 1 AND $COL_IS_SEEN = 1 AND (($COL_SENDER_ID = ? AND $COL_RECEIVER_ID = ?) OR ($COL_SENDER_ID = ? AND $COL_RECEIVER_ID = ?))",
            arrayOf(user1Id.toString(), user2Id.toString(), user2Id.toString(), user1Id.toString())
        )
        db.close()
    }
    
    // Mark messages as seen
    fun markMessagesAsSeen(senderId: Int, receiverId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_SEEN, 1)
        }
        db.update(
            TABLE_MESSAGES,
            values,
            "$COL_SENDER_ID = ? AND $COL_RECEIVER_ID = ? AND $COL_IS_SEEN = 0",
            arrayOf(senderId.toString(), receiverId.toString())
        )
        db.close()
    }
    
    // Delete message
    fun deleteMessage(messageId: Int) {
        val db = writableDatabase
        db.delete(TABLE_MESSAGES, "$COL_ID = ?", arrayOf(messageId.toString()))
        db.close()
    }
    
    // Clear all messages (for testing or logout)
    fun clearAllMessages() {
        val db = writableDatabase
        db.delete(TABLE_MESSAGES, null, null)
        db.close()
    }
}

