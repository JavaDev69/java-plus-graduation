package ru.practicum.events.service;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.dto.events.EventState;
import ru.practicum.events.dal.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventSpecification {
    public static Specification<Event> hasStatePublished() {
        return (root, query, cb) -> cb.equal(root.get(Event.Fields.state), EventState.PUBLISHED);
    }

    public static Specification<Event> hasTextInAnnotationOrDescription(String text) {
        if (text == null || text.isBlank()) {
            return Specification.where(null);
        }
        String searchText = "%" + text.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get(Event.Fields.annotation)), searchText),
                        cb.like(cb.lower(root.get(Event.Fields.description)), searchText)
                );
    }

    public static Specification<Event> belongsToCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get(Event.Fields.categoryId).in(categoryIds);
    }

    public static Specification<Event> isPaid(Boolean paid) {
        if (paid == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get(Event.Fields.paid), paid);
    }

    public static Specification<Event> isWithinRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (rangeStart != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(Event.Fields.eventDate), rangeStart));
            }
            if (rangeEnd != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(Event.Fields.eventDate), rangeEnd));
            }
            return predicate;
        };
    }
}
