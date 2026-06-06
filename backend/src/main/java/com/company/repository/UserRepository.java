package com.company.repository;

import com.company.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    
    // 根据工号查询用户
    User findByJobNo(String jobNo);
    
    // 根据上级工号查询下级
    List<User> findBySuperiorJobNo(String superiorJobNo);
    
    // 根据姓名或工号模糊查询
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.jobNo LIKE %:keyword%")
    List<User> findByNameOrJobNo(@Param("keyword") String keyword);

    List<User> findByDepartmentId(Integer departmentId);

    List<User> findByDepartmentIdAndRole(Integer departmentId, String role);
}