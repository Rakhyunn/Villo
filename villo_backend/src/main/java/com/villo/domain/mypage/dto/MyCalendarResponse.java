package com.villo.domain.mypage.dto;

import java.util.List;

public record MyCalendarResponse(
        List<String> completedDates
) {
}
