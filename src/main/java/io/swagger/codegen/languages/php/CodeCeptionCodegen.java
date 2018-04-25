package io.swagger.codegen.languages.php;

import com.github.jknack.handlebars.EscapingStrategy;
import io.swagger.codegen.*;
import io.swagger.codegen.languages.CodegenHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.*;

public class CodeCeptionCodegen extends AbstractPhpCodegen
{
     @SuppressWarnings("hiding")
    protected String apiVersion = "1.0.0";
    private final String BEGIN_SPACE = "\n\t\t\t\t\t";
    private final String END_SPACE = "\n\t\t\t\t";
    private final String[] DIRECTORIES = {"/" + CODECEPTION_DIRECTORY + "/_data", "/" + CODECEPTION_DIRECTORY + "/_output"};


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

        supportingFiles.add(new SupportingFile("composer.mustache", packagePath + File.separator +
                srcBasePath, "composer.json"));
        supportingFiles.add(new SupportingFile("README.mustache", packagePath + File.separator +
                srcBasePath, "README.md"));
        supportingFiles.add(new SupportingFile("codeception.mustache", packagePath + File.separator +
                srcBasePath + File.separator, "codeception.yml"));
        supportingFiles.add(new SupportingFile("testcase.mustache", packagePath + File.separator +
                srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator + "acceptance", "TestCase.php"));
        supportingFiles.add(new SupportingFile("acceptance.suite.mustache", packagePath + File.separator +
                srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator, "acceptance.suite.yml"));
        supportingFiles.add(new SupportingFile("acceptanceTester.mustache", packagePath + File.separator +
                srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator + "_support", "AcceptanceTester.php"));
        supportingFiles.add(new SupportingFile("acceptanceHelper.mustache", packagePath + File.separator +
                srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator + "_support" + File.separator + "Helper" + File.separator, "Acceptance.php"));
        supportingFiles.add(new SupportingFile("acceptanceTesterActions.mustache", packagePath + File.separator +
                srcBasePath + File.separator + CODECEPTION_DIRECTORY + File.separator + "_support" + File.separator + "_generated" + File.separator, "AcceptanceTesterActions.php"));
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        for (String directory : DIRECTORIES) {
            if(!new File(this.outputFolder + directory).isDirectory()) {
                new File(this.outputFolder + directory).mkdirs();
            }
        }

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
            if (operation.queryParams.size() > 0) {
                for (int i = 0; i < operation.queryParams.size(); i++) {
                    if(i == 0) {
                        path += "?" + operation.queryParams.get(i).baseName + "=$" + operation.queryParams.get(i).baseName;
                    } else {
                        path += "&" + operation.queryParams.get(i).baseName + "=$" + operation.queryParams.get(i).baseName;
                    }

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

            StringBuilder requestBodyJson = new StringBuilder("");
            if (pathItem != null) {
                Operation postOperation = pathItem.getPost();
                if (postOperation != null) {
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
                                    Schema schema = openApiRequestBodySchema;
                                    Map<String, Schema> properties = schema.getProperties();
                                    Set<String> propertiesKeys = properties.keySet();
                                    int counter = 0;
                                    for (String key : propertiesKeys) {
                                        String property$ref = properties.get(key).get$ref();
                                        if (property$ref != null) {
                                            requestBodyJson.append("'" + key + "' => ");
                                            printProperties(property$ref, key, requestBodyJson, postOperation,
                                                    false, BEGIN_SPACE, END_SPACE);
                                        } else if (properties.get(key) instanceof ArraySchema) {
                                            printArrayProperties(properties.get(key), key, requestBodyJson,
                                                    postOperation, false, BEGIN_SPACE, END_SPACE);
                                        } else {
                                            String fakerMethod = CodegenHelper.getFakerMethod(
                                                    "$this->faker->",
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
                                } else if (openApiRequestBodySchema instanceof ArraySchema){
                                    printArrayProperties(openApiRequestBodySchema, "", requestBodyJson,
                                            postOperation, false, BEGIN_SPACE, END_SPACE);
                                } else {
                                    String fakerMethod = CodegenHelper.getFakerMethod(
                                            "$this->faker->",
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

    public void printProperties(String property$ref, String key, StringBuilder requestBodyJson, Operation postOperation,
                                Boolean recursive, String beginSpace, String endSpace) {
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
                printProperties(propertyOpenApiRequestBodySchema.get$ref(), key,
                        requestBodyJson, postOperation, true, beginSpace, endSpace);
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
                String fakerMethod = CodegenHelper.getFakerMethod(
                        "$this->faker->",
                        propertiesOfProperty.get(keyOfProperty).getType(),
                        propertiesOfProperty.get(keyOfProperty).getFormat()
                );

                requestBodyJson.append("'" + keyOfProperty + "' => " + fakerMethod);

                if (propertiesOfProperty.get(keyOfProperty) instanceof ArraySchema) {
                    printArrayProperties(propertiesOfProperty.get(keyOfProperty), "", requestBodyJson,
                            postOperation, true, beginSpace, endSpace);
                }

                if (propertiesOfProperty.get(keyOfProperty).get$ref() != null) {
                    printProperties(propertiesOfProperty.get(keyOfProperty).get$ref(), keyOfProperty, requestBodyJson,
                            postOperation, true, beginSpace, endSpace);
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

    public void printArrayProperties(Schema property, String key, StringBuilder requestBodyJson, Operation postOperation,
                                     Boolean recursive, String beginSpace, String endSpace) {
        if (((ArraySchema) property).getItems().getType() != null) {
            String fakerMethod = CodegenHelper.getFakerMethod(
                    "$this->faker->",
                    ((ArraySchema) property).getItems().getType(),
                    ((ArraySchema) property).getItems().getFormat()
            );
            if (!key.isEmpty()) {
                requestBodyJson.append("'" + key + "' => ");
            }
            requestBodyJson.append("[" + fakerMethod + "]");
        } else if (((ArraySchema) property).getItems().get$ref() != null) {
            if (!key.isEmpty()) {
                requestBodyJson.append("'" + key + "' => ");
            }
            printProperties(((ArraySchema) property).getItems().get$ref(),
                    key, requestBodyJson, postOperation, recursive, beginSpace,
                    endSpace);
        }

    }
}
