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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : FragmentActivity() {

    private lateinit var appAdapter: AppAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var nameSearchEditText: EditText
    private lateinit var versionSearchEditText: EditText
    private lateinit var appViewModel: AppViewModel
    private val TAG = "MainActivityTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // 初始化ViewModel
        appViewModel = ViewModelProvider(this).get(AppViewModel::class.java)

        // 初始化UI组件
        recyclerView = findViewById(R.id.showingAppRecyclerView)
        nameSearchEditText = findViewById(R.id.nameSearchEditText)
        versionSearchEditText = findViewById(R.id.versionSearchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        appAdapter = AppAdapter(emptyList())
        recyclerView.adapter = appAdapter

        // 观察ViewModel中的数据变化
        appViewModel.apps.observe(this, Observer { apps ->
            appAdapter.appList = apps
            appAdapter.notifyDataSetChanged()
        })
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nameQuery = nameSearchEditText.text.toString()
                val versionQuery = versionSearchEditText.text.toString()

                appViewModel.filterApps(nameQuery, versionQuery, this@MainActivity)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        nameSearchEditText.addTextChangedListener(textWatcher)
        versionSearchEditText.addTextChangedListener(textWatcher)

        appViewModel.loadAllApps()

        appViewModel.updateInstalledApps(this)
    }
}
