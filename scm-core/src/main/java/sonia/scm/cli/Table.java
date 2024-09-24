/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.cli;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This table can be used to display table-like command output
 * @since 2.33.0
 */
public final class Table implements Iterable<Table.Row> {

  private static final String DEFAULT_LABEL_VALUE_SEPARATOR = ": ";

  private final List<String[]> data = new ArrayList<>();

  @Nullable
  private final ResourceBundle bundle;

  Table(@Nullable ResourceBundle bundle) {
    this.bundle = bundle;
  }

  /**
   * Sets the table headers.
   * You can use resource keys which will be translated using the related resource bundle.
   * @param keys actual names or resource keys for your table header
   */
  public void addHeader(String... keys) {
    data.add(Arrays.stream(keys).map(this::getLocalizedValue).toArray(String[]::new));
  }

  /**
   * Add a single row of values to the table.
   * @param row values for a single table row
   */
  public void addRow(String... row) {
    data.add(row);
  }

  /**
   * Creates a table entry with two columns separated by {@link #DEFAULT_LABEL_VALUE_SEPARATOR}.
   * @param label label for the left table column
   * @param value value for the right table column
   */
  public void addLabelValueRow(String label, String value) {
    addLabelValueRow(label, value, DEFAULT_LABEL_VALUE_SEPARATOR);
  }

  /**
   * Creates a table entry with two columns separated by the given separator.
   * @param label label for the left table column
   * @param value value for the right table column
   * @param separator separator used to separate the label from the value
   */
  public void addLabelValueRow(String label, String value, String separator) {
    addRow(getLocalizedValue(label) + separator, value);
  }

  /**
   * Returns a list of the table rows.
   * This is required for the internal table implementation.
   * @return a list of the table rows
   */
  public List<Row> getRows() {
    Map<Integer, Integer> maxLength = calculateMaxLength();

    List<Row> rows = new ArrayList<>();
    for (int r = 0; r < data.size(); r++) {
      String[] rowArray = data.get(r);

      List<Cell> cells = new ArrayList<>();
      Row row = new Row(r == 0, r + 1 == data.size(), r, cells);
      for (int c = 0; c < rowArray.length; c++) {
        String value = createValueWithLength(Strings.nullToEmpty(rowArray[c]), maxLength.get(c));
        Cell cell = new Cell(row, c == 0, c + 1 == rowArray.length, c, value);
        cells.add(cell);
      }
      rows.add(row);
    }

    return rows;
  }

  private String getLocalizedValue(String key) {
    if (bundle != null && bundle.containsKey(key)) {
      return bundle.getString(key);
    }
    return key;
  }

  private String createValueWithLength(String value, int length) {
    if (value.length() < length) {
      StringBuilder builder = new StringBuilder(value);
      for (int j = value.length(); j < length; j++) {
        builder.append(" ");
      }
      return builder.toString();
    }
    return value;
  }

  private Map<Integer, Integer> calculateMaxLength() {
    Map<Integer, Integer> maxLength = new HashMap<>();
    for (String[] row : data) {
      for (int i = 0; i < row.length; i++) {
        int currentMaxLength = maxLength.getOrDefault(i, 0);
        String col = row[i];
        int length = col != null ? col.length() : 0;
        if (length > currentMaxLength) {
          maxLength.put(i, length);
        }
      }
    }
    return maxLength;
  }

  @Override
  public Iterator<Row> iterator() {
    return getRows().iterator();
  }

  @Value
  public class Cell {

    Row row;
    boolean first;
    boolean last;
    int index;
    String value;

  }

  @Value
  public class Row {

    boolean first;
    boolean last;
    int index;
    List<Cell> cols;

  }
}
