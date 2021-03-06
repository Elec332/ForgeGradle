/*
 * ForgeGradle
 * Copyright (C) 2018 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradle.common.util;

import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModConfig extends GroovyObjectSupport {

    private transient final Project project;

    private final String name;
    private FileCollection resources;
    private FileCollection classes;

    private List<SourceSet> sources;

    public ModConfig(@Nonnull final Project project, @Nonnull final String name) {
        this.project = project;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setClasses(FileCollection classes) {
        this.classes = classes;
    }

    public void classes(@Nonnull final Object... classes) {
        setClasses(getClasses().plus(project.files(classes)));
    }

    public FileCollection getClasses() {
        if (classes == null) {
            classes = project.files();
        }

        return classes;
    }

    public void setResources(@Nonnull final FileCollection resources) {
        this.resources = resources;
    }

    public void resources(@Nonnull final Object... resources) {
        setResources(getResources().plus(project.files(resources)));
    }

    public void resource(@Nonnull final Object resource) {
        resources(resource);
    }

    public FileCollection getResources() {
        if (resources == null) {
            resources = project.files();
        }

        return resources;
    }

    public void setSources(List<SourceSet> sources) {
        this.sources = sources;
    }

    public void sources(@Nonnull final List<SourceSet> sources) {
        getSources().addAll(sources);

        sources.forEach(source -> {
            classes(source.getOutput().getClassesDirs());
            resource(source.getOutput().getResourcesDir());
        });
    }

    public void sources(@Nonnull final SourceSet... sources) {
        sources(Arrays.asList(sources));
    }

    public void source(@Nonnull final SourceSet source) {
        sources(source);
    }

    public List<SourceSet> getSources() {
        if (sources == null) {
            sources = new ArrayList<>();
        }

        return sources;
    }

    public void merge(@Nonnull final ModConfig other, boolean overwrite) {
        if (overwrite) {
            sources = other.sources == null ? sources : other.sources;
            classes = other.classes == null ? classes : other.classes;
            resources = other.resources == null ? resources : other.resources;
        } else {
            if (other.resources != null) {
                resources(other.getResources());
            }

            if (other.classes != null) {
                classes(other.getClasses());
            }

            if (other.sources != null) {
                sources(other.getSources());
            }
        }
    }

    public void configureTokens(@Nonnull final Map<String, String> tokens) {
        final SourceSet main = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        Stream<String> modClasses = Stream.concat((resources == null ? Stream.of(main.getOutput().getResourcesDir()) : resources.getFiles().stream()),
                (classes == null ? main.getOutput().getClassesDirs().getFiles() : classes.getFiles()).stream())
                .distinct()
                .map(file -> getName() + "%%" + file.getAbsolutePath());

        if (tokens.containsKey("source_roots")) {
            modClasses = Stream.concat(Arrays.stream(tokens.get("source_roots").split(File.pathSeparator)), modClasses);
        }

        tokens.put("source_roots", modClasses.distinct().collect(Collectors.joining(File.pathSeparator)));
    }

}
