package uk.gov.ccew.rsi.flow.util;


import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FormDataUtil {

    private FormDataUtil() {}

    private static final Pattern RADIOGROUP_PATTERN = Pattern.compile("radiogroup", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHECKBOX_PATTERN = Pattern.compile("checkbox", Pattern.CASE_INSENSITIVE);



    public static Map<String, Object> cleanFormData(Map<String, String> formData) {
        if (formData == null || formData.isEmpty()) {
            return Map.of();
        }


        final Function<String, String> keyTransform = normalizeText.andThen(normalizeCheckBox).andThen(transformToFlowEngineString);
        final Predicate<Map.Entry<String, String>> combinedFilter = filterRadioGroup.or(filterCheckBox);

        return formData.entrySet().stream()
                .filter(combinedFilter)
                .collect(Collectors.toMap(
                        e -> keyTransform.apply(e.getKey()), Map.Entry::getValue, (existing, replacement) -> replacement
                ));
    }



    private static final Predicate<Map.Entry<String, String>> filterRadioGroup = entry -> entry.getKey().toLowerCase().contains("radiogroup")
            && (entry.getValue().equalsIgnoreCase("yes")
            || entry.getValue().equalsIgnoreCase("no")
            || entry.getValue().toLowerCase().contains("option"));

    private static final Predicate<Map.Entry<String,String>> filterCheckBox = entry -> entry.getKey().toLowerCase().contains("checkbox");



    private static final UnaryOperator<String> transformToFlowEngineString = key -> key.replace(".", "_").replace("-", "_");
    private static final UnaryOperator<String> normalizeText = key -> RADIOGROUP_PATTERN.matcher(key).replaceAll("radioGroup");
    private static  final UnaryOperator<String> normalizeCheckBox = key -> CHECKBOX_PATTERN.matcher(key).replaceAll("checkBox");
}
