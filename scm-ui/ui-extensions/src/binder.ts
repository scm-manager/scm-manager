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

type Predicate = (props: any) => boolean;

type ExtensionRegistration = {
  predicate: Predicate;
  extension: any;
  extensionName: string;
};

/**
 * Binder is responsible for binding plugin extensions to their corresponding extension points.
 * The Binder class is mainly exported for testing, plugins should only use the default export.
 */
export class Binder {
  name: string;
  extensionPoints: {
    [key: string]: Array<ExtensionRegistration>;
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
   * @param predicate to decide if the extension gets rendered for the given props
   */
  bind(extensionPoint: string, extension: any, predicate?: Predicate, extensionName?: string) {
    if (!this.extensionPoints[extensionPoint]) {
      this.extensionPoints[extensionPoint] = [];
    }
    const registration = {
      predicate: predicate ? predicate : () => true,
      extension,
      extensionName: extensionName ? extensionName : ""
    };
    this.extensionPoints[extensionPoint].push(registration);
  }

  /**
   * Returns the first extension or null for the given extension point and its props.
   *
   * @param extensionPoint name of extension point
   * @param props of the extension point
   */
  getExtension(extensionPoint: string, props?: object) {
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
   * @param props of the extension point
   */
  getExtensions(extensionPoint: string, props?: object): Array<any> {
    let registrations = this.extensionPoints[extensionPoint] || [];
    if (props) {
      registrations = registrations.filter(reg => reg.predicate(props || {}));
    }
    registrations.sort(this.sortExtensions);
    return registrations.map(reg => reg.extension);
  }

  /**
   * Returns true if at least one extension is bound to the extension point and its props.
   */
  hasExtension(extensionPoint: string, props?: object): boolean {
    return this.getExtensions(extensionPoint, props).length > 0;
  }

  /**
   * Sort extensions in ascending order, starting with entries with specified extensionName.
   */
  sortExtensions = (a: ExtensionRegistration, b: ExtensionRegistration) => {
    const regA = a.extensionName ? a.extensionName.toUpperCase() : "";
    const regB = b.extensionName ? b.extensionName.toUpperCase() : "";

    if (regA === "" && regB !== "") {
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
