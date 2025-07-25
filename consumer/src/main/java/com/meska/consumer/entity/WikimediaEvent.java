package com.meska.consumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class WikimediaEvent {
    @Id
    private String id; // Use the "id" field from JSON (e.g., 2942944030)
    private String type; // e.g., "categorize"
    private String title; // e.g., "Category:2014 in Tunisia"
    @Column(name = "event_user")
    private String user;
    private long timestamp; // e.g., 1753394496
    private String wiki; // e.g., "commonswiki"
    private String comment; // e.g., "File:593Thurburbo Maius (51099504608).jpg added to category"
}
