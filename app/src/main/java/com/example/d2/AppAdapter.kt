package com.example.d2
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val appList: List<InstalledApp>) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = appList[position]
        holder.appName.text = app.appName
        holder.packageName.text = app.packageName
        holder.version.text = app.version
        app.icon?.let {
            holder.appIcon.setImageBitmap(getBitmapFromByteArray(it))
        }
    }

    override fun getItemCount(): Int = appList.size

    private fun getBitmapFromByteArray(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val packageName: TextView = view.findViewById(R.id.packageName)
        val version: TextView = view.findViewById(R.id.version)
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
    }
}