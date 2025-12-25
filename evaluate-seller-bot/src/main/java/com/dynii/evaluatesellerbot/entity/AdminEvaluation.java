package com.dynii.evaluatesellerbot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "admin_evaluation")
public class AdminEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_eval_id")
    private Long adminEvalId;

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

    @Column(name = "admin_comment", length = 250)
    private String adminComment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ai_eval_id", nullable = false)
    private Long aiEvalId;
}