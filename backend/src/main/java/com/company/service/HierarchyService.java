package com.company.service;

import com.company.entity.User;
import com.company.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class HierarchyService {

    private final UserRepository userRepository;

    public HierarchyService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isSuperiorOf(String superiorJobNo, String targetJobNo) {
        if (superiorJobNo == null || targetJobNo == null) return false;
        if (superiorJobNo.equals(targetJobNo)) return false;

        User currentUser = userRepository.findByJobNo(targetJobNo);
        Set<String> visited = new HashSet<>();
        while (currentUser != null && currentUser.getSuperiorJobNo() != null
                && !currentUser.getSuperiorJobNo().isBlank()) {
            if (!visited.add(currentUser.getJobNo())) break;
            if (superiorJobNo.equals(currentUser.getSuperiorJobNo())) return true;
            currentUser = userRepository.findByJobNo(currentUser.getSuperiorJobNo());
        }
        return false;
    }

    public void validateHierarchy(String operatorJobNo, String targetJobNo) {
        if (operatorJobNo == null || operatorJobNo.isBlank() || targetJobNo == null || targetJobNo.isBlank()) {
            throw new IllegalArgumentException("缺少谈话人或被谈话人信息");
        }
        if (!isSuperiorOf(operatorJobNo, targetJobNo)) {
            throw new IllegalArgumentException("只能由上级对下级发起谈话");
        }
    }
}
