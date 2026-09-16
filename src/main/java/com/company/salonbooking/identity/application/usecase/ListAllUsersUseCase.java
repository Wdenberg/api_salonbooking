package com.company.salonbooking.identity.application.usecase;

import com.company.salonbooking.identity.domain.model.User;
import com.company.salonbooking.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAllUsersUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;

    public ListAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> execute(int page, int size) {
        return userRepository.findAll(page, Math.min(size, MAX_PAGE_SIZE));
    }
}