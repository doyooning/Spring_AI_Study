package com.dynii.evaluatesellerbot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "seller_register")
public class SellerRegisterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "register_id")
    private Long id;

    // Business plan file submitted for seller review.
    @Lob
    @Column(name = "plan_file", columnDefinition = "LONGBLOB")
    private byte[] planFile;

    // Seller user id tied to this registration.
    @Column(name = "seller_id")
    private Long sellerId;

    // Optional description of the seller's business.
    @Column(name = "description")
    private String description;

    // Company name submitted during seller signup.
    @Column(name = "company_name")
    private String companyName;
}
