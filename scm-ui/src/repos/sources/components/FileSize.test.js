import FileSize from "./FileSize";

it("should format bytes", () => {
  expect(FileSize.format(0)).toBe("0 B");
  expect(FileSize.format(160)).toBe("160 B");
  expect(FileSize.format(6304)).toBe("6.16 K");
  expect(FileSize.format(28792588)).toBe("27.46 M");
  expect(FileSize.format(1369510189)).toBe("1.28 G");
  expect(FileSize.format(42949672960)).toBe("40.00 G");
});
