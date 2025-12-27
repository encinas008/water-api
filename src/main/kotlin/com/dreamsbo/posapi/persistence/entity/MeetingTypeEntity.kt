package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import java.util.*

@Entity
@Table(schema = "pos", name = "meeting_type")
@NoArg
data class MeetingTypeEntity(
    @Id
    @Column(name = "meeting_type_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "code", unique = true, nullable = false)
    var code: String,

    @Column(name = "name", nullable = false)
    var name: String,

    var active: Boolean = true,
)



