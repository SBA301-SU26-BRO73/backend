package com.sba301.backend.service.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.dto.request.CreatePaymentRequest;
import com.sba301.backend.dto.response.PaymentResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Payment;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.PaymentMapper;
import com.sba301.backend.repository.BookingRepository;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.PaymentRepository;
import com.sba301.backend.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final PaymentMapper paymentMapper;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        Branch branch = getBranch(request.getBranchId());
        List<Booking> bookings = getAndValidateBookings(request.getBookingIds(), branch.getId());

        Payment payment = new Payment();
        payment.setBranch(branch);
        payment.setAmount(request.getAmount());
        payment.setBillImageUrl(uploadBillImage(request.getBillImage()));

        Payment savedPayment = paymentRepository.save(payment);
        bookings.forEach(booking -> {
            booking.setPayment(savedPayment);
            booking.setStatus(BookingStatus.AWAITING_CONFIRMATION);
        });
        bookingRepository.saveAll(bookings);
        savedPayment.setBookings(bookings);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getById(Long id) {
        return paymentMapper.toResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id)));
    }

    @Override
    public Page<PaymentResponse> getAll(Long branchId, Pageable pageable) {
        Page<Payment> payments = branchId == null
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findAllByBranchId(branchId, pageable);
        return payments.map(paymentMapper::toResponse);
    }

    private Branch getBranch(Long branchId) {
        return branchRepository.findByIdAndStatusAndDeletedAtIsNull(branchId, BranchStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
    }

    private List<Booking> getAndValidateBookings(List<Long> bookingIds, Long branchId) {
        Set<Long> distinctIds = new HashSet<>(bookingIds);
        if (distinctIds.size() != bookingIds.size()) {
            throw new BadRequestException("Booking ids must not contain duplicates");
        }

        List<Booking> bookings = bookingRepository.findAllByIdIn(distinctIds);
        if (bookings.size() != distinctIds.size()) {
            throw new ResourceNotFoundException("One or more bookings were not found");
        }

        for (Booking booking : bookings) {
            if (!booking.getCourt().getBranch().getId().equals(branchId)) {
                throw new BadRequestException("All bookings must belong to the payment branch");
            }
            if (booking.getPayment() != null) {
                throw new BadRequestException("Booking " + booking.getId() + " already has a payment");
            }
            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
                throw new BadRequestException(
                        "Booking " + booking.getId() + " is not pending payment");
            }
        }
        return bookings;
    }

    private String uploadBillImage(MultipartFile image) {
        if (image.isEmpty() || image.getContentType() == null
                || !image.getContentType().startsWith("image/")) {
            throw new BadRequestException("Bill image must be a non-empty image file");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap("folder", "payments/bills", "resource_type", "image"));
            return result.get("secure_url").toString();
        } catch (IOException exception) {
            throw new BadRequestException("Could not upload bill image");
        }
    }
}
