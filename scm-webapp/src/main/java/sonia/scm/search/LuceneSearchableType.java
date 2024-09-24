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

package sonia.scm.search;

import com.google.common.base.Strings;
import jakarta.annotation.Nonnull;
import lombok.Value;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;

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
  boolean namespaceScoped;

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
    this.namespaceScoped = annotation.namespaceScoped();
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
    return repositoryScoped || namespaceScoped;
  }
}
