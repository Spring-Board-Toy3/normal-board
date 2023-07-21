package com.boardapplication.controller;

import com.boardapplication.common.core.LoginUserId;
import com.boardapplication.dto.*;
import com.boardapplication.repository.UserRepository;
import com.boardapplication.service.BoardService;
import com.boardapplication.service.CommentService;
import com.boardapplication.service.ReportService;
import com.core.entity.Board;
import com.core.entity.Category;
import com.core.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final ReportService reportService;


    @Value("${file.dir}")
    private String dirPath;


    @GetMapping("/boardList")
    public String list(Model model, @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        UserSessionDto principal = (UserSessionDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("authentication = " + principal.getUsername());

        Page<BoardDto> boardDtoPage = boardService.getBoardList(pageable);

        //Pageable은 0부터 시작
        int nowPage = boardDtoPage.getPageable().getPageNumber();
        // 현재 페이지에서 가장 앞에 페이지 번호를 보여즘
        // -1값이 들어가는 것을 막기 위해 max값으로 두개 값을 넣고 더 큰값을 넣어줌
        int startPage = Math.max(nowPage - 4, 1);
        //현재 페이지에서 가장 뒤에 페이지 번호를 보여줌 페이지 번호가 넘어가버리면 안되기에  min사용
        int endPage = Math.min(startPage + 9, boardDtoPage.getTotalPages());
        model.addAttribute("boardPage", boardDtoPage);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "boardList.html";
    }

    @GetMapping("/boardList/{role}")
    public String listByRole(@PathVariable("role") String role, Model model, @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDto> boardDtoPage = boardService.getBoardListByRole(role, pageable);
        //Pageable은 0부터 시작
        int nowPage = boardDtoPage.getPageable().getPageNumber();
        // 현재 페이지에서 가장 앞에 페이지 번호를 보여즘
        // -1값이 들어가는 것을 막기 위해 max값으로 두개 값을 넣고 더 큰값을 넣어줌
        int startPage = Math.max(nowPage - 4, 1);
        //현재 페이지에서 가장 뒤에 페이지 번호를 보여줌 페이지 번호가 넘어가버리면 안되기에  min사용
        int endPage = Math.min(startPage + 9, boardDtoPage.getTotalPages());
        model.addAttribute("boardPage", boardDtoPage);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "boardList.html";
    }

    @GetMapping("/searchBoardList")
    public String searchBoardList(String searchType, String keyword, Model model, @PageableDefault(page = 0, size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDto> boardDtoPage = boardService.searchBoardList(searchType, keyword, pageable);
        if (boardDtoPage.isEmpty()) {
            model.addAttribute("noResults", true);
        }
        //Pageable은 0부터 시작
        int nowPage = boardDtoPage.getPageable().getPageNumber();
        // 현재 페이지에서 가장 앞에 페이지 번호를 보여즘
        // -1값이 들어가는 것을 막기 위해 max값으로 두개 값을 넣고 더 큰값을 넣어줌
        int startPage = Math.max(nowPage - 4, 1);
        //현재 페이지에서 가장 뒤에 페이지 번호를 보여줌 페이지 번호가 넘어가버리면 안되기에  min사용
        int endPage = Math.min(startPage + 9, boardDtoPage.getTotalPages());
        model.addAttribute("boardPage", boardDtoPage);
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "searchBoardList.html";

    }

    @GetMapping("/board/write")
    public String createBoardView() {
        return "board/write";
    }

    @PostMapping("/board/write")
    public String createBoard(
            @LoginUserId Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) throws IOException {
        CreateBoardRequestDto createBoardRequestDto = CreateBoardRequestDto.builder()
                .title(title)
                .content(content)
                .file(file)
                .build();

        System.out.println(userId);
        Long savedBoardId = boardService.save(userId, createBoardRequestDto, file);
        redirectAttributes.addAttribute("boardId", savedBoardId);
        return "redirect:/board/{boardId}";
    }

    @GetMapping("/board/{boardId}")
    public String getBoardDetailView(@LoginUserId Long userId, Model model, @PathVariable Long boardId, HttpServletResponse response) throws IOException {
        Board board = boardService.getBoardById(boardId);
        model.addAttribute("board", board);
        List<Comment> list = commentService.getAllByBoardId(boardId);
        model.addAttribute("comments", list);
        model.addAttribute("userId", userId);
        return "board/detail";
    }

    @GetMapping(value = "/board/image/{fileName}")
    @ResponseBody
    public byte[] getImage(@PathVariable String fileName) {
        try (InputStream inputStream = new FileInputStream(dirPath + fileName)){

            return inputStream.readAllBytes();
        }catch (Exception e){
            return null;
        }
    }

    @PostMapping("/board/{boardId}/comment")
    public String createComment(@LoginUserId Long userId, @ModelAttribute CommentCreateRequestDto commentCreateRequestDto, @PathVariable Long boardId, RedirectAttributes redirectAttributes) throws IOException {
        Comment comment = commentService.createParent(userId, commentCreateRequestDto.getContent(), boardId);
//        redirectAttributes.addAttribute("comment", comment);
        return "redirect:/board/{boardId}";
    }

    @PostMapping("/board/{boardId}/comment/{commentId}")
    public String createChildrenComment(@LoginUserId Long userId, @ModelAttribute CommentCreateRequestDto commentCreateRequestDto, @PathVariable Long commentId, @PathVariable Long boardId, RedirectAttributes redirectAttributes) throws IOException {
        commentService.createChildren(userId, boardId, commentId, commentCreateRequestDto);
//        redirectAttributes.addAttribute("comment", comment);
        return "redirect:/board/{boardId}";
    }

    @PostMapping("/board/{boardId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId, @PathVariable Long boardId) throws IOException {
        commentService.deleteById(commentId);
//        redirectAttributes.addAttribute("comment", comment);
        return "redirect:/board/{boardId}";
    }


    @GetMapping("/board/{boardId}/update")
    public String updateBoardView(@PathVariable Long boardId, Model model) {
        Board findBoard = boardService.getBoardById(boardId);
        model.addAttribute("board", findBoard);
        return "board/edit";
    }

    @PostMapping("/board/{boardId}/update")
    public String updateBoard(@PathVariable Long boardId, @ModelAttribute UpdateBoardRequestDto updateBoardRequestDto, Model model) {
        boardService.update(boardId, updateBoardRequestDto);
        return "redirect:/board/{boardId}";
    }

    @PostMapping("/board/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId) {
        boardService.deleteById(boardId);
        return "redirect:/boardList";
    }

    @GetMapping("/board/{boardId}/report")
    public String reportBoardView(@PathVariable Long boardId, Model model) {
        model.addAttribute("boardId", boardId);
        model.addAttribute("categories", Category.values());
        return "board/report";
    }

    @PostMapping("/board/{boardId}/report")
    public String reportBoard(@LoginUserId Long userId, @PathVariable Long boardId, Category category, MultipartFile file) throws IOException {
        reportService.create(userId, boardId, file, category);
        return "redirect:/board/{boardId}";
    }
}