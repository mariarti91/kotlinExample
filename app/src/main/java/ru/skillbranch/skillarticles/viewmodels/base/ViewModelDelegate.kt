package ru.skillbranch.skillarticles.viewmodels.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewModelDelegate<T: ViewModel>(
        private val clazz: Class<T>,
        private val arg: Any?
) : ReadOnlyProperty<FragmentActivity, T>{

    private val vmFactory by lazy {
        ViewModelFactory(arg?:"")
    }

    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        return ViewModelProviders.of(thisRef, vmFactory).get(clazz)
    }
}