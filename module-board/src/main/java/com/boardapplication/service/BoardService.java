package com.boardapplication.service;

import com.boardapplication.dto.BoardDto;
import com.boardapplication.dto.UpdateBoardRequestDto;
import com.boardapplication.repository.BoardRepository;
import com.boardapplication.repository.UserRepository;
import com.boardapplication.dto.CreateBoardRequestDto;
import com.core.entity.Board;
import com.core.entity.Role;
import com.core.entity.Status;
import com.core.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    @Value("${file.dir}")
    private String fileDir;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    @Transactional
    public Page<BoardDto> getBoardList(Pageable pageable){
        Page<Board> boards = boardRepository.findByStatus(Status.NORMAL, pageable);
        List<BoardDto> boardDtoList = new ArrayList<>();
        for(Board board : boards){
            if(board.getStatus().equals(Status.NORMAL)){
                User user = userRepository.findById(board.getUser().getId()).orElse(null);
                String nickname = user != null ? user.getNickname() : null;

                BoardDto dto = BoardDto.builder()
                        .id(board.getId())
                        .user(user)
                        .title(board.getTitle())
                        .content(board.getContent())
                        .thumbnail(board.getThumbnail())
                        .status(board.getStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .userNickname(nickname)
                        .build();
                boardDtoList.add(dto);
            }
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());
    }

    public Page<BoardDto> getBoardListByRole(String role, Pageable pageable) {
        // 사용자 역할(role)에 따라 보여줄 게시판 카테고리를 분기 처리
        if (role.equals("vip")) {
            return getVipBoardList(pageable);
        } else {
            return getNormalBoardList(pageable);
        }
    }

    private Page<BoardDto> getNormalBoardList(Pageable pageable) {
        Page<Board> boards = boardRepository.findByRoleAndStatus(Role.NORMAL, Status.NORMAL, pageable);
        List<BoardDto> boardDtoList = new ArrayList<>();
        for(Board board : boards){
            if(board.getStatus().equals(Status.NORMAL)){
                User user = userRepository.findById(board.getUser().getId()).orElse(null);
                String nickname = user != null ? user.getNickname() : null;

                BoardDto dto = BoardDto.builder()
                        .id(board.getId())
                        .user(user)
                        .title(board.getTitle())
                        .content(board.getContent())
                        .thumbnail(board.getThumbnail())
                        .status(board.getStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .userNickname(nickname)
                        .build();
                boardDtoList.add(dto);
            }
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());

    }

    private Page<BoardDto> getVipBoardList(Pageable pageable) {
        Page<Board> boards = boardRepository.findByRoleAndStatus(Role.VIP, Status.NORMAL, pageable);
        List<BoardDto> boardDtoList = new ArrayList<>();
        for(Board board : boards){
            if(board.getStatus().equals(Status.NORMAL)){
                User user = userRepository.findById(board.getUser().getId()).orElse(null);
                String nickname = user != null ? user.getNickname() : null;

                BoardDto dto = BoardDto.builder()
                        .id(board.getId())
                        .user(user)
                        .title(board.getTitle())
                        .content(board.getContent())
                        .thumbnail(board.getThumbnail())
                        .status(board.getStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .userNickname(nickname)
                        .build();
                boardDtoList.add(dto);
            }
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());
    }

    public Page<BoardDto> searchBoardList(String searchType, String keyword, Pageable pageable){
        Page<Board> boards = null;
        List<BoardDto> boardDtoList = new ArrayList<>();

        if(searchType.equals("boardTitle")){
            boards = boardRepository.findByTitleContainingAndStatus(keyword, Status.NORMAL, pageable);
        }
        else if(searchType.equals("boardWriter")){
            boards = boardRepository.findByNicknameContainingAndStatus(keyword, Status.NORMAL, pageable);
        }
        else {
            boards = boardRepository.findByContentContainingAndStatus(keyword, Status.NORMAL, pageable);
        }
        for(Board board : boards){
            if(board.getStatus().equals(Status.NORMAL)){
                User user = userRepository.findById(board.getUser().getId()).orElse(null);
                String nickname = user != null ? user.getNickname() : null;
                BoardDto dto = BoardDto.builder()
                        .id(board.getId())
                        .user(user)
                        .title(board.getTitle())
                        .content(board.getContent())
                        .thumbnail(board.getThumbnail())
                        .status(board.getStatus())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .userNickname(nickname)
                        .build();
                boardDtoList.add(dto);
            }
        }
        return new PageImpl<>(boardDtoList, pageable, boards.getTotalElements());
    }


    @Transactional
    public Long save(Long userId, CreateBoardRequestDto createBoardRequestDto, MultipartFile file) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다"));

        Board board = null;
        if (file.isEmpty()) {
            board = Board.builder()
                    .title(createBoardRequestDto.getTitle())
                    .content(createBoardRequestDto.getContent())
                    .user(user)
                    .status(Status.NORMAL)
                    .build();
            return boardRepository.save(board).getId();
        }


        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String fullPath = fileDir + fileName;
        file.transferTo(new File(fullPath));
        board = Board.builder()
                .title(createBoardRequestDto.getTitle())
                .content(createBoardRequestDto.getContent())
                .thumbnail(fileName)
                .status(Status.NORMAL)
                .user(user)
                .build();
        return boardRepository.save(board).getId();
    }

    @Transactional(readOnly = true)
    public Board getBoardById(Long boardId) {
        return boardRepository.getReferenceById(boardId);
    }

    public Board update(Long boardId, UpdateBoardRequestDto updateBoardRequestDto) {
        Board findBoard = getBoardById(boardId);
        findBoard.update(updateBoardRequestDto.getTitle(), updateBoardRequestDto.getContent());
        return findBoard;
    }

    public void deleteById(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
