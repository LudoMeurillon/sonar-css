/*
 * SonarQube CSS Plugin
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
package org.sonar.css;

import com.google.common.base.Charsets;
import com.sonar.sslr.impl.Parser;
import org.sonar.css.api.CssMetric;
import org.sonar.css.ast.visitors.SonarComponents;
import org.sonar.css.ast.visitors.SyntaxHighlighterVisitor;
import org.sonar.css.parser.CssGrammar;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.squidbridge.metrics.CommentsVisitor;
import org.sonar.squidbridge.metrics.CounterVisitor;
import org.sonar.squidbridge.metrics.LinesOfCodeVisitor;
import org.sonar.squidbridge.metrics.LinesVisitor;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.parser.ParserAdapter;

import javax.annotation.Nullable;

import java.io.File;
import java.util.Collection;

public final class CssAstScanner {

  private CssAstScanner() {
  }

  /**
   * Helper method for testing checks without having to deploy them on a Sonar instance.
   */
  public static SourceFile scanSingleFile(File file, SquidAstVisitor<LexerlessGrammar>... visitors) {
    if (!file.isFile()) {
      throw new IllegalArgumentException("File '" + file + "' not found.");
    }
    AstScanner scanner = create(new CssConfiguration(Charsets.UTF_8), null, visitors);
    scanner.scanFile(file);
    Collection<SourceCode> sources = scanner.getIndex().search(new QueryByType(SourceFile.class));
    if (sources.size() != 1) {
      throw new IllegalStateException("Only one SourceFile was expected whereas " + sources.size() + " has been returned.");
    }
    return (SourceFile) sources.iterator().next();
  }

  public static AstScanner<LexerlessGrammar> create(CssConfiguration conf, @Nullable SonarComponents sonarComponents, SquidAstVisitor<LexerlessGrammar>... visitors) {
    final SquidAstVisitorContextImpl<LexerlessGrammar> context = new SquidAstVisitorContextImpl<LexerlessGrammar>(new SourceProject("Css Project"));
    final Parser<LexerlessGrammar> parser = new ParserAdapter<LexerlessGrammar>(conf.charset(), CssGrammar.createGrammar());

    AstScanner.Builder<LexerlessGrammar> builder = AstScanner.<LexerlessGrammar>builder(context).setBaseParser(parser);

    /* Metrics */
    builder.withMetrics(CssMetric.values());

    /* Comments */
    builder.setCommentAnalyser(new CssCommentAnalyser());

    builder.withSquidAstVisitor(CommentsVisitor.<LexerlessGrammar>builder().withCommentMetric(
      CssMetric.COMMENT_LINES)
      .withNoSonar(true)
      .withIgnoreHeaderComment(conf.ignoreHeaderComments()).build());

    /* Files */
    builder.setFilesMetric(CssMetric.FILES);

    /*
     * Statements not in CSS syntax term
     * selectors and declarations at-keywords
     */
    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder()
      .setMetricDef(CssMetric.STATEMENTS)
      .subscribeTo(CssGrammar.AT_KEYWORD, CssGrammar.SELECTOR, CssGrammar.DECLARATION)
      .build());

    /* Rule sets */
    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder()
      .setMetricDef(CssMetric.RULE_SETS)
      .subscribeTo(CssGrammar.RULESET)
      .build());

    /* At rules */
    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder()
      .setMetricDef(CssMetric.AT_RULES)
      .subscribeTo(CssGrammar.AT_RULE)
      .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<LexerlessGrammar>(CssMetric.LINES));
    builder.withSquidAstVisitor(new LinesOfCodeVisitor<LexerlessGrammar>(CssMetric.LINES_OF_CODE));

    /* Syntax highlighter */
    if (sonarComponents != null) {
      builder.withSquidAstVisitor(new SyntaxHighlighterVisitor(sonarComponents, conf.charset()));
    }

    for (SquidAstVisitor<LexerlessGrammar> visitor : visitors) {
      if (visitor instanceof CharsetAwareVisitor) {
        ((CharsetAwareVisitor) visitor).setCharset(conf.charset());
      }
      builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

}
