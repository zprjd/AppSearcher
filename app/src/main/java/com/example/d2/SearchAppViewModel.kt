package com.example.d2

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.health.connect.datatypes.AppInfo
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(application, AppDatabase::class.java, "app_database").build()
    private val appDao: InstalledAppDao

    // LiveData 用于 UI 更新
    private val _apps = MutableLiveData<List<InstalledApp>>()
    val apps: LiveData<List<InstalledApp>> get() = _apps

    init {
        appDao = db.installedAppDao()
    }


    // 加载所有应用
    fun loadApps() {
        CoroutineScope(Dispatchers.IO).launch {
            val allApps = appDao.getAllApps()
            withContext(Dispatchers.Main) {
                _apps.value = allApps
            }
        }
    }

    // 根据名称搜索应用
    fun searchAppsAccordingName(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredApps = appDao.searchAppsAccordingName("%$query%")
            withContext(Dispatchers.Main) {
                _apps.value = filteredApps
            }
        }
    }

    // 根据版本搜索应用
    fun searchAppsAccordingVersion(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredApps = appDao.searchAppsAccordingVerison("%$query%")
            withContext(Dispatchers.Main) {
                _apps.value = filteredApps
            }
        }
    }

    // 更新安装的应用
    fun updateInstalledApps(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val apps = getInstalledApps(context)
            for (app in apps) {
                appDao.insertApp(app)
            }
            loadApps() // 更新应用列表
        }
    }

    // 获取已安装的应用
    private fun getInstalledApps(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager
        val apps = mutableListOf<InstalledApp>()
        val installedPackages = packageManager.getInstalledApplications(0)

        for (appInfo in installedPackages) {
            try {
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val packageName = appInfo.packageName
                val version = packageManager.getPackageInfo(packageName, 0).versionName
                val icon = packageManager.getApplicationIcon(appInfo).toBitmap()
                val iconByteArray = icon?.let { getByteArrayFromBitmap(it) }

                apps.add(InstalledApp(packageName, appName, version, iconByteArray))
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        return apps
    }

    // 将 Bitmap 转换为 ByteArray
    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}