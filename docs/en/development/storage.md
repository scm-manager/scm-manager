---
title: Architecture of the Persistence Layer with Embedded SQLite
---

# Introduction

In SCM-Manager, data outside the actual repositories has been stored in XML files since version 2.0. For this purpose, a
persistence layer was developed that allows various types of data to be stored. It is also possible to choose whether
data should be stored globally or associated with a repository.

This type of storage has generally proven effective and offers several advantages (data is stored with the repository,
easy troubleshooting, simple backup, etc.). However, with large amounts of data or frequently changing data, this
architecture reaches its limits. Several optimizations have been made (e.g., through caches), but the fundamental
limitations remain. In particular, searches are difficult because many files have to be read and processed.

It was therefore necessary to look for new possibilities. The fundamental advantages of SCM-Manager should remain,
especially the easy installation, simple operation, the ability to easily transfer repositories between different
instances through export and import, and not least the easy use of the persistence layer by plugins.

It quickly became clear that using a database system would be a sensible alternative. However, it was uncertain whether it
should be a "classic" database or a NoSQL database. XML storage had proven to be very helpful, as the only prerequisite for
a persistent data type was the use of JaxB annotations. However, a widely recognized technology should also be used.

The choice finally fell on SQLite. This system is available for almost every platform and databases can be used "
embedded", so no separate server process is needed. The deciding factor was the performance, which is also present with
embedded JSON data.

The next point to clarify was how the abstraction should look. It was clear from the beginning that plugins should not
directly access databases via SQL. Rather, the API for persistence should be oriented towards the XML-based solution.

The following sections introduce the most important concepts.

# Important Components of the Architecture

## Objectives

The following aspects were decisive in introducing the new persistence layer:

- Primarily, an alternative should be developed for the existing Data Store, as most data is stored there.
  Configuration stores are unlikely to pose a performance problem.
- The specific choice of database should not be noticeable in the API, so that a change of the specific technology
  remains possible in principle.
- The API for using the new persistence layer should be as similar as possible to the existing API. In particular, it
  should not be necessary to create a mapping from the entities to be stored to a database schema (like an OR mapping).
- As an extension to the XML layer, it should be possible to store data not only globally or related to a repository or
  namespace but also to allow other hierarchies (even those that may only arise through plugins and cannot be
  anticipated in the API).
- Unlike XML persistence, queries should be possible that span multiple entries. Additionally, for entities assigned to
  individual repositories, queries should also be able to cross repository boundaries.
- The API should, especially for queries, provide the best possible options, such as which fields can be searched and
  which operators are possible for these fields.
- The previous functions such as export and import with metadata, update steps, and automatic data cleanup, 
  for example when deleting a repository, should remain available.
- A switch from the old to the new persistence layer should be as simple as possible.
- The principle "All data belonging to a repository is in a single directory" can be relaxed for performance reasons.

## Annotations and API Generation

To achieve the best possible "Developer Experience", code generation is used. This is triggered by using a new
annotation for persistent entities.

### The "Queryable Type"

The `@QueryableType` annotation is the central element of the persistence architecture. It allows classes to be marked
for use in SQL-based database queries. In the annotation, so-called parent classes can be listed to which the entities
should later belong. For a repository-related type, the `Repository` class must be entered here. Multiple classes can
also be specified here in the sense of a hierarchy (e.g., a comment can belong to a pull request, which in turn belongs
to a repository).

For such marked classes, additional classes are automatically generated: a Store Factory and a class with constants for
the individual fields that can be used in queries (the "Query Fields").

### Store Factories and Stores

The generated Store Factories are similar to the known `DataStoreFactory`. Unlike the generic `DataStoreFactory`,
however, specific methods are created here based on the parent classes mentioned in the annotation.

To create and change data, specific IDs must be specified to access the store if parent classes have been defined. This
store implements the known Store API (the `DataStore` interface), so no adjustments are needed in the application.

### Queryable Store

For more advanced queries that also extend beyond the boundaries of the parent classes, there is a new store with a new
API, the `QueryableStore`. This offers a `query` function in which conditions can be specified and a query can be
started. The conditions are based on the generated Queryable Fields described below.

### Queryable Mutable Store

To store, delete, and change data, a new store with the `QueryableMutableStore` API is used. This API
extends `QueryableStore` and `DataStore` to allow both queries and changes to stored objects. In contrast to the
pure Queryable Store, it is mandatory to specify all parents to create a mutable store. This is needed so that new
entities can be assigned to the correct parent(s).

Deleting objects in mutual stores can be done by either selecting all elements to be deleted with a query and 
execute the `deleteAll()` function of the API or using a `retain(long keptElements)`. The latter is the recommended way 
to implement FIFO *(first in, first out)* lists, which is especially intended for managing log entries.
Take this example: You are maintaining a display of when your spaceships received their maintenance.

```java
spaceshipStore
  .query(Spaceship.SPACESHIP_INSERVICE
    .after(Instant.now().minus(5, ChronoUnit.DAYS)))
  .orderBy(Order.DESC)
  .retain(10);
```

With this code snippet, you will delete all alements older than five days and only keep 10 newest ones 
matching this criterion. 

### Queryable Maintenance Store

The `QueryableMaintenanceStore` is responsible for maintenance tasks,
such as deleting all data of a specific type or updating stored JSON data.

- One use case is deleting a parent ID (e.g., repository ID):

  For example, if a repository is deleted, all entries with this ID as the parent ID must also be removed. This automatic
  cleanup is ensured by the `QueryableMaintenanceStore`.
  With the `clear()` function, all entries of a specific type can be specifically removed.

- Another use case is "update steps". Here, all entries of a store can be iterated and potentially updated or deleted using
  the `QueryableMaintenanceStore`.

### Queryable Fields

The individually generated Queryable Fields for a "Queryable Type" are a collection of constants that can be used to
define conditions for queries over Queryable Stores. For all attributes of the Queryable Type with supported data types,
a corresponding constant is generated. These offer functions for operators such as equality, greater and less for scalar
values, or "contains" for collections, depending on the data type.

The generated store factories described in the previous section restrict the usable queryable fields per generic to 
prevent incorrect queries from being created.

### Queryable Type Annotation Processor

The `QueryableTypeAnnotationProcessor` is an annotation processor that automatically generates SQL-related classes
during compilation. It identifies classes annotated with `@QueryableType` and creates corresponding `QueryField` classes
and Store Factories.

Functions:

- Identification of classes annotated with `@QueryableType`
- Generation of Query Field classes and Store Factories

## Implementation in the Database

When the SCM-Manager starts, an embedded SQLite database is set up. This is stored in the `scm.db` file in the SCM home
directory. Additionally, during startup, a table is created in the database for each queryable type if it does not already exist. 
Each table includes the following columns:

- A column for the ID of each parent level
- A column for the ID of the actual entity
- A column containing the entity converted to JSON

### Rules for the Database Structure

- The existing table structure must not be changed.
- No new parent classes (parents) may be added to or removed from an existing entity.
- The JSON data within the existing column may be updated to make changes to the stored entities.

These restrictions ensure that the integrity of the database structure is maintained and migrations can be performed
without manual adjustments to the schema definition.

### Table Creation with the TableCreator

The `TableCreator` class is responsible for creating and validating the table structure. It checks whether a table
exists and whether the required columns (ID, JSON, and specific columns for the parents) are present.

The implementation ensures that only consistent table structures are created and used.

### Implementation of StoreFactory and Stores

#### SQLiteQueryableStoreFactory

The `SQLiteQueryableStoreFactory` class is the concrete implementation of `QueryableStoreFactory` for SQLite databases.

Functions:

- Management of SQLite database connections:
  - Connects the application to the SQLite database (`scm.db`).
  - Ensures that the connection is correctly opened and closed.
- Table initialization:
  - Tables are automatically created based on the metadata of `@QueryableType`.
- Creation of stores:
  - Supports both reading (`QueryableStore`) and writing (`QueryableMutableStore`) stores as well as stores for
    maintenance (`QueryableMaintenanceStore`).

#### SQLiteQueryableStore

`SQLiteQueryableStore` is a generic implementation of `QueryableStore` that abstracts SQL logic and seamlessly
integrates into the persistence architecture.

Purpose and scope:

- Abstraction of SQL logic:
  - Developers define queries in an object-oriented manner without having to write SQL directly.
- Integration with SQLite:
  - Uses a JDBC connection to perform database operations.
- Data management:
  - Supports reading queries on persisted data defined by annotations such as `@QueryableType`.
- Architecture and operation:
  - Metadata integration:

    Uses `QueryableTypeDescriptor` to interpret table structure and relationships.

    *Note:* The parents of an already existing `QueryableType` must not be changed (new ones added or old ones removed)
    as this would differ from the existing database structure and could lead to errors.
    Declarative queries: Queries are created and internally translated into SQL. Results are mapped to objects of type
    T.

#### SQLiteStoreMetadataProvider

The `SQLiteStoreMetaDataProvider` class serves as a provider of metadata for stored types within the SQLite database. It
manages the mapping of stored entity types to their respective parent types and provides mechanisms for querying this
information. The use case is to be able to recognize which tables are repository-related, i.e., which tables have a
repository as a parent.

Functions:

- Loading metadata:

  When initializing, all types annotated with `@QueryableType` are loaded and registered.
  The information comes from the `PluginLoader` and is organized based on the specified parent classes.
- Management of the type hierarchy:

  Stores the mapping between parent types and their subordinate types in a map.
- Retrieval of types based on parent classes:

  Provides a method for querying all entity types associated with a specific parent class.
  Uses a mapping list (`Map<Collection<String>, Collection<Class<?>>>`) to enable efficient searching for stored types.

This class is essential for the correct management of stored data types in the SQLite database and ensures that the data
hierarchy can be correctly built and queried.

#### StoreDeletionNotifier

The `StoreDeletionNotifier` interface serves as an extension point (`@ExtensionPoint`) to notify components about the
deletion of persisted objects.

Functions:

- Registration of deletion handlers:

  Allows the registration of `DeletionHandler` instances that should be notified when a stored object is deleted.
- Notification of deleted entities:

  `DeletionHandler` can receive deletion events and react to them.

  Supports both single and multiple objects to be deleted.

Inner components:

- `DeletionHandler`
  Is notified when an object is removed from the store.

This interface is essential to ensure consistent management of deleted entities and can be used, for example, to remove
dependent data or perform actions after an object is deleted from the store.

## Testability

To support unit tests, there is an extension for JUnit Jupiter, the `QueryableStoreExtension`. In a unit test, this must
be specified in a JUnit extension annotation. Additionally, the test class must be annotated
with `QueryableStoreExtension#QueryableTypes` to specify which types are needed in the test. Subsequently, it is
possible to obtain store factories via parameters to test methods (or also to methods annotated with `@BeforeEach`).

# Examples

## Using the New Queryable Store API

First, a data type must be marked as a "Queryable Type":

```java
import lombok.Data;
import sonia.scm.store.QueryableType;

@Data
@QueryableType
public class MyEntity {
  private String id;
  private String name;
  private String alias;
  private int age;
  private List<String> tags;
}
```

In this example, the entity has no relation to parent elements. The `@QueryableType` annotation is sufficient to store
the entity in the database.  During compilation, the following classes are automatically generated:  

- `MyEntityQueryFields`: Constants for the fields that can be used in queries
- `MyEntityStoreFactory`: Factory for accessing the store

Using these classes, data can then be stored and queried as shown in the following example:

```java
public class Demo {

  private final MyEntityStoreFactory storeFactory;

  @Inject
  public Demo(MyEntityStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public String create(String name, int age, List<String> tags) {
    MyEntity entity = new MyEntity();
    entity.setName(name);
    entity.setAge(age);
    entity.setTags(tags);

    QueryableMutableStore<MyEntity> store = storeFactory.getMutable();
    return store.put(entity);
  }

  public MyEntity readById(String id) {
    QueryableMutableStore<MyEntity> store = storeFactory.getMutable();
    return store.get(id);
  }

  public Collection<MyEntity> findByAge(int age) {
    QueryableStore<MyEntity> store = storeFactory.get();
    return store.query(MyEntityQueryFields.AGE.eq(age)).findAll();
  }

  public Collection<MyEntity> findByName(String name) {
    QueryableStore<MyEntity> store = storeFactory.get();
    return store.query(
      Conditions.or(
        MyEntityQueryFields.NAME.eq(name),
        MyEntityQueryFields.ALIAS.eq(name)
      )
    ).findAll();
  }

  public Collection<MyEntity> findByTag(String tag) {
    QueryableStore<MyEntity> store = storeFactory.get();
    return store.query(MyEntityQueryFields.TAGS.contains(tag)).findAll();
  }
}
```

## Using the Queryable Store API with Parent Element

Consider the following example with a parent element where we want to store multiple contacts for a user:

```java
@Data
@QueryableType(User.class)
public class Contact {
  private String mail;
}
```

For entities with parent elements, queries can be made both for specific parents and across all parents.

```java
public class Demo {

  private final ContactStoreFactory storeFactory;

  @Inject
  public Demo(ContactStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void addContact(User user, String mail) {
    QueryableMutableStore<Contact> store = storeFactory.getMutable(user);
    Contact contact = new Contact();
    contact.setMail(mail);
    store.put(contact);
  }

  /** Get contact for a single user. */
  public Collection<Contact> getContacts(User user) {
    QueryableMutableStore<Contact> store = storeFactory.getMutable(user);
    return store.getAll().values();
  }

  /** Get all contacts for all users. */
  public Collection<Contact> getAllContacts() {
    QueryableStore<Contact> store = storeFactory.getOverall();
    return store.query().findAll();
  }
}
```

In this example, all `Contact` entries will be deleted, when the related `User` is deleted. This works out-of-the-box
for all entities whose top level parent is a `User`, a `Group`, or a `Repository`. You can build this behavior for your
own parent types by implementing a `StoreDeletionNotifier` as an extension. Best take a look at the `GroupDeletionNotifier`
for an example:

```java
@Extension
public class GroupDeletionNotifier implements StoreDeletionNotifier {
  private DeletionHandler handler;

  @Override
  public void registerHandler(DeletionHandler handler) {
    this.handler = handler;
  }

  @Subscribe(referenceType = ReferenceType.STRONG)
  public void onDelete(GroupEvent event) {
    if (handler != null && event.getEventType() == HandlerEventType.DELETE) {
      handler.notifyDeleted(Group.class, event.getItem().getId());
    }
  }
}
```

## Handling of IDs

If you have an entity that has an id field, you can use the `@Id` annotation to mark this field as the ID of the entity.
This field must be of type `String`. If such a field is present, the store will automatically use it as the ID of the
entity. This means that

- if an entity is stored using the put method without explicit ID parameter (`DataStore#put(T)`), the store will check
  if the ID field has a non-null value. If this is the case, this value will be used as the ID. If the ID field is
  null, a new ID will be generated and assigned to the annotated field of the entity.
- if an entity is stored using the put method with an explicit ID parameter (`DataStore#put(String, T)`), this ID
  will be used to store the entity. The ID field of the entity will be set with this given ID.

Please note that if you change the ID field of an entity after it has been stored, the store will not automatically
update the ID in the store. You must explicitly call the `put` method with the new ID to store the entity with the
new ID and remove the old entry with the old ID.

```java
import lombok.Data;
import sonia.scm.store.QueryableType;
import sonia.scm.store.Id;

@Data
@QueryableType
public class MyEntity {
  @Id
  private String id;
  private String name;
  private String alias;
  private int age;
  private List<String> tags;
}
```

## Generated IDs with auto-increment

If you want to use auto-generated IDs, you can set the `idGenerator` property of the `@QueryableType` annotation to
`IdGenerator.AUTO_INCREMENT`. This will cause the store to generate a numerical, incremented ID for each entity when it
is stored (and no explicit ID is set). Note that this ID will be a `String` representation of the numerical value, so it
can still be used as a `String` ID in the store. The ID will start at 1 and increment for each new entity stored.

```java
import lombok.Data;
import sonia.scm.store.QueryableType;
import sonia.scm.store.Id;
import sonia.scm.store.IdGenerator;

@Data
@QueryableType(idGenerator = IdGenerator.AUTO_INCREMENT)
public class MyEntity {
  private String name;
}
```

This feature cannot be used in combination with an explicit ID field annotated with `@Id`.

## Update Steps

Update steps can be used to update data in the database. The following example shows how to update all entities of a
specific type. For this let's assume, that we want to add a `type` field to the `Contact` entity from the previous
example:

```java
@Data
@QueryableType(User.class)
public class Contact {
  private String mail;
  private String type;
}
```

The following update step can be used to add the `type` field to all `Contact` entities:

```java
@Extension
public class AddTypeToContactsUpdateStep implements UpdateStep {

  private final StoreUpdateStepUtilFactory updateStepUtilFactory;

  @Inject
  public AddTypeToContactsUpdateStep(StoreUpdateStepUtilFactory updateStepUtilFactory) {
    this.updateStepUtilFactory = updateStepUtilFactory;
  }

  @Override
  public void doUpdate() {
    try (MaintenanceIterator<Contact> iter = updateStepUtilFactory.forQueryableType(Contact.class).iterateAll()) {
      while(iter.hasNext()) {
        MaintenanceStoreEntry<Contact> entry = iter.next();
        Contact contact = entry.get();
        contact.setType("personal");
        entry.update(contact);
      }
    }
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "userContacts";
  }
}
```

Please note that the iterator from the `StoreUpdateStepUtilFactory` has to be closed after usage. This is done best with
a try-with-resources block like in the example above.

If the new entity differs in a significant way so that the old stored data can no longer be read from the store using
the new entity, you can use the method `entry#getAs(Class<T>)` with a class that matches the old structure of the entity
and use this to create a new entity that can be stored with the new structure.
