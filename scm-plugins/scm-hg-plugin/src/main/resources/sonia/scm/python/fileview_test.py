#
# Copyright (c) 2020 - present Cloudogu GmbH
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

from fileview import File_Viewer, File_Printer, SubRepository
import unittest

class DummyManifestEntry:
  def __init__(self, name):
    self.name = name

  def path(self):
    return self.name

  def size(self):
    return len(self.name)

class DummyRevContext():

  def __init__(self, mf):
    self.mf = mf

  def __getitem__(self, path):
    return DummyManifestEntry(path)

  def manifest(self):
    return self.mf

class File_Object_Collector():

  def __init__(self):
    self.stack = []

  def __getitem__(self, key):
    if len(self.stack) == 0 and key == 0:
      return self.last
    return self.stack[key]

  def visit(self, file):
    while len(self.stack) > 0:
      current = self.stack[-1]
      if file.get_parent() == current.path:
        current.add_child(file)
        break
      else:
        self.stack.pop()
    if file.directory:
      self.stack.append(file)
    self.last = file

class CollectingWriter:
  def __init__(self):
    self.stack = []

  def __len__(self):
    return len(self.stack)

  def __getitem__(self, key):
    return self.stack[key]

  def write(self, value):
    self.stack.append(value)

class Test_File_Viewer(unittest.TestCase):

  def test_single_file(self):
    root = self.collect(["a.txt", "b.txt"], "a.txt")
    self.assertFile(root, "a.txt")

  def test_simple(self):
    root = self.collect(["a.txt", "b.txt"])
    self.assertFile(root[0], "a.txt")
    self.assertFile(root[1], "b.txt")

  def test_recursive(self):
    root = self.collect(["a", "b", "c/d.txt", "c/e.txt", "f.txt", "c/g/h.txt"], "", True)
    self.assertChildren(root, ["c", "a", "b", "f.txt"])
    c = root[0]
    self.assertDirectory(c, "c")
    self.assertChildren(c, ["c/g", "c/d.txt", "c/e.txt"])
    g = c[0]
    self.assertDirectory(g, "c/g")
    self.assertChildren(g, ["c/g/h.txt"])

  def test_printer(self):
    paths = ["a", "b", "c/d.txt", "c/e.txt", "f.txt", "c/g/h.txt"]
    writer = self.view_with_limit_and_offset(paths, 1000, 0)
    self.assertPaths(writer, ["/", "c/", "c/g/", "c/g/h.txt", "c/d.txt", "c/e.txt", "a", "b", "f.txt"])

  def test_printer_with_limit(self):
    paths = ["a", "b", "c/d.txt", "c/e.txt", "f.txt", "c/g/h.txt"]
    writer = self.view_with_limit_and_offset(paths, 1, 0)
    self.assertPaths(writer, ["/", "c/", "c/g/", "c/g/h.txt"])

  def test_printer_with_offset(self):
    paths = ["c/g/h.txt", "c/g/i.txt", "c/d.txt", "c/e.txt", "a", "b", "f.txt"]
    writer = self.view_with_limit_and_offset(paths, 100, 1)
    self.assertPaths(writer, ["/", "c/g/i.txt", "c/d.txt", "c/e.txt", "a", "b", "f.txt"])

  def view_with_limit_and_offset(self, paths, limit, offset):
    revCtx = DummyRevContext(paths)
    collector = File_Object_Collector()

    writer = CollectingWriter()
    printer = File_Printer(writer, None, revCtx, True, False, limit, offset)

    viewer = File_Viewer(revCtx, printer)
    viewer.recursive = True
    viewer.view("")
    return writer

  def assertPath(self, actual, expected):
    path = actual[:len(expected)]
    self.assertEqual(path, expected)
    nextChar = actual[len(expected)]
    self.assertTrue(nextChar == " " or nextChar == "\n", expected + " does not match " + actual)

  def assertPaths(self, actual, expected):
    self.assertEqual(len(actual), len(expected))
    for idx,item in enumerate(actual):
      self.assertPath(item, expected[idx])

  def test_recursive_with_path(self):
    root = self.collect(["a", "b", "c/d.txt", "c/e.txt", "f.txt", "c/g/h.txt"], "c", True)
    self.assertDirectory(root, "c")
    self.assertChildren(root, ["c/g", "c/d.txt", "c/e.txt"])
    g = root[0]
    self.assertDirectory(g, "c/g")
    self.assertChildren(g, ["c/g/h.txt"])

  def test_recursive_with_deep_path(self):
    root = self.collect(["a", "b", "c/d.txt", "c/e.txt", "f.txt", "c/g/h.txt"], "c/g", True)
    self.assertDirectory(root, "c/g")
    self.assertChildren(root, ["c/g/h.txt"])

  def test_non_recursive(self):
    root = self.collect(["a.txt", "b.txt", "c/d.txt", "c/e.txt", "c/f/g.txt"])
    self.assertDirectory(root, "")
    self.assertChildren(root, ["c", "a.txt", "b.txt"])
    c = root[0]
    self.assertEmptyDirectory(c, "c")

  def test_non_recursive_with_path(self):
    root = self.collect(["a.txt", "b.txt", "c/d.txt", "c/e.txt", "c/f/g.txt"], "c")
    self.assertDirectory(root, "c")
    self.assertChildren(root, ["c/f", "c/d.txt", "c/e.txt"])
    f = root[0]
    self.assertEmptyDirectory(f, "c/f")

  def test_non_recursive_with_path_with_ending_slash(self):
    root = self.collect(["c/d.txt"], "c/")
    self.assertDirectory(root, "c")
    self.assertFile(root[0], "c/d.txt")

  def test_with_sub_directory(self):
    revCtx = DummyRevContext(["a.txt", "b/c.txt"])
    collector = File_Object_Collector()
    viewer = File_Viewer(revCtx, collector)
    sub_repositories = {}
    sub_repositories["d"] = SubRepository()
    sub_repositories["d"].url = "d"
    sub_repositories["d"].revision = "42"
    viewer.sub_repositories = sub_repositories
    viewer.view()

    d = collector[0][1]
    self.assertDirectory(d, "d")

  # https://github.com/scm-manager/scm-manager/issues/1719
  def test_with_folder_and_file_same_name(self):
    files = ["folder/ThisIsIt/ThisIsIt.sln", "folder/ThisIsIt/ThisIsIt/ThisIsIt.csproj"]
    root = self.collect(files)
    self.assertChildren(root, ["folder"])

    root = self.collect(files, "folder")
    self.assertChildren(root, ["folder/ThisIsIt"])

    root = self.collect(files, "folder/ThisIsIt")
    self.assertChildren(root, ["folder/ThisIsIt/ThisIsIt", "folder/ThisIsIt/ThisIsIt.sln"])

    root = self.collect(files, "folder/ThisIsIt/ThisIsIt")
    self.assertChildren(root, ["folder/ThisIsIt/ThisIsIt/ThisIsIt.csproj"])


  def collect(self, paths, path = "", recursive = False):
    revCtx = DummyRevContext(paths)
    collector = File_Object_Collector()

    viewer = File_Viewer(revCtx, collector)
    viewer.recursive = recursive
    viewer.view(path)

    return collector[0]

  def assertChildren(self, parent, expectedPaths):
    self.assertEqual(len(parent), len(expectedPaths))
    for idx,item in enumerate(parent.children):
      self.assertEqual(item.path, expectedPaths[idx])

  def assertFile(self, file, expectedPath):
    self.assertEquals(file.path, expectedPath)
    self.assertFalse(file.directory)

  def assertDirectory(self, file, expectedPath):
    self.assertEquals(file.path, expectedPath)
    self.assertTrue(file.directory)

  def assertEmptyDirectory(self, file, expectedPath):
    self.assertDirectory(file, expectedPath)
    self.assertTrue(len(file.children) == 0)

if __name__ == '__main__':
  unittest.main()
