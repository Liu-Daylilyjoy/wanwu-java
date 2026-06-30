package com.unicomai.wanwu.api.model;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelStatusCommand;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelUpsertCommand;
import com.unicomai.wanwu.api.model.dto.ProviderListQuery;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelQuery;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;

public interface ModelService {

    ServiceDescriptor describe();

    ModelInfo importModel(ModelUpsertCommand command);

    void updateModel(ModelUpsertCommand command);

    void deleteModel(String userId, String orgId, String modelId);

    void changeModelStatus(ModelStatusCommand command);

    ModelInfo getModel(String userId, String orgId, String modelId);

    ModelListResult listModels(ModelListQuery query);

    ModelListResult listTypeModels(ModelTypeQuery query);

    ProviderModelTypeResult listImportProviders(ProviderListQuery query);

    RecommendModelResult recommendModels(RecommendModelQuery query);
}
