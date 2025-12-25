package com.dynii.evaluatesellerbot.repository;

import com.dynii.evaluatesellerbot.entity.AiEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<AiEvaluation, Long> {
    List<AiEvaluation> findByAiEvalIdOrderByCreatedAtAsc(Long aiEvalId);
}
