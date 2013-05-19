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

import analyzer.Metric;
import analyzer.MetricResult;
import java.util.List;
import analyzer.MumpsSyntaxError;
import java.util.Map;
import main.StampedeAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

/**
 * This class analyzes a MUMPS source distribution stored in the file system and
 * loads the results into Sonar.
 *
 * @author cbarthauer
 */
public final class MumpsSensor implements Sensor {

    private static final Logger LOG = LoggerFactory.getLogger(MumpsSensor.class);
    
    private final Map<Metric, org.sonar.api.measures.Metric> sonarMetricMap;    
    private final RuleFinder ruleFinder;

    /**
     * Create a MumpsSensor configured with the given RuleFinder.
     * Called by the Sonar dependency injection mechanism.
     * 
     * @param ruleFinder for looking up MUMPS code quality rules
     *   (See /sonar/rules.xml).
     * @param sonarMetricMap contains mappings between STAMPEDE metrics
     *   and Sonar metrics.
     */
    public MumpsSensor(RuleFinder ruleFinder, SonarMetricMap sonarMetricMap) {
        this.sonarMetricMap = sonarMetricMap;
        this.ruleFinder = ruleFinder;
    }
    
    @Override
    public final boolean shouldExecuteOnProject(Project project) {
        return project.getLanguage().getKey().equals(Mumps.KEY);
    }

    @Override
    public final void analyse(Project project, SensorContext context) {
        String qualifier = project.getQualifier();

        if (Qualifiers.PROJECT.equals(qualifier)) {
            analyseProject(project, context);
        } else if (Qualifiers.MODULE.equals(qualifier)) {
            analyseModule(project, context);
        }
    }

    private void analyseModule(Project project, SensorContext context) {
        List<InputFile> inputFiles = project.getFileSystem().mainFiles(Mumps.KEY);
        StampedeAnalyzer analyzer = 
                SonarMumpsAnalyzerFactory.getMumpsAnalyzer(inputFiles);
        analyzer.analyze();
        
        saveMetricResults(project, context, analyzer.metricResults());
        saveSyntaxErrors(project, context, analyzer.syntaxErrors());
        savePhysicalLinesAggregateViolations(
                project, 
                context, 
                analyzer.metricResults());
    }

    private void analyseProject(Project project, SensorContext context) {
        //Not implemented.
    }

    private void saveMetricResults(
            Project project, 
            SensorContext context, 
            List<MetricResult> metricResults) {

        for (MetricResult result : metricResults) {
            File sonarFile = File.fromIOFile(
                    new java.io.File(result.getPath()),
                    project);

            sonarFile.setEffectiveKey(
                    project.getKey() + ":" + sonarFile.getKey());
            
            for(Metric metric : result.getSupportedMetrics()) {
                context.saveMeasure(
                        sonarFile,
                        sonarMetricMap.get(metric),
                        result.getDouble(metric));
            }
        }
    }

    private void saveSyntaxErrors(
            Project project, 
            SensorContext context, 
            List<MumpsSyntaxError> syntaxErrors) {
        
        Rule rule = ruleFinder.findByKey(MumpsRuleRepository.KEY, "syntaxError");
        
        for(MumpsSyntaxError error : syntaxErrors) {
            File sonarFile = File.fromIOFile(
                    new java.io.File(error.getIdentifier()),
                    project);
            sonarFile.setEffectiveKey(
                    project.getKey() + ":" + sonarFile.getKey());
            Violation violation = Violation.create(rule, sonarFile)
                    .setLineId(error.getLine())
                    .setMessage("[line: " + error.getLine() 
                        + ", column: " + error.getCharPositionInLine() 
                        + "] " + error.getMessage());
            context.saveViolation(violation);
        }
    }

    private void savePhysicalLinesAggregateViolations(
            Project project, 
            SensorContext context, 
            List<MetricResult> metricResults) {
        
        Rule rule = ruleFinder.findByKey(
                MumpsRuleRepository.KEY, 
                "physicalLinesAggregate");
        
        for(MetricResult result : metricResults) {
            double lines = result.getDouble(Metric.LOC);
            double ncloc = result.getDouble(Metric.NCLOC);
            double commentLines = result.getDouble(Metric.COMMENT_LINES);
            
            if(!(lines == ncloc + commentLines)) {
                File sonarFile = File.fromIOFile(
                        new java.io.File(result.getPath()),
                        project);
                sonarFile.setEffectiveKey(
                        project.getKey() + ":" + sonarFile.getKey());
                Violation violation = Violation.create(rule, sonarFile)
                    .setLineId(1)
                    .setMessage(rule.getDescription());
                context.saveViolation(violation);
            }
        }
    }
}
