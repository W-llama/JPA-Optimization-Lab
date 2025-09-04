package com.example.jpaoptimizationlab.user.repository.persistence;

import com.example.jpaoptimizationlab.user.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    List<User> findAllWithUserPhotosFetchJoin();

    User findByIdWithUserPhotosFetchJoin(Long id);

    List<User> findAllWithUserPhotosFetchJoinQueryDsl();

    User findByIdWithUserPhotosFetchJoinQueryDsl(Long id);
}
