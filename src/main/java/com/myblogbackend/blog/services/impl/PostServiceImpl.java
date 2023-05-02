package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.constant.ErrorMessage;
import com.myblogbackend.blog.exception.BlogLangException;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.pagination.OffsetPageRequest;
import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final PostMapper postMapper;

    @Override
    public PostResponse createPost(final PostRequest postRequest) {
        // Get the signed-in user from the JWT token
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        // Validate the category ID and return the corresponding category
        var category = validateCategory(postRequest.getCategoryId());
        // Map the post request to a post entity and set its category
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        // Set the post's owner to the signed-in user
        postEntity.setUser(usersRepository.findById(signedInUser.getId()).orElseThrow());
        try {
            // Log a success message
            var createdPost = postRepository.save(postEntity);
            logger.info("Post was created with id: {}", createdPost.getId());
            return postMapper.toPostResponse(createdPost);
        } catch (Exception e) {
            logger.error("Failed to create post", e);
            throw new RuntimeException("Failed to create post");
        }
    }

    @Override
    public PaginationPage<PostResponse> getAllPostsByUserId(final Integer offset, final Integer limited, final UUID userId) {
        var pageable = new OffsetPageRequest(offset, limited);
        var postEntities = postRepository.findAllByUserId(userId, pageable);
        var postResponses = postEntities.getContent().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
        return new PaginationPage<PostResponse>()
                .setRecords(postResponses)
                .setOffset(postEntities.getNumber())
                .setLimit(postEntities.getSize())
                .setTotalRecords(postEntities.getTotalElements());
    }

    @Override
    public PaginationPage<PostResponse> getAllPostsByCategoryId(final Integer offset, final Integer limited, final UUID categoryId) {
        var pageable = new OffsetPageRequest(offset, limited);
        var posts = postRepository.findAllByCategoryId(pageable, categoryId);
        var postResponses = posts.getContent().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        return new PaginationPage<PostResponse>()
                .setRecords(postResponses)
                .setOffset(posts.getNumber())
                .setLimit(posts.getSize())
                .setTotalRecords(posts.getTotalElements());
    }

    @Override
    public PostResponse getPostById(final UUID id) {
        var post = postRepository
                .findById(id)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
        return postMapper.toPostResponse(post);
    }


    @Override
    public PostResponse updatePost(final UUID id, final PostRequest postRequest) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
        var category = validateCategory(postRequest.getCategoryId());
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setCategory(category);
        var updatedPost = postRepository.save(post);
        return postMapper.toPostResponse(updatedPost);
    }

    private CategoryEntity validateCategory(final UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
    }
}
