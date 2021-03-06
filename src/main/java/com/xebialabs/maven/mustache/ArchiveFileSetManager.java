package com.xebialabs.maven.mustache;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.tar.TarBZip2Driver;
import de.schlichtherle.truezip.fs.archive.tar.TarDriver;
import de.schlichtherle.truezip.fs.archive.tar.TarGZipDriver;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.util.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ArchiveFileSetManager {

    final Logger logger = LoggerFactory.getLogger(ArchiveFileSetManager.class);

    public String[] getIncludedFiles(FileSet fileSet) {
        DirectoryScanner scanner = scan(fileSet);
        if (scanner != null) {
            return scanner.getIncludedFiles();
        }
        return new String[0];
    }

    private DirectoryScanner scan(final FileSet fileSet) {
        TConfig.get().setArchiveDetector(
                new TArchiveDetector(
                        TArchiveDetector.NULL,
                        new Object[][]{
                                {"tar", new TarDriver(IOPoolLocator.SINGLETON)},
                                {"tgz|tar.gz", new TarGZipDriver(IOPoolLocator.SINGLETON)},
                                {"tbz|tb2|tar.bz2", new TarBZip2Driver(IOPoolLocator.SINGLETON)},
                                {"jar|war|ear|zip", new ZipDriver(IOPoolLocator.SINGLETON)},
                        }
                )
        );

        File basedir = new TFile(fileSet.getDirectory());
        if (!basedir.exists()) {
            logger.debug("Directory doesn't exist: {}", basedir);
            return null; // supported by caller.
        }
        DirectoryScanner scanner = new ArchiveScanner();
        String[] includesArray = fileSet.getIncludesArray();
        String[] excludesArray = fileSet.getExcludesArray();

        if (includesArray.length > 0) {
            scanner.setIncludes(includesArray);
        }

        if (excludesArray.length > 0) {
            scanner.setExcludes(excludesArray);
        }
        scanner.setBasedir(basedir);
        scanner.scan();

        return scanner;
    }
}
