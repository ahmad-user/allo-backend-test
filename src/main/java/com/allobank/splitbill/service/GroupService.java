package com.allobank.splitbill.service;

import com.allobank.splitbill.domain.GroupEntity;
import com.allobank.splitbill.domain.ParticipantEntity;
import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.dto.ParticipantRequest;
import com.allobank.splitbill.dto.ParticipantResponse;
import com.allobank.splitbill.exception.BadRequestException;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Group name is required");
        }
        if (request.participants() == null || request.participants().isEmpty()) {
            throw new BadRequestException("Participant list is required and cannot be empty");
        }

        Set<String> participantNames = new HashSet<>();
        for (ParticipantRequest p : request.participants()) {
            if (p.name() == null || p.name().isBlank()) {
                throw new BadRequestException("Participant name is required and cannot be blank");
            }
            String trimmedName = p.name().trim();
            if (!participantNames.add(trimmedName.toLowerCase())) {
                throw new BadRequestException("Duplicate participant name '" + p.name() + "' in the same group");
            }
        }

        GroupEntity group = new GroupEntity(request.name().trim());

        for (ParticipantRequest p : request.participants()) {
            ParticipantEntity participant = new ParticipantEntity(p.name().trim());
            group.addParticipant(participant);
        }

        GroupEntity savedGroup = groupRepository.save(group);
        return toGroupResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId) {
        GroupEntity group = groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        return toGroupResponse(group);
    }

    @Transactional(readOnly = true)
    public GroupEntity getGroupEntity(Long groupId) {
        return groupRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    public GroupResponse toGroupResponse(GroupEntity group) {
        List<ParticipantResponse> participantResponses = group.getParticipants().stream()
                .map(p -> new ParticipantResponse(p.getId(), p.getName()))
                .toList();

        return new GroupResponse(group.getId(), group.getName(), participantResponses);
    }
}
