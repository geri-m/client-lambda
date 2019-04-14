package at.madlmayr.tools;

import at.madlmayr.ToolCallException;
import at.madlmayr.artifactory.ArtifactoryCall;
import at.madlmayr.jira.JiraV2Call;
import at.madlmayr.slack.SlackCall;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AWSLambdaAsyncMock implements AWSLambdaAsync {

    private final int port;

    public AWSLambdaAsyncMock(int port) {
        this.port = port;
    }


    @Override
    public Future<AddLayerVersionPermissionResult> addLayerVersionPermissionAsync(AddLayerVersionPermissionRequest addLayerVersionPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<AddLayerVersionPermissionResult> addLayerVersionPermissionAsync(AddLayerVersionPermissionRequest addLayerVersionPermissionRequest, AsyncHandler<AddLayerVersionPermissionRequest, AddLayerVersionPermissionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<AddPermissionResult> addPermissionAsync(AddPermissionRequest addPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<AddPermissionResult> addPermissionAsync(AddPermissionRequest addPermissionRequest, AsyncHandler<AddPermissionRequest, AddPermissionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateAliasResult> createAliasAsync(CreateAliasRequest createAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateAliasResult> createAliasAsync(CreateAliasRequest createAliasRequest, AsyncHandler<CreateAliasRequest, CreateAliasResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateEventSourceMappingResult> createEventSourceMappingAsync(CreateEventSourceMappingRequest createEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateEventSourceMappingResult> createEventSourceMappingAsync(CreateEventSourceMappingRequest createEventSourceMappingRequest, AsyncHandler<CreateEventSourceMappingRequest, CreateEventSourceMappingResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateFunctionResult> createFunctionAsync(CreateFunctionRequest createFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<CreateFunctionResult> createFunctionAsync(CreateFunctionRequest createFunctionRequest, AsyncHandler<CreateFunctionRequest, CreateFunctionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteAliasResult> deleteAliasAsync(DeleteAliasRequest deleteAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteAliasResult> deleteAliasAsync(DeleteAliasRequest deleteAliasRequest, AsyncHandler<DeleteAliasRequest, DeleteAliasResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteEventSourceMappingResult> deleteEventSourceMappingAsync(DeleteEventSourceMappingRequest deleteEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteEventSourceMappingResult> deleteEventSourceMappingAsync(DeleteEventSourceMappingRequest deleteEventSourceMappingRequest, AsyncHandler<DeleteEventSourceMappingRequest, DeleteEventSourceMappingResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteFunctionResult> deleteFunctionAsync(DeleteFunctionRequest deleteFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteFunctionResult> deleteFunctionAsync(DeleteFunctionRequest deleteFunctionRequest, AsyncHandler<DeleteFunctionRequest, DeleteFunctionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteFunctionConcurrencyResult> deleteFunctionConcurrencyAsync(DeleteFunctionConcurrencyRequest deleteFunctionConcurrencyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteFunctionConcurrencyResult> deleteFunctionConcurrencyAsync(DeleteFunctionConcurrencyRequest deleteFunctionConcurrencyRequest, AsyncHandler<DeleteFunctionConcurrencyRequest, DeleteFunctionConcurrencyResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteLayerVersionResult> deleteLayerVersionAsync(DeleteLayerVersionRequest deleteLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<DeleteLayerVersionResult> deleteLayerVersionAsync(DeleteLayerVersionRequest deleteLayerVersionRequest, AsyncHandler<DeleteLayerVersionRequest, DeleteLayerVersionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetAccountSettingsResult> getAccountSettingsAsync(GetAccountSettingsRequest getAccountSettingsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetAccountSettingsResult> getAccountSettingsAsync(GetAccountSettingsRequest getAccountSettingsRequest, AsyncHandler<GetAccountSettingsRequest, GetAccountSettingsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetAliasResult> getAliasAsync(GetAliasRequest getAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetAliasResult> getAliasAsync(GetAliasRequest getAliasRequest, AsyncHandler<GetAliasRequest, GetAliasResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetEventSourceMappingResult> getEventSourceMappingAsync(GetEventSourceMappingRequest getEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetEventSourceMappingResult> getEventSourceMappingAsync(GetEventSourceMappingRequest getEventSourceMappingRequest, AsyncHandler<GetEventSourceMappingRequest, GetEventSourceMappingResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetFunctionResult> getFunctionAsync(GetFunctionRequest getFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetFunctionResult> getFunctionAsync(GetFunctionRequest getFunctionRequest, AsyncHandler<GetFunctionRequest, GetFunctionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetFunctionConfigurationResult> getFunctionConfigurationAsync(GetFunctionConfigurationRequest getFunctionConfigurationRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetFunctionConfigurationResult> getFunctionConfigurationAsync(GetFunctionConfigurationRequest getFunctionConfigurationRequest, AsyncHandler<GetFunctionConfigurationRequest, GetFunctionConfigurationResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetLayerVersionResult> getLayerVersionAsync(GetLayerVersionRequest getLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetLayerVersionResult> getLayerVersionAsync(GetLayerVersionRequest getLayerVersionRequest, AsyncHandler<GetLayerVersionRequest, GetLayerVersionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetLayerVersionPolicyResult> getLayerVersionPolicyAsync(GetLayerVersionPolicyRequest getLayerVersionPolicyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetLayerVersionPolicyResult> getLayerVersionPolicyAsync(GetLayerVersionPolicyRequest getLayerVersionPolicyRequest, AsyncHandler<GetLayerVersionPolicyRequest, GetLayerVersionPolicyResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetPolicyResult> getPolicyAsync(GetPolicyRequest getPolicyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<GetPolicyResult> getPolicyAsync(GetPolicyRequest getPolicyRequest, AsyncHandler<GetPolicyRequest, GetPolicyResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<InvokeResult> invokeAsync(InvokeRequest invokeRequest) {

        final RequestStreamHandler lambdaToExecute;

        switch (invokeRequest.getFunctionName()) {
            case "JiraV2Call":
                lambdaToExecute = new JiraV2Call(port);
                break;
            case "SlackCall":
                lambdaToExecute = new SlackCall(port);
                break;
            case "ArtifactoryCall":
                lambdaToExecute = new ArtifactoryCall(port);
                break;
            default:
                throw new ToolCallException(String.format("No function '%s' defined", invokeRequest.getFunctionName()));
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Callable<InvokeResult> callable = new AWSLambdaMockCallable(lambdaToExecute, invokeRequest);
        return executor.submit(callable);
    }

    @Override
    public Future<InvokeResult> invokeAsync(InvokeRequest invokeRequest, AsyncHandler<InvokeRequest, InvokeResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<InvokeAsyncResult> invokeAsyncAsync(InvokeAsyncRequest invokeAsyncRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<InvokeAsyncResult> invokeAsyncAsync(InvokeAsyncRequest invokeAsyncRequest, AsyncHandler<InvokeAsyncRequest, InvokeAsyncResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListAliasesResult> listAliasesAsync(ListAliasesRequest listAliasesRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListAliasesResult> listAliasesAsync(ListAliasesRequest listAliasesRequest, AsyncHandler<ListAliasesRequest, ListAliasesResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListEventSourceMappingsResult> listEventSourceMappingsAsync(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListEventSourceMappingsResult> listEventSourceMappingsAsync(ListEventSourceMappingsRequest listEventSourceMappingsRequest, AsyncHandler<ListEventSourceMappingsRequest, ListEventSourceMappingsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListEventSourceMappingsResult> listEventSourceMappingsAsync() {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListEventSourceMappingsResult> listEventSourceMappingsAsync(AsyncHandler<ListEventSourceMappingsRequest, ListEventSourceMappingsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListFunctionsResult> listFunctionsAsync(ListFunctionsRequest listFunctionsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListFunctionsResult> listFunctionsAsync(ListFunctionsRequest listFunctionsRequest, AsyncHandler<ListFunctionsRequest, ListFunctionsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListFunctionsResult> listFunctionsAsync() {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListFunctionsResult> listFunctionsAsync(AsyncHandler<ListFunctionsRequest, ListFunctionsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListLayerVersionsResult> listLayerVersionsAsync(ListLayerVersionsRequest listLayerVersionsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListLayerVersionsResult> listLayerVersionsAsync(ListLayerVersionsRequest listLayerVersionsRequest, AsyncHandler<ListLayerVersionsRequest, ListLayerVersionsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListLayersResult> listLayersAsync(ListLayersRequest listLayersRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListLayersResult> listLayersAsync(ListLayersRequest listLayersRequest, AsyncHandler<ListLayersRequest, ListLayersResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListTagsResult> listTagsAsync(ListTagsRequest listTagsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListTagsResult> listTagsAsync(ListTagsRequest listTagsRequest, AsyncHandler<ListTagsRequest, ListTagsResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListVersionsByFunctionResult> listVersionsByFunctionAsync(ListVersionsByFunctionRequest listVersionsByFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<ListVersionsByFunctionResult> listVersionsByFunctionAsync(ListVersionsByFunctionRequest listVersionsByFunctionRequest, AsyncHandler<ListVersionsByFunctionRequest, ListVersionsByFunctionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PublishLayerVersionResult> publishLayerVersionAsync(PublishLayerVersionRequest publishLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PublishLayerVersionResult> publishLayerVersionAsync(PublishLayerVersionRequest publishLayerVersionRequest, AsyncHandler<PublishLayerVersionRequest, PublishLayerVersionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PublishVersionResult> publishVersionAsync(PublishVersionRequest publishVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PublishVersionResult> publishVersionAsync(PublishVersionRequest publishVersionRequest, AsyncHandler<PublishVersionRequest, PublishVersionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PutFunctionConcurrencyResult> putFunctionConcurrencyAsync(PutFunctionConcurrencyRequest putFunctionConcurrencyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<PutFunctionConcurrencyResult> putFunctionConcurrencyAsync(PutFunctionConcurrencyRequest putFunctionConcurrencyRequest, AsyncHandler<PutFunctionConcurrencyRequest, PutFunctionConcurrencyResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<RemoveLayerVersionPermissionResult> removeLayerVersionPermissionAsync(RemoveLayerVersionPermissionRequest removeLayerVersionPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<RemoveLayerVersionPermissionResult> removeLayerVersionPermissionAsync(RemoveLayerVersionPermissionRequest removeLayerVersionPermissionRequest, AsyncHandler<RemoveLayerVersionPermissionRequest, RemoveLayerVersionPermissionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<RemovePermissionResult> removePermissionAsync(RemovePermissionRequest removePermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<RemovePermissionResult> removePermissionAsync(RemovePermissionRequest removePermissionRequest, AsyncHandler<RemovePermissionRequest, RemovePermissionResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<TagResourceResult> tagResourceAsync(TagResourceRequest tagResourceRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<TagResourceResult> tagResourceAsync(TagResourceRequest tagResourceRequest, AsyncHandler<TagResourceRequest, TagResourceResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UntagResourceResult> untagResourceAsync(UntagResourceRequest untagResourceRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UntagResourceResult> untagResourceAsync(UntagResourceRequest untagResourceRequest, AsyncHandler<UntagResourceRequest, UntagResourceResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateAliasResult> updateAliasAsync(UpdateAliasRequest updateAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateAliasResult> updateAliasAsync(UpdateAliasRequest updateAliasRequest, AsyncHandler<UpdateAliasRequest, UpdateAliasResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateEventSourceMappingResult> updateEventSourceMappingAsync(UpdateEventSourceMappingRequest updateEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateEventSourceMappingResult> updateEventSourceMappingAsync(UpdateEventSourceMappingRequest updateEventSourceMappingRequest, AsyncHandler<UpdateEventSourceMappingRequest, UpdateEventSourceMappingResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateFunctionCodeResult> updateFunctionCodeAsync(UpdateFunctionCodeRequest updateFunctionCodeRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateFunctionCodeResult> updateFunctionCodeAsync(UpdateFunctionCodeRequest updateFunctionCodeRequest, AsyncHandler<UpdateFunctionCodeRequest, UpdateFunctionCodeResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateFunctionConfigurationResult> updateFunctionConfigurationAsync(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public Future<UpdateFunctionConfigurationResult> updateFunctionConfigurationAsync(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest, AsyncHandler<UpdateFunctionConfigurationRequest, UpdateFunctionConfigurationResult> asyncHandler) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public void setEndpoint(String s) {

    }

    @Override
    public void setRegion(Region region) {

    }

    @Override
    public AddLayerVersionPermissionResult addLayerVersionPermission(AddLayerVersionPermissionRequest addLayerVersionPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public AddPermissionResult addPermission(AddPermissionRequest addPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public CreateAliasResult createAlias(CreateAliasRequest createAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public CreateFunctionResult createFunction(CreateFunctionRequest createFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public DeleteAliasResult deleteAlias(DeleteAliasRequest deleteAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public DeleteEventSourceMappingResult deleteEventSourceMapping(DeleteEventSourceMappingRequest deleteEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public DeleteFunctionResult deleteFunction(DeleteFunctionRequest deleteFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public DeleteFunctionConcurrencyResult deleteFunctionConcurrency(DeleteFunctionConcurrencyRequest deleteFunctionConcurrencyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public DeleteLayerVersionResult deleteLayerVersion(DeleteLayerVersionRequest deleteLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetAccountSettingsResult getAccountSettings(GetAccountSettingsRequest getAccountSettingsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetAliasResult getAlias(GetAliasRequest getAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetEventSourceMappingResult getEventSourceMapping(GetEventSourceMappingRequest getEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetFunctionResult getFunction(GetFunctionRequest getFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetFunctionConfigurationResult getFunctionConfiguration(GetFunctionConfigurationRequest getFunctionConfigurationRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetLayerVersionResult getLayerVersion(GetLayerVersionRequest getLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetLayerVersionPolicyResult getLayerVersionPolicy(GetLayerVersionPolicyRequest getLayerVersionPolicyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public GetPolicyResult getPolicy(GetPolicyRequest getPolicyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public InvokeResult invoke(InvokeRequest invokeRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public InvokeAsyncResult invokeAsync(InvokeAsyncRequest invokeAsyncRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListAliasesResult listAliases(ListAliasesRequest listAliasesRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListEventSourceMappingsResult listEventSourceMappings(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListEventSourceMappingsResult listEventSourceMappings() {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListFunctionsResult listFunctions(ListFunctionsRequest listFunctionsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListFunctionsResult listFunctions() {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListLayerVersionsResult listLayerVersions(ListLayerVersionsRequest listLayerVersionsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListLayersResult listLayers(ListLayersRequest listLayersRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListTagsResult listTags(ListTagsRequest listTagsRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public ListVersionsByFunctionResult listVersionsByFunction(ListVersionsByFunctionRequest listVersionsByFunctionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public PublishLayerVersionResult publishLayerVersion(PublishLayerVersionRequest publishLayerVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public PublishVersionResult publishVersion(PublishVersionRequest publishVersionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public PutFunctionConcurrencyResult putFunctionConcurrency(PutFunctionConcurrencyRequest putFunctionConcurrencyRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public RemoveLayerVersionPermissionResult removeLayerVersionPermission(RemoveLayerVersionPermissionRequest removeLayerVersionPermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public RemovePermissionResult removePermission(RemovePermissionRequest removePermissionRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public TagResourceResult tagResource(TagResourceRequest tagResourceRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public UntagResourceResult untagResource(UntagResourceRequest untagResourceRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public UpdateAliasResult updateAlias(UpdateAliasRequest updateAliasRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public UpdateEventSourceMappingResult updateEventSourceMapping(UpdateEventSourceMappingRequest updateEventSourceMappingRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public UpdateFunctionCodeResult updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }

    @Override
    public void shutdown() {

    }

    @Override
    public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest amazonWebServiceRequest) {
        throw new ToolCallException("Mock - not Implemented");
    }
}
