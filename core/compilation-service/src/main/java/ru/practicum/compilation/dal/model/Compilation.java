package ru.practicum.compilation.dal.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @Column(name = "description", length = 1000)
    private String description;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id")
    )
    @Column(name = "event_id")
    private List<Long> eventIds = new ArrayList<>();

    public Compilation(String title, Boolean pinned, String description) {
        this.title = title;
        this.pinned = pinned;
        this.description = description;
    }
}
