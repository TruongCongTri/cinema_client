package com.example.cinema_client.controllers.client;

import com.example.cinema_client.constants.PaymentConstants;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.payment.vnpay.service.VNPayService;
import com.mservice.shared.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.cinema_client.constants.ExpiryBookingTime.EXPIRY_BOOKING_TIME;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/payment/vnpay")
public class VNPayPaymentController {
    @Autowired
    private VNPayService vnPayService;

    @Qualifier("bookingSeatStore")
    @Autowired
    Map<Integer, Map<Integer, LocalDateTime>> bookingSeatStore;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{orderId}")
    public String displayRoomAndSeat(
            @PathVariable(name = "orderId") String orderId,
            @RequestParam long amount,
            HttpServletRequest request, Model model) {
        System.out.println("LOG: accessing vnpay payment method page");
        LogUtils.init();
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // split orderId - get first string - before P
        String[] objects = orderId.split("P");
        // get schedule ID
        Integer scheduleId = Integer.parseInt(objects[0]);

        // convert list of seat ids to list of integer
        // split orderId - get second string - after P
        // each seatId is seperated by regex -
        List<Integer> listSeatIds = Arrays
                .stream(objects[1].split("T")[0].split("-"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        // set seats holding time
        LocalDateTime expiredDateTime = LocalDateTime.now().plusMinutes(EXPIRY_BOOKING_TIME);
        bookingSeatStore.putIfAbsent(Integer.parseInt(objects[0]), new HashMap<>());

        String message = "Ghế đã được đặt, vui lòng chọn lại ghế khác!";
        for (Integer seatId : listSeatIds) {
            if (bookingSeatStore.get(scheduleId).containsKey(seatId)
                    && bookingSeatStore.get(scheduleId).get(seatId).compareTo(now) > 0) { // check if selected seat is unavailable
                System.out.println("FAIL: Pay via VNPay - occupied seat");
                session.setAttribute("bookedError", message);
                return "redirect:/booking/"+scheduleId;
            }
            bookingSeatStore.get(scheduleId).put(seatId, expiredDateTime);
        }

        String orderInfo = "Payment for buying movie ticket with VNPay";
        String errorMessage = "Đã xảy ra lỗi không thể thanh toán bằng VNPay!";

        try {
            int amountInInt = Long.valueOf(amount).intValue();
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createOrder(amountInInt, orderId, baseUrl);

            return "redirect:" + vnpayUrl;
        } catch (Exception e) {
            session.setAttribute("bookedError", errorMessage);
            e.printStackTrace();
        }

        return "redirect:/booking/" + scheduleId;
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(
            HttpServletRequest request, Model model){
        System.out.println("LOG: verifying VNPay payment");
        int paymentStatus = vnPayService.orderReturn(request);
        String orderId = request.getParameter("vnp_OrderInfo");
        String totalPrice = request.getParameter("vnp_Amount");
        Integer amountInInt = Integer.valueOf(totalPrice);
        String returnUrl = PaymentConstants.REDIRECT_PAYMENT + "?orderId=" + orderId + "&amount=" + amountInInt + "&resultCode=0";
        String cancelUrl = PaymentConstants.REDIRECT_PAYMENT + "?orderId=" + orderId + "&amount=" + amountInInt + "&resultCode=1";

        if (paymentStatus == 1) {
            try {
                System.out.println("SUCCESS: Pay via VNPay - process to create bill");
                return "redirect:" + returnUrl;
            } catch (Exception e) {
                System.out.println("FAIL: Pay via VNPay - unsuccessful payment");
                throw new RuntimeException(e);
            }
        }

        System.out.println("FAIL: Pay via VNPay - unsuccessful payment");
        return "redirect:" + cancelUrl;
    }
}
