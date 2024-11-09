package com.example.d2

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : FragmentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var appDao: InstalledAppDao
    private lateinit var appAdapter: AppAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var allApps: List<InstalledApp>
    private val TAG = "MainActivityTAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
        appDao = db.installedAppDao()

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        appAdapter = AppAdapter(emptyList())
        recyclerView.adapter = appAdapter

        // 查询数据库并加载应用
        loadApps()

        // 设置搜索框监听器
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                Log.e(TAG, "afterTextChanged:query ${query}", )
                if (query.isNotEmpty()) {
                    searchApps(query)
                } else {
                    loadApps()  // 如果没有输入内容，则加载所有应用
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadApps() {
        // 如果数据库中有数据，直接使用数据库数据
        CoroutineScope(Dispatchers.IO).launch {
            allApps = appDao.getAllApps()
            withContext(Dispatchers.Main) {
                appAdapter = AppAdapter(allApps)
                recyclerView.adapter = appAdapter
            }
        }
    }

    private fun searchApps(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredApps = appDao.searchApps("%$query%")
            withContext(Dispatchers.Main) {
                appAdapter = AppAdapter(filteredApps)
                recyclerView.adapter = appAdapter
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // 每次打开App时，刷新应用数据库
        updateInstalledApps()
    }

    private fun updateInstalledApps() {
        val apps = getInstalledApps(this)
        CoroutineScope(Dispatchers.IO).launch {
            for (app in apps) {
                appDao.insertApp(app)
            }
            loadApps()  // 更新界面
        }
    }

    fun getInstalledApps(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager
        val apps = mutableListOf<InstalledApp>()

        val installedPackages = packageManager.getInstalledApplications(0)
        Toast.makeText(this, "number: ${installedPackages.size}", Toast.LENGTH_SHORT).show()
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

    fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
