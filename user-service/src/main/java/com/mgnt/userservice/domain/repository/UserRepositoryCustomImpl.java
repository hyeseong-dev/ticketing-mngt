package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.QUsers;
import com.mgnt.userservice.domain.entity.Users;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Users> findUserByEmail(String email) {
        QUsers user = QUsers.users;
        Users foundUser = queryFactory.selectFrom(user)
                .where(user.email.eq(email))
                .fetchOne();
        return Optional.ofNullable(foundUser);
    }

    @Override
    public List<Users> findUsersWithNoDeletion() {
        QUsers user = QUsers.users;
        return queryFactory.selectFrom(user)
                .where(user.deletedAt.isNull())
                .fetch();
    }

    @Override
    public Optional<Users> findUserByIdIfNotDeleted(Long id) {
        QUsers user = QUsers.users;
        Users foundUser = queryFactory.selectFrom(user)
                .where(user.userId.eq(id)
                        .and(user.deletedAt.isNull()))
                .fetchOne();
        return Optional.ofNullable(foundUser);
    }
}