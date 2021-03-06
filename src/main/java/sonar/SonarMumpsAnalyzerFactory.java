/**
 * Copyright (C) 2013 Chris Barthauer <mumpsstampede@gmail.com>
 *
 * This file is part of STAMPEDE.
 *
 * STAMPEDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * STAMPEDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with STAMPEDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Chris Barthauer - Initial API and implementation.
 */
package sonar;

import antlr.AntlrRoutineProcessorBuilder;
import listener.InMemoryLexerErrorListener;
import analyzer.InMemoryMetricStore;
import listener.LexerErrorListener;
import analyzer.MetricStore;
import analyzer.RoutineProcessor;
import analyzer.SourceDistribution;
import antlr.AntlrMetricListenerFactory;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import listener.InMemoryParserErrorListener;
import listener.ParserErrorListener;
import listener.PrintStreamLexerErrorListener;
import listener.PrintStreamParserErrorListener;
import main.StampedeAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.InputFile;

/**
 * A factory for creating StampedeAnalyzer objects suitable for use within
 * the Sonar MumpsSensor class.
 * 
 * @author cbarthauer
 */
final class SonarMumpsAnalyzerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SonarMumpsAnalyzerFactory.class);
    
    /**
     * Creates a StampedeAnalyzer for the give inputFiles. The StampedeAnalyzer
     * is suitable for use within the Sonar MumpsSensor.
     * 
     * @param inputFiles List of MUMPS source files from Sonar.
     * @return A properly initialized StampedeAnalyzer.
     */
    static StampedeAnalyzer getMumpsAnalyzer(List<InputFile> inputFiles) {
        final SourceDistribution distribution = 
                new SonarSourceDistribution(inputFiles);
        final LexerErrorListener lexerListener = getLexerErrorListener();
        final ParserErrorListener parserListener = getParserErrorListener();
        final AntlrRoutineProcessorBuilder builder = 
                new AntlrRoutineProcessorBuilder();
        final RoutineProcessor processor = 
            builder.setAntlrMetricListeners(AntlrMetricListenerFactory.allListeners())
                .setLexerErrorListener(lexerListener)
                .setParserErrorListener(parserListener)
                .build();
        MetricStore store = new InMemoryMetricStore();
        StampedeAnalyzer analyzer = new StampedeAnalyzer(
                distribution,
                processor,
                store);
        return analyzer;
    }
    
    private static LexerErrorListener getLexerErrorListener() {
        PrintStream out = System.out;
        
        try {
            out = new PrintStream(new java.io.File("lexer_errors.txt"));
        }
        catch(FileNotFoundException e) {
            LOG.error("Unable to create lexer_errors.log; redirecting lexer errors to System.out.", e);
        }
        
        return new PrintStreamLexerErrorListener(
                out, 
                new InMemoryLexerErrorListener());
    }

    private static ParserErrorListener getParserErrorListener() {
        PrintStream out = System.out;
        
        try {
            out = new PrintStream(new java.io.File("parser_errors.txt"));
        }
        catch(FileNotFoundException e) {
            LOG.error("Unable to create parser_errors.log; redirecting parser errors to System.out.", e);
        }
        
        return new PrintStreamParserErrorListener(
                out, 
                new InMemoryParserErrorListener());
    }
    
    private SonarMumpsAnalyzerFactory() {
        //Hide utility class constructor.
    }
}
