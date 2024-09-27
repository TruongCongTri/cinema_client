package com.example.cinema_client.controllers.client;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.example.cinema_client.models.JwtResponseDTO;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.constants.Currency;
import com.example.cinema_client.constants.PaymentConstants;
import com.example.cinema_client.payment.paypal.config.PaypalPaymentIntent;
import com.example.cinema_client.payment.paypal.config.PaypalPaymentMethod;
import com.example.cinema_client.payment.paypal.service.PaypalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import static com.example.cinema_client.constants.ExpiryBookingTime.EXPIRY_BOOKING_TIME;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/payment/paypal")
public class PaypalPaymentController {
    @Autowired
    private PaypalService paypalService;

    @Qualifier("bookingSeatStore")
    @Autowired
    Map<Integer, Map<Integer, LocalDateTime>> bookingSeatStore;
    @Autowired
    private RestTemplate restTemplate;
    public static String API_EXCHANGE_CURRENCY = API.baseURL+"/api/currency/vnd";

    @GetMapping("/{orderId}")
    public String displayRoomAndSeat(
            @PathVariable(name = "orderId") String orderId,
            @RequestParam long amount,
            HttpServletRequest request, Model model) {
        System.out.println("LOG: accessing paypal payment method page");
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
                /*model.addAttribute("errorMsg",
                        "Ghế đã có người đặt thành công, vui lòng chọn ghế khác!");
                model.addAttribute("errorStatus", 1000);
                return "errorPage";*/
                System.out.println("FAIL: Pay via PayPal - occupied seat");
                session.setAttribute("bookedError", message);
                return "redirect:/booking/"+scheduleId;
            }
            bookingSeatStore.get(scheduleId).put(seatId, expiredDateTime);
        }

        // convert amount from USD to VND based on exchange rate
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("currency", "USD");
        listRequestParam.put("vndMoney", String.valueOf(amount));
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_EXCHANGE_CURRENCY)
                .queryParam("vndMoney", "{vndMoney}")
                .queryParam("currency", "{currency}")
                .encode().toUriString();

        ResponseEntity<Double> response = restTemplate
                .exchange(urlTemplate, HttpMethod.GET,entity,Double.class,listRequestParam);
        Double total = response.getBody();
        System.out.println(total);

        String orderInfo = "Payment for buying movie ticket with Paypal";
        String errorMessage = "Đã xảy ra lỗi không thể thanh toán bằng Paypal!";

        try {
            Payment payment = paypalService.createPayment(
                    Currency.USD,
                    total,
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    PaymentConstants.REDIRECT_PAYMENT + "?orderId=" + orderId + "&amount=" + amount + "&resultCode=2",
                    PaymentConstants.REDIRECT_PAYMENT + "?orderId=" + orderId + "&amount=" + amount + "&resultCode=0",
                    orderInfo);

            return "redirect:" + payment.getLinks()
                    .stream()
                    .filter(e -> e.getRel().equalsIgnoreCase("approval_url"))
                    .findAny()
                    .get()
                    .getHref()
                    + "&orderId=" + orderId;
        } catch (PayPalRESTException e) {
            System.out.println("FAIL: Pay via PayPal - unsuccessful payment");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("FAIL: Pay via PayPal - unsuccessful payment");
            session.setAttribute("bookedError", errorMessage);
            e.printStackTrace();
        }

        return "redirect:/booking/"+scheduleId;
    }
}
