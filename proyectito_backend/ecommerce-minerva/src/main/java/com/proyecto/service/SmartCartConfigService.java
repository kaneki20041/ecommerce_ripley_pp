package com.proyecto.service;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.SmartCartConfig;
import com.proyecto.request.SmartCartConfigRequest;

public interface SmartCartConfigService {
    SmartCartConfig getConfig();
    SmartCartConfig updateConfig(SmartCartConfigRequest req) throws ProductException;
}
