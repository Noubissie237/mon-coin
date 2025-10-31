package com.propentatech.moncoin.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.propentatech.moncoin.data.model.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val gson = Gson()
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    
    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    // List<Int> converters
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    // TaskType converters
    @TypeConverter
    fun fromTaskType(value: TaskType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTaskType(value: String): TaskType {
        return TaskType.valueOf(value)
    }
    
    // TaskMode converters
    @TypeConverter
    fun fromTaskMode(value: TaskMode): String {
        return value.name
    }
    
    @TypeConverter
    fun toTaskMode(value: String): TaskMode {
        return TaskMode.valueOf(value)
    }
    
    // TaskState converters
    @TypeConverter
    fun fromTaskState(value: TaskState): String {
        return value.name
    }
    
    @TypeConverter
    fun toTaskState(value: String): TaskState {
        return TaskState.valueOf(value)
    }
    
    // SleepConflictPolicy converters
    @TypeConverter
    fun fromSleepConflictPolicy(value: SleepConflictPolicy): String {
        return value.name
    }
    
    @TypeConverter
    fun toSleepConflictPolicy(value: String): SleepConflictPolicy {
        return SleepConflictPolicy.valueOf(value)
    }
    
    // Recurrence converters
    @TypeConverter
    fun fromRecurrence(value: Recurrence?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toRecurrence(value: String?): Recurrence? {
        return value?.let { 
            val type = object : TypeToken<Recurrence>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // List<DayOfWeek> converters (for Recurrence)
    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String {
        return gson.toJson(value?.map { it.name } ?: emptyList<String>())
    }
    
    @TypeConverter
    fun toDayOfWeekList(value: String): List<DayOfWeek> {
        val type = object : TypeToken<List<String>>() {}.type
        val names: List<String> = gson.fromJson(value, type) ?: emptyList()
        return names.map { DayOfWeek.valueOf(it) }
    }
}
