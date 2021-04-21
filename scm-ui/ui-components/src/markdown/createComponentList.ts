import {
  createRemark2RehypeCodeRendererAdapter,
  createRemark2RehypeHeadingRendererAdapterFactory,
  createRemark2RehypeLinkRendererAdapter
} from "./remarkToRehypeRendererAdapters";

export type CreateComponentListOptions = {
  permalink?: string;
};

export const createComponentList = (
  remarkRendererList: Record<string, any>,
  { permalink }: CreateComponentListOptions
) => {
  const components: Record<string, any> = {};

  if (remarkRendererList.code) {
    components.code = createRemark2RehypeCodeRendererAdapter(remarkRendererList.code);
  }

  if (remarkRendererList.link) {
    components.a = createRemark2RehypeLinkRendererAdapter(remarkRendererList.link);
  }

  if (remarkRendererList.heading) {
    const createHeadingRendererAdapter = createRemark2RehypeHeadingRendererAdapterFactory(
      remarkRendererList.heading,
      permalink
    );
    components.h1 = createHeadingRendererAdapter(1);
    components.h2 = createHeadingRendererAdapter(2);
    components.h3 = createHeadingRendererAdapter(3);
    components.h4 = createHeadingRendererAdapter(4);
    components.h5 = createHeadingRendererAdapter(5);
    components.h6 = createHeadingRendererAdapter(6);
  }

  if (remarkRendererList.break) {
    components.br = remarkRendererList.break;
  }

  if (remarkRendererList.delete) {
    components.del = remarkRendererList.delete;
  }

  if (remarkRendererList.emphasis) {
    components.em = remarkRendererList.emphasis;
  }

  if (remarkRendererList.blockquote) {
    components.blockquote = remarkRendererList.blockquote;
  }

  if (remarkRendererList.image) {
    components.img = remarkRendererList.image;
  }

  if (remarkRendererList.list) {
    components.ol = remarkRendererList.list;
    components.ul = remarkRendererList.list;
  }

  if (remarkRendererList.listItem) {
    components.li = remarkRendererList.listItem;
  }

  if (remarkRendererList.paragraph) {
    components.p = remarkRendererList.paragraph;
  }

  if (remarkRendererList.strong) {
    components.strong = remarkRendererList.strong;
  }

  if (remarkRendererList.table) {
    components.table = remarkRendererList.table;
  }

  if (remarkRendererList.tableHead) {
    components.thead = remarkRendererList.tableHead;
  }

  if (remarkRendererList.tableBody) {
    components.tbody = remarkRendererList.tableBody;
  }

  if (remarkRendererList.tableRow) {
    components.tr = remarkRendererList.tableRow;
  }

  if (remarkRendererList.tableCell) {
    components.td = remarkRendererList.tableCell;
    components.th = remarkRendererList.tableCell;
  }

  if (remarkRendererList.thematicBreak) {
    components.hr = remarkRendererList.thematicBreak;
  }

  return components;
};
