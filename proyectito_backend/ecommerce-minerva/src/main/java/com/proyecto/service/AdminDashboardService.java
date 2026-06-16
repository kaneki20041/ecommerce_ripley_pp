package com.proyecto.service;

import com.proyecto.response.AdminDashboardResponse;

public interface AdminDashboardService {
    /**
     * @param period  "7d" | "30d" | "90d" | "365d"
     */
    AdminDashboardResponse getDashboardStats(String period);
}
