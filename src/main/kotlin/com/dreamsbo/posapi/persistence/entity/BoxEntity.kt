package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "box")
@NoArg
data class BoxEntity(
    @Id
    @Column(name = "box_id")
    var id: UUID = UUID.randomUUID(),

    val name: String,
    val description: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: UserEntity,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
