package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<MutableList<Pair<Int,Int>>>{
    val result = List(bounds.size) {
        mutableListOf<Pair<Int,Int>>()
    }

    var currentIndex = 0;
    forEach { (start, end) ->
        while (start > bounds[currentIndex].second){
            ++currentIndex
        }
        if(end <= bounds[currentIndex].second) {
            result[currentIndex].add(start to end)
        }else{
            result[currentIndex].add(start to bounds[currentIndex].second)
            ++currentIndex
            result[currentIndex].add(bounds[currentIndex].first to end)
        }
    }

    return result
}