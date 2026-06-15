package com.sba301.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.dto.request.CreatePaymentRequest;
import com.sba301.backend.dto.response.PaymentResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.Payment;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.PaymentMapper;
import com.sba301.backend.repository.BookingRepository;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Branch branch;
    private Booking firstBooking;
    private Booking secondBooking;
    private CreatePaymentRequest request;

    @BeforeEach
    void setUp() {
        branch = new Branch();
        branch.setId(1L);
        branch.setName("Branch 1");

        Court court = new Court();
        court.setId(10L);
        court.setBranch(branch);

        firstBooking = booking(100L, court);
        secondBooking = booking(101L, court);

        request = CreatePaymentRequest.builder()
                .branchId(branch.getId())
                .bookingIds(List.of(firstBooking.getId(), secondBooking.getId()))
                .amount(new BigDecimal("300000"))
                .billImage(new MockMultipartFile(
                        "billImage", "bill.png", "image/png", new byte[] { 1, 2, 3 }))
                .build();
    }

    @Test
    void createUploadsImageAndAttachesManyBookingsToOnePayment() throws Exception {
        PaymentResponse response = PaymentResponse.builder().id(5L).build();
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(bookingRepository.findAllByIdIn(any())).thenReturn(List.of(firstBooking, secondBooking));
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://cloudinary.example/bill.png"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(5L);
            return payment;
        });
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(response);

        PaymentResponse result = paymentService.create(request);

        assertSame(response, result);
        assertEquals(firstBooking.getPayment(), secondBooking.getPayment());
        assertEquals(BookingStatus.AWAITING_CONFIRMATION, firstBooking.getStatus());
        assertEquals(BookingStatus.AWAITING_CONFIRMATION, secondBooking.getStatus());
        assertEquals("https://cloudinary.example/bill.png", firstBooking.getPayment().getBillImageUrl());
        assertEquals(List.of(firstBooking, secondBooking), firstBooking.getPayment().getBookings());
        verify(bookingRepository).saveAll(List.of(firstBooking, secondBooking));
    }

    @Test
    void createRejectsBookingsFromAnotherBranchBeforeUploading() {
        Branch anotherBranch = new Branch();
        anotherBranch.setId(2L);
        Court anotherCourt = new Court();
        anotherCourt.setBranch(anotherBranch);
        secondBooking.setCourt(anotherCourt);

        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(bookingRepository.findAllByIdIn(any())).thenReturn(List.of(firstBooking, secondBooking));

        assertThrows(BadRequestException.class, () -> paymentService.create(request));

        verify(cloudinary, never()).uploader();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsAlreadyPaidBookingBeforeUploading() {
        firstBooking.setPayment(new Payment());
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(bookingRepository.findAllByIdIn(any())).thenReturn(List.of(firstBooking, secondBooking));

        assertThrows(BadRequestException.class, () -> paymentService.create(request));

        verify(cloudinary, never()).uploader();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingBookingBeforeUploading() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(bookingRepository.findAllByIdIn(any())).thenReturn(List.of(firstBooking));

        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(request));

        verify(cloudinary, never()).uploader();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createRejectsNonImageFile() {
        request.setBillImage(new MockMultipartFile(
                "billImage", "bill.txt", "text/plain", new byte[] { 1 }));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(bookingRepository.findAllByIdIn(any())).thenReturn(List.of(firstBooking, secondBooking));

        assertThrows(BadRequestException.class, () -> paymentService.create(request));

        verify(cloudinary, never()).uploader();
        verify(paymentRepository, never()).save(any());
    }

    private Booking booking(Long id, Court court) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setCourt(court);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        return booking;
    }
}
