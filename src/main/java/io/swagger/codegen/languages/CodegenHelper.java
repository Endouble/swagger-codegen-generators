package io.swagger.codegen.languages;

import java.util.*;

public class CodegenHelper {

    static Set<String> getDefaultIncludes() {
        return new HashSet<>(
                Arrays.asList("double",
                        "int",
                        "long",
                        "short",
                        "char",
                        "float",
                        "String",
                        "boolean",
                        "Boolean",
                        "Double",
                        "Void",
                        "Integer",
                        "Long",
                        "Float")
        );
    }

    static Map<String, String> getTypeMappings() {
        final Map<String, String> typeMapping = new HashMap<>();
        typeMapping.put("array", "List");
        typeMapping.put("map", "Map");
        typeMapping.put("List", "List");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");
        typeMapping.put("int", "Integer");
        typeMapping.put("float", "Float");
        typeMapping.put("number", "BigDecimal");
        typeMapping.put("DateTime", "Date");
        typeMapping.put("long", "Long");
        typeMapping.put("short", "Short");
        typeMapping.put("char", "String");
        typeMapping.put("double", "Double");
        typeMapping.put("object", "Object");
        typeMapping.put("integer", "Integer");
        typeMapping.put("ByteArray", "byte[]");
        typeMapping.put("binary", "byte[]");
        typeMapping.put("file", "File");
        typeMapping.put("UUID", "UUID");
        typeMapping.put("BigDecimal", "BigDecimal");
        return typeMapping;
    }

    static Map<String, String> getImportMappings() {
        final Map<String, String> importMapping = new HashMap<>();
        importMapping.put("BigDecimal", "java.math.BigDecimal");
        importMapping.put("UUID", "java.util.UUID");
        importMapping.put("File", "java.io.File");
        importMapping.put("Date", "java.util.Date");
        importMapping.put("Timestamp", "java.sql.Timestamp");
        importMapping.put("Map", "java.util.Map");
        importMapping.put("HashMap", "java.util.HashMap");
        importMapping.put("Array", "java.util.List");
        importMapping.put("ArrayList", "java.util.ArrayList");
        importMapping.put("List", "java.util.*");
        importMapping.put("Set", "java.util.*");
        importMapping.put("DateTime", "org.joda.time.*");
        importMapping.put("LocalDateTime", "org.joda.time.*");
        importMapping.put("LocalDate", "org.joda.time.*");
        importMapping.put("LocalTime", "org.joda.time.*");
        return importMapping;
    }

    static void initalizeSpecialCharacterMapping(Map<String, String> specialCharReplacements) {
        specialCharReplacements.put("$", "Dollar");
        specialCharReplacements.put("^", "Caret");
        specialCharReplacements.put("|", "Pipe");
        specialCharReplacements.put("=", "Equal");
        specialCharReplacements.put("*", "Star");
        specialCharReplacements.put("-", "Minus");
        specialCharReplacements.put("&", "Ampersand");
        specialCharReplacements.put("%", "Percent");
        specialCharReplacements.put("#", "Hash");
        specialCharReplacements.put("@", "At");
        specialCharReplacements.put("!", "Exclamation");
        specialCharReplacements.put("+", "Plus");
        specialCharReplacements.put(":", "Colon");
        specialCharReplacements.put(">", "Greater_Than");
        specialCharReplacements.put("<", "Less_Than");
        specialCharReplacements.put(".", "Period");
        specialCharReplacements.put("_", "Underscore");
        specialCharReplacements.put("?", "Question_Mark");
        specialCharReplacements.put(",", "Comma");
        specialCharReplacements.put("'", "Quote");
        specialCharReplacements.put("\"", "Double_Quote");
        specialCharReplacements.put("/", "Slash");
        specialCharReplacements.put("\\", "Back_Slash");
        specialCharReplacements.put("(", "Left_Parenthesis");
        specialCharReplacements.put(")", "Right_Parenthesis");
        specialCharReplacements.put("{", "Left_Curly_Bracket");
        specialCharReplacements.put("}", "Right_Curly_Bracket");
        specialCharReplacements.put("[", "Left_Square_Bracket");
        specialCharReplacements.put("]", "Right_Square_Bracket");
        specialCharReplacements.put("~", "Tilde");
        specialCharReplacements.put("`", "Backtick");
        specialCharReplacements.put("<=", "Less_Than_Or_Equal_To");
        specialCharReplacements.put(">=", "Greater_Than_Or_Equal_To");
        specialCharReplacements.put("!=", "Not_Equal");
    }

    public static String getHTTPDescription(int httpStatusCode) {
        if (httpStatusCode <= 0) {
            return "";
        }

        HashMap<Integer,String> httpCodeAndDescription = new HashMap<Integer,String>();
        httpCodeAndDescription.put(100, "CONTINUE");
        httpCodeAndDescription.put(102, "PROCESSING");
        httpCodeAndDescription.put(200, "OK");
        httpCodeAndDescription.put(201, "CREATED");
        httpCodeAndDescription.put(202, "ACCEPTED");
        httpCodeAndDescription.put(203, "NON-AUTHORITATIVE INFORMATION");
        httpCodeAndDescription.put(204, "NO CONTENT");
        httpCodeAndDescription.put(205, "RESET CONTENT");
        httpCodeAndDescription.put(206, "PARTIAL CONTENT");
        httpCodeAndDescription.put(207, "MULTI-STATUS");
        httpCodeAndDescription.put(208, "ALREADY REPORTED");
        httpCodeAndDescription.put(226, "IM USED");
        httpCodeAndDescription.put(300, "MULTIPLE CHOICES");
        httpCodeAndDescription.put(301, "MOVED PERMANENTLY");
        httpCodeAndDescription.put(302, "FOUND");
        httpCodeAndDescription.put(303, "SEE OTHER");
        httpCodeAndDescription.put(304, "NOT MODIFIED");
        httpCodeAndDescription.put(305, "USE PROXY");
        httpCodeAndDescription.put(307, "TEMPORARY REDIRECT");
        httpCodeAndDescription.put(308, "PERMANENT REDIRECT");
        httpCodeAndDescription.put(400, "BAD REQUEST");
        httpCodeAndDescription.put(401, "UNAUTHORIZED");
        httpCodeAndDescription.put(402, "PAYMENT REQUIRED");
        httpCodeAndDescription.put(403, "FORBIDDEN");
        httpCodeAndDescription.put(404, "NOT FOUND");
        httpCodeAndDescription.put(405, "METHOD NOT ALLOWED");
        httpCodeAndDescription.put(406, "NOT ACCEPTABLE");
        httpCodeAndDescription.put(407, "PROXY AUTHENTICATION REQUIRED");
        httpCodeAndDescription.put(408, "REQUEST TIMEOUT");
        httpCodeAndDescription.put(409, "CONFLICT");
        httpCodeAndDescription.put(410, "GONE");
        httpCodeAndDescription.put(411, "LENGTH REQUIRED");
        httpCodeAndDescription.put(412, "PRECONDITION FAILED");
        httpCodeAndDescription.put(413, "REQUEST ENTITY TOO LARGE");
        httpCodeAndDescription.put(414, "REQUEST-URI TOO LONG");
        httpCodeAndDescription.put(415, "UNSUPPORTED MEDIA TYPE");
        httpCodeAndDescription.put(416, "REQUESTED RANGE NOT SATISFIABLE");
        httpCodeAndDescription.put(417, "EXPECTATION FAILED");
        httpCodeAndDescription.put(421, "MISDIRECTED REQUEST");
        httpCodeAndDescription.put(422, "UNPROCESSABLE ENTITY");
        httpCodeAndDescription.put(423, "LOCKED");
        httpCodeAndDescription.put(424, "FAILED DEPENDENCY");
        httpCodeAndDescription.put(426, "UPGRADE REQUIRED");
        httpCodeAndDescription.put(428, "PRECONDITION REQUIRED");
        httpCodeAndDescription.put(429, "TOO MANY REQUESTS");
        httpCodeAndDescription.put(431, "REQUEST HEADER FIELDS TOO LARGE");
        httpCodeAndDescription.put(500, "INTERNAL SERVER ERROR");
        httpCodeAndDescription.put(501, "NOT IMPLEMENTED");
        httpCodeAndDescription.put(502, "BAD GATEWAY");
        httpCodeAndDescription.put(503, "SERVICE UNAVAILABLE");
        httpCodeAndDescription.put(504, "GATEWAY TIMEOUT");
        httpCodeAndDescription.put(505, "HTTP VERSION NOT SUPPORTED");
        httpCodeAndDescription.put(506, "VARIANT ALSO NEGOTIATES");
        httpCodeAndDescription.put(507, "INSUFFICIENT STORAGE");
        httpCodeAndDescription.put(508, "LOOP DETECTED");
        httpCodeAndDescription.put(510, "NOT EXTENDED");
        httpCodeAndDescription.put(511, "NETWORK AUTHENTICATION REQUIRED");

        return httpCodeAndDescription.get(httpStatusCode);
    }

    public static String getFakerMethod(String fakerVariable, String type, String format) {
        if (fakerVariable.isEmpty() || fakerVariable == null || type.isEmpty() || type == null) {
            return "";
        }

        if (format == null) {
            format = "";
        }

        HashMap<List<String>,String> keysToReplace = new HashMap<List<String>,String>();
        keysToReplace.put(Arrays.asList("string", ""), fakerVariable + "text()");
        keysToReplace.put(Arrays.asList("string", "date"), fakerVariable + "date()");
        keysToReplace.put(Arrays.asList("string", "date-time"), fakerVariable + "dateTime()");
        keysToReplace.put(Arrays.asList("string", "password"), fakerVariable + "password()");
        keysToReplace.put(Arrays.asList("string", "uuid"), fakerVariable + "uuid");
        keysToReplace.put(Arrays.asList("string", "uri"), fakerVariable + "url");
        keysToReplace.put(Arrays.asList("string", "email"), fakerVariable + "email");
        keysToReplace.put(Arrays.asList("string", "ipv4"), fakerVariable + "ipv4");
        keysToReplace.put(Arrays.asList("string", "ipv6"), fakerVariable + "ipv6");
        keysToReplace.put(Arrays.asList("integer", ""), fakerVariable + "numberBetween()");
        keysToReplace.put(Arrays.asList("integer", "int32"), fakerVariable + "numberBetween()");
        keysToReplace.put(Arrays.asList("integer", "int64"), fakerVariable + "numberBetween()");
        keysToReplace.put(Arrays.asList("number", ""), fakerVariable + "numberBetween()");
        keysToReplace.put(Arrays.asList("number", "float"), fakerVariable + "randomFloat()");
        keysToReplace.put(Arrays.asList("number", "double"), fakerVariable + "randomFloat()");
        keysToReplace.put(Arrays.asList("boolean", ""), fakerVariable + "boolean()");

        if (
                keysToReplace.get(Arrays.asList(type, format)) == null
                        || keysToReplace.get(Arrays.asList(type, format)).isEmpty()
                ) {
            return "null";
        }

        return keysToReplace.get(Arrays.asList(type, format));
    }
}

