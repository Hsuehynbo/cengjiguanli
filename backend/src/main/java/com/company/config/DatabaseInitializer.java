package com.company.config;

import com.company.entity.Department;
import com.company.entity.Permission;
import com.company.entity.User;
import com.company.repository.DepartmentRepository;
import com.company.repository.PermissionRepository;
import com.company.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        // 将明文密码升级为BCrypt编码
        upgradePasswords();

        // 权限初始化（独立于部门/用户初始化，可重复执行）
        initPermissions();

        if (departmentRepository.count() > 0) {
            log.info("数据库已经初始化过，跳过初始化过程");
            return;
        }

        log.info("开始初始化数据库数据");

        List<Department> departments = new ArrayList<>();
        String[] deptNames = {
            "办公室", "政工室", "情报指挥中心", "政治安全保卫大队", "治安管理大队",
            "刑事侦查大队", "网络安全保卫大队", "交通管理大队", "法治大队", "督察审计大队",
            "经济犯罪侦查大队", "资源环境和食品药品犯罪侦查大队", "巡特警大队", "县看守所",
            "城北派出所", "城南派出所", "梅源派出所", "石塘派出所", "紧水滩派出所", "云和湖派出所"
        };

        for (String deptName : deptNames) {
            departments.add(new Department(deptName));
        }
        departmentRepository.saveAll(departments);
        log.info("部门创建完成，共{}个部门", departments.size());

        List<User> users = new ArrayList<>();
        int currentUserJobNo = 1;

        // 系统超级管理员
        User admin = new User();
        admin.setJobNo("admin");
        admin.setName("系统管理员");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setPosition("系统管理员");
        admin.setRole("ADMIN_GLOBAL");
        admin.setDepartment(departments.get(0));
        users.add(admin);

        // 局长
        User director = new User();
        director.setJobNo(formatJobNo(currentUserJobNo));
        director.setName("局长");
        director.setPassword(passwordEncoder.encode(formatJobNo(currentUserJobNo)));
        director.setPosition("局长");
        director.setRole("ADMIN_GLOBAL");
        director.setRiskLevel("NORMAL");
        director.setDepartment(departments.get(0));
        users.add(director);
        currentUserJobNo++;

        // 为每个部门生成层级人员 (跳过办公室，局长已占)
        for (Department dept : departments) {
            if (dept.getDeptName().equals("办公室")) {
                continue;
            }

            String headPosition = getHeadPosition(dept.getDeptName());
            String deputyPosition = getDeputyPosition(dept.getDeptName());

            User deptHead = new User();
            deptHead.setJobNo(formatJobNo(currentUserJobNo));
            deptHead.setName(dept.getDeptName() + headPosition);
            deptHead.setPassword(passwordEncoder.encode(formatJobNo(currentUserJobNo)));
            deptHead.setPosition(headPosition);
            deptHead.setDepartment(dept);
            deptHead.setSuperiorJobNo(director.getJobNo());
            applyRiskLevel(deptHead, currentUserJobNo);
            users.add(deptHead);
            currentUserJobNo++;

            int numDeputies = 1;
            for (int i = 0; i < numDeputies; i++) {
                User deputy = new User();
                deputy.setJobNo(formatJobNo(currentUserJobNo));
                deputy.setName(dept.getDeptName() + deputyPosition + (i + 1));
                deputy.setPassword(passwordEncoder.encode(formatJobNo(currentUserJobNo)));
                deputy.setPosition(deputyPosition);
                deputy.setDepartment(dept);
                deputy.setSuperiorJobNo(deptHead.getJobNo());
                applyRiskLevel(deputy, currentUserJobNo);
                users.add(deputy);
                currentUserJobNo++;

                int numTeamLeaders = 2;
                for (int j = 0; j < numTeamLeaders; j++) {
                    User teamLeader = new User();
                    teamLeader.setJobNo(formatJobNo(currentUserJobNo));
                    teamLeader.setName(dept.getDeptName() + "组长" + (j + 1));
                    teamLeader.setPassword(passwordEncoder.encode(formatJobNo(currentUserJobNo)));
                    teamLeader.setPosition("组长");
                    teamLeader.setDepartment(dept);
                    teamLeader.setSuperiorJobNo(deputy.getJobNo());
                    applyRiskLevel(teamLeader, currentUserJobNo);
                    users.add(teamLeader);
                    currentUserJobNo++;

                    int numTeamMembers = 3;
                    for (int k = 0; k < numTeamMembers; k++) {
                        User teamMember = new User();
                        teamMember.setJobNo(formatJobNo(currentUserJobNo));
                        teamMember.setName(dept.getDeptName() + "组员" + (k + 1));
                        teamMember.setPassword(passwordEncoder.encode(formatJobNo(currentUserJobNo)));
                        teamMember.setPosition("组员");
                        teamMember.setDepartment(dept);
                        teamMember.setSuperiorJobNo(teamLeader.getJobNo());
                        applyRiskLevel(teamMember, currentUserJobNo);
                        users.add(teamMember);
                        currentUserJobNo++;
                    }
                }
            }
        }

        log.info("开始保存用户数据，共{}个用户", users.size());
        long startTime = System.currentTimeMillis();
        userRepository.saveAll(users);
        long endTime = System.currentTimeMillis();
        log.info("用户数据保存完成，耗时：{}ms", (endTime - startTime));

        log.info("数据库初始化完成：创建了{}个部门和{}个用户", departments.size(), users.size());

        long deptCount = departmentRepository.count();
        long userCount = userRepository.count();
        log.info("验证数据：部门数={}，用户数={}", deptCount, userCount);
    }

    private void upgradePasswords() {
        List<User> allUsers = userRepository.findAll();
        List<User> usersToFix = new ArrayList<>();
        for (User u : allUsers) {
            String pwd = u.getPassword();
            if (pwd != null && !pwd.startsWith("$2a$")) {
                u.setPassword(passwordEncoder.encode(pwd));
                usersToFix.add(u);
            }
        }
        if (!usersToFix.isEmpty()) {
            userRepository.saveAll(usersToFix);
            log.info("已将 {} 个用户的明文密码升级为BCrypt编码", usersToFix.size());
        }
    }

    private String formatJobNo(int number) {
        return String.format("%06d", number);
    }

    private String getHeadPosition(String deptName) {
        if (deptName.contains("派出所")) return "所长";
        if (deptName.contains("大队")) return "队长";
        if (deptName.contains("室")) return "科长";
        return "负责人";
    }

    private String getDeputyPosition(String deptName) {
        if (deptName.contains("派出所")) return "副所长";
        if (deptName.contains("大队")) return "副队长";
        if (deptName.contains("室")) return "副科长";
        return "副职";
    }

    private void applyRiskLevel(User user, int seed) {
        if (seed % 17 == 0) user.setRiskLevel("KEY");
        else if (seed % 13 == 0) user.setRiskLevel("RISK");
        else if (seed % 11 == 0) user.setRiskLevel("ATTENTION");
        else user.setRiskLevel("NORMAL");
    }

    private void initPermissions() {
        // 如果权限表为空，插入种子数据
        if (permissionRepository.count() == 0) {
            List<Permission> permissions = List.of(
                new Permission("GLOBAL_DASHBOARD", "全局看板", "查看全局管理看板", "DASHBOARD"),
                new Permission("PERSONNEL_MANAGE", "人事管理", "查看和编辑人员信息", "MANAGEMENT"),
                new Permission("HIERARCHY_MANAGE", "层级管理", "配置本部门人事层级和上下级归属", "MANAGEMENT"),
                new Permission("ACTIVITY_PUBLISH", "活动发布", "创建和管理活动任务", "MANAGEMENT"),
                new Permission("STAT_REPORTS", "统计报表", "查看统计报表和导出", "REPORTS")
            );
            permissionRepository.saveAll(permissions);
            log.info("权限初始化完成，共{}项权限", permissions.size());
        }

        // 补充新增的 HIERARCHY_MANAGE 权限（兼容已有数据库）
        if (!permissionRepository.existsByCode("HIERARCHY_MANAGE")) {
            permissionRepository.save(new Permission("HIERARCHY_MANAGE", "层级管理", "配置本部门人事层级和上下级归属", "MANAGEMENT"));
            log.info("补充新增权限：HIERARCHY_MANAGE");
        }

        // 清理已废弃的权限（兼容旧数据库）
        for (String obsoleteCode : List.of("TALK_RECORD_MANAGE", "HOME_VISIT_MANAGE")) {
            permissionRepository.findByCode(obsoleteCode).ifPresent(p -> {
                // 先删除关联表中的记录，再删除权限本身
                entityManager.createNativeQuery("DELETE FROM user_permissions WHERE permission_id = :pid")
                        .setParameter("pid", p.getId())
                        .executeUpdate();
                entityManager.flush();
                permissionRepository.delete(p);
                log.info("已清理废弃权限：{}", obsoleteCode);
            });
        }

        Set<Permission> allPerms = new HashSet<>(permissionRepository.findAll());
        if (allPerms.isEmpty()) return;

        // 为尚未授权的全局管理员授予全部权限
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(u -> ("admin".equals(u.getJobNo()) || "ADMIN_GLOBAL".equals(u.getRole()))
                        && (u.getPermissions() == null || u.getPermissions().isEmpty()))
                .toList();
        if (!adminUsers.isEmpty()) {
            for (User user : adminUsers) {
                user.setPermissions(allPerms);
            }
            userRepository.saveAll(adminUsers);
            log.info("已为{}个全局管理员授予全部权限", adminUsers.size());
        }

        // 为尚未授权的单位管理员授予 HIERARCHY_MANAGE + STAT_REPORTS
        Permission hierarchyPerm = permissionRepository.findByCode("HIERARCHY_MANAGE").orElse(null);
        Permission reportsPerm = permissionRepository.findByCode("STAT_REPORTS").orElse(null);
        if (hierarchyPerm != null && reportsPerm != null) {
            Set<Permission> unitAdminPerms = Set.of(hierarchyPerm, reportsPerm);
            List<User> unitAdmins = userRepository.findAll().stream()
                    .filter(u -> "ADMIN_UNIT".equals(u.getRole())
                            && (u.getPermissions() == null || u.getPermissions().isEmpty()))
                    .toList();
            if (!unitAdmins.isEmpty()) {
                for (User user : unitAdmins) {
                    user.setPermissions(new HashSet<>(unitAdminPerms));
                }
                userRepository.saveAll(unitAdmins);
                log.info("已为{}个单位管理员授予 HIERARCHY_MANAGE + STAT_REPORTS 权限", unitAdmins.size());
            }
        }
    }
}
