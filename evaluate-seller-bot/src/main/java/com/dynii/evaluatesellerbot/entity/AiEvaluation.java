package com.dynii.evaluatesellerbot.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ai_evaluation")
public class AiEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_eval_id")
    private Long aiEvalId;

    @Column(name = "business_stability", nullable = false)
    private Integer businessStability;

    @Column(name = "product_competency", nullable = false)
    private Integer productCompetency;

    @Column(name = "live_suitability", nullable = false)
    private Integer liveSuitability;

    @Column(name = "operation_coop", nullable = false)
    private Integer operationCoop;

    @Column(name = "growth_potential", nullable = false)
    private Integer growthPotential;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_recommended", nullable = false)
    private SellerGrade sellerGrade;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @CurrentTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

}
