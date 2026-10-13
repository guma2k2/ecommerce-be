package com.yas.system.payment.internal.gateway;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayResolver {

    private final Map<PaymentMethod, PaymentGateway> gatewayMap;

    public PaymentGatewayResolver(List<PaymentGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(PaymentGateway::getPaymentMethod, Function.identity()));
    }

    public PaymentGateway resolve(PaymentMethod method) {
        if (method == null) {
            throw new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, "null");
        }
        PaymentGateway gateway = gatewayMap.get(method);
        if (gateway == null) {
            throw new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, method);
        }
        return gateway;
    }
}
