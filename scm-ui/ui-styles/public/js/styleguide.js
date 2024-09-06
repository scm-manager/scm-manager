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

const rgb2hex = (c) => "#" + c.match(/\d+/g).map((x) => (+x).toString(16).padStart(2, 0)).join``;

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
