package com.dynii.evaluatesellerbot.dto;

import com.dynii.evaluatesellerbot.entity.SellerGrade;

public record EvaluateDTO(int businessStability, int productCompetency,
                          int liveSuitability, int operationCoop,
                          int growthPotential, int total_score,
                          SellerGrade gradeRecommended, String summary) {
}
