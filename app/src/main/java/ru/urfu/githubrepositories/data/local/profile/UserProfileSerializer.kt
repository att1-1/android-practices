package ru.urfu.githubrepositories.data.local.profile

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserProfileSerializer : Serializer<UserProfileProto> {
    override val defaultValue: UserProfileProto = UserProfileProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserProfileProto {
        try {
            return UserProfileProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Не удалось прочитать профиль", exception)
        }
    }

    override suspend fun writeTo(t: UserProfileProto, output: OutputStream) {
        t.writeTo(output)
    }
}
