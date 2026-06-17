package com.studproj.axiom.infrastructure.config;

import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectRoleSeederTest {

    @Test
    void seedsMissingMemberAndAdminRoles() {
        ProjectRoleRepository repository = mock(ProjectRoleRepository.class);
        when(repository.findByType(ProjectRoleType.MEMBER)).thenReturn(Optional.empty());
        when(repository.findByType(ProjectRoleType.ADMIN)).thenReturn(Optional.empty());
        ProjectRoleSeeder seeder = new ProjectRoleSeeder(repository);

        seeder.run(mock(ApplicationArguments.class));

        ArgumentCaptor<ProjectRole> captor = ArgumentCaptor.forClass(ProjectRole.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ProjectRole::getType)
                .containsExactly(ProjectRoleType.MEMBER, ProjectRoleType.ADMIN);
        assertThat(captor.getAllValues())
                .extracting(ProjectRole::getId)
                .doesNotContainNull();
    }

    @Test
    void doesNotDuplicateExistingRoles() {
        ProjectRoleRepository repository = mock(ProjectRoleRepository.class);
        when(repository.findByType(ProjectRoleType.MEMBER))
                .thenReturn(Optional.of(new ProjectRole(UUID.randomUUID(), ProjectRoleType.MEMBER)));
        when(repository.findByType(ProjectRoleType.ADMIN))
                .thenReturn(Optional.of(new ProjectRole(UUID.randomUUID(), ProjectRoleType.ADMIN)));
        ProjectRoleSeeder seeder = new ProjectRoleSeeder(repository);

        seeder.run(mock(ApplicationArguments.class));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(ProjectRole.class));
    }
}
