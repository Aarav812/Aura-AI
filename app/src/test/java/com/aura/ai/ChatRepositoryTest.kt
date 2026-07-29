package com.aura.ai

import com.aura.ai.core.common.DispatcherProvider
import com.aura.ai.data.local.dao.ChatDao
import com.aura.ai.data.repository.ChatRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ChatRepositoryTest {

    private val dao = mockk<ChatDao>(relaxed = true)
    private val dispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }
    private val repo = ChatRepositoryImpl(dao, dispatchers)

    @Test fun `createChat persists and returns chat`() = runTest {
        val chat = repo.createChat(model = "meta/llama-3.3-70b-instruct")
        assertNotNull(chat.id)
        assertEquals("meta/llama-3.3-70b-instruct", chat.model)
        coVerify { dao.upsertChat(any()) }
    }

    @Test fun `setPinned delegates to dao`() = runTest {
        repo.setPinned("chat1", true)
        coVerify { dao.setPinned("chat1", true) }
    }
}
