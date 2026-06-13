package com.sba301.backend.repository.specifacation;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.dto.request.BranchFilterRequest;
import com.sba301.backend.entity.Branch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BranchSpecification {

    public static Specification<Branch> filterByCriteria(BranchFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getAddress())) {
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + filter.getAddress().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getWard())) {
                predicates.add(cb.like(cb.lower(root.get("ward")), "%" + filter.getWard().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getCity())) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + filter.getCity().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getPhone())) {
                predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone() + "%"));
            }
            if (StringUtils.hasText(filter.getStatus())) {
                try {
                    BranchStatus statusEnum = BranchStatus.valueOf(filter.getStatus().trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            if (StringUtils.hasText(filter.getCourtTypeName())) {
                query.distinct(true);
                Join<Object, Object> courtsJoin = root.join("courts", JoinType.LEFT);
                Join<Object, Object> courtTypeJoin = courtsJoin.join("courtType", JoinType.LEFT);
                predicates.add(cb.like(
                        cb.lower(courtTypeJoin.get("name")),
                        "%" + filter.getCourtTypeName().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}