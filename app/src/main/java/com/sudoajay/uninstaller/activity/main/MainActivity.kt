package com.sudoajay.uninstaller.activity.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sudoajay.uninstaller.R
import com.sudoajay.uninstaller.activity.BaseActivity
import com.sudoajay.uninstaller.activity.main.database.FilterAppBottomSheet
import com.sudoajay.uninstaller.activity.main.root.RootState
import com.sudoajay.uninstaller.activity.settingActivity.SettingsActivity
import com.sudoajay.uninstaller.databinding.ActivityMainBinding
import com.sudoajay.uninstaller.firebase.NotificationChannels.notificationOnCreate
import com.sudoajay.uninstaller.helper.CustomToast
import com.sudoajay.uninstaller.helper.DarkModeBottomSheet
import com.sudoajay.uninstaller.helper.InsetDivider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : BaseActivity() , FilterAppBottomSheet.IsSelectedBottomSheetFragment {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    private var isDarkTheme: Boolean = false
    private var TAG = "MainActivityClass"
    private var doubleBackToExitPressedOnce = false



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        isDarkTheme = isDarkMode(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isDarkTheme)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        changeStatusBarColor()

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        if (!intent.action.isNullOrEmpty() && intent.action.toString() == settingId) {
            openSetting()
        }


    }


    override fun onStart() {
        Log.e(TAG, " Activity - onStart ")
        super.onStart()
    }

    override fun onResume() {

        setReference()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationOnCreate(applicationContext)
        }

        checkRootState()

        super.onResume()
    }


    override fun onPause() {
        Log.e(TAG, " Activity - onPause ")

        super.onPause()
    }


    override fun onStop() {
        Log.e(TAG, " Activity - onStop ")

        super.onStop()
    }
    override fun onRestart() {
        Log.e(TAG, " Activity - onRestart ")

        super.onRestart()
    }


    override fun onDestroy() {
        Log.e(TAG, " Activity - onDestroy ")

        super.onDestroy()
    }
    private fun setReference() {

        //      Setup Swipe RecyclerView
        binding.swipeRefresh.setColorSchemeResources(
            if (isDarkTheme) R.color.swipeSchemeDarkColor else R.color.swipeSchemeColor
        )
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                applicationContext,
                if (isDarkTheme) R.color.swipeBgDarkColor else R.color.swipeBgColor

            )
        )


//         Setup BottomAppBar Navigation Setup
        binding.bottomAppBar.navigationIcon?.mutate()?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.setTint(
                    ContextCompat.getColor(
                        applicationContext,
                        if (isDarkTheme) R.color.navigationIconDarkColor else R.color.navigationIconColor
                    )
                )
            }
            binding.bottomAppBar.navigationIcon = it
        }

        setSupportActionBar(binding.bottomAppBar)


        setRecyclerView()
    }

    private fun setRecyclerView() {


        val recyclerView = binding.recyclerView
        val divider = getInsertDivider()
        recyclerView.addItemDecoration(divider)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val pagingAppRecyclerAdapter = PagingAppRecyclerAdapter(applicationContext)
        recyclerView.adapter = pagingAppRecyclerAdapter
        viewModel.appList!!.observe(this, {
            pagingAppRecyclerAdapter.submitList(it)

            if (binding.swipeRefresh.isRefreshing)
                binding.swipeRefresh.isRefreshing = false

            viewModel.hideProgress!!.value = it.isEmpty()

        })


        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
        }

    }

    private fun getInsertDivider(): RecyclerView.ItemDecoration {
        val dividerHeight = resources.getDimensionPixelSize(R.dimen.divider_height)
        val dividerColor = ContextCompat.getColor(
            applicationContext,
            R.color.divider
        )
        val marginLeft = resources.getDimensionPixelSize(R.dimen.divider_inset)
        return InsetDivider.Builder(this)
            .orientation(InsetDivider.VERTICAL_LIST)
            .dividerHeight(dividerHeight)
            .color(dividerColor)
            .insets(marginLeft, 0)
            .build()
    }



    private fun showDarkMode() {
        val darkModeBottomSheet = DarkModeBottomSheet(homeId)
        darkModeBottomSheet.show(
            supportFragmentManager.beginTransaction(),
            darkModeBottomSheet.tag
        )
    }

    private fun showNavigationDrawer(){
        val navigationDrawerBottomSheet = NavigationDrawerBottomSheet()
        navigationDrawerBottomSheet.show(supportFragmentManager, navigationDrawerBottomSheet.tag)
    }

    private fun showFilterAppBottomSheet(){
        val filterAppBottomSheet = FilterAppBottomSheet()
        filterAppBottomSheet.show(supportFragmentManager, filterAppBottomSheet.tag)
    }

    private fun openSetting() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> showNavigationDrawer()
            R.id.filterList_optionMenu -> showFilterAppBottomSheet()
            R.id.darkMode_optionMenu -> showDarkMode()
            R.id.more_setting_optionMenu -> openSetting()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_toolbar_menu, menu)
        val actionSearch = menu.findItem(R.id.search_optionMenu)
        manageSearch(actionSearch)
        return super.onCreateOptionsMenu(menu)
    }

    private fun manageSearch(searchItem: MenuItem) {
        val searchView =
            searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        manageFabOnSearchItemStatus(searchItem)
        manageInputTextInSearchView(searchView)
    }

    private fun manageFabOnSearchItemStatus(searchItem: MenuItem) {
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                binding.deleteFloatingActionButton.hide()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                binding.deleteFloatingActionButton.show()
                return true
            }
        })
    }

    private fun manageInputTextInSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val query: String = newText.toLowerCase(Locale.ROOT).trim { it <= ' ' }
                viewModel.filterChanges(query)
                return true
            }
        })
    }

    override fun handleDialogClose() {
        viewModel.filterChanges()
    }


    private fun checkRootState(): RootState? {
        val rootState: RootState = viewModel.checkRootPermission()!!
        Log.e(TAG, rootState.name)
        when (rootState) {
            RootState.NO_ROOT -> {
                setRootAccessAlreadyObtained(false, applicationContext)
                generateRootStateAlertDialog(
                    resources.getString(R.string.alert_dialog_title_no_root_permission),
                    resources.getString(R.string.alert_dialog_message_no_root_permission)
                )
            }
            RootState.BE_ROOT -> {
                setRootAccessAlreadyObtained(false, applicationContext)
                generateRootStateAlertDialog(
                    resources.getString(R.string.alert_dialog_title_be_root),
                    resources.getString(R.string.alert_dialog_message_be_root)
                )
            }
            RootState.HAVE_ROOT -> {

                if (isRootAccessAlreadyObtained(applicationContext)) return null
                setRootAccessAlreadyObtained(true, applicationContext)
                generateRootStateAlertDialog(
                    resources.getString(R.string.alert_dialog_title_have_root),
                    resources.getString(R.string.alert_dialog_message_have_root)
                )
            }
        }
        return rootState
    }

    private fun generateRootStateAlertDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlertDialog.Builder(
                    this,
                    if (!isDarkMode(applicationContext)) android.R.style.Theme_Material_Light_Dialog_Alert else android.R.style.Theme_Material_Dialog_Alert
                )
            } else {
                AlertDialog.Builder(this)
            }
        builder.setTitle(title)
            .setMessage(message)
            .setNegativeButton("Ok") { _, _ ->

            }
            .setCancelable(true)
        val dialog = builder.create()
        dialog.show()
        val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.alert_dialog_message_size)
        )
    }


    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        if (doubleBackToExitPressedOnce) {
            closeApp()
            return
        }
        doubleBackToExitPressedOnce = true
        CustomToast.toastIt(applicationContext, "Click Back Again To Exit")
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)
            doubleBackToExitPressedOnce = false
        }
    }

    private fun closeApp() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }


    /**
     * Making notification bar transparent
     */
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isDarkTheme) {
                val window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

    companion object {
        const val settingId = "setting"
        const val homeId = "home"

        private fun setRootAccessAlreadyObtained(status: Boolean, context: Context) {
            context.getSharedPreferences("state", Context.MODE_PRIVATE).edit()
                .putBoolean(
                    context.getString(R.string.is_root_permission_text), status
                ).apply()
        }

        private fun isRootAccessAlreadyObtained(context: Context): Boolean {
            return context.getSharedPreferences("state", Context.MODE_PRIVATE)
                .getBoolean(
                    context.getString(R.string.is_root_permission_text), false
                )
        }


    }



}
