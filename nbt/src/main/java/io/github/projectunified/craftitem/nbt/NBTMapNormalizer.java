package io.github.projectunified.craftitem.nbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Normalizes maps and converts them to the right types for SNBT.
 *
 * <p>Recursively converts map and list values to properly typed values. Supports custom string
 * translators for dynamic substitution and forced-value maps using {@code $type} and {@code $value} keys.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * Map<String, Object> map = Map.of(
 *     "$type", "float",
 *     "$value", "123.45"
 * );
 * Object result = NBTMapNormalizer.normalize(map);  // Returns 123.45f
 * }</pre>
 */
public final class NBTMapNormalizer {
    private NBTMapNormalizer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Normalizes a value by resolving forced-value maps and applying translator
     *
     * @param value The value to normalize
     * @return Normalized value (can be a Map, primitive, or array)
     * @throws IllegalArgumentException if forced-value map is invalid
     */
    public static Object normalize(Object value) {
        return normalize(value, s -> s);
    }

    /**
     * Normalizes a value by resolving forced-value maps and applying translator
     *
     * @param value      The value to normalize
     * @param translator Custom string translator for values
     * @return Normalized value (can be a Map, primitive, or array)
     * @throws IllegalArgumentException if forced-value map is invalid
     */
    public static Object normalize(Object value, UnaryOperator<String> translator) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof Map)) {
            return value;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;

        // Check if this is a forced-value map
        if (map.containsKey("$type")) {
            if (!map.containsKey("$value")) {
                throw new IllegalArgumentException(
                        "Map with '$type' entry must also have '$value' entry");
            }
            return normalizeForcedValue(map.get("$type"), map.get("$value"), translator);
        }

        // Recursively normalize map values
        Map<String, Object> result = new HashMap<>(map);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) entryValue;
                result.put(entry.getKey(), normalize(nested, translator));
            } else if (entryValue instanceof List) {
                result.put(entry.getKey(), normalizeList((List<?>) entryValue, translator));
            }
        }
        return result;
    }

    /**
     * Normalizes a forced-value based on specified $type
     */
    private static Object normalizeForcedValue(Object type, Object value, UnaryOperator<String> translator) {
        if (!(type instanceof String)) {
            throw new IllegalArgumentException("Type must be a string");
        }

        String typeStr = ((String) type).toLowerCase();

        switch (typeStr) {
            case "byte":
                return normalizeToByte(value, translator);
            case "boolean":
                return normalizeToBoolean(value, translator);
            case "short":
                return normalizeToShort(value, translator);
            case "int":
            case "integer":
                return normalizeToInt(value, translator);
            case "long":
                return normalizeToLong(value, translator);
            case "float":
                return normalizeToFloat(value, translator);
            case "double":
                return normalizeToDouble(value, translator);
            case "string":
                return normalizeToString(value, translator);
            case "raw":
                return normalizeToRaw(value, translator);
            case "list":
                return normalizeToList(value, translator);
            case "compound":
                return normalize(value, translator);
            case "byte_array":
            case "bytearray":
                return normalizeToByteArray(value, translator);
            case "int_array":
            case "intarray":
                return normalizeToIntArray(value, translator);
            case "long_array":
            case "longarray":
                return normalizeToLongArray(value, translator);
            default:
                throw new IllegalArgumentException("Unknown type: " + typeStr);
        }
    }

    /**
     * Normalizes a list by recursively normalizing its elements
     */
    private static List<?> normalizeList(List<?> list, UnaryOperator<String> translator) {
        List<Object> result = new ArrayList<>(list);
        for (int i = 0; i < result.size(); i++) {
            Object item = result.get(i);
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) item;
                result.set(i, normalize(nested, translator));
            } else if (item instanceof List) {
                result.set(i, normalizeList((List<?>) item, translator));
            }
        }
        return result;
    }

    // Normalization methods for forced values
    private static Byte normalizeToByte(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("b") || str.endsWith("B")) {
                str = str.substring(0, str.length() - 1);
            }
            return Byte.parseByte(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to byte");
    }

    private static Boolean normalizeToBoolean(Object value, UnaryOperator<String> translator) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            str = translator.apply(str);
            str = str.toLowerCase();
            if (str.equals("true")) return true;
            if (str.equals("false")) return false;
            try {
                int num = Integer.parseInt(str);
                return num != 0;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert " + value + " to boolean");
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to boolean");
    }

    private static Short normalizeToShort(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("s") || str.endsWith("S")) {
                str = str.substring(0, str.length() - 1);
            }
            return Short.parseShort(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to short");
    }

    private static Integer normalizeToInt(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("i") || str.endsWith("I")) {
                str = str.substring(0, str.length() - 1);
            }
            return Integer.parseInt(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to int");
    }

    private static Long normalizeToLong(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("l") || str.endsWith("L")) {
                str = str.substring(0, str.length() - 1);
            }
            return Long.parseLong(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to long");
    }

    private static Float normalizeToFloat(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("f") || str.endsWith("F")) {
                str = str.substring(0, str.length() - 1);
            }
            return Float.parseFloat(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to float");
    }

    private static Double normalizeToDouble(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            if (str.endsWith("d") || str.endsWith("D")) {
                str = str.substring(0, str.length() - 1);
            }
            return Double.parseDouble(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to double");
    }

    private static String normalizeToString(Object value, UnaryOperator<String> translator) {
        return translator.apply(value.toString());
    }

    private static Object normalizeToRaw(Object value, UnaryOperator<String> translator) {
        String rawValue = translator.apply(value.toString());
        return new NBTRaw(rawValue);
    }

    private static List<?> normalizeToList(Object value, UnaryOperator<String> translator) {
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Value must be a List");
        }
        return normalizeList((List<?>) value, translator);
    }

    private static byte[] normalizeToByteArray(Object value, UnaryOperator<String> translator) {
        byte[] arr;
        if (value instanceof byte[]) {
            arr = (byte[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToByte(item, translator);
                } else {
                    arr[i] = ((Number) item).byteValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be byte[] or List");
        }
        return arr;
    }

    private static int[] normalizeToIntArray(Object value, UnaryOperator<String> translator) {
        int[] arr;
        if (value instanceof int[]) {
            arr = (int[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToInt(item, translator);
                } else {
                    arr[i] = ((Number) item).intValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be int[] or List");
        }
        return arr;
    }

    private static long[] normalizeToLongArray(Object value, UnaryOperator<String> translator) {
        long[] arr;
        if (value instanceof long[]) {
            arr = (long[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToLong(item, translator);
                } else {
                    arr[i] = ((Number) item).longValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be long[] or List");
        }
        return arr;
    }
}
