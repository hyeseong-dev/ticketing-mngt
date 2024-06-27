package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 정보 조회 서비스
 *
 * 이 클래스는 다른 도메인에서 사용자 관련 정보를 단순 조회할 때 사용됩니다.
 */
@Component
@RequiredArgsConstructor
public class UserReader {
    /* 타 도메인에서 User 관련 정보 단순 조회용 */

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public User findUser(Long userId) {
        return userRepository.findById(userId);
    }
}
