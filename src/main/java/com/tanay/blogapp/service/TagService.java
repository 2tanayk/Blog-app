package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.TagsDto;
import com.tanay.blogapp.entity.Tag;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.TagMapper;
import com.tanay.blogapp.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    /**
     * Deletes a tag by its ID.
     * <p>
     * Bulk-deletes join table rows first (post_tag) so the FK constraint
     * doesn't block the tag delete — no entity loading, no N+1.
     */
    @PreAuthorize("hasAuthority('TAG_DELETE')")
    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag with id " + id + " not found");
        }

        tagRepository.deletePostTagAssociations(id);
        tagRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TagsDto getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tagMapper.toTagsDto(tags);
    }
}