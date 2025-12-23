package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeetingPartnerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeetingPartnerRepository : JpaRepository<MeetingPartnerEntity, UUID> {
    fun findByMeetingIdAndActive(meetingId: UUID, active: Boolean): MutableList<MeetingPartnerEntity>
    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean): MutableList<MeetingPartnerEntity>
    fun findByMeetingIdAndPartnerId(meetingId: UUID, partnerId: UUID): Optional<MeetingPartnerEntity>

    @Query("SELECT mp FROM MeetingPartnerEntity mp WHERE mp.meeting.id = :meetingId AND mp.partner.id = :partnerId AND mp.active = :active")
    fun findActiveByMeetingIdAndPartnerId(meetingId: UUID, partnerId: UUID, active: Boolean): Optional<MeetingPartnerEntity>
}

