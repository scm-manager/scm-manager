package sonia.scm.event;

/**
 * This event forces the {@link ScmEventBus} to recreate the underlying implementation and to clear all its caches.
 * Note: After this event is fired, every subscription is removed from the event bus.
 */
public final class RecreateEventBusEvent {}
