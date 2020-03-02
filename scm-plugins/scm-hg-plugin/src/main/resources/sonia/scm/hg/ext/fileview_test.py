from fileview import File_Viewer, SubRepository
import unittest

class DummyRevContext():

  def __init__(self, mf):
    self.mf = mf

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
