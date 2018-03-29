package io.swagger.codegen.languages.php;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.CodegenHelper;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ObjectSchema;

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
        acceptanceTemplateFiles.put("acceptance.handlebars", ".php");
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
        
        for (CodegenOperation operation : ops) {
            String path = operation.path;
            if (operation.pathParams.size() > 0) {
                for (CodegenParameter pathParam : operation.pathParams) {
                    path = path.replace("{" + pathParam.baseName + "}", "$" + pathParam.baseName);
                }
            }

            CodegenOperation updateOperation = operation;
            updateOperation.resolvedPath = path;

            List<CodegenResponse> responses = operation.responses;
            for (CodegenResponse response : responses) {
                response.httpDescription = CodegenHelper.getHTTPDescription(Integer.parseInt(response.getCode()));
            }

            StringBuilder responseJson = new StringBuilder("");
            for (CodegenResponse response : responses) {
                ObjectSchema test = (ObjectSchema) response.getSchema();
                if (test != null) {
                    Map<java.lang.String,io.swagger.v3.oas.models.media.Schema> properties = test.getProperties();
                    Set<String> propertiesKeys = properties.keySet();
                    int counter = 0;
                    for (String key : propertiesKeys) {
                        HashMap<String,String> keysToReplace = new HashMap<String,String>();
                        keysToReplace.put("number", "float");
                        keysToReplace.put("object", "array");

                        for (Map.Entry<String, String> entry : keysToReplace.entrySet()) {
                            if (properties.get(key).getType().toString().equals(entry.getKey())) {
                                properties.get(key).type(entry.getValue());
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
