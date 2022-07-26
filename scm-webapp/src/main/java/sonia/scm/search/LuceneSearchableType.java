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

package sonia.scm.search;

import com.google.common.base.Strings;
import lombok.Value;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class LuceneSearchableType implements SearchableType {

  private static final float DEFAULT_BOOST = 1f;

  Class<?> type;
  String name;
  String permission;
  List<LuceneSearchableField> fields;
  String[] defaultFieldNames;
  Map<String, Float> boosts;
  Map<String, PointsConfig> pointsConfig;
  TypeConverter typeConverter;
  boolean repositoryScoped;

  public LuceneSearchableType(Class<?> type, @Nonnull IndexedType annotation, List<LuceneSearchableField> fields) {
    this.type = type;
    this.name = Names.create(type, annotation);
    this.permission = Strings.emptyToNull(annotation.permission());
    this.fields = fields;
    this.defaultFieldNames = defaultFieldNames(fields);
    this.boosts = boosts(fields);
    this.pointsConfig = pointsConfig(fields);
    this.typeConverter = TypeConverters.create(type);
    this.repositoryScoped = annotation.repositoryScoped();
  }

  public Optional<String> getPermission() {
    return Optional.ofNullable(permission);
  }

  private String[] defaultFieldNames(List<LuceneSearchableField> fields) {
    return fields.stream()
      .filter(LuceneSearchableField::isDefaultQuery)
      .map(LuceneSearchableField::getName)
      .toArray(String[]::new);
  }

  private Map<String, Float> boosts(List<LuceneSearchableField> fields) {
    Map<String, Float> map = new HashMap<>();
    for (LuceneSearchableField field : fields) {
      if (field.isDefaultQuery() && field.getBoost() != DEFAULT_BOOST) {
        map.put(field.getName(), field.getBoost());
      }
    }
    return Collections.unmodifiableMap(map);
  }

  private Map<String, PointsConfig> pointsConfig(List<LuceneSearchableField> fields) {
    Map<String, PointsConfig> map = new HashMap<>();
    for (LuceneSearchableField field : fields) {
      PointsConfig config = field.getPointsConfig();
      if (config != null) {
        map.put(field.getName(), config);
      }
    }
    return Collections.unmodifiableMap(map);
  }

  @Override
  public Collection<LuceneSearchableField> getFields() {
    return Collections.unmodifiableCollection(
      fields.stream()
        .filter(LuceneSearchableField::isSearchable)
        .collect(Collectors.toList())
    );
  }

  public Collection<LuceneSearchableField> getAllFields() {
    return Collections.unmodifiableCollection(fields);
  }

  @Override
  public boolean limitableToRepository() {
    return repositoryScoped;
  }

  @Override
  public boolean limitableToNamespace() {
    return repositoryScoped;
  }
}
