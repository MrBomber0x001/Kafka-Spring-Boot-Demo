package com.meska.consumer.repo;

import com.meska.consumer.entity.WikimediaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikimediaEventRepository extends JpaRepository<WikimediaEvent, Long> {
}