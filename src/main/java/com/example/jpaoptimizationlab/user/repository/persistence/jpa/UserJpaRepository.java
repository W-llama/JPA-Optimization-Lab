package com.example.jpaoptimizationlab.user.repository.persistence.jpa;

import com.example.jpaoptimizationlab.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserJpaRepository extends JpaRepository<User,Long> {

    /**
     * JPQL과 FETCH JOIN을 사용하여 User와 UserPhotos를 한 번에 조회합니다.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.userPhotos")
    List<User> findAllWithUserPhotosFetchJoin();

    /**
     * JPQL과 FETCH JOIN을 사용하여 특정 User와 UserPhotos를 조회합니다.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.userPhotos WHERE u.id = :id")
    User findByIdWithUserPhotosFetchJoin(Long id);
}
