package ru.urfu.githubrepositories.data.local.profile

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import java.io.ByteArrayInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserProfileDataStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun newStoreReturnsEmptyProfile() = runBlocking {
        val file = temporaryFolder.root.resolve("profile.pb")
        val job = SupervisorJob()
        try {
            val store = DataStoreFactory.create(
                serializer = UserProfileSerializer,
                scope = CoroutineScope(Dispatchers.IO + job),
                produceFile = { file }
            )
            assertEquals(UserProfileProto.getDefaultInstance(), store.data.first())
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun savedProfileSurvivesReopeningStore() = runBlocking {
        val file = temporaryFolder.root.resolve("profile.pb")
        val profile = UserProfileProto.newBuilder()
            .setFullName("Иван Иванов")
            .setPosition("Студент")
            .setResumeUrl("https://example.com/resume.pdf")
            .setAvatarUri("content://ru.urfu.githubrepositories.fileprovider/profile_images/avatar.img")
            .build()
        val firstJob = SupervisorJob()
        try {
            val store = DataStoreFactory.create(
                serializer = UserProfileSerializer,
                scope = CoroutineScope(Dispatchers.IO + firstJob),
                produceFile = { file }
            )
            store.updateData { profile }
        } finally {
            firstJob.cancelAndJoin()
        }

        // The first store must stop before another store opens the same file.
        val secondJob = SupervisorJob()
        try {
            val reopened = DataStoreFactory.create(
                serializer = UserProfileSerializer,
                scope = CoroutineScope(Dispatchers.IO + secondJob),
                produceFile = { file }
            )
            assertEquals(profile, reopened.data.first())
            reopened.updateData { it.toBuilder().setPosition("").setAvatarUri("").build() }
            assertEquals("", reopened.data.first().avatarUri)
            assertEquals("", reopened.data.first().position)
            assertEquals(profile.fullName, reopened.data.first().fullName)
        } finally {
            secondJob.cancelAndJoin()
        }
    }

    @Test(expected = CorruptionException::class)
    fun malformedProtoReportsCorruption(): Unit = runBlocking {
        UserProfileSerializer.readFrom(ByteArrayInputStream(byteArrayOf(0x0A, 0x05, 0x41)))
        Unit
    }
}
