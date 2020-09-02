package com.sudoajay.uninstaller.activity.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.sudoajay.uninstaller.R
import com.sudoajay.uninstaller.activity.main.database.App
import com.sudoajay.uninstaller.activity.main.database.AppDao
import com.sudoajay.uninstaller.activity.main.database.AppRepository
import com.sudoajay.uninstaller.activity.main.database.AppRoomDatabase
import com.sudoajay.uninstaller.activity.main.root.RootManager
import com.sudoajay.uninstaller.activity.main.root.RootState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var loadApps: LoadApps
    private var _application = application
    var appRepository: AppRepository
    private val rootManager: RootManager = RootManager()
    private var appDao: AppDao =
        AppRoomDatabase.getDatabase(_application.applicationContext).appDao()


    var hideProgress: MutableLiveData<Boolean>? = null

    private val filterChanges: MutableLiveData<String> = MutableLiveData()


    var appList: LiveData<PagedList<App>>? = null

    init {

//        Creating Object and Initialization
        appRepository = AppRepository(_application.applicationContext, appDao)
        loadApps = LoadApps(_application.applicationContext, appRepository)

//        setDefaultValue()
        getHideProgress()
        appList = Transformations.switchMap(filterChanges) {
            appRepository.handleFilterChanges(it)
        }
        filterChanges()

        databaseConfiguration()

    }


    fun filterChanges(filter: String = _application.getString(R.string.filter_changes_text)) {
        filterChanges.value = filter

    }

//    private fun setDefaultValue() {
//        // Set Custom Apps to SharedPreferences
//        _application.getSharedPreferences("state", Context.MODE_PRIVATE).edit()
//            .putString(
//                _application.getString(R.string.title_menu_select_option),
//                _application.getString(R.string.menu_custom_app)
//            ).apply()
//    }

    private fun databaseConfiguration() {
        getHideProgress()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                loadApps.searchInstalledApps()
            }
            filterChanges.postValue(_application.getString(R.string.filter_changes_text))

        }

    }

    suspend fun isEmpty(): Boolean {
        return appRepository.getCount() == 0
    }

    fun onRefresh() {
            appList!!.value!!.dataSource.invalidate()
    }


    fun getHideProgress(): LiveData<Boolean> {
        if (hideProgress == null) {
            hideProgress = MutableLiveData()
            loadHideProgress()
        }
        return hideProgress as MutableLiveData<Boolean>
    }

    private fun loadHideProgress() {
        hideProgress!!.value = true
    }

    fun checkRootPermission(): RootState? {
        val hasRootedPermission: Boolean = rootManager.hasRootedPermision()
        if (hasRootedPermission) return RootState.HAVE_ROOT
        val wasRooted: Boolean = rootManager.wasRooted()
        return if (wasRooted) RootState.BE_ROOT else RootState.NO_ROOT
    }


}