package com.projects.musicplayer.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*


class PlaylistConverter {

    companion object {
        val gson = Gson()

        @TypeConverter
        @JvmStatic
        fun toList(data: String?): List<Int>? { //TODO changed Int? to Int
            if (data == null) {
                return Collections.emptyList()
            }
            val listType: Type = object : TypeToken<List<Int>?>() {}.getType()
            return gson.fromJson<List<Int>>(data, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromList(someObjects: List<Int?>?): String? {
            return gson.toJson(someObjects)
        }
    }
}