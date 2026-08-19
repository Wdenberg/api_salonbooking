package com.company.salonbooking.business.application.usecase;

import com.company.salonbooking.business.application.command.UpdateOpeningHoursCommand;
import com.company.salonbooking.business.domain.exception.BusinessNotFoundException;
import com.company.salonbooking.business.domain.exception.InvalidOpeningHoursException;
import com.company.salonbooking.business.domain.model.Business;
import com.company.salonbooking.business.domain.model.OpeningHourInterval;
import com.company.salonbooking.business.domain.repository.BusinessOpeningHourRepository;
import com.company.salonbooking.business.domain.repository.BusinessRepository;
import com.company.salonbooking.shared.exception.UnauthorizedResourceException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateOpeningHoursUseCase {

    private final BusinessRepository businessRepository;
    private final BusinessOpeningHourRepository openingHourRepository;

    public UpdateOpeningHoursUseCase(BusinessRepository businessRepository, BusinessOpeningHourRepository openingHourRepository) {
        this.businessRepository = businessRepository;
        this.openingHourRepository = openingHourRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = "business-opening-hours", key = "#command.businessId()")
    public List<OpeningHourInterval> execute(UpdateOpeningHoursCommand command) {
        Business business = businessRepository.findById(command.businessId())
                .orElseThrow(() -> new BusinessNotFoundException(command.businessId()));

        if (!business.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedResourceException("You do not own this business.");
        }

        validateNoOverlaps(command.intervals());

        return openingHourRepository.replaceAll(command.businessId(), command.intervals());
    }

    private void validateNoOverlaps(List<OpeningHourInterval> intervals) {
        Map<DayOfWeek, List<OpeningHourInterval>> byDay = intervals.stream()
                .collect(Collectors.groupingBy(OpeningHourInterval::getDayOfWeek));

        for (List<OpeningHourInterval> dayIntervals : byDay.values()) {
            for (int i = 0; i < dayIntervals.size(); i++) {
                for (int j = i + 1; j < dayIntervals.size(); j++) {
                    if (dayIntervals.get(i).getTimeRange().overlaps(dayIntervals.get(j).getTimeRange())) {
                        throw new InvalidOpeningHoursException(
                                "Opening hour intervals overlap on " + dayIntervals.get(i).getDayOfWeek());
                    }
                }
            }
        }
    }
}