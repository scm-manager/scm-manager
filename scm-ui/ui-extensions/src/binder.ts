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

type Predicate<P extends Record<any, any> = Record<any, any>> = (props: P) => unknown;

type ExtensionRegistration<P extends Record<any, any>, T> = {
  predicate: Predicate<P>;
  extension: T;
  extensionName: string;
  priority: number;
};

export type ExtensionPointDefinition<N extends string, T, P = undefined> = {
  name: N;
  type: T;
  props: P;
};

export type SimpleDynamicExtensionPointDefinition<P extends string, T, Props, S extends string | undefined> =
  ExtensionPointDefinition<S extends string ? `${P}${S}` : `${P}${string}`, T, Props>;

export type BindOptions<Props extends Record<any, any>> = {
  predicate?: Predicate<Props>;

  /**
   * Extensions are ordered by name (ASC).
   */
  extensionName?: string;

  /**
   * Extensions are ordered by priority (DESC).
   */
  priority?: number;
};

function isBindOptions<Props extends Record<any, any>>(input?: string | Predicate<Props> | BindOptions<Props>): input is BindOptions<Props> {
  return typeof input !== "string" && typeof input !== "function" && typeof input === "object";
}

/**
 * Binder is responsible for binding plugin extensions to their corresponding extension points.
 * The Binder class is mainly exported for testing, plugins should only use the default export.
 */
export class Binder {
  name: string;
  extensionPoints: {
    [key: string]: Array<ExtensionRegistration<Record<any, any>, unknown>>;
  };

  constructor(name: string) {
    this.name = name;
    this.extensionPoints = {};
  }

  /**
   * Binds an extension to the extension point.
   *
   * @param extensionPoint name of extension point
   * @param extension provided extension
   */
  bind<E extends ExtensionPointDefinition<string, unknown>>(extensionPoint: E["name"], extension: E["type"]): void;
  /**
   * Binds an extension to the extension point.
   *
   * @param extensionPoint name of extension point
   * @param extension provided extension
   * @param predicate to decide if the extension gets rendered for the given props
   * @param extensionName name used for sorting alphabetically on retrieval (ASC)
   */
  bind<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    extension: E["type"],
    predicate?: Predicate<E["props"]>,
    extensionName?: string
  ): void;
  /**
   * Binds an extension to the extension point.
   *
   * @param extensionPoint name of extension point
   * @param extension provided extension
   * @param options object with additional settings
   */
  bind<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    extension: E["type"],
    options?: BindOptions<E["props"]>
  ): void;
  /**
   * Binds an extension to the extension point.
   *
   * @param extensionPoint name of extension point
   * @param extension provided extension
   * @param predicate to decide if the extension gets rendered for the given props
   * @param options object with additional settings
   */
  bind<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    extension: E["type"],
    predicate?: Predicate<E["props"]>,
    options?: BindOptions<E["props"]>
  ): void;
  bind<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    extension: E["type"],
    predicateOrOptions?: Predicate<E["props"]> | BindOptions<E["props"]>,
    extensionNameOrOptions?: string | BindOptions<E["props"]>
  ) {
    let predicate: Predicate<E["props"]> = () => true;
    let priority = 0;
    if (isBindOptions(predicateOrOptions)) {
      if (predicateOrOptions.predicate) {
        predicate = predicateOrOptions.predicate;
      }
      if (predicateOrOptions.extensionName) {
        extensionNameOrOptions = predicateOrOptions.extensionName;
      }
      if (typeof predicateOrOptions.priority === "number") {
        priority = predicateOrOptions.priority;
      }
    } else if (predicateOrOptions) {
      predicate = predicateOrOptions;
      if (isBindOptions(extensionNameOrOptions)) {
        if (typeof extensionNameOrOptions.priority === "number") {
          priority = extensionNameOrOptions.priority;
        }
        if (extensionNameOrOptions?.extensionName) {
          extensionNameOrOptions = extensionNameOrOptions.extensionName;
        } else {
          extensionNameOrOptions = undefined;
        }
      }
    }
    if (!this.extensionPoints[extensionPoint]) {
      this.extensionPoints[extensionPoint] = [];
    }
    const registration = {
      predicate,
      extension,
      extensionName: extensionNameOrOptions ? extensionNameOrOptions : "",
      priority,
    } as ExtensionRegistration<E["props"], E["type"]>;
    this.extensionPoints[extensionPoint].push(registration);
  }

  /**
   * Returns the first extension or null for the given extension point and its props.
   *
   * @param extensionPoint name of extension point
   */
  getExtension<E extends ExtensionPointDefinition<string, any>>(extensionPoint: E["name"]): E["type"] | null;
  /**
   * Returns the first extension or null for the given extension point and its props.
   *
   * @param extensionPoint name of extension point
   * @param props of the extension point
   */
  getExtension<E extends ExtensionPointDefinition<string, any, any>>(
    extensionPoint: E["name"],
    props: E["props"]
  ): E["type"] | null;
  getExtension<E extends ExtensionPointDefinition<any, unknown, any>>(
    extensionPoint: E["name"],
    props?: E["props"]
  ): E["type"] | null {
    const extensions = this.getExtensions(extensionPoint, props);
    if (extensions.length > 0) {
      return extensions[0];
    }
    return null;
  }

  /**
   * Returns all registered extensions for the given extension point and its props.
   *
   * @param extensionPoint name of extension point
   */
  getExtensions<E extends ExtensionPointDefinition<string, unknown>>(extensionPoint: E["name"]): Array<E["type"]>;
  /**
   * Returns all registered extensions for the given extension point and its props.
   *
   * @param extensionPoint name of extension point
   * @param props of the extension point
   */
  getExtensions<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    props: E["props"]
  ): Array<E["type"]>;
  getExtensions<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    props?: E["props"]
  ): Array<E["type"]> {
    let registrations = this.extensionPoints[extensionPoint] || [];
    registrations = registrations.filter((reg) => reg.predicate(props??{}));
    registrations.sort(this.sortExtensions);
    return registrations.map((reg) => reg.extension);
  }

  /**
   * Returns true if at least one extension is bound to the extension point and its props.
   */
  hasExtension<E extends ExtensionPointDefinition<string, unknown>>(extensionPoint: E["name"]): boolean;
  /**
   * Returns true if at least one extension is bound to the extension point and its props.
   */
  hasExtension<E extends ExtensionPointDefinition<string, unknown, any>>(
    extensionPoint: E["name"],
    props: E["props"]
  ): boolean;
  hasExtension<E extends ExtensionPointDefinition<any, unknown, any>>(
    extensionPoint: E["name"],
    props?: E["props"]
  ): boolean {
    return this.getExtensions(extensionPoint, props).length > 0;
  }

  /**
   * Sort extensions in ascending order, starting with entries with specified extensionName.
   */
  sortExtensions = (a: ExtensionRegistration<Record<any, any>, unknown>, b: ExtensionRegistration<Record<any, any>, unknown>) => {
    const regA = a.extensionName ? a.extensionName.toUpperCase() : "";
    const regB = b.extensionName ? b.extensionName.toUpperCase() : "";

    if (a.priority > b.priority) {
      return -1;
    } else if (a.priority < b.priority) {
      return 1;
    } else if (regA === "" && regB !== "") {
      return 1;
    } else if (regA !== "" && regB === "") {
      return -1;
    } else if (regA > regB) {
      return 1;
    } else if (regA < regB) {
      return -1;
    }
    return 0;
  };
}

// singleton binder
const binder = new Binder("default");

export default binder;
