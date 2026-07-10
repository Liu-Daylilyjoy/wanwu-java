package com.unicomai.wanwu.api.model;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogDeleteCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelInvokeCommand;
import com.unicomai.wanwu.api.model.dto.ModelInvokeResult;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelStatusCommand;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelUpsertCommand;
import com.unicomai.wanwu.api.model.dto.ProviderListQuery;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelQuery;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;

import java.util.List;

public interface ModelService {

    ServiceDescriptor describe();

    ModelInfo importModel(ModelUpsertCommand command);

    void updateModel(ModelUpsertCommand command);

    void deleteModel(String userId, String orgId, String modelId);

    void changeModelStatus(ModelStatusCommand command);

    ModelInfo getModel(String userId, String orgId, String modelId);

    ModelInvokeResult invokeModel(ModelInvokeCommand command);

    List<String> listModelIdsByUuids(List<String> uuids);

    void checkModelUserPermission(String userId, String orgId, List<String> modelIds);

    ModelListResult listModels(ModelListQuery query);

    ModelListResult listTypeModels(ModelTypeQuery query);

    ProviderModelTypeResult listImportProviders(ProviderListQuery query);

    RecommendModelResult recommendModels(RecommendModelQuery query);

    ModelExperienceDialogInfo saveModelExperienceDialog(ModelExperienceDialogSaveCommand command);

    ModelExperienceDialogListResult listModelExperienceDialogs(ModelExperienceDialogListQuery query);

    void deleteModelExperienceDialog(ModelExperienceDialogDeleteCommand command);

    void saveModelExperienceDialogRecord(ModelExperienceDialogRecordSaveCommand command);

    ModelExperienceDialogRecordListResult listModelExperienceDialogRecords(ModelExperienceDialogRecordQuery query);
}
