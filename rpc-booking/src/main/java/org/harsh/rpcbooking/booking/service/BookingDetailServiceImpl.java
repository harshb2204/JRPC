package org.harsh.rpcbooking.booking.service;

import com.harsh.common.rpc.booking.BookingDetailService;
import com.harsh.rpc.config.MarkAsRpc;

public class BookingDetailServiceImpl implements BookingDetailService {

    @MarkAsRpc
    @Override
    public String getBookingDetailByUserId(int userId) {
        return "Booking service is called and user id is "+ userId;
    }
}
