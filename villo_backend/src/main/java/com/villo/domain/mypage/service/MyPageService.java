package com.villo.domain.mypage.service;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.mypage.dto.MyCalendarResponse;
import com.villo.domain.mypage.dto.MyDailyTodoResponse;
import com.villo.domain.mypage.dto.MyPageResponse;
import com.villo.domain.mypage.dto.MyStatsResponse;
import com.villo.domain.todo.repository.TodoCompletionRepository;
import com.villo.domain.village.repository.UserVillagePeopleRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;
    private final TodoCompletionRepository todoCompletionRepository;
    private final UserVillagePeopleRepository userVillagePeopleRepository;

    // 프로필 조회
    @Transactional(readOnly = true)
    public MyPageResponse getProfile(Long userId) {
        User user = getUser(userId);
        return MyPageResponse.from(user);
    }

    // 통계 조회
    @Transactional(readOnly = true)
    public MyStatsResponse getStats(Long userId) {
        // 완료 퀘스트 수
        int completedCount = todoCompletionRepository.countByUserId(userId);

        // 연속 달성일
        int consecutiveDays = calculateConsecutiveDays(userId);

        // 보유 주민 수
        int villagerCount = userVillagePeopleRepository.countByUserId(userId);

        return new MyStatsResponse(completedCount, consecutiveDays, villagerCount);
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(Long userId, String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        User user = getUser(userId);
        user.updateNickname(nickname);
    }

    // 월별 완료 날짜 목록 (달력 점 표시용)
    @Transactional(readOnly = true)
    public MyCalendarResponse getCalendar(Long userId, int year, int month) {
        List<LocalDate> dates = todoCompletionRepository
                .findCompletedDatesByYearAndMonth(userId, year, month);

        List<String> dateStrings = dates.stream()
                .map(LocalDate::toString)
                .toList();

        return new MyCalendarResponse(dateStrings);
    }

    // 날짜별 완료 투두 목록
    @Transactional(readOnly = true)
    public List<MyDailyTodoResponse> getDailyTodos(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return todoCompletionRepository
                .findByUserIdAndCompletedDateBetween(userId, start, end)
                .stream()
                .map(MyDailyTodoResponse::from)
                .toList();
    }

    // (공통) 유저 조회
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 연속 달성일 계산
    private int calculateConsecutiveDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime fromDate = today.minusDays(364).atStartOfDay();

        List<LocalDate> completedDates = todoCompletionRepository
                .findCompletedDatesSince(userId, fromDate);

        if (completedDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> completedDateSet = new HashSet<>(completedDates);

        int consecutiveDays = 0;
        LocalDate checkDate = today;

        while (completedDateSet.contains(checkDate)) {
            consecutiveDays++;
            checkDate = checkDate.minusDays(1);
        }

        return consecutiveDays;
    }
}
