/*
 * Sonar CSS Plugin
 * Copyright (C) 2013 Tamas Kende
 * kende.tamas@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.css;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.Checks;

import org.junit.Ignore;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.css.ast.visitors.SonarComponents;
import org.sonar.plugins.css.core.Css;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CssSquidSensorTest {

  private DefaultFileSystem fileSystem = new DefaultFileSystem(new java.io.File("fake_project"));
  private FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
  private CssSquidSensor sensor = new CssSquidSensor(null, fileSystem, mock(CheckFactory.class));

  @Test
  public void should_execute_on() {
    Project project = mock(Project.class);
    CssSquidSensor cssSensor = new CssSquidSensor(mock(SonarComponents.class), fileSystem, mock(CheckFactory.class));
    assertThat(cssSensor.shouldExecuteOnProject(project)).isFalse();

    fileSystem.add(new DefaultInputFile("fake.css").setLanguage(Css.KEY));
    assertThat(cssSensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void should_analyse() {
    Project project = new Project("key");
    //addProjectFileSystem(project);
    SensorContext context = mock(SensorContext.class);

    sensor.analyse(project, context);

    verify(context).saveMeasure(Mockito.any(Resource.class), Mockito.eq(CoreMetrics.FILES), Mockito.eq(1.0));
    verify(context).saveMeasure(Mockito.any(Resource.class), Mockito.eq(CoreMetrics.LINES), Mockito.eq(34.0));
    verify(context).saveMeasure(Mockito.any(Resource.class), Mockito.eq(CoreMetrics.NCLOC), Mockito.eq(24.0));
    verify(context).saveMeasure(Mockito.any(Resource.class), Mockito.eq(CoreMetrics.STATEMENTS), Mockito.eq(18.0));
    verify(context).saveMeasure(Mockito.any(Resource.class), Mockito.eq(CoreMetrics.COMMENT_LINES), Mockito.eq(5.0));
  }

  /**
   * This is unavoidable in order to be compatible with sonarqube 4.2
   */
  /*
  private void addProjectFileSystem(Project project) {
    FileSystem fs = mock(FileSystem.class);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("src/test/resources/org/sonar/plugins/css/cssProject/css/")));

    project.setFileSystem(fs);
  }*/


}
