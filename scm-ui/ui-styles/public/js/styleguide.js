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

fetch('styleguide.html').then(resp => resp.text()).then(content => {
  document.querySelector("main").innerHTML = content;

  document.querySelectorAll("table.colors span.button").forEach((button) => {
    button.addEventListener("click", onClickColorButton);
  });
});
