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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import org.sonar.css.parser.CssGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.parser.ParserAdapter;
import org.w3c.dom.css.CSSRuleList;

import java.io.File;
import java.nio.charset.Charset;

public class CssTokenizer implements Tokenizer {

  private final Charset encoding;

  public CssTokenizer(Charset encoding) {
    this.encoding = encoding;
  }

  @Override
  public final void tokenize(SourceCode source, Tokens cpdTokens) {
    String fileName = source.getFileName();
    cpdTokens.add(TokenEntry.getEOF());

    Parser<LexerlessGrammar> parser = new ParserAdapter<LexerlessGrammar>(encoding, CssGrammar.createGrammar());

    try {
      String filename = source.getFileName();

      AstNode result = parser.parse(new File(filename));

      for (Token token : result.getTokens()) {
        TokenEntry cpdToken = new TokenEntry(getTokenImage(token), filename, token.getLine());
        cpdTokens.add(cpdToken);
      }
    } catch (RecognitionException e) {
      // Do nothing, the parse error is already properly reported by the Squid phase
    }

    cpdTokens.add(TokenEntry.getEOF());
  }

  private String getTokenImage(Token token) {
    if (token.getType() == GenericTokenType.LITERAL) {
      return GenericTokenType.LITERAL.getValue();
    }
    return token.getValue();
  }

}
