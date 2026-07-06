package com.villo.domain.mypage;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.mypage.service.MyPageService;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.TodoCompletion;
import com.villo.domain.todo.entity.type.Difficulty;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.TodoCompletionRepository;
import com.villo.domain.todo.repository.TodoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@DisplayName("연속 달성일 계산시 쿼리 개수 확인 테스트")
@Transactional
public class MyPageStatsQueryCountTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private TodoCompletionRepository todoCompletionRepository;
    @Autowired
    private MyPageService myPageService;
    @Autowired
    private EntityManager em;

    private Long userId;
    private static final int STREAK_DAYS = 30;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("streak" + System.nanoTime() + "@test.com")
                .provider(Provider.KAKAO)
                .providerId("streak-" + System.nanoTime())
                .totalGold(0)
                .dailyGold(0)
                .lastGoldResetDate(LocalDate.now())
                .build());
        userId = user.getId();

        // 오늘부터 거꾸로 30일 연속 완료 기록 생성
        for (int i = 0; i < STREAK_DAYS; i++) {
            LocalDate day = LocalDate.now().minusDays(i);

            Todo todo = todoRepository.save(Todo.builder()
                    .user(user)
                    .title("Day " + i + " 투두")
                    .category("생활")
                    .difficulty(Difficulty.NORMAL)
                    .gold(40)
                    .isRepeat(false)
                    .status(TodoStatus.COMPLETED)
                    .build());

            TodoCompletion completion = todoCompletionRepository.save(
                    TodoCompletion.builder()
                            .todo(todo)
                            .user(user)
                            .isCertified(false)
                            .earnedGold(40)
                            .build()
            );

            em.flush();
            em.createNativeQuery("UPDATE todo_completion SET completed_date = ?1 WHERE id = ?2")
                    .setParameter(1, day.atStartOfDay())
                    .setParameter(2, completion.getId())
                    .executeUpdate();
        }
        em.flush();
        em.clear();

        // setUp까지의 트랜잭션을 커밋하고 세션 종료
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 테스트 본문을 위한 새 트랜잭션(= 새 Session) 시작
        TestTransaction.start();
    }

    @Test
    void myPageStatsQueryCountTest() {
        // 통계 조회 1번 호출 → 내부에서 calculateConsecutiveDays 실행됨
        var stats = myPageService.getStats(userId);

        System.out.println("=== 연속 달성일: " + stats.consecutiveDays());
        // 콘솔에 찍히는 Hibernate 쿼리 로그 개수를 확인
    }
}
