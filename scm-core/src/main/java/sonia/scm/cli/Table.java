/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.cli;

import com.google.common.base.Strings;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public final class Table implements Iterable<Table.Row> {

  private final List<String[]> data = new ArrayList<>();

  @Nullable
  private final ResourceBundle bundle;

  Table(@Nullable ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public void addHeaderKeys(String... keys) {
    if (bundle == null) {
      throw new IllegalStateException("no resource bundle found");
    }
    data.add(Arrays.stream(keys).map(bundle::getString).toArray(String[]::new));
  }

  public void addHeader(String... columns) {
    data.add(columns);
  }

  public void addRow(String... row) {
    data.add(row);
  }

  public List<Row> getRows() {
    Map<Integer, Integer> maxLength = calculateMaxLength();

    List<Row> rows = new ArrayList<>();
    for (int r = 0; r < data.size(); r++) {
      String[] rowArray = data.get(r);

      List<Cell> row = new ArrayList<>();
      for (int c = 0; c < rowArray.length; c++) {
        String value = createValueWithLength(Strings.nullToEmpty(rowArray[c]), maxLength.get(c));
        Cell cell = new Cell(c == 0, c + 1 == rowArray.length, c, value);
        row.add(cell);
      }
      rows.add(new Row(r == 0, r + 1 == data.size(), r, row));
    }

    return rows;
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
