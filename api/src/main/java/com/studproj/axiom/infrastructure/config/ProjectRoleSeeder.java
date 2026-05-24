package com.studproj.axiom.infrastructure.config;

import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectRoleSeeder implements ApplicationRunner {
    private final ProjectRoleRepository projectRoleRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        seed(ProjectRoleType.MEMBER);
        seed(ProjectRoleType.ADMIN);
    }

    private void seed(ProjectRoleType type) {
        projectRoleRepository.findByType(type).orElseGet(() -> {
            ProjectRole role = ProjectRole.builder()
                    .id(UUID.randomUUID())
                    .type(type)
                    .build();
            projectRoleRepository.save(role);
            return role;
        });
    }
}