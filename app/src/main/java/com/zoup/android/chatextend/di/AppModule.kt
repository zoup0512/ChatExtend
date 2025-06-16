package com.zoup.android.chatextend.di

// AppModule.kt
import com.zoup.android.chatextend.ChatApplication
import com.zoup.android.chatextend.data.database.AppDatabase
import com.zoup.android.chatextend.data.repository.ChatMessageRepository
import com.zoup.android.chatextend.data.repository.MessageCategoryRepository
import com.zoup.android.chatextend.data.repository.NotesRepository
import com.zoup.android.chatextend.ui.category.CategoryViewModel
import com.zoup.android.chatextend.ui.chat.ChatViewModel
import com.zoup.android.chatextend.ui.notes.NotesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // 提供 Application 实例
    single { androidApplication() as ChatApplication }

    // 提供 AppDatabase 实例（使用 Application 中的 database 属性）
    single {
        val application: ChatApplication = get()
        application.database
    }

    // 提供 ChatMessageDao 实例
    single { get<AppDatabase>().chatMessageDao() }

     // 提供 MessageCategoryDao 实例
    single { get<AppDatabase>().messageCategoryDao() }

    // 提供 ChatRepository 实例，依赖 ChatMessageDao
    single { ChatMessageRepository(get()) }

    single {  MessageCategoryRepository(get())}

    single { NotesRepository(get(), get()) }

    // 提供 ChatViewModel 实例，依赖 ChatRepository
    viewModel { ChatViewModel(get()) }

    viewModel { CategoryViewModel(get()) }

    viewModel { NotesViewModel(get()) }

    // ViewModel
//    factory { CategoryViewModel(get()) }

}