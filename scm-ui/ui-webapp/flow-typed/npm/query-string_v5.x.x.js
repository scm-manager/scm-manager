// flow-typed signature: 45d44f189fa426ca21dee3f5149a4f99
// flow-typed version: c6154227d1/query-string_v5.x.x/flow_>=v0.104.x

declare module "query-string" {
  declare type ArrayFormat = "none" | "bracket" | "index";
  declare type ParseOptions = {|
    arrayFormat?: ArrayFormat
  |};

  declare type StringifyOptions = {|
    arrayFormat?: ArrayFormat,
    encode?: boolean,
    strict?: boolean
  |};

  declare module.exports: {
    extract(str: string): string,
    parse(str: string, opts?: ParseOptions): Object,
    stringify(obj: Object, opts?: StringifyOptions): string,
    ...
  };
}
