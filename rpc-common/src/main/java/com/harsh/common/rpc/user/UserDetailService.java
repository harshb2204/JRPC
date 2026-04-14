package com.harsh.common.rpc.user;

import com.harsh.rpc.config.RemoteService;

public interface UserDetailService extends RemoteService {
    public String getUserDetails(int userId);
}
