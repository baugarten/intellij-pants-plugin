// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.jps.incremental.model;

import com.intellij.util.containers.ContainerUtil;
import com.twitter.intellij.pants.jps.incremental.serialization.PantsJpsProjectExtensionSerializer;
import com.twitter.intellij.pants.jps.util.PantsJpsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PantsBuildTargetType extends BuildTargetType<PantsBuildTarget> {
  public static PantsBuildTargetType INSTANCE = new PantsBuildTargetType();

  protected PantsBuildTargetType() {
    super("pants-goal-compile");
  }

  @NotNull
  @Override
  public List<PantsBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    return ContainerUtil.createMaybeSingletonList(getTarget(model));
  }

  @Nullable
  public PantsBuildTarget getTarget(JpsModel model) {
    final JpsProject jpsProject = model.getProject();
    final JpsPantsProjectExtension pantsProjectExtension = PantsJpsProjectExtensionSerializer.findPantsProjectExtension(jpsProject);
    final boolean compileWithPants = pantsProjectExtension != null && !pantsProjectExtension.isCompileWithIntellij();

    final Set<String> allTargetAddresses = new HashSet<String>();
    for (JpsPantsModuleExtension moduleExtension : PantsJpsUtil.findPantsModules(jpsProject.getModules())) {
      allTargetAddresses.addAll(moduleExtension.getTargetAddresses());
    }

    return compileWithPants && PantsJpsUtil.containsPantsModules(jpsProject.getModules()) ?
           new PantsBuildTarget(pantsProjectExtension.getExternalProjectPath(), new HashSet<String>(allTargetAddresses)) : null;
  }

  @NotNull
  @Override
  public BuildTargetLoader<PantsBuildTarget> createLoader(@NotNull final JpsModel model) {
    return new BuildTargetLoader<PantsBuildTarget>() {
      @Nullable
      @Override
      public PantsBuildTarget createTarget(@NotNull String targetId) {
        return getTarget(model);
      }
    };
  }
}
