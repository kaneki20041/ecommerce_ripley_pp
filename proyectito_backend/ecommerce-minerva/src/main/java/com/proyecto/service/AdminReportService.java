package com.proyecto.service;

import com.proyecto.response.ReportStatDto;
import java.util.List;

import java.util.Map;

public interface AdminReportService {
    List<ReportStatDto> getReportStats(String type, String fromDate, String toDate);
    List<Map<String, Object>> getReportDetails(String type, String fromDateStr, String toDateStr);
}
