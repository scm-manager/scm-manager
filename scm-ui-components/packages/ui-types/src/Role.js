//@flow

export type Role = {
  name: string,
  creationDate: number | null,
  lastModified: number | null,
  type: string,
  verb: string[]
};
