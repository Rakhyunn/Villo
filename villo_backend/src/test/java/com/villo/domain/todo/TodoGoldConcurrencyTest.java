package com.villo.domain.todo;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.todo.entity.Todo;
import com.villo.domain.todo.entity.type.Difficulty;
import com.villo.domain.todo.entity.type.TodoStatus;
import com.villo.domain.todo.repository.TodoRepository;
import com.villo.domain.todo.service.TodoCompletionService;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("투두 완료 동시성 테스트")
public class TodoGoldConcurrencyTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserVillageRepository userVillageRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private TodoCompletionService todoCompletionService;

    private Long userId;
    private List<Long> todoIds;
    private static final int GOLD_PER_TODO = 40;
    private static final int TODO_COUNT = 10;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("concurrency@test.com")
                .provider(Provider.KAKAO)
                .providerId("concurrency-" + System.nanoTime())
                .totalGold(0)
                .dailyGold(0)
                .lastGoldResetDate(LocalDate.now())
                .build());
        userId = user.getId();

        userVillageRepository.save(UserVillage.builder()
                .user(user)
                .villageName("테스트마을")
                .villageLevel(1)
                .build());

        // Todo 10개 생성 (각 40G, 총합 400G — 일일 상한 500G 이내)
        todoIds = new ArrayList<>();
        for (int i = 0; i < TODO_COUNT; i++) {
            Todo todo = todoRepository.save(Todo.builder()
                    .user(user)
                    .title("테스트 Todo " + (i + 1))
                    .category("생활")
                    .difficulty(Difficulty.NORMAL)
                    .gold(GOLD_PER_TODO)
                    .isRepeat(false)
                    .status(TodoStatus.PENDING)
                    .build());
            todoIds.add(todo.getId());
        }
    }

    @Test
    void todoGoldConcurrencyTest() throws InterruptedException {
        int threadCount = todoIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        IntStream.range(0, threadCount).forEach(i -> {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();     // 모든 스레드가 동시에 출발하도록 대기
                    todoCompletionService.completeTodo(userId, todoIds.get(i));
                } catch (Exception e) {
                    System.out.println("완료 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        });

        readyLatch.await();         // 스레드 전부 준비될 때까지 대기
        startLatch.countDown();     // 동시에 출발 신호
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        User after = userRepository.findById(userId).orElseThrow();

        int expectedGoldSum = GOLD_PER_TODO * TODO_COUNT; // 400G 기대
        long completedCount = todoRepository.findAllById(todoIds).stream()
                .filter(todo -> todo.getStatus() == TodoStatus.COMPLETED)
                .count();

        System.out.println("=== 기대 골드 증가량(투두 gold 합): " + expectedGoldSum);
        System.out.println("=== 실제 골드 증가량: " + after.getTotalGold());
        System.out.println("=== dailyGold: " + after.getDailyGold());

        // 검증 1: 10개 투두 모두 완료 처리됐어야 함
        assertThat(completedCount).isEqualTo(TODO_COUNT);

        // 검증 2: 골드가 정확히 기대값(400G)과 일치해야 함
        assertThat(after.getTotalGold()).isEqualTo(expectedGoldSum);
    }
}
