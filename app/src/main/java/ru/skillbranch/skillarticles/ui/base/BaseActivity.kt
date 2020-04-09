package ru.skillbranch.skillarticles.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toolbar
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import kotlinx.android.synthetic.main.activity_root.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify

abstract class BaseActivity<T: BaseViewModel<out IViewModelState>> : AppCompatActivity(){
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected abstract val viewModel : T
    protected abstract val layout: Int
    lateinit var navController: NavController

    val toolbarBuilder = ToolbarBuilder()

    abstract fun subscribeOnState(state: IViewModelState)
    abstract fun renderNotification(notify: Notify)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        setSupportActionBar(toolbar)
        viewModel.observeState(this){ subscribeOnState(it) }
        viewModel.observeNotifications(this){ renderNotification(it) }

        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.restoreState()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

class ToolbarBuilder(){
    var title: String? = null
    var subtitle: String? = null
    var logo: String? = null
    var visibility: Boolean = true
    val items: MutableList<MenuItemHolder> = mutableListOf()

    fun setTitle(title:String): ToolbarBuilder{
        this.title = title
        return this
    }

    fun setSubtitle(subtitle:String): ToolbarBuilder{
        this.subtitle = subtitle
        return this
    }

    fun setLogo(logo:String): ToolbarBuilder{
        this.logo = logo
        return this
    }

    fun setVisibility(isVisible:Boolean):ToolbarBuilder{
        visibility = isVisible
        return this
    }

    fun addMenuItem(item:MenuItemHolder): ToolbarBuilder{
        this.items.add(item)
        return this
    }

    fun invalidate():ToolbarBuilder{
        title = null
        subtitle = null
        logo = null
        visibility = true
        items.clear()
        return this
    }

    fun prepare(prepareFn: (ToolbarBuilder.()-> Unit)?): ToolbarBuilder{
        invalidate()
        prepareFn?.invoke(this)
        return this
    }

    fun build(context: FragmentActivity){
        with(context.toolbar){
            if(this@ToolbarBuilder.title != null) title = this@ToolbarBuilder.title
            subtitle = this@ToolbarBuilder.subtitle
            if(this@ToolbarBuilder.logo != null){
                val logoSize = context.dpToIntPx(40)
                val logoMargin = context.dpToIntPx(16)
                val logoPlaceholder = getDrawable(context, R.drawable.logo_placeholder)

                logo = logoPlaceholder

                val logo = children.last() as? ImageView
                if(logo != null) {
                    logo.scaleType = ImageView.ScaleType.CENTER_CROP
                    (logo.layoutParams as? Toolbar.LayoutParams)?.let{
                        it.width = logoSize
                        it.height = logoSize
                        it.marginEnd = logoMargin
                        logo.layoutParams = it
                    }

                    Glide.with(context)
                            .load(this@ToolbarBuilder.logo)
                            .apply(circleCropTransform())
                            .override(logoSize)
                            .into(logo)
                }
            }else{
                logo = null
            }
        }
    }
}

data class MenuItemHolder(
        val title:String,
        val menuId:Int,
        val icon:Int,
        val actionViewLayout:Int,
        val clickListener:((MenuItem)->Unit)? = null
)