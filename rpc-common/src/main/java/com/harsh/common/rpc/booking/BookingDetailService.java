package com.harsh.common.rpc.booking;

import com.harsh.rpc.config.RemoteService;

public interface BookingDetailService extends RemoteService {

    //rpc method
    public String getBookingDetailByUserId(int userId);
}
