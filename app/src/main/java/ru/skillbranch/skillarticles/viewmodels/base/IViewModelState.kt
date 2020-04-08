package ru.skillbranch.skillarticles.viewmodels.base

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

interface IViewModelState {
    fun save(outState: SavedStateHandle){
        //empty implementation
    }
    fun restore(savedState: SavedStateHandle): IViewModelState{
        //empty implementation
        return this
    }
}