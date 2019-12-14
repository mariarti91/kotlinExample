package ru.skillbranch.kotlinexample.extensions

fun <E> List<E>.dropLastUntil(expr: (E) -> Boolean) : List<E> {
    val index = this.indexOfLast(expr)
    if(index <= 0) return listOf()
    return this.subList(0, index)
}
