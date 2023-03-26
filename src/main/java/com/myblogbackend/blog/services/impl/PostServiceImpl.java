package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.constant.ErrorMessage;
import com.myblogbackend.blog.exception.BlogLangException;
import com.myblogbackend.blog.exception.ResourceNotFoundException;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final PostMapper postMapper;

    @Override
    public PostResponse createPost(PostRequest postRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var category = validateCategory(postRequest.getCategoryId());
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        // populate owner
        postEntity.setUser(usersRepository.findById(signedInUser.getId()).orElseThrow());
        var createdPost = postRepository.save(postEntity);
        return postMapper.toPostResponse(createdPost);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        var postEntities = postRepository.findAll();
        return postMapper.toListPostResponse(postEntities);
    }

    @Override
    public List<PostResponse> getAllPostsByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BlogLangException(ErrorMessage.NOT_FOUND);
        }
        var posts = postRepository.findByCategoryId(categoryId);
        return postMapper.toListPostResponse(posts);
    }

    @Override
    public PostResponse getPostById(Long id) {
        var post = postRepository
                .findById(id)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
        return postMapper.toPostResponse(post);
    }


    @Override
    public PostResponse updatePost(Long id, PostRequest postRequest) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
        var category = validateCategory(postRequest.getCategoryId());
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setCategory(category);
        var updatedPost = postRepository.save(post);
        return postMapper.toPostResponse(post);
    }

    private CategoryEntity validateCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BlogLangException(ErrorMessage.NOT_FOUND));
    }
}
