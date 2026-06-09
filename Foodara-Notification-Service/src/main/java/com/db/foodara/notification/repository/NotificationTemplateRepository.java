package com.db.foodara.notification.repository;

import com.db.foodara.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByCodeAndChannel(String code, String channel);

    Optional<NotificationTemplate> findByCode(String code);
}
