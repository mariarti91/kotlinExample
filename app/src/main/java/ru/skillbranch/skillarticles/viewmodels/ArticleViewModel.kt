package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()) {

    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()){ article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()){ content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()){ info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()){ settings, state ->
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    private fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    private fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    private fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }


    fun handelTextUp(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    fun handelTextDown(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    fun handelNightMode(){
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    fun handleLike(){
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val msg = if(currentState.isLike) Notify.TextMessage("Mark as liked")
        else Notify.ActionMessage(
                "Don't like it anymore",
                "No, still like it",
                toggleLike
            )

        notify(msg)
    }

    fun handleBookmark(){

    }

    fun handleShare(){
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "ok", null))
    }

    fun handleToggleMenu(){
        updateState { it.copy(isSnowMenu = !it.isSnowMenu) }
    }

}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReviews: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isSnowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResult: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)