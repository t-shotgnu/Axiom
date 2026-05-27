package com.studproj.axiom.application.features.projectmembers.removeprojectmember;

import java.util.UUID;

public record RemoveProjectMemberCommand(UUID projectId, UUID userId) {}
