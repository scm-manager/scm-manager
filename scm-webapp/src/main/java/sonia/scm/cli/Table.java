package sonia.scm.cli;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import sonia.scm.repository.Repository;

import javax.annotation.Nullable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Table implements Iterable<Table.Row> {

  private final List<String[]> data = new ArrayList<>();

  @Nullable
  private final ResourceBundle bundle;

  public Table(@Nullable ResourceBundle bundle) {
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
        String value = createValueWithLength(rowArray[c], maxLength.get(c));
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
        int length = col.length();
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
