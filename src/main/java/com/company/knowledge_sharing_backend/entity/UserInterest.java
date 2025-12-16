package com.company.knowledge_sharing_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_interests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tag_id"}),
       indexes = {
           @Index(name = "idx_interest_user", columnList = "user_id"),
           @Index(name = "idx_interest_tag", columnList = "tag_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}

