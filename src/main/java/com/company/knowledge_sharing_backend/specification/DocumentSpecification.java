package com.company.knowledge_sharing_backend.specification;

import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentSpecification {

    public static Specification<Document> buildSpecification(DocumentSearchRequest request, Long currentUserId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude archived documents by default
            if (!request.getIncludeArchived()) {
                predicates.add(criteriaBuilder.isFalse(root.get("isArchived")));
            }

            // Keyword search (title, summary, content)
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                String keyword = "%" + request.getQuery() + "%";

                Predicate titleMatch = criteriaBuilder.like(
                    root.get("title"), keyword);
                Predicate summaryMatch = criteriaBuilder.like(
                    root.get("summary"), keyword);
                Predicate contentMatch = criteriaBuilder.like(
                    root.get("content"), keyword);

                predicates.add(criteriaBuilder.or(titleMatch, summaryMatch, contentMatch));
            }

            // Filter by sharing level
            if (request.getSharingLevel() != null && !request.getSharingLevel().isEmpty()) {
                try {
                    SharingLevel level = SharingLevel.valueOf(request.getSharingLevel().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("sharingLevel"), level));
                } catch (IllegalArgumentException e) {
                    // Invalid sharing level, ignore
                }
            }

            // Filter by file type
            if (request.getFileType() != null && !request.getFileType().isEmpty()) {
                try {
                    FileType type = FileType.valueOf(request.getFileType().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("fileType"), type));
                } catch (IllegalArgumentException e) {
                    // Invalid file type, ignore
                }
            }

            // Filter by owner ID
            if (request.getOwnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner").get("id"), request.getOwnerId()));
            }

            // Filter by owner username
            if (request.getOwnerUsername() != null && !request.getOwnerUsername().isEmpty()) {
                String usernamePattern = "%" + request.getOwnerUsername() + "%";
                predicates.add(criteriaBuilder.like(
                    root.get("owner").get("username"),
                    usernamePattern
                ));
            }

            // Filter by tags
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                Join<Document, Tag> tagJoin = root.join("tags", JoinType.INNER);

                if (request.getMatchAllTags()) {
                    // Match ALL tags (AND logic)
                    for (String tagName : request.getTags()) {
                        Subquery<Long> subquery = query.subquery(Long.class);
                        Root<Document> subRoot = subquery.from(Document.class);
                        Join<Document, Tag> subTagJoin = subRoot.join("tags");

                        subquery.select(subRoot.get("id"))
                                .where(criteriaBuilder.and(
                                    criteriaBuilder.equal(subRoot.get("id"), root.get("id")),
                                    criteriaBuilder.equal(subTagJoin.get("name"), tagName)
                                ));

                        predicates.add(criteriaBuilder.exists(subquery));
                    }
                } else {
                    // Match ANY tags (OR logic)
                    predicates.add(tagJoin.get("name").in(request.getTags()));
                }
            }

            // Filter by groups
            if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
                Join<Document, Group> groupJoin = root.join("groups", JoinType.INNER);
                predicates.add(groupJoin.get("id").in(request.getGroupIds()));
            }

            // Filter by rating range (using subquery to calculate average)
            if (request.getMinRating() != null || request.getMaxRating() != null) {
                Subquery<Double> ratingSubquery = query.subquery(Double.class);
                Root<Rating> ratingRoot = ratingSubquery.from(Rating.class);

                ratingSubquery.select(criteriaBuilder.avg(ratingRoot.get("ratingValue")))
                    .where(criteriaBuilder.equal(ratingRoot.get("document").get("id"), root.get("id")));

                if (request.getMinRating() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(ratingSubquery, request.getMinRating()));
                }
                if (request.getMaxRating() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(ratingSubquery, request.getMaxRating()));
                }
            }

            // Filter by date range
            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), request.getToDate()));
            }

            // Filter by favorited (only user's favorites)
            if (request.getOnlyFavorited() && currentUserId != null) {
                Join<Document, Favorite> favoriteJoin = root.join("favorites", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                    favoriteJoin.get("user").get("id"), currentUserId));
            }

            // Remove duplicates when using joins
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

