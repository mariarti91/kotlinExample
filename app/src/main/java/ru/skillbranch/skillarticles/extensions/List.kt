package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<MutableList<Pair<Int,Int>>>{
    //val mlist = mutableListOf(10 to 20, 30 to 40)
    return bounds.map{ mutableListOf(it) }
}