package com.example.cinema_client.payment.paypal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cinema_client.constants.Currency;
import com.example.cinema_client.payment.paypal.config.PaypalPaymentIntent;
import com.example.cinema_client.payment.paypal.config.PaypalPaymentMethod;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;

/**
 * @author tritcse00526x
 */
@Service
@RequiredArgsConstructor
public class PaypalService {
    private final APIContext apiContext;

    public Payment createPayment(
            Currency currency, Double total,
            PaypalPaymentMethod method, PaypalPaymentIntent intent,
            String cancelUrl, String returnUrl, String description)
            throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency.toString());
        amount.setTotal(String.format("%.2f", total).replace(",", "."));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method.toString());

        Payment payment = new Payment();
        payment.setIntent(intent.toString());
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(returnUrl);
        payment.setRedirectUrls(redirectUrls);
        return payment.create(apiContext);
    }

    public Payment executePayment(
            String paymentId, String payerId)
            throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecute);
    }
}

