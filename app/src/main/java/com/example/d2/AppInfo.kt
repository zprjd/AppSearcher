package com.example.d2

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase

@Entity(tableName = "installed_apps")
data class InstalledApp(
    @PrimaryKey @ColumnInfo(name = "packageName") val packageName: String,
    @ColumnInfo(name = "app_name") val appName: String,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "icon") val icon: ByteArray? // 使用 ByteArray 来存储图标
) {
    @Ignore
    constructor():this("","","",null)

    override fun toString(): String {
        return super.toString()
    }
}

@Dao
interface InstalledAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(app: InstalledApp)

    @Query("SELECT * FROM installed_apps WHERE app_name LIKE '%' || :name || '%'")
//    @Query("SELECT * FROM installed_apps WHERE app_name LIKE :query")
    fun searchAppsAccordingName(name: String): List<InstalledApp>


    @Query("SELECT * FROM installed_apps WHERE version LIKE '%' || :version || '%'")
//    @Query("SELECT * FROM installed_apps WHERE app_name LIKE :query")
    fun searchAppsAccordingVerison(version: String): List<InstalledApp>

    @Query("SELECT * FROM installed_apps")
    fun getAllApps(): List<InstalledApp>
}

@Database(entities = [InstalledApp::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
}