package com.db.foodara.controller;

import com.db.foodara.dto.request.ExampleCreationRequest;
import com.db.foodara.dto.request.ExampleUpdateRequest;
import com.db.foodara.entity.Example;
import com.db.foodara.service.ExampleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hello")
public class ExampleController {
    @Autowired
    private ExampleService exampleService;

    @PostMapping
    Example createUser(@RequestBody @Valid ExampleCreationRequest request) {
        return exampleService.createUser(request);
    }

    @GetMapping()
    List<Example> getUser() {
        return exampleService.getUser();
    }

    @GetMapping("/{userId}")
    Example getUser(@PathVariable String userId) {
        return exampleService.getUser(userId);
    }

    @PutMapping("/{userId}")
    Example updateUser(@PathVariable String userId, @RequestBody ExampleUpdateRequest request) {
        return exampleService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId) {
        exampleService.deleteUser(userId);
        return "User has ben deleted";
    }
}
