package com.sba301.backend.service.impl;

import org.springframework.stereotype.Service;

import com.sba301.backend.entity.Court;
import com.sba301.backend.service.CourtPricingService;

@Service
public class CourtPricingServiceImpl implements CourtPricingService {

    @Override
    public void applyDefaultPricing(Court court) {
        // TODO: Create default pricing from the selected court type when pricing rules are available.
    }
}
