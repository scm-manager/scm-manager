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
const rgb2hex = (c) => `#${c.match(/\d+/g).map((x) => (+x).toString(16).padStart(2, 0)).join``}`;

function onClickColorButton(e) {
  const button = e.target;

  const cell = button.parentElement;

  const div = cell.querySelector("div.color-text");
  if (div) {
    div.remove();
  } else {
    let color = window.getComputedStyle(button).backgroundColor;
    color = rgb2hex(color);

    const colorText = document.createElement("div");
    colorText.className = "color-text";
    colorText.innerText = color;

    cell.appendChild(colorText);
  }
}

document.querySelectorAll("table.colors span.button").forEach((button) => {
  button.addEventListener("click", onClickColorButton);
});
