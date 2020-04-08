package ru.skillbranch.skillarticles.data.delegates
import kotlinx.android.synthetic.main.item_article.*

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import ru.skillbranch.skillarticles.data.local.PrefManager

class PrefDelegate<T>(
        private val defaultValue: T
) {
    private var storedValue: T? = null

    operator fun provideDelegate(
            thisRef: PrefManager,
            prop: KProperty<*>
    ): ReadWriteProperty<PrefManager, T?> {
        val key = prop.name
        return object : ReadWriteProperty<PrefManager, T?> {

            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (storedValue == null) {
                    @Suppress("UNCHECKED_CAST")
                    storedValue = when (defaultValue) {
                        is Boolean -> thisRef.preferences.getBoolean(key, defaultValue) as T?
                        is String -> thisRef.preferences.getString(key, defaultValue) as T?
                        is Float -> thisRef.preferences.getFloat(key, defaultValue) as T?
                        is Int -> thisRef.preferences.getInt(key, defaultValue) as T?
                        is Long -> thisRef.preferences.getLong(key, defaultValue) as T?
                        else -> error("PrefDelegate supports only Boolean, String, Float, Int, Long")
                    }
                }
                return storedValue
            }

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                with(thisRef.preferences.edit()) {
                    when (value) {
                        is Boolean -> putBoolean(key, value)
                        is String -> putString(key, value)
                        is Float -> putFloat(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        else ->error("PrefDelegate supports only Boolean, String, Float, Int, Long")
                    }
                    apply()
                }
                storedValue = value
            }
        }
    }
}

