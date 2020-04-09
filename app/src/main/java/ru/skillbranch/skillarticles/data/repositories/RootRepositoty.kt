package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.LocalDataHolder

object RootRepositoty {

    fun isAuth(): LiveData<Boolean> = LocalDataHolder.isAuth()
    fun setAuth(auth:Boolean) = LocalDataHolder.setAuth(auth)
}