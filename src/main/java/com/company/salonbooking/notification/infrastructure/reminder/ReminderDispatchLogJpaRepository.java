package com.company.salonbooking.notification.infrastructure.reminder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderDispatchLogJpaRepository extends JpaRepository<ReminderDispatchLogJpaEntity, ReminderDispatchLogId> {
}