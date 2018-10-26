// @flow
export type Description = {
  title: string,
  message: string
};

export function parseDescription(description: string): Description {
  let title = "";
  let message = "";

  if (description != null) {
    const lineBreak = description.indexOf("\n");
    if (lineBreak > 0) {
      title = description.substring(0, lineBreak);
      message = description.substring(lineBreak + 1);
    } else {
      title = description;
    }
  }

  return {
    title,
    message
  };
}
