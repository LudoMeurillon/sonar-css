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
package org.sonar.plugins.css.cpd;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.plugins.css.core.Css;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;

public class CssCpdMappingTest {

  private CssCpdMapping mapping;

  @Before
  public void setup() {
    DefaultFileSystem fileSystem = new DefaultFileSystem(new File("dunno"));
    fileSystem.add(new DefaultInputFile("fake.css").setLanguage(Css.KEY));
    mapping = new CssCpdMapping(
      new Css(new Settings()), fileSystem);
  }

  @Test
  public void test() {
    assertThat(mapping.getLanguage().getKey()).isEqualTo(Css.KEY);
    assertThat(mapping.getTokenizer()).isNotNull();
  }

}
