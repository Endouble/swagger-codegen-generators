package io.swagger.codegen.languages.php;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.CodegenHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class CodeCeptionCodegen extends AbstractPhpCodegen {
    @SuppressWarnings("hiding")
    protected String apiVersion = "1.0.0";
    private final String BEGIN_SPACE = "\n\t\t\t\t\t";
    private final String END_SPACE = "\n\t\t\t\t";
    private final String[] DIRECTORIES = {"/" + CODECEPTION_DIRECTORY + "/_data", "/" +
            CODECEPTION_DIRECTORY + "/_output"};
    private final String REMOVE_CONTENT_TYPE = "RemoveContentType";
    private final String FAKER_CALL = "$this->faker->";
    private final String HTTP_METHOD_FOR_METHOD_NOT_ALLOWED = "PATCH";

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "php-codeception";
    }

    @Override
    public String getArgumentsLocation() {
        return "";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a CodeCeption library.";
    }

    public CodeCeptionCodegen() {
        super();
        acceptanceTemplateFiles.put("acceptance.mustache", ".php");
        embeddedTemplateDir = templateDir = "v2/CodeCeption";

        /*
         * packPath
         */
        packagePath = "";
        srcBasePath = "";

        // template files want to be ignored
        apiTemplateFiles.clear();
        modelTemplateFiles.clear();
        apiTestTemplateFiles.clear();
        apiDocTemplateFiles.clear();
        modelDocTemplateFiles.clear();

        supportingFiles.add(new SupportingFile(
                "composer.mustache", packagePath + File.separator + srcBasePath,
                "composer.json")
        );
        supportingFiles.add(
                new SupportingFile(
                        "README.mustache", packagePath + File.separator + srcBasePath,
                        "README.md"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "codeception.mustache", packagePath + File.separator + srcBasePath +
                        File.separator, "codeception.yml"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "testcase.mustache", packagePath + File.separator + srcBasePath +
                        File.separator + CODECEPTION_DIRECTORY + File.separator + "acceptance",
                        "TestCase.php"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "acceptance.suite.mustache", packagePath + File.separator + srcBasePath +
                        File.separator + CODECEPTION_DIRECTORY + File.separator, "acceptance.suite.yml"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "acceptanceTester.mustache", packagePath + File.separator + srcBasePath +
                        File.separator + CODECEPTION_DIRECTORY + File.separator + "_support",
                        "AcceptanceTester.php"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "acceptanceHelper.mustache", packagePath + File.separator + srcBasePath +
                        File.separator + CODECEPTION_DIRECTORY + File.separator + "_support" + File.separator +
                        "Helper" + File.separator, "Acceptance.php"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "acceptanceTesterActions.mustache", packagePath + File.separator +
                        srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator + "_support" +
                        File.separator + "_generated" + File.separator, "AcceptanceTesterActions.php"
                )
        );
        supportingFiles.add(
                new SupportingFile(
                        "image.png", packagePath + File.separator + srcBasePath + File.separator +
                        CODECEPTION_DIRECTORY + File.separator + "_data", "image.png"
                )
        );
    }

    /**
     * Change operations values to CodeCeption format.
     *
     * @param objs Map<String, Object>
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        List<CodegenOperation> extraResponseOperations = new ArrayList<CodegenOperation>();
        for (String directory : DIRECTORIES) {
            if (!new File(this.outputFolder + directory).isDirectory()) {
                new File(this.outputFolder + directory).mkdirs();
            }
        }

        objs = super.postProcessOperations(objs);
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
        ops = sortByHttpMethod(ops);

        int operationCounter = 0;
        Paths paths = this.openAPI.getPaths();

        for (CodegenOperation operation : ops) {
            ArrayList<Integer> extraResponses = new ArrayList<>();
            String path = operation.path;
            PathItem pathItem = paths.get(path);
            if (operation.pathParams.size() > 0) {
                for (CodegenParameter pathParam : operation.pathParams) {
                    setParamExample(pathParam);
                    path = path.replace("{" + pathParam.baseName + "}", "$" + pathParam.baseName);
                }
            }
            if (operation.queryParams.size() > 0) {
                for (int i = 0; i < operation.queryParams.size(); i++) {
                    if (i == 0) {
                        path += "?";
                    } else {
                        path += "&";
                    }
                    setParamExample(operation.queryParams.get(i));
                    path += operation.queryParams.get(i).baseName + "=$" + operation.queryParams.get(i).baseName;
                    ;

                    if (operation.getQueryParams().get(i).getItems() != null) {
                        if (operation.getQueryParams().get(i).getItems().get_enum() != null) {
                            List<String> enumValues = operation.getQueryParams().get(i).getItems().get_enum();
                            List<String> enumNewValues = new ArrayList();
                            for (String enumValue : enumValues) {
                                String newEnumValue = "\"" + enumValue + "\"";
                                enumNewValues.add(newEnumValue);
                            }
                            operation.getQueryParams().get(i).getItems().set_enum(enumNewValues);
                        }
                    }
                }
            }

            CodegenOperation updateOperation = operation;
            updateOperation.resolvedPath = StringEscapeUtils.unescapeHtml4(path);

            List<CodegenResponse> responses = updateOperation.responses;
            for (CodegenResponse response : responses) {
                response.httpDescription = CodegenHelper.getHTTPDescription(Integer.parseInt(response.getCode()));
            }

            updateOperation.codeCeptionRequestBody = createRequestBody(pathItem, operation, updateOperation);
            updateOperation.codeCeptionResponse = createJsonResponse(responses, extraResponses);

            if (updateOperation.contentType == null) {
                updateOperation.contentType = "application/json";
                updateOperation.returnJsonEncoded = true;
            }
            if (updateOperation.contentType.equals(REMOVE_CONTENT_TYPE)) {
                updateOperation.contentType = null;
                updateOperation.returnJsonEncoded = false;
            }
            ops.set(operationCounter, updateOperation);

            for (Integer extraResponse : extraResponses) {
                if(
                extraResponse.equals(400)
                    &&
                    (
                        !updateOperation.httpMethod.equals("POST")
                        && !updateOperation.httpMethod.equals("PUT")
                    )
                ) {

                } else {
                    CodegenOperation extraOperation = null;
                    try {
                        extraOperation = (CodegenOperation) updateOperation.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    extraOperation.operationId = extraResponse + extraOperation.operationId;
                    extraResponseOperations.add(extraOperation);
                }
            }

            operationCounter++;
        }

        addOperationForExtraResponse(ops, extraResponseOperations);

        operations.put("operation", sortByHttpMethod(ops));
        return objs;
    }

    /**
     * Convert schemas of requestBody to CodeCeption requestBody.
     *
     * @param pathItem PathItem
     * @param operation CodegenOperation
     * @param updateOperation CodegenOperation
     * @return String
     */
    public String createRequestBody(PathItem pathItem, CodegenOperation operation, CodegenOperation updateOperation) {
        StringBuilder requestBodyJson = new StringBuilder("");
        if (pathItem != null) {
            Operation postOperation = pathItem.getPut();
            if (operation.httpMethod.equals("POST")) {
                postOperation = pathItem.getPost();
            }
            if (postOperation != null) {
                if (postOperation.getRequestBody().getContent() != null) {
                    updateOperation.contentType = postOperation.getRequestBody().getContent().keySet().iterator().next();
                    updateOperation.returnJsonEncoded = true;
                }
                if (postOperation.getOperationId().equals(operation.getOperationId())) {
                    RequestBody requestBody = postOperation.getRequestBody();
                    String $ref = "";
                    Schema openApiRequestBodySchema = null;
                    if (postOperation.getRequestBody().get$ref() != null) {
                        $ref = postOperation.getRequestBody().get$ref();
                        while ($ref != null) {
                            if ($ref.contains("requestBodies")) {
                                $ref = super.getSimpleRef($ref);
                                openApiRequestBodySchema = super.openAPI.getComponents().getRequestBodies().get($ref).
                                        getContent().get("application/json").getSchema();
                                $ref = super.openAPI.getComponents().getRequestBodies().get($ref).
                                        getContent().get("application/json").getSchema().get$ref();
                            } else {
                                $ref = super.getSimpleRef($ref);
                                openApiRequestBodySchema = super.openAPI.getComponents().getSchemas().get($ref);
                                $ref = super.openAPI.getComponents().getSchemas().get($ref).get$ref();
                            }
                        }
                    } else if (
                            postOperation.getRequestBody().getContent().get(postOperation.getRequestBody().getContent()
                                    .keySet().iterator().next()).getSchema().get$ref() != null
                    ) {
                        $ref = postOperation.getRequestBody().getContent().get(postOperation.getRequestBody()
                                .getContent().keySet().iterator().next()).getSchema().get$ref();
                        while ($ref != null) {
                            if ($ref.contains("requestBodies")) {
                                $ref = super.getSimpleRef($ref);
                                openApiRequestBodySchema = super.openAPI.getComponents().getRequestBodies().get($ref).
                                        getContent().get("application/json").getSchema();
                                $ref = super.openAPI.getComponents().getRequestBodies().get($ref).
                                        getContent().get("application/json").getSchema().get$ref();
                            } else {
                                $ref = super.getSimpleRef($ref);
                                openApiRequestBodySchema = super.openAPI.getComponents().getSchemas().get($ref);
                                $ref = super.openAPI.getComponents().getSchemas().get($ref).get$ref();
                            }
                        }
                    } else {
                        openApiRequestBodySchema = postOperation.getRequestBody().getContent().get(postOperation
                                .getRequestBody().getContent().keySet().iterator().next()).getSchema();
                    }

                    if ($ref == null || $ref.isEmpty()) {
                        if (openApiRequestBodySchema != null) {
                            if (openApiRequestBodySchema.getFormat() != null) {
                                if (
                                    openApiRequestBodySchema.getFormat().equals("binary")
                                    || openApiRequestBodySchema.getFormat().equals("byte")
                                ) {
                                    updateOperation.contentType = REMOVE_CONTENT_TYPE;
                                }
                            }
                            if (openApiRequestBodySchema.getProperties() != null) {
                                Schema schema = openApiRequestBodySchema;
                                Map<String, Schema> properties = schema.getProperties();
                                Set<String> propertiesKeys = properties.keySet();
                                int counter = 0;
                                for (String key : propertiesKeys) {
                                    String property$ref = properties.get(key).get$ref();
                                    if (property$ref != null) {
                                        requestBodyJson.append("'" + key + "' => ");
                                        printProperties(
                                            property$ref, key, requestBodyJson, postOperation, false,
                                            BEGIN_SPACE, END_SPACE
                                        );
                                    } else if (properties.get(key) instanceof ArraySchema) {
                                        printArrayProperties(
                                            properties.get(key), key, requestBodyJson, postOperation,
                                            false, BEGIN_SPACE, END_SPACE, true
                                        );
                                    } else if (properties.get(key).getExample() != null) {
                                        requestBodyJson.append(
                                            "'" + key + "' => '" + properties.get(key).getExample() + "'"
                                        );
                                    } else {
                                        String fakerMethod = CodegenHelper.getFakerMethod(
                                            FAKER_CALL,
                                            properties.get(key).getType(),
                                            properties.get(key).getFormat()
                                        );
                                        requestBodyJson.append("'" + key + "' => " + fakerMethod);
                                    }

                                    if ((counter + 1) < propertiesKeys.size()) {
                                        requestBodyJson.append(",\n\t\t\t\t");
                                    }
                                    counter++;

                                }
                            } else if (openApiRequestBodySchema instanceof ArraySchema) {
                                printArrayProperties(
                                    openApiRequestBodySchema, "", requestBodyJson, postOperation,
                                    false, BEGIN_SPACE, END_SPACE, false
                                );
                            } else {
                                String fakerMethod = CodegenHelper.getFakerMethod(
                                    FAKER_CALL,
                                    openApiRequestBodySchema.getType(),
                                    openApiRequestBodySchema.getFormat()
                                );
                                requestBodyJson.append(fakerMethod);
                            }
                        }
                    }
                }
            }
        }
        return requestBodyJson.toString();
    }

    /**
     * Create CodeCeption response in JSON format.
     *
     * @param responses List<CodegenResponse>
     * @param extraResponses ArrayList<Integer>
     * @return String
     */
    public String createJsonResponse(List<CodegenResponse> responses, ArrayList<Integer> extraResponses) {
        StringBuilder responseJson = new StringBuilder("");
        for (int i = 0; i < responses.size(); i++) {
            if (i == 0) {
                Schema responseSchema = (Schema) responses.get(0).getSchema();
                if (responseSchema != null) {
                    String $ref = responseSchema.get$ref();
                    if ($ref != null) {
                        $ref = super.getSimpleRef($ref);
                        Schema openApiResponseSchema = null;
                        if (super.openAPI.getComponents().getResponses() != null) {
                            if (super.openAPI.getComponents().getResponses().get($ref) != null) {
                                openApiResponseSchema = super.openAPI.getComponents().getResponses().get($ref).
                                        getContent().get("application/json").getSchema();
                            } else {
                                openApiResponseSchema = super.openAPI.getComponents().getSchemas().get($ref);
                            }
                        } else {
                            openApiResponseSchema = super.openAPI.getComponents().getSchemas().get($ref);
                        }

                        if (openApiResponseSchema != null) {
                            Map<String, Schema> properties = openApiResponseSchema.getProperties();
                            Set<String> propertiesKeys = properties.keySet();
                            int counter = 0;
                            for (String key : propertiesKeys) {
                                HashMap<String, String> keysToReplace = new HashMap<String, String>();
                                keysToReplace.put("number", "float");
                                keysToReplace.put("object", "array");

                                for (Map.Entry<String, String> entry : keysToReplace.entrySet()) {
                                    if (properties.get(key).getType() != null) {
                                        if (properties.get(key).getType().toString().equals(entry.getKey())) {
                                            properties.get(key).type(entry.getValue());
                                        }
                                    } else {
                                        properties.get(key).type("array");
                                    }
                                }
                                responseJson.append("'" + key + "' => '" + properties.get(key).getType() + "'");

                                if ((counter + 1) < propertiesKeys.size()) {
                                    responseJson.append(",\n\t\t\t\t");
                                }
                                counter++;
                            }
                        }
                    }
                }
            } else {
                extraResponses.add(Integer.parseInt(responses.get(i).getCode()));
            }
        }
        return responseJson.toString();
    }

    /**
     * Convert schemas to CodeCeption.
     *
     * @param property$ref String
     * @param key String
     * @param requestBodyJson StringBuilder
     * @param postOperation Operation
     * @param recursive Boolean
     * @param beginSpace String
     * @param endSpace String
     */
    public void printProperties(
            String property$ref, String key, StringBuilder requestBodyJson, Operation postOperation, Boolean recursive,
            String beginSpace, String endSpace
    ) {
        if (recursive) {
            beginSpace = beginSpace + "\t";
            endSpace = endSpace + "\t";
            requestBodyJson.append("[" + beginSpace);
        } else {
            requestBodyJson.append("[" + BEGIN_SPACE);
        }

        Schema propertyOpenApiRequestBodySchema = null;
        Map<String, Schema> propertiesOfProperty = null;
        while (property$ref != null) {
            property$ref = super.getSimpleRef(property$ref);
            propertyOpenApiRequestBodySchema = super.openAPI.getComponents().getSchemas().get(property$ref);
            property$ref = super.openAPI.getComponents().getSchemas().get(property$ref).get$ref();
        }

        if (propertyOpenApiRequestBodySchema instanceof ComposedSchema) {
            propertyOpenApiRequestBodySchema = ((ComposedSchema) propertyOpenApiRequestBodySchema).getAllOf().get(0);
            if (propertyOpenApiRequestBodySchema.get$ref() != null) {
                printProperties(
                    propertyOpenApiRequestBodySchema.get$ref(), key, requestBodyJson, postOperation,
                    true, beginSpace, endSpace
                );
            } else {
                propertiesOfProperty = propertyOpenApiRequestBodySchema.getProperties();
            }
        } else {
            propertiesOfProperty = propertyOpenApiRequestBodySchema.getProperties();
        }

        if (propertiesOfProperty != null) {
            Set<String> propertiesKeysOfProperty = propertiesOfProperty.keySet();
            int propertyCounter = 0;
            for (String keyOfProperty : propertiesKeysOfProperty) {
                if (propertiesOfProperty.get(keyOfProperty).getExample() != null) {
                    requestBodyJson.append(
                        "'" + keyOfProperty + "' => '" + propertiesOfProperty.get(keyOfProperty).getExample() + "'"
                    );
                } else {
                    String fakerMethod = CodegenHelper.getFakerMethod(
                        FAKER_CALL,
                        propertiesOfProperty.get(keyOfProperty).getType(),
                        propertiesOfProperty.get(keyOfProperty).getFormat()
                    );

                    requestBodyJson.append("'" + keyOfProperty + "' => " + fakerMethod);
                }

                if (propertiesOfProperty.get(keyOfProperty) instanceof ArraySchema) {
                    printArrayProperties(
                        propertiesOfProperty.get(keyOfProperty), "", requestBodyJson, postOperation,
                        true, beginSpace, endSpace, false
                    );
                }

                if (propertiesOfProperty.get(keyOfProperty).get$ref() != null) {
                    printProperties(
                        propertiesOfProperty.get(keyOfProperty).get$ref(), keyOfProperty, requestBodyJson,
                        postOperation, true, beginSpace, endSpace
                    );
                }

                if ((propertyCounter + 1) < propertiesKeysOfProperty.size()) {
                    if (recursive) {
                        requestBodyJson.append("," + beginSpace);
                    } else {
                        requestBodyJson.append("," + BEGIN_SPACE);
                    }
                }
                propertyCounter++;
            }
        }

        if (recursive) {
            requestBodyJson.append(endSpace + "]");
        } else {
            requestBodyJson.append(END_SPACE + "]");
        }
    }

    /**
     * Convert ArraySchema to CodeCeption array.
     *
     * @param property Schema
     * @param key String
     * @param requestBodyJson StringBuilder
     * @param postOperation Operation
     * @param recursive Boolean
     * @param beginSpace String
     * @param endSpace String
     * @param addArray Boolean
     */
    private void printArrayProperties(
            Schema property, String key, StringBuilder requestBodyJson, Operation postOperation, Boolean recursive,
            String beginSpace, String endSpace, Boolean addArray
    ) {
        if (((ArraySchema) property).getItems().getType() != null) {
            String fakerMethod = CodegenHelper.getFakerMethod(
                FAKER_CALL,
                ((ArraySchema) property).getItems().getType(),
                ((ArraySchema) property).getItems().getFormat()
            );
            if (!key.isEmpty()) {
                requestBodyJson.append("'" + key + "' => ");
            }
            requestBodyJson.append("array(" + fakerMethod + ")");
        } else if (((ArraySchema) property).getItems().get$ref() != null) {
            if (!key.isEmpty()) {
                requestBodyJson.append("'" + key + "' => ");
            }
            if (addArray) {
                requestBodyJson.append("array(");
            }
            printProperties(
                ((ArraySchema) property).getItems().get$ref(), key, requestBodyJson, postOperation, recursive,
                beginSpace, endSpace
            );
            if (addArray) {
                requestBodyJson.append(")");
            }
        }

    }

    /**
     * Add an extra operation for other responses then the first(standard/expected) response.
     *
     * @param ops List<CodegenOperation>
     * @param extraResponseOperations List<CodegenOperation>
     */
    private void addOperationForExtraResponse(List<CodegenOperation> ops, List<CodegenOperation> extraResponseOperations)
    {
        int extraResponseCounter = 0;
        for (CodegenOperation extraResponseOperation : extraResponseOperations) {
            String responseCode = extraResponseOperation.getOperationId().substring(
                    0, Math.min(extraResponseOperation.getOperationId().length(), 3)
            );
            extraResponseOperation.operationId = extraResponseOperation.operationId.substring(3);
            List<CodegenResponse> newResponse = new ArrayList<CodegenResponse>(extraResponseOperation.getResponses());
            List<CodegenResponse> responsesToDelete = new ArrayList<>();

            for (int i = 0; i < newResponse.size(); i++) {
                if (!newResponse.get(i).getCode().equals(responseCode.toString())) {
                    responsesToDelete.add(newResponse.get(i));
                }
            }
            for (CodegenResponse responseToDelete : responsesToDelete) {
                newResponse.remove(responseToDelete);
            }

            extraResponseOperation.produces = null;
            extraResponseOperation.codeCeptionResponse = null;
            extraResponseOperation.responses = newResponse;
            extraResponseOperation.operationId += "TestResponse" + responseCode;

            if (responseCode.equals("400")) {
                if (extraResponseOperation.returnJsonEncoded
                    && (extraResponseOperation.httpMethod.equals("POST")
                    || extraResponseOperation.httpMethod.equals("PUT"))
                ) {
                    extraResponseOperation.returnJsonEncoded = false;
                    extraResponseOperation.codeCeptionRequestBody = CodegenHelper.getFakerMethod(FAKER_CALL,
                            "string", "binary");
                }
            } else if (responseCode.equals("404")) {
                if (!extraResponseOperation.pathParams.isEmpty() || !extraResponseOperation.queryParams.isEmpty()) {
                    List<CodegenParameter> extraPathParams = new ArrayList<>();
                    List<CodegenParameter> extraQueryParams = new ArrayList<>();

                    for (CodegenParameter pathParam : extraResponseOperation.pathParams) {
                        try {
                            extraPathParams.add((CodegenParameter) pathParam.clone());
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }

                    }
                    for (CodegenParameter queryParam : extraResponseOperation.queryParams) {
                        try {
                            extraQueryParams.add((CodegenParameter) queryParam.clone());
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }

                    for (CodegenParameter pathParam : extraPathParams) {
                        pathParam.example = "!@#$% ^&*()";
                    }

                    for (CodegenParameter queryParam : extraQueryParams) {
                        queryParam.example = "!@#$% ^&*()";
                    }

                    extraResponseOperation.pathParams = extraPathParams;
                    extraResponseOperation.queryParams = extraQueryParams;
                } else {
                    if (extraResponseOperation.resolvedPath.contains("?")) {
                        String[] resolvedPathParts = extraResponseOperation.resolvedPath.split("\\?");
                        extraResponseOperation.resolvedPath = resolvedPathParts[0] + "/Swagger/OpenApi/Specification?" +
                                resolvedPathParts[1];
                    } else {
                        extraResponseOperation.resolvedPath += "/Swagger/OpenApi/Specification";
                    }
                }
            } else if (responseCode.equals("405")) {
                if (extraResponseOperation.httpMethod.equals("GET") || extraResponseOperation.httpMethod.equals("DELETE")) {
                    if (extraResponseOperation.resolvedPath.contains("?")) {
                        String[] resolvedPathParts = extraResponseOperation.resolvedPath.split("\\?");
                        extraResponseOperation.resolvedPath = resolvedPathParts[0] + "?!@#$%^&*()" +
                                resolvedPathParts[1];
                    } else {
                        extraResponseOperation.resolvedPath += "?!@#$%^&*()";
                    }
                } else {
                    extraResponseOperation.httpMethod = HTTP_METHOD_FOR_METHOD_NOT_ALLOWED;
                }
            }

            ops.add(extraResponseOperation);
            extraResponseCounter++;
        }
    }

    /**
     * Sort the operations by HTTP request methods.
     *
     * @param ops List<CodegenOperation>
     * @return List<CodegenOperation>
     */
    private List<CodegenOperation> sortByHttpMethod(List<CodegenOperation> ops) {
        final String[] HTTP_METHODS_ORDER = {
                "POST", "GET", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PUT", "PATCH", "DELETE"
        };
        List<CodegenOperation> opsSorted = new ArrayList<CodegenOperation>();
        List<CodegenOperation> operationsToDelete = new ArrayList<CodegenOperation>();

        for (int i = 0; i < HTTP_METHODS_ORDER.length; i++) {
            for (int j = 0; j < ops.size(); j++) {
                if (ops.get(j).httpMethod.equals(HTTP_METHODS_ORDER[i])) {
                    opsSorted.add(ops.get(j));
                    operationsToDelete.add(ops.get(j));
                }
            }

            for (CodegenOperation operation : operationsToDelete) {
                ops.remove(operation);
            }
        }
        return opsSorted;
    }

    /**
     * Add example to the parameters.
     *
     * @param param CodegenParameter
     * @return CodegenParameter
     */
    private CodegenParameter setParamExample(CodegenParameter param) {
        JSONObject paramJsonObject = new JSONObject(param.getJsonSchema());

        if (paramJsonObject.has("example")) {
            param.example = paramJsonObject.get("example").toString();
        } else {
            if (paramJsonObject.has("schema")) {
                paramJsonObject = new JSONObject(paramJsonObject.get("schema").toString());
                if (paramJsonObject.has("example")) {
                    param.example = paramJsonObject.get("example").toString();
                }
            }
        }

        return param;
    }
}
