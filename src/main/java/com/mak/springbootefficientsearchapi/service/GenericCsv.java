package com.mak.springbootefficientsearchapi.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.pivovarit.function.ThrowingFunction.unchecked;

public abstract class GenericCsv<T> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CELL_SEPARATOR = ";";

    private final Class<T> clazz;

    protected GenericCsv(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return this.clazz;
    }


    public List<T> parseCsvFile(MultipartFile multipartFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8));

        List<String> header = Arrays.asList(bufferedReader.readLine().split(CELL_SEPARATOR));
        Map<String, Method> setMap = setterMethodsMap();
        return getList(bufferedReader, header, setMap);
    }

    private List<T> getList(BufferedReader bufferedReader, List<String> header, Map<String, Method> setMap) {
        return bufferedReader.lines()
                .parallel()
                .map(line -> line.split(CELL_SEPARATOR))
                .map(lineValues -> IntStream.range(0, lineValues.length)
                        .boxed()
                        .collect(Collectors.toMap(header::get, i -> lineValues[i])))
                .peek(mapcar -> mapcar.remove("id"))
                .map(unchecked(mapcar2 -> createCar(mapcar2, setMap)))
                .collect(Collectors.toList());
    }

    private T createCar(Map<String, String> mapEntity, Map<String, Method> setMap) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        T t = instantiateObject(this.clazz);
        for (Map.Entry<String, String> entry : mapEntity.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            Method method = setMap.get(k);
            method.invoke(t, convert(method.getParameterTypes()[0], v));
        }
        return t;
    }

    private Object convert(Class<?> targetType, String text) {
        if (targetType.toString().contains("LocalDate")) {
            return LocalDate.parse(text, FORMATTER);
        }
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    private Map<String, Method> setterMethodsMap() {
        return Arrays.stream(getClazz().getMethods())
                .filter(method -> method.getName().startsWith("set") && !"getClass".equals(method.getName()))
                .collect(Collectors.toMap(this::getColumnName, method -> method));
    }

    private String getColumnName(Method method) {
        return StringUtils.uncapitalize(method.getName().substring(3));
    }


    public <T> T instantiateObject(Class<T> clazz) throws IllegalAccessException, InstantiationException {
//        T t;
//      t = new T();   // This is what you want to do but will cause compile error.
//      return    t = cls.newInstance();  // use reflection to create instance
        return clazz.newInstance();  // use reflection to create instance
    }


    public Resource generateCsvFile(List<T> clazz) throws IOException {
        List<String> noNeededColumn = new ArrayList<>();
        noNeededColumn.add("id");
        List<Method> methods = getMethodsList(clazz, noNeededColumn);
        String[] extractHeader = getHeader(methods);

        StringWriter st = new StringWriter();
        try (ICsvMapWriter mapWriter = new CsvMapWriter(st, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
            mapWriter.writeHeader(extractHeader);
            for (final T car : clazz) {
                Map<String, Object> mapPrices = buildMapLine(methods, car);
                mapWriter.write(mapPrices, extractHeader, new CellProcessor[extractHeader.length]);
            }
        }
        return (new InputStreamResource(new ByteArrayInputStream(st.toString().getBytes(StandardCharsets.UTF_8))));
    }


    private Map<String, Object> buildMapLine(List<Method> methods, T line) {
        return methods.stream()
                .parallel()
                .collect(Collectors.toMap(this::getColumnName,
                        unchecked(method -> line.getClass().getMethod(method.getName()).invoke(line))));
    }


    private List<Method> getMethodsList(List<T> clazz, List<String> noNeededColumn) {
        return Arrays.stream(clazz.get(0).getClass().getMethods())
                .parallel()
                .filter(method -> method.getName().startsWith("get")
                        && !"getClass".equals(method.getName())
                        && !noNeededColumn.contains(getColumnName(method)))
                .sorted(Comparator.comparing(this::getColumnName))
                .collect(Collectors.toList());
    }

    private String[] getHeader(List<Method> methods) {
        return methods.stream()
                .map(this::getColumnName)
                .toArray(String[]::new);
    }
}
