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
package org.sonar.css.checks;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Token;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "nosonar",
  name = "\"NOSONAR\" tags should not be used to switch off issues",
  priority = Priority.INFO)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("1min")
public class NosonarTagPresenceCheck extends SquidCheck<LexerlessGrammar> implements AstAndTokenVisitor {

  private static final String PATTERN = "NOSONAR";
  private static final String MESSAGE = "Is NOSONAR used to exclude false positive or to hide real quality flaw?";

  private final CommentContainsPatternChecker checker = new CommentContainsPatternChecker(this, PATTERN, MESSAGE);

  @Override
  public void visitToken(Token token) {
    checker.visitToken(token);
  }

}
