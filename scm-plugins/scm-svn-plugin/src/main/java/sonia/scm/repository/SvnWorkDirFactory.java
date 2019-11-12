package sonia.scm.repository;

import sonia.scm.repository.spi.SvnContext;
import sonia.scm.repository.util.WorkdirFactory;

import java.io.File;

public interface SvnWorkDirFactory extends WorkdirFactory<File, File, SvnContext> {
}
