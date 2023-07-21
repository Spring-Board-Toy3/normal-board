package com.boardapplication.configuration;

import com.boardapplication.dto.JoinDto;
import com.boardapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class JoinValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(JoinDto.class);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        JoinDto joinDto = (JoinDto) obj;
        if (userRepository.existsByUsername(joinDto.getUsername())) {
            errors.rejectValue("username", "invalid.username",
                    new Object[]{joinDto.getUsername()}, "이미 사용중인 아이디입니다.");
        }
    }
}