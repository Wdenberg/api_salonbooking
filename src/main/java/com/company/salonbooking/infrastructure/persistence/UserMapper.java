package com.company.salonbooking.infrastructure.persistence;

import com.company.salonbooking.identity.domain.model.User;

public class UserMapper {
    private UserMapper() {
    }

    static User toDomain(UserJpaEntity entity){
        return User.restore(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getStatus(),
                entity.getRoles(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    static UserJpaEntity toEntity(User user){
        return new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
