// UserRepositoryImpl.java
package com.example.jpaoptimizationlab.user.repository.persistence;

import com.example.jpaoptimizationlab.user.domain.entity.User;
import com.example.jpaoptimizationlab.user.repository.persistence.jpa.UserJpaRepository;
import com.example.jpaoptimizationlab.user.repository.persistence.querydsl.UserQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserQueryDslRepository userQueryDslRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public List<User> findAllWithUserPhotosFetchJoin() {
        return userJpaRepository.findAllWithUserPhotosFetchJoin();
    }

    @Override
    public User findByIdWithUserPhotosFetchJoin(Long id) {
        return userJpaRepository.findByIdWithUserPhotosFetchJoin(id);
    }

    @Override
    public List<User> findAllWithUserPhotosFetchJoinQueryDsl() {
        return userQueryDslRepository.findAllWithUserPhotosFetchJoin();
    }

    @Override
    public User findByIdWithUserPhotosFetchJoinQueryDsl(Long id) {
        return userQueryDslRepository.findByIdWithUserPhotosFetchJoin(id);
    }
}