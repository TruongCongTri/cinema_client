package com.example.cinema_client.controllers.client;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.cinema_client.constants.PaymentConstants;
import com.example.cinema_client.models.JwtResponseDTO;
import com.mservice.config.Environment;
import com.mservice.enums.RequestType;
import com.mservice.models.PaymentResponse;
import com.mservice.processor.CreateOrderMoMo;
import com.mservice.shared.utils.LogUtils;

import static com.example.cinema_client.constants.ExpiryBookingTime.EXPIRY_BOOKING_TIME;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/payment/momo")
public class MomoPaymentController {
    @Qualifier("bookingSeatStore")
    @Autowired
    Map<Integer, Map<Integer, LocalDateTime>> bookingSeatStore;

    /**TODO: MOMO page*/
    @GetMapping("/{orderId}")
    public String displayMomoPage(
            @PathVariable(name = "orderId") String orderId,
            @RequestParam long amount,
            HttpServletRequest request, Model model) {
        System.out.println("LOG: accessing momo payment method page");
        LogUtils.init();
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String[] objects = orderId.split("P");
        Integer scheduleId = Integer.parseInt(objects[0]);
        // convert list of seat ids to list of integer
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
                /*model.addAttribute("errorMsg",
                        "Ghế đã có người đặt thành công, vui lòng chọn ghế khác!");
                model.addAttribute("errorStatus", 1000);
                return "errorPage";*/
                session.setAttribute("bookedError", message);
                return "redirect:/booking/"+scheduleId;
            }
            bookingSeatStore.get(scheduleId).put(seatId, expiredDateTime);
        }

        //
        String requestId = String.valueOf(System.currentTimeMillis());

        session.setAttribute("orderId", orderId);
        session.setAttribute("requestId", requestId);

        Environment environment = Environment.selectEnv("dev");

        String orderInfo = "Payment for buying movie ticket with Momo";
        String errorMessage = "Đã xảy ra lỗi không thể thanh toán bằng Momo!";

        try {
            PaymentResponse captureATMMoMoResponse = CreateOrderMoMo.process(
                    environment,
                    orderId,
                    requestId,
                    Long.toString(amount),
                    orderInfo,
                    PaymentConstants.REDIRECT_PAYMENT,
                    PaymentConstants.NOTIFY_PAYMENT,
                    "",
                    RequestType.CAPTURE_WALLET,
                    true);
            if (captureATMMoMoResponse.getResultCode() != 0) {
                System.out.println("Error payment");
                throw new Exception();
            } else {
                System.out.println("payment");
                return "redirect:" + captureATMMoMoResponse.getPayUrl();
            }
        } catch (Exception e) {
            session.setAttribute("bookedError", errorMessage);
            e.printStackTrace();
        }

        return "redirect:/booking/" + scheduleId;
    }
}
