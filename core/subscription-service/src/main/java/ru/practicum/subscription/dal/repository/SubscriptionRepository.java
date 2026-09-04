package ru.practicum.subscription.dal.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.subscription.dal.model.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsBySubscriberIdAndPublisherId(Long subscriberId, Long publisherId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.subscriberId = :subscriberId AND s.publisherId = :publisherId")
    int deleteBySubscriberIdAndPublisherId(
            @Param("subscriberId") Long subscriberId,
            @Param("publisherId") Long publisherId);

    List<Long> findAllBySubscriberId(Long subscriberId);

    @Query("SELECT s.publisherId FROM Subscription s WHERE s.subscriberId = :subscriberId")
    List<Long> findAllPublisherBySubscriberId(
            @NotNull(message = "Идентификатор не может быть null")
            @Positive(message = "Идентификатор должен быть положительным числом (больше 0)")
            @Param("subscriberId") Long subscriberId);
}
