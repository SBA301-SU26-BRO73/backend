package com.sba301.backend.repository.specification;

import com.sba301.backend.dto.request.BranchFilterRequest;
import com.sba301.backend.entity.Branch;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BranchSpecification {

    public static Specification<Branch> filterByCriteria(BranchFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
            }
            if (filter.getAddress() != null && !filter.getAddress().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + filter.getAddress().trim().toLowerCase() + "%"));
            }
            if (filter.getWard() != null && !filter.getWard().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("ward")), filter.getWard().trim().toLowerCase()));
            }
            if (filter.getCity() != null && !filter.getCity().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), filter.getCity().trim().toLowerCase()));
            }
            if (filter.getPhone() != null && !filter.getPhone().trim().isEmpty()) {
                predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone().trim() + "%"));
            }

            // Xử lý status: Nếu có truyền thì lọc theo biến, nếu không thì mặc định là ACTIVE
            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus().trim()));
            } else {
                predicates.add(cb.equal(root.get("status"), "ACTIVE"));
            }

            // Loại bỏ các bản ghi đã bị xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Sắp xếp giảm dần theo thời gian tạo
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}