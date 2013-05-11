/**
 * Copyright (C) 2013 Chris Barthauer <mumpsanalyzer@gmail.com>
 *
 * This file is part of MumpsAnalyzer.
 *
 * MumpsAnalyzer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MumpsAnalyzer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MumpsAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Chris Barthauer - Initial API and implementation.
 */
package antlr;

import listener.AntlrMetricListener;
import listener.LineCountListener;
import listener.NonCommentLineCounter;

/**
 * Static factory providing convenience methods for obtaining
 * collections of AntlrMetricListeners.
 * 
 * @author cbarthauer
 */
public final class AntlrMetricListenerFactory {
    
    private AntlrMetricListenerFactory() {
        //Hide utilit class constructor.
    }
    
    /**
     * Return all available AntlrMetricListeners.
     * 
     * @return Array containing all supported AntlrMetricListeners.
     */
    public static AntlrMetricListener[] allListeners() {
        return new AntlrMetricListener[] {
                new LineCountListener(),
                new NonCommentLineCounter()};
    }

}
