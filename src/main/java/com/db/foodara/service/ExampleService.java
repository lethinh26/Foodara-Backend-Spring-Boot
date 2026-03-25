package com.db.foodara.service;

import com.db.foodara.dto.request.ExampleCreationRequest;
import com.db.foodara.dto.request.ExampleUpdateRequest;
import com.db.foodara.entity.Example;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.ExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleService {
    @Autowired
    private ExampleRepository exampleRepository;

    public Example createUser(ExampleCreationRequest request) {
        Example example = new Example();

        if (exampleRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        example.setUsername(request.getUsername());
        example.setPassword(request.getPassword());
        example.setFirstName(request.getFirstName());
        example.setLastName(request.getLastName());
        example.setDob(request.getDob());

        return exampleRepository.save(example);
    }

    public List<Example> getUser() {
        return exampleRepository.findAll();
    }

    public Example getUser(String id) {
        return exampleRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Example updateUser(String userId, ExampleUpdateRequest request) {
        Example example = getUser(userId);
        example.setPassword(request.getPassword());
        example.setFirstName(request.getFirstName());
        example.setLastName(request.getLastName());
        example.setDob(request.getDob());

        return exampleRepository.save(example);
    }

    public void deleteUser(String userId) {
        exampleRepository.deleteById(userId);
    }
}
