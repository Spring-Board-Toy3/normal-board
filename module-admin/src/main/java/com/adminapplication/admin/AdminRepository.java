package com.adminapplication.admin;

import com.adminapplication.dto.*;
import com.core.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminRepository {
//    List<User> findAllUsers();

    List<AllUsersInfoResponseDto> findSortedAllUsersInfo(String orderBy, String target, String desc);

    Integer updateRoleById(String role, Long id);

    User findUserById(Long id);

    Board findBoardById(Long id);

    Integer countBoardSizeByUserId(Long id);

    List<AllBoardsResponseDto> findAllBoards();

    Integer updateStatusById(Status status, Long id);

    Integer deleteBoardById(Long id);

    void deleteAllCommentsByBoardId(Long id);

    Integer countCommentSizeByBoardId(Long id);

    List<AllReportsResponseDto> findAllReports();

    Integer deleteReportByBoardId(Long id);

    Integer countReportSizeByBoardId(Long id);

    List<ReportDetailsResponseDto> findReportsByBoardId(Long id);

    List<AllBlacklistsResponseDto> findAllBlacklists();

    Integer insertBlacklist(Blacklist blacklist);

    Integer deleteBlacklistById(Long id);

    List<Board> findAllBoardsById(Long id);

    Admin findByUsernameAndPassword(String username, String password);

    Admin findByUsername(String username);

}
