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

import net.sourceforge.pmd.cpd.Tokenizer;
import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Language;
import org.sonar.plugins.css.core.Css;

public class CssCpdMapping extends AbstractCpdMapping {

  private final Css language;
  private final FileSystem fileSystem;

  public CssCpdMapping(Css language, FileSystem fileSystem) {
    this.language = language;
    this.fileSystem = fileSystem;
  }

  @Override
  public Tokenizer getTokenizer() {
    return new CssTokenizer(fileSystem.encoding());
  }

  @Override
  public Language getLanguage() {
    return language;
  }

}
