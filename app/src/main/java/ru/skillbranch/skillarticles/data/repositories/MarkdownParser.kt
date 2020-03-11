package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP = "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))"
    private const val RULE_GROUP = "(^-{3}|_{3}|\\*{3}$)"
    private const val INLINE_GROUP = "((?<!`)`{1}[^\\s`][^\n`]*?`{1}(?!`))"
    private const val LINK_GROUP = "(\\[.+?]\\(.+?\\))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d+\\. .+$)"
    private const val BLOCK_CODE_GROUP = "(^`{3}[^`]*`{3}$)"

    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP" +
            "|$ORDERED_LIST_ITEM_GROUP|$BLOCK_CODE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    fun clear(string: String?):String? {
        string ?: return ""
        val elements = findElements(string)
        return buildString {
            elements.forEach {
                clearElement(it, this)
            }
        }
    }

    private fun clearElement(element: Element, builder: StringBuilder): CharSequence {
        return builder.apply {
            if(element.elements.isEmpty()) append(element.text)
            else element.elements.forEach {
                clearElement(it, this)
            }
        }
    }

    private fun findElements(string: CharSequence):List<Element>{
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@while(matcher.find(lastStartIndex)){
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if(lastStartIndex < startIndex){
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            var text: CharSequence

            val groups = 1..11
            var group = -1
            for(gr in groups){
                if(matcher.group(gr) != null){
                    group = gr
                    break
                }
            }

            when(group){
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                //HEADER
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)

                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)

                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)

                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)

                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())

                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    val reg = "\\[(.+)]\\((.+)\\)".toRegex().find(string.subSequence(startIndex, endIndex))
                    val (title, link) = reg!!.groupValues.drop(1)
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ORDERED LIST ITEM
                10 -> {
                    val reg = "(\\d+\\.) (.+)".toRegex().find(string.subSequence(startIndex,endIndex))
                    val (order, title) = reg!!.groupValues.drop(1)
                    val subs = findElements(title)
                    val element = Element.OrderedListItem(order, title, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                //CODE BLOCK
                11 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))

                    if(text.contains(LINE_SEPARATOR)){
                        for((index, line) in text.lines().withIndex()){
                            when(index){
                                text.lines().lastIndex -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.END, line)
                                )
                                0 -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.START, line + LINE_SEPARATOR)
                                )
                                else -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.MIDDLE, line + LINE_SEPARATOR)
                                )
                            }
                        }
                    }else parents.add(Element.BlockCode(Element.BlockCode.Type.SINGLE, text))

                    lastStartIndex = endIndex
                }
            }

        }

        if(lastStartIndex < string.length){
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}

private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    elements.add(this)
    elements.addAll(this.elements.spread())
    return elements
}

private fun List<Element>.spread():List<Element>{
    val elements = mutableListOf<Element>()
    if(this.isNotEmpty()) elements.addAll(
            this.fold(mutableListOf()){acc, el -> acc.also { it.addAll(el.spread()) }}
    )
    return elements
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
    ): Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Header(
            val level: Int = 1,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ): Element()

    data class Quote(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ): Element()

    data class Italic(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
            override val text: CharSequence = " ",
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
            val link: String,
            override val text: CharSequence, //for insert span
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
            val order: String,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
            val type: Type = Type.MIDDLE,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}