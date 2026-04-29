package com.yas.system.customer.api;

import com.yas.system.customer.internal.entity.Customer;

public interface CustomerQueryService {

    Customer findByEmail(String email);

}
