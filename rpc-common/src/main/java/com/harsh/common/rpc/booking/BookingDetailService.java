package com.harsh.common.rpc.booking;

import com.harsh.rpc.config.RemoteService;

public interface BookingDetailService extends RemoteService {

    // RPC method
    public String getBookingDetailsByUserId(int userId);

    // Not RPC method
    public String getBookingInformationByUserId(int userId);
}

// This is the contract — the shared agreement
// between the consumer (rpc-user) and the producer (rpc-booking)
// about what booking-related operations exist.