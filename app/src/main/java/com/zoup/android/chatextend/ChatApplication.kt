package com.zoup.android.chatextend

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport
import com.zoup.android.chatextend.data.database.AppDatabase
import com.zoup.android.chatextend.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class ChatApplication : Application() {
    // 提供一个全局的数据库实例
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "b49f1be65e", false);
        // 可以在这里做其他全局初始化操作
        startKoin {
            androidContext(this@ChatApplication)
            modules(appModule)
        }
    }
}
