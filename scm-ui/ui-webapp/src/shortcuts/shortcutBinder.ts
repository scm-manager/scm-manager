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

export default class ShortcutBinder {
  static readonly DEBOUNCE_TIMEOUT_MS = 500;
  private idCount = 0;
  private pressedKeys: Record<string, boolean> = {};
  private timer?: NodeJS.Timeout;

  register(key: string, callback: (event: KeyboardEvent) => void): number {
    const id = ++this.idCount;
    // TODO: Implement
    return id;
  }

  unregister(id: number) {
    // TODO: Implement
  }

  trigger() {
    // eslint-disable-next-line no-console
    console.log(Object.keys(this.pressedKeys));
    this.pressedKeys = {};
  }

  debounce() {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    this.timer = setTimeout(this.trigger.bind(this), ShortcutBinder.DEBOUNCE_TIMEOUT_MS);
  }

  handleKeyUp = (event: KeyboardEvent) => {
    // delete this.pressedKeys[event.key];
    // this.debounce();
    // eslint-disable-next-line no-console
    console.log(event);
  };

  handleKeyDown = (event: KeyboardEvent) => {
    // this.pressedKeys[event.code] = true;
    // this.debounce();
    // eslint-disable-next-line no-console
    console.log(event);
  };
}
