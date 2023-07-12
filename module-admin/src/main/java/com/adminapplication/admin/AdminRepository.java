package com.adminapplication.admin;

import com.adminapplication.dto.AllUsersInfoResponseDto;
import com.core.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminRepository {
    List<User> findAllUsers();
    List<AllUsersInfoResponseDto> findAllUsersInfo();
}
