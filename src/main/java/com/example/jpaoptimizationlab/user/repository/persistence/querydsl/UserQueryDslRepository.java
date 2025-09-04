package com.example.jpaoptimizationlab.user.repository.persistence.querydsl;

import com.example.jpaoptimizationlab.user.domain.entity.QUser;
import com.example.jpaoptimizationlab.user.domain.entity.QUserPhoto;
import com.example.jpaoptimizationlab.user.domain.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public UserQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    /**
     * QueryDSL을 사용하여 User와 UserPhotos를 한 번에 조회합니다.
     */
    public List<User> findAllWithUserPhotosFetchJoin() {
        QUser user = QUser.user;
        QUserPhoto userPhoto = QUserPhoto.userPhoto;

        return queryFactory
                .selectFrom(user)
                .distinct()
                .join(user.userPhotos, userPhoto).fetchJoin()
                .fetch();
    }

    /**
     * QueryDSL을 사용하여 특정 User와 UserPhotos를 조회합니다.
     */
    public User findByIdWithUserPhotosFetchJoin(Long id) {
        QUser user = QUser.user;
        QUserPhoto userPhoto = QUserPhoto.userPhoto;

        return queryFactory
                .selectFrom(user)
                .join(user.userPhotos, userPhoto).fetchJoin()
                .where(user.id.eq(id))
                .fetchOne();
    }
}