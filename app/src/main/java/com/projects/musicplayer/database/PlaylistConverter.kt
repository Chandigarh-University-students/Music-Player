package com.projects.musicplayer.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*


class PlaylistConverter {
    val gson = Gson()

    @TypeConverter
    fun toList(data: String?): List<Int?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Int?>?>() {}.getType()
        return gson.fromJson<List<Int?>>(data, listType)
    }

    @TypeConverter
    fun toString(someObjects: List<Int?>?): String? {
        return gson.toJson(someObjects)
    }
}