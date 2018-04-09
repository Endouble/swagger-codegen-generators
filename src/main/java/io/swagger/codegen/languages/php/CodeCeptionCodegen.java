package io.swagger.codegen.languages.php;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.CodegenHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
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
                        if (postOperation.getRequestBody().get$ref() != null) {
                            String $ref = postOperation.getRequestBody().get$ref();
                            if ($ref != null) {
                                $ref = super.getSimpleRef($ref);
                            }
                            System.out.println(super.openAPI.getComponents().getRequestBodies().get($ref));
                        } else {
                            Set<String> requestBodyKeySet = requestBody.getContent().keySet();
                            String requestBodyContentKey = requestBodyKeySet.iterator().next();
                            Schema requestBodySchema = requestBody.getContent().get(requestBodyContentKey).getSchema();
                            if (requestBodySchema != null) {
                                String $ref = requestBodySchema.get$ref();
                                if ($ref != null) {
                                    $ref = super.getSimpleRef($ref);
                                    ObjectSchema openApiRequestBodySchema = null;
                                    if (super.openAPI.getComponents().getRequestBodies() != null) {
                                        if (super.openAPI.getComponents().getRequestBodies().get($ref) != null) {
                                            openApiRequestBodySchema = (ObjectSchema) super.openAPI.getComponents().getRequestBodies().get($ref).
                                                    getContent().get("application/json").getSchema();
                                        } else {
                                            openApiRequestBodySchema = (ObjectSchema) super.openAPI.getComponents().getSchemas().get($ref);
                                        }
                                    } else {
                                        openApiRequestBodySchema = (ObjectSchema) super.openAPI.getComponents().getSchemas().get($ref);
                                    }

                                    if (openApiRequestBodySchema != null) {
                                        List<String> requiredFields = openApiRequestBodySchema.getRequired();
                                        if (requiredFields != null && requiredFields.size() > 0) {
                                            int counter = 0;
                                            for (String requiredField : requiredFields) {
                                                Schema requiredFieldSchema = openApiRequestBodySchema.getProperties().get(requiredField);
                                                if (requiredFieldSchema != null) {
                                                    String fakerMethod = CodegenHelper.getFakerMethod(
                                                            "$this->faker->",
                                                            requiredFieldSchema.getType(),
                                                            requiredFieldSchema.getFormat()
                                                    );
                                                    requestBodyJson.append("'" + requiredField + "' => " + fakerMethod);

                                                    if ((counter + 1) < requiredFields.size()) {
                                                        requestBodyJson.append(",\n\t\t\t\t");
                                                    }
                                                }

                                                counter++;
                                            }
                                        } else {
                                            //System.out.println(operation.operationId);
                                            Map<String, Schema> properties = openApiRequestBodySchema.getProperties();
                                            Set<String> propertiesKeys = properties.keySet();
                                            int counter = 0;
                                            for (String key : propertiesKeys) {
                                                Map<String, Schema> propertiesFields = properties;
                                                String propertiesRef = propertiesFields.get(key).get$ref();
                                                if (propertiesRef == null) {
                                                    String fakerMethod = CodegenHelper.getFakerMethod(
                                                            "$this->faker->",
                                                            propertiesFields.get(key).getType(),
                                                            propertiesFields.get(key).getFormat()
                                                    );
                                                    requestBodyJson.append("'" + key + "' => " + fakerMethod);
                                                    if ((counter + 1) < propertiesKeys.size()) {
                                                        requestBodyJson.append(",\n\t\t\t\t");
                                                    }
                                                } else {
                                                    requestBodyJson.append("'" + key + "' => [");
                                                    requestBodyJson.append("\n");

                                                    requestBodyJson.append("\n\t\t\t\t");
                                                    requestBodyJson.append("]");
                                                    if ((counter + 1) < propertiesKeys.size()) {
                                                        requestBodyJson.append(",\n\t\t\t\t");
                                                    }
                                                    counter++;

//                                                    Map<String, Schema> propertiesFields = properties;
//
//                                                    while (propertiesRef != null) {
//                                                        propertiesRef = super.getSimpleRef(propertiesRef);
//                                                        ObjectSchema objectSchema = (ObjectSchema) super.openAPI.getComponents().getSchemas().get(propertiesRef);
//                                                        propertiesRef = objectSchema.get$ref();
//
//                                                        if (objectSchema.get$ref() == null) {
//                                                            propertiesFields = objectSchema.getProperties();
//                                                        }
//                                                    }
//                                                    if (propertiesRef == null) {
//                                                        String fakerMethod = CodegenHelper.getFakerMethod(
//                                                                "$this->faker->",
//                                                                propertiesFields.get(key).getType(),
//                                                                propertiesFields.get(key).getFormat()
//                                                        );
//                                                        requestBodyJson.append("'" + key + "' => " + fakerMethod);
//
//                                                        if ((counter + 1) < propertiesKeys.size()) {
//                                                            requestBodyJson.append(",\n\t\t\t\t");
//                                                        }
//                                                    }
                                                }
                                            }


//                                            Map<String, Schema> properties = openApiRequestBodySchema.getProperties();
//                                            Set<String> propertiesKeys = properties.keySet();
//                                            int counter = 0;
//                                            for (String key : propertiesKeys) {
//                                                Map<String, Schema> propertiesFields = properties;
//                                                String propertiesRef = propertiesFields.get(key).get$ref();
//                                                while (propertiesRef != null) {
//                                                    propertiesRef = super.getSimpleRef(propertiesRef);
//                                                    ObjectSchema objectSchema = (ObjectSchema) super.openAPI.getComponents().getSchemas().get(propertiesRef);
//                                                    propertiesRef = objectSchema.get$ref();
//
//                                                    if (objectSchema.get$ref() == null) {
//                                                        propertiesFields = objectSchema.getProperties();
//                                                    }
//                                                }
//                                                if (propertiesRef == null) {
//                                                    String fakerMethod = CodegenHelper.getFakerMethod(
//                                                            "$this->faker->",
//                                                            propertiesFields.get(key).getType(),
//                                                            propertiesFields.get(key).getFormat()
//                                                    );
//                                                    requestBodyJson.append("'" + key + "' => " + fakerMethod);
//
//                                                    if ((counter + 1) < propertiesKeys.size()) {
//                                                        requestBodyJson.append(",\n\t\t\t\t");
//                                                    }
//                                                }
//                                                counter++;
//                                            }
                                        }
                                    }
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

            if (operation.httpMethod.equals("POST")) {
                operations.put("postMethod", operation.operationId);
            }

            ops.set(operationCounter, updateOperation);
            operationCounter++;
        }
        return objs;
    }
}
