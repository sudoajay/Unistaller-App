package com.sudoajay.uninstaller.activity.main.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.sudoajay.uninstaller.R


class AppRepository(private val context: Context, private val appDao: AppDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    lateinit var app: DataSource.Factory<Int, App>
    lateinit var id: List<Int>
    private val webBrowserPackageNames: MutableList<String> =
        ArrayList()

    fun handleFilterChanges(filter: String): LiveData<PagedList<App>> {
        if (filter == context.getString(R.string.filter_changes_text)) {
            //         Sorting Data in Alpha or Install date
            val getOrderBy =
                context.getSharedPreferences("state", Context.MODE_PRIVATE).getString(
                    context.getString(R.string.title_menu_order_by),
                    context.getString(R.string.menu_alphabetical_order)
                )

//         Is System App Show
            val isSystemApp = if (context.getSharedPreferences("state", Context.MODE_PRIVATE)
                    .getBoolean(context.getString(R.string.menu_system_app), true)
            ) 1 else 2
//         Is User App Show

            val isUserApp = if (context.getSharedPreferences("state", Context.MODE_PRIVATE)
                    .getBoolean(context.getString(R.string.menu_user_app), true)
            ) 1 else 2


            app = when {
                getOrderBy!! == context.getString(R.string.menu_alphabetical_order) -> {
                    appDao.getSortByAlpha(isSystemApp, isUserApp)
                }
                getOrderBy == context.getString(R.string.menu_installation_date) -> {
                    appDao.getSortByDate(isSystemApp, isUserApp)
                }
                else -> {
                    appDao.getSortBySize(isSystemApp, isUserApp)
                }
            }
            return app.toLiveData(
                PagedList.Config.Builder()
                    .setPageSize(20) //
                    .setInitialLoadSizeHint(20) //
                    .setEnablePlaceholders(false) //
                    .build()
            )
        } else {

            val value = "%$filter%"


            return appDao.searchItem(value).toLiveData(
                PagedList.Config.Builder()
                    .setPageSize(20) //
                    .setInitialLoadSizeHint(20) //
                    .setEnablePlaceholders(false) //
                    .build()
            )

        }
    }


    suspend fun insert(app: App) {
        appDao.insert(app)
    }

    suspend fun getAppFromId(id:Int):App{
        return appDao.getAppFromId(id)
    }

    suspend fun isPresent(packageName: String): Int {
        return appDao.isPresent(packageName)
    }
    suspend fun setUpdateInstall(packageName: String) {
        appDao.updateInstalledByPackage(packageName)
    }

    suspend fun setDefaultValueInstall() {
        appDao.setDefaultValueInstall()
    }

    suspend fun removeUninstallAppFromDB() {
        for (i in appDao.getUninstallList()) {
            appDao.deleteRow(i)
        }
    }

    suspend fun setSelectedToDefault() {
        for (pack in getSelectedApp()) {
            updateSelectedApp(false, pack.packageName)
        }
    }

    suspend fun getSelectedApp():MutableList<App>{
        return appDao.getSelectedApp()
    }

    suspend fun updateSelectedApp(selected: Boolean, packageName: String) {
        appDao.updateSelectedApp(selected, packageName)
    }

    suspend fun getPackageFromSelected(selected: Boolean): MutableList<String> {
        return appDao.getPackageFromSelected(selected)
    }

    suspend fun listRefresh(packageName: String) {
        appDao.listRefresh(packageName)
    }

    suspend fun getCount(): Int {
        return appDao.getCount()
    }
}