package com.proyecto.service;

import com.proyecto.response.EmployeeDashboardResponse;

public interface EmployeeDashboardService {
    /**
     * @param period  "7d" | "30d" | "90d"
     */
    EmployeeDashboardResponse getDashboardStats(String period);
}
