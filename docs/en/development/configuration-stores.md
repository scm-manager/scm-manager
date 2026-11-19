---
title: Configuration Stores
---

Configuration stores persist one configuration object per store. The default implementation uses JAXB and caches the
stored configuration in memory.

Starting with SCM-Manager 4, `ConfigurationStore.get()` and `getOptional()` return independent copies of the stored
configuration. Change a returned object and pass it to `set()` to save the changes. Mutating a returned object alone
does not change the store. Likewise, `set()` takes an independent snapshot: later changes to its argument do not
change the stored configuration.

This isolation includes nested mutable objects and collections. It also lets the audit decorator read the original
configuration when a caller saves a modified copy, so configuration changes can be logged correctly.

## Concurrent access

`AbstractStore` serializes `get()`, `set()` and `delete()` on each store instance. The lock covers copying,
persistence and cache updates, keeping the cached snapshot consistent with the backing store. The audit decorator
also serializes each complete old-value read, auditor callback and write or deletion on its own instance, so
concurrent changes produce consecutive audit transitions. Writes to that instance wait for auditor callbacks.

Separate calls to `get()` and `set()` do not form an atomic update: two callers can read the same configuration and
later overwrite each other's changes. Callers must coordinate such updates when needed. Separate store instances
also retain independent cached snapshots; this synchronization does not provide cache invalidation between them.

## Custom store implementations

Subclasses of `sonia.scm.store.AbstractStore` must implement `protected T copy(T object)`. The method must return a
deep copy of the stored state without sharing mutable objects with the argument. `AbstractStore` handles null values
and only invokes `copy` with non-null objects. Its cached object is private.

On writes, `AbstractStore` copies the argument first, persists that snapshot, and only then replaces its cached value.
If copying or writing fails, the cached value is not replaced. Subclasses must ensure their write implementation
does not leave partially persisted changes when it fails.

The JAXB implementation copies through JAXB SAX events using the existing context, configured XML adapters and plugin
class loader. It does not require `Cloneable` or `Serializable`. Only JAXB-persisted state is copied; fields marked
`@XmlTransient` are omitted. XML adapters run during copying, so their marshal/unmarshal round trip must preserve the
stored state.

Each non-null read and write creates a deep copy. Repeated reads still use the in-memory snapshot instead of reading
the file again. Configuration objects should contain persisted settings rather than large runtime data structures.

Existing callers retain the same methods. Custom subclasses must implement `copy` and replace any direct access to
the formerly protected cached field. These changes apply to the 4.x line.

For tests, prefer `InMemoryByteConfigurationStoreFactory`, which already isolates values through serialization.
The deprecated `InMemoryConfigurationStoreFactory` retains object references and does not reproduce this behavior.
