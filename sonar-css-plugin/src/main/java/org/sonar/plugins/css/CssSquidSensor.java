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

import com.google.common.collect.Lists;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.css.CssAstScanner;
import org.sonar.css.CssConfiguration;
import org.sonar.css.api.CssMetric;
import org.sonar.css.ast.visitors.SonarComponents;
import org.sonar.css.checks.CheckList;
import org.sonar.plugins.css.core.Css;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.io.File;
import java.util.Collection;

public class CssSquidSensor implements Sensor {

  private final CheckFactory checkFactory;
  private final FilePredicates predicates;

  private Project project;
  private SensorContext context;
  private AstScanner<LexerlessGrammar> scanner;
  private final SonarComponents sonarComponents;
  private final FileSystem fileSystem;

  public CssSquidSensor(SonarComponents sonarComponents, FileSystem fileSystem, CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
    this.sonarComponents = sonarComponents;
    this.fileSystem = fileSystem;
    this.predicates = fileSystem.predicates();
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(predicates.hasLanguage(Css.KEY));
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    this.project = project;
    this.context = context;

    Checks<SquidAstVisitor> checks = checkFactory.<SquidAstVisitor>create(CheckList.REPOSITORY_KEY).addAnnotatedChecks(CheckList.getChecks());
    Collection<SquidAstVisitor> checkList = checks.all();
    CssConfiguration conf = new CssConfiguration(fileSystem.encoding());
    this.scanner = CssAstScanner.create(conf, sonarComponents, checkList.toArray(new SquidAstVisitor[checkList.size()]));
    Iterable<File> sourceFiles = fileSystem.files(predicates.and(
      predicates.hasLanguage(Css.KEY),
      predicates.hasType(InputFile.Type.MAIN)));

    scanner.scanFiles(Lists.newArrayList(sourceFiles));

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(squidSourceFiles, checks);
  }

  private void save(Collection<SourceCode> squidSourceFiles, Checks<SquidAstVisitor> checks) {
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;

      InputFile sonarFile =  fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(squidFile.getKey()/*.getAbsolutePath()*/));

      saveMeasures(sonarFile, squidFile);
      saveViolations(sonarFile, squidFile, checks);
    }
  }

  private void saveMeasures(InputFile sonarFile, SourceFile squidFile) {
    context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(CssMetric.FILES));
    context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(CssMetric.LINES));
    context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(CssMetric.LINES_OF_CODE));
    context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(CssMetric.STATEMENTS));
    context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(CssMetric.COMMENT_LINES));
  }

  private void saveViolations(InputFile sonarFile, SourceFile squidFile, Checks<SquidAstVisitor> checks) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    if (messages != null) {
      for (CheckMessage message : messages) {
        RuleKey activeRule = checks.ruleKey((SquidAstVisitor) message.getCheck());
        Issuable issuable = sonarComponents.getResourcePerspectives().as(Issuable.class, sonarFile);
        Issue issue = issuable.newIssueBuilder()
          .ruleKey(RuleKey.of(activeRule.repository(), activeRule.rule()))
          .line(message.getLine())
          .message(message.formatDefaultMessage())
          .build();
        issuable.addIssue(issue);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
