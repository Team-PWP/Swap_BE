package team_pwp.swap_be.service.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import team_pwp.swap_be.domain.user.UserCreate;
import team_pwp.swap_be.domain.user.UserUpdate;
import team_pwp.swap_be.dto.user.response.UserInfoResponse;
import team_pwp.swap_be.entity.chat.ChatRoom;
import team_pwp.swap_be.entity.chat.EnterChatRoom;
import team_pwp.swap_be.entity.user.User;
import team_pwp.swap_be.repository.chat.ChatRoomJpaRepository;
import team_pwp.swap_be.repository.chat.EnterChatRoomJpaRepository;
import team_pwp.swap_be.repository.user.UserJpaRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final EnterChatRoomJpaRepository enterChatRoomJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final RestTemplate restTemplate = new RestTemplate();


    public Optional<User> SignIn(UserCreate userInfo) {
        //email로 회원가입이 되어있는지 확인
        Optional<User> user = userJpaRepository.findByEmail(userInfo.getEmail());
        log.info("user: {}", user);
        //회원가입이 되어있지 않다면 회원가입 진행
        if (!user.isPresent()) {
            User newUser = User.createUser(userInfo);
            userJpaRepository.save(newUser);
            log.info("회원가입 완료");
            //6번 채팅방에 입장
            ChatRoom chatRoom = chatRoomJpaRepository.findById(6L)
                .orElseThrow(() -> new IllegalArgumentException("전체 채팅방 정보 조회 실패"));
            EnterChatRoom enterChatRoom = EnterChatRoom.create(chatRoom, newUser);
            enterChatRoomJpaRepository.save(enterChatRoom);

            return userJpaRepository.findByEmail(userInfo.getEmail());
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(long userId) {
        return UserInfoResponse.from(userJpaRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 정보 조회 실패")));
    }

    public UserInfoResponse modifyNickname(long userId, UserUpdate userUpdate) {
        User user = userJpaRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저 정보 조회 실패"));
        user.updateUser(userUpdate);
        return UserInfoResponse.from(user);
    }
}
