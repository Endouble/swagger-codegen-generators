package io.swagger.codegen.languages.php;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.CodegenHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.io.File;
import java.util.*;

public class CodeCeptionCodegen extends AbstractPhpCodegen
{
     @SuppressWarnings("hiding")
    protected String apiVersion = "1.0.0";


    /**
     * Configures the type of generator.
     * 
     * @return  the CodegenType for this generator
     * @see     io.swagger.codegen.CodegenType
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

        supportingFiles.add(new SupportingFile("testcase.handlebars", packagePath + File.separator +
                srcBasePath + File.separator + "Acceptance", "TestCase.php"));
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        objs = super.postProcessOperations(objs);
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

        int operationCounter = 0;
        Paths paths = this.openAPI.getPaths();
//        for (String resourcePath : paths.keySet()) {
//            PathItem path = paths.get(resourcePath);
//            Operation postOperation = path.getPost();
//            if (postOperation != null) {
//                System.out.println(postOperation.getOperationId());
//            }
//        }

        for (CodegenOperation operation : ops) {
            String path = operation.path;
            PathItem pathItem = paths.get(path);
            if (operation.pathParams.size() > 0) {
                for (CodegenParameter pathParam : operation.pathParams) {
                    path = path.replace("{" + pathParam.baseName + "}", "$" + pathParam.baseName);
                }
            }

            CodegenOperation updateOperation = operation;
            updateOperation.resolvedPath = path;

            List<CodegenResponse> responses = updateOperation.responses;
            for (CodegenResponse response : responses) {
                response.httpDescription = CodegenHelper.getHTTPDescription(Integer.parseInt(response.getCode()));
            }


            //TODO Think about array and objects.
            StringBuilder requestBodyJson = new StringBuilder("");
            if (pathItem != null) {
                Operation postOperation = pathItem.getPost();
                if (postOperation != null) {
                    if (postOperation.getOperationId().equals(operation.getOperationId())) {
                        RequestBody requestBody = postOperation.getRequestBody();
                        String $ref = "";
                        Schema openApiRequestBodySchema = null;
                        if (postOperation.getRequestBody().get$ref() != null) {
                            //System.out.println(super.openAPI.getComponents().getRequestBodies().get(super.getSimpleRef(postOperation.getRequestBody().get$ref())));
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
                        } else if (postOperation.getRequestBody().getContent().get(postOperation.getRequestBody().getContent().keySet().iterator().next()).getSchema().get$ref() != null) {
                            $ref = postOperation.getRequestBody().getContent().get(postOperation.getRequestBody().getContent().keySet().iterator().next()).getSchema().get$ref();
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
                            openApiRequestBodySchema = postOperation.getRequestBody().getContent().get(postOperation.getRequestBody().getContent().keySet().iterator().next()).getSchema();
                        }

                        if ($ref == null || $ref.isEmpty()) {
                            if (openApiRequestBodySchema != null) {
                                if (openApiRequestBodySchema.getProperties() != null) {
                                    Map<String, Schema> properties = openApiRequestBodySchema.getProperties();
                                    Set<String> propertiesKeys = properties.keySet();
                                    int counter = 0;
                                    for (String key : propertiesKeys) {
                                        String type = properties.get(key).getType();
                                        String format = properties.get(key).getFormat();

                                        if (properties.get(key).get$ref() != null) {
                                            requestBodyJson.append("'" + key + "' => [\n\t\t\t\t\t");
                                            Schema propertyOpenApiRequestBodySchema = null;
                                            String property$ref = properties.get(key).get$ref();
                                            while (property$ref != null) {
                                                property$ref = super.getSimpleRef(property$ref);
                                                propertyOpenApiRequestBodySchema = super.openAPI.getComponents().getSchemas().get(property$ref);
                                                property$ref = super.openAPI.getComponents().getSchemas().get(property$ref).get$ref();
                                            }
                                            Map<String, Schema> propertiesOfProperty = propertyOpenApiRequestBodySchema.getProperties();
                                            Set<String> propertiesKeysOfProperty = propertiesOfProperty.keySet();
                                            int propertyCounter = 0;
                                            for (String keyOfProperty : propertiesKeysOfProperty) {
                                                String fakerMethod = CodegenHelper.getFakerMethod(
                                                        "$this->faker->",
                                                        propertiesOfProperty.get(keyOfProperty).getType(),
                                                        propertiesOfProperty.get(keyOfProperty).getFormat()
                                                );
                                                requestBodyJson.append("'" + keyOfProperty + "' => " + fakerMethod);

                                                if ((propertyCounter + 1) < propertiesKeysOfProperty.size()) {
                                                    requestBodyJson.append(",\n\t\t\t\t\t");
                                                }
                                                propertyCounter++;
                                            }
                                            requestBodyJson.append("\n\t\t\t\t]");
                                        } else {
                                            String fakerMethod = CodegenHelper.getFakerMethod(
                                                    "$this->faker->",
                                                    type,
                                                    format
                                            );
                                            requestBodyJson.append("'" + key + "' => " + fakerMethod);
                                        }

                                        if ((counter + 1) < propertiesKeys.size()) {
                                            requestBodyJson.append(",\n\t\t\t\t");
                                        }
                                        counter++;
                                    }
                                } else {
                                    String fakerMethod = CodegenHelper.getFakerMethod(
                                            "$this->faker->",
                                            openApiRequestBodySchema.getType(),
                                            openApiRequestBodySchema.getFormat()
                                    );
                                    requestBodyJson.append("'" + openApiRequestBodySchema.getName() + "' => " + fakerMethod);
                                }
                            }
                        }
                    }
                }
            }
            updateOperation.codeCeptionRequestBody = requestBodyJson.toString();

            StringBuilder responseJson = new StringBuilder("");
            //for (CodegenResponse response : responses) {
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
            //}
            updateOperation.codeCeptionResponse = responseJson.toString();

            ops.set(operationCounter, updateOperation);
            operationCounter++;
        }
        return objs;
    }
}
