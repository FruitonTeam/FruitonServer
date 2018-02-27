package cz.cuni.mff.fruiton.component.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class ReleasesHelper {

    @Value("${releases.path}")
    private File releasesDir;

    public boolean isReleasesPathCorrect() {
        return releasesDir.exists() && releasesDir.canRead() && releasesDir.isDirectory();
    }

    public String getReleasesPath() {
        return releasesDir.toURI().toString();
    }

    public List<String> getReleases() {
        if (!isReleasesPathCorrect()) {
            return Collections.emptyList();
        }

        File[] files = releasesDir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .filter(f -> f.isFile() && f.canRead())
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }

}
