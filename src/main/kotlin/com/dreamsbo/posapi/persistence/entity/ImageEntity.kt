package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "pos", name = "image")
@NoArg
data class ImageEntity(
    @Id
    @Column(name = "image_id")
    var id: UUID = UUID.randomUUID(),

    var name: String?,
    @Column(name = "content_type")
    var contentType: String?,
    @Column(name = "content_size_kb")
    var contentSize: Long = 0L,
    var url: String = "",

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
