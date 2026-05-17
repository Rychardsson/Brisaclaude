package com.example.brisa.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ExcelImportHelper {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy")
    );

    private final DataFormatter dataFormatter = new DataFormatter(new Locale("pt", "BR"));

    public Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        if (headerRow == null) {
            return headers;
        }

        for (Cell cell : headerRow) {
            String normalized = normalizeHeader(getStringValue(cell));
            if (!normalized.isBlank()) {
                headers.putIfAbsent(normalized, cell.getColumnIndex());
            }
        }

        return headers;
    }

    public Integer findColumn(Map<String, Integer> headers, List<String> aliases, Integer fallbackIndex) {
        for (String alias : aliases) {
            Integer index = headers.get(normalizeHeader(alias));
            if (index != null) {
                return index;
            }
        }
        return fallbackIndex;
    }

    public String getString(Row row, Integer index) {
        if (row == null || index == null) {
            return null;
        }
        return trimToNull(getStringValue(row.getCell(index)));
    }

    public Integer getInteger(Row row, Integer index) {
        Double value = getDouble(row, index);
        return value == null ? null : value.intValue();
    }

    public Double getDouble(Row row, Integer index) {
        if (row == null || index == null) {
            return null;
        }

        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }

        String text = trimToNull(getStringValue(cell));
        if (text == null) {
            return null;
        }

        String normalized = text.replace(".", "").replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public LocalDate getDate(Row row, Integer index) {
        if (row == null || index == null) {
            return null;
        }

        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        String text = trimToNull(getStringValue(cell));
        if (text == null) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        short lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) {
            return true;
        }

        for (int i = 0; i < lastCellNum; i++) {
            if (trimToNull(getStringValue(row.getCell(i))) != null) {
                return false;
            }
        }

        return true;
    }

    public String normalizeHeader(String value) {
        return normalize(value).replaceAll("[^a-z0-9]+", "");
    }

    public String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    public String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }

        return dataFormatter.formatCellValue(cell);
    }
}
