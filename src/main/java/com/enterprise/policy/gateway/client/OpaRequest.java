package com.enterprise.policy.gateway.client;

import java.util.Map;

public record OpaRequest(Map<String, Object> input) {}
