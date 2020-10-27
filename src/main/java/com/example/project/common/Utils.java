package com.example.project.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.time.DateUtils.parseDate;
import static org.springframework.util.ReflectionUtils.FieldCallback;
import static org.springframework.util.ReflectionUtils.doWithFields;

@Slf4j
public class Utils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public static String generateInvoiceId() {
        return String.valueOf(System.currentTimeMillis()).substring(1, 13);
    }

    public static <T> boolean contains(Collection<T> c1, Collection<T> c2) {
        if (!c1.isEmpty() && !c2.isEmpty()) {
            for (T o : c1) {
                if (c2.contains(o)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String mobile66(String msisdn) {
        return msisdn.startsWith("0") ? "66" + msisdn.substring(1) : msisdn;
    }

    public static String mobile66Remove(String msisdn) {
        return msisdn.startsWith("66") ? "0" + msisdn.substring(2) : msisdn;
    }

    public static String mobile66Dash(String mobile) {
        if (mobile.startsWith("66"))
            return "66-" + mobile.substring(2);
        else if (mobile.startsWith("0"))
            return "66-" + mobile.substring(1);
        return mobile;
    }

    public static String newMobileFormat(String mobile) {
        if (mobile.indexOf("-") >= 0) {
            return mobile;
        } else {
            return mobile66Remove(mobile);
        }
    }

    public static boolean isExpired(Date date) {
        Date now = new Date();
        return now.after(date);
    }


    public static <T> void sort(List<T> list, Class<T> clazz) {
        Sort.sort(list, clazz);
    }

    public static String idByTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String string(String message, Object... arguments) {
        FormattingTuple arrayFormat = MessageFormatter.arrayFormat(message, arguments);
        return arrayFormat.getMessage();
    }

    public static String string(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }

    public static String stringLong(Date date) {
        DateFormat format = DateFormat.getDateInstance(DateFormat.FULL);
        return format.format(date);
    }

    public static String string(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String printJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();

        String s = null;
        try {
            s = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String getNameFromEmail(String email) {
        try {
            return email.split("@")[0];
        } catch (Exception e) {
            return email;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void trimAll(final Object obj) {
        if (obj == null) return;

        final Class c = obj.getClass();
        final Method[] methods = c.getMethods();
        final Class[] SETTER_ARGS = new Class[]{String.class};
        final Object[] SETTER_VAL = new Object[1];
        final String SET = "set";
        final String GET = "get";
        final String SPACE = "\u0020";
        final String TAB = "\t";

        for (final Method m : methods) {
            try {
                final String name = m.getName();
                if (name.length() > GET.length()
                        && name.indexOf(GET) == 0
                        && m.getReturnType().equals(String.class)
                        && m.getParameterTypes().length == 0) {
                    final String v = (String) m.invoke(obj);
                    if (v != null && (v.contains(SPACE) || v.contains(TAB))) {
                        final Method setter = c.getMethod(SET + name.substring(3), SETTER_ARGS);
                        if (setter != null) {
                            SETTER_VAL[0] = v.trim();
                            setter.invoke(obj, SETTER_VAL);
                            log.debug("trim {} '{}'", v, SETTER_VAL[0]);
                        }
                    }
                }
            } catch (final Throwable e) {
                log.info("trimAll error:" + obj, e);
            }
        }
    }

    public static Map<String, String> beanToMap(Object model) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = model.getClass().getDeclaredFields();

        for (Field field : fields) {
            Name annotation = field.getAnnotation(Name.class);
            String name = annotation != null ? annotation.value() : field.getName();
            try {
                field.setAccessible(true);
                Object value = field.get(model);
                map.put(name, value != null ? value.toString() : "");
            } catch (Exception e) {
                log.debug("toMap error {} = {}", name, e.getMessage());
            }
        }

        return map;
    }

    public static MultiValueMap<String, String> beanToMultiValueMap(final Object model) {
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        final Class<?> myClass = model.getClass();

        doWithFields(myClass, new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                Name annotation = field.getAnnotation(Name.class);
                String name = annotation != null ? annotation.value() : field.getName();
                try {
                    Object value = PropertyUtils.getProperty(model, field.getName());
                    if (value != null) {
                        map.add(name, value.toString());
                    }
                } catch (InvocationTargetException | NoSuchMethodException e) {
                    log.debug("Reflection fail", e);
                }
            }
        });

        return map;
    }

    public static boolean isBetween(Date refTime, Date start, Date end) {
        return start.before(refTime) && end.after(refTime);
    }

    public static int closest(int of, List<Integer> in) {
        int min = Integer.MAX_VALUE;
        int closest = of;

        for (int v : in) {
            final int diff = Math.abs(v - of);

            if (diff < min) {
                min = diff;
                closest = v;
            }
        }

        return closest;
    }

    private static String getType(Object o) {
        try {
            if (o instanceof String)
                return "String";
            Integer.parseInt(o.toString());
            return "Integer";
        } catch (Exception e) {
            return "Float";
        }
    }


    public static String stringISO8601(Date date) {
        return stringISO8601(date, TimeZone.getDefault());
    }

    public static String stringISO8601(Date date, TimeZone timezone) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(timezone);
        return df.format(date);
    }

    public static Date toDate(ZonedDateTime zdt) {
        return Date.from(zdt.toInstant());
    }

    public static Date toDate(java.time.LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(java.time.Instant instant) {
        return Date.from(instant);
    }

    public static Date toDate(java.time.LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static java.time.LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static java.time.LocalDateTime toLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(String date, String pattern) {
        try {
            return parseDate(date, pattern);
        } catch (ParseException e) {
        }
        return null;
    }

    public static String dateToString(Date date, String pattern) {
        try {
            DateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        } catch (Exception e) {
        }
        return null;
    }

    public static Date toMidnight(Date date) {
        return new DateMidnight(date).toDate();
    }

    public static Date toSearchStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // add 7 hours
        calendar.add(Calendar.HOUR, 7);
        return calendar.getTime();
    }

    public static Date toSearchEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // set end time of day
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        // add 7 hours
        calendar.add(Calendar.HOUR, 7);
        return calendar.getTime();
    }

    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<>();
        try {
            String[] params = query.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
        } catch (Exception e) {
            log.warn("getQueryMap error:{}", e.getMessage());
        }
        return map;
    }

    public static int getPercent(float price, float chargingPrice) {
        try {
            return (int) (100 - (chargingPrice / price) * 100);
        } catch (Exception e) {
        }
        return 0;
    }

    public static String getFile(String name) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Cannot find resource on classpath: '" + name + "'");
        }
        return url.getFile();
    }

    public static boolean isEmpties(Object... value) {
        for (Object object : value) {
            if (object != null) {
                if (object instanceof String) {
                    if (org.springframework.util.StringUtils.hasText((String) object)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public void doit() {
        LocalDate localDate = new LocalDate();
        System.out.println("localDate = " + localDate);
    }

    public static List<LocalDate> asDateList(String[] dates) {
        List<LocalDate> results = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        for (String date : dates) {
            results.add(LocalDate.parse(date, formatter));
        }
        return results;
    }

    public static List<LocalTime> asTimeList(String csvTime) {
        List<LocalTime> results = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
        String[] split = csvTime.split(",");
        for (String timeValue : split) {
            results.add(LocalTime.parse(timeValue.trim(), fmt));
        }
        return results;
    }


    public static String join(String joiner, Object... parts) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; ++i) {
            builder.append(parts[i] != null ? parts[i] : "");
            if (i < parts.length - 1) {
                builder.append(joiner);
            }
        }

        return builder.toString();
    }

    public static Date today00() {
        return DateMidnight.now().toDate();
    }

    public static Date today2359() {
        return DateMidnight.now().plusDays(1).toDate();
    }

    public enum ItemExpireType {
        FIX, PLUS_DAY
    }

    public static Date calculateItemExpire(ItemExpireType expireType, String value) {
        if (expireType == ItemExpireType.FIX) {
            LocalDate date = LocalDate.parse(value, DateTimeFormat.forPattern("yyyyMMdd"));
            return DateUtils.addSeconds(date.plusDays(1).toDate(), -1);
        } else {
            return DateUtils.addSeconds(LocalDate.now().plusDays(Integer.parseInt(value) + 1).toDate(), -1);
        }
    }

    public static String toLocalDateFormat(String orgDate) {
        String[] splitDate = orgDate.split("[/-]", 3);
        String d = splitDate[0];
        String m = splitDate[1];
        String y = splitDate[2];

        String date = StringUtils.leftPad(splitDate[0], 2, '0');
        String month = StringUtils.leftPad(splitDate[1], 2, '0');

        Integer yearInt = Integer.valueOf(splitDate[2]);

        String year = yearInt > 2400 ? String.valueOf(yearInt - 543) : String.valueOf(yearInt);

        return date + "-" + month + "-" + year;
    }

    public boolean isValidFileName(final String aFileName) {
        final File aFile = new File(aFileName);
        boolean isValid = true;
        try {
            if (aFile.createNewFile()) {
                aFile.delete();
            }
        } catch (IOException e) {
            isValid = false;
        }
        return isValid;
    }

    public static double convertAppVersion(String gdVersion) {
        double defaultVersion = 360;
        try {
            String version = gdVersion.replace(".", "");
            defaultVersion = Double.valueOf(version);
        } catch (Exception e) {
            log.warn("cannot convert app version {}", gdVersion);
        }
        return defaultVersion;
    }

}

