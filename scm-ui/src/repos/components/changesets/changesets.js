// @flow
export type Description = {
  title: string,
  message: string
};

export function parseDescription(description?: string): Description {
  const desc = description ? description : "";
  const lineBreak = desc.indexOf("\n");

  let title;
  let message = "";

  if (lineBreak > 0) {
    title = desc.substring(0, lineBreak);
    message = desc.substring(lineBreak + 1);
  } else {
    title = desc;
  }

  return {
    title,
    message
  };
}
