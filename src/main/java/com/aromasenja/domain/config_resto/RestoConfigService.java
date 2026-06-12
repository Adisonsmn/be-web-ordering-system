package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.dto.UpdateRestoConfigRequest;

public interface RestoConfigService {
    RestoConfigResponse getConfig();
    RestoConfigResponse updateConfig(UpdateRestoConfigRequest request);
}
