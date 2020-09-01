package com.sudoajay.uninstaller.activity.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sudoajay.uninstaller.R
import com.sudoajay.uninstaller.activity.BaseActivity
import com.sudoajay.uninstaller.databinding.ActivityMainBinding
import com.sudoajay.uninstaller.firebase.NotificationChannels.notificationOnCreate
import com.sudoajay.uninstaller.helper.CustomToast
import com.sudoajay.uninstaller.helper.DarkModeBottomSheet
import com.sudoajay.uninstaller.helper.InsetDivider
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class MainActivity : BaseActivity(), SelectOptionBottomSheet.IsSelectedBottomSheetFragment {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    private var isDarkTheme: Boolean = false
    private var TAG = "MainActivityClass"
    private var doubleBackToExitPressedOnce = false
    private var pagingAppRecyclerAdapter: PagingAppRecyclerAdapter? = null



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

//        if (!intent.action.isNullOrEmpty() && intent.action.toString() == settingId) {
//            openMoreSetting()
//        }




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
        pagingAppRecyclerAdapter?.submitList(null)

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


        binding.filterFloatingActionButton.setOnClickListener {
            showFilterOption()
        }

        setRecyclerView()
    }

    private fun setRecyclerView() {


        val recyclerView = binding.recyclerView
        val divider = getInsertDivider()
        recyclerView.addItemDecoration(divider)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pagingAppRecyclerAdapter = PagingAppRecyclerAdapter(this)
        recyclerView.adapter = pagingAppRecyclerAdapter
        viewModel.appList!!.observe(this, androidx.lifecycle.Observer {

            for (x in it) {
                Log.e(TAG, x.name)
            }

            pagingAppRecyclerAdapter!!.totalSize = it.size
            pagingAppRecyclerAdapter!!.submitList(it)
            pagingAppRecyclerAdapter!!.isSdCardPresent = isSdCardPresent()

            if (binding.swipeRefresh.isRefreshing)
                binding.swipeRefresh.isRefreshing = false

            isDataEmpty(it.size)

        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onRefresh()
            isDataEmpty(pagingAppRecyclerAdapter!!.itemCount)

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

    private fun showSelectOption() {
        val selectOptionBottomSheet = SelectOptionBottomSheet()
        selectOptionBottomSheet.show(
            supportFragmentManager.beginTransaction(),
            selectOptionBottomSheet.tag
        )

    }

    private fun showFilterOption(){
        val filterPdfBottomSheet = FilterPdfBottomSheet()
        filterPdfBottomSheet.show(supportFragmentManager, filterPdfBottomSheet.tag)
    }

    private fun showNavigationDrawer(){
        val navigationDrawerBottomSheet = NavigationDrawerBottomSheet()
        navigationDrawerBottomSheet.show(supportFragmentManager, navigationDrawerBottomSheet.tag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> showNavigationDrawer()
            R.id.darkMode_optionMenu -> showDarkMode()
            R.id.refresh_optionMenu -> viewModel.onRefresh()
            R.id.filePicker_optionMenu -> openFilePicker()
            R.id.more_setting_optionMenu -> openMoreSetting()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_toolbar_menu, menu)
        val actionSearch = menu.findItem(R.id.bottomToolbar_search)
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
                binding.filterFloatingActionButton.hide()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                binding.filterFloatingActionButton.show()
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






    override fun handleDialogClose(value: String) {
        if (value == getString(R.string.select_option_text)) {
            if (SelectOptionBottomSheet.getValue(applicationContext) == getString(R.string.select_file_text)) {
                Log.e(TAG, "select_file_text Option Click")
                openFilePicker()
            } else {
                Log.e(TAG, "scan_file_text Option Click")
                androidExternalStoragePermission?.callPermission()
            }
        } else {
            viewModel.filterChanges()
        }
    }



    private fun openMoreSetting() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
    }


    @SuppressLint("Recycle")
    private fun queryName(resolver: ContentResolver, uri: Uri?): String {
        val returnCursor = resolver.query(uri!!, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }




    /**
     * Showing popup menu when tapping on 3 dots
     */
    fun showPopupMenu(view: View, path: String) {
        val popup = PopupMenu(this, view, Gravity.END)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.more_option, popup.menu)

        //set menu item click listener here
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(this, path))
        popup.show()
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
        CoroutineScope(Dispatchers.Main).launch {
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
    }


}
