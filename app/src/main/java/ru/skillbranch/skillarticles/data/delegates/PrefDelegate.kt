package ru.skillbranch.skillarticles.data.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import ru.skillbranch.skillarticles.data.local.PrefManager

class PrefDelegate<T:Any>(
        private val defaultValue: T
) : ReadWriteProperty<PrefManager, T?>
{
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        return  when(defaultValue){
            is Boolean -> thisRef.preferences.getBoolean(property.name, defaultValue) as T?
            is String -> thisRef.preferences.getString(property.name, defaultValue) as T?
            is Float -> thisRef.preferences.getFloat(property.name, defaultValue) as T?
            is Int -> thisRef.preferences.getInt(property.name, defaultValue) as T?
            is Long -> thisRef.preferences.getLong(property.name, defaultValue) as T?
            else -> null
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        val editor = thisRef.preferences.edit()
        when(value){
            is Boolean -> editor.putBoolean(property.name, value)
            is String -> editor.putString(property.name, value)
            is Float -> editor.putFloat(property.name, value)
            is Int -> editor.putInt(property.name, value)
            is Long -> editor.putLong(property.name, value)
            else -> throw IllegalArgumentException("PrefDelegate supports only Boolean, String, Float, Int, Long")
        }
        editor.apply()
    }
}