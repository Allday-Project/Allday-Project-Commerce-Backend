package jpa.basic.alldayprojectcommerce.domain.chat.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.CreateChatRoomRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import jpa.basic.alldayprojectcommerce.domain.chat.service.ChatMessageService;
import jpa.basic.alldayprojectcommerce.domain.chat.service.ChatRoomCommandService;
import jpa.basic.alldayprojectcommerce.domain.chat.service.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatMessageService chatMessageService;

    /**
     * 채팅방 생성 또는 기존 활성 방 반환
     *
     * 유저당 활성화된 방 최대 1개만 가능
     */
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody @Valid CreateChatRoomRequest request) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomCommandService.createOrGetActiveRoom(loginUserInfo.id(), request)));
    }

    /**
     * 고객 본인의 활성 채팅방 조회
     *
     * 활성 방이 없으면 null 반환
     * 프론트에서 null 체크 후 "상담원 연결" 버튼 표시 여부 결정
     */
    @GetMapping("/rooms/my")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getMyRoom(
            @LoginUser LoginUserInfo loginUserInfo) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomQueryService.getMyActiveRoom(loginUserInfo.id())));
    }

    /**
     * 채팅방 단건 조회
     *
     * 고객   -> 본인 방만
     * 관리자 -> 모든 방
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoom(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long roomId) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomQueryService.getRoom(roomId, loginUserInfo.id(), loginUserInfo.role())));
    }

    /**
     * 채팅방 메시지 목록 조회
     *
     * cursorId 생략 시 최신 size개 반환
     * 스크롤 업 시 마지막 페이지 id를 cursorId로 전달
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<CursorResponse<ChatMessageResponse>>> getMessages(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "30") int size) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatMessageService.getMessages(roomId, loginUserInfo.id(), loginUserInfo.role(), cursorId, size)));
    }

    /**
     * 채팅방 종료
     *
     * 고객이 종료하거나, 일정 시간이 지나면 자동 종료
     */
    @PostMapping("/rooms/{roomId}/close")
    public ResponseEntity<ApiResponse<Void>> closeRoom(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long roomId) {

        chatRoomCommandService.closeChatRoom(loginUserInfo.id(), roomId, loginUserInfo.role());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    // === 관리자 전용 API ===
    /**
     * 전체 채팅방 목록 조회
     *
     * chatRoomStatus 파라미터로 상태 필터링 (생략 시 전체 조회)
     */
    @GetMapping("/admin/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getAdminRooms(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false) ChatRoomStatus chatRoomStatus) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomQueryService.getAllRooms(chatRoomStatus)));
    }

    /**
     * 채팅방 참여
     *
     * WAITING -> IN_PROGRESS
     */
    @PostMapping("/admin/rooms/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinRoom(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long roomId) {

        chatRoomCommandService.joinChatRoom(loginUserInfo.id(), roomId, loginUserInfo.role());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }
}
