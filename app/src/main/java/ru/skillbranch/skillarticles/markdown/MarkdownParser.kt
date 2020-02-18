package ru.skillbranch.skillarticles.markdown

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"

    const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String):MarkdownText{
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    fun clear(string: String):String?{
        return null
    }

    private fun findElements(string: CharSequence):List<Element>{
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@while(matcher.find(lastStartIndex)){
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if(lastStartIndex < startIndex){
                parents.add(Element.Text(string.subSequence(startIndex, endIndex)))
            }

            var text: CharSequence

            val groups = 1..1
            var group = -1
            for(gr in groups){
                if(matcher.group(gr) != null){
                    group = gr
                    break
                }
            }

            when(group){
                -1 -> break@loop

                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }
            }

        }



        return parents
    }
}

data class MarkdownText(
        val elements:List<Element>
)

sealed class Element(){
    abstract val text:CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ):Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ):Element()
}