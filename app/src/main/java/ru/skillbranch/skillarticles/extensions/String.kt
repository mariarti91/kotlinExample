package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(query: String, ignoreCase: Boolean = true): List<Int>{
    this?:return emptyList()
    if(query.isEmpty()) return emptyList()
    var last = -1
    val result = mutableListOf<Int>()
    while(true) {
        last = this.indexOf(query, last + 1, ignoreCase)
        if(last == -1) return result.toList()
        result.add(last)
    }
}