package com.harsh.rpc.config;

// Base Marker interface for all Rpc services
public interface RemoteService {
}
// When the framework scans packages looking for RPC services, this is the
// filter. If a class implements (or extends) RemoteService,
// it gets picked up. If not, it's ignored.