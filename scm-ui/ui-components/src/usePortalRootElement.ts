import { useEffect, useState } from "react";

const createElement = (id: string) => {
  const element = document.createElement("div");
  element.setAttribute("id", id);
  return element;
};

const appendRootElement = (rootElement: HTMLElement) => {
  document.body.appendChild(rootElement);
};

const usePortalRootElement = (id: string) => {
  const [rootElement, setRootElement] = useState<HTMLElement>();
  useEffect(() => {
    let element = document.getElementById(id);
    if (!element) {
      element = createElement(id);
      appendRootElement(element);
    }
    setRootElement(element);
    return () => {
      if (element) {
        element.remove();
      }
      setRootElement(undefined);
    };
  }, [id]);

  return rootElement;
};

export default usePortalRootElement;
