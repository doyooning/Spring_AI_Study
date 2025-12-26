package com.dynii.evaluatesellerbot.controller;

import com.dynii.evaluatesellerbot.entity.AiEvaluation;
import com.dynii.evaluatesellerbot.entity.SellerRegisterEntity;
import com.dynii.evaluatesellerbot.repository.ChatRepository;
import com.dynii.evaluatesellerbot.repository.SellerRegisterRepository;
import com.dynii.evaluatesellerbot.service.SellerPlanEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SellerEvaluationController {

    // Repository for persisting seller registration data.
    private final SellerRegisterRepository sellerRegisterRepository;
    // Service for evaluating a seller plan with AI.
    private final SellerPlanEvaluationService sellerPlanEvaluationService;
    // Repository for reading AI evaluation results.
    private final ChatRepository chatRepository;

    // Render seller registration form.
    @GetMapping("/seller/register")
    public String showSellerRegisterForm() {
        return "seller-register";
    }

    // Handle seller registration and trigger AI evaluation.
    @PostMapping("/seller/register")
    public String submitSellerRegister(@RequestParam("sellerId") Long sellerId,
                                       @RequestParam("companyName") String companyName,
                                       @RequestParam("description") String description,
                                       @RequestParam("planFile") MultipartFile planFile) throws IOException {
        // Build seller registration entity from form input.
        SellerRegisterEntity registerEntity = new SellerRegisterEntity();
        registerEntity.setSellerId(sellerId);
        registerEntity.setCompanyName(companyName);
        registerEntity.setDescription(description);
        registerEntity.setPlanFile(planFile.getBytes());

        // Save registration and run evaluation.
        SellerRegisterEntity saved = sellerRegisterRepository.save(registerEntity);
        sellerPlanEvaluationService.evaluateAndSave(saved);

        return "redirect:/admin/evaluations";
    }

    // Render admin evaluation list.
    @GetMapping("/admin/evaluations")
    public String showAdminEvaluations(Model model) {
        // Load evaluations in descending time order for quick inspection.
        List<AiEvaluation> evaluations = chatRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("evaluations", evaluations);
        return "admin-evaluations";
    }
}
